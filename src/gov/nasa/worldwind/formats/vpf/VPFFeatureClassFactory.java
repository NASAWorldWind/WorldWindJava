/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFFeatureClassFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface VPFFeatureClassFactory
{
    public VPFFeatureClass createFromSchema(VPFCoverage coverage, VPFFeatureClassSchema schema);
}
