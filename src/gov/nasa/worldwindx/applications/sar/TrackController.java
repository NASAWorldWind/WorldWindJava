/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.avlist.AVKey;
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
public class TrackController {

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

    private final SelectListener selectListener = new SelectListener() {
        public void selected(SelectEvent event) {
            if (event == null) {
                return;
            }

            onSelected(event);
        }
    };

    public TrackController() {
        this.trackBuilder = new SARTrackBuilder();
        this.trackExtensionTool = new SARTrackExtensionTool();
    }

    public WorldWindow getWwd() {
        return wwd;
    }

    public void setWwd(WorldWindow wwd) {
        if (wwd == this.wwd) {
            return;
        }

        if (this.wwd != null) {
            this.wwd.removeSelectListener(this.selectListener);
        }

        this.wwd = wwd;

        if (this.wwd != null) {
            this.wwd.addSelectListener(this.selectListener);
        }

        this.trackBuilder.setWwd(this.wwd);
        this.trackExtensionTool.setWorldWindow(this.wwd);
    }

    public TracksPanel getTracksPanel() {
        return tracksPanel;
    }

    public void setTracksPanel(TracksPanel tracksPanel) {
        this.tracksPanel = tracksPanel;
    }

    public AnalysisPanel getAnalysisPanel() {
        return analysisPanel;
    }

    public void setAnalysisPanel(AnalysisPanel analysisPanel) {
        this.analysisPanel = analysisPanel;
        this.analysisPanel.setTrackController(this);
    }

    public void addTrack(SARTrack track) {
        if (track == null) {
            return;
        }

        this.createPathTrackRepresentation(track);

        track.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (null != propertyChangeEvent.getPropertyName()) {
                    switch (propertyChangeEvent.getPropertyName()) {
                        case TrackController.TRACK_REMOVE:
                            removeTrack((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_MODIFY:
                            updateTrack((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_ENABLE:
                            enableTrack((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_DISABLE:
                            disableTrack((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_CURRENT:
                            trackCurrent((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_NAME:
                            trackName((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.TRACK_DIRTY_BIT:
                            trackDirtyBit((SARTrack) propertyChangeEvent.getSource());
                            break;
                        case TrackController.BEGIN_TRACK_POINT_ENTRY:
                            beginTrackPointEntry(propertyChangeEvent);
                            break;
                        case TrackController.END_TRACK_POINT_ENTRY:
                            endTrackPointEntry(propertyChangeEvent);
                            break;
                        case TrackController.MOVE_TO_NEXT_POINT:
                            moveToNextTrackPoint();
                            break;
                        case TrackController.REMOVE_LAST_POINT:
                            removeLastTrackPoint();
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        this.tracksPanel.addTrack(track);
        this.moveToTrack(track);
    }

    public SARTrack getCurrentTrack() {
        return this.tracksPanel.getCurrentTrack();
    }

    public void refreshCurrentTrack() {
        trackCurrent(getCurrentTrack());
    }

    private void createPathTrackRepresentation(SARTrack track) {
        Path airPath = new Path(track);
        airPath.setOffset(track.getOffset());
        airPath.setPathType(AVKey.RHUMB_LINE);
        var attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(track.getColor()));
        airPath.setAttributes(attrs);

        Path groundPath = new Path(track);
        groundPath.setSurfacePath(true);
        groundPath.setPathType(AVKey.RHUMB_LINE);
        attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(track.getColor()));
        attrs.setOutlineStippleFactor(5);
        attrs.setOutlineStipplePattern((short) 0xAAAA);
        groundPath.setAttributes(attrs);

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderable(airPath);
        layer.addRenderable(groundPath);
        this.wwd.getModel().getLayers().add(layer);
        if (this.wwd != null) {
            this.wwd.redraw();
        }
        this.trackLayers.put(track, layer);
    }

    private void removeTrack(SARTrack track) {
        Layer layer = this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        this.trackLayers.remove(track);
        this.wwd.getModel().getLayers().remove(layer);
        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private void enableTrack(SARTrack track) {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        layer.setEnabled(true);
        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private void disableTrack(SARTrack track) {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        layer.setEnabled(false);
        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private void updateTrack(SARTrack track) {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        for (Renderable r : layer.getRenderables()) {
            Path line = (Path) r;
            line.setPositions(track);
            if (!line.isFollowTerrain()) {
                line.setOffset(track.getOffset());
            }
        }

        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private void trackCurrent(SARTrack track) {
        this.analysisPanel.setCurrentTrack(track);
        if (this.isExtending() && track != null) {
            endTrackPointEntry(new PropertyChangeEvent(track, END_TRACK_POINT_ENTRY, null, null));
        }

        // Adjust track line width
        for (SARTrack st : this.trackLayers.keySet()) {
            if (st != track) {
                this.setTrackLayerLineWidth(st, 1);
            }
        }
        this.setTrackLayerLineWidth(track, 2);

        this.wwd.firePropertyChange(TRACK_CURRENT, null, track); // broadcast track change via wwd
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void trackName(SARTrack track) {
        // Intentionally left blank, as a placeholder for future functionality.
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void trackDirtyBit(SARTrack track) {
        // Intentionally left blank, as a placeholder for future functionality.
    }

    private void beginTrackPointEntry(PropertyChangeEvent event) {
        SARTrack track = (SARTrack) event.getSource();

        if (event.getNewValue().equals(EXTENSION_PLANE)) {
            this.trackExtensionTool.setArmed(false);

            this.trackExtensionTool.setTrack(track);
            this.trackExtensionTool.setArmed(true);
        } else {
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

    private void endTrackPointEntry(PropertyChangeEvent event) {
        this.trackBuilder.setArmed(false);
        this.trackExtensionTool.setArmed(false);

        // Broadcast event via wwd
        this.wwd.firePropertyChange(event);
    }

    private void moveToNextTrackPoint() {
        if (this.trackExtensionTool.isArmed() && this.trackExtensionTool.canMoveToNextTrackPoint()) {
            this.trackExtensionTool.moveToNextTrackPoint();
        }
    }

    private void removeLastTrackPoint() {
        if (this.trackBuilder.isArmed() && this.trackBuilder.canRemoveLastTrackPoint()) {
            this.trackBuilder.removeLastTrackPoint();
        } else if (this.trackExtensionTool.isArmed() && this.trackExtensionTool.canRemoveLastTrackPoint()) {
            this.trackExtensionTool.removeLastTrackPoint();
        }
    }

    public boolean isExtending() {
        return this.trackBuilder.isArmed() || this.trackExtensionTool.isArmed();
    }

    //move to the first position in a track
    private void moveToTrack(SARTrack track) {

        OrbitView view = (OrbitView) this.wwd.getView();
        if (!track.getPositions().isEmpty()) {
            Position pos = track.getPositions().get(0);
            ((BasicOrbitView) view).addPanToAnimator(pos, view.getHeading(), Angle.ZERO, 10000, true);
        }
    }

    protected void onSelected(SelectEvent event) {
        SARTrack track = this.getPickedTrack(event.getTopPickedObject());

        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
            if (track != null) {
                this.onTrackClicked(track);
            }
        } else if (event.getEventAction().equals(SelectEvent.ROLLOVER)) {
            this.onTrackRollover(track);
        } else if (event.getEventAction().equals(SelectEvent.HOVER)) {
            this.onTrackHover(track);
        }
    }

    protected SARTrack getPickedTrack(PickedObject pickedObject) {
        if (pickedObject == null) {
            return null;
        }

        Layer layer = pickedObject.getParentLayer();
        if (layer == null) {
            return null;
        }

        return this.getTrackForLayer(layer);
    }

    protected void onTrackClicked(SARTrack track) {
        this.tracksPanel.setCurrentTrack(track);
    }

    protected void onTrackRollover(SARTrack track) {
        for (SARTrack st : this.trackLayers.keySet()) {
            if (st != track) {
                this.setTrackLayerColor(st, st.getColor());
            }
        }

        if (track != null) {
            Color rolloverColor = WWUtil.makeColorDarker(track.getColor());
            this.setTrackLayerColor(track, rolloverColor);
        }
    }

    protected void onTrackHover(SARTrack track) {
        // TODO: show tool tip with track name
    }

    private void setTrackLayerColor(SARTrack track, Color color) {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        for (Renderable r : layer.getRenderables()) {
            Path line = (Path) r;
            line.getActiveAttributes().setOutlineMaterial(new Material(color));
        }

        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private void setTrackLayerLineWidth(SARTrack track, double width) {
        RenderableLayer layer = (RenderableLayer) this.trackLayers.get(track);
        if (layer == null) {
            return;
        }

        for (Renderable r : layer.getRenderables()) {
            Path line = (Path) r;
            line.getActiveAttributes().setOutlineWidth(width);
        }

        if (this.wwd != null) {
            this.wwd.redraw();
        }
    }

    private SARTrack getTrackForLayer(Layer layer) {
        for (Map.Entry<SARTrack, Layer> entry : this.trackLayers.entrySet()) {
            if (entry.getValue() == layer) {
                return entry.getKey();
            }
        }

        return null;
    }
}
