/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.worldwindow.core.Controller;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.*;
import gov.nasa.worldwindx.applications.worldwindow.util.*;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.Box;
import java.awt.*;
import java.awt.event.*;

/**
 * Displays UI components for a set of caller specified imported data, and manages creation of WorldWind components
 * from that data. Callers fill the panel with imported data by invoking {@link #addImportedData(org.w3c.dom.Element,
 * gov.nasa.worldwind.avlist.AVList)}. This adds the UI components for a specified data set (a "Go To" button, and a
 * label description), creates a WorldWind component from the DataConfiguration, and adds the component to the World
 * Window passed to the panel during construction.
 *
 * @author dcollins
 * @version $Id: ImportedDataPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImportedDataPanel extends ShadedPanel
{
    protected Controller controller;
    protected JPanel dataConfigPanel;

    /**
     * Constructs an ImportedDataPanel with the specified title and WorldWindow. Upon construction, the panel is
     * configured to accept imported data via calls to {@link #addImportedData(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)}.
     *
     * @param title      the panel's title, displayed in a titled border.
     * @param controller the application controller.
     *
     * @throws IllegalArgumentException if the WorldWindow is null.
     */
    public ImportedDataPanel(String title, Controller controller)
    {
        super(new BorderLayout());

        if (controller == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.controller = controller;
        this.layoutComponents(title);
    }

    /**
     * Adds the UI components for the specified imported data to this panel, and adds the WorldWind component created
     * from the data to the WorldWindow passed to this panel during construction.
     *
     * @param domElement the document which describes a WorldWind data configuration.
     * @param params     the parameter list which overrides or extends information contained in the document.
     *
     * @throws IllegalArgumentException if the Element is null.
     */
    public void addImportedData(final Element domElement, final AVList params)
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
        box.setOpaque(false);
        box.add(new JButton(new GoToSectorAction(sector)));
        box.add(Box.createHorizontalStrut(10));
        JLabel descLabel = new JLabel(description);
        descLabel.setOpaque(false);
        box.add(descLabel);

        this.dataConfigPanel.add(box);
        this.revalidate();
    }

    protected void layoutComponents(String title)
    {
        this.add(new PanelTitle(title, SwingConstants.CENTER), BorderLayout.NORTH);

        this.dataConfigPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.dataConfigPanel.setOpaque(false);
        this.dataConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right

        // Put the grid in a container to prevent scroll panel from stretching its vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.setOpaque(false);
        dummyPanel.add(this.dataConfigPanel, BorderLayout.NORTH);

        // Add the dummy panel to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right

        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel bodyPanel = new JPanel(new GridLayout(0, 1, 0, 10)); // rows, cols, hgap, vgap
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(bodyPanel, BorderLayout.CENTER);
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
        try
        {
            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
            Layer layer = (Layer) factory.createFromConfigSource(domElement, params);
            if (layer != null)
            {
                layer.setEnabled(true);
                this.addLayer(layer, new LayerPath("Imported"));
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void addLayer(final Layer layer, final LayerPath pathToParent)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                LayerPath path = new LayerPath(pathToParent, layer.getName());
                doAddLayer(layer, path);
            }
        });
    }

    protected void doAddLayer(final Layer layer, final LayerPath path)
    {
        LayerManager layerManager = controller.getLayerManager();
        layerManager.addLayer(layer, path.lastButOne());
        layerManager.selectLayer(layer, true);
        layerManager.expandPath(path.lastButOne());
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

        ElevationModel defaultElevationModel = this.controller.getWWd().getModel().getGlobe().getElevationModel();
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
            this.controller.getWWd().getModel().getGlobe().setElevationModel(cm);
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
            Extent extent = Sector.computeBoundingCylinder(controller.getWWd().getModel().getGlobe(),
                controller.getWWd().getSceneController().getVerticalExaggeration(), this.sector);

            Angle fov = controller.getWWd().getView().getFieldOfView();
            Position centerPos = new Position(this.sector.getCentroid(), 0d);
            double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

            controller.getWWd().getView().goTo(centerPos, zoom);
        }
    }
}
