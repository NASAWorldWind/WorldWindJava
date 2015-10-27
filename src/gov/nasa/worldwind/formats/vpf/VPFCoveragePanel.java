/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * VPF Layer selection panel
 *
 * @version $Id: VPFCoveragePanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFCoveragePanel extends JPanel
{
    private WorldWindow wwd;
    private Dimension preferredSize;
    private JTabbedPane libraryTabbedPane;
    private VPFLayer layer;

    private JPanel legendPanel;

    public VPFCoveragePanel(WorldWindow wwd, VPFDatabase db)
    {
        // Make a panel at a default size.
        super(new BorderLayout());
        this.wwd = wwd;
        this.makePanel(db, new Dimension(200, 400));
    }

    public VPFCoveragePanel(VPFDatabase db, Dimension size)
    {
        // Make a panel at a specified size.
        super(new BorderLayout());
        this.makePanel(db, size);
    }

    public VPFLayer getLayer()
    {
        return this.layer;
    }

    public void setLayer(VPFLayer layer)
    {
        this.layer = layer;
        this.fillLegendPanel();
    }

    private void makePanel(VPFDatabase db, Dimension size)
    {
        this.preferredSize = size;
        this.libraryTabbedPane = new JTabbedPane();
        this.add(this.libraryTabbedPane, BorderLayout.CENTER);

        this.fill(db);
        this.startLegendUpdateTimer();
    }

    private void fill(VPFDatabase db)
    {
        this.addDatabase(db);
        this.addLegend();
    }

    public void addDatabase(VPFDatabase db)
    {
        // Sort the library list alphabetically.
        ArrayList<VPFLibrary> sortedList = new ArrayList<VPFLibrary>();
        sortedList.addAll(db.getLibraries());
        this.sortPropertyLists(sortedList, AVKey.DISPLAY_NAME);

        for (VPFLibrary lib : sortedList)
        {
            this.addLibrary(db, lib);
        }
    }

    public void addLibrary(VPFDatabase db, VPFLibrary lib)
    {
        // Make and fill the panel holding the library coverages.
        JPanel selectionPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(selectionPanel, BorderLayout.NORTH);

        // Put the name panel in a scroll bar.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (this.preferredSize != null)
            scrollPane.setPreferredSize(this.preferredSize);

        // Add the scroll bar and name panel to a titled panel that will resize with the main window.
        JPanel westPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        westPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Coverage")));
        westPanel.setToolTipText("Coverage to Show");
        westPanel.add(scrollPane);

        // Add the library enable/disable check box
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(18, 9, 0, 9)); // top, left, bottom, right

        ZoomAction zoomAction = new ZoomAction(lib);
        JButton button = new JButton(zoomAction);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(button);
        box.add(Box.createVerticalStrut(18));

        LibraryAction libraryAction = new LibraryAction(db, lib, false); // default to non selected
        JCheckBox jcb = new JCheckBox(libraryAction);
        jcb.setSelected(libraryAction.selected);
        jcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(jcb);

        // Must put the box in a container to prevent layout from stretching their vertical spacing.
        dummyPanel = new JPanel(new BorderLayout(0, 0)); // hgap, vgap
        dummyPanel.add(box, BorderLayout.NORTH);
        dummyPanel.add(westPanel, BorderLayout.CENTER);

        // Put the coverage titled panel in the library tabbed pane.
        this.libraryTabbedPane.add(lib.getName(), dummyPanel);
        int index = this.libraryTabbedPane.indexOfComponent(dummyPanel);
        this.libraryTabbedPane.setToolTipTextAt(index, lib.getDescription());

        // Sort the coverage list alphabetically.
        ArrayList<VPFCoverage> sortedList = new ArrayList<VPFCoverage>();
        sortedList.addAll(lib.getCoverages());
        this.sortPropertyLists(sortedList, AVKey.DESCRIPTION);

        for (VPFCoverage cov : sortedList)
        {
            if (cov.isReferenceCoverage())
                continue;

            this.addCoverage(db, cov, selectionPanel);
        }
    }

    protected void sortPropertyLists(java.util.List<? extends AVList> propertyList, final String propertyName)
    {
        Collections.sort(propertyList, new Comparator<AVList>()
        {
            public int compare(AVList a, AVList b)
            {
                String aValue = (a.getValue(propertyName) != null) ? a.getValue(propertyName).toString() : "";
                String bValue = (b.getValue(propertyName) != null) ? b.getValue(propertyName).toString() : "";
                return String.CASE_INSENSITIVE_ORDER.compare(aValue, bValue);
            }
        });
    }

    public void addLegend()
    {
        // Make and fill the panel holding the legend items.
        this.legendPanel = new JPanel();
        this.legendPanel.setLayout(new BoxLayout(this.legendPanel, BoxLayout.PAGE_AXIS));
        this.legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Must put the grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.legendPanel, BorderLayout.NORTH);

        // Put the panel in a scroll bar.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (this.preferredSize != null)
            scrollPane.setPreferredSize(this.preferredSize);

        // Add the scroll bar and panel to a titled panel that will resize with the main window.
        JPanel westPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        westPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Legend")));
        westPanel.setToolTipText("VPF Layer Legend");
        westPanel.add(scrollPane);
        westPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(westPanel);

        // Must put the box in a container to prevent layout from stretching their vertical spacing.
        dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(box, BorderLayout.CENTER);

        // Put the titled panel in the library tabbed pane.
        this.libraryTabbedPane.add("Legend", dummyPanel);
    }

    protected void fillLegendPanel()
    {
        this.legendPanel.removeAll();

        if (this.layer == null)
            return;

        Iterable<VPFSymbol> symbols = this.layer.getActiveSymbols();
        if (symbols == null)
            return;

        // Sort rendering attributes, and eliminate duplicate entries.
        Iterable<SymbolInfo> symbolInfo = this.getUniqueSymbols(symbols);

        // Compose legend panel
        VPFLegendSupport legendSupport = new VPFLegendSupport();
        String coverageName = null;
        for (SymbolInfo info : symbolInfo)
        {
            // Insert coverage title
            if (coverageName == null || !info.getFeatureClass().getCoverage().getName().equals(coverageName))
            {
                if (coverageName != null)
                    this.legendPanel.add(Box.createVerticalStrut(5));
                JLabel label = new JLabel(info.getFeatureClass().getCoverage().getDescription());
                this.legendPanel.add(label);
                coverageName = info.getFeatureClass().getCoverage().getName();
                this.legendPanel.add(new JSeparator(JSeparator.HORIZONTAL));
                this.legendPanel.add(Box.createVerticalStrut(5));
            }
            // Add legend item
            String description = info.getDescription() != null ? info.getDescription() : "";
            if (description.length() > 0)
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
            BufferedImage legendImage = legendSupport.createLegendImage(info.getAttributes(), 60, 22, 0);
            if (legendImage != null && description.length() > 0)
            {
                Icon icon = new ImageIcon(legendImage);
                JLabel label = new JLabel(description, icon, SwingConstants.LEFT);
                label.setIconTextGap(8);
                this.legendPanel.add(label);
                this.legendPanel.add(Box.createVerticalStrut(2));
            }
        }

        this.legendPanel.revalidate();
        this.legendPanel.repaint();
    }

    protected Iterable<SymbolInfo> getUniqueSymbols(Iterable<VPFSymbol> iterable)
    {
        // Use a TreeSet to consolidate duplicate symbol attributes and simultaneously sort the attributes.

        Set<SymbolInfo> set = new TreeSet<SymbolInfo>(new Comparator<SymbolInfo>()
        {
            public int compare(SymbolInfo a, SymbolInfo b)
            {
                String aCoverageName = (a.getFeatureClass().getCoverage().getName() != null)
                    ? a.getFeatureClass().getCoverage().getName() : "";
                String bCoverageName = (b.getFeatureClass().getCoverage().getName() != null)
                    ? b.getFeatureClass().getCoverage().getName() : "";

                int i = String.CASE_INSENSITIVE_ORDER.compare(aCoverageName, bCoverageName);
                if (i != 0)
                    return i;

                String aKey = (a.getAttributes().getSymbolKey() != null) ? a.getAttributes().getSymbolKey().toString()
                    : "";
                String bKey = (b.getAttributes().getSymbolKey() != null) ? b.getAttributes().getSymbolKey().toString()
                    : "";

                i = String.CASE_INSENSITIVE_ORDER.compare(aKey, bKey);
                if (i != 0)
                    return i;

                int aType = (a.getFeatureClass().getType() != null) ? a.getFeatureClass().getType().ordinal() : -1;
                int bType = (b.getFeatureClass().getType() != null) ? b.getFeatureClass().getType().ordinal() : -1;

                return (aType < bType) ? -1 : ((aType > bType) ? 1 : 0);
            }
        });

        for (VPFSymbol symbol : iterable)
        {
            if (symbol != null && symbol.getFeature() != null && symbol.getAttributes() != null)
            {
                set.add(new SymbolInfo(symbol.getFeature().getFeatureClass(), symbol.getAttributes()));
            }
        }

        return set;
    }

    protected static class SymbolInfo
    {
        protected VPFFeatureClass featureClass;
        protected VPFSymbolAttributes attributes;

        public SymbolInfo(VPFFeatureClass featureClass, VPFSymbolAttributes attributes)
        {
            this.featureClass = featureClass;
            this.attributes = attributes;
        }

        public VPFFeatureClass getFeatureClass()
        {
            return this.featureClass;
        }

        public VPFSymbolAttributes getAttributes()
        {
            return this.attributes;
        }

        public String getDescription()
        {
            return this.attributes.getDescription();
        }
    }

    protected void addCoverage(VPFDatabase db, VPFCoverage cov, Container parent)
    {
        CoverageAction action = new CoverageAction(db, cov, false); // default to non selected
        JCheckBox jcb = new JCheckBox(action);
        jcb.setSelected(action.selected);
        parent.add(jcb);
    }

    public void update(VPFDatabase db)
    {
        // Refresh the coverage list from the given db
        this.libraryTabbedPane.removeAll();
        this.fill(db);
        this.libraryTabbedPane.revalidate();
        this.libraryTabbedPane.repaint();
    }

    public void clear()
    {
        this.libraryTabbedPane.removeAll();
        this.libraryTabbedPane.revalidate();
        this.libraryTabbedPane.repaint();
    }

    private void startLegendUpdateTimer()
    {
        Timer timer = new Timer(3000, new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                fillLegendPanel();
            }
        });
        timer.start();
    }

    private class LibraryAction extends AbstractAction
    {
        VPFDatabase db;
        VPFLibrary library;
        private boolean selected;

        public LibraryAction(VPFDatabase db, VPFLibrary library, boolean selected)
        {
            super("Show Library");
            this.putValue(Action.SHORT_DESCRIPTION, "Show " + library.getName());
            this.db = db;
            this.library = library;
            this.selected = selected;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Fire property change event on the database
            boolean newState = ((JCheckBox) actionEvent.getSource()).isSelected();
            this.db.firePropertyChange(
                new PropertyChangeEvent(this.library, VPFLayer.LIBRARY_CHANGED, this.selected, newState));
            this.selected = newState;
            wwd.redraw();
        }
    }

    private class CoverageAction extends AbstractAction implements PropertyChangeListener
    {
        VPFDatabase db;
        VPFCoverage coverage;
        private boolean selected;

        public CoverageAction(VPFDatabase db, VPFCoverage coverage, boolean selected)
        {
            super(coverage.getDescription());
            this.putValue(Action.SHORT_DESCRIPTION, "Show " + coverage.getDescription());
            this.db = db;
            this.coverage = coverage;
            this.selected = selected;
            this.db.addPropertyChangeListener(this);
            this.setEnabled(false);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Fire property change event on the database
            boolean newState = ((JCheckBox) actionEvent.getSource()).isSelected();
            this.db.firePropertyChange(
                new PropertyChangeEvent(this.coverage, VPFLayer.COVERAGE_CHANGED, this.selected, newState));
            this.selected = newState;
            wwd.redraw();
        }

        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getPropertyName().equals(VPFLayer.LIBRARY_CHANGED))
            {
                VPFLibrary library = (VPFLibrary) event.getSource();
                boolean enabled = (Boolean) event.getNewValue();

                if (library.getFilePath().equals(this.coverage.getLibrary().getFilePath()))
                {
                    this.setEnabled(enabled);
                }
            }
        }
    }

    private class ZoomAction extends AbstractAction
    {
        private VPFLibrary library;

        private ZoomAction(VPFLibrary library)
        {
            super("Zoom To Library");
            this.library = library;
            this.setEnabled(this.library.getBounds() != null);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            Sector sector = this.library.getBounds().toSector();
            Extent extent = Sector.computeBoundingCylinder(wwd.getModel().getGlobe(),
                wwd.getSceneController().getVerticalExaggeration(), sector);

            Angle fov = wwd.getView().getFieldOfView();
            Position centerPos = new Position(sector.getCentroid(), 0d);
            double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

            wwd.getView().goTo(centerPos, zoom);
        }
    }
}
