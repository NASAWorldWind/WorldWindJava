/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.actions;

import gov.nasa.worldwindx.applications.sar.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: AddOffsetToPositionsAction.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AddOffsetToPositionsAction extends AbstractAction
{
    protected final PositionTable table;

    public AddOffsetToPositionsAction(final PositionTable table)
    {
        this.table = table;

        int numSelectedPositions = table.getSelectedRowCount();

        putValue(NAME, "Add Altitude Offset To Selected");
        putValue(LONG_DESCRIPTION, "Add the track altitude offset to the selected positions");

        if (numSelectedPositions == 0)
            this.setEnabled(false);

        SARTrack st = table.getSarTrack();
        if (st == null || st.getOffset() == 0)
            this.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        SARTrack st = table.getSarTrack();
        if (st == null || st.getOffset() == 0)
            return;

        double offset = st.getOffset();
        for (int index : this.table.getSelectedRows())
        {
            SARPosition pos = st.get(index);
            st.set(index, new SARPosition(pos.getLatitude(), pos.getLongitude(), pos.getElevation() + offset));
        }
    }
}
