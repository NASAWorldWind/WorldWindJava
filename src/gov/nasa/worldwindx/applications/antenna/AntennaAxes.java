/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.antenna;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import javax.xml.stream.*;
import java.awt.*;
import java.io.IOException;
import java.nio.*;

/**
 * Provides axes for {@link AntennaModel}. The axes are positioned by a {@link Position}, an azimuth and ane elevation
 * angle. The azimuth orients the axes clockwise from north. The elevation angle rotates the axes vertically
 * counterclockwise from the horizon.
 *
 * @author tag
 * @version $Id: AntennaAxes.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class AntennaAxes extends AbstractShape
{
    public static final int DISPLAY_MODE_FILL = GL2.GL_FILL;
    public static final int DISPLAY_MODE_LINE = GL2.GL_LINE;

    protected int nHeightIntervals = 10;
    protected int nThetaIntervals = 20;

    protected Position position = Position.ZERO;
    protected Angle azimuth;
    protected Angle elevationAngle;
    protected double length = 1e3;
    protected double radius = 0.05 * length;
    protected Font labelFont = Font.decode("Arial-PLAIN-14");
    protected String xAxisLabel = "Body X";
    protected String yAxisLabel = "Body Y";
    protected String zAxisLabel = "Bore Sight";

    /**
     * This class holds globe-specific data for this shape. It's managed via the shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractShapeData
    {
        protected FloatBuffer vertices;
        protected IntBuffer[] indices;
        protected FloatBuffer normals;
        protected FloatBuffer coneVertices;
        protected IntBuffer coneIndices;
        protected FloatBuffer coneNormals;

        /**
         * Construct a cache entry using the boundaries of this shape.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, AntennaAxes shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
        }
    }

    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    /**
     * Returns the current shape data cache entry.
     *
     * @return the current data cache entry.
     */
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    public AntennaAxes()
    {
    }

    @Override
    protected void initialize()
    {
        // Nothing unique to initialize.
    }

    public Position getPosition()
    {
        return position;
    }

    /**
     * Specifies the position of the axes' origin.
     *
     * @param position the position of the axes' origin.
     */
    public void setPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.reset();
    }

    public Angle getAzimuth()
    {
        return azimuth;
    }

    /**
     * Specifies an angle clockwise from north by which to rotate the axes.
     *
     * @param azimuth the angle from north.
     */
    public void setAzimuth(Angle azimuth)
    {
        this.azimuth = azimuth;
    }

    public Angle getElevationAngle()
    {
        return elevationAngle;
    }

    /**
     * Specifies an angle to rotate the axes vertically counterclockwise from the horizon. The rotation is a
     * right-handed rotation relative to the X axis.
     *
     * @param elevationAngle the elevation angle.
     */
    public void setElevationAngle(Angle elevationAngle)
    {
        this.elevationAngle = elevationAngle;
    }

    public double getRadius()
    {
        return radius;
    }

    public void setRadius(double radius)
    {
        this.radius = radius;
        this.reset();
    }

    public double getLength()
    {
        return length;
    }

    /**
     * Specifies the length of the axes, in meters.
     *
     * @param length the axes length in meters.
     */
    public void setLength(double length)
    {
        this.length = length;
        this.reset();
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font labelFont)
    {
        this.labelFont = labelFont;
    }

    public String getXAxisLabel()
    {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel)
    {
        this.xAxisLabel = xAxisLabel;
    }

    public String getYAxisLabel()
    {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel)
    {
        this.yAxisLabel = yAxisLabel;
    }

    public String getZAxisLabel()
    {
        return zAxisLabel;
    }

    public void setZAxisLabel(String zAxisLabel)
    {
        this.zAxisLabel = zAxisLabel;
    }

    public Position getReferencePosition()
    {
        return this.getPosition();
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
            return extent;

        this.getCurrent().setExtent(new Sphere(globe.computePointFromPosition(this.getReferencePosition()),
            this.getRadius()));

        return this.getCurrent().getExtent();
    }

    public Sector getSector()
    {
        if (this.sector == null)
            this.sector = null; // TODO

        return this.sector;
    }

    protected boolean mustApplyTexture(DrawContext dc)
    {
        return false;
    }

    @Override
    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return false;
    }

    @Override
    public void render(DrawContext dc)
    {
        super.render(dc);
    }

    @Override
    protected boolean mustDrawOutline()
    {
        return false;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        if (shapeData.vertices == null)
            return true;

        if (this.getAltitudeMode() == WorldWind.ABSOLUTE
            && shapeData.getGlobeStateKey() != null
            && shapeData.getGlobeStateKey().equals(dc.getGlobe().getGlobeStateKey(dc)))
            return false;

        // Determine whether the reference point has changed. If it hasn't, then no other points need to change.
        Vec4 rp = this.computePoint(dc.getTerrain(), this.getPosition());
        if (shapeData.getReferencePoint() != null && shapeData.getReferencePoint().equals(rp))
            return false;

        return super.mustRegenerateGeometry(dc);
    }

    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        if (!this.intersectsFrustum(dc))
            return false;

        this.makeCylinderVertices(dc);
        this.makeCone();

        ShapeData shapeData = this.getCurrent();

        if (shapeData.indices == null)
            this.makeCylinderIndices();

        if (shapeData.normals == null)
            this.makeCylinderNormals();

        return true;
    }

    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        return shapeData.vertices != null && shapeData.indices != null && shapeData.normals != null;
    }

    protected void doDrawOutline(DrawContext dc)
    {
        return;
    }

    protected void doDrawInterior(DrawContext dc)
    {
        this.drawAxes(dc);
    }

    public void drawAxes(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glPushMatrix();

        // Rotate to align with longitude.
        gl.glRotated(this.getPosition().getLongitude().degrees, 0, 1, 0);

        // Rotate to align with latitude.
        gl.glRotated(Math.abs(90 - this.getPosition().getLatitude().degrees), 1, 0, 0);

        // Apply the azimuth.
        if (this.getAzimuth() != null)
            gl.glRotated(-this.getAzimuth().degrees, 0, 1, 0);

        // Apply the elevation angle.
        if (this.getElevationAngle() != null)
            gl.glRotated(this.getElevationAngle().degrees, 1, 0, 0);

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.vertices.rewind());

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.normals.rewind());

        // Draw the "Z axis
        this.drawCylinder(dc, shapeData);

        // Draw the X axis
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glRotated(90d, 0, 0, -1);
        this.drawCylinder(dc, shapeData);
        gl.glPopMatrix();

        // Draw the "Y axis
        gl.glPushMatrix();
        gl.glRotated(90d, +1, 0, 0);
        this.drawCylinder(dc, shapeData);
        gl.glPopMatrix();

        // Draw the axis cones.
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.coneVertices.rewind());

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.coneNormals.rewind());

        // Draw the "Z axis cone
        this.drawCone(dc, shapeData);

        // Draw the Y axis cone
        gl.glPushMatrix();
        gl.glRotated(90d, 0, 0, -1);
        this.drawCone(dc, shapeData);
        gl.glPopMatrix();

        // Draw the "X" axis cone
        gl.glPushMatrix();
        gl.glRotated(90d, +1, 0, 0);
        this.drawCone(dc, shapeData);
        gl.glPopMatrix();

        gl.glPopMatrix();

        if (!dc.isPickingMode()) // labels aren't pickable
            this.drawLabels(dc);
    }

    protected void drawCylinder(DrawContext dc, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        for (IntBuffer iBuffer : shapeData.indices)
        {
            gl.glDrawElements(GL.GL_TRIANGLE_STRIP, iBuffer.limit(), GL.GL_UNSIGNED_INT, iBuffer.rewind());
        }
    }

    protected void drawCone(DrawContext dc, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        IntBuffer iBuffer = shapeData.coneIndices;
        gl.glDrawElements(GL.GL_TRIANGLE_FAN, iBuffer.limit(), GL.GL_UNSIGNED_INT, iBuffer.rewind());
    }

    protected void drawLabels(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Compute the positioning transform.
        Matrix lat = Matrix.fromAxisAngle(Angle.POS90.subtract(this.position.getLatitude()), Vec4.UNIT_X);
        Matrix lon = Matrix.fromAxisAngle(this.position.getLongitude(), Vec4.UNIT_Y);
        Matrix baseM = lon.multiply(lat);
        if (this.getAzimuth() != null)
            baseM = baseM.multiply(Matrix.fromAxisAngle(this.getAzimuth().multiply(-1), Vec4.UNIT_Y));
        if (this.getElevationAngle() != null)
            baseM = baseM.multiply(Matrix.fromAxisAngle(this.getElevationAngle(), Vec4.UNIT_X));

        // Compute the screen points at which to place the labels. These are all points on the World Wind principal
        // axes, not the antenna model's axes. The correct labeling of those takes place below when the labels are
        // drawn. They have the same directions, but in the antenna model WW's Y axis is the model's Z axis, WW's Z
        // axis is the model's X axis, and WW's X axis is the model's Y axis.

        Vec4 px = new Vec4(1.1 * this.getLength(), 0, 0).transformBy3(baseM); // define points along principal axes
        Vec4 py = new Vec4(0, 1.1 * this.getLength(), 0).transformBy3(baseM);
        Vec4 pz = new Vec4(0, 0, 1.1 * this.getLength()).transformBy3(baseM);

        Vec4 rp = this.getCurrent().getReferencePoint();
        px = px.add3(rp); // shift the points to the reference position
        py = py.add3(rp);
        pz = pz.add3(rp);

        Vec4 screenPointX = dc.getView().project(px); // project points to the viewport
        Vec4 screenPointY = dc.getView().project(py);
        Vec4 screenPointZ = dc.getView().project(pz);

        // We don't want the current reference center to apply because we're going to draw the labels in 2D and the
        // translation to the reference point is already incorporated in the screen point calculations above. The
        // reference center is restored below after the labels are drawn.
        dc.getView().popReferenceCenter(dc);

        OGLStackHandler osh = new OGLStackHandler();

        try
        {
            osh.pushProjectionIdentity(gl);
            gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

            osh.pushModelviewIdentity(gl);

            // Draw the labels.

            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
                this.labelFont);

            Color textColor = this.getActiveAttributes().getInteriorMaterial().getDiffuse();
            Color backgroundColor = (textColor.getAlpha() < 255 ? new Color(0, 0, 0, textColor.getAlpha())
                : Color.BLACK);

            // Do not depth buffer the labels.
            osh.pushAttrib(gl, GL2.GL_DEPTH_BUFFER_BIT);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            textRenderer.begin3DRendering();

            try
            {
                this.drawLabel(textRenderer, this.getYAxisLabel(), screenPointX, textColor, backgroundColor);
                this.drawLabel(textRenderer, this.getZAxisLabel(), screenPointY, textColor, backgroundColor);
                this.drawLabel(textRenderer, this.getXAxisLabel(), screenPointZ, textColor, backgroundColor);
            }
            finally
            {
                textRenderer.end3DRendering();
            }
        }
        finally
        {
            osh.pop(gl);
            dc.getView().pushReferenceCenter(dc, getCurrent().getReferencePoint());
        }
    }

    protected void drawLabel(TextRenderer textRenderer, String text, Vec4 screenPoint, Color textColor, Color bgColor)
    {
        textRenderer.setColor(bgColor);
        textRenderer.draw3D(text, (int) screenPoint.x + 1, (int) screenPoint.y - 1, 0, 1);
        textRenderer.setColor(textColor);
        textRenderer.draw3D(text, (int) screenPoint.x, (int) screenPoint.y, 0, 1);
    }

    protected void makeCylinderVertices(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        Vec4 rp = this.computePoint(dc.getTerrain(), this.getPosition());
        shapeData.setReferencePoint(rp);

        int nVertices = (this.nHeightIntervals + 1) * (this.nThetaIntervals + 1);
        shapeData.vertices = Buffers.newDirectFloatBuffer(3 * nVertices);

        double xMax = -Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;
        double zMax = -Double.MAX_VALUE;

        double dHeight = this.getLength() / this.nHeightIntervals;
        double dTheta = 2d * Math.PI / this.nThetaIntervals;

        Double r = this.getRadius();

        for (int ih = 0; ih <= this.nHeightIntervals; ih++)
        {
            double height = ih * dHeight;

            if (ih == this.nHeightIntervals)
                height = this.getLength();

            for (int it = 0; it <= this.nThetaIntervals; it++)
            {
                double theta = it * dTheta;

                if (it == this.nThetaIntervals)
                    theta = 0;

                double x = r * Math.sin(theta);
                double z = r * Math.cos(theta);
                double y = height;

                double xa = Math.abs(x);
                double ya = Math.abs(y);
                double za = Math.abs(z);
                if (xa > xMax)
                    xMax = xa;
                if (ya > yMax)
                    yMax = ya;
                if (za > zMax)
                    zMax = za;

                shapeData.vertices.put((float) x).put((float) y).put((float) z);
            }
        }

        shapeData.setExtent(new Sphere(rp, Math.sqrt(xMax * xMax + yMax * yMax + zMax * zMax)));
    }

    protected void makeCylinderIndices()
    {
        ShapeData shapeData = this.getCurrent();

        shapeData.indices = new IntBuffer[this.nHeightIntervals];

        for (int j = 0; j < this.nHeightIntervals; j++)
        {
            shapeData.indices[j] = Buffers.newDirectIntBuffer(2 * this.nThetaIntervals + 2);

            for (int i = 0; i <= this.nThetaIntervals; i++)
            {
                int k1 = i + j * (this.nThetaIntervals + 1);
                int k2 = k1 + this.nThetaIntervals + 1;
                shapeData.indices[j].put(k1).put(k2);
            }
        }
    }

    protected void makeCylinderNormals()
    {
        ShapeData shapeData = this.getCurrent();

        shapeData.normals = Buffers.newDirectFloatBuffer(shapeData.vertices.limit());

        for (int i = 0; i < shapeData.vertices.limit(); i += 3)
        {
            Vec4 n = new Vec4(shapeData.vertices.get(i), 0, shapeData.vertices.get(i + 2)).normalize3();

            shapeData.normals.put((float) -n.x).put(0f).put((float) -n.z);
        }
    }

    protected void makeCone()
    {
        double dTheta = 2 * Math.PI / this.nThetaIntervals;

        // This is the center vertex for a triangle fan.
        Vec4 v0 = new Vec4(0, 1.05 * this.getLength(), 0);

        // Compute the outer vertices.
        double r = 1.0 * this.getRadius();
        Vec4[] outerVerts = new Vec4[this.nThetaIntervals];
        for (int i = 0; i < outerVerts.length; i++)
        {
            double theta = i * dTheta;

            double x = r * Math.sin(theta);
            double z = r * Math.cos(theta);

            outerVerts[i] = new Vec4(x, this.getLength(), z);
        }

        // Compute the vertex normals.
        ShapeData shapeData = this.getCurrent();

        Vec4[] outerNormals = new Vec4[outerVerts.length];
        Vec4 na = null, nb;
        Vec4 va, vb, vc;
        for (int i = 0; i < outerVerts.length; i++)
        {
            if (i == 0)
            {
                va = outerVerts[outerVerts.length - 1].subtract3(v0);
                vb = outerVerts[i].subtract3(v0);
                vc = outerVerts[i + 1].subtract3(v0);
                na = va.cross3(vb).multiply3(0.5);
            }
            else if (i == outerVerts.length - 1)
            {
                vb = outerVerts[i].subtract3(v0);
                vc = outerVerts[0].subtract3(v0);
            }
            else
            {
                vb = outerVerts[i].subtract3(v0);
                vc = outerVerts[i + 1].subtract3(v0);
            }

            nb = vb.cross3(vc).multiply3(0.5);
            outerNormals[i] = na.add3(nb).normalize3();
            na = nb;
        }

        // Fill buffers for a closed triangle fan.
        shapeData.coneVertices = Buffers.newDirectFloatBuffer(3 * (outerVerts.length + 1));
        shapeData.coneNormals = Buffers.newDirectFloatBuffer(shapeData.coneVertices.capacity());

        shapeData.coneVertices.put((float) v0.x).put((float) v0.y).put((float) v0.z);
        shapeData.coneNormals.put(0f).put(1f).put(0f);

        for (int i = 0; i < outerVerts.length; i++)
        {
            Vec4 vert = outerVerts[i];
            Vec4 normal = outerNormals[i];

            shapeData.coneVertices.put((float) vert.x).put((float) vert.y).put((float) vert.z);
            shapeData.coneNormals.put((float) normal.x).put((float) normal.y).put((float) normal.z);
        }

        shapeData.coneIndices = Buffers.newDirectIntBuffer(outerVerts.length + 2);
        for (int i = 0; i < shapeData.coneIndices.capacity() - 1; i++)
        {
            shapeData.coneIndices.put(i);
        }
        shapeData.coneIndices.put(1); // close the fan by duplicating first outer vertex as last outer vertex
    }

    @Override
    protected void fillVBO(DrawContext dc)
    {
    }

    @Override
    public void moveTo(Position position)
    {
    }

    @Override
    public java.util.List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        return null;
    }

    @Override
    public String isExportFormatSupported(String mimeType)
    {
        return Exportable.FORMAT_NOT_SUPPORTED;
    }

    @Override
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        throw new UnsupportedOperationException("KML output not supported for AntennaModel");
    }
}
