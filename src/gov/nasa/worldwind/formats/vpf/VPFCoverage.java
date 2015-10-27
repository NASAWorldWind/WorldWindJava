/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.util.ArrayList;

/**
 * DIGEST Part 2, Annex C.2.2.2.3:<br/>A coverage is composed of features whose primitives maintain topological
 * relationships according to a level of topology (level 0, 1, 2, or 3) defined for the coverage.  All of the file
 * structures that make up a coverage are stored in a directory or subdirectories of that directory.
 *
 * @author dcollins
 * @version $Id: VPFCoverage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFCoverage extends AVListImpl
{
    private VPFLibrary library;
    private boolean tiled;
    private VPFBufferedRecordData featureClassSchemaTable;
    private VPFBufferedRecordData featureClassAttributeTable;
    private VPFBufferedRecordData characterValueDescriptionTable;
    private VPFBufferedRecordData integerValueDescriptionTable;
    private VPFBufferedRecordData symbolRelatedAttributeTable;

    protected VPFCoverage(VPFLibrary library)
    {
        if (library == null)
        {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.library = library;
    }

    /**
     * Constructs a VPF Coverage from a specified VPF Library and coverage name. This initializes the Coverage's Feature
     * Class Schema table, the Feature Class Attribute table, the Character Value Description table, the Integer Value
     * Descrption table, and the Symbol Related Attribute table.
     *
     * @param library the Library which the Coverage resides in.
     * @param name    the Coverage's name.
     *
     * @return a new Coverage from the specified Library with the specified name.
     *
     * @throws IllegalArgumentException if the library is null, or if the name is null or empty.
     */
    public static VPFCoverage fromFile(VPFLibrary library, String name)
    {
        if (library == null)
        {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(name))
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = new File(library.getFilePath(), name);
        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        // Coverage tables.
        VPFBufferedRecordData fcs = VPFUtils.readTable(new File(file, VPFConstants.FEATURE_CLASS_SCHEMA_TABLE));
        if (fcs == null)
        {
            String message = Logging.getMessage("VPF.FeatureClassSchemaTableMissing");
            throw new WWRuntimeException(message);
        }

        VPFBufferedRecordData fca = VPFUtils.readTable(
            new File(file, VPFConstants.FEATURE_CLASS_ATTRIBUTE_TABLE));
        VPFBufferedRecordData char_vdt = VPFUtils.readTable(
            new File(file, VPFConstants.CHARACTER_VALUE_DESCRIPTION_TABLE));
        VPFBufferedRecordData int_vdt = VPFUtils.readTable(
            new File(file, VPFConstants.INTEGER_VALUE_DESCRIPTION_TABLE));
        VPFBufferedRecordData symbol_rat = VPFUtils.readTable(
            new File(file, "symbol" + VPFConstants.RELATED_ATTRIBUTE_TABLE));

        VPFCoverage coverage = new VPFCoverage(library);
        coverage.setFeatureClassSchemaTable(fcs);
        coverage.setFeatureClassAttributeTable(fca);
        coverage.setCharacterValueDescriptionTable(char_vdt);
        coverage.setIntegerValueDescriptionTable(int_vdt);
        coverage.setSymbolRelatedAttributeTable(symbol_rat);

        // Coverage metadata attributes.
        VPFRecord record = library.getCoverageAttributeTable().getRecord("coverage_name", name);
        if (record != null)
        {
            VPFUtils.checkAndSetValue(record, "coverage_name", AVKey.DISPLAY_NAME, coverage);
            VPFUtils.checkAndSetValue(record, "description", AVKey.DESCRIPTION, coverage);
        }

        return coverage;
    }

    public VPFLibrary getLibrary()
    {
        return this.library;
    }

    /**
     * Returns the text name of this Coverage.
     *
     * @return name of this Coverage.
     */
    public String getName()
    {
        return this.getStringValue(AVKey.DISPLAY_NAME);
    }

    /**
     * Returns a text description of this Coverage.
     *
     * @return description of this Coverager.
     */
    public String getDescription()
    {
        return this.getStringValue(AVKey.DESCRIPTION);
    }

    public String getFilePath()
    {
        StringBuilder sb = new StringBuilder(this.library.getFilePath());
        sb.append(File.separator);
        sb.append(this.getName());
        return sb.toString();
    }

    public boolean isReferenceCoverage()
    {
        String name = this.getName();
        return name.equalsIgnoreCase(VPFConstants.DATA_QUALITY_COVERAGE)
            || name.equalsIgnoreCase(VPFConstants.LIBRARY_REFERENCE_COVERAGE)
            || name.equalsIgnoreCase(VPFConstants.NAMES_REFERENCE_COVERAGE)
            || name.equalsIgnoreCase(VPFConstants.TILE_REFERENCE_COVERAGE);
    }

    public boolean isTiled()
    {
        return this.tiled;
    }

    public void setTiled(boolean tiled)
    {
        this.tiled = tiled;
    }

    public VPFFeatureClassSchema[] getFeatureClasses(FileFilter featureTableFilter)
    {
        if (featureTableFilter == null)
        {
            String message = Logging.getMessage("nullValue.FilterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // List the file names in the coverage directory matching the specified feature table file filter.
        String[] names = WWIO.listChildFilenames(new File(this.getFilePath()), featureTableFilter);
        if (names == null)
            return null;

        int numFeatures = names.length;
        VPFFeatureClassSchema[] desc = new VPFFeatureClassSchema[numFeatures];

        for (int i = 0; i < numFeatures; i++)
        {
            String featureTableName = names[i];
            String className = WWIO.replaceSuffix(featureTableName, "");
            String type = null;

            // If the Feature Class Attriute Table is available, then use it to determine the feature type for the
            // specified class.
            if (this.featureClassAttributeTable != null)
            {
                VPFRecord record = this.featureClassAttributeTable.getRecord("fclass", className);
                if (record != null)
                    type = (String) record.getValue("type");
            }

            // Otherwise, determine the feature type is based on the feature table extension.
            if (type == null)
                type = VPFUtils.getFeatureTypeName(featureTableName);

            desc[i] = new VPFFeatureClassSchema(className, VPFFeatureType.fromTypeName(type), featureTableName);
        }

        return desc;
    }

    public VPFRelation[] getFeatureClassRelations(String className)
    {
        if (className == null)
        {
            String message = Logging.getMessage("nullValue.ClassNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<VPFRelation> rels = new ArrayList<VPFRelation>();

        for (VPFRecord row : this.featureClassSchemaTable)
        {
            Object o = row.getValue("feature_class");
            if (o == null || !(o instanceof String))
                continue;

            if (!className.equalsIgnoreCase((String) o))
                continue;

            rels.add(new VPFRelation(
                (String) row.getValue("table1"),
                (String) row.getValue("table1_key"),
                (String) row.getValue("table2"),
                (String) row.getValue("table2_key")));
        }

        VPFRelation[] array = new VPFRelation[rels.size()];
        rels.toArray(array);
        return array;
    }

    public VPFBufferedRecordData getFeatureClassSchemaTable()
    {
        return this.featureClassSchemaTable;
    }

    public void setFeatureClassSchemaTable(VPFBufferedRecordData table)
    {
        this.featureClassSchemaTable = table;
    }

    public VPFBufferedRecordData getFeatureClassAttributeTable()
    {
        return this.featureClassAttributeTable;
    }

    public void setFeatureClassAttributeTable(VPFBufferedRecordData table)
    {
        this.featureClassAttributeTable = table;
    }

    public VPFBufferedRecordData getCharacterValueDescriptionTable()
    {
        return this.characterValueDescriptionTable;
    }

    public void setCharacterValueDescriptionTable(VPFBufferedRecordData table)
    {
        this.characterValueDescriptionTable = table;
    }

    public VPFBufferedRecordData getIntegerValueDescriptionTable()
    {
        return this.integerValueDescriptionTable;
    }

    public void setIntegerValueDescriptionTable(VPFBufferedRecordData table)
    {
        this.integerValueDescriptionTable = table;
    }

    public VPFBufferedRecordData getSymbolRelatedAttributeTable()
    {
        return this.symbolRelatedAttributeTable;
    }

    public void setSymbolRelatedAttributeTable(VPFBufferedRecordData table)
    {
        this.symbolRelatedAttributeTable = table;
    }
}
