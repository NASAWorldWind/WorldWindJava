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
