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

package gov.nasa.worldwindx.applications.worldwindow.util;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: ShadedPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ShadedPanel extends JPanel
{
    protected Color c1 = new Color(0xFFFFFF);
    protected Color c2 = new Color(0xC8D2DE);

    public ShadedPanel(LayoutManager layoutManager, boolean b)
    {
        super(layoutManager, b);
        this.setOpaque(false);
    }

    public ShadedPanel(LayoutManager layoutManager)
    {
        super(layoutManager);
        this.setOpaque(false);
    }

    public ShadedPanel(boolean b)
    {
        super(b);
        this.setOpaque(false);
    }

    public ShadedPanel()
    {
        this.setOpaque(false);
    }

    public void setColors(Color c1, Color c2)
    {
        this.c1 = c1;
        this.c2 = c2;
    }

    public void reverseShadingDirection()
    {
        Color t = this.c1;
        this.c1 = this.c2;
        this.c2 = t;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;

        // Creates a two-stops gradient
        GradientPaint p;
        p = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
//        p = new GradientPaint(0, 0, new Color(0x6b89c8),
//            0, getHeight(), new Color(0x000000));
//        p = new GradientPaint(0, 0, new Color(0x6b89c8),
//            0, getHeight(), this.getBackground());

        // Saves the state
        Paint oldPaint = g2.getPaint();

        // Paints the background
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Restores the state
        g2.setPaint(oldPaint);

        // Paints borders, text...
//            super.paintComponent(g);

    }
}
