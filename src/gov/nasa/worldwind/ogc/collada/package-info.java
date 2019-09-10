/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
