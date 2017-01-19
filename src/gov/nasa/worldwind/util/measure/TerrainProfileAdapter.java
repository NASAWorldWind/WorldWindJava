/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.Logging;

import java.beans.*;
import java.util.ArrayList;

/**
 * Adapter that forwards control-point position changes from a {@link MeasureTool}
 * to a {@link TerrainProfileLayer} so that the height-data along the measured
 * path can be visualized.
 * 
 * @author Wiehann Matthysen
 */
public class TerrainProfileAdapter implements PropertyChangeListener
{
    protected WorldWindow wwd;
    protected TerrainProfileLayer profileLayer;
    
    /**
     * Construct an adapter for the specified <code>WorldWindow</code> and <code>TerrainProfileLayer</code>.
     *
     * @param wwd the <code>WorldWindow</code> the specified layer is associated with.
     * @param layer the layer to forward control-point events to.
     */
    public TerrainProfileAdapter(WorldWindow wwd, TerrainProfileLayer layer)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        this.wwd = wwd;
        this.profileLayer = layer;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        MeasureTool measureTool = (MeasureTool)event.getSource();
        // Measure shape position list changed - update terrain profile
        if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
        {
            ArrayList<? extends LatLon> positions = measureTool.getPositions();
            if (positions != null && positions.size() > 1)
            {
                this.profileLayer.setPathPositions(positions);
                this.profileLayer.setEnabled(true);
            } else
            {
                this.profileLayer.setEnabled(false);
            }
            this.wwd.redraw();
        }
    }
}
