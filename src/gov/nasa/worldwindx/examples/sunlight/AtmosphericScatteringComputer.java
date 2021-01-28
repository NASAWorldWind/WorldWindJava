package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.geom.*;

import java.awt.*;

/**
 * Computes the color of the atmosphere according to the Sun position, the eye position and a direction.
 * <p>
 * Based on code from Sean O'Neil "Real-Time Atmospheric Scattering" Gamedev article and C++ demo.<br />
 * http://www.gamedev.net/reference/articles/article2093.asp<br />
 * http://sponeil.net
 *
 * @author Patrick Murris
 * @version $Id: AtmosphericScatteringComputer.java 10406 2009-04-22 18:28:45Z patrickmurris $
 */
public class AtmosphericScatteringComputer
{
    private float fInnerRadius;
    private float fOuterRadius;
    private float fScale;

    private int nSamples = 4;       // Number of sample rays to use in integral equation
    private float Kr = 0.001f; //0.0025f;		// Rayleigh scattering constant
    private float Kr4PI = Kr * 4.0f * (float)Math.PI;
    private float Km = 0.0015f;		// Mie scattering constant
    private float Km4PI = Km * 4.0f * (float)Math.PI;
    private float ESun = 15.0f;		// Sun brightness constant
    private float g = -0.85f;		// The Mie phase asymmetry factor
    private float fRayleighScaleDepth = 0.25f;
    private float fMieScaleDepth = 0.1f;
    private float[] fWavelength = new float[] {0.650f, 0.570f, 0.475f}; // 650nm red, 570nm green, 475nm blue
    private float[] fWavelength4 = new float[3];

    private float[] fCameraDepth = new float[] { 0, 0, 0, 0 };
    private float[] fLightDepth = new float[4];
    private float[] fSampleDepth = new float[4];
    private float[] fRayleighSum = new float[] { 0, 0, 0 };
    private float[] fMieSum = new float[] { 0, 0, 0 };
    private float[] fAttenuation = new float[3];
    
    // Optical depth buffer
    private float[] opticalDepthBuffer;
    private float DELTA = 1e-6f;
    private int nChannels = 4;
    private int nBufferWidth = 128;
    private int nBufferHeight = 128;

    private Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    public AtmosphericScatteringComputer(double globeRadius, double thickness)
    {
        // Init
        fWavelength4[0] = (float)Math.pow(fWavelength[0], 4.0f);
        fWavelength4[1] = (float)Math.pow(fWavelength[1], 4.0f);
        fWavelength4[2] = (float)Math.pow(fWavelength[2], 4.0f);

        fInnerRadius = (float)globeRadius;
        fOuterRadius = (float)(globeRadius + thickness);
        fScale = 1.0f / (fOuterRadius - fInnerRadius);
        
        // Init optical depth buffer
        this.computeOpticalDepthBuffer();
    }

    public Color getAtmosphereColor(Vec4 lookAtPoint, Vec4 eyePoint, Vec4 lightDirection)
    {
        // Find out intersection point on world scattering sphere
        Vec4 vRay = lookAtPoint.subtract3(eyePoint);
        vRay = vRay.normalize3();

        // Calculate the closest intersection of the ray with the outer atmosphere
        float B = 2.0f * (float)eyePoint.dot3(vRay);
        float C = (float)(eyePoint.dotSelf3() - fOuterRadius * fOuterRadius);
        float fDet = B * B - 4.0f * C;

        Color color = TRANSPARENT_COLOR;
        if (fDet >= 0)
        {
            // Camera ray intersect atmosphere
            float fNear1 = 0.5f * (-B - (float)Math.sqrt(fDet));
            float fNear2 = 0.5f * (-B + (float)Math.sqrt(fDet));
            if (fNear1 >= 0 || fNear2 >= 0)
            {
                // largest distance - not sure why...
                float fNear = Math.max(fNear1, fNear2);
                Vec4 vPos = eyePoint.add3(vRay.multiply3(fNear));
                color = getColorForVertex(vPos, eyePoint, lightDirection);
            }
        }
        return color;
    }

    private Color getColorForVertex(Vec4 vPos, Vec4 vCamera, Vec4 vLightDirection)
    {
        // Get the ray from the camera to the vertex, and its length (which is the far point of the ray
        // passing through the atmosphere)
        Vec4 vRay = vPos.subtract3(vCamera);
        float fFar = (float)vRay.getLength3();
        vRay = vRay.normalize3();

        // Calculate the closest intersection of the ray with the outer atmosphere (which is the near point
        // of the ray passing through the atmosphere)
        float B = 2.0f * (float)vCamera.dot3(vRay);
        float C = (float)(vCamera.dotSelf3() - fOuterRadius * fOuterRadius);
        float fDet = Math.max(0.0f, B * B - 4.0f * C);
        float fNear = 0.5f * (-B - (float)Math.sqrt(fDet));
        boolean bCameraAbove = true;

        for (int i = 0; i < fCameraDepth.length; i++)
            fCameraDepth[i] = 0;

        for (int i = 0; i < fLightDepth.length; i++)
            fLightDepth[i] = 0;

        for (int i = 0; i < fSampleDepth.length; i++)
            fSampleDepth[i] = 0;

        if (fNear <= 0)
        {
            // If the near point is behind the camera, it means the camera is inside the atmosphere
            fNear = 0;
            float fCameraHeight = (float)vCamera.getLength3();
            float fCameraAltitude = (fCameraHeight - fInnerRadius) * fScale;
            bCameraAbove = fCameraHeight >= vPos.getLength3();
            float fCameraAngle = (float)(bCameraAbove ? vRay.getNegative3().dot3(vCamera) : vRay.dot3(vRay)) / fCameraHeight;
            interpolate(fCameraDepth, fCameraAltitude, 0.5f - fCameraAngle * 0.5f);
        }
        else
        {
            // Otherwise, move the camera up to the near intersection point
            vCamera = vCamera.add3(vRay.multiply3(fNear));
            fFar -= fNear;
            fNear = 0;
        }

        // If the distance between the points on the ray is negligible, don't bother to calculate anything
        if (fFar <= DELTA)
        {
            return TRANSPARENT_COLOR;
        }

        // Initialize a few variables to use inside the loop
        for (int i = 0; i < fRayleighSum.length; i++)
            fRayleighSum[i] = 0;
        for (int i = 0; i < fMieSum.length; i++)
            fMieSum[i] = 0;

        float fSampleLength = fFar / nSamples;
        float fScaledLength = fSampleLength * fScale;
        Vec4 vSampleRay = vRay.multiply3(fSampleLength);

        // Start at the center of the first sample ray, and loop through each of the others
        vPos = vCamera.add3(vSampleRay.multiply3(0.5f));
        for (int i = 0; i < nSamples; i++)
        {
            float fHeight = (float)vPos.getLength3();

            // Start by looking up the optical depth coming from the light source to this point
            float fLightAngle = (float)vLightDirection.dot3(vPos) / fHeight;
            float fAltitude = (fHeight - fInnerRadius) * fScale;
            interpolate(fLightDepth, fAltitude, 0.5f - fLightAngle * 0.5f);

            // If no light light reaches this part of the atmosphere, no light is scattered in at this point
            if (fLightDepth[0] > DELTA)
            {


                // Get the density at this point, along with the optical depth from the light source to this point
                float fRayleighDensity = fScaledLength * fLightDepth[0];
                float fRayleighDepth = fLightDepth[1];
                float fMieDensity = fScaledLength * fLightDepth[2];
                float fMieDepth = fLightDepth[3];

                // If the camera is above the point we're shading, we calculate the optical depth from the sample point to the camera
                // Otherwise, we calculate the optical depth from the camera to the sample point
                if (bCameraAbove)
                {
                    float fSampleAngle = (float)vRay.getNegative3().dot3(vPos) / fHeight;
                    interpolate(fSampleDepth, fAltitude, 0.5f - fSampleAngle * 0.5f);
                    fRayleighDepth += fSampleDepth[1] - fCameraDepth[1];
                    fMieDepth += fSampleDepth[3] - fCameraDepth[3];
                }
                else
                {
                    float fSampleAngle = (float)vRay.dot3(vPos) / fHeight;
                    interpolate(fSampleDepth, fAltitude, 0.5f - fSampleAngle * 0.5f);
                    fRayleighDepth += fCameraDepth[1] - fSampleDepth[1];
                    fMieDepth += fCameraDepth[3] - fSampleDepth[3];
                }

                // Now multiply the optical depth by the attenuation factor for the sample ray
                fRayleighDepth *= Kr4PI;
                fMieDepth *= Km4PI;

                // Calculate the attenuation factor for the sample ray
                fAttenuation[0] = (float)Math.exp(-fRayleighDepth / fWavelength4[0] - fMieDepth);
                fAttenuation[1] = (float)Math.exp(-fRayleighDepth / fWavelength4[1] - fMieDepth);
                fAttenuation[2] = (float)Math.exp(-fRayleighDepth / fWavelength4[2] - fMieDepth);

                fRayleighSum[0] += fRayleighDensity * fAttenuation[0];
                fRayleighSum[1] += fRayleighDensity * fAttenuation[1];
                fRayleighSum[2] += fRayleighDensity * fAttenuation[2];

                fMieSum[0] += fMieDensity * fAttenuation[0];
                fMieSum[1] += fMieDensity * fAttenuation[1];
                fMieSum[2] += fMieDensity * fAttenuation[2];
            }
            // Move the position to the center of the next sample ray
            vPos = vPos.add3(vSampleRay);
        }

        // Calculate the angle and phase values (this block of code could be handled by a small 1D lookup table,
        // or a 1D texture lookup in a pixel shader)
        float fAngle = (float)vRay.getNegative3().dot3(vLightDirection);
        float[] fPhase = new float[2];
        float fAngle2 = fAngle * fAngle;
        float g2 = g * g;
        fPhase[0] = 0.75f * (1.0f + fAngle2);
        fPhase[1] = 1.5f * ((1 - g2) / (2 + g2)) * (1.0f + fAngle2) / (float)Math.pow(1 + g2 - 2 * g * fAngle, 1.5f);
        fPhase[0] *= Kr * ESun;
        fPhase[1] *= Km * ESun;
        // Calculate the in-scattering color and clamp it to the max color value
        float[] fColor = new float[] { 0, 0, 0 };
        fColor[0] = fRayleighSum[0] * fPhase[0] / fWavelength4[0] + fMieSum[0] * fPhase[1];
        fColor[1] = fRayleighSum[1] * fPhase[0] / fWavelength4[1] + fMieSum[1] * fPhase[1];
        fColor[2] = fRayleighSum[2] * fPhase[0] / fWavelength4[2] + fMieSum[2] * fPhase[1];
        fColor[0] = Math.min(fColor[0], 1.0f);
        fColor[1] = Math.min(fColor[1], 1.0f);
        fColor[2] = Math.min(fColor[2], 1.0f);

        // Compute alpha transparency (PM 2006-11-19)
        float alpha = (fColor[0] + fColor[1] + fColor[2]) / 3;  // Average luminosity
        alpha = (float)Math.min(alpha + 0.50, 1f);			  // increase opacity

        // Last but not least, return the color
        return  new Color(fColor[0], fColor[1], fColor[2], alpha);
    }

    private void interpolate(float[] p, float x, float y)
    {
        float fX = x * (nBufferWidth - 1);
        float fY = y * (nBufferHeight - 1);
        int nX = Math.min(nBufferWidth - 2, Math.max(0, (int)fX));
        int nY = Math.min(nBufferHeight - 2, Math.max(0, (int)fY));
        float fRatioX = fX - nX;
        float fRatioY = fY - nY;

        int pValueOffset = (nBufferWidth * nY + nX) * 4;

        for (int i = 0; i < nChannels; i++)
        {
            p[i] = opticalDepthBuffer[pValueOffset] * (1 - fRatioX) * (1 - fRatioY) +
                opticalDepthBuffer[pValueOffset + nChannels] * (fRatioX) * (1 - fRatioY) +
                opticalDepthBuffer[pValueOffset + nChannels * nBufferWidth] * (1 - fRatioX) * (fRatioY) +
                opticalDepthBuffer[pValueOffset + nChannels * (nBufferWidth + 1)] * (fRatioX) * (fRatioY);
            pValueOffset++;
        }
    }

    private void computeOpticalDepthBuffer()
    {
        int nSize = 128;
        int nBufferSamples = 50;

        if (opticalDepthBuffer == null)
            opticalDepthBuffer = new float[nSize * nSize * 4];

        int nIndex = 0;
        for (int nAngle = 0; nAngle < nSize; nAngle++)
        {
            // angle goes from 0 to 180 degrees
            float fCos = 1.0f - (nAngle + nAngle) / (float)nSize;
            float fAngle = (float)Math.acos(fCos);

            Vec4 vRay = new Vec4((float)Math.sin(fAngle), (float)Math.cos(fAngle), 0);	// Ray pointing to the viewpoint
            for (int nHeight = 0; nHeight < nSize; nHeight++)
            {
                // from the bottom of the atmosphere to the top
                float fHeight = DELTA + fInnerRadius + ((fOuterRadius - fInnerRadius) * nHeight) / nSize;
                Vec4 vPos = new Vec4(0, fHeight, 0);				// The position of the camera

                // If the ray from vPos heading in the vRay direction intersects the inner radius (i.e. the planet),
                // then this spot is not visible from the viewpoint
                float B = 2.0f * (float)vPos.dot3(vRay);
                float Bsq = B * B;
                float Cpart = (float)vPos.dotSelf3();
                float C = Cpart - fInnerRadius * fInnerRadius;
                float fDet = Bsq - 4.0f * C;
                boolean bVisible = (fDet < 0 ||
                    (0.5f * (-B - (float)Math.sqrt(fDet)) <= 0) && (0.5f * (-B + (float)Math.sqrt(fDet)) <= 0));
                float fRayleighDensityRatio;
                float fMieDensityRatio;
                if (bVisible)
                {
                    fRayleighDensityRatio = (float)Math.exp(-(fHeight - fInnerRadius) * fScale / fRayleighScaleDepth);
                    fMieDensityRatio = (float)Math.exp(-(fHeight - fInnerRadius) * fScale / fMieScaleDepth);
                }
                else
                {
                    // Smooth the transition from light to shadow
                    fRayleighDensityRatio = opticalDepthBuffer[nIndex - nSize * nChannels] * 0.75f;
                    fMieDensityRatio = opticalDepthBuffer[nIndex + 2 - nSize * nChannels] * 0.75f;

                }

                // Determine where the ray intersects the outer radius (the top of the atmosphere)
                // This is the end of our ray for determining the optical depth (vPos is the start)
                C = Cpart - fOuterRadius * fOuterRadius;
                fDet = Bsq - 4.0f * C;
                float fFar = 0.5f * (-B + (float)Math.sqrt(fDet));

                // Next determine the length of each sample, scale the sample ray, and make sure position
                // checks are at the center of a sample ray
                float fSampleLength = fFar / nBufferSamples;
                float fScaledLength = fSampleLength * fScale;
                Vec4 vSampleRay = vRay.multiply3(fSampleLength);
                vPos = vPos.add3(vSampleRay.multiply3(0.5f));

                // Iterate through the samples to sum up the optical depth for the distance the ray travels
                // through the atmosphere
                float fRayleighDepth = 0;
                float fMieDepth = 0;
                for (int i = 0; i < nBufferSamples; i++)
                {
                    fHeight = (float)vPos.getLength3();
                    float fAltitude = (fHeight - fInnerRadius) * fScale;
                    fAltitude = Math.max(fAltitude, 0.0f);
                    fRayleighDepth += (float)Math.exp(-fAltitude / fRayleighScaleDepth);
                    fMieDepth += (float)Math.exp(-fAltitude / fMieScaleDepth);
                    vPos = vPos.add3(vSampleRay);
                }

                // Multiply the sums by the length the ray traveled
                fRayleighDepth *= fScaledLength;
                fMieDepth *= fScaledLength;

                // Store the results for Rayleigh to the light source, Rayleigh to the camera, Mie to the
                // light source, and Mie to the camera
                opticalDepthBuffer[nIndex++] = fRayleighDensityRatio;
                opticalDepthBuffer[nIndex++] = fRayleighDepth;
                opticalDepthBuffer[nIndex++] = fMieDensityRatio;
                opticalDepthBuffer[nIndex++] = fMieDepth;

            } // height

        } // angle
    }

}
