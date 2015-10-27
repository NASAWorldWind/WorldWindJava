/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: NIMAUtils.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NIMAUtils
{
    public static boolean isReferenceLibrary(String libraryName)
    {
        return libraryName.equalsIgnoreCase(NIMAConstants.REFERENCE_LIBRARY);
    }

    public static boolean isDatabaseReferenceCoverage(String coverageName)
    {
        return coverageName.equalsIgnoreCase(NIMAConstants.DATABASE_REFERENCE_COVERAGE);
    }
}
