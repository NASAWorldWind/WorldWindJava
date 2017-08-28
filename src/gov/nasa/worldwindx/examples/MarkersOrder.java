/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.gpx.GpxReader;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.util.PowerOfTwoPaddedImage;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.text.*;
import java.util.*;

/**
 * Shows how to control track markers attributes to convey their order in time. The markers can be colored by the day of
 * week that the data was collected, by the hour of day that the data was collected, or by a simple color ramp. Use the
 * controls in the lower left corner of the window to change the ordering mode.
 *
 * @author Patrick Murris
 * @version $Id: MarkersOrder.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class MarkersOrder extends ApplicationTemplate
{
    protected static final String TRACK_PATH = "gov/nasa/worldwindx/examples/data/tuolumne.gpx";
    protected static final double TRACK_LATITUDE = 37.90;
    protected static final double TRACK_LONGITUDE = -119.52;

    @SuppressWarnings("unchecked")
    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected static final int COLOR_MODE_RAMP = 0;
        protected static final int COLOR_MODE_DOW = 1;
        protected static final int COLOR_MODE_HOURS = 2;

        protected static final int RAMP_VALUES = 32;

        // Attributes color ramps
        // Monochrome
        protected static MarkerAttributes[] attrsRampMono = new MarkerAttributes[RAMP_VALUES];

        static
        {
            for (int i = 0; i < RAMP_VALUES; i++)
            {
                float opacity = Math.max(1f - (float) i / RAMP_VALUES, .2f);
                attrsRampMono[i] = new BasicMarkerAttributes(new Material(Color.RED),
                    BasicMarkerShape.SPHERE, opacity, 10, 5);
            }
        }

        // Monochrome desaturated
        protected static MarkerAttributes[] attrsRampDesat = new MarkerAttributes[RAMP_VALUES];

        static
        {
            for (int i = 0; i < RAMP_VALUES; i++)
            {
                float hue = 1f;  // Red
                float sat = 1f - (float) i / (RAMP_VALUES * 1.1f);  // bias to avoid falling to plain white
                float opacity = Math.max(1f - (float) i / RAMP_VALUES, .2f);
                attrsRampDesat[i] = new BasicMarkerAttributes(new Material(Color.getHSBColor(hue, sat, 1f)),
                    BasicMarkerShape.SPHERE, opacity, 10, 5);
            }
        }

        // Two color gradient
        protected static MarkerAttributes[] attrsRampGradient = new MarkerAttributes[RAMP_VALUES];

        static
        {
            for (int i = 0; i < RAMP_VALUES; i++)
            {
                float factor = 1f - (float) i / RAMP_VALUES;
                float opacity = Math.max(1f - (float) i / RAMP_VALUES, .2f);
                attrsRampGradient[i] = new BasicMarkerAttributes(
                    new Material(interpolateColor(Color.RED, Color.BLUE, factor)),
                    BasicMarkerShape.SPHERE, opacity, 10, 5);
            }
        }

        // Rainbow
        protected static MarkerAttributes[] attrsRampHue = new MarkerAttributes[RAMP_VALUES];

        static
        {
            for (int i = 0; i < RAMP_VALUES; i++)
            {
                float hue = (float) i / (RAMP_VALUES * 1.1f); // Bias to avoid looping back to red
                float opacity = Math.max(1f - (float) i / RAMP_VALUES, .2f);
                attrsRampHue[i] = new BasicMarkerAttributes(new Material(Color.getHSBColor(hue, 1f, 1f)),
                    BasicMarkerShape.SPHERE, opacity, 10, 5);
            }
        }

        // Seven days color set
        protected static MarkerAttributes[] attrsDayOfWeek = new MarkerAttributes[7];

        static
        {
            for (int i = 1; i <= 7; i++)        // Sunday=1... Saturday=7
            {
                attrsDayOfWeek[i - 1] = new BasicMarkerAttributes(
                    new Material(computeColorForDayOfWeek(i)),
                    BasicMarkerShape.SPHERE, 1, 10, 5);
            }
        }

        // 24h color set
        protected static MarkerAttributes[] attrsHours = new MarkerAttributes[24];

        static
        {
            for (int i = 0; i < 24; i++)        // 0...23
            {
                attrsHours[i] = new BasicMarkerAttributes(
                    new Material(computeColorForHour(i)),
                    BasicMarkerShape.SPHERE, 1, 10, 5);
            }
        }

        public static Color interpolateColor(Color from, Color to, double factor)
        {
            return new Color((int) (from.getRed() * factor + to.getRed() * (1 - factor)),
                (int) (from.getGreen() * factor + to.getGreen() * (1 - factor)),
                (int) (from.getBlue() * factor + to.getBlue() * (1 - factor)),
                (int) (from.getAlpha() * factor + to.getAlpha() * (1 - factor)));
        }

        public static Color computeColorForDayOfWeek(int day)
        {
            // Day goes from Sunday=1 to Saturday=7
            // Bias ratio to avoid looping back to red on saturday
            return Color.getHSBColor((float) (day - 1) / 6.5f, 1f, 1f);
        }

        public static Color computeColorForHour(int hour)
        {
            // Hour from 0 to 23
            // Bias ratio to avoid looping back to red for 23:00
            return Color.getHSBColor((float) hour / 26f, 1f, 1f);
        }

        protected RenderableLayer renderableLayer;
        protected ScreenAnnotation screenAnnotation;
        protected JComboBox colorRampCombo;
        protected JSlider timeScaleSlider;
        protected Marker lastHighlit;
        protected BasicMarkerAttributes lastAttrs;

        protected final PowerOfTwoPaddedImage dayOfWeekLegend = createLegendForDaysOfWeek(attrsDayOfWeek);
        protected final PowerOfTwoPaddedImage hoursLegend = createLegendForHours(attrsHours);

        protected int colorMode = COLOR_MODE_DOW;  // default color mode is day of week
        protected MarkerAttributes[] attrs = attrsDayOfWeek;

        public AppFrame()
        {
            super(true, true, false);

            // Add marker layer
            final TimedMarkerLayer layer = buildTracksLayer();
            insertBeforePlacenames(this.getWwd(), layer);

            // Add renderable layer for legend
            this.renderableLayer = new RenderableLayer();
            this.renderableLayer.setName("Markers Legend");
            this.renderableLayer.setPickEnabled(false);
            updateScreenAnnotation(this.dayOfWeekLegend);
            insertBeforePlacenames(getWwd(), this.renderableLayer);

            // Add UI to control the layer color ramp type and scale
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    new TitledBorder("Markers Color")));

            // Color mode radio buttons
            JPanel radioPanel = new JPanel(new GridLayout(0, 4, 0, 0));
            radioPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            radioPanel.add(new JLabel("Mode:"));
            final ButtonGroup group = new ButtonGroup();
            JRadioButton btRamp = new JRadioButton("Ramp");
            btRamp.setSelected(false);
            btRamp.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    colorMode = COLOR_MODE_RAMP;
                    colorRampCombo.setEnabled(true);
                    timeScaleSlider.setEnabled(true);
                    switch (colorRampCombo.getSelectedIndex())
                    {
                        case 0:
                            attrs = attrsRampMono;
                            break;
                        case 1:
                            attrs = attrsRampDesat;
                            break;
                        case 2:
                            attrs = attrsRampGradient;
                            break;
                        case 3:
                            attrs = attrsRampHue;
                            break;
                    }
                    updateScreenAnnotation(null);
                    getWwd().redraw();
                }
            });
            group.add(btRamp);
            radioPanel.add(btRamp);
            JRadioButton btDow = new JRadioButton("Days");
            btDow.setSelected(true);
            btDow.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    colorMode = COLOR_MODE_DOW;
                    colorRampCombo.setEnabled(false);
                    timeScaleSlider.setEnabled(false);
                    attrs = attrsDayOfWeek;
                    updateScreenAnnotation(dayOfWeekLegend);
                    getWwd().redraw();
                }
            });
            group.add(btDow);
            radioPanel.add(btDow);
            JRadioButton btHours = new JRadioButton("Hours");
            btHours.setSelected(false);
            btHours.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    colorMode = COLOR_MODE_HOURS;
                    colorRampCombo.setEnabled(false);
                    timeScaleSlider.setEnabled(false);
                    attrs = attrsHours;
                    updateScreenAnnotation(hoursLegend);
                    getWwd().redraw();
                }
            });
            group.add(btHours);
            radioPanel.add(btHours);
            controlPanel.add(radioPanel);

            // Time scale slider
            JPanel sliderPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            sliderPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            timeScaleSlider = new JSlider(0, 120, 10);
            timeScaleSlider.setEnabled(false);
            timeScaleSlider.setToolTipText("Color ramp time scale - Minutes");
            timeScaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    layer.setTimeScale(timeScaleSlider.getValue() * 60 * 1000 + 100);
                    getWwd().redraw();
                }
            });
            timeScaleSlider.setPaintLabels(true);
            timeScaleSlider.setPaintTicks(true);
            timeScaleSlider.setMajorTickSpacing(15);
            sliderPanel.add(timeScaleSlider);
            controlPanel.add(sliderPanel);
            // Color ramp type combo
            JPanel comboPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            comboPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            comboPanel.add(new JLabel("Color scheme:"));
            colorRampCombo = new JComboBox(new String[] {"Monochrome", "Desaturated", "Gradient", "Rainbow"});
            colorRampCombo.setEnabled(false);
            colorRampCombo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    switch (colorRampCombo.getSelectedIndex())
                    {
                        case 0:
                            attrs = attrsRampMono;
                            break;
                        case 1:
                            attrs = attrsRampDesat;
                            break;
                        case 2:
                            attrs = attrsRampGradient;
                            break;
                        case 3:
                            attrs = attrsRampHue;
                            break;
                    }
                    getWwd().redraw();
                }
            });
            comboPanel.add(colorRampCombo);
            controlPanel.add(comboPanel);
            this.getControlPanel().add(controlPanel, BorderLayout.SOUTH);

            // Setup select listener to highlight markers on rollover
            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (lastHighlit != null
                        && (event.getTopObject() == null || !event.getTopObject().equals(lastHighlit)))
                    {
                        lastHighlit.setAttributes(lastAttrs);
                        lastHighlit = null;
                    }

                    if (!event.getEventAction().equals(SelectEvent.ROLLOVER))
                        return;

                    if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() == null)
                        return;

                    if (event.getTopPickedObject().getParentLayer() != layer)
                        return;

                    if (lastHighlit == null && event.getTopObject() instanceof Marker)
                    {
                        lastHighlit = (Marker) event.getTopObject();
                        lastAttrs = (BasicMarkerAttributes) lastHighlit.getAttributes();
                        MarkerAttributes highliteAttrs = new BasicMarkerAttributes(lastAttrs);
                        highliteAttrs.setMaterial(Material.WHITE);
                        highliteAttrs.setOpacity(1d);
                        highliteAttrs.setMarkerPixels(lastAttrs.getMarkerPixels() * 1.4);
                        highliteAttrs.setMinMarkerSize(lastAttrs.getMinMarkerSize() * 1.4);
                        lastHighlit.setAttributes(highliteAttrs);
                    }
                }
            });
        }

        protected TimedMarkerLayer buildTracksLayer()
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readStream(WWIO.openFileOrResourceStream(TRACK_PATH, this.getClass()));
                TrackPointIterator trackPoints = new TrackPointIteratorImpl(reader.getTracks());
                long latestTime = 0;
                ArrayList<Marker> markers = new ArrayList<Marker>();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                df.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                while (trackPoints.hasNext())
                {
                    TrackPoint tp = trackPoints.next();
                    long time = 0;
                    if (tp.getTime() != null)
                    {
                        try
                        {
                            time = df.parse(tp.getTime().replaceAll("[TZ]", " ").trim()).getTime();
                        }
                        catch (Exception e)
                        {
                            time = 0;
                        }
                    }
                    latestTime = time > latestTime ? time : latestTime;
                    markers.add(new TimedMarker(tp.getPosition(), attrs[0], time));
                }

                TimedMarkerLayer layer = new TimedMarkerLayer(markers);
                layer.setLatestTime(latestTime);
                layer.setOverrideMarkerElevation(true);
                layer.setKeepSeparated(true);
                layer.setElevation(0);

                return layer;
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected class TimedMarker extends BasicMarker
        {
            protected long time;

            public TimedMarker(Position position, MarkerAttributes attributes, long time)
            {
                super(position, attributes);
                this.time = time;
            }
        }

        protected class TimedMarkerLayer extends MarkerLayer
        {
            protected long latestTime = 0;
            protected long timeScale = (long) 60e3 * 10;   // 10 minutes between attributes ramp steps

            public TimedMarkerLayer(Iterable<Marker> markers)
            {
                super(markers);
            }

            public void draw(DrawContext dc, java.awt.Point pickPoint)
            {
                if (!dc.isPickingMode())
                {
                    Calendar cal = Calendar.getInstance();
                    for (Marker marker1 : getMarkers())
                    {
                        TimedMarker marker = (TimedMarker) marker1;
                        int i = 0;
                        switch (colorMode)
                        {
                            case COLOR_MODE_RAMP:
                                i = Math.min((int) ((latestTime - marker.time) / timeScale), attrs.length - 1);
                                break;
                            case COLOR_MODE_DOW:
                                cal.setTimeInMillis(marker.time);
                                i = cal.get(Calendar.DAY_OF_WEEK) - 1;
                                break;
                            case COLOR_MODE_HOURS:
                                cal.setTimeInMillis(marker.time);
                                i = cal.get(Calendar.HOUR_OF_DAY) % 24;
                                break;
                        }
                        if (marker != lastHighlit)
                            marker.setAttributes(attrs[i]);
                    }
                }
                super.draw(dc, pickPoint);
            }

            public void setLatestTime(long time)
            {
                this.latestTime = time;
            }

            public long getLatestTime()
            {
                return this.latestTime;
            }

            public void setTimeScale(long time)
            {
                this.timeScale = time;
            }

            public long getTimeScale()
            {
                return this.timeScale;
            }
        }

        protected PowerOfTwoPaddedImage createLegendForHours(MarkerAttributes[] attrs)
        {
            BufferedImage image = new BufferedImage(64, 320, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g2 = image.getGraphics();
            int divisions = 24;
            int margin = 2; // space between items in pixels
            int w = image.getWidth() / 2 - margin;
            int h = (image.getHeight() - margin * (divisions - 1)) / divisions;
            for (int hour = 0; hour < divisions; hour++)
            {
                int x = 0;
                int y = hour * (image.getHeight() / divisions);
                // Draw color rectangle
                g2.setColor(attrs[hour].getMaterial().getDiffuse());
                g2.fillRect(x, y, w, h);
                // Draw hour label
                x = w + margin + margin;
                y = y + h;
                String label = String.format("%02d", hour);
                g2.setColor(Color.BLACK);
                g2.drawString(label, x + 1, y + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(label, x, y);
            }
            return PowerOfTwoPaddedImage.fromBufferedImage(image);
        }

        protected PowerOfTwoPaddedImage createLegendForDaysOfWeek(MarkerAttributes[] attrs)
        {
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayNames = dfs.getShortWeekdays();
            BufferedImage image = new BufferedImage(64, 100, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g2 = image.getGraphics();
            int divisions = 7;
            int margin = 2; // space between items in pixels
            int w = image.getWidth() / 2 - margin;
            int h = (image.getHeight() - margin * (divisions - 1)) / divisions;
            for (int day = 0; day < divisions; day++)
            {
                int x = 0;
                int y = day * (image.getHeight() / divisions);
                // Draw color rectangle
                g2.setColor(attrs[day].getMaterial().getDiffuse());
                g2.fillRect(x, y, w, h);
                // Draw day label
                x = w + margin + margin;
                y = y + h - 1;
                String label = dayNames[day + 1].toUpperCase();
                g2.setColor(Color.BLACK);
                g2.drawString(label, x + 1, y + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(label, x, y);
            }
            return PowerOfTwoPaddedImage.fromBufferedImage(image);
        }

        protected void updateScreenAnnotation(PowerOfTwoPaddedImage image)
        {
            if (this.screenAnnotation != null)
                this.renderableLayer.removeRenderable(this.screenAnnotation);
            if (image != null)
            {
                // Setup annotation in lower left viewport corner
                this.screenAnnotation = new ScreenAnnotation("", new Point(20, 20));
                this.screenAnnotation.getAttributes().setImageSource(image.getPowerOfTwoImage());
                this.screenAnnotation.getAttributes().setSize(
                    new Dimension(image.getOriginalWidth(), image.getOriginalHeight()));
                this.screenAnnotation.getAttributes().setDrawOffset(new Point(image.getOriginalWidth() / 2, 0));
                this.screenAnnotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
                this.screenAnnotation.getAttributes().setBorderWidth(0);
                this.screenAnnotation.getAttributes().setCornerRadius(0);
                this.screenAnnotation.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, 0f));
                this.renderableLayer.addRenderable(this.screenAnnotation);
            }
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, TRACK_LATITUDE);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, TRACK_LONGITUDE);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 40e3);
        ApplicationTemplate.start("WorldWind Markers Order", AppFrame.class);
    }
}
