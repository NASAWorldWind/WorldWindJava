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

import gov.nasa.worldwind.exception.WWRuntimeException;
import java.io.File;

/**
 * @author Lado Garakanidze
 * @version $Id: NativeLibraryLoader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NativeLibraryLoader {
    
    public static void loadLibrary(String folder, String libName) throws WWRuntimeException, IllegalArgumentException {
        if (WWUtil.isEmpty(libName)) {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            throw new IllegalArgumentException(message);
        }
        
        try {
            if (folder == null) {
                System.loadLibrary(libName);
            } else {
                Runtime.getRuntime().load(folder + File.separator + makeFullLibName(libName));
            }
        } catch (java.lang.UnsatisfiedLinkError ule) {
            String message = Logging.getMessage("generic.LibraryNotLoaded", libName, ule.getMessage());
            throw new WWRuntimeException(message);
        } catch (Throwable t) {
            String message = Logging.getMessage("generic.LibraryNotLoaded", libName, t.getMessage());
            throw new WWRuntimeException(message);
        }
    }
    
    protected static String makeFullLibName(String libName) {
        if (WWUtil.isEmpty(libName)) {
            return null;
        }
        
        if (gov.nasa.worldwind.Configuration.isWindowsOS()) {
            if (!libName.toLowerCase().endsWith(".dll")) {
                return libName + ".dll";
            }
        } else if (gov.nasa.worldwind.Configuration.isMacOS()) {
            if (!libName.toLowerCase().endsWith(".jnilib") && !libName.toLowerCase().startsWith("lib")) {
                return "lib" + libName + ".jnilib";
            }
        } else if (gov.nasa.worldwind.Configuration.isUnixOS()) // covers Solaris and Linux
        {
            if (!libName.toLowerCase().endsWith(".so") && !libName.toLowerCase().startsWith("lib")) {
                return "lib" + libName + ".so";
            }
        }
        return libName;
    }
}
