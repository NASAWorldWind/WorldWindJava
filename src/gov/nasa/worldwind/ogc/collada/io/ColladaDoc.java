/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada.io;

import java.io.*;

/**
 * Represents the source of a COLLADA document, and provides access to the document's content.
 *
 * @author pabercrombie
 * @version $Id: ColladaDoc.java 660 2012-06-26 16:13:11Z pabercrombie $
 */
public interface ColladaDoc
{
    /**
     * Returns an {@link java.io.InputStream} to the associated COLLADA document.
     * <p>
     * Implementations of this interface do not close the stream; the user of the class must close the stream.
     *
     * @return an input stream positioned to the head of the COLLADA document.
     *
     * @throws java.io.IOException if an error occurs while attempting to create or open the input stream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns an absolute path or URL to a file indicated by a path relative to the COLLADA file's location.
     *
     * @param path the path of the requested file.
     *
     * @return an absolute path or URL to the file, or null if the file does not exist.
     *
     * @throws IllegalArgumentException if the specified path is null.
     * @throws java.io.IOException      if an error occurs while attempting to read the support file.
     */
    String getSupportFilePath(String path) throws IOException;
}
