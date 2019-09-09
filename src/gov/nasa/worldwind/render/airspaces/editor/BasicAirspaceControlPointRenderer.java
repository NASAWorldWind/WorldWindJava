/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.*;

// TODO: this renderer largely redundant with MarkerRenderer, and the additional fucntionality here should be
// integrated into MarkerRenderer. There are two key pieces of additional functionality:
// (1) an attribute representing the maximum marker size, and
// (2) the ability to disable the depth test.

/**
 * @author dcollins
 * @version $Id: BasicAirspaceControlPointRenderer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicAirspaceControlPointRenderer implements AirspaceControlPointRenderer
{
    private boolean enableLighting;
    private Marker controlPointMarker;
    private Material lightMaterial;
    private Vec4 lightDirection;
    private boolean enableDepthTest;
    // Rendering support.
    private double maxMarkerSize;
    private PickSupport pickSupport = new PickSupport();

    public BasicAirspaceControlPointRenderer(Marker controlPointMarker)
    {
        if (controlPointMarker == null)
        {
            String message = Logging.getMessage("nullValue.MarkerIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.enableLighting = true;
        this.controlPointMarker = controlPointMarker;
        this.lightMaterial = Material.WHITE;
        this.lightDirection = new Vec4(1.0, 0.5, 1.0);
        this.enableDepthTest = true;
    }

    public BasicAirspaceControlPointRenderer()
    {
        this(createDefaultMarker());
    }

    public static Marker createDefaultMarker()
    {
        // Create an opaque blue sphere. By default the sphere has a 16 pixel radius, but its radius must be at least
        // 0.1 meters .
        MarkerAttributes attributes = new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 1.0, 16, 0.1);
        return new BasicMarker(null, attributes, null);
    }

    public boolean isEnableLighting()
    {
        return this.enableLighting;
    }

    public void setEnableLighting(boolean enable)
    {
        this.enableLighting = enable;
    }

    public Marker getControlPointMarker()
    {
        return controlPointMarker;
    }

    public void setControlPointMarker(Marker marker)
    {
        if (marker == null)
        {
            String message = Logging.getMessage("nullValue.MarkerIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.controlPointMarker = marker;
    }

    public Material getLightMaterial()
    {
        return this.lightMaterial;
    }

    public void setLightMaterial(Material material)
    {
        if (material != null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lightMaterial = material;
    }

    public Vec4 getLightDirection()
    {
        return this.lightDirection;
    }

    public void setLightDirection(Vec4 direction)
    {
        if (direction != null)
        {
            String message = Logging.getMessage("nullValue.DirectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lightDirection = direction;
    }

    public boolean isEnableDepthTest()
    {
        return this.enableDepthTest;
    }

    public void setEnableDepthTest(boolean enable)
    {
        this.enableDepthTest = enable;
    }

    public void render(DrawContext dc, Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.draw(dc, controlPoints);
    }

    public void pick(DrawContext dc, Iterable<? extends AirspaceControlPoint> controlPoints, Point pickPoint,
        Layer layer)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.pickSupport.clearPickList();
        this.draw(dc, controlPoints);
        this.pickSupport.resolvePick(dc, pickPoint, layer);
        this.pickSupport.clearPickList(); // to ensure entries can be garbage collected
    }

    //**************************************************************//
    //********************  Control Point Rendering  ***************//
    //**************************************************************//

    protected void draw(DrawContext dc, Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        this.begin(dc);
        try
        {
            this.drawControlPoints(dc, controlPoints);
        }
        finally
        {
            this.end(dc);
        }
    }

    protected void drawControlPoints(DrawContext dc, Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        // Render the control points from back-to front.
        SortedSet<AirspaceControlPoint> sortedPoints = this.sortControlPoints(dc, controlPoints);
        this.drawMarkers(dc, sortedPoints);
    }

    protected void begin(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            this.pickSupport.beginPicking(dc);
            gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_TRANSFORM_BIT);
        }
        else
        {
            gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_HINT_BIT
                | GL2.GL_LIGHTING_BIT | GL2.GL_TRANSFORM_BIT);

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            if (this.isEnableLighting())
            {
                this.setupLighting(dc);
            }

            // Were applying a scale transform on the modelview matrix, so the normal vectors must be re-normalized
            // before lighting is computed. In this case we're scaling by a constant factor, so GL_RESCALE_NORMAL
            // is sufficient and potentially less expensive than GL_NORMALIZE (or computing unique normal vectors
            // for each value of radius). GL_RESCALE_NORMAL was introduced in OpenGL version 1.2.
            gl.glEnable(GL2.GL_NORMALIZE);

            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        }

        if (this.isEnableDepthTest())
        {
            gl.glEnable(GL.GL_DEPTH_TEST);
        }
        else
        {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    protected void end(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPopMatrix();

        gl.glPopAttrib();

        if (dc.isPickingMode())
        {
            this.pickSupport.endPicking(dc);
        }
    }

    protected PickSupport getPickSupport()
    {
        return this.pickSupport;
    }

    //**************************************************************//
    //********************  Marker Rendering  **********************//
    //**************************************************************//

    protected void drawMarkers(DrawContext dc, Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        // Compute the maximum marker size as a function of the control points to render.
        this.setMaxMarkerSize(this.computeMaxMarkerSize(controlPoints));

        // Apply the marker attributes.
        if (!dc.isPickingMode())
        {
            if (this.isEnableLighting())
            {
                this.getControlPointMarker().getAttributes().apply(dc);
            }
            else
            {
                float[] compArray = new float[4];
                Color color = this.getControlPointMarker().getAttributes().getMaterial().getDiffuse();
                color.getRGBComponents(compArray);
                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glColor4fv(compArray, 0);
            }
        }

        for (AirspaceControlPoint p : controlPoints)
        {
            this.drawMarker(dc, p);
        }
    }

    protected void drawMarker(DrawContext dc, AirspaceControlPoint controlPoint)
    {
        if (!dc.getView().getFrustumInModelCoordinates().contains(controlPoint.getPoint()))
            return;

        if (dc.isPickingMode())
        {
            java.awt.Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            PickedObject po = new PickedObject(colorCode, controlPoint);
            this.pickSupport.addPickableObject(po);
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        }

        Vec4 point = controlPoint.getPoint();
        double radius = this.computeMarkerRadius(dc, this.getControlPointMarker(), point);

        this.getControlPointMarker().render(dc, point, radius);
    }

    protected double getMaxMarkerSize()
    {
        return this.maxMarkerSize;
    }

    protected void setMaxMarkerSize(double size)
    {
        this.maxMarkerSize = size;
    }

    protected double computeMarkerRadius(DrawContext dc, Marker marker, Vec4 point)
    {
        double d = dc.getView().getEyePoint().distanceTo3(point);
        double radius = marker.getAttributes().getMarkerPixels() * dc.getView().computePixelSizeAtDistance(d);

        // Constrain the minimum marker size by the marker attributes.
        if (radius < marker.getAttributes().getMinMarkerSize())
        {
            radius = marker.getAttributes().getMinMarkerSize();
        }

        // Constrain the maximum marker size by the computed maximum size.
        if (this.getMaxMarkerSize() > 0.0)
        {
            if (radius > this.getMaxMarkerSize())
            {
                radius = this.getMaxMarkerSize();
            }
        }

        return radius;
    }

    protected double computeMaxMarkerSize(Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        // Compute the maximum marker size as a fraction of the average distance between control points. This will
        // prevent all but the nearest control points from touching as the view moves away from the airspace.

        double totalDistance = 0.0;
        int count = 0;

        for (AirspaceControlPoint p1 : controlPoints)
        {
            for (AirspaceControlPoint p2 : controlPoints)
            {
                if (p1 != p2)
                {
                    double d = p1.getPoint().distanceTo3(p2.getPoint());
                    totalDistance += d;
                    count++;
                }
            }
        }

        // TODO: this is a function that maps average marker distance to a maximum marker size. The function
        // (f(x) = x/16) is currently hard-coded and should be extracted as a method so it will be clearly defined,
        // and the function may be overridden with a different behavior.
        return (count == 0) ? 0.0 : (totalDistance / (double) count / 16.0);
    }

    //**************************************************************//
    //********************  Rendering Support  *********************//
    //**************************************************************//

    protected void setupLighting(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        float[] modelAmbient = new float[4];
        modelAmbient[0] = 1.0f;
        modelAmbient[1] = 1.0f;
        modelAmbient[2] = 1.0f;
        modelAmbient[3] = 0.0f;

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, modelAmbient, 0);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_FALSE);
        gl.glShadeModel(GL2.GL_SMOOTH);

        // The alpha value at a vertex is taken only from the diffuse material's alpha channel, without any
        // lighting computations applied. Therefore we specify alpha=0 for all lighting ambient, specular and
        // emission values. This will have no effect on material alpha.
        float[] ambient = new float[4];
        float[] diffuse = new float[4];
        float[] specular = new float[4];
        getLightMaterial().getDiffuse().getRGBColorComponents(diffuse);
        getLightMaterial().getSpecular().getRGBColorComponents(specular);
        ambient[3] = diffuse[3] = specular[3] = 0.0f;

        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specular, 0);

        // Setup the light as a directional light coming from the viewpoint. This requires two state changes
        // (a) Set the light position as direction x, y, z, and set the w-component to 0, which tells OpenGL this is
        //     a directional light.
        // (b) Invoke the light position call with the identity matrix on the modelview stack. Since the position
        //     is transfomed by the

        Vec4 vec = getLightDirection().normalize3();
        float[] params = new float[4];
        params[0] = (float) vec.x;
        params[1] = (float) vec.y;
        params[2] = (float) vec.z;
        params[3] = 0.0f;

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, params, 0);

        gl.glPopMatrix();
    }

    protected SortedSet<AirspaceControlPoint> sortControlPoints(DrawContext dc,
        Iterable<? extends AirspaceControlPoint> unsortedPoints)
    {
        final Vec4 eyePoint = dc.getView().getEyePoint();

        // Sort control points from lower altitude to upper altitude, then from back to front. This will give priority
        // to the upper altitude control points during picking. We give priority to the upper points, in case the
        // shape has been flattened against the terrain. In this case the lower points may not be movable, therefore
        // the user must be able to select an upper point to raise the shape and fix the problem.

        TreeSet<AirspaceControlPoint> set = new TreeSet<AirspaceControlPoint>(new Comparator<AirspaceControlPoint>()
        {
            public int compare(AirspaceControlPoint p1, AirspaceControlPoint p2)
            {
                double d1 = p1.getPoint().distanceTo3(eyePoint);
                double d2 = p2.getPoint().distanceTo3(eyePoint);
                int alt1 = p1.getAltitudeIndex();
                int alt2 = p2.getAltitudeIndex();

                if (alt2 < alt1)
                {
                    return -1;
                }
                else if (alt2 > alt1)
                {
                    return 1;
                }
                else
                {
                    if (d1 < d2)
                    {
                        return 1;
                    }
                    else if (d1 > d2)
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
        });

        for (AirspaceControlPoint p : unsortedPoints)
        {
            set.add(p);
        }

        return set;
    }
}
