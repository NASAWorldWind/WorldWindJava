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

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.poi.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * @author tag
 * @version $Id: GazetteerPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class GazetteerPanel extends AbstractFeature implements FeaturePanel
{
    private JPanel panel;
    private Gazetteer gazetteer;

    public GazetteerPanel(Registry registry)
    {
        super("Gazetteer Panel", Constants.FEATURE_GAZETTEER_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        this.gazetteer = this.getGazetteer();

        this.panel.setOpaque(false);
        createComponents(this.panel);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public JComponent[] getDialogControls()
    {
        return null;
    }

    private Gazetteer getGazetteer()
    {
        if (this.gazetteer != null)
            return this.gazetteer;

        Object o = controller.getRegisteredObject(Constants.FEATURE_GAZETTEER);

        return o instanceof Gazetteer ? (Gazetteer) o : null;
    }

    private void createComponents(JPanel p)
    {
        String tt = "Any of these:  45.5 -120.2   or   45 30 0 n 120 12 0 w   or   Seattle";

        JComboBox field = new JComboBox();
        field.setOpaque(false);
        field.setEditable(true);
        field.setLightWeightPopupEnabled(false);
        field.setPreferredSize(new Dimension(200, field.getPreferredSize().height));
        field.setToolTipText(tt);

        JLabel label = new JLabel(
            ImageLibrary.getIcon("gov/nasa/worldwindx/applications/worldwindow/images/safari-24x24.png"));
//            new ImageIcon(getClass().getResource("gov/nasa/worldwindx/applications/worldwindow/images/safari-24x24.png")));
        label.setOpaque(false);
        label.setToolTipText(tt);

        p.add(label, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);

        field.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                performGazeteerAction(actionEvent);
            }
        });
    }

    private void performGazeteerAction(final ActionEvent e)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    handleEntryAction(e);
                }
                catch (NoItemException e)
                {
                    controller.showMessageDialog("No search string was specified", "No Search String",
                        JOptionPane.ERROR_MESSAGE);
                }
                catch (Exception e)
                {
                    controller.showMessageDialog("Location not found", "Location Unknown", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void handleEntryAction(ActionEvent actionEvent) throws IOException, ParserConfigurationException,
        XPathExpressionException, SAXException, NoItemException
    {
        if (this.getGazetteer() == null)
        {
            Util.getLogger().severe("No gazeteer is registered");
            return;
        }

        String lookupString;

        JComboBox cmb = ((JComboBox) actionEvent.getSource());
        lookupString = cmb.getSelectedItem().toString();

        if (lookupString == null || lookupString.length() < 1)
            return;

        java.util.List<PointOfInterest> results = this.gazetteer.findPlaces(lookupString);
        if (results == null || results.size() == 0)
            return;

        this.controller.moveToLocation(results.get(0));

        // Add it to the list if not already there
        for (int i = 0; i < cmb.getItemCount(); i++)
        {
            Object oi = cmb.getItemAt(i);
            if (oi != null && oi.toString().trim().equals(lookupString))
                return; // item exists
        }
        cmb.insertItemAt(lookupString, 0);
    }
}
