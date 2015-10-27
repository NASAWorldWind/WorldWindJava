/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.layermanager;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Displays the name and controls for one elevation model in the elevation model manager.
 *
 * @author tag
 * @version $Id: ElevationModelPanel.java 1179 2013-02-15 17:47:37Z tgaskins $
 */
public class ElevationModelPanel extends JPanel
{
    protected ElevationModel elevationModel;

    protected JCheckBox checkBox;
    protected JButton upButton;
    protected JButton downButton;

    public ElevationModelPanel(final WorldWindow wwd, final ElevationModelManagerPanel emPanel,
                               final ElevationModel elevationModel)
    {
        super(new BorderLayout(10, 10));

        this.elevationModel = elevationModel;
        SelectModelAction action = new SelectModelAction(wwd, elevationModel, elevationModel.isEnabled());
        this.checkBox = new JCheckBox(action);
        this.checkBox.setSelected(action.selected);
        this.add(this.checkBox, BorderLayout.CENTER);

        this.upButton = new JButton(LayerPanel.UP_ARROW);
        this.upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                moveElevationModel(wwd, elevationModel, -1);
                emPanel.update(wwd);
            }
        });

        this.downButton = new JButton(LayerPanel.DOWN_ARROW);
        this.downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                moveElevationModel(wwd, elevationModel, +1);
                emPanel.update(wwd);
            }
        });

        // The buttons shouldn't look like actual JButtons.
        this.upButton.setBorderPainted(false);
        this.upButton.setContentAreaFilled(false);
        this.upButton.setPreferredSize(new Dimension(24, 24));
        this.downButton.setBorderPainted(false);
        this.downButton.setContentAreaFilled(false);
        this.downButton.setPreferredSize(new Dimension(24, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 0));
        buttonPanel.add(this.upButton);
        buttonPanel.add(this.downButton);
        this.add(buttonPanel, BorderLayout.EAST);

        int index = this.findElevationModelPosition(wwd, elevationModel);
        this.upButton.setEnabled(index != 0);
        this.downButton.setEnabled(index != this.getNumElevationModels(wwd) - 1);
    }

    public ElevationModel getElevationModel()
    {
        return this.elevationModel;
    }

    protected void moveElevationModel(WorldWindow wwd, ElevationModel elevationModel, int direction)
    {
        // Moves the model associated with this instance in the direction indicated relative to the other models.

        int index = this.findElevationModelPosition(wwd, elevationModel);
        if (index < 0)
            return; // model not found or not a globe isn't using a CompoundElevationModel

        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

        if (direction < 0 && index == 0) // can't move lowest layer any lower
            return;

        if (direction > 0 && index == cem.getElevationModels().size() - 1) // can't move highest model any higher
            return;

        // Remove the model from the model list and then re-insert it.

        cem.getElevationModels().remove(elevationModel);

        if (direction > 0)
            cem.getElevationModels().add(index + 1, elevationModel);
        else if (direction < 0)
            cem.getElevationModels().add(index - 1, elevationModel);

        // Update WorldWind so the change is visible.
        wwd.redraw();
    }

    protected int findElevationModelPosition(WorldWindow wwd, ElevationModel elevationModel)
    {
        // Determines the ordinal location of an elevation model in the globe's elevation models.

        if (!(wwd.getModel().getGlobe().getElevationModel() instanceof CompoundElevationModel))
            return -1;

        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

        for (int i = 0; i < cem.getElevationModels().size(); i++)
        {
            if (elevationModel == cem.getElevationModels().get(i))
                return i;
        }

        return -1;
    }

    protected int getNumElevationModels(WorldWindow wwd)
    {
        // Determines the ordinal location of an elevation model in the globe's elevation models.

        if (!(wwd.getModel().getGlobe().getElevationModel() instanceof CompoundElevationModel))
            return 1;

        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();
        return cem.getElevationModels().size();
    }

    protected static class SelectModelAction extends AbstractAction
    {
        // This action handles elevation model selection and de-selection.

        protected WorldWindow wwd;
        protected ElevationModel elevationModel;
        protected boolean selected;

        public SelectModelAction(WorldWindow wwd, ElevationModel elevationModel, boolean selected)
        {
            super(elevationModel.getName());

            this.wwd = wwd;
            this.elevationModel = elevationModel;
            this.selected = selected;
            this.elevationModel.setEnabled(this.selected);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Simply enable or disable the model based on its toggle button.
            if (((JCheckBox) actionEvent.getSource()).isSelected())
                this.elevationModel.setEnabled(true);
            else
                this.elevationModel.setEnabled(false);

            wwd.redraw();
        }
    }
}
