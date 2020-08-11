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

package gov.nasa.worldwindx.applications.sar;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: ControlPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ControlPanel extends JPanel
{
    protected TracksPanel tracksPanel;
	protected AnalysisPanel analysisPanel;

    public ControlPanel()
    {
        this.initComponents();
        this.layoutComponents();
	}

    public TracksPanel getTracksPanel()
    {
        return this.tracksPanel;
    }

    public AnalysisPanel getAnalysisPanel()
    {
        return this.analysisPanel;
    }

    protected void initComponents()
    {
		this.tracksPanel = new TracksPanel();
		this.analysisPanel = new AnalysisPanel();
	}

    protected void layoutComponents()
    {
        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap

        this.analysisPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 0, 10)); // top, left, bottom, right

        // Create a vertical split pane with a continuous layout. Put the track table panel in the top, and put the
        // track controls panel in the bottom.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, this.tracksPanel, this.analysisPanel);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.5);
		this.add(splitPane, BorderLayout.CENTER);
    }
}
