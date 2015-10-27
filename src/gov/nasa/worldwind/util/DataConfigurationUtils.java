/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.terrain.AbstractElevationModel;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import org.w3c.dom.*;

import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import java.net.*;
import java.util.concurrent.*;

/**
 * A collection of static methods useful for opening, reading, and otherwise working with World Wind data configuration
 * documents.
 *
 * @author dcollins
 * @version $Id: DataConfigurationUtils.java 2120 2014-07-03 03:05:03Z tgaskins $
 */
public class DataConfigurationUtils
{
    protected static final String DATE_TIME_PATTERN = "dd MM yyyy HH:mm:ss z";
    protected static final String DEFAULT_TEXTURE_FORMAT = "image/dds";

    /**
     * Returns true if the specified {@link org.w3c.dom.Element} is a data configuration document. This recognizes the
     * following data configuration documents: <ul> <li>Layer Configuration Documents</li> <li>Elevation Model
     * Configuration Documents</li> <li>Installed DataDescriptor Documents</li> <li>World Wind .NET LayerSet
     * Documents</li> </ul>
     *
     * @param domElement the document in question.
     *
     * @return true if the document is a data configuration document; false otherwise.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static boolean isDataConfig(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (AbstractLayer.isLayerConfigDocument(domElement))
        {
            return true;
        }

        if (AbstractElevationModel.isElevationModelConfigDocument(domElement))
        {
            return true;
        }

        if (isInstalledDataDescriptorConfigDocument(domElement))
        {
            return true;
        }

        //noinspection RedundantIfStatement
        if (isWWDotNetLayerSetConfigDocument(domElement))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the specified data configuration document transformed to a standard Layer or ElevationModel configuration
     * document. This returns the original document if the document is already in a standard form, or if the document is
     * not one of the recognized types. Installed DataDescriptor documents are transformed to standard Layer or
     * ElevationModel configuration documents, depending on the document contents. World Wind .NET LayerSet documents
     * are transformed to standard Layer configuration documents. This returns null if the document's root element is
     * null.
     *
     * @param doc the document to transform.
     *
     * @return the specified document transformed to a standard data configuration document, the original document if
     *         it's already in a standard form or is unrecognized, or null if the document's root element is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static Document convertToStandardDataConfigDocument(Document doc)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Element el = doc.getDocumentElement();
        if (el == null)
        {
            return null;
        }

        if (isInstalledDataDescriptorConfigDocument(el))
        {
            return transformInstalledDataDescriptorConfigDocument(el);
        }

        if (isWWDotNetLayerSetConfigDocument(el))
        {
            return transformWWDotNetLayerSetConfigDocument(el);
        }

        return doc;
    }

    /**
     * Returns the specified data configuration document's display name as a string, or null if the document is not one
     * of the recognized types. This determines the display name for each type of data configuration document as
     * follows: <table> <tr><th>Document Type</th><th>Path to Display Name</th></tr> <tr><td>Layer
     * Configuration</td><td>./DisplayName</td></tr> <tr><td>Elevation Model Configuration</td><td>./DisplayName</td></tr>
     * <tr><td>Installed DataDescriptor</td><td>./property[@name="dataSet"]/property[@name="gov.nasa.worldwind.avkey.DatasetNameKey"]</td></tr>
     * <tr><td>World Wind .NET LayerSet</td><td>./QuadTileSet/Name</td></tr> <tr><td>Other</td><td>null</td></tr>
     * </table>
     *
     * @param domElement the data configuration document who's display name is returned.
     *
     * @return a String representing the data configuration document's display name, or null if the document is not
     *         recognized.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static String getDataConfigDisplayName(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (AbstractLayer.isLayerConfigDocument(domElement) || AbstractElevationModel.isElevationModelConfigDocument(
            domElement))
        {
            return WWXML.getText(domElement, "DisplayName");
        }

        if (isInstalledDataDescriptorConfigDocument(domElement))
        {
            return WWXML.getText(domElement,
                "property[@name=\"dataSet\"]/property[@name=\"gov.nasa.worldwind.avkey.DatasetNameKey\"]");
        }

        if (isWWDotNetLayerSetConfigDocument(domElement))
        {
            return WWXML.getText(domElement, "QuadTileSet/Name");
        }

        return null;
    }

    /**
     * Returns the specified data configuration document's type as a string, or null if the document is not one of the
     * recognized types. This maps data configuration documents to a type string as follows: <table> <tr><th>Document
     * Type</th><th>Type String</th></tr> <tr><td>Layer Configuration</td><td>"Layer"</td></tr> <tr><td>Elevation Model
     * Configuration</td><td>"Elevation Model"</td></tr> <tr><td>Installed DataDescriptor</td><td>"Layer" or
     * "ElevationModel"</td></tr> <tr><td>World Wind .NET LayerSet</td><td>"Layer"</td></tr>
     * <tr><td>Other</td><td>null</td></tr> </table>
     *
     * @param domElement the data configuration document to determine a type for.
     *
     * @return a String representing the data configuration document's type, or null if the document is not recognized.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static String getDataConfigType(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (AbstractLayer.isLayerConfigDocument(domElement))
        {
            return "Layer";
        }

        if (AbstractElevationModel.isElevationModelConfigDocument(domElement))
        {
            return "ElevationModel";
        }

        if (isInstalledDataDescriptorConfigDocument(domElement))
        {
            String s = WWXML.getText(domElement,
                "property[@name=\"dataSet\"]/property[@name=\"gov.nasa.worldwind.avkey.DataType\"]",
                null);
            if (s != null && s.equals("gov.nasa.worldwind.avkey.TiledElevations"))
            {
                return "ElevationModel";
            }
            else
            {
                return "Layer";
            }
        }

        if (isWWDotNetLayerSetConfigDocument(domElement))
        {
            return "Layer";
        }

        return null;
    }

    /**
     * Returns a file store path name for the specified parameters list. This returns null if the parameter list does
     * not contain enough information to construct a path name.
     *
     * @param params the parameter list to extract a configuration filename from.
     * @param suffix the file suffix to append on the path name, or null to append no suffix.
     *
     * @return a file store path name with the specified suffix, or null if a path name cannot be constructed.
     *
     * @throws IllegalArgumentException if the parameter list is null.
     */
    public static String getDataConfigFilename(AVList params, String suffix)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = params.getStringValue(AVKey.DATA_CACHE_NAME);
        if (path == null || path.length() == 0)
        {
            return null;
        }

        String filename = params.getStringValue(AVKey.DATASET_NAME);

        if (filename == null || filename.length() == 0)
        {
            filename = params.getStringValue(AVKey.DISPLAY_NAME);
        }

        if (filename == null || filename.length() == 0)
        {
            filename = "DataConfiguration";
        }

        filename = WWIO.replaceIllegalFileNameCharacters(filename);

        return path + java.io.File.separator + filename + (suffix != null ? suffix : "");
    }

    /**
     * Convenience method for computing a data configuration file's cache name in a FileStore, given the file's cache
     * path. This writes the computed cache name to the specified parameter list under the key {@link
     * gov.nasa.worldwind.avlist.AVKey#DATA_CACHE_NAME}. If the parameter already exists, it's left unchanged.
     * <p/>
     * A data configuration file's cache name is its parent directory in the cache. The cache name therefore points to
     * the directory containing both the configuration file and any cached data associated with it. Determining the
     * cache name at run time - instead of hard wiring it in the data configuration file - enables cache data to be
     * moved to an arbitrary location within the cache.
     *
     * @param dataConfigCachePath the data configuration file's cache path.
     * @param params              the output key-value pairs which receive the DATA_CACHE_NAME parameter. A null
     *                            reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the data config file's cache path is null or has length zero.
     */
    public static AVList getDataConfigCacheName(String dataConfigCachePath, AVList params)
    {
        if (dataConfigCachePath == null || dataConfigCachePath.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        String s = params.getStringValue(AVKey.DATA_CACHE_NAME);
        if (s == null || s.length() == 0)
        {
            // Get the data configuration file's parent cache name.
            s = WWIO.getParentFilePath(dataConfigCachePath);
            if (s != null && s.length() > 0)
            {
                // Replace any windows-style path separators with the unix-style path separator, which is the convention
                // for cache paths.
                s = s.replaceAll("\\\\", "/");
                params.setValue(AVKey.DATA_CACHE_NAME, s);
            }
        }

        return params;
    }

    /**
     * Returns true if a configuration file name exists in the store which has not expired. This returns false if a
     * configuration file does not exist, or it has expired. This invokes {@link #findExistingDataConfigFile(gov.nasa.worldwind.cache.FileStore,
     * String)} to determine the URL of any existing file names. If an existing file has expired, and removeIfExpired is
     * true, this removes the existing file.
     *
     * @param fileStore       the file store in which to look.
     * @param fileName        the file name to look for. If a file with this name does not exist in the store, this
     *                        looks at the file's siblings for a match.
     * @param removeIfExpired true to remove the existing file, if it exists and is expired; false otherwise.
     * @param expiryTime      the time in milliseconds, before which a file is considered to be expired.
     *
     * @return whether a configuration file already exists which has not expired.
     *
     * @throws IllegalArgumentException if either the file store or file name are null.
     */
    public static boolean hasDataConfigFile(FileStore fileStore, String fileName, boolean removeIfExpired,
        long expiryTime)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (fileName == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Look for an existing configuration file in the store. Return true if a configuration file does not exist,
        // or it has expired; otherwise return false.
        java.net.URL url = findExistingDataConfigFile(fileStore, fileName);
        if (url != null && !WWIO.isFileOutOfDate(url, expiryTime))
        {
            return true;
        }

        // A configuration file exists but it is expired. Remove the file and return false, indicating that there is
        // no configuration document.
        if (url != null && removeIfExpired)
        {
            fileStore.removeFile(url);

            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.logger().fine(message);
        }

        return false;
    }

    /**
     * Returns the URL of an existing data configuration file under the specified file store, or null if no
     * configuration file exists. This first looks for a configuration file with the specified name. If that does not
     * exist, this checks the siblings of the specified file for a configuration file match.
     *
     * @param fileStore the file store in which to look.
     * @param fileName  the file name to look for. If a file with this name does not exist in the store, this looks at
     *                  the file's siblings for a match.
     *
     * @return the URL of an existing configuration file in the store, or null if none exists.
     *
     * @throws IllegalArgumentException if either the file store or file name are null.
     */
    public static java.net.URL findExistingDataConfigFile(FileStore fileStore, String fileName)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (fileName == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Attempt to find the specified file name in the store. If it exists, then we've found a match and we're done.
        java.net.URL url = fileStore.findFile(fileName, false);
        if (url != null)
        {
            return url;
        }

        // If the specified name did not exist, then try to find any data configuration file under the file's parent
        // path. Find only the file names which are siblings of the specified file name.
        String path = WWIO.getParentFilePath(fileName);
        if (path == null || path.length() == 0)
        {
            return null;
        }

        String[] names = fileStore.listFileNames(path, new DataConfigurationFilter());
        if (names == null || names.length == 0)
        {
            return null;
        }

        // Ignore all but the first file match.
        return fileStore.findFile(names[0], false);
    }

    /**
     * Convenience method to create a {@link java.util.concurrent.ScheduledExecutorService} which can be used by World
     * Wind components to schedule periodic resource checks. The returned ExecutorService is backed by a single daemon
     * thread with minimum priority.
     *
     * @param threadName the String name for the ExecutorService's thread, may be <code>null</code>.
     *
     * @return a new ScheduledExecutorService appropriate for scheduling periodic resource checks.
     */
    public static ScheduledExecutorService createResourceRetrievalService(final String threadName)
    {
        ThreadFactory threadFactory = new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);

                if (threadName != null)
                {
                    thread.setName(threadName);
                }

                return thread;
            }
        };

        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    //**************************************************************//
    //********************  WMS Common Configuration  **************//
    //**************************************************************//

    /**
     * Appends WMS layer parameters as elements to a specified context. This appends elements for the following
     * parameters: <table> <th><td>Parameter</td><td>Element Path</td><td>Type</td></th> <tr><td>{@link
     * AVKey#WMS_VERSION}</td><td>Service/@version</td><td>String</td></tr> <tr><td>{@link
     * AVKey#LAYER_NAMES}</td><td>Service/LayerNames</td><td>String</td></tr> <tr><td>{@link
     * AVKey#STYLE_NAMES}</td><td>Service/StyleNames</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_MAP_URL}</td><td>Service/GetMapURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_CAPABILITIES_URL}</td><td>Service/GetCapabilitiesURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#SERVICE}</td><td>AVKey#GET_MAP_URL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#DATASET_NAME}</td><td>AVKey.LAYER_NAMES</td><td>String</td></tr> </table>
     *
     * @param params  the key-value pairs which define the WMS layer configuration parameters.
     * @param context the XML document root on which to append WMS layer configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createWMSLayerConfigElements(AVList params, Element context)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        // Service properties. The service element may already exist, in which case we want to append the "URL" element
        // to the existing service element.
        Element el = WWXML.getElement(context, "Service", xpath);
        if (el == null)
        {
            el = WWXML.appendElementPath(context, "Service");
        }

        // Try to get the SERVICE_NAME property, but default to "OGC:WMS".
        String s = AVListImpl.getStringValue(params, AVKey.SERVICE_NAME, OGCConstants.WMS_SERVICE_NAME);
        if (s != null && s.length() > 0)
        {
            WWXML.setTextAttribute(el, "serviceName", s);
        }

        s = params.getStringValue(AVKey.WMS_VERSION);
        if (s != null && s.length() > 0)
        {
            WWXML.setTextAttribute(el, "version", s);
        }

        WWXML.checkAndAppendTextElement(params, AVKey.LAYER_NAMES, el, "LayerNames");
        WWXML.checkAndAppendTextElement(params, AVKey.STYLE_NAMES, el, "StyleNames");
        WWXML.checkAndAppendTextElement(params, AVKey.GET_MAP_URL, el, "GetMapURL");
        WWXML.checkAndAppendTextElement(params, AVKey.GET_CAPABILITIES_URL, el, "GetCapabilitiesURL");

        // Since this is a WMS tiled image layer, we want to express the service URL as a GetMap URL. If we have a
        // GET_MAP_URL property, then remove any existing SERVICE property from the DOM document.
        s = params.getStringValue(AVKey.GET_MAP_URL);
        if (s != null && s.length() > 0)
        {
            Element urlElement = WWXML.getElement(el, "URL", xpath);
            if (urlElement != null)
            {
                el.removeChild(urlElement);
            }
        }

        return context;
    }

    /**
     * Appends WCS layer parameters as elements to a specified context. This appends elements for the following
     * parameters: <table> <th><td>Parameter</td><td>Element Path</td><td>Type</td></th> <tr><td>{@link
     * AVKey#WCS_VERSION}</td><td>Service/@version</td><td>String</td></tr> <tr><td>{@link
     * AVKey#COVERAGE_IDENTIFIERS}</td><td>Service/coverageIdentifiers</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_COVERAGE_URL}</td><td>Service/GetCoverageURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_CAPABILITIES_URL}</td><td>Service/GetCapabilitiesURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#SERVICE}</td><td>AVKey#GET_COVERAGE_URL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#DATASET_NAME}</td><td>AVKey.COVERAGE_IDENTIFIERS</td><td>String</td></tr> </table>
     *
     * @param params  the key-value pairs which define the WMS layer configuration parameters.
     * @param context the XML document root on which to append WMS layer configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createWCSLayerConfigElements(AVList params, Element context)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        // Service properties. The service element may already exist, in which case we want to append the "URL" element
        // to the existing service element.
        Element el = WWXML.getElement(context, "Service", xpath);
        if (el == null)
        {
            el = WWXML.appendElementPath(context, "Service");
        }

        // Try to get the SERVICE_NAME property, but default to "OGC:WCS".
        String s = AVListImpl.getStringValue(params, AVKey.SERVICE_NAME, OGCConstants.WCS_SERVICE_NAME);
        if (s != null && s.length() > 0)
        {
            WWXML.setTextAttribute(el, "serviceName", s);
        }

        s = params.getStringValue(AVKey.WCS_VERSION);
        if (s != null && s.length() > 0)
        {
            WWXML.setTextAttribute(el, "version", s);
        }

        WWXML.checkAndAppendTextElement(params, AVKey.COVERAGE_IDENTIFIERS, el, "CoverageIdentifiers");
        WWXML.checkAndAppendTextElement(params, AVKey.GET_COVERAGE_URL, el, "GetCoverageURL");
        WWXML.checkAndAppendTextElement(params, AVKey.GET_CAPABILITIES_URL, el, "GetCapabilitiesURL");

        // Since this is a WCS tiled coverage, we want to express the service URL as a GetCoverage URL. If we have a
        // GET_COVERAGE_URL property, then remove any existing SERVICE property from the DOM document.
        s = params.getStringValue(AVKey.GET_COVERAGE_URL);
        if (s != null && s.length() > 0)
        {
            Element urlElement = WWXML.getElement(el, "URL", xpath);
            if (urlElement != null)
            {
                el.removeChild(urlElement);
            }
        }

        return context;
    }

    /**
     * Parses WMS layer parameters from the XML configuration document starting at domElement. This writes output as
     * key-value pairs to params. If a parameter from the XML document already exists in params, that parameter is
     * ignored. Supported key and parameter names are: <table> <th><td>Parameter</td><td>Element
     * Path</td><td>Type</td></th> <tr><td>{@link AVKey#WMS_VERSION}</td><td>Service/@version</td><td>String</td></tr>
     * <tr><td>{@link AVKey#LAYER_NAMES}</td><td>Service/LayerNames</td><td>String</td></tr> <tr><td>{@link
     * AVKey#STYLE_NAMES}</td><td>Service/StyleNames</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_MAP_URL}</td><td>Service/GetMapURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#GET_CAPABILITIES_URL}</td><td>Service/GetCapabilitiesURL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#SERVICE}</td><td>AVKey#GET_MAP_URL</td><td>String</td></tr> <tr><td>{@link
     * AVKey#DATASET_NAME}</td><td>AVKey.LAYER_NAMES</td><td>String</td></tr> </table>
     *
     * @param domElement the XML document root to parse for WMS layer parameters.
     * @param params     the output key-value pairs which receive the WMS layer parameters. A null reference is
     *                   permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getWMSLayerConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        XPath xpath = WWXML.makeXPath();

        // Need to determine these for URLBuilder construction.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.WMS_VERSION, "Service/@version", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.LAYER_NAMES, "Service/LayerNames", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.STYLE_NAMES, "Service/StyleNames", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.GET_MAP_URL, "Service/GetMapURL", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.GET_CAPABILITIES_URL, "Service/GetCapabilitiesURL",
            xpath);

        params.setValue(AVKey.SERVICE, params.getValue(AVKey.GET_MAP_URL));
        String serviceURL = params.getStringValue(AVKey.SERVICE);
        if (serviceURL != null)
        {
            params.setValue(AVKey.SERVICE, WWXML.fixGetMapString(serviceURL));
        }

        // The dataset name is the layer-names string for WMS elevation models
        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
        if (layerNames != null)
        {
            params.setValue(AVKey.DATASET_NAME, layerNames);
        }

        return params;
    }

    public static AVList getWCSConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        XPath xpath = WWXML.makeXPath();

        // Need to determine these for URLBuilder construction.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.WCS_VERSION, "Service/@version", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.COVERAGE_IDENTIFIERS, "Service/CoverageIdentifiers",
            xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.GET_COVERAGE_URL, "Service/GetCoverageURL", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.GET_CAPABILITIES_URL, "Service/GetCapabilitiesURL",
            xpath);

        params.setValue(AVKey.SERVICE, params.getValue(AVKey.GET_COVERAGE_URL));
        String serviceURL = params.getStringValue(AVKey.SERVICE);
        if (serviceURL != null)
        {
            params.setValue(AVKey.SERVICE, WWXML.fixGetMapString(serviceURL));
        }

        // The dataset name is the layer-names string for WMS elevation models
        String coverages = params.getStringValue(AVKey.COVERAGE_IDENTIFIERS);
        if (coverages != null)
        {
            params.setValue(AVKey.DATASET_NAME, coverages);
        }

        return params;
    }

    public static AVList getWMSLayerConfigParams(WMSCapabilities caps, String[] formatOrderPreference, AVList params)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = params.getStringValue(AVKey.STYLE_NAMES);
        if (layerNames == null || layerNames.length() == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] names = layerNames.split(",");
        if (names == null || names.length == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String coordinateSystem = params.getStringValue(AVKey.COORDINATE_SYSTEM);
        if (WWUtil.isEmpty(coordinateSystem))
        {
            for (String name : names)
            {
                WMSLayerCapabilities layerCaps = caps.getLayerByName(name);
                if (layerCaps == null)
                {
                    Logging.logger().warning(Logging.getMessage("WMS.LayerNameMissing", name));
                    continue;
                }

                if (layerCaps.hasCoordinateSystem("EPSG:4326"))
                {
                    coordinateSystem = "EPSG:4326";
                    break; // this assumes that the CS is available for all the layers in layerNames
                }
                else if (layerCaps.hasCoordinateSystem("CRS:84"))
                {
                    coordinateSystem = "CRS:84";
                    break; // this assumes that the CS is available for all the layers in layerNames
                }
            }

            if (!WWUtil.isEmpty(coordinateSystem))
                params.setValue(AVKey.COORDINATE_SYSTEM, coordinateSystem);
        }

        // Define the DISPLAY_NAME and DATASET_NAME from the WMS layer names and styles.
        params.setValue(AVKey.DISPLAY_NAME, makeTitle(caps, layerNames, styleNames));
        params.setValue(AVKey.DATASET_NAME, layerNames);

        // Get the EXPIRY_TIME from the WMS layer last update time.
        Long lastUpdate = caps.getLayerLatestLastUpdateTime(names);
        if (lastUpdate != null)
        {
            params.setValue(AVKey.EXPIRY_TIME, lastUpdate);
        }

        // Get the GET_MAP_URL from the WMS getMapRequest URL.
        String mapRequestURIString = caps.getRequestURL("GetMap", "http", "get");
        if (params.getValue(AVKey.GET_MAP_URL) == null)
        {
            params.setValue(AVKey.GET_MAP_URL, mapRequestURIString);
        }
        mapRequestURIString = params.getStringValue(AVKey.GET_MAP_URL);
        // Throw an exception if there's no GET_MAP_URL property, or no getMapRequest URL in the WMS Capabilities.
        if (mapRequestURIString == null || mapRequestURIString.length() == 0)
        {
            Logging.logger().severe("WMS.RequestMapURLMissing");
            throw new WWRuntimeException(Logging.getMessage("WMS.RequestMapURLMissing"));
        }

        // Get the GET_CAPABILITIES_URL from the WMS getCapabilitiesRequest URL.
        String capsRequestURIString = caps.getRequestURL("GetCapabilities", "http", "get");
        if (params.getValue(AVKey.GET_CAPABILITIES_URL) == null)
        {
            params.setValue(AVKey.GET_CAPABILITIES_URL, capsRequestURIString);
        }

        // Define the SERVICE from the GET_MAP_URL property.
        params.setValue(AVKey.SERVICE, params.getValue(AVKey.GET_MAP_URL));
        String serviceURL = params.getStringValue(AVKey.SERVICE);
        if (serviceURL != null)
        {
            params.setValue(AVKey.SERVICE, WWXML.fixGetMapString(serviceURL));
        }

        // Define the SERVICE_NAME as the standard OGC WMS service string.
        if (params.getValue(AVKey.SERVICE_NAME) == null)
        {
            params.setValue(AVKey.SERVICE_NAME, OGCConstants.WMS_SERVICE_NAME);
        }

        // Define the WMS VERSION as the version fetched from the Capabilities document.
        String versionString = caps.getVersion();
        if (params.getValue(AVKey.WMS_VERSION) == null)
        {
            params.setValue(AVKey.WMS_VERSION, versionString);
        }

        // Form the cache path DATA_CACHE_NAME from a set of unique WMS parameters.
        if (params.getValue(AVKey.DATA_CACHE_NAME) == null)
        {
            try
            {
                URI mapRequestURI = new URI(mapRequestURIString);
                String cacheName = WWIO.formPath(mapRequestURI.getAuthority(), mapRequestURI.getPath(), layerNames,
                    styleNames);
                params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            }
            catch (URISyntaxException e)
            {
                String message = Logging.getMessage("WMS.RequestMapURLBad", mapRequestURIString);
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
                throw new WWRuntimeException(message);
            }
        }

        // Determine image format to request.
        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            String imageFormat = chooseImageFormat(caps.getImageFormats().toArray(), formatOrderPreference);
            params.setValue(AVKey.IMAGE_FORMAT, imageFormat);
        }

        // Throw an exception if we cannot determine an image format to request.
        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            Logging.logger().severe("WMS.NoImageFormats");
            throw new WWRuntimeException(Logging.getMessage("WMS.NoImageFormats"));
        }

        // Determine bounding sector.
        Sector sector = (Sector) params.getValue(AVKey.SECTOR);
        if (sector == null)
        {
            for (String name : names)
            {
                Sector layerSector = caps.getLayerByName(name).getGeographicBoundingBox();
                if (layerSector == null)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, "WMS.NoGeographicBoundingBoxForLayer", name);
                    continue;
                }

                sector = Sector.union(sector, layerSector);
            }

            if (sector == null)
            {
                Logging.logger().severe("WMS.NoGeographicBoundingBox");
                throw new WWRuntimeException(Logging.getMessage("WMS.NoGeographicBoundingBox"));
            }
            params.setValue(AVKey.SECTOR, sector);
        }

        // TODO: adjust for subsetable, fixedimage, etc.

        return params;
    }

    public static AVList getWCSConfigParameters(WCS100Capabilities caps, WCS100DescribeCoverage coverage, AVList params)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (coverage == null)
        {
            String message = Logging.getMessage("nullValue.WCSDescribeCoverage");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        WCS100CoverageOffering offering = coverage.getCoverageOfferings().get(0);

        params.setValue(AVKey.SERVICE_NAME, OGCConstants.WCS_SERVICE_NAME);
        params.setValue(AVKey.WCS_VERSION, caps.getVersion() != null ? caps.getVersion() : "1.0.0");
        params.setValue(AVKey.DISPLAY_NAME, offering.getLabel());
        params.setValue(AVKey.COVERAGE_IDENTIFIERS, offering.getName());
        params.setValue(AVKey.GET_COVERAGE_URL, caps.getCapability().getGetOperationAddress("GetCoverage"));
        params.setValue(AVKey.GET_CAPABILITIES_URL, caps.getCapability().getGetOperationAddress("GetCapabilities"));

        params.setValue(AVKey.SERVICE, params.getValue(AVKey.GET_COVERAGE_URL));
        String serviceURL = params.getStringValue(AVKey.SERVICE);
        if (serviceURL != null)
        {
            params.setValue(AVKey.SERVICE, WWXML.fixGetMapString(serviceURL));
        }

        String coverages = params.getStringValue(AVKey.COVERAGE_IDENTIFIERS);
        if (coverages != null)
        {
            params.setValue(AVKey.DATASET_NAME, coverages);
        }

        // Form the cache path DATA_CACHE_NAME from a set of unique WMS parameters.
        if (params.getValue(AVKey.DATA_CACHE_NAME) == null)
        {
            try
            {
                URI mapRequestURI = new URI(params.getStringValue(AVKey.GET_COVERAGE_URL));
                String cacheName = WWIO.formPath(mapRequestURI.getAuthority(), mapRequestURI.getPath(),
                    params.getStringValue(AVKey.COVERAGE_IDENTIFIERS));
                params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            }
            catch (URISyntaxException e)
            {
                String message = Logging.getMessage("WCS.RequestMapURLBad",
                    params.getStringValue(AVKey.GET_COVERAGE_URL));
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
                throw new WWRuntimeException(message);
            }
        }

        for (String format : offering.getSupportedFormats().getStrings())
        {
            if (format.toLowerCase().contains("image/tiff"))
            {
                params.setValue(AVKey.IMAGE_FORMAT, format);
                break;
            }
            else if (format.toLowerCase().contains("tiff")) // lots of variants in use, so find one
            {
                params.setValue(AVKey.IMAGE_FORMAT, format);
                break;
            }
        }

        // Determine bounding sector.
        WCS100LonLatEnvelope envelope = offering.getLonLatEnvelope();
        if (envelope != null)
        {
            double[] sw = envelope.getPositions().get(0).getPos2();
            double[] ne = envelope.getPositions().get(1).getPos2();

            if (sw != null && ne != null)
            {
                params.setValue(AVKey.SECTOR, Sector.fromDegreesAndClamp(sw[1], ne[1], sw[0], ne[0]));
            }
        }

        String epsg4326 = "EPSG:4326";
        String crs = null;
        if (offering.getSupportedCRSs() != null)
        {
            if (offering.getSupportedCRSs().getRequestResponseCRSs().contains(epsg4326))
            {
                crs = epsg4326;
            } else if (offering.getSupportedCRSs().getRequestCRSs().contains(epsg4326)
                && offering.getSupportedCRSs().getResponseCRSs().contains(epsg4326))
            {
                crs = epsg4326;
            }
        }

        if (crs != null)
        {
            params.setValue(AVKey.COORDINATE_SYSTEM, crs);
        }

        WCS100Values nullValues = offering.getRangeSet().getRangeSet().getNullValues();
        if (nullValues != null && nullValues.getSingleValues() != null && nullValues.getSingleValues().size() > 0)
        {
            Double nullValue = nullValues.getSingleValues().get(0).getSingleValue();
            if (nullValue != null)
            {
                params.setValue(AVKey.MISSING_DATA_SIGNAL, nullValue);
            }
        }

        return params;
    }

    /**
     * Convenience method to get the OGC GetCapabilities URL from a specified parameter list. If all the necessary
     * parameters are available, this returns the GetCapabilities URL. Otherwise this returns null.
     *
     * @param params parameter list to get the GetCapabilities parameters from.
     *
     * @return a OGC GetCapabilities URL, or null if the necessary parameters are not available.
     *
     * @throws IllegalArgumentException if the parameter list is null.
     */
    public static URL getOGCGetCapabilitiesURL(AVList params)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String uri = params.getStringValue(AVKey.GET_CAPABILITIES_URL);
        if (uri == null || uri.length() == 0)
        {
            return null;
        }

        String service = params.getStringValue(AVKey.SERVICE_NAME);
        if (service == null || service.length() == 0)
        {
            return null;
        }

        try
        {
            if (service.equals(OGCConstants.WMS_SERVICE_NAME))
            {
                service = "WMS";
                CapabilitiesRequest request = new CapabilitiesRequest(new URI(uri), service);
                return request.getUri().toURL();
            }
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        catch (MalformedURLException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        return null;
    }

    /**
     * Convenience method to get the OGC {@link AVKey#LAYER_NAMES} parameter from a specified parameter list. If the
     * parameter is available as a String, this returns all the OGC layer names found in that String. Otherwise this
     * returns null.
     *
     * @param params parameter list to get the layer names from.
     *
     * @return an array of layer names, or null if none exist.
     *
     * @throws IllegalArgumentException if the parameter list is null.
     */
    public static String[] getOGCLayerNames(AVList params)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String s = params.getStringValue(AVKey.LAYER_NAMES);
        if (s == null || s.length() == 0)
        {
            return null;
        }

        return s.split(",");
    }

    protected static String chooseImageFormat(Object[] formats, String[] formatOrderPreference)
    {
        if (formats == null || formats.length == 0)
        {
            return null;
        }

        // No preferred formats specified; just use the first in the caps list.
        if (formatOrderPreference == null || formatOrderPreference.length == 0)
        {
            return formats[0].toString();
        }

        for (String s : formatOrderPreference)
        {
            for (Object f : formats)
            {
                if (f.toString().equalsIgnoreCase(s))
                {
                    return f.toString();
                }
            }
        }

        return formats[0].toString(); // No preferred formats recognized; just use the first in the caps list.
    }

    protected static String makeTitle(WMSCapabilities caps, String layerNames, String styleNames)
    {
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }

            String layerName = lNames[i];
            WMSLayerCapabilities layer = caps.getLayerByName(layerName);
            String layerTitle = layer.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
            {
                continue;
            }

            String styleName = sNames[i];
            WMSLayerStyle style = layer.getStyleByName(styleName);
            if (style == null)
            {
                continue;
            }

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    //protected static int[] getLayerFixedWidthAndHeight(Capabilities caps, Element layer)
    //{
    //    Integer width;
    //    Integer height;
    //
    //    String s = caps.getLayerFixedWidth(layer);
    //    if (s == null)
    //        return null;
    //
    //    width = WWUtil.convertStringToInteger(s);
    //    if (width == null)
    //        return null;
    //
    //    s = caps.getLayerFixedHeight(layer);
    //    if (s == null)
    //        return null;
    //
    //    height = WWUtil.convertStringToInteger(s);
    //    if (height == null)
    //        return null;
    //
    //    return new int[] {width, height};
    //}

    //protected static LatLon getLayerLatLon(Element layer, String path)
    //{
    //    XPath xpath = WWXML.makeXPath();
    //
    //    Element el = WWXML.getElement(layer, path, xpath);
    //    if (el == null)
    //        return null;
    //
    //    Double latDegrees = WWXML.getDouble(el, "Latitude", xpath);
    //    Double lonDegrees = WWXML.getDouble(el, "Longitude", xpath);
    //    if (latDegrees == null || lonDegrees == null)
    //        return null;
    //
    //    return LatLon.fromDegrees(latDegrees, lonDegrees);
    //}

    //protected static int computeLayerNumLevels(LatLon minDelta, LatLon maxDelta)
    //{
    //    return Math.max(
    //        computeLayerNumLevels(minDelta.getLatitude(), maxDelta.getLatitude()),
    //        computeLayerNumLevels(minDelta.getLongitude(), maxDelta.getLongitude()));
    //}

    //protected static int computeLayerNumLevels(Angle minDelta, Angle maxDelta)
    //{
    //    double log2MinDelta = WWMath.logBase2(minDelta.getDegrees());
    //    double log2MaxDelta = WWMath.logBase2(maxDelta.getDegrees());
    //    return 1 + (int) Math.round(log2MaxDelta - log2MinDelta);
    //}

    //**************************************************************//
    //********************  LevelSet Common Configuration  *********//
    //**************************************************************//

    /**
     * Appends LevelSet configuration parameters as elements to the specified context. This appends elements for the
     * following parameters: <table> <th><td>Key</td><td>Name</td><td>Path</td></th> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DATASET_NAME}</td><td>DatasetName</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DATA_CACHE_NAME}</td><td>DataCacheName</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#SERVICE}</td><td>Service/URL</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#EXPIRY_TIME}</td><td>ExpiryTime</td><td>Long</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#EXPIRY_TIME}</td><td>LastUpdate</td><td>Long</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#FORMAT_SUFFIX}</td><td>FormatSuffix</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#NUM_LEVELS}</td><td>NumLevels/@count</td><td>Integer</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#NUM_EMPTY_LEVELS}</td><td>NumLevels/@numEmpty</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#INACTIVE_LEVELS}</td><td>NumLevels/@inactive</td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR}</td><td>Sector</td><td>{@link
     * gov.nasa.worldwind.geom.Sector}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR_RESOLUTION_LIMITS}</td><td>SectorResolutionLimit</td>
     * <td>{@link LevelSet.SectorResolution}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_ORIGIN}</td><td>TileOrigin/LatLon</td><td>{@link
     * gov.nasa.worldwind.geom.LatLon}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_WIDTH}</td><td>TileSize/Dimension/@width</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_HEIGHT}</td><td>TileSize/Dimension/@height</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#LEVEL_ZERO_TILE_DELTA}</td><td>LastUpdate</td><td>LatLon</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MAX_ABSENT_TILE_ATTEMPTS}</td><td>MaxAbsentTileAttempts</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MIN_ABSENT_TILE_CHECK_INTERVAL}</td><td>MinAbsentTileCheckInterval</td><td>Integer</td></tr>
     * </table>
     *
     * @param params  the key-value pairs which define the LevelSet configuration parameters.
     * @param context the XML document root on which to append LevelSet configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createLevelSetConfigElements(AVList params, Element context)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Title and cache name properties.
        WWXML.checkAndAppendTextElement(params, AVKey.DATASET_NAME, context, "DatasetName");
        WWXML.checkAndAppendTextElement(params, AVKey.DATA_CACHE_NAME, context, "DataCacheName");

        // Service properties.
        String s = params.getStringValue(AVKey.SERVICE);
        if (s != null && s.length() > 0)
        {
            // The service element may already exist, in which case we want to append the "URL" element to the existing
            // service element.
            Element el = WWXML.getElement(context, "Service", null);
            if (el == null)
            {
                el = WWXML.appendElementPath(context, "Service");
            }
            WWXML.appendText(el, "URL", s);
        }

        // Expiry time properties.
        WWXML.checkAndAppendLongElement(params, AVKey.EXPIRY_TIME, context, "LastUpdate");

        // Image format properties.
        WWXML.checkAndAppendTextElement(params, AVKey.FORMAT_SUFFIX, context, "FormatSuffix");

        // Tile structure properties.
        Integer numLevels = AVListImpl.getIntegerValue(params, AVKey.NUM_LEVELS);
        if (numLevels != null)
        {
            Element el = WWXML.appendElementPath(context, "NumLevels");
            WWXML.setIntegerAttribute(el, "count", numLevels);

            Integer i = AVListImpl.getIntegerValue(params, AVKey.NUM_EMPTY_LEVELS, 0);
            WWXML.setIntegerAttribute(el, "numEmpty", i);

            s = params.getStringValue(AVKey.INACTIVE_LEVELS);
            if (s != null && s.length() > 0)
            {
                WWXML.setTextAttribute(el, "inactive", s);
            }
        }

        WWXML.checkAndAppendSectorElement(params, AVKey.SECTOR, context, "Sector");
        WWXML.checkAndAppendSectorResolutionElement(params, AVKey.SECTOR_RESOLUTION_LIMITS, context,
            "SectorResolutionLimit");
        WWXML.checkAndAppendLatLonElement(params, AVKey.TILE_ORIGIN, context, "TileOrigin/LatLon");

        Integer tileWidth = AVListImpl.getIntegerValue(params, AVKey.TILE_WIDTH);
        Integer tileHeight = AVListImpl.getIntegerValue(params, AVKey.TILE_HEIGHT);
        if (tileWidth != null && tileHeight != null)
        {
            Element el = WWXML.appendElementPath(context, "TileSize/Dimension");
            WWXML.setIntegerAttribute(el, "width", tileWidth);
            WWXML.setIntegerAttribute(el, "height", tileHeight);
        }

        WWXML.checkAndAppendLatLonElement(params, AVKey.LEVEL_ZERO_TILE_DELTA, context, "LevelZeroTileDelta/LatLon");

        // Retrieval properties.
        if (params.getValue(AVKey.MAX_ABSENT_TILE_ATTEMPTS) != null ||
            params.getValue(AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL) != null)
        {
            Element el = WWXML.getElement(context, "AbsentTiles", null);
            if (el == null)
            {
                el = WWXML.appendElementPath(context, "AbsentTiles");
            }

            WWXML.checkAndAppendIntegerlement(params, AVKey.MAX_ABSENT_TILE_ATTEMPTS, el, "MaxAttempts");
            WWXML.checkAndAppendTimeElement(params, AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL, el, "MinCheckInterval/Time");
        }

        return context;
    }

    /**
     * Parses LevelSet configuration parameters from the specified DOM document. This writes output as key-value pairs
     * to params. If a parameter from the XML document already exists in params, that parameter is ignored. Supported
     * key and parameter names are: <table> <th><td>Parameter</td><td>Element path</td><td>Type</td></th> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DATASET_NAME}</td><td>DatasetName</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DATA_CACHE_NAME}</td><td>DataCacheName</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#SERVICE}</td><td>Service/URL</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#EXPIRY_TIME}</td><td>ExpiryTime</td><td>Long</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#EXPIRY_TIME}</td><td>LastUpdate</td><td>Long</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#FORMAT_SUFFIX}</td><td>FormatSuffix</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#NUM_LEVELS}</td><td>NumLevels/@count</td><td>Integer</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#NUM_EMPTY_LEVELS}</td><td>NumLevels/@numEmpty</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#INACTIVE_LEVELS}</td><td>NumLevels/@inactive</td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR}</td><td>Sector</td><td>{@link
     * gov.nasa.worldwind.geom.Sector}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR_RESOLUTION_LIMITS}</td><td>SectorResolutionLimit</td>
     * <td>{@link LevelSet.SectorResolution}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_ORIGIN}</td><td>TileOrigin/LatLon</td><td>{@link
     * gov.nasa.worldwind.geom.LatLon}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_WIDTH}</td><td>TileSize/Dimension/@width</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_HEIGHT}</td><td>TileSize/Dimension/@height</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#LEVEL_ZERO_TILE_DELTA}</td><td>LastUpdate</td><td>LatLon</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MAX_ABSENT_TILE_ATTEMPTS}</td><td>AbsentTiles/MaxAttempts</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MIN_ABSENT_TILE_CHECK_INTERVAL}</td><td>AbsentTiles/MinCheckInterval/Time</td><td>Integer
     * milliseconds</td></tr> </table>
     *
     * @param domElement the XML document root to parse for LevelSet configuration parameters.
     * @param params     the output key-value pairs which receive the LevelSet configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getLevelSetConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        XPath xpath = WWXML.makeXPath();

        // Title and cache name properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.DATASET_NAME, "DatasetName", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);

        // Service properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE, "Service/URL", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE_NAME, "Service/@serviceName", xpath);

        WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
        WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate", DATE_TIME_PATTERN, xpath);

        // Image format properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.FORMAT_SUFFIX, "FormatSuffix", xpath);

        // Tile structure properties.
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.NUM_LEVELS, "NumLevels/@count", xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.NUM_EMPTY_LEVELS, "NumLevels/@numEmpty", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.INACTIVE_LEVELS, "NumLevels/@inactive", xpath);
        WWXML.checkAndSetSectorParam(domElement, params, AVKey.SECTOR, "Sector", xpath);
        WWXML.checkAndSetSectorResolutionParam(domElement, params, AVKey.SECTOR_RESOLUTION_LIMITS,
            "SectorResolutionLimit", xpath);
        WWXML.checkAndSetLatLonParam(domElement, params, AVKey.TILE_ORIGIN, "TileOrigin/LatLon", xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.TILE_WIDTH, "TileSize/Dimension/@width", xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.TILE_HEIGHT, "TileSize/Dimension/@height", xpath);
        WWXML.checkAndSetLatLonParam(domElement, params, AVKey.LEVEL_ZERO_TILE_DELTA, "LevelZeroTileDelta/LatLon",
            xpath);

        // Retrieval properties.
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.MAX_ABSENT_TILE_ATTEMPTS,
            "AbsentTiles/MaxAttempts", xpath);
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL,
            "AbsentTiles/MinCheckInterval/Time", xpath);

        return params;
    }

    /**
     * Gathers LevelSet configuration parameters from a specified LevelSet reference. This writes output as key-value
     * pairs params. If a parameter from the XML document already exists in params, that parameter is ignored. Supported
     * key and parameter names are: <table> <th><td>Parameter</td><td>Element Path</td><td>Type</td></th> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DATASET_NAME}</td><td>First Level's dataset</td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#DATA_CACHE_NAME}</td><td>First Level's
     * cacheName</td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SERVICE}</td><td>First Level's
     * service</td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#EXPIRY_TIME}</td><td>First
     * Level's expiryTime</td><td>Long</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#FORMAT_SUFFIX}</td><td>FirstLevel's
     * formatSuffix</td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#NUM_LEVELS}</td><td>numLevels</td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#NUM_EMPTY_LEVELS}</td><td>1 + index of first non-empty
     * Level</td><td>Integer</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#INACTIVE_LEVELS}</td><td>Comma
     * delimited string of Level numbers</td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR}</td><td>sector</td><td>{@link
     * gov.nasa.worldwind.geom.Sector}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR_RESOLUTION_LIMITS}</td><td>sectorLevelLimits</td>
     * <td>{@link LevelSet.SectorResolution}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_ORIGIN}</td><td>tileOrigin</td><td>{@link
     * gov.nasa.worldwind.geom.LatLon}</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_WIDTH}</td><td>First
     * Level's tileWidth<td><td>Integer</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_HEIGHT}</td><td>First
     * Level's tileHeight</td><td>Integer</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#LEVEL_ZERO_TILE_DELTA}</td><td>levelZeroTileDelta</td><td>LatLon</td></tr>
     * </table>
     *
     * @param levelSet the LevelSet reference to gather configuration parameters from.
     * @param params   the output key-value pairs which receive the LevelSet configuration parameters. A null reference
     *                 is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getLevelSetConfigParams(LevelSet levelSet, AVList params)
    {
        if (levelSet == null)
        {
            String message = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        Level firstLevel = levelSet.getFirstLevel();

        // Title and cache name properties.
        String s = params.getStringValue(AVKey.DATASET_NAME);
        if (s == null || s.length() == 0)
        {
            s = firstLevel.getDataset();
            if (s != null && s.length() > 0)
            {
                params.setValue(AVKey.DATASET_NAME, s);
            }
        }

        s = params.getStringValue(AVKey.DATA_CACHE_NAME);
        if (s == null || s.length() == 0)
        {
            s = firstLevel.getCacheName();
            if (s != null && s.length() > 0)
            {
                params.setValue(AVKey.DATA_CACHE_NAME, s);
            }
        }

        // Service properties.
        s = params.getStringValue(AVKey.SERVICE);
        if (s == null || s.length() == 0)
        {
            s = firstLevel.getService();
            if (s != null && s.length() > 0)
            {
                params.setValue(AVKey.SERVICE, s);
            }
        }

        Object o = params.getValue(AVKey.EXPIRY_TIME);
        if (o == null)
        {
            // If the expiry time is zero or negative, then treat it as an uninitialized value.
            long l = firstLevel.getExpiryTime();
            if (l > 0)
            {
                params.setValue(AVKey.EXPIRY_TIME, l);
            }
        }

        // Image format properties.
        s = params.getStringValue(AVKey.FORMAT_SUFFIX);
        if (s == null || s.length() == 0)
        {
            s = firstLevel.getFormatSuffix();
            if (s != null && s.length() > 0)
            {
                params.setValue(AVKey.FORMAT_SUFFIX, s);
            }
        }

        // Tile structure properties.
        o = params.getValue(AVKey.NUM_LEVELS);
        if (o == null)
        {
            params.setValue(AVKey.NUM_LEVELS, levelSet.getNumLevels());
        }

        o = params.getValue(AVKey.NUM_EMPTY_LEVELS);
        if (o == null)
        {
            params.setValue(AVKey.NUM_EMPTY_LEVELS, getNumEmptyLevels(levelSet));
        }

        s = params.getStringValue(AVKey.INACTIVE_LEVELS);
        if (s == null || s.length() == 0)
        {
            s = getInactiveLevels(levelSet);
            if (s != null && s.length() > 0)
            {
                params.setValue(AVKey.INACTIVE_LEVELS, s);
            }
        }

        o = params.getValue(AVKey.SECTOR);
        if (o == null)
        {
            Sector sector = levelSet.getSector();
            if (sector != null)
            {
                params.setValue(AVKey.SECTOR, sector);
            }
        }

        o = params.getValue(AVKey.SECTOR_RESOLUTION_LIMITS);
        if (o == null)
        {
            LevelSet.SectorResolution[] srs = levelSet.getSectorLevelLimits();
            if (srs != null && srs.length > 0)
            {
                params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS, srs);
            }
        }

        o = params.getValue(AVKey.TILE_ORIGIN);
        if (o == null)
        {
            LatLon ll = levelSet.getTileOrigin();
            if (ll != null)
            {
                params.setValue(AVKey.TILE_ORIGIN, ll);
            }
        }

        o = params.getValue(AVKey.TILE_WIDTH);
        if (o == null)
        {
            params.setValue(AVKey.TILE_WIDTH, firstLevel.getTileWidth());
        }

        o = params.getValue(AVKey.TILE_HEIGHT);
        if (o == null)
        {
            params.setValue(AVKey.TILE_HEIGHT, firstLevel.getTileHeight());
        }

        o = params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA);
        if (o == null)
        {
            LatLon ll = levelSet.getLevelZeroTileDelta();
            if (ll != null)
            {
                params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, ll);
            }
        }

        // Note: retrieval properties MAX_ABSENT_TILE_ATTEMPTS and MIN_ABSENT_TILE_CHECK_INTERVAL are initialized
        // through the AVList constructor on LevelSet and Level. Rather than expose those properties in Level, we rely
        // on the caller to gather those properties via the AVList used to construct the LevelSet.

        return params;
    }

    protected static int getNumEmptyLevels(LevelSet levelSet)
    {
        int i;
        for (i = 0; i < levelSet.getNumLevels(); i++)
        {
            if (!levelSet.getLevel(i).isEmpty())
            {
                break;
            }
        }

        return i;
    }

    protected static String getInactiveLevels(LevelSet levelSet)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < levelSet.getNumLevels(); i++)
        {
            if (!levelSet.getLevel(i).isActive())
            {
                if (sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append(i);
            }
        }

        return (sb.length() > 0) ? sb.toString() : null;
    }

    //**************************************************************//
    //********************  Installed DataDescriptor Configuration  //
    //**************************************************************//

    /**
     * Returns true if a specified DOM document is a DataDescriptor configuration document, and false otherwise.
     *
     * @param domElement the DOM document in question.
     *
     * @return true if the document is a DataDescriptor configuration document; false otherwise.
     *
     * @throws IllegalArgumentException if document is null.
     */
    public static boolean isInstalledDataDescriptorConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Element[] els = WWXML.getElements(domElement, "/dataDescriptor", null);

        return els != null && els.length > 0;
    }

    /**
     * Transforms a DataDescriptor configuration document to a standard layer or elevation model configuration document,
     * depending on the contents of the {@link org.w3c.dom.Element}.
     *
     * @param domElement DataDescriptor document to transform.
     *
     * @return standard Layer or ElevationModel document, or null if the DataDescriptor cannot be transformed to a
     *         standard document.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static Document transformInstalledDataDescriptorConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        Element[] els = WWXML.getElements(domElement, "/dataDescriptor/property[@name=\"dataSet\"]", xpath);
        if (els == null || els.length == 0)
        {
            return null;
        }

        // Ignore all but the first dataSet element.
        Document outDoc = WWXML.createDocumentBuilder(true).newDocument();
        transformDataDescriptorDataSet(els[0], outDoc, xpath);

        return outDoc;
    }

    protected static void transformDataDescriptorDataSet(Element context, Document outDoc, XPath xpath)
    {
        String s = WWXML.getText(context, "property[@name=\"gov.nasa.worldwind.avkey.DataType\"]", xpath);

        // ElevationModel output.
        if (s != null && s.equals("gov.nasa.worldwind.avkey.TiledElevations"))
        {
            Element el = WWXML.setDocumentElement(outDoc, "ElevationModel");
            WWXML.setIntegerAttribute(el, "version", 1);
            transformDataDescriptorCommonElements(context, el, xpath);
            transformDataDescriptorElevationModelElements(context, el, xpath);
        }
        // Default to Layer output.
        else
        {
            Element el = WWXML.setDocumentElement(outDoc, "Layer");
            WWXML.setIntegerAttribute(el, "version", 1);
            WWXML.setTextAttribute(el, "layerType", "TiledImageLayer");
            transformDataDescriptorCommonElements(context, el, xpath);
            transformDataDescriptorLayerElements(context, el, xpath);
        }
    }

    protected static void transformDataDescriptorCommonElements(Element context, Element outElem, XPath xpath)
    {
        // Display name and datset name properties.
        String s = WWXML.getText(context, "property[@name=\"gov.nasa.worldwind.avkey.DatasetNameKey\"]", xpath);
        if (s != null && s.length() != 0)
        {
            WWXML.appendText(outElem, "DisplayName", s);
            WWXML.appendText(outElem, "DatasetName", s);
        }

        // Service properties.
        // DataDescriptor documents always describe an offline pyramid of tiled imagery in the file store, Therefore we
        // define the service as "Offline".
        Element el = WWXML.appendElementPath(outElem, "Service");
        WWXML.setTextAttribute(el, "serviceName", "Offline");

        // Image format properties.
        s = WWXML.getText(context, "property[@name=\"gov.nasa.worldwind.avkey.FormatSuffixKey\"]", xpath);
        if (s != null && s.length() != 0)
        {
            WWXML.appendText(outElem, "FormatSuffix", s);

            // DataDescriptor documents contain a format suffix, but not image format type. Convert the format suffix
            // to a mime type, then use it to populate the ImageFormat and AvailableImageFormat elements in the
            // transformed Layer or ElevationModel configuration document.
            String mimeType = WWIO.makeMimeTypeForSuffix(s);
            if (mimeType != null && mimeType.length() != 0)
            {
                WWXML.appendText(outElem, "ImageFormat", mimeType);
                WWXML.appendText(outElem, "AvailableImageFormats/ImageFormat", mimeType);
            }
        }

        // Tile structure properties.
        Integer numLevels = WWXML.getInteger(context, "property[@name=\"gov.nasa.worldwind.avkey.NumLevels\"]", xpath);
        Integer numEmptyLevels = WWXML.getInteger(context,
            "property[@name=\"gov.nasa.worldwind.avkey.NumEmptyLevels\"]", xpath);
        if (numLevels != null)
        {
            el = WWXML.appendElementPath(outElem, "NumLevels");
            WWXML.setIntegerAttribute(el, "count", numLevels);
            WWXML.setIntegerAttribute(el, "numEmpty", (numEmptyLevels != null) ? numEmptyLevels : 0);
        }

        // Note the upper case K in "avKey". This was a typo in AVKey.SECTOR, and is intentionally reproduced here.
        Sector sector = getDataDescriptorSector(context, "property[@name=\"gov.nasa.worldwind.avKey.Sector\"]", xpath);
        if (sector != null)
        {
            WWXML.appendSector(outElem, "Sector", sector);
        }

        LatLon ll = getDataDescriptorLatLon(context, "property[@name=\"gov.nasa.worldwind.avkey.TileOrigin\"]", xpath);
        if (ll != null)
        {
            WWXML.appendLatLon(outElem, "TileOrigin/LatLon", ll);
        }

        ll = getDataDescriptorLatLon(context, "property[@name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta\"]", xpath);
        if (ll != null)
        {
            WWXML.appendLatLon(outElem, "LevelZeroTileDelta/LatLon", ll);
        }

        Integer tileWidth = WWXML.getInteger(context, "property[@name=\"gov.nasa.worldwind.avkey.TileWidthKey\"]",
            xpath);
        Integer tileHeight = WWXML.getInteger(context, "property[@name=\"gov.nasa.worldwind.avkey.TileHeightKey\"]",
            xpath);
        if (tileWidth != null && tileHeight != null)
        {
            el = WWXML.appendElementPath(outElem, "TileSize/Dimension");
            WWXML.setIntegerAttribute(el, "width", tileWidth);
            WWXML.setIntegerAttribute(el, "height", tileHeight);
        }
    }

    protected static void transformDataDescriptorElevationModelElements(Element context, Element outElem,
        XPath xpath)
    {
        // Image format properties.
        Element el = WWXML.appendElementPath(outElem, "DataType");

        String pixelType = WWXML.getText(context, "property[@name=\"gov.nasa.worldwind.avkey.PixelType\"]", xpath);
        if (pixelType != null && pixelType.length() != 0)
        {
            WWXML.setTextAttribute(el, "type", WWXML.dataTypeAsText(pixelType));
        }

        String byteOrder = WWXML.getText(context, "property[@name=\"gov.nasa.worldwind.avkey.ByteOrder\"]", xpath);
        if (byteOrder != null && byteOrder.length() != 0)
        {
            WWXML.setTextAttribute(el, "byteOrder", WWXML.byteOrderAsText(byteOrder));
        }

        // Data descriptor files are written with the property "gov.nasa.worldwind.avkey.MissingDataValue". But it
        // means the value that denotes a missing data point, and not the value that replaces missing values.
        // Translate that key here to MissingDataSignal, so it is properly understood by the World Wind API
        // (esp. BasicElevationModel).
        Double d = WWXML.getDouble(context, "property[@name=\"gov.nasa.worldwind.avkey.MissingDataValue\"]", xpath);
        if (d != null)
        {
            el = WWXML.appendElementPath(outElem, "MissingData");
            WWXML.setDoubleAttribute(el, "signal", d);
        }

        // DataDescriptor documents always describe an offline pyramid of tiled imagery or elevations in the file
        // store. Therefore we can safely assume that network retrieval should be disabled.

        // Optional boolean properties.
        WWXML.appendBoolean(outElem, "NetworkRetrievalEnabled", false);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected static void transformDataDescriptorLayerElements(Element context, Element outElem, XPath xpath)
    {
        // Set the texture format to DDS. If the texture data is already in DDS format, this parameter is benign.
        WWXML.appendText(outElem, "TextureFormat", DEFAULT_TEXTURE_FORMAT);

        // DataDescriptor documents always describe an offline pyramid of tiled imagery or elevations in the file
        // store. Therefore we can safely assume that network retrieval should be disabled. Because we know nothing
        // about the nature of the imagery, it's best to enable mipmapping and transparent textures by default.
        WWXML.appendBoolean(outElem, "NetworkRetrievalEnabled", false);
        WWXML.appendBoolean(outElem, "UseMipMaps", true);
        WWXML.appendBoolean(outElem, "UseTransparentTextures", true);
    }

    protected static LatLon getDataDescriptorLatLon(Element context, String path, XPath xpath)
    {
        Element el = (path == null) ? context : WWXML.getElement(context, path, xpath);
        if (el == null)
        {
            return null;
        }

        Double latDegrees = WWXML.getDouble(el, "property[@name=\"latitudeDegrees\"]", xpath);
        Double lonDegrees = WWXML.getDouble(el, "property[@name=\"longitudeDegrees\"]", xpath);
        if (latDegrees == null || lonDegrees == null)
        {
            return null;
        }

        return LatLon.fromDegrees(latDegrees, lonDegrees);
    }

    protected static Sector getDataDescriptorSector(Element context, String path, XPath xpath)
    {
        Element el = (path == null) ? context : WWXML.getElement(context, path, xpath);
        if (el == null)
        {
            return null;
        }

        Double minLatDegrees = WWXML.getDouble(el, "property[@name=\"minLatitudeDegrees\"]", xpath);
        Double maxLatDegrees = WWXML.getDouble(el, "property[@name=\"maxLatitudeDegrees\"]", xpath);
        Double minLonDegrees = WWXML.getDouble(el, "property[@name=\"minLongitudeDegrees\"]", xpath);
        Double maxLonDegrees = WWXML.getDouble(el, "property[@name=\"maxLongitudeDegrees\"]", xpath);

        if (minLatDegrees == null || maxLatDegrees == null || minLonDegrees == null || maxLonDegrees == null)
        {
            return null;
        }

        return Sector.fromDegrees(minLatDegrees, maxLatDegrees, minLonDegrees, maxLonDegrees);
    }

    //**************************************************************//
    //********************  World Wind .NET LayerSet Configuration  //
    //**************************************************************//

    /**
     * Returns true if a specified document is a World Wind .NET LayerSet configuration document, and false otherwise.
     *
     * @param domElement the document in question.
     *
     * @return true if the document is a LayerSet configuration document; false otherwise.
     *
     * @throws IllegalArgumentException if document is null.
     */
    public static boolean isWWDotNetLayerSetConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();
        Element[] elements = WWXML.getElements(domElement, "/LayerSet", xpath);

        return elements != null && elements.length > 0;
    }

    /**
     * Returns true if a specified XML event is the root of a World Wind .NET LayerSet configuration document, and false
     * otherwise.
     *
     * @param event the XML event in question.
     *
     * @return true if the event is a LayerSet configuration document element; false otherwise.
     *
     * @throws IllegalArgumentException if the event is null.
     */
    public static boolean isWWDotNetLayerSetConfigEvent(XMLEvent event)
    {
        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!event.isStartElement())
        {
            return false;
        }

        String name = WWXML.getUnqalifiedName(event.asStartElement());
        return name != null && name.equals("LayerSet");
    }

    /**
     * Parses World Wind .NET LayerSet configuration parameters from the specified document. This writes output as
     * key-value pairs to params. If a parameter from the LayerSet document already exists in params, that parameter is
     * ignored. Supported key and parameter names are: <table> <tr><th>Parameter</th><th>Element
     * Path</th><th>Type</th></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#DISPLAY_NAME}</td><td>QuadTileSet/Name<td></td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#DATASET_NAME}</td><td>QuadTileSet/Name<td></td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#OPACITY}</td><td>QuadTileSet/Opacity<td></td><td>Double</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SERVICE_NAME}</td><td>"Offline" (string
     * constant)<td></td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#FORMAT_SUFFIX}</td><td>QuadTileSet/ImageAccessor/ImageFileExtension<td></td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#IMAGE_FORMAT}</td><td>QuadTileSet/ImageAccessor/ImageFileExtension
     * (converted to mime type)<td></td><td>String</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#AVAILABLE_IMAGE_FORMATS}</td><td>QuadTileSet/ImageAccessor/ImageFileExtension
     * (converted to mime type)<td></td><td>String array</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#NUM_LEVELS}</td><td>QuadTileSet/ImageAccessor/NumberLevels<td></td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#NUM_EMPTY_LEVELS}</td><td>0 (integer
     * constant)<td></td><td>Integer</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#SECTOR}</td><td>QuadTileSet/BoundingBox<td></td><td>Sector</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_ORIGIN}</td><td>(-90, -180) (geographic location
     * constant)<td></td><td>LatLon</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#LEVEL_ZERO_TILE_DELTA}</td><td>QuadTileSet/ImageAccessor/LevelZeroTileSizeDegrees<td></td><td>LatLon</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_WIDTH}</td><td>QuadTileSet/ImageAccessor/TextureSizePixels<td></td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TILE_HEIGHT}</td><td>QuadTileSet/ImageAccessor/TextureSizePixels<td></td><td>Integer</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#NETWORK_RETRIEVAL_ENABLED}</td><td>false (boolean
     * constant)<td></td><td>Boolean</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#TEXTURE_FORMAT}</td><td>"image/dds"<td></td><td>String</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#USE_MIP_MAPS}</td><td>true (boolean
     * constant)<td></td><td>Boolean</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#USE_TRANSPARENT_TEXTURES}</td><td>true
     * (boolean constant)<td></td><td>Boolean</td></tr> </table>
     *
     * @param domElement the XML document root to parse for LayerSet configuration parameters.
     * @param params     the output key-value pairs which receive the LayerSet configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getWWDotNetLayerSetConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        // Find the first QuadTileSet element in this LayerSet document.
        Element el = WWXML.getElement(domElement, "QuadTileSet", xpath);
        if (el == null)
        {
            return params;
        }

        if (params == null)
        {
            params = new AVListImpl();
        }

        // Title and cache name properties.
        WWXML.checkAndSetStringParam(el, params, AVKey.DISPLAY_NAME, "Name", xpath);
        WWXML.checkAndSetStringParam(el, params, AVKey.DATASET_NAME, "Name", xpath);

        // Display properties.
        if (params.getValue(AVKey.OPACITY) == null)
        {
            Double d = WWXML.getDouble(el, "Opacity", xpath);
            if (d != null)
            {
                params.setValue(AVKey.OPACITY, d / 255d);
            }
        }

        // Service properties.
        // LayerSet documents always describe an offline pyramid of tiled imagery in the file store, Therefore we define
        // the service as "Offline".
        if (params.getValue(AVKey.SERVICE_NAME) == null)
        {
            params.setValue(AVKey.SERVICE_NAME, "Offline");
        }

        // Image format properties.
        if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
        {
            String s = WWXML.getText(el, "ImageAccessor/ImageFileExtension", xpath);
            if (s != null && s.length() != 0)
            {
                if (!s.startsWith("."))
                {
                    s = "." + s;
                }
                params.setValue(AVKey.FORMAT_SUFFIX, s);
            }
        }

        // LayerSet documents contain a format suffix, but not image format type. Convert the format suffix to a
        // mime type, then use it to populate the IMAGE_FORMAT and AVAILABLE_IMAGE_FORMAT properties.
        if (params.getValue(AVKey.FORMAT_SUFFIX) != null)
        {
            String s = WWIO.makeMimeTypeForSuffix(params.getValue(AVKey.FORMAT_SUFFIX).toString());
            if (s != null)
            {
                if (params.getValue(AVKey.IMAGE_FORMAT) == null)
                {
                    params.setValue(AVKey.IMAGE_FORMAT, s);
                }
                if (params.getValue(AVKey.AVAILABLE_IMAGE_FORMATS) == null)
                {
                    params.setValue(AVKey.AVAILABLE_IMAGE_FORMATS, new String[] {s});
                }
            }
        }

        // Set the texture format to DDS. If the texture data is already in DDS format, this parameter is benign.
        if (params.getValue(AVKey.TEXTURE_FORMAT) == null)
        {
            params.setValue(AVKey.TEXTURE_FORMAT, DEFAULT_TEXTURE_FORMAT);
        }

        // Tile structure properties.
        WWXML.checkAndSetIntegerParam(el, params, AVKey.NUM_LEVELS, "ImageAccessor/NumberLevels", xpath);

        if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
        {
            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        }

        if (params.getValue(AVKey.SECTOR) == null)
        {
            Sector s = getWWDotNetLayerSetSector(el, "BoundingBox", xpath);
            if (s != null)
            {
                params.setValue(AVKey.SECTOR, s);
            }
        }

        if (params.getValue(AVKey.TILE_ORIGIN) == null)
        {
            params.setValue(AVKey.TILE_ORIGIN, new LatLon(Angle.NEG90, Angle.NEG180));
        }

        if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
        {
            LatLon ll = getWWDotNetLayerSetLatLon(el, "ImageAccessor/LevelZeroTileSizeDegrees", xpath);
            if (ll != null)
            {
                params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, ll);
            }
        }

        Integer tileDimension = WWXML.getInteger(el, "ImageAccessor/TextureSizePixels", xpath);
        if (tileDimension != null)
        {
            if (params.getValue(AVKey.TILE_WIDTH) == null)
            {
                params.setValue(AVKey.TILE_WIDTH, tileDimension);
            }
            if (params.getValue(AVKey.TILE_HEIGHT) == null)
            {
                params.setValue(AVKey.TILE_HEIGHT, tileDimension);
            }
        }

        // LayerSet documents always describe an offline pyramid of tiled imagery in the file store. Therefore we can
        // safely assume that network retrieval should be disabled. Because we know nothing about the nature of the
        // imagery, it's best to enable mipmapping and transparent textures by default.
        if (params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED) == null)
        {
            params.setValue(AVKey.NETWORK_RETRIEVAL_ENABLED, false);
        }
        if (params.getValue(AVKey.USE_MIP_MAPS) == null)
        {
            params.setValue(AVKey.USE_MIP_MAPS, true);
        }
        if (params.getValue(AVKey.USE_TRANSPARENT_TEXTURES) == null)
        {
            params.setValue(AVKey.USE_TRANSPARENT_TEXTURES, true);
        }

        return params;
    }

    /**
     * Transforms a World Wind .NET LayerSet configuration document to a standard layer configuration document.
     *
     * @param domElement LayerSet document to transform.
     *
     * @return standard Layer document, or null if the LayerSet document cannot be transformed to a standard document.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static Document transformWWDotNetLayerSetConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        Element[] els = WWXML.getElements(domElement, "/LayerSet/QuadTileSet", xpath);
        if (els == null || els.length == 0)
        {
            return null;
        }

        // Ignore all but the first QuadTileSet element.
        Document outDoc = WWXML.createDocumentBuilder(true).newDocument();
        transformWWDotNetLayerSet(els[0], outDoc, xpath);

        return outDoc;
    }

    protected static void transformWWDotNetLayerSet(Element context, Document outDoc, XPath xpath)
    {
        Element el = WWXML.setDocumentElement(outDoc, "Layer");
        WWXML.setIntegerAttribute(el, "version", 1);
        WWXML.setTextAttribute(el, "layerType", "TiledImageLayer");

        transformWWDotNetQuadTileSet(context, el, xpath);
    }

    protected static void transformWWDotNetQuadTileSet(Element context, Element outElem, XPath xpath)
    {
        // Display name and dataset name properties.
        String s = WWXML.getText(context, "Name", xpath);
        if (s != null && s.length() != 0)
        {
            WWXML.appendText(outElem, "DisplayName", s);
            WWXML.appendText(outElem, "DatasetName", s);
        }

        // Display properties.
        Double d = WWXML.getDouble(context, "Opacity", xpath);
        if (d != null)
        {
            WWXML.appendDouble(outElem, "Opacity", d / 255d);
        }

        // Service properties.
        // LayerSet documents always describe an offline pyramid of tiled imagery in the file store, Therefore we define
        // the service as "Offline".
        Element el = WWXML.appendElementPath(outElem, "Service");
        WWXML.setTextAttribute(el, "serviceName", "Offline");

        // Image format properties.
        s = WWXML.getText(context, "ImageAccessor/ImageFileExtension", xpath);
        if (s != null && s.length() != 0)
        {
            if (!s.startsWith("."))
            {
                s = "." + s;
            }
            WWXML.appendText(outElem, "FormatSuffix", s);

            // LayerSet documents contain a format suffix, but not image format type. Convert the format suffix to a
            // mime type, then use it to populate the ImageFormat and AvailableImageFormat elements in the transformed
            // Layer configuration document.
            String mimeType = WWIO.makeMimeTypeForSuffix(s);
            if (mimeType != null && mimeType.length() != 0)
            {
                WWXML.appendText(outElem, "ImageFormat", mimeType);
                WWXML.appendText(outElem, "AvailableImageFormats/ImageFormat", mimeType);
            }
        }

        // Set the texture format to DDS. If the texture data is already in DDS format, this parameter is benign.
        WWXML.appendText(outElem, "TextureFormat", DEFAULT_TEXTURE_FORMAT);

        // Tile structure properties.
        Integer numLevels = WWXML.getInteger(context, "ImageAccessor/NumberLevels", xpath);
        if (numLevels != null)
        {
            el = WWXML.appendElementPath(outElem, "NumLevels");
            WWXML.setIntegerAttribute(el, "count", numLevels);
            WWXML.setIntegerAttribute(el, "numEmpty", 0);
        }

        Sector sector = getWWDotNetLayerSetSector(context, "BoundingBox", xpath);
        if (sector != null)
        {
            WWXML.appendSector(outElem, "Sector", sector);
        }

        WWXML.appendLatLon(outElem, "TileOrigin/LatLon", new LatLon(Angle.NEG90, Angle.NEG180));

        LatLon ll = getWWDotNetLayerSetLatLon(context, "ImageAccessor/LevelZeroTileSizeDegrees", xpath);
        if (ll != null)
        {
            WWXML.appendLatLon(outElem, "LevelZeroTileDelta/LatLon", ll);
        }

        Integer tileDimension = WWXML.getInteger(context, "ImageAccessor/TextureSizePixels", xpath);
        if (tileDimension != null)
        {
            el = WWXML.appendElementPath(outElem, "TileSize/Dimension");
            WWXML.setIntegerAttribute(el, "width", tileDimension);
            WWXML.setIntegerAttribute(el, "height", tileDimension);
        }

        // LayerSet documents always describe an offline pyramid of tiled imagery in the file store. Therefore we can
        // safely assume that network retrieval should be disabled. Because we know nothing about the nature of
        // the imagery, it's best to enable mipmapping and transparent textures by default.
        WWXML.appendBoolean(outElem, "NetworkRetrievalEnabled", false);
        WWXML.appendBoolean(outElem, "UseMipMaps", true);
        WWXML.appendBoolean(outElem, "UseTransparentTextures", true);
    }

    protected static LatLon getWWDotNetLayerSetLatLon(Element context, String path, XPath xpath)
    {
        Double degrees = WWXML.getDouble(context, path, xpath);
        if (degrees == null)
        {
            return null;
        }

        return LatLon.fromDegrees(degrees, degrees);
    }

    protected static Sector getWWDotNetLayerSetSector(Element context, String path, XPath xpath)
    {
        Element el = (path == null) ? context : WWXML.getElement(context, path, xpath);
        if (el == null)
        {
            return null;
        }

        Double minLatDegrees = WWXML.getDouble(el, "South/Value", xpath);
        Double maxLatDegrees = WWXML.getDouble(el, "North/Value", xpath);
        Double minLonDegrees = WWXML.getDouble(el, "West/Value", xpath);
        Double maxLonDegrees = WWXML.getDouble(el, "East/Value", xpath);

        if (minLatDegrees == null || maxLatDegrees == null || minLonDegrees == null || maxLonDegrees == null)
        {
            return null;
        }

        return Sector.fromDegrees(minLatDegrees, maxLatDegrees, minLonDegrees, maxLonDegrees);
    }
}
