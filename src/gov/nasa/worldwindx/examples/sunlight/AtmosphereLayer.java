package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;
import com.jogamp.opengl.*;

import java.awt.*;

/**
 * Renders an atmosphere around the globe and a sky dome at low altitude. Uses atmospheric scattering as color source.
 * <p>
 * Issue : Ellipsoidal globe doesnt match the spherical atmosphere everywhere.
 * <p>
 * TODO: Find a way to get a blue sky at ground level TODO: Increase dome geometry resolution and implement partial
 * sphere
 *
 * @author Patrick Murris
 * @version $Id: AtmosphereLayer.java 13704 2010-09-03 07:16:58Z tgaskins $
 */
public class AtmosphereLayer extends AbstractLayer {

    protected final static int STACKS = 24;
    protected final static int SLICES = 64;

    protected int glListId = -1;        // GL list id
    protected double thickness = 60e3; // Atmosphere thickness
    protected double lastRebuildHorizon = 0;

    protected AtmosphericScatteringComputer asc;
    protected Vec4 sunDirection;
    protected boolean update = true;

    /**
     * Renders an atmosphere around the globe
     */
    public AtmosphereLayer() {
    }

    /**
     * Get the atmosphere thickness in meter
     *
     * @return the atmosphere thickness in meter
     */
    public double getAtmosphereThickness() {
        return this.thickness;
    }

    /**
     * Set the atmosphere thickness in meter
     *
     * @param thickness the atmosphere thickness in meter
     */
    public void setAtmosphereThickness(double thickness) {
        if (thickness < 0) {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.thickness = thickness;
        this.asc = null; // invalidate atmospheric scattering computer
        this.update = true;
    }

    public Vec4 getSunDirection() {
        return this.sunDirection;
    }

    public void setSunDirection(Vec4 direction) {
        this.sunDirection = direction;
        this.update = true;
    }

    @Override
    public void doRender(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try {
            View view = dc.getView();
            Position camPos = dc.getGlobe().computePositionFromPoint(view.getEyePoint());
            double worldRadius = dc.getGlobe().getRadiusAt(camPos);
            double distToCenterOfPlanet = view.getEyePoint().getLength3();
            double camAlt = camPos.getElevation();
            double tangentalDistance = view.getFarClipDistance();
            // Dome radius
            double domeRadius = tangentalDistance;

            // horizon latitude degrees
            double horizonLat = (-Math.PI / 2 + Math.acos(tangentalDistance / distToCenterOfPlanet))
                    * 180 / Math.PI;
            // zenith latitude degrees
            double zenithLat = 90;
            if (camAlt >= thickness) {
                double tangentalDistanceZenith = Math.sqrt(distToCenterOfPlanet * distToCenterOfPlanet
                        - (worldRadius + thickness) * (worldRadius + thickness));
                zenithLat = (-Math.PI / 2 + Math.acos(tangentalDistanceZenith / distToCenterOfPlanet)) * 180 / Math.PI;
            }
            if (camAlt < thickness && camAlt > thickness * 0.7) {
                zenithLat = (thickness - camAlt) / (thickness - thickness * 0.7) * 90;
            }

            // Build or rebuild sky dome if horizon distance changed more then 100m
            if (this.update || this.glListId == -1 || Math.abs(this.lastRebuildHorizon - tangentalDistance) > 100) {
                if (this.glListId != -1) {
                    gl.glDeleteLists(this.glListId, 1);
                }

                this.makeSkyDome(dc, (float) (domeRadius), horizonLat, zenithLat, SLICES, STACKS);
                this.lastRebuildHorizon = tangentalDistance;
                this.update = false;
            }

            // GL set up
            gl.glPushAttrib(GL2.GL_POLYGON_BIT); // Temporary hack around aliased sky.
            gl.glPopAttrib();

            gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL2.GL_TRANSFORM_BIT
                    | GL2.GL_POLYGON_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_ENABLE_BIT
                    | GL2.GL_CURRENT_BIT);
            attribsPushed = true;
            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            Matrix projection = Matrix.fromPerspective(view.getFieldOfView(),
                    view.getViewport().getWidth(), view.getViewport().getHeight(),
                    10e3, 2 * distToCenterOfPlanet + 10e3);
            double[] matrixArray = new double[16];
            projection.toArray(matrixArray, 0, false);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadMatrixd(matrixArray, 0);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            // Sky transform
            Matrix skyTransform = computeSkyTransform(dc);
            Matrix modelView = view.getModelviewMatrix().multiply(skyTransform);
            modelView.toArray(matrixArray, 0, false);
            gl.glLoadMatrixd(matrixArray, 0);
            // Draw sky
            if (this.glListId != -1) {
                gl.glCallList(this.glListId);
            }

        } finally {
            // Restore GL state
            if (modelviewPushed) {
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (projectionPushed) {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (attribsPushed) {
                gl.glPopAttrib();
            }
        }
    }

    /**
     * Build sky dome and draw into a glList
     *
     * @param dc the current DrawContext
     * @param radius the sky dome radius in meters.
     * @param startLat the horizon latitude in decimal degrees.
     * @param endLat the zenith latitude in decimal degrees.
     * @param slices the number of longitude divisions used for the dome geometry.
     * @param stacks the number of latitude divisions used for the dome geometry.
     */
    protected void makeSkyDome(DrawContext dc, float radius, double startLat, double endLat,
            int slices, int stacks) {
        if (this.sunDirection == null) {
            return;
        }

        GL2 gl = dc.getGL().getGL2();
        this.glListId = gl.glGenLists(1);
        gl.glNewList(this.glListId, GL2.GL_COMPILE);
        this.drawSkyGradient(dc, radius, startLat, endLat, slices, stacks);
        gl.glEndList();
    }

    /**
     * Draws the sky dome
     *
     * @param dc the current DrawContext
     * @param radius the sky dome radius
     * @param startLat the horizon latitude
     * @param endLat the zenith latitude
     * @param slices the number of slices - vertical divisions
     * @param stacks the nuber os stacks - horizontal divisions
     */
    protected void drawSkyGradient(DrawContext dc, float radius, double startLat, double endLat,
            int slices, int stacks) {
        // Init atmospheric scattering computer
        if (this.asc == null) {
            this.asc = new AtmosphericScatteringComputer(dc.getGlobe().getRadius(), this.thickness);
        }

        // Get sky dome transform
        Matrix skyTransform = computeSkyTransform(dc);

        // GL setup
        GL2 gl = dc.getGL().getGL2();
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);
        gl.glDisable(GL.GL_TEXTURE_2D);
        //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);    // wireframe

        double latitude, longitude, latitudeTop = endLat;
        double linear, linearTop, k, kTop;
        Color color;
        Color[] stackColors = new Color[slices + 1];
        Vec4 eyePoint = dc.getView().getEyePoint();

        // bottom fade
        latitude = startLat - Math.max((endLat - startLat) / 4, 2);
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int slice = 0; slice <= slices; slice++) {
            longitude = 180 - ((float) slice / slices * (float) 360);
            Vec4 v1 = SphericalToCartesian(latitude, longitude, radius);
            Vec4 v2 = SphericalToCartesian(startLat, longitude, radius);
            color = this.asc.getAtmosphereColor(v2.transformBy4(skyTransform), eyePoint, this.sunDirection);
            gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0f);
            gl.glVertex3d(v1.getX(), v1.getY(), v1.getZ());
            gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            gl.glVertex3d(v2.getX(), v2.getY(), v2.getZ());
            stackColors[slice] = color;
        }
        gl.glEnd();

        // stacks and slices
        for (int stack = 1; stack < stacks - 1; stack++) {
            // bottom vertex
            linear = (float) (stack - 1) / (stacks - 1f);
            k = 1 - Math.cos(linear * Math.PI / 2);
            latitude = startLat + Math.pow(k, 3) * (endLat - startLat);
            // top vertex
            linearTop = (float) (stack) / (stacks - 1f);
            kTop = 1 - Math.cos(linearTop * Math.PI / 2);
            latitudeTop = startLat + Math.pow(kTop, 3) * (endLat - startLat);
            // Draw stack
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int slice = 0; slice <= slices; slice++) {
                longitude = 180 - ((float) slice / slices * (float) 360);
                Vec4 v = SphericalToCartesian(latitude, longitude, radius);
                color = stackColors[slice];
                gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                v = SphericalToCartesian(latitudeTop, longitude, radius);
                color = this.asc.getAtmosphereColor(v.transformBy4(skyTransform), eyePoint, this.sunDirection);
                gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                stackColors[slice] = color;
            }
            gl.glEnd();
        }

        // Top fade
        if (endLat < 90) {
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int slice = 0; slice <= slices; slice++) {
                longitude = 180 - ((float) slice / slices * (float) 360);
                Vec4 v = SphericalToCartesian(latitudeTop, longitude, radius);
                color = stackColors[slice];
                gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                v = SphericalToCartesian(endLat, longitude, radius);
                gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
            }
            gl.glEnd();
        }

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
    }

    protected Matrix computeSkyTransform(DrawContext dc) {
        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(dc.getGlobe().computeModelCoordinateOriginTransform(dc.getView().getEyePosition()));
        transform = transform.multiply(Matrix.fromRotationX(Angle.POS90));
        return transform;
    }

    /**
     * Converts position in spherical coordinates (lat/lon/altitude) to cartesian (XYZ) coordinates.
     *
     * @param latitude Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param radius Radius
     * @return the corresponding Point
     */
    protected static Vec4 SphericalToCartesian(double latitude, double longitude, double radius) {
        latitude *= Math.PI / 180.0f;
        longitude *= Math.PI / 180.0f;

        double radCosLat = radius * Math.cos(latitude);

        return new Vec4(
                radCosLat * Math.sin(longitude),
                radius * Math.sin(latitude),
                radCosLat * Math.cos(longitude));
    }

    public void dispose() {
        if (this.glListId < 0) {
            return;
        }

        GLContext glc = GLContext.getCurrent();
        if (glc == null) {
            return;
        }

        glc.getGL().getGL2().glDeleteLists(this.glListId, 1);
        this.glListId = -1;
    }

    @Override
    public String toString() {
        return Logging.getMessage("layers.Earth.SkyGradientLayer.Name");
    }
}
