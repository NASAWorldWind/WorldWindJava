/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import com.jogamp.opengl.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * TiledImageLayer modified 2009-02-03 to add support for Mercator projections.
 *
 * @author tag
 * @version $Id: MercatorTiledImageLayer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public abstract class MercatorTiledImageLayer extends AbstractLayer
{
    // Infrastructure
    private static final LevelComparer levelComparer = new LevelComparer();
    private final LevelSet levels;
    private ArrayList<MercatorTextureTile> topLevels;
    private boolean forceLevelZeroLoads = false;
    private boolean levelZeroLoaded = false;
    private boolean retainLevelZeroTiles = false;
    private String tileCountName;
    @SuppressWarnings({"FieldCanBeLocal"})
    private double splitScale = 0.9; // TODO: Make configurable
    private boolean useMipMaps = false;
    private ArrayList<String> supportedImageFormats = new ArrayList<String>();

    // Diagnostic flags
    private boolean showImageTileOutlines = false;
    private boolean drawTileBoundaries = false;
    private boolean useTransparentTextures = false;
    private boolean drawTileIDs = false;
    private boolean drawBoundingVolumes = false;

    // Stuff computed each frame
    private ArrayList<MercatorTextureTile> currentTiles = new ArrayList<MercatorTextureTile>();
    private MercatorTextureTile currentResourceTile;
    private Vec4 referencePoint;
    private boolean atMaxResolution = false;
    private PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(
        200);

    abstract protected void requestTexture(DrawContext dc, MercatorTextureTile tile);

    abstract protected void forceTextureLoad(MercatorTextureTile tile);

    public MercatorTiledImageLayer(LevelSet levelSet)
    {
        if (levelSet == null)
        {
            String message = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.

        this.createTopLevelTiles();

        this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
        this.tileCountName = this.getName() + " Tiles";
    }

    @Override
    public void setName(String name)
    {
        super.setName(name);
        this.tileCountName = this.getName() + " Tiles";
    }

    public boolean isUseTransparentTextures()
    {
        return this.useTransparentTextures;
    }

    public void setUseTransparentTextures(boolean useTransparentTextures)
    {
        this.useTransparentTextures = useTransparentTextures;
    }

    public boolean isForceLevelZeroLoads()
    {
        return this.forceLevelZeroLoads;
    }

    public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
    {
        this.forceLevelZeroLoads = forceLevelZeroLoads;
    }

    public boolean isRetainLevelZeroTiles()
    {
        return retainLevelZeroTiles;
    }

    public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
    {
        this.retainLevelZeroTiles = retainLevelZeroTiles;
    }

    public boolean isDrawTileIDs()
    {
        return drawTileIDs;
    }

    public void setDrawTileIDs(boolean drawTileIDs)
    {
        this.drawTileIDs = drawTileIDs;
    }

    public boolean isDrawTileBoundaries()
    {
        return drawTileBoundaries;
    }

    public void setDrawTileBoundaries(boolean drawTileBoundaries)
    {
        this.drawTileBoundaries = drawTileBoundaries;
    }

    public boolean isShowImageTileOutlines()
    {
        return showImageTileOutlines;
    }

    public void setShowImageTileOutlines(boolean showImageTileOutlines)
    {
        this.showImageTileOutlines = showImageTileOutlines;
    }

    public boolean isDrawBoundingVolumes()
    {
        return drawBoundingVolumes;
    }

    public void setDrawBoundingVolumes(boolean drawBoundingVolumes)
    {
        this.drawBoundingVolumes = drawBoundingVolumes;
    }

    protected LevelSet getLevels()
    {
        return levels;
    }

    protected PriorityBlockingQueue<Runnable> getRequestQ()
    {
        return requestQ;
    }

    public boolean isMultiResolution()
    {
        return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
    }

    public boolean isAtMaxResolution()
    {
        return this.atMaxResolution;
    }

    public boolean isUseMipMaps()
    {
        return useMipMaps;
    }

    public void setUseMipMaps(boolean useMipMaps)
    {
        this.useMipMaps = useMipMaps;
    }

    private void createTopLevelTiles()
    {
        MercatorSector sector = (MercatorSector) this.levels.getSector();

        Level level = levels.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();

        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        // Determine the row and column offset from the common World Wind global tiling origin.
        int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
        int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
        int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
        int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        this.topLevels = new ArrayList<MercatorTextureTile>(nLatTiles
            * nLonTiles);

        //Angle p1 = Tile.computeRowLatitude(firstRow, dLat);
        double deltaLat = dLat.degrees / 90;
        double d1 = -1.0 + deltaLat * firstRow;
        for (int row = firstRow; row <= lastRow; row++)
        {
            //Angle p2;
            //p2 = p1.add(dLat);
            double d2 = d1 + deltaLat;

            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
            for (int col = firstCol; col <= lastCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);

                this.topLevels.add(new MercatorTextureTile(new MercatorSector(
                    d1, d2, t1, t2), level, row, col));
                t1 = t2;
            }
            d1 = d2;
        }
    }

    private void loadAllTopLevelTextures(DrawContext dc)
    {
        for (MercatorTextureTile tile : this.topLevels)
        {
            if (!tile.isTextureInMemory(dc.getTextureCache()))
                this.forceTextureLoad(tile);
        }

        this.levelZeroLoaded = true;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    private void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();

        for (MercatorTextureTile tile : this.topLevels)
        {
            if (this.isTileVisible(dc, tile))
            {
                this.currentResourceTile = null;
                this.addTileOrDescendants(dc, tile);
            }
        }
    }

    private void addTileOrDescendants(DrawContext dc, MercatorTextureTile tile)
    {
        if (this.meetsRenderCriteria(dc, tile))
        {
            this.addTile(dc, tile);
            return;
        }

        // The incoming tile does not meet the rendering criteria, so it must be subdivided and those
        // subdivisions tested against the criteria.

        // All tiles that meet the selection criteria are drawn, but some of those tiles will not have
        // textures associated with them either because their texture isn't loaded yet or because they
        // are finer grain than the layer has textures for. In these cases the tiles use the texture of
        // the closest ancestor that has a texture loaded. This ancestor is called the currentResourceTile.
        // A texture transform is applied during rendering to align the sector's texture coordinates with the
        // appropriate region of the ancestor's texture.

        MercatorTextureTile ancestorResource = null;

        try
        {
            // TODO: Revise this to reflect that the parent layer is only requested while the algorithm continues
            // to search for the layer matching the criteria.
            // At this point the tile does not meet the render criteria but it may have its texture in memory.
            // If so, register this tile as the resource tile. If not, then this tile will be the next level
            // below a tile with texture in memory. So to provide progressive resolution increase, add this tile
            // to the draw list. That will cause the tile to be drawn using its parent tile's texture, and it will
            // cause it's texture to be requested. At some future call to this method the tile's texture will be in
            // memory, it will not meet the render criteria, but will serve as the parent to a tile that goes
            // through this same process as this method recurses. The result of all this is that a tile isn't rendered
            // with its own texture unless all its parents have their textures loaded. In addition to causing
            // progressive resolution increase, this ensures that the parents are available as the user zooms out, and
            // therefore the layer remains visible until the user is zoomed out to the point the layer is no longer
            // active.
            if (tile.isTextureInMemory(dc.getTextureCache())
                || tile.getLevelNumber() == 0)
            {
                ancestorResource = this.currentResourceTile;
                this.currentResourceTile = tile;
            }
            else if (!tile.getLevel().isEmpty())
            {
                //                this.addTile(dc, tile);
                //                return;

                // Issue a request for the parent before descending to the children.
                if (tile.getLevelNumber() < this.levels.getNumLevels())
                {
                    // Request only tiles with data associated at this level
                    if (!this.levels.isResourceAbsent(tile))
                        this.requestTexture(dc, tile);
                }
            }

            MercatorTextureTile[] subTiles = tile.createSubTiles(this.levels
                .getLevel(tile.getLevelNumber() + 1));
            for (MercatorTextureTile child : subTiles)
            {
                if (this.isTileVisible(dc, child))
                    this.addTileOrDescendants(dc, child);
            }
        }
        finally
        {
            if (ancestorResource != null) // Pop this tile as the currentResource ancestor
                this.currentResourceTile = ancestorResource;
        }
    }

    private void addTile(DrawContext dc, MercatorTextureTile tile)
    {
        tile.setFallbackTile(null);

        if (tile.isTextureInMemory(dc.getTextureCache()))
        {
            //            System.out.printf("Sector %s, min = %f, max = %f\n", tile.getSector(),
            //                dc.getGlobe().getMinElevation(tile.getSector()), dc.getGlobe().getMaxElevation(tile.getSector()));
            this.addTileToCurrent(tile);
            return;
        }

        // Level 0 loads may be forced
        if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads
            && !tile.isTextureInMemory(dc.getTextureCache()))
        {
            this.forceTextureLoad(tile);
            if (tile.isTextureInMemory(dc.getTextureCache()))
            {
                this.addTileToCurrent(tile);
                return;
            }
        }

        // Tile's texture isn't available, so request it
        if (tile.getLevelNumber() < this.levels.getNumLevels())
        {
            // Request only tiles with data associated at this level
            if (!this.levels.isResourceAbsent(tile))
                this.requestTexture(dc, tile);
        }

        // Set up to use the currentResource tile's texture
        if (this.currentResourceTile != null)
        {
            if (this.currentResourceTile.getLevelNumber() == 0
                && this.forceLevelZeroLoads
                && !this.currentResourceTile.isTextureInMemory(dc
                .getTextureCache())
                && !this.currentResourceTile.isTextureInMemory(dc
                .getTextureCache()))
                this.forceTextureLoad(this.currentResourceTile);

            if (this.currentResourceTile
                .isTextureInMemory(dc.getTextureCache()))
            {
                tile.setFallbackTile(currentResourceTile);
                this.addTileToCurrent(tile);
            }
        }
    }

    private void addTileToCurrent(MercatorTextureTile tile)
    {
        this.currentTiles.add(tile);
    }

    private boolean isTileVisible(DrawContext dc, MercatorTextureTile tile)
    {
        //        if (!(tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
        //            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()))))
        //            return false;
        //
        //        Position eyePos = dc.getView().getEyePosition();
        //        LatLon centroid = tile.getSector().getCentroid();
        //        Angle d = LatLon.greatCircleDistance(eyePos.getLatLon(), centroid);
        //        if ((!tile.getLevelName().equals("0")) && d.compareTo(tile.getSector().getDeltaLat().multiply(2.5)) == 1)
        //            return false;
        //
        //        return true;
        //
        return tile.getExtent(dc).intersects(
            dc.getView().getFrustumInModelCoordinates())
            && (dc.getVisibleSector() == null || dc.getVisibleSector()
            .intersects(tile.getSector()));
    }

    //
    //    private boolean meetsRenderCriteria2(DrawContext dc, TextureTile tile)
    //    {
    //        if (this.levels.isFinalLevel(tile.getLevelNumber()))
    //            return true;
    //
    //        Sector sector = tile.getSector();
    //        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe());
    //        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe());
    //
    //        View view = dc.getView();
    //        double d1 = view.getEyePoint().distanceTo3(corners[0]);
    //        double d2 = view.getEyePoint().distanceTo3(corners[1]);
    //        double d3 = view.getEyePoint().distanceTo3(corners[2]);
    //        double d4 = view.getEyePoint().distanceTo3(corners[3]);
    //        double d5 = view.getEyePoint().distanceTo3(centerPoint);
    //
    //        double minDistance = d1;
    //        if (d2 < minDistance)
    //            minDistance = d2;
    //        if (d3 < minDistance)
    //            minDistance = d3;
    //        if (d4 < minDistance)
    //            minDistance = d4;
    //        if (d5 < minDistance)
    //            minDistance = d5;
    //
    //        double r = 0;
    //        if (minDistance == d1)
    //            r = corners[0].getLength3();
    //        if (minDistance == d2)
    //            r = corners[1].getLength3();
    //        if (minDistance == d3)
    //            r = corners[2].getLength3();
    //        if (minDistance == d4)
    //            r = corners[3].getLength3();
    //        if (minDistance == d5)
    //            r = centerPoint.getLength3();
    //
    //        double texelSize = tile.getLevel().getTexelSize(r);
    //        double pixelSize = dc.getView().computePixelSizeAtDistance(minDistance);
    //
    //        return 2 * pixelSize >= texelSize;
    //    }

    private boolean meetsRenderCriteria(DrawContext dc, MercatorTextureTile tile)
    {
        return this.levels.isFinalLevel(tile.getLevelNumber())
            || !needToSplit(dc, tile.getSector());
    }

    private boolean needToSplit(DrawContext dc, Sector sector)
    {
        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        View view = dc.getView();
        double d1 = view.getEyePoint().distanceTo3(corners[0]);
        double d2 = view.getEyePoint().distanceTo3(corners[1]);
        double d3 = view.getEyePoint().distanceTo3(corners[2]);
        double d4 = view.getEyePoint().distanceTo3(corners[3]);
        double d5 = view.getEyePoint().distanceTo3(centerPoint);

        double minDistance = d1;
        if (d2 < minDistance)
            minDistance = d2;
        if (d3 < minDistance)
            minDistance = d3;
        if (d4 < minDistance)
            minDistance = d4;
        if (d5 < minDistance)
            minDistance = d5;

        double cellSize = (Math.PI * sector.getDeltaLatRadians() * dc
            .getGlobe().getRadius()) / 20; // TODO

        return !(Math.log10(cellSize) <= (Math.log10(minDistance) - this.splitScale));
    }

    private boolean atMaxLevel(DrawContext dc)
    {
        Position vpc = dc.getViewportCenterPosition();
        if (dc.getView() == null || this.getLevels() == null || vpc == null)
            return false;

        if (!this.getLevels().getSector().contains(vpc.getLatitude(),
            vpc.getLongitude()))
            return true;

        Level nextToLast = this.getLevels().getNextToLastLevel();
        if (nextToLast == null)
            return true;

        Sector centerSector = nextToLast.computeSectorForPosition(vpc.getLatitude(), vpc.getLongitude(),
            this.getLevels().getTileOrigin());
        return this.needToSplit(dc, centerSector);
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    @Override
    public void render(DrawContext dc)
    {
        this.atMaxResolution = this.atMaxLevel(dc);
        super.render(dc);
    }

    @Override
    protected final void doRender(DrawContext dc)
    {
        if (this.forceLevelZeroLoads && !this.levelZeroLoaded)
            this.loadAllTopLevelTextures(dc);
        if (dc.getSurfaceGeometry() == null
            || dc.getSurfaceGeometry().size() < 1)
            return;

        dc.getGeographicSurfaceTileRenderer().setShowImageTileOutlines(
            this.showImageTileOutlines);

        draw(dc);
    }

    private void draw(DrawContext dc)
    {
        this.referencePoint = this.computeReferencePoint(dc);

        this.assembleTiles(dc); // Determine the tiles to draw.

        if (this.currentTiles.size() >= 1)
        {
            MercatorTextureTile[] sortedTiles = new MercatorTextureTile[this.currentTiles
                .size()];
            sortedTiles = this.currentTiles.toArray(sortedTiles);
            Arrays.sort(sortedTiles, levelComparer);

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (this.isUseTransparentTextures() || this.getOpacity() < 1)
            {
                gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT
                    | GL2.GL_CURRENT_BIT);
                gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            }
            else
            {
                gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT);
            }

            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT,
                this.tileCountName, this.currentTiles.size());
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc,
                this.currentTiles);

            gl.glPopAttrib();

            if (this.drawTileIDs)
                this.drawTileIDs(dc, this.currentTiles);

            if (this.drawBoundingVolumes)
                this.drawBoundingVolumes(dc, this.currentTiles);

            this.currentTiles.clear();
        }

        this.sendRequests();
        this.requestQ.clear();
    }

    private void sendRequests()
    {
        Runnable task = this.requestQ.poll();
        while (task != null)
        {
            if (!WorldWind.getTaskService().isFull())
            {
                WorldWind.getTaskService().addTask(task);
            }
            task = this.requestQ.poll();
        }
    }

    public boolean isLayerInView(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (dc.getView() == null)
        {
            String message = Logging
                .getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return !(dc.getVisibleSector() != null && !this.levels.getSector()
            .intersects(dc.getVisibleSector()));
    }

    private Vec4 computeReferencePoint(DrawContext dc)
    {
        if (dc.getViewportCenterPosition() != null)
            return dc.getGlobe().computePointFromPosition(
                dc.getViewportCenterPosition());

        java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
        int x = (int) viewport.getWidth() / 2;
        for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
        {
            Position pos = dc.getView().computePositionFromScreenPoint(x, y);
            if (pos == null)
                continue;

            return dc.getGlobe().computePointFromPosition(pos.getLatitude(),
                pos.getLongitude(), 0d);
        }

        return null;
    }

    protected Vec4 getReferencePoint()
    {
        return this.referencePoint;
    }

    private static class LevelComparer implements
        Comparator<MercatorTextureTile>
    {
        public int compare(MercatorTextureTile ta, MercatorTextureTile tb)
        {
            int la = ta.getFallbackTile() == null ? ta.getLevelNumber() : ta
                .getFallbackTile().getLevelNumber();
            int lb = tb.getFallbackTile() == null ? tb.getLevelNumber() : tb
                .getFallbackTile().getLevelNumber();

            return la < lb ? -1 : la == lb ? 0 : 1;
        }
    }

    private void drawTileIDs(DrawContext dc,
        ArrayList<MercatorTextureTile> tiles)
    {
        java.awt.Rectangle viewport = dc.getView().getViewport();
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            java.awt.Font.decode("Arial-Plain-13"));

        dc.getGL().glDisable(GL.GL_DEPTH_TEST);
        dc.getGL().glDisable(GL.GL_BLEND);
        dc.getGL().glDisable(GL.GL_TEXTURE_2D);

        textRenderer.setColor(java.awt.Color.YELLOW);
        textRenderer.beginRendering(viewport.width, viewport.height);
        for (MercatorTextureTile tile : tiles)
        {
            String tileLabel = tile.getLabel();

            if (tile.getFallbackTile() != null)
                tileLabel += "/" + tile.getFallbackTile().getLabel();

            LatLon ll = tile.getSector().getCentroid();
            Vec4 pt = dc.getGlobe().computePointFromPosition(
                ll.getLatitude(),
                ll.getLongitude(),
                dc.getGlobe().getElevation(ll.getLatitude(),
                    ll.getLongitude()));
            pt = dc.getView().project(pt);
            textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
        }
        textRenderer.endRendering();
    }

    private void drawBoundingVolumes(DrawContext dc,
        ArrayList<MercatorTextureTile> tiles)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        float[] previousColor = new float[4];
        gl.glGetFloatv(GL2.GL_CURRENT_COLOR, previousColor, 0);
        gl.glColor3d(0, 1, 0);

        for (MercatorTextureTile tile : tiles)
        {
            ((Cylinder) tile.getExtent(dc)).render(dc);
        }

        Cylinder c = Sector.computeBoundingCylinder(dc.getGlobe(), dc.getVerticalExaggeration(),
            this.levels.getSector());
        gl.glColor3d(1, 1, 0);
        c.render(dc);

        gl.glColor4fv(previousColor, 0);
    }

    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //

    public List<String> getAvailableImageFormats()
    {
        return new ArrayList<String>(this.supportedImageFormats);
    }

    public boolean isImageFormatAvailable(String imageFormat)
    {
        return imageFormat != null
            && this.supportedImageFormats.contains(imageFormat);
    }

    public String getDefaultImageFormat()
    {
        return this.supportedImageFormats.size() > 0 ? this.supportedImageFormats
            .get(0)
            : null;
    }

    protected void setAvailableImageFormats(String[] formats)
    {
        this.supportedImageFormats.clear();

        if (formats != null)
        {
            this.supportedImageFormats.addAll(Arrays.asList(formats));
        }
    }

    private BufferedImage requestImage(MercatorTextureTile tile, String mimeType)
        throws URISyntaxException
    {
        String pathBase = tile.getPath().substring(0,
            tile.getPath().lastIndexOf("."));
        String suffix = WWIO.makeSuffixForMimeType(mimeType);
        String path = pathBase + suffix;
        URL url = this.getDataFileStore().findFile(path, false);

        if (url == null) // image is not local
            return null;

        if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
        {
            // The file has expired. Delete it.
            this.getDataFileStore().removeFile(url);
            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.logger().fine(message);
        }
        else
        {
            try
            {
                File imageFile = new File(url.toURI());
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null)
                {
                    String message = Logging.getMessage(
                        "generic.ImageReadFailed", imageFile);
                    throw new RuntimeException(message);
                }

                this.levels.unmarkResourceAbsent(tile);
                return image;
            }
            catch (IOException e)
            {
                // Assume that something's wrong with the file and delete it.
                this.getDataFileStore().removeFile(url);
                this.levels.markResourceAbsent(tile);
                String message = Logging.getMessage(
                    "generic.DeletedCorruptDataFile", url);
                Logging.logger().info(message);
            }
        }

        return null;
    }

    private void downloadImage(final MercatorTextureTile tile, String mimeType)
        throws Exception
    {
        //        System.out.println(tile.getPath());
        final URL resourceURL = tile.getResourceURL(mimeType);
        Retriever retriever;

        String protocol = resourceURL.getProtocol();

        if ("http".equalsIgnoreCase(protocol))
        {
            retriever = new HTTPRetriever(resourceURL, new HttpRetrievalPostProcessor(tile));
            retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy layers
        }
        else
        {
            String message = Logging
                .getMessage("layers.TextureLayer.UnknownRetrievalProtocol",
                    resourceURL);
            throw new RuntimeException(message);
        }

        retriever.setConnectTimeout(10000);
        retriever.setReadTimeout(20000);
        retriever.call();
    }

    public int computeLevelForResolution(Sector sector, Globe globe,
        double resolution)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        double texelSize = 0;
        Level targetLevel = this.levels.getLastLevel();
        for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (this.levels.isLevelEmpty(i))
                continue;

            texelSize = this.levels.getLevel(i).getTexelSize();
            if (texelSize > resolution)
                continue;

            targetLevel = this.levels.getLevel(i);
            break;
        }

        Logging.logger().info(
            Logging.getMessage("layers.TiledImageLayer.LevelSelection",
                targetLevel.getLevelNumber(), texelSize));
        return targetLevel.getLevelNumber();
    }

    public BufferedImage composeImageForSector(Sector sector, int imageWidth,
        int imageHeight, int levelNumber, String mimeType,
        boolean abortOnError, BufferedImage image)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (levelNumber < 0)
        {
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }
        else if (levelNumber > this.levels.getLastLevel().getLevelNumber())
        {
            Logging.logger().warning(
                Logging.getMessage(
                    "generic.LevelRequestedGreaterThanMaxLevel",
                    levelNumber, this.levels.getLastLevel()
                    .getLevelNumber()));
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }

        MercatorTextureTile[][] tiles = this.getTilesInSector(sector,
            levelNumber);

        if (tiles.length == 0 || tiles[0].length == 0)
        {
            Logging
                .logger()
                .severe(
                    Logging
                        .getMessage("layers.TiledImageLayer.NoImagesAvailable"));
            return null;
        }

        if (image == null)
            image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();

        for (MercatorTextureTile[] row : tiles)
        {
            for (MercatorTextureTile tile : row)
            {
                if (tile == null)
                    continue;

                BufferedImage tileImage;
                try
                {
                    tileImage = this.getImage(tile, mimeType);

                    double sh = ((double) imageHeight / (double) tileImage
                        .getHeight())
                        * (tile.getSector().getDeltaLat().divide(sector
                        .getDeltaLat()));
                    double sw = ((double) imageWidth / (double) tileImage
                        .getWidth())
                        * (tile.getSector().getDeltaLon().divide(sector
                        .getDeltaLon()));

                    double dh = imageHeight
                        * (-tile.getSector().getMaxLatitude().subtract(
                        sector.getMaxLatitude()).degrees / sector
                        .getDeltaLat().degrees);
                    double dw = imageWidth
                        * (tile.getSector().getMinLongitude().subtract(
                        sector.getMinLongitude()).degrees / sector
                        .getDeltaLon().degrees);

                    AffineTransform txf = g.getTransform();
                    g.translate(dw, dh);
                    g.scale(sw, sh);
                    g.drawImage(tileImage, 0, 0, null);
                    g.setTransform(txf);
                }
                catch (Exception e)
                {
                    if (abortOnError)
                        throw new RuntimeException(e);

                    String message = Logging.getMessage(
                        "generic.ExceptionWhileRequestingImage", tile
                        .getPath());
                    Logging.logger().log(java.util.logging.Level.WARNING,
                        message, e);
                }
            }
        }

        return image;
    }

    public int countImagesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel()
                .getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector
            .getMaxLatitude(), latOrigin);
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
            .getMinLongitude(), lonOrigin);
        final int seRow = Tile.computeRow(delta.getLatitude(), sector
            .getMinLatitude(), latOrigin);
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector
            .getMaxLongitude(), lonOrigin);

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;

        return numRows * numCols;
    }

    private MercatorTextureTile[][] getTilesInSector(Sector sector,
        int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel()
                .getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector
            .getMaxLatitude(), latOrigin);
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
            .getMinLongitude(), lonOrigin);
        final int seRow = Tile.computeRow(delta.getLatitude(), sector
            .getMinLatitude(), latOrigin);
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector
            .getMaxLongitude(), lonOrigin);

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;
        MercatorTextureTile[][] sectorTiles = new MercatorTextureTile[numRows][numCols];

        for (int row = nwRow; row >= seRow; row--)
        {
            for (int col = nwCol; col <= seCol; col++)
            {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row,
                    col, targetLevel.getCacheName());
                Sector tileSector = this.levels.computeSectorForKey(key);
                MercatorSector mSector = MercatorSector.fromSector(tileSector); //TODO: check
                sectorTiles[nwRow - row][col - nwCol] = new MercatorTextureTile(
                    mSector, targetLevel, row, col);
            }
        }

        return sectorTiles;
    }

    private BufferedImage getImage(MercatorTextureTile tile, String mimeType)
        throws Exception
    {
        // Read the image from disk.
        BufferedImage image = this.requestImage(tile, mimeType);
        if (image != null)
            return image;

        // Retrieve it from the net since it's not on disk.
        this.downloadImage(tile, mimeType);

        // Try to read from disk again after retrieving it from the net.
        image = this.requestImage(tile, mimeType);
        if (image == null)
        {
            String message = Logging.getMessage(
                "layers.TiledImageLayer.ImageUnavailable", tile.getPath());
            throw new RuntimeException(message);
        }

        return image;
    }

    private class HttpRetrievalPostProcessor implements RetrievalPostProcessor
    {
        private MercatorTextureTile tile;

        public HttpRetrievalPostProcessor(MercatorTextureTile tile)
        {
            this.tile = tile;
        }

        public ByteBuffer run(Retriever retriever)
        {
            if (!retriever.getState().equals(
                Retriever.RETRIEVER_STATE_SUCCESSFUL))
                return null;

            HTTPRetriever htr = (HTTPRetriever) retriever;
            if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
            {
                // Mark tile as missing to avoid excessive attempts
                MercatorTiledImageLayer.this.levels.markResourceAbsent(tile);
                return null;
            }

            if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            URLRetriever r = (URLRetriever) retriever;
            ByteBuffer buffer = r.getBuffer();

            String suffix = WWIO.makeSuffixForMimeType(htr.getContentType());
            if (suffix == null)
            {
                return null; // TODO: log error
            }

            String path = tile.getPath().substring(0,
                tile.getPath().lastIndexOf("."));
            path += suffix;

            final File outFile = getDataFileStore().newFile(path);
            if (outFile == null)
                return null;

            try
            {
                WWIO.saveBuffer(buffer, outFile);
                return buffer;
            }
            catch (IOException e)
            {
                e.printStackTrace(); // TODO: log error
                return null;
            }
        }
    }
}
