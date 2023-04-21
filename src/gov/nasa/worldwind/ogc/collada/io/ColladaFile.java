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
