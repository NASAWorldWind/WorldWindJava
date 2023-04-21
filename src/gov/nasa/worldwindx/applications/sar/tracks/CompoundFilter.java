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
package gov.nasa.worldwindx.applications.sar.tracks;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: CompoundFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CompoundFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
    private final java.io.FileFilter[] filters;
    private final String description;

    public CompoundFilter(java.io.FileFilter[] filters, String description)
    {
        this.filters = new java.io.FileFilter[filters.length];
        System.arraycopy(filters, 0, this.filters, 0, filters.length);
        this.description = description;
    }

    public java.io.FileFilter[] getFilters()
    {
        java.io.FileFilter[] copy = new java.io.FileFilter[this.filters.length];
        System.arraycopy(this.filters, 0, copy, 0, this.filters.length);
        return copy;
    }

    public String getDescription()
    {
        return this.description;
    }

    public boolean accept(java.io.File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (file.isDirectory())
            return true;

        for (java.io.FileFilter filter : this.filters)
        {
            if (filter.accept(file))
                return true;
        }

        return false;
    }
}
