/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada.io;

import gov.nasa.worldwind.util.Logging;

import java.io.*;

/**
 * Represents a COLLADA document read from a file.
 *
 * @author pabercrombie
 * @version $Id: ColladaFile.java 660 2012-06-26 16:13:11Z pabercrombie $
 */
public class ColladaFile implements ColladaDoc
{
    /** File from which COLLADA content is read. */
    protected File colladaFile;

    /**
     * Create a new instance from a file.
     *
     * @param file COLLADA file from which to read content.
     */
    public ColladaFile(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.colladaFile = file;
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(this.colladaFile);
    }

    /** {@inheritDoc} */
    public String getSupportFilePath(String path)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File pathFile = new File(path);
        if (pathFile.isAbsolute())
            return null;

        pathFile = new File(this.colladaFile.getParentFile(), path);

        return pathFile.exists() ? pathFile.getPath() : null;
    }
}
