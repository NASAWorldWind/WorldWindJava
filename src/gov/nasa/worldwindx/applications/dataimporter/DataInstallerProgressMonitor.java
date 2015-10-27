/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.data.DataStoreProducer;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Displays the progress of data set installation.
 *
 * @author tag
 * @version $Id: DataInstallerProgressMonitor.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class DataInstallerProgressMonitor extends ProgressMonitor implements PropertyChangeListener
{
    protected DataStoreProducer producer;
    protected AtomicInteger progress;
    java.util.Timer progressTimer = new java.util.Timer();

    public DataInstallerProgressMonitor(Component parent, DataStoreProducer producer)
    {
        super(parent, "Importing ....", null, 0, 100);

        this.producer = producer;

        this.progress = new AtomicInteger(0);
        this.progressTimer = new java.util.Timer();

        // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
        // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
        PropertyChangeListener progressListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (DataInstallerProgressMonitor.this.isCanceled())
                    return;

                if (evt.getPropertyName().equals(AVKey.PROGRESS))
                    DataInstallerProgressMonitor.this.progress.set((int) (100 * (Double) evt.getNewValue()));
            }
        };

        producer.addPropertyChangeListener(progressListener);
        this.setProgress(0);
    }

    public void start()
    {
        // Configure a timer to check if the user has clicked the ProgressMonitor's "Cancel" button. If so, stop
        // production as soon as possible. This just stops the production from completing; it doesn't clean up any
        // state
        // changes made during production,
        java.util.Timer progressTimer = new java.util.Timer();
        progressTimer.schedule(new TimerTask()
        {
            public void run()
            {
                setProgress(progress.get());

                if (isCanceled())
                {
                    if (producer != null)
                        producer.stopProduction();
                    this.cancel();
                }
            }
        }, this.getMillisToDecideToPopup(), 100L);
    }

    public void stop()
    {
        if (this.producer != null)
        {
            this.producer.removePropertyChangeListener(this);
            this.producer.removeAllDataSources();
        }

        this.close();
        this.progressTimer.cancel();
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        if (DataInstallerProgressMonitor.this.isCanceled())
            return;

        if (event.getPropertyName().equals(AVKey.PROGRESS))
            DataInstallerProgressMonitor.this.progress.set((int) (100 * (Double) event.getNewValue()));
    }
}
