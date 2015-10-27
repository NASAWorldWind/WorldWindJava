/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
     * Causes resources used by the World Window to be freed. The World Window cannot be used once this method is
     * called. An OpenGL context for the window must be current.
     */
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

    public GpuResourceCache getGpuResourceCache()
    {
        return this.gpuResourceCache;
    }

    public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
    {
        this.gpuResourceCache = gpuResourceCache;
        this.sceneController.setGpuResourceCache(this.gpuResourceCache);
    }

    public void setModel(Model model)
    {
        // model can be null, that's ok - it indicates no model.
        if (this.sceneController != null)
            this.sceneController.setModel(model);
    }

    public Model getModel()
    {
        return this.sceneController != null ? this.sceneController.getModel() : null;
    }

    public void setView(View view)
    {
        // view can be null, that's ok - it indicates no view.
        if (this.sceneController != null)
            this.sceneController.setView(view);
    }

    public View getView()
    {
        return this.sceneController != null ? this.sceneController.getView() : null;
    }

    public void setModelAndView(Model model, View view)
    {
        this.setModel(model);
        this.setView(view);
    }

    public SceneController getSceneController()
    {
        return this.sceneController;
    }

    public void setSceneController(SceneController sc)
    {
        if (sc != null && this.getSceneController() != null)
        {
            sc.setGpuResourceCache(this.sceneController.getGpuResourceCache());
        }

        this.sceneController = sc;
    }

    public InputHandler getInputHandler()
    {
        return this.inputHandler;
    }

    public void setInputHandler(InputHandler inputHandler)
    {
        this.inputHandler = inputHandler;
    }

    public void redraw()
    {
    }

    public void redrawNow()
    {
    }

    public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        if (this.sceneController != null)
            this.sceneController.setPerFrameStatisticsKeys(keys);
    }

    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        if (this.sceneController == null || this.sceneController.getPerFrameStatistics() == null)
            return new ArrayList<PerformanceStatistic>(0);

        return this.sceneController.getPerFrameStatistics();
    }

    public PickedObjectList getObjectsAtCurrentPosition()
    {
        return null;
    }

    public PickedObjectList getObjectsInSelectionBox()
    {
        return null;
    }

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

    public void addRenderingListener(RenderingListener listener)
    {
        this.eventListeners.add(RenderingListener.class, listener);
    }

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

    public void addPositionListener(PositionListener listener)
    {
        this.eventListeners.add(PositionListener.class, listener);
    }

    public void removePositionListener(PositionListener listener)
    {
        this.eventListeners.remove(PositionListener.class, listener);
    }

    protected void callPositionListeners(final PositionEvent event)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                for (PositionListener listener : eventListeners.getListeners(PositionListener.class))
                {
                    listener.moved(event);
                }
            }
        });
    }

    public void addSelectListener(SelectListener listener)
    {
        this.eventListeners.add(SelectListener.class, listener);
    }

    public void removeSelectListener(SelectListener listener)
    {
        this.eventListeners.remove(SelectListener.class, listener);
    }

    protected void callSelectListeners(final SelectEvent event)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                for (SelectListener listener : eventListeners.getListeners(SelectListener.class))
                {
                    listener.selected(event);
                }
            }
        });
    }

    public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.eventListeners.add(RenderingExceptionListener.class, listener);
    }

    public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.eventListeners.remove(RenderingExceptionListener.class, listener);
    }

    protected void callRenderingExceptionListeners(final Throwable exception)
    {
        EventQueue.invokeLater(new Runnable()
        {
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
