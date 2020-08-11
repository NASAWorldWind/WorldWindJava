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

/**
 * <p>
 * Provides classes for parsing COLLADA files and streams.</p>
 * <h3>How to Parse a COLLADA File or Stream</h3>
 * <ol>
 * <li>
 * Create a {@link gov.nasa.worldwind.ogc.collada.ColladaRoot} via one of its constructors, such as {@link
 * gov.nasa.worldwind.ogc.collada.ColladaRoot#ColladaRoot(java.io.File)}, passing the COLLADA source to the constructor.
 * A <code>ColladaRoot</code> provides access to the COLLADA contents. The source can be either a file, an input stream,
 * or a URL.
 * </li>
 * <li>
 * Call {@link gov.nasa.worldwind.ogc.collada.ColladaRoot#parse(Object[])} to parse the document.
 * </li>
 * <li>
 * The <code>ColladaRoot</code> class provides accessor methods for the <code>COLLADA</code> element of the COLLADA
 * file, its root. The content includes the element's <i>asset</i> and <i>scene</i> elements. Once the root is parsed,
 * use these methods to obtain objects representing these elements, and use the accessor methods of those objects in
 * turn to obtain their contents.
 * </li>
 * <li>
 * Each COLLADA element defined in the <a href="http://www.khronos.org/collada/">COLLADA specification version 1.4.1</a>
 * has a corresponding class provided in this package. Each of those classes contains methods to obtain the element's
 * contents as defined by the specification.
 * </li>
 * </ol>
 *
<h4>Extending the Classes</h4>
 *
<p>
 * This package's classes are designed for easy behavior modification and replacement, and for straightforward addition
 * of operations to be performed during parsing. See the description of {@link
 * gov.nasa.worldwind.util.xml.AbstractXMLEventParser} for further information.</p>
 *
<h4>Relative References</h4>
 *
<p>
 * By default, relative references will be resolved relative to the location of the COLLADA file that includes the
 * reference. However, this behavior can be overridden by providing the ColladaRoot with a {@link
 * gov.nasa.worldwind.ogc.collada.ColladaResourceResolver}.</p>
 *
 */
package gov.nasa.worldwind.ogc.collada;
