/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFConstants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface VPFConstants
{
    // Column Types
    // DIGEST Part 2, Annex C, Table C-11
    final String TEXT = "T";           // Text (US ASCII)
    final String TEXT_L1 = "L";        // Level 1 (Latin 1 - ISO 8859) text
    final String TEXT_L2 = "N";        // Level 2 (obsolete - retained for backward compatibility)
    final String TEXT_L3 = "M";        // Level 3 (Multilingual - ISO 10646) text
    final String SHORT_FLOAT = "F";    // Short floating point (4 bytes)
    final String LONG_FLOAT = "R";     // Long floating point (8 bytes)
    final String SHORT_INT = "S";      // Short integer (2 bytes)
    final String LONG_INT = "I";       // Long integer (4 bytes)
    final String SHORT_COORD_2F = "C"; // 2-coordinate - short floating point
    final String LONG_COORD_2F = "B";  // 2-coordinate - long floating point
    final String SHORT_COORD_3F = "Z"; // 3-coordinate - short floating point
    final String LONG_COORD_3F = "Y";  // 3-coordinate - long floating point
    final String DATE_AND_TIME = "D";  // Date and time
    final String NULL = "X";           // Null field
    final String TRIPLET_ID = "K";     // Triplet id
    final String SHORT_COORD_2I = "G"; // 2-coordinate array - short integer
    final String LONG_COORD_2I = "H";  // 2-coordinate array - long integer
    final String SHORT_COORD_3I = "V"; // 3-coordinate array - short integer
    final String LONG_COORD_3I = "W";  // 3-coordinate array - long integer

    // Column Names
    final String ID = "id";

    // Key Types
    // DIGEST Part 2, Annex C, Table C-12
    final String PRIMARY_KEY = "P";
    final String UNIQUE_KEY = "U";
    final String NON_UNIQUE_KEY = "N";

    // Reserved File Names
    // DIGEST Part 2, Annex C, Table C-14
    final String COVERAGE_ATTRIBUTE_TABLE = "cat";
    final String CONNECTED_NODE_PRIMITIVE_TABLE = "cnd";
    final String CONNECTED_NODE_SPATIAL_INDEX = "csi";
    final String DATABASE_HEADER_TABLE = "dht";
    final String DATA_QUALITY_TABLE = "dqt";
    final String EDGE_BOUNDING_RECTANGLE_TABLE = "ebr";
    final String EDGE_PRIMITIVE_TABLE = "edg";
    final String ENTITY_NODE_PRIMITIVE_TABLE = "end";
    final String EDGE_SPATIAL_INDEX = "esi";
    final String FACE_PRIMITIVE_TABLE = "fac";
    final String FACE_BOUNDING_RECTANGLE_TABLE = "fbr";
    final String FEATURE_CLASS_ATTRIBUTE_TABLE = "fca";
    final String FEATURE_CLASS_SCHEMA_TABLE = "fcs";
    final String FACE_SPATIAL_INDEX = "fsi";
    final String GEOGRAPHIC_REFERENCE_TABLE = "grt";
    final String LIBRARY_ATTRIBUTE_TABLE = "lat";
    final String LIBRARY_HEADER_TABLE = "lht";
    final String NODE_PRIMITIVE_TABLE = "nod";
    final String NODE_OR_ENTITY_NODE_SPATIAL_INDEX = "nsi";
    final String PRIMITIVE_EXPANSION_SCHEMA_TABLE = "pes";
    final String RING_TABLE = "rng";
    final String TEXT_PRIMITIVE_TABLE = "txt";
    final String TEXT_SPATIAL_INDEX = "tsi";
    final String CHARACTER_VALUE_DESCRIPTION_TABLE = "char.vdt";
    final String INTEGER_VALUE_DESCRIPTION_TABLE = "int.vdt";

    // Reserved Directory Names
    // DIGEST Part 2, Annex C, Table C-15
    final String LIBRARY_REFERENCE_COVERAGE = "libref";
    final String DATA_QUALITY_COVERAGE = "dq";
    final String TILE_REFERENCE_COVERAGE = "tileref";
    final String NAMES_REFERENCE_COVERAGE = "gazette";

    // Reserved Table Name Extensions
    // DIGEST Part 2, Annex C, Table C-16
    final String AREA_BOUNDING_RECTANGLE_TABLE = ".abr";
    final String AREA_FEATURE_TABLE = ".aft";
    final String AREA_JOIN_TABLE = ".ajt";
    final String AREA_THEMATIC_INDEX = ".ati";
    final String COMPLEX_BOUNDING_RECTANGLE_TABLE = ".cbr";
    final String COMPLEX_FEATURE_TABLE = ".cft";
    final String COMPLEX_JOIN_TABLE = ".cjt";
    final String COMPLEX_THEMATIC_INDEX = ".cti";
    final String NARRATIVE_TABLE = ".doc";
    final String DIAGNOSTIC_POINT_TABLE = ".dpt";
    final String FEATURE_INDEX_TABLE = ".fit";
    final String FEATURE_RELATIONS_JOIN_TABLE = ".fjt";
    final String FEATURE_INDEX_TABLE_THEMATIC_INDEX = ".fti";
    final String JOIN_THEMATIC_INDEX = ".jti";
    final String LINE_BOUNDING_RECTANGLE_TABLE = ".lbr";
    final String LINE_FEATURE_TABLE = ".lft";
    final String LINE_JOIN_TABLE = ".ljt";
    final String LINE_THEMATIC_INDEX = ".lti";
    final String POINT_BOUNDING_RECTANGLE_TABLE = ".pbr";
    final String POINT_FEATURE_TABLE = ".pft";
    final String POINT_JOIN_TABLE = ".pjt";
    final String POINT_THEMATIC_INDEX = ".pti";
    final String RELATED_ATTRIBUTE_TABLE = ".rat";
    final String REGISTRATION_POINT_TABLE = ".rpt";
    final String TEXT_BOUNDING_RECTANGLE_TABLE = ".tbr";
    final String TEXT_FEATURE_TABLE = ".tft";
    final String TEXT_FEATURE_JOIN_TABLE = ".tjt";
    final String TEXT_THEMATIC_INDEX = ".tti";

    // Feature Types
    // DIGEST Part 2, Annex C, Table C-65
    final String POINT_FEATURE_TYPE = "P";
    final String LINE_FEATURE_TYPE = "L";
    final String AREA_FEATURE_TYPE = "A";
    final String TEXT_FEATURE_TYPE = "T";
    final String COMPLEX_FEATURE_TYPE = "C";

    // Feature Tables
    // DIGEST Part 2, Annex C.2.3.3.1
    final String TEXT_FEATURE_COLUMN = ".txt_id";

    // Reserved Feature Table Names
    // DIGEST Part 2, Annex C.2.3.5.4.1
    final String TILE_REFERENCE_AREA_FEATURE = "tileref.aft";
}
