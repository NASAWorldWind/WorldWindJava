/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.avlist;

/**
 * @author Tom Gaskins
 * @version $Id: AVKey.java 2375 2014-10-10 23:35:05Z tgaskins $
 */
public interface AVKey // TODO: Eliminate unused constants, if any
{
    // NOTE: Keep all keys in alphabetical order except where noted

    // Direction constants
    final String NORTHWEST = "gov.nasa.worldwind.layers.ViewControlsLayer.NorthWest";
    final String SOUTHWEST = "gov.nasa.worldwind.layers.ViewControlsLayer.SouthWest";
    final String NORTHEAST = "gov.nasa.worldwind.layers.ViewControlsLayer.NorthEast";
    final String SOUTHEAST = "gov.nasa.worldwind.layers.ViewControlsLayer.SouthEast";

    // Start alphabetic order
    final String ABOVE_GROUND_LEVEL = "gov.nasa.worldwind.avkey.AboveGroundLevel";
    final String ABOVE_GROUND_REFERENCE = "gov.nasa.worldwind.avkey.AboveGroundReference";
    final String ABOVE_MEAN_SEA_LEVEL = "gov.nasa.worldwind.avkey.AboveMeanSeaLevel";
    final String ACTION = "gov.nasa.worldwind.avkey.Action";
    final String AIRSPACE_GEOMETRY_CACHE_SIZE = "gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize";
    final String ALLOW = "gov.nasa.worldwind.avkey.Allow";
    final String AUTH_TOKEN = "gov.nasa.worldwind.avkey.AuthToken";

    final String AVAILABLE_IMAGE_FORMATS = "gov.nasa.worldwind.avkey.AvailableImageFormats";
    final String AVERAGE_TILE_SIZE = "gov.nasa.worldwind.avkey.AverageTileSize";

    final String BALLOON = "gov.nasa.worldwind.avkey.Balloon";
    final String BALLOON_TEXT = "gov.nasa.worldwind.avkey.BalloonText";
    final String BACK = "gov.nasa.worldwind.avkey.Back";
    final String BEGIN = "gov.nasa.worldwind.avkey.Begin";
    final String BIG_ENDIAN = "gov.nasa.worldwind.avkey.BigEndian";
    final String BOTTOM = "gov.nasa.worldwind.avkey.Bottom";
    final String BYTE_ORDER = "gov.nasa.worldwind.avkey.ByteOrder";
    final String BANDS_ORDER = "gov.nasa.worldwind.avkey.BandsOrder";

    final String BLACK_GAPS_DETECTION = "gov.nasa.worldwind.avkey.DetectBlackGaps";
    final String BOUNDS = "gov.nasa.worldwind.avkey.Bounds";

    final String CACHE_CONTENT_TYPES = "gov.nasa.worldwind.avkey.CacheContentTypes";
    final String CENTER = "gov.nasa.worldwind.avkey.Center";

    final String CLASS_LEVEL = "gov.nasa.worldwind.avkey.ClassLevel";
    final String CLASS_LEVEL_UNCLASSIFIED = "gov.nasa.worldwind.avkey.ClassLevel.Unclassified";
    final String CLASS_LEVEL_RESTRICTED = "gov.nasa.worldwind.avkey.ClassLevel.Restricted";
    final String CLASS_LEVEL_CONFIDENTIAL = "gov.nasa.worldwind.avkey.ClassLevel.Confidential";
    final String CLASS_LEVEL_SECRET = "gov.nasa.worldwind.avkey.ClassLevel.Secret";
    final String CLASS_LEVEL_TOPSECRET = "gov.nasa.worldwind.avkey.ClassLevel.TopSecret";

    final String CLOCKWISE = "gov.nasa.worldwind.avkey.ClockWise";
    final String CLOSE = "gov.nasa.worldwind.avkey.Close";
    final String COLOR = "gov.nasa.worldwind.avkey.Color";
    final String COMPRESS_TEXTURES = "gov.nasa.worldwind.avkey.CompressTextures";
    final String CONSTRUCTION_PARAMETERS = "gov.nasa.worldwind.avkey.ConstructionParameters";
    final String CONTEXT = "gov.nasa.worldwind.avkey.Context";
    final String COORDINATE_SYSTEM = "gov.nasa.worldwind.avkey.CoordinateSystem";
    final String COORDINATE_SYSTEM_GEOGRAPHIC = "gov.nasa.worldwind.avkey.CoordinateSystem.Geographic";
    final String COORDINATE_SYSTEM_NAME = "gov.nasa.worldwind.avkey.CoordinateSystem.Name";
    final String COORDINATE_SYSTEM_PROJECTED = "gov.nasa.worldwind.avkey.CoordinateSystem.Projected";
    final String COORDINATE_SYSTEM_SCREEN = "gov.nasa.worldwind.avkey.CoordinateSystem.Screen";
    final String COORDINATE_SYSTEM_UNKNOWN = "gov.nasa.worldwind.avkey.CoordinateSystem.Unknown";

    final String COUNTER_CLOCKWISE = "gov.nasa.worldwind.avkey.CounterClockWise";
    final String COVERAGE_IDENTIFIERS = "gov.nasa.worldwind.avkey.CoverageIdentifiers";

    final String DATA_CACHE_NAME = "gov.nasa.worldwind.avkey.DataCacheNameKey";
    final String DATA_FILE_STORE_CLASS_NAME = "gov.nasa.worldwind.avkey.DataFileStoreClassName";
    final String DATA_FILE_STORE_CONFIGURATION_FILE_NAME
        = "gov.nasa.worldwind.avkey.DataFileStoreConfigurationFileName";
    final String DATASET_NAME = "gov.nasa.worldwind.avkey.DatasetNameKey";
    final String DATA_RASTER_READER_FACTORY_CLASS_NAME = "gov.nasa.worldwind.avkey.DataRasterReaderFactoryClassName";
    final String DATASET_TYPE = "gov.nasa.worldwind.avkey.DatasetTypeKey";
    final String DATE_TIME = "gov.nasa.worldwind.avkey.DateTime";
    /**
     * Indicates the primitive data type of a dataset or a buffer of data. When used as a key, the corresponding value
     * may be one of the following: <code>INT8</code>, <code>INT16</code>, <code>INT32</code>, <code>INT64</code>,
     * <code>FLOAT32</code>, or <code>FLOAT64</code>.
     */
    final String DATA_TYPE = "gov.nasa.worldwind.avkey.DataType";
    final String DELETE_CACHE_ON_EXIT = "gov.nasa.worldwind.avkey.DeleteCacheOnExit";
    /**
     * Indicates the World Wind scene's worst-case depth resolution, in meters. This is typically interpreted by the
     * View as the desired resolution at the scene's maximum drawing distance. In this case, the resolution closer to
     * the viewer's eye point is significantly better then the worst-case resolution. Decreasing this value enables the
     * viewer to get closer to 3D shapes positioned above the terrain at the coast of potential rendering artifacts
     * between shapes that are places closely together or close to the terrain.
     */
    final String DEPTH_RESOLUTION = "gov.nasa.worldwind.avkey.DepthResolution";
    final String DESCRIPTION = "gov.nasa.worldwind.avkey.Description";
    final String DETAIL_HINT = "gov.nasa.worldwind.avkey.DetailHint";
    final String DISPLAY_ICON = "gov.nasa.worldwind.avkey.DisplayIcon";
    final String DISPLAY_NAME = "gov.nasa.worldwind.avkey.DisplayName";
    final String DOCUMENT = "gov.nasa.worldwind.avkey.Document";
    /**
     * Indicates the state of dragging. Provided by the {@link gov.nasa.worldwind.drag.DragContext} object to objects
     * implementing {@link gov.nasa.worldwind.drag.Draggable}.
     */
    final String DRAG_BEGIN = "gov.nasa.worldwind.avkey.DragBegin";
    final String DRAG_CHANGE = "gov.nasa.worldwind.avkey.DragChange";
    final String DRAG_ENDED = "gov.nasa.worldwind.avkey.DragEnded";

    final String DTED_LEVEL = "gov.nasa.worldwind.avkey.DTED.Level";

    final String EARTH_ELEVATION_MODEL_CAPABILITIES = "gov.nasa.worldwind.avkey.EarthElevationModelCapabilities";
    final String EARTH_ELEVATION_MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.EarthElevationModelClassName";
    final String EARTH_ELEVATION_MODEL_CONFIG_FILE = "gov.nasa.worldwind.avkey.EarthElevationModelConfigFile";
    final String EAST = "gov.nasa.worldwind.avkey.East";

    final String ELEVATION = "gov.nasa.worldwind.avkey.Elevation";
    final String ELEVATION_EXTREMES_FILE = "gov.nasa.worldwind.avkey.ElevationExtremesFileKey";
    final String ELEVATION_EXTREMES_LOOKUP_CACHE_SIZE = "gov.nasa.worldwind.avkey.ElevationExtremesLookupCacheSize";
    final String ELEVATION_MIN = "gov.nasa.worldwind.avkey.ElevationMinKey";
    final String ELEVATION_MAX = "gov.nasa.worldwind.avkey.ElevationMaxKey";
    final String ELEVATION_MODEL = "gov.nasa.worldwind.avkey.ElevationModel";
    final String ELEVATION_MODEL_FACTORY = "gov.nasa.worldwind.avkey.ElevationModelFactory";
    final String ELEVATION_TILE_CACHE_SIZE = "gov.nasa.worldwind.avkey.ElevationTileCacheSize";
    final String ELEVATION_UNIT = "gov.nasa.worldwind.avkey.ElevationUnit";

    final String END = "gov.nasa.worldwind.avkey.End";

    final String EXPIRY_TIME = "gov.nasa.worldwind.avkey.ExpiryTime";
    final String EXTENT = "gov.nasa.worldwind.avkey.Extent";
    final String EXTERNAL_LINK = "gov.nasa.worldwind.avkey.ExternalLink";

    final String FEEDBACK_ENABLED = "gov.nasa.worldwind.avkey.FeedbackEnabled";
    final String FEEDBACK_REFERENCE_POINT = "gov.nasa.worldwind.avkey.FeedbackReferencePoint";
    final String FEEDBACK_SCREEN_BOUNDS = "gov.nasa.worldwind.avkey.FeedbackScreenBounds";
    final String FILE = "gov.nasa.worldwind.avkey.File";
    final String FILE_NAME = "gov.nasa.worldwind.avkey.FileName";
    final String FILE_SIZE = "gov.nasa.worldwind.avkey.FileSize";
    final String FILE_STORE = "gov.nasa.worldwind.avkey.FileStore";
    final String FILE_STORE_LOCATION = "gov.nasa.worldwind.avkey.FileStoreLocation";
    final String FLOAT32 = "gov.nasa.worldwind.avkey.Float32";
    final String FLOAT64 = "gov.nasa.worldwind.avkey.Float64";
    final String FORMAT_SUFFIX = "gov.nasa.worldwind.avkey.FormatSuffixKey";
    final String FORWARD = "gov.nasa.worldwind.avkey.Forward";
    final String FOV = "gov.nasa.worldwind.avkey.FieldOfView";
    final String FORCE_LEVEL_ZERO_LOADS = "gov.nasa.worldwind.avkey.ForceLevelZeroLoads";
    final String FRACTION = "gov.nasa.worldwind.avkey.Fraction";
    final String FRAME_TIMESTAMP = "gov.nasa.worldwind.avkey.FrameTimestamp";

    final String GDAL_AREA = "gov.nasa.worldwind.avkey.GDAL.Area";
    final String GDAL_CACHEMAX = "gov.nasa.worldwind.avkey.GDAL.CacheMax";
    final String GDAL_DEBUG = "gov.nasa.worldwind.avkey.GDAL.Debug";
    final String GDAL_MASK_DATASET = "gov.nasa.worldwind.avkey.GDAL.MaskDataset";
    final String GDAL_TIMEOUT = "gov.nasa.worldwind.avkey.GDAL.TimeOut";
    final String GDAL_PATH = "gov.nasa.worldwind.avkey.GDAL.Path";

    final String GET_CAPABILITIES_URL = "gov.nasa.worldwind.avkey.GetCapabilitiesURL";
    final String GET_COVERAGE_URL = "gov.nasa.worldwind.avkey.GetCoverageURL";
    final String GET_MAP_URL = "gov.nasa.worldwind.avkey.GetMapURL";
    final String GEOGRAPHIC_PROJECTION_CLASS_NAME = "gov.nasa.worldwind.globes.GeographicProjectionClassName";
    final String GLOBE = "gov.nasa.worldwind.avkey.GlobeObject";
    final String GLOBE_CLASS_NAME = "gov.nasa.worldwind.avkey.GlobeClassName";
    final String GRAYSCALE = "gov.nasa.worldwind.avkey.Grayscale";
    final String GREAT_CIRCLE = "gov.nasa.worldwind.avkey.GreatCircle";

    final String HEADING = "gov.nasa.worldwind.avkey.Heading";
    final String HEIGHT = "gov.nasa.worldwind.avkey.Height";
    final String HIDDEN = "gov.nasa.worldwind.avkey.Hidden";
    final String HORIZONTAL = "gov.nasa.worldwind.avkey.Horizontal";
    final String HOT_SPOT = "gov.nasa.worldwind.avkey.HotSpot";
    final String HOVER_TEXT = "gov.nasa.worldwind.avkey.HoverText";
    final String HTTP_SSL_CONTEXT = "gov.nasa.worldwind.avkey.HTTP.SSLContext";

    final String ICON_NAME = "gov.nasa.worldwind.avkey.IconName";
    final String IGNORE = "gov.nasa.worldwind.avkey.Ignore";
    final String IMAGE = "gov.nasa.worldwind.avkey.Image";
    final String IMAGE_FORMAT = "gov.nasa.worldwind.avkey.ImageFormat";
    /**
     * Indicates whether an image represents color or grayscale values. When used as a key, the corresponding value may
     * be one of the following: <code>COLOR</code> or <code>GRAYSCALE</code>.
     */
    final String IMAGE_COLOR_FORMAT = "gov.nasa.worldwind.avkey.ImageColorFormat";
    final String INACTIVE_LEVELS = "gov.nasa.worldwind.avkey.InactiveLevels";
    final String INSTALLED = "gov.nasa.worldwind.avkey.Installed";
    final String INITIAL_ALTITUDE = "gov.nasa.worldwind.avkey.InitialAltitude";
    final String INITIAL_HEADING = "gov.nasa.worldwind.avkey.InitialHeading";
    final String INITIAL_LATITUDE = "gov.nasa.worldwind.avkey.InitialLatitude";
    final String INITIAL_LONGITUDE = "gov.nasa.worldwind.avkey.InitialLongitude";
    final String INITIAL_PITCH = "gov.nasa.worldwind.avkey.InitialPitch";
    final String INPUT_HANDLER_CLASS_NAME = "gov.nasa.worldwind.avkey.InputHandlerClassName";
    final String INSET_PIXELS = "gov.nasa.worldwind.avkey.InsetPixels";
    final String INT8 = "gov.nasa.worldwind.avkey.Int8";
    final String INT16 = "gov.nasa.worldwind.avkey.Int16";
    final String INT32 = "gov.nasa.worldwind.avkey.Int32";
    final String INT64 = "gov.nasa.worldwind.avkey.Int64";

    final String LABEL = "gov.nasa.worldwind.avkey.Label";
    final String LAST_UPDATE = "gov.nasa.worldwind.avkey.LastUpdateKey";
    final String LAYER = "gov.nasa.worldwind.avkey.LayerObject";
    final String LAYER_ABSTRACT = "gov.nasa.worldwind.avkey.LayerAbstract";
    final String LAYER_DESCRIPTOR_FILE = "gov.nasa.worldwind.avkey.LayerDescriptorFile";
    final String LAYER_FACTORY = "gov.nasa.worldwind.avkey.LayerFactory";
    final String LAYER_NAME = "gov.nasa.worldwind.avkey.LayerName";
    final String LAYER_NAMES = "gov.nasa.worldwind.avkey.LayerNames";
    final String LAYERS = "gov.nasa.worldwind.avkey.LayersObject";
    final String LAYERS_CLASS_NAMES = "gov.nasa.worldwind.avkey.LayerClassNames";
    final String LEFT = "gov.nasa.worldwind.avkey.Left";
    final String LEFT_OF_CENTER = "gov.nasa.worldwind.avkey.LeftOfCenter";
    final String LEVEL_NAME = "gov.nasa.worldwind.avkey.LevelNameKey";
    final String LEVEL_NUMBER = "gov.nasa.worldwind.avkey.LevelNumberKey";
    final String LEVEL_ZERO_TILE_DELTA = "gov.nasa.worldwind.avkey.LevelZeroTileDelta";
    final String LINEAR = "gov.nasa.worldwind.avkey.Linear";
    final String LITTLE_ENDIAN = "gov.nasa.worldwind.avkey.LittleEndian";
    final String LOGGER_NAME = "gov.nasa.worldwind.avkey.LoggerName";
    final String LOXODROME = "gov.nasa.worldwind.avkey.Loxodrome";

    final String MAP_SCALE = "gov.nasa.worldwind.avkey.MapScale";
    final String MARS_ELEVATION_MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.MarsElevationModelClassName";
    final String MARS_ELEVATION_MODEL_CONFIG_FILE = "gov.nasa.worldwind.avkey.MarsElevationModelConfigFile";

    /**
     * Describes the maximum number of attempts to make when downloading a resource before attempts are suspended.
     * Attempts are restarted after the interval specified by {@link #MIN_ABSENT_TILE_CHECK_INTERVAL}.
     *
     * @see #MIN_ABSENT_TILE_CHECK_INTERVAL
     */
    final String MAX_ABSENT_TILE_ATTEMPTS = "gov.nasa.worldwind.avkey.MaxAbsentTileAttempts";

    final String MAX_ACTIVE_ALTITUDE = "gov.nasa.worldwind.avkey.MaxActiveAltitude";
    final String MAX_MESSAGE_REPEAT = "gov.nasa.worldwind.avkey.MaxMessageRepeat";
    final String MEMORY_CACHE_SET_CLASS_NAME = "gov.nasa.worldwind.avkey.MemoryCacheSetClassName";
    /**
     * Indicates the location that MIL-STD-2525 tactical symbols and tactical point graphics retrieve their icons from.
     * When used as a key, the corresponding value must be a string indicating a URL to a remote server, a URL to a
     * ZIP/JAR file, or a path to folder on the local file system.
     */
    final String MIL_STD_2525_ICON_RETRIEVER_PATH = "gov.nasa.worldwind.avkey.MilStd2525IconRetrieverPath";
    final String MIME_TYPE = "gov.nasa.worldwind.avkey.MimeType";

    /**
     * Describes the interval to wait before allowing further attempts to download a resource after the number of
     * attempts specified by {@link #MAX_ABSENT_TILE_ATTEMPTS} are made.
     *
     * @see #MAX_ABSENT_TILE_ATTEMPTS
     */
    final String MIN_ABSENT_TILE_CHECK_INTERVAL = "gov.nasa.worldwind.avkey.MinAbsentTileCheckInterval";
    final String MIN_ACTIVE_ALTITUDE = "gov.nasa.worldwind.avkey.MinActiveAltitude";

    // Implementation note: the keys MISSING_DATA_SIGNAL and MISSING_DATA_REPLACEMENT are intentionally different than
    // their actual string values. Legacy code is expecting the string values "MissingDataFlag" and "MissingDataValue",
    // respectively.
    final String MISSING_DATA_SIGNAL = "gov.nasa.worldwind.avkey.MissingDataFlag";
    final String MISSING_DATA_REPLACEMENT = "gov.nasa.worldwind.avkey.MissingDataValue";

    final String MODEL = "gov.nasa.worldwind.avkey.ModelObject";
    final String MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.ModelClassName";
    final String MOON_ELEVATION_MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.MoonElevationModelClassName";
    final String MOON_ELEVATION_MODEL_CONFIG_FILE = "gov.nasa.worldwind.avkey.MoonElevationModelConfigFile";

    final String NAME = "gov.nasa.worldwind.avkey.Name";
    final String NETWORK_STATUS_CLASS_NAME = "gov.nasa.worldwind.avkey.NetworkStatusClassName";
    final String NETWORK_STATUS_TEST_SITES = "gov.nasa.worldwind.avkey.NetworkStatusTestSites";
    final String NEXT = "gov.nasa.worldwind.avkey.Next";
    final String NUM_BANDS = "gov.nasa.worldwind.avkey.NumBands";
    final String NUM_EMPTY_LEVELS = "gov.nasa.worldwind.avkey.NumEmptyLevels";
    final String NUM_LEVELS = "gov.nasa.worldwind.avkey.NumLevels";
    final String NETWORK_RETRIEVAL_ENABLED = "gov.nasa.worldwind.avkey.NetworkRetrievalEnabled";
    final String NORTH = "gov.nasa.worldwind.avkey.North";

    final String OFFLINE_MODE = "gov.nasa.worldwind.avkey.OfflineMode";
    final String OPACITY = "gov.nasa.worldwind.avkey.Opacity";
    /**
     * Indicates an object's position in a series. When used as a key, the corresponding value must be an {@link
     * Integer} object indicating the ordinal.
     */
    final String ORDINAL = "gov.nasa.worldwind.avkey.Ordinal";
    /**
     * Indicates a list of one or more object's positions in a series. When used as a key, the corresponding value must
     * be a {@link java.util.List} of {@link Integer} objects indicating the ordinals.
     */
    final String ORDINAL_LIST = "gov.nasa.worldwind.avkey.OrdinalList";
    final String ORIGIN = "gov.nasa.worldwind.avkey.Origin";

    final String PARENT_LAYER_NAME = "gov.nasa.worldwind.avkey.ParentLayerName";

    final String PAUSE = "gov.nasa.worldwind.avkey.Pause";
    final String PICKED_OBJECT = "gov.nasa.worldwind.avkey.PickedObject";
    final String PICKED_OBJECT_ID = "gov.nasa.worldwind.avkey.PickedObject.ID";
    final String PICKED_OBJECT_PARENT_LAYER = "gov.nasa.worldwind.avkey.PickedObject.ParentLayer";
    final String PICKED_OBJECT_PARENT_LAYER_NAME = "gov.nasa.worldwind.avkey.PickedObject.ParentLayer.Name";
    final String PICKED_OBJECT_SIZE = "gov.nasa.worldwind.avkey.PickedObject.Size";
    final String PICK_ENABLED = "gov.nasa.worldwind.avkey.PickEnabled";
    final String PIXELS = "gov.nasa.worldwind.avkey.Pixels";
    /**
     * Indicates whether a raster's pixel values represent imagery or elevation data. When used as a key, the
     * corresponding value may be one of the following: <code>IMAGERY</code> or <code>ELEVATION</code>.
     */
    final String PIXEL_FORMAT = "gov.nasa.worldwind.avkey.PixelFormat";
    final String PIXEL_HEIGHT = "gov.nasa.worldwind.avkey.PixelHeight";
    final String PIXEL_WIDTH = "gov.nasa.worldwind.avkey.PixelWidth";
    /** @deprecated Use <code>{@link #DATA_TYPE} instead.</code>. */
    final String PIXEL_TYPE = AVKey.DATA_TYPE;

    final String PLACENAME_LAYER_CACHE_SIZE = "gov.nasa.worldwind.avkey.PlacenameLayerCacheSize";
    final String PLAY = "gov.nasa.worldwind.avkey.Play";
    final String POSITION = "gov.nasa.worldwind.avkey.Position";
    final String PREVIOUS = "gov.nasa.worldwind.avkey.Previous";

    final String PRODUCER_ENABLE_FULL_PYRAMID = "gov.nasa.worldwind.avkey.Producer.EnableFullPyramid";

    final String PROGRESS = "gov.nasa.worldwind.avkey.Progress";
    final String PROGRESS_MESSAGE = "gov.nasa.worldwind.avkey.ProgressMessage";

    final String PROJECTION_DATUM = "gov.nasa.worldwind.avkey.Projection.Datum";
    final String PROJECTION_DESC = "gov.nasa.worldwind.avkey.Projection.Description";
    final String PROJECTION_EPSG_CODE = "gov.nasa.worldwind.avkey.Projection.EPSG.Code";
    final String PROJECTION_HEMISPHERE = "gov.nasa.worldwind.avkey.Projection.Hemisphere";
    final String PROJECTION_NAME = "gov.nasa.worldwind.avkey.Projection.Name";
    final String PROJECTION_UNITS = "gov.nasa.worldwind.avkey.Projection.Units";
    final String PROJECTION_UNKNOWN = "gov.nasa.worldwind.Projection.Unknown";
    final String PROJECTION_UTM = "gov.nasa.worldwind.avkey.Projection.UTM";
    final String PROJECTION_ZONE = "gov.nasa.worldwind.avkey.Projection.Zone";

    final String PROPERTIES = "gov.nasa.worldwind.avkey.Properties";

    final String PROTOCOL = "gov.nasa.worldwind.avkey.Protocol";
    final String PROTOCOL_HTTP = "gov.nasa.worldwind.avkey.Protocol.HTTP";
    final String PROTOCOL_HTTPS = "gov.nasa.worldwind.avkey.Protocol.HTTPS";

    final String RECTANGLES = "gov.nasa.worldwind.avkey.Rectangles";
    final String REDRAW_ON_MOUSE_PRESSED = "gov.nasa.worldwind.avkey.ForceRedrawOnMousePressed";

    final String RELATIVE_TO_GLOBE = "gov.nasa.worldwind.avkey.RelativeToGlobe";
    final String RELATIVE_TO_SCREEN = "gov.nasa.worldwind.avkey.RelativeToScreen";

    final String RANGE = "gov.nasa.worldwind.avkey.Range";
    final String RASTER_BAND_ACTUAL_BITS_PER_PIXEL = "gov.nasa.worldwind.avkey.RasterBand.ActualBitsPerPixel";
    final String RASTER_BAND_MIN_PIXEL_VALUE = "gov.nasa.worldwind.avkey.RasterBand.MinPixelValue";
    final String RASTER_BAND_MAX_PIXEL_VALUE = "gov.nasa.worldwind.avkey.RasterBand.MaxPixelValue";

    final String RASTER_HAS_ALPHA = "gov.nasa.worldwind.avkey.RasterHasAlpha";
    final String RASTER_HAS_OVERVIEWS = "gov.nasa.worldwind.avkey.Raster.HasOverviews";
    final String RASTER_HAS_VOIDS = "gov.nasa.worldwind.avkey.Raster.HasVoids";
    final String RASTER_LAYER_CLASS_NAME = "gov.nasa.worldwind.avkey.RasterLayer.ClassName";
    final String RASTER_PIXEL = "gov.nasa.worldwind.avkey.RasterPixel";
    final String RASTER_PIXEL_IS_AREA = "gov.nasa.worldwind.avkey.RasterPixelIsArea";
    final String RASTER_PIXEL_IS_POINT = "gov.nasa.worldwind.avkey.RasterPixelIsPoint";
    final String RECTANGULAR_TESSELLATOR_MAX_LEVEL = "gov.nasa.worldwind.avkey.RectangularTessellatorMaxLevel";
    final String REPAINT = "gov.nasa.worldwind.avkey.Repaint";
    final String REPEAT_NONE = "gov.nasa.worldwind.avkey.RepeatNone";
    final String REPEAT_X = "gov.nasa.worldwind.avkey.RepeatX";
    final String REPEAT_Y = "gov.nasa.worldwind.avkey.RepeatY";
    final String REPEAT_XY = "gov.nasa.worldwind.avkey.RepeatXY";

    final String RESIZE = "gov.nasa.worldwind.avkey.Resize";
    /** On window resize, scales the item to occupy a constant relative size of the viewport. */
    final String RESIZE_STRETCH = "gov.nasa.worldwind.CompassLayer.ResizeStretch";
    /**
     * On window resize, scales the item to occupy a constant relative size of the viewport, but not larger than the
     * item's inherent size scaled by the layer's item scale factor.
     */
    final String RESIZE_SHRINK_ONLY = "gov.nasa.worldwind.CompassLayer.ResizeShrinkOnly";
    /** Does not modify the item size when the window changes size. */
    final String RESIZE_KEEP_FIXED_SIZE = "gov.nasa.worldwind.CompassLayer.ResizeKeepFixedSize";
    final String RETAIN_LEVEL_ZERO_TILES = "gov.nasa.worldwind.avkey.RetainLevelZeroTiles";
    final String RETRIEVAL_POOL_SIZE = "gov.nasa.worldwind.avkey.RetrievalPoolSize";
    final String RETRIEVE_PROPERTIES_FROM_SERVICE = "gov.nasa.worldwind.avkey.RetrievePropertiesFromService";
    final String RETRIEVAL_QUEUE_SIZE = "gov.nasa.worldwind.avkey.RetrievalQueueSize";
    final String RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT = "gov.nasa.worldwind.avkey.RetrievalStaleRequestLimit";
    final String RETRIEVAL_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.RetrievalServiceClassName";
    final String RETRIEVER_FACTORY_LOCAL = "gov.nasa.worldwind.avkey.RetrieverFactoryLocal";
    final String RETRIEVER_FACTORY_REMOTE = "gov.nasa.worldwind.avkey.RetrieverFactoryRemote";
    final String RETRIEVER_STATE = "gov.nasa.worldwind.avkey.RetrieverState";
    final String RETRIEVAL_STATE_ERROR = "gov.nasa.worldwind.avkey.RetrievalStateError";
    final String RETRIEVAL_STATE_SUCCESSFUL = "gov.nasa.worldwind.avkey.RetrievalStateSuccessful";
    final String RHUMB_LINE = "gov.nasa.worldwind.avkey.RhumbLine";
    final String RIGHT = "gov.nasa.worldwind.avkey.Right";
    final String RIGHT_OF_CENTER = "gov.nasa.worldwind.avkey.RightOfCenter";
    final String ROLL = "gov.nasa.worldwind.avkey.Roll";
    final String ROLLOVER_TEXT = "gov.nasa.worldwind.avkey.RolloverText";

    final String SCHEDULED_TASK_POOL_SIZE = "gov.nasa.worldwind.avkey.ScheduledTaskPoolSize";
    final String SCHEDULED_TASK_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.ScheduledTaskServiceClassName";
    final String SCENE_CONTROLLER = "gov.nasa.worldwind.avkey.SceneControllerObject";
    final String SCENE_CONTROLLER_CLASS_NAME = "gov.nasa.worldwind.avkey.SceneControllerClassName";
    final String SCREEN = "gov.nasa.worldwind.avkey.ScreenObject";
    final String SCREEN_CREDIT = "gov.nasa.worldwind.avkey.ScreenCredit";
    final String SCREEN_CREDIT_LINK = "gov.nasa.worldwind.avkey.ScreenCreditLink";
    final String SECTOR = "gov.nasa.worldwind.avKey.Sector";
    final String SECTOR_BOTTOM_LEFT = "gov.nasa.worldwind.avkey.Sector.BottomLeft";
    final String SECTOR_BOTTOM_RIGHT = "gov.nasa.worldwind.avkey.Sector.BottomRight";
    final String SECTOR_GEOMETRY_CACHE_SIZE = "gov.nasa.worldwind.avkey.SectorGeometryCacheSize";
    final String SECTOR_RESOLUTION_LIMITS = "gov.nasa.worldwind.avkey.SectorResolutionLimits";
    final String SECTOR_RESOLUTION_LIMIT = "gov.nasa.worldwind.avkey.SectorResolutionLimit";
    final String SECTOR_UPPER_LEFT = "gov.nasa.worldwind.avkey.Sector.UpperLeft";
    final String SECTOR_UPPER_RIGHT = "gov.nasa.worldwind.avkey.Sector.UpperRight";
    final String SENDER = "gov.nasa.worldwind.avkey.Sender";
    final String SERVER = "gov.nasa.worldwind.avkey.Server";
    final String SERVICE = "gov.nasa.worldwind.avkey.ServiceURLKey";
    final String SERVICE_CLASS = "gov.nasa.worldwind.avkey.ServiceClass";
    final String SERVICE_NAME = "gov.nasa.worldwind.avkey.ServiceName";
    final String SERVICE_NAME_LOCAL_RASTER_SERVER = "LocalRasterServer";
    final String SERVICE_NAME_OFFLINE = "Offline";
    final String SESSION_CACHE_CLASS_NAME = "gov.nasa.worldwind.avkey.SessionCacheClassName";
    final String SHAPE_ATTRIBUTES = "gov.nasa.worldwind.avkey.ShapeAttributes";
    final String SHAPE_CIRCLE = "gov.nasa.worldwind.avkey.ShapeCircle";
    final String SHAPE_ELLIPSE = "gov.nasa.worldwind.avkey.ShapeEllipse";
    final String SHAPE_LINE = "gov.nasa.worldwind.avkey.ShapeLine";
    final String SHAPE_NONE = "gov.nasa.worldwind.avkey.ShapeNone";
    final String SHAPE_PATH = "gov.nasa.worldwind.avkey.ShapePath";
    final String SHAPE_POLYGON = "gov.nasa.worldwind.avkey.ShapePolygon";
    final String SHAPE_QUAD = "gov.nasa.worldwind.avkey.ShapeQuad";
    final String SHAPE_RECTANGLE = "gov.nasa.worldwind.avkey.ShapeRectangle";
    final String SHAPE_SQUARE = "gov.nasa.worldwind.avkey.ShapeSquare";
    final String SHAPE_TRIANGLE = "gov.nasa.worldwind.avkey.ShapeTriangle";
    final String SHAPEFILE_GEOMETRY_CACHE_SIZE = "gov.nasa.worldwind.avkey.ShapefileGeometryCacheSize";
    final String SHAPEFILE_LAYER_FACTORY = "gov.nasa.worldwind.avkey.ShapefileLayerFactory";
    final String SHORT_DESCRIPTION = "gov.nasa.worldwind.avkey.Server.ShortDescription";
    final String SIZE_FIT_TEXT = "gov.nasa.worldwind.avkey.SizeFitText";
    final String SIZE_FIXED = "gov.nasa.worldwind.avkey.SizeFixed";
    final String SPATIAL_REFERENCE_WKT = "gov.nasa.worldwind.avkey.SpatialReference.WKT";
    final String SOUTH = "gov.nasa.worldwdind.avkey.South";
    final String START = "gov.nasa.worldwind.avkey.Start";
    final String STEREO_FOCUS_ANGLE = "gov.nasa.worldwind.StereoFocusAngle";
    final String STEREO_INTEROCULAR_DISTANCE = "gov.nasa.worldwind.StereoFInterocularDistance";
    final String STEREO_MODE = "gov.nasa.worldwind.stereo.mode"; // lowercase to match Java property convention
    final String STEREO_MODE_DEVICE = "gov.nasa.worldwind.avkey.StereoModeDevice";
    final String STEREO_MODE_NONE = "gov.nasa.worldwind.avkey.StereoModeNone";
    final String STEREO_MODE_RED_BLUE = "gov.nasa.worldwind.avkey.StereoModeRedBlue";
    final String STEREO_TYPE = "gov.nasa.worldwind.stereo.type";
    final String STEREO_TYPE_TOED_IN = "gov.nasa.worldwind.avkey.StereoModeToedIn";
    final String STEREO_TYPE_PARALLEL = "gov.nasa.worldwind.avkey.StereoModeParallel";
    final String STOP = "gov.nasa.worldwind.avkey.Stop";
    final String STYLE_NAMES = "gov.nasa.worldwind.avkey.StyleNames";
    final String SURFACE_TILE_DRAW_CONTEXT = "gov.nasa.worldwind.avkey.SurfaceTileDrawContext";

    final String TESSELLATOR_CLASS_NAME = "gov.nasa.worldwind.avkey.TessellatorClassName";
    final String TEXTURE = "gov.nasa.worldwind.avkey.Texture";
    final String TEXTURE_CACHE_SIZE = "gov.nasa.worldwind.avkey.TextureCacheSize";
    final String TEXTURE_COORDINATES = "gov.nasa.worldwind.avkey.TextureCoordinates";
    final String TEXTURE_FORMAT = "gov.nasa.worldwind.avkey.TextureFormat";
    final String TEXTURE_IMAGE_CACHE_SIZE = "gov.nasa.worldwind.avkey.TextureTileCacheSize";
    final String TARGET = "gov.nasa.worldwind.avkey.Target";
    final String TASK_POOL_SIZE = "gov.nasa.worldwind.avkey.TaskPoolSize";
    final String TASK_QUEUE_SIZE = "gov.nasa.worldwind.avkey.TaskQueueSize";
    final String TASK_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.TaskServiceClassName";
    final String TEXT = "gov.nasa.worldwind.avkey.Text";
    final String TEXT_EFFECT_NONE = "gov.nasa.worldwind.avkey.TextEffectNone";
    final String TEXT_EFFECT_OUTLINE = "gov.nasa.worldwind.avkey.TextEffectOutline";
    final String TEXT_EFFECT_SHADOW = "gov.nasa.worldwind.avkey.TextEffectShadow";
    final String TILE_DELTA = "gov.nasa.worldwind.avkey.TileDeltaKey";
    final String TILE_HEIGHT = "gov.nasa.worldwind.avkey.TileHeightKey";
    final String TILE_ORIGIN = "gov.nasa.worldwind.avkey.TileOrigin";
    final String TILE_RETRIEVER = "gov.nasa.worldwind.avkey.TileRetriever";
    final String TILE_URL_BUILDER = "gov.nasa.worldwind.avkey.TileURLBuilder";
    final String TILE_WIDTH = "gov.nasa.worldwind.avkey.TileWidthKey";
    final String TILED_IMAGERY = "gov.nasa.worldwind.avkey.TiledImagery";
    final String TILED_ELEVATIONS = "gov.nasa.worldwind.avkey.TiledElevations";
    final String TILED_RASTER_PRODUCER_CACHE_SIZE = "gov.nasa.worldwind.avkey.TiledRasterProducerCacheSize";
    final String TILED_RASTER_PRODUCER_LARGE_DATASET_THRESHOLD =
        "gov.nasa.worldwind.avkey.TiledRasterProducerLargeDatasetThreshold";
    final String TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL = "gov.nasa.worldwind.avkey.TiledRasterProducer.LimitMaxLevel";
    final String TILT = "gov.nasa.worldwind.avkey.Tilt";
    final String TITLE = "gov.nasa.worldwind.avkey.Title";
    final String TOP = "gov.nasa.worldwind.avkey.Top";
    final String TRANSPARENCY_COLORS = "gov.nasa.worldwind.avkey.TransparencyColors";
    final String TREE = "gov.nasa.worldwind.avkey.Tree";
    final String TREE_NODE = "gov.nasa.worldwind.avkey.TreeNode";

    final String UNIT_FOOT = "gov.nasa.worldwind.avkey.Unit.Foot";
    final String UNIT_METER = "gov.nasa.worldwind.avkey.Unit.Meter";

    final String UNRESOLVED = "gov.nasa.worldwind.avkey.Unresolved";
    final String UPDATED = "gov.nasa.worldwind.avkey.Updated";
    final String URL = "gov.nasa.worldwind.avkey.URL";
    final String URL_CONNECT_TIMEOUT = "gov.nasa.worldwind.avkey.URLConnectTimeout";
    final String URL_PROXY_HOST = "gov.nasa.worldwind.avkey.UrlProxyHost";
    final String URL_PROXY_PORT = "gov.nasa.worldwind.avkey.UrlProxyPort";
    final String URL_PROXY_TYPE = "gov.nasa.worldwind.avkey.UrlProxyType";
    final String URL_READ_TIMEOUT = "gov.nasa.worldwind.avkey.URLReadTimeout";
    final String USE_MIP_MAPS = "gov.nasa.worldwind.avkey.UseMipMaps";
    final String USE_TRANSPARENT_TEXTURES = "gov.nasa.worldwind.avkey.UseTransparentTextures";

    final String VBO_THRESHOLD = "gov.nasa.worldwind.avkey.VBOThreshold";
    final String VBO_USAGE = "gov.nasa.worldwind.avkey.VBOUsage";
    final String VERSION = "gov.nasa.worldwind.avkey.Version";
    final String VERTICAL = "gov.nasa.worldwind.avkey.Vertical";
    final String VERTICAL_EXAGGERATION = "gov.nasa.worldwind.avkey.VerticalExaggeration";
    final String VERTICAL_EXAGGERATION_UP = "gov.nasa.worldwind.avkey.VerticalExaggerationUp";
    final String VERTICAL_EXAGGERATION_DOWN = "gov.nasa.worldwind.avkey.VerticalExaggerationDown";
    final String VIEW = "gov.nasa.worldwind.avkey.ViewObject";
    final String VIEW_CLASS_NAME = "gov.nasa.worldwind.avkey.ViewClassName";
    final String VIEW_INPUT_HANDLER_CLASS_NAME = "gov.nasa.worldwind.avkey.ViewInputHandlerClassName";
    final String VIEW_QUIET = "gov.nasa.worldwind.avkey.ViewQuiet";

    // Viewing operations
    final String VIEW_OPERATION = "gov.nasa.worldwind.avkey.ViewOperation";
    final String VIEW_PAN = "gov.nasa.worldwind.avkey.Pan";
    final String VIEW_LOOK = "gov.nasa.worldwind.avkey.ControlLook";
    final String VIEW_HEADING_LEFT = "gov.nasa.worldwind.avkey.HeadingLeft";
    final String VIEW_HEADING_RIGHT = "gov.nasa.worldwind.avkey.HeadingRight";
    final String VIEW_ZOOM_IN = "gov.nasa.worldwind.avkey.ZoomIn";
    final String VIEW_ZOOM_OUT = "gov.nasa.worldwind.avkey.ZoomOut";
    final String VIEW_PITCH_UP = "gov.nasa.worldwind.avkey.PitchUp";
    final String VIEW_PITCH_DOWN = "gov.nasa.worldwind.avkey.PitchDown";
    final String VIEW_FOV_NARROW = "gov.nasa.worldwind.avkey.FovNarrow";
    final String VIEW_FOV_WIDE = "gov.nasa.worldwind.avkey.FovWide";

    final String VISIBILITY_ACTION_RELEASE = "gov.nasa.worldwind.avkey.VisibilityActionRelease";
    final String VISIBILITY_ACTION_RETAIN = "gov.nasa.worldwind.avkey.VisibilityActionRetain";

    final String WAKEUP_TIMEOUT = "gov.nasa.worldwind.avkey.WakeupTimeout";
    final String WEB_VIEW_FACTORY = "gov.nasa.worldwind.avkey.WebViewFactory";
    final String WEST = "gov.nasa.worldwind.avkey.West";
    final String WIDTH = "gov.nasa.worldwind.avkey.Width";
    final String WMS_BACKGROUND_COLOR = "gov.nasa.worldwind.avkey.BackgroundColor";

    final String WFS_URL = "gov.nasa.worldwind.avkey.WFS.URL";
    final String WCS_VERSION = "gov.nasa.worldwind.avkey.WCSVersion";
    final String WMS_VERSION = "gov.nasa.worldwind.avkey.WMSVersion";
    final String WORLD_MAP_IMAGE_PATH = "gov.nasa.worldwind.avkey.WorldMapImagePath";
    final String WORLD_WIND_DOT_NET_LAYER_SET = "gov.nasa.worldwind.avkey.WorldWindDotNetLayerSet";
    final String WORLD_WIND_DOT_NET_PERMANENT_DIRECTORY = "gov.nasa.worldwind.avkey.WorldWindDotNetPermanentDirectory";
    final String WORLD_WINDOW_CLASS_NAME = "gov.nasa.worldwind.avkey.WorldWindowClassName";
}
