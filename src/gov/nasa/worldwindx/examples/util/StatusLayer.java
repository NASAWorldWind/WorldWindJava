/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.*;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jparsons
 * @version $Id: StatusLayer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */

/**
 * Renders statusbar information as a layer.
 * <p/>
 * Used ScalebarLayer and StatusBar as template
 */
//TODO
//  3. move some methods duplicated in statusbar to a utility class
//  6. add ability to put status text on top of window
public class StatusLayer extends AbstractLayer implements PositionListener, RenderingListener
{
    public final static String UNIT_METRIC = "gov.nasa.worldwind.StatusLayer.Metric";
    public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.StatusLayer.Imperial";

    private String iconFilePath_bg = "images/dot-clockwise-32.png";
    private Color color = Color.white;                //text color
    private Font defaultFont = Font.decode("Arial-BOLD-12");
    protected WorldWindow eventSource;
    protected String latDisplay = "";
    protected String lonDisplay = "";
    protected String elevDisplay = "";
    protected String altDisplay = "";
    private String noNetwork = "";
    private String elevationUnit = UNIT_METRIC;
    private boolean showNetworkStatus = true;
    private AtomicBoolean isNetworkAvailable = new AtomicBoolean(true);
    private boolean activatedDownload = false;
    private int bgWidth;
    private int bgHeight;
    private double iconScale = .5d; //adjust icon size
    private Texture iconTexture;
    private double rotated = 0.0d;
    private Color backColor = new Color(0f, 0f, 0f, 0.4f);
    protected int coordDecimalPlaces = 4;
    static int rotationIncrement = 60;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    private OrderedIcon orderedImage = new OrderedIcon();

    private class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            StatusLayer.this.draw(dc);
        }

        public void render(DrawContext dc)
        {
            StatusLayer.this.draw(dc);
        }
    }

    public StatusLayer()
    {
        setPickEnabled(false);

        Timer downloadTimer = new Timer(300, new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent actionEvent)
            {
                if (!showNetworkStatus)
                {
                    activatedDownload = false;
                    noNetwork = "";
                    return;
                }

                if (!isNetworkAvailable.get())
                {
                    noNetwork = Logging.getMessage("term.NoNetwork");
                    return;
                }
                else
                    noNetwork = "";

                if (isNetworkAvailable.get() && WorldWind.getRetrievalService().hasActiveTasks())
                {
                    activatedDownload = true;
                    bumpRotation();
                    if (eventSource != null)
                        eventSource.redraw();  //smooth graphic
                }
                else
                {
                    if (activatedDownload && (eventSource != null))
                    {
                        eventSource.redraw();  //force a redraw to clear downloading graphic
                    }
                    activatedDownload = false;
                }
            }
        });
        downloadTimer.start();

        Timer netCheckTimer = new Timer(10000, new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent actionEvent)
            {
                if (!showNetworkStatus)
                    return;

                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        isNetworkAvailable.set(!WorldWind.getNetworkStatus().isNetworkUnavailable());
                    }
                });
                t.start();
            }
        });
        netCheckTimer.start();
    }

    public void setElevationUnits(String units)
    {
        elevationUnit = units;
    }

    public Font getDefaultFont()
    {
        return defaultFont;
    }

    public void setDefaultFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.defaultFont = font;
    }

    public int getCoordSigDigits()
    {
        return coordDecimalPlaces;
    }

    public void setCoordDecimalPlaces(int coordDecimalPlaces)
    {
        this.coordDecimalPlaces = coordDecimalPlaces;
    }

    public Color getBackColor()
    {
        return backColor;
    }

    public void setBackColor(Color backColor)
    {
        if (backColor == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.backColor = backColor;
    }

    protected WorldWindow getEventSource()
    {
        return eventSource;
    }

    private double getScaledBGWidth()
    {
        return this.bgWidth * this.iconScale;
    }

    private double getScaledBGHeight()
    {
        return this.bgHeight * this.iconScale;
    }

    // Rendering
    @Override
    public void doRender(DrawContext dc)
    {
        dc.addOrderedRenderable(this.orderedImage);
    }

    @Override
    public void doPick(DrawContext dc, Point pickPoint)
    {
        // Delegate drawing to the ordered renderable list
        dc.addOrderedRenderable(this.orderedImage);
    }

    public void setEventSource(WorldWindow newEventSource)
    {
        if (newEventSource == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.eventSource != null)
        {
            this.eventSource.removePositionListener(this);
            this.eventSource.removeRenderingListener(this);
        }

        newEventSource.addPositionListener(this);
        newEventSource.addRenderingListener(this);

        this.eventSource = newEventSource;
    }

    public void moved(PositionEvent event)
    {
        this.handleCursorPositionChange(event);
    }

    // Rendering
    public void draw(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;
        try
        {
            gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_TRANSFORM_BIT
                | GL2.GL_VIEWPORT_BIT
                | GL2.GL_CURRENT_BIT);
            attribsPushed = true;

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();

            String label = String.format("%s   %s   %s   %s", altDisplay, latDisplay, lonDisplay,
                elevDisplay);
            Dimension size = getTextRenderSize(dc, label);
            if (size.width < viewport.getWidth())   //todo more accurate add size of graphic
            {
                double maxwh = size.width > size.height ? size.width : size.height;
                gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();
                modelviewPushed = true;
                gl.glLoadIdentity();

                int iconHeight = 16;
                if (backColor != null)
                    drawFilledRectangle(dc, new Vec4(0, 0, 0),
                        new Dimension((int) viewport.getWidth(), Math.max((int) size.getHeight(), iconHeight)),
                        this.backColor);
                int verticalSpacing = 2;
                drawLabel(dc, label, new Vec4(1, verticalSpacing, 0), this.color);

                if (noNetwork.length() > 0)
                {
                    size = getTextRenderSize(dc, noNetwork);
                    double x = viewport.getWidth() - size.getWidth();
                    drawLabel(dc, noNetwork, new Vec4(x, verticalSpacing, 0), Color.RED);
                }
                else if (activatedDownload)
                {
                    //draw background image
                    if (iconTexture == null)
                        initBGTexture(dc);

                    double width = this.getScaledBGWidth();
                    double height = this.getScaledBGHeight();

                    if (iconTexture != null)
                    {
                        gl.glTranslated(viewport.getWidth() - width, 0, 0d);
                        gl.glTranslated(width / 2, height / 2, 0);
                        gl.glRotated(rotated, 0d, 0d, 1d);
                        gl.glTranslated(-width / 2, -height / 2, 0);

                        if (iconTexture != null)
                        {
                            gl.glEnable(GL.GL_TEXTURE_2D);
                            iconTexture.bind(gl);
                            gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                            gl.glEnable(GL.GL_BLEND);
                            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                            TextureCoords texCoords = iconTexture.getImageTexCoords();
                            gl.glScaled(width, height, 1d);
                            dc.drawUnitQuad(texCoords);
                            gl.glDisable(GL.GL_TEXTURE_2D);
                            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
                        }
                    }
                }
            }
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    private void bumpRotation()
    {
        if (rotated > rotationIncrement)
            rotated = rotated - rotationIncrement;
        else
            rotated = 360;
    }

    public void stageChanged(RenderingEvent event)
    {
        if (!event.getStage().equals(RenderingEvent.BEFORE_BUFFER_SWAP))
            return;

        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                if (eventSource.getView() != null && eventSource.getView().getEyePosition() != null)
                    altDisplay = makeEyeAltitudeDescription(
                        eventSource.getView().getEyePosition().getElevation());
                else
                    altDisplay = (Logging.getMessage("term.Altitude"));
            }
        });
    }

    private Dimension getTextRenderSize(DrawContext dc, String text)
    {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            this.defaultFont);
        Rectangle2D nameBound = textRenderer.getBounds(text);

        return nameBound.getBounds().getSize();
    }

    // Draw the label
    private void drawLabel(DrawContext dc, String text, Vec4 screenPoint, Color textColor)
    {
        int x = (int) screenPoint.x();
        int y = (int) screenPoint.y();

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            this.defaultFont);
        textRenderer.begin3DRendering();
        textRenderer.setColor(this.getBackgroundColor(textColor));
        textRenderer.draw(text, x + 1, y - 1);
        textRenderer.setColor(textColor);
        textRenderer.draw(text, x, y);
        textRenderer.end3DRendering();
    }

    private final float[] compArray = new float[4];

    // Compute background color for best contrast
    private Color getBackgroundColor(Color color)
    {
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if (compArray[2] > 0.5)
            return new Color(0, 0, 0, 0.7f);
        else
            return new Color(1, 1, 1, 0.7f);
    }

    protected Position previousPos;

    private void handleCursorPositionChange(PositionEvent event)
    {
        Position newPos = event.getPosition();
        if (newPos != null)
        {
            latDisplay = makeAngleDescription("Lat", newPos.getLatitude(), coordDecimalPlaces);
            lonDisplay = makeAngleDescription("Lon", newPos.getLongitude(), coordDecimalPlaces);
            elevDisplay = makeCursorElevationDescription(
                eventSource.getModel().getGlobe().getElevation(newPos.getLatitude(), newPos.getLongitude()));

            //Need to force an extra draw.  without this the displayed value lags the actual when just moving cursor
            if ((previousPos != null) && (previousPos.getLatitude().compareTo(newPos.getLatitude()) != 0)
                && (previousPos.getLongitude().compareTo(newPos.getLongitude()) != 0))
                this.eventSource.redraw();
        }
        else
        {
            latDisplay = "";
            lonDisplay = Logging.getMessage("term.OffGlobe");
            elevDisplay = "";
        }

        previousPos = newPos;
    }

    private void initBGTexture(DrawContext dc)
    {
        try
        {
            InputStream iconStream = this.getClass().getResourceAsStream("/" + iconFilePath_bg);
            if (iconStream == null)
            {
                File iconFile = new File(iconFilePath_bg);
                if (iconFile.exists())
                {
                    iconStream = new FileInputStream(iconFile);
                }
            }

            TextureData textureData = OGLUtil.newTextureData(dc.getGL().getGLProfile(), iconStream, false);
            iconTexture = TextureIO.newTexture(textureData);
            iconTexture.bind(dc.getGL());
            this.bgWidth = iconTexture.getWidth();
            this.bgHeight = iconTexture.getHeight();
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg, e);
        }
    }

    private void drawFilledRectangle(DrawContext dc, Vec4 origin, Dimension dimension, Color color)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
            (byte) color.getBlue(), (byte) color.getAlpha());
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3d(origin.x, origin.y, 0);
        gl.glVertex3d(origin.x + dimension.getWidth(), origin.y, 0);
        gl.glVertex3d(origin.x + dimension.getWidth(), origin.y + dimension.getHeight(), 0);
        gl.glVertex3d(origin.x, origin.y + dimension.getHeight(), 0);
        gl.glVertex3d(origin.x, origin.y, 0);
        gl.glEnd();
    }

    protected String makeAngleDescription(String label, Angle angle, int places)
    {
        return String.format("%s %s", label, angle.toDecimalDegreesString(places));
    }

    protected String makeEyeAltitudeDescription(double metersAltitude)
    {
        String altitude = Logging.getMessage("term.Altitude");
        if (UNIT_IMPERIAL.equals(elevationUnit))
            return String.format("%s %,d mi", altitude, (int) Math.round(WWMath.convertMetersToMiles(metersAltitude)));
        else // Default to metric units.
            return String.format("%s %,d km", altitude, (int) Math.round(metersAltitude / 1e3));
    }

    protected String makeCursorElevationDescription(double metersElevation)
    {
        String elev = Logging.getMessage("term.Elev");
        if (UNIT_IMPERIAL.equals(elevationUnit))
            return String.format("%s %,d feet", elev, (int) Math.round(WWMath.convertMetersToFeet(metersElevation)));
        else // Default to metric units.
            return String.format("%s %,d meters", elev, (int) Math.round(metersElevation));
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.StatusLayer.Name");
    }

    public static class StatusUTMLayer extends StatusLayer
    {
        public void moved(PositionEvent event)
        {
            this.handleCursorPositionChange(event);
        }

        private void handleCursorPositionChange(PositionEvent event)
        {
            Position newPos = event.getPosition();
            if (newPos != null)
            {
                //merge lat & lon into one field to display UMT coordinates in lon field
                String las = makeAngleDescription("Lat", newPos.getLatitude(), coordDecimalPlaces) + " "
                    + makeAngleDescription("Lon", newPos.getLongitude(), coordDecimalPlaces);
                String els = makeCursorElevationDescription(
                    getEventSource().getModel().getGlobe().getElevation(newPos.getLatitude(), newPos.getLongitude()));
                String los;
                try
                {
                    UTMCoord UTM = UTMCoord.fromLatLon(newPos.getLatitude(), newPos.getLongitude(),
                        getEventSource().getModel().getGlobe());
                    los = UTM.toString();
                }
                catch (Exception e)
                {
                    los = "";
                }
                latDisplay = las;
                lonDisplay = los;
                elevDisplay = els;

                if ((previousPos != null) && (previousPos.getLatitude().compareTo(newPos.getLatitude()) != 0)
                    && (previousPos.getLongitude().compareTo(newPos.getLongitude()) != 0))
                    this.eventSource.redraw();
            }
            else
            {
                latDisplay = "";
                lonDisplay = Logging.getMessage("term.OffGlobe");
                elevDisplay = "";
            }
        }
    }

    public static class StatusMGRSLayer extends StatusLayer
    {
        public void moved(PositionEvent event)
        {
            this.handleCursorPositionChange(event);
        }

        private void handleCursorPositionChange(PositionEvent event)
        {
            Position newPos = event.getPosition();
            if (newPos != null)
            {
                //merge lat & lon into one field to display MGRS in lon field
                String las = makeAngleDescription("Lat", newPos.getLatitude(), coordDecimalPlaces) + " "
                    + makeAngleDescription("Lon", newPos.getLongitude(), coordDecimalPlaces);
                String els = makeCursorElevationDescription(
                    getEventSource().getModel().getGlobe().getElevation(newPos.getLatitude(), newPos.getLongitude()));
                String los;
                try
                {
                    MGRSCoord MGRS = MGRSCoord.fromLatLon(newPos.getLatitude(), newPos.getLongitude(),
                        getEventSource().getModel().getGlobe());
                    los = MGRS.toString();
                }
                catch (Exception e)
                {
                    los = "";
                }
                latDisplay = las;
                lonDisplay = los;
                elevDisplay = els;

                if ((previousPos != null) && (previousPos.getLatitude().compareTo(newPos.getLatitude()) != 0)
                    && (previousPos.getLongitude().compareTo(newPos.getLongitude()) != 0))
                    this.eventSource.redraw();
            }
            else
            {
                latDisplay = "";
                lonDisplay = Logging.getMessage("term.OffGlobe");
                elevDisplay = "";
            }
        }
    }
}
