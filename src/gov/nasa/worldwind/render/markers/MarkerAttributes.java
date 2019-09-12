/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author tag
 * @version $Id: MarkerAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MarkerAttributes
{
    Material getMaterial();

    void setMaterial(Material material);

    double getOpacity();

    void setOpacity(double opacity);

    void apply(DrawContext dc);

    double getMarkerPixels();

    void setMarkerPixels(double markerPixels);

    double getMinMarkerSize();

    void setMinMarkerSize(double minMarkerSize);

    MarkerShape getShape(DrawContext dc);

    void setShapeType(String shapeType);

    String getShapeType();

    Material getHeadingMaterial();

    void setHeadingMaterial(Material headingMaterial);

    double getHeadingScale();

    void setHeadingScale(double headingScale);

    double getMaxMarkerSize();

    void setMaxMarkerSize(double markerSize);
}
