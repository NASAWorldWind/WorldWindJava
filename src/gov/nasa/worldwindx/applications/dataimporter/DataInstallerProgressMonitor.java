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
