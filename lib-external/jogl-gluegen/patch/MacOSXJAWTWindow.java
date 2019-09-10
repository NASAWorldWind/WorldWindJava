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

package jogamp.nativewindow.jawt.macosx;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.nio.Buffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.MutableSurface;
import com.jogamp.nativewindow.util.Point;

import com.jogamp.common.util.PropertyAccess;
import com.jogamp.nativewindow.awt.JAWTWindow;

import jogamp.nativewindow.Debug;
import jogamp.nativewindow.awt.AWTMisc;
import jogamp.nativewindow.jawt.JAWT;
import jogamp.nativewindow.jawt.JAWTFactory;
import jogamp.nativewindow.jawt.JAWTUtil;
import jogamp.nativewindow.jawt.JAWT_DrawingSurface;
import jogamp.nativewindow.jawt.JAWT_DrawingSurfaceInfo;
import jogamp.nativewindow.jawt.macosx.JAWT_MacOSXDrawingSurfaceInfo;
import jogamp.nativewindow.macosx.OSXUtil;

public class MacOSXJAWTWindow extends JAWTWindow implements MutableSurface {
  /** May lead to deadlock, due to AWT pos comparison .. don't enable for Applets! */
  private static final boolean DEBUG_CALAYER_POS_CRITICAL;

  static {
      Debug.initSingleton();
      DEBUG_CALAYER_POS_CRITICAL = PropertyAccess.isPropertyDefined("nativewindow.debug.JAWT.OSXCALayerPos", true /* jnlpAlias */);
  }

  public MacOSXJAWTWindow(final Object comp, final AbstractGraphicsConfiguration config) {
    super(comp, config);
    if(DEBUG) {
        dumpInfo();
    }
  }

  @Override
  protected void invalidateNative() {
      if(DEBUG) {
          System.err.println("MacOSXJAWTWindow.invalidateNative(): osh-enabled "+isOffscreenLayerSurfaceEnabled()+
                             ", osd-set "+offscreenSurfaceDrawableSet+
                             ", osd "+toHexString(offscreenSurfaceDrawable)+
                             ", osl "+toHexString(getAttachedSurfaceLayer())+
                             ", rsl "+toHexString(rootSurfaceLayer)+
                             ", wh "+toHexString(windowHandle)+" - "+Thread.currentThread().getName());
      }
      offscreenSurfaceDrawable=0;
      offscreenSurfaceDrawableSet=false;
      if( isOffscreenLayerSurfaceEnabled() ) {
          if(0 != windowHandle) {
              OSXUtil.DestroyNSWindow(windowHandle);
          }
          // OSXUtil.RunOnMainThread(false, true /* kickNSApp */, new Runnable() {
          OSXUtil.RunOnMainThread(false, false /* kickNSApp */, new Runnable() {
              @Override
              public void run() {
                  if( 0 != jawtSurfaceLayersHandle) {
                      // null rootSurfaceLayer OK
                      UnsetJAWTRootSurfaceLayer0(jawtSurfaceLayersHandle, rootSurfaceLayer);
                  }
                  jawtSurfaceLayersHandle = 0;
                  if( 0 != rootSurfaceLayer ) {
                      OSXUtil.DestroyCALayer(rootSurfaceLayer);
                      rootSurfaceLayer = 0;
                  }
              }
          });
      }
      windowHandle=0;
  }

  @Override
  public boolean setSurfaceScale(final float[] pixelScale) {
      super.setSurfaceScale(pixelScale);
      if( 0 != getWindowHandle() && setReqPixelScale() ) { // locked at least once _and_ updated pixel-scale
          if( 0 != getAttachedSurfaceLayer() ) {
              OSXUtil.RunOnMainThread(false, false, new Runnable() {
                  @Override
                  public void run() {
                      final long osl = getAttachedSurfaceLayer();
                      if( 0 != osl ) {
                          OSXUtil.SetCALayerPixelScale(rootSurfaceLayer, osl, getPixelScaleX());
                      }
                  }
              });
          }
          return true;
      } else {
          return false;
      }
  }

  @Override
  protected void attachSurfaceLayerImpl(final long layerHandle) {
      OSXUtil.RunOnMainThread(false, false /* kickNSApp */, new Runnable() {
              @Override
              public void run() {
                  // AWT position is top-left w/ insets, where CALayer position is bottom/left from root CALayer w/o insets.
                  // Determine p0: components location on screen w/o insets.
                  // CALayer position will be determined in native code.
                  // See detailed description in {@link JAWTUtil#JAWT_OSX_CALAYER_QUIRK_LAYOUT}
                  final Point p0 = new Point();
                  final Component outterComp = AWTMisc.getLocationOnScreenNonBlocking(p0, component, DEBUG);
                  final java.awt.Insets outterInsets = AWTMisc.getInsets(outterComp, true);
                  final Point p1 = (Point)p0.cloneMutable();
                  p1.translate(-outterComp.getX(), -outterComp.getY());
                  if( null != outterInsets ) {
                      p1.translate(-outterInsets.left, -outterInsets.top);
                  }

                  if( DEBUG_CALAYER_POS_CRITICAL ) {
                      final java.awt.Point pA0 = component.getLocationOnScreen();
                      final Point pA1 = new Point(pA0.x, pA0.y);
                      pA1.translate(-outterComp.getX(), -outterComp.getY());
                      if( null != outterInsets ) {
                          pA1.translate(-outterInsets.left, -outterInsets.top);
                      }
                      System.err.println("JAWTWindow.attachSurfaceLayerImpl: "+toHexString(layerHandle) + ", [ins "+outterInsets+"], pA "+pA0+" -> "+pA1+
                              ", p0 "+p0+" -> "+p1+", bounds "+bounds);
                  } else if( DEBUG ) {
                      System.err.println("JAWTWindow.attachSurfaceLayerImpl: "+toHexString(layerHandle) + ", [ins "+outterInsets+"], p0 "+p0+" -> "+p1+", bounds "+bounds);
                  }
                  // HiDPI: uniform pixel scale
                  OSXUtil.AddCASublayer(rootSurfaceLayer, layerHandle, p1.getX(), p1.getY(), getWidth(), getHeight(), getPixelScaleX(), JAWTUtil.getOSXCALayerQuirks());
              } } );
  }

  @Override
  protected void layoutSurfaceLayerImpl(final long layerHandle, final boolean visible) {
      final int caLayerQuirks = JAWTUtil.getOSXCALayerQuirks();
      // AWT position is top-left w/ insets, where CALayer position is bottom/left from root CALayer w/o insets.
      // Determine p0: components location on screen w/o insets.
      // CALayer position will be determined in native code.
      // See detailed description in {@link JAWTUtil#JAWT_OSX_CALAYER_QUIRK_LAYOUT}
      final Point p0 = new Point();
      final Component outterComp = AWTMisc.getLocationOnScreenNonBlocking(p0, component, DEBUG);
      final java.awt.Insets outterInsets = AWTMisc.getInsets(outterComp, true);
      final Point p1 = (Point)p0.cloneMutable();
      p1.translate(-outterComp.getX(), -outterComp.getY());
      if( null != outterInsets ) {
          p1.translate(-outterInsets.left, -outterInsets.top);
      }

      if( DEBUG_CALAYER_POS_CRITICAL ) {
          final java.awt.Point pA0 = component.getLocationOnScreen();
          final Point pA1 = new Point(pA0.x, pA0.y);
          pA1.translate(-outterComp.getX(), -outterComp.getY());
          if( null != outterInsets ) {
              pA1.translate(-outterInsets.left, -outterInsets.top);
          }
          System.err.println("JAWTWindow.layoutSurfaceLayerImpl: "+toHexString(layerHandle) + ", quirks "+caLayerQuirks+", visible "+visible+
                  ", [ins "+outterInsets+"], pA "+pA0+" -> "+pA1+
                  ", p0 "+p0+" -> "+p1+", bounds "+bounds);
      } else if( DEBUG ) {
          System.err.println("JAWTWindow.layoutSurfaceLayerImpl: "+toHexString(layerHandle) + ", quirks "+caLayerQuirks+", visible "+visible+
                  ", [ins "+outterInsets+"], p0 "+p0+" -> "+p1+", bounds "+bounds);
      }
      OSXUtil.FixCALayerLayout(rootSurfaceLayer, layerHandle, visible, p1.getX(), p1.getY(), getWidth(), getHeight(), caLayerQuirks);
  }

  @Override
  protected void detachSurfaceLayerImpl(final long layerHandle, final Runnable detachNotify) {
      OSXUtil.RunOnMainThread(false, true /* kickNSApp */, new Runnable() {
              @Override
              public void run() {
                  detachNotify.run();
                  OSXUtil.RemoveCASublayer(rootSurfaceLayer, layerHandle);
              } });
  }

  @Override
  public final long getWindowHandle() {
    return windowHandle;
  }

  @Override
  public final long getSurfaceHandle() {
    return offscreenSurfaceDrawableSet ? offscreenSurfaceDrawable : drawable /* super.getSurfaceHandle() */ ;
  }

  @Override
  public void setSurfaceHandle(final long surfaceHandle) {
      if( !isOffscreenLayerSurfaceEnabled() ) {
          throw new java.lang.UnsupportedOperationException("Not using CALAYER");
      }
      if(DEBUG) {
        System.err.println("MacOSXJAWTWindow.setSurfaceHandle(): "+toHexString(surfaceHandle));
      }
      this.offscreenSurfaceDrawable = surfaceHandle;
      this.offscreenSurfaceDrawableSet = true;
  }

  @Override
  protected JAWT fetchJAWTImpl() throws NativeWindowException {
       // use offscreen if supported and [ applet or requested ]
      return JAWTUtil.getJAWT(getShallUseOffscreenLayer() || isApplet());
  }

  @Override
  protected int lockSurfaceImpl(final GraphicsConfiguration gc) throws NativeWindowException {
    int ret = NativeSurface.LOCK_SURFACE_NOT_READY;
    ds = getJAWT().GetDrawingSurface(component);
    if (ds == null) {
      // Widget not yet realized
      unlockSurfaceImpl();
      return NativeSurface.LOCK_SURFACE_NOT_READY;
    }
    final int res = ds.Lock();
    dsLocked = ( 0 == ( res & JAWTFactory.JAWT_LOCK_ERROR ) ) ;
    if (!dsLocked) {
      unlockSurfaceImpl();
      throw new NativeWindowException("Unable to lock surface");
    }
    // See whether the surface changed and if so destroy the old
    // OpenGL context so it will be recreated (NOTE: removeNotify
    // should handle this case, but it may be possible that race
    // conditions can cause this code to be triggered -- should test
    // more)
    if ((res & JAWTFactory.JAWT_LOCK_SURFACE_CHANGED) != 0) {
      ret = NativeSurface.LOCK_SURFACE_CHANGED;
    }
    if (firstLock) {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
          @Override
          public Object run() {
            dsi = ds.GetDrawingSurfaceInfo();
            return null;
          }
        });
    } else {
      dsi = ds.GetDrawingSurfaceInfo();
    }
    if (dsi == null) {
      unlockSurfaceImpl();
      return NativeSurface.LOCK_SURFACE_NOT_READY;
    }
    updateLockedData(dsi.getBounds(), gc);
    if (DEBUG && firstLock ) {
      dumpInfo();
    }
    firstLock = false;
    if( !isOffscreenLayerSurfaceEnabled() ) {
        macosxdsi = (JAWT_MacOSXDrawingSurfaceInfo) dsi.platformInfo(getJAWT());
        if (macosxdsi == null) {
          unlockSurfaceImpl();
          return NativeSurface.LOCK_SURFACE_NOT_READY;
        }
        drawable = macosxdsi.getCocoaViewRef();

        if (drawable == 0) {
          unlockSurfaceImpl();
          return NativeSurface.LOCK_SURFACE_NOT_READY;
        } else {
          windowHandle = OSXUtil.GetNSWindow(drawable);
          ret = NativeSurface.LOCK_SUCCESS;
        }
    } else {
        /**
         * Only create a fake invisible NSWindow for the drawable handle
         * to please frameworks requiring such (eg. NEWT).
         *
         * The actual surface/ca-layer shall be created/attached
         * by the upper framework (JOGL) since they require more information.
         */
        String errMsg = null;
        if(0 == drawable) {
            windowHandle = OSXUtil.CreateNSWindow(0, 0, 64, 64);
            if(0 == windowHandle) {
              errMsg = "Unable to create dummy NSWindow (layered case)";
            } else {
                drawable = OSXUtil.GetNSView(windowHandle);
                if(0 == drawable) {
                  errMsg = "Null NSView of NSWindow "+toHexString(windowHandle);
                }
            }
            if(null == errMsg) {
                // Fix caps reflecting offscreen! (no GL available here ..)
                final Capabilities caps = (Capabilities) getGraphicsConfiguration().getChosenCapabilities().cloneMutable();
                caps.setOnscreen(false);
                setChosenCapabilities(caps);
            }
        }
        if(null == errMsg) {
            jawtSurfaceLayersHandle = GetJAWTSurfaceLayersHandle0(dsi.getBuffer());
            OSXUtil.RunOnMainThread(false, false, new Runnable() {
                    @Override
                    public void run() {
                        String errMsg = null;
                        if(0 == rootSurfaceLayer && 0 != jawtSurfaceLayersHandle) {
                            rootSurfaceLayer = OSXUtil.CreateCALayer(bounds.getWidth(), bounds.getHeight(), getPixelScaleX()); // HiDPI: uniform pixel scale
                            if(0 == rootSurfaceLayer) {
                              errMsg = "Could not create root CALayer";
                            } else {
                                try {
                                    SetJAWTRootSurfaceLayer0(jawtSurfaceLayersHandle, rootSurfaceLayer);
                                } catch(final Exception e) {
                                    errMsg = "Could not set JAWT rootSurfaceLayerHandle "+toHexString(rootSurfaceLayer)+", cause: "+e.getMessage();
                                }
                            }
                            if(null != errMsg) {
                                if(0 != rootSurfaceLayer) {
                                  OSXUtil.DestroyCALayer(rootSurfaceLayer);
                                  rootSurfaceLayer = 0;
                                }
                                throw new NativeWindowException(errMsg+": "+MacOSXJAWTWindow.this);
                            }
                        }
                    } } );
        }
        if(null != errMsg) {
            if(0 != windowHandle) {
              OSXUtil.DestroyNSWindow(windowHandle);
              windowHandle = 0;
            }
            drawable = 0;
            unlockSurfaceImpl();
            throw new NativeWindowException(errMsg+": "+this);
        }
        ret = NativeSurface.LOCK_SUCCESS;
    }

    return ret;
  }

  @Override
  protected void unlockSurfaceImpl() throws NativeWindowException {
    if(null!=ds) {
        if (null!=dsi) {
            ds.FreeDrawingSurfaceInfo(dsi);
        }
        if (dsLocked) {
            ds.Unlock();
        }
        getJAWT().FreeDrawingSurface(ds);
    }
    ds = null;
    dsi = null;
  }

  private void dumpInfo() {
      System.err.println("MaxOSXJAWTWindow: 0x"+Integer.toHexString(this.hashCode())+" - thread: "+Thread.currentThread().getName());
      dumpJAWTInfo();
  }

  /**
   * {@inheritDoc}
   * <p>
   * On OS X locking the surface at this point (ie after creation and for location validation)
   * is 'tricky' since the JVM traverses through many threads and crashes at:
   *   lockSurfaceImpl() {
   *      ..
   *      ds = getJAWT().GetDrawingSurface(component);
   * due to a SIGSEGV.
   *
   * Hence we have some threading / sync issues with the native JAWT implementation.
   * </p>
   */
  @Override
  public Point getLocationOnScreen(Point storage) {
      if( null == storage ) {
          storage = new Point();
      }
      AWTMisc.getLocationOnScreenNonBlocking(storage, component, DEBUG);
      return storage;
  }
  @Override
  protected Point getLocationOnScreenNativeImpl(final int x0, final int y0) { return null; }


  private static native long GetJAWTSurfaceLayersHandle0(Buffer jawtDrawingSurfaceInfoBuffer);

  /**
   * Set the given root CALayer in the JAWT surface
   */
  private static native void SetJAWTRootSurfaceLayer0(long jawtSurfaceLayersHandle, long caLayer);

  /**
   * Unset the given root CALayer in the JAWT surface, passing the NIO DrawingSurfaceInfo buffer
   */
  private static native void UnsetJAWTRootSurfaceLayer0(long jawtSurfaceLayersHandle, long caLayer);

  // Variables for lockSurface/unlockSurface
  private JAWT_DrawingSurface ds;
  private boolean dsLocked;
  private JAWT_DrawingSurfaceInfo dsi;
  private long jawtSurfaceLayersHandle;

  private JAWT_MacOSXDrawingSurfaceInfo macosxdsi;

  private volatile long rootSurfaceLayer = 0; // attached to the JAWT_SurfaceLayer

  private long windowHandle = 0;
  private long offscreenSurfaceDrawable = 0;
  private boolean offscreenSurfaceDrawableSet = false;

  // Workaround for instance of 4796548
  private boolean firstLock = true;

}

