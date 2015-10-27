/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: AbstractFeatureLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractFeatureLayer extends AbstractFeature
{
    protected Layer layer;
    private boolean twoState = false;

    protected abstract Layer doAddLayer();

    protected AbstractFeatureLayer(String featureTitle, String featureID, String iconFilePath, boolean twoState,
        Registry registry)
    {
        super(featureTitle, featureID, iconFilePath, registry);

        this.twoState = twoState;
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.layer = this.doAddLayer();
    }

    @Override
    public void setEnabled(boolean tf)
    {
        super.setEnabled(tf);

        if (this.layer != null)
            this.layer.setEnabled(isEnabled());
    }

    public boolean isOn()
    {
        return this.layer != null && this.isEnabled() && this.layer.isEnabled()
            && this.controller.getActiveLayers().contains(this.layer);
    }

    @Override
    public boolean isTwoState()
    {
        return this.twoState;
    }

    @Override
    public void turnOn(boolean tf)
    {
        boolean currentState = this.isOn();

        layer.setEnabled(tf);

        this.firePropertyChange(Constants.ON_STATE, currentState, layer.isEnabled());

        controller.redraw();
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        this.turnOn(this.layer == null || !layer.isEnabled());
        controller.redraw();
    }
}
