/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

/**
 * This program returns the version and implementation information for the Java Bindings for OpenGL (R) implementation
 * found in the CLASSPATH.  This information is also found in the manifest for jogl-all.jar, and this program uses the
 * java.lang.Package class to retrieve it programmatically.
 *
 * @version $Id: JOGLVersionInfo.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class JOGLVersionInfo
{
    private static JOGLVersionInfo svi = new JOGLVersionInfo();
    private Package p;

    private JOGLVersionInfo()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        this.p = pkgInfo(classLoader, "javax.media.opengl", "GL");
    }

    private static Package pkgInfo(ClassLoader classLoader, String pkgName, String className)
    {
        Package p = null;

        try
        {
            classLoader.loadClass(pkgName + "." + className);

            // TODO: message logging
            p = Package.getPackage(pkgName);
            if (p == null)
                System.out.println("WARNING: Package.getPackage(" + pkgName + ") is null");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Unable to load " + pkgName);
        }

        return p;
    }

    public static Package getPackage()
    {
        return svi.p;
    }

    public static boolean isCompatibleWith(String version)
    {
        return svi.p != null && svi.p.isCompatibleWith(version);
    }

    public static String getSpecificationTitle()
    {
        return svi.p != null ? svi.p.getSpecificationTitle() : null;
    }

    public static String getSpecificationVendor()
    {
        return svi.p != null ? svi.p.getSpecificationVendor() : null;
    }

    public static String getSpecificationVersion()
    {
        return svi.p != null ? svi.p.getSpecificationVersion() : null;
    }

    public static String getImplementationTitle()
    {
        return svi.p != null ? svi.p.getImplementationTitle() : null;
    }

    public static String getImplementationVersion()
    {
        return svi.p != null ? svi.p.getImplementationVersion() : null;
    }

    public static void main(String[] args)
    {
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
