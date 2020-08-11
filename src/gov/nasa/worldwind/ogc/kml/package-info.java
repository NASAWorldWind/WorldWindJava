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
 * Provides classes for parsing KML and KMZ files and streams.</p>
 * <h3>How to Parse a KML or KMZ File or Stream</h3>
 * <ol>
 * <li>
 * Create a {@link gov.nasa.worldwind.ogc.kml.KMLRoot} via one of its constructors, such as {@link
 * gov.nasa.worldwind.ogc.kml.KMLRoot#KMLRoot(java.io.File)}, passing the KML or KMZ source to the constructor. A
 * <code>KMLRoot</code> provides access to the KML contents. The source can be either a file, an input stream or a URL.
 * </li>
 * <li>
 * Call {@link gov.nasa.worldwind.ogc.kml.KMLRoot#parse(Object[])} to parse the document.
 * </li>
 * <li>
 * The <code>KMLRoot</code> class provides accessor methods for the <code>kml</code> element of the KML file, its root.
 * The content includes the element's single attribute, <i>hint</i>, and the <i>NetworkLinkControl</i> and
 * <i>Feature</i> elements it may contain. Once the root is parsed, use these methods to obtain objects representing
 * these elements, and use the accessor methods of those objects in turn to obtain their contents.
 * </li>
 * <li>
 * Each KML element defined in the <a href="http://www.opengeospatial.org/standards/kml/">KML specification</a> has a
 * corresponding class provided in this package. Each of those classes contains methods to obtain the element's contents
 * as defined by the specification. The same is true for the other specifications KML uses: <a
 * href="http://tools.ietf.org/html/rfc4287">Atom</a>, <a
 * href="http://www.oasis-open.org/committees/ciq/download.html">XAL</a> and the <a
 * href="http://code.google.com/apis/kml/documentation/kmlreference.html#kmlextensions"> GX extensions</a>
 * defined by Google. Use these classes to obtain the contents of individual elements of the KML file.
 * </li>
 * </ol>
 * The classes in this package only read and parse a KML file. Mapping them to shapes, annotations and other WorldWind
 * objects is a separate step. WorldWind provides default mappings for many KML elements, but an application is fully
 * able to provide its own, and in some cases is expected to.
 * <h4>Extending the Classes</h4>
 *
<p>
 * This package's classes are designed for easy behavior modification and replacement, and for straightforward addition
 * of operations to be performed during parsing. See the description of {@link
 * gov.nasa.worldwind.util.xml.AbstractXMLEventParser} for further information.</p>
 *
<h4>Relative References</h4>
 *
<p>
 * Because the KML specification requires relative references within KML files to be resolved relative to the location
 * of the file, a context is provided to resolve these references. The context is specific to the document type &#151;
 * file, stream, KML or KMZ &#151; and is provided by the {@link gov.nasa.worldwind.ogc.kml.io.KMLDoc} interface and its
 * implementing classes. It's available from the {@link gov.nasa.worldwind.ogc.kml.KMLRoot}. See the description of the
 * <code>KMLDoc</code> interface for further information. </p>
 *
 */
package gov.nasa.worldwind.ogc.kml;
