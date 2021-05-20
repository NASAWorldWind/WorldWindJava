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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.util.*;

import java.io.File;
import java.util.*;

/**
 * Finds all the data sets within a filestore.
 *
 * @author tag
 * @version $Id: FileStoreDataSetFinder.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileStoreDataSetFinder
{
    public List<FileStoreDataSet> findDataSets(FileStore fileStore)
    {
        final List<FileStoreDataSet> dataSets = new ArrayList<FileStoreDataSet>();

        for (File file : fileStore.getLocations())
        {
            if (!file.exists())
                continue;

            if (!fileStore.isInstallLocation(file.getPath()))
                continue;

            dataSets.addAll(this.findDataSets(file));
        }

        return dataSets;
    }

    protected List<FileStoreDataSet> findDataSets(File cacheRoot)
    {
        if (cacheRoot == null)
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] configFilePaths = WWIO.listDescendantFilenames(cacheRoot, new DataConfigurationFilter(), false);
        if (configFilePaths == null || configFilePaths.length == 0)
            return Collections.emptyList();

        List<FileStoreDataSet> dataSets = new ArrayList<FileStoreDataSet>();

        for (String configFilePath : configFilePaths)
        {
            File configFile = new File(configFilePath);
            dataSets.add(new FileStoreDataSet(cacheRoot.getPath(),
                cacheRoot.getPath() + File.separator + configFile.getParent(),
                cacheRoot.getPath() + File.separator + configFilePath));
        }

        return dataSets;
    }
}
