/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: DataStoreProducer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DataStoreProducer extends WWObject
{
    AVList getStoreParameters();

    void setStoreParameters(AVList parameters);

    String getDataSourceDescription();

    Iterable<Object> getDataSources();

    boolean acceptsDataSource(Object source, AVList params);

    boolean containsDataSource(Object source);

    void offerDataSource(Object source, AVList params);

    void offerAllDataSources(Iterable<?> sources);

    void removeDataSource(Object source);

    void removeAllDataSources();

    void startProduction() throws Exception;

    void stopProduction();

    Iterable<?> getProductionResults();

    AVList getProductionParameters();

    void removeProductionState();
}
