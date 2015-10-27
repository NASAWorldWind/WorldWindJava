/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.PickedObject;

import java.awt.*;
import java.util.Collection;

/**
 * SurfaceTileDrawContext defines a context for rendering into off-screen surface tiles. SurfaceTileDrawContext is
 * defined by a geographic sector, a screen viewport, and a collection of pick candidates. The sector maps geographic
 * coordinates to pixels in an abstract off-screen tile. The pick candidates provide registration of picked objects
 * drawn into the surface tile.
 *
 * @author dcollins
 * @version $Id: SurfaceTileDrawContext.java 2320 2014-09-17 19:29:24Z dcollins $
 */
public class SurfaceTileDrawContext
{
    protected Sector sector;
    protected Rectangle viewport;
    protected Matrix modelview;
    protected Collection<PickedObject> pickCandidates;

    /**
     * Creates a new SurfaceTileDrawContext from the specified tile and collection of pick candidates. The tile defines
     * this context's geographic extent and screen viewport. The pick candidate collection is used to register picked
     * objects drawn into the surface tile.
     *
     * @param tile           the context's tile.
     * @param pickCandidates the context's list of pick candidates.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public SurfaceTileDrawContext(Tile tile, Collection<PickedObject> pickCandidates)
    {
        if (tile == null)
        {
            String message = Logging.getMessage("nullValue.TileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (pickCandidates == null)
        {
            String message = Logging.getMessage("nullValue.PickedObjectList");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sector = tile.getSector();
        this.viewport = new Rectangle(0, 0, tile.getWidth(), tile.getHeight());
        this.modelview = Matrix.fromGeographicToViewport(tile.getSector(), 0, 0, tile.getWidth(), tile.getHeight());
        this.pickCandidates = pickCandidates;
    }

    /**
     * Returns this context's sector.
     *
     * @return this's sector.
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Returns this context's viewport.
     *
     * @return this context's viewport.
     */
    public Rectangle getViewport()
    {
        return this.viewport;
    }

    /**
     * Returns a Matrix mapping geographic coordinates to pixels in the off-screen tile.
     *
     * @return Matrix mapping geographic coordinates to tile coordinates.
     */
    public Matrix getModelviewMatrix()
    {
        return this.modelview;
    }

    /**
     * Returns a Matrix mapping geographic coordinates to pixels in the off-screen tile. The reference location defines
     * the geographic coordinate origin.
     *
     * @param referenceLocation the geographic coordinate origin.
     *
     * @return Matrix mapping geographic coordinates to tile coordinates.
     *
     * @throws IllegalArgumentException if the reference location is null.
     */
    public Matrix getModelviewMatrix(LatLon referenceLocation)
    {
        if (referenceLocation == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.modelview.multiply(
            Matrix.fromTranslation(referenceLocation.getLongitude().degrees, referenceLocation.getLatitude().degrees,
                0));
    }

    /**
     * Returns the collection of pick candidates associated with this context. This collection provides a registration
     * of picked objects drawn into the surface tile.
     *
     * @return this context's pick candidates.
     */
    public Collection<PickedObject> getPickCandidates()
    {
        return this.pickCandidates;
    }

    /**
     * Adds the specified picked object to this context's the collection of pick candidates. This collection can be
     * accessed by calling {@link #getPickCandidates()}.
     *
     * @param pickedObject the object to add.
     *
     * @throws IllegalArgumentException if the object is null.
     */
    public void addPickCandidate(PickedObject pickedObject)
    {
        if (null == pickedObject)
        {
            String msg = Logging.getMessage("nullValue.PickedObject");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.pickCandidates.add(pickedObject);
    }
}
