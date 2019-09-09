/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import java.beans.PropertyChangeEvent;

/**
 * @author tag
 * @version $Id: AbstractLayer.java 2254 2014-08-22 17:02:46Z tgaskins $
 */
public abstract class AbstractLayer extends WWObjectImpl implements Layer
{
    private boolean enabled = true;
    private boolean pickable = true;
    private double opacity = 1d;
    private double minActiveAltitude = -Double.MAX_VALUE;
    private double maxActiveAltitude = Double.MAX_VALUE;
    private boolean networkDownloadEnabled = true;
    private long expiryTime = 0;
    private ScreenCredit screenCredit = null;
    private FileStore dataFileStore = WorldWind.getDataFileStore();

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public boolean isPickEnabled()
    {
        return pickable;
    }

    public void setPickEnabled(boolean pickable)
    {
        this.pickable = pickable;
    }

    public void setEnabled(boolean enabled)
    {
        Boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        this.propertyChange(new PropertyChangeEvent(this, "Enabled", oldEnabled, this.enabled));
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

    public double getOpacity()
    {
        return opacity;
    }

    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    public double getMinActiveAltitude()
    {
        return minActiveAltitude;
    }

    public void setMinActiveAltitude(double minActiveAltitude)
    {
        this.minActiveAltitude = minActiveAltitude;
    }

    public double getMaxActiveAltitude()
    {
        return maxActiveAltitude;
    }

    public void setMaxActiveAltitude(double maxActiveAltitude)
    {
        this.maxActiveAltitude = maxActiveAltitude;
    }

    public Double getMinEffectiveAltitude(Double radius)
    {
        return null;
    }

    public Double getMaxEffectiveAltitude(Double radius)
    {
        return null;
    }

    public double getScale()
    {
        Object o = this.getValue(AVKey.MAP_SCALE);
        return o != null && o instanceof Double ? (Double) o : 1;
    }

    public boolean isNetworkRetrievalEnabled()
    {
        return networkDownloadEnabled;
    }

    public void setNetworkRetrievalEnabled(boolean networkDownloadEnabled)
    {
        this.networkDownloadEnabled = networkDownloadEnabled;
    }

    public FileStore getDataFileStore()
    {
        return this.dataFileStore;
    }

    public void setDataFileStore(FileStore fileStore)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.dataFileStore = fileStore;
    }

    public boolean isLayerInView(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return true;
    }

    public boolean isLayerActive(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
            return false;

        double altitude = eyePos.getElevation();
        return altitude >= this.minActiveAltitude && altitude <= this.maxActiveAltitude;
    }

    public void preRender(DrawContext dc)
    {
        if (!this.enabled)
            return; // Don't check for arg errors if we're disabled

        if (null == dc)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc))
            return;

        if (!this.isLayerInView(dc))
            return;

        this.doPreRender(dc);
    }

    /**
     * @param dc the current draw context
     *
     * @throws IllegalArgumentException if <code>dc</code> is null, or <code>dc</code>'s <code>Globe</code> or
     *                                  <code>View</code> is null
     */
    public void render(DrawContext dc)
    {
        if (!this.enabled)
            return; // Don't check for arg errors if we're disabled

        if (null == dc)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc))
            return;

        if (!this.isLayerInView(dc))
            return;

        this.doRender(dc);
    }

    public void pick(DrawContext dc, java.awt.Point point)
    {
        if (!this.enabled)
            return; // Don't check for arg errors if we're disabled

        if (null == dc)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc))
            return;

        if (!this.isLayerInView(dc))
            return;

        this.doPick(dc, point);
    }

    protected void doPick(DrawContext dc, java.awt.Point point)
    {
        // any state that could change the color needs to be disabled, such as GL_TEXTURE, GL_LIGHTING or GL_FOG.
        // re-draw with unique colors
        // store the object info in the selectable objects table
        // read the color under the cursor
        // use the color code as a key to retrieve a selected object from the selectable objects table
        // create an instance of the PickedObject and add to the dc via the dc.addPickedObject() method
    }

    public void dispose() // override if disposal is a supported operation
    {
    }

    protected void doPreRender(DrawContext dc)
    {
    }

    protected abstract void doRender(DrawContext dc);

    public boolean isAtMaxResolution()
    {
        return !this.isMultiResolution();
    }

    public boolean isMultiResolution()
    {
        return false;
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

    public void setExpiryTime(long expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public long getExpiryTime()
    {
        return this.expiryTime;
    }

    protected ScreenCredit getScreenCredit()
    {
        return screenCredit;
    }

    protected void setScreenCredit(ScreenCredit screenCredit)
    {
        this.screenCredit = screenCredit;
    }

    //**************************************************************//
    //********************  Configuration  *************************//
    //**************************************************************//

    /**
     * Returns true if a specified DOM document is a Layer configuration document, and false otherwise.
     *
     * @param domElement the DOM document in question.
     *
     * @return true if the document is a Layer configuration document; false otherwise.
     *
     * @throws IllegalArgumentException if document is null.
     */
    public static boolean isLayerConfigDocument(Element domElement)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();
        Element[] elements = WWXML.getElements(domElement, "//Layer", xpath);

        return elements != null && elements.length > 0;
    }

    /**
     * Appends layer configuration parameters as elements to the specified context. This appends elements for the
     * following parameters: <table> <caption style="font-weight: bold;">Append Elements</caption>
     * <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> <tr><td>{@link
     * AVKey#DISPLAY_NAME}</td><td>DisplayName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#OPACITY}</td><td>Opacity</td><td>Double</td></tr> <tr><td>{@link AVKey#MAX_ACTIVE_ALTITUDE}</td><td>ActiveAltitudes/@max</td><td>Double</td></tr>
     * <tr><td>{@link AVKey#MIN_ACTIVE_ALTITUDE}</td><td>ActiveAltitudes/@min</td><td>Double</td></tr> <tr><td>{@link
     * AVKey#NETWORK_RETRIEVAL_ENABLED}</td><td>NetworkRetrievalEnabled</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#MAP_SCALE}</td><td>MapScale</td><td>Double</td></tr> <tr><td>{@link AVKey#SCREEN_CREDIT}</td><td>ScreenCredit</td><td>ScreenCredit</td></tr>
     * </table>
     *
     * @param params  the key-value pairs which define the layer configuration parameters.
     * @param context the XML document root on which to append layer configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createLayerConfigElements(AVList params, Element context)
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
        WWXML.checkAndAppendDoubleElement(params, AVKey.OPACITY, context, "Opacity");

        Double maxAlt = AVListImpl.getDoubleValue(params, AVKey.MAX_ACTIVE_ALTITUDE);
        Double minAlt = AVListImpl.getDoubleValue(params, AVKey.MIN_ACTIVE_ALTITUDE);
        if (maxAlt != null || minAlt != null)
        {
            Element el = WWXML.appendElementPath(context, "ActiveAltitudes");
            if (maxAlt != null)
                WWXML.setDoubleAttribute(el, "max", maxAlt);
            if (minAlt != null)
                WWXML.setDoubleAttribute(el, "min", minAlt);
        }

        WWXML.checkAndAppendBooleanElement(params, AVKey.NETWORK_RETRIEVAL_ENABLED, context, "NetworkRetrievalEnabled");
        WWXML.checkAndAppendDoubleElement(params, AVKey.MAP_SCALE, context, "MapScale");
        WWXML.checkAndAppendScreenCreditElement(params, AVKey.SCREEN_CREDIT, context, "ScreenCredit");
        WWXML.checkAndAppendBooleanElement(params, AVKey.PICK_ENABLED, context, "PickEnabled");

        return context;
    }

    /**
     * Parses layer configuration parameters from the specified DOM document. This writes output as key-value pairs to
     * params. If a parameter from the XML document already exists in params, that parameter is ignored. Supported key
     * and parameter names are: <table> <caption style="font-weight: bold;">Supported Names</caption>
     * <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> <tr><td>{@link
     * AVKey#DISPLAY_NAME}</td><td>DisplayName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#OPACITY}</td><td>Opacity</td><td>Double</td></tr> <tr><td>{@link AVKey#MAX_ACTIVE_ALTITUDE}</td><td>ActiveAltitudes/@max</td><td>Double</td></tr>
     * <tr><td>{@link AVKey#MIN_ACTIVE_ALTITUDE}</td><td>ActiveAltitudes/@min</td><td>Double</td></tr> <tr><td>{@link
     * AVKey#NETWORK_RETRIEVAL_ENABLED}</td><td>NetworkRetrievalEnabled</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#MAP_SCALE}</td><td>MapScale</td><td>Double</td></tr> <tr><td>{@link AVKey#SCREEN_CREDIT}</td><td>ScreenCredit</td><td>{@link
     * ScreenCredit}</td></tr> </table>
     *
     * @param domElement the XML document root to parse for layer configuration elements.
     * @param params     the output key-value pairs which recieve the layer configuration parameters. A null reference
     *                   is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getLayerConfigParams(Element domElement, AVList params)
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
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.OPACITY, "Opacity", xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.MAX_ACTIVE_ALTITUDE, "ActiveAltitudes/@max", xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.MIN_ACTIVE_ALTITUDE, "ActiveAltitudes/@min", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.NETWORK_RETRIEVAL_ENABLED, "NetworkRetrievalEnabled",
            xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.MAP_SCALE, "MapScale", xpath);
        WWXML.checkAndSetScreenCreditParam(domElement, params, AVKey.SCREEN_CREDIT, "ScreenCredit", xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.MAX_ABSENT_TILE_ATTEMPTS, "MaxAbsentTileAttempts",
            xpath);
        WWXML.checkAndSetIntegerParam(domElement, params, AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL,
            "MinAbsentTileCheckInterval", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.PICK_ENABLED, "PickEnabled", xpath);

        return params;
    }
}
