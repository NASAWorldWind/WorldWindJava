/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import java.beans.PropertyChangeEvent;

/**
 * @author tag
 * @version $Id: Navigation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Navigation extends AbstractFeatureLayer
{
    public static final String POSITION_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.PostionProperty";
    public static final String ORIENTATION_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.OrientationProperty";
    public static final String SIZE_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.SizeProperty";
    public static final String OPACITY_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.OpacityProperty";

    public static final String PAN_CONTROLS_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.PanControlS";
    public static final String ZOOM_CONTROLS_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.ZoomControlS";
    public static final String TILT_CONTROLS_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.TiltControlS";
    public static final String HEADING_CONTROLS_PROPERTY = "gov.nasa.worldwindowx.applications.features.Navegacion.HeadingControlS";

    public Navigation()
    {
        this(null);
    }

    public Navigation(Registry registry)
    {
        super("Navigation", Constants.FEATURE_NAVIGATION,
            "gov/nasa/worldwindx/applications/worldwindow/images/navegacion-64x64.png", true, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);
    }

    protected Layer doAddLayer()
    {
        ViewControlsLayer layer = new ViewControlsLayer();

        layer.setValue(Constants.SCREEN_LAYER, true);
        layer.setValue(Constants.INTERNAL_LAYER, true);
        layer.setLayout(AVKey.VERTICAL);

        controller.addInternalLayer(layer);

        ViewControlsSelectListener listener = new ViewControlsSelectListener(this.controller.getWWd(), layer);
        listener.setRepeatTimerDelay(30);
        listener.setZoomIncrement(0.5);
        listener.setPanIncrement(0.5);
        this.controller.getWWd().addSelectListener(listener);

        return layer;
    }

    private ViewControlsLayer getLayer()
    {
        return (ViewControlsLayer) this.layer;
    }

    @Override
    public void doPropertyChange(PropertyChangeEvent event)
    {
        if (event.getPropertyName().equals(POSITION_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof String)
            {
                this.getLayer().setPosition((String) event.getNewValue());
                this.controller.redraw();
            }
        }
        else if (event.getPropertyName().equals(ORIENTATION_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof String)
            {
                this.getLayer().setLayout((String) event.getNewValue());
                this.controller.redraw();
            }
        }
        else if (event.getPropertyName().equals(PAN_CONTROLS_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof Boolean)
            {
                this.getLayer().setShowPanControls((Boolean) event.getNewValue());
                this.controller.redraw();
            }
        }
        else if (event.getPropertyName().equals(ZOOM_CONTROLS_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof Boolean)
            {
                this.getLayer().setShowZoomControls((Boolean) event.getNewValue());
                this.controller.redraw();
            }
        }
        else if (event.getPropertyName().equals(HEADING_CONTROLS_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof Boolean)
            {
                this.getLayer().setShowHeadingControls((Boolean) event.getNewValue());
                this.controller.redraw();
            }
        }
        else if (event.getPropertyName().equals(TILT_CONTROLS_PROPERTY))
        {
            if (event.getNewValue() != null && event.getNewValue() instanceof Boolean)
            {
                this.getLayer().setShowPitchControls((Boolean) event.getNewValue());
                this.controller.redraw();
            }
        }
    }

    public double getSize()
    {
        return this.layer.getScale();
    }

    public double getOpacity()
    {
        return this.layer.getOpacity();
    }

    public String getOrientation()
    {
        return this.getLayer().getLayout();
    }

    public String getPosition()
    {
        return this.getLayer().getPosition();
    }

    public boolean isShowPan()
    {
        return this.getLayer().isShowPanControls();
    }

    public boolean isShowZoom()
    {
        return this.getLayer().isShowZoomControls();
    }

    public boolean isShowTilt()
    {
        return this.getLayer().isShowPitchControls();
    }

    public boolean isShowHeading()
    {
        return this.getLayer().isShowHeadingControls();
    }
}
