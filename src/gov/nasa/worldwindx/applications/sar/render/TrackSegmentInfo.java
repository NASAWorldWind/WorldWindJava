/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.sar.*;

import com.jogamp.opengl.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: TrackSegmentInfo.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class TrackSegmentInfo implements Renderable
{
    protected boolean enabled;
    protected SARTrack track;
    protected int segmentIndex;
    protected Position segmentPosition;
    protected Object angleFormat;
    protected Object elevationUnit;

    public TrackSegmentInfo()
    {
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enable)
    {
        this.enabled = enable;
    }

    public SARTrack getTrack()
    {
        return this.track;
    }

    public void setTrack(SARTrack track)
    {
        this.track = track;
    }

    public int getSegmentIndex()
    {
        return this.segmentIndex;
    }

    public void setSegmentIndex(int index)
    {
        this.segmentIndex = index;
    }

    public Position getSegmentPosition()
    {
        return this.segmentPosition;
    }

    public void setSegmentPosition(Position pos)
    {
        this.segmentPosition = pos;
    }

    public Object getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(Object angleFormat)
    {
        this.angleFormat = angleFormat;
    }

    public Object getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(Object elevationUnit)
    {
        this.elevationUnit = elevationUnit;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isEnabled())
            return;

        this.doRender(dc);
    }

    protected void doRender(DrawContext dc)
    {
        if (this.track == null)
            return;

        if (this.segmentIndex < 0 || this.segmentIndex >= this.track.size())
            return;

        this.drawSegmentLabel(dc, this.track, this.segmentIndex);

        if (this.segmentIndex < this.track.size() - 1)
            this.drawSegmentLabel(dc, this.track, this.segmentIndex + 1);

        if (this.segmentPosition != null)
            this.drawSegmentPositionLabel(dc, this.track, this.segmentIndex, this.segmentPosition);
    }

    protected void drawSegmentLabel(DrawContext dc, SARTrack track, int index)
    {
        SARPosition pos = track.get(index);
        Vec4 screenPoint = this.getScreenPoint(dc, pos);
        this.drawLatLonLabel(dc, (int) screenPoint.x, (int) screenPoint.y, Font.decode("Arial-BOLD-12"),
            WWUtil.makeColorBrighter(track.getColor()), pos);
    }

    protected Vec4 getScreenPoint(DrawContext dc, Position position)
    {
        if (dc.getGlobe() == null || dc.getView() == null)
            return null;

        Vec4 modelPoint = dc.getGlobe().computePointFromPosition(position.getLatitude(), position.getLongitude(),
            position.getElevation());
        if (modelPoint == null)
            return null;

        return dc.getView().project(modelPoint);
    }

    protected void drawSegmentPositionLabel(DrawContext dc, SARTrack track, int index, Position pos)
    {
        Angle heading = null;
        // If there is a track position after this index, then compute the heading from this track position to the next
        // track position.
        if (index < track.size() - 1)
        {
            heading = LatLon.rhumbAzimuth(track.get(index), track.get(index + 1));
        }
        // Otherwise, this is the last track position. If there is a track position before this index, then compute the
        // heading from the previous track position to this track position.
        else if (index > 0)
        {
            heading = LatLon.rhumbAzimuth(track.get(index - 1), track.get(index));
        }

        Vec4 screenPoint = this.getScreenPoint(dc, pos);
        this.drawHeadingAltitudeLabel(dc, (int) screenPoint.x, (int) screenPoint.y, Font.decode("Arial-BOLD-12"),
            Color.YELLOW, heading, pos);
    }

    protected void drawHeadingAltitudeLabel(DrawContext dc, int x, int y, Font font, Color color, Angle heading,
        Position pos)
    {
        double surfaceElevation = this.computeSurfaceElevation(dc, pos.getLatitude(), pos.getLongitude());
        double distanceFromEye = dc.getView().getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(pos));

        StringBuilder sb = new StringBuilder();
        if (heading != null)
        {
            sb.append("Heading: ");
            sb.append(heading.toDecimalDegreesString(0));
            sb.append("\n");
        }
        sb.append("Alt: ");
        sb.append(this.formatAltitude(pos.getElevation()));
        sb.append("\n");
        sb.append("AGL: ");
        sb.append(this.formatAltitude(pos.getElevation() - surfaceElevation));

        this.drawText(dc, sb.toString(), x, y, font, color, distanceFromEye);
    }

    protected void drawLatLonLabel(DrawContext dc, int x, int y, Font font, Color color, Position pos)
    {
        double distanceFromEye = dc.getView().getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(pos));

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.formatAngle(pos.getLatitude()));
        sb.append(", ");
        sb.append(this.formatAngle(pos.getLongitude()));
        sb.append(")");

        this.drawText(dc, sb.toString(), x, y, font, color, distanceFromEye);
    }

    protected void drawText(DrawContext dc, String text, int x, int y, Font font, Color color, double distanceFromEye)
    {
        dc.addOrderedRenderable(new OrderedText(text, x, y, font, color, distanceFromEye));
    }

    protected static class OrderedText implements OrderedRenderable
    {
        private final String text;
        private final int x;
        private final int y;
        private final Font font;
        private final Color color;
        private final double distanceFromEye;

        public OrderedText(String text, int x, int y, Font font, Color color, double distanceFromEye)
        {
            this.text = text;
            this.x = x;
            this.y = y;
            this.font = font;
            this.color = color;
            this.distanceFromEye = distanceFromEye;
        }

        public double getDistanceFromEye()
        {
            return this.distanceFromEye;
        }

        public void render(DrawContext dc)
        {
            this.drawText(dc, this.text, this.x, this.y, this.font, this.color);
        }

        protected void drawText(DrawContext dc, String text, int x, int y, Font font, Color color)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            Rectangle viewport = dc.getView().getViewport();

            OGLStackHandler stackHandler = new OGLStackHandler();
            stackHandler.pushAttrib(gl, GL2.GL_CURRENT_BIT); // For current color.
            try
            {
                MultiLineTextRenderer tr = this.getTextRendererFor(dc, font);
                tr.setTextAlign(AVKey.CENTER);
                tr.getTextRenderer().beginRendering(viewport.width, viewport.height);
                try
                {
                    tr.setTextColor(color);
                    tr.setBackColor(Color.BLACK);

                    Rectangle bounds = tr.getBounds(text);
                    tr.draw(text, x, y + (3 * bounds.height / 2), AVKey.TEXT_EFFECT_OUTLINE);
                }
                finally
                {
                    tr.getTextRenderer().endRendering();
                }
            }
            finally
            {
                stackHandler.pop(gl);
            }
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
        }

        protected MultiLineTextRenderer getTextRendererFor(DrawContext dc, Font font)
        {
            TextRenderer tr = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            return new MultiLineTextRenderer(tr);
        }
    }

    protected double computeSurfaceElevation(DrawContext dc, Angle latitude, Angle longitude)
    {
        if (dc.getSurfaceGeometry() != null)
        {
            Vec4 surfacePoint = dc.getSurfaceGeometry().getSurfacePoint(latitude, longitude);
            if (surfacePoint != null)
            {
                Position surfacePos = dc.getGlobe().computePositionFromPoint(surfacePoint);
                return surfacePos.getElevation();
            }
        }

        return dc.getGlobe().getElevation(latitude, longitude);
    }

    protected String formatAngle(Angle angle)
    {
        return Angle.ANGLE_FORMAT_DMS.equals(this.angleFormat) ? angle.toDMSString() : angle.toDecimalDegreesString(4);
    }

    protected String formatAltitude(double altitude)
    {
        return SAR2.UNIT_IMPERIAL.equals(this.elevationUnit) ?
            String.format("%d ft", (long) WWMath.convertMetersToFeet(altitude)) :
            String.format("%d m", (long) altitude);
    }
}
