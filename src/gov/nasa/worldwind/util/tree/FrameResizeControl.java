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

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.AbstractResizeHotSpot;

import java.awt.*;
import java.awt.event.*;

/**
 * A screen control for resizing a frame. This class handles the resize input events, but does does not actually draw
 * the resize control.
 *
 * @author pabercrombie
 * @version $Id: FrameResizeControl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FrameResizeControl extends AbstractResizeHotSpot
{
    protected ScrollFrame frame;

    /**
     * Create a resize control.
     *
     * @param frame     Frame to resize.
     */
    public FrameResizeControl(ScrollFrame frame)
    {
        this.frame = frame;
    }

    /** {@inheritDoc} */
    @Override
    protected void beginDrag(Point point)
    {
        super.beginDrag(point);
    }

    /** {@inheritDoc} */
    @Override
    protected void endDrag()
    {
        super.endDrag();
    }

    /** {@inheritDoc} */
    @Override
    protected Dimension getSize()
    {
        return this.frame.getCurrentSize();
    }

    /** {@inheritDoc} */
    @Override
    protected void setSize(Dimension newSize)
    {
        this.frame.setSize(Size.fromPixels(newSize.width, newSize.height));
    }

    /** {@inheritDoc} */
    @Override
    protected Point getScreenPoint()
    {
        Point point = this.frame.getScreenLocation();
        return new Point(point);
    }

    /** {@inheritDoc} */
    @Override
    protected void setScreenPoint(Point newPoint)
    {
        this.frame.setScreenLocation(newPoint);
    }

    /** {@inheritDoc} */
    @Override
    protected Dimension getMinimumSize()
    {
        return this.frame.getMinimumSize();
    }

    /**
     * Forwards mouse wheel events to the frame, so that the contents can be scrolled when the mouse is over the resize
     * area.
     *
     * @param event The event to handle.
     */
    @Override
	public void mouseWheelMoved(MouseWheelEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.frame.mouseWheelMoved(event);
    }
}
