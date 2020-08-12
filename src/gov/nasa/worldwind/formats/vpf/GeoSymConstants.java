/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
