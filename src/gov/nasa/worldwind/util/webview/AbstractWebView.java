/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
 */
public abstract class AbstractWebView extends WWObjectImpl implements WebView, Disposable
{
    /** The size of the WebView frame in pixels. Initially null, indicating the default size is used. */
    protected Dimension frameSize;
    /** The WebView's current texture representation. Lazily created in {@link #getTextureRepresentation}. */
    protected WWTexture textureRep;
    /** Indicates whether the WebView is active. */
    protected boolean active;

    /**
     * Overridden to ensure that the WebView's native resources are disposed when the WebView is reclaimed by the
     * garbage collector. This does nothing if the WebView's owner has already called {@link #dispose()}.
     */
    @Override
    protected void finalize() throws Throwable
    {
        this.dispose();
        super.finalize();
    }

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
