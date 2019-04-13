/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.OGCCapabilities;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.ogc.wcs.wcs100.WCS100Capabilities;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A factory to create {@link gov.nasa.worldwind.globes.ElevationModel}s.
 *
 * @author tag
 * @version $Id: BasicElevationModelFactory.java 2347 2014-09-24 23:37:03Z dcollins $
 */
public class BasicElevationModelFactory extends BasicFactory
{
    /**
     * Creates an elevation model from a general configuration source. The source can be one of the following: <ul>
     * <li>a {@link java.net.URL}</li> <li>a {@link java.io.File}</li> <li>a {@link java.io.InputStream}</li> <li> an
     * {@link org.w3c.dom.Element}</li> <li>a {@link String} holding a file name, a name of a resource on the classpath,
     * or a string representation of a URL</li> </ul>
     * <p>
     * For non-compound models, this method maps the <code>serviceName</code> attribute of the
     * <code>ElevationModel/Service</code> element of the XML configuration document to the appropriate elevation-model
     * type. Service types recognized are:" <ul> <li>"WMS" for elevation models that draw their data from a WMS web
     * service.</li> <li>"WWTileService" for elevation models that draw their data from a WorldWind tile service.</li>
     * <li>"Offline" for elevation models that draw their data only from the local cache.</li> </ul>
     *
     * @param configSource the configuration source. See above for supported types.
     * @param params       properties to associate with the elevation model during creation.
     *
     * @return an elevation model.
     *
     * @throws IllegalArgumentException if the configuration file name is null or an empty string.
     * @throws WWUnrecognizedException  if the source type is unrecognized or the requested elevation-model type is
     *                                  unrecognized.
     * @throws WWRuntimeException       if object creation fails for other reasons. The exception identifying the source
     *                                  of the failure is included as the {@link Exception#initCause(Throwable)}.
     */
    @Override
    public Object createFromConfigSource(Object configSource, AVList params)
    {
        ElevationModel model = (ElevationModel) super.createFromConfigSource(configSource, params);
        if (model == null)
        {
            String msg = Logging.getMessage("generic.UnrecognizedDocument", configSource);
            throw new WWUnrecognizedException(msg);
        }

        return model;
    }

    @Override
    protected ElevationModel doCreateFromCapabilities(OGCCapabilities caps, AVList params)
    {
        String serviceName = caps.getServiceInformation().getServiceName();
        if (serviceName == null || !(serviceName.equalsIgnoreCase(OGCConstants.WMS_SERVICE_NAME)
            || serviceName.equalsIgnoreCase("WMS")))
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

        return new WMSBasicElevationModel((WMSCapabilities) caps, params);
    }

    protected Object doCreateFromCapabilities(WCS100Capabilities caps, AVList params)
    {
        return new WCSElevationModel(caps, params);
    }

    /**
     * Creates an elevation model from an XML description. An "href" link to an external elevation model description is
     * followed if it exists.
     *
     * @param domElement an XML element containing the elevation model description.
     * @param params     any parameters to apply when creating the elevation models.
     *
     * @return the requested elevation model, or null if the specified element does not describe an elevation model.
     *
     * @throws Exception if a problem occurs during creation.
     * @see #createNonCompoundModel(org.w3c.dom.Element, gov.nasa.worldwind.avlist.AVList)
     */
    @Override
    protected ElevationModel doCreateFromElement(Element domElement, AVList params) throws Exception
    {
        Element element = WWXML.getElement(domElement, ".", null);
        if (element == null)
            return null;

        String href = WWXML.getText(element, "@href");
        if (href != null && href.length() > 0)
            return (ElevationModel) this.createFromConfigSource(href, params);

        Element[] elements = WWXML.getElements(element, "./ElevationModel", null);

        String modelType = WWXML.getText(element, "@modelType");
        if (modelType != null && modelType.equalsIgnoreCase("compound"))
            return this.createCompoundModel(elements, params);

        String localName = WWXML.getUnqualifiedName(domElement);
        if (elements != null && elements.length > 0)
            return this.createCompoundModel(elements, params);
        else if (localName != null && localName.equals("ElevationModel"))
            return this.createNonCompoundModel(domElement, params);

        return null;
    }

    /**
     * Creates a compound elevation model and populates it with a specified list of elevation models.
     * <p>
     * Any exceptions occurring during creation of the elevation models are logged and not re-thrown. The elevation
     * models associated with the exceptions are not included in the returned compound model.
     *
     * @param elements the XML elements describing the models in the new elevation model.
     * @param params   any parameters to apply when creating the elevation models.
     *
     * @return a compound elevation model populated with the specified elevation models. The compound model will contain
     *         no elevation models if none were specified or exceptions occurred for all that were specified.
     *
     * @see #createNonCompoundModel(org.w3c.dom.Element, gov.nasa.worldwind.avlist.AVList)
     */
    protected CompoundElevationModel createCompoundModel(Element[] elements, AVList params)
    {
        CompoundElevationModel compoundModel = new CompoundElevationModel();

        if (elements == null || elements.length == 0)
            return compoundModel;

        for (Element element : elements)
        {
            try
            {
                ElevationModel em = this.doCreateFromElement(element, params);
                if (em != null)
                    compoundModel.addElevationModel(em);
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("ElevationModel.ExceptionCreatingElevationModel");
                Logging.logger().log(java.util.logging.Level.WARNING, msg, e);
            }
        }

        return compoundModel;
    }

    /**
     * Create a simple elevation model.
     *
     * @param domElement the XML element describing the elevation model to create. The element must inculde a service
     *                   name identifying the type of service to use to retrieve elevation data. Recognized service
     *                   types are "Offline", "WWTileService" and "OGC:WMS".
     * @param params     any parameters to apply when creating the elevation model.
     *
     * @return a new elevation model
     *
     * @throws WWUnrecognizedException if the service type given in the describing element is unrecognized.
     */
    protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
    {
        ElevationModel em;

        String serviceName = WWXML.getText(domElement, "Service/@serviceName");

        if (serviceName.equals("Offline"))
        {
            em = new BasicElevationModel(domElement, params);
        }
        else if (serviceName.equals("WWTileService"))
        {
            em = new BasicElevationModel(domElement, params);
        }
        else if (serviceName.equals(OGCConstants.WMS_SERVICE_NAME))
        {
            em = new WMSBasicElevationModel(domElement, params);
        }
        else if (serviceName.equals(OGCConstants.WCS_SERVICE_NAME))
        {
            em = new WCSElevationModel(domElement, params);
        }
        else if (AVKey.SERVICE_NAME_LOCAL_RASTER_SERVER.equals(serviceName))
        {
            em = new LocalRasterServerElevationModel(domElement, params);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
            throw new WWUnrecognizedException(msg);
        }

        return em;
    }
}
