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
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.pick.PickedObjectList;

import java.awt.event.*;

/**
 * This class is a specialization of {@link SelectEvent} and includes the pick point screen position of the most recent
 * drag event prior to the current one.
 *
 * @author tag
 * @version $Id: DragSelectEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DragSelectEvent extends SelectEvent
{
    private final java.awt.Point previousPickPoint;

    public DragSelectEvent(Object source, String eventAction, java.awt.Point awtPt, MouseEvent mouseEvent, 
    		               PickedObjectList pickedObjects,
    		               java.awt.Point previousPickPoint)
    {
        super(source, eventAction, awtPt, mouseEvent, pickedObjects);
        this.previousPickPoint = previousPickPoint;
    }

    /**
     * Indicates the most screen position of the drag event immediately prior to this one.
     *
     * @return the screen position of the event just prior to this one.
     */
    public java.awt.Point getPreviousPickPoint()
    {
        return this.previousPickPoint;
    }
}
