/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

/**
 * @author tag
 * @version $Id: Constants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Constants
{
    // Names and titles
    static final String APPLICATION_DISPLAY_NAME
        = "gov.nasa.worldwindx.applications.worldwindow.ApplicationDisplayName";

    // Services
    public static final String IMAGE_SERVICE = "gov.nasa.worldwindx.applications.worldwindow.ImageService";

    // Core object IDs
    static final String APP_PANEL = "gov.nasa.worldwindx.applications.worldwindow.AppPanel";
    static final String APP_FRAME = "gov.nasa.worldwindx.applications.worldwindow.AppFrame";
    static final String APPLET_PANEL = "gov.nasa.worldwindx.applications.worldwindow.AppletPanel";
    static final String CONTROLS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.ControlsPanel";
    static final String MENU_BAR = "gov.nasa.worldwindx.applications.worldwindow.MenuBar";
    static final String NETWORK_STATUS_SIGNAL = "gov.nasa.worldwindx.applications.worldwindow.NetworkStatusSignal";
    static final String TOOL_BAR = "gov.nasa.worldwindx.applications.worldwindow.ToolBar";
    static final String STATUS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.StatusPanel";
    static final String WW_PANEL = "gov.nasa.worldwindx.applications.worldwindow.WWPanel";

    // Miscellaneous
    static final String ACCELERATOR_SUFFIX = ".Accelerator";
    static final String ACTION_COMMAND = "gov.nasa.worldwindx.applications.worldwindow.ActionCommand";
    static final String CONTEXT_MENU_INFO = "gov.nasa.worldwindx.applications.worldwindow.ContextMenuString";
    static final String FILE_MENU = "gov.nasa.worldwindx.applications.worldwindow.feature.FileMenu";
    static final String INFO_PANEL_TEXT = "gov.nasa.worldwindx.applications.worldwindow.InfoPanelText";
    static final String ON_STATE = "gov.nasa.worldwindx.applications.worldwindow.OnState";
    static final String RADIO_GROUP = "gov.nasa.worldwindx.applications.worldwindow.StatusBarMessage";
    static final String STATUS_BAR_MESSAGE = "gov.nasa.worldwindx.applications.worldwindow.StatusBarMessage";

    // Layer types
    static final String INTERNAL_LAYER = "gov.nasa.worldwindx.applications.worldwindow.InternalLayer";
        // application controls, etc.
    static final String ACTIVE_LAYER = "gov.nasa.worldwindx.applications.worldwindow.ActiveLayer";
        // force display in active layers
    static final String USER_LAYER = "gov.nasa.worldwindx.applications.worldwindow.UserLayer"; // User-generated layers
    static final String SCREEN_LAYER = "gov.nasa.worldwindx.applications.worldwindow.ScreenLayer";
    // in-screen application controls, etc.

    // Feature IDs
    static final String FEATURE = "gov.nasa.worldwindx.applications.worldwindow.feature";
    static final String FEATURE_ID = "gov.nasa.worldwindx.applications.worldwindow.FeatureID";
    static final String FEATURE_ACTIVE_LAYERS_PANEL
        = "gov.nasa.worldwindx.applications.worldwindow.feature.ActiveLayersPanel";
    static final String FEATURE_COMPASS = "gov.nasa.worldwindx.applications.worldwindow.feature.Compass";
    static final String FEATURE_CROSSHAIR = "gov.nasa.worldwindx.applications.worldwindow.feature.Crosshair";
    static final String FEATURE_COORDINATES_DISPLAY
        = "gov.nasa.worldwindx.applications.worldwindow.feature.CoordinatesDisplay";
    static final String FEATURE_EXTERNAL_LINK_CONTROLLER
        = "gov.nasa.worldwindx.applications.worldwindow.feature.ExternalLinkController";
    static final String FEATURE_GAZETTEER = "gov.nasa.worldwindx.applications.worldwindow.feature.Gazetteer";
    static final String FEATURE_GAZETTEER_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.GazetteerPanel";
    static final String FEATURE_GRATICULE = "gov.nasa.worldwindx.applications.worldwindow.feature.Graticule";
    static final String FEATURE_ICON_CONTROLLER = "gov.nasa.worldwindx.applications.worldwindow.feature.IconController";
    static final String FEATURE_IMPORT_IMAGERY = "gov.nasa.worldwindx.applications.worldwindow.feature.ImportImagery";
    static final String FEATURE_INFO_PANEL_CONTROLLER
        = "gov.nasa.worldwindx.applications.worldwindow.feature.InfoPanelController";
    static final String FEATURE_LAYER_MANAGER_DIALOG
        = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManagerDialog";
    static final String FEATURE_LAYER_MANAGER = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManager";
    static final String FEATURE_LAYER_MANAGER_PANEL
        = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManagerPanel";
    static final String FEATURE_LATLON_GRATICULE
        = "gov.nasa.worldwindx.applications.worldwindow.feature.LatLonGraticule";
    static final String FEATURE_MEASUREMENT = "gov.nasa.worldwindx.applications.worldwindow.feature.Measurement";
    static final String FEATURE_MEASUREMENT_DIALOG
        = "gov.nasa.worldwindx.applications.worldwindow.feature.MeasurementDialog";
    static final String FEATURE_MEASUREMENT_PANEL
        = "gov.nasa.worldwindx.applications.worldwindow.feature.MeasurementPanel";
    static final String FEATURE_NAVIGATION = "gov.nasa.worldwindx.applications.worldwindow.feature.Navigation";
    static final String FEATURE_OPEN_FILE = "gov.nasa.worldwindx.applications.worldwindow.feature.OpenFile";
    static final String FEATURE_OPEN_URL = "gov.nasa.worldwindx.applications.worldwindow.feature.OpenURL";
    static final String FEATURE_SCALE_BAR = "gov.nasa.worldwindx.applications.worldwindow.feature.ScaleBar";
    static final String FEATURE_TOOLTIP_CONTROLLER
        = "gov.nasa.worldwindx.applications.worldwindow.feature.ToolTipController";
    static final String FEATURE_UTM_GRATICULE = "gov.nasa.worldwindx.applications.worldwindow.feature.UTMGraticule";
    static final String FEATURE_WMS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.WMSPanel";
    static final String FEATURE_WMS_DIALOG = "gov.nasa.worldwindx.applications.worldwindow.feature.WMSDialog";

    // Specific properties
    static final String FEATURE_OWNER_PROPERTY = "gov.nasa.worldwindx.applications.worldwindow.FeatureOwnerProperty";
    static final String TOOL_BAR_ICON_SIZE_PROPERTY
        = "gov.nasa.worldwindx.applications.worldwindow.ToolBarIconSizeProperty";
}
