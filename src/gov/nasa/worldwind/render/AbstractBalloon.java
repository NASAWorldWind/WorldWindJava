/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.util.*;

/**
 * Abstract implementation of {@link Balloon}.
 *
 * @author pabercrombie
 * @version $Id: AbstractBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractBalloon extends WWObjectImpl implements Balloon
{
    protected boolean alwaysOnTop = false;
    protected boolean pickEnabled = true;
    protected Object delegateOwner;
    protected BalloonAttributes attributes;
    protected BalloonAttributes highlightAttributes;
    protected BalloonAttributes activeAttributes = new BasicBalloonAttributes(); // re-determined each frame

    protected String text;
    protected TextDecoder textDecoder = new BasicTextDecoder();

    protected boolean visible = true;
    protected boolean highlighted;

    protected double minActiveAltitude = -Double.MAX_VALUE;
    protected double maxActiveAltitude = Double.MAX_VALUE;

    /** The attributes used if attributes are not specified. */
    protected static final BalloonAttributes defaultAttributes;

    static
    {
        defaultAttributes = new BasicBalloonAttributes();
    }

    /** Create a balloon. */
    protected AbstractBalloon()
    {
    }

    /**
     * Create a balloon with text.
     *
     * @param text The balloon text.
     */
    protected AbstractBalloon(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
    }

    /** {@inheritDoc} */
    public boolean isAlwaysOnTop()
    {
        return this.alwaysOnTop;
    }

    /** {@inheritDoc} */
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.alwaysOnTop = alwaysOnTop;
    }

    /** {@inheritDoc} */
    public boolean isPickEnabled()
    {
        return this.pickEnabled;
    }

    /** {@inheritDoc} */
    public void setPickEnabled(boolean enable)
    {
        this.pickEnabled = enable;
    }

    /** {@inheritDoc} */
    public String getText()
    {
        return this.text;
    }

    /**
     * Get text after it has been processed by the text decoder. Returns the original text if there is no {@link
     * TextDecoder}.
     *
     * @return Decoded text.
     *
     * @see TextDecoder
     * @see #setTextDecoder(gov.nasa.worldwind.util.TextDecoder)
     * @see #getTextDecoder()
     */
    protected String getDecodedText()
    {
        return this.getTextDecoder().getDecodedText();
    }

    /** {@inheritDoc} */
    public void setText(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.text = text;
        this.getTextDecoder().setText(text);
    }

    /** {@inheritDoc} */
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    /** {@inheritDoc} */
    public void setDelegateOwner(Object delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }

    /** {@inheritDoc} */
    public BalloonAttributes getAttributes()
    {
        return this.attributes;
    }

    /** {@inheritDoc} */
    public void setAttributes(BalloonAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.BalloonAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes = attributes;
    }

    /** {@inheritDoc} */
    public BalloonAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /** {@inheritDoc} */
    public void setHighlightAttributes(BalloonAttributes highlightAttributes)
    {
        if (highlightAttributes == null)
        {
            String message = Logging.getMessage("nullValue.BalloonAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.highlightAttributes = highlightAttributes;
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                this.activeAttributes.copy(this.getHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we will use the normal attributes.
                if (this.getAttributes() != null)
                    this.activeAttributes.copy(this.getAttributes());
                else
                    this.activeAttributes.copy(defaultAttributes);
            }
        }
        else if (this.getAttributes() != null)
        {
            this.activeAttributes.copy(this.getAttributes());
        }
        else
        {
            this.activeAttributes.copy(defaultAttributes);
        }
    }

    /**
     * Get the active attributes, based on the highlight state.
     *
     * @return Highlight attributes if the balloon is highlighted, or normal attributes otherwise.
     */
    protected BalloonAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /** {@inheritDoc} */
    public TextDecoder getTextDecoder()
    {
        return this.textDecoder;
    }

    /** {@inheritDoc} */
    public void setTextDecoder(TextDecoder decoder)
    {
        if (decoder == null)
        {
            String message = Logging.getMessage("nullValue.TextDecoderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.textDecoder = decoder;
        this.textDecoder.setText(this.getText());
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return visible;
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /** {@inheritDoc} */
    public double getMinActiveAltitude()
    {
        return this.minActiveAltitude;
    }

    /** {@inheritDoc} */
    public void setMinActiveAltitude(double minActiveAltitude)
    {
        this.minActiveAltitude = minActiveAltitude;
    }

    /** {@inheritDoc} */
    public double getMaxActiveAltitude()
    {
        return this.maxActiveAltitude;
    }

    /** {@inheritDoc} */
    public void setMaxActiveAltitude(double maxActiveAltitude)
    {
        this.maxActiveAltitude = maxActiveAltitude;
    }
}
