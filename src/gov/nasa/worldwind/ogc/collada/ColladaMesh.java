/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
