/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.beans.*;
import java.util.*;

/**
 * Highlights the coverage area of selected data sets.
 *
 * @author tag
 * @version $Id: FileSetHighlighter.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetHighlighter implements ListSelectionListener, SelectListener, PropertyChangeListener
{
    protected FileSetPanel fileSetPanel;
    protected WorldWindow wwd;

    public FileSetHighlighter(WorldWindow wwd, FileSetPanel panel)
    {
        this.wwd = wwd;
        this.fileSetPanel = panel;

        this.wwd.addSelectListener(this);
        this.fileSetPanel.addSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent)
    {
        // Called when the data-install panel table's selection changes.

        if (listSelectionEvent.getValueIsAdjusting())
            return;

        this.handleSelection(this.fileSetPanel.getSelectedFileSets());
    }

    List<FileSet> currentlyHighlightedSets = new ArrayList<FileSet>();

    protected void handleSelection(List<FileSet> selectedFileSets)
    {
        this.unHighlightSelectedSets();

        if (selectedFileSets == null || selectedFileSets.size() == 0)
            return;

        Sector overallSector = this.highlightSelectedSets(selectedFileSets);
        if (overallSector != null)
        {
            // Sometimes the overall sector goes out of limits, so normalize it if it does.
            if (!overallSector.isWithinLatLonLimits())
                overallSector = normalizeSector(overallSector);

            ExampleUtil.goTo(this.wwd, overallSector);
        }
        else
        {
            this.wwd.redraw();
        }
    }

    protected static Sector normalizeSector(Sector sector)
    {
        return new Sector(sector.getMinLatitude().normalizedLatitude(), sector.getMaxLatitude().normalizedLatitude(),
            sector.getMinLongitude().normalizedLongitude(), sector.getMaxLongitude().normalizedLongitude());
    }

    protected void unHighlightSelectedSets()
    {
        for (FileSet fileSet : this.currentlyHighlightedSets)
        {
            Layer layer = (Layer) fileSet.getValue(AVKey.LAYER);
            if (layer != null)
            {
                this.wwd.getModel().getLayers().remove(layer);
            }
        }

        this.currentlyHighlightedSets.clear();
    }

    protected Sector highlightSelectedSets(List<FileSet> fileSets)
    {
        Sector overallSector = null;

        for (FileSet fileSet : fileSets)
        {
            Layer layer = (Layer) fileSet.getValue(AVKey.LAYER);
            if (layer == null)
            {
                layer = createSectorLayer(fileSet);
                layer.setValue("FileSet", fileSet);
            }

            this.currentlyHighlightedSets.add(fileSet);
            this.wwd.getModel().getLayers().add(layer);

            Sector sector = fileSet.getSector();
            if (sector != null)
                overallSector = overallSector == null ? sector : overallSector.union(sector);
        }

        return overallSector;
    }

    protected Layer createSectorLayer(FileSet fileSet)
    {
        RenderableLayer layer = new RenderableLayer();
        fileSet.setValue(AVKey.LAYER, layer);
        layer.setValue(AVKey.IGNORE, true);
        layer.setValue(DataInstaller.PREVIEW_LAYER, true);

        this.populateLayer(fileSet, layer);

        return layer;
    }

    protected void populateLayer(FileSet fileSet, RenderableLayer layer)
    {
        Object[] sectors = fileSet.getSectorList();

        // Add a Path for each sector in the file set.
        for (int i = 0; i < sectors.length; i++)
        {
            BasicShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(fileSet.getColor()));
            attrs.setOutlineWidth(2);

            Sector sector = (Sector) sectors[i];
            List<LatLon> locations = sector.asList();
            List<Position> positions = new ArrayList<Position>(5);
            for (LatLon location : locations)
            {
                positions.add(new Position(location, 0));
            }
            positions.add(new Position(locations.get(0), 0)); // to form a closed path
            // Work around a bug in Path that does not tessellate the final segment.
            positions.add(new Position(locations.get(0), 0)); // to form a closed path

            Path path = new Path(positions);
            path.setSurfacePath(true);
            path.setAttributes(attrs);

            layer.addRenderable(path);
        }

        // Potentially add the preview image.
        if (fileSet.isImagery() && fileSet.getLength() <= fileSet.getMaxFilesForPreviewImage())
            this.addImage(fileSet);
    }

    @Override
    public void selected(SelectEvent event)
    {
        // Called when the sector is selected in the WorldWindow. Ensures that the selected data set's entry in the
        // data set table is visible.

        if (!event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            return;

        if (!(event.getTopObject() instanceof Path))
            return;

        FileSet fileSet = (FileSet) event.getTopPickedObject().getParentLayer().getValue("FileSet");
        if (fileSet == null)
            return;

        this.fileSetPanel.scrollToFileSet(fileSet);
    }

    public void addImage(final FileSet fileSet)
    {
        BufferedImage image = fileSet.getImage();
        if (image != null)
        {
            fileSet.removePropertyChangeListener(AVKey.IMAGE, this); // notification no longer needed

            // Add a surface image for the preview image to the file set's layer.
            Sector sector = (Sector) fileSet.getValue(AVKey.SECTOR);
            SurfaceImage surfaceImage = new SurfaceImage(image, sector);

            RenderableLayer layer = (RenderableLayer) fileSet.getValue(AVKey.LAYER);
            layer.addRenderable(surfaceImage);

            this.wwd.redraw();
        }
        else
        {
            // Register to be notified when the image is available.
            fileSet.addPropertyChangeListener(AVKey.IMAGE, this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        // Adds a file set's newly created image to the file set's layer.

        if (!event.getPropertyName().equals(AVKey.IMAGE))
            return;

        final FileSet fileSet = (FileSet) event.getSource();

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                addImage(fileSet);
            }
        });
    }
}
