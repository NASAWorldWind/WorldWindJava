/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA Instance element. An Instance is a pointer to an element in a COLLADA document. The Instance
 * may target another element in the current COLLADA document, or an element in a remote document.
 *
 * @author pabercrombie
 * @version $Id: ColladaAbstractInstance.java 600 2012-05-17 22:57:25Z pabercrombie $
 */
public abstract class ColladaAbstractInstance<T> extends ColladaAbstractObject
{
    /** Resolved target of the link. */
    protected T instance;

    /**
     * Create an instance.
     *
     * @param ns Namespace.
     */
    public ColladaAbstractInstance(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the URL of the target resource.
     *
     * @return URL field.
     */
    public String getUrl()
    {
        return (String) this.getField("url");
    }

    /**
     * Retrieves the target resource for this instance. Calling this method will cause the instance to resolve the
     * linked resource. If the resource is external it may not be available immediately, in which case this method
     * returns null.
     *
     * @return The linked resource, or null if the resource is not available.
     */
    @SuppressWarnings("unchecked")
    public T get()
    {
        if (this.instance == null)
        {
            Object o = this.getRoot().resolveReference(this.getUrl());
            this.instance = (T) o; // May be null
        }

        return this.instance;
    }
}
