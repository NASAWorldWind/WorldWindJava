/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.layers.Layer;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: AirspaceEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface AirspaceEditor extends Layer
{
    Airspace getAirspace();

    boolean isArmed();

    void setArmed(boolean armed);

    boolean isUseRubberBand();

    void setUseRubberBand(boolean state);

    boolean isKeepControlPointsAboveTerrain();

    void setKeepControlPointsAboveTerrain(boolean state);

    AirspaceControlPointRenderer getControlPointRenderer();

    void setControlPointRenderer(AirspaceControlPointRenderer renderer);

    AirspaceEditListener[] getEditListeners();

    void addEditListener(AirspaceEditListener listener);

    void removeEditListener(AirspaceEditListener listener);

    // TODO
    // the purposes of these methods may be okay, but there are some obvious problems:
    //
    // 1. any change in parameters would require a signature change (params should be bundled)
    //
    // 2. they do not allow the editor any control over how to respond to input
    //
    // 3. they assume the editor can do something reasonable with the call

    void moveAirspaceLaterally(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint);

    void moveAirspaceVertically(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint);

    AirspaceControlPoint addControlPoint(WorldWindow wwd, Airspace airspace, 
        Point mousePoint);

    void removeControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint);

    void moveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);

    void resizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);
}
