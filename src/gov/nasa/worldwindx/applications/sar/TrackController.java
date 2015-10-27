/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.view.orbit.*;

import java.awt.*;
import java.beans.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: TrackController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackController
{
    public static final String TRACK_ADD = "TrackController.TrackAdded";
    public static final String TRACK_CURRENT = "TrackController.TrackCurrent";
    public static final String TRACK_DIRTY_BIT = "TrackController.TrackDirtyBit";
    public static final String TRACK_DISABLE = "TrackController.TrackDisabled";
    public static final String TRACK_ENABLE = "TrackController.TrackEnabled";
    public static final String TRACK_MODIFY = "TrackController.TrackModified";
    public static final String TRACK_NAME = "TrackController.TrackName";
    public static final String TRACK_OFFSET = "TrackController.TrackOffset";
    public static final String TRACK_REMOVE = "TrackController.TrackRemoved";

    public static final String BEGIN_TRACK_POINT_ENTRY = "TrackController.BeginTrackPointEntry";
    public static final String END_TRACK_POINT_ENTRY = "TrackController.EndTrackPointEntry";
    public static final String MOVE_TO_NEXT_POINT = "TrackController.MoveToNextPoint";
    public static final String REMOVE_LAST_POINT = "TrackController.RemoveLastPoint";

    public static final String EXTENSION_PLANE = "TrackController.ExtensionPlane";
    public static final String EXTENSION_CURSOR_GROUND = "TrackController.ExtensionMouseGround";
    public static final String EXTENSION_CURSOR_AIR = "TrackController.ExtensionMouseAir";

    private WorldWindow wwd;
    private TracksPanel tracksPanel;
    private AnalysisPanel analysisPanel;
    private HashMap<SARTrack, Layer> trackLayers = new HashMap<SARTrack, Layer>();
    private SARTrackBuilder trackBuilder;
    private SARTrackExtensionTool trackExtensionTool;

    private final SelectListener selectListener = new SelectListener()
    {
        public void selected(SelectEvent event)
        {
            if (event == null)
                return;

            onSelected(event);
        }
    };

    public TrackController()
    {
        this.trackBuilder = new SARTrackBuilder();
        this.trackExtensionTool = new SARTrackExtensionTool();
    }

    public WorldWindow getWwd()
    {
        return wwd;
    }

    public void setWwd(WorldWindow wwd)
    {
        if (wwd == this.wwd)
            return;

        if (this.wwd != null)
            this.wwd.removeSelectListener(this.selectListener);

        this.wwd = wwd;

        if (this.wwd != null)
            this.wwd.addSelectListener(this.selectListener);

        this.trackBuilder.setWwd(this.wwd);
        this.trackExtensionTool.setWorldWindow(this.wwd);
    }

    public TracksPanel getTracksPanel()
    {
        return tracksPanel;
    }

    public void setTracksPanel(TracksPanel tracksPanel)
    {
        this.tracksPanel = tracksPanel;
    }

    public AnalysisPanel getAnalysisPanel()
    {
        return analysisPanel;
    }

    public void setAnalysisPanel(AnalysisPanel analysisPanel)
    {
        this.analysisPanel = analysisPanel;
        this.analysisPanel.setTrackController(this);
    }

    public void addTrack(SARTrack track)
    {
        if (track == null)
            return;

        this.createPolylineTrackRepresentation(track);

        track.addPropertyChangeListener(new PropertyChangeListener()
        {
            @SuppressWarnings({"StringEquality"})
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_REMOVE)
                    removeTrack((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_MODIFY)
                    updateTrack((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_ENABLE)
                    enableTrack((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_DISABLE)
                    disableTrack((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_CURRENT)
                    trackCurrent((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_NAME)
                    trackName((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.TRACK_DIRTY_BIT)
                    trackDirtyBit((SARTrack) propertyChangeEvent.getSource());
                else if (propertyChangeEvent.getPropertyName() == TrackController.BEGIN_TRACK_POINT_ENTRY)
                    beginTrackPointEntry(propertyChangeEvent);
                else if (propertyChangeEvent.getPropertyName() == TrackController.END_TRACK_POINT_ENTRY)
                    endTrackPointEntry(propertyChangeEvent);
                else if (propertyChangeEvent.getPropertyName() == TrackController.MOVE_TO_NEXT_POINT)
                    moveToNextTrackPoint();
                else if (propertyChangeEvent.getPropertyName() == TrackController.REMOVE_LAST_POINT)
                    removeLastTrackPoint();
            }
        });

        this.tracksPanel.addTrack(track);
        this.moveToTrack(track);
    }

    public SARTrack getCurrentTrack()
    {
        return this.tracksPanel.getCurrentTrack();
    }

    public void refreshCurrentTrack()
    {
        trackCurrent(getCurrentTrack());
    }

    private void createPolylineTrackRepresentation(SARTrack track)
    {
        Polyline airPath = new Polyline(track);
        airPath.setOffset(track.getOffset());
        airPath.setPathType(Polyline.RHUMB_LINE);
        airPath.setColor(track.getColor());

        Polyline groundPath = new Polyline(track);
        groundPath.setFollowTerrain(true);
        groundPath.setPathType(Polyline.RHUMB_LINE);
        groundPath.setColor(track.getColor());
        groundPath.setStippleFactor(5);
        groundPath.setStipplePattern((short) 0xAAAA);

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderable(airPath);
        layer.addRenderable(groundPath);
        this.wwd.getModel().getLayers().add(layer);
        if (this.wwd != null)
            this.wwd.redraw();
        this.trackLayers.put(track, layer);
    }

    private void removeTrack(SARTrack track)
    {
        Layer layer = this.trackLayers.get(track);
        if (layer == null)
            return;

        this.trackLayers.remove(track);
        this.wwd.getModel().getLayers().remove(layer);
        if (this.wwd != null)
            this.wwd.redraw();
    }

    private void enableTrack(SARTrack track)
    {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null)
            return;

        layer.setEnabled(true);
        if (this.wwd != null)
            this.wwd.redraw();
    }

    private void disableTrack(SARTrack track)
    {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null)
            return;

        layer.setEnabled(false);
        if (this.wwd != null)
            this.wwd.redraw();
    }

    private void updateTrack(SARTrack track)
    {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null)
            return;

        for (Renderable r : layer.getRenderables())
        {
            Polyline line = (Polyline) r;
            line.setPositions(track);
            if (!line.isFollowTerrain())
                line.setOffset(track.getOffset());
        }

        if (this.wwd != null)
        {
            this.wwd.redraw();
        }
    }

    private void trackCurrent(SARTrack track)
    {
        this.analysisPanel.setCurrentTrack(track);
        if (this.isExtending() && track != null)
            endTrackPointEntry(new PropertyChangeEvent(track, END_TRACK_POINT_ENTRY, null, null));

        // Adjust track line width
        for (SARTrack st : this.trackLayers.keySet())
        {
            if (st != track)
            {
                this.setTrackLayerLineWidth(st, 1);
            }
        }
        this.setTrackLayerLineWidth(track, 2);

        this.wwd.firePropertyChange(TRACK_CURRENT, null, track); // broadcast track change via wwd
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void trackName(SARTrack track)
    {
        // Intentionally left blank, as a placeholder for future functionality.
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void trackDirtyBit(SARTrack track)
    {
        // Intentionally left blank, as a placeholder for future functionality.
    }

    private void beginTrackPointEntry(PropertyChangeEvent event)
    {
        SARTrack track = (SARTrack) event.getSource();

        if (event.getNewValue().equals(EXTENSION_PLANE))
        {
            this.trackExtensionTool.setArmed(false);

            this.trackExtensionTool.setTrack(track);
            this.trackExtensionTool.setArmed(true);
        }
        else
        {
            this.trackBuilder.setArmed(false);

            this.trackBuilder.setTrack(track);
            this.trackBuilder.setUseTrackElevation(event.getNewValue().equals(EXTENSION_CURSOR_AIR));
            this.trackBuilder.setArmed(true);
        }

        // Broadcast event via wwd
        this.wwd.firePropertyChange(event);
        // Goto track end
        this.analysisPanel.gotoTrackEnd();
    }

    private void endTrackPointEntry(PropertyChangeEvent event)
    {
        this.trackBuilder.setArmed(false);
        this.trackExtensionTool.setArmed(false);

        // Broadcast event via wwd
        this.wwd.firePropertyChange(event);
    }

    private void moveToNextTrackPoint()
    {
        if (this.trackExtensionTool.isArmed() && this.trackExtensionTool.canMoveToNextTrackPoint())
            this.trackExtensionTool.moveToNextTrackPoint();
    }

    private void removeLastTrackPoint()
    {
        if (this.trackBuilder.isArmed() && this.trackBuilder.canRemoveLastTrackPoint())
            this.trackBuilder.removeLastTrackPoint();
        else if (this.trackExtensionTool.isArmed() && this.trackExtensionTool.canRemoveLastTrackPoint())
            this.trackExtensionTool.removeLastTrackPoint();
    }

    public boolean isExtending()
    {
        return this.trackBuilder.isArmed() || this.trackExtensionTool.isArmed();
    }

    //move to the first position in a track
    private void moveToTrack(SARTrack track)
    {

        OrbitView view = (OrbitView) this.wwd.getView();
        if (!track.getPositions().isEmpty())
        {
            Position pos = track.getPositions().get(0);
            ((BasicOrbitView) view).addPanToAnimator(pos, view.getHeading(), Angle.ZERO, 10000, true);
        }
    }

    protected void onSelected(SelectEvent event)
    {
        SARTrack track = this.getPickedTrack(event.getTopPickedObject());

        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            if (track != null)
                this.onTrackClicked(track);
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            this.onTrackRollover(track);
        }
        else if (event.getEventAction().equals(SelectEvent.HOVER))
        {
            this.onTrackHover(track);
        }
    }

    protected SARTrack getPickedTrack(PickedObject pickedObject)
    {
        if (pickedObject == null)
            return null;

        Layer layer = pickedObject.getParentLayer();
        if (layer == null)
            return null;

        return this.getTrackForLayer(layer);
    }

    protected void onTrackClicked(SARTrack track)
    {
        this.tracksPanel.setCurrentTrack(track);
    }

    protected void onTrackRollover(SARTrack track)
    {
        for (SARTrack st : this.trackLayers.keySet())
        {
            if (st != track)
            {
                this.setTrackLayerColor(st, st.getColor());
            }
        }

        if (track != null)
        {
            Color rolloverColor = WWUtil.makeColorDarker(track.getColor());
            this.setTrackLayerColor(track, rolloverColor);
        }
    }

    protected void onTrackHover(SARTrack track)
    {
        // TODO: show tool tip with track name
    }

    private void setTrackLayerColor(SARTrack track, Color color)
    {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null)
            return;

        for (Renderable r : layer.getRenderables())
        {
            Polyline line = (Polyline) r;
            line.setColor(color);
        }

        if (this.wwd != null)
            this.wwd.redraw();
    }

    private void setTrackLayerLineWidth(SARTrack track, double width)
    {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null)
            return;

        for (Renderable r : layer.getRenderables())
        {
            Polyline line = (Polyline) r;
            line.setLineWidth(width);
        }

        if (this.wwd != null)
            this.wwd.redraw();
    }

    private SARTrack getTrackForLayer(Layer layer)
    {
        for (Map.Entry<SARTrack, Layer> entry : this.trackLayers.entrySet())
        {
            if (entry.getValue() == layer)
                return entry.getKey();
        }

        return null;
    }
}
