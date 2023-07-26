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

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import javax.swing.event.*;
import java.awt.*;
import java.util.*;

/**
 * An implementation class for the {@link WorldWindow} interface. Classes implementing <code>WorldWindow</code> can
 * subclass or aggregate this object to provide default <code>WorldWindow</code> functionality.
 *
 * @author Tom Gaskins
 * @version $Id: WorldWindowImpl.java 1855 2014-02-28 23:01:02Z tgaskins $
 */
public abstract class WorldWindowImpl extends WWObjectImpl implements WorldWindow
{
    private SceneController sceneController;
    private final EventListenerList eventListeners = new EventListenerList();
    private InputHandler inputHandler;
    protected GpuResourceCache gpuResourceCache;

    public WorldWindowImpl()
    {
        this.sceneController = (SceneController) WorldWind.createConfigurationComponent(
            AVKey.SCENE_CONTROLLER_CLASS_NAME);

        // Set up to initiate a repaint whenever a file is retrieved and added to the local file store.
        WorldWind.getDataFileStore().addPropertyChangeListener(this);
    }

    /**
     * Causes resources used by the WorldWindow to be freed. The WorldWindow cannot be used once this method is
     * called. An OpenGL context for the window must be current.
     */
    @Override
    public void shutdown()
    {
        WorldWind.getDataFileStore().removePropertyChangeListener(this);

        if (this.inputHandler != null)
        {
            this.inputHandler.dispose();
            this.inputHandler = new NoOpInputHandler();
        }

        // Clear the texture cache
        if (this.getGpuResourceCache() != null)
            this.getGpuResourceCache().clear();

        // Dispose all the layers //  TODO: Need per-window dispose for layers
        if (this.getModel() != null && this.getModel().getLayers() != null)
        {
            for (Layer layer : this.getModel().getLayers())
            {
                try
                {
                    layer.dispose();
                }
                catch (Exception e)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, Logging.getMessage(
                        "WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"), e);
                }
            }
        }

        SceneController sc = this.getSceneController();
        if (sc != null)
            sc.dispose();
    }

    @Override
    public GpuResourceCache getGpuResourceCache()
    {
        return this.gpuResourceCache;
    }

    public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
    {
        this.gpuResourceCache = gpuResourceCache;
        this.sceneController.setGpuResourceCache(this.gpuResourceCache);
    }

    @Override
    public void setModel(Model model)
    {
        // model can be null, that's ok - it indicates no model.
        if (this.sceneController != null)
            this.sceneController.setModel(model);
    }

    @Override
    public Model getModel()
    {
        return this.sceneController != null ? this.sceneController.getModel() : null;
    }

    @Override
    public void setView(View view)
    {
        // view can be null, that's ok - it indicates no view.
        if (this.sceneController != null)
            this.sceneController.setView(view);
    }

    @Override
    public View getView()
    {
        return this.sceneController != null ? this.sceneController.getView() : null;
    }

    @Override
    public void setModelAndView(Model model, View view)
    {
        this.setModel(model);
        this.setView(view);
    }

    @Override
    public SceneController getSceneController()
    {
        return this.sceneController;
    }

    @Override
    public void setSceneController(SceneController sc)
    {
        if (sc != null && this.getSceneController() != null)
        {
            sc.setGpuResourceCache(this.sceneController.getGpuResourceCache());
        }

        this.sceneController = sc;
    }

    @Override
    public InputHandler getInputHandler()
    {
        return this.inputHandler;
    }

    @Override
    public void setInputHandler(InputHandler inputHandler)
    {
        this.inputHandler = inputHandler;
    }

    @Override
    public void redraw()
    {
    }

    @Override
    public void redrawNow()
    {
    }

    @Override
    public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        if (this.sceneController != null)
            this.sceneController.setPerFrameStatisticsKeys(keys);
    }

    @Override
    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        if (this.sceneController == null || this.sceneController.getPerFrameStatistics() == null)
            return new ArrayList<PerformanceStatistic>(0);

        return this.sceneController.getPerFrameStatistics();
    }

    @Override
    public PickedObjectList getObjectsAtCurrentPosition()
    {
        return null;
    }

    @Override
    public PickedObjectList getObjectsInSelectionBox()
    {
        return null;
    }

    @Override
    public Position getCurrentPosition()
    {
        if (this.sceneController == null)
            return null;

        PickedObjectList pol = this.getSceneController().getPickedObjectList();
        if (pol == null || pol.size() < 1)
            return null;

        Position p = null;
        PickedObject top = pol.getTopPickedObject();
        if (top != null && top.hasPosition())
            p = top.getPosition();
        else if (pol.getTerrainObject() != null)
            p = pol.getTerrainObject().getPosition();

        return p;
    }

    protected PickedObject getCurrentSelection()
    {
        if (this.sceneController == null)
            return null;

        PickedObjectList pol = this.getSceneController().getPickedObjectList();
        if (pol == null || pol.size() < 1)
            return null;

        PickedObject top = pol.getTopPickedObject();
        return top.isTerrain() ? null : top;
    }

    protected PickedObjectList getCurrentBoxSelection()
    {
        if (this.sceneController == null)
            return null;

        PickedObjectList pol = this.sceneController.getObjectsInPickRectangle();
        return pol != null && pol.size() > 0 ? pol : null;
    }

    @Override
    public void addRenderingListener(RenderingListener listener)
    {
        this.eventListeners.add(RenderingListener.class, listener);
    }

    @Override
    public void removeRenderingListener(RenderingListener listener)
    {
        this.eventListeners.remove(RenderingListener.class, listener);
    }

    protected void callRenderingListeners(RenderingEvent event)
    {
        for (RenderingListener listener : this.eventListeners.getListeners(RenderingListener.class))
        {
            listener.stageChanged(event);
        }
    }

    @Override
    public void addPositionListener(PositionListener listener)
    {
        this.eventListeners.add(PositionListener.class, listener);
    }

    @Override
    public void removePositionListener(PositionListener listener)
    {
        this.eventListeners.remove(PositionListener.class, listener);
    }

    protected void callPositionListeners(final PositionEvent event)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (PositionListener listener : eventListeners.getListeners(PositionListener.class))
                {
                    listener.moved(event);
                }
            }
        });
    }

    @Override
    public void addSelectListener(SelectListener listener)
    {
        this.eventListeners.add(SelectListener.class, listener);
    }

    @Override
    public void removeSelectListener(SelectListener listener)
    {
        this.eventListeners.remove(SelectListener.class, listener);
    }

    protected void callSelectListeners(final SelectEvent event)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (SelectListener listener : eventListeners.getListeners(SelectListener.class))
                {
                    listener.selected(event);
                }
            }
        });
    }

    @Override
    public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.eventListeners.add(RenderingExceptionListener.class, listener);
    }

    @Override
    public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.eventListeners.remove(RenderingExceptionListener.class, listener);
    }

    protected void callRenderingExceptionListeners(final Throwable exception)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (RenderingExceptionListener listener : eventListeners.getListeners(
                    RenderingExceptionListener.class))
                {
                    listener.exceptionThrown(exception);
                }
            }
        });
    }

    private static final long FALLBACK_TEXTURE_CACHE_SIZE = 60000000;

    public static GpuResourceCache createGpuResourceCache()
    {
        long cacheSize = Configuration.getLongValue(AVKey.TEXTURE_CACHE_SIZE, FALLBACK_TEXTURE_CACHE_SIZE);
        return new BasicGpuResourceCache((long) (0.8 * cacheSize), cacheSize);
    }
}
