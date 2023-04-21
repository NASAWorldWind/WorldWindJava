/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.*;
import java.io.File;

/**
 * @author dcollins
 * @version $Id: LicenseAgreement.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LicenseAgreement extends WWObjectImpl
{
    private final Object license;
    private final String licenseKey;

    public static final String LICENSE_KEY_CACHE_NAME = AVKey.DATA_CACHE_NAME;
    public static final String LICENSE_CONTENT_TYPE = "gov.nasa.worldwind.ContentType";
    public static final String DIALOG_PREFERRED_SIZE = "gov.nasa.worldwind.PreferredSize";
    public static final String DIALOG_TITLE = AVKey.TITLE;

    private static final String DEFAULT_LICENSE_KEY_CACHE_NAME = "license";
    private static final String DEFAULT_LICENSE_CONTENT_TYPE = "text/plain";
    private static final String DEFAULT_DIALOG_TITLE = "License Agreement";

    public static final String LICENSE_ACCEPTED = "gov.nasa.worldwind.LicenseAccepted";
    public static final String LICENSE_ACCEPTED_AND_INSTALLED = "gov.nasa.worldwind.LicenseAcceptedAndInstalled";
    public static final String LICENSE_DECLINED = "gov.nasa.worldwind.LicenseDeclined";

    public LicenseAgreement(Object license, String licenseKey)
    {
        this(license, licenseKey, null);
    }

    public LicenseAgreement(Object license, String licenseKey, AVList params)
    {
        if (license == null)
        {
            String message = "nullValue.licenseIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (licenseKey == null)
        {
            String message = "nullValue.licenseKeyIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);   
        }

        this.license = license;
        this.licenseKey = licenseKey;
        if (params != null)
            setValues(params);
    }

    public final Object getLicense()
    {
        return this.license;
    }

    public final String getLicenseKey()
    {
        return this.licenseKey;
    }

    public String checkForLicenseAgreement(Component parentComponent)
    {
        // License has already been accepted and installed.
        if (isLicenseInstalled())
            return LICENSE_ACCEPTED_AND_INSTALLED;

        // License is not installed - display license agreement.
        int result = displayLicenseAgreement(parentComponent);
        if (result == LicenseDialog.DECLINE_OPTION)
            return LICENSE_DECLINED;

        // Install the license key only if the user accepted,
        // and no problems occurred displaying the license. 
        if (result == LicenseDialog.ACCEPT_OPTION)
            installLicenseKey();

        return isLicenseInstalled() ? LICENSE_ACCEPTED_AND_INSTALLED : LICENSE_ACCEPTED;
    }

    public int displayLicenseAgreement(Component parentComponent)
    {
        String contentType = getStringValue(this, LICENSE_CONTENT_TYPE, DEFAULT_LICENSE_CONTENT_TYPE);
        Object dialogSize = getValue(DIALOG_PREFERRED_SIZE);
        String dialogTitle = getStringValue(this, DIALOG_TITLE, DEFAULT_DIALOG_TITLE);

        LicenseDialog dialog = new LicenseDialog(this.license);
        dialog.setContentType(contentType);
        dialog.setTitle(dialogTitle);
        if (dialogSize != null && dialogSize instanceof Dimension)
            dialog.setPreferredSize((Dimension) dialogSize);
        
        return dialog.showDialog(parentComponent);
    }

    public boolean isLicenseInstalled()
    {
        File keyFile = getLicenseKeyFile();
        return keyFile != null && keyFile.exists();
    }

    private void installLicenseKey()
    {
        File keyFile = getLicenseKeyFile();
        try
        {
            if (keyFile != null)
                keyFile.createNewFile();
        }
        catch (Exception e)
        {
            String message = "Exception while installing license key file";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    private File getLicenseKeyFile()
    {
        File keyFile = null;
        try
        {
            String cacheName = getStringValue(this, LICENSE_KEY_CACHE_NAME, DEFAULT_LICENSE_KEY_CACHE_NAME);
            String keyPath = WWIO.formPath(cacheName, this.licenseKey);
            keyFile = WorldWind.getDataFileStore().newFile(keyPath);
        }
        catch (Exception e)
        {
            String message = "Exception while searching license key file";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return keyFile;
    }
}
