/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import com.jogamp.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.ScreenCreditController;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.dashboard.DashboardController;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.AWTGLAutoDrawable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * A non-platform specific {@link WorldWindow} class. This class can be aggregated into platform-specific classes to
 * provide the core functionality of WorldWind.
 *
 * @author Tom Gaskins
 * @version $Id: WorldWindowGLAutoDrawable.java 2047 2014-06-06 22:48:33Z tgaskins $
 */
public class WorldWindowGLAutoDrawable extends WorldWindowImpl implements WorldWindowGLDrawable, GLEventListener
{
    /**
     * Default time in milliseconds that the view must remain unchanged before the {@link View#VIEW_STOPPED} message is
     * sent.
     */
    public static final long DEFAULT_VIEW_STOP_TIME = 1000;

    private GLAutoDrawable drawable;
    private DashboardController dashboard;
    private boolean shuttingDown = false;
    private Timer redrawTimer;
    private boolean firstInit = true;
    /** Time in milliseconds that the view must remain unchanged before the {@link View#VIEW_STOPPED} message is sent. */
    protected long viewStopTime = DEFAULT_VIEW_STOP_TIME;
    /**
     * The most recent View modelView ID.
     *
     * @see gov.nasa.worldwind.View#getViewStateID()
     */
    protected long lastViewID;
    /** Schedule task to send the {@link View#VIEW_STOPPED} message after the view stop time elapses. */
    protected ScheduledFuture viewRefreshTask;
    protected boolean enableGpuCacheReinitialization = true;

    /** Construct a new <code>WorldWindowGLCanvas</code> for a specified {@link GLDrawable}. */
    public WorldWindowGLAutoDrawable()
    {
        SceneController sc = this.getSceneController();
        if (sc != null)
            sc.addPropertyChangeListener(this);
    }

    /**
     * Indicates the amount of time, in milliseconds, that the View must remain unchanged before a {@link
     * View#VIEW_STOPPED} event is triggered.
     *
     * @return Time in milliseconds that the View must must remain unchanged before the view stopped event is
     *         triggered.
     */
    public long getViewStopTime()
    {
        return this.viewStopTime;
    }

    /**
     * Specifies the amount of time, in milliseconds, that the View must remain unchanged before a {@link
     * View#VIEW_STOPPED} event is triggered.
     *
     * @param time Time in milliseconds that the View must must remain unchanged before the view stopped event is
     *             triggered.
     */
    public void setViewStopTime(long time)
    {
        this.viewStopTime = time;
    }

    public void initDrawable(GLAutoDrawable glAutoDrawable)
    {
        if (glAutoDrawable == null)
        {
            String msg = Logging.getMessage("nullValue.DrawableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.drawable = glAutoDrawable;
        this.drawable.setAutoSwapBufferMode(false);
        this.drawable.addGLEventListener(this);
    }

    @Override
    public boolean isEnableGpuCacheReinitialization()
    {
        return enableGpuCacheReinitialization;
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization)
    {
        this.enableGpuCacheReinitialization = enableGpuCacheReinitialization;
    }

    public void initGpuResourceCache(GpuResourceCache cache)
    {
        if (cache == null)
        {
            String msg = Logging.getMessage("nullValue.GpuResourceCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setGpuResourceCache(cache);
    }

    public void endInitialization()
    {
        initializeCreditsController();
        this.dashboard = new DashboardController(this, (Component) this.drawable);
    }

    protected void initializeCreditsController()
    {
        new ScreenCreditController((WorldWindow) this.drawable);
    }

    @Override
    public void shutdown()
    {
        this.shuttingDown = true;
        this.redrawNow(); // Invokes a repaint, where the rest of the shutdown work is done.
    }

    protected void doShutdown()
    {
        super.shutdown();
        this.drawable.removeGLEventListener(this);
        if (this.dashboard != null)
            this.dashboard.dispose();
        if (this.viewRefreshTask != null)
            this.viewRefreshTask.cancel(false);
        this.shuttingDown = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.redraw(); // Queue a JOGL display request.
    }

    public GLContext getContext()
    {
        return this.drawable.getContext();
    }

    protected boolean isGLContextCompatible(GLContext context)
    {
        return context != null && context.isGL2();
    }

    protected String[] getRequiredOglFunctions()
    {
        return new String[] {"glActiveTexture", "glClientActiveTexture"};
    }

    protected String[] getRequiredOglExtensions()
    {
        return new String[] {};
    }

    /**
     * See {@link GLEventListener#init(GLAutoDrawable)}.
     *
     * @param glAutoDrawable the drawable
     */
    public void init(GLAutoDrawable glAutoDrawable)
    {
        if (!this.isGLContextCompatible(glAutoDrawable.getContext()))
        {
            String msg = Logging.getMessage("WorldWindowGLAutoDrawable.IncompatibleGLContext",
                glAutoDrawable.getContext());
            this.callRenderingExceptionListeners(new WWAbsentRequirementException(msg));
        }

        for (String funcName : this.getRequiredOglFunctions())
        {
            if (!glAutoDrawable.getGL().isFunctionAvailable(funcName))
            {
                //noinspection ThrowableInstanceNeverThrown
                this.callRenderingExceptionListeners(new WWAbsentRequirementException(funcName + " not available"));
            }
        }

        for (String extName : this.getRequiredOglExtensions())
        {
            if (!glAutoDrawable.getGL().isExtensionAvailable(extName))
            {
                //noinspection ThrowableInstanceNeverThrown
                this.callRenderingExceptionListeners(new WWAbsentRequirementException(extName + " not available"));
            }
        }

        if (this.firstInit)
            this.firstInit = false;
        else if (this.enableGpuCacheReinitialization)
            this.reinitialize(glAutoDrawable);

        // Disables use of the OpenGL extension GL_ARB_texture_rectangle by JOGL's Texture creation utility.
        //
        // Between version 1.1.1 and version 2.x, JOGL modified its texture creation utility to favor
        // GL_ARB_texture_rectangle over GL_ARB_texture_non_power_of_two on Mac OS X machines with ATI graphics cards. See
        // the following URL for details on the texture rectangle extension: http://www.opengl.org/registry/specs/ARB/texture_rectangle.txt
        //
        // There are two problems with favoring texture rectangle for non power of two textures:
        // 1) As of November 2012, we cannot find any evidence that the GL_ARB_texture_non_power_of_two extension is
        //    problematic on Mac OS X machines with ATI graphics cards. The texture rectangle extension is more limiting
        //    than the NPOT extension, and therefore not preferred.
        // 2) WorldWind assumes that a texture's target is always GL_TEXTURE_2D, and therefore incorrectly displays
        //    textures with the target GL_TEXTURE_RECTANGLE.
        TextureIO.setTexRectEnabled(false);

//        this.drawable.setGL(new DebugGL(this.drawable.getGL())); // uncomment to use the debug drawable
    }

    @SuppressWarnings({"UnusedParameters"})
    protected void reinitialize(GLAutoDrawable glAutoDrawable)
    {
        // Clear the gpu resource cache if the window is reinitializing, most likely with a new gl hardware context.
        if (this.getGpuResourceCache() != null)
            this.getGpuResourceCache().clear();

        this.getSceneController().reinitialize();
    }

    /**
     * See {@link GLEventListener#init(GLAutoDrawable)}.
     * <p>
     * GLEventListener's dispose method indicates that the GL context has been released, and provides the listener an
     * opportunity to clean up any resources. Dispose does not imply that the component's lifecycle has ended or that
     * the application is closing. There are three cases in which dispose may be called:
     * <ul> <li>The WorldWindow is removed from its parent component.</li> <li>The WorldWindow's parent frame is
     * closed.</li> <li>The application calls either GLCanvas.dispose or GLJPanel.dispose.</li> </ul>
     * <p>
     * This implementation is left empty. In the case when a WorldWindow or a WorldWind application has reached its end
     * of life, its resources should be released by calling {@link gov.nasa.worldwind.WorldWindow#shutdown()} or {@link
     * gov.nasa.worldwind.WorldWind#shutDown()}, respectively. In the case when a WorldWindow is removed from its parent
     * frame or that frame is closed without a call to shutdown, it is assumed that the application intends to reuse the
     * WorldWindow. Resources associated with the previous GL context are released in the subsequent call to init.
     *
     * @param glAutoDrawable the drawable
     */
    @Override
    public void dispose(GLAutoDrawable glAutoDrawable)
    {
    }

    /**
     * See {@link GLEventListener#display(GLAutoDrawable)}.
     *
     * @param glAutoDrawable the drawable
     *
     * @throws IllegalStateException if no {@link SceneController} exists for this canvas
     */
    public void display(GLAutoDrawable glAutoDrawable)
    {
        // Performing shutdown here in order to do so with a current GL context for GL resource disposal.
        if (this.shuttingDown)
        {
            try
            {
                this.doShutdown();
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, Logging.getMessage(
                    "WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"), e);
            }
            return;
        }

        try
        {
            SceneController sc = this.getSceneController();
            if (sc == null)
            {
                String msg = Logging.getMessage("WorldWindowGLCanvas.ScnCntrllerNullOnRepaint");
                Logging.logger().severe(msg);
                throw new IllegalStateException(msg);
            }

            // Determine if the view has changed since the last frame.
            this.checkForViewChange();

            Position positionAtStart = this.getCurrentPosition();
            PickedObject selectionAtStart = this.getCurrentSelection();
            PickedObjectList boxSelectionAtStart = this.getCurrentBoxSelection();

            try
            {
                this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.BEFORE_RENDERING));
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"), e);
            }

            int redrawDelay = this.doDisplay();
            if (redrawDelay > 0)
            {
                if (this.redrawTimer == null)
                {
                    this.redrawTimer = new Timer(redrawDelay, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent actionEvent)
                        {
                            redraw();
                            redrawTimer = null;
                        }
                    });
                    redrawTimer.setRepeats(false);
                    redrawTimer.start();
                }
            }

            try
            {
                this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.BEFORE_BUFFER_SWAP));
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"), e);
            }

            this.doSwapBuffers(this.drawable);

            Double frameTime = sc.getFrameTime();
            if (frameTime != null)
                this.setValue(PerformanceStatistic.FRAME_TIME, frameTime);

            Double frameRate = sc.getFramesPerSecond();
            if (frameRate != null)
                this.setValue(PerformanceStatistic.FRAME_RATE, frameRate);

            // Dispatch the rendering exceptions accumulated by the SceneController during this frame to our
            // RenderingExceptionListeners.
            Iterable<Throwable> renderingExceptions = sc.getRenderingExceptions();
            if (renderingExceptions != null)
            {
                for (Throwable t : renderingExceptions)
                {
                    if (t != null)
                        this.callRenderingExceptionListeners(t);
                }
            }

            this.callRenderingListeners(new RenderingEvent(this.drawable, RenderingEvent.AFTER_BUFFER_SWAP));

            // Position and selection notification occurs only on triggering conditions, not same-state conditions:
            // start == null, end == null: nothing selected -- don't notify
            // start == null, end != null: something now selected -- notify
            // start != null, end == null: something was selected but no longer is -- notify
            // start != null, end != null, start != end: something new was selected -- notify
            // start != null, end != null, start == end: same thing is selected -- don't notify

            Position positionAtEnd = this.getCurrentPosition();
            if (positionAtStart != null || positionAtEnd != null)
            {
                // call the listener if both are not null or positions are the same
                if (positionAtStart != null && positionAtEnd != null)
                {
                    if (!positionAtStart.equals(positionAtEnd))
                        this.callPositionListeners(new PositionEvent(this.drawable, sc.getPickPoint(),
                            positionAtStart, positionAtEnd));
                }
                else
                {
                    this.callPositionListeners(new PositionEvent(this.drawable, sc.getPickPoint(),
                        positionAtStart, positionAtEnd));
                }
            }

            PickedObject selectionAtEnd = this.getCurrentSelection();
            if (selectionAtStart != null || selectionAtEnd != null)
            {
                this.callSelectListeners(new SelectEvent(this.drawable, SelectEvent.ROLLOVER,
                    sc.getPickPoint(), sc.getPickedObjectList()));
            }

            PickedObjectList boxSelectionAtEnd = this.getCurrentBoxSelection();
            if (boxSelectionAtStart != null || boxSelectionAtEnd != null)
            {
                this.callSelectListeners(new SelectEvent(this.drawable, SelectEvent.BOX_ROLLOVER,
                    sc.getPickRectangle(), sc.getObjectsInPickRectangle()));
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage(
                "WorldWindowGLCanvas.ExceptionAttemptingRepaintWorldWindow"), e);
        }
    }

    /**
     * Determine if the view has changed since the previous frame. If the view has changed, schedule a task that will
     * send a {@link View#VIEW_STOPPED} to the Model if the view does not change for {@link #viewStopTime}
     * milliseconds.
     *
     * @see #getViewStopTime()
     */
    protected void checkForViewChange()
    {
        long viewId = this.getView().getViewStateID();

        // Determine if the view has changed since the previous frame.
        if (viewId != this.lastViewID)
        {
            // View has changed, capture the new viewStateID
            this.lastViewID = viewId;

            // Cancel the previous view stop task and schedule a new one because the view has changed.
            this.scheduleViewStopTask(this.getViewStopTime());
        }
    }

    /**
     * Performs the actual repaint. Provided so that subclasses may override the repaint steps.
     *
     * @return if greater than zero, the window should be automatically repainted again at the indicated number of
     *         milliseconds from this method's return.
     */
    protected int doDisplay()
    {
        return this.getSceneController().repaint();
    }

    /**
     * Performs the actual buffer swap. Provided so that subclasses may override the swap steps.
     *
     * @param drawable the window's associated drawable.
     */
    protected void doSwapBuffers(GLAutoDrawable drawable)
    {
        drawable.swapBuffers();
    }

    /**
     * See {@link GLEventListener#reshape(GLAutoDrawable, int, int, int, int)}.
     *
     * @param glAutoDrawable the drawable
     */
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h)
    {
        // This is apparently necessary to enable the WWJ canvas to resize correctly with JSplitPane.
        ((Component) glAutoDrawable).setMinimumSize(new Dimension(0, 0));
    }

    @Override
    public void redraw()
    {
        if (this.drawable != null)
            ((AWTGLAutoDrawable) this.drawable).repaint();
    }

    public void redrawNow()
    {
        if (this.drawable != null)
            this.drawable.display();
    }

    /**
     * Schedule a task that will send a {@link View#VIEW_STOPPED} message to the Model when the task executes. If the
     * task runs (is not cancelled), then the view is considered stopped. Only one view stop task is scheduled at a
     * time. If this method is called again before the task executes, the task will be cancelled and a new task
     * scheduled.
     *
     * @param delay Delay in milliseconds until the task runs.
     */
    protected void scheduleViewStopTask(long delay)
    {
        Runnable viewStoppedTask = new Runnable()
        {
            public void run()
            {
                // Call onMessage on the EDT with a VIEW_STOP message
                EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        WorldWindowGLAutoDrawable.this.onMessage(
                            new Message(View.VIEW_STOPPED, WorldWindowGLAutoDrawable.this));
                    }
                });
            }
        };

        // Cancel the previous view stop task
        if (this.viewRefreshTask != null)
        {
            this.viewRefreshTask.cancel(false);
        }

        // Schedule the task for execution in delay milliseconds
        this.viewRefreshTask = WorldWind.getScheduledTaskService()
            .addScheduledTask(viewStoppedTask, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Forward the message event to the Model for distribution to the layers.
     *
     * @param msg Message event.
     */
    @Override
    public void onMessage(Message msg)
    {
        Model model = this.getModel();
        if (model != null)
        {
            model.onMessage(msg);
        }
    }
}
