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
    String APPLICATION_DISPLAY_NAME = "gov.nasa.worldwindx.applications.worldwindow.ApplicationDisplayName";

    // Services
    String IMAGE_SERVICE = "gov.nasa.worldwindx.applications.worldwindow.ImageService";

    // Core object IDs
    String APP_PANEL = "gov.nasa.worldwindx.applications.worldwindow.AppPanel";
    String APP_FRAME = "gov.nasa.worldwindx.applications.worldwindow.AppFrame";
    String CONTROLS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.ControlsPanel";
    String MENU_BAR = "gov.nasa.worldwindx.applications.worldwindow.MenuBar";
    String NETWORK_STATUS_SIGNAL = "gov.nasa.worldwindx.applications.worldwindow.NetworkStatusSignal";
    String TOOL_BAR = "gov.nasa.worldwindx.applications.worldwindow.ToolBar";
    String STATUS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.StatusPanel";
    String WW_PANEL = "gov.nasa.worldwindx.applications.worldwindow.WWPanel";

    // Miscellaneous
    String ACCELERATOR_SUFFIX = ".Accelerator";
    String ACTION_COMMAND = "gov.nasa.worldwindx.applications.worldwindow.ActionCommand";
    String CONTEXT_MENU_INFO = "gov.nasa.worldwindx.applications.worldwindow.ContextMenuString";
    String FILE_MENU = "gov.nasa.worldwindx.applications.worldwindow.feature.FileMenu";
    String INFO_PANEL_TEXT = "gov.nasa.worldwindx.applications.worldwindow.InfoPanelText";
    String ON_STATE = "gov.nasa.worldwindx.applications.worldwindow.OnState";
    String RADIO_GROUP = "gov.nasa.worldwindx.applications.worldwindow.StatusBarMessage";
    String STATUS_BAR_MESSAGE = "gov.nasa.worldwindx.applications.worldwindow.StatusBarMessage";

    // Layer types
    String INTERNAL_LAYER = "gov.nasa.worldwindx.applications.worldwindow.InternalLayer"; // application controls, etc.
    String ACTIVE_LAYER = "gov.nasa.worldwindx.applications.worldwindow.ActiveLayer"; // force display in active layers
    String USER_LAYER = "gov.nasa.worldwindx.applications.worldwindow.UserLayer"; // User-generated layers
    String SCREEN_LAYER = "gov.nasa.worldwindx.applications.worldwindow.ScreenLayer";
        // in-screen application controls, etc.

    // Feature IDs
    String FEATURE = "gov.nasa.worldwindx.applications.worldwindow.feature";
    String FEATURE_ID = "gov.nasa.worldwindx.applications.worldwindow.FeatureID";
    String FEATURE_ACTIVE_LAYERS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.ActiveLayersPanel";
    String FEATURE_COMPASS = "gov.nasa.worldwindx.applications.worldwindow.feature.Compass";
    String FEATURE_CROSSHAIR = "gov.nasa.worldwindx.applications.worldwindow.feature.Crosshair";
    String FEATURE_COORDINATES_DISPLAY = "gov.nasa.worldwindx.applications.worldwindow.feature.CoordinatesDisplay";
    String FEATURE_EXTERNAL_LINK_CONTROLLER
        = "gov.nasa.worldwindx.applications.worldwindow.feature.ExternalLinkController";
    String FEATURE_GAZETTEER = "gov.nasa.worldwindx.applications.worldwindow.feature.Gazetteer";
    String FEATURE_GAZETTEER_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.GazetteerPanel";
    String FEATURE_GRATICULE = "gov.nasa.worldwindx.applications.worldwindow.feature.Graticule";
    String FEATURE_ICON_CONTROLLER = "gov.nasa.worldwindx.applications.worldwindow.feature.IconController";
    String FEATURE_IMPORT_IMAGERY = "gov.nasa.worldwindx.applications.worldwindow.feature.ImportImagery";
    String FEATURE_INFO_PANEL_CONTROLLER = "gov.nasa.worldwindx.applications.worldwindow.feature.InfoPanelController";
    String FEATURE_LAYER_MANAGER_DIALOG = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManagerDialog";
    String FEATURE_LAYER_MANAGER = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManager";
    String FEATURE_LAYER_MANAGER_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.LayerManagerPanel";
    String FEATURE_LATLON_GRATICULE = "gov.nasa.worldwindx.applications.worldwindow.feature.LatLonGraticule";
    String FEATURE_MEASUREMENT = "gov.nasa.worldwindx.applications.worldwindow.feature.Measurement";
    String FEATURE_MEASUREMENT_DIALOG = "gov.nasa.worldwindx.applications.worldwindow.feature.MeasurementDialog";
    String FEATURE_MEASUREMENT_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.MeasurementPanel";
    String FEATURE_NAVIGATION = "gov.nasa.worldwindx.applications.worldwindow.feature.Navigation";
    String FEATURE_OPEN_FILE = "gov.nasa.worldwindx.applications.worldwindow.feature.OpenFile";
    String FEATURE_OPEN_URL = "gov.nasa.worldwindx.applications.worldwindow.feature.OpenURL";
    String FEATURE_SCALE_BAR = "gov.nasa.worldwindx.applications.worldwindow.feature.ScaleBar";
    String FEATURE_TOOLTIP_CONTROLLER = "gov.nasa.worldwindx.applications.worldwindow.feature.ToolTipController";
    String FEATURE_UTM_GRATICULE = "gov.nasa.worldwindx.applications.worldwindow.feature.UTMGraticule";
    String FEATURE_WMS_PANEL = "gov.nasa.worldwindx.applications.worldwindow.feature.WMSPanel";
    String FEATURE_WMS_DIALOG = "gov.nasa.worldwindx.applications.worldwindow.feature.WMSDialog";

    // Specific properties
    String FEATURE_OWNER_PROPERTY = "gov.nasa.worldwindx.applications.worldwindow.FeatureOwnerProperty";
    String TOOL_BAR_ICON_SIZE_PROPERTY = "gov.nasa.worldwindx.applications.worldwindow.ToolBarIconSizeProperty";
}
