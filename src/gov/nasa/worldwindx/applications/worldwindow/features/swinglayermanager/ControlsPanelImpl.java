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

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.*;
import gov.nasa.worldwindx.applications.worldwindow.features.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: ControlsPanelImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ControlsPanelImpl extends AbstractFeature implements ControlsPanel
{
    private static final int DEFAULT_DIVIDER_LOCATION = 250;

    private JPanel panel;

    public ControlsPanelImpl(Registry registry)
    {
        super("Controls Panel", Constants.CONTROLS_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());

        LayerManager layerManager = (LayerManager) this.controller.getRegisteredObject(Constants.FEATURE_LAYER_MANAGER);
        if (layerManager != null && layerManager instanceof FeaturePanel)
            centerPanel.add(((FeaturePanel) layerManager).getJPanel(), BorderLayout.CENTER);

        ActiveLayersManager layerList = (ActiveLayersManager) this.controller.getRegisteredObject(
            Constants.FEATURE_ACTIVE_LAYERS_PANEL);
        if (layerList != null && layerList instanceof FeaturePanel)
            topPanel.add(((FeaturePanel) layerList).getJPanel(), BorderLayout.CENTER);

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(DEFAULT_DIVIDER_LOCATION);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(centerPanel);

        this.panel.add(splitPane, BorderLayout.CENTER);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }
}
