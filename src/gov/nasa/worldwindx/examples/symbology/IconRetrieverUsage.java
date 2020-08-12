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

package gov.nasa.worldwindx.examples.symbology;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
 * Example of using {@link IconRetriever} to retrieve the icon for a MIL-STD-2525C symbol. This example retrieves
 * several symbology icons as BufferedImages and displays them in a JFrame. See the <a
 * href="https://worldwind.arc.nasa.gov/java/tutorials/icon-retriever/" target="_blank">Icon Retriever Usage
 * Guide</a> for more information on using IconRetriever.
 *
 * @author pabercrombie
 * @version $Id: IconRetrieverUsage.java 521 2012-04-13 17:53:42Z pabercrombie $
 */
public class IconRetrieverUsage
{
    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.

    private static class AppFrame extends javax.swing.JFrame
    {
        protected IconRetriever iconRetriever;

        public AppFrame()
        {
            this.getContentPane().setLayout(new FlowLayout());

            // Create an icon retriever using the path specified in the config file, or the default path.
            String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
                MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
            this.iconRetriever = new MilStd2525IconRetriever(iconRetrieverPath);

            // Retrieve icons on a background thread. Icons may be retrieved from the network or a local disk.
            // This operation should not run on the UI thread.
            WorldWind.getTaskService().addTask(new Runnable()
            {
                public void run()
                {
                    AVList params = new AVListImpl();

                    // Create an icon with the default parameters.
                    BufferedImage image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
                    addLater(image, "Full symbol");

                    // Create a unframed icon.
                    params.setValue(SymbologyConstants.SHOW_FRAME, false);
                    image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
                    addLater(image, "No frame");

                    // Create a framed icon with no fill.
                    params.setValue(SymbologyConstants.SHOW_FRAME, true);
                    params.setValue(SymbologyConstants.SHOW_FILL, false);
                    image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
                    addLater(image, "No fill");

                    // Create an icon with a custom color.
                    params.setValue(AVKey.COLOR, Color.GREEN);
                    params.setValue(SymbologyConstants.SHOW_FRAME, true);
                    params.setValue(SymbologyConstants.SHOW_FILL, true);
                    image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
                    addLater(image, "Custom color");
                }
            });
        }

        protected void addLater(final BufferedImage image, final String text)
        {
            // Add labels to the frame on the Event Dispatch Thread.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    JLabel label = new JLabel(new ImageIcon(image));
                    label.setText(text);
                    getContentPane().add(label);
                    pack();
                }
            });
        }
    }

    public static void main(String[] args)
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Icon Retriever");
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                JFrame appFrame = new AppFrame();
                appFrame.setTitle("WorldWind Icon Retriever");
                appFrame.setVisible(true);
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }
}
