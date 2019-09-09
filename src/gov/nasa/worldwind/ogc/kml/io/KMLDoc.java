/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.io;

import java.io.*;

/**
 * Defines the interface for opening a KML or KMZ file or stream and for resolving paths to files referenced by the KML
 * content.
 *
 * @author tag
 * @version $Id: KMLDoc.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface KMLDoc
{
    /**
     * Returns an {@link InputStream} to the associated KML document within either a KML file or stream or a KMZ file or
     * stream.
     * <p>
     * Implementations of this interface do not close the stream; the user of the class must close the stream.
     *
     * @return an input stream positioned to the head of the KML document.
     *
     * @throws IOException if an error occurs while attempting to create or open the input stream.
     */
    InputStream getKMLStream() throws IOException;

    /**
     * Returns a file specified by a path relative to the KML document. If the document is in a KML file, the path is
     * resolved relative to the KML file's location in the file system. If the document is in a KMZ file or stream, the
     * path is resolved relative to the root of the KMZ file or stream. If the document is a KML stream, the relative
     * path is resolved relative to the base URI of the stream, if a base URI has been specified.
     *
     * @param path the path of the requested file.
     *
     * @return an input stream positioned to the start of the requested file, or null if the file cannot be found.
     *
     * @throws IllegalArgumentException if the path is null.
     * @throws IOException              if an error occurs while attempting to create or open the input stream.
     */
    InputStream getSupportFileStream(String path) throws IOException;

    /**
     * Returns an absolute path or URL to a file indicated by a path relative to the KML file's location.
     *
     * @param path the path of the requested file.
     *
     * @return an absolute path or URL to the file, or null if the file does not exist.
     *
     * @throws IllegalArgumentException if the specified path is null.
     * @throws java.io.IOException if an error occurs while attempting to read the support file.
     */
    String getSupportFilePath(String path) throws IOException;
}
