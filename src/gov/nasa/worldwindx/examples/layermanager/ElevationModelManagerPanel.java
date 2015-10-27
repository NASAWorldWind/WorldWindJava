/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.layermanager;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.List;

/**
 * Manages visibility and Z order of elevation models, similar to the layer manager.
 *
 * @author tag
 * @version $Id: ElevationModelManagerPanel.java 2071 2014-06-21 21:04:34Z tgaskins $
 */
public class ElevationModelManagerPanel extends JPanel
{
    protected JPanel modelNamesPanel;
    protected List<ElevationModelPanel> modelPanels = new ArrayList<ElevationModelPanel>();


    public ElevationModelManagerPanel(final WorldWindow wwd)
    {
        super(new BorderLayout(10, 10));

        this.modelNamesPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        this.modelNamesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add the panel to a titled panel.
        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 10));
        titlePanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Elevations")));
        titlePanel.setToolTipText("Elevation models to use");
        titlePanel.add(this.modelNamesPanel);
        this.add(titlePanel, BorderLayout.CENTER);

        this.fill(wwd);

        // Add a property change listener that causes this panel to be updated whenever the elevation model list
        // changes.
        wwd.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                if (propertyChangeEvent.getPropertyName().equals(AVKey.ELEVATION_MODEL))
                    if (!SwingUtilities.isEventDispatchThread())
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    public void run()
                                    {
                                        update(wwd);
                                    }
                                });
                            }
                        });
                    else
                        update(wwd);
            }
        });
    }

    public void update(WorldWindow wwd)
    {
        // Repopulate this elevation model manager.

        this.fill(wwd);
        this.revalidate();
        this.repaint();
    }

    protected void fill(WorldWindow wwd)
    {
        // Populate this manager with an entry for each elevation model in the WorldWindow.

        if (this.isUpToDate(wwd))
            return;

        // First remove all the existing entries.
        this.modelPanels.clear();
        this.modelNamesPanel.removeAll();

        // Fill the panel with the titles of all elevation models in the WorldWindow.

        if (!(wwd.getModel().getGlobe().getElevationModel() instanceof CompoundElevationModel))
        {
            ElevationModelPanel elevationModelPanel = new ElevationModelPanel(wwd, this,
                wwd.getModel().getGlobe().getElevationModel());
            this.modelPanels.add(elevationModelPanel);
            this.modelNamesPanel.add(elevationModelPanel);
        }
        else
        {
            CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

            for (ElevationModel elevationModel : cem.getElevationModels())
            {
                if (elevationModel.getValue(AVKey.IGNORE) != null)
                    continue;

                ElevationModelPanel elevationModelPanel = new ElevationModelPanel(wwd, this, elevationModel);
                this.modelPanels.add(elevationModelPanel);
                this.modelNamesPanel.add(elevationModelPanel);
            }
        }
    }

    protected boolean isUpToDate(WorldWindow wwd)
    {
        // Determines whether this manager's elevation model list is consistent with the specified WorldWindow's.
        // Knowing this prevents redundant updates.

        if (!(wwd.getModel().getGlobe().getElevationModel() instanceof CompoundElevationModel))
        {
            return this.modelPanels.get(0).getElevationModel() == wwd.getModel().getGlobe().getElevationModel();
        }

        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

        if (this.modelPanels.size() != cem.getElevationModels().size())
            return false;

        for (int i = 0; i < cem.getElevationModels().size(); i++)
        {
            if (cem.getElevationModels().get(i) != this.modelPanels.get(i).getElevationModel())
                return false;
        }

        return true;
    }
}
