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
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.*;
import java.beans.*;
import java.util.*;

/**
 * <code>WorldWindowGLCanvas</code> is a heavyweight AWT component for displaying WorldWind {@link Model}s (globe and
 * layers). It's a self-contained component intended to serve as an application's <code>WorldWindow</code>. Construction
 * options exist to specify a specific graphics device and to share graphics resources with another graphics device.
 * <p>
 * Heavyweight AWT components such as instances of this class can be used in conjunction with lightweight Swing
 * components. A discussion of doing so is in the <em>Heavyweight and Lightweight Issues</em> section of the <a
 * href="http://download.java.net/media/jogl/doc/userguide/">"JOGL User's Guide"</a>. All that's typically necessary is
 * to invoke the following methods of the indicated Swing classes: {@link javax.swing.ToolTipManager#setLightWeightPopupEnabled(boolean)},
 * {@link javax.swing.JPopupMenu#setLightWeightPopupEnabled(boolean)} and {@link javax.swing.JPopupMenu#setLightWeightPopupEnabled(boolean)}.
 * These methods should be invoked within a <code>static</code> block within an application's main class.
 * <p>
 * This class is capable of supporting stereo devices. To cause a stereo device to be selected and used, specify the
 * Java VM property "gov.nasa.worldwind.stereo.mode=device" prior to creating an instance of this class. A stereo
 * capable {@link SceneController} such as {@link gov.nasa.worldwind.StereoSceneController} must also be specified in
 * the WorldWind {@link Configuration}. The default configuration specifies a stereo-capable controller. To prevent
 * stereo from being used by subsequently opened {@code WorldWindowGLCanvas}es, set the property to a an empty string,
 * "". If a stereo device cannot be selected and used, this falls back to a non-stereo device that supports WorldWind's
 * minimum requirements.
 * <p>
 * Under certain conditions, JOGL replaces the <code>GLContext</code> associated with instances of this class. This then
 * necessitates that all resources such as textures that have been stored on the graphic devices must be regenerated for
 * the new context. WorldWind does this automatically by clearing the associated {@link GpuResourceCache}. Objects
 * subsequently rendered automatically re-create those resources. If an application creates its own graphics resources,
 * including textures, vertex buffer objects and display lists, it must store them in the <code>GpuResourceCache</code>
 * associated with the current {@link gov.nasa.worldwind.render.DrawContext} so that they are automatically cleared, and
 * be prepared to re-create them if they do not exist in the <code>DrawContext</code>'s current
 * <code>GpuResourceCache</code> when needed. Examples of doing this can be found by searching for usages of the method
 * {@link GpuResourceCache#get(Object)} and {@link GpuResourceCache#getTexture(Object)}.
 *
 * @author Tom Gaskins
 * @version $Id: WorldWindowGLCanvas.java 2924 2015-03-26 01:32:02Z tgaskins $
 */
public class WorldWindowGLCanvas extends GLCanvas implements WorldWindow, PropertyChangeListener
{
    /** The drawable to which {@link WorldWindow} methods are delegated. */
    protected final WorldWindowGLDrawable wwd; // WorldWindow interface delegates to wwd

    /** Constructs a new <code>WorldWindowGLCanvas</code> on the default graphics device. */
    public WorldWindowGLCanvas()
    {
        super(Configuration.getRequiredGLCapabilities(), new BasicGLCapabilitiesChooser(), null);

        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.addPropertyChangeListener(this);
            this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> on the default graphics device and shares graphics resources
     * with another <code>WorldWindow</code>.
     *
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     *
     * @see GLCanvas#GLCanvas(GLCapabilitiesImmutable, GLCapabilitiesChooser, GraphicsDevice)
     */
    public WorldWindowGLCanvas(WorldWindow shareWith)
    {
        super(Configuration.getRequiredGLCapabilities(), new BasicGLCapabilitiesChooser(), null);

        if (shareWith != null)
            this.setSharedAutoDrawable((WorldWindowGLCanvas) shareWith);

        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.addPropertyChangeListener(this);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> on a specified graphics device and shares graphics resources
     * with another <code>WorldWindow</code>.
     *
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     * @param device    the <code>GraphicsDevice</code> on which to create the window. May be null, in which case the
     *                  default screen device of the local {@link GraphicsEnvironment} is used.
     *
     * @see GLCanvas#GLCanvas(GLCapabilitiesImmutable, GLCapabilitiesChooser, GraphicsDevice)
     */
    public WorldWindowGLCanvas(WorldWindow shareWith, java.awt.GraphicsDevice device)
    {
        super(Configuration.getRequiredGLCapabilities(), new BasicGLCapabilitiesChooser(), device);

        if (shareWith != null)
            this.setSharedContext(shareWith.getContext());

        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.addPropertyChangeListener(this);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> on a specified device with the specified capabilities and
     * shares graphics resources with another <code>WorldWindow</code>.
     *
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     * @param device       the <code>GraphicsDevice</code> on which to create the window. May be null, in which case the
     *                     default screen device of the local {@link GraphicsEnvironment} is used.
     * @param capabilities a capabilities object indicating the OpenGL rendering context's capabilities. May be null, in
     *                     which case a default set of capabilities is used.
     * @param chooser      a chooser object that customizes the specified capabilities. May be null, in which case a
     *                     default chooser is used.
     *
     * @see GLCanvas#GLCanvas(GLCapabilitiesImmutable, GLCapabilitiesChooser, GraphicsDevice)
     */
    public WorldWindowGLCanvas(WorldWindow shareWith, java.awt.GraphicsDevice device,
        GLCapabilities capabilities, GLCapabilitiesChooser chooser)
    {
        super(capabilities, chooser, device);

        if (shareWith != null)
            this.setSharedContext(shareWith.getContext());

        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    @Override
	public void propertyChange(PropertyChangeEvent evt)
    {
        if(this.wwd == evt.getSource())
            this.firePropertyChange(evt);

        //noinspection StringEquality
        if (evt.getPropertyName() == WorldWind.SHUTDOWN_EVENT)
            this.shutdown();
    }

    @Override
	public void shutdown()
    {
        WorldWind.removePropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
        this.wwd.shutdown();
    }

    @Override
    public boolean isEnableGpuCacheReinitialization()
    {
        return this.wwd.isEnableGpuCacheReinitialization();
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization)
    {
        this.wwd.setEnableGpuCacheReinitialization(enableGpuCacheReinitialization);
    }

    /** Constructs and attaches the {@link View} for this <code>WorldWindow</code>. */
    protected void createView()
    {
        this.setView((View) WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME));
    }

    /** Constructs and attaches the {@link InputHandler} for this <code>WorldWindow</code>. */
    protected void createDefaultInputHandler()
    {
        this.setInputHandler((InputHandler) WorldWind.createConfigurationComponent(AVKey.INPUT_HANDLER_CLASS_NAME));
    }

    @Override
	public InputHandler getInputHandler()
    {
        return this.wwd.getInputHandler();
    }

    @Override
	public void setInputHandler(InputHandler inputHandler)
    {
        if (this.wwd.getInputHandler() != null)
            this.wwd.getInputHandler().setEventSource(null); // remove this window as a source of events

        this.wwd.setInputHandler(inputHandler != null ? inputHandler : new NoOpInputHandler());
        if (inputHandler != null)
            inputHandler.setEventSource(this);
    }

    @Override
	public SceneController getSceneController()
    {
        return this.wwd.getSceneController();
    }

    @Override
	public void setSceneController(SceneController sceneController)
    {
        this.wwd.setSceneController(sceneController);
    }

    @Override
	public GpuResourceCache getGpuResourceCache()
    {
        return this.wwd.getGpuResourceCache();
    }

    @Override
	public void redraw()
    {
        this.repaint();
    }

    @Override
	public void redrawNow()
    {
        this.wwd.redrawNow();
    }

    @Override
	public void setModel(Model model)
    {
        // null models are permissible
        this.wwd.setModel(model);
    }

    @Override
	public Model getModel()
    {
        return this.wwd.getModel();
    }

    @Override
	public void setView(View view)
    {
        // null views are permissible
        if (view != null)
            this.wwd.setView(view);
    }

    @Override
	public View getView()
    {
        return this.wwd.getView();
    }

    @Override
	public void setModelAndView(Model model, View view)
    {   // null models/views are permissible
        this.setModel(model);
        this.setView(view);
    }

    @Override
	public void addRenderingListener(RenderingListener listener)
    {
        this.wwd.addRenderingListener(listener);
    }

    @Override
	public void removeRenderingListener(RenderingListener listener)
    {
        this.wwd.removeRenderingListener(listener);
    }

    @Override
	public void addSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().addSelectListener(listener);
        this.wwd.addSelectListener(listener);
    }

    @Override
	public void removeSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().removeSelectListener(listener);
        this.wwd.removeSelectListener(listener);
    }

    @Override
	public void addPositionListener(PositionListener listener)
    {
        this.wwd.addPositionListener(listener);
    }

    @Override
	public void removePositionListener(PositionListener listener)
    {
        this.wwd.removePositionListener(listener);
    }

    @Override
	public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.addRenderingExceptionListener(listener);
    }

    @Override
	public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.removeRenderingExceptionListener(listener);
    }

    @Override
	public Position getCurrentPosition()
    {
        return this.wwd.getCurrentPosition();
    }

    @Override
	public PickedObjectList getObjectsAtCurrentPosition()
    {
        return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getPickedObjectList() : null;
    }

    @Override
	public PickedObjectList getObjectsInSelectionBox()
    {
        return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getObjectsInPickRectangle() : null;
    }

    @Override
	public Object setValue(String key, Object value)
    {
        return this.wwd.setValue(key, value);
    }

    @Override
	public AVList setValues(AVList avList)
    {
        return this.wwd.setValues(avList);
    }

    @Override
	public Object getValue(String key)
    {
        return this.wwd.getValue(key);
    }

    @Override
	public Collection<Object> getValues()
    {
        return this.wwd.getValues();
    }

    @Override
	public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.wwd.getEntries();
    }

    @Override
	public String getStringValue(String key)
    {
        return this.wwd.getStringValue(key);
    }

    @Override
	public boolean hasKey(String key)
    {
        return this.wwd.hasKey(key);
    }

    @Override
	public Object removeKey(String key)
    {
        return this.wwd.removeKey(key);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        super.removePropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        super.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.wwd.firePropertyChange(propertyChangeEvent);
    }

    @Override
	public AVList copy()
    {
        return this.wwd.copy();
    }

    @Override
	public AVList clearList()
    {
        return this.wwd.clearList();
    }

    @Override
	public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        this.wwd.setPerFrameStatisticsKeys(keys);
    }

    @Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.wwd.getPerFrameStatistics();
    }
    
    /**
     * @return current mouse pointer position in GL surface screen coordinates.
     */
    @Override
    public Point getMousePosition() throws HeadlessException {
    	Point mpos = super.getMousePosition();
		int [] awtPt = { mpos.x, mpos.y };
		
		if (wwd.getContext() == null) return mpos;
	
		GLDrawable drawable = wwd.getContext().getGLDrawable();
		if (drawable == null) return mpos;

		// Convert to GL surface coordinates
		int [] glSurfacePt = drawable.getNativeSurface().convertToPixelUnits(awtPt);
		int glSurfaceHeight = drawable.getSurfaceHeight();
		glSurfacePt[1] = glSurfaceHeight - glSurfacePt[1] - 1;
		return new Point(glSurfacePt[0], glSurfacePt[1]);
    }
}
