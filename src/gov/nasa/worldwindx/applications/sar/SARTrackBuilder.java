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
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;

import java.awt.event.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: SARTrackBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARTrackBuilder
{
    private WorldWindow wwd;
    private SARTrack sarTrack;
    private boolean armed = false;
    private boolean active = false;
    private boolean useTrackElevation = false;
    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;
    private PositionListener positionListener;

    public SARTrackBuilder()
    {
        this.mouseAdapter = new MouseAdapter()
        {
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (armed && sarTrack != null && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
                    {
                        if (mouseEvent.isAltDown() && !mouseEvent.isControlDown())
                        {
                            active = true;
                            addPosition();
                        }
                    }
                    mouseEvent.consume();
                }
            }

            public void mouseReleased(MouseEvent mouseEvent)
            {
                if (armed && sarTrack != null && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    active = false;
                    mouseEvent.consume();
                }
            }

            public void mouseClicked(MouseEvent mouseEvent)
            {
                if (armed && sarTrack != null && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    if (mouseEvent.isControlDown())
                        removeLastTrackPoint();
                    mouseEvent.consume();
                }
            }
        };

        this.mouseMotionAdapter = new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent mouseEvent)
            {
                if (armed && sarTrack != null && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
                {
                    // Don't update the track here because the wwd current cursor position will not
                    // have been updated to reflect the current mouse position. Wait to update in the
                    // position listener, but consume the event so the view doesn't respond to it.
                    if (active)
                        mouseEvent.consume();
                }
            }
        };

        this.positionListener = new PositionListener()
        {
            public void moved(PositionEvent event)
            {
                if (!active || sarTrack == null)
                    return;

                replacePosition();
            }
        };
    }

    public void setWwd(WorldWindow wwd)
    {
        if (this.wwd == wwd)
            return;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().removeMouseListener(this.mouseAdapter);
            this.wwd.getInputHandler().removeMouseMotionListener(this.mouseMotionAdapter);
            this.wwd.removePositionListener(this.positionListener);
        }

        this.wwd = wwd;
        this.wwd.getInputHandler().addMouseListener(this.mouseAdapter);
        this.wwd.getInputHandler().addMouseMotionListener(this.mouseMotionAdapter);
        this.wwd.addPositionListener(this.positionListener);
    }

    public void setTrack(SARTrack track)
    {
        this.sarTrack = track;
    }

    public boolean isUseTrackElevation()
    {
        return this.useTrackElevation;
    }

    public void setUseTrackElevation(boolean state)
    {
        this.useTrackElevation = state;
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        this.armed = armed;

        if (this.armed)
            ((Component) this.wwd).setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        else
            ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
    }

    private void addPosition()
    {
        Position curPos = this.wwd != null ? this.wwd.getCurrentPosition() : null;
        if (curPos == null)
            return;

        if (this.useTrackElevation && this.sarTrack.size() > 0)
            curPos = new Position(curPos, this.sarTrack.get(this.sarTrack.size() - 1).getElevation());

        this.sarTrack.appendPosition(new SARPosition(curPos));
    }

    private void replacePosition()
    {
        Position curPos = this.wwd != null ? this.wwd.getCurrentPosition() : null;
        if (curPos == null)
            return;

        if (this.useTrackElevation && this.sarTrack.size() > 0)
            curPos = new Position(curPos, this.sarTrack.get(this.sarTrack.size() - 1).getElevation());
        
        int index = this.sarTrack.size() - 1;
        if (index < 0)
            index = 0;

        this.sarTrack.set(index, new SARPosition(curPos));
    }

    public boolean canRemoveLastTrackPoint()
    {
        return this.sarTrack != null && this.sarTrack.size() != 0;
    }

    public void removeLastTrackPoint()
    {
        if (this.sarTrack == null || this.sarTrack.size() == 0)
            return;

        this.sarTrack.removePosition(this.sarTrack.size() - 1);
    }

}
