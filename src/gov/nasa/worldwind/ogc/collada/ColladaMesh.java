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

import java.util.*;

/**
 * Represents the COLLADA <i>mesh</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaMesh.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaMesh extends ColladaAbstractObject
{
    protected List<ColladaSource> sources = new ArrayList<ColladaSource>();
    protected List<ColladaVertices> vertices = new ArrayList<ColladaVertices>();

    // Most meshes contain either triangles or lines. Lazily allocate these lists.
    protected List<ColladaTriangles> triangles;
    protected List<ColladaLines> lines;

    public ColladaMesh(String ns)
    {
        super(ns);
    }

    public List<ColladaSource> getSources()
    {
        return this.sources;
    }

    public List<ColladaTriangles> getTriangles()
    {
        return this.triangles != null ? this.triangles : Collections.<ColladaTriangles>emptyList();
    }

    public List<ColladaLines> getLines()
    {
        return this.lines != null ? this.lines : Collections.<ColladaLines>emptyList();
    }

    public List<ColladaVertices> getVertices()
    {
        return this.vertices;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("vertices"))
        {
            this.vertices.add((ColladaVertices) value);
        }
        else if (keyName.equals("source"))
        {
            this.sources.add((ColladaSource) value);
        }
        else if (keyName.equals("triangles"))
        {
            if (this.triangles == null)
                this.triangles = new ArrayList<ColladaTriangles>();

            this.triangles.add((ColladaTriangles) value);
        }
        else if (keyName.equals("lines"))
        {
            if (this.lines == null)
                this.lines = new ArrayList<ColladaLines>();

            this.lines.add((ColladaLines) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
