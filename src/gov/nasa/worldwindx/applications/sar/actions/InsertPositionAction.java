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
package gov.nasa.worldwindx.applications.sar.actions;

import gov.nasa.worldwindx.applications.sar.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: InsertPositionAction.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class InsertPositionAction extends AbstractAction
{
    private final boolean above;
    protected final PositionTable table;

    public InsertPositionAction(final boolean above, final PositionTable table)
    {
        this.table = table;
        this.above = above;
        if (this.above)
        {
            putValue(NAME, "Insert New Position Above Selection");
            putValue(LONG_DESCRIPTION, "Insert a new position above the selected positions");
        }
        else
        {
            putValue(NAME, "Insert New Position Below Selection");
            putValue(LONG_DESCRIPTION, "Insert a new position below the selected positions");
        }

        if (table.getSelectedRowCount() == 0)
            this.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        SARTrack st = table.getSarTrack();
        if (st == null)
            return;

        int index = table.getSelectionModel().getMinSelectionIndex();
        if (!this.above)
            index = table.getSelectionModel().getMaxSelectionIndex();

        if (index < 0)
            return;

        st.insertPosition(index, new SARPosition());

        table.getSelectionModel().setSelectionInterval(index, index);
    }
}

