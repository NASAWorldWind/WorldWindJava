/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: AbstractDataStoreProducer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractDataStoreProducer extends WWObjectImpl implements DataStoreProducer
{
    public static class SourceInfo extends AVListImpl
    {
        public Object source;

        public SourceInfo(Object source, AVList params)
        {
            this.source = source;
            if (null != params)
                this.setValues(params);
        }
    }

    private AVList params;
    private java.util.List<SourceInfo> dataSourceList =
        Collections.synchronizedList(new java.util.ArrayList<SourceInfo>());
    private java.util.List<Object> productionResults = new java.util.ArrayList<Object>();
    private boolean isStopped = false;

    protected AVList productionParams = null;

    public AbstractDataStoreProducer()
    {
    }

    public AVList getProductionParameters()
    {
        return this.productionParams;
    }

    public AVList getStoreParameters()
    {
        return this.params;
    }

    public void setStoreParameters(AVList parameters)
    {
        if (parameters == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String message = this.validateProductionParameters(parameters);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.params = parameters;
    }

    public Iterable<Object> getDataSources()
    {
        ArrayList<Object> list = new ArrayList<Object>();

        for (SourceInfo info : this.dataSourceList)
        {
            list.add(info.source);
        }

        return list;
    }

    public boolean acceptsDataSource(Object source, AVList params)
    {
        if (source == null || this.isStopped())
            return false;

        String message = this.validateDataSource(source, params);
        //noinspection RedundantIfStatement
        if (message != null)
        {
            // TODO garakl Do we want to log these files which we do not have readers for?
//            Logging.logger().finest(message);
            return false;
        }

        return true;
    }

    public boolean containsDataSource(Object source)
    {
        for (SourceInfo info : this.dataSourceList)
        {
            if (info.source != null ? info.source.equals(source) : (source == null))
                return true;
        }

        return false;
    }

    public void offerDataSource(Object source, AVList params)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        params = (null == params) ? new AVListImpl() : params.copy();
        String message = this.validateDataSource(source, params);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.dataSourceList.add(new SourceInfo(source, params));
    }

    public void offerAllDataSources(Iterable<?> sources)
    {
        if (sources == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Object source : sources)
        {
            this.offerDataSource(source, null);
        }
    }

    public void removeDataSource(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            return; // Warn but don't throw an exception.
        }

        Iterator<SourceInfo> iter = this.dataSourceList.iterator();
        if (!iter.hasNext())
            return;

        for (SourceInfo info = iter.next(); iter.hasNext(); info = iter.next())
        {
            if (info.source != null && info.source.equals(source))
                iter.remove();
        }
    }

    public void removeAllDataSources()
    {
        this.dataSourceList.clear();
    }

    public void startProduction() throws Exception
    {
        if (this.isStopped())
        {
            String message = Logging.getMessage("DataStoreProducer.Stopped");
            Logging.logger().warning(message);
            return;
        }

        String message = this.validateProductionParameters(this.params);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.doStartProduction(this.params);
    }

    public synchronized void stopProduction()
    {
        this.isStopped = true;
    }

    protected synchronized boolean isStopped()
    {
        return this.isStopped;
    }

    public Iterable<?> getProductionResults()
    {
        return java.util.Collections.unmodifiableList(this.productionResults);
    }

    public void removeProductionState()
    {
        // Left as an optional operation for subclasses to define.
    }

    protected java.util.List<SourceInfo> getDataSourceList()
    {
        return this.dataSourceList;
    }

    protected java.util.List<Object> getProductionResultsList()
    {
        return this.productionResults;
    }

    protected abstract void doStartProduction(AVList parameters) throws Exception;

    protected abstract String validateProductionParameters(AVList parameters);

    protected abstract String validateDataSource(Object source, AVList params);
}
