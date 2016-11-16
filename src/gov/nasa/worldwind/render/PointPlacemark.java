/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.xml.stream.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import static gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil.kmlBoolean;

/**
 * Represents a point placemark consisting of an image, an optional line linking the image to a corresponding point on
 * the terrain, and an optional label. The image and the label are displayed in the plane of the screen.
 * <p/>
 * Point placemarks have separate attributes for normal rendering and highlighted rendering. If highlighting is
 * requested but no highlight attributes are specified, the normal attributes are used. If the normal attributes are not
 * specified, default attributes are used. See {@link #getDefaultAttributes()}.
 * <p/>
 * This class implements and extends the functionality of a KML <i>Point</i>.
 * <p/>
 * Point placemarks can participate in global text decluttering by setting their decluttering-enabled flag to {@code
 * true}. See {@link #setEnableDecluttering(boolean)}. The default for this flag is {@code false}. When participating in
 * decluttering, only the point placemark's label is considered when determining interference with other text.
 * <p/>
 * When the label of a point placemark is picked, the associated {@link gov.nasa.worldwind.pick.PickedObject} contains
 * the key {@link AVKey#LABEL}
 *
 * @author tag
 * @version $Id: PointPlacemark.java 3028 2015-04-17 00:10:19Z tgaskins $
 */
public class PointPlacemark extends WWObjectImpl
    implements Renderable, Locatable, Movable, Highlightable, Exportable, Draggable
{
    /**
     * An interface to enable application selection of placemark level of detail.
     */
    public interface LODSelector
    {
        /**
         * Modifies the placemark's attributes and properties to achieve a desired level of detail during rendering. This
         * method is called during rendering in order to provide the application an opportunity to adjust the placemark's
         * attributes and properties to achieve a level of detail based on the placemark's distance from the view's eye
         * point or other criteria.
         *
         * @param dc          the current draw context.
         * @param placemark   the placemark about to be rendered.
         * @param eyeDistance the distance in meters from the view's eye point to the placemark's geographic position.
         */
        void selectLOD(DrawContext dc, PointPlacemark placemark, double eyeDistance);
    }

    /** The scale to use when highlighting if no highlight attributes are specified. */
    protected static final Double DEFAULT_HIGHLIGHT_SCALE = 1.3;
    /** The label offset to use if none is specified but an image has been specified. */
    protected static final Offset DEFAULT_LABEL_OFFSET_IF_UNSPECIFIED = new Offset(1d, 0.6d, AVKey.FRACTION,
        AVKey.FRACTION);
    /** The point size to use when none is specified. */
    protected static final Double DEFAULT_POINT_SIZE = 5d;
    /**
     * The address of the transparent image used when attributes.isDrawImage is false.
     */
    protected static final String TRANSPARENT_IMAGE_ADDRESS = "images/transparent2x2.png";

    // Label picking needs to add padding around the label to make it easier to pick.
    protected static final int PICK_Y_OFFSET = -5;
    protected static final int PICK_Y_SIZE_DELTA = 2;

    /** The attributes used if attributes are not specified. */
    protected static final PointPlacemarkAttributes defaultAttributes = new PointPlacemarkAttributes();

    static
    {
        defaultAttributes.setImageAddress(PointPlacemarkAttributes.DEFAULT_IMAGE_PATH);
        defaultAttributes.setImageOffset(PointPlacemarkAttributes.DEFAULT_IMAGE_OFFSET);
        defaultAttributes.setLabelOffset(PointPlacemarkAttributes.DEFAULT_LABEL_OFFSET);
        defaultAttributes.setScale(PointPlacemarkAttributes.DEFAULT_IMAGE_SCALE);
        defaultAttributes.setLabelScale(PointPlacemarkAttributes.DEFAULT_LABEL_SCALE);
    }

    public class OrderedPlacemark implements OrderedRenderable, Declutterable
    {
        protected Vec4 placePoint; // the Cartesian point corresponding to the placemark position
        protected Vec4 terrainPoint; // point on the terrain extruded from the placemark position.
        protected Vec4 screenPoint; // the projection of the place-point in the viewport (on the screen)
        protected double eyeDistance; // used to order the placemark as an ordered renderable
        protected Rectangle imageBounds;

        public PointPlacemark getPlacemark()
        {
            return PointPlacemark.this;
        }

        @Override
        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public Vec4 getScreenPoint()
        {
            return this.screenPoint;
        }

        public boolean isEnableBatchRendering()
        {
            return PointPlacemark.this.isEnableBatchRendering();
        }

        public boolean isEnableBatchPicking()
        {
            return PointPlacemark.this.isEnableBatchPicking();
        }

        public Layer getPickLayer()
        {
            return PointPlacemark.this.pickLayer;
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint)
        {
            PointPlacemark.this.pick(dc, pickPoint, this);
        }

        @Override
        public void render(DrawContext dc)
        {
            PointPlacemark.this.drawOrderedRenderable(dc, this);
        }

        protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
        {
            PointPlacemark.this.doDrawOrderedRenderable(dc, pickCandidates, this);
        }

        @Override
        public boolean isEnableDecluttering()
        {
            return PointPlacemark.this.isEnableDecluttering();
        }

        @Override
        public Rectangle2D getBounds(DrawContext dc)
        {
            return PointPlacemark.this.getLabelBounds(dc, this);
        }

        public Rectangle getImageBounds()
        {
            return imageBounds;
        }

        public Vec4 getPlacePoint()
        {
            return placePoint;
        }

        public Vec4 getTerrainPoint()
        {
            return terrainPoint;
        }
    }

    protected Position position;
    protected String labelText;
    protected PointPlacemarkAttributes normalAttrs;
    protected PointPlacemarkAttributes highlightAttrs;
    protected PointPlacemarkAttributes activeAttributes = new PointPlacemarkAttributes(); // re-determined each frame
    protected Map<String, WWTexture> textures = new HashMap<String, WWTexture>(); // holds the textures created
    protected WWTexture activeTexture; // determined each frame

    protected boolean highlighted;
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;
    protected boolean visible = true;
    protected int altitudeMode = WorldWind.CLAMP_TO_GROUND;
    protected boolean lineEnabled;
    protected boolean applyVerticalExaggeration = true;
    protected int linePickWidth = 10;
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected Object delegateOwner;
    protected boolean clipToHorizon = true;
    protected boolean enableDecluttering = false;
    protected boolean enableLabelPicking = false;
    protected boolean alwaysOnTop = false;
    protected LODSelector LODSelector = null;

    // Values computed once per frame and reused during the frame as needed.
    protected long frameNumber = -1; // identifies frame used to calculate these values
    protected Vec4 placePoint; // the Cartesian point corresponding to the placemark position
    protected Vec4 terrainPoint; // point on the terrain extruded from the placemark position.
    protected Vec4 screenPoint; // the projection of the place-point in the viewport (on the screen)
    protected double eyeDistance; // used to order the placemark as an ordered renderable
    protected double dx; // offsets needed to position image relative to the placemark position
    protected double dy;
    protected Layer pickLayer; // shape's layer when ordered renderable was created

    protected PickSupport pickSupport = new PickSupport();

    /**
     * Construct a point placemark.
     *
     * @param position the placemark position.
     *
     * @throws IllegalArgumentException if the position is null.
     */
    public PointPlacemark(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
    }

    /**
     * Sets the placemark's position.
     *
     * @param position the placemark position.
     *
     * @throws IllegalArgumentException if the position is null.
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
    }

    /**
     * Returns the placemark's position.
     *
     * @return the placemark's position.
     */
    public Position getPosition()
    {
        return this.position;
    }

    /**
     * Indicates whether the placemark is drawn when in view.
     *
     * @return true if the placemark is drawn when in view, otherwise false.
     */
    public boolean isVisible()
    {
        return this.visible;
    }

    /**
     * Specifies whether the placemark is drawn when in view.
     *
     * @param visible true if the placemark is drawn when in view, otherwise false.
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Returns the placemark's altitude mode. See {@link #setAltitudeMode(int)} for a description of the modes.
     *
     * @return the placemark's altitude mode.
     */
    public int getAltitudeMode()
    {
        return this.altitudeMode;
    }

    /**
     * Specifies the placemark's altitude mode. Recognized modes are: <ul> <li><b>@link WorldWind#CLAMP_TO_GROUND}</b>
     * -- the point is placed on the terrain at the latitude and longitude of its position.</li> <li><b>@link
     * WorldWind#RELATIVE_TO_GROUND}</b> -- the point is placed above the terrain at the latitude and longitude of its
     * position and the distance specified by its elevation.</li> <li><b>{@link WorldWind#ABSOLUTE}</b> -- the point is
     * placed at its specified position. </ul>
     *
     * @param altitudeMode the altitude mode
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    /**
     * Returns the distance from the current view's eye point to the placemark.
     *
     * @return the distance from the placemark to the current view's eye point.
     */
    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }

    /**
     * Indicates whether a line from the placemark point to the corresponding position on the terrain is drawn.
     *
     * @return true if the line is drawn, otherwise false.
     */
    public boolean isLineEnabled()
    {
        return lineEnabled;
    }

    /**
     * Specifies whether a line from the placemark point to the corresponding position on the terrain is drawn.
     *
     * @param lineEnabled true if the line is drawn, otherwise false.
     */
    public void setLineEnabled(boolean lineEnabled)
    {
        this.lineEnabled = lineEnabled;
    }

    /**
     * Specifies the attributes used when the placemark is drawn normally, not highlighted.
     *
     * @param attrs the attributes to use in normal mode. May be null to indicate use of default attributes.
     */
    public void setAttributes(PointPlacemarkAttributes attrs)
    {
        if (this.normalAttrs != null && this.normalAttrs.getImageAddress() != null)
            this.textures.remove(this.normalAttrs.getImageAddress());

        this.normalAttrs = attrs;
    }

    /**
     * Returns the attributes used when the placemark is drawn normally, not highlighted.
     *
     * @return the attributes used in normal mode. May be null to indicate use of default attributes.
     */
    public PointPlacemarkAttributes getAttributes()
    {
        return this.normalAttrs;
    }

    /**
     * Specifies the attributes used to draw the placemark when it's highlighted.
     *
     * @param attrs the attributes to use in normal mode. May be null to indicate use of the normal attributes.
     */
    public void setHighlightAttributes(PointPlacemarkAttributes attrs)
    {
        if (this.highlightAttrs != null && this.highlightAttrs.getImageAddress() != null)
            this.textures.remove(this.highlightAttrs.getImageAddress());

        this.highlightAttrs = attrs;
    }

    /**
     * Returns the attributes used to draw the placemark when it's highlighted.
     *
     * @return the attributes used in normal mode. May be null to indicate use of the normal attributes.
     */
    public PointPlacemarkAttributes getHighlightAttributes()
    {
        return this.highlightAttrs;
    }

    /**
     * Returns the attributes used if normal attributes are not specified.
     *
     * @return the default attributes.
     */
    public PointPlacemarkAttributes getDefaultAttributes()
    {
        return defaultAttributes;
    }

    /**
     * Indicates whether the placemark is drawn highlighted.
     *
     * @return true if the placemark is drawn highlighted, otherwise false.
     */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /**
     * Specfies whether the placemark is drawn highlighted.
     *
     * @param highlighted true if the placemark is drawn highlighted, otherwise false.
     */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /**
     * Returns the placemark's label text.
     *
     * @return the placemark's label next, which man be null.
     */
    public String getLabelText()
    {
        return labelText;
    }

    /**
     * Specifies the placemark's label text that is displayed alongside the placemark.
     *
     * @param labelText the placemark label text. If null, no label is displayed.
     */
    public void setLabelText(String labelText)
    {
        this.labelText = labelText != null ? labelText.trim() : null;
    }

    public boolean isApplyVerticalExaggeration()
    {
        return applyVerticalExaggeration;
    }

    public void setApplyVerticalExaggeration(boolean applyVerticalExaggeration)
    {
        this.applyVerticalExaggeration = applyVerticalExaggeration;
    }

    public int getLinePickWidth()
    {
        return linePickWidth;
    }

    public void setLinePickWidth(int linePickWidth)
    {
        this.linePickWidth = linePickWidth;
    }

    /**
     * Indicates whether batch rendering is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    public boolean isEnableBatchRendering()
    {
        return enableBatchRendering;
    }

    /**
     * Specifies whether adjacent PointPlacemarks in the ordered renderable list may be rendered together if they are
     * contained in the same layer. This increases performance and there is seldom a reason to disable it.
     *
     * @param enableBatchRendering true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchPicking(boolean).
     */
    public boolean isEnableBatchPicking()
    {
        return enableBatchPicking;
    }

    /**
     * Returns the delegate owner of this placemark. If non-null, the returned object replaces the placemark as the
     * pickable object returned during picking. If null, the placemark itself is the pickable object returned during
     * picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the placemark is
     * returned during picking.
     */
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    /**
     * Specifies the delegate owner of this placemark. If non-null, the delegate owner replaces the placemark as the
     * pickable object returned during picking. If null, the placemark itself is the pickable object returned during
     * picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the placemark.
     */
    public void setDelegateOwner(Object owner)
    {
        this.delegateOwner = owner;
    }

    protected PointPlacemarkAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /**
     * Specifies whether adjacent PointPlacemarks in the ordered renderable list may be pick-tested together if they are
     * contained in the same layer. This increases performance but allows only the top-most of the placemarks to be
     * reported in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the placemarks are at the pick
     * position.
     * <p/>
     * Batch rendering ({@link #setEnableBatchRendering(boolean)}) must be enabled in order for batch picking to occur.
     *
     * @param enableBatchPicking true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    /**
     * Indicates whether this placemark is shown if it is beyond the horizon.
     *
     * @return the value of the clip-to-horizon flag. {@code true} if horizon clipping is enabled, otherwise {@code
     * false}. The default value is {@code true}.
     */
    public boolean isClipToHorizon()
    {
        return clipToHorizon;
    }

    /**
     * Specifies whether this placemark is shown if it is beyond the horizon.
     *
     * @param clipToHorizon {@code true} if this placemark should not be shown when beyond the horizon, otherwise {@code
     *                      false}.
     */
    public void setClipToHorizon(boolean clipToHorizon)
    {
        this.clipToHorizon = clipToHorizon;
    }

    /**
     * Indicates whether this placemark participates in global text decluttering.
     *
     * @return {@code true} if this placemark participates in global text decluttering, otherwise false. The default
     * value is {@code false}. Only the placemark's label is considered during decluttering.
     */
    public boolean isEnableDecluttering()
    {
        return enableDecluttering;
    }

    /**
     * Specifies whether this placemark participates in globe text decluttering.
     *
     * @param enableDecluttering {@code true} if the placemark participates in global text decluttering, otherwise
     *                           {@code false}. The default value is {@code false}. Only the placemark lable is
     *                           considered during decluttering.
     */
    public void setEnableDecluttering(boolean enableDecluttering)
    {
        this.enableDecluttering = enableDecluttering;
    }

    /**
     * Indicates whether picking considers this placemark's label.
     *
     * @return <code>true</code> if this placemark's label is considered during picking, otherwise <code>false</code>.
     */
    public boolean isEnableLabelPicking()
    {
        return enableLabelPicking;
    }

    /**
     * Specifies whether this placemark's label should be considered during picking. The default is not to consider the
     * placemark's label during picking.
     *
     * @param enableLabelPicking <code>true</code> to consider the label during picking, otherwise <code>false</code>.
     */
    public void setEnableLabelPicking(boolean enableLabelPicking)
    {
        this.enableLabelPicking = enableLabelPicking;
    }

    /**
     * Indicates the state of this placemark's always-on-top flag.
     *
     * @return <code>true</code> if the always-on-top flag is set, otherwise <code>false</code>.
     */
    public boolean isAlwaysOnTop()
    {
        return alwaysOnTop;
    }

    /**
     * Specifies whether this placemark should appear on top of other placemarks and shapes in the scene. If the flag is
     * <code>true</code>, this placemark's eye distance is set to 0 so that it will appear visually above other shapes
     * whose eye distance is greater than 0.
     *
     * @param alwaysOnTop <code>true</code> if the placemark should appear always on top, otherwise <code>false</code>.
     */
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.alwaysOnTop = alwaysOnTop;
    }

    /**
     * Indicates this placemark's level of detail selector.
     *
     * @return this placemark's level of detail selector, or null if one has not been specified.
     */
    public LODSelector getLODSelector()
    {
        return this.LODSelector;
    }

    /**
     * Specifies this placemark's level of detail selector.
     *
     * @param LODSelector the level of detail selector. May be null, the default, to indicate no level of detail
     *                    selector.
     */
    public void setLODSelector(LODSelector LODSelector)
    {
        this.LODSelector = LODSelector;
    }

    /**
     * Indicates whether a point should be drawn when the active texture is null.
     *
     * @param dc the current draw context.
     *
     * @return true if a point should be drawn, otherwise false.
     */
    @SuppressWarnings({"UnusedParameters"})
    protected boolean isDrawPoint(DrawContext dc)
    {
        return this.activeTexture == null && this.getActiveAttributes().isUsePointAsDefaultImage()
            && this.getActiveAttributes().isDrawImage();
    }

    public void pick(DrawContext dc, Point pickPoint, OrderedPlacemark opm)
    {
        // This method is called only when ordered renderables are being drawn.

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.drawOrderedRenderable(dc, opm);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    public void render(DrawContext dc)
    {
        // This render method is called twice during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. These two calls determine
        // whether to add the placemark and its optional line to the ordered renderable list during pick and render.
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getSurfaceGeometry() == null)
            return;

        if (!this.isVisible())
            return;

        if (dc.is2DGlobe())
        {
            Sector limits = ((Globe2D) dc.getGlobe()).getProjection().getProjectionLimits();
            if (limits != null && !limits.contains(this.getPosition()))
                return;
        }

        this.makeOrderedRenderable(dc);
    }

    /**
     * If the scene controller is rendering ordered renderables, this method draws this placemark's image as an ordered
     * renderable. Otherwise the method determines whether this instance should be added to the ordered renderable
     * list.
     * <p/>
     * The Cartesian and screen points of the placemark are computed during the first call per frame and re-used in
     * subsequent calls of that frame.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // The code in this method determines whether to queue an ordered renderable for the placemark
        // and its optional line.

        OrderedPlacemark opm = new OrderedPlacemark();

        // Try to re-use values already calculated this frame, unless we're rendering a continuous 2D globe.
        if (dc.getFrameTimeStamp() != this.frameNumber || dc.isContinuous2DGlobe())
        {
            this.computePlacemarkPoints(dc, opm);
            if (opm.placePoint == null || opm.screenPoint == null)
                return;

            if (this.getLODSelector() != null)
                this.getLODSelector().selectLOD(dc, this, opm.placePoint.distanceTo3(dc.getView().getEyePoint()));

            this.determineActiveAttributes();
            if (this.activeTexture == null && !this.getActiveAttributes().isUsePointAsDefaultImage())
                return;

            this.computeImageOffset(dc); // calculates offsets to align the image with the hotspot

            this.frameNumber = dc.getFrameTimeStamp();
        }
        else
        {
            opm.placePoint = this.placePoint;
            opm.screenPoint = this.screenPoint;
            opm.terrainPoint = this.terrainPoint;
            opm.eyeDistance = this.eyeDistance;
        }

        if (this.isClipToHorizon() && !dc.is2DGlobe())
        {
            // Don't draw if beyond the horizon.
            double horizon = dc.getView().getHorizonDistance();
            if (this.eyeDistance > horizon)
                return;
        }

        this.computeImageBounds(dc, opm);

        if (this.intersectsFrustum(dc, opm) || this.isDrawLine(dc, opm))
        {
            dc.addOrderedRenderable(opm); // add the image ordered renderable
        }

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();
    }

    /**
     * Determines whether the placemark image intersects the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return true if the image intersects the frustum, otherwise false.
     */
    protected boolean intersectsFrustum(DrawContext dc, OrderedPlacemark opm)
    {
        View view = dc.getView();

        // Test the placemark's model coordinate point against the near and far clipping planes.
        if (opm.placePoint != null
            && (view.getFrustumInModelCoordinates().getNear().distanceTo(opm.placePoint) < 0
            || view.getFrustumInModelCoordinates().getFar().distanceTo(opm.placePoint) < 0))
        {
            return false;
        }

        Rectangle rect = opm.getImageBounds();
        if (dc.isPickingMode())
        {
            if (this.isEnableDecluttering())
            {
                // If decluttering then we need everything within the viewport drawn.
                return view.getViewport().intersects(rect);
            }
            else
            {
                // Test image rect against pick frustums.
                if (dc.getPickFrustums().intersectsAny(rect))
                    return true;

                if (this.getLabelText() != null && this.isEnableLabelPicking())
                {
                    rect = this.getLabelBounds(dc, opm);
                    rect = new Rectangle(rect.x, rect.y + PICK_Y_OFFSET, rect.width, rect.height + PICK_Y_SIZE_DELTA);
                    if (dc.getPickFrustums().intersectsAny(rect))
                        return true;
                }
            }
        }
        else if (rect.getWidth() > 0)
        {
            return view.getViewport().intersects(rect);
        }
        else if (mustDrawLabel())
        {
            // We are drawing a label but not an image. Determine if the placemark point is visible. This case comes up
            // when the image scale is zero and the label scale is non-zero.
            return view.getViewport().contains((int) opm.screenPoint.x, (int) opm.screenPoint.y);
        }

        return false;
    }

    /**
     * Establish the OpenGL state needed to draw Paths.
     *
     * @param dc the current draw context.
     */
    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attrMask =
            GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                | GL2.GL_TRANSFORM_BIT // for modelview and perspective
                | GL2.GL_VIEWPORT_BIT // for depth range
                | GL2.GL_CURRENT_BIT // for current color
                | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL2.GL_DEPTH_BUFFER_BIT // for depth func
                | GL2.GL_ENABLE_BIT // for enable/disable changes
                | GL2.GL_HINT_BIT | GL2.GL_LINE_BIT; // for antialiasing and line attrs

        gl.glPushAttrib(attrMask);

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
        }
    }

    /**
     * Pop the state set in beginDrawing.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        gl.glPopAttrib();
    }

    /**
     * Draws the path as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void drawOrderedRenderable(DrawContext dc, OrderedPlacemark opm)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc, this.pickSupport, opm);

            if (this.isEnableBatchRendering())
                this.drawBatched(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Draws this ordered renderable and all subsequent PointPlacemark ordered renderables in the ordered renderable
     * list.
     *
     * @param dc the current draw context.
     */
    protected void drawBatched(DrawContext dc)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem instanceof OrderedPlacemark)
            {
                OrderedPlacemark opm = (OrderedPlacemark) nextItem;
                if (!opm.isEnableBatchRendering())
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                opm.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
        else if (this.isEnableBatchPicking())
        {
            while (nextItem != null && nextItem instanceof OrderedPlacemark)
            {
                OrderedPlacemark opm = (OrderedPlacemark) nextItem;
                if (!opm.isEnableBatchRendering() || !opm.isEnableBatchPicking())
                    break;

                if (opm.getPickLayer() != this.pickLayer) // batch pick only within a single layer
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                opm.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Draw this placemark as an ordered renderable. If in picking mode, add it to the picked object list of specified
     * {@link PickSupport}. The <code>PickSupport</code> may not be the one associated with this instance. During batch
     * picking the <code>PickSupport</code> of the instance initiating the batch picking is used so that all shapes
     * rendered in batch are added to the same pick list.
     *
     * @param dc             the current draw context.
     * @param pickCandidates a pick support holding the picked object list to add this shape to.
     */
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates, OrderedPlacemark opm)
    {
        if (this.isDrawLine(dc, opm))
            this.drawLine(dc, pickCandidates, opm);

        if (this.activeTexture == null)
        {
            if (this.isDrawPoint(dc))
                this.drawPoint(dc, pickCandidates, opm);
            return;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler osh = new OGLStackHandler();
        try
        {
            if (dc.isPickingMode())
            {
                // Set up to replace the non-transparent texture colors with the single pick color.
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);

                Color pickColor = dc.getUniquePickColor();
                pickCandidates.addPickableObject(this.createPickedObject(dc, pickColor));
                gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
            }
            else
            {
                gl.glEnable(GL.GL_TEXTURE_2D);
                Color color = this.getActiveAttributes().getImageColor();
                if (color == null)
                    color = PointPlacemarkAttributes.DEFAULT_IMAGE_COLOR;
                gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                    (byte) color.getAlpha());
            }

            // The image is drawn using a parallel projection.
            osh.pushProjectionIdentity(gl);
            gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

            // Apply the depth buffer but don't change it (for screen-space shapes).
            if ((!dc.isDeepPickingEnabled()))
                gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            // Suppress any fully transparent image pixels.
            gl.glEnable(GL2.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

            // Adjust depth of image to bring it slightly forward
            double depth = opm.screenPoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);

            // The image is drawn using a translated and scaled unit quad.
            // Translate to screen point and adjust to align hot spot.
            osh.pushModelviewIdentity(gl);
            gl.glTranslated(opm.screenPoint.x + this.dx, opm.screenPoint.y + this.dy, 0);

            // Compute the scale
            double xscale;
            Double scale = this.getActiveAttributes().getScale();
            if (scale != null)
                xscale = scale * this.activeTexture.getWidth(dc);
            else
                xscale = this.activeTexture.getWidth(dc);

            double yscale;
            if (scale != null)
                yscale = scale * this.activeTexture.getHeight(dc);
            else
                yscale = this.activeTexture.getHeight(dc);

            Double heading = getActiveAttributes().getHeading();
            Double pitch = getActiveAttributes().getPitch();

            // Adjust heading to be relative to globe or screen
            if (heading != null)
            {
                if (AVKey.RELATIVE_TO_GLOBE.equals(this.getActiveAttributes().getHeadingReference()))
                    heading = dc.getView().getHeading().degrees - heading;
                else
                    heading = -heading;
            }

            // Apply the heading and pitch if specified.
            if (heading != null || pitch != null)
            {
                gl.glTranslated(xscale / 2, yscale / 2, 0);
                if (pitch != null)
                    gl.glRotated(pitch, 1, 0, 0);
                if (heading != null)
                    gl.glRotated(heading, 0, 0, 1);
                gl.glTranslated(-xscale / 2, -yscale / 2, 0);
            }

            // Scale the unit quad
            gl.glScaled(xscale, yscale, 1);

            if (this.activeTexture.bind(dc))
                dc.drawUnitQuad(activeTexture.getTexCoords());

            gl.glDepthRange(0, 1); // reset depth range to the OGL default
//
//            gl.glDisable(GL.GL_TEXTURE_2D);
//            dc.drawUnitQuadOutline(); // for debugging label placement

            if (this.mustDrawLabel())
            {
                if (!dc.isPickingMode() || this.isEnableLabelPicking())
                    this.drawLabel(dc, pickCandidates, opm);
            }
        }
        finally
        {
            if (dc.isPickingMode())
            {
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
            }

            gl.glDisable(GL.GL_TEXTURE_2D);
            osh.pop(gl);
        }
    }

    /**
     * Create a {@link PickedObject} for this placemark. The PickedObject returned by this method will be added to the
     * pick list to represent the current placemark.
     *
     * @param dc        Active draw context.
     * @param pickColor Unique color for this PickedObject.
     *
     * @return A new picked object.
     */
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        Object delegateOwner = this.getDelegateOwner();
        return new PickedObject(pickColor.getRGB(), delegateOwner != null ? delegateOwner : this);
    }

    /**
     * Determines if the placemark label will be rendered.
     *
     * @return True if the label must be drawn.
     */
    protected boolean mustDrawLabel()
    {
        return this.getLabelText() != null && this.getActiveAttributes().isDrawLabel();
    }

    /**
     * Determines the screen coordinate boundaries of this placemark's label.
     *
     * @param dc  the current draw context.
     * @param opm the ordered renderable for the placemark.
     *
     * @return the label bounds, in lower-left origin screen coordinates, or null if there is no label.
     */
    protected Rectangle getLabelBounds(DrawContext dc, OrderedPlacemark opm)
    {
        if (this.getLabelText() == null)
            return null;

        Vec4 labelPoint = this.computeLabelPoint(dc, opm);

        Font font = this.getActiveAttributes().getLabelFont();
        if (font == null)

            font = PointPlacemarkAttributes.DEFAULT_LABEL_FONT;
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
        Rectangle2D bounds = textRenderer.getBounds(this.getLabelText());
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        Double labelScale = this.getActiveAttributes().getLabelScale();
        if (labelScale != null)
        {
            width *= labelScale;
            height *= labelScale;
        }

        return new Rectangle((int) labelPoint.x, (int) labelPoint.getY(), (int) Math.ceil(width),
            (int) Math.ceil(height));
    }

    /**
     * Draws the placemark's label if a label is specified.
     *
     * @param dc the current draw context.
     */
    protected void drawLabel(DrawContext dc, PickSupport pickCandidates, OrderedPlacemark opm)
    {
        if (this.getLabelText() == null)
            return;

        Color color = this.getActiveAttributes().getLabelColor();
        // Use the default color if the active attributes do not specify one.
        if (color == null)
            color = PointPlacemarkAttributes.DEFAULT_LABEL_COLOR;
        // If the label color's alpha component is 0 or less, then the label is completely transparent. Exit
        // immediately; the label does not need to be rendered.
        if (color.getAlpha() <= 0)
            return;

        // Apply the label color's alpha component to the background color. This causes both the label foreground and
        // background to blend evenly into the frame. If the alpha component is 255 we just use the pre-defined constant
        // for BLACK to avoid creating a new background color every frame.
        Color backgroundColor = (color.getAlpha() < 255 ? new Color(0, 0, 0, color.getAlpha()) : Color.BLACK);

        Vec4 labelPoint = this.computeLabelPoint(dc, opm);
        float x = (float) labelPoint.x;
        float y = (float) labelPoint.y;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        Double labelScale = this.getActiveAttributes().getLabelScale();
        if (labelScale != null)
        {
            gl.glTranslatef(x, y, 0); // Assumes matrix mode is MODELVIEW
            gl.glScaled(labelScale, labelScale, 1);
            gl.glTranslatef(-x, -y, 0);
        }

        // Do not depth buffer the label. (Placemarks beyond the horizon are culled above.)
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        Font font = this.getActiveAttributes().getLabelFont();
        if (font == null)
            font = PointPlacemarkAttributes.DEFAULT_LABEL_FONT;

        if (dc.isPickingMode())
        {
            // Pick the text box, not just the text.

            Rectangle textBounds = this.getLabelBounds(dc, opm);

            Color pickColor = dc.getUniquePickColor();
            PickedObject po = this.createPickedObject(dc, pickColor);
            po.setValue(AVKey.PICKED_OBJECT_ID, AVKey.LABEL);
            pickCandidates.addPickableObject(po);
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());

            gl.glTranslated(textBounds.getX(), textBounds.getY() + PICK_Y_OFFSET, 0);
            gl.glScaled(textBounds.getWidth(), textBounds.getHeight() + PICK_Y_SIZE_DELTA, 1);
            gl.glDisable(GL.GL_TEXTURE_2D);
            dc.drawUnitQuad();
        }
        else
        {
            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            try
            {
                textRenderer.begin3DRendering();
                textRenderer.setColor(backgroundColor);
                textRenderer.draw3D(this.getLabelText(), x + 1, y - 1, 0, 1);
                textRenderer.setColor(color);
                textRenderer.draw3D(this.getLabelText(), x, y, 0, 1);
            }
            finally
            {
                textRenderer.end3DRendering();
            }
        }
    }

    /**
     * Draws the placemark's line.
     *
     * @param dc             the current draw context.
     * @param pickCandidates the pick support object to use when adding this as a pick candidate.
     */
    protected void drawLine(DrawContext dc, PickSupport pickCandidates, OrderedPlacemark opm)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if ((!dc.isDeepPickingEnabled()))
            gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glDepthMask(true);

        try
        {
            dc.getView().pushReferenceCenter(dc, opm.placePoint); // draw relative to the place point

            this.setLineWidth(dc);
            this.setLineColor(dc, pickCandidates);

            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glVertex3d(Vec4.ZERO.x, Vec4.ZERO.y, Vec4.ZERO.z);
            gl.glVertex3d(opm.terrainPoint.x - opm.placePoint.x, opm.terrainPoint.y - opm.placePoint.y,
                opm.terrainPoint.z - opm.placePoint.z);
            gl.glEnd();
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
        }
    }

    /**
     * Draws the placemark's line.
     *
     * @param dc             the current draw context.
     * @param pickCandidates the pick support object to use when adding this as a pick candidate.
     */
    protected void drawPoint(DrawContext dc, PickSupport pickCandidates, OrderedPlacemark opm)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler osh = new OGLStackHandler();
        try
        {
            osh.pushAttrib(gl, GL2.GL_POINT_BIT);

            this.setLineColor(dc, pickCandidates);
            this.setPointSize(dc);

            // The point is drawn using a parallel projection.
            osh.pushProjectionIdentity(gl);
            gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

            osh.pushModelviewIdentity(gl);

            // Apply the depth buffer but don't change it (for screen-space shapes).
            if ((!dc.isDeepPickingEnabled()))
                gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            // Suppress any fully transparent pixels.
            gl.glEnable(GL2.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

            // Adjust depth of point to bring it slightly forward
            double depth = opm.screenPoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);

            gl.glBegin(GL2.GL_POINTS);
            gl.glVertex3d(opm.screenPoint.x, opm.screenPoint.y, 0);
            gl.glEnd();

            gl.glDepthRange(0, 1); // reset depth range to the OGL default

            if (this.mustDrawLabel())
            {
                if (!dc.isPickingMode() || this.isEnableLabelPicking())
                    this.drawLabel(dc, pickCandidates, opm);
            }
        }
        finally
        {
            osh.pop(gl);
        }
    }

    /**
     * Determines whether the placemark's optional line should be drawn and whether it intersects the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return true if the line should be drawn and it intersects the view frustum, otherwise false.
     */
    protected boolean isDrawLine(DrawContext dc, OrderedPlacemark opm)
    {
        if (!this.isLineEnabled() || dc.is2DGlobe() || this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND
            || opm.terrainPoint == null)
            return false;

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(opm.placePoint, opm.terrainPoint);
        else
            return dc.getView().getFrustumInModelCoordinates().intersectsSegment(opm.placePoint, opm.terrainPoint);
    }

    /**
     * Sets the width of the placemark's line during rendering.
     *
     * @param dc the current draw context.
     */
    protected void setLineWidth(DrawContext dc)
    {
        Double lineWidth = this.getActiveAttributes().getLineWidth();
        if (lineWidth != null)
        {
            GL gl = dc.getGL();

            if (dc.isPickingMode())
            {
                gl.glLineWidth(lineWidth.floatValue() + this.getLinePickWidth());
            }
            else
                gl.glLineWidth(lineWidth.floatValue());

            if (!dc.isPickingMode())
            {
                gl.glHint(GL.GL_LINE_SMOOTH_HINT, this.getActiveAttributes().getAntiAliasHint());
                gl.glEnable(GL.GL_LINE_SMOOTH);
            }
        }
    }

    /**
     * Sets the width of the placemark's point during rendering.
     *
     * @param dc the current draw context.
     */
    protected void setPointSize(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Double scale = this.getActiveAttributes().getScale();
        if (scale == null)
            scale = DEFAULT_POINT_SIZE;

        if (dc.isPickingMode())
            gl.glPointSize(scale.floatValue() + this.getLinePickWidth());
        else
            gl.glPointSize(scale.floatValue());

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL2.GL_POINT_SMOOTH);
            gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
        }
    }

    /**
     * Sets the color of the placemark's line during rendering.
     *
     * @param dc             the current draw context.
     * @param pickCandidates the pick support object to use when adding this as a pick candidate.
     */
    protected void setLineColor(DrawContext dc, PickSupport pickCandidates)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            Color color = this.getActiveAttributes().getLineColor();
            if (color == null)
                color = PointPlacemarkAttributes.DEFAULT_LINE_COLOR;
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) color.getAlpha());
        }
        else
        {
            Color pickColor = dc.getUniquePickColor();
            Object delegateOwner = this.getDelegateOwner();
            pickCandidates.addPickableObject(pickColor.getRGB(), delegateOwner != null ? delegateOwner : this,
                this.getPosition());
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        PointPlacemarkAttributes actAttrs = this.getActiveAttributes();

        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
            {
                actAttrs.copy(this.getHighlightAttributes());

                // Even though there are highlight attributes, there may not be an image for them, so use the normal image.
                if (WWUtil.isEmpty(actAttrs.getImageAddress())
                    && this.getAttributes() != null && !WWUtil.isEmpty(this.getAttributes().getImageAddress()))
                {
                    actAttrs.setImageAddress(this.getAttributes().getImageAddress());
                    if (this.getAttributes().getScale() != null)
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE * this.getAttributes().getScale());
                    else
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE);
                }
            }
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // for highlighting.
                if (this.getAttributes() != null)
                {
                    actAttrs.copy(this.getAttributes());
                    if (getAttributes().getScale() != null)
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE * this.getAttributes().getScale());
                    else
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE);
                }
                else
                {
                    actAttrs.copy(defaultAttributes);
                    if (defaultAttributes.getScale() != null)
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE * defaultAttributes.getScale());
                    else
                        actAttrs.setScale(DEFAULT_HIGHLIGHT_SCALE);
                }
            }
        }
        else if (this.getAttributes() != null)
        {
            actAttrs.copy(this.getAttributes());
        }
        else
        {
            actAttrs.copy(defaultAttributes);
            if (this.activeTexture == null && actAttrs.isUsePointAsDefaultImage())
            {
                actAttrs.setImageAddress(null);
                actAttrs.setScale(DEFAULT_POINT_SIZE);
            }
        }

        this.activeTexture = this.chooseTexture(actAttrs);

        if (this.activeTexture == null && actAttrs.isUsePointAsDefaultImage())
        {
            actAttrs.setImageAddress(null);
            actAttrs.setImageOffset(null);
            if (actAttrs.getScale() == null)
                actAttrs.setScale(DEFAULT_POINT_SIZE);
        }
    }

    /**
     * Determines the appropriate texture for the current availability.
     *
     * @param attrs the attributes specifying the placemark image and properties.
     *
     * @return the appropriate texture, or null if an image is not available.
     */
    protected WWTexture chooseTexture(PointPlacemarkAttributes attrs)
    {
        if (!attrs.isDrawImage())
        {
            WWTexture texture = this.textures.get(TRANSPARENT_IMAGE_ADDRESS);
            if (texture == null)
            {
                URL localUrl = WorldWind.getDataFileStore().requestFile(TRANSPARENT_IMAGE_ADDRESS);
                if (localUrl != null)
                {
                    texture = new BasicWWTexture(localUrl, true);
                    this.textures.put(TRANSPARENT_IMAGE_ADDRESS, texture);
                }
            }

            return texture;
        }

        if (!WWUtil.isEmpty(attrs.getImageAddress()))
        {
            WWTexture texture = this.textures.get(attrs.getImageAddress());
            if (texture != null)
                return texture;

            texture = this.initializeTexture(attrs.getImageAddress());
            if (texture != null)
            {
                this.textures.put(attrs.getImageAddress(), texture);
                return texture;
            }
        }

        if (this.getActiveAttributes().usePointAsDefaultImage)
            return null;

        // Use the default image if no other is defined or it's not yet available.
        WWTexture texture = this.textures.get(defaultAttributes.getImageAddress());
        this.getActiveAttributes().setImageOffset(defaultAttributes.getImageOffset());
        if (attrs.getScale() != null)
            this.getActiveAttributes().setScale(defaultAttributes.getScale() * attrs.getScale());
        else
            this.getActiveAttributes().setScale(defaultAttributes.getScale());
        if (texture == null)
        {
            URL localUrl = WorldWind.getDataFileStore().requestFile(defaultAttributes.getImageAddress());
            if (localUrl != null)
            {
                texture = new BasicWWTexture(localUrl, true);
                this.textures.put(defaultAttributes.getImageAddress(), texture);
            }
        }

        return texture;
    }

    /**
     * Load a texture. If the texture source is not available locally, this method requests the texture source and
     * returns null.
     *
     * @param address Path or URL to the image to load into a texture.
     *
     * @return The new texture, or null if the texture could not be created because the resource is not yet available
     * locally.
     */
    protected WWTexture initializeTexture(String address)
    {
        if (this.getActiveAttributes().getImage() != null)
        {
            // App has specified a buffered image.
            return new BasicWWTexture(this.getActiveAttributes().getImage(), true);
        }

        URL localUrl = WorldWind.getDataFileStore().requestFile(address);

        if (localUrl != null)
        {
            return new BasicWWTexture(localUrl, true);
        }

        return null;
    }

    /**
     * Computes and stores the placemark's Cartesian location, the Cartesian location of the corresponding point on the
     * terrain (if the altitude mode requires it), and the screen-space projection of the placemark's point. Applies the
     * placemark's altitude mode when computing the points.
     *
     * @param dc the current draw context.
     */
    protected void computePlacemarkPoints(DrawContext dc, OrderedPlacemark opm)
    {
        opm.placePoint = null;
        opm.terrainPoint = null;
        opm.screenPoint = null;

        Position pos = this.getPosition();
        if (pos == null)
            return;

        if (this.altitudeMode == WorldWind.CLAMP_TO_GROUND || dc.is2DGlobe())
        {
            opm.placePoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0);
        }
        else if (this.altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            opm.placePoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
        }
        else  // ABSOLUTE
        {
            double height = pos.getElevation()
                * (this.isApplyVerticalExaggeration() ? dc.getVerticalExaggeration() : 1);
            opm.placePoint = dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
        }

        if (opm.placePoint == null)
            return;

        // Compute a terrain point if needed.
        if (this.isLineEnabled() && this.altitudeMode != WorldWind.CLAMP_TO_GROUND && !dc.is2DGlobe())
            opm.terrainPoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0);

        // Compute the placemark point's screen location.
        opm.screenPoint = dc.getView().project(opm.placePoint);
        opm.eyeDistance = this.isAlwaysOnTop() ? 0 : opm.placePoint.distanceTo3(dc.getView().getEyePoint());

        // Cache the computed values for subsequent use in this frame.
        this.placePoint = opm.placePoint;
        this.screenPoint = opm.screenPoint;
        this.terrainPoint = opm.terrainPoint;
        this.eyeDistance = opm.eyeDistance;
    }

    /**
     * Computes the screen-space rectangle bounding the placemark image.
     *
     * @param dc  the current draw context.
     * @param opm the ordered placemark.
     *
     * @return the bounding rectangle.
     */
    protected void computeImageBounds(DrawContext dc, OrderedPlacemark opm)
    {
        double s = this.getActiveAttributes().getScale() != null ? this.getActiveAttributes().getScale() : 1;

        double width = s * (this.activeTexture != null ? this.activeTexture.getWidth(dc) : 1);
        double height = s * (this.activeTexture != null ? this.activeTexture.getHeight(dc) : 1);

        double x = opm.screenPoint.x + (this.isDrawPoint(dc) ? -0.5 * s : this.dx);
        double y = opm.screenPoint.y + (this.isDrawPoint(dc) ? -0.5 * s : this.dy);

        opm.imageBounds = new Rectangle((int) x, (int) y, (int) Math.ceil(width), (int) Math.ceil(height));
    }

    /**
     * Computes the screen coordinate (lower-left origin) location of this placemark's label.
     *
     * @param dc  the current draw context.
     * @param opm the ordered renderable for the placemark.
     *
     * @return the 2D label location, or null if there is no label.
     */
    protected Vec4 computeLabelPoint(DrawContext dc, OrderedPlacemark opm)
    {
        if (this.getLabelText() == null)
            return null;

        float x = (float) (opm.screenPoint.x + this.dx);
        float y = (float) (opm.screenPoint.y + this.dy);

        Double imageScale = this.getActiveAttributes().getScale();
        Offset os = this.getActiveAttributes().getLabelOffset();
        if (os == null)
            os = DEFAULT_LABEL_OFFSET_IF_UNSPECIFIED;
        double w = this.activeTexture != null ? this.activeTexture.getWidth(dc) : 1;
        double h = this.activeTexture != null ? this.activeTexture.getHeight(dc) : 1;
        Point.Double offset = os.computeOffset(w, h, imageScale, imageScale);
        x += offset.x;
        y += offset.y;

        return new Vec4(x, y);
    }

    protected void computeImageOffset(DrawContext dc)
    {
        // Determine the screen-space offset needed to align the image hot spot with the placemark point.
        this.dx = 0;
        this.dy = 0;

        if (this.isDrawPoint(dc))
            return;

        Offset os = this.getActiveAttributes().getImageOffset();
        if (os == null)
            return;

        double w = this.activeTexture != null ? this.activeTexture.getWidth(dc) : 1;
        double h = this.activeTexture != null ? this.activeTexture.getHeight(dc) : 1;
        Point.Double offset = os.computeOffset(w, h,
            this.getActiveAttributes().getScale(), this.getActiveAttributes().getScale());

        this.dx = -offset.x;
        this.dy = -offset.y;
    }

    /** {@inheritDoc} */
    public String isExportFormatSupported(String format)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(format))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.getPosition();
    }

    /** {@inheritDoc} */
    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position refPos = this.getReferencePosition();

        // The reference position is null if this shape has positions. With PointPlacemark, this should never happen
        // because its position must always be non-null. We check and this case anyway to handle a subclass overriding
        // getReferencePosition and returning null. In this case moving the shape by a relative delta is meaningless
        // because the shape has no geographic location. Therefore we fail softly by exiting and doing nothing.
        if (refPos == null)
            return;

        this.moveTo(refPos.add(delta));
    }

    /** {@inheritDoc} */
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setPosition(position);
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
            this.draggableSupport = new DraggableSupport(this, this.getAltitudeMode());

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragScreenSizeConstant(dragContext);
    }

    /**
     * Export the Placemark. The {@code output} object will receive the exported data. The type of this object depends
     * on the export format. The formats and object types supported by this class are:
     * <p/>
     * <pre>
     * Format                                         Supported output object types
     * ================================================================================
     * KML (application/vnd.google-earth.kml+xml)     java.io.Writer
     *                                                java.io.OutputStream
     *                                                javax.xml.stream.XMLStreamWriter
     * </pre>
     *
     * @param mimeType MIME type of desired export format.
     * @param output   An object that will receive the exported data. The type of this object depends on the export
     *                 format (see above).
     *
     * @throws IOException If an exception occurs writing to the output object.
     */
    public void export(String mimeType, Object output) throws IOException
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (output == null)
        {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            try
            {
                exportAsKML(output);
            }
            catch (XMLStreamException e)
            {
                Logging.logger().throwing(getClass().getName(), "export", e);
                throw new IOException(e);
            }
        }
        else
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Export the placemark to KML as a {@code <Placemark>} element. The {@code output} object will receive the data.
     * This object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");
        xmlWriter.writeStartElement("name");
        xmlWriter.writeCharacters(this.getLabelText());
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        String description = (String) getValue(AVKey.BALLOON_TEXT);
        if (description != null)
        {
            xmlWriter.writeStartElement("description");
            xmlWriter.writeCharacters(description);
            xmlWriter.writeEndElement();
        }

        final PointPlacemarkAttributes normalAttributes = getAttributes();
        final PointPlacemarkAttributes highlightAttributes = getHighlightAttributes();

        // Write style map
        if (normalAttributes != null || highlightAttributes != null)
        {
            xmlWriter.writeStartElement("StyleMap");
            exportAttributesAsKML(xmlWriter, KMLConstants.NORMAL, normalAttributes);
            exportAttributesAsKML(xmlWriter, KMLConstants.HIGHLIGHT, highlightAttributes);
            xmlWriter.writeEndElement(); // StyleMap
        }

        // Write geometry
        xmlWriter.writeStartElement("Point");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters(kmlBoolean(isLineEnabled()));
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        final String coordString = String.format(Locale.US, "%f,%f,%f",
            position.getLongitude().getDegrees(),
            position.getLatitude().getDegrees(),
            position.getElevation());
        xmlWriter.writeStartElement("coordinates");
        xmlWriter.writeCharacters(coordString);
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement(); // Point
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }

    /**
     * Export PointPlacemarkAttributes as KML Style element.
     *
     * @param xmlWriter  Writer to receive the Style element.
     * @param styleType  The type of style: normal or highlight. Value should match either {@link KMLConstants#NORMAL}
     *                   or {@link KMLConstants#HIGHLIGHT}
     * @param attributes Attributes to export. The method takes no action if this parameter is null.
     *
     * @throws XMLStreamException if exception occurs writing XML.
     * @throws IOException        if exception occurs exporting data.
     */
    private void exportAttributesAsKML(XMLStreamWriter xmlWriter, String styleType, PointPlacemarkAttributes attributes)
        throws XMLStreamException, IOException
    {
        if (attributes != null)
        {
            xmlWriter.writeStartElement("Pair");
            xmlWriter.writeStartElement("key");
            xmlWriter.writeCharacters(styleType);
            xmlWriter.writeEndElement();

            attributes.export(KMLConstants.KML_MIME_TYPE, xmlWriter);
            xmlWriter.writeEndElement(); // Pair
        }
    }
}
