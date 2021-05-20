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

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.*;

/**
 * An object that can be rendered in a {@link ScrollFrame}. A scrollable content object needs be able to determine its
 * size, and render itself at a given location.
 *
 * @author pabercrombie
 * @version $Id: Scrollable.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see ScrollFrame
 */
public interface Scrollable
{
    /**
     * Render the scrollable component. The component should render itself with the lower left corner of the content
     * located at {@code location}.in the rectangle specified by {@code bounds}. Note that some of the content may be
     * clipped by the scroll frame.
     *
     * @param dc         Draw context.
     * @param location   Point at which to draw the Scrollable contents. This point indicates the location of the lower
     *                   left corner of the content, in GL screen coordinates (origin at lower left corner of the
     *                   screen).
     * @param frameSize  Size of the frame that will hold the content.
     * @param clipBounds Bounds of the clip rectangle. Any pixels outside of this box will be discarded, and do not need
     *                   to be drawn. The rectangle is specified in GL screen coordinates.
     */
    public void renderScrollable(DrawContext dc, Point location, Dimension frameSize, Rectangle clipBounds);

    /**
     * Get the size of the object on screen.
     *
     * @param dc        Draw context.
     * @param frameSize Size of the frame that will hold the the scrollable content. Implementations should be prepared
     *                  to handle a {@code null} frame size because the frame may need to determine the content size
     *                  before it can determine its own size.
     *
     * @return The size of the scrollable object.
     */
    Dimension getSize(DrawContext dc, Dimension frameSize);

    /**
     * Set the highlight state of the content to match the frame.
     *
     * @param highlighted {@code true} if the frame is highlighted.
     */
    void setHighlighted(boolean highlighted);

    /**
     * Get the time in milliseconds since the Epoch at which the Scrollable contents last changed. {@code ScrollFrame}
     * uses this timestamp to determine if the contents have updated since they were last rendered. Only events that
     * might cause a change in the rendered content should change the update time.
     *
     * @return Time at which the contents were last updated.
     */
    long getUpdateTime();
}
