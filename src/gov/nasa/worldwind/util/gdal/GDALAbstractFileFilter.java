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

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import gov.nasa.worldwind.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALAbstractFileFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
abstract class GDALAbstractFileFilter implements java.io.FileFilter {

    protected HashSet<String> listFolders = new HashSet<>();
    protected final String searchPattern;

    protected GDALAbstractFileFilter(String searchPattern) {
        if (null == searchPattern || searchPattern.length() == 0) {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.searchPattern = searchPattern;

        listFolders.clear();
    }

    protected boolean isHidden(String path) {
        if (!WWUtil.isEmpty(path)) {
            String[] folders = path.split(Pattern.quote(File.separator));
            if (!WWUtil.isEmpty(folders)) {
                for (String folder : folders) {
                    if (!WWUtil.isEmpty(folder) && folder.startsWith(".")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<String> getFolders() {
        return new ArrayList<>(listFolders);
    }
}
