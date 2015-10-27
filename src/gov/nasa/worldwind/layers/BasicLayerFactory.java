/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.ogc.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Element;

import java.util.*;

/**
 * A factory that creates {@link gov.nasa.worldwind.layers.Layer} instances.
 *
 * @author dcollins
 * @version $Id: BasicLayerFactory.java 2348 2014-09-25 23:35:46Z dcollins $
 */
public class BasicLayerFactory extends BasicFactory
{
    /** Creates an instance of BasicLayerFactory; otherwise does nothing. */
    public BasicLayerFactory()
    {
    }

    /**
     * Creates a layer or layer list from a general configuration source. The source can be one of the following: <ul>
     * <li>a {@link java.net.URL}</li> <li>a {@link java.io.File}</li> <li>a {@link java.io.InputStream}</li> <li>{@link
     * Element}</li> <li>a {@link String} holding a file name, a name of a resource on the classpath, or a string
     * representation of a URL</li> </ul>
     * <p/>
     * For tiled image layers, this maps the <code>serviceName</code> attribute of the <code>Layer/Service</code>
     * element of the XML configuration file to the appropriate base tiled image layer type. Service types recognized
     * are: <ul> <li>"WMS" for layers that draw their data from a WMS web service.</li> <li>"WWTileService" for layers
     * that draw their data from a World Wind tile service.</li> <li>"Offline" for layers that draw their data only from
     * the local cache.</li> </ul>
     *
     * @param configSource the configuration source. See above for supported types.
     *
     * @return a layer or layer list.
     *
     * @throws IllegalArgumentException if the configuration file name is null or an empty string.
     * @throws WWUnrecognizedException  if the layer service type is unrecognized.
     * @throws WWRuntimeException       if object creation fails. The exception indicating the source of the failure is
     *                                  included as the {@link Exception#initCause(Throwable)}.
     */
    public Object createFromConfigSource(Object configSource, AVList params)
    {
        Object layerOrLists = super.createFromConfigSource(configSource, params);

        if (layerOrLists == null)
        {
            String msg = Logging.getMessage("generic.UnrecognizedDocument", configSource);
            throw new WWUnrecognizedException(msg);
        }

        return layerOrLists;
    }

    @Override
    protected Layer doCreateFromCapabilities(OGCCapabilities caps, AVList params)
    {
        String serviceName = caps.getServiceInformation().getServiceName();
        if (serviceName == null || !(serviceName.equalsIgnoreCase(OGCConstants.WMS_SERVICE_NAME)
            || serviceName.contains("WMS")))
        {
            String message = Logging.getMessage("WMS.NotWMSService", serviceName != null ? serviceName : "null");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        if (params.getStringValue(AVKey.LAYER_NAMES) == null)
        {
            // Use the first named layer since no other guidance given
            List<WMSLayerCapabilities> namedLayers = ((WMSCapabilities) caps).getNamedLayers();

            if (namedLayers == null || namedLayers.size() == 0 || namedLayers.get(0) == null)
            {
                String message = Logging.getMessage("WMS.NoLayersFound");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            params.setValue(AVKey.LAYER_NAMES, namedLayers.get(0).getName());
        }

        return new WMSTiledImageLayer((WMSCapabilities) caps, params);
    }

    /**
     * Create the objects described in an XML element containing layer and/or layer-list descriptions. Nested layer
     * lists and included layers are created recursively.
     *
     * @param domElement an XML element describing the layers and/or layer lists.
     * @param params     any properties to apply when creating the included layers.
     *
     * @return a <code>Layer</code>, <code>LayerList</code> or array of <code>LayerList</code>s, as described by the
     *         specified description.
     *
     * @throws Exception if an exception occurs during creation. Exceptions occurring during creation of internal layers
     *                   or layer lists are not re-thrown but are logged. The layer or layer list associated with the
     *                   exception is not contained in the returned object.
     */
    @Override
    protected Object doCreateFromElement(Element domElement, AVList params) throws Exception
    {
        Element[] elements = WWXML.getElements(domElement, "//LayerList", null);
        if (elements != null && elements.length > 0)
            return createLayerLists(elements, params);

        elements = WWXML.getElements(domElement, "./Layer", null);
        if (elements != null && elements.length > 1)
            return createLayerList(elements, params);

        if (elements != null && elements.length == 1)
            return this.createFromLayerDocument(elements[0], params);

        String localName = WWXML.getUnqualifiedName(domElement);
        if (localName != null && localName.equals("Layer"))
            return this.createFromLayerDocument(domElement, params);

        return null;
    }

    /**
     * Create a collection of layer lists and their included layers described by an array of XML layer-list description
     * elements.
     * <p/>
     * Any exceptions occurring during creation of the layer lists or their included layers are logged and not
     * re-thrown. The layers associated with the exceptions are not included in the returned layer list.
     *
     * @param elements the XML elements describing the layer lists to create.
     * @param params   any parameters to apply when creating the included layers.
     *
     * @return an array containing the specified layer lists.
     */
    protected LayerList[] createLayerLists(Element[] elements, AVList params)
    {
        ArrayList<LayerList> layerLists = new ArrayList<LayerList>();

        for (Element element : elements)
        {
            try
            {
                String href = WWXML.getText(element, "@href");
                if (href != null && href.length() > 0)
                {
                    Object o = this.createFromConfigSource(href, params);
                    if (o == null)
                        continue;

                    if (o instanceof Layer)
                    {
                        LayerList ll = new LayerList();
                        ll.add((Layer) o);
                        o = ll;
                    }

                    if (o instanceof LayerList)
                    {
                        LayerList list = (LayerList) o;
                        if (list != null && list.size() > 0)
                            layerLists.add(list);
                    }
                    else if (o instanceof LayerList[])
                    {
                        LayerList[] lists = (LayerList[]) o;
                        if (lists != null && lists.length > 0)
                            layerLists.addAll(Arrays.asList(lists));
                    }
                    else
                    {
                        String msg = Logging.getMessage("LayerFactory.UnexpectedTypeForLayer", o.getClass().getName());
                        Logging.logger().log(java.util.logging.Level.WARNING, msg);
                    }

                    continue;
                }

                String title = WWXML.getText(element, "@title");
                Element[] children = WWXML.getElements(element, "./Layer", null);
                if (children != null && children.length > 0)
                {
                    LayerList list = this.createLayerList(children, params);
                    if (list != null && list.size() > 0)
                    {
                        layerLists.add(list);
                        if (title != null && title.length() > 0)
                            list.setValue(AVKey.DISPLAY_NAME, title);
                    }
                }
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.WARNING, e.getMessage(), e);
                // keep going to create other layers
            }
        }

        return layerLists.toArray(new LayerList[layerLists.size()]);
    }

    /**
     * Create a list of layers described by an array of XML layer description elements.
     * <p/>
     * Any exceptions occurring during creation of the layers are logged and not re-thrown. The layers associated with
     * the exceptions are not included in the returned layer list.
     *
     * @param layerElements the XML elements describing the layers to create.
     * @param params        any parameters to apply when creating the layers.
     *
     * @return a layer list containing the specified layers.
     */
    protected LayerList createLayerList(Element[] layerElements, AVList params)
    {
        LayerList layerList = new LayerList();

        for (Element element : layerElements)
        {
            try
            {
                layerList.add(this.createFromLayerDocument(element, params));
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.WARNING, e.getMessage(), e);
                // keep going to create other layers
            }
        }

        return layerList;
    }

    /**
     * Create a layer described by an XML layer description.
     *
     * @param domElement the XML element describing the layer to create.
     * @param params     any parameters to apply when creating the layer.
     *
     * @return a new layer
     *
     * @throws WWUnrecognizedException if the layer type or service type given in the describing element is
     *                                 unrecognized.
     * @see #createTiledImageLayer(org.w3c.dom.Element, gov.nasa.worldwind.avlist.AVList).
     */
    protected Layer createFromLayerDocument(Element domElement, AVList params)
    {
        String className = WWXML.getText(domElement, "@className");
        if (className != null && className.length() > 0)
        {
            Layer layer = (Layer) WorldWind.createComponent(className);
            String actuate = WWXML.getText(domElement, "@actuate");
            layer.setEnabled(WWUtil.isEmpty(actuate) || actuate.equals("onLoad"));
            WWXML.invokePropertySetters(layer, domElement);
            return layer;
        }

        AVList props = WWXML.copyProperties(domElement, null);
        if (props != null)
        {   // Copy params and add any properties for this layer to the copy
            if (params != null)
                props.setValues(params);
            params = props;
        }

        Layer layer;
        String href = WWXML.getText(domElement, "@href");
        if (href != null && href.length() > 0)
        {
            Object o = this.createFromConfigSource(href, params);
            if (o == null)
                return null;

            if (!(o instanceof Layer))
            {
                String msg = Logging.getMessage("LayerFactory.UnexpectedTypeForLayer", o.getClass().getName());
                throw new WWRuntimeException(msg);
            }

            layer = (Layer) o;
        }
        else
        {
            String layerType = WWXML.getText(domElement, "@layerType");
            if (layerType != null && layerType.equals("TiledImageLayer"))
            {
                layer = this.createTiledImageLayer(domElement, params);
            }
            else if (layerType != null && layerType.equals("ShapefileLayer"))
            {
                layer = this.createShapefileLayer(domElement, params);
            }
            else
            {
                String msg = Logging.getMessage("generic.UnrecognizedLayerType", layerType);
                throw new WWUnrecognizedException(msg);
            }
        }

        if (layer != null)
        {
            String actuate = WWXML.getText(domElement, "@actuate");
            layer.setEnabled(actuate != null && actuate.equals("onLoad"));
            WWXML.invokePropertySetters(layer, domElement);
        }

        return layer;
    }

    /**
     * Create a {@link TiledImageLayer} layer described by an XML layer description.
     *
     * @param domElement the XML element describing the layer to create. The element must inculde a service name
     *                   identifying the type of service to use to retrieve layer data. Recognized service types are
     *                   "Offline", "WWTileService" and "OGC:WMS".
     * @param params     any parameters to apply when creating the layer.
     *
     * @return a new layer
     *
     * @throws WWUnrecognizedException if the service type given in the describing element is unrecognized.
     */
    protected Layer createTiledImageLayer(Element domElement, AVList params)
    {
        Layer layer;

        String serviceName = WWXML.getText(domElement, "Service/@serviceName");

        if ("Offline".equals(serviceName))
        {
            layer = new BasicTiledImageLayer(domElement, params);
        }
        else if ("WWTileService".equals(serviceName))
        {
            layer = new BasicTiledImageLayer(domElement, params);
        }
        else if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
        {
            layer = new WMSTiledImageLayer(domElement, params);
        }
        else if (AVKey.SERVICE_NAME_LOCAL_RASTER_SERVER.equals(serviceName))
        {
            layer = new LocalRasterServerLayer(domElement, params);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
            throw new WWUnrecognizedException(msg);
        }
//
//        String name = layer.getStringValue(AVKey.DISPLAY_NAME);
//        System.out.println(name);

        String actuate = WWXML.getText(domElement, "@actuate");
        layer.setEnabled(actuate != null && actuate.equals("onLoad"));

        return layer;
    }

    /**
     * Creates a shapefile layer described by an XML layer description. This delegates layer construction to the factory
     * class associated with the configuration key "gov.nasa.worldwind.avkey.ShapefileLayerFactory".
     *
     * @param domElement the XML element describing the layer to create. The element must contain the shapefile
     *                   location, and may contain elements specifying shapefile attribute mappings, shape attributes to
     *                   assign to created shapes, and layer properties.
     * @param params     any parameters to apply when creating the layer.
     *
     * @return a new layer
     */
    protected Layer createShapefileLayer(Element domElement, AVList params)
    {
        return (Layer) BasicFactory.create(AVKey.SHAPEFILE_LAYER_FACTORY, domElement, params);
    }
}
