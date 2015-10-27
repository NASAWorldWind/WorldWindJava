/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.applet;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.*;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;

/**
 * Illustrates the how to display a World Wind <code>{@link WorldWindow}</code> in a Java Applet and interact with the
 * WorldWindow through JavaScript code running in the browser. This class extends <code>{@link JApplet}</code> and
 * embeds a WorldWindowGLCanvas and a StatusBar in the Applet's content pane.
 *
 * @author Patrick Murris
 * @version $Id: WWJApplet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWJApplet extends JApplet
{
    protected WorldWindowGLCanvas wwd;
    protected RenderableLayer labelsLayer;

    public WWJApplet()
    {
    }

    public void init()
    {
        try
        {
            // Check for initial configuration values
            String value = getParameter("InitialLatitude");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.parseDouble(value));
            value = getParameter("InitialLongitude");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.parseDouble(value));
            value = getParameter("InitialAltitude");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double.parseDouble(value));
            value = getParameter("InitialHeading");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_HEADING, Double.parseDouble(value));
            value = getParameter("InitialPitch");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_PITCH, Double.parseDouble(value));

            // Create World Window GL Canvas
            this.wwd = new WorldWindowGLCanvas();
            this.getContentPane().add(this.wwd, BorderLayout.CENTER);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Add a renderable layer for application labels
            this.labelsLayer = new RenderableLayer();
            this.labelsLayer.setName("Labels");
            insertBeforeLayerName(this.wwd, this.labelsLayer, "Compass");

            // Add the status bar
            StatusBar statusBar = new StatusBar();
            this.getContentPane().add(statusBar, BorderLayout.PAGE_END);

            // Forward events to the status bar to provide the cursor position info.
            statusBar.setEventSource(this.wwd);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            // Call javascript appletInit()
            try
            {
                JSObject win = JSObject.getWindow(this);
                win.call("appletInit", null);
            }
            catch (Exception ignore)
            {
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        // Call javascript appletStart()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStart", null);
        }
        catch (Exception ignore)
        {
        }
    }

    public void stop()
    {
        // Call javascript appletSop()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStop", null);
        }
        catch (Exception ignore)
        {
        }

        // Shut down World Wind when the browser stops this Applet.
        WorldWind.shutDown();
    }

    /**
     * Adds a layer to WW current layerlist, before a named layer. Target name can be a part of the layer name
     *
     * @param wwd        the <code>WorldWindow</code> reference.
     * @param layer      the layer to be added.
     * @param targetName the partial layer name to be matched - case sensitive.
     */
    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
    {
        // Insert the layer into the layer list just before the target layer.
        LayerList layers = wwd.getModel().getLayers();
        int targetPosition = layers.size() - 1;
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    // ============== Public API - Javascript ======================= //

    /**
     * Move the current view position
     *
     * @param lat the target latitude in decimal degrees
     * @param lon the target longitude in decimal degrees
     */
    public void gotoLatLon(double lat, double lon)
    {
        this.gotoLatLon(lat, lon, Double.NaN, 0, 0);
    }

    /**
     * Move the current view position, zoom, heading and pitch
     *
     * @param lat     the target latitude in decimal degrees
     * @param lon     the target longitude in decimal degrees
     * @param zoom    the target eye distance in meters
     * @param heading the target heading in decimal degrees
     * @param pitch   the target pitch in decimal degrees
     */
    public void gotoLatLon(double lat, double lon, double zoom, double heading, double pitch)
    {
        BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
        if (!Double.isNaN(lat) || !Double.isNaN(lon) || !Double.isNaN(zoom))
        {
            lat = Double.isNaN(lat) ? view.getCenterPosition().getLatitude().degrees : lat;
            lon = Double.isNaN(lon) ? view.getCenterPosition().getLongitude().degrees : lon;
            zoom = Double.isNaN(zoom) ? view.getZoom() : zoom;
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;
            view.addPanToAnimator(Position.fromDegrees(lat, lon, 0),
                Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom, true);
        }
    }

    /**
     * Set the current view heading and pitch
     *
     * @param heading the target heading in decimal degrees
     * @param pitch   the target pitch in decimal degrees
     */
    public void setHeadingAndPitch(double heading, double pitch)
    {
        BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
        if (!Double.isNaN(heading) || !Double.isNaN(pitch))
        {
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;

            view.addHeadingPitchAnimator(
                view.getHeading(), Angle.fromDegrees(heading), view.getPitch(), Angle.fromDegrees(pitch));
        }
    }

    /**
     * Set the current view zoom
     *
     * @param zoom the target eye distance in meters
     */
    public void setZoom(double zoom)
    {
        BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
        if (!Double.isNaN(zoom))
        {
            view.addZoomAnimator(view.getZoom(), zoom);
        }
    }

    /**
     * Get the WorldWindowGLCanvas
     *
     * @return the current WorldWindowGLCanvas
     */
    public WorldWindowGLCanvas getWW()
    {
        return this.wwd;
    }

    /**
     * Get the current OrbitView
     *
     * @return the current OrbitView
     */
    public OrbitView getOrbitView()
    {
        if (this.wwd.getView() instanceof OrbitView)
            return (OrbitView) this.wwd.getView();
        return null;
    }

    /**
     * Get a reference to a layer with part of its name
     *
     * @param layerName part of the layer name to match.
     *
     * @return the corresponding layer or null if not found.
     */
    public Layer getLayerByName(String layerName)
    {
        for (Layer layer : wwd.getModel().getLayers())
        {
            if (layer.getName().indexOf(layerName) != -1)
                return layer;
        }
        return null;
    }

    /**
     * Add a text label at a position on the globe.
     *
     * @param text  the text to be displayed.
     * @param lat   the latitude in decimal degrees.
     * @param lon   the longitude in decimal degrees.
     * @param font  a string describing the font to be used.
     * @param color the color to be used as an hexadecimal coded string.
     */
    public void addLabel(String text, double lat, double lon, String font, String color)
    {
        GlobeAnnotation ga = new GlobeAnnotation(text, Position.fromDegrees(lat, lon, 0),
            Font.decode(font), Color.decode(color));
        ga.getAttributes().setBackgroundColor(Color.BLACK);
        ga.getAttributes().setDrawOffset(new Point(0, 0));
        ga.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
        ga.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        ga.getAttributes().setTextAlign(AVKey.CENTER);
        this.labelsLayer.addRenderable(ga);
    }
}
