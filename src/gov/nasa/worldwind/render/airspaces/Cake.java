/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * A cake consists of mutiple adjacent cylinder segments. Each cylinder is defined by its center location in latitude
 * longitude, its radius in meters, and two aziumths specifying the active circumferential portion of the cylinder. Cake
 * segments are altitude-limited shapes and therefore have an associated minimum and maximum altitude.
 *
 * @author tag
 * @version $Id: Cake.java 2331 2014-09-19 19:45:55Z tgaskins $
 */
public class Cake extends AbstractAirspace
{
    /** An inner class of {@link Cake} defining the parameters of one of the cake's cylinders. */
    public static class Layer extends PartialCappedCylinder
    {
        public Layer(LatLon location, double radius, Angle leftAzimuth, Angle rightAzimuth,
            double lowerAltitude, double upperAltitude)
        {
            super(location, radius, leftAzimuth, rightAzimuth);
            this.setAltitudes(lowerAltitude, upperAltitude);
        }

        public Layer(LatLon location, double radius, Angle leftAzimuth, Angle rightAzimuth)
        {
            super(location, radius, leftAzimuth, rightAzimuth);
        }

        public Layer(LatLon location, double radius)
        {
            super(location, radius);
        }

        public Layer(AirspaceAttributes attributes)
        {
            super(attributes);
        }

        public Layer()
        {
        }
    }

    private List<Layer> layers = new ArrayList<Layer>();

    public Cake(Collection<Layer> layers)
    {
        this.addLayers(layers);
    }

    public Cake(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    public Cake()
    {
    }

    /**
     * Returns the partial cylinders comprising the shape.
     *
     * @return the cylinders comprising the shape, or an empty list if the shape contains no layers.
     */
    public List<Layer> getLayers()
    {
        return Collections.unmodifiableList(this.layers);
    }

    /**
     * Set the partial cylinders comprising the shape.
     *
     * @param layers the cylinders comprising the shape. May be an empty list.
     *
     * @throws IllegalArgumentException if the list reference is null.
     */
    public void setLayers(Collection<Layer> layers)
    {
        this.layers.clear();
        this.addLayers(layers);
    }

    protected void addLayers(Iterable<Layer> newLayers)
    {
        if (newLayers != null)
        {
            for (Layer l : newLayers)
            {
                if (l != null)
                    this.layers.add(l);
            }
        }

        this.invalidateAirspaceData();
    }

    public void setEnableCaps(boolean enable)
    {
        for (Layer l : this.layers)
        {
            l.setEnableCaps(enable);
        }
    }

    public void setEnableDepthOffset(boolean enable)
    {
        for (Layer l : this.layers)
        {
            l.setEnableDepthOffset(enable);
        }
    }

    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
    {
        super.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
        for (Layer l : this.layers)
        {
            l.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
        }
    }

    public boolean isAirspaceVisible(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If the parent Cake is not visible, then return false immediately without testing the child layers.        
        if (!super.isAirspaceVisible(dc))
            return false;

        boolean visible = false;

        // The parent Cake is visible. Since the parent Cake's extent potentially contains volumes where no child
        // geometry exists, test that at least one of the child layers are visible.
        for (Layer l : this.layers)
        {
            if (l.isAirspaceVisible(dc))
            {
                visible = true;
                break;
            }
        }

        return visible;
    }

    public Position getReferencePosition()
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>(this.layers.size());
        for (Layer l : this.layers)
        {
            locations.add(l.getCenter());
        }

        return this.computeReferencePosition(locations, this.getAltitudes());
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Layer> cakeLayers = this.getLayers();

        if (cakeLayers == null || cakeLayers.isEmpty())
        {
            return null;
        }
        else if (cakeLayers.size() == 1)
        {
            return cakeLayers.get(0).computeExtent(globe, verticalExaggeration);
        }
        else
        {
            ArrayList<Box> extents = new ArrayList<Box>();

            for (Layer layer : cakeLayers)
            {
                extents.add(layer.computeExtent(globe, verticalExaggeration));
            }

            return Box.union(extents);
        }
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        return null; // Cake is a geometry container, and therefore has no geometry itself.
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.doMoveTo(oldRef, newRef);

        for (Layer l : this.layers)
        {
            l.doMoveTo(globe, oldRef, newRef);
        }

        this.invalidateAirspaceData();
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.doMoveTo(oldRef, newRef);

        for (Layer l : this.layers)
        {
            l.doMoveTo(oldRef, newRef);
        }

        this.invalidateAirspaceData();
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        this.determineActiveAttributes(dc);

        for (Layer layer : this.layers)
        {
            // Synchronize the layer's attributes with this cake's attributes, and setup this cake as the layer's pick
            // delegate.
            layer.setAttributes(this.getActiveAttributes());
            layer.setDelegateOwner(this.getDelegateOwner() != null ? this.getDelegateOwner() : this);
            layer.preRender(dc);
        }
    }

    @Override
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        if (!this.isAirspaceVisible(dc))
            return;

        for (Layer layer : this.layers)
        {
            layer.render(dc);
        }
    }

    @Override
    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        // Intentionally left blank.
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        RestorableSupport.StateObject so = rs.addStateObject(context, "layers");
        for (Layer layer : this.layers)
        {
            RestorableSupport.StateObject lso = rs.addStateObject(so, "layer");
            layer.doGetRestorableState(rs, lso);
        }
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        RestorableSupport.StateObject so = rs.getStateObject(context, "layers");
        if (so == null)
            return;

        RestorableSupport.StateObject[] lsos = rs.getAllStateObjects(so, "layer");
        if (lsos == null || lsos.length == 0)
            return;

        ArrayList<Layer> layerList = new ArrayList<Layer>(lsos.length);

        for (RestorableSupport.StateObject lso : lsos)
        {
            if (lso != null)
            {
                Layer layer = new Layer();
                layer.doRestoreState(rs, lso);
                layerList.add(layer);
            }
        }

        this.setLayers(layerList);
    }
}
