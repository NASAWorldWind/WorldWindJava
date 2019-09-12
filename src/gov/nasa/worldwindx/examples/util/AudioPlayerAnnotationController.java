/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;

import javax.sound.sampled.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: AudioPlayerAnnotationController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AudioPlayerAnnotationController extends DialogAnnotationController implements LineListener
{
    protected static final long PLAYER_UPDATE_DELAY_MILLIS = 20;

    private AudioPlayer audioPlayer;
    private javax.swing.Timer playerUpdateTimer;

    public AudioPlayerAnnotationController(WorldWindow worldWindow, AudioPlayerAnnotation audioAnnotation, Clip clip)
    {
        super(worldWindow, audioAnnotation);

        this.setAudioPlayer(new AudioPlayer());
        this.setClip(clip);
    }

    public AudioPlayerAnnotationController(WorldWindow worldWindow, AudioPlayerAnnotation audioAnnotation)
    {
        this(worldWindow, audioAnnotation, null);
    }

    public Clip getClip()
    {
        return this.audioPlayer.getClip();
    }

    public void setClip(Clip clip)
    {
        this.audioPlayer.setClip(clip);
        this.updateAudioAnnotation();
    }

    protected AudioPlayer getAudioPlayer()
    {
        return this.audioPlayer;
    }

    protected void setAudioPlayer(AudioPlayer audioPlayer)
    {
        if (this.audioPlayer == audioPlayer)
            return;

        this.stopPlayerUpdate();

        if (this.audioPlayer != null)
        {
            this.audioPlayer.removeLineListener(this);
        }

        this.audioPlayer = audioPlayer;

        if (this.audioPlayer != null)
        {
            this.audioPlayer.addLineListener(this);
        }
    }

    public String getClipStatus()
    {
        return (this.audioPlayer != null) ? this.audioPlayer.getStatus() : null;
    }

    public void startClip()
    {
        if (this.audioPlayer == null)
            return;

        this.audioPlayer.play();
        this.updateAudioAnnotation();
    }

    public void stopClip()
    {
        if (this.audioPlayer == null)
            return;

        this.audioPlayer.stop();
        this.updateAudioAnnotation();
    }

    public void pauseClip()
    {
        if (this.audioPlayer == null)
            return;

        this.audioPlayer.pause();
        this.updateAudioAnnotation();
    }

    protected void updateAudioAnnotation()
    {
        if (this.getAnnotation() == null)
            return;

        if (this.audioPlayer != null)
        {
            long pos = this.audioPlayer.getMillisecondPosition();
            long length = this.audioPlayer.getMillisecondLength();
            boolean haveClip = (this.audioPlayer.getClip() != null);
            this.doUpdateAudioAnnotation(pos, length, haveClip);
        }
        else
        {
            this.doUpdateAudioAnnotation(0, 0, false);
        }
    }

    protected void doUpdateAudioAnnotation(long position, long length, boolean haveClip)
    {
        AudioPlayerAnnotation audioAnnotation = (AudioPlayerAnnotation) this.getAnnotation();
        audioAnnotation.setClipPosition(position);
        audioAnnotation.setClipLength(length);
        audioAnnotation.getPlayButton().setEnabled(haveClip);
        audioAnnotation.getBackButton().setEnabled(haveClip);

        this.getWorldWindow().redraw();
    }

    //**************************************************************//
    //********************  Action Listener  ***********************//
    //**************************************************************//

    @SuppressWarnings({"StringEquality"})
    public void onActionPerformed(ActionEvent e)
    {
        super.onActionPerformed(e);

        if (e.getActionCommand() == AVKey.PLAY)
        {
            this.playPressed(e);
        }
        else if (e.getActionCommand() == AVKey.STOP)
        {
            this.stopPressed(e);
        }
    }

    protected void playPressed(ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onPlayPressed(e);
    }

    protected void stopPressed(ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onStopPressed(e);
    }

    @SuppressWarnings({"UnusedDeclaration", "StringEquality"})
    protected void onPlayPressed(ActionEvent e)
    {
        String status = this.getClipStatus();
        if (status == null)
            return;

        if (status == AVKey.PLAY)
        {
            this.pauseClip();
        }
        else if (status == AVKey.STOP || status == AVKey.PAUSE)
        {
            this.startClip();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onStopPressed(ActionEvent e)
    {
        this.stopClip();
    }

    //**************************************************************//
    //********************  Line Listener  *************************//
    //**************************************************************//

    public void update(LineEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;
        
        if (e.getType() == LineEvent.Type.START)
        {
            this.onClipStart(e);
        }
        else if (e.getType() == LineEvent.Type.STOP)
        {
            this.onClipStop(e);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onClipStart(LineEvent e)
    {
        AudioPlayerAnnotation audioAnnotation = (AudioPlayerAnnotation) this.getAnnotation();
        audioAnnotation.setPlayButtonState(AVKey.PAUSE);
        this.startPlayerUpdate();
        this.updateAudioAnnotation();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onClipStop(LineEvent e)
    {
        AudioPlayerAnnotation audioAnnotation = (AudioPlayerAnnotation) this.getAnnotation();
        audioAnnotation.setPlayButtonState(AVKey.PLAY);
        this.stopPlayerUpdate();
        this.updateAudioAnnotation();
    }

    //**************************************************************//
    //********************  Player Update Timer  *******************//
    //**************************************************************//

    protected void onPlayerUpdate()
    {
        this.updateAudioAnnotation();
    }

    protected void startPlayerUpdate()
    {
        this.playerUpdateTimer = new javax.swing.Timer((int) PLAYER_UPDATE_DELAY_MILLIS,
            new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent actionEvent)
                {
                    onPlayerUpdate();
                }
            });
        this.playerUpdateTimer.start();
    }

    protected void stopPlayerUpdate()
    {
        if (this.playerUpdateTimer != null)
        {
            this.playerUpdateTimer.stop();
        }

        this.playerUpdateTimer = null;
    }
}
