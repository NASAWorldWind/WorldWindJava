/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util;

import java.awt.*;

/**
 * @author tag
 * @version $Id: GB.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GB extends GridBagConstraints
{
    public GB(int gridx, int gridy)
    {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public GB(int gridx, int gridy, int gridwidth, int gridheight)
    {
        this.gridx = gridx;
        this.gridy = gridy;
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
    }

    public GB setAnchor(int anchor)
    {
        this.anchor = anchor;
        return this;
    }

    public GB setFill(int fill)
    {
        this.fill = fill;
        return this;
    }

    public GB setWeight(double weightx, double weighty)
    {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }

    public GB setInsets(int distance)
    {
        this.insets = new Insets(distance, distance, distance, distance);
        return this;
    }

    public GB setInsets(int top, int left, int bottom, int right)
    {
        this.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public GB setIpad(int ipadx, int ipady)
    {
        this.ipadx = ipadx;
        this.ipady = ipady;
        return this;
    }
}
