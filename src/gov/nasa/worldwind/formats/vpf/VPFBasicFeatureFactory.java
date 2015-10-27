/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFBasicFeatureFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFBasicFeatureFactory implements VPFFeatureFactory
{
    private VPFTile tile;
    private VPFPrimitiveData primitiveData;

    /**
     * Constructs an instance of a VPFBasicFeatureFactory which will construct feature data for the specified {@link
     * gov.nasa.worldwind.formats.vpf.VPFTile} and {@link gov.nasa.worldwind.formats.vpf.VPFPrimitiveData}. The
     * primitive data must contain information for at least those features found in the specified tile.
     *
     * @param tile          the tile which defines the geographic region to construct features for.
     * @param primitiveData the primitive data describing feature information for the geographic region defined by the
     *                      tile.
     */
    public VPFBasicFeatureFactory(VPFTile tile, VPFPrimitiveData primitiveData)
    {
        this.tile = tile;
        this.primitiveData = primitiveData;
    }

    public VPFTile getTile()
    {
        return this.tile;
    }

    public VPFPrimitiveData getPrimitiveData()
    {
        return this.primitiveData;
    }

    public Collection<? extends VPFFeature> createPointFeatures(VPFFeatureClass featureClass)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.doCreateSimpleFeatures(featureClass);
    }

    public Collection<? extends VPFFeature> createLineFeatures(VPFFeatureClass featureClass)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.doCreateSimpleFeatures(featureClass);
    }

    public Collection<? extends VPFFeature> createAreaFeatures(VPFFeatureClass featureClass)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.doCreateSimpleFeatures(featureClass);
    }

    public Collection<? extends VPFFeature> createTextFeatures(VPFFeatureClass featureClass)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.doCreateSimpleFeatures(featureClass);
    }

    public Collection<? extends VPFFeature> createComplexFeatures(VPFFeatureClass featureClass)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.doCreateComplexFeatures(featureClass);
    }

    //**************************************************************//
    //********************  Simple Feature Assembly  ***************//
    //**************************************************************//

    protected Collection<? extends VPFFeature> doCreateSimpleFeatures(VPFFeatureClass featureClass)
    {
        if (this.primitiveData == null)
        {
            String message = Logging.getMessage("VPF.NoPrimitiveData");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        ArrayList<VPFFeature> results = new ArrayList<VPFFeature>();

        VPFBufferedRecordData featureTable = this.createFeatureTable(featureClass);
        if (featureTable == null)
            return null;

        VPFBufferedRecordData joinTable = this.createJoinTable(featureClass);
        Iterable<String> attributeKeys = this.getFeatureAttributeKeys(featureTable);

        for (VPFRecord featureRow : featureTable)
        {
            VPFFeature feature = this.doCreateSimpleFeature(featureClass, featureRow, joinTable, attributeKeys);
            if (feature != null)
                results.add(feature);
        }

        return results;
    }

    protected VPFFeature doCreateSimpleFeature(VPFFeatureClass featureClass, VPFRecord featureRow,
        VPFBufferedRecordData joinTable, Iterable<String> attributeKeys)
    {
        if (joinTable != null)
        {
            return this.createCompoundSimpleFeature(featureClass, featureRow, joinTable, attributeKeys);
        }
        else
        {
            return this.createSimpleFeature(featureClass, featureRow, attributeKeys);
        }
    }

    protected VPFFeature createSimpleFeature(VPFFeatureClass featureClass, VPFRecord featureRow,
        Iterable<String> attributeKeys)
    {
        // Feature has a direct 1:1 relation to the primitive table.

        if (this.tile != null && !matchesTile(featureRow, this.tile))
            return null;

        VPFRelation featureToPrimitive = this.getFeatureToPrimitiveRelation(featureClass);
        if (featureToPrimitive == null)
            return null;

        int primitiveId = asInt(featureRow.getValue(featureToPrimitive.getTable1Key()));
        VPFPrimitiveData.PrimitiveInfo primitiveInfo = this.primitiveData.getPrimitiveInfo(
            featureToPrimitive.getTable2(), primitiveId);

        return this.createFeature(featureClass, featureRow, attributeKeys, primitiveInfo.getBounds(),
            new int[] {primitiveId});
    }

    protected VPFFeature createCompoundSimpleFeature(VPFFeatureClass featureClass, VPFRecord featureRow,
        VPFBufferedRecordData joinTable, Iterable<String> attributeKeys)
    {
        // Feature has a direct 1:* relation to the primitive table through a join table.

        // Query the number of primitives which match the feature.
        Object o = this.getPrimitiveIds(featureClass, featureRow, joinTable, null, true);
        if (o == null || !(o instanceof Integer))
            return null;

        int numPrimitives = (Integer) o;
        if (numPrimitives < 1)
            return null;

        // Gather the actual primitive ids matching the feature.
        int[] primitiveIds = new int[numPrimitives];
        VPFBoundingBox bounds = (VPFBoundingBox) this.getPrimitiveIds(featureClass, featureRow, joinTable, primitiveIds,
            false);

        return this.createFeature(featureClass, featureRow, attributeKeys, bounds, primitiveIds);
    }

    protected VPFFeature createFeature(VPFFeatureClass featureClass, VPFRecord featureRow,
        Iterable<String> attributeKeys,
        VPFBoundingBox bounds, int[] primitiveIds)
    {
        VPFFeature feature = new VPFFeature(featureClass, featureRow.getId(), bounds, primitiveIds);
        this.setFeatureAttributes(featureRow, attributeKeys, feature);

        return feature;
    }

    //**************************************************************//
    //********************  Complex Feature Assembly  **************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected Collection<? extends VPFFeature> doCreateComplexFeatures(VPFFeatureClass featureClass)
    {
        throw new UnsupportedOperationException();
    }

    //**************************************************************//
    //********************  Common Feature Assembly  ***************//
    //**************************************************************//

    protected Object getPrimitiveIds(VPFFeatureClass featureClass, VPFRecord featureRow,
        VPFBufferedRecordData joinTable, int[] primitiveIds, boolean query)
    {
        // Although a direct link between feature and primitive(s) is provided by the primitive_id column in the join
        // table, a sequential search of the feature_id column must still be performed to find all primitives associated
        // with a selected feature.

        VPFRelation featureToJoin = this.getFeatureToJoinRelation(featureClass);
        if (featureToJoin == null)
            return null;

        VPFRelation joinToPrimitive = this.getJoinToPrimitiveRelation(featureClass);
        if (joinToPrimitive == null)
            return null;

        int featureId = featureRow.getId();
        String joinFeatureKey = featureToJoin.getTable2Key();
        String joinPrimitiveKey = joinToPrimitive.getTable1Key();
        String primitiveTable = joinToPrimitive.getTable2();

        int numPrimitives = 0;
        VPFBoundingBox bounds = null;

        for (VPFRecord joinRow : joinTable)
        {
            if (this.tile != null && !matchesTile(joinRow, this.tile))
                continue;

            int fId = asInt(joinRow.getValue(joinFeatureKey));
            if (featureId != fId)
                continue;

            if (!query)
            {
                int pId = asInt(joinRow.getValue(joinPrimitiveKey));
                primitiveIds[numPrimitives] = pId;

                VPFPrimitiveData.PrimitiveInfo primitiveInfo = this.primitiveData.getPrimitiveInfo(primitiveTable, pId);
                bounds = (bounds != null) ? bounds.union(primitiveInfo.getBounds()) : primitiveInfo.getBounds();
            }

            numPrimitives++;
        }

        return query ? numPrimitives : bounds;
    }

    protected Iterable<String> getFeatureAttributeKeys(VPFBufferedRecordData table)
    {
        ArrayList<String> keys = new ArrayList<String>();

        for (String name : table.getRecordParameterNames())
        {
            if (name.equalsIgnoreCase("id") ||
                name.equalsIgnoreCase("tile_id") ||
                name.equalsIgnoreCase("from_to") ||
                name.equalsIgnoreCase("nod_id") ||
                name.equalsIgnoreCase("end_id") ||
                name.equalsIgnoreCase("cnd_id") ||
                name.equalsIgnoreCase("edg_id") ||
                name.equalsIgnoreCase("fac_id") ||
                name.equalsIgnoreCase("txt_id"))
            {
                continue;
            }

            keys.add(name);
        }

        return keys;
    }

    protected void setFeatureAttributes(VPFRecord row, Iterable<String> attributeKeys, AVList params)
    {
        for (String key : attributeKeys)
        {
            VPFUtils.checkAndSetValue(row, key, key, params);
        }
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected VPFBufferedRecordData createFeatureTable(VPFFeatureClass featureClass)
    {
        StringBuilder sb = new StringBuilder(featureClass.getCoverage().getFilePath());
        sb.append(File.separator);
        sb.append(featureClass.getFeatureTableName());

        return VPFUtils.readTable(new File(sb.toString()));
    }

    protected VPFBufferedRecordData createJoinTable(VPFFeatureClass featureClass)
    {
        if (featureClass.getJoinTableName() == null)
            return null;

        StringBuilder sb = new StringBuilder(featureClass.getCoverage().getFilePath());
        sb.append(File.separator);
        sb.append(featureClass.getJoinTableName());

        return VPFUtils.readTable(new File(sb.toString()));
    }

    protected VPFRelation getFeatureToPrimitiveRelation(VPFFeatureClass featureClass)
    {
        return findFirstRelation(featureClass.getFeatureTableName(), featureClass.getPrimitiveTableName(),
            featureClass.getRelations());
    }

    protected VPFRelation getFeatureToJoinRelation(VPFFeatureClass featureClass)
    {
        return findFirstRelation(featureClass.getFeatureTableName(), featureClass.getJoinTableName(),
            featureClass.getRelations());
    }

    protected VPFRelation getJoinToPrimitiveRelation(VPFFeatureClass featureClass)
    {
        return findFirstRelation(featureClass.getJoinTableName(), featureClass.getPrimitiveTableName(),
            featureClass.getRelations());
    }

    protected static VPFRelation findFirstRelation(String table1, String table2, VPFRelation[] relations)
    {
        if (relations == null)
            return null;

        for (VPFRelation rel : relations)
        {
            if (rel.getTable1().equalsIgnoreCase(table1) && rel.getTable2().equalsIgnoreCase(table2))
                return rel;
        }

        return null;
    }

    protected static boolean matchesTile(VPFRecord row, VPFTile tile)
    {
        Object fk = row.getValue("tile_id");
        return (fk != null) && (tile.getId() == asInt(fk));
    }

    protected static int asInt(Object o)
    {
        if (o instanceof Number)
            return ((Number) o).intValue();

        return -1;
    }
}
