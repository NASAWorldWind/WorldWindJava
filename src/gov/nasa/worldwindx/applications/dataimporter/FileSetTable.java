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

package gov.nasa.worldwindx.applications.dataimporter;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Displays the data sets available to install.
 *
 * @author tag
 * @version $Id: FileSetTable.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetTable extends JTable
{
    public FileSetTable(FileSetMap fileSetMap)
    {
        this.setModel(new FileSetTableModel(fileSetMap));

        this.setIntercellSpacing(new Dimension(10, 1));
        this.setRowHeight(36);

        this.setDefaultRenderer(Color.class, new TableCellColorRenderer(true));

        TableColumn column = this.getColumnModel().getColumn(0); // color column
        column.setPreferredWidth(10);

        column = this.getColumnModel().getColumn(1); // image column
        column.setPreferredWidth(36);

        column = this.getColumnModel().getColumn(2); // name column
        column.setPreferredWidth(160);

        column = this.getColumnModel().getColumn(3); // scale column
        column.setPreferredWidth(70);

        column = this.getColumnModel().getColumn(4); // type column
        column.setPreferredWidth(35);

        column = this.getColumnModel().getColumn(5); // number of files column
        column.setPreferredWidth(20);
    }

    public void setFileSetMap(FileSetMap fileSetMap)
    {
        ((FileSetTableModel) this.getModel()).setFileSetMap(fileSetMap);
    }

    public java.util.List<FileSet> getSelectedFileSets()
    {
        int[] selectedRows = this.getSelectedRows();

        if (selectedRows.length == 0)
            return null;

        java.util.List<FileSet> selectedFileSets = new ArrayList<FileSet>(selectedRows.length);

        for (int i = 0; i < selectedRows.length; i++)
        {
            int modelRow = this.convertRowIndexToModel(selectedRows[i]);
            FileSet fileSet = ((FileSetTableModel) this.getModel()).getRow(modelRow);
            selectedFileSets.add(fileSet);
        }

        return selectedFileSets;
    }

    public void scrollToFileSet(FileSet fileSet)
    {
        Integer row = ((FileSetTableModel) this.getModel()).getRowForFileSet(fileSet);

        if (row != null)
            this.scrollToVisible(row, 0);
    }

    public void scrollToVisible(int rowIndex, int vColIndex)
    {
        if (!(this.getParent() instanceof JViewport))
            return;

        JViewport viewport = (JViewport) this.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = this.getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }
}
