/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.pick;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Range;

import javax.media.opengl.*;
import java.awt.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: PickSupport.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public class PickSupport
{
    /**
     * The picked objects currently registered with this PickSupport, represented as a map of color codes to picked
     * objects. This maps provides constant time access to a picked object when its color code is known.
     */
    protected Map<Integer, PickedObject> pickableObjects = new HashMap<Integer, PickedObject>();
    /**
     * The picked object color code ranges currently registered with this PickSupport, represented as a map of color
     * code ranges to picked object factories. PickSupport uses these factories to delay PickedObject construction until
     * a matching pick color is identified.
     */
    protected Map<Range, PickedObjectFactory> pickableObjectRanges = new HashMap<Range, PickedObjectFactory>();
    /**
     * Indicates the minimum and maximum color code associated with the picked objects in the pickableObjects map.
     * Initially <code>null</code>, indicating that the minimum and maximum color codes are unknown.
     */
    protected int[] minAndMaxColorCodes;

    public void clearPickList()
    {
        this.getPickableObjects().clear();
        this.getPickableObjectRanges().clear();
        this.minAndMaxColorCodes = null; // Reset the min and max color codes.
    }

    public void addPickableObject(int colorCode, Object o, Position position, boolean isTerrain)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o, position, isTerrain));
        this.adjustExtremeColorCodes(colorCode);
    }

    public void addPickableObject(int colorCode, Object o, Position position)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o, position, false));
        this.adjustExtremeColorCodes(colorCode);
    }

    public void addPickableObject(int colorCode, Object o)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o));
        this.adjustExtremeColorCodes(colorCode);
    }

    public void addPickableObject(PickedObject po)
    {
        this.getPickableObjects().put(po.getColorCode(), po);
        this.adjustExtremeColorCodes(po.getColorCode());
    }

    /**
     * Associates a collection of picked objects with a range of pick colors. PickSupport uses the factory to delay
     * PickedObject construction until a matching pick color is identified by getTopObject or resolvePick. This
     * eliminates the overhead of creating and managing a large collection of PickedObject instances when only a few may
     * actually be picked.
     *
     * @param colorCode the first color code associated with the range of sequential color codes.
     * @param count     the number of sequential color codes in the range of sequential color codes.
     * @param factory   the PickedObjectFactory to use when creating a PickedObject for a color in the specified range.
     */
    public void addPickableObjectRange(int colorCode, int count, PickedObjectFactory factory)
    {
        Range range = new Range(colorCode, count);
        this.pickableObjectRanges.put(range, factory);
        this.adjustExtremeColorCodes(colorCode);
        this.adjustExtremeColorCodes(colorCode + count - 1); // max code is last element in sequence of count codes
    }

    public PickedObject getTopObject(DrawContext dc, Point pickPoint)
    {
        if (!this.hasPickableObjects()) // avoid reading the current GL color when no pickable objects are registered
            return null;

        int colorCode = this.getTopColor(dc, pickPoint);
        if (colorCode == 0) // getTopColor returns 0 if the pick point selects the clear color.
            return null;

        PickedObject pickedObject = this.lookupPickableObject(colorCode);
        if (pickedObject == null)
            return null;

        return pickedObject;
    }

    /**
     * Adds picked object registered with this PickSupport that are drawn at the specified pick point or intersect the
     * draw context's pick rectangle to the draw context's list of picked objects. This clears any registered picked
     * objects upon returning.
     * <p/>
     * If this pick point is <code>null</code>, this ignores the pick point and does not attempt to determine which
     * picked objects are drawn there. If the draw context's pick rectangle is <code>null</code>, this ignores the pick
     * rectangle and does not attempt to determine which picked objects intersect it. This does nothing if no picked
     * objects are currently registered with this PickSupport.
     *
     * @param dc        the draw context which receives the picked object.
     * @param pickPoint the point in AWT screen coordinates.
     * @param layer     the layer associated with the picked object.
     *
     * @return the picked object added to the draw context, or <code>null</code> if no picked object is drawn at the
     *         specified point.
     */
    public PickedObject resolvePick(DrawContext dc, Point pickPoint, Layer layer)
    {
        if (!this.hasPickableObjects()) // avoid reading the current GL color when no pickable objects are registered
            return null;

        PickedObject po = null;

        // Resolve the object at the pick point, if any, adding it to the draw context's list of objects at the pick
        // point. If any object is at the pick point we return it. Note that the pick point can be null when the pick
        // rectangle is specified but the pick point is not.
        if (pickPoint != null)
            po = this.doResolvePick(dc, pickPoint, layer);

        // Resolve the objects in the pick rectangle, if any, adding them to the draw context's list of objects
        // intersecting the pick rectangle. Note that the pick rectangle can be null when the pick point is specified
        // but the pick rectangle is not.
        if (dc.getPickRectangle() != null && !dc.getPickRectangle().isEmpty())
            this.doResolvePick(dc, dc.getPickRectangle(), layer);

        this.clearPickList();

        return po;
    }

    /**
     * Adds a picked object registered with this PickSupport that is drawn at the specified point in AWT screen
     * coordinates (if one exists) to the draw context's list of picked objects.
     *
     * @param dc        the draw context which receives the picked object.
     * @param pickPoint the point in AWT screen coordinates.
     * @param layer     the layer associated with the picked object.
     *
     * @return the picked object added to the draw context, or <code>null</code> if no picked object is drawn at the
     *         specified point.
     */
    protected PickedObject doResolvePick(DrawContext dc, Point pickPoint, Layer layer)
    {
        PickedObject pickedObject = this.getTopObject(dc, pickPoint);
        if (pickedObject != null)
        {
            if (layer != null)
                pickedObject.setParentLayer(layer);

            dc.addPickedObject(pickedObject);
        }

        return pickedObject;
    }

    /**
     * Adds all picked objects that are registered with this PickSupport and intersect the specified rectangle in AWT
     * screen coordinates (if any) to the draw context's list of picked objects.
     *
     * @param dc       the draw context which receives the picked objects.
     * @param pickRect the rectangle in AWT screen coordinates.
     * @param layer    the layer associated with the picked objects.
     */
    protected void doResolvePick(DrawContext dc, Rectangle pickRect, Layer layer)
    {
        // Get the unique pick colors in the specified screen rectangle. Use the minimum and maximum color codes to cull
        // the number of colors that the draw context must consider with identifying the unique pick colors in the
        // specified rectangle.
        int[] colorCodes = dc.getPickColorsInRectangle(pickRect, this.minAndMaxColorCodes);
        if (colorCodes == null || colorCodes.length == 0)
            return;

        // Lookup the pickable object (if any) for each unique color code appearing in the pick rectangle. Each picked
        // object that corresponds to a picked color is added to the draw context.
        for (int colorCode : colorCodes)
        {
            if (colorCode == 0) // This should never happen, but we check anyway.
                continue;

            PickedObject po = this.lookupPickableObject(colorCode);
            if (po == null)
                continue;

            if (layer != null)
                po.setParentLayer(layer);

            dc.addObjectInPickRectangle(po);
        }
    }

    /**
     * Returns the framebuffer RGB color for a point in AWT screen coordinates, formatted as a pick color code. The red,
     * green, and blue components are each stored as an 8-bit unsigned integer, and packed into bits 0-23 of the
     * returned integer as follows: bits 16-23 are red, bits 8-15 are green, and bits 0-7 are blue. This format is
     * consistent with the RGB integers used to create the pick colors.
     * <p/>
     * This returns 0 if the point is <code>null</code>, if the point contains the clear color, or if the point is
     * outside the draw context's drawable area.
     *
     * @param dc        the draw context to return a color for.
     * @param pickPoint the point to return a color for, in AWT screen coordinates.
     *
     * @return the RGB color corresponding to the specified point.
     */
    public int getTopColor(DrawContext dc, Point pickPoint)
    {
        // This method's implementation has been moved into DrawContext.getPickColor in order to consolidate this logic
        // into one place. We've left this method here to avoid removing an interface that applications may rely on.
        return pickPoint != null ? dc.getPickColorAtPoint(pickPoint) : 0;
    }

    public void beginPicking(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT);

        gl.glDisable(GL.GL_DITHER);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL2.GL_FOG);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_TEXTURE_2D);

        if (dc.isDeepPickingEnabled())
            gl.glDisable(GL.GL_DEPTH_TEST);
    }

    public void endPicking(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glPopAttrib();

        // Some nvidia Quadro cards have a bug in which the current color is not restored. Restore it to the
        // default here.
        gl.glColor3ub((byte) 255, (byte) 255, (byte) 255);
    }

    protected Map<Integer, PickedObject> getPickableObjects()
    {
        return this.pickableObjects;
    }

    protected Map<Range, PickedObjectFactory> getPickableObjectRanges()
    {
        return this.pickableObjectRanges;
    }

    protected boolean hasPickableObjects()
    {
        return this.getPickableObjects().size() > 0 || this.getPickableObjectRanges().size() > 0;
    }

    protected PickedObject lookupPickableObject(int colorCode)
    {
        // Try looking up the color code in the pickable object map.
        PickedObject po = this.getPickableObjects().get(colorCode);
        if (po != null)
            return po;

        // Try matching the color code to one of the pickable object ranges.
        for (Map.Entry<Range, PickedObjectFactory> entry : this.getPickableObjectRanges().entrySet())
        {
            Range range = entry.getKey();
            PickedObjectFactory factory = entry.getValue();

            if (range.contains(colorCode) && factory != null)
                return factory.createPickedObject(colorCode);
        }

        return null;
    }

    /**
     * Adjust this PickSupport's minimum and maximum color codes by decreasing and increasing each extreme,
     * respectively, according to the specified code. If the minimum and maximum color codes are unknown, both the
     * minimum and maximum color codes are set to the specified code.
     *
     * @param colorCode the code used to adjust the current min and max codes.
     */
    protected void adjustExtremeColorCodes(int colorCode)
    {
        if (this.minAndMaxColorCodes == null)
            this.minAndMaxColorCodes = new int[] {colorCode, colorCode};
        else
        {
            if (this.minAndMaxColorCodes[0] > colorCode)
                this.minAndMaxColorCodes[0] = colorCode;
            if (this.minAndMaxColorCodes[1] < colorCode)
                this.minAndMaxColorCodes[1] = colorCode;
        }
    }

    /**
     * Indicates whether two picked objects refer to the same user object.
     *
     * @param a the first picked object.
     * @param b the second picked object.
     *
     * @return true if both objects are not null and they refer to the same user object, otherwise false.
     */
    public static boolean areSelectionsTheSame(PickedObject a, PickedObject b)
    {
        return a != null && b != null && a.getObject() == b.getObject();
    }
}
