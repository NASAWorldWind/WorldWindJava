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
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.AVKey;

import javax.sound.sampled.*;

/**
 * Plays an audio file.
 *
 * @author Patrick Murris
 * @version $Id: AudioPlayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AudioPlayer
{
    private javax.sound.sampled.Clip clip;
    private String status = AVKey.STOP;
    protected long pausedMicrosecondPosition;
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    
    protected LineListener lineListener = new LineListener()
    {
        public void update(LineEvent e)
        {
            onLineEvent(e);
        }
    };

    public AudioPlayer()
    {
    }

    public AudioPlayer(Clip clip)
    {
        this.setClip(clip);
    }

    public Clip getClip()
    {
        return this.clip;
    }

    public void setClip(Clip clip)
    {
        if (this.clip == clip)
            return;

        if (this.clip != null)
        {
            if (this.lineListener != null)
            {
                this.clip.removeLineListener(this.lineListener);
            }
        }

        this.clip = clip;

        if (this.clip != null)
        {
            if (this.lineListener != null)
            {
                this.clip.addLineListener(this.lineListener);
            }
        }
    }

    public String getStatus()
    {
        return this.status;
    }

    public long getMillisecondLength()
    {
        if (this.clip == null)
            return 0;

        return this.clip.getMicrosecondLength() / 1000;
    }

    public long getMillisecondPosition()
    {
        if (this.clip == null)
            return 0;

        return this.clip.getMicrosecondPosition() / 1000;
    }

    public void setMillisecondPosition(long position)
    {
        if (this.clip == null)
            return;

        if (position < 0 || position > getMillisecondLength())
            return;

        this.clip.setMicrosecondPosition(position * 1000);
    }

    @SuppressWarnings({"StringEquality"})
    public void play()
    {
        if (this.clip == null)
            return;

        if (this.getStatus() == AVKey.PAUSE)
        {
            this.doStart(this.pausedMicrosecondPosition);
        }
        else if (this.getStatus() == AVKey.STOP)
        {
            this.doStart(0);
        }

        this.pausedMicrosecondPosition = 0;
    }

    @SuppressWarnings({"StringEquality"})
    public void stop()
    {
        if (this.clip == null)
            return;

        this.doStop(0);
    }

    @SuppressWarnings({"StringEquality"})
    public void pause()
    {
        if (this.clip == null)
            return;

        if (this.getStatus() == AVKey.PLAY)
        {
            this.doPause();
        }
    }

    public LineListener[] getLineListeners()
    {
        return this.listenerList.getListeners(LineListener.class);
    }

    public void addLineListener(LineListener listener)
    {
        this.listenerList.add(LineListener.class, listener);
    }

    public void removeLineListener(LineListener listener)
    {
        this.listenerList.remove(LineListener.class, listener);
    }

    protected void doStart(long microsecondPosition)
    {
        this.status = AVKey.PLAY;
        this.clip.setMicrosecondPosition(microsecondPosition);
        this.clip.start();
    }

    @SuppressWarnings({"StringEquality"})
    protected void doStop(long microsecondPosition)
    {
        boolean needToStop = (this.getStatus() != AVKey.STOP);

        this.status = AVKey.STOP;
        this.pausedMicrosecondPosition = microsecondPosition;
        this.clip.setMicrosecondPosition(microsecondPosition);

        if (needToStop)
        {
            this.clip.stop();
        }
    }

    protected void doPause()
    {
        this.status = AVKey.PAUSE;
        this.pausedMicrosecondPosition = this.clip.getMicrosecondPosition();
        this.clip.stop();
    }

    protected void onLineEvent(final LineEvent e)
    {
        // This event comes from the Java Sound Dispatch Thread. Synchronize access to this class by processing the
        // event on the AWT Event Thread.
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                processLineEvent(e);
            }
        });
    }

    @SuppressWarnings({"StringEquality"})
    protected void processLineEvent(LineEvent e)
    {
        if (e.getType() == LineEvent.Type.STOP)
        {
            // If the player's statis is STATUS_PLAY, then this event is arriving because the clip has reached its end,
            // but not due to an explicit call to Clip.stop(). In this case, we must explicity stop the clip to keep
            // the player's state synchronized with the clip.
            if (this.getStatus() == AVKey.PLAY)
            {
                long microsecondLength = this.getClip().getMicrosecondLength();
                this.doStop(microsecondLength);
            }
        }

        this.fireUpdate(e);
    }

    protected void fireUpdate(LineEvent e)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == LineListener.class)
            {
                ((LineListener) listeners[i+1]).update(e);
            }
        }
    }
}
