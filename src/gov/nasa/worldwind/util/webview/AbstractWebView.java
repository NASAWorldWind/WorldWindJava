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
package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

/**
 * Abstract base class for {@link WebView} implementations.
 *
 * @author pabercrombie
 * @version $Id: AbstractWebView.java 1171 2013-02-11 21:45:02Z dcollins $
 * @deprecated 
 */
@Deprecated
public abstract class AbstractWebView extends WWObjectImpl implements WebView, Disposable
{
    /** The size of the WebView frame in pixels. Initially null, indicating the default size is used. */
    protected Dimension frameSize;
    /** The WebView's current texture representation. Lazily created in {@link #getTextureRepresentation}. */
    protected WWTexture textureRep;
    /** Indicates whether the WebView is active. */
    protected boolean active;

    /** {@inheritDoc} */
    public Dimension getFrameSize()
    {
        return this.frameSize;
    }

    /** {@inheritDoc} */
    public void setFrameSize(Dimension size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Setting the frame size requires a call into native code, and requires us to regenerate the texture. Only
        // do this if the size has actually changed.
        if (this.frameSize.equals(size))
            return;

        this.frameSize = size;
        this.textureRep = null; // The texture needs to be regenerated because the frame size changed.

        this.doSetFrameSize(this.frameSize);
    }

    protected abstract void doSetFrameSize(Dimension size);

    /** {@inheritDoc} */
    public WWTexture getTextureRepresentation(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.textureRep == null)
            this.textureRep = this.createTextureRepresentation(dc);

        return this.textureRep;
    }

    /** {@inheritDoc} */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /** {@inheritDoc} */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Create a texture representation of the WebView.
     *
     * @param dc draw context.
     *
     * @return A texture representation of the WebView contents.
     */
    protected abstract WWTexture createTextureRepresentation(DrawContext dc);

    @Override
    public void propertyChange(final PropertyChangeEvent event)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    propertyChange(event);
                }
            });
        }
        else
        {
            this.firePropertyChange(AVKey.REPAINT, null, this);
        }
    }
}
