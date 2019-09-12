/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: GeoSymConstants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GeoSymConstants
{
    // Column Types
    // MIL-DTL-89045 3.5.3.1
    final String INTEGER = "N";          // integer
    final String CHARACTER_STRING = "T"; // character string

    // Reserved path names.
    final String ASCII = "ascii";
    final String BINARY = "bin";
    final String CLEAR_TEXT = "ctext";
    final String GEOSYM = "geosym";
    final String GRAPHICS = "graphics";
    final String SYMBOLOGY_ASSIGNMENT = "symasgn";

    // Reserved attribute file names.
    final String ATTRIBUTE_EXPRESSION_FILE = "attexp.txt";
    final String CODE_VALUE_DESCRIPTION_FILE = "code.txt";
    final String COLOR_ASSIGNMENT_FILE = "color.txt";
    final String FULL_SYMBOL_ASSIGNMENT_FILE = "fullsym.txt";
    final String SIMPLIFIED_SYMBOL_ASSIGNMENT_FILE = "simpsym.txt";
    final String TEXT_ABBREVIATIONS_ASSIGNMENT_FILE = "textabbr.txt";
    final String TEXT_LABEL_CHARACTERISTICS_FILE = "textchar.txt";
    final String TEXT_LABEL_JOIN_FILE = "textjoin.txt";
    final String TEXT_LABEL_LOCATION_FILE = "textloc.txt";
    final String LINE_AREA_ATTRIBUTES_FILE = "geosym-line-area-attr.csv";
}
