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

