/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
