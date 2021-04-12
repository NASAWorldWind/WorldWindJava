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
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.Configuration;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: BrowserOpener.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BrowserOpener
{
    public static void browse(URL url) throws Exception
    {
        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String urlString = url.toString();
            if (Configuration.isMacOS())
                browseMacOS(urlString);
            else if (Configuration.isWindowsOS())
                browseWindows(urlString);
            else if (Configuration.isUnixOS() || Configuration.isLinuxOS())
                browseUnix(urlString);
        }
        catch (Exception e)
        {
            throw new Exception(String.format("Cannot browse URL %s", url), e);
        }
    }

    private static void browseMacOS(String urlString) throws Exception
    {
        Class<?> fileManager = Class.forName("com.apple.eio.FileManager");
        Method openURL = fileManager.getDeclaredMethod("openURL", String.class);
        openURL.invoke(null, urlString);
    }

    private static void browseWindows(String urlString) throws Exception
    {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + urlString);
    }

    private static void browseUnix(String urlString) throws Exception
    {
        String browser = null;

        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
        for (String curBrowser : browsers)
            if (Runtime.getRuntime().exec(new String[] {"which", curBrowser}).waitFor() == 0)
                browser = curBrowser;

        if (browser == null)
            throw new Exception("Cannot find browser");

        Runtime.getRuntime().exec(new String[] {browser, urlString});
    }
}