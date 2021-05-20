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

package gov.nasa.worldwind.formats.rpf;
/**
 * @author Lado Garakanidze
 * @version $Id: RPFFileComponents.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFileComponents
{
    private java.nio.ByteBuffer  buffer;

    private RPFHeaderSection headerSection;
    private RPFLocationSection locationSection;

    public RPFFileComponents(java.nio.ByteBuffer buffer)
    {
        this.buffer = buffer;
        this.headerSection = new RPFHeaderSection(buffer);

        buffer.position(this.headerSection.locationSectionLocation);
        this.locationSection = new RPFLocationSection(buffer);
        
    }

    public RPFHeaderSection getRPFHeaderSection()
    {
        return this.headerSection;
    }

    public RPFFrameFileIndexSection getRPFFrameFileIndexSection()
    {
        if( 0 < locationSection.getFrameFileIndexSectionSubheaderLength())
        {
            this.buffer.position(locationSection.getFrameFileIndexSectionSubheaderLocation());
            return new RPFFrameFileIndexSection(buffer);
        }
        return null;
    }

    public RPFBoundingRectangleSection getRPFBoundingRectangleSection()
    {
        if (0 < locationSection.getBoundaryRectangleSectionSubheaderLength())
        {
            this.buffer.position(locationSection.getBoundaryRectangleSectionSubheaderLocation());
            return new RPFBoundingRectangleSection(buffer);
        }
        return null;
    }
}
