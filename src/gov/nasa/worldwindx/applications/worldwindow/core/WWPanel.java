/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: WWPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface WWPanel extends WWOPanel, Initializable
{
    Dimension getSize();

    WorldWindow getWWd();

    void insertBeforeNamedLayer(Layer layer, String targetLayerName);

    void insertAfterNamedLayer(Layer layer, String targetLayerName);

    void addLayer(Layer layer);

    public JPanel getJPanel();

    void removeLayer(Layer layer);
}
