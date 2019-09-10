/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL;
import java.awt.*;

/**
 * Represent a text label attached to a Position on the globe and its rendering attributes.
 *
 * @author Patrick Murris
 * @version $Id: GlobeAnnotation.java 2297 2014-09-05 17:05:39Z tgaskins $
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 */
public class GlobeAnnotation extends AbstractAnnotation implements Locatable, Movable, Draggable
{
    protected Position position;
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;
    protected double heightInMeter = 0;

    protected Integer altitudeMode;

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>.
     *
     * @param text     the annotation text.
     * @param position the annotation <code>Position</code>.
     */
    public GlobeAnnotation(String text, Position position)
    {
        this.init(text, position, null, null);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>. Specify
     * the <code>Font</code> to be used.
     *
     * @param text     the annotation text.
     * @param position the annotation <code>Position</code>.
     * @param font     the <code>Font</code> to use.
     */
    public GlobeAnnotation(String text, Position position, Font font)
    {
        this.init(text, position, font, null);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>. Specify
     * the <code>Font</code> and text <code>Color</code> to be used.
     *
     * @param text      the annotation text.
     * @param position  the annotation <code>Position</code>.
     * @param font      the <code>Font</code> to use.
     * @param textColor the text <code>Color</code>.
     */
    public GlobeAnnotation(String text, Position position, Font font, Color textColor)
    {
        this.init(text, position, font, textColor);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>. Specify the
     * default {@link AnnotationAttributes} set.
     *
     * @param text     the annotation text.
     * @param position the annotation <code>Position</code>.
     * @param defaults the default {@link AnnotationAttributes} set.
     */
    public GlobeAnnotation(String text, Position position, AnnotationAttributes defaults)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
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
        this.position = position;
        this.getAttributes().setDefaults(defaults);
    }

    private void init(String text, Position position, Font font, Color textColor)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.position = position;
        this.getAttributes().setFont(font);
        this.getAttributes().setTextColor(textColor);
    }

    public Position getPosition()
    {
        return this.position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    /**
     * Get the annotation's altitude mode. The altitude mode may be null, indicating that the legacy altitude mode
     * described below will be used.
     * <p>
     * <b>Legacy altitude mode</b><br>If the annotation Position elevation is lower then the highest elevation on the
     * globe, the annotation will be drawn above the ground using its elevation as an offset, scaled by the current
     * vertical exaggeration. Otherwise, the original elevation will be used. This functionality is supported for
     * backward compatibility. New code that uses Globe Annotation should specify an altitude mode.
     *
     * @return The altitude mode, one of {@link WorldWind#CLAMP_TO_GROUND}, {@link WorldWind#RELATIVE_TO_GROUND}, {@link
     *         WorldWind#ABSOLUTE}, or {@code null}.
     *
     * @see #setAltitudeMode(Integer)
     */
    public Integer getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Set the annotation's altitude mode.
     *
     * @param altitudeMode The altitude mode, one of {@link WorldWind#CLAMP_TO_GROUND}, {@link
     *                     WorldWind#RELATIVE_TO_GROUND}, or {@link WorldWind#ABSOLUTE}. {@code null} indicates that the
     *                     legacy altitude mode should be used. See {@link #getAltitudeMode()} for details on this
     *                     mode.
     *
     * @see #getAltitudeMode()
     */
    public void setAltitudeMode(Integer altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    /**
     * Returns the real world height of the annotation frame in meter. If this dimension is greater then zero, the
     * annotation will be scaled so as to maintain this fixed dimension, which makes it appear as part of the
     * surrounding terrain. This overrides min and max distance scaling - however min distance opacity is still accounted
     * for.
     * <p>
     * If this dimension is zero, the annotation always maintains the same apparent size with possible scaling relative
     * to the viewport center point if min and max distance scale factors are not one.
     *
     * @return the real world height of the annotation frame in meter.
     */
    public double getHeightInMeter()
    {
        return this.heightInMeter;
    }

    /**
     * Set the real world height of the annotation frame in meter. If this dimension is greater then zero, the
     * annotation will be scaled so as to maintain this fixed dimension, which makes it appear as part of the
     * surrounding terrain. This overrides min and max distance scaling - however min distance opacity is still accounted
     * for.
     * <p>
     * If this dimension is zero, the annotation always maintains the same apparent size with possible scaling relative
     * to the viewport center point if min and max distance scale factors are not one.
     *
     * @param meters the real world height of the annotation frame in meter.
     */
    public void setHeightInMeter(double meters)
    {
        this.heightInMeter = meters;
    }

    public void move(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.position = this.position.add(position);
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.position = position;
    }

    @Override
    public boolean isDragEnabled()
    {
        return this.dragEnabled;
    }

    @Override
    public void setDragEnabled(boolean enabled)
    {
        this.dragEnabled = enabled;
    }

    @Override
    public void drag(DragContext dragContext)
    {
        if (!this.dragEnabled)
            return;

        if (this.draggableSupport == null)
        {
            // The following addresses the special case described in {@link GlobeAnnotation#getAltitudeMode}
            if (this.getAltitudeMode() == null)
            {
                if (this.position.getElevation() > dragContext.getGlobe().getMaxElevation())
                {
                    this.draggableSupport = new DraggableSupport(this, WorldWind.ABSOLUTE);
                }
                else
                {
                    this.draggableSupport = new DraggableSupport(this, WorldWind.RELATIVE_TO_GROUND);
                }
            }
            else
            {
                this.draggableSupport = new DraggableSupport(this, this.getAltitudeMode());
            }
        }

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragScreenSizeConstant(dragContext);
    }

    public Position getReferencePosition()
    {
        return this.position;
    }

    //**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    protected Rectangle computeBounds(DrawContext dc)
    {
        Vec4 point = this.getAnnotationDrawPoint(dc);
        if (point == null)
            return null;

        Vec4 screenPoint = dc.getView().project(point);
        if (screenPoint == null)
            return null;

        java.awt.Dimension size = this.getPreferredSize(dc);
        double[] scaleAndOpacity = computeDistanceScaleAndOpacity(dc, point, size);
        double finalScale = scaleAndOpacity[0] * this.computeScale(dc);
        java.awt.Point offset = this.getAttributes().getDrawOffset();

        double offsetX = offset.x * finalScale;
        double offsetY = offset.y * finalScale;
        double width = size.width * finalScale;
        double height = size.height * finalScale;
        double x = screenPoint.x - width / 2 + offsetX;
        double y = screenPoint.y + offsetY;   // use OGL coordinate system

        Rectangle frameRect = new Rectangle((int) x, (int) y, (int) width, (int) height);

        // Include reference point in bounds
        return this.computeBoundingRectangle(frameRect, (int) screenPoint.x, (int) screenPoint.y);
    }

    protected void doRenderNow(DrawContext dc)
    {
        if (dc.isPickingMode() && this.getPickSupport() == null)
            return;

        Vec4 point = this.getAnnotationDrawPoint(dc);
        if (point == null)
            return;

        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0)
            return;

        Vec4 screenPoint = dc.getView().project(point);
        if (screenPoint == null)
            return;

        java.awt.Dimension size = this.getPreferredSize(dc);
        Position pos = dc.getGlobe().computePositionFromPoint(point);

        // Scale and opacity depending on distance from eye
        double[] scaleAndOpacity = computeDistanceScaleAndOpacity(dc, point, size);

        this.setDepthFunc(dc, screenPoint);
        this.drawTopLevelAnnotation(dc, (int) screenPoint.x, (int) screenPoint.y, size.width, size.height,
            scaleAndOpacity[0], scaleAndOpacity[1], pos);
    }

    protected double[] computeDistanceScaleAndOpacity(DrawContext dc, Vec4 point, Dimension size)
    {
        double scale = 1, opacity = 1;

        if (this.heightInMeter <= 0)
        {
            // Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
            Double lookAtDistance = this.computeLookAtDistance(dc);
            if (lookAtDistance != null)
            {
                double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
                double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
                scale = WWMath.clamp(distanceFactor,
                    this.attributes.getDistanceMinScale(), this.attributes.getDistanceMaxScale());
                opacity = WWMath.clamp(distanceFactor,
                    this.attributes.getDistanceMinOpacity(), 1);
            }
        }
        else
        {
            // Determine scale and opacity so as to maintain real world dimension
            double distance = dc.getView().getEyePoint().distanceTo3(point);
            double pixelSize = dc.getView().computePixelSizeAtDistance(distance);
            double scaledHeight = this.heightInMeter / pixelSize;
            scale = scaledHeight / size.height;
            opacity = WWMath.clamp(scale, this.attributes.getDistanceMinOpacity(), 1);
        }

        return new double[] {scale, opacity};
    }

    protected Double computeLookAtDistance(DrawContext dc)
    {
        // TODO: Remove this method once the new mechanism for scaling and opacity is in place.
        View view = dc.getView();

        // Get point in the middle of the screen
        Position groundPos = view.computePositionFromScreenPoint(
            view.getViewport().getCenterX(), view.getViewport().getCenterY());

        if (groundPos == null)
        {
            // Decrease the point's y coordinate until it intersects the globe.
            Rectangle vp = view.getViewport();
            double y = view.getViewport().getCenterY() + 1;
            while (groundPos == null && y < vp.height - 1)
            {
                groundPos = view.computePositionFromScreenPoint(view.getViewport().getCenterX(), y++);
            }
        }

        // Update look at distance if center point found
        if (groundPos != null)
            // Compute distance from eye to the position in the middle of the screen
            return view.getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(groundPos));
        else
            return null;
    }

    protected void setDepthFunc(DrawContext dc, Vec4 screenPoint)
    {
        GL gl = dc.getGL();

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
            return;
        }

        double altitude = eyePos.getElevation();
        if (altitude < (dc.getGlobe().getMaxElevation() * dc.getVerticalExaggeration()))
        {
            double depth = screenPoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);
        }
        else if (screenPoint.z >= 1d)
        {
            gl.glDepthFunc(GL.GL_EQUAL);
            gl.glDepthRange(1d, 1d);
        }
        else
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
        }
    }

    /**
     * Get the final Vec4 point at which an annotation will be drawn. The altitude mode will be used to determine the
     * annotation point, if an altitude mode has been set. If the altitude mode is null, then the legacy altitude mode
     * described in {@link #getAltitudeMode()} will be used to determine the point.
     *
     * @param dc the current DrawContext.
     *
     * @return the annotation draw Cartesian point
     *
     * @see #getAltitudeMode()
     */
    public Vec4 getAnnotationDrawPoint(DrawContext dc)
    {
        Vec4 drawPoint;

        Position pos = this.getPosition();
        Integer altitudeMode = this.getAltitudeMode();

        if (dc.is2DGlobe())
        {
            drawPoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0);
        }
        else if (altitudeMode == null)
        {
            drawPoint = getAnnotationDrawPointLegacy(dc);
        }
        else if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
        {
            drawPoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0);
        }
        else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            drawPoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
        }
        else  // ABSOLUTE
        {
            double height = pos.getElevation() * dc.getVerticalExaggeration();
            drawPoint = dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
        }

        return drawPoint;
    }

    /**
     * Compute the draw point using the legacy altitude mode. See {@link #getAltitudeMode()} for details on the legacy
     * mode.
     *
     * @param dc the current DrawContext.
     *
     * @return the annotation draw Cartesian point
     *
     * @see #getAltitudeMode()
     */
    protected Vec4 getAnnotationDrawPointLegacy(DrawContext dc)
    {
        Vec4 drawPoint = null;

        Position pos = this.getPosition();
        if (pos.getElevation() < dc.getGlobe().getMaxElevation())
            drawPoint = dc.getSurfaceGeometry().getSurfacePoint(pos.getLatitude(), pos.getLongitude(),
                pos.getElevation() * dc.getVerticalExaggeration());

        if (drawPoint == null)
            drawPoint = dc.getGlobe().computePointFromPosition(
                dc.getVerticalExaggeration() == 1 ? pos
                    : new Position(pos, pos.getElevation() * dc.getVerticalExaggeration()));

        return drawPoint;
    }

    //**************************************************************//
    //********************  Restorable State  **********************//
    //**************************************************************//

    /**
     * Returns an XML state document String describing the public attributes of this GlobeAnnotation.
     *
     * @return XML state document string describing this GlobeAnnotation.
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

        // Save the position property only if all parts (latitude, longitude, and elevation) can be saved.
        // We will not save a partial position (for example, just the elevation).
        if (this.position != null
            && this.position.getLatitude() != null
            && this.position.getLongitude() != null)
        {
            RestorableSupport.StateObject positionStateObj = restorableSupport.addStateObject("position");
            if (positionStateObj != null)
            {
                restorableSupport.addStateValueAsDouble(positionStateObj, "latitude",
                    this.position.getLatitude().degrees);
                restorableSupport.addStateValueAsDouble(positionStateObj, "longitude",
                    this.position.getLongitude().degrees);
                restorableSupport.addStateValueAsDouble(positionStateObj, "elevation",
                    this.position.getElevation());
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
     * @param stateInXml an XML document String describing a GlobeAnnotation.
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

        // Restore the position property only if all parts are available.
        // We will not restore a partial position (for example, just the latitude).
        RestorableSupport.StateObject positionStateObj = restorableSupport.getStateObject("position");
        if (positionStateObj != null)
        {
            Double latitudeState = restorableSupport.getStateValueAsDouble(positionStateObj, "latitude");
            Double longitudeState = restorableSupport.getStateValueAsDouble(positionStateObj, "longitude");
            Double elevationState = restorableSupport.getStateValueAsDouble(positionStateObj, "elevation");
            if (latitudeState != null && elevationState != null)
                setPosition(Position.fromDegrees(latitudeState, longitudeState, elevationState));
        }
    }
}
