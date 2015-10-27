/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: ShapefileRenderable.java 3232 2015-06-20 04:08:11Z dcollins $
 */
public abstract class ShapefileRenderable extends WWObjectImpl
    implements Renderable, Iterable<ShapefileRenderable.Record>
{
    /**
     * AttributeDelegate provides an entry point for configuring a ShapefileRenderable.Record's shape attributes and
     * key-value attributes during ShapefileRenderable construction. In particular, the dBASE attributes associated with
     * a ShapefileRecord are available only during these entry points.
     * <p/>
     * AttributeDelegate entry points may be called on a non-EDT thread. Implementations of AttributeDelegate may modify
     * the ShapefileRenderable.Record passed to these methods, but should not modify the ShapefileRenderable without
     * synchronizing access with the thread used to create the ShapefileRenderable.
     */
    public interface AttributeDelegate
    {
        /**
         * Entry point for configuring a ShapefileRenderable.Record's shape attributes and key-value attributes during
         * ShapefileRenderable construction. The ShapefileRecord's dBASE attributes are available only during the
         * execution of this method.
         * <p/>
         * This method may be called on a non-EDT thread. Implementations may modify the renderableRecord, but should
         * not modify the ShapefileRenderable without synchronizing access with the thread used to create the
         * ShapefileRenderable.
         *
         * @param shapefileRecord  The shapefile record used to create the ShapefileRenderable.Record.
         * @param renderableRecord The ShapefileRenderable.Record to assign attributes for.
         */
        void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord);
    }

    public static class Record extends AVListImpl implements Highlightable
    {
        // Record properties.
        protected ShapefileRenderable shapefileRenderable;
        protected Sector sector;
        protected int ordinal;
        protected boolean visible = true;
        protected boolean highlighted;
        protected ShapeAttributes normalAttrs;
        protected ShapeAttributes highlightAttrs;
        // Data structures supporting record tessellation and display.
        protected final CompoundVecBuffer pointBuffer;
        protected int firstPartNumber;
        protected int numberOfParts;
        protected int numberOfPoints;

        public Record(ShapefileRenderable shapefileRenderable, ShapefileRecord shapefileRecord)
        {
            if (shapefileRenderable == null)
            {
                String msg = Logging.getMessage("nullValue.RenderableIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (shapefileRecord == null)
            {
                String msg = Logging.getMessage("nullValue.RecordIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.shapefileRenderable = shapefileRenderable;
            this.sector = shapefileRecord.getBoundingRectangle() != null ? Sector.fromDegrees(
                shapefileRecord.getBoundingRectangle()) : null;
            this.pointBuffer = shapefileRecord.getShapeFile().getPointBuffer();
            this.firstPartNumber = shapefileRecord.getFirstPartNumber();
            this.numberOfParts = shapefileRecord.getNumberOfParts();
            this.numberOfPoints = shapefileRecord.getNumberOfPoints();
        }

        public ShapefileRenderable getShapefileRenderable()
        {
            return this.shapefileRenderable;
        }

        public Sector getSector()
        {
            return this.sector;
        }

        public int getOrdinal()
        {
            return this.ordinal;
        }

        public boolean isVisible()
        {
            return this.visible;
        }

        public void setVisible(boolean visible)
        {
            if (this.visible != visible)
            {
                this.visible = visible;
                this.shapefileRenderable.recordDidChange(this);
            }
        }

        @Override
        public boolean isHighlighted()
        {
            return this.highlighted;
        }

        @Override
        public void setHighlighted(boolean highlighted)
        {
            if (this.highlighted != highlighted)
            {
                this.highlighted = highlighted;
                this.shapefileRenderable.recordDidChange(this);
            }
        }

        public ShapeAttributes getAttributes()
        {
            return this.normalAttrs;
        }

        public void setAttributes(ShapeAttributes normalAttrs)
        {
            if (this.normalAttrs != normalAttrs)
            {
                this.normalAttrs = normalAttrs;
                this.shapefileRenderable.recordDidChange(this);
            }
        }

        public ShapeAttributes getHighlightAttributes()
        {
            return this.highlightAttrs;
        }

        public void setHighlightAttributes(ShapeAttributes highlightAttrs)
        {
            if (this.highlightAttrs != highlightAttrs)
            {
                this.highlightAttrs = highlightAttrs;
                this.shapefileRenderable.recordDidChange(this);
            }
        }

        public int getBoundaryCount()
        {
            return this.numberOfParts;
        }

        public VecBuffer getBoundaryPoints(int index)
        {
            if (index < 0 || index >= this.numberOfParts)
            {
                String msg = Logging.getMessage("generic.indexOutOfRange", index);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            synchronized (this.pointBuffer) // synchronize access to the Shapefile's shared pointBuffer
            {
                return this.pointBuffer.subBuffer(this.firstPartNumber + index);
            }
        }

        public Iterable<Position> getBoundaryPositions(int index)
        {
            if (index < 0 || index >= this.numberOfParts)
            {
                String msg = Logging.getMessage("generic.indexOutOfRange", index);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            VecBuffer points = this.getBoundaryPoints(index);
            return points.getPositions();
        }
    }

    protected Sector sector;
    protected ArrayList<ShapefileRenderable.Record> records;
    protected boolean visible = true;
    // Properties used during initialization.
    protected ShapeAttributes initNormalAttrs;
    protected ShapeAttributes initHighlightAttrs;
    protected ShapefileRenderable.AttributeDelegate initAttributeDelegate;

    protected static ShapeAttributes defaultAttributes;
    protected static ShapeAttributes defaultHighlightAttributes;

    static
    {
        defaultAttributes = new BasicShapeAttributes();
        defaultAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
        defaultAttributes.setOutlineMaterial(Material.DARK_GRAY);
        defaultHighlightAttributes = new BasicShapeAttributes();
        defaultHighlightAttributes.setInteriorMaterial(Material.WHITE);
        defaultHighlightAttributes.setOutlineMaterial(Material.DARK_GRAY);
    }

    /**
     * Initializes this ShapefileRenderable with the specified shapefile. The normal attributes, the highlight
     * attributes and the attribute delegate are optional. Specifying a non-null value for normalAttrs or highlightAttrs
     * causes each ShapefileRenderable.Record to adopt those attributes. Specifying a non-null value for the attribute
     * delegate enables callbacks during creation of each ShapefileRenderable.Record. See {@link AttributeDelegate} for
     * more information.
     *
     * @param shapefile         The shapefile to display.
     * @param normalAttrs       The normal attributes for each ShapefileRenderable.Record. May be null to use the
     *                          default attributes.
     * @param highlightAttrs    The highlight attributes for each ShapefileRenderable.Record. May be null to use the
     *                          default highlight attributes.
     * @param attributeDelegate Optional callback for configuring each ShapefileRenderable.Record's shape attributes and
     *                          key-value attributes. May be null.
     */
    protected void init(Shapefile shapefile, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
        ShapefileRenderable.AttributeDelegate attributeDelegate)
    {
        double[] boundingRect = shapefile.getBoundingRectangle();
        if (boundingRect == null) // suppress record assembly for empty shapefiles
            return;

        this.sector = Sector.fromDegrees(boundingRect);
        this.initNormalAttrs = normalAttrs;
        this.initHighlightAttrs = highlightAttrs;
        this.initAttributeDelegate = attributeDelegate;
        this.assembleRecords(shapefile);
    }

    protected void assembleRecords(Shapefile shapefile)
    {
        this.records = new ArrayList<ShapefileRenderable.Record>();

        while (shapefile.hasNext())
        {
            ShapefileRecord shapefileRecord = shapefile.nextRecord();

            if (this.mustAssembleRecord(shapefileRecord))
            {
                this.assembleRecord(shapefileRecord);
            }
        }

        this.records.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
    }

    protected boolean mustAssembleRecord(ShapefileRecord shapefileRecord)
    {
        return shapefileRecord.getNumberOfParts() > 0
            && shapefileRecord.getNumberOfPoints() > 0
            && !shapefileRecord.isNullRecord();
    }

    protected void assembleRecord(ShapefileRecord shapefileRecord)
    {
        ShapefileRenderable.Record renderableRecord = new ShapefileRenderable.Record(this, shapefileRecord);
        this.addRecord(shapefileRecord, renderableRecord);
    }

    protected void addRecord(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord)
    {
        renderableRecord.setAttributes(this.initNormalAttrs);
        renderableRecord.setHighlightAttributes(this.initHighlightAttrs);
        renderableRecord.ordinal = this.records.size();
        this.records.add(renderableRecord);

        if (this.initAttributeDelegate != null)
        {
            this.initAttributeDelegate.assignAttributes(shapefileRecord, renderableRecord);
        }
    }

    public Sector getSector()
    {
        return this.sector;
    }

    public int getRecordCount()
    {
        if (this.records == null)
            return 0;

        return this.records.size();
    }

    public ShapefileRenderable.Record getRecord(int ordinal)
    {
        if (this.records == null || ordinal < 0 || ordinal >= this.records.size())
        {
            String msg = Logging.getMessage("generic.indexOutOfRange", ordinal);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.records.get(ordinal);
    }

    @Override
    public Iterator<ShapefileRenderable.Record> iterator()
    {
        if (this.records == null)
            return Collections.<ShapefileRenderable.Record>emptyList().iterator();

        return this.records.iterator();
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    protected void recordDidChange(ShapefileRenderable.Record record)
    {
        // Intentionally left empty. May be overridden by subclass.
    }

    protected ShapeAttributes determineActiveAttributes(ShapefileRenderable.Record record)
    {
        if (record.highlighted)
        {
            return record.highlightAttrs != null ? record.highlightAttrs : defaultHighlightAttributes;
        }
        else if (record.normalAttrs != null)
        {
            return record.normalAttrs;
        }
        else
        {
            return defaultAttributes;
        }
    }
}