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

/**
 * This program returns the version and implementation information for the Java Bindings for OpenGL (R) implementation
 * found in the CLASSPATH. This information is also found in the manifest for jogl-all.jar, and this program uses the
 * java.lang.Package class to retrieve it programmatically.
 *
 * @version $Id: JOGLVersionInfo.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class JOGLVersionInfo {

    private static final JOGLVersionInfo svi = new JOGLVersionInfo();
    private final Package p;

    private JOGLVersionInfo() {
        ClassLoader classLoader = getClass().getClassLoader();
        this.p = pkgInfo(classLoader, "com.jogamp.opengl", "GL");
    }

    private static Package pkgInfo(ClassLoader classLoader, String pkgName, String className) {
        Package p = null;

        try {
            classLoader.loadClass(pkgName + "." + className);

            // TODO: message logging
            p = classLoader.getDefinedPackage(pkgName);
            if (p == null) {
                System.out.println("WARNING: Package.getPackage(" + pkgName + ") is null");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load " + pkgName);
        }

        return p;
    }

    public static Package getPackage() {
        return svi.p;
    }

    public static boolean isCompatibleWith(String version) {
        return svi.p != null && svi.p.isCompatibleWith(version);
    }

    public static String getSpecificationTitle() {
        return svi.p != null ? svi.p.getSpecificationTitle() : null;
    }

    public static String getSpecificationVendor() {
        return svi.p != null ? svi.p.getSpecificationVendor() : null;
    }

    public static String getSpecificationVersion() {
        return svi.p != null ? svi.p.getSpecificationVersion() : null;
    }

    public static String getImplementationTitle() {
        return svi.p != null ? svi.p.getImplementationTitle() : null;
    }

    public static String getImplementationVersion() {
        return svi.p != null ? svi.p.getImplementationVersion() : null;
    }

    public static void main(String[] args) {
        System.out.println(JOGLVersionInfo.getPackage());
        System.out.println(JOGLVersionInfo.getSpecificationTitle());
        System.out.println(JOGLVersionInfo.getSpecificationVendor());
        System.out.println(JOGLVersionInfo.getSpecificationVersion());
        System.out.println(JOGLVersionInfo.getImplementationTitle());
        System.out.println(JOGLVersionInfo.getImplementationVersion());
        System.out.println(JOGLVersionInfo.isCompatibleWith("1.0"));
        System.out.println(JOGLVersionInfo.isCompatibleWith("1.1.1"));
        System.out.println(JOGLVersionInfo.isCompatibleWith("1.2.1"));
        System.out.println(
                JOGLVersionInfo.getImplementationVersion().compareToIgnoreCase("1.1.1-pre-20070511-02:12:11"));
        System.out.println(
                JOGLVersionInfo.getImplementationVersion().compareToIgnoreCase("1.1.1-pre-20070512-02:12:11"));
        System.out.println(JOGLVersionInfo.getImplementationVersion().compareToIgnoreCase("1.1.1"));
    }
}
