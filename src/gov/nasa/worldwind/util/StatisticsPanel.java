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

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @version $Id: StatisticsPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class StatisticsPanel extends JPanel
{
    private JPanel statsPanel;
    private JPanel outerPanel;
    private JScrollPane scrollPane;
    private WorldWindow wwd;
    private int updateInterval = 500;
    private long lastUpdate;

    public StatisticsPanel(WorldWindow wwd)
    {
        // Make a panel at a default size.
        super(new BorderLayout());
        this.makePanel(new Dimension(200, 400));
    }

    public StatisticsPanel(WorldWindow wwd, Dimension size)
    {
        // Make a panel at a specified size.
        super(new BorderLayout());

        this.wwd = wwd;
        this.makePanel(size);
        wwd.setPerFrameStatisticsKeys(PerformanceStatistic.ALL_STATISTICS_SET);

        wwd.addRenderingListener(new RenderingListener()
        {
            public void stageChanged(RenderingEvent event)
            {
                long now = System.currentTimeMillis();
                if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP)
                    && event.getSource() instanceof WorldWindow && now - lastUpdate > updateInterval)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            update();
                        }
                    });
                    lastUpdate = now;
                }
            }
        });
    }

    private void makePanel(Dimension size)
    {
        // Make and fill the panel holding the statistics.
        this.statsPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        this.statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Must put the grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.statsPanel, BorderLayout.NORTH);

        // Put the name panel in a scroll bar.
        this.scrollPane = new JScrollPane(dummyPanel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null)
            this.scrollPane.setPreferredSize(size);

        // Add the scroll bar and stats panel to a titled panel that will resize with the main window.
        outerPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        outerPanel
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Statistics")));
        outerPanel.setToolTipText("Runtime Statistics");
        outerPanel.add(scrollPane);
        this.add(outerPanel, BorderLayout.CENTER);
    }

    private void fill(WorldWindow wwd)
    {
        if (wwd.getSceneController().getPerFrameStatistics().size() < 1)
            return;

        PerformanceStatistic[] pfs = new PerformanceStatistic[wwd.getPerFrameStatistics().size()];
        pfs = wwd.getSceneController().getPerFrameStatistics().toArray(pfs);
        Arrays.sort(pfs);
        for (PerformanceStatistic stat : pfs)
        {
            JLabel jcb = new JLabel(stat.toString());
            this.statsPanel.add(jcb);
        }
    }

    public void update(WorldWindow wwd)
    {
        // Replace all the statistics in the panel with the current ones.
        this.statsPanel.removeAll();
        this.fill(wwd);
        this.outerPanel.revalidate();
        this.outerPanel.repaint();
    }

    public void update()
    {
        // Replace all the statistics in the panel with the current ones.
        this.statsPanel.removeAll();
        this.fill(this.wwd);
        this.outerPanel.revalidate();
        this.outerPanel.repaint();
    }

    @Override
    public void setToolTipText(String string)
    {
        this.scrollPane.setToolTipText(string);
    }
}
