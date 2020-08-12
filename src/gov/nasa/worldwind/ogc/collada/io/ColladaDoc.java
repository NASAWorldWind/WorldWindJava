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
