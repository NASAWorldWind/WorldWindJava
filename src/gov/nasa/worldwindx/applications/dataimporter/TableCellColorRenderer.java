/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Renders a color bar into a table cell.
 *
 * @author tag
 * @version $Id: TableCellColorRenderer.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class TableCellColorRenderer extends JLabel implements TableCellRenderer
{
    protected java.util.List<Color> fileSetColors = new ArrayList<Color>();

    protected Border unselectedBorder = null;
    protected Border selectedBorder = null;
    protected boolean isBordered = true;

    public TableCellColorRenderer(boolean isBordered)
    {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(
        JTable table, Object color,
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
        Color newColor = (Color) color;
        setBackground(newColor);
        if (isBordered)
        {
            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                        table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            }
            else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                        table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }

        return this;
    }

    protected void initializeColors()
    {
        this.fileSetColors.clear();

        this.fileSetColors.add(Color.YELLOW);
        this.fileSetColors.add(Color.GREEN);
        this.fileSetColors.add(Color.BLUE);
        this.fileSetColors.add(Color.CYAN);
        this.fileSetColors.add(Color.MAGENTA);
        this.fileSetColors.add(Color.RED);
        this.fileSetColors.add(Color.ORANGE);
        this.fileSetColors.add(Color.PINK);
    }

    protected Color determineFileSetColor(FileSet fileSet)
    {
        if (fileSet.getValue(AVKey.COLOR) != null)
            return (Color) fileSet.getValue(AVKey.COLOR);

        // Try to use a pre-defined color.
        if (this.fileSetColors.size() > 0)
        {
            Color color = this.fileSetColors.get(0);
            this.fileSetColors.remove(color);
            return color;
        }

        // No more pre-defined colors left, so use a random color.
        return WWUtil.makeRandomColor(null);
    }
}