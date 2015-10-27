/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.Logging;

import javax.xml.stream.*;
import java.io.IOException;
import java.util.Map;

/**
 * A shape that is positioned and oriented independently of its defining geometry. This shape typically represents 3D
 * models imported from COLLADA and other 3D file formats. The model's geometry is defined in its own coordinate system;
 * properties of this shape place and orient the model geographically. Specific model types subclass this class to
 * provide their implementation and any additional properties.
 * <p/>
 * This class also accepts a resource map to independently link resources named in the model definition (the defining
 * model file) to actual resources. Each map entry's key is a resource name as expressed in the model definition. Each
 * map entry's value is the reference to the actual resource. The reference is interpreted by rules specific to the
 * model format, but typically may be a relative or absolute file reference or a URL to a local or remote resource. If
 * the map or an entry referred to by the model definition is not defined, resource references are typically considered
 * relative to the location of the model definition's location.
 * <p/>
 * In the case of a COLLADA file referenced from a KML file, relative references are considered relative to the location
 * of the KML file. If the file is KMZ, relative references are considered references to resources within the KMZ
 * archive.
 * <p/>
 * This class applies {@link ShapeAttributes} to the shape, but the effect of some of those attributes, such as {@link
 * ShapeAttributes#isDrawOutline()} is dependent on the specific implementation of this <code>AbstractGeneralShape</code>. See
 * the class description of those shapes to determine how shape attributes are applied.
 *
 * @author tag
 * @version $Id: AbstractGeneralShape.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractGeneralShape extends AbstractShape
{
    /**
     * This class holds globe-specific data for this shape. It's managed via the shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractShapeData
    {
        /**
         * Construct a cache entry for this shape.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, AbstractGeneralShape shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
        }
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

    /** This shape's geographic location. The altitude is relative to this shapes altitude mode. */
    protected Position modelPosition;
    /** This shape's heading, positive values are clockwise from north. Null is an allowed value. */
    protected Angle heading;
    /**
     * This shape's pitch (often called tilt), its rotation about the model's X axis. Positive values are clockwise.
     * Null is an allowed value.
     */
    protected Angle pitch;
    /**
     * This shape's roll, its rotation about the model's Y axis. Positive values are clockwise. Null is an allowed
     * Value.
     */
    protected Angle roll;
    /** A scale to apply to the model. Null is an allowed value. */
    protected Vec4 modelScale;
    /** A map indicating the actual location of resources named in the model. Null is an allowed value. */
    protected Map<String, Object> resourceMap;

    /**
     * Constructs a shape at 0 latitude, longitude and altitude. Because the shape's default altitude mode is
     * <code>ABSOLUTE</code>, the default altitude is relative to mean sea level.
     */
    public AbstractGeneralShape()
    {
        this.modelPosition = Position.ZERO;
    }

    @Override
    protected void initialize()
    {
        // Overridden to specify a default altitude mode unique to AbstractGeneralShape.
        this.altitudeMode = WorldWind.CLAMP_TO_GROUND;
    }

    /**
     * Returns this shape's resource map.
     *
     * @return this shape's resource map, or null if this shape has no resource map.
     */
    public Map<String, Object> getResourceMap()
    {
        return resourceMap;
    }

    /**
     * Specifies this shape's resource map. The resource map is described above in this class' description.
     *
     * @param resourceMap the resource map for this shape. May be null, in which case no resource map is used.
     */
    public void setResourceMap(Map<String, Object> resourceMap)
    {
        this.resourceMap = resourceMap;
    }

    /**
     * Indicates this shape's geographic position.
     *
     * @return this shape's geographic position. The position's altitude is relative to this shape's altitude mode.
     */
    public Position getModelPosition()
    {
        return modelPosition;
    }

    /**
     * Specifies this shape's geographic position. The position's altitude is relative to this shape's altitude mode.
     *
     * @param modelPosition this shape's geographic position.
     *
     * @throws IllegalArgumentException if the position is null.
     */
    public void setModelPosition(Position modelPosition)
    {
        this.modelPosition = modelPosition;
    }

    /**
     * Indicates this shape's scale, if any.
     *
     * @return this shape's scale, or null if no scale has been specified.
     */
    public Vec4 getModelScale()
    {
        return modelScale;
    }

    /**
     * Specifies this shape's scale. The scale is applied to the shape's model definition in the model's coordinate
     * system prior to oriented and positioning the model.
     *
     * @param modelScale this shape's scale. May be null, in which case no scaling is applied.
     */
    public void setModelScale(Vec4 modelScale)
    {
        this.modelScale = modelScale;
    }

    /**
     * Indicates this shape's heading, its rotation clockwise from north.
     *
     * @return this shape's heading, or null if no heading has been specified.
     */
    public Angle getHeading()
    {
        return heading;
    }

    /**
     * Specifies this shape's heading, its rotation clockwise from north.
     *
     * @param heading this shape's heading. May be null.
     */
    public void setHeading(Angle heading)
    {
        this.heading = heading;
    }

    /**
     * Indicates this shape's pitch -- often referred to as tilt -- the angle to rotate this shape's model about its X
     * axis.
     *
     * @return this shape's pitch, or null if no pitch has been specified. Positive values are clockwise as observed
     *         looking along the model's X axis toward the model's origin.
     */
    public Angle getPitch()
    {
        return pitch;
    }

    /**
     * Specifies this shape's pitch -- often referred to as tilt -- the angle to rotate this shape's model about its X
     * axis.
     *
     * @param pitch this shape's pitch. Positive values are clockwise as observed looking along the model's X axis
     *              toward the model's origin. May be null.
     */
    public void setPitch(Angle pitch)
    {
        this.pitch = pitch;
    }

    /**
     * Indicates this shape's roll, the angle to rotate this shape's model about its Y axis.
     *
     * @return this shape's roll, or null if no roll has been specified. Positive values are clockwise as observed
     *         looking along the model's Y axis toward the origin.
     */
    public Angle getRoll()
    {
        return roll;
    }

    /**
     * Specifies this shape's roll, the angle to rotate this shape's model about its Y axis.
     *
     * @param roll this shape's roll. May be null. Positive values are clockwise as observed looking along the model's Y
     *             axis toward the origin.
     */
    public void setRoll(Angle roll)
    {
        this.roll = roll;
    }

    public Position getReferencePosition()
    {
        return this.getModelPosition();
    }

    protected Vec4 computeReferencePoint(Terrain terrain)
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        Vec4 refPt = terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0);
        if (refPt == null)
            return null;

        return refPt;
    }

    /**
     * Computes the minimum distance between this shape and the eye point.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc        the current draw context.
     * @param shapeData the current shape data for this shape.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc, ShapeData shapeData)
    {
        Vec4 eyePoint = dc.getView().getEyePoint();

        // TODO: compute distance using extent.getEffectiveRadius(Plane)
        Extent extent = shapeData.getExtent();
        if (extent != null)
            return extent.getCenter().distanceTo3(eyePoint) + extent.getRadius();

        Vec4 refPt = shapeData.getReferencePoint();
        if (refPt != null)
            return refPt.distanceTo3(eyePoint);

        return 0;
    }

    public void moveTo(Position position)
    {
        this.setModelPosition(position);
    }

    @Override
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        String message = Logging.getMessage("unsupportedOperation.doExportAsKML");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }

    /** {@inheritDoc} Not currently supported. */
    public Sector getSector()
    {
        return null;
    }
}
