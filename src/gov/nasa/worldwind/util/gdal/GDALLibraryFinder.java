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

package gov.nasa.worldwind.util.gdal;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.*;

import java.io.File;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALLibraryFinder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class GDALLibraryFinder extends GDALAbstractFileFilter
{
    protected final String libExtension =
        Configuration.isWindowsOS() ? ".dll" : (Configuration.isMacOS() ? ".jnilib" : ".so");

    public GDALLibraryFinder()
    {
        super("gdal");
    }

    public GDALLibraryFinder(String searchPattern)
    {
        super(searchPattern);
    }

    public boolean accept(File pathname)
    {
        String filename;
        String dir;
        if (null != pathname
            && !isHidden(pathname.getAbsolutePath())
            && null != (dir = pathname.getParent())
            && !this.listFolders.contains(dir)                  // skip already discovered
            && null != (filename = pathname.getName())          // get folder name
            && !filename.startsWith(".")
            && null != (filename = filename.toLowerCase())      // change to lower case
            && filename.contains(this.searchPattern)
            && filename.endsWith(this.libExtension)
//            && this.canLoad(pathname.getAbsolutePath())
            )
        {
            this.listFolders.add(dir);
            return true;
        }
        Thread.yield();
        return false;
    }

    /**
     * Attempts to load the specified filename from the local file system as a dynamic library. The filename argument
     * must be a complete path name.
     *
     * @param pathToLibrary - the file to load
     *
     * @return TRUE if the file is loadable library
     */
    protected boolean canLoad(String pathToLibrary)
    {
        try
        {
            System.load(pathToLibrary);

            return true;
        }
        catch (Throwable t)
        {
            Logging.logger().finest(WWUtil.extractExceptionReason(t));
        }
        return false;
    }
}
