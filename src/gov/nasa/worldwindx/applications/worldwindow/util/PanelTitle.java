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
