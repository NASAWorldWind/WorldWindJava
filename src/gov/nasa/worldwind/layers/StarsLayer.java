/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.io.*;
import java.nio.*;

/**
 * Renders a star background based on a subset of ESA Hipparcos catalog.
 *
 * @author Patrick Murris
 * @version $Id: StarsLayer.java 2176 2014-07-25 16:35:25Z dcollins $
 */
public class StarsLayer extends RenderableLayer
{
    /** The default name of the stars file.s */
    protected static final String DEFAULT_STARS_FILE = "config/Hipparcos_Stars_Mag6x5044.dat";
    protected static final double DEFAULT_MIN_ACTIVE_ALTITUDE = 100e3;

    /** The stars file name. */
    protected String starsFileName =
        Configuration.getStringValue("gov.nasa.worldwind.StarsLayer.StarsFileName", DEFAULT_STARS_FILE);
    /** The float buffer holding the Cartesian star coordinates. */
    protected FloatBuffer starsBuffer;
    protected int numStars;
    protected boolean rebuild;            // True if need to rebuild GL list
    /** The radius of the spherical shell containing the stars. */
    protected Double radius; // radius is either set explicitly or taken from the star file
    /** The star sphere longitudinal rotation. */
    protected Angle longitudeOffset = Angle.ZERO;
    /** The star sphere latitudinal rotation. */
    protected Angle latitudeOffset = Angle.ZERO;
    protected Object vboCacheKey = new Object();

    /** Constructs a stars layer using the default stars file, which may be specified in {@link Configuration}. */
    public StarsLayer()
    {
        this.initialize(null, null);
    }

    /**
     * Constructs a stars layer using a specified stars file.
     *
     * @param starsFileName the full path the star file.
     */
    public StarsLayer(String starsFileName)
    {
        this.initialize(starsFileName, null);
    }

    /**
     * Constructs a stars layer using a specified stars file and star-field radius.
     *
     * @param starsFileName the full path the star file.
     * @param radius        the radius of the stars sphere. May be null, in which case the radius in the stars file is
     *                      used.
     */
    public StarsLayer(String starsFileName, Double radius)
    {
        if (WWUtil.isEmpty(starsFileName))
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize(starsFileName, radius);
    }

    /**
     * Called by constructors to save the stars file name, the stars field radius and the layer's minimum active
     * altitude.
     *
     * @param starsFileName the full path the star file.
     * @param radius        the radius of the stars sphere. May be null, in which case the radius in the stars file is
     *                      used.
     */
    protected void initialize(String starsFileName, Double radius)
    {
        if (starsFileName != null)
            this.setStarsFileName(starsFileName);

        if (radius != null)
            this.radius = radius;

        this.setPickEnabled(false);

        // Turn the layer off to eliminate its overhead when the user zooms in.
        this.setMinActiveAltitude(DEFAULT_MIN_ACTIVE_ALTITUDE);
    }

    /**
     * Indicates the path and filename of the stars file.
     *
     * @return name of stars catalog file.
     */
    public String getStarsFileName()
    {
        return this.starsFileName;
    }

    /**
     * Specifies the path and filename of the stars file.
     *
     * @param fileName the path and filename.
     *
     * @throws IllegalArgumentException if the file name is null or empty.
     */
    public void setStarsFileName(String fileName)
    {
        if (WWUtil.isEmpty(fileName))
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.starsFileName = fileName;
        this.rebuild = true;
    }

    /**
     * Returns the latitude offset (tilt) for the star sphere.
     *
     * @return the latitude offset.
     */
    public Angle getLatitudeOffset()
    {
        return this.latitudeOffset;
    }

    /**
     * Sets the latitude offset (tilt) of the star sphere.
     *
     * @param offset the latitude offset.
     */
    public void setLatitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.latitudeOffset = offset;
    }

    /**
     * Returns the longitude offset of the star sphere.
     *
     * @return the longitude offset.
     */
    public Angle getLongitudeOffset()
    {
        return this.longitudeOffset;
    }

    /**
     * Sets the longitude offset of the star sphere.
     *
     * @param offset the longitude offset.
     *
     * @throws IllegalArgumentException if the angle is null.s
     */
    public void setLongitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.longitudeOffset = offset;
    }

    @Override
    public void doRender(DrawContext dc)
    {
        if (dc.is2DGlobe())
            return; // Layer doesn't make sense in 2D

        // Load or reload stars if not previously loaded
        if (this.starsBuffer == null || this.rebuild)
        {
            this.loadStars();
            this.rebuild = false;
        }

        // Still no stars to render ?
        if (this.starsBuffer == null)
            return;

        // Exit if the viewport is not visible, in which case rendering results in exceptions.
        View view = dc.getView();
        if (view.getViewport().getWidth() == 0 || view.getViewport().getHeight() == 0)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        double[] matrixArray = new double[16];

        try
        {
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Override the default projection matrix in order to extend the far clip plane to include the stars.
            Matrix projection = Matrix.fromPerspective(view.getFieldOfView(), view.getViewport().width,
                view.getViewport().height, 1, this.radius + 1);
            ogsh.pushProjectionIdentity(gl);
            gl.glLoadMatrixd(projection.toArray(matrixArray, 0, false), 0);

            // Override the default modelview matrix in order to force the eye point to the origin, and apply the
            // latitude and longitude rotations for the stars dataset. Forcing the eye point to the origin causes the
            // stars to appear at an infinite distance, regardless of the view's eye point.
            Matrix modelview = view.getModelviewMatrix();
            modelview = modelview.multiply(Matrix.fromTranslation(view.getEyePoint()));
            modelview = modelview.multiply(Matrix.fromAxisAngle(this.longitudeOffset, 0, 1, 0));
            modelview = modelview.multiply(Matrix.fromAxisAngle(Angle.fromDegrees(-this.latitudeOffset.degrees), 1, 0, 0));
            ogsh.pushModelviewIdentity(gl);
            gl.glLoadMatrixd(modelview.toArray(matrixArray, 0, false), 0);

            // Draw
            ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
            {
                if (!this.drawWithVBO(dc))
                    this.drawWithVertexArray(dc);
            }
            else
            {
                this.drawWithVertexArray(dc);
            }
        }
        finally
        {
            dc.restoreDefaultDepthTesting();
            ogsh.pop(gl);
        }
    }

    protected void drawWithVertexArray(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glInterleavedArrays(GL2.GL_C3F_V3F, 0, this.starsBuffer);
        gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
    }

    protected boolean drawWithVBO(DrawContext dc)
    {
        int[] vboId = (int[]) dc.getGpuResourceCache().get(this.vboCacheKey);
        if (vboId == null)
        {
            this.fillVbo(dc);
            vboId = (int[]) dc.getGpuResourceCache().get(this.vboCacheKey);
            if (vboId == null)
                return false;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
        gl.glInterleavedArrays(GL2.GL_C3F_V3F, 0, 0);
        gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        return true;
    }

    /**
     * Creates and fills this layer's vertex buffer.
     *
     * @param dc the current draw context.
     */
    protected void fillVbo(DrawContext dc)
    {
        GL gl = dc.getGL();

        //Create a new bufferId
        int glBuf[] = new int[1];
        gl.glGenBuffers(1, glBuf, 0);

        // Load the buffer
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, glBuf[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, this.starsBuffer.limit() * 4, this.starsBuffer, GL.GL_STATIC_DRAW);

        // Add it to the gpu resource cache
        dc.getGpuResourceCache().put(this.vboCacheKey, glBuf, GpuResourceCache.VBO_BUFFERS,
            this.starsBuffer.limit() * 4);
    }

    /** Read stars file and load it into a float buffer. */
    protected void loadStars()
    {
        ByteBuffer byteBuffer = null;

        if (WWIO.getSuffix(this.starsFileName).equals("dat"))
        {
            try
            {
                //Try loading from a resource
                InputStream starsStream = WWIO.openFileOrResourceStream(this.starsFileName, this.getClass());
                if (starsStream == null)
                {
                    String message = Logging.getMessage("layers.StarLayer.CannotReadStarFile");
                    Logging.logger().severe(message);
                    return;
                }

                //Read in the binary buffer
                try
                {
                    byteBuffer = WWIO.readStreamToBuffer(starsStream, true); // Read stars to a direct ByteBuffer.
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                }
                finally
                {
                    WWIO.closeStream(starsStream, starsFileName);
                }
            }
            catch (IOException e)
            {
                String message = "IOException while loading stars data from " + this.starsFileName;
                Logging.logger().severe(message);
            }
        }
        else
        {
            //Assume it is a tsv text file
            byteBuffer = StarsConvertor.convertTsvToByteBuffer(this.starsFileName);
        }

        if (byteBuffer == null)
        {
            String message = "IOException while loading stars data from " + this.starsFileName;
            Logging.logger().severe(message);
            return;
        }

        //Grab the radius from the first value in the buffer
        if (this.radius == null)
            this.radius = (double) byteBuffer.getFloat();
        else
            byteBuffer.getFloat(); // skip over it

        //View the rest of the ByteBuffer as a FloatBuffer
        this.starsBuffer = byteBuffer.asFloatBuffer();

        //byteBuffer is Little-Endian. If native order is not Little-Endian, switch to Big-Endian.
        if (byteBuffer.order() != ByteOrder.nativeOrder())
        {
            //tmpByteBuffer is allocated as Big-Endian on all systems
            ByteBuffer tmpByteBuffer = ByteBuffer.allocateDirect(byteBuffer.limit());

            //View it as a Float Buffer
            FloatBuffer fbuffer = tmpByteBuffer.asFloatBuffer();

            //Fill it with the floats in starsBuffer
            for (int i = 0; i < fbuffer.limit(); i++)
            {
                fbuffer.put(this.starsBuffer.get(i));
            }

            fbuffer.flip();

            //Make the starsBuffer the Big-Endian buffer
            this.starsBuffer = fbuffer;
        }

        //Number of stars = limit / 6 floats per star -> (R,G,B,X,Y,Z)
        this.numStars = this.starsBuffer.limit() / 6;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.StarsLayer.Name");
    }
}
