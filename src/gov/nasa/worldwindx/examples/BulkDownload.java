/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * Illustrates how to use WorldWind to retrieve data from layers and elevation models in bulk from a remote source.
 * This class uses a <code>{@link gov.nasa.worldwindx.examples.util.SectorSelector}</code> to specify the geographic area
 * to retrieve, then retrieves data for the specified area using the <code>{@link gov.nasa.worldwind.retrieve.BulkRetrievable}</code>
 * interface on layers and elevation models that support it.
 *
 * @author Patrick Murris
 * @version $Id: BulkDownload.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class BulkDownload extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Add the bulk download control panel.
            this.getControlPanel().add(new BulkDownloadPanel(this.getWwd()), BorderLayout.SOUTH);

            // Size the application window to provide enough screen space for the WorldWindow and the bulk download
            // panel, then center the application window on the screen.
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Bulk Download", AppFrame.class);
    }
}
