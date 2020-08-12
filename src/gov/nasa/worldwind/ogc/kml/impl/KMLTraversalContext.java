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

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.KMLRegion;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * <code>KMLTraversalContext</code> provides a suitcase of KML specific state used to render a hierarchy of KML
 * features.
 *
 * @author tag
 * @version $Id: KMLTraversalContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLTraversalContext
{
    /**
     * The <code>Deque</code> as this KML traversal context's Region stack. The region stack is used to implement
     * Regions inheritance of from a KML containers to their descendant KML features.
     */
    protected Deque<KMLRegion> regionStack = new ArrayDeque<KMLRegion>();
    /**
     * Indicates this KML traversal context's detail hint. Modifies the default relationship of KML scene resolution to
     * screen resolution as viewing distance changes. Values greater than 0 increase the resolution. Values less than 0
     * decrease the resolution. Initially 0.
     */
    protected double detailHint;

    /** Constructs a new KML traversal context in a default state, but otherwise does nothing. */
    public KMLTraversalContext()
    {
    }

    /**
     * Initializes this KML traversal context to its default state. This should be called at the beginning of each frame
     * to prepare this traversal context for the coming render pass.
     */
    public void initialize()
    {
        this.regionStack.clear();
        this.detailHint = 0.0;
    }

    /**
     * Adds the specified <code>region</code> to the top of this KML traversal context's Region stack. The specified
     * region is returned by any subsequent calls to <code>peekRegion</code> until either <code>pushRegion</code> or
     * <code>popRegion</code> are called. The <code>region</code> is removed from the stack by calling
     * <code>popRegion</code>.
     *
     * @param region the KML Region to add to the top of the stack.
     *
     * @throws IllegalArgumentException if <code>region</code> is <code>null</code>.
     */
    public void pushRegion(KMLRegion region)
    {
        if (region == null)
        {
            String message = Logging.getMessage("nullValue.RegionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.regionStack.push(region);
    }

    /**
     * Returns the KML Region on the top of this KML traversal context's Region stack, or <code>null</code> if the
     * Region stack is empty. The Region on the top of the stack the last region added with a call to
     * <code>pushRegion</code>. This does not modify the contents of the stack.
     *
     * @return the Region on the top of this context's stack, or <code>null</code> if the stack is empty.
     */
    public KMLRegion peekRegion()
    {
        return this.regionStack.peek();
    }

    /**
     * Removes the KML Region from the top of this KML traversal context's Region stack. This throws an exception if the
     * Region stack is empty, otherwise this removes and returns the last Region added to the stack by a call to
     * <code>pushRegion</code>.
     *
     * @return the Region removed from the top of the stack.
     *
     * @throws NoSuchElementException if the Region stack is empty.
     */
    public KMLRegion popRegion()
    {
        return this.regionStack.pop();
    }

    /**
     * Indicates this KML traversal context's detail hint, which is described in <code>{@link
     * #setDetailHint(double)}</code>.
     *
     * @return the detail hint.
     *
     * @see #setDetailHint(double)
     */
    public double getDetailHint()
    {
        return this.detailHint;
    }

    /**
     * Specifies this KML traversal context's detail hint. The detail hint modifies the default relationship of KML
     * scene resolution to screen resolution as the viewing distance changes. Values greater than 0 cause KML elements
     * with a level of detail to appear at higher resolution at greater distances than normal, but at an increased
     * performance cost. Values less than 0 decrease the default resolution at any given distance. The default value is
     * 0. Values typically range between -0.5 and 0.5.
     *
     * @param detailHint the degree to modify the default relationship of KML scene resolution to screen resolution as
     *                   viewing distance changes. Values greater than 0 increase the resolution. Values less than 0
     *                   decrease the resolution. The default value is 0.
     */
    public void setDetailHint(double detailHint)
    {
        this.detailHint = detailHint;
    }
}
