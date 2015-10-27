/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.render.Material;

import java.awt.*;

/**
 * Defines constants used by the MIL-STD-2525 symbology classes.
 *
 * @author dcollins
 * @version $Id: MilStd2525Constants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MilStd2525Constants
{
    /**
     * The default location that MIL-STD-2525 tactical symbols and tactical point graphics retrieve their icons from:
     * http://worldwind.arc.nasa.gov/milstd2525c/rev1/
     */
    final String DEFAULT_ICON_RETRIEVER_PATH = "http://worldwind.arc.nasa.gov/milstd2525c/rev1/";

    // Color RGB values from MIL-STD-2525C Table XIII, pg. 44.
    /** Default material used to color tactical graphics that represent friendly entities. */
    final Material MATERIAL_FRIEND = Material.BLACK;
    /** Default material used to color tactical graphics that represent hostile entities. */
    final Material MATERIAL_HOSTILE = new Material(new Color(255, 48, 49));
    /** Default material used to color tactical graphics that represent neutral entities. */
    final Material MATERIAL_NEUTRAL = new Material(new Color(0, 226, 0));
    /** Default material used to color tactical graphics that represent unknown entities. */
    final Material MATERIAL_UNKNOWN = new Material(new Color(255, 255, 0));
    /** Default material used to color tactical graphics that represent obstacles. */
    final Material MATERIAL_OBSTACLE = new Material(new Color(0, 226, 0));
}
