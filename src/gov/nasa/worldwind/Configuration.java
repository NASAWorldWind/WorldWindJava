/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import com.jogamp.opengl.*;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * This class manages the initial WorldWind configuration. It reads WorldWind configuration files and registers their
 * contents. Configurations files contain the names of classes to create at run-time, the initial model definition,
 * including the globe, elevation model and layers, and various control quantities such as cache sizes and data
 * retrieval timeouts.
 * <p>
 * The Configuration class is a singleton, but its instance is not exposed publicly. It is addressed only via static
 * methods of the class. It is constructed upon first use of any of its static methods.
 * <p>
 * When the Configuration class is first instantiated it reads the XML document <code>config/worldwind.xml</code> and
 * registers all the information there. The information can subsequently be retrieved via the class' various
 * <code>getValue</code> methods. Many WorldWind start-up objects query this information to determine the classes to
 * create. For example, the first WorldWind object created by an application is typically a {@link
 * gov.nasa.worldwind.awt.WorldWindowGLCanvas}. During construction that class causes WorldWind's internal classes to
 * be constructed, using the names of those classes drawn from the Configuration singleton, this class.
 * <p>
 * The default WorldWind configuration document is <code>config/worldwind.xml</code>. This can be changed by setting
 * the Java property <code>gov.nasa.worldwind.config.file</code> to a different file name or a valid URL prior to
 * creating any WorldWind object or invoking any static methods of WorldWind classes, including the Configuration
 * class. When an application specifies a different configuration location it typically does so in its main method prior
 * to using WorldWind. If a file is specified its location must be on the classpath. (The contents of application and
 * WorldWind jar files are typically on the classpath, in which case the configuration file may be in the jar file.)
 * <p>
 * Additionally, an application may set another Java property, <code>gov.nasa.worldwind.app.config.document</code>, to a
 * file name or URL whose contents contain configuration values to override those of the primary configuration document.
 * WorldWind overrides only those values in this application document, it leaves all others to the value specified in
 * the primary document. Applications usually specify an override document in order to specify the initial layers in the
 * model.
 * <p>
 * See <code>config/worldwind.xml</code> for documentation on setting configuration values.
 * <p>
 * Configuration values can also be set programatically via {@link Configuration#setValue(String, Object)}, but they are
 * not retroactive so affect only Configuration queries made subsequent to setting the value.
 * <p>
 * <em>Note:</em> Prior to September of 2009, configuration properties were read from the file
 * <code>config/worldwind.properties</code>. An alternate file could be specified via the
 * <code>gov.nasa.worldwind.config.file</code> Java property. These mechanisms remain available but are deprecated.
 * WorldWind no longer contains a <code>worldwind.properties</code> file. If <code>worldwind.properties</code> or its
 * replacement as specified through the Java property exists at run-time and can be found via the classpath,
 * configuration values specified by that mechanism are given precedence over values specified by the new mechanism.
 *
 * @author Tom Gaskins
 * @version $Id: Configuration.java 1739 2013-12-04 03:38:19Z dcollins $
 */
public class Configuration // Singleton
{
    public static final String DEFAULT_LOGGER_NAME = "gov.nasa.worldwind";

    private static final String CONFIG_PROPERTIES_FILE_NAME = "config/worldwind.properties";
    private static final String CONFIG_FILE_PROPERTY_KEY = "gov.nasa.worldwind.config.file";

    private static final String CONFIG_WW_DOCUMENT_KEY = "gov.nasa.worldwind.config.document";
    private static final String CONFIG_WW_DOCUMENT_NAME = "config/worldwind.xml";

    private static final String CONFIG_APP_DOCUMENT_KEY = "gov.nasa.worldwind.app.config.document";

    private static Configuration ourInstance = new Configuration();

    private static Configuration getInstance()
    {
        return ourInstance;
    }

    private final Properties properties;
    private final ArrayList<Document> configDocs = new ArrayList<Document>();

    /** Private constructor invoked only internally. */
    private Configuration()
    {
        this.properties = initializeDefaults();

        // Load the app's configuration if there is one
        try
        {
            String appConfigLocation = System.getProperty(CONFIG_APP_DOCUMENT_KEY);
            if (appConfigLocation != null)
                this.loadConfigDoc(System.getProperty(CONFIG_APP_DOCUMENT_KEY)); // Load app's config first
        }
        catch (Exception e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.WARNING, "Configuration.ConfigNotFound",
                System.getProperty(CONFIG_APP_DOCUMENT_KEY));
            // Don't stop if the app config file can't be found or parsed
        }

        try
        {
            // Load the default configuration
            this.loadConfigDoc(System.getProperty(CONFIG_WW_DOCUMENT_KEY, CONFIG_WW_DOCUMENT_NAME));

            // Load config properties, ensuring that the app's config takes precedence over wwj's
            for (int i = this.configDocs.size() - 1; i >= 0; i--)
            {
                this.loadConfigProperties(this.configDocs.get(i));
            }
        }
        catch (Exception e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.WARNING, "Configuration.ConfigNotFound",
                System.getProperty(CONFIG_WW_DOCUMENT_KEY));
        }

        // To support old-style configuration, read an existing config properties file and give the properties
        // specified there precedence.
        this.initializeCustom();
    }

    private void loadConfigDoc(String configLocation)
    {
        if (!WWUtil.isEmpty(configLocation))
        {
            Document doc = WWXML.openDocument(configLocation);
            if (doc != null)
            {
                this.configDocs.add(doc);
//                this.loadConfigProperties(doc);
            }
        }
    }

    private void insertConfigDoc(String configLocation)
    {
        if (!WWUtil.isEmpty(configLocation))
        {
            Document doc = WWXML.openDocument(configLocation);
            if (doc != null)
            {
                this.configDocs.add(0, doc);
                this.loadConfigProperties(doc);
            }
        }
    }

    private void loadConfigProperties(Document doc)
    {
        try
        {
            XPath xpath = WWXML.makeXPath();

            NodeList nodes = (NodeList) xpath.evaluate("/WorldWindConfiguration/Property", doc, XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return;

            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                String prop = xpath.evaluate("@name", node);
                String value = xpath.evaluate("@value", node);
                if (WWUtil.isEmpty(prop))// || WWUtil.isEmpty(value))
                    continue;

                this.properties.setProperty(prop, value);
            }
        }
        catch (XPathExpressionException e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.WARNING, "XML.ParserConfigurationException");
        }
    }

    private Properties initializeDefaults()
    {
        Properties defaults = new Properties();
        java.util.TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
        if (tz != null)
            defaults.setProperty(AVKey.INITIAL_LONGITUDE,
                Double.toString(
                    Angle.fromDegrees(180.0 * tz.getOffset(System.currentTimeMillis()) / (12.0 * 3.6e6)).degrees));
        return defaults;
    }

    private void initializeCustom()
    {
        // IMPORTANT NOTE: Always use the single argument version of Logging.logger in this method because the non-arg
        // method assumes an instance of Configuration already exists.

        String configFileName = System.getProperty(CONFIG_FILE_PROPERTY_KEY, CONFIG_PROPERTIES_FILE_NAME);
        try
        {
            java.io.InputStream propsStream = null;
            File file = new File(configFileName);
            if (file.exists())
            {
                try
                {
                    propsStream = new FileInputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    Logging.logger(DEFAULT_LOGGER_NAME).log(Level.FINEST, "Configuration.LocalConfigFileNotFound",
                        configFileName);
                }
            }

            if (propsStream == null)
            {
                propsStream = this.getClass().getResourceAsStream("/" + configFileName);
            }

            if (propsStream != null)
                this.properties.load(propsStream);
        }
        // Use a named logger in all the catch statements below to prevent Logger from calling back into
        // Configuration when this Configuration instance is not yet fully instantiated.
        catch (IOException e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.SEVERE, "Configuration.ExceptionReadingPropsFile", e);
        }
    }

    public static void insertConfigurationDocument(String fileName)
    {
        getInstance().insertConfigDoc(fileName);
    }

    /**
     * Return as a string the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist.
     */
    public static synchronized String getStringValue(String key, String defaultValue)
    {
        String v = getStringValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as a string the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist.
     */
    public static synchronized String getStringValue(String key)
    {
        Object o = getInstance().properties.getProperty(key);
        return o != null ? o.toString() : null;
    }

    /**
     * Return as an Integer the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not an
     *         Integer or string representation of an Integer.
     */
    public static synchronized Integer getIntegerValue(String key, Integer defaultValue)
    {
        Integer v = getIntegerValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Integer the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not an Integer or string
     *         representation of an Integer.
     */
    public static synchronized Integer getIntegerValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    /**
     * Return as an Long the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not a
     *         Long or string representation of a Long.
     */
    public static synchronized Long getLongValue(String key, Long defaultValue)
    {
        Long v = getLongValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Long the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not a Long or string
     *         representation of a Long.
     */
    public static synchronized Long getLongValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Long.parseLong(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    /**
     * Return as an Double the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not an
     *         Double or string representation of an Double.
     */
    public static synchronized Double getDoubleValue(String key, Double defaultValue)
    {
        Double v = getDoubleValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Double the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not an Double or string
     *         representation of an Double.
     */
    public static synchronized Double getDoubleValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    /**
     * Return as a Boolean the value associated with a specified key.
     * <p>
     * Valid values for true are '1' or anything that starts with 't' or 'T'. ie. 'true', 'True', 't' Valid values for
     * false are '0' or anything that starts with 'f' or 'F'. ie. 'false', 'False', 'f'
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not a
     *         Boolean or string representation of an Boolean.
     */
    public static synchronized Boolean getBooleanValue(String key, Boolean defaultValue)
    {
        Boolean v = getBooleanValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as a Boolean the value associated with a specified key.
     * <p>
     * Valid values for true are '1' or anything that starts with 't' or 'T'. ie. 'true', 'True', 't' Valid values for
     * false are '0' or anything that starts with 'f' or 'F'. ie. 'false', 'False', 'f'
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not a Boolean or string
     *         representation of an Boolean.
     */
    public static synchronized Boolean getBooleanValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        if (v.trim().toUpperCase().startsWith("T") || v.trim().equals("1"))
        {
            return true;
        }
        else if (v.trim().toUpperCase().startsWith("F") || v.trim().equals("0"))
        {
            return false;
        }
        else
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    /**
     * Determines whether a key exists in the configuration.
     *
     * @param key the key of interest.
     *
     * @return true if the key exists, otherwise false.
     */
    public static synchronized boolean hasKey(String key)
    {
        return getInstance().properties.contains(key);
    }

    /**
     * Removes a key and its value from the configuration if the configuration contains the key.
     *
     * @param key the key of interest.
     */
    public static synchronized void removeKey(String key)
    {
        getInstance().properties.remove(key);
    }

    /**
     * Adds a key and value to the configuration, or changes the value associated with the key if the key is already in
     * the configuration.
     *
     * @param key   the key to set.
     * @param value the value to associate with the key.
     */
    public static synchronized void setValue(String key, Object value)
    {
        getInstance().properties.put(key, value.toString());
    }

    // OS, user, and run-time specific system properties. //

    /**
     * Returns the path to the application's current working directory.
     *
     * @return the absolute path to the application's current working directory.
     */
    public static String getCurrentWorkingDirectory()
    {
        String dir = System.getProperty("user.dir");
        return (dir != null) ? dir : ".";
    }

    /**
     * Returns the path to the application user's home directory.
     *
     * @return the absolute path to the application user's home directory.
     */
    public static String getUserHomeDirectory()
    {
        String dir = System.getProperty("user.home");
        return (dir != null) ? dir : ".";
    }

    /**
     * Returns the path to the operating system's temp directory.
     *
     * @return the absolute path to the operating system's temporary directory.
     */
    public static String getSystemTempDirectory()
    {
        String dir = System.getProperty("java.io.tmpdir");
        return (dir != null) ? dir : ".";
    }

    /**
     * Returns the path to the current user's application data directory. The path returned depends on the operating
     * system on which the Java Virtual Machine is running. The following table provides the path for all supported
     * operating systems:
     * <table><caption style="font-weight: bold;">Mapping</caption>
     * <tr><th>Operating System</th><th>Path</th></tr> <tr><td>Mac OS X</td><td>~/Library/Application
     * Support</td></tr> <tr><td>Windows</td><td>~\\Application Data</td></tr> <tr><td>Linux, Unix,
     * Solaris</td><td>~/</td></tr> </table>
     *
     * @return the absolute path to the current user's application data directory.
     */
    public static String getCurrentUserAppDataDirectory()
    {
        if (isMacOS())
        {
            // Return a path that Mac OS X has designated for app-specific data and support files. See the following URL
            // for details:
            // http://developer.apple.com/library/mac/#documentation/FileManagement/Conceptual/FileSystemProgrammingGUide/MacOSXDirectories/MacOSXDirectories.html#//apple_ref/doc/uid/TP40010672-CH10-SW1
            return getUserHomeDirectory() + "/Library/Application Support";
        }
        else if (isWindowsOS())
        {
            return getUserHomeDirectory() + "\\Application Data";
        }
        else if (isLinuxOS() || isUnixOS() || isSolarisOS())
        {
            return getUserHomeDirectory();
        }
        else
        {
            String msg = Logging.getMessage("generic.UnknownOperatingSystem");
            Logging.logger().fine(msg);
            return null;
        }
    }

    /**
     * Determines whether the operating system is a Mac operating system.
     *
     * @return true if the operating system is a Mac operating system, otherwise false.
     */
    public static boolean isMacOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("mac");
    }

    /**
     * Determines whether the operating system is Windows operating system.
     *
     * @return true if the operating system is a Windows operating system, otherwise false.
     */
    public static boolean isWindowsOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }

    /**
     * Determines whether the operating system is Windows XP operating system.
     *
     * @return true if the operating system is a Windows XP operating system, otherwise false.
     */
    public static boolean isWindowsXPOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows") && osName.contains("xp");
    }

    /**
     * Determines whether the operating system is Windows Vista operating system.
     *
     * @return true if the operating system is a Windows Vista operating system, otherwise false.
     */
    public static boolean isWindowsVistaOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows") && osName.contains("vista");
    }

    /**
     * Determines whether the operating system is Windows 7 operating system.
     *
     * @return true if the operating system is a Windows Vista operating system, otherwise false.
     */
    public static boolean isWindows7OS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows") && osName.contains("7");
    }

    /**
     * Determines whether the operating system is Linux operating system.
     *
     * @return true if the operating system is a Linux operating system, otherwise false.
     */
    public static boolean isLinuxOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("linux");
    }

    /**
     * Determines whether the operating system is Unix operating system.
     *
     * @return true if the operating system is a Unix operating system, otherwise false.
     */
    public static boolean isUnixOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && (osName.toLowerCase().contains("linux") || osName.toLowerCase().contains("unix"));
    }

    /**
     * Determines whether the operating system is Solaris operating system.
     *
     * @return true if the operating system is a Solaris operating system, otherwise false.
     */
    public static boolean isSolarisOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("solaris");
    }

    /**
     * Returns the version of the Java virtual machine.
     *
     * @return the Java virtual machine version.
     */
    public static float getJavaVersion()
    {
        float ver = 0f;
        String s = System.getProperty("java.specification.version");
        if (null == s || s.length() == 0)
            s = System.getProperty("java.version");
        try
        {
            ver = Float.parseFloat(s.trim());
        }
        catch (NumberFormatException ignore)
        {
        }
        return ver;
    }

    /**
     * Returns the highest OpenGL profile available on the current graphics device that is compatible with WorldWind.
     * The returned profile favors hardware acceleration over software acceleration. With JOGL version 2.0, this returns
     * the highest available profile from the following list:
     * <ul> <li>OpenGL compatibility profile 4.x</li> <li>OpenGL compatibility profile 3.x</li> <li>OpenGL profile 1.x
     * up to 3.0</li> </ul>
     *
     * @return the highest compatible OpenGL profile.
     */
    public static GLProfile getMaxCompatibleGLProfile()
    {
        return GLProfile.getMaxFixedFunc(true); // Favor a hardware rasterizer.
    }

    /**
     * Returns a {@link com.jogamp.opengl.GLCapabilities} identifying graphics features required by WorldWind. The
     * capabilities instance returned requests the maximum OpenGL profile supporting GL fixed function operations, a
     * frame buffer with 8 bits each of red, green, blue and alpha, a 24-bit depth buffer, double buffering, and if the
     * Java property "gov.nasa.worldwind.stereo.mode" is set to "device", device supported stereo.
     *
     * @return a new capabilities instance identifying required graphics features.
     */
    public static GLCapabilities getRequiredGLCapabilities()
    {
        GLCapabilities caps = new GLCapabilities(getMaxCompatibleGLProfile());

        caps.setAlphaBits(8);
        caps.setRedBits(8);
        caps.setGreenBits(8);
        caps.setBlueBits(8);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);

        // Determine whether we should request a stereo canvas
        String stereo = System.getProperty(AVKey.STEREO_MODE);
        if ("device".equals(stereo))
            caps.setStereo(true);

        return caps;
    }

    /**
     * Returns a specified element of an XML configuration document.
     *
     * @param xpathExpression an XPath expression identifying the element of interest.
     *
     * @return the element of interest if the XPath expression is valid and the element exists, otherwise null.
     *
     * @throws NullPointerException if the XPath expression is null.
     */
    public static Element getElement(String xpathExpression)
    {
        XPath xpath = WWXML.makeXPath();

        for (Document doc : getInstance().configDocs)
        {
            try
            {
                Node node = (Node) xpath.evaluate(xpathExpression, doc.getDocumentElement(), XPathConstants.NODE);
                if (node != null)
                    return (Element) node;
            }
            catch (XPathExpressionException e)
            {
                return null;
            }
        }

        return null;
    }
}
