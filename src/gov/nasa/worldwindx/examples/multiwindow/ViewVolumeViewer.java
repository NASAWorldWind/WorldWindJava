/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwindx.examples.util.ViewVolumeRenderer;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.StatusBar;

import javax.swing.*;
import java.awt.*;

/**
 * This class holds a WorldWindow that displays the view volume and currently visible terrain of another WorldWindow.
 *
 * @author tag
 * @version $Id: ViewVolumeViewer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ViewVolumeViewer extends JFrame
{
    protected WWPanel wwp;
    protected WorldWindowGLCanvas wwd;
    protected WorldWindow observed;

    /**
     * Construct a view volume viewer.
     *
     * @param observedWindow the WorldWindow to observe
     * @param size           the size of the view volume viewer window
     */
    public ViewVolumeViewer(WorldWindow observedWindow, Dimension size)
    {
        this.observed = observedWindow;
        this.getContentPane().setLayout(new BorderLayout(5, 5));

        this.wwp = new WWPanel((WorldWindowGLCanvas) this.observed, size, this.makeModel());
        this.getContentPane().add(wwp, BorderLayout.CENTER);

        this.wwd = wwp.wwd;

        final SectorGeometryLayer sgLayer = new SectorGeometryLayer();
        this.wwd.getModel().getLayers().add(sgLayer);

        final ViewVolumeLayer vvLayer = new ViewVolumeLayer();
        this.wwd.getModel().getLayers().add(0, vvLayer);

        // This view volume viewer updates its display within a rendering listener registered for the observed window
        this.observed.addRenderingListener(new RenderingListener()
        {
            public void stageChanged(RenderingEvent event)
            {
                if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
                {
                    // Get the observed window's sector geometry and update this window's terrain display layer
                    SectorGeometryList sgCopy = new SectorGeometryList(observed.getSceneController().getTerrain());
                    sgLayer.setGeometry(sgCopy);

                    // Get the observed window's view and update this window's view volume display layer
                    vvLayer.setView(observed.getView());

                    // Redraw this (the view volume viewer's) window
                    wwd.redraw();
                }
            }
        });

        this.setTitle("View Volume Viewer");
        this.setResizable(true);
        this.pack();
    }

    // Creates a model for the view volume viewer, selecting appropriate layers from the observed WorldWindow
    protected Model makeModel()
    {
        LayerList layers = new LayerList();

        for (Layer layer : this.observed.getModel().getLayers())
        {
            if (layer instanceof TiledImageLayer) // share TiledImageLayers
                layers.add(layer);
        }

        Model model = new BasicModel();
        model.setGlobe(this.observed.getModel().getGlobe()); // share the globe
        model.setLayers(layers);

        return model;
    }

    public WorldWindow getWwd()
    {
        return this.wwp.wwd;
    }

    public WorldWindow getObserved()
    {
        return this.observed;
    }

    protected static class WWPanel extends JPanel
    {
        protected WorldWindowGLCanvas wwd;

        public WWPanel(WorldWindowGLCanvas shareWith, Dimension size, Model model)
        {
            this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
            this.wwd.setSize(size);
            this.wwd.setModel(model);

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    // A layer to display the terrain of the observed WorldWindow
    protected static class SectorGeometryLayer extends RenderableLayer
    {
        protected SectorGeometryList sg;

        public SectorGeometryLayer()
        {
            this.setPickEnabled(false);
        }

        public void setGeometry(SectorGeometryList sectorGeometry)
        {
            this.sg = sectorGeometry;
        }

        @Override
        protected void doRender(DrawContext dc)
        {
            if (this.sg != null)
            {
                Position currentPosition = this.getCurrentPosition(dc);

                for (SectorGeometry sg : this.sg)
                {
                    sg.renderWireframe(dc, false, true);
                    if (currentPosition != null && sg.getSector().contains(currentPosition))
                    {
                        sg.renderBoundingVolume(dc);
                        sg.renderTileID(dc);
                    }
                }
            }
        }

        public Position getCurrentPosition(DrawContext dc)
        {
            PickedObjectList pos = dc.getPickedObjects();
            PickedObject terrainObject = pos != null ? pos.getTerrainObject() : null;

            return terrainObject != null ? terrainObject.getPosition() : null;
        }
    }

    // A layer to display the view volume of the observed window
    protected static class ViewVolumeLayer extends RenderableLayer
    {
        protected View view;
        protected ViewVolumeRenderer renderer = new ViewVolumeRenderer();

        public ViewVolumeLayer()
        {
            this.setPickEnabled(false);
        }

        public void setView(View view)
        {
            this.view = view;
        }

        @Override
        protected void doRender(DrawContext dc)
        {
            if (this.view != null)
                this.renderer.render(dc, this.view.getModelviewMatrix(), this.view.getProjectionMatrix(),
                    this.view.getViewport());
        }
    }
}
