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

import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 * @author tag
 * @version $Id: Util.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Util
{
    public static final String DECIMAL_SYMBOL = Character.toString(new DecimalFormatSymbols().getDecimalSeparator());

    public static Logger getLogger()
    {
        return Logger.getLogger("gov.nasa.worldwind");
    }

    public static Frame findParentFrame(Component c)
    {
        while (c != null)
        {
            if (c instanceof Frame)
                return (Frame) c;

            c = c.getParent();
        }

        return null;
    }

    public static File ensureFileSuffix(File file, String suffixWithoutPeriod)
    {
        String suffix = WWIO.getSuffix(file.getPath());
        if (suffix == null || !suffix.equalsIgnoreCase(suffixWithoutPeriod))
            return new File(file.getPath() + (suffix == null ? "." : "") + suffixWithoutPeriod);
        else
            return file;
    }

    public static void centerDialogInContainer(JDialog dialog, Container frame)
    {
        Dimension prefSize = dialog.getPreferredSize();
        java.awt.Point parentLocation = frame.getLocationOnScreen();
        Dimension parentSize = frame.getSize();//Toolkit.getDefaultToolkit().getScreenSize();
        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
        dialog.setLocation(x, y);
    }

    public static void positionDialogInContainer(JDialog dialog, Container frame, int horizontal, int vertical)
    {
        Dimension prefSize = dialog.getPreferredSize();
        java.awt.Point parentLocation = frame.getLocationOnScreen();
        Dimension parentSize = frame.getSize();

        // default to center
        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;

        switch (horizontal)
        {
            case SwingConstants.WEST:
                x = parentLocation.x;
                break;
            case SwingConstants.EAST:
                x = parentLocation.x + parentSize.width - prefSize.width;
                break;
        }

        switch (vertical)
        {
            case SwingConstants.NORTH:
                y = parentLocation.y;
                break;
            case SwingConstants.SOUTH:
                y = parentLocation.y + parentSize.height - prefSize.height;
                break;
        }

        dialog.setLocation(x, y);
    }

    public static String[] splitLines(String linesString)
    {
        if (WWUtil.isEmpty(linesString))
            return null;

        String[] lines = linesString.trim().split("\n");

        return lines.length > 0 ? lines : null;
    }

    public static String[] splitWords(String wordsString)
    {
        return splitWords(wordsString, ",");
    }

    public static String[] splitWords(String wordsString, String separators)
    {
        if (WWUtil.isEmpty(wordsString))
            return null;

        String[] words = wordsString.trim().split(separators);

        return words.length > 0 ? words : null;
    }

    public static String makeMultiLineToolTip(String original)
    {
        StringBuilder sb = new StringBuilder();

        if (!original.trim().toLowerCase().startsWith("<html>"))
            sb.append("<html>");

//        Pattern p = Pattern.compile("(.{0,80}\\b\\s*)|(.{80}\\B)");
        Pattern p = Pattern.compile("(.{0,80}\\b\\s*)");
        Matcher m = p.matcher(original);
        if (m.find())
            sb.append(original.substring(m.start(), m.end()).trim());
        while (m.find())
        {
            sb.append("<br>");
            sb.append(original.substring(m.start(), m.end()).trim());
        }

        if (!original.trim().toLowerCase().endsWith("</html>"))
            sb.append("</html>");

        return sb.toString();
    }
}
