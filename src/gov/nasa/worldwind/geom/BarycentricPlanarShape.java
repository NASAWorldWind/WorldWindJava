/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

/**
 * @author tag
 * @version $Id: BarycentricPlanarShape.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface BarycentricPlanarShape
{
    double[] getBarycentricCoords(Vec4 p);

    Vec4 getPoint(double[] w);

    @SuppressWarnings({"UnnecessaryLocalVariable"})
    double[] getBilinearCoords(double alpha, double beta);
}
