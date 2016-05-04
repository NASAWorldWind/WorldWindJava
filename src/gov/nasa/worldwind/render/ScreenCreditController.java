/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: ScreenCreditController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenCreditController implements Renderable, SelectListener, Disposable
{
    private int creditWidth = 32;
    private int creditHeight = 32;
    private int leftMargin = 240;
    private int bottomMargin = 10;
    private int separation = 10;
    private double baseOpacity = 0.5;
    private double highlightOpacity = 1;
    private WorldWindow wwd;
    private boolean enabled = true;

    public ScreenCreditController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            throw new IllegalArgumentException();
        }

        this.wwd = wwd;

        if (wwd.getSceneController().getScreenCreditController() != null)
            wwd.getSceneController().getScreenCreditController().dispose();

        wwd.getSceneController().setScreenCreditController(this);
        wwd.addSelectListener(this);
    }

    public void dispose()
    {
        wwd.removeSelectListener(this);
        if (wwd.getSceneController() == this)
            wwd.getSceneController().setScreenCreditController(null);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void pick(DrawContext dc, Point pickPoint)
    {
        if (dc == null)
        {
            throw new IllegalArgumentException();
        }

        if (!this.isEnabled())
            return;

        if (dc.getScreenCredits() == null || dc.getScreenCredits().size() < 1)
            return;

        Set<Map.Entry<ScreenCredit, Long>> credits = dc.getScreenCredits().entrySet();

        int y = dc.getView().getViewport().height - (bottomMargin + creditHeight / 2);
        int x = leftMargin + creditWidth / 2;

        for (Map.Entry<ScreenCredit, Long> entry : credits)
        {
            ScreenCredit credit = entry.getKey();
            Rectangle viewport = new Rectangle(x, y, creditWidth, creditHeight);

            credit.setViewport(viewport);
            credit.pick(dc, pickPoint);

            x += (separation + creditWidth);
        }
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            throw new IllegalArgumentException();
        }

        if (dc.getScreenCredits() == null || dc.getScreenCredits().size() < 1)
            return;

        if (!this.isEnabled())
            return;

        Set<Map.Entry<ScreenCredit, Long>> credits = dc.getScreenCredits().entrySet();

        int y = dc.getView().getViewport().height - (bottomMargin + creditHeight / 2);
        int x = leftMargin + creditWidth / 2;

        for (Map.Entry<ScreenCredit, Long> entry : credits)
        {
            ScreenCredit credit = entry.getKey();
            Rectangle viewport = new Rectangle(x, y, creditWidth, creditHeight);

            credit.setViewport(viewport);
            if (entry.getValue() == dc.getFrameTimeStamp())
            {
                Object po = dc.getPickedObjects().getTopObject();
                credit.setOpacity(po != null && po instanceof ScreenCredit ? this.highlightOpacity : this.baseOpacity);
                credit.render(dc);
            }

            x += (separation + creditWidth);
        }
    }

    public void selected(SelectEvent event)
    {
        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
            return;

        Object po = event.getTopObject();

        if (po != null && po instanceof ScreenCredit)
        {
            if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK))
            {
                openBrowser((ScreenCredit) po);
            }
        }
    }

    private Set<String> badURLsReported = new HashSet<String>();

    protected void openBrowser(ScreenCredit credit)
    {
        if (credit.getLink() != null && credit.getLink().length() > 0)
        {
            try
            {
                BrowserOpener.browse(new URL(credit.getLink()));
            }
            catch (MalformedURLException e)
            {
                if (!badURLsReported.contains(credit.getLink())) // report it only once
                {
                            badURLsReported.add(credit.getLink());
                }
            }
            catch (Exception e)
            {
                }
        }
    }
}
