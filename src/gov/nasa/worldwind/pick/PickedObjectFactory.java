/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.pick;

/**
 * PickedObjectFactory provides an interface for lazily creating PickedObject instances.
 * <p>
 * This interface is used by PickSupport to associate a collection of picked objects with a range of pick colors.
 * PickSupport uses this factory to delay PickedObject construction until a matching pick color is identified. This
 * eliminates the overhead of creating and managing a large collection of PickedObject instances when only a few may
 * actually be picked.
 *
 * @author dcollins
 * @version $Id: PickedObjectFactory.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public interface PickedObjectFactory
{
    /**
     * Create a picked object from the specified pick color code.
     *
     * @param colorCode the pick color code to associate with the picked object.
     *
     * @return the new picked object.
     */
    PickedObject createPickedObject(int colorCode);
}
