/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
