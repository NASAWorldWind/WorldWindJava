/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

import java.io.*;
import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: VPFBasicFeatureClassFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFBasicFeatureClassFactory implements VPFFeatureClassFactory
{
    /** Constructs an instance of a VPFBasicCoverageFactory, but otherwise does nothing. */
    public VPFBasicFeatureClassFactory()
    {
    }

    public VPFFeatureClass createFromSchema(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        if (coverage == null)
        {
            String message = Logging.getMessage("nullValue.CoverageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (schema == null)
        {
            String message = Logging.getMessage("nullValue.SchemaIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            return this.doCreateFromSchema(coverage, schema);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileReading",
                coverage.getFilePath() + File.separator + schema.getClassName());
            throw new WWRuntimeException(message, e);
        }
    }

    protected VPFFeatureClass doCreateFromSchema(VPFCoverage coverage, VPFFeatureClassSchema schema) throws IOException
    {
        // DIGEST Part 2, Annex C.2.2.2.2.3.a: Simple feature classes.
        // A simple feature class consists of a (logically) single primitive table and a single simple feature table.
        // There are four subtypes of the simple feature class in VRF:
        // * Point feature classes (composed of entity or connected nodes).
        // * Line feature classes  (composed of edges).
        // * Area feature classes (composed of faces (level 3) or of edges (level 0-2). See C.2.2.2.3.1 for
        // definition of topological levels).
        // * Text feature classes.
        //
        // A text feature class consists of a text primitive table and a text feature
        // table. The text feature class is not a true feature class, but it is often useful to process text as if it
        // were a feature.  For instance, many maps contain text annotation that does not reference a specific
        // geographic entity.  The text "Himalaya Mountains" may not define any geometric primitive or feature, but
        // merely provide associative information for the viewer.  Using a text feature allows thematic queries on text
        // just like other features.  For instance, if a text feature has a height attribute, software can retrieve
        // 'all text with HEIGHT > 0.5'.

        VPFFeatureClass cls = this.doCreateFeatureClass(coverage, schema);
        if (cls != null)
            return cls;

        cls = this.doCreateFromFeatureType(coverage, schema);
        this.initFeatureClass(cls);

        return cls;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected VPFFeatureClass doCreateFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return null;
    }

    protected VPFFeatureClass doCreateFromFeatureType(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        switch (schema.getType())
        {
            case POINT:
                return this.createPointFeatureClass(coverage, schema);
            case LINE:
                return this.createLineFeatureClass(coverage, schema);
            case AREA:
                return this.createAreaFeatureClass(coverage, schema);
            case TEXT:
                return this.createTextFeatureClass(coverage, schema);
            case COMPLEX:
                return this.createComplexFeatureClass(coverage, schema);
            default:
                return this.createUnknownFeatureClass(coverage, schema);
        }
    }

    protected void initFeatureClass(VPFFeatureClass cls)
    {
        VPFRelation[] rels = cls.getCoverage().getFeatureClassRelations(cls.getClassName());
        if (rels != null)
        {
            cls.setRelations(rels);
        }
    }

    //**************************************************************//
    //********************  Feature Class Assembly  ****************//
    //**************************************************************//

    protected VPFFeatureClass createPointFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema,
            this.getJoinTableName(coverage, schema, VPFConstants.POINT_JOIN_TABLE),
            this.getPointFeaturePrimitiveTable(coverage, schema))
        {
            public Collection<? extends VPFFeature> createFeatures(VPFFeatureFactory factory)
            {
                return factory.createPointFeatures(this);
            }

            public Collection<? extends VPFSymbol> createFeatureSymbols(VPFSymbolFactory factory)
            {
                return factory.createPointSymbols(this);
            }
        };
    }

    protected VPFFeatureClass createLineFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema,
            this.getJoinTableName(coverage, schema, VPFConstants.LINE_JOIN_TABLE),
            VPFConstants.EDGE_PRIMITIVE_TABLE)
        {
            public Collection<? extends VPFFeature> createFeatures(VPFFeatureFactory factory)
            {
                return factory.createLineFeatures(this);
            }

            public Collection<? extends VPFSymbol> createFeatureSymbols(VPFSymbolFactory factory)
            {
                return factory.createLineSymbols(this);
            }
        };
    }

    protected VPFFeatureClass createAreaFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema,
            this.getJoinTableName(coverage, schema, VPFConstants.AREA_JOIN_TABLE),
            VPFConstants.FACE_PRIMITIVE_TABLE)
        {
            public Collection<? extends VPFFeature> createFeatures(VPFFeatureFactory factory)
            {
                return factory.createAreaFeatures(this);
            }

            public Collection<? extends VPFSymbol> createFeatureSymbols(VPFSymbolFactory factory)
            {
                return factory.createAreaSymbols(this);
            }
        };
    }

    protected VPFFeatureClass createTextFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema,
            this.getJoinTableName(coverage, schema, VPFConstants.TEXT_FEATURE_JOIN_TABLE),
            VPFConstants.TEXT_PRIMITIVE_TABLE)
        {
            public Collection<? extends VPFFeature> createFeatures(VPFFeatureFactory factory)
            {
                return factory.createTextFeatures(this);
            }

            public Collection<? extends VPFSymbol> createFeatureSymbols(VPFSymbolFactory factory)
            {
                return factory.createTextSymbols(this);
            }
        };
    }

    protected VPFFeatureClass createComplexFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema,
            this.getJoinTableName(coverage, schema, VPFConstants.TEXT_FEATURE_JOIN_TABLE), null)
        {
            public Collection<? extends VPFFeature> createFeatures(VPFFeatureFactory factory)
            {
                return factory.createComplexFeatures(this);
            }

            public Collection<? extends VPFSymbol> createFeatureSymbols(VPFSymbolFactory factory)
            {
                return factory.createComplexSymbols(this);
            }
        };
    }

    protected VPFFeatureClass createUnknownFeatureClass(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        return new VPFFeatureClass(coverage, schema, null, null);
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected String getJoinTableName(VPFCoverage coverage, VPFFeatureClassSchema schema, String suffix)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(schema.getClassName());
        sb.append(suffix);

        String tableName = sb.toString();
        File file = new File(coverage.getFilePath(), tableName);

        return file.exists() ? tableName : null;
    }

    protected String getPointFeaturePrimitiveTable(VPFCoverage coverage, VPFFeatureClassSchema schema)
    {
        String primitiveTableName = null;

        VPFRelation[] rels = coverage.getFeatureClassRelations(schema.getClassName());
        if (rels != null)
        {
            for (VPFRelation rel : rels)
            {
                if (rel.getTable2().equalsIgnoreCase(VPFConstants.NODE_PRIMITIVE_TABLE))
                    primitiveTableName = VPFConstants.NODE_PRIMITIVE_TABLE;
                else if (rel.getTable2().equalsIgnoreCase(VPFConstants.ENTITY_NODE_PRIMITIVE_TABLE))
                    primitiveTableName = VPFConstants.ENTITY_NODE_PRIMITIVE_TABLE;
                else if (rel.getTable2().equalsIgnoreCase(VPFConstants.CONNECTED_NODE_PRIMITIVE_TABLE))
                    primitiveTableName = VPFConstants.CONNECTED_NODE_PRIMITIVE_TABLE;

                if (primitiveTableName != null)
                    break;
            }
        }

        return primitiveTableName;
    }
}
