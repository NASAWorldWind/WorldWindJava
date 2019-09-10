/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Renders an icon image over the terrain surface.
 *
 * @author Patrick Murris
 * @version $Id: SurfaceIcon.java 1772 2013-12-18 02:43:27Z tgaskins $
 */
public class SurfaceIcon extends AbstractSurfaceRenderable implements Movable, Draggable
{
    private Object imageSource;
    private boolean useMipMaps = true;
    private LatLon location;
    private Vec4 locationOffset;                    // Pixels
    private double scale = 1d;
    private Angle heading = Angle.ZERO;             // CW from north
    private Color color = Color.WHITE;
    private boolean maintainSize = false;
    private double maxSize = Double.MAX_VALUE;      // Meter
    private double minSize = .1;                    // Meter

    protected WWTexture texture;
    protected int imageWidth = 32;
    protected int imageHeight = 32;
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;

    public SurfaceIcon(Object imageSource)
    {
        this(imageSource, null);
    }

    public SurfaceIcon(Object imageSource, LatLon location)
    {
        this.setImageSource(imageSource);
        if (location != null)
            this.setLocation(location);
    }

    /**
     * Get the icon reference location on the globe.
     *
     * @return the icon reference location on the globe.
     */
    public LatLon getLocation()
    {
        return this.location;
    }

    /**
     * Set the icon reference location on the globe.
     *
     * @param location the icon reference location on the globe.
     *
     * @throws IllegalArgumentException if location is <code>null</code>.
     */
    public void setLocation(LatLon location)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.location = location;
        this.onShapeChanged();
    }

    /**
     * Get the icon displacement in pixels relative to the reference location.  Can be <code>null</code>.
     * <p>
     * When <code>null</code> the icon will be drawn with it's image center on top of it's reference location - see
     * {@link #setLocation(LatLon)}. Otherwise the icon will be shifted of a distance equivalent to the number of pixels
     * specified as <code>x</code> and <code>y</code> offset values. Positive values will move the icon to the right for
     * <code>x</code> and up for <code>y</code>. Negative values will have the opposite effect.
     *
     * @return the icon displacement in pixels relative to the reference location.
     */
    public Vec4 getLocationOffset()
    {
        return this.locationOffset;
    }

    /**
     * Set the icon displacement in pixels relative to the reference location. Can be <code>null</code>.
     * <p>
     * When <code>null</code> the icon will be drawn with it's image center on top of it's refence location - see {@link
     * #setLocation(LatLon)}. Otherwise the icon will be shifted of a distance equivalent to the number of pixels
     * specified as <code>x</code> and <code>y</code> offset values. Positive values will move the icon to the right for
     * <code>x</code> and up for <code>y</code>. Negative values will have the opposite effect.
     *
     * @param locationOffset the icon displacement in pixels relative to the reference location.
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset; // can be null
        this.onShapeChanged();
    }

    /**
     * Get the source for the icon image. Can be a file path to a local image or a {@link java.awt.image.BufferedImage}
     * reference.
     *
     * @return the source for the icon image.
     */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Set the source for the icon image. Can be a file path to a local image or a {@link java.awt.image.BufferedImage}
     * reference.
     *
     * @param imageSource the source for the icon image.
     *
     * @throws IllegalArgumentException if imageSource is <code>null</code>.
     */
    public void setImageSource(Object imageSource)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.texture = null;
        this.onShapeChanged();
    }

    /**
     * Returns whether the icon will apply mip-map filtering to it's source image. If <code>true</code> the icon image
     * is drawn using mip-maps. If <code>false</code> the icon is drawn without mip-maps, resulting in aliasing if the
     * icon image is drawn smaller than it's native size in pixels.
     *
     * @return <code>true</code> if the icon image is drawn with mip-map filtering; <code>false</code> otherwise.
     */
    public boolean isUseMipMaps()
    {
        return this.useMipMaps;
    }

    /**
     * Sets whether the icon will apply mip-map filtering to it's source image. If <code>true</code> the icon image is
     * drawn using mip-maps. If <code>false</code> the icon is drawn without mip-maps, resulting in aliasing if the icon
     * image is drawn smaller than it's native size in pixels.
     *
     * @param useMipMaps <code>true</code> if the icon image should be drawn with mip-map filtering; <code>false</code>
     *                   otherwise.
     */
    public void setUseMipMaps(boolean useMipMaps)
    {
        this.useMipMaps = useMipMaps;
        this.texture = null;
        this.onShapeChanged();
    }

    /**
     * Get the current scaling factor applied to the source image.
     *
     * @return the current scaling factor applied to the source image.
     */
    public double getScale()
    {
        return this.scale;
    }

    /**
     * Set the scaling factor to apply to the source image. A value of <code>1</code> will produce no change, a value
     * greater then <code>1</code> will enlarge the image and a value smaller then <code>1</code> will reduce it.
     *
     * @param scale the scaling factor to apply to the source image.
     *
     * @throws IllegalArgumentException if scale is zero or negative.
     */
    public void setScale(double scale)
    {
        if (scale <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "scale must be greater then zero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.scale = scale;
        this.onShapeChanged();
    }

    /**
     * Get the current heading {@link Angle}, clockwise from North or <code>null</code>.
     *
     * @return the current heading {@link Angle}, clockwise from North or <code>null</code>.
     */
    public Angle getHeading()
    {
        return this.heading;
    }

    /**
     * Set the heading {@link Angle}, clockwise from North. Setting this value to <code>null</code> will have the icon
     * follow the view heading so as to always face the eye. The icon will rotate around it's reference location.
     *
     * @param heading the heading {@link Angle}, clockwise from North or <code>null</code>.
     */
    public void setHeading(Angle heading)
    {
        this.heading = heading;  // can be null
        this.onShapeChanged();
    }

    /**
     * Determines whether the icon constantly maintains it's apparent size. If <code>true</code> the icon is constantly
     * redrawn at the proper size depending on it's distance from the eye. If <code>false</code> the icon will be drawn
     * only once per level of the underlying tile pyramid. Thus it's apparent size will vary up to twice it's 'normal'
     * dimension in between levels.
     *
     * @return <code>true</code> if the icon constantly maintains it's apparent size.
     */
    public boolean isMaintainSize()
    {
        return this.maintainSize;
    }

    /**
     * Sets whether the icon constantly maintains it's apparent size. If <code>true</code> the icon is constantly
     * redrawn at the proper size depending on it's distance from the eye. If <code>false</code> the icon will be drawn
     * only once per level of the underlying tile pyramid. Thus it's apparent size will vary up to twice it's 'normal'
     * dimension in between levels.
     *
     * @param state <code>true</code> if the icon should constantly maintains it's apparent size.
     */
    public void setMaintainSize(boolean state)
    {
        this.maintainSize = state;
    }

    /**
     * Get the minimum size in meter the icon image is allowed to be reduced to once applied to the terrain surface.
     * This limit applies to the source image largest dimension.
     * <p>
     * The icon will try to maintain it's apparent size depending on it's distance from the eye and will extend over a
     * rectangular area which largest dimension is bounded by the values provided with {@link #setMinSize(double)} and
     * {@link #setMaxSize(double)}.
     *
     * @return the minimum size of the icon in meter.
     */
    public double getMinSize()
    {
        return this.minSize;
    }

    /**
     * Set the minimum size in meter the icon image is allowed to be reduced to once applied to the terrain surface.
     * This limit applies to the source image largest dimension.
     * <p>
     * The icon will try to maintain it's apparent size depending on it's distance from the eye and will extend over a
     * rectangular area which largest dimension is bounded by the values provided with <code>setMinSize(double)</code>
     * and {@link #setMaxSize(double)}.
     *
     * @param sizeInMeter the minimum size of the icon in meter.
     */
    public void setMinSize(double sizeInMeter)
    {
        this.minSize = sizeInMeter;
        this.onShapeChanged();
    }

    /**
     * Get the maximum size in meter the icon image is allowed to be enlarged to once applied to the terrain surface.
     * This limit applies to the source image largest dimension.
     * <p>
     * The icon will try to maintain it's apparent size depending on it's distance from the eye and will extend over a
     * rectangular area which largest dimension is bounded by the values provided with {@link #setMinSize(double)} and
     * {@link #setMaxSize(double)}.
     *
     * @return the maximum size of the icon in meter.
     */
    public double getMaxSize()
    {
        return this.maxSize;
    }

    /**
     * Get the maximum size in meter the icon image is allowed to be enlarged to once applied to the terrain surface.
     * This limit applies to the source image largest dimension.
     * <p>
     * The icon will try to maintain it's apparent size depending on it's distance from the eye and will extend over a
     * rectangular area which largest dimension is bounded by the values provided with {@link #setMinSize(double)} and
     * <code>setMaxSize(double)</code>.
     *
     * @param sizeInMeter the maximum size of the icon in meter.
     */
    public void setMaxSize(double sizeInMeter)
    {
        this.maxSize = sizeInMeter;
        this.onShapeChanged();
    }

    /**
     * Get the {@link Color} the source image is combined with.
     *
     * @return the {@link Color} the source image is combined with.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the {@link Color} the source image will be combined with - default to white.
     * <p>
     * A non white color will mostly affect the white portions from the original image. This is mostly useful to alter
     * the appearance of 'colorless' icons - which mainly contain black, white and shades of gray.
     *
     * @param color the {@link Color} the source image will be combined with.
     *
     * @throws IllegalArgumentException if color is <code>null</code>.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.color = color;
        this.onShapeChanged();
    }

    protected boolean isMaintainAppearance()
    {
        return this.getHeading() == null || this.isMaintainSize();  // always facing or constant size
    }

    // *** SurfaceObject interface ***

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to return a unique state key if the icon is configured to always redraw. SurfaceIcon does not use a
     * cached representation if it's heading is configured to follow the view, or if it's configured to maintain a
     * constant screen size.
     *
     * @see #getHeading()
     * @see #isMaintainSize()
     */
    @Override
    public Object getStateKey(DrawContext dc)
    {
        // If the icon always redraws, return a unique object that is not equivalent to any other state key.
        if (this.isMaintainAppearance())
            return new Object();

        return super.getStateKey(dc);
    }

    public List<Sector> getSectors(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.computeSectors(dc);
    }

    public Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.computeExtent(dc);
    }

    public void drawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        WWTexture texture = getTexture();
        if (texture == null)
            return;

        this.beginDraw(dc);
        try
        {
            if (texture.bind(dc))
            {
                // Update image width and height
                this.imageWidth = texture.getWidth(dc);
                this.imageHeight = texture.getHeight(dc);

                // Apply texture local transform
                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glMatrixMode(GL.GL_TEXTURE);
                this.getTexture().applyInternalTransform(dc);

                // Apply draw color
                this.applyDrawColor(dc);

                //Draw
                this.drawIcon(dc, sdc);
            }
        }
        catch (Exception e)
        {
            // TODO: log error
        }
        finally
        {
            // Restore gl state
            this.endDraw(dc);
        }
    }

    protected List<Sector> computeSectors(DrawContext dc)
    {
        if (this.location == null)
            return null;

        Globe globe = dc.getGlobe();
        // Compute real world icon extent depending on distance from eye
        Rectangle2D.Double rect = computeDrawDimension(dc, this.location); // meter
        // If the icon does not redraw all the time, double it's dimension
        if (!this.isMaintainAppearance())
        {
            rect.setRect(rect.x, rect.y, rect.width * 2, rect.height * 2);
        }
        // Compute bounding sector and apply location offset to it
        double cosLat = Math.max(this.location.getLatitude().cos(), .01); // avoids division by zero at the poles
        double dLatRadians = rect.height / globe.getRadius();
        double dLonRadians = rect.width / globe.getRadius() / cosLat;
        double offsetLatRadians = locationOffset != null ? locationOffset.y * dLatRadians / this.imageHeight : 0;
        double offsetLonRadians = locationOffset != null ? locationOffset.x * dLonRadians / this.imageWidth : 0;
        Sector sector = new Sector(
            this.location.getLatitude().subtractRadians(dLatRadians / 2).addRadians(offsetLatRadians),
            this.location.getLatitude().addRadians(dLatRadians / 2).addRadians(offsetLatRadians),
            this.location.getLongitude().subtractRadians(dLonRadians / 2).addRadians(offsetLonRadians),
            this.location.getLongitude().addRadians(dLonRadians / 2).addRadians(offsetLonRadians)
        );
        // Rotate sector around location 
        sector = computeRotatedSectorBounds(sector, this.location, computeDrawHeading(dc));

        return computeNormalizedSectors(sector);
    }

    protected Rectangle2D.Double computeDrawDimension(DrawContext dc, LatLon location)
    {
        // Compute icon extent at 1:1 depending on distance from eye
        double pixelSize = computePixelSizeAtLocation(dc, location);
        return computeDrawDimension(pixelSize);
    }

    protected Rectangle2D.Double computeDrawDimension(double pixelSize)
    {
        // Compute icon extent at 1:1 depending on target tile pixel size
        double height = this.imageHeight * this.scale * pixelSize;
        double width = this.imageWidth * this.scale * pixelSize;
        // Clamp to size range
        double size = height > width ? height : width;
        double scale = size > this.maxSize ? this.maxSize / size : size < this.minSize ? this.minSize / size : 1;

        return new Rectangle2D.Double(0, 0, width * scale, height * scale); // meter
    }

    protected Angle computeDrawHeading(DrawContext dc)
    {
        if (this.heading != null)
            return this.heading;

        return getViewHeading(dc);
    }

    protected void beginDraw(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attributeMask = GL2.GL_TRANSFORM_BIT // for modelview
            | GL2.GL_CURRENT_BIT // for current color
            | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
            | GL2.GL_ENABLE_BIT; // for enable/disable changes
        gl.glPushAttrib(attributeMask);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        if (dc.isPickingMode())
        {
            // Set up to replace the non-transparent texture colors with the single pick color.
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
        }
        else
        {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    protected void endDraw(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void applyDrawTransform(DrawContext dc, SurfaceTileDrawContext sdc, LatLon location, double drawScale)
    {
        // Compute icon viewport point
        // Apply hemisphere offset if needed - for icons that may cross the date line
        double offset = computeHemisphereOffset(sdc.getSector(), location);
        Vec4 point = new Vec4(location.getLongitude().degrees + offset, location.getLatitude().degrees, 1);
        point = point.transformBy4(sdc.getModelviewMatrix());

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        // Translate to location point
        gl.glTranslated(point.x(), point.y(), point.z());
        // Add x scaling transform to maintain icon width and aspect ratio at any latitude
        gl.glScaled(drawScale / location.getLatitude().cos(), drawScale, 1);
        // Add rotation to account for icon heading
        gl.glRotated(this.computeDrawHeading(dc).degrees, 0, 0, -1);
        // Translate to lower left corner
        gl.glTranslated(-this.imageWidth / 2, -this.imageHeight / 2, 0);
        // Apply location offset if any
        if (this.locationOffset != null)
            gl.glTranslated(this.locationOffset.x, this.locationOffset.y, 0);
    }

    protected double computeDrawScale(DrawContext dc, SurfaceTileDrawContext sdc, LatLon location)
    {
        // Compute scaling to maintain apparent size
        double drawPixelSize;
        double regionPixelSize = this.computeDrawPixelSize(dc, sdc);
        if (this.isMaintainAppearance())
            // Compute precise size depending on eye distance
            drawPixelSize = this.computeDrawDimension(dc, location).width / this.imageWidth;
        else
            // Compute size according to draw tile resolution
            drawPixelSize = this.computeDrawDimension(regionPixelSize).width / this.imageWidth;
        return drawPixelSize / regionPixelSize;
    }

    protected void applyDrawColor(DrawContext dc)
    {
        if (!dc.isPickingMode())
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            applyPremultipliedAlphaColor(gl, this.color, getOpacity());
        }
    }

    protected void drawIcon(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        double drawScale = this.computeDrawScale(dc, sdc, this.location);
        this.applyDrawTransform(dc, sdc, this.location, drawScale);
        gl.glScaled(this.imageWidth, this.imageHeight, 1d);
        dc.drawUnitQuad(new TextureCoords(0, 0, 1, 1));
    }

    protected WWTexture getTexture()
    {
        if (this.texture == null)
            this.texture = new BasicWWTexture(this.imageSource, this.useMipMaps);

        return this.texture;
    }

    // *** Movable interface

    public Position getReferencePosition()
    {
        return new Position(this.location, 0);
    }

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position referencePos = this.getReferencePosition();
        if (referencePos == null)
            return;

        this.moveTo(referencePos.add(delta));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setLocation(position);
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
            this.draggableSupport = new DraggableSupport(this, WorldWind.CLAMP_TO_GROUND);

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragGlobeSizeConstant(dragContext);
    }

    //**************************************************************//
    //********************  Sector Cache Info  *********************//
    //**************************************************************//

    protected static class SectorInfo
    {
        protected List<Sector> sectors;
        protected Object globeStateKey;

        public SectorInfo(List<Sector> sectors, DrawContext dc)
        {
            // Surface icon sectors depend on the state of the globe used to compute it.
            this.sectors = sectors;
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
        }

        public boolean isValid(DrawContext dc)
        {
            return this.globeStateKey.equals(dc.getGlobe().getStateKey(dc));
        }
    }
}
