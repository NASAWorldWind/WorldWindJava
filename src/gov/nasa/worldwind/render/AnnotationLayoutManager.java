/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickSupport;

/**
 * @author dcollins
 * @version $Id: AnnotationLayoutManager.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface AnnotationLayoutManager
{
    // TODO: Create javadocus, including pictures, illustrating how annotation layouts work

    PickSupport getPickSupport();

    void setPickSupport(PickSupport pickSupport);

    java.awt.Dimension getPreferredSize(DrawContext dc, Iterable<? extends Annotation> annotations);

    void drawAnnotations(DrawContext dc, java.awt.Rectangle bounds,
        Iterable<? extends Annotation> annotations, double opacity, Position pickPosition);

    void beginDrawAnnotations(DrawContext dc, java.awt.Rectangle bounds);

    void endDrawAnnotations(DrawContext dc);
}
