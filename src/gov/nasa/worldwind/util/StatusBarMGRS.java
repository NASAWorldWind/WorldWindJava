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
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;

/**
 * @author Patrick Murris
 * @version $Id: StatusBarMGRS.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class StatusBarMGRS extends StatusBar
{
    public void moved(PositionEvent event)
    {
        this.handleCursorPositionChange(event);
    }

    protected void handleCursorPositionChange(PositionEvent event)
    {
        Position newPos = event.getPosition();
        if (newPos != null)
        {
            String las = String.format("%7.4f\u00B0 %7.4f\u00B0", newPos.getLatitude().getDegrees(), newPos.getLongitude().getDegrees());
            String els = makeCursorElevationDescription(
                getEventSource().getModel().getGlobe().getElevation(newPos.getLatitude(), newPos.getLongitude()));
            String los = "";
            try
            {
                MGRSCoord MGRS = MGRSCoord.fromLatLon(newPos.getLatitude(), newPos.getLongitude(),
                        getEventSource().getModel().getGlobe());
                los = MGRS.toString();
            }
            catch (Exception e)
            {
                los = "";
            }
            latDisplay.setText(las);
            lonDisplay.setText(los);
            eleDisplay.setText(els);
        }
        else
        {
            latDisplay.setText("");
            lonDisplay.setText("Off globe");
            eleDisplay.setText("");
        }
    }

}
