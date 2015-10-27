/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import java.awt.*;

/**
 * Represent a text label attached to a Point on the viewport and its rendering attributes.
 *
 * @author Patrick Murris
 * @version $Id: ScreenAnnotation.java 2122 2014-07-03 03:42:25Z tgaskins $
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 */
public class ScreenAnnotation extends AbstractAnnotation
{
    protected Point screenPoint;
    protected Position position;

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position.
     *
     * @param text     the annotation text.
     * @param position the annotation viewport position.
     */
    public ScreenAnnotation(String text, Point position)
    {
        this.init(text, position, null, null);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position. Specifiy the
     * <code>Font</code> to be used.
     *
     * @param text     the annotation text.
     * @param position the annotation viewport position.
     * @param font     the <code>Font</code> to use.
     */
    public ScreenAnnotation(String text, Point position, Font font)
    {
        this.init(text, position, font, null);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position. Specifiy the
     * <code>Font</code> and text <code>Color</code> to be used.
     *
     * @param text      the annotation text.
     * @param position  the annotation viewport position.
     * @param font      the <code>Font</code> to use.
     * @param textColor the text <code>Color</code>.
     */
    public ScreenAnnotation(String text, Point position, Font font, Color textColor)
    {
        this.init(text, position, font, textColor);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position. Specify the default
     * {@link AnnotationAttributes} set.
     *
     * @param text     the annotation text.
     * @param position the annotation viewport position.
     * @param defaults the default {@link AnnotationAttributes} set.
     */
    public ScreenAnnotation(String text, Point position, AnnotationAttributes defaults)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (defaults == null)
        {
            String message = Logging.getMessage("nullValue.AnnotationAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.screenPoint = position;
        this.getAttributes().setDefaults(defaults);
        this.getAttributes().setLeader(AVKey.SHAPE_NONE);
        this.getAttributes().setDrawOffset(new Point(0, 0));
    }

    private void init(String text, Point position, Font font, Color textColor)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.screenPoint = position;
        this.getAttributes().setFont(font);
        this.getAttributes().setTextColor(textColor);
        this.getAttributes().setLeader(AVKey.SHAPE_NONE);
        this.getAttributes().setDrawOffset(new Point(0, 0));
    }

    /**
     * Get the <code>Point</code> where the annotation is drawn in the viewport.
     *
     * @return the <code>Point</code> where the annotation is drawn in the viewport.
     */
    public Point getScreenPoint()
    {
        return this.screenPoint;
    }

    /**
     * Get the <code>Point</code> where the annotation is drawn in the viewport.
     *
     * @param dc the current draw context.
     *
     * @return the <code>Point</code> where the annotation is drawn in the viewport.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Point getScreenPoint(DrawContext dc)
    {
        return this.position != null ? this.computeAnnotationPosition(dc, this.position) : this.screenPoint;
    }

    protected Point computeAnnotationPosition(DrawContext dc, Position pos)
    {
        Vec4 surfacePoint = dc.getTerrain().getSurfacePoint(pos);
        if (surfacePoint == null)
        {
            Globe globe = dc.getGlobe();
            surfacePoint = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                globe.getElevation(pos.getLatitude(), pos.getLongitude()));
        }

        Vec4 pt = dc.getView().project(surfacePoint);

        return new Point((int) pt.x, (int) pt.y);
    }

    /**
     * Set the <code>Point</code> where the annotation will be drawn in the viewport.
     *
     * @param position the <code>Point</code> where the annotation will be drawn in the viewport.
     */
    public void setScreenPoint(Point position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.screenPoint = position;
    }

    /**
     * Returns the position set via {@link #setPosition(gov.nasa.worldwind.geom.Position)}.
     *
     * @return The position previously set.
     */
    public Position getPosition()
    {
        return position;
    }

    /**
     * Specifies an optional geographic position that is mapped to a screen position during rendering. This value
     * overrides this object's screen point and computes it anew each time this annotation is drawn.
     *
     * @param position This annotation's geographic position. May be null, in which case this annotation's screen point
     *                 is used directly.
     *
     * @see #setScreenPoint(java.awt.Point)
     */
    public void setPosition(Position position)
    {
        this.position = position;
    }
//**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    protected Rectangle computeBounds(DrawContext dc)
    {
        java.awt.Dimension size = this.getPreferredSize(dc);
        double finalScale = this.computeScale(dc);
        java.awt.Point offset = this.getAttributes().getDrawOffset();

        double offsetX = offset.x * finalScale;
        double offsetY = offset.y * finalScale;
        double width = size.width * finalScale;
        double height = size.height * finalScale;

        Point sp = this.getScreenPoint(dc);
        double x = sp.x - width / 2 + offsetX;
        double y = sp.y + offsetY; // use OGL coordinate system

        Rectangle frameRect = new Rectangle((int) x, (int) y, (int) width, (int) height);

        // Include reference point in bounds
        return this.computeBoundingRectangle(frameRect, sp.x, sp.y);
    }

    protected Point computeSize(DrawContext dc)
    {
        double finalScale = this.computeScale(dc);
        java.awt.Dimension size = this.getPreferredSize(dc);

        return new Point((int) (size.width * finalScale), (int) (size.height * finalScale));
    }

    protected double[] computeOffset(DrawContext dc)
    {
        double finalScale = this.computeScale(dc);
        Point offset = this.getAttributes().getDrawOffset();

        return new double[] {offset.x * finalScale, offset.y * finalScale};
    }

    protected void doRenderNow(DrawContext dc)
    {
        if (dc.isPickingMode() && this.getPickSupport() == null)
            return;

        GL gl = dc.getGL();
        gl.glDepthFunc(GL.GL_ALWAYS);

        java.awt.Dimension size = this.getPreferredSize(dc);

        Point sp = this.getScreenPoint(dc);
        this.drawTopLevelAnnotation(dc, sp.x, sp.y, size.width, size.height, 1, 1, null);
    }

    //**************************************************************//
    //********************  Restorable State  **********************//
    //**************************************************************//

    /**
     * Returns an XML state document String describing the public attributes of this ScreenAnnotation.
     *
     * @return XML state document string describing this ScreenAnnotation.
     */
    public String getRestorableState()
    {
        RestorableSupport restorableSupport = null;

        // Try to parse the superclass' xml state document, if it defined one.
        String superStateInXml = super.getRestorableState();
        if (superStateInXml != null)
        {
            try
            {
                restorableSupport = RestorableSupport.parse(superStateInXml);
            }
            catch (Exception e)
            {
                // Parsing the document specified by the superclass failed.
                String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", superStateInXml);
                Logging.logger().severe(message);
            }
        }

        // Create our own state document from scratch.
        if (restorableSupport == null)
            restorableSupport = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (restorableSupport == null)
            return null;

        if (this.screenPoint != null)
        {
            RestorableSupport.StateObject screenPointStateObj = restorableSupport.addStateObject("screenPoint");
            if (screenPointStateObj != null)
            {
                restorableSupport.addStateValueAsDouble(screenPointStateObj, "x", this.screenPoint.getX());
                restorableSupport.addStateValueAsDouble(screenPointStateObj, "y", this.screenPoint.getY());
            }
        }

        return restorableSupport.getStateAsXml();
    }

    /**
     * Restores publicly settable attribute values found in the specified XML state document String. The document
     * specified by <code>stateInXml</code> must be a well formed XML document String, or this will throw an
     * IllegalArgumentException. Unknown structures in <code>stateInXml</code> are benign, because they will simply be
     * ignored.
     *
     * @param stateInXml an XML document String describing a ScreenAnnotation.
     *
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or if <code>stateInXml</code> is not a well
     *                                  formed XML document String.
     */
    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Allow the superclass to restore it's state.
        try
        {
            super.restoreState(stateInXml);
        }
        catch (Exception e)
        {
            // Superclass will log the exception.
        }

        RestorableSupport restorableSupport;
        try
        {
            restorableSupport = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        // Restore the screenPoint property only if all parts are available.
        // We will not restore a partial screenPoint (for example, just the x value).
        RestorableSupport.StateObject screenPointStateObj = restorableSupport.getStateObject("screenPoint");
        if (screenPointStateObj != null)
        {
            Double xState = restorableSupport.getStateValueAsDouble(screenPointStateObj, "x");
            Double yState = restorableSupport.getStateValueAsDouble(screenPointStateObj, "y");
            if (xState != null && yState != null)
                setScreenPoint(new Point(xState.intValue(), yState.intValue()));
        }
    }
}
