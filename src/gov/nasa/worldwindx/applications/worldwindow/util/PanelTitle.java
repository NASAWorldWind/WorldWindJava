/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: PanelTitle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PanelTitle extends ShadedPanel
{
    private static final Color c1 = new Color(29, 78, 169, 200);
    private static Color c2 = new Color(93, 158, 223, 200);

    public PanelTitle(String title)
    {
        this(title, SwingConstants.LEFT);
    }

    public PanelTitle(String title, int alignment)
    {
        super(new BorderLayout());

        this.setColors(c1, c2);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(alignment);
        titleLabel.setFont(Font.decode("Arial-Bold-14"));
        titleLabel.setForeground(Color.WHITE);
        this.add(titleLabel, BorderLayout.CENTER);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
    }
}
