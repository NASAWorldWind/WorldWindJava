/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: SARAboutDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARAboutDialog extends AboutDialog
{
    public SARAboutDialog()
    {
        setContent("SARAbout.html");
        setContentType("text/html");
        setPreferredSize(new Dimension(400, 230));
    }

}
