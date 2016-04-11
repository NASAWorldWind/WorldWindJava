#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

# $Id: README.txt 3419 2015-08-28 00:09:50Z dcollins $

This document provides links on getting started with the World Wind Java SDK, provides instructions on running the a
World Wind demo application, and outlines the key changes between each World Wind Java SDK release.


Getting Started With the World Wind Java SDK
------------------------------------------------------------

Key files and folders in the World Wind Java SDK:
- build.xml: Apache ANT build file for the World Wind Java SDK.
- src: Contains all Java source files for the World Wind Java SDK.
- lib-external/gdal: Contains the GDAL native binaries libraries that may optionally be distributed with World Wind.

Links to important World Wind sites that will help you get started using the World Wind Java SDK in your application:

World Wind Developer's Guide:
http://goworldwind.org/

World Wind Main Website:
http://worldwind.arc.nasa.gov/java/

World Wind Forum:
http://forum.worldwindcentral.com/forumdisplay.php?f=37

World Wind API Documentation:
http://builds.worldwind.arc.nasa.gov/worldwind-releases/2.0/docs/api/index.html

World Wind Bug Base:
http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ


Running a Basic Demo Application
------------------------------------------------------------

To run the basic demo on Mac OS X or Linux:
    1) Open a terminal.
    2) cd to the World Wind release folder.
    3) chmod +x run-demo.bash
    4) ./run-demo.bash gov.nasa.worldwindx.examples.ApplicationTemplate

To run the basic demo on Windows:
    1) Open a command prompt.
    2) cd to the World Wind release folder.
    3) run-demo.bat gov.nasa.worldwindx.examples.ApplicationTemplate

Your computer must have a modern graphics card with an up-to-date driver.  The source of most getting-up-and-running
problems is an out-of-date graphics driver. To get an updated driver, visit your graphics card manufacturer's web site.
This will most likely be either NVIDIA, ATI or Intel. The drivers are typically under a link named "Downloads" or
"Support". If your computer is a laptop, then updated drivers are probably at the laptop manufacturer's web site rather
than the graphics card manufacturer's.


New features and improvements in World Wind Java SDK 2.1.0
------------------------------------------------------------
- See http://goworldwind.org/releases/ for a description of this release's major features.

- Repaired an issue with the accuracy of airspace shape projection to the 3D globe. The geometry for airspace shapes
  with large dimensions are now displayed correctly. The following airspace shapes are affected: CappedCylinder,
  PartialCappedCylinder, Orbit, PolyArc, TrackAirspace, Route, Cake.
- Repaired WWJ-521, LevelSet.getLastLevel not returning correct level for sector resolution limits.
- Repaired WWJ-522, HighResolutionTerrain bulk intersector not notifying of exceptions. Added notification interface.
- Repaired WWJ-533, WMS layers intermittently fail to update expiry time.
- Repaired WWJ-537, Surface shape lines appear dashed when viewed obliquely.
- Repaired an issue where AnalyticSurface returns an incorrect picked object when the altitude mode is CLAMP_TO_GROUND.
- Added ability to retrieve elevations via WCS. Added WCSElevations example.
- Added image texture support to SurfacePolygon.
- Modified CompoundElevationModel to sort elevation models from lowest resolution to highest when models are added.
- Modified SurfaceImage to optionally display on top of surface shapes, via the alwaysOnTop property.

New features and improvements in World Wind Java SDK 2.0.0
------------------------------------------------------------
- See http://goworldwind.org/releases/ for a description of this release's major features.

- Migrated World Wind's usage of the JOGL library to JOGL version 2.1.5.
- Redesigned the World Wind Java Web Start site to work with JOGL 2 and Java 7 on all platforms..
- Redesigned the World Wind Java applet examples to work with JOGL 2 and Java 7 on all platforms.
  New applets leverage the same Java Web Start resources used by applications.
- Redesigned the World Wind Java demos for simplicity, and consolidated all demo resources in a single place.
- Added a new ANT target webstart.site that creates a self-contained and deployable World Wind Web Start site.
- Removed the World Wind servers module, and its associated packages in the World Wind client:
  gov.nasa.worldwind.database, gov.nasa.worldwind.ows, gov.nasa.worldwind.wfs, gov.nasa.worldwind.wss
- Removed the need for the separate performance JAR file worldwind-performance.jar.
  Moved the performance package into gov.nasa.worldwindx.performance.
- Repaired a problem that caused the compass not to be displayed when another instance of the layer had been used in
  another WorldWindow.
- Added a method to the Terrain interface that identifies the input positions' altitude reference and converts the
  altitudes to relative-to-ground, which is what the intersection methods expect.
- Updated TiledImageLayer, RectangularTessellator and SurfaceObjectTileBuilder to adjust their level of detail when the
  application uses a narrow field of view. See WWJ-445.
- Updated View to use the smallest possible near clip distance for the currently available depth buffer resolution.
  See WWJ-460.
- Updated the FlatWorldEarthquakes example to use the USGS GeoJSON earthquake feed.
- Fixed WWJ-302, that caused flashing of continuously updating KML ground overlays.
- Fixed WWJ-371, where some portions of Collada shapes were not pickable.
- Fixed WWJ-425, where terrain picking fails on VMware virtual desktops.
- Fixed WWJ-433, where surface shapes crossing both the prime meridian and the anti-meridian display incorrectly.
- Fixed WWJ-452, where surface shapes containing a pole draw an incorrect outline segment.
- Fixed WWJ-443, where enabling stereo rendering on an unsupported GPU caused surface shape picking to fail without warning.
- Fixed WWJ-449, which corrects the KML parser's interpretation of an unspecified altitudeMode.
- Fixed WWJ-451, where LevelSet.getTileNumber overflows when the number of tiles is large.
- Fixed WWJ-432, BrowserBalloon content does not display on Mac with Java Web Start.
- Repaired non-scoped abstract method declaration in RigidShape class.
- Added Angle formatting for degrees and decimal minutes. Added degrees and decimal minutes option to LatLonGraticule.
- Modified shapefile loading to recognize the HGT attribute field as a height and create extruded polygons as a result.
- Modified shapefile loading to display point geometry using PointPlacemark instead of UserFacingIcon.
- Repaired KML LineString but described in forum post http://forum.worldwindcentral.com/showthread.php?41174
- Repaired bug described at http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-410
- Added option to eliminate library loader replacement when initializing GDAL.
- Added a base depth to ExtrudedPolygon to enable positioning of base vertices below the terrain.
- Consume left-click, left double-click and drag end events in ViewControlsSelectListener.
- Fixed WWJ-353, clamp bounding box angles returned from WMS capabilities documents.
- Fixed WWJ-454, WMS requests use incorrect version string.
- Fixed WWJ-441, Duplicate picked objects from IconLayer. Modified IconLayer to prevent duplicates in quadtree.
- Added IconPicking example.
- Fixed WWJ-434, KML rendering will freeze while having bad internet connection.
- Fixed WWJ-466, KML balloon text showing entity reference instead of blank field.
- Fixed WWJ-467, StatusBar altitude display below 1 km gives 0 km.
- Fixed WWJ-469, Warning from KMLRoot evictIfExpired.
- Added a batch intersection method to HighResolutionTerrain. See intersect(List<Position> positions ...
- Fixed WWJ-482, RigidShapes not regenerating geometry when detail hint changes.
- Fixed WWJ-483, GeoTIFF elevations created by ExportImageOrElevations example are incorrect.

The outstanding bugs and improvement requests can be viewed with the following URL:
http://issues.worldwind.arc.nasa.gov/jira/issues/?jql=project%20%3D%20WWJ%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened)

  Note on using the JOGL libraries without the default runtime extraction of native binaries.
    This is accomplished by modifying World Wind's JOGL distribution to load native binaries directly from the library
    path instead of dynamically using the native binary JAR files. Here are instructions on how to implement this:

    1) Extract the GlueGen and JOGL native binary JAR files for the desired platform.
    These JAR files follow the naming pattern gluegen-rt-natives-PLATFORM.jar and jogl-all-natives-PLATFORM.jar

    2) Place the extracted native binaries either in the program's working directory or in a location specified as the
    library path.
    The following JOGL user's guide page outlines supported library path variables:
    https://jogamp.org/jogl/doc/userguide/index.html#traditionallibraryloading

    3) Remove the GlueGen and JOGL native binary JAR files from your application's workspace.
    JOGL attempts to use the native binary JAR files before loading from the library path, so these files must not be
    deployed with the application.

    4) When running, specify the JVM argument -Djogamp.gluegen.UseTempJarCache=false


New features and improvements in World Wind Java SDK 1.5.1 - July 24, 2013
------------------------------------------------------------

- Repaired an issue where the DTED file reader incorrectly added a 1/2 pixel border to the DTED bounding box.
- Repaired an issue where the WWJ data importer's BIL writer output BIL world files with an incorrect pixel size.
- Modified the DTED file reader to interpret values outside the practical range of [-12000,+9000] as missing values.
  See MIL-PRF-89020B sections 3.11.2 and 3.11.3 for more information on why this change is valid for DTED files.


New features and improvements in World Wind Java SDK 1.5.0 - January 21, 2013
------------------------------------------------------------
- See http://goworldwind.org/releases/ for a description of this release's major features.

- Added global text decluttering. See the ClutterFilter and Declutterable interfaces.
- Added support for refreshing KML icons.
- Added support for applying Earth Gravitational Model offsets (EGM96) to EllipsoidalGlobe.
- Added support for HighResolutionTerrain line intersection.
- Added an OpenStreetMap layer provided by a NASA hosted MapServer instance.
- Added delegate owner support to ScreenImage.
- Added a section to the Javadoc overview outlining the behavior of World Wind path types.
- Added static utility methods for equirectangular interpolation to LatLon.
- Added the PersistSessionState example, which demonstrates how to persist a session's layer and view state.
- Removed erroneous inclusion of the BGCOLOR parameter in WMS elevation requests. This parameter causes GeoServer to return an exception.
- Increased the texture cache size from 300 MB to 500 MB.
- Improved shutdown behavior by simplifying automatic retrieval of WMS capabilities documents in WMSTiledImageLayer and WMSBasicElevationModel.
- Improved the performance of SectorGeometryList.getSurfacePoint.
- Repaired Path and Polyline's handling of the LINEAR path type.
- Repaired Path and Polyline's handling of the LOXODROME alias for RHUMB_LINE path types.
- Repaired an OpenGL state leak of GL_ELEMENT_ARRAY_BUFFER binding in terrain rendering.
- Repaired an OpenGL state leak of the normal pointer binding in COLLADA models, which caused a JVM crash on certain machines.
- Repaired BasicShapeAttributes' handling of the enableLighting attribute in its restorable state.
- Repaired Matrix.getInverse, which now returns a correct inverse for all nonsingular matrices.
- Fixed an issue where World Wind does not compile on Java 7 in IntelliJ IDEA.
- Fixed a regression bug where Box ignores the R axis while computing its effective radius.
- Fixed a bug where enabling Path lighting caused the JVM to crash.
- Fixed a bug where enabling ExtrudedPolygon side lighting with cap lighting disabled caused a NullPointerException.
- Fixed a bug where MIL-STD-2525 tactical symbols throw an exception when the OpenGL Context changes.
- Fixed a bug where the MIL-STD-2525 Fire Support Line graphic appears in the wrong place.
- Fixed a bug preventing COLLADA models from updating after a position change.
- Fixed a bug in Triangle.intersectTriangleTypes preventing triangle strip intersection from operating correctly.
- Fixed a bug in WMSTiledImageLayer causing WMS 1.3 GetMap requests to use the WMS 1.1.1 "srs" query parameter for coordinate system.
- Fixed a bug in WMSBasicElevationModel causing WMS 1.3 GetMap requests to use the WMS 1.1.1 "srs" query parameter for coordinate system.
- Fixed a bug where clicking in browser balloons or view controls prevents focus traversal.
- Fixed a bug where the MeasureToolUsage example hangs on Mac.
- Fixed a bug where the WorldWindDiagnostics app hangs on Mac.
- Fixed a bug where the Path shape changes the default shape attributes and affects the color of other AbstractShapes.
- Fixed a bug preventing the copying of text from browser balloons.
- Fixed a bug where keyboard input did not work in browser balloons.
- Modified MIL-STD-2525 symbol code parsing to correctly handle symbol modifier codes without an echelon in the second character.
- Modified MIL-STD-2525 symbol code parsing to correctly compose the modifier code for the feint/dummy installation modifier.
- Modified MIL-STD-2525 TacticalGraphicFactory to enable application configuration of MIL-STD-2525 implementation classes.
- Modified MIL-STD-2525 TacticalGraphicSymbol to enable subclasses to change and extend the symbol layout.
- Modified SurfaceText to add control over the text size in meters.
- Modified the World Wind release build script to include the resources directory. This directory is missing from the
  World Wind 1.3 and 1.4 releases, and will be included in all subsequent releases.


New features and improvements in World Wind Java SDK 1.4.0 - July 20, 2012
------------------------------------------------------------
- See http://goworldwind.org/releases/ for a description of this release's major features.

- Calculate expiration time using the difference between the Expires and Date headers. This guards against clock skew between client and server.
- Added ZeroElevationModel example.
- Fixed bug in KML NetworkLinkControl. NetworkLinkControl expiration time should be read from NetworkLinkControl in the target document, not the document that contains the link. Added test file to make sure that the NetworkLinkControl expiration time takes priority over expiration time set in the HTTP headers.
- Modified pre-cache methods of HighResolutionTerrain to throw InterruptedException rather than converting it to WWRuntimeException.
- Added example that shows how to build a custom renderable.
- Removed normalization of line in HighResolutionTerrain intersection calculations. Added pre-caching by Sector to HighResolutionTerrain.
- Implemented expiration of KML NetworkLinks expiration set in HTTP headers.
- Added missing constant for Drop Zone graphic to list of supported graphics in BasicArea. (Reported in forum: http://forum.worldwindcentral.com/showthread.php?t=33071). Added unit test to make sure that the tactical graphic factory supports all of the graphics that are listed as supported at http://goworldwind.org/developers-guide/symbology/tactical-graphic-status/
- Added documentation to the Globes package.
- Added Bing imagery to default layer list.
- Minor corrections to HighResolutionTerrain.
- Added readme file for Windows WebView implementation.
- PhaseLine class did not include Release Line in its list of supported graphics.
- Corrected problem in SurfaceObjectTileBuilder that caused surface shapes to appear blurry when drawn in a small window. See http://forum.worldwindcentral.com/showthread.php?t=33023
- Added COLLADA files to KML manual test instructions.
- Added missing icon for MIL-STD-2525C Forward Edge of Battle Area (FEBA) graphic in Anticipated state. Apply changes in status to the FEBA symbols. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-293.
- Modified KMLRoot.parse to only accept XML documents that begin with a tag from the KML schema. This allows malformed KML documents that do not contain a <kml> root tag to be parsed, but does not allow COLLADA files to be accidentally parsed as KML. Changed KMLRoot.resolveRemoteReference to make sure that a file was successfully parsed before returning a KMLRoot. These changes allow KML files that contain COLLADA models to be loaded by URL (for example http://kml-samples.googlecode.com/svn/trunk/kml/Model/ResourceMap/macky-normal.kml).
- Enable backface culling in COLLADA models. Some models (such as http://sketchup.google.com/3dwarehouse/details?mid=a39ade90000d690279500c4eb8df7d94&prevstart=0) do not render correctly without culling.
- Modified globe-dragging code to use the terrain object even if it's not the top object.
- Use one buffer in ColladaMeshShapes for vertex coordinates, normal vectors, and texture coordinates.
- COLLADA documentation.
- Do not convert entities in EntityMap to lower case. The entities are case sensitive. Also fixed error in the entity map. &lang; and &rang; entities were listed twice.
- Fixed infinite loop in EntityMap.replaceAll. The loop never terminated when an entity was matched in the input string that did not have a replacement. See  http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-292.
- Marked XMLEventParserContext.resolveInternalReferences as deprecated. This method was an early attempt to handle KML reference resolution that was accidentally left in the code. This reference resolution is now handled by KMLRoot and KMLStyleMap.
- Set position of COLLADA node shapes every frame so that the nodes will pickup changes in the ColladaRoot position. Added test program that moves a model along a path.
- Split COLLADA number array strings on any whitespace, not just spaces.
- Added a test program to load a COLLADA file directly (not part of a KMZ archive).
- Added package document for COLLADA packages.
- Modified MeasureToolController to use the terrain position for rubber-banding rather than the current position, which incorporates shape positions in addition to terrain. Also modified MeasureTool so that the readouts both on-screen and in the position-list panel correspond.
- Modified ScreenCreditController placement factors and enabled it by default.
- Added Bing image layer.
- Fixed NullPointerException thrown while attempting to render a 2525 target graphic without a text string. Also, do not put "null" into a route graphic's text if the text modifier is missing. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-291
- Fixed bug in MIL-STD-2525C Route class. Route was incorrectly applying route highlight attributes to the normal attributes of the control points.
- Highlight a COLLADA model as a single unit.
- Removed quirky logic in BasicDragger that computed a different reference point based on the current altitude. This logic was implemented several places in the WW system and has proven to be problematic because the calculations don't come close to the results computed by the alternative logic.
- Reverted change 578 that was specifying EPSG:4326 but lon/lat ordering, which is incorrect for that CRS. The original specified CRS:84, which has lon/lat ordering.
- Cache results of some computations in ColladaMeshShape.
- Added support for COLLADA bind_vertex_input element.
- Added configuration for DocumentSource to KML parser and double_sided to COLLADA parser. These elements are included in most files produced by SketchUp.
- Added parser configuration for COLLADA elements that are valid COLLADA markup, but are not used by World Wind. This eliminates warnings about unsupported XML tags when parsing some COLLADA files.
- Fix bug that prevented the TacticalGraphicFactory from creating the MIL-STD-2525C Unmanned Aircraft Route graphic.
- Apply KML Model's scale and orientation to COLLADA shape.
- Added support for COLLADA lines primitive.
- Support COLLADA nodes that contain multiple instance_geometry elements.
- Do not apply material to COLLADA shapes when in picking mode.
- Reset ColladaTraversalContext between traversals (fixes bug applying transform matrix). Check to make sure that a COLLADA model has geometry before trying to create a shape for it.
- Do not retrieve network links in inactive regions. Even though KMLNetworkLink extends KMLAbstractContainer, it should behave a feature for purposes of region culling.
- Several small optimizations to COLLADA and KML network link code. Mostly caching results of hash map lookups that are accessed many times while rendering a deep tree of network links.
- Augmented documentation for WorldWindow.getGpuResourceCache() to indicate that it should not be called by applications.
- Modified ColladaTriangleMesh to render all the Triangles elements in a COLLADA node instead of just one. This allows batch rendering to work correctly, and eliminates unnecessary GL state switching.
- Compute extent of COLLADA model for view frustum culling. Extent calculation does not yet handle nodes that are rendered multiple times with different transforms.
- Changed KMLUtil.getPositions to handle Model geometry.
- Added test files for COLLADA models.
- Added flag to override horizon clipping in PointPlacemark.
- Use one ordered renderable to draw an entire COLLADA node, instead of one for each shape within the node. Some COLLADA models render the same geometry multiple times with different materials, and treating each piece as its own ordered renderable was causing z-fighting.
- Support loading COLLADA from a URL.
- Moved EntityMap to core to remove core dependency on worldwindx package.
- Added support for resource maps in KML models.
- ColladaTriangleMesh now creates a new object to represent the mesh as an ordered renderable. We can't add the mesh itself to the ordered renderable queue because the same mesh may be rendered multiple times with different transform matrices.
- Partial handling of materials defined in COLLADA models.
- Moved parallel path generator methods to WWMath.
- Added equality test to TiledImageLayer's test for expired texture tiles.
- Made TextureTile.updateTime atomic.
- Improved how COLLADA code finds the texture for an effect. Now actually uses the shader element to locate the texture instead of just grabbing the texture from the param element.
- Added COLLADA schema (version 1.4.1).
- Convert units COLLADA doc to meters.
- Added support for transform matrices in COLLADA nodes.
- More work on COLLADA textures. Resources with a KMZ are now resolved correctly.
- Added CacheLocationConfiguration example.
- Made the renderables collection in RenderableLayer protected rather than private.
- Apply textures to COLLADA models.
- Fixed bug that caused TacticalSymbol text to not change opacity when the symbol opacity changed. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-287.
- Fixed a bug which can cause a TacticalSymbol to incorrectly reuse a partially complete cached layout, causing the symbol to render without a graphic modifier that was not available when the layout was computed. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-286.
- Changed border on Alternative Operational Condition modifiers from white to black. Black looks better, and matches the examples images in MIL-STD-2525C (pg 19).
- Draw 2525 Alternate Operational Condition when the SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE modifier is set. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-285.
- Handle resolution and rendering of COLLADA instance_nodes. Added generic class ColladaLibrary that handles all COLLADA library elements.
- Added method to read data from a COLLADA accessor into a Buffer.
- Repaired DTED reader per forum post http://forum.worldwindcentral.com/showthread.php?t=32856
- Added classes for parsing Collada files.
- Marked SymbologyConstants.SHOW_LOCATION as deprecated. Use TacticalSymbol.setShowLocation instead.
- Performance improvement in TacticalGraphicLabel: Cache the bounds of each line of text. Computing text bounds is expensive, and only needs to happen when the text or text size changes.
- Modified Sector.splitBoundingSectors to handle the edge case where the radius of the circle is equal to one quarter of the globe radius. (tan(r) is not defined in this case.)
- Modified Sector.computeBoundingBox to compute the correct bounding box for a sector that  spans 360 degrees of longitude. This resolves problem picking surface shapes that cover a pole.
- Fixed problem in AbstractSurfaceShape: bounding sector was not computed correctly for a polygon that enclosed a pole.
- Changed how Sector.splitBoundingSector(LatLon,double,Globe) computes min and max longitude. The previous method did not compute the correct bounding sector when the circle was close to a pole.
- Corrected documentation on SurfaceImageLayer.addImage. The error was reported forum: http://forum.worldwindcentral.com/showthread.php?t=32836
- Use StringBuilder instead of StringBuffer in Sector.toString. This object is only used by one thread, so there's no reason to use a StringBuffer.
- Fixed rendering of surface shapes that cover either the North or South pole.
- Reapply: Disable writes to depth buffer in AbstractShape when the shape is transparent.
- Disable writes to depth buffer in AbstractShape when the shape is transparent.
- Commented out Path optimization that was causing Paths' eye distance not to be updated.
- Updated address of USGS NAIP.
- Corrected globe-based calculation of icon point to account for the elevation.
- Reduced the test for equality when checking WMS server names.


New features and improvements in World Wind Java SDK 1.3.0 - April 27, 2012
------------------------------------------------------------

- 2525C Symbology
- KML NetworkLinkControl and Update


New features and improvements in World Wind Java SDK 1.2.0 - July 19, 2011
------------------------------------------------------------

- KML file parsing and display.
- Improved Shapefile parsing and display performance.
- GeoJSON file parsing and display.
- Vector Product Format (VPF) database parsing and display

- Web-browser balloons on Mac OS X and Windows that display HTML5 and JavaScript content.
- 3D polygon and extruded polygon shapes.
- 3D rigid shapes: ellipsoid, wedge, cylinder, cone, pyramid, box.
- Improved 3D path/polyline shape.
- Editors for 3D polygon and extruded polygon shapes.
- Editors for 3D rigid shapes.
- Line of sight intersections with 3D shapes and high-resolution terrain.
- Improved on-screen layer manager. Integrated with KML file parsing and display.

- Image and elevation data import using GDAL.
  - Adds support for most common image and elevation data formats.
  - Reduces the hard drive footprint and time required to import large datasets.

- Bulk download / cache building support for surface image layers, place name layer, and elevation models.
- Screen credit support for surface image layers.
- New high resolution topographic maps for the United States.
- High resolution NAIP imagery for the United States.
- Improved image layer rendering performance.
- Improved XML parsing performance.

- WorldWindow application framework.
- Simplified project structure and build scripts.
- Applications and examples separated into a new JAR file: worldwindx.jar.
- WMS server integrated into the World Wind project.
