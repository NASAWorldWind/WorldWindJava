/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;

import java.awt.event.*;
import java.beans.*;

/**
 * @author tag
 * @version $Id: Graticule.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Graticule extends AbstractFeature
{
    private static final String FEATURE_TITLE = "Graticule";
    private static final String ICON_PATH = "gov/nasa/worldwindx/applications/worldwindow/images/lat-long-64x64.png";

    public Graticule(Registry registry)
    {
        super(FEATURE_TITLE, Constants.FEATURE_GRATICULE, ICON_PATH, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();

        for (Feature r : getGraticules())
        {
            if (r != null)
                r.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent event)
                    {
                        if (event.getPropertyName().equals(Constants.ON_STATE))
                        {
                            if ((Boolean) event.getNewValue())
                            {
                                lastOneOn = ((Feature) event.getSource());

                                Feature[] rs = getGraticules();
                                for (Feature r : rs)
                                {
                                    if (r != null && r != lastOneOn)
                                        r.turnOn(false);
                                }
                            }
                        }
                    }
                });
        }
    }

    protected Feature[] getGraticules()
    {
        Feature r1 = (Feature) this.controller.getRegisteredObject(Constants.FEATURE_LATLON_GRATICULE);
        Feature r2 = (Feature) this.controller.getRegisteredObject(Constants.FEATURE_UTM_GRATICULE);

        return new Feature[] {r1, r2};
    }

    public boolean isOn()
    {
        if (!this.isEnabled())
            return false;

        Feature[] rs = getGraticules();
        for (Feature r : rs)
        {
            if (r != null && r.isOn())
                return true;
        }

        return false;
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    @Override
    public void turnOn(boolean tf)
    {
        for (Feature r : getGraticules())
        {
            if (r != null)
                r.turnOn(tf);
        }
    }

    @Override
    public void setEnabled(boolean tf)
    {
        super.setEnabled(tf);

        for (Feature r : getGraticules())
        {
            if (r != null)
                r.setEnabled(tf);
        }
    }

    private Feature lastOneOn;

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        if (isOn())
            turnOn(false);
        else if (lastOneOn != null)
            lastOneOn.turnOn(true);
        else
        {
            Feature r = (Feature) this.controller.getRegisteredObject(Constants.FEATURE_LATLON_GRATICULE);
            if (r != null)
                r.turnOn(true);
        }

        controller.redraw();
    }
}
