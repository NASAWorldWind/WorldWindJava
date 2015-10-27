/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.core.Controller;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerPath;

import java.util.*;

/**
 * @author tag
 * @version $Id: LayerTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class LayerTree implements Iterable<Layer>
{
    protected String name;
    protected LayerList layers = new LayerList();
    protected List<LayerTree> children = new ArrayList<LayerTree>();
    protected Controller controller;

    abstract public void createLayers(Object infoItem, AVList params);

    public LayerTree(Controller controller)
    {
        this.controller = controller;
    }

    public String getDisplayName()
    {
        return name;
    }

    public void setDisplayName(String name)
    {
        this.name = name;
    }

    public LayerTree(LayerList root)
    {
        this.layers = root;
    }

    public LayerList getLayers()
    {
        return layers;
    }

    public void setLayers(LayerList layerList)
    {
        this.layers = layerList;
    }

    public List<LayerTree> getChildren()
    {
        return children;
    }

    public void setChildren(List<LayerTree> children)
    {
        this.children = children;
    }

    public boolean hasLayers()
    {
        return this.getLayers() != null && this.getLayers().size() > 0;
    }

    public boolean hasChildren()
    {
        return this.getChildren().size() > 0;
    }

    public Object getChild(String name)
    {
        for (Layer layer : this.layers)
        {
            if (layer.getName().equals(name))
                return layer;
        }

        if (!this.hasChildren())
            return null;

        for (LayerTree subTree : this.getChildren())
        {
            if (subTree.getDisplayName().equals(name))
                return subTree;
        }

        return null;
    }

    public Layer getLayer(List<String> path)
    {
        LayerTree tree = this;

        for (int i = 1; i < path.size(); i++)
        {
            Object o = tree.getChild(path.get(i));
            if (o instanceof Layer)
                return (Layer) o;

            if (o instanceof LayerTree)
            {
                tree = (LayerTree) o;
                continue;
            }

            break; // no children have the expected name
        }

        return null;
    }

    public Iterator<Layer> iterator()
    {
        return new LayerIterator();
    }

    private class LayerIterator implements Iterator<Layer>
    {
        protected ArrayList<Layer> layers = new ArrayList<Layer>();
        protected Iterator<Layer> layerListIterator;

        public LayerIterator()
        {
            this.buildLayerList(LayerTree.this);
            this.layerListIterator = this.layers.iterator();
        }

        public boolean hasNext()
        {
            return this.layerListIterator.hasNext();
        }

        public Layer next()
        {
            return this.layerListIterator.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove() not supported for LayerIterator");
        }

        protected void buildLayerList(LayerTree tree)
        {
            for (Layer layer : tree.getLayers())
            {
                this.layers.add(layer);
            }

            for (LayerTree subTree : tree.getChildren())
            {
                this.buildLayerList(subTree);
            }
        }
    }

    public Iterator<LayerPath> getPathIterator(LayerPath basePath)
    {
        return new LayerPathIterator(basePath);
    }

    private class LayerPathIterator implements Iterator<LayerPath>
    {
        protected ArrayList<LayerPath> paths = new ArrayList<LayerPath>();
        protected Iterator<LayerPath> pathIterator;
        protected LayerPath basePath;

        public LayerPathIterator(LayerPath basePath)
        {
            this.basePath = basePath != null ? basePath : new LayerPath();
            this.buildPathList(LayerTree.this);
            this.pathIterator = this.paths.iterator();
        }

        public boolean hasNext()
        {
            return this.pathIterator.hasNext();
        }

        public LayerPath next()
        {
            return this.pathIterator.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove() not supported for LayerIterator");
        }

        protected void buildPathList(LayerTree tree)
        {
            this.buildPath(tree, this.basePath);
        }

        protected void buildPath(LayerTree tree, LayerPath ancestorPath)
        {
            for (Layer layer : tree.getLayers())
            {
                LayerPath path = new LayerPath(ancestorPath);
                path.add(layer.getName());
                this.paths.add(path);
            }

            for (LayerTree subTree : tree.getChildren())
            {
                LayerPath path = new LayerPath(ancestorPath);
                path.add(subTree.getDisplayName());
                this.buildPath(subTree, path);
            }
        }
    }
}
