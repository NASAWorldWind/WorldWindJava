/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Allocates colors used to key data sets to the visible sectors they cover.
 *
 * @author tag
 * @version $Id: ColorAllocator.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class ColorAllocator
{
    protected static ConcurrentLinkedQueue<Color> initialColors = new ConcurrentLinkedQueue<Color>();

    static
    {
        initializeColors();
    }

    public static void initializeColors()
    {
        initialColors.clear();

        // Create some standard first-used colors. Just add to this list to define more.
        initialColors.add(Color.YELLOW);
        initialColors.add(Color.GREEN);
        initialColors.add(Color.BLUE);
        initialColors.add(Color.CYAN);
        initialColors.add(Color.MAGENTA);
        initialColors.add(Color.RED);
        initialColors.add(Color.ORANGE);
        initialColors.add(Color.PINK);
    }

    public static Color getNextColor()
    {
        // Try to use a pre-defined color.
        if (initialColors.size() > 0)
            return initialColors.poll();

        // No more pre-defined colors left, so use a random color.
        return WWUtil.makeRandomColor(null);
    }
}
