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
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;

        if (wwd.getSceneController().getScreenCreditController() != null)
            wwd.getSceneController().getScreenCreditController().dispose();

        wwd.getSceneController().setScreenCreditController(this);
        wwd.addSelectListener(this);
    }

    @Override
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

    public void pick(DrawContext dc, @SuppressWarnings("unused") Point pickPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
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

            x += (separation + creditWidth);
        }
    }

    @Override
	public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getScreenCredits() == null || dc.getScreenCredits().size() < 1)
            return;

        if (!this.isEnabled())
            return;

        Set<Map.Entry<ScreenCredit, Long>> credits = dc.getScreenCredits().entrySet();

        int y = bottomMargin + creditHeight / 2;
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

    @Override
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
                    String msg = Logging.getMessage("generic.URIInvalid",
                        credit.getLink() != null ? credit.getLink() : "null");
                    Logging.logger().warning(msg);
                    badURLsReported.add(credit.getLink());
                }
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower for URL",
                    credit.getLink());
                Logging.logger().warning(msg);
            }
        }
    }
}
