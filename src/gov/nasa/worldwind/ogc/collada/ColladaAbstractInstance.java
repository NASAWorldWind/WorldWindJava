/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
