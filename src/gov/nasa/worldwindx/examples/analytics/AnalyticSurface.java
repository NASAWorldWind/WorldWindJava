/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.analytics;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * AnalyticSurface represents a connected grid of geographic locations, covering a specified {@link Sector} at a
 * specified base altitude in meters. The number of grid locations is defined by the AnalyticSurface's dimensions. The
 * default dimensions are <code>(10, 10)</code>. Callers specify the dimensions by using one of the constructors
 * accepting <code>width</code> and <code>height</code>, or by invoking {@link #setDimensions(int, int)}. Each grid
 * point has the following set of attributes: <ul> <li>Scalar value : the grid point's height relative to the surface's
 * base altitude, both in meters</li> <li>Color : the grid point's RGBA color components</li> </ul> Callers specify the
 * attributes at each grid point by invoking {@link #setValues(Iterable)} with an {@link Iterable} of {@link
 * GridPointAttributes}. Grid points are assigned attributes from this iterable staring at the upper left hand corner,
 * and proceeding in row-first order across the grid. The iterable should contain at least <code>width * height</code>
 * values, where width and height are the AnalyticSurface's grid dimensions. If the caller does not specify any
 * GridPointAttributes, or the caller specified iterable contains too few values, the unassigned grid points are given
 * default attributes: the default scalar value is 0, and the default color is {@link java.awt.Color#BLACK}.
 * <p/>
 * <h2>Surface Altitude</h2>
 * <p/>
 * AnalyticSurface's altitude can vary at each grid point. The altitude of each grid point depends on four properties:
 * the altitude mode, the surface altitude, the vertical scale, and the scalar value from GridPointAttributes. The
 * following table outlines how the altitude at each grid point is computed for each altitude mode:
 * <p/>
 * <table border="1"> <tr><th>Altitude Mode</th><th>Grid Point Altitude</th></tr> <tr><td>WorldWind.ABSOLUTE
 * (default)</td><td>surface altitude + (vertical scale * scalar value from GridPointAttributes)</td></tr>
 * <tr><td>WorldWind.RELATIVE_TO_GROUND</td><td>terrain height at grid point + surface altitude + (vertical scale *
 * scalar value from GridPointAttributes)</td></tr> <tr><td>WorldWind.CLAMP_TO_GROUND</td><td>terrain height at grid
 * point</td></tr> </table>
 * <p/>
 * Note that when the altitude mode is WorldWind.CLAMP_TO_GROUND the surface altitude, vertical scale, and the scalar
 * value from GridPointAttributes are ignored. In this altitude mode only the Sector, dimensions, and color from
 * GridPointAttributes are used.
 *
 * @author dcollins
 * @version $Id: AnalyticSurface.java 3020 2015-04-14 21:23:03Z dcollins $
 */
public class AnalyticSurface implements Renderable, PreRenderable
{
    /** GridPointAttributes defines the properties associated with a single grid point of an AnalyticSurface. */
    public interface GridPointAttributes
    {
        /**
         * Returns the scalar value associated with a grid point. By default, AnalyticSurface interprets this value as
         * the grid point's height relative to the AnalyticSurface's base altitude, both in meters.
         *
         * @return the grid point's scalar value.
         */
        double getValue();

        /**
         * Returns the {@link java.awt.Color} associated with a grid point. By default, AnalyticSurface interprets this
         * Color as the RGBA components of a grid point's RGBA color.
         *
         * @return the grid point's RGB color components.
         */
        java.awt.Color getColor();
    }

    protected static final double DEFAULT_ALTITUDE = 0d;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.ABSOLUTE;
    protected static final int DEFAULT_DIMENSION = 10;
    protected static final double DEFAULT_VALUE = 0d;
    protected static final Color DEFAULT_COLOR = Color.BLACK;
    protected static final GridPointAttributes DEFAULT_GRID_POINT_ATTRIBUTES = createGridPointAttributes(
        DEFAULT_VALUE, DEFAULT_COLOR);
    /** The time period between surface regeneration when altitude mode is relative-to-ground. */
    protected static final long RELATIVE_TO_GROUND_REGEN_PERIOD = 2000;

    protected boolean visible = true;
    protected Sector sector;
    protected double altitude;
    protected int altitudeMode = DEFAULT_ALTITUDE_MODE;
    protected int width;
    protected int height;
    protected Iterable<? extends GridPointAttributes> values;
    protected double verticalScale = 1d;
    protected AnalyticSurfaceAttributes surfaceAttributes = new AnalyticSurfaceAttributes();
    protected Object pickObject;
    protected Layer clientLayer;
    // Runtime rendering state.
    protected double[] extremeValues;
    protected boolean expired = true;
    protected boolean updateFailed;
    protected Object globeStateKey;
    protected long regenTime;
    // Computed surface rendering properties.
    protected Position referencePos;
    protected Vec4 referencePoint;
    protected RenderInfo surfaceRenderInfo;
    protected AnalyticSurfaceObject clampToGroundSurface;
    protected AnalyticSurfaceObject shadowSurface;
    protected final PickSupport pickSupport = new PickSupport();

    /**
     * Constructs a new AnalyticSurface with the specified {@link Sector}, base altitude in meters, grid dimensions, and
     * iterable of GridPointAttributes. The iterable should contain at least <code>with * height</code> non-null
     * GridPointAttributes.
     *
     * @param sector   the Sector which defines the surface's geographic region.
     * @param altitude the base altitude to place the surface at, in meters.
     * @param width    the surface grid width, in number of grid points.
     * @param height   the surface grid height, in number of grid points.
     * @param iterable the attributes associated with each grid point.
     *
     * @throws IllegalArgumentException if the sector is null, if the width is less than 1, if the height is less than
     *                                  1, or if the iterable is null.
     */
    public AnalyticSurface(Sector sector, double altitude, int width, int height,
        Iterable<? extends GridPointAttributes> iterable)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sector = sector;
        this.altitude = altitude;
        this.width = width;
        this.height = height;
        this.values = iterable;
        this.setExpired(true);
    }

    /**
     * Constructs a new AnalyticSurface with the specified {@link Sector}, base altitude in meters, and grid dimensions.
     * The new AnalyticSurface has the default {@link GridPointAttributes}.
     *
     * @param sector   the Sector which defines the surface's geographic region.
     * @param altitude the base altitude to place the surface at, in meters.
     * @param width    the surface grid width, in number of grid points.
     * @param height   the surface grid height, in number of grid points.
     *
     * @throws IllegalArgumentException if the sector is null, if the width is less than 1, or if the height is less
     *                                  than 1.
     */
    public AnalyticSurface(Sector sector, double altitude, int width, int height)
    {
        this(sector, altitude, width, height, createDefaultValues(width * height));
    }

    /**
     * Constructs a new AnalyticSurface with the specified {@link Sector} and base altitude in meters. The new
     * AnalyticSurface has default dimensions of <code>(10, 10)</code>, and default {@link GridPointAttributes}.
     *
     * @param sector   the Sector which defines the surface's geographic region.
     * @param altitude the base altitude to place the surface at, in meters.
     *
     * @throws IllegalArgumentException if the sector is null.
     */
    public AnalyticSurface(Sector sector, double altitude)
    {
        this(sector, altitude, DEFAULT_DIMENSION, DEFAULT_DIMENSION);
    }

    /**
     * Constructs a new AnalyticSurface with the specified grid dimensions. The new AnalyticSurface is has the default
     * Sector {@link Sector#EMPTY_SECTOR}, the default altitude of 0 meters, and default {@link GridPointAttributes}.
     *
     * @param width  the surface grid width, in number of grid points.
     * @param height the surface grid height, in number of grid points.
     *
     * @throws IllegalArgumentException if the sector is null.
     */
    public AnalyticSurface(int width, int height)
    {
        this(Sector.EMPTY_SECTOR, DEFAULT_ALTITUDE, width, height);
    }

    /**
     * Constructs a new AnalyticSurface with the default Sector {@link Sector#EMPTY_SECTOR}, the default altitude of 0
     * meters, default dimensions of <code>(10, 10)</code>, and default {@link GridPointAttributes}.
     */
    public AnalyticSurface()
    {
        this(DEFAULT_DIMENSION, DEFAULT_DIMENSION);
    }

    /**
     * Returns true if the surface is visible in the scene, and false otherwise.
     *
     * @return true if the surface is visible in the scene, and false otherwise
     */
    public boolean isVisible()
    {
        return this.visible;
    }

    /**
     * Sets whether or not the surface is visible in the scene.
     *
     * @param visible true to make the surface visible, and false to make it hidden.
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Returns the {@link Sector} defining the geographic boundary of this surface.
     *
     * @return this surface's geographic boundary, as a Sector.
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Sets this surface's geographic boundary, as a {@link Sector}.
     *
     * @param sector this surface's new geographic boundary, as a Sector.
     *
     * @throws IllegalArgumentException if the sector is null.
     */
    public void setSector(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sector = sector;
        this.setExpired(true);
    }

    /**
     * Returns this surface's base altitude, in meters.
     *
     * @return this surface's base altitude, in meters.
     *
     * @see #setAltitude(double)
     */
    public double getAltitude()
    {
        return altitude;
    }

    /**
     * Sets this surface's base altitude, in meters. See the AnalyticSurface class documentation for information on how
     * the altitude is interpreted.
     *
     * @param altitude the new base altitude, in meters.
     */
    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
        this.setExpired(true);
    }

    /**
     * Returns the surface's altitude mode, one of {@link gov.nasa.worldwind.WorldWind#CLAMP_TO_GROUND}, {@link
     * gov.nasa.worldwind.WorldWind#RELATIVE_TO_GROUND}, or {@link gov.nasa.worldwind.WorldWind#ABSOLUTE}.
     *
     * @return the surface's altitude mode.
     *
     * @see #setAltitudeMode(int)
     */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Specifies the surface's altitude mode, one of {@link gov.nasa.worldwind.WorldWind#CLAMP_TO_GROUND}, {@link
     * gov.nasa.worldwind.WorldWind#RELATIVE_TO_GROUND}, or {@link gov.nasa.worldwind.WorldWind#ABSOLUTE}. The altitude
     * mode {@link gov.nasa.worldwind.WorldWind#CONSTANT} is not supported and {@link
     * gov.nasa.worldwind.WorldWind#ABSOLUTE} is used if specified. See the AnalyticSurface class documentation for
     * information on how the altitude mode is interpreted.
     *
     * @param altitudeMode the surface's altitude mode.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
        this.setExpired(true);
    }

    /**
     * Returns the number of horizontal and vertical points composing this surface as an array with two values. The
     * value at index 0 indicates the grid width, and the value at index 1 indicates the grid height.
     *
     * @return the dimensions of this surface's grid.
     */
    public int[] getDimensions()
    {
        return new int[] {this.width, this.height};
    }

    /**
     * Sets the number of horizontal and vertical points composing this surface.
     *
     * @param width  the new grid width.
     * @param height the new grid height.
     *
     * @throws IllegalArgumentException if either width or heigth are less than 1.
     */
    public void setDimensions(int width, int height)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;
        this.height = height;
        this.setExpired(true);
    }

    /**
     * Returns the surface's iterable of {@link GridPointAttributes}. See {@link #setValues(Iterable)} for details on
     * how this iterable is interpreted by AnalyticSurface.
     *
     * @return this surface's GridPointAttributes.
     */
    public Iterable<? extends GridPointAttributes> getValues()
    {
        return this.values;
    }

    /**
     * Sets this surface's iterable of {@link GridPointAttributes}. Grid points are assigned attributes from this
     * iterable staring at the upper left hand corner, and proceeding in row-first order across the grid. The iterable
     * should contain at least <code>width * height</code> values, where width and height are the AnalyticSurface's grid
     * dimensions. If the iterable contains too few values, the unassigned grid points are given default attributes: the
     * default scalar value is 0, and the default color is {@link java.awt.Color#BLACK}.
     *
     * @param iterable the new grid point attributes.
     *
     * @throws IllegalArgumentException if the iterable is null.
     */
    public void setValues(Iterable<? extends GridPointAttributes> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.values = iterable;
        this.extremeValues = computeExtremeValues(iterable);
        this.setExpired(true);
    }

    /**
     * Returns the scale applied to the value at each grid point.
     *
     * @return the surface's vertical scale coefficient.
     */
    public double getVerticalScale()
    {
        return this.verticalScale;
    }

    /**
     * Sets the scale applied to the value at each grid point. Before rendering, this value is applied to each grid
     * points scalar value, thus increasing or decreasing it's height relative to the surface's base altitude, both in
     * meters.
     *
     * @param scale the surface's vertical scale coefficient.
     */
    public void setVerticalScale(double scale)
    {
        this.verticalScale = scale;
        this.setExpired(true);
    }

    /**
     * Returns a copy of the rendering attributes associated with this surface. Modifying the contents of the returned
     * reference has no effect on this surface. In order to make an attribute change take effect, invoke {@link
     * #setSurfaceAttributes(AnalyticSurfaceAttributes)} with the modified attributes.
     *
     * @return a copy of this surface's rendering attributes.
     */
    public AnalyticSurfaceAttributes getSurfaceAttributes()
    {
        return this.surfaceAttributes.copy();
    }

    /**
     * Sets the rendering attributes associated with this surface. The caller cannot assume that modifying the attribute
     * reference after calling setSurfaceAttributes() will have any effect, as the implementation may defensively copy
     * the attribute reference. In order to make an attribute change take effect, invoke
     * setSurfaceAttributes(AnalyticSurfaceAttributes) again with the modified attributes.
     *
     * @param attributes this surface's new rendering attributes.
     *
     * @throws IllegalArgumentException if attributes is null.
     */
    public void setSurfaceAttributes(AnalyticSurfaceAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.surfaceAttributes = attributes.copy();
        this.setExpired(true);
    }

    /**
     * Returns the object which is associated with this surface during picking. A null value is permitted and indicates
     * that the surface itself will be the object returned during picking.
     *
     * @return this surface's pick object.
     */
    public Object getPickObject()
    {
        return this.pickObject;
    }

    /**
     * Sets the object associated with this surface during picking. A null value is permitted and indicates that the
     * surface itself will be the object returned during picking.
     *
     * @param pickObject the object to associated with this surface during picking. A null value is permitted and
     *                   indicates that the surface itself will be the object returned during picking.
     */
    public void setPickObject(Object pickObject)
    {
        this.pickObject = pickObject;
    }

    /**
     * Returns the layer associated with this surface during picking.
     *
     * @return this surface's pick layer.
     */
    public Layer getClientLayer()
    {
        return this.clientLayer;
    }

    /**
     * Sets the layer associated with this surface during picking. A null value is permitted, and indicates that no
     * layer is associated with this surface.
     *
     * @param layer this surface's pick layer.
     */
    public void setClientLayer(Layer layer)
    {
        this.clientLayer = layer;
    }

    /**
     * {@inheritDoc}
     *
     * @param dc
     */
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        if (!this.intersectsFrustum(dc))
            return;

        if (this.isExpired(dc))
            this.update(dc);

        if (this.isExpired(dc))
            return;

        this.preRenderSurfaceObjects(dc);
    }

    /**
     * {@inheritDoc}
     *
     * @param dc the <code>DrawContext</code> to be used
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        if (!intersectsFrustum(dc))
            return;

        if (this.isExpired(dc))
            this.update(dc);

        if (this.isExpired(dc))
            return;

        this.makeOrderedRenderable(dc);
        this.drawSurfaceObjects(dc);
    }

    /**
     * Returns this surface's extent in model coordinates.
     *
     * @param dc the current DrawContext.
     *
     * @return this surface's extent in the specified DrawContext.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
        {
            return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.getSector());
        }
        else
        {
            double minAltitude = this.getAltitude();
            double maxAltitude = this.getAltitude();
            double[] minAndMaxElevations = dc.getGlobe().getMinAndMaxElevations(this.getSector());

            if (this.extremeValues != null)
            {
                minAltitude = this.getAltitude() + this.getVerticalScale() * this.extremeValues[0];
                maxAltitude = this.getAltitude() + this.getVerticalScale() * this.extremeValues[1];
            }

            if (minAndMaxElevations != null)
            {
                if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
                {
                    minAltitude -= minAndMaxElevations[0];
                    maxAltitude += minAndMaxElevations[1];
                }

                if (minAltitude > minAndMaxElevations[0])
                    minAltitude = minAndMaxElevations[0];
                if (maxAltitude < minAndMaxElevations[1])
                    maxAltitude = minAndMaxElevations[1];
            }

            return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.getSector(),
                minAltitude, maxAltitude);
        }
    }

    /**
     * Test if this AnalyticSurface intersects the specified draw context's frustum. During picking mode, this tests
     * intersection against all of the draw context's pick frustums. During rendering mode, this tests intersection
     * against the draw context's viewing frustum.
     *
     * @param dc the current draw context.
     *
     * @return true if this AnalyticSurface intersects the draw context's frustum; false otherwise.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        // A null extent indicates an object which has no location.
        Extent extent = this.getExtent(dc);
        if (extent == null)
            return false;

        // Test this object's extent against the pick frustum list
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(extent);

        // Test this object's extent against the viewing frustum.
        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    //**************************************************************//
    //********************  Attribute Construction  ****************//
    //**************************************************************//

    /**
     * Returns the minimum and maximum values in the specified iterable of {@link GridPointAttributes}. Values
     * equivalent to the specified <code>missingDataSignal</code> are ignored. This returns null if the iterable is
     * empty or contains only missing values.
     *
     * @param iterable          the GridPointAttributes to search for the minimum and maximum value.
     * @param missingDataSignal the number indicating a specific value to ignore.
     *
     * @return an array containing the minimum value in index 0 and the maximum value in index 1, or null if the
     *         iterable is empty or contains only missing values.
     *
     * @throws IllegalArgumentException if the iterable is null.
     */
    public static double[] computeExtremeValues(Iterable<? extends GridPointAttributes> iterable,
        double missingDataSignal)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;

        for (GridPointAttributes attr : iterable)
        {
            double value = attr.getValue();
            if (Double.compare(value, missingDataSignal) == 0)
                continue;

            if (minValue > value)
                minValue = value;
            if (maxValue < value)
                maxValue = value;
        }

        if (minValue == Double.MAX_VALUE || minValue == -Double.MIN_VALUE)
            return null;

        return new double[] {minValue, maxValue};
    }

    /**
     * Returns the minimum and maximum values in the specified iterable of {@link GridPointAttributes}. Values
     * equivalent to <code>Double.NaN</code> are ignored. This returns null if the buffer is empty or contains only NaN
     * values.
     *
     * @param iterable the GridPointAttributes to search for the minimum and maximum value.
     *
     * @return an array containing the minimum value in index 0 and the maximum value in index 1, or null if the
     *         iterable is empty or contains only NaN values.
     *
     * @throws IllegalArgumentException if the iterable is null.
     */
    public static double[] computeExtremeValues(Iterable<? extends GridPointAttributes> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return computeExtremeValues(iterable, Double.NaN);
    }

    /**
     * Returns a new instance of {@link GridPointAttributes} with the specified value and color.
     *
     * @param value the new GridPointAttributes' value.
     * @param color the new GridPointAttributes' color.
     *
     * @return a new GridPointAttributes defined by the specified value and color.
     */
    public static GridPointAttributes createGridPointAttributes(final double value, final java.awt.Color color)
    {
        return new AnalyticSurface.GridPointAttributes()
        {
            public double getValue()
            {
                return value;
            }

            public Color getColor()
            {
                return color;
            }
        };
    }

    /**
     * Returns a new instance of {@link GridPointAttributes} with a Color computed from the specified value and value
     * range. The color's RGB components are computed by mapping value's relative position in the range <code>[minValue,
     * maxValue]</code> to the range of color hues <code>[minHue, maxHue]</code>. The color's Alpha component is
     * computed by mapping the values's relative position in the range <code>[minValue, minValue + (maxValue - minValue)
     * / 10]</code> to the range <code>[0, 1]</code>. This has the effect of interpolating hue and alpha based on the
     * grid point value.
     *
     * @param value    the new GridPointAttributes' value.
     * @param minValue the minimum value.
     * @param maxValue the maximum value.
     * @param minHue   the mimimum color hue, corresponding to the minimum value.
     * @param maxHue   the maximum color hue, corresponding to the maximum value.
     *
     * @return a new GridPointAttributes defined by the specified value, value range, and color hue range.
     */
    public static AnalyticSurface.GridPointAttributes createColorGradientAttributes(final double value,
        double minValue, double maxValue, double minHue, double maxHue)
    {
        double hueFactor = WWMath.computeInterpolationFactor(value, minValue, maxValue);
        Color color = Color.getHSBColor((float) WWMath.mixSmooth(hueFactor, minHue, maxHue), 1f, 1f);
        double opacity = WWMath.computeInterpolationFactor(value, minValue, minValue + (maxValue - minValue) * 0.1);
        Color rgbaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * opacity));

        return createGridPointAttributes(value, rgbaColor);
    }

    /**
     * Returns a new iterable populated with the default {@link GridPointAttributes}. The default GridPointAttributes
     * have a value of 0, and the color {@link java.awt.Color#BLACK}.
     *
     * @param count the desired number of GridPointAttributes to return.
     *
     * @return an iterable containing <code>count</code> default GridPointAttributes.
     */
    public static Iterable<? extends GridPointAttributes> createDefaultValues(int count)
    {
        ArrayList<GridPointAttributes> list = new ArrayList<GridPointAttributes>(count);
        Collections.fill(list, DEFAULT_GRID_POINT_ATTRIBUTES);
        return list;
    }

    /**
     * Returns a new iterable populated with {@link GridPointAttributes} computed by invoking {@link
     * #createColorGradientAttributes(double, double, double, double, double)} for each double value in the speicfied
     * {@link BufferWrapper}. Values equivalent to the specified <code>missingDataSignal</code> are replaced with the
     * specified <code>minValue</code>.
     *
     * @param values            the buffer of values.
     * @param missingDataSignal the number indicating a specific value to ignore.
     * @param minValue          the minimum value.
     * @param maxValue          the maximum value.
     * @param minHue            the mimimum color hue, corresponding to the minimum value.
     * @param maxHue            the maximum color hue, corresponding to the maximum value.
     *
     * @return an iiterable GridPointAttributes defined by the specified buffer of values.
     */
    public static Iterable<? extends AnalyticSurface.GridPointAttributes> createColorGradientValues(
        BufferWrapper values, double missingDataSignal, double minValue, double maxValue, double minHue, double maxHue)
    {
        if (values == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
            = new ArrayList<AnalyticSurface.GridPointAttributes>();

        for (int i = 0; i < values.length(); i++)
        {
            double value = values.getDouble(i);
            if (Double.compare(value, missingDataSignal) == 0)
                value = minValue;

            attributesList.add(createColorGradientAttributes(value, minValue, maxValue, minHue, maxHue));
        }

        return attributesList;
    }

    //**************************************************************//
    //********************  Surface Rendering  *********************//
    //**************************************************************//

    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Clamp-to-ground analytic surface is drawn entirely by surface objects prepared during
        // preRenderSurfaceObjects().
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return;

        Extent extent = this.getExtent(dc);
        double eyeDistance = dc.getView().getEyePoint().distanceTo3(extent.getCenter()) - extent.getRadius();
        if (eyeDistance < 1)
            eyeDistance = 1;

        dc.addOrderedRenderable(new OrderedSurface(this, eyeDistance));
    }

    protected void drawOrderedRenderable(DrawContext dc)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    protected void doDrawOrderedRenderable(DrawContext dc)
    {
        this.bind(dc);

        // If the outline and interior will be drawn, then draw the outline color, but do not affect the depth
        // buffer. When the interior is drawn, it will draw on top of these colors, and the outline will be visible
        // behind the potentially transparent interior.
        if (this.surfaceAttributes.isDrawOutline() && this.surfaceAttributes.isDrawInterior())
        {
            dc.getGL().glDepthMask(false);
            this.drawOutline(dc);
            dc.getGL().glDepthMask(true);
        }

        if (this.surfaceAttributes.isDrawInterior())
            this.drawInterior(dc);

        if (this.surfaceAttributes.isDrawOutline())
            this.drawOutline(dc);
    }

    protected void bind(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.surfaceRenderInfo.cartesianVertexBuffer);

        if (!dc.isPickingMode())
        {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, this.surfaceRenderInfo.cartesianNormalBuffer);

            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, this.surfaceRenderInfo.colorBuffer);
        }
    }

    protected void drawInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            // Bind the shapes vertex colors as the diffuse material parameter.
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE);
            this.surfaceAttributes.getInteriorMaterial().apply(gl, GL2.GL_FRONT_AND_BACK,
                (float) this.surfaceAttributes.getInteriorOpacity());
        }

        gl.glCullFace(GL.GL_FRONT);
        this.surfaceRenderInfo.drawInterior(dc);

        gl.glCullFace(GL.GL_BACK);
        this.surfaceRenderInfo.drawInterior(dc);
    }

    protected void drawOutline(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_LINE_SMOOTH);
            // Unbind the shapes vertex colors as the diffuse material parameter.
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
            // Set the outline color.
            Color color = this.surfaceAttributes.getOutlineMaterial().getDiffuse();
            // Convert the floating point opacity from the range [0, 1] to the unsigned byte range [0, 255].
            int alpha = (int) (255 * this.surfaceAttributes.getOutlineOpacity() + 0.5);
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) alpha);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

        gl.glLineWidth((float) this.surfaceAttributes.getOutlineWidth());
        this.surfaceRenderInfo.drawOutline(dc);

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glDisable(GL2.GL_LINE_STIPPLE);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        }
    }

    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushAttrib(
            GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, blend func
                | GL2.GL_CURRENT_BIT
                | GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_LINE_BIT // for line width
                | GL2.GL_POLYGON_BIT // for cull face
                | (!dc.isPickingMode() ? GL2.GL_LIGHTING_BIT : 0) // for lighting.
                | (!dc.isPickingMode() ? GL2.GL_TRANSFORM_BIT : 0)); // for normalize state.

        // Enable the alpha test.
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.0f);

        gl.glEnable(GL.GL_CULL_FACE);

        if (dc.isPickingMode())
        {
            Color color = dc.getUniquePickColor();

            this.pickSupport.addPickableObject(color.getRGB(),
                (this.getPickObject() != null) ? this.getPickObject() : this,
                new Position(this.sector.getCentroid(), this.altitude), false);

            gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        }
        else
        {
            // Enable blending in non-premultiplied color mode. Premultiplied colors don't work with GL fixed
            // functionality lighting.
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);

            // Enable lighting with GL_LIGHT1.
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
            gl.glDisable(GL2.GL_LIGHT0);
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT1);
            gl.glEnable(GL2.GL_NORMALIZE);
            // Configure the lighting model for two-sided smooth shading.
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
            gl.glShadeModel(GL2.GL_SMOOTH);
            // Configure GL_LIGHT1 as a white light eminating from the viewer's eye point.
            OGLUtil.applyLightingDirectionalFromViewer(gl, GL2.GL_LIGHT1, new Vec4(1.0, 0.5, 1.0).normalize3());
        }

        dc.getView().pushReferenceCenter(dc, this.referencePoint);
    }

    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        dc.getView().popReferenceCenter(dc);
        gl.glPopAttrib();

        // Restore default GL client vertex array state.
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    }

    //**************************************************************//
    //********************  Surface Construction  ******************//
    //**************************************************************//

    protected boolean isExpired(DrawContext dc)
    {
        if (this.expired)
            return true;

        if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
        {
            if (dc.getFrameTimeStamp() - this.regenTime > RELATIVE_TO_GROUND_REGEN_PERIOD)
                return true;
        }

        if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND ||
            this.getAltitudeMode() == WorldWind.ABSOLUTE)
        {
            Object gsk = dc.getGlobe().getStateKey(dc);
            if (this.globeStateKey != null ? !this.globeStateKey.equals(gsk) : gsk != null)
                return true;
        }

        return false;
    }

    protected void setExpired(boolean expired)
    {
        this.expired = expired;

        if (this.expired)
        {
            if (this.clampToGroundSurface != null)
                this.clampToGroundSurface.markAsModified();
            if (this.shadowSurface != null)
                this.shadowSurface.markAsModified();
        }
    }

    protected void update(DrawContext dc)
    {
        if (this.updateFailed)
            return;

        try
        {
            this.doUpdate(dc);
            this.setExpired(false);
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
            this.regenTime = dc.getFrameTimeStamp();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileUpdating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            this.updateFailed = true;
        }
    }

    protected void doUpdate(DrawContext dc)
    {
        this.referencePos = new Position(this.sector.getCentroid(), this.altitude);
        this.referencePoint = dc.getGlobe().computePointFromPosition(this.referencePos);

        if (this.surfaceRenderInfo == null ||
            this.surfaceRenderInfo.getGridWidth() != this.width ||
            this.surfaceRenderInfo.getGridHeight() != this.height)
        {
            this.surfaceRenderInfo = new RenderInfo(this.width, this.height);
        }

        this.updateSurfacePoints(dc, this.surfaceRenderInfo);
        this.updateSurfaceNormals(this.surfaceRenderInfo);
    }

    protected void updateSurfacePoints(DrawContext dc, RenderInfo outRenderInfo)
    {
        Iterator<? extends GridPointAttributes> iter = this.values.iterator();

        double latStep = -this.sector.getDeltaLatDegrees() / (double) (this.height - 1);
        double lonStep = this.sector.getDeltaLonDegrees() / (double) (this.width - 1);

        double lat = this.sector.getMaxLatitude().degrees;
        for (int y = 0; y < this.height; y++)
        {
            double lon = this.sector.getMinLongitude().degrees;
            for (int x = 0; x < this.width; x++)
            {
                GridPointAttributes attr = iter.hasNext() ? iter.next() : null;
                this.updateNextSurfacePoint(dc, Angle.fromDegrees(lat), Angle.fromDegrees(lon), attr, outRenderInfo);

                lon += lonStep;
            }
            lat += latStep;
        }

        outRenderInfo.cartesianVertexBuffer.rewind();
        outRenderInfo.geographicVertexBuffer.rewind();
        outRenderInfo.colorBuffer.rewind();
        outRenderInfo.shadowColorBuffer.rewind();
    }

    protected void updateNextSurfacePoint(DrawContext dc, Angle lat, Angle lon, GridPointAttributes attr,
        RenderInfo outRenderInfo)
    {
        Color color = (attr != null) ? attr.getColor() : DEFAULT_COLOR;
        // Convert the floating point opacity from the range [0, 1] to the unsigned byte range [0, 255].
        int alpha = (int) (color.getAlpha() * this.surfaceAttributes.getInteriorOpacity() + 0.5);
        outRenderInfo.colorBuffer.put((byte) color.getRed());
        outRenderInfo.colorBuffer.put((byte) color.getGreen());
        outRenderInfo.colorBuffer.put((byte) color.getBlue());
        outRenderInfo.colorBuffer.put((byte) alpha);

        // We need geographic vertices if the surface's altitude mode is clamp-to-ground, or if we're drawing the
        // surface's shadow.
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND || this.surfaceAttributes.isDrawShadow())
        {
            outRenderInfo.geographicVertexBuffer.put((float) (lon.degrees - this.referencePos.getLongitude().degrees));
            outRenderInfo.geographicVertexBuffer.put((float) (lat.degrees - this.referencePos.getLatitude().degrees));
            outRenderInfo.geographicVertexBuffer.put(1);
        }

        // We need cartesian vertices if the surface's altitude mode is absolute or relative-to-ground.
        if (this.getAltitudeMode() != WorldWind.CLAMP_TO_GROUND) // WorldWind.ABSOLUTE or WorldWind.RELATIVE_TO_GROUND
        {
            Vec4 point = this.computeSurfacePoint(dc, lat, lon, (attr != null ? attr.getValue() : DEFAULT_VALUE));
            outRenderInfo.cartesianVertexBuffer.put((float) (point.x - this.referencePoint.x));
            outRenderInfo.cartesianVertexBuffer.put((float) (point.y - this.referencePoint.y));
            outRenderInfo.cartesianVertexBuffer.put((float) (point.z - this.referencePoint.z));
        }

        // We need shadow colors if the surface's shadow is enabled.
        if (this.surfaceAttributes.isDrawShadow())
        {
            // Convert the floating point opacity from the range [0, 1] to the unsigned byte range [0, 255].
            int shadowAlpha = (int) (alpha * this.surfaceAttributes.getShadowOpacity() + 0.5);
            outRenderInfo.shadowColorBuffer.put((byte) 0);
            outRenderInfo.shadowColorBuffer.put((byte) 0);
            outRenderInfo.shadowColorBuffer.put((byte) 0);
            outRenderInfo.shadowColorBuffer.put((byte) shadowAlpha);
        }
    }

    protected Vec4 computeSurfacePoint(DrawContext dc, Angle lat, Angle lon, double value)
    {
        double offset = this.altitude + this.verticalScale * value;

        if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
        {
            return dc.computeTerrainPoint(lat, lon, dc.getVerticalExaggeration() * offset);
        }
        else // WorldWind.ABSOLUTE
        {
            return dc.getGlobe().computePointFromPosition(lat, lon, offset);
        }
    }

    protected void updateSurfaceNormals(RenderInfo outRenderInfo)
    {
        WWMath.computeNormalsForIndexedTriangleStrip(outRenderInfo.interiorIndexBuffer,
            outRenderInfo.cartesianVertexBuffer, outRenderInfo.cartesianNormalBuffer);
    }

    //**************************************************************//
    //********************  Internal Support Classes  **************//
    //**************************************************************//

    protected static class OrderedSurface implements OrderedRenderable
    {
        protected final AnalyticSurface surface;
        protected final double eyeDistance;

        public OrderedSurface(AnalyticSurface surface, double eyeDistance)
        {
            this.surface = surface;
            this.eyeDistance = eyeDistance;
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            this.surface.pickSupport.beginPicking(dc);
            try
            {
                this.render(dc);
            }
            finally
            {
                this.surface.pickSupport.endPicking(dc);
                this.surface.pickSupport.resolvePick(dc, dc.getPickPoint(), this.surface.getClientLayer());
            }
        }

        public void render(DrawContext dc)
        {
            this.surface.drawOrderedRenderable(dc);
        }
    }

    protected static class RenderInfo
    {
        protected final int gridWidth;
        protected final int gridHeight;
        protected final IntBuffer interiorIndexBuffer;
        protected final IntBuffer outlineIndexBuffer;
        protected final FloatBuffer cartesianVertexBuffer;
        protected final FloatBuffer cartesianNormalBuffer;
        protected final FloatBuffer geographicVertexBuffer;
        protected final ByteBuffer colorBuffer;
        protected final ByteBuffer shadowColorBuffer;

        public RenderInfo(int gridWidth, int gridHeight)
        {
            int numVertices = gridWidth * gridHeight;
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            this.interiorIndexBuffer = WWMath.computeIndicesForGridInterior(gridWidth, gridHeight);
            this.outlineIndexBuffer = WWMath.computeIndicesForGridOutline(gridWidth, gridHeight);
            this.cartesianVertexBuffer = Buffers.newDirectFloatBuffer(3 * numVertices);
            this.cartesianNormalBuffer = Buffers.newDirectFloatBuffer(3 * numVertices);
            this.geographicVertexBuffer = Buffers.newDirectFloatBuffer(3 * numVertices);
            this.colorBuffer = Buffers.newDirectByteBuffer(4 * numVertices);
            this.shadowColorBuffer = Buffers.newDirectByteBuffer(4 * numVertices);
        }

        public int getGridWidth()
        {
            return this.gridWidth;
        }

        public int getGridHeight()
        {
            return this.gridHeight;
        }

        public int getNumVertices()
        {
            return this.gridWidth * this.gridHeight;
        }

        public void drawInterior(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glDrawElements(GL.GL_TRIANGLE_STRIP, this.interiorIndexBuffer.remaining(), GL.GL_UNSIGNED_INT,
                this.interiorIndexBuffer);
        }

        public void drawOutline(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glDrawElements(GL.GL_LINE_LOOP, this.outlineIndexBuffer.remaining(), GL.GL_UNSIGNED_INT,
                this.outlineIndexBuffer);
        }
    }

    //**************************************************************//
    //********************  Surface Objects  ***********************//
    //**************************************************************//

    protected void preRenderSurfaceObjects(DrawContext dc)
    {
        if (this.surfaceAttributes.isDrawShadow())
        {
            if (this.shadowSurface == null)
                this.shadowSurface = this.createShadowSurface();
            this.shadowSurface.setDelegateOwner(this.getPickObject() != null ? this.getPickObject() : this);
            this.shadowSurface.preRender(dc);
        }

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
        {
            if (this.clampToGroundSurface == null)
                this.clampToGroundSurface = this.createClampToGroundSurface();
            this.clampToGroundSurface.setDelegateOwner(this.getPickObject() != null ? this.getPickObject() : this);
            this.clampToGroundSurface.preRender(dc);
        }
    }

    protected void drawSurfaceObjects(DrawContext dc)
    {
        if (this.surfaceAttributes.isDrawShadow())
        {
            if (!dc.isPickingMode())
                this.shadowSurface.render(dc);
        }

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
        {
            this.clampToGroundSurface.render(dc);
        }
    }

    protected AnalyticSurfaceObject createClampToGroundSurface()
    {
        return new ClampToGroundSurface(this);
    }

    protected AnalyticSurfaceObject createShadowSurface()
    {
        return new ShadowSurface(this);
    }

    protected static class AnalyticSurfaceObject extends AbstractSurfaceObject
    {
        protected AnalyticSurface analyticSurface;

        public AnalyticSurfaceObject(AnalyticSurface analyticSurface)
        {
            this.analyticSurface = analyticSurface;
        }

        public void markAsModified()
        {
            super.updateModifiedTime();
            super.clearCaches();
        }

        public List<Sector> getSectors(DrawContext dc)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return Arrays.asList(this.analyticSurface.sector);
        }

        protected void drawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
        {
            this.beginDrawing(dc);
            try
            {
                this.doDrawGeographic(dc, sdc);
            }
            finally
            {
                this.endDrawing(dc);
            }
        }

        protected void beginDrawing(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glPushAttrib(
                GL2.GL_COLOR_BUFFER_BIT       // for alpha func and ref, blend func
                    | GL2.GL_CURRENT_BIT      // for current RGBA color
                    | GL2.GL_DEPTH_BUFFER_BIT // for depth test disable
                    | GL2.GL_LINE_BIT);       // for line width, line smooth

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();

            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
            gl.glDisable(GL.GL_DEPTH_TEST);

            if (!dc.isPickingMode())
            {
                gl.glEnable(GL.GL_LINE_SMOOTH);
            }
        }

        protected void endDrawing(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glPopMatrix();
            gl.glPopAttrib();

            // Restore default GL client vertex array state.
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

        protected void doDrawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            Matrix modelview = sdc.getModelviewMatrix(this.analyticSurface.referencePos);
            gl.glMultMatrixd(modelview.toArray(new double[16], 0, false), 0);

            this.bind(dc);

            if (this.analyticSurface.surfaceAttributes.isDrawInterior())
                this.drawInterior(dc);

            if (this.analyticSurface.surfaceAttributes.isDrawOutline())
                this.drawOutline(dc);
        }

        protected void bind(DrawContext dc)
        {
        }

        protected void drawInterior(DrawContext dc)
        {
            this.analyticSurface.surfaceRenderInfo.drawInterior(dc);
        }

        protected void drawOutline(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glLineWidth((float) this.analyticSurface.surfaceAttributes.getOutlineWidth());
            this.analyticSurface.surfaceRenderInfo.drawOutline(dc);
        }
    }

    protected static class ClampToGroundSurface extends AnalyticSurfaceObject
    {
        public ClampToGroundSurface(AnalyticSurface analyticSurface)
        {
            super(analyticSurface);
        }

        @Override
        protected void bind(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.analyticSurface.surfaceRenderInfo.geographicVertexBuffer);

            if (!dc.isPickingMode())
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, this.analyticSurface.surfaceRenderInfo.colorBuffer);
            }
        }

        protected void drawOutline(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!dc.isPickingMode())
            {
                // Set the outline color.
                Color color = this.analyticSurface.surfaceAttributes.getOutlineMaterial().getDiffuse();
                // Convert the floating point opacity from the range [0, 1] to the unsigned byte range [0, 255].
                int alpha = (int) (255 * this.analyticSurface.surfaceAttributes.getOutlineOpacity() + 0.5);
                gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) alpha);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
            }

            super.drawOutline(dc);

            if (!dc.isPickingMode())
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            }
        }
    }

    protected static class ShadowSurface extends AnalyticSurfaceObject
    {
        public ShadowSurface(AnalyticSurface analyticSurface)
        {
            super(analyticSurface);
        }

        @Override
        protected void buildPickRepresentation(DrawContext dc)
        {
            // The analytic surface's shadow is not drawn during picking. Suppress any attempt to create a pick
            // representation for the shadow to eliminate unnecessary overhead.
        }

        @Override
        protected void bind(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.analyticSurface.surfaceRenderInfo.geographicVertexBuffer);

            if (!dc.isPickingMode())
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, this.analyticSurface.surfaceRenderInfo.shadowColorBuffer);
            }
        }

        protected void drawOutline(DrawContext dc)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!dc.isPickingMode())
            {
                // Convert the floating point opacity from the range [0, 1] to the unsigned byte range [0, 255].
                int alpha = (int) (255 * this.analyticSurface.surfaceAttributes.getOutlineOpacity()
                    * this.analyticSurface.surfaceAttributes.getShadowOpacity() + 0.5);
                gl.glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) alpha);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
            }

            super.drawOutline(dc);

            if (!dc.isPickingMode())
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            }
        }
    }
}
