/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.jogamp.opengl.awt;

import java.beans.Beans;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.OffscreenLayerOption;
import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.nativewindow.VisualIDHolder;
import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.AbstractGraphicsScreen;
import com.jogamp.nativewindow.GraphicsConfigurationFactory;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.GLSharedContextSetter;
import com.jogamp.opengl.Threading;

import com.jogamp.common.GlueGenVersion;
import com.jogamp.common.util.VersionUtil;
import com.jogamp.common.util.awt.AWTEDTExecutor;
import com.jogamp.common.util.locks.LockFactory;
import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.awt.AWTGraphicsConfiguration;
import com.jogamp.nativewindow.awt.AWTGraphicsDevice;
import com.jogamp.nativewindow.awt.AWTGraphicsScreen;
import com.jogamp.nativewindow.awt.AWTPrintLifecycle;
import com.jogamp.nativewindow.awt.AWTWindowClosingProtocol;
import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.util.GLDrawableUtil;
import com.jogamp.opengl.util.TileRenderer;

import jogamp.nativewindow.SurfaceScaleUtils;
import jogamp.opengl.Debug;
import jogamp.opengl.GLContextImpl;
import jogamp.opengl.GLDrawableHelper;
import jogamp.opengl.GLDrawableImpl;
import jogamp.opengl.awt.AWTTilePainter;

// FIXME: Subclasses need to call resetGLFunctionAvailability() on their
// context whenever the displayChanged() function is called on our
// GLEventListeners

/** A heavyweight AWT component which provides OpenGL rendering
    support. This is the primary implementation of an AWT {@link GLDrawable};
    {@link GLJPanel} is provided for compatibility with Swing user
    interfaces when adding a heavyweight doesn't work either because
    of Z-ordering or LayoutManager problems.
 *
 * <h5><a name="offscreenlayer">Offscreen Layer Remarks</a></h5>
 *
 * {@link OffscreenLayerOption#setShallUseOffscreenLayer(boolean) setShallUseOffscreenLayer(true)}
 * maybe called to use an offscreen drawable (FBO or PBuffer) allowing
 * the underlying JAWT mechanism to composite the image, if supported.
 * <p>
 * {@link OffscreenLayerOption#setShallUseOffscreenLayer(boolean) setShallUseOffscreenLayer(true)}
 * is being called if {@link GLCapabilitiesImmutable#isOnscreen()} is <code>false</code>.
 * </p>
 *
 * <h5><a name="java2dgl">Java2D OpenGL Remarks</a></h5>
 *
 * To avoid any conflicts with a potential Java2D OpenGL context,<br>
 * you shall consider setting the following JVM properties:<br>
 * <ul>
 *    <li><pre>sun.java2d.opengl=false</pre></li>
 *    <li><pre>sun.java2d.noddraw=true</pre></li>
 * </ul>
 * This is especially true in case you want to utilize a GLProfile other than
 * {@link GLProfile#GL2}, eg. using {@link GLProfile#getMaxFixedFunc()}.<br>
 * On the other hand, if you like to experiment with GLJPanel's utilization
 * of Java2D's OpenGL pipeline, you have to set them to
 * <ul>
 *    <li><pre>sun.java2d.opengl=true</pre></li>
 *    <li><pre>sun.java2d.noddraw=true</pre></li>
 * </ul>
 *
 * <h5><a name="backgrounderase">Disable Background Erase</a></h5>
 *
 * GLCanvas tries to disable background erase for the AWT Canvas
 * before native peer creation (X11) and after it (Windows), <br>
 * utilizing the optional {@link java.awt.Toolkit} method <code>disableBeackgroundErase(java.awt.Canvas)</code>.<br>
 * However if this does not give you the desired results, you may want to disable AWT background erase in general:
 * <ul>
 *   <li><pre>sun.awt.noerasebackground=true</pre></li>
 * </ul>
 *
 * <h5><a name="contextSharing">OpenGL Context Sharing</a></h5>
 * 
 * To share a {@link GLContext} see the following note in the documentation overview:
 * <a href="../../../../overview-summary.html#SHARING">context sharing</a>
 * as well as {@link GLSharedContextSetter}.
 * 
 */

@SuppressWarnings("serial")
public class GLCanvas extends Canvas implements AWTGLAutoDrawable, WindowClosingProtocol, OffscreenLayerOption,
                                                AWTPrintLifecycle, GLSharedContextSetter, ScalableSurface {

  private static final boolean DEBUG = Debug.debug("GLCanvas");

  private final RecursiveLock lock = LockFactory.createRecursiveLock();
  private final GLDrawableHelper helper = new GLDrawableHelper();
  private volatile GLDrawableImpl drawable; // volatile: avoid locking for read-only access
  private volatile JAWTWindow jawtWindow; // the JAWTWindow presentation of this AWT Canvas, bound to the 'drawable' lifecycle
  private volatile GLContextImpl context; // volatile: avoid locking for read-only access
  private volatile boolean sendReshape = false; // volatile: maybe written by EDT w/o locking
  private final float[] minPixelScale = new float[] { ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE };
  private final float[] maxPixelScale = new float[] { ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE };
  private final float[] hasPixelScale = new float[] { ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE };
  final float[] reqPixelScale = new float[] { ScalableSurface.AUTOMAX_PIXELSCALE, ScalableSurface.AUTOMAX_PIXELSCALE };

  // copy of the cstr args, mainly for recreation
  private final GLCapabilitiesImmutable capsReqUser;
  private final GLCapabilitiesChooser chooser;
  private int additionalCtxCreationFlags = 0;
  private boolean shallUseOffscreenLayer = false;

  private volatile GraphicsDevice awtDeviceReq; // one time user req.
  private volatile AWTGraphicsConfiguration awtConfig;
  private volatile boolean isShowing;
  private final HierarchyListener hierarchyListener = new HierarchyListener() {
      @Override
      public void hierarchyChanged(final HierarchyEvent e) {
          isShowing = GLCanvas.this.isShowing();
      }
  };

  private final AWTWindowClosingProtocol awtWindowClosingProtocol =
          new AWTWindowClosingProtocol(this, new Runnable() {
                @Override
                public void run() {
                    GLCanvas.this.destroyImpl( true );
                }
            }, null);

  /** Creates a new GLCanvas component with a default set of OpenGL
      capabilities, using the default OpenGL capabilities selection
      mechanism, on the default screen device.
      <p>
      See details about <a href="#contextSharing">OpenGL context sharing</a>.
      </p>
   * @throws GLException if no default profile is available for the default desktop device.
   */
  public GLCanvas() throws GLException {
    this(null);
  }

  /** Creates a new GLCanvas component with the requested set of
      OpenGL capabilities, using the default OpenGL capabilities
      selection mechanism, on the default screen device.
      <p>
      See details about <a href="#contextSharing">OpenGL context sharing</a>.
      </p>
   * @throws GLException if no GLCapabilities are given and no default profile is available for the default desktop device.
   * @see GLCanvas#GLCanvas(com.jogamp.opengl.GLCapabilitiesImmutable, com.jogamp.opengl.GLCapabilitiesChooser, com.jogamp.opengl.GLContext, java.awt.GraphicsDevice)
   */
  public GLCanvas(final GLCapabilitiesImmutable capsReqUser) throws GLException {
    this(capsReqUser, null, null);
  }

  /** Creates a new GLCanvas component. The passed GLCapabilities
      specifies the OpenGL capabilities for the component; if null, a
      default set of capabilities is used. The GLCapabilitiesChooser
      specifies the algorithm for selecting one of the available
      GLCapabilities for the component; a DefaultGLCapabilitesChooser
      is used if null is passed for this argument.
      The passed GraphicsDevice indicates the screen on
      which to create the GLCanvas; the GLDrawableFactory uses the
      default screen device of the local GraphicsEnvironment if null
      is passed for this argument.
      <p>
      See details about <a href="#contextSharing">OpenGL context sharing</a>.
      </p>
   * @throws GLException if no GLCapabilities are given and no default profile is available for the default desktop device.
   */
  public GLCanvas(final GLCapabilitiesImmutable capsReqUser,
                  final GLCapabilitiesChooser chooser,
                  final GraphicsDevice device)
      throws GLException
  {
    /*
     * Determination of the native window is made in 'super.addNotify()',
     * which creates the native peer using AWT's GraphicsConfiguration.
     * GraphicsConfiguration is returned by this class overwritten
     * 'getGraphicsConfiguration()', which returns our OpenGL compatible
     * 'chosen' GraphicsConfiguration.
     */
    super();

    if(null==capsReqUser) {
        this.capsReqUser = new GLCapabilities(GLProfile.getDefault(GLProfile.getDefaultDevice()));
    } else {
        // don't allow the user to change data
        this.capsReqUser = (GLCapabilitiesImmutable) capsReqUser.cloneMutable();
    }
    if( !this.capsReqUser.isOnscreen() ) {
        setShallUseOffscreenLayer(true); // trigger offscreen layer - if supported
    }

    // One time user AWT GraphicsDevice request
    awtDeviceReq = device;

    // instantiation will be issued in addNotify()
    this.chooser = chooser;

    this.addHierarchyListener(hierarchyListener);
    this.isShowing = isShowing();
  }

  @Override
  public final void setSharedContext(final GLContext sharedContext) throws IllegalStateException {
      helper.setSharedContext(this.context, sharedContext);
  }

  @Override
  public final void setSharedAutoDrawable(final GLAutoDrawable sharedAutoDrawable) throws IllegalStateException {
      helper.setSharedAutoDrawable(this, sharedAutoDrawable);
  }

  @Override
  public final Object getUpstreamWidget() {
    return this;
  }

  @Override
  public final RecursiveLock getUpstreamLock() { return lock; }

  @Override
  public final boolean isThreadGLCapable() { return Threading.isOpenGLThread(); }

  @Override
  public void setShallUseOffscreenLayer(final boolean v) {
      shallUseOffscreenLayer = v;
  }

  @Override
  public final boolean getShallUseOffscreenLayer() {
      return shallUseOffscreenLayer;
  }

  @Override
  public final boolean isOffscreenLayerSurfaceEnabled() {
      final JAWTWindow _jawtWindow = jawtWindow;
      if(null != _jawtWindow) {
          return _jawtWindow.isOffscreenLayerSurfaceEnabled();
      }
      return false;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Overridden to choose a {@link GraphicsConfiguration} from a parent container's
   * {@link GraphicsDevice}.
   * </p>
   * <p>
   * Method also intercepts {@link GraphicsConfiguration} changes regarding to
   * its capabilities and its {@link GraphicsDevice}. This may happen in case
   * the display changes its configuration or the component is moved to another screen.
   * </p>
   */
  @Override
  public GraphicsConfiguration getGraphicsConfiguration() {
      /**
       * parentGC will be null unless:
       *   - A native peer has assigned it. This means we have a native
       *     peer, and are already committed to a graphics configuration.
       *   - This canvas has been added to a component hierarchy and has
       *     an ancestor with a non-null GC, but the native peer has not
       *     yet been created. This means we can still choose the GC on
       *     all platforms since the peer hasn't been created.
       */
      final GraphicsConfiguration parentGC = super.getGraphicsConfiguration();

      if( Beans.isDesignTime() ) {
          return parentGC;
      }
      final GraphicsConfiguration oldGC =  null != awtConfig ? awtConfig.getAWTGraphicsConfiguration() : null;

      if ( null != parentGC && null != oldGC && !oldGC.equals(parentGC) ) {
          // Previous oldGC != parentGC of native peer

          if ( !oldGC.getDevice().getIDstring().equals(parentGC.getDevice().getIDstring()) ) {
              // Previous oldGC's GraphicsDevice != parentGC's GraphicsDevice of native peer

              /**
               * Here we select a GraphicsConfiguration on the alternate device.
               * In case the new configuration differs (-> !equalCaps),
               * we might need a reconfiguration,
               */
              final AWTGraphicsConfiguration newConfig = chooseGraphicsConfiguration( (GLCapabilitiesImmutable)awtConfig.getChosenCapabilities(),
                      (GLCapabilitiesImmutable)awtConfig.getRequestedCapabilities(),
                      chooser, parentGC.getDevice());
              final GraphicsConfiguration newGC = newConfig.getAWTGraphicsConfiguration();
              final boolean equalCaps = newConfig.getChosenCapabilities().equals(awtConfig.getChosenCapabilities());
              if(DEBUG) {
                  System.err.println(getThreadName()+": getGraphicsConfiguration() Info: Changed GC and GD");
                  System.err.println("Created Config (n): Old     GC "+oldGC);
                  System.err.println("Created Config (n): Old     GD "+oldGC.getDevice().getIDstring());
                  System.err.println("Created Config (n): Parent  GC "+parentGC);
                  System.err.println("Created Config (n): Parent  GD "+parentGC.getDevice().getIDstring());
                  System.err.println("Created Config (n): New     GC "+newGC);
                  System.err.println("Created Config (n): New     GD "+newGC.getDevice().getIDstring());
                  System.err.println("Created Config (n): Old     CF "+awtConfig);
                  System.err.println("Created Config (n): New     CF "+newConfig);
                  System.err.println("Created Config (n): EQUALS CAPS "+equalCaps);
                  // Thread.dumpStack();
              }
              if ( null != newGC ) {
                  if( !equalCaps && GLAutoDrawable.SCREEN_CHANGE_ACTION_ENABLED ) {
                      // complete destruction!
                      destroyImpl( true );
                      // recreation!
                      setAWTGraphicsConfiguration(newConfig);
                      createJAWTDrawableAndContext();
                      validateGLDrawable();
                  } else {
                      setAWTGraphicsConfiguration(newConfig);
                  }
                  /**
                   * Return the newGC, which covers the desired capabilities and is compatible
                   * with the available GC's of its devices.
                   */
                  if(DEBUG) {
                      System.err.println(getThreadName()+": Info: getGraphicsConfiguration - end.01: newGC "+newGC);
                  }
                  return newGC;
              } else {
                  if(DEBUG) {
                      System.err.println(getThreadName()+": Info: getGraphicsConfiguration - end.00: oldGC "+oldGC);
                  }
              }
          }
          /**
           * If a new GC was _not_ found/defined above,
           * method returns oldGC as selected in the constructor or first addNotify().
           * This may cause an exception in Component.checkGD when adding to a
           * container, and is the desired behavior.
           */
          return oldGC;
      } else if (null == parentGC) {
          /**
           * The parentGC is null, which means we have no native peer, and are not
           * part of a (realized) component hierarchy. So we return the
           * desired visual that was selected in the constructor (possibly
           * null).
           */
          return oldGC;
      } else {
          /**
           * Otherwise we have not explicitly selected a GC in the constructor, so
           * just return what Canvas would have.
           */
          return parentGC;
      }
  }

  @Override
  public GLContext createContext(final GLContext shareWith) {
    final RecursiveLock _lock = lock;
    _lock.lock();
    try {
        if(drawable != null) {
          final GLContext _ctx = drawable.createContext(shareWith);
          _ctx.setContextCreationFlags(additionalCtxCreationFlags);
          return _ctx;
        }
        return null;
    } finally {
        _lock.unlock();
    }
  }

  private final void setRealizedImpl(final boolean realized) {
      final RecursiveLock _lock = lock;
      _lock.lock();
      try {
          final GLDrawable _drawable = drawable;
          if( null == _drawable || realized == _drawable.isRealized() ||
              realized && ( 0 >= _drawable.getSurfaceWidth() || 0 >= _drawable.getSurfaceHeight() ) ) {
              return;
          }
         _drawable.setRealized(realized);
          if( realized && _drawable.isRealized() ) {
              sendReshape=true; // ensure a reshape is being send ..
          }
      } finally {
          _lock.unlock();
      }
  }
  private final Runnable realizeOnEDTAction = new Runnable() {
    @Override
    public void run() { setRealizedImpl(true); }
  };
  private final Runnable unrealizeOnEDTAction = new Runnable() {
    @Override
    public void run() { setRealizedImpl(false); }
  };

  @Override
  public final void setRealized(final boolean realized) {
      // Make sure drawable realization happens on AWT-EDT and only there. Consider the AWTTree lock!
      AWTEDTExecutor.singleton.invoke(getTreeLock(), false /* allowOnNonEDT */, true /* wait */, realized ? realizeOnEDTAction : unrealizeOnEDTAction);
  }

  @Override
  public boolean isRealized() {
      final GLDrawable _drawable = drawable;
      return ( null != _drawable ) ? _drawable.isRealized() : false;
  }

  @Override
  public WindowClosingMode getDefaultCloseOperation() {
      return awtWindowClosingProtocol.getDefaultCloseOperation();
  }

  @Override
  public WindowClosingMode setDefaultCloseOperation(final WindowClosingMode op) {
      return awtWindowClosingProtocol.setDefaultCloseOperation(op);
  }

  @Override
  public void display() {
    if( !validateGLDrawable() ) {
        if(DEBUG) {
            System.err.println(getThreadName()+": Info: GLCanvas display - skipped GL render, drawable not valid yet");
        }
        return; // not yet available ..
    }
    if( isShowing && !printActive ) {
        Threading.invoke(true, displayOnEDTAction, getTreeLock());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * This impl. only destroys all GL related resources.
   * </p>
   * <p>
   * This impl. does not remove the GLCanvas from it's parent AWT container
   * so this class's {@link #removeNotify()} AWT override won't get called.
   * To do so, remove this component from it's parent AWT container.
   * </p>
   */
  @Override
  public void destroy() {
    destroyImpl( false );
  }

  protected void destroyImpl(final boolean destroyJAWTWindowAndAWTDevice) {
    Threading.invoke(true, destroyOnEDTAction, getTreeLock());
    if( destroyJAWTWindowAndAWTDevice ) {
        AWTEDTExecutor.singleton.invoke(getTreeLock(), true /* allowOnNonEDT */, true /* wait */, disposeJAWTWindowAndAWTDeviceOnEDT);
    }
  }

  /** Overridden to cause OpenGL rendering to be performed during
      repaint cycles. Subclasses which override this method must call
      super.paint() in their paint() method in order to function
      properly.
    */
  @Override
  public void paint(final Graphics g) {
    if( Beans.isDesignTime() ) {
      // Make GLCanvas behave better in NetBeans GUI builder
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, getWidth(), getHeight());
      final FontMetrics fm = g.getFontMetrics();
      String name = getName();
      if (name == null) {
        name = getClass().getName();
        final int idx = name.lastIndexOf('.');
        if (idx >= 0) {
          name = name.substring(idx + 1);
        }
      }
      final Rectangle2D bounds = fm.getStringBounds(name, g);
      g.setColor(Color.WHITE);
      g.drawString(name,
                   (int) ((getWidth()  - bounds.getWidth())  / 2),
                   (int) ((getHeight() + bounds.getHeight()) / 2));
    } else if( !this.helper.isAnimatorAnimatingOnOtherThread() ) {
        display();
    }
  }

  /** Overridden to track when this component is added to a container.
      Subclasses which override this method must call
      super.addNotify() in their addNotify() method in order to
      function properly. <P>

      <B>Overrides:</B>
      <DL><DD><CODE>addNotify</CODE> in class <CODE>java.awt.Component</CODE></DD></DL> */
    @SuppressWarnings("deprecation")
    @Override
  public void addNotify() {
    final RecursiveLock _lock = lock;
    _lock.lock();
    try {
        final boolean isBeansDesignTime = Beans.isDesignTime();

        if(DEBUG) {
            System.err.println(getThreadName()+": Info: addNotify - start, bounds: "+this.getBounds()+", isBeansDesignTime "+isBeansDesignTime);
            // Thread.dumpStack();
        }

        if( isBeansDesignTime ) {
            super.addNotify();
        } else {
            /**
             * 'super.addNotify()' determines the GraphicsConfiguration,
             * while calling this class's overridden 'getGraphicsConfiguration()' method
             * after which it creates the native peer.
             * Hence we have to set the 'awtConfig' before since it's GraphicsConfiguration
             * is being used in getGraphicsConfiguration().
             * This code order also allows recreation, ie re-adding the GLCanvas.
             */

            // before native peer is valid: X11
            disableBackgroundErase();

            final GraphicsDevice awtDevice;
            if(null==awtDeviceReq) {
                // Query AWT GraphicsDevice from parent tree, default
                final GraphicsConfiguration gc = super.getGraphicsConfiguration();
                if(null==gc) {
                    throw new GLException("Error: NULL AWT GraphicsConfiguration");
                }
                awtDevice = gc.getDevice();
            } else {
                // Use one time user AWT GraphicsDevice request
                awtDevice = awtDeviceReq;
                awtDeviceReq = null;
            }
            final AWTGraphicsConfiguration awtConfig = chooseGraphicsConfiguration(capsReqUser, capsReqUser, chooser, awtDevice);
            if(null==awtConfig) {
                throw new GLException("Error: NULL AWTGraphicsConfiguration");
            }
            setAWTGraphicsConfiguration(awtConfig);

            // issues getGraphicsConfiguration() and creates the native peer
            super.addNotify();

            // after native peer is valid: Windows
            disableBackgroundErase();

            createJAWTDrawableAndContext();

            // init drawable by paint/display makes the init sequence more equal
            // for all launch flavors (applet/javaws/..)
            // validateGLDrawable();
        }
        awtWindowClosingProtocol.addClosingListener();

        if(DEBUG) {
            System.err.println(getThreadName()+": Info: addNotify - end");
        }
    } finally {
        _lock.unlock();
    }
  }

  @Override
  public final boolean setSurfaceScale(final float[] pixelScale) {
      System.arraycopy(pixelScale, 0, reqPixelScale, 0, 2);
      if( isRealized() && isShowing ) {
          Threading.invoke(true, setSurfaceScaleOnEDTAction, getTreeLock());
          return true;
      } else {
          return false;
      }
  }
  private final Runnable setSurfaceScaleOnEDTAction = new Runnable() {
    @Override
    public void run() {
        final RecursiveLock _lock = lock;
        _lock.lock();
        try {
            if( null != drawable && drawable.isRealized() ) {
                if( setSurfaceScaleImpl(jawtWindow) ) {
                    reshapeImpl(getWidth(), getHeight());
                    if( !helper.isAnimatorAnimatingOnOtherThread() ) {
                        helper.invokeGL(drawable, context, displayAction, initAction); // display
                    }
                }
            }
        } finally {
            _lock.unlock();
        }
    }  };
  private final boolean setSurfaceScaleImpl(final ScalableSurface ns) {
      if( ns.setSurfaceScale(reqPixelScale) ) {
          ns.getCurrentSurfaceScale(hasPixelScale);
          return true;
      } else {
          return false;
      }
  }

  private final boolean updatePixelScale() {
      if( jawtWindow.hasPixelScaleChanged() ) {
          jawtWindow.getMaximumSurfaceScale(maxPixelScale);
          jawtWindow.getMinimumSurfaceScale(minPixelScale);
          return setSurfaceScaleImpl(jawtWindow);
      } else {
          return false;
      }
  }

  @Override
  public final float[] getRequestedSurfaceScale(final float[] result) {
      System.arraycopy(reqPixelScale, 0, result, 0, 2);
      return result;
  }

  @Override
  public final float[] getCurrentSurfaceScale(final float[] result) {
      System.arraycopy(hasPixelScale, 0, result, 0, 2);
      return result;
  }

  @Override
  public float[] getMinimumSurfaceScale(final float[] result) {
      System.arraycopy(minPixelScale, 0, result, 0, 2);
      return result;
  }

  @Override
  public float[] getMaximumSurfaceScale(final float[] result) {
      System.arraycopy(maxPixelScale, 0, result, 0, 2);
      return result;
  }

  private void createJAWTDrawableAndContext() {
    if ( !Beans.isDesignTime() ) {
        jawtWindow = (JAWTWindow) NativeWindowFactory.getNativeWindow(this, awtConfig);
        jawtWindow.setShallUseOffscreenLayer(shallUseOffscreenLayer);
        jawtWindow.lockSurface();
        try {
            jawtWindow.setSurfaceScale(reqPixelScale);
            drawable = (GLDrawableImpl) GLDrawableFactory.getFactory(capsReqUser.getGLProfile()).createGLDrawable(jawtWindow);
            createContextImpl(drawable);
            jawtWindow.getCurrentSurfaceScale(hasPixelScale);
            jawtWindow.getMinimumSurfaceScale(minPixelScale);
            jawtWindow.getMaximumSurfaceScale(maxPixelScale);
        } finally {
            jawtWindow.unlockSurface();
        }
    }
  }
  private boolean createContextImpl(final GLDrawable drawable) {
    final GLContext[] shareWith = { null };
    if( !helper.isSharedGLContextPending(shareWith) ) {
        context = (GLContextImpl) drawable.createContext(shareWith[0]);
        context.setContextCreationFlags(additionalCtxCreationFlags);
        if(DEBUG) {
            System.err.println(getThreadName()+": Context created: has shared "+(null != shareWith[0]));
        }
        return true;
    } else {
        if(DEBUG) {
            System.err.println(getThreadName()+": Context !created: pending share");
        }
        return false;
    }
  }

  private boolean validateGLDrawable() {
      if( Beans.isDesignTime() || !isDisplayable() ) {
          return false; // early out!
      }
      final GLDrawable _drawable = drawable;
      if ( null != _drawable ) {
          boolean res = _drawable.isRealized();
          if( !res ) {
              // re-try drawable creation
              if( 0 >= _drawable.getSurfaceWidth() || 0 >= _drawable.getSurfaceHeight() ) {
                  return false; // early out!
              }
              setRealized(true);
              res = _drawable.isRealized();
              if(DEBUG) {
                  System.err.println(getThreadName()+": Realized Drawable: isRealized "+res+", "+_drawable.toString());
                  // Thread.dumpStack();
              }
          }
          if( res && null == context ) {
              // re-try context creation
              res = createContextImpl(_drawable); // pending creation.
          }
          return res;
      }
      return false;
  }

  private void setAWTGraphicsConfiguration(final AWTGraphicsConfiguration config) {
    // Cache awtConfig
    awtConfig = config;
    if( null != jawtWindow ) {
        // Notify JAWTWindow ..
        jawtWindow.setAWTGraphicsConfiguration(config);
    }
  }

  /** <p>Overridden to track when this component is removed from a
      container. Subclasses which override this method must call
      super.removeNotify() in their removeNotify() method in order to
      function properly. </p>
      <p>User shall not call this method outside of EDT, read the AWT/Swing specs
      about this.</p>
      <B>Overrides:</B>
      <DL><DD><CODE>removeNotify</CODE> in class <CODE>java.awt.Component</CODE></DD></DL> */
    @SuppressWarnings("deprecation")
    @Override
  public void removeNotify() {
    if(DEBUG) {
        System.err.println(getThreadName()+": Info: removeNotify - start");
        // Thread.dumpStack();
    }

    awtWindowClosingProtocol.removeClosingListener();

    if( Beans.isDesignTime() ) {
      super.removeNotify();
    } else {
      try {
        destroyImpl( true );
      } finally {
        super.removeNotify();
      }
    }
    if(DEBUG) {
        System.err.println(getThreadName()+": Info: removeNotify - end");
    }
  }

  /** Overridden to cause {@link GLDrawableHelper#reshape} to be
      called on all registered {@link GLEventListener}s. Subclasses
      which override this method must call super.reshape() in
      their reshape() method in order to function properly. <P>

      <B>Overrides:</B>
      <DL><DD><CODE>reshape</CODE> in class <CODE>java.awt.Component</CODE></DD></DL> */
    @SuppressWarnings("deprecation")
    @Override
  public void reshape(final int x, final int y, final int width, final int height) {
    synchronized (getTreeLock()) { // super.reshape(..) claims tree lock, so we do extend it's lock over reshape
        super.reshape(x, y, width, height);
        reshapeImpl(width, height);
    }
  }
  private void reshapeImpl(final int width, final int height) {
    final int scaledWidth = SurfaceScaleUtils.scale(width, hasPixelScale[0]);
    final int scaledHeight = SurfaceScaleUtils.scale(height, hasPixelScale[1]);

    if(DEBUG) {
        final NativeSurface ns = getNativeSurface();
        final long nsH = null != ns ? ns.getSurfaceHandle() : 0;
        System.err.println(getThreadName()+": GLCanvas.reshape.0 "+this.getName()+" resize"+(printActive?"WithinPrint":"")+
                " [ this "+getWidth()+"x"+getHeight()+", pixelScale "+getPixelScaleStr()+
                "] -> "+(printActive?"[skipped] ":"") + width+"x"+height+" * "+getPixelScaleStr()+" -> "+scaledWidth+"x"+scaledHeight+
                " - surfaceHandle 0x"+Long.toHexString(nsH));
        // Thread.dumpStack();
    }
    if( validateGLDrawable() && !printActive ) {
        final GLDrawableImpl _drawable = drawable;
        if( ! _drawable.getChosenGLCapabilities().isOnscreen() ) {
            final RecursiveLock _lock = lock;
            _lock.lock();
            try {
                final GLDrawableImpl _drawableNew = GLDrawableHelper.resizeOffscreenDrawable(_drawable, context, scaledWidth, scaledHeight);
                if(_drawable != _drawableNew) {
                    // write back
                    drawable = _drawableNew;
                }
            } finally {
               _lock.unlock();
            }
        }
        sendReshape = true; // async if display() doesn't get called below, but avoiding deadlock
    }
  }

  /**
   * Overridden from Canvas to prevent the AWT's clearing of the
   * canvas from interfering with the OpenGL rendering.
   */
  @Override
  public void update(final Graphics g) {
    paint(g);
  }

  private volatile boolean printActive = false;
  private GLAnimatorControl printAnimator = null;
  private GLAutoDrawable printGLAD = null;
  private AWTTilePainter printAWTTiles = null;

  @Override
  public void setupPrint(final double scaleMatX, final double scaleMatY, final int numSamples, final int tileWidth, final int tileHeight) {
      printActive = true;
      final int componentCount = isOpaque() ? 3 : 4;
      final TileRenderer printRenderer = new TileRenderer();
      printAWTTiles = new AWTTilePainter(printRenderer, componentCount, scaleMatX, scaleMatY, numSamples, tileWidth, tileHeight, DEBUG);
      AWTEDTExecutor.singleton.invoke(getTreeLock(), true /* allowOnNonEDT */, true /* wait */, setupPrintOnEDT);
  }
  private final Runnable setupPrintOnEDT = new Runnable() {
      @Override
      public void run() {
          final RecursiveLock _lock = lock;
          _lock.lock();
          try {
              if( !validateGLDrawable() ) {
                  if(DEBUG) {
                      System.err.println(getThreadName()+": Info: GLCanvas setupPrint - skipped GL render, drawable not valid yet");
                  }
                  printActive = false;
                  return; // not yet available ..
              }
              if( !isVisible() ) {
                  if(DEBUG) {
                      System.err.println(getThreadName()+": Info: GLCanvas setupPrint - skipped GL render, canvas not visible");
                  }
                  printActive = false;
                  return; // not yet available ..
              }
              sendReshape = false; // clear reshape flag
              printAnimator =  helper.getAnimator();
              if( null != printAnimator ) {
                  printAnimator.remove(GLCanvas.this);
              }
              printGLAD = GLCanvas.this; // _not_ default, shall be replaced by offscreen GLAD
              final GLCapabilitiesImmutable gladCaps = getChosenGLCapabilities();
              final int printNumSamples = printAWTTiles.getNumSamples(gladCaps);
              GLDrawable printDrawable = printGLAD.getDelegatedDrawable();
              final boolean reqNewGLADSamples = printNumSamples != gladCaps.getNumSamples();
              final boolean reqNewGLADSize = printAWTTiles.customTileWidth != -1 && printAWTTiles.customTileWidth != printDrawable.getSurfaceWidth() ||
                                             printAWTTiles.customTileHeight != -1 && printAWTTiles.customTileHeight != printDrawable.getSurfaceHeight();
              final boolean reqNewGLADOnscrn = gladCaps.isOnscreen();

              final GLCapabilities newGLADCaps = (GLCapabilities)gladCaps.cloneMutable();
              newGLADCaps.setDoubleBuffered(false);
              newGLADCaps.setOnscreen(false);
              if( printNumSamples != newGLADCaps.getNumSamples() ) {
                  newGLADCaps.setSampleBuffers(0 < printNumSamples);
                  newGLADCaps.setNumSamples(printNumSamples);
              }
              final boolean reqNewGLADSafe = GLDrawableUtil.isSwapGLContextSafe(getRequestedGLCapabilities(), gladCaps, newGLADCaps);

              final boolean reqNewGLAD = ( reqNewGLADOnscrn || reqNewGLADSamples || reqNewGLADSize ) && reqNewGLADSafe;

              if( DEBUG ) {
                  System.err.println("AWT print.setup: reqNewGLAD "+reqNewGLAD+"[ onscreen "+reqNewGLADOnscrn+", samples "+reqNewGLADSamples+", size "+reqNewGLADSize+", safe "+reqNewGLADSafe+"], "+
                                     ", drawableSize "+printDrawable.getSurfaceWidth()+"x"+printDrawable.getSurfaceHeight()+
                                     ", customTileSize "+printAWTTiles.customTileWidth+"x"+printAWTTiles.customTileHeight+
                                     ", scaleMat "+printAWTTiles.scaleMatX+" x "+printAWTTiles.scaleMatY+
                                     ", numSamples "+printAWTTiles.customNumSamples+" -> "+printNumSamples+", printAnimator "+printAnimator);
              }
              if( reqNewGLAD ) {
                  final GLDrawableFactory factory = GLDrawableFactory.getFactory(newGLADCaps.getGLProfile());
                  GLOffscreenAutoDrawable offGLAD = null;
                  try {
                      offGLAD = factory.createOffscreenAutoDrawable(null, newGLADCaps, null,
                                  printAWTTiles.customTileWidth != -1 ? printAWTTiles.customTileWidth : DEFAULT_PRINT_TILE_SIZE,
                                  printAWTTiles.customTileHeight != -1 ? printAWTTiles.customTileHeight : DEFAULT_PRINT_TILE_SIZE);
                  } catch (final GLException gle) {
                      if( DEBUG ) {
                          System.err.println("Caught: "+gle.getMessage());
                          gle.printStackTrace();
                      }
                  }
                  if( null != offGLAD ) {
                      printGLAD = offGLAD;
                      GLDrawableUtil.swapGLContextAndAllGLEventListener(GLCanvas.this, printGLAD);
                      printDrawable = printGLAD.getDelegatedDrawable();
                  }
              }
              printAWTTiles.setGLOrientation(printGLAD.isGLOriented(), printGLAD.isGLOriented());
              printAWTTiles.renderer.setTileSize(printDrawable.getSurfaceWidth(), printDrawable.getSurfaceHeight(), 0);
              printAWTTiles.renderer.attachAutoDrawable(printGLAD);
              if( DEBUG ) {
                  System.err.println("AWT print.setup "+printAWTTiles);
                  System.err.println("AWT print.setup AA "+printNumSamples+", "+newGLADCaps);
                  System.err.println("AWT print.setup printGLAD: "+printGLAD.getSurfaceWidth()+"x"+printGLAD.getSurfaceHeight()+", "+printGLAD);
                  System.err.println("AWT print.setup printDraw: "+printDrawable.getSurfaceWidth()+"x"+printDrawable.getSurfaceHeight()+", "+printDrawable);
              }
          } finally {
              _lock.unlock();
          }
      }
  };

  @Override
  public void releasePrint() {
      if( !printActive || null == printGLAD ) {
          throw new IllegalStateException("setupPrint() not called");
      }
      sendReshape = false; // clear reshape flag
      AWTEDTExecutor.singleton.invoke(getTreeLock(), true /* allowOnNonEDT */, true /* wait */, releasePrintOnEDT);
  }
  private final Runnable releasePrintOnEDT = new Runnable() {
      @Override
      public void run() {
          final RecursiveLock _lock = lock;
          _lock.lock();
          try {
              if( DEBUG ) {
                  System.err.println("AWT print.release "+printAWTTiles);
              }
              printAWTTiles.dispose();
              printAWTTiles= null;
              if( printGLAD != GLCanvas.this ) {
                  GLDrawableUtil.swapGLContextAndAllGLEventListener(printGLAD, GLCanvas.this);
                  printGLAD.destroy();
              }
              printGLAD = null;
              if( null != printAnimator ) {
                  printAnimator.add(GLCanvas.this);
                  printAnimator = null;
              }
              sendReshape = true; // trigger reshape, i.e. gl-viewport and -listener - this component might got resized!
              printActive = false;
              display();
          } finally {
              _lock.unlock();
          }
      }
  };

  @Override
  public void print(final Graphics graphics) {
      if( !printActive || null == printGLAD ) {
          throw new IllegalStateException("setupPrint() not called");
      }
      if(DEBUG && !EventQueue.isDispatchThread()) {
          System.err.println(getThreadName()+": Warning: GLCanvas print - not called from AWT-EDT");
          // we cannot dispatch print on AWT-EDT due to printing internal locking ..
      }
      sendReshape = false; // clear reshape flag

      final Graphics2D g2d = (Graphics2D)graphics;
      try {
          printAWTTiles.setupGraphics2DAndClipBounds(g2d, getWidth(), getHeight());
          final TileRenderer tileRenderer = printAWTTiles.renderer;
          if( DEBUG ) {
              System.err.println("AWT print.0: "+tileRenderer);
          }
          if( !tileRenderer.eot() ) {
              try {
                  do {
                      if( printGLAD != GLCanvas.this ) {
                          tileRenderer.display();
                      } else {
                          Threading.invoke(true, displayOnEDTAction, getTreeLock());
                      }
                  } while ( !tileRenderer.eot() );
                  if( DEBUG ) {
                      System.err.println("AWT print.1: "+printAWTTiles);
                  }
              } finally {
                  tileRenderer.reset();
                  printAWTTiles.resetGraphics2D();
              }
          }
      } catch (final NoninvertibleTransformException nte) {
          System.err.println("Caught: Inversion failed of: "+g2d.getTransform());
          nte.printStackTrace();
      }
      if( DEBUG ) {
          System.err.println("AWT print.X: "+printAWTTiles);
      }
  }

  @Override
  public void addGLEventListener(final GLEventListener listener) {
    helper.addGLEventListener(listener);
  }

  @Override
  public void addGLEventListener(final int index, final GLEventListener listener) throws IndexOutOfBoundsException {
    helper.addGLEventListener(index, listener);
  }

  @Override
  public int getGLEventListenerCount() {
      return helper.getGLEventListenerCount();
  }

  @Override
  public GLEventListener getGLEventListener(final int index) throws IndexOutOfBoundsException {
      return helper.getGLEventListener(index);
  }

  @Override
  public boolean areAllGLEventListenerInitialized() {
     return helper.areAllGLEventListenerInitialized();
  }

  @Override
  public boolean getGLEventListenerInitState(final GLEventListener listener) {
      return helper.getGLEventListenerInitState(listener);
  }

  @Override
  public void setGLEventListenerInitState(final GLEventListener listener, final boolean initialized) {
      helper.setGLEventListenerInitState(listener, initialized);
  }

  @Override
  public GLEventListener disposeGLEventListener(final GLEventListener listener, final boolean remove) {
    final DisposeGLEventListenerAction r = new DisposeGLEventListenerAction(listener, remove);
    Threading.invoke(true, r, getTreeLock());
    return r.listener;
  }

  @Override
  public GLEventListener removeGLEventListener(final GLEventListener listener) {
    return helper.removeGLEventListener(listener);
  }

  @Override
  public void setAnimator(final GLAnimatorControl animatorControl) {
    helper.setAnimator(animatorControl);
  }

  @Override
  public GLAnimatorControl getAnimator() {
    return helper.getAnimator();
  }

  @Override
  public final Thread setExclusiveContextThread(final Thread t) throws GLException {
      return helper.setExclusiveContextThread(t, context);
  }

  @Override
  public final Thread getExclusiveContextThread() {
      return helper.getExclusiveContextThread();
  }

  @Override
  public boolean invoke(final boolean wait, final GLRunnable glRunnable) throws IllegalStateException {
    return helper.invoke(this, wait, glRunnable);
  }

  @Override
  public boolean invoke(final boolean wait, final List<GLRunnable> glRunnables) throws IllegalStateException {
    return helper.invoke(this, wait, glRunnables);
  }

  @Override
  public void flushGLRunnables() {
      helper.flushGLRunnables();
  }

  @Override
  public GLContext setContext(final GLContext newCtx, final boolean destroyPrevCtx) {
      final RecursiveLock _lock = lock;
      _lock.lock();
      try {
          final GLContext oldCtx = context;
          GLDrawableHelper.switchContext(drawable, oldCtx, destroyPrevCtx, newCtx, additionalCtxCreationFlags);
          context=(GLContextImpl)newCtx;
          return oldCtx;
      } finally {
          _lock.unlock();
      }
  }

  @Override
  public final GLDrawable getDelegatedDrawable() {
    return drawable;
  }

  @Override
  public GLContext getContext() {
    return context;
  }

  @Override
  public GL getGL() {
    if( Beans.isDesignTime() ) {
      return null;
    }
    final GLContext _context = context;
    return (_context == null) ? null : _context.getGL();
  }

  @Override
  public GL setGL(final GL gl) {
    final GLContext _context = context;
    if (_context != null) {
      _context.setGL(gl);
      return gl;
    }
    return null;
  }


  @Override
  public void setAutoSwapBufferMode(final boolean onOrOff) {
    helper.setAutoSwapBufferMode(onOrOff);
  }

  @Override
  public boolean getAutoSwapBufferMode() {
    return helper.getAutoSwapBufferMode();
  }

  @Override
  public void swapBuffers() {
    Threading.invoke(true, swapBuffersOnEDTAction, getTreeLock());
  }

  @Override
  public void setContextCreationFlags(final int flags) {
    additionalCtxCreationFlags = flags;
    final GLContext _context = context;
    if(null != _context) {
      _context.setContextCreationFlags(additionalCtxCreationFlags);
    }
  }

  @Override
  public int getContextCreationFlags() {
    return additionalCtxCreationFlags;
  }

  @Override
  public GLProfile getGLProfile() {
    return capsReqUser.getGLProfile();
  }

  @Override
  public GLCapabilitiesImmutable getChosenGLCapabilities() {
    if( Beans.isDesignTime() ) {
        return capsReqUser;
    } else if( null == awtConfig ) {
        throw new GLException("No AWTGraphicsConfiguration: "+this);
    }
    return (GLCapabilitiesImmutable)awtConfig.getChosenCapabilities();
  }

  @Override
  public GLCapabilitiesImmutable getRequestedGLCapabilities() {
    if( null == awtConfig ) {
        return capsReqUser;
    }
    return (GLCapabilitiesImmutable)awtConfig.getRequestedCapabilities();
  }

  @Override
  public int getSurfaceWidth() {
      return SurfaceScaleUtils.scale(getWidth(), hasPixelScale[0]);
  }

  @Override
  public int getSurfaceHeight() {
      return SurfaceScaleUtils.scale(getHeight(), hasPixelScale[1]);
  }

  @Override
  public boolean isGLOriented() {
    final GLDrawable _drawable = drawable;
    return null != _drawable ? _drawable.isGLOriented() : true;
  }

  @Override
  public NativeSurface getNativeSurface() {
    final GLDrawable _drawable = drawable;
    return (null != _drawable) ? _drawable.getNativeSurface() : null;
  }

  @Override
  public long getHandle() {
    final GLDrawable _drawable = drawable;
    return (null != _drawable) ? _drawable.getHandle() : 0;
  }

  @Override
  public GLDrawableFactory getFactory() {
    final GLDrawable _drawable = drawable;
    return (null != _drawable) ? _drawable.getFactory() : null;
  }

  @Override
  public String toString() {
    final GLDrawable _drawable = drawable;
    final int dw = (null!=_drawable) ? _drawable.getSurfaceWidth() : -1;
    final int dh = (null!=_drawable) ? _drawable.getSurfaceHeight() : -1;

    return "AWT-GLCanvas[Realized "+isRealized()+
                          ",\n\t"+((null!=_drawable)?_drawable.getClass().getName():"null-drawable")+
                          ",\n\tFactory   "+getFactory()+
                          ",\n\thandle    0x"+Long.toHexString(getHandle())+
                          ",\n\tDrawable size "+dw+"x"+dh+" surface["+getSurfaceWidth()+"x"+getSurfaceHeight()+"]"+
                          ",\n\tAWT[pos "+getX()+"/"+getY()+", size "+getWidth()+"x"+getHeight()+
                          ",\n\tvisible "+isVisible()+", displayable "+isDisplayable()+", showing "+isShowing+
                          ",\n\t"+awtConfig+"]]";
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private final String getPixelScaleStr() { return "["+hasPixelScale[0]+", "+hasPixelScale[1]+"]"; }

  private final Runnable destroyOnEDTAction = new Runnable() {
    @Override
    public void run() {
        final RecursiveLock _lock = lock;
        _lock.lock();
        try {
            final GLAnimatorControl animator =  getAnimator();

            if(DEBUG) {
                System.err.println(getThreadName()+": Info: destroyOnEDTAction() - START, hasContext " +
                        (null!=context) + ", hasDrawable " + (null!=drawable)+", "+animator);
                // Thread.dumpStack();
            }

            final boolean animatorPaused;
            if(null!=animator) {
                // can't remove us from animator for recreational addNotify()
                animatorPaused = animator.pause();
            } else {
                animatorPaused = false;
            }

            GLException exceptionOnDisposeGL = null;

            // OLS will be detached by disposeGL's context destruction below
            if( null != context ) {
                if( context.isCreated() ) {
                    try {
                        helper.disposeGL(GLCanvas.this, context, true);
                        if(DEBUG) {
                            System.err.println(getThreadName()+": destroyOnEDTAction() - post ctx: "+context);
                        }
                    } catch (final GLException gle) {
                        exceptionOnDisposeGL = gle;
                    }
                }
                context = null;
            }

            Throwable exceptionOnUnrealize = null;
            if( null != drawable ) {
                try {
                    drawable.setRealized(false);
                    if(DEBUG) {
                        System.err.println(getThreadName()+": destroyOnEDTAction() - post drawable: "+drawable);
                    }
                } catch( final Throwable re ) {
                    exceptionOnUnrealize = re;
                }
                drawable = null;
            }

            if(animatorPaused) {
                animator.resume();
            }

            // throw exception in order of occurrence ..
            if( null != exceptionOnDisposeGL ) {
                throw exceptionOnDisposeGL;
            }
            if( null != exceptionOnUnrealize ) {
                throw GLException.newGLException(exceptionOnUnrealize);
            }

            if(DEBUG) {
                System.err.println(getThreadName()+": dispose() - END, animator "+animator);
            }

        } finally {
            _lock.unlock();
        }
    }
  };

  /**
   * Disposes the JAWTWindow and AbstractGraphicsDevice within EDT,
   * since resources created (X11: Display), must be destroyed in the same thread, where they have been created.
   * <p>
   * The drawable and context handle are null'ed as well, assuming {@link #destroy()} has been called already.
   * </p>
   *
   * @see #chooseGraphicsConfiguration(com.jogamp.opengl.GLCapabilitiesImmutable, com.jogamp.opengl.GLCapabilitiesImmutable, com.jogamp.opengl.GLCapabilitiesChooser, java.awt.GraphicsDevice)
   */
  private final Runnable disposeJAWTWindowAndAWTDeviceOnEDT = new Runnable() {
    @Override
    public void run() {
        context=null;
        drawable=null;

        if( null != jawtWindow ) {
            jawtWindow.destroy();
            if(DEBUG) {
                System.err.println(getThreadName()+": GLCanvas.disposeJAWTWindowAndAWTDeviceOnEDT(): post JAWTWindow: "+jawtWindow);
            }
            jawtWindow=null;
        }
        hasPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
        hasPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;
        minPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
        minPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;
        maxPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
        maxPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;

        if(null != awtConfig) {
            final AbstractGraphicsConfiguration aconfig = awtConfig.getNativeGraphicsConfiguration();
            final AbstractGraphicsDevice adevice = aconfig.getScreen().getDevice();
            final String adeviceMsg;
            if(DEBUG) {
                adeviceMsg = adevice.toString();
            } else {
                adeviceMsg = null;
            }
            final boolean closed = adevice.close();
            if(DEBUG) {
                System.err.println(getThreadName()+": GLCanvas.disposeJAWTWindowAndAWTDeviceOnEDT(): post GraphicsDevice: "+adeviceMsg+", result: "+closed);
            }
        }
        awtConfig = null;
    }
  };

  private final Runnable initAction = new Runnable() {
    @Override
    public void run() {
      helper.init(GLCanvas.this, !sendReshape);
    }
  };

  private final Runnable displayAction = new Runnable() {
    @Override
    public void run() {
      if (sendReshape) {
        if(DEBUG) {
            System.err.println(getThreadName()+": Reshape: "+getSurfaceWidth()+"x"+getSurfaceHeight());
        }
        // Note: we ignore the given x and y within the parent component
        // since we are drawing directly into this heavyweight component.
        helper.reshape(GLCanvas.this, 0, 0, getSurfaceWidth(), getSurfaceHeight());
        sendReshape = false;
      }

      helper.display(GLCanvas.this);
    }
  };

  private final Runnable displayOnEDTAction = new Runnable() {
    @Override
    public void run() {
        final RecursiveLock _lock = lock;
        _lock.lock();
        try {
            if( null != drawable && drawable.isRealized() ) {
                if( GLCanvas.this.updatePixelScale() ) {
                    GLCanvas.this.reshapeImpl(getWidth(), getHeight());
                }
                helper.invokeGL(drawable, context, displayAction, initAction);
            }
        } finally {
            _lock.unlock();
        }
    }
  };

  private final Runnable swapBuffersOnEDTAction = new Runnable() {
    @Override
    public void run() {
        final RecursiveLock _lock = lock;
        _lock.lock();
        try {
            if( null != drawable && drawable.isRealized() ) {
                drawable.swapBuffers();
            }
        } finally {
            _lock.unlock();
        }
    }
  };

  private class DisposeGLEventListenerAction implements Runnable {
    GLEventListener listener;
    private final boolean remove;
    private DisposeGLEventListenerAction(final GLEventListener listener, final boolean remove) {
        this.listener = listener;
        this.remove = remove;
    }

    @Override
    public void run() {
        final RecursiveLock _lock = lock;
        _lock.lock();
        try {
            listener = helper.disposeGLEventListener(GLCanvas.this, drawable, context, listener, remove);
        } finally {
            _lock.unlock();
        }
    }
  };

  // Disables the AWT's erasing of this Canvas's background on Windows
  // in Java SE 6. This internal API is not available in previous
  // releases, but the system property
  // -Dsun.awt.noerasebackground=true can be specified to get similar
  // results globally in previous releases.
  // private static boolean disableBackgroundEraseInitialized;
  // private static Method  disableBackgroundEraseMethod;
  private void disableBackgroundErase() {
    // if (!disableBackgroundEraseInitialized) {
      // try {
        // AccessController.doPrivileged(new PrivilegedAction<Object>() {
            // @Override
            // public Object run() {
              // try {
                // Class<?> clazz = getToolkit().getClass();
                // while (clazz != null && disableBackgroundEraseMethod == null) {
                  // try {
                    // disableBackgroundEraseMethod =
                      // clazz.getDeclaredMethod("disableBackgroundErase",
                                              // new Class[] { Canvas.class });
                    // disableBackgroundEraseMethod.setAccessible(true);
                  // } catch (final Exception e) {
                    // clazz = clazz.getSuperclass();
                  // }
                // }
              // } catch (final Exception e) {
              // }
              // return null;
            // }
          // });
      // } catch (final Exception e) {
      // }
      // disableBackgroundEraseInitialized = true;
      // if(DEBUG) {
        // System.err.println(getThreadName()+": GLCanvas: TK disableBackgroundErase method found: "+
                // (null!=disableBackgroundEraseMethod));
      // }
    // }
    // if (disableBackgroundEraseMethod != null) {
      // Throwable t=null;
      // try {
        // disableBackgroundEraseMethod.invoke(getToolkit(), new Object[] { this });
      // } catch (final Exception e) {
        // t = e;
      // }
      // if(DEBUG) {
        // System.err.println(getThreadName()+": GLCanvas: TK disableBackgroundErase error: "+t);
      // }
    // }
  }

  /**
   * Issues the GraphicsConfigurationFactory's choosing facility within EDT,
   * since resources created (X11: Display), must be destroyed in the same thread, where they have been created.
   *
   * @param capsChosen
   * @param capsRequested
   * @param chooser
   * @param device
   * @return the chosen AWTGraphicsConfiguration
   *
   * @see #disposeJAWTWindowAndAWTDeviceOnEDT
   */
  private AWTGraphicsConfiguration chooseGraphicsConfiguration(final GLCapabilitiesImmutable capsChosen,
                                                               final GLCapabilitiesImmutable capsRequested,
                                                               final GLCapabilitiesChooser chooser,
                                                               final GraphicsDevice device) {
    // Make GLCanvas behave better in NetBeans GUI builder
    if( Beans.isDesignTime() ) {
      return null;
    }
    if( null == device ) {
        throw new GLException("Error: NULL AWT GraphicsDevice");
    }
    final AbstractGraphicsScreen aScreen = AWTGraphicsScreen.createScreenDevice(device, AbstractGraphicsDevice.DEFAULT_UNIT);
    AWTGraphicsConfiguration config = null;

    if( EventQueue.isDispatchThread() || Thread.holdsLock(getTreeLock()) ) {
        config = (AWTGraphicsConfiguration)
                GraphicsConfigurationFactory.getFactory(AWTGraphicsDevice.class, GLCapabilitiesImmutable.class).chooseGraphicsConfiguration(capsChosen,
                                                                                                             capsRequested,
                                                                                                             chooser, aScreen, VisualIDHolder.VID_UNDEFINED);
    } else {
        try {
            final ArrayList<AWTGraphicsConfiguration> bucket = new ArrayList<AWTGraphicsConfiguration>(1);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    final AWTGraphicsConfiguration c = (AWTGraphicsConfiguration)
                            GraphicsConfigurationFactory.getFactory(AWTGraphicsDevice.class, GLCapabilitiesImmutable.class).chooseGraphicsConfiguration(capsChosen,
                                                                                                                         capsRequested,
                                                                                                                         chooser, aScreen, VisualIDHolder.VID_UNDEFINED);
                    bucket.add(c);
                }
            });
            config = ( bucket.size() > 0 ) ? bucket.get(0) : null ;
        } catch (final InvocationTargetException e) {
            throw new GLException(e.getTargetException());
        } catch (final InterruptedException e) {
            throw new GLException(e);
        }
    }

    if ( null == config ) {
      throw new GLException("Error: Couldn't fetch AWTGraphicsConfiguration");
    }

    return config;
  }

  protected static String getThreadName() { return Thread.currentThread().getName(); }

  /**
   * A most simple JOGL AWT test entry
   */
  public static void main(final String args[]) {
    System.err.println(VersionUtil.getPlatformInfo());
    System.err.println(GlueGenVersion.getInstance());
    // System.err.println(NativeWindowVersion.getInstance());
    System.err.println(JoglVersion.getInstance());

    System.err.println(JoglVersion.getDefaultOpenGLInfo(null, null, true).toString());

    final GLCapabilitiesImmutable caps = new GLCapabilities( GLProfile.getDefault(GLProfile.getDefaultDevice()) );
    final Frame frame = new Frame("JOGL AWT Test");

    final GLCanvas glCanvas = new GLCanvas(caps);
    frame.add(glCanvas);
    frame.setSize(128, 128);

    glCanvas.addGLEventListener(new GLEventListener() {
        @Override
        public void init(final GLAutoDrawable drawable) {
            final GL gl = drawable.getGL();
            System.err.println(JoglVersion.getGLInfo(gl, null));
        }
        @Override
        public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) { }
        @Override
        public void display(final GLAutoDrawable drawable) { }
        @Override
        public void dispose(final GLAutoDrawable drawable) { }
    });

    try {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }});
    } catch (final Throwable t) {
        t.printStackTrace();
    }
    glCanvas.display();
    try {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.dispose();
            }});
    } catch (final Throwable t) {
        t.printStackTrace();
    }
  }

}
