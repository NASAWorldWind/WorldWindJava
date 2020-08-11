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

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.net.URL;
import java.net.URI;
import java.io.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: LicenseDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LicenseDialog
{
    private final Object license;
    private String contentType;
    private Dimension preferredSize;
    private String title;

    public static final int ACCEPT_OPTION = 1;
    public static final int DECLINE_OPTION = 2;
    public static final int ERROR_OPTION = 3;

    public LicenseDialog(Object license)
    {
        if (license == null)
        {
            String message = "nullValue.licenseIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.license = license;
    }

    public final Object getLicense()
    {
        return this.license;
    }

    public String getContentType()
    {
        return this.contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public Dimension getPreferredSize()
    {
        return this.preferredSize;
    }

    public void setPreferredSize(Dimension preferredSize)
    {
        this.preferredSize = preferredSize;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int showDialog(Component parentComponent)
    {
        int result;
        Object licenseComponent = makeLicenseComponent();
        if (licenseComponent != null)
        {
            result = showLicenseDialog(parentComponent, licenseComponent, this.title);
        }
        else
        {
            String message = missingLicenseMessage();
            showMissingLicenseDialog(parentComponent, message, null);
            result = ERROR_OPTION;
        }
        return result;
    }

    private static int showLicenseDialog(Component parentComponent, Object licenseComponent, String title)
    {
        int result;
        try
        {
            if (licenseComponent instanceof Component)
                licenseComponent = new JScrollPane((Component) licenseComponent);
            
            result = JOptionPane.showOptionDialog(
                parentComponent, // parentComponent
                licenseComponent, // message
                title, // title
                JOptionPane.YES_NO_OPTION, // optionType
                JOptionPane.PLAIN_MESSAGE, // messageType
                null, // icon
                new Object[] {"Accept", "Decline"}, // options
                "Accept" //initialValue
                );
        }
        catch (Exception e)
        {
            String message = "Exception while displaying license dialog";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return ERROR_OPTION;
        }
        return (result == JOptionPane.YES_OPTION) ? ACCEPT_OPTION : DECLINE_OPTION;
    }

    private static void showMissingLicenseDialog(Component parentComponent, String message, String title)
    {
        try
        {
            JOptionPane.showOptionDialog(
                parentComponent, // parentComponent
                message, // message
                title, // title
                JOptionPane.OK_OPTION, // optionType
                JOptionPane.ERROR_MESSAGE, // messageType
                null, // icon
                new Object[] {"OK"}, // options
                "OK" //initialValue
                );
        }
        catch (Exception e)
        {
            String msg = "Exception while displaying missing license dialog";
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
        }
    }

    private String missingLicenseMessage()
    {
        return "License Agreement is missing.";
    }

    private Object makeLicenseComponent()
    {
        JEditorPane editor = null;
        try
        {
            if (this.license != null)
            {
                if (this.license instanceof URL)
                {
                    Logging.logger().fine("Fetching license (URL): " + this.license);
                    editor = new JEditorPane();
                    if (this.contentType != null)
                        editor.setContentType(this.contentType);
                    editor.setPage((URL) this.license);
                }
                else if (this.license instanceof File)
                {
                    Logging.logger().fine("Fetching license (File): " + this.license);
                    URI uri = ((File) this.license).toURI();
                    URL url = uri.toURL();
                    editor = new JEditorPane();
                    if (this.contentType != null)
                        editor.setContentType(this.contentType);
                    editor.setPage(url);
                }
                else
                {
                    Logging.logger().fine("Fetching license (String): " + this.license.toString());
                    URL url = getClass().getResource(this.license.toString());
                    editor = new JEditorPane();
                    if (this.contentType != null)
                        editor.setContentType(this.contentType);
                    editor.setPage(url);
                }
            }

            if (editor != null)
            {
                editor.setEditable(false);
                if (this.preferredSize != null)
                    editor.setPreferredSize(this.preferredSize);
            }
        }
        catch (Exception e)
        {
            String message = "Exception while fetching license content";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            editor = null;
        }
        return editor;
    }
}
