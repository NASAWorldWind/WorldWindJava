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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.event.*;
import java.util.*;

/**
 * Highlights a filestore data set by showing its bounding sector.
 *
 * @author tag
 * @version $Id: FileStoreSectorHighlighter.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileStoreSectorHighlighter implements ListSelectionListener, SelectListener
{
    protected static final String SECTOR_LAYER = "SectorLayer";

    protected FileStorePanel fileStorePanel;
    protected WorldWindow wwd;

    public FileStoreSectorHighlighter(WorldWindow wwd, FileStorePanel panel)
    {
        this.wwd = wwd;
        this.fileStorePanel = panel;

        this.wwd.addSelectListener(this); // to get sector pick events
        this.fileStorePanel.addSelectionListener(this); // to get notification of table selection changes
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent)
    {
        // This method is called when the table selection changes.

        if (listSelectionEvent.getValueIsAdjusting())
            return;

        this.handleSelection(this.fileStorePanel.getSelectedDataSets());
    }

    List<FileStoreDataSet> currentlyHighlightedSets = new ArrayList<FileStoreDataSet>();

    protected void handleSelection(List<FileStoreDataSet> selectedDataSets)
    {
        this.unHighlightSelectedSets();

        if (selectedDataSets == null || selectedDataSets.size() == 0)
            return;

        Sector overallSector = this.highlightSelectedSets(selectedDataSets);
        if (overallSector != null)
            ExampleUtil.goTo(this.wwd, overallSector);
        else
            this.wwd.redraw();
    }

    protected void unHighlightSelectedSets()
    {
        for (FileStoreDataSet dataSet : this.currentlyHighlightedSets)
        {
            Layer layer = (Layer) dataSet.getValue(SECTOR_LAYER);
            if (layer != null)
            {
                this.wwd.getModel().getLayers().remove(layer);
            }
        }

        this.currentlyHighlightedSets.clear();
    }

    protected Sector highlightSelectedSets(List<FileStoreDataSet> dataSets)
    {
        Sector overallSector = null;

        for (FileStoreDataSet dataSet : dataSets)
        {
            Layer layer = (Layer) dataSet.getValue(SECTOR_LAYER);
            if (layer == null)
            {
                layer = createSectorLayer(dataSet);
                layer.setValue("FileStoreDataSet", dataSet);
                layer.setValue(AVKey.IGNORE, true);
            }

            this.currentlyHighlightedSets.add(dataSet);
            ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);

            Sector sector = dataSet.getSector();
            if (sector != null)
                overallSector = overallSector == null ? sector : overallSector.union(sector);
        }

        return overallSector;
    }

    protected Layer createSectorLayer(FileStoreDataSet dataSet)
    {
        RenderableLayer layer = new RenderableLayer();
        dataSet.setValue(SECTOR_LAYER, layer);

        this.populateLayer(dataSet, layer);

        return layer;
    }

    protected void populateLayer(FileStoreDataSet dataSet, RenderableLayer layer)
    {
        Sector sector = (Sector) dataSet.getValue(AVKey.SECTOR);
        if (sector == null)
            return;

        BasicShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(dataSet.getColor()));
        attrs.setOutlineWidth(2);

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

    @Override
    public void selected(SelectEvent event)
    {
        // This method is called when the user picks a displayed sector. It ensures that the corresponding data set is
        // visible in the installed-data table.

        if (!event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            return;

        if (!(event.getTopObject() instanceof Path))
            return;

        FileStoreDataSet dataSet = (FileStoreDataSet) event.getTopPickedObject().getParentLayer().getValue
            ("FileStoreDataSet");
        if (dataSet == null)
            return;

        this.fileStorePanel.scrollToDataSet(dataSet);
    }
}
