/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
