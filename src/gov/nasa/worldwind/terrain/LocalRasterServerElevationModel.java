/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import java.io.File;
import java.net.URL;

/**
 * Implements an {@link gov.nasa.worldwind.globes.ElevationModel} for a local dataset accessed via a local raster server
 * ({@link RasterServer}).
 *
 * @author tag
 * @version $Id: LocalRasterServerElevationModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LocalRasterServerElevationModel extends BasicElevationModel
{
    /**
     * Constructs an elevation model from a list of parameters describing the elevation model.
     * <p/>
     * Parameter values for DATASET_NAME and DATA_CACHE_NAME are required.
     * <p/>
     * TODO: Enumerate the other required and optional parameters.
     *
     * @param params the parameters describing the dataset.
     *
     * @throws IllegalArgumentException if the parameter list is null.
     * @throws IllegalStateException    if the required parameters are missing from the parameter list.
     */
    public LocalRasterServerElevationModel(AVList params)
    {
        super(params);

        this.createRasterServer(params);
    }

    /**
     * Constructs an elevation model from an XML document description.
     * <p/>
     * Either the specified XML document or parameter list must contain values for DATASET_NAME and DATA_CACHE_NAME.
     * <p/>
     * TODO: Enumerate the other required and optional parameters.
     *
     * @param dom    the XML document describing the dataset.
     * @param params a list of parameters that each override a parameter of the same name in the XML document, or that
     *               augment the definition there.
     *
     * @throws IllegalArgumentException if the XML document reference is null.
     * @throws IllegalStateException    if the required parameters are missing from the XML document or the parameter
     *                                  list.
     */
    public LocalRasterServerElevationModel(Document dom, AVList params)
    {
        super(dom, params);

        this.createRasterServer(params != null ? params : (AVList) this.getValue(AVKey.CONSTRUCTION_PARAMETERS));
    }

    /**
     * Constructs an elevation model from an XML document {@link Element}.
     * <p/>
     * Either the specified XML element or parameter list must contain values for DATASET_NAME and DATA_CACHE_NAME.
     * <p/>
     * TODO: Enumerate the other required and optional parameters.
     *
     * @param domElement the XML document describing the dataset.
     * @param params     a list of parameters that each override a parameter of the same name in the XML document, or
     *                   that augment the definition there.
     *
     * @throws IllegalArgumentException if the XML document reference is null.
     * @throws IllegalStateException    if the required parameters are missing from the XML element or the parameter
     *                                  list.
     */
    public LocalRasterServerElevationModel(Element domElement, AVList params)
    {
        super(domElement, params);

        this.createRasterServer(params != null ? params : (AVList) this.getValue(AVKey.CONSTRUCTION_PARAMETERS));
    }

    /**
     * Constructs an elevation model from restorable state obtained by a call to {@link #getRestorableState()} on
     * another instance of this class.
     *
     * @param restorableStateInXml a string containing the restorable state.
     *
     * @throws IllegalArgumentException if the restorable state is null or cannot be interpreted.
     * @throws IllegalStateException    if the restorable state does not contain values for DATASET_NAME and
     *                                  DATA_CACHE_NAME.
     */
    public LocalRasterServerElevationModel(String restorableStateInXml)
    {
        super(restorableStateInXml);

        this.createRasterServer((AVList) this.getValue(AVKey.CONSTRUCTION_PARAMETERS));
    }

    protected void createRasterServer(AVList params)
    {
        if (params == null)
        {
            String reason = Logging.getMessage("nullValue.ParamsIsNull");
            String msg = Logging.getMessage("generic.CannotCreateRasterServer", reason);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        if (this.getDataFileStore() == null)
        {
            String reason = Logging.getMessage("nullValue.FileStoreIsNull");
            String msg = Logging.getMessage("generic.CannotCreateRasterServer", reason);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        String datasetName = params.getStringValue(AVKey.DATASET_NAME);
        if (WWUtil.isEmpty(datasetName))
        {
            String reason = Logging.getMessage("generic.MissingRequiredParameter", AVKey.DATASET_NAME);
            String msg = Logging.getMessage("generic.CannotCreateRasterServer", reason);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        String dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
        if (WWUtil.isEmpty(dataCacheName))
        {
            String reason = Logging.getMessage("generic.MissingRequiredParameter", AVKey.DATA_CACHE_NAME);
            String msg = Logging.getMessage("generic.CannotCreateRasterServer", reason);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        String rasterServerConfigFilename = dataCacheName + File.separator + datasetName + ".RasterServer.xml";

        final URL rasterServerFileURL = this.getDataFileStore().findFile(rasterServerConfigFilename, false);
        if (WWUtil.isEmpty(rasterServerFileURL))
        {
            String reason = Logging.getMessage("Configuration.ConfigNotFound", rasterServerConfigFilename);
            String msg = Logging.getMessage("generic.CannotCreateRasterServer", reason);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        final AVList rasterServerParams = params.copy();

        rasterServerParams.setValue(AVKey.FILE_STORE, this.getDataFileStore());

        RetrieverFactory retrieverFactory = new RetrieverFactory()
        {
            final protected RasterServer rasterServer = new BasicRasterServer(rasterServerFileURL, rasterServerParams);

            public Retriever createRetriever(AVList tileParams, RetrievalPostProcessor postProcessor)
            {
                LocalRasterServerRetriever retriever =
                    new LocalRasterServerRetriever(tileParams, rasterServer, postProcessor);

                // copy only values that do not exist in destination AVList
                // from rasterServerParams (source) to retriever (destination)
                String[] keysToCopy = new String[] {
                    AVKey.DATASET_NAME, AVKey.DISPLAY_NAME,
                    AVKey.FILE_STORE, AVKey.BYTE_ORDER,
                    AVKey.IMAGE_FORMAT, AVKey.DATA_TYPE, AVKey.FORMAT_SUFFIX,
                    AVKey.MISSING_DATA_SIGNAL, AVKey.MISSING_DATA_REPLACEMENT,
                    AVKey.ELEVATION_MIN, AVKey.ELEVATION_MAX,
                };

                WWUtil.copyValues(rasterServerParams, retriever, keysToCopy, false);

                return retriever;
            }
        };

        params.setValue(AVKey.RETRIEVER_FACTORY_LOCAL, retrieverFactory);
        this.setValue(AVKey.RETRIEVER_FACTORY_LOCAL, retrieverFactory);
    }
}
