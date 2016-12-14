/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.geojson.GeoJSONPoint;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.*;

import com.jogamp.opengl.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Using the EarthFlat and FlatOrbitView to display USGS latest earthquakes rss feed.
 *
 * @author Patrick Murris
 * @version $Id: FlatWorldEarthquakes.java 2219 2014-08-11 21:39:44Z dcollins $
 */
public class FlatWorldEarthquakes extends ApplicationTemplate
{
    // See the USGS GeoJSON feed documentation for information on this earthquake data feed:
    // https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
    protected static final String USGS_EARTHQUAKE_FEED_URL
        = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.geojson";
    protected static final String USGS_EARTHQUAKE_MAGNITUDE = "mag";
    protected static final String USGS_EARTHQUAKE_PLACE = "place";
    protected static final String USGS_EARTHQUAKE_TIME = "time";
    protected static final long UPDATE_INTERVAL = 300000; // 5 minutes
    protected static final long MILLISECONDS_PER_MINUTE = 60000;
    protected static final long MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
    protected static final long MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;

    @SuppressWarnings("unchecked")
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private RenderableLayer eqLayer;
        private EqAnnotation mouseEq, latestEq;
        private GlobeAnnotation tooltipAnnotation;
        private JButton downloadButton;
        private JLabel statusLabel, latestLabel;
        private Blinker blinker;
        private Timer updater;
        private long updateTime;
        private JComboBox magnitudeCombo;

        public AppFrame()
        {
            super(true, true, false);

            // Init tooltip annotation
            this.tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
            Font font = Font.decode("Arial-Plain-16");
            this.tooltipAnnotation.getAttributes().setFont(font);
            this.tooltipAnnotation.getAttributes().setSize(new Dimension(400, 0));
            this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
            this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
            this.tooltipAnnotation.getAttributes().setVisible(false);
            this.tooltipAnnotation.setPickEnabled(false);
            this.tooltipAnnotation.setAlwaysOnTop(true);

            // Add control panels
            JPanel controls = new JPanel();
            controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
            // Add earthquakes view control panel
            controls.add(makeEarthquakesPanel());

            // Add select listener for earthquake picking
            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                        highlight(event.getTopObject());
                }
            });

            // Add click-and-go select listener for earthquakes
            this.getWwd().addSelectListener(new ClickAndGoSelectListener(
                this.getWwd(), EqAnnotation.class, 1000e3));

            // Add updater timer
            this.updater = new Timer(1000, new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    long now = System.currentTimeMillis();
                    long elapsed = now - updateTime;
                    if (elapsed >= UPDATE_INTERVAL)
                    {
                        updateTime = now;
                        downloadButton.setText("Update");
                        startEarthquakeDownload();
                    }
                    else
                    {
                        // Display remaining time in button text
                        long remaining = UPDATE_INTERVAL - elapsed;
                        int min = (int) Math.floor((double) remaining / MILLISECONDS_PER_MINUTE);
                        int sec = (int) ((remaining - min * MILLISECONDS_PER_MINUTE) / 1000);
                        downloadButton.setText(String.format("Update (in %1$02d:%2$02d)", min, sec));
                    }
                }
            });
            this.updater.start();
        }

        private void highlight(Object o)
        {
            if (this.mouseEq == o)
                return; // same thing selected

            if (this.mouseEq != null)
            {
                this.mouseEq.getAttributes().setHighlighted(false);
                this.mouseEq = null;
                this.tooltipAnnotation.getAttributes().setVisible(false);
            }

            if (o != null && o instanceof EqAnnotation)
            {
                this.mouseEq = (EqAnnotation) o;
                this.mouseEq.getAttributes().setHighlighted(true);
                this.tooltipAnnotation.setText(this.composeEarthquakeText(this.mouseEq));
                this.tooltipAnnotation.setPosition(this.mouseEq.getPosition());
                this.tooltipAnnotation.getAttributes().setVisible(true);
                this.getWwd().redraw();
            }
        }

        private void setBlinker(EqAnnotation ea)
        {
            if (this.blinker != null)
            {
                this.blinker.stop();
                this.getWwd().redraw();
            }

            if (ea == null)
                return;

            this.blinker = new Blinker(ea);
        }

        private void setLatestLabel(EqAnnotation ea)
        {
            if (ea != null)
            {
                this.latestLabel.setText(this.composeEarthquakeText(ea));
            }
            else
            {
                this.latestLabel.setText("");
            }
        }

        private String composeEarthquakeText(EqAnnotation eqAnnotation)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");

            Number magnitude = (Number) eqAnnotation.getValue(USGS_EARTHQUAKE_MAGNITUDE);
            String place = (String) eqAnnotation.getValue(USGS_EARTHQUAKE_PLACE);
            if (magnitude != null || !WWUtil.isEmpty(place))
            {
                sb.append("<b>");

                if (magnitude != null)
                    sb.append("M ").append(magnitude).append(" - ");

                if (place != null)
                    sb.append(place);

                sb.append("</b>");
                sb.append("<br/>");
            }

            Number time = (Number) eqAnnotation.getValue(USGS_EARTHQUAKE_TIME);
            if (time != null)
            {
                long elapsed = this.updateTime - time.longValue();
                sb.append(this.timePassedToString(elapsed));
                sb.append("<br/>");
            }

            sb.append(String.format("%.2f", eqAnnotation.getPosition().elevation)).append(" km deep");

            sb.append("</html>");

            return sb.toString();
        }

        protected String timePassedToString(long duration)
        {
            if (duration > MILLISECONDS_PER_DAY)
            {
                long days = duration / MILLISECONDS_PER_DAY;
                return days + (days > 1 ? " days ago" : " day ago");
            }
            else if (duration > MILLISECONDS_PER_HOUR)
            {
                long hours = duration / MILLISECONDS_PER_HOUR;
                return hours + (hours > 1 ? " hours ago" : " hour ago");
            }
            else if (duration > MILLISECONDS_PER_MINUTE)
            {
                long minutes = duration / MILLISECONDS_PER_MINUTE;
                return minutes + (minutes > 1 ? " minutes ago" : " minute ago");
            }
            else
            {
                return "moments ago";
            }
        }

        private JPanel makeEarthquakesPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

            // Zoom on latest button
            JPanel zoomPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            zoomPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JButton btZoom = new JButton("Zoom on latest");
            btZoom.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if (latestEq != null)
                    {
                        Position targetPos = latestEq.getPosition();
                        BasicOrbitView view = (BasicOrbitView) getWwd().getView();
                        view.addPanToAnimator(
                            // The elevation component of 'targetPos' here is not the surface elevation,
                            // so we ignore it when specifying the view center position.
                            new Position(targetPos, 0),
                            Angle.ZERO, Angle.ZERO, 1000e3);
                    }
                }
            });
            zoomPanel.add(btZoom);
            controlPanel.add(zoomPanel);

            // View reset button
            JPanel viewPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            viewPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JButton btReset = new JButton("Reset Global View");
            btReset.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Double lat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
                    Double lon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
                    Double elevation = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
                    Position targetPos = Position.fromDegrees(lat, lon, 0);
                    BasicOrbitView view = (BasicOrbitView) getWwd().getView();
                    view.addPanToAnimator(
                        // The elevation component of 'targetPos' here is not the surface elevation,
                        // so we ignore it when specifying the view center position.
                        new Position(targetPos, 0),
                        Angle.ZERO, Angle.ZERO, elevation);
                }
            });
            viewPanel.add(btReset);
            controlPanel.add(viewPanel);

            // Update button
            JPanel downloadPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            downloadPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            this.downloadButton = new JButton("Update");
            this.downloadButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    startEarthquakeDownload();
                }
            });
            this.downloadButton.setEnabled(false);
            downloadPanel.add(this.downloadButton);
            controlPanel.add(downloadPanel);

            // Status label
            JPanel statusPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            statusPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            this.statusLabel = new JLabel();
            this.statusLabel.setPreferredSize(new Dimension(200, 20));
            this.statusLabel.setVerticalAlignment(SwingConstants.CENTER);
            statusPanel.add(this.statusLabel);
            controlPanel.add(statusPanel);

            // Magnitude filter combo
            JPanel magnitudePanel = new JPanel(new GridLayout(0, 2, 0, 0));
            magnitudePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            magnitudePanel.add(new JLabel("Min Magnitude:"));
            magnitudeCombo = new JComboBox(new String[] {"2.5", "3", "4", "5", "6", "7"});
            magnitudeCombo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    applyMagnitudeFilter(Double.parseDouble((String) magnitudeCombo.getSelectedItem()));
                }
            });
            magnitudePanel.add(magnitudeCombo);
            controlPanel.add(magnitudePanel);

            // Blink latest checkbox
            JPanel blinkPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            blinkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            blinkPanel.add(new JLabel("Latest:"));
            final JCheckBox jcb = new JCheckBox("Animate");
            jcb.setSelected(true);
            jcb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if (jcb.isSelected())
                    {
                        setBlinker(latestEq);
                    }
                    else
                    {
                        setBlinker(null);
                    }
                }
            });
            blinkPanel.add(jcb);
            controlPanel.add(blinkPanel);

            // Latest label
            JPanel latestPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            latestPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            this.latestLabel = new JLabel();
            this.latestLabel.setPreferredSize(new Dimension(200, 60));
            this.latestLabel.setVerticalAlignment(SwingConstants.TOP);
            latestPanel.add(this.latestLabel);
            controlPanel.add(latestPanel);

            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Earthquakes")));
            controlPanel.setToolTipText("Earthquakes controls.");
            return controlPanel;
        }

        // Earthquake layer ------------------------------------------------------------------

        private void startEarthquakeDownload()
        {
            WorldWind.getScheduledTaskService().addTask(new Runnable()
            {
                public void run()
                {
                    downloadEarthquakes(USGS_EARTHQUAKE_FEED_URL);
                }
            });
        }

        private void downloadEarthquakes(String earthquakeFeedUrl)
        {
            // Disable download button and update status label
            if (this.downloadButton != null)
                this.downloadButton.setEnabled(false);
            if (this.statusLabel != null)
                this.statusLabel.setText("Updating earthquakes...");

            RenderableLayer newLayer = (RenderableLayer) buildEarthquakeLayer(earthquakeFeedUrl);
            if (newLayer.getNumRenderables() > 0)
            {
                LayerList layers = this.getWwd().getModel().getLayers();
                if (this.eqLayer != null)
                    layers.remove(this.eqLayer);
                this.eqLayer = newLayer;
                this.eqLayer.addRenderable(this.tooltipAnnotation);
                insertBeforePlacenames(this.getWwd(), this.eqLayer);
                this.applyMagnitudeFilter(Double.parseDouble((String) magnitudeCombo.getSelectedItem()));

                if (this.statusLabel != null)
                    this.statusLabel.setText("Updated " + new SimpleDateFormat("EEE h:mm aa").format(new Date())); // now
            }
            else
            {
                if (this.statusLabel != null)
                    this.statusLabel.setText("No earthquakes");
            }

            if (this.downloadButton != null)
                this.downloadButton.setEnabled(true);
        }

        private Layer buildEarthquakeLayer(String earthquakeFeedUrl)
        {
            GeoJSONLoader loader = new GeoJSONLoader()
            {
                @Override
                protected void addRenderableForPoint(GeoJSONPoint geom, RenderableLayer layer, AVList properties)
                {
                    try
                    {
                        addEarthquake(geom, layer, properties);
                    }
                    catch (Exception e)
                    {
                        Logging.logger().log(Level.WARNING, "Exception adding earthquake", e);
                    }
                }
            };

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Earthquakes");
            loader.addSourceGeometryToLayer(earthquakeFeedUrl, layer);

            return layer;
        }

        private AnnotationAttributes eqAttributes;
        private Color eqColors[] =
            {
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.GRAY,
                Color.BLACK,
            };

        private void addEarthquake(GeoJSONPoint geom, RenderableLayer layer, AVList properties)
        {
            if (eqAttributes == null)
            {
                // Init default attributes for all eq
                eqAttributes = new AnnotationAttributes();
                eqAttributes.setLeader(AVKey.SHAPE_NONE);
                eqAttributes.setDrawOffset(new Point(0, -16));
                eqAttributes.setSize(new Dimension(32, 32));
                eqAttributes.setBorderWidth(0);
                eqAttributes.setCornerRadius(0);
                eqAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
            }

            EqAnnotation eq = new EqAnnotation(geom.getPosition(), eqAttributes);
            eq.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // GeoJON point's 3rd coordinate indicates depth
            eq.setValues(properties);

            Number eqMagnitude = (Number) eq.getValue(USGS_EARTHQUAKE_MAGNITUDE);
            Number eqTime = (Number) eq.getValue(USGS_EARTHQUAKE_TIME);

            int elapsedDays = 6;
            if (eqTime != null)
            {
                // Compute days elapsed since earthquake event
                elapsedDays = (int) ((this.updateTime - eqTime.longValue()) / MILLISECONDS_PER_DAY);

                // Update latest earthquake event
                if (this.latestEq != null)
                {
                    Number latestEqTime = (Number) this.latestEq.getValue(USGS_EARTHQUAKE_TIME);
                    if (latestEqTime.longValue() < eqTime.longValue())
                        this.latestEq = eq;
                }
                else
                {
                    this.latestEq = eq;
                }
            }

            eq.getAttributes().setTextColor(eqColors[Math.min(elapsedDays, eqColors.length - 1)]);
            eq.getAttributes().setScale(eqMagnitude.doubleValue() / 10);
            layer.addRenderable(eq);
        }

        private void applyMagnitudeFilter(double minMagnitude)
        {
            this.latestEq = null;
            setBlinker(null);
            setLatestLabel(null);

            Iterable<Renderable> renderables = eqLayer.getRenderables();
            for (Renderable r : renderables)
            {
                if (r instanceof EqAnnotation)
                {
                    EqAnnotation eq = (EqAnnotation) r;
                    Number eqMagnitude = (Number) eq.getValue(USGS_EARTHQUAKE_MAGNITUDE);
                    Number eqTime = (Number) eq.getValue(USGS_EARTHQUAKE_TIME);

                    boolean meetsMagnitudeCriteria = eqMagnitude.doubleValue() >= minMagnitude;
                    eq.getAttributes().setVisible(meetsMagnitudeCriteria);
                    if (meetsMagnitudeCriteria)
                    {
                        if (this.latestEq != null)
                        {
                            Number latestEqTime = (Number) this.latestEq.getValue(USGS_EARTHQUAKE_TIME);
                            if (latestEqTime != null && eqTime != null && latestEqTime.longValue() < eqTime.longValue())
                                this.latestEq = eq;
                        }
                        else
                        {
                            this.latestEq = eq;
                        }
                    }
                }
            }
            setBlinker(this.latestEq);
            setLatestLabel(this.latestEq);
            this.getWwd().redraw();
        }

        private class EqAnnotation extends GlobeAnnotation
        {
            public EqAnnotation(Position position, AnnotationAttributes defaults)
            {
                super("", position, defaults);
            }

            protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
            {
                double finalScale = scale * this.computeScale(dc);

                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glTranslated(x, y, 0);
                gl.glScaled(finalScale, finalScale, 1);
            }

            // Override annotation drawing for a simple circle
            private DoubleBuffer shapeBuffer;

            protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
            {
                // Draw colored circle around screen point - use annotation's text color
                if (dc.isPickingMode())
                {
                    this.bindPickableObject(dc, pickPosition);
                }

                this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);

                // Draw 32x32 shape from its bottom left corner
                int size = 32;
                if (this.shapeBuffer == null)
                    this.shapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_ELLIPSE, size, size, 0, null);
                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glTranslated(-size / 2, -size / 2, 0);
                FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
            }
        }

        private class Blinker
        {
            private EqAnnotation annotation;
            private double initialScale, initialOpacity;
            private int steps = 10;
            private int step = 0;
            private int delay = 100;
            private Timer timer;

            private Blinker(EqAnnotation ea)
            {
                this.annotation = ea;
                this.initialScale = this.annotation.getAttributes().getScale();
                this.initialOpacity = this.annotation.getAttributes().getOpacity();
                this.timer = new Timer(delay, new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        annotation.getAttributes().setScale(initialScale * (1f + 7f * ((float) step / (float) steps)));
                        annotation.getAttributes().setOpacity(initialOpacity * (1f - ((float) step / (float) steps)));
                        step = step == steps ? 0 : step + 1;
                        getWwd().redraw();
                    }
                });
                start();
            }

            private void stop()
            {
                timer.stop();
                step = 0;
                this.annotation.getAttributes().setScale(initialScale);
                this.annotation.getAttributes().setOpacity(initialOpacity);
            }

            private void start()
            {
                timer.start();
            }
        }
    } // End AppFrame

    // --- Main -------------------------------------------------------------------------
    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 50e6);
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        ApplicationTemplate.start("World Wind USGS Earthquakes M 2.5+ - 7 days", AppFrame.class);
    }
}
