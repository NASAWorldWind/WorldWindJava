/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;

import javax.swing.*;

/**
 * @author tag
 * @version $Id: AbstractFeaturePanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractFeaturePanel extends AbstractFeature implements FeaturePanel
{
    protected JPanel panel;

    public AbstractFeaturePanel(String s, String featureID, JPanel panel, Registry registry)
    {
        super(s, featureID, registry);

        panel.putClientProperty(Constants.FEATURE, this);
        this.panel = panel;
    }

    public AbstractFeaturePanel(String s, String featureID, String largeIconPath, JPanel panel, Registry registry)
    {
        super(s, featureID, largeIconPath, registry);

        panel.putClientProperty(Constants.FEATURE, this);
        this.panel = panel;
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        if (this.panel != null)
            this.panel.putClientProperty(Constants.FEATURE_OWNER_PROPERTY, this);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public JComponent[] getDialogControls()
    {
        return null;
    }
}
