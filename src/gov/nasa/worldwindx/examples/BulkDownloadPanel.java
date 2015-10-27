/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.cache.BasicDataFileStore;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Bulk download panel.
 *
 * @author Patrick Murris
 * @version $Id: BulkDownloadPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see BulkRetrievalThread
 * @see BulkRetrievable
 */
public class BulkDownloadPanel extends JPanel
{
    @SuppressWarnings( {"FieldCanBeLocal"})
    protected WorldWindow wwd;
    protected Sector currentSector;
    protected ArrayList<BulkRetrievablePanel> retrievables;

    protected JButton selectButton;
    protected JLabel sectorLabel;
    protected JButton startButton;
    protected JPanel monitorPanel;
    protected BasicDataFileStore cache;

    protected SectorSelector selector;

    public BulkDownloadPanel(WorldWindow wwd)
    {
        this.wwd = wwd;

        // Init retievable list
        this.retrievables = new ArrayList<BulkRetrievablePanel>();
        // Layers
        for (Layer layer : this.wwd.getModel().getLayers())
        {
            if (layer instanceof BulkRetrievable)
                this.retrievables.add(new BulkRetrievablePanel((BulkRetrievable) layer));
        }
        // Elevation models
        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();
        for (ElevationModel elevationModel : cem.getElevationModels())
        {
            if (elevationModel instanceof BulkRetrievable)
                this.retrievables.add(new BulkRetrievablePanel((BulkRetrievable) elevationModel));
        }

        // Init sector selector
        this.selector = new SectorSelector(wwd);
        this.selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
        this.selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
        this.selector.setBorderWidth(3);
        this.selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateSector();
            }
        });

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        this.initComponents();
    }

    protected void updateSector()
    {
        this.currentSector = this.selector.getSector();
        if (this.currentSector != null)
        {
            // Update sector description
            this.sectorLabel.setText(makeSectorDescription(this.currentSector));
            this.selectButton.setText("Clear sector");
            this.startButton.setEnabled(true);
        }
        else
        {
            // null sector
            this.sectorLabel.setText("-");
            this.selectButton.setText("Select sector");
            this.startButton.setEnabled(false);
        }
        updateRetrievablePanels(this.currentSector);
    }

    protected void updateRetrievablePanels(Sector sector)
    {
        for (BulkRetrievablePanel panel : this.retrievables)
        {
            panel.updateDescription(sector);
        }
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void selectButtonActionPerformed(ActionEvent event)
    {
        if (this.selector.getSector() != null)
        {
            this.selector.disable();
        }
        else
        {
            this.selector.enable();
        }
        updateSector();
    }

    /** Clear the current selection sector and remove it from the globe. */
    public void clearSector()
    {
        if (this.selector.getSector() != null)
        {
            this.selector.disable();
        }
        updateSector();
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void startButtonActionPerformed(ActionEvent event)
    {
        for (BulkRetrievablePanel panel : this.retrievables)
        {
            if (panel.selectCheckBox.isSelected())
            {
                BulkRetrievable retrievable = panel.retrievable;
                BulkRetrievalThread thread = retrievable.makeLocal(this.currentSector, 0, this.cache,
                    new BulkRetrievalListener()
                    {
                        public void eventOccurred(BulkRetrievalEvent event)
                        {
                            // This is how you'd include a retrieval listener. Uncomment below to monitor downloads.
                            // Be aware that the method is not invoked on the event dispatch thread, so any interaction
                            // with AWT or Swing must be within a SwingUtilities.invokeLater() runnable.

                            //System.out.printf("%s: item %s\n",
                            //    event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_SUCCEEDED) ? "Succeeded"
                            //: event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_FAILED) ? "Failed"
                            //    : "Unknown event type", event.getItem());
                        }
                    });

                if (thread != null)
                    this.monitorPanel.add(new DownloadMonitorPanel(thread));
            }
        }
        this.getTopLevelAncestor().validate();
    }

    /**
     * Determines whether there are any active downloads running.
     *
     * @return <code>true</code> if at leat one download thread is active.
     */
    public boolean hasActiveDownloads()
    {
        for (Component c : this.monitorPanel.getComponents())
        {
            if (c instanceof DownloadMonitorPanel)
                if (((DownloadMonitorPanel) c).thread.isAlive())
                    return true;
        }
        return false;
    }

    /** Cancel all active downloads. */
    public void cancelActiveDownloads()
    {
        for (Component c : this.monitorPanel.getComponents())
        {
            if (c instanceof DownloadMonitorPanel)
            {
                if (((DownloadMonitorPanel) c).thread.isAlive())
                {
                    DownloadMonitorPanel panel = (DownloadMonitorPanel) c;
                    panel.cancelButtonActionPerformed(null);
                    try
                    {
                        // Wait for thread to die before moving on
                        long t0 = System.currentTimeMillis();
                        while (panel.thread.isAlive() && System.currentTimeMillis() - t0 < 500)
                        {
                            Thread.sleep(10);
                        }
                    }
                    catch (Exception ignore)
                    {
                    }
                }
            }
        }
    }

    /** Remove inactive downloads from the monitor panel. */
    public void clearInactiveDownloads()
    {
        for (int i = this.monitorPanel.getComponentCount() - 1; i >= 0; i--)
        {
            Component c = this.monitorPanel.getComponents()[i];
            if (c instanceof DownloadMonitorPanel)
            {
                DownloadMonitorPanel panel = (DownloadMonitorPanel) c;
                if (!panel.thread.isAlive() || panel.thread.isInterrupted())
                {
                    this.monitorPanel.remove(i);
                }
            }
        }
        this.monitorPanel.validate();
    }

    protected void initComponents()
    {
        int border = 6;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Download")));
        this.setToolTipText("Layer imagery bulk download.");

        final JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
        JLabel locationLabel = new JLabel(" Cache:");
        final JLabel locationName = new JLabel("");
        JButton locationButton = new JButton("...");
        locationPanel.add(locationLabel, BorderLayout.WEST);
        locationPanel.add(locationName, BorderLayout.CENTER);
        locationPanel.add(locationButton, BorderLayout.EAST);
        this.add(locationPanel);

        locationButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                int status = fc.showOpenDialog(locationPanel);
                if (status == JFileChooser.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    if (file != null)
                    {
                        locationName.setText(file.getPath());
                        cache = new BasicDataFileStore(file);
                        updateRetrievablePanels(selector.getSector());
                    }
                }
            }
        });

        // Select sector button
        JPanel sectorPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        sectorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        selectButton = new JButton("Select sector");
        selectButton.setToolTipText("Press Select then press and drag button 1 on globe");
        selectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                selectButtonActionPerformed(event);
            }
        });
        sectorPanel.add(selectButton);
        sectorLabel = new JLabel("-");
        sectorLabel.setPreferredSize(new Dimension(350, 16));
        sectorLabel.setHorizontalAlignment(JLabel.CENTER);
        sectorPanel.add(sectorLabel);
        this.add(sectorPanel);

        // Retrievable list combo and start button
        JPanel retrievablesPanel = new JPanel();
        retrievablesPanel.setLayout(new BoxLayout(retrievablesPanel, BoxLayout.Y_AXIS));
        retrievablesPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

        // RetrievablePanel list
        for (JPanel panel : this.retrievables)
        {
            retrievablesPanel.add(panel);
        }
        this.add(retrievablesPanel);

        // Start button
        JPanel startPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        startPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        startButton = new JButton("Start download");
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                startButtonActionPerformed(event);
            }
        });
        startPanel.add(startButton);
        this.add(startPanel);

        // Download monitor panel
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new BoxLayout(monitorPanel, BoxLayout.Y_AXIS));
        monitorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        //this.add(monitorPanel);

        // Put the monitor panel in a scroll pane.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(monitorPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setPreferredSize(new Dimension(350, 200));
        this.add(scrollPane);
    }

    public static String makeSectorDescription(Sector sector)
    {
        return String.format("S %7.4f\u00B0 W %7.4f\u00B0 N %7.4f\u00B0 E %7.4f\u00B0",
            sector.getMinLatitude().degrees,
            sector.getMinLongitude().degrees,
            sector.getMaxLatitude().degrees,
            sector.getMaxLongitude().degrees);
    }

    public static String makeSizeDescription(long size)
    {
        double sizeInMegaBytes = size / 1024 / 1024;
        if (sizeInMegaBytes < 1024)
            return String.format("%,.1f MB", sizeInMegaBytes);
        else if (sizeInMegaBytes < 1024 * 1024)
            return String.format("%,.1f GB", sizeInMegaBytes / 1024);
        return String.format("%,.1f TB", sizeInMegaBytes / 1024 / 1024);
    }

    public class BulkRetrievablePanel extends JPanel
    {
        protected BulkRetrievable retrievable;
        protected JCheckBox selectCheckBox;
        protected JLabel descriptionLabel;
        protected Thread updateThread;
        protected Sector sector;

        BulkRetrievablePanel(BulkRetrievable retrievable)
        {
            this.retrievable = retrievable;

            this.initComponents();
        }

        protected void initComponents()
        {
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            // Check + name
            this.selectCheckBox = new JCheckBox(this.retrievable.getName());
            this.selectCheckBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (((JCheckBox) e.getSource()).isSelected() && sector != null)
                        updateDescription(sector);
                }
            });
            this.add(this.selectCheckBox, BorderLayout.WEST);
            // Description (size...)
            this.descriptionLabel = new JLabel();
            this.add(this.descriptionLabel, BorderLayout.EAST);
        }

        public void updateDescription(final Sector sector)
        {
            if (this.updateThread != null && this.updateThread.isAlive())
                return;

            this.sector = sector;
            if (!this.selectCheckBox.isSelected())
            {
                doUpdateDescription(null);
                return;
            }

            this.updateThread = new Thread(new Runnable()
            {
                public void run()
                {
                    doUpdateDescription(sector);
                }
            });
            this.updateThread.setDaemon(true);
            this.updateThread.start();
        }

        protected void doUpdateDescription(final Sector sector)
        {
            if (sector != null)
            {
                try
                {
                    long size = retrievable.getEstimatedMissingDataSize(sector, 0, cache);
                    final String formattedSize = BulkDownloadPanel.makeSizeDescription(size);
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            descriptionLabel.setText(formattedSize);
                        }
                    });
                }
                catch (Exception e)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            descriptionLabel.setText("-");
                        }
                    });
                }
            }
            else
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        descriptionLabel.setText("-");
                    }
                });
        }

        public String toString()
        {
            return this.retrievable.getName();
        }
    }

    public class DownloadMonitorPanel extends JPanel
    {
        protected BulkRetrievalThread thread;
        protected Progress progress;
        protected Timer updateTimer;

        protected JLabel descriptionLabel;
        protected JProgressBar progressBar;
        protected JButton cancelButton;

        public DownloadMonitorPanel(BulkRetrievalThread thread)
        {
            this.thread = thread;
            this.progress = thread.getProgress();

            this.initComponents();

            this.updateTimer = new Timer(1000, new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    updateStatus();
                }
            });
            this.updateTimer.start();
        }

        protected void updateStatus()
        {
            // Update description
            String text = thread.getRetrievable().getName();
            text = text.length() > 30 ? text.substring(0, 27) + "..." : text;
            text += " (" + BulkDownloadPanel.makeSizeDescription(this.progress.getCurrentSize())
                + " / " + BulkDownloadPanel.makeSizeDescription(this.progress.getTotalSize())
                + ")";
            this.descriptionLabel.setText(text);
            // Update progress bar
            int percent = 0;
            if (this.progress.getTotalCount() > 0)
                percent = (int) ((float) this.progress.getCurrentCount() / this.progress.getTotalCount() * 100f);
            this.progressBar.setValue(Math.min(percent, 100));
            // Update tooltip
            String tooltip = BulkDownloadPanel.makeSectorDescription(this.thread.getSector());
            this.descriptionLabel.setToolTipText(tooltip);
            this.progressBar.setToolTipText(makeProgressDescription());

            // Check for end of thread
            if (!this.thread.isAlive())
            {
                // Thread is done
                this.cancelButton.setText("Remove");
                this.cancelButton.setBackground(Color.GREEN);
                this.updateTimer.stop();
            }
        }

        @SuppressWarnings( {"UnusedDeclaration"})
        protected void cancelButtonActionPerformed(ActionEvent event)
        {
            if (this.thread.isAlive())
            {
                // Cancel thread
                this.thread.interrupt();
                this.cancelButton.setBackground(Color.ORANGE);
                this.cancelButton.setText("Remove");
                this.updateTimer.stop();
            }
            else
            {
                // Remove from monitor panel
                Container top = this.getTopLevelAncestor();
                this.getParent().remove(this);
                top.validate();
            }
        }

        protected void initComponents()
        {
            int border = 2;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // Description label
            JPanel descriptionPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            String text = thread.getRetrievable().getName();
            text = text.length() > 40 ? text.substring(0, 37) + "..." : text;
            descriptionLabel = new JLabel(text);
            descriptionPanel.add(descriptionLabel);
            this.add(descriptionPanel);

            // Progrees and cancel button
            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(100, 16));
            progressPanel.add(progressBar);
            progressPanel.add(Box.createHorizontalStrut(8));
            cancelButton = new JButton("Cancel");
            cancelButton.setBackground(Color.RED);
            cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    cancelButtonActionPerformed(event);
                }
            });
            progressPanel.add(cancelButton);
            this.add(progressPanel);
        }

        protected String makeProgressDescription()
        {
            String text = "";
            if (this.progress.getTotalCount() > 0)
            {
                int percent = (int) ((double) this.progress.getCurrentCount() / this.progress.getTotalCount() * 100d);
                text = percent + "% of ";
                text += makeSizeDescription(this.progress.getTotalSize());
            }
            return text;
        }
    }
}

