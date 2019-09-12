/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Basic implementation of AnnotationRenderer. Process Annotation rendering as OrderedRenderable objects batch.
 *
 * @author Patrick Murris
 * @version $Id: BasicAnnotationRenderer.java 2223 2014-08-13 23:56:06Z tgaskins $
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 * @see AnnotationLayer
 */
public class BasicAnnotationRenderer implements AnnotationRenderer
{
    protected PickSupport pickSupport = new PickSupport();
    protected long currentFrameTime;
    protected HashSet<Annotation> currentPickAnnotations = new HashSet<Annotation>();
    protected HashSet<Annotation> currentDrawAnnotations = new HashSet<Annotation>();

    protected static boolean isAnnotationValid(Annotation annotation, boolean checkPosition)
    {
        if (annotation == null || annotation.getText() == null)
            return false;

        //noinspection RedundantIfStatement,SimplifiableIfStatement
        if (checkPosition && annotation instanceof Locatable)
            return ((Locatable) annotation).getPosition() != null;

        return true;
    }

    public void pick(DrawContext dc, Iterable<Annotation> annotations, Point pickPoint, Layer layer)
    {
        this.drawMany(dc, annotations, layer);
    }

    public void pick(DrawContext dc, Annotation annotation, Vec4 annotationPoint, java.awt.Point pickPoint, Layer layer)
    {
        if (!isAnnotationValid(annotation, false))
            return;

        this.drawOne(dc, annotation, annotationPoint, layer);
    }

    public void render(DrawContext dc, Iterable<Annotation> annotations, Layer layer)
    {
        this.drawMany(dc, annotations, layer);
    }

    public void render(DrawContext dc, Annotation annotation, Vec4 annotationPoint, Layer layer)
    {
        if (!isAnnotationValid(annotation, false))
            return;

        this.drawOne(dc, annotation, annotationPoint, layer);
    }

    protected void drawMany(DrawContext dc, Iterable<Annotation> annotations, Layer layer)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null)
            return;

        if (annotations == null)
        {
            String msg = Logging.getMessage("nullValue.AnnotationIterator");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.isContinuous2DGlobe() && this.currentFrameTime != dc.getFrameTimeStamp())
        {
            // Keep track of which annotations are added to the ordered renderable list so that they are not added
            // to that list more than once per frame.
            this.currentPickAnnotations.clear();
            this.currentDrawAnnotations.clear();
            this.currentFrameTime = dc.getFrameTimeStamp();
        }

        Iterator<Annotation> iterator = annotations.iterator();

        if (!iterator.hasNext())
            return;

        double altitude = dc.getView().getEyePosition().getElevation();

        while (iterator.hasNext())
        {
            Annotation annotation = iterator.next();
            if (!isAnnotationValid(annotation, true))
                continue;

            if (!annotation.getAttributes().isVisible())
                continue;

            // Do not draw the pick pass if not at pick point range;
            if (dc.isPickingMode() && !this.isAtPickRange(dc, annotation))
                continue;

            if (altitude < annotation.getMinActiveAltitude() || altitude > annotation.getMaxActiveAltitude())
                continue;

            if (dc.isContinuous2DGlobe() && annotation instanceof ScreenAnnotation)
            {
                if (dc.isPickingMode() && this.currentPickAnnotations.contains(annotation))
                    continue;

                if (currentDrawAnnotations.contains(annotation))
                    continue;
            }

            // TODO: cull annotations that are beyond the horizon
            double eyeDistance = 1;
            if (annotation instanceof Locatable)
            {
                // Determine Cartesian position from the surface geometry if the annotation is near the surface,
                // otherwise draw it from the globe.
                Vec4 annotationPoint = getAnnotationDrawPoint(dc, annotation);
                if (annotationPoint == null)
                    continue;
                eyeDistance = annotation.isAlwaysOnTop() ? 0 : dc.getView().getEyePoint().distanceTo3(annotationPoint);
            }

            if (annotation instanceof ScreenAnnotation)
            {
                Rectangle screenBounds = annotation.getBounds(dc);
                if (screenBounds != null && !dc.getView().getViewport().intersects(screenBounds))
                    return;
            }

            // The annotations aren't drawn here, but added to the ordered queue to be drawn back-to-front.
            dc.addOrderedRenderable(new OrderedAnnotation(annotation, layer, eyeDistance));

            if (dc.isContinuous2DGlobe() && annotation instanceof ScreenAnnotation)
            {
                if (dc.isPickingMode())
                    this.currentPickAnnotations.add(annotation);
                else
                    this.currentDrawAnnotations.add(annotation);
            }
        }
    }

    protected void drawOne(DrawContext dc, Annotation annotation, Vec4 annotationPoint, Layer layer)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.isContinuous2DGlobe() && annotation instanceof ScreenAnnotation
            && this.currentFrameTime != dc.getFrameTimeStamp())
        {
            // Keep track of which screen annotations are added to the ordered renderable list so that they are not added
            // to that list more than once per frame.
            this.currentPickAnnotations.clear();
            this.currentDrawAnnotations.clear();
            this.currentFrameTime = dc.getFrameTimeStamp();
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null)
            return;

        if (!annotation.getAttributes().isVisible())
            return;

        // Do not draw the pick pass if not at pick point range;
        if (dc.isPickingMode() && !this.isAtPickRange(dc, annotation))
            return;

        if (dc.isContinuous2DGlobe() && annotation instanceof ScreenAnnotation)
        {
            if (dc.isPickingMode() && this.currentPickAnnotations.contains(annotation))
                return;

            if (currentDrawAnnotations.contains(annotation))
                return;
        }

        double altitude = dc.getView().getEyePosition().getElevation();
        if (altitude < annotation.getMinActiveAltitude() || altitude > annotation.getMaxActiveAltitude())
            return;

        double eyeDistance = 1;
        if (annotation instanceof Locatable)
        {
            if (annotationPoint == null)
            {
                Position pos = ((Locatable) annotation).getPosition();

                if (!dc.getVisibleSector().contains(pos.getLatitude(), pos.getLongitude()))
                    return;

                // Determine Cartesian position from the surface geometry if the annotation is near the surface,
                // otherwise draw it from the globe.
                annotationPoint = getAnnotationDrawPoint(dc, annotation);
                if (annotationPoint == null)
                    return;
            }

            if (!dc.getView().getFrustumInModelCoordinates().contains(annotationPoint))
                return;

            if (!dc.isContinuous2DGlobe())
            {
                double horizon = dc.getView().getHorizonDistance();
                eyeDistance = annotation.isAlwaysOnTop() ? 0 : dc.getView().getEyePoint().distanceTo3(annotationPoint);
                if (eyeDistance > horizon)
                    return;
            }
        }

        if (annotation instanceof ScreenAnnotation)
        {
            Rectangle screenBounds = annotation.getBounds(dc);
            if (screenBounds != null && !dc.getView().getViewport().intersects(screenBounds))
                return;
        }

        // The annotation isn't drawn here, but added to the ordered queue to be drawn back-to-front.
        dc.addOrderedRenderable(new OrderedAnnotation(annotation, layer, eyeDistance));

        if (dc.isContinuous2DGlobe() && annotation instanceof ScreenAnnotation)
        {
            if (dc.isPickingMode())
                this.currentPickAnnotations.add(annotation);
            else
                this.currentDrawAnnotations.add(annotation);
        }
    }

    protected boolean isAtPickRange(DrawContext dc, Annotation annotation)
    {
        Rectangle screenBounds = annotation.getBounds(dc);
        return screenBounds != null && dc.getPickFrustums().intersectsAny(screenBounds);
    }

    /**
     * Get the final Vec4 point at which an annotation will be drawn. If the annotation Position elevation is lower then
     * the highest elevation on the globe, it will be drawn above the ground using its elevation as an offset.
     * Otherwise, the original elevation will be used.
     *
     * @param dc         the current DrawContext.
     * @param annotation the annotation
     *
     * @return the annotation draw cartesian point
     */
    protected Vec4 getAnnotationDrawPoint(DrawContext dc, Annotation annotation)
    {
        Vec4 drawPoint = null;
        if (annotation instanceof Locatable)
        {
            Position pos = ((Locatable) annotation).getPosition();
            if (pos.getElevation() < dc.getGlobe().getMaxElevation())
                drawPoint = dc.getSurfaceGeometry().getSurfacePoint(pos.getLatitude(), pos.getLongitude(),
                    pos.getElevation());
            if (drawPoint == null)
                drawPoint = dc.getGlobe().computePointFromPosition(pos);
        }
        return drawPoint;
    }

    protected class OrderedAnnotation implements OrderedRenderable
    {
        protected Annotation annotation;
        protected double eyeDistance;
        protected Layer layer;

        public OrderedAnnotation(Annotation annotation, double eyeDistance)
        {
            this.annotation = annotation;
            this.eyeDistance = eyeDistance;
        }

        public OrderedAnnotation(Annotation annotation, Layer layer, double eyeDistance)
        {
            this.annotation = annotation;
            this.eyeDistance = eyeDistance;
            this.layer = layer;
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public void render(DrawContext dc)
        {
            OGLStackHandler stackHandler = new OGLStackHandler();
            BasicAnnotationRenderer.this.beginDrawAnnotations(dc, stackHandler);
            try
            {
                this.doRender(dc, this);
                // Draw as many as we can in a batch to save ogl state switching.
                while (dc.peekOrderedRenderables() instanceof OrderedAnnotation)
                {
                    OrderedAnnotation oa = (OrderedAnnotation) dc.pollOrderedRenderables();
                    this.doRender(dc, oa);
                }
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingAnnotation", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingAnnotation", e);
            }
            finally
            {
                BasicAnnotationRenderer.this.endDrawAnnotations(dc, stackHandler);
            }
        }

        public void pick(DrawContext dc, java.awt.Point pickPoint)
        {
            OGLStackHandler stackHandler = new OGLStackHandler();
            BasicAnnotationRenderer.this.pickSupport.clearPickList();
            BasicAnnotationRenderer.this.beginDrawAnnotations(dc, stackHandler);
            try
            {
                this.annotation.setPickSupport(BasicAnnotationRenderer.this.pickSupport);
                this.doRender(dc, this);
                // Draw as many as we can in a batch to save ogl state switching.
                while (dc.peekOrderedRenderables() instanceof OrderedAnnotation)
                {
                    OrderedAnnotation oa = (OrderedAnnotation) dc.pollOrderedRenderables();
                    oa.annotation.setPickSupport(BasicAnnotationRenderer.this.pickSupport);
                    this.doRender(dc, oa);
                }
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhilePickingAnnotation", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhilePickingAnnotation", e);
            }
            finally
            {
                BasicAnnotationRenderer.this.endDrawAnnotations(dc, stackHandler);
                BasicAnnotationRenderer.this.pickSupport.resolvePick(dc, pickPoint, this.layer);
                BasicAnnotationRenderer.this.pickSupport.clearPickList(); // to ensure entries can be garbage collected
            }
        }

        protected void doRender(DrawContext dc, OrderedAnnotation oa)
        {
            // Swap the draw context's current layer with that of the ordered annotation
            Layer previousCurrentLayer = dc.getCurrentLayer();
            try
            {
                dc.setCurrentLayer(oa.layer);
                oa.annotation.renderNow(dc);
            }
            finally
            {
                dc.setCurrentLayer(previousCurrentLayer); // restore the original layer
            }
        }
    }

    protected void beginDrawAnnotations(DrawContext dc, OGLStackHandler stackHandler)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attributeMask = GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, blend func
            | GL2.GL_CURRENT_BIT // for current color
            | GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask, depth func
            | GL2.GL_ENABLE_BIT // for enable/disable changes
            | GL2.GL_HINT_BIT // for line smoothing hint
            | GL2.GL_LINE_BIT // for line width, line stipple
            | GL2.GL_TRANSFORM_BIT // for matrix mode
            | GL2.GL_VIEWPORT_BIT; // for viewport, depth range
        stackHandler.pushAttrib(gl, attributeMask);

        // Load a parallel projection with dimensions (viewportWidth, viewportHeight)
        stackHandler.pushProjectionIdentity(gl);
        gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

        // Push identity matrices on the texture and modelview matrix stacks. Leave the matrix mode as modelview.
        stackHandler.pushTextureIdentity(gl);
        stackHandler.pushModelviewIdentity(gl);

        // Enable the alpha test.
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.0f);

        // Apply the depth buffer but don't change it.
        if ((!dc.isDeepPickingEnabled()))
            gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Disable lighting and backface culling.
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_CULL_FACE);

        if (!dc.isPickingMode())
        {
            // Enable blending in premultiplied color mode.
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, true);
        }
        else
        {
            this.pickSupport.beginPicking(dc);
        }
    }

    protected void endDrawAnnotations(DrawContext dc, OGLStackHandler stackHandler)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            this.pickSupport.endPicking(dc);
        }

        stackHandler.pop(gl);
    }
//
//
//    //-- Collision avoidance ---------------------------------------------------
//    ArrayList<Rectangle> usedRectangles = new ArrayList<Rectangle>();
//    Point defaultDrawOffset = new Point(-10, 20);
//
//    // Try to find a free rectangular space around a point
//    // TODO: Fix me
//    private Point computeOffset(Point point, Dimension dimension)
//    {
//        Point offset = this.defaultDrawOffset;
//        Rectangle r = new Rectangle(point.x + offset.x - dimension.width / 2,
//                point.y + offset.y + dimension.height,
//                dimension.width, dimension.height);
//        double radius = 20;
//        Angle angle = Angle.ZERO;
//        int step = 0;
//        int angleStep = 1;
//        while(rectangleIntersectsUsed(r))
//        {
//            // Give up after some number of tries
//            if(step++ > 100)
//            {
//                usedRectangles.clear();
//                return this.defaultDrawOffset;
//            }
//
//            // Increment angle and radius
//            int a = 90 + (10 * (angleStep / 2) * (angleStep % 2 == 0 ? 1 : -1));
//            if(Math.abs(a) <= 10)
//            {
//                angleStep = 1;
//                radius += 50;
//            }
//            else
//                angleStep++;
//
//            // Compute new rectangle
//            angle = Angle.fromDegrees(a);
//            offset.x = (int)(radius * angle.cos());
//            offset.y = (int)(radius * angle.sin());
//            r.setBounds(point.x + offset.x - dimension.width / 2,
//                    point.y + offset.y + dimension.height,
//                    dimension.width, dimension.height);
//        }
//
//        // Keep track of used rectangle
//        this.usedRectangles.add(r);
//
//        return offset;
//    }
//
//    // Test if a rectangle intersects one of the previously used rectangles
//    private boolean rectangleIntersectsUsed(Rectangle r)
//    {
//        for(Rectangle ur : this.usedRectangles)
//            if(r.intersects(ur))
//                return true;
//        return false;
//    }
}
