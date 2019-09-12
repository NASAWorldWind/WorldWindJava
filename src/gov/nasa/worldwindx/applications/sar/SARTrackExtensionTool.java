/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwindx.applications.sar.segmentplane.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Arrays;

/**
 * @author tag
 * @version $Id: SARTrackExtensionTool.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARTrackExtensionTool implements MouseListener, PositionListener, PropertyChangeListener
{
    private boolean armed;
    private WorldWindow wwd; // Can be null.
    private SARTrack track; // Can be null.
    protected SARSegmentPlane segmentPlane;
    protected Position potentialNextPosition;
    protected boolean waitingForNextPosition = true;
    protected boolean ignoreTrackChangeEvents = false;
    protected SegmentPlaneAttributes.GeometryAttributes segmentEndGeomAttribs;
    protected SegmentPlaneAttributes.LabelAttributes segmentEndLabelAttribs;

    public SARTrackExtensionTool()
    {
        this.segmentPlane = new SARSegmentPlane();
        this.segmentPlane.addPropertyChangeListener(this);

        this.segmentEndGeomAttribs =
            this.segmentPlane.getAttributes().getGeometryAttributes(SegmentPlane.SEGMENT_END).copy();
        this.segmentEndLabelAttribs =
            this.segmentPlane.getAttributes().getLabelAttributes(SegmentPlane.SEGMENT_END).copy();
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        boolean wasArmed = this.armed;
        this.armed = armed;
        this.segmentPlane.setArmed(armed);

        if (!wasArmed && this.armed)
        {
            this.start();
        }
        else if (wasArmed && !this.armed)
        {
            this.stop();
        }
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.wwd == wwd)
            return;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().removeMouseListener(this);
            this.wwd.removePositionListener(this);
        }

        this.wwd = wwd;
        this.segmentPlane.setWorldWindow(wwd);

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().addMouseListener(this);
            this.wwd.addPositionListener(this);
        }
    }

    public SARTrack getTrack()
    {
        return this.track;
    }

    public void setTrack(SARTrack track)
    {
        if (this.track == track)
            return;

        if (this.track != null)
        {
            this.track.removePropertyChangeListener(this);
        }

        this.track = track;
        this.onTrackChanged();

        if (this.track != null)
        {
            this.track.addPropertyChangeListener(this);
        }
    }

    public boolean canMoveToNextTrackPoint()
    {
        return this.track != null && !this.waitingForNextPosition;
    }

    public void moveToNextTrackPoint()
    {
        if (this.track == null || this.waitingForNextPosition)
            return;

        this.start();
    }

    public boolean canRemoveLastTrackPoint()
    {
        return this.track != null && this.track.size() != 0;
    }

    public void removeLastTrackPoint()
    {
        if (this.track == null || this.track.size() == 0)
            return;

        int lastIndex = this.track.size() - 1;
        this.track.removePosition(lastIndex);
        this.waitingForNextPosition = true;
    }

    protected void start()
    {
        if (this.track.size() >= 1)
        {
            this.snapPlaneToLastTrackPoint();
            this.segmentPlane.setVisible(true);
        }
        else
        {
            this.segmentPlane.setVisible(false);
        }

        this.waitingForNextPosition = true;
    }

    protected void stop()
    {
        this.segmentPlane.setVisible(false);
    }

    protected void setNextPosition(Position position)
    {
        SARPosition trackPosition = this.positionToTrackPosition(position);

        this.ignoreTrackChangeEvents = true;
        try
        {
            this.track.appendPosition(trackPosition);
        }
        finally
        {
            this.ignoreTrackChangeEvents = false;
        }

        this.segmentPlane.getAttributes().setGeometryAttributes(SegmentPlane.SEGMENT_END,
            this.segmentEndGeomAttribs.copy());
        this.segmentPlane.getAttributes().setLabelAttributes(SegmentPlane.SEGMENT_END,
            this.segmentEndLabelAttribs.copy());

        this.snapPlaneToLastTrackSegment();
    }

    protected void setPotentialNextPosition(Position position)
    {
        this.potentialNextPosition = position;

        if (this.potentialNextPosition != null)
        {
            Position[] segmentPositions = this.segmentPlane.getSegmentPositions();

            this.segmentPlane.setSegmentPositions(segmentPositions[0], this.potentialNextPosition);

            this.segmentPlane.getAttributes().setGeometryAttributes(SegmentPlane.SEGMENT_END,
                this.createPotentialNextPositionGeomAttributes());
            this.segmentPlane.getAttributes().setLabelAttributes(SegmentPlane.SEGMENT_END,
                this.createPotentialNextPositionLabelAttributes());

            this.showSegmentEndPoint(true);
        }
        else
        {
            this.showSegmentEndPoint(false);
        }
    }

    protected PickedObject getTopPickedObject()
    {
        return (this.wwd.getSceneController().getPickedObjectList() != null) ?
            this.wwd.getSceneController().getPickedObjectList().getTopPickedObject() : null;
    }

    //**************************************************************//
    //********************  Mouse Events  **************************//
    //**************************************************************//

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        if (e == null || e.isConsumed())
        {
            return;
        }

        if (!this.armed || this.wwd == null)
        {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (this.waitingForNextPosition)
            {
                if (this.potentialNextPosition != null)
                {
                    this.setNextPosition(this.potentialNextPosition);
                    this.waitingForNextPosition = false;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    //**************************************************************//
    //********************  Position Events  ***********************//
    //**************************************************************//

    public void moved(PositionEvent e)
    {
        if (e == null)
        {
            return;
        }

        if (!this.armed || this.wwd == null)
        {
            return;
        }

        if (this.waitingForNextPosition)
        {
            Position nextPosition = null;

            PickedObject po = this.getTopPickedObject();
            if (po != null)
            {
                Object id = po.getValue(AVKey.PICKED_OBJECT_ID);
                if (id == SegmentPlane.PLANE_BACKGROUND ||
                    (this.segmentPlane.isSnapToGrid() && id == SegmentPlane.PLANE_GRID))
                {
                    nextPosition = po.getPosition();
                }
            }

            this.setPotentialNextPosition(nextPosition);
        }
    }

    //**************************************************************//
    //********************  Property Change Events  ****************//
    //**************************************************************//

    @SuppressWarnings({"StringEquality"})
    public void propertyChange(PropertyChangeEvent e)
    {
        String propertyName = e.getPropertyName();

        if (propertyName == SegmentPlane.SEGMENT_END)
        {
            this.snapTrackPointToPlanePoint(propertyName);
        }
        else if (propertyName == TrackController.TRACK_MODIFY || propertyName == TrackController.TRACK_OFFSET)
        {
            if (!this.ignoreTrackChangeEvents)
            {
                this.start();
            }
        }
    }

    //**************************************************************//
    //********************  Track/Plane Synchronization  ***********//
    //**************************************************************//

    protected void onTrackChanged()
    {
        SegmentPlaneAttributes.LabelAttributes labelAttrib = this.segmentPlane.getAttributes().getLabelAttributes(
            SegmentPlane.HORIZONTAL_AXIS_LABELS);
        if (labelAttrib != null)
        {
            Color labelColor = (this.track != null) ? WWUtil.makeColorBrighter(this.track.getColor())
                : WWUtil.makeColorBrighter(Color.RED);
            labelAttrib.setColor(labelColor);
        }
    }

    @SuppressWarnings({"StringEquality"})
    protected void snapTrackPointToPlanePoint(String planePoint)
    {
        if (this.track == null)
            return;

        if (this.track.size() == 0)
            return;

        if (this.waitingForNextPosition && planePoint == SegmentPlane.SEGMENT_END)
            return;

        Position[] segmentPositions = this.segmentPlane.getSegmentPositions();

        this.ignoreTrackChangeEvents = true;
        try
        {
            if (planePoint == SegmentPlane.SEGMENT_END)
            {
                int lastIndex = this.track.size() - 1;
                SARPosition trackPosition = this.positionToTrackPosition(segmentPositions[1]);
                this.track.set(lastIndex, trackPosition);
            }
        }
        finally
        {
            this.ignoreTrackChangeEvents = false;
        }
    }

    protected void snapPlaneToLastTrackPoint()
    {
        if (this.track == null)
            return;

        if (this.track.size() == 0)
            return;

        int lastIndex = this.track.size() - 1;
        SARPosition lastTrackPosition = this.track.get(lastIndex);
        SARPosition nextTrackPosition = this.computeNextTrackPosition();
        if (nextTrackPosition == null)
            nextTrackPosition = lastTrackPosition;

        Position position1 = this.trackPositionToPosition(lastTrackPosition);
        Position position2 = this.trackPositionToPosition(nextTrackPosition);

        double[] altitudes = this.segmentPlane.computeAltitudesToFitPositions(Arrays.asList(position1, position2));
        LatLon[] locations = this.segmentPlane.computeLocationsToFitPositions(position1, position2);

        this.segmentPlane.setPlaneAltitudes(altitudes[0], altitudes[1]);
        this.segmentPlane.setPlaneLocations(locations[0], locations[1]);
        this.segmentPlane.setSegmentPositions(position1, position2);
        this.showSegmentEndPoint(false);

        this.wwd.redraw();
    }

    protected void snapPlaneToLastTrackSegment()
    {
        if (this.track == null)
            return;

        if (this.track.size() < 2)
            return;

        int lastIndex = this.track.size() - 1;
        SARPosition lastTrackPosition = this.track.get(lastIndex - 1);
        SARPosition nextTrackPosition = this.track.get(lastIndex);

        Position position1 = this.trackPositionToPosition(lastTrackPosition);
        Position position2 = this.trackPositionToPosition(nextTrackPosition);

        double[] altitudes = this.segmentPlane.computeAltitudesToFitPositions(Arrays.asList(position1, position2));
        LatLon[] locations = this.segmentPlane.computeLocationsToFitPositions(position1, position2);

        this.segmentPlane.setPlaneAltitudes(altitudes[0], altitudes[1]);
        this.segmentPlane.setPlaneLocations(locations[0], locations[1]);
        this.segmentPlane.setSegmentPositions(position1, position2);
        this.showSegmentEndPoint(true);

        this.wwd.redraw();
    }

    protected void showSegmentEndPoint(boolean show)
    {
        this.segmentPlane.setObjectVisible(SegmentPlane.SEGMENT_END, show, show);
        this.segmentPlane.setObjectVisible(SegmentPlane.ALTIMETER, show, false);
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected SARPosition computeNextTrackPosition(Point mousePoint)
    {
        View view = this.wwd.getView();
        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Position position = this.segmentPlane.getIntersectionPosition(ray);

        return this.positionToTrackPosition(position);
    }

    protected SARPosition computeNextTrackPosition()
    {
        if (this.track.size() < 2)
        {
            return null;
        }

        Globe globe = this.wwd.getModel().getGlobe();
        double[] gridDimensions = this.segmentPlane.getGridCellDimensions();

        int lastIndex = this.track.size() - 1;
        SARPosition lastPosition = this.track.get(lastIndex);
        Vec4 point = globe.computePointFromPosition(lastPosition);
        double size = this.segmentPlane.getObjectSize(SegmentPlane.SEGMENT_END, point);

        double distance = Math.ceil(2 * size / gridDimensions[0]);
        if (distance < 1)
            distance = 1;
        distance = distance * gridDimensions[0];

        Angle heading = LatLon.rhumbAzimuth(this.track.get(lastIndex - 1), lastPosition);
        Angle angularDistance = Angle.fromRadians(distance / globe.getRadius());
        LatLon nextLocation = LatLon.rhumbEndPosition(lastPosition, heading, angularDistance);

        return new SARPosition(nextLocation.getLatitude(), nextLocation.getLongitude(), lastPosition.getElevation());
    }

    protected SARPosition positionToTrackPosition(Position position)
    {
        double trackOffset = this.track.getOffset();
        return new SARPosition(position.getLatitude(), position.getLongitude(), position.getElevation() - trackOffset);
    }

    protected Position trackPositionToPosition(Position position)
    {
        double trackOffset = this.track.getOffset();
        return new Position(position.getLatitude(), position.getLongitude(), position.getElevation() + trackOffset);
    }

    //**************************************************************//
    //********************  Mouse Events  **************************//
    //**************************************************************//

    protected SegmentPlaneAttributes.GeometryAttributes createPotentialNextPositionGeomAttributes()
    {
        SegmentPlaneAttributes.GeometryAttributes geometryAttributes = new SegmentPlaneAttributes.GeometryAttributes(
            Material.BLUE, 1.0);
        geometryAttributes.setSize(8);
        geometryAttributes.setPickSize(0);

        return geometryAttributes;
    }

    protected SegmentPlaneAttributes.LabelAttributes createPotentialNextPositionLabelAttributes()
    {
        SARSegmentPlane.MessageLabelAttributes labelAttributes = new SARSegmentPlane.MessageLabelAttributes(
            Color.WHITE, Font.decode("Arial-18"), AVKey.LEFT, AVKey.CENTER, "Click to add");
        labelAttributes.setOffset(new Vec4(15, 0, 0));

        return labelAttributes;
    }
}