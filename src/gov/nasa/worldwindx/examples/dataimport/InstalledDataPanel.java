/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Displays UI components for a set of caller specified installed data, and manages creation of World Wind components
 * from that data. Callers fill the panel with installed data by invoking <code>{@link
 * #addInstalledData(org.w3c.dom.Element, gov.nasa.worldwind.avlist.AVList)}</code>. This adds the UI components for a
 * specified data set (a <code>Go To</code> button, and a label description), creates a World Wind component from the
 * DataConfiguration, and adds the component to the World Window passed to the panel during construction.
 *
 * @author dcollins
 * @version $Id: InstalledDataPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class InstalledDataPanel extends JPanel
{
    protected WorldWindow worldWindow;
    protected JPanel dataConfigPanel;

    /**
     * Constructs an InstalledDataPanel with the specified title and WorldWindow. Upon construction, the panel is
     * configured to accept installed data via calls to {@link #addInstalledData(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)}.
     *
     * @param title       the panel's title, displayed in a titled border.
     * @param worldWindow the panel's WorldWindow, which any World Wind components are added to.
     *
     * @throws IllegalArgumentException if the WorldWindow is null.
     */
    public InstalledDataPanel(String title, WorldWindow worldWindow)
    {
        if (worldWindow == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.worldWindow = worldWindow;
        this.dataConfigPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.layoutComponents(title);
    }

    /**
     * Adds the UI components for the specified installed data to this panel, and adds the World Wind component created
     * from the data to the WorldWindow passed to this panel during construction.
     *
     * @param domElement the document which describes a World Wind data configuration.
     * @param params     the parameter list which overrides or extends information contained in the document.
     *
     * @throws IllegalArgumentException if the Element is null.
     */
    public void addInstalledData(final Element domElement, final AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addToWorldWindow(domElement, params);

        String description = this.getDescription(domElement);
        Sector sector = this.getSector(domElement);

        Box box = Box.createHorizontalBox();
        box.add(new JButton(new GoToSectorAction(sector)));
        box.add(Box.createHorizontalStrut(10));
        box.add(new JLabel(description));

        this.dataConfigPanel.add(box);
        this.revalidate();
    }

    protected void layoutComponents(String title)
    {
        this.dataConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right
        // Put the grid in a container to prevent scroll panel from stretching its vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.dataConfigPanel, BorderLayout.NORTH);
        // Add the dummy panel to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right
        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 10)); // rows, cols, hgap, vgap
        titlePanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder(title)));
        titlePanel.add(scrollPane);

        this.setLayout(new GridLayout(0, 1, 0, 0)); // rows, cols, hgap, vgap
        this.add(titlePanel);
    }

    //**************************************************************//
    //********************  DataConfiguration Utils  ***************//
    //**************************************************************//

    protected String getDescription(Element domElement)
    {
        String displayName = DataConfigurationUtils.getDataConfigDisplayName(domElement);
        String type = DataConfigurationUtils.getDataConfigType(domElement);

        StringBuilder sb = new StringBuilder(displayName);

        if (type.equalsIgnoreCase("Layer"))
        {
            sb.append(" (Layer)");
        }
        else if (type.equalsIgnoreCase("ElevationModel"))
        {
            sb.append(" (Elevations)");
        }

        return sb.toString();
    }

    protected Sector getSector(Element domElement)
    {
        return WWXML.getSector(domElement, "Sector", null);
    }

    protected void addToWorldWindow(Element domElement, AVList params)
    {
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type == null)
            return;

        if (type.equalsIgnoreCase("Layer"))
        {
            this.addLayerToWorldWindow(domElement, params);
        }
        else if (type.equalsIgnoreCase("ElevationModel"))
        {
            this.addElevationModelToWorldWindow(domElement, params);
        }
    }

    protected void addLayerToWorldWindow(Element domElement, AVList params)
    {
        Layer layer = null;
        try
        {
            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
            layer = (Layer) factory.createFromConfigSource(domElement, params);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        if (layer == null)
            return;

        layer.setEnabled(true); // TODO: BasicLayerFactory creates layer which is intially disabled

        if (!this.worldWindow.getModel().getLayers().contains(layer))
            ApplicationTemplate.insertBeforePlacenames(this.worldWindow, layer);
    }

    protected void addElevationModelToWorldWindow(Element domElement, AVList params)
    {
        ElevationModel em = null;
        try
        {
            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
            em = (ElevationModel) factory.createFromConfigSource(domElement, params);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        if (em == null)
            return;

        ElevationModel defaultElevationModel = this.worldWindow.getModel().getGlobe().getElevationModel();
        if (defaultElevationModel instanceof CompoundElevationModel)
        {
            if (!((CompoundElevationModel) defaultElevationModel).containsElevationModel(em))
                ((CompoundElevationModel) defaultElevationModel).addElevationModel(em);
        }
        else
        {
            CompoundElevationModel cm = new CompoundElevationModel();
            cm.addElevationModel(defaultElevationModel);
            cm.addElevationModel(em);
            this.worldWindow.getModel().getGlobe().setElevationModel(cm);
        }
    }

    //**************************************************************//
    //********************  Actions  *******************************//
    //**************************************************************//

    protected class GoToSectorAction extends AbstractAction
    {
        protected Sector sector;

        public GoToSectorAction(Sector sector)
        {
            super("Go To");
            this.sector = sector;
            this.setEnabled(this.sector != null);
        }

        public void actionPerformed(ActionEvent e)
        {
            ExampleUtil.goTo(worldWindow, this.sector);
        }
    }
}
