/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author tag
 * @version $Id: ScreenCreditImage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenCreditImage extends ScreenImage implements ScreenCredit
{
    private String name;
    private String link;
    private Rectangle viewport;

    public ScreenCreditImage(String name, Object imageSource)
    {
        if (imageSource == null)
        {
            String msg = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.name = name;
        this.setImageSource(imageSource);
    }

    public void setViewport(Rectangle viewport)
    {
        if (viewport == null)
        {
            String msg = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.viewport = viewport;
        this.setScreenLocation(new Point(viewport.x, viewport.y));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Rectangle getViewport()
    {
        return this.viewport;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getLink()
    {
        return this.link;
    }

    @Override
    public int getImageWidth(DrawContext dc)
    {
        return (int) this.getViewport().getWidth();
    }

    @Override
    public int getImageHeight(DrawContext dc)
    {
        return (int) this.getViewport().getHeight();
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ScreenCreditImage that = (ScreenCreditImage) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}
