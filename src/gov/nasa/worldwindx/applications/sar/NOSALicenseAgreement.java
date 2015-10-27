/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

/**
 * @author dcollins
 * @version $Id: NOSALicenseAgreement.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NOSALicenseAgreement extends LicenseAgreement
{
    public NOSALicenseAgreement(String applicationKey)
    {
        super("worldwind-nosa-1.3.html", applicationKey, makeParams());
    }

    private static AVList makeParams()
    {
        AVList params = new AVListImpl();
        params.setValue(LICENSE_CONTENT_TYPE, "text/html");
        params.setValue(DIALOG_PREFERRED_SIZE, new java.awt.Dimension(700, 500));
        params.setValue(DIALOG_TITLE, "NASA OPEN SOURCE AGREEMENT VERSION 1.3");
        return params;
    }
}
