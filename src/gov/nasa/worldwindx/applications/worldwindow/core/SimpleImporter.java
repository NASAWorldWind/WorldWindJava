/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.*;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tag
 * @version $Id: SimpleImporter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SimpleImporter {

    protected static final String DEFAULT_GROUP = "Recently Opened";

    protected static final AtomicInteger nextLayerNumber = new AtomicInteger(0);

    protected Object source;
    protected Controller controller;

    public SimpleImporter(Object source, Controller controller) {
        this.source = source;
        this.controller = controller;
    }

    protected LayerPath getDefaultPathToParent() {
        return new LayerPath(DEFAULT_GROUP);
    }

    protected void addLayer(final Layer layer, final LayerPath pathToParent) {
        SwingUtilities.invokeLater(() -> {
            LayerPath path = new LayerPath(pathToParent != null ? pathToParent : getDefaultPathToParent(),
                    layer.getName());
            doAddLayer(layer, path);
        });
    }

    protected void doAddLayer(final Layer layer, final LayerPath path) {
        LayerManager layerManager = controller.getLayerManager();
        layerManager.addLayer(layer, path.lastButOne());
        layerManager.selectLayer(layer, true);
        layerManager.expandPath(path.lastButOne());
    }

    public String formName(Object source, String defaultName) {
        if (source instanceof File) {
            return ((File) source).getName();
        }

        if (source instanceof URL) {
            return ((URL) source).getPath();
        }

        if (source instanceof URI) {
            return ((URI) source).getPath();
        }

        if (source instanceof String && WWIO.makeURL((String) source) != null) {
            return WWIO.makeURL((String) source).getPath();
        }

        return (defaultName != null ? defaultName : "Layer ") + nextLayerNumber.addAndGet(1);
    }

    public void startImport() {
        if (this.source == null) {
            String message = Logging.getMessage("nullValue.SourceIsNull"); // TODO: show error dialog for all errors
            throw new IllegalStateException(message);
        }

        if (this.isKML(this.source)) {
            this.openKML(this.source);
        } else if (this.isShapfile(this.source)) {
            this.openShapefile(this.source);
        } else {
            String message = Logging.getMessage("generic.UnrecognizedSourceType", source.toString());
            throw new IllegalArgumentException(message);
        }
    }

    protected boolean isKML(Object source) {
        return source != null && (source.toString().endsWith(".kml") || source.toString().endsWith(".kmz"));
    }

    protected void openKML(Object source) {
        KMLController kmlController;

        try {
            KMLRoot kmlRoot = KMLRoot.create(source);
            if (kmlRoot == null) {
                String message = Logging.getMessage("generic.UnrecognizedSourceType", source.toString(),
                        source.toString());
                throw new IllegalArgumentException(message);
            }

            kmlRoot.parse();
            kmlController = new KMLController(kmlRoot);
            final RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(kmlController);
            layer.setName(formName(source, null));
            this.addLayer(layer, null);
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    protected boolean isShapfile(Object source) {
        return source != null && source.toString().endsWith(".shp");
    }

    protected void openShapefile(Object source) {
        var factory = new ShapefileLayerFactory();
        Layer layer = (Layer) factory.createFromShapefileSource(source);
        if (layer != null) {
            layer.setName(formName(source, null));
            this.addLayer(layer, null);
        }
    }
}
