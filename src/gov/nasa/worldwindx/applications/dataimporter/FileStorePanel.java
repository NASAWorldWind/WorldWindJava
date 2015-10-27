/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Manages the installed-data table and related actions.
 *
 * @author tag
 * @version $Id: FileStorePanel.java 2982 2015-04-06 19:52:46Z tgaskins $
 */
public class FileStorePanel extends JPanel implements ListSelectionListener
{
    protected static long VISIBILITY_UPDATE_INTERVAL = 2000; // milliseconds

    protected WorldWindow wwd;
    protected FileStoreTable fileStoreTable;
    protected FileStoreSectorHighlighter sectorHighlighter;
    protected long previousUpdate; // identifies when the visibility filter was last applied
    protected boolean applyVisibilityFilter = false;

    public FileStorePanel(WorldWindow wwd)
    {
        super(new BorderLayout(5, 5));

        this.wwd = wwd;

        this.fileStoreTable = new FileStoreTable();

        this.setPreferredSize(new Dimension(800, 250));

        JScrollPane scrollPane = new JScrollPane(this.fileStoreTable);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        JButton deleteButton = new JButton(new AbstractAction("Delete Selected Data")
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                deleteSelected();
            }
        });
        buttonPanel.add(deleteButton, BorderLayout.EAST);
        this.add(buttonPanel, BorderLayout.SOUTH);

        final JCheckBox cb = new JCheckBox("Filter by Visibility");
        cb.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                applyVisibilityFilter = cb.isSelected();
                previousUpdate = 0;
                FileStorePanel.this.wwd.redraw(); // necessary to update the dataset visibility flags
            }
        });
        buttonPanel.add(cb, BorderLayout.WEST);

        this.sectorHighlighter = new FileStoreSectorHighlighter(wwd, this);
        this.addSelectionListener(this);

        // Establish a rendering listener that filters the dataset table by its inclusion in the current view.
        this.wwd.addRenderingListener(new RenderingListener()
        {
            @Override
            public void stageChanged(RenderingEvent event)
            {
                if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
                {
                    filterForVisibility();
                }
            }
        });
    }

    /**
     * Finds and displays all data sets within a specified filestore.
     *
     * @param fileStore the filestore for which to display the data sets.
     */
    public void update(final FileStore fileStore)
    {
        final FileStoreDataSetFinder dataSetFinder = new FileStoreDataSetFinder();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                final java.util.List<FileStoreDataSet> dataSets = dataSetFinder.findDataSets(fileStore);

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        fileStoreTable.setDataSets(dataSets);
                        setCursor(Cursor.getDefaultCursor());
                        wwd.redraw(); // causes the dataset visibility flags to update
                    }
                });
            }
        });
        t.start();
    }

    public void addSelectionListener(ListSelectionListener listener)
    {
        // This is a convenience method that simply forwards to the table.

        this.fileStoreTable.getSelectionModel().addListSelectionListener(listener);
    }

    public java.util.List<FileStoreDataSet> getSelectedDataSets()
    {
        // This is a convenience method that simply forwards to the table.

        return this.fileStoreTable.getSelectedDataSets();
    }

    public void scrollToDataSet(FileStoreDataSet dataSet)
    {
        // This method makes the selected file set visible as the top row in the table.
        this.fileStoreTable.scrollToDataSet(dataSet);
    }

    @Override
    public void valueChanged(final ListSelectionEvent event)
    {
        // This method is called when the table selection changes.

        if (event.getValueIsAdjusting())
            return;

        if (event.getFirstIndex() < 0) // a row was deleted
            return;

        // This list is searched below to determine whether a data set is selected.
        final java.util.List<FileStoreDataSet> selectedDataSets = this.fileStoreTable.getSelectedDataSets();

        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // Loop through the selected interval and determine whether the rows were selected or deselected.
                for (int i = event.getFirstIndex(); i <= event.getLastIndex(); i++)
                {
                    int modelRow = fileStoreTable.convertRowIndexToModel(i);
                    FileStoreDataSet dataSet = ((FileStoreTableModel) fileStoreTable.getModel()).getRow(modelRow);

                    if (dataSet.isImagery())
                        manageLayer(dataSet, selectedDataSets.contains(dataSet));
                    else if (dataSet.isElevation())
                        manageElevationModel(dataSet, selectedDataSets.contains(dataSet));
                }

                fileStoreTable.repaint();
            }
        });
        t.start();
    }

    protected void manageLayer(FileStoreDataSet dataSet, boolean tf)
    {
        if (tf)
        {
            this.addToWorldWindow(dataSet);
        }
        else
        {
            Layer existingLayer = DataInstaller.findLayer(this.wwd, dataSet.getName());
            if (existingLayer != null)
                this.wwd.getModel().getLayers().remove(existingLayer);
        }
    }

    protected void manageElevationModel(FileStoreDataSet dataSet, boolean tf)
    {
        if (tf)
        {
            this.addToWorldWindow(dataSet);
        }
        else
        {
            ElevationModel existingElevationModel = DataInstaller.findElevationModel(this.wwd, dataSet.getName());
            if (existingElevationModel != null)
                DataInstaller.removeElevationModel(this.wwd, existingElevationModel);
        }
    }

    protected void addToWorldWindow(FileStoreDataSet dataSet)
    {
        Document doc = null;

        try
        {
            doc = WWXML.openDocument(new File(dataSet.configFilePath));
            doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
        }
        catch (WWRuntimeException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "Exception making data set active", e);
        }

        if (doc == null)
            return;

        DataInstaller.addToWorldWindow(this.wwd, doc.getDocumentElement(), dataSet, true);
    }

    protected void deleteSelected()
    {
        java.util.List<FileStoreDataSet> dataSets = this.fileStoreTable.getSelectedDataSets();

        for (FileStoreDataSet dataSet : dataSets)
        {
            String message = "Delete " + dataSet.getName() + "?" + "\nThis operation cannot be undone.";
            int status = JOptionPane.showConfirmDialog(this, message, "Verify Data Deletion",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (status == 0)
                this.deleteDataSet(dataSet); // "Yes" selected
            else if (status == 1)
                continue; // "No" selected
            else
                return; // "Cancel" selected
        }
    }

    protected void deleteDataSet(FileStoreDataSet dataSet)
    {
        dataSet.delete();
        ((FileStoreTableModel) this.fileStoreTable.getModel()).removeDataSet(dataSet);

        // Notify listeners so that the layer and elevations menus are updated.
        if (dataSet.isImagery())
        {
            Layer layer = DataInstaller.findLayer(this.wwd, dataSet.getName());
            if (layer != null)
                this.wwd.getModel().getLayers().remove(layer);
        }
        else if (dataSet.isElevation())
        {
            ElevationModel elevationModel = DataInstaller.findElevationModel(this.wwd, dataSet.getName());
            if (elevationModel != null)
                DataInstaller.removeElevationModel(this.wwd, elevationModel);
        }
    }

    protected void filterForVisibility()
    {
        // Determine whether each dataset is within the current view.

        long now = System.currentTimeMillis();
        if (previousUpdate + VISIBILITY_UPDATE_INTERVAL > now)
            return;

        FileStoreTableModel model = (FileStoreTableModel) this.fileStoreTable.getModel();
        if (model.getDataSets().size() == 0)
            return;

        Sector visibleSector = this.wwd.getSceneController().getDrawContext().getVisibleSector();
        if (this.applyVisibilityFilter && visibleSector == null)
        {
            for (FileStoreDataSet dataSet : model.getDataSets())
            {
                dataSet.removeKey(FileStoreTable.VISIBLE);
            }
        }
        else
        {
            for (FileStoreDataSet dataSet : model.getDataSets())
            {
                if (!applyVisibilityFilter || visibleSector.contains(dataSet.getSector()))
                    dataSet.setValue(FileStoreTable.VISIBLE, true);
                else
                    dataSet.removeKey(FileStoreTable.VISIBLE);
            }
        }

        this.previousUpdate = now;
        this.fileStoreTable.repaint();
    }
//
//    public void tableChanged(TableModelEvent event)
//    {
//        System.out.println("CHANGE " + event.getColumn());
//        if (event.getColumn() != 1)
//            return;
//
//        FileStoreDataSet dataSet = ((FileStoreTableModel) this.fileStoreTable.getModel()).getRow(event.getFirstRow
// ());
//        Boolean tf = (Boolean) ((TableModel) event.getSource()).getValueAt(event.getFirstRow(), event.getColumn());
//        System.out.printf("%b, DataSet %s\n", tf, dataSet.getName());
//
//        if (dataSet.getDatasetType().equals("Imagery"))
//            this.manageLayer(dataSet, tf);
//    }
//
//    protected void handleSelectionChanged2(ListSelectionEvent event)
//    {
//        if (event.getValueIsAdjusting())
//            return;
//
//        List<FileStoreDataSet> dataSets = this.fileStoreTable.getSelectedDataSets();
//        if (dataSets.size() == 0)
//            this.handleDeSelection(event);
//        else
//            this.handleSelection();
//
//        this.fileStoreTable.repaint();
//    }
//
//    protected void handleSelection()
//    {
//        for (FileStoreDataSet dataSet : this.fileStoreTable.getSelectedDataSets())
//        {
//            if (dataSet.getDatasetType().equals("Imagery"))
//                this.manageLayer(dataSet, true);
//        }
//    }
//
//    protected void handleDeSelection(ListSelectionEvent event)
//    {
//        for (int i = event.getFirstIndex(); i <= event.getLastIndex(); i++)
//        {
//            int modelRow = this.fileStoreTable.convertRowIndexToModel(i);
//            FileStoreDataSet dataSet = ((FileStoreTableModel) this.fileStoreTable.getModel()).getRow(modelRow);
//
//            Layer layer = (Layer) dataSet.getValue(AVKey.LAYER);
//            if (layer != null)
//            {
//                layer.setEnabled(false);
//            }
//        }
//    }
}
