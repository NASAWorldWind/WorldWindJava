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

package gov.nasa.worldwindx.applications.worldwindow.core.layermanager;

import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * @author tag
 * @version $Id: LayerPath.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerPath extends ArrayList<String>
{
    public LayerPath()
    {
    }

    public LayerPath(LayerPath initialPath, String... args)
    {
        this.addAll(initialPath);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    public LayerPath(String initialPathEntry, String... args)
    {
        this.add(initialPathEntry);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    public LayerPath(List<String> initialPathEntries)
    {
        this.addAll(initialPathEntries);
    }

    public LayerPath lastButOne()
    {
        return this.subPath(0, this.size() - 1);
    }

    public LayerPath subPath(int start, int end)
    {
        return new LayerPath(this.subList(start, end));
    }

    public static boolean isEmptyPath(LayerPath path)
    {
        return path == null || path.size() == 0 || WWUtil.isEmpty(path.get(0));
    }

    @Override
    public String toString()
    {
        if (this.size() == 0)
            return "<empty path>";

        StringBuilder sb = new StringBuilder();

        for (String s : this)
        {
            if (WWUtil.isEmpty(s))
                s = "<empty>";

            if (sb.length() == 0)
                sb.append(s);
            else
                sb.append("/").append(s);
        }

        return sb.toString();
    }
}
