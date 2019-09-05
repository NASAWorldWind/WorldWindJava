/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import java.util.List;

/**
 * @author tag
 * @version $Id: AbstractElevationModel.java 3420 2015-09-10 23:25:43Z tgaskins $
 */
abstract public class AbstractElevationModel extends WWObjectImpl implements ElevationModel
{
    protected FileStore dataFileStore = WorldWind.getDataFileStore();
    protected double missingDataFlag = -Double.MAX_VALUE;
    protected double missingDataValue = 0;

    protected boolean networkRetrievalEnabled = true;
    protected long expiryTime = 0;
    protected boolean enabled = true;

    public void dispose()
    {
    }

    public String getName()
    {
        Object n = this.getValue(AVKey.DISPLAY_NAME);

        return n != null ? n.toString() : this.toString();
    }

    public void setName(String name)
    {
        this.setValue(AVKey.DISPLAY_NAME, name);
    }

    public String toString()
    {
        Object n = this.getValue(AVKey.DISPLAY_NAME);

        return n != null ? n.toString() : super.toString();
    }

    public boolean isNetworkRetrievalEnabled()
    {
        return this.networkRetrievalEnabled;
    }

    public void setNetworkRetrievalEnabled(boolean enabled)
    {
        this.networkRetrievalEnabled = enabled;
    }

    public long getExpiryTime()
    {
        return this.expiryTime;
    }

    public void setExpiryTime(long expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public double getMissingDataSignal()
    {
        return missingDataFlag;
    }

    public void setMissingDataSignal(double missingDataFlag)
    {
        this.missingDataFlag = missingDataFlag;
    }

    public double getMissingDataReplacement()
    {
        return missingDataValue;
    }

    public void setMissingDataReplacement(double missingDataValue)
    {
        this.missingDataValue = missingDataValue;
    }

    public double getDetailHint(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return 0.0;
    }

    public FileStore getDataFileStore()
    {
        return dataFileStore;
    }

    public void setDataFileStore(FileStore dataFileStore)
    {
        this.dataFileStore = dataFileStore;
    }

    public double getElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double e = this.getUnmappedElevation(latitude, longitude);
        return e == this.missingDataFlag ? this.missingDataValue : e;
    }

    public double[] getElevations(Sector sector, List<? extends LatLon> latLons, double[] targetResolutions,
        double[] elevations)
    {
        return new double[] {this.getElevations(sector, latLons, targetResolutions[0], elevations)};
    }

    public double[] getUnmappedElevations(Sector sector, List<? extends LatLon> latLons, double[] targetResolutions,
        double[] elevations)
    {
        return new double[] {this.getElevations(sector, latLons, targetResolutions[0], elevations)};
    }

    public double[] getBestResolutions(Sector sector)
    {
        return new double[] {this.getBestResolution(sector)};
    }

    public String getRestorableState()
    {
        return null;
    }

    public void restoreState(String stateInXml)
    {
        String message = Logging.getMessage("RestorableSupport.RestoreNotSupported");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }

    public void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
        throws Exception
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (latlons == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tileWidth < 1)
        {
            String msg = Logging.getMessage("generic.SizeOutOfRange", tileWidth);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < latlons.size() || tileWidth > latlons.size())
        {
            String msg = Logging.getMessage("ElevationModel.ElevationsBufferTooSmall", latlons.size());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            double e = this.getUnmappedElevation(ll.getLatitude(), ll.getLongitude());
            if (e != this.getMissingDataSignal() && !this.isTransparentValue(e))
                buffer[i] = e;
        }
    }

    protected boolean isTransparentValue(Double value)
    {
        return ((value == null || value.equals(this.getMissingDataSignal()))
            && this.getMissingDataReplacement() == this.getMissingDataSignal());
    }

    //**************************************************************//
    //********************  Configuration  *************************//
    //**************************************************************//

    /**
     * Returns true if a specified DOM document is an ElevationModel configuration document, and false otherwise.
     *
     * @param domElement the DOM document in question.
     *
     * @return true if the document is an ElevationModel configuration document; false otherwise.
     *
     * @throws IllegalArgumentException if document is null.
     */
    public static boolean isElevationModelConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();
        Element[] elements = WWXML.getElements(domElement, "//ElevationModel", xpath);

        return elements != null && elements.length > 0;
    }

    /**
     * Appends elevation model configuration parameters as elements to the specified context. This appends elements for
     * the following parameters: <table> <caption style="font-weight: bold;">Parameters</caption>
     * <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> 
     * <tr><td>{@link
     * AVKey#DISPLAY_NAME}</td><td>DisplayName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#NETWORK_RETRIEVAL_ENABLED}</td><td>NetworkRetrievalEnabled</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#MISSING_DATA_SIGNAL}</td><td>MissingData/@signal</td><td>Double</td></tr> <tr><td>{@link
     * AVKey#MISSING_DATA_REPLACEMENT}</td><td>MissingData/@replacement</td><td>Double</td></tr> <tr><td>{@link
     * AVKey#DETAIL_HINT}</td><td>DataDetailHint</td><td>Double</td></tr> </table>
     *
     * @param params  the key-value pairs which define the elevation model configuration parameters.
     * @param context the XML document root on which to append elevation model configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createElevationModelConfigElements(AVList params, Element context)
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

        WWXML.checkAndAppendTextElement(params, AVKey.DISPLAY_NAME, context, "DisplayName");
        WWXML.checkAndAppendBooleanElement(params, AVKey.NETWORK_RETRIEVAL_ENABLED, context, "NetworkRetrievalEnabled");

        if (params.getValue(AVKey.MISSING_DATA_SIGNAL) != null ||
            params.getValue(AVKey.MISSING_DATA_REPLACEMENT) != null)
        {
            Element el = WWXML.getElement(context, "MissingData", null);
            if (el == null)
                el = WWXML.appendElementPath(context, "MissingData");

            Double d = AVListImpl.getDoubleValue(params, AVKey.MISSING_DATA_SIGNAL);
            if (d != null)
                WWXML.setDoubleAttribute(el, "signal", d);

            d = AVListImpl.getDoubleValue(params, AVKey.MISSING_DATA_REPLACEMENT);
            if (d != null)
                WWXML.setDoubleAttribute(el, "replacement", d);
        }

        WWXML.checkAndAppendDoubleElement(params, AVKey.DETAIL_HINT, context, "DataDetailHint");

        return context;
    }

    /**
     * Parses elevation model configuration parameters from the specified DOM document. This writes output as key-value
     * pairs to params. If a parameter from the XML document already exists in params, that parameter is ignored.
     * Supported parameters are: <table> <caption style="font-weight: bold;">Parameters</caption>
     * <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> 
     * <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#DISPLAY_NAME}</td><td>DisplayName</td><td>String</td></tr> <tr><td>{@link
     * gov.nasa.worldwind.avlist.AVKey#NETWORK_RETRIEVAL_ENABLED}</td><td>NetworkRetrievalEnabled</td><td>Boolean</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MISSING_DATA_SIGNAL}</td><td>MissingData/@signal</td><td>Double</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#MISSING_DATA_REPLACEMENT}</td><td>MissingData/@replacement</td><td>Double</td></tr>
     * <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#DETAIL_HINT}</td><td>DataDetailHint</td><td>Double</td></tr>
     * </table>
     *
     * @param domElement the XML document root to parse for elevation model configuration elements.
     * @param params     the output key-value pairs which recieve the elevation model configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getElevationModelConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        XPath xpath = WWXML.makeXPath();

        WWXML.checkAndSetStringParam(domElement, params, AVKey.DISPLAY_NAME, "DisplayName", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.NETWORK_RETRIEVAL_ENABLED, "NetworkRetrievalEnabled",
            xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.MISSING_DATA_SIGNAL, "MissingData/@signal", xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.MISSING_DATA_REPLACEMENT, "MissingData/@replacement",
            xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.DETAIL_HINT, "DataDetailHint", xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.MAX_ABSENT_TILE_ATTEMPTS, "MaxAbsentTileAttempts",
            xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL,
            "MinAbsentTileCheckInterval", xpath);

        return params;
    }

    public double getLocalDataAvailability(Sector sector, Double targetResolution)
    {
        return 1d;
    }

    public double getUnmappedLocalSourceElevation(Angle latitude, Angle longitude)
    {
        return this.getUnmappedElevation(latitude, longitude);
    }
}
