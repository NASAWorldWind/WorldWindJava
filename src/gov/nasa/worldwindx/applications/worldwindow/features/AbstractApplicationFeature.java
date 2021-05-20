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

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.*;
import gov.nasa.worldwindx.applications.worldwindow.util.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AbstractApplicationFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractApplicationFeature extends AbstractFeature implements NetworkActivitySignal.NetworkUser
{
    protected boolean on;
    protected boolean autoSelectLayers;
    protected LayerList appLayers;
    protected Thread createLayersThread;

    protected abstract String getLayerGroupName();

    protected abstract void doCreateLayers();

    protected AbstractApplicationFeature(String name, String featureID, String largeIconPath, Registry registry)
    {
        super(name, featureID, largeIconPath, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);
    }

    public boolean hasNetworkActivity()
    {
        return this.createLayersThread != null && this.createLayersThread.isAlive();
    }

    @Override
    public boolean isOn()
    {
        return this.on;
    }

    protected void setOn(boolean tf)
    {
        this.on = tf;
    }

    public boolean isAutoSelectLayers()
    {
        return autoSelectLayers;
    }

    public void setAutoSelectLayers(boolean autoSelectLayers)
    {
        this.autoSelectLayers = autoSelectLayers;
    }

    public LayerList getAppLayers()
    {
        return this.appLayers != null ? this.appLayers : new LayerList();
    }

    protected void destroyLayers()
    {
        this.killPopulateLayerThread();

        if (this.appLayers == null)
            return;

        for (Layer layer : this.appLayers)
        {
            this.destroyLayer(layer);
        }
        this.appLayers.clear();
        this.appLayers = null;
    }

    protected void destroyLayer(Layer layer)
    {
        this.controller.getLayerManager().removeLayer(layer);
        this.appLayers.remove(layer);
        layer.dispose();
    }

    protected void killPopulateLayerThread()
    {
        if (this.createLayersThread != null && this.createLayersThread.isAlive())
        {
            this.createLayersThread.interrupt();
            this.controller.getNetworkActivitySignal().removeNetworkUser(this);
            this.createLayersThread = null;
        }
    }

    protected void handleInterrupt()
    {
        if (Thread.currentThread().isInterrupted() && this.appLayers != null)
        {
            Util.getLogger().info("Data retrieval cancelled");

            // Clean up so the user can try again later
            this.destroyLayers();
        }
    }

    protected void removeLayers()
    {
        this.controller.getLayerManager().removeLayers(this.appLayers);
    }

    protected void createLayers()
    {
        if (this.appLayers == null)
        {
            this.appLayers = new LayerList();
            this.appLayers.setDisplayName(this.getLayerGroupName());
        }

        this.createLayersThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    doCreateLayers();
                }
                finally
                {
                    handleInterrupt();
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            controller.getNetworkActivitySignal().removeNetworkUser(AbstractApplicationFeature.this);
                            createLayersThread = null;
                        }
                    });
                }
            }
        });
        this.createLayersThread.setPriority(Thread.MIN_PRIORITY);
        this.createLayersThread.start();

        this.controller.getNetworkActivitySignal().addNetworkUser(AbstractApplicationFeature.this);
    }

    protected void addLayer(final Layer layer, final LayerPath path)
    {
        try
        {
            // In order to synchronize layer additions, they are added on the EDT.
            if (SwingUtilities.isEventDispatchThread())
                this.doAddLayer(layer, path);
            else
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        doAddLayer(layer, path);
                    }
                });
        }
        catch (InterruptedException e)
        {
            // Don't do anything here because higher level code will detect and report the interrupt.
        }
        catch (InvocationTargetException e)
        {
            Util.getLogger().log(Level.WARNING, "Invocation target exception", e);
        }
    }

    protected void doAddLayer(final Layer layer, final LayerPath path)
    {
        LayerManager layerManager = controller.getLayerManager();
        Layer oldLayer = layerManager.getLayerFromPath(path);
        if (oldLayer != null)
        {
            this.controller.getLayerManager().removeLayer(path);
            this.appLayers.remove(oldLayer);
        }

        // Cause the cache files to be deleted when the JVM exits.
//        layer.setValue(AVKey.DELETE_CACHE_ON_EXIT, true);

        this.appLayers.add(layer);
        layerManager.addLayer(layer, path.lastButOne());
        layerManager.selectLayer(layer, this.isAutoSelectLayers());
        layerManager.expandPath(path.lastButOne());
    }

    protected void addLayers(LayerList layerList)
    {
        for (Layer layer : layerList)
        {
            this.addLayer(layer, new LayerPath(layerList.getDisplayName(), layer.getName()));
        }
    }

    protected LayerTree addLayerTree(LayerTree layerTree)
    {
        LayerPath basePath = new LayerPath(this.getLayerGroupName());

        Iterator<LayerPath> iter = layerTree.getPathIterator(basePath);
        while (iter.hasNext())
        {
            LayerPath path = iter.next();
            Layer layer = layerTree.getLayer(path);
            this.addLayer(layer, path);
        }

        return layerTree;
    }
}
