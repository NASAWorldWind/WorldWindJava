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

import gov.nasa.worldwindx.applications.sar.actions.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: PositionsContextMenu.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PositionsContextMenu extends MouseAdapter
{
    private final PositionTable positionTable;

    public PositionsContextMenu(final PositionTable positionTable)
    {
        this.positionTable = positionTable;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    private void checkPopup(MouseEvent e)
    {
        if (!e.isPopupTrigger())
            return;

        JMenuItem mi;
        JPopupMenu pum = new JPopupMenu();

        mi = new JMenuItem(new DeletePositionsAction(positionTable));
        pum.add(mi);

        pum.addSeparator();

        mi = new JMenuItem(new AppendPositionAction(positionTable));
        pum.add(mi);

        mi = new JMenuItem(new InsertPositionAction(true, positionTable));
        pum.add(mi);

        mi = new JMenuItem(new InsertPositionAction(false, positionTable));
        pum.add(mi);

        pum.addSeparator();

        mi = new JMenuItem(new AddOffsetToPositionsAction(positionTable));
        pum.add(mi);

        pum.show(positionTable, e.getX(), e.getY());
    }
}
