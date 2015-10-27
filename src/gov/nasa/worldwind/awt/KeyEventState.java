/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.awt;

import java.awt.event.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: KeyEventState.java 2193 2014-08-01 23:33:16Z dcollins $
 */
public class KeyEventState implements KeyListener, MouseListener
{
    protected static class InputState
    {

        protected int eventType;
        protected int keyOrButtonCode;
        protected long timestamp;

        public InputState(int eventType, int keyOrButtonCode, long timestamp)
        {
            this.eventType = eventType;
            this.keyOrButtonCode = keyOrButtonCode;
            this.timestamp = timestamp;
        }

        public int getEventType()
        {
            return this.eventType;
        }

        public int getKeyOrButtonCode()
        {
            return this.keyOrButtonCode;
        }

        public long getTimestamp()
        {
            return this.timestamp;
        }
    }

    protected Map<Object, InputState> keyStateMap = new HashMap<Object, InputState>();
    protected int modifiers;
    protected int modifiersEx;
    protected int mouseModifiers;
    protected int mouseModifiersEx;

    public KeyEventState()
    {
    }

    public boolean isKeyDown(int keyCode)
    {
        InputState state = this.getKeyState(keyCode);
        return state != null && state.getEventType() == KeyEvent.KEY_PRESSED;
    }

    public int keyState(int keyCode)
    {
        InputState state = this.getKeyState(keyCode);
        return state != null && state.getEventType() == KeyEvent.KEY_PRESSED ? 1 : 0;
    }

    public int getNumKeysDown()
    {
        if (keyStateMap.isEmpty())
        {
            return(0);
        }
        int numKeys = 0;
        for (Object o : this.keyStateMap.keySet())
        {
            //Integer key = (KeyEvent) o;
            InputState is = this.keyStateMap.get(o);
            if (is.getEventType() == KeyEvent.KEY_PRESSED)
            {
                numKeys++;
            }

        }
        return(numKeys);
    }

    public int getNumButtonsDown()
    {
        if (keyStateMap.isEmpty())
        {
            return(0);
        }
        int numKeys = 0;
        for (Object o : this.keyStateMap.keySet())
        {
            //Integer key = (KeyEvent) o;
            InputState is = this.keyStateMap.get(o);
            if (is.getEventType() == MouseEvent.MOUSE_PRESSED)
            {
                numKeys++;
            }

        }
        return(numKeys);
    }

    public int getModifiers()
    {
        return this.modifiers;
    }

    public int getModifiersEx()
    {
        return this.modifiersEx;
    }

    public int getMouseModifiers()
    {
        return this.mouseModifiers;
    }

    public int getMouseModifiersEx()
    {
        return this.mouseModifiersEx;
    }

    public void clearKeyState()
    {
        this.keyStateMap.clear();
        this.modifiers = 0;
        this.modifiersEx = 0;
        this.mouseModifiers = 0;
        this.mouseModifiersEx = 0;
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        this.onKeyEvent(e, KeyEvent.KEY_PRESSED);
    }

    public void keyReleased(KeyEvent e)
    {
        this.removeKeyState(e);
    }

    protected void onKeyEvent(KeyEvent e, int eventType)
    {
        if (e == null)
            return;

        long timestamp = this.getTimeStamp(e, eventType, this.keyStateMap.get(e.getKeyCode()));
        this.setKeyState(e.getKeyCode(), new InputState(eventType, e.getKeyCode(), timestamp));
        this.setModifiers(e.getModifiers());
        this.setModifiersEx(e.getModifiersEx());
    }

    public void mouseClicked(java.awt.event.MouseEvent mouseEvent)
    {
    }

    public void mousePressed(java.awt.event.MouseEvent e)
    {
        long timestamp = this.getTimeStamp(e, MouseEvent.MOUSE_PRESSED, this.keyStateMap.get(e.getModifiersEx()));
        this.setKeyState(e.getButton(), new InputState(MouseEvent.MOUSE_PRESSED, e.getButton(), timestamp));
        this.setMouseModifiers(e.getModifiers());
        this.setMouseModifiersEx(e.getModifiersEx());
    }

    public void mouseReleased(java.awt.event.MouseEvent e)
    {
        this.keyStateMap.remove(e.getButton());
        this.setMouseModifiers(0);
        this.setMouseModifiersEx(0);
    }

    public void mouseEntered(java.awt.event.MouseEvent mouseEvent)
    {

    }

    public void mouseExited(java.awt.event.MouseEvent mouseEvent)
    {
        
    }

    protected InputState getKeyState(int keyCode)
    {
        return this.keyStateMap.get(keyCode);
    }

    protected void setKeyState(int keyCode, InputState state)
    {
        this.keyStateMap.put(keyCode, state);
    }

    protected void setModifiers(int modifiers)
    {
        this.modifiers = modifiers;
    }

    protected void setModifiersEx(int modifiersEx)
    {
        this.modifiersEx = modifiersEx;
    }

    protected void setMouseModifiersEx(int modifiersEx)
    {
        this.mouseModifiersEx = modifiersEx;
    }

    protected void setMouseModifiers(int modifiers)
    {
        this.mouseModifiers = modifiers;
    }



    protected void removeKeyState(KeyEvent e) {
        this.keyStateMap.remove(e.getKeyCode());
        this.setModifiers(e.getModifiers());
        this.setModifiersEx(e.getModifiersEx());
    }

    protected long getTimeStamp(InputEvent e, int eventType, InputState currentState)
    {
        // If the current state for this input event type exists and is not null, then keep the current timestamp.
        if (currentState != null && currentState.getEventType() == eventType)
            return currentState.getTimestamp();
        // Otherwise return the InputEvent's timestamp.
        return e.getWhen();
    }
}