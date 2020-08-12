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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.vpf.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Illustrates how to import data from a Vector Product Format (VPF) database into WorldWind. This uses <code>{@link
 * VPFLayer}</code> to display an imported VPF database, and uses <code>{@link VPFCoveragePanel}</code> to enable the
 * user to choose which shapes from the VPF database to display.
 * <p>
 * To display VPF shapes with the appropriate color, style, and icon, applications must include the JAR file
 * <code>vpf-symbols.jar</code> in the Java class-path. If this JAR file is not in the Java class-path, VPFLayer outputs
 * the following message in the WorldWind log: <code>WARNING: GeoSym style support is disabled</code>. In this case,
 * VPF shapes are displayed as gray outlines, and icons are displayed as a gray question mark.
 *
 * @author dcollins
 * @version $Id: VPFLayerDemo.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class VPFLayerDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            this.makeControlPanel();
        }

        protected void addVPFLayer(File file)
        {
            VPFDatabase db = VPFUtils.readDatabase(file);
            VPFLayer layer = new VPFLayer(db);
            insertBeforePlacenames(this.getWwd(), layer);
            this.openVPFCoveragePanel(db, layer);
        }

        protected void openVPFCoveragePanel(VPFDatabase db, VPFLayer layer)
        {
            VPFCoveragePanel panel = new VPFCoveragePanel(getWwd(), db);
            panel.setLayer(layer);
            JFrame frame = new JFrame(db.getName());
            frame.setResizable(true);
            frame.setAlwaysOnTop(true);
            frame.add(panel);
            frame.pack();
            WWUtil.alignComponent(this, frame, AVKey.CENTER);
            frame.setVisible(true);
        }

        protected void showOpenDialog()
        {
            JFileChooser fc = new JFileChooser(Configuration.getUserHomeDirectory());
            fc.addChoosableFileFilter(new VPFFileFilter());

            int retVal = fc.showOpenDialog(this);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            this.addVPFLayer(file);
        }

        protected void makeControlPanel()
        {
            JButton button = new JButton("Open VPF Database");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenDialog();
                }
            });

            Box box = Box.createHorizontalBox();
            box.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // top, left, bottom, right
            box.add(button);

            this.getControlPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static class VPFFileFilter extends FileFilter
    {
        protected VPFDatabaseFilter filter;

        public VPFFileFilter()
        {
            this.filter = new VPFDatabaseFilter();
        }

        public boolean accept(File file)
        {
            if (file == null)
            {
                String message = Logging.getMessage("nullValue.FileIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return file.isDirectory() || this.filter.accept(file);
        }

        public String getDescription()
        {
            return "VPF Databases (dht)";
        }
    }

    public static void main(String[] args)
    {
        start("WorldWind VPF Shapes", AppFrame.class);
    }
}
