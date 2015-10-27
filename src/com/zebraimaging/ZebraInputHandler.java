package com.zebraimaging;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.*;

import javax.media.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * An alternative input handler used to synchronize input with the Zebra Imaging display controller. Applications are
 * not expected to create instances of this class directly or call its methods. To use it, specify it as the
 * gov.nasa.worldwind.avkey.InputHandlerClassName in the World Wind configuration file.
 */
public class ZebraInputHandler extends AWTInputHandler
{
    /** All instantiations of this class are stored for internal retrieval. */
    private static List<ZebraInputHandler> instances = new ArrayList<ZebraInputHandler>();
    private static Timer repaintContextsTimer = null;
    
    final static TimerTask repaintContextsTask = new TimerTask()
	{
		public void run()        	
		{
			Iterator<ZebraInputHandler> itr = instances.iterator();
	        while (itr.hasNext())
	        {
	            ZebraInputHandler h = itr.next();	           
	            if (h.NeedsRefresh() == true)
	            {
	            	h.SetRefresh(false);
	            	h.getWorldWindow().redraw();
	            }        		            	
	        }
		}
	};
	
	private long hwnd = 0;
    private boolean arGL2Present = false;    
    private boolean refresh = false;
    
    public ZebraInputHandler()
    {
        /**
         * Attempt to load zebraIntegrator.  If it's not found, assume we're either:
         * (a) Not connected to the Zebra UPSD Dynamic Display.
         * (b) Not using the Zebra integration tools.
         */
        try
        {        	
            System.loadLibrary("arGL2Integrator");
            arGL2Present = true;
            instances.add(this);
            System.out.println("Loaded arGL2Integrator successfully");
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println("FAILED to load arGL2Integrator.dll");
        }
        
        if (repaintContextsTimer == null)
        {
        	repaintContextsTimer = new Timer();
        	repaintContextsTimer.scheduleAtFixedRate(repaintContextsTask, 0, 10);
        }
    }

    private synchronized void SetRefresh(boolean value)
    { 
    	refresh = value;
    }
    
    private synchronized boolean NeedsRefresh()
    {
    	return refresh;
	}
    
    public void keyPressed(KeyEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraKeyPressed(getGLCanvasHandle(), e.getKeyCode());
        if (consumed == true)
            e.consume();
        else
            super.keyPressed(e);
    }

    public void keyReleased(KeyEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraKeyReleased(getGLCanvasHandle(), e.getKeyCode());
        if (consumed == true)
            e.consume();
        else
            super.keyReleased(e);
    }

    public void mouseClicked(MouseEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraMouseReleased(getGLCanvasHandle(), e.getButton(), e.getX(), e.getY());
        if (consumed == true)
            e.consume();
        else
            super.mouseClicked(e);
    }

    public void mousePressed(MouseEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraMousePressed(getGLCanvasHandle(), e.getButton(), e.getX(), e.getY());
        if (consumed == true)
            e.consume();
        else
            super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraMouseReleased(getGLCanvasHandle(), e.getButton(), e.getX(), e.getY());
        if (consumed == true)
            e.consume();
        else
            super.mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        /** The mouseDragged event does not populate the button field of MouseEvent.  Therefore it must be done manually. */
        int button = 0;
        button = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK ? 1 : button;
        button = (e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) == InputEvent.BUTTON2_DOWN_MASK ? 2 : button;
        button = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK ? 3 : button;

        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraMouseMoved(getGLCanvasHandle(), button, e.getX(), e.getY());
        if (consumed == true)
            e.consume();
        else
            super.mouseDragged(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        boolean consumed = false;
        if (arGL2Present)
            consumed = zebraMouseWheel(getGLCanvasHandle(), e.getWheelRotation());
        if (consumed == true)
            e.consume();
        else
            super.mouseWheelMoved(e);
    }

    private long getGLCanvasHandle()
    {
        /**
         *  Returns the win32 HWND handle of the GLCanvas component by calling native
         *  C++ code in arGL2Integrator.
         */
        if (hwnd == 0)
        {
            WorldWindow ww = this.getWorldWindow();
            if (ww != null)
            {
                WorldWindowGLCanvas wwgl = (WorldWindowGLCanvas) ww;
                GLCanvas glc = wwgl;
                Canvas cv = glc;
                Component c = cv;
                hwnd = zebraGetWin32Handle(c);
            }
        }

        return hwnd;
    }

    private static ZebraInputHandler getInstance(long hwnd)
    {
        Iterator<ZebraInputHandler> itr = instances.iterator();
        while (itr.hasNext())
        {
            ZebraInputHandler h = itr.next();
            if (h.hwnd == hwnd)
                return h;
        }

        return null;
    }

    // Java static methods executed by arGL2Integrator.dll via JNI

    public static void forceRepaint(long hwnd)
    {   
        /** Force the instance of the ZebraViewInputHandler class to redraw it's associated OpenGL window. */
        ZebraInputHandler h = getInstance(hwnd);
        if (h != null)
        {
        	h.SetRefresh(true);
        	//h.refresh = true;
        }
    }

    public static double[] getModelviewMatrix(long hwnd)
    {
        double[] matrix = new double[16];

        ZebraInputHandler h = getInstance(hwnd);
        if (h != null)
        {
            h.getWorldWindow().getView().getModelviewMatrix().toArray(matrix, 0, false);
        }

        return matrix;
    }

    public static double[] getProjectionMatrix(long hwnd)
    {
        double[] matrix = new double[16];

        ZebraInputHandler h = getInstance(hwnd);
        if (h != null)
        {
            h.getWorldWindow().getView().getProjectionMatrix().toArray(matrix, 0, false);
        }

        return matrix;
    }

    //   Methods imported from the zebra's arGL2Integrator.dll library and executed by java

    public native boolean zebraKeyPressed(long hwnd, int keyCode);

    public native boolean zebraKeyReleased(long hwnd, int keyCode);

    public native boolean zebraMousePressed(long hwnd, int button, int x, int y);

    public native boolean zebraMouseReleased(long hwnd, int button, int x, int y);

    public native boolean zebraMouseMoved(long hwnd, int button, int x, int y);

    public native boolean zebraMouseWheel(long hwnd, int delta);

    public native void zebraSetModelview(long hwnd, double[] matrix);

    public native void zebraSetProjection(long hwnd, double[] matrix);

    public native long zebraGetWin32Handle(Component component);
}
