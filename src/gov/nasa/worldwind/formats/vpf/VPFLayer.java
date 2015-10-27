/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.beans.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Renders elements from a VPF database.
 *
 * @author Patrick Murris
 * @version $Id: VPFLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFLayer extends AbstractLayer
{
    public static final String LIBRARY_CHANGED = "VPFLayer.LibraryChanged";
    public static final String COVERAGE_CHANGED = "VPFLayer.CoverageChanged";

    // Reference
    protected VPFDatabase db;
    protected ArrayList<VPFLibraryRenderable> libraries;

    // Renderables
    protected double drawDistance = 1e6;
    protected int maxTilesToDraw = 4;
    protected boolean drawTileExtents = false;
    protected ArrayList<VPFSymbol> symbols = new ArrayList<VPFSymbol>();
    protected ArrayList<GeographicText> textObjects = new ArrayList<GeographicText>();
    protected ArrayList<Renderable> renderableObjects = new ArrayList<Renderable>();

    // Renderers
    protected GeographicTextRenderer textRenderer = new GeographicTextRenderer();
    protected VPFSymbolSupport symbolSupport = new VPFSymbolSupport(GeoSymConstants.GEOSYM, "image/png");

    // Threaded requests
    protected Queue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(4);
    protected Queue<Disposable> disposalQ = new ConcurrentLinkedQueue<Disposable>();

    // --- Inner classes ----------------------------------------------------------------------

    protected static final VPFTile NULL_TILE = new VPFTile(-1, "NullTile", new VPFBoundingBox(0, 0, 0, 0));

    protected static class VPFLibraryRenderable
    {
        protected boolean enabled = false;
        protected VPFLayer layer;
        protected VPFLibrary library;
        protected VPFCoverageRenderable referenceCoverage;
        protected ArrayList<VPFCoverageRenderable> coverages = new ArrayList<VPFCoverageRenderable>();
        protected ArrayList<VPFTile> currentTiles = new ArrayList<VPFTile>();

        public VPFLibraryRenderable(VPFLayer layer, VPFLibrary library)
        {
            this.layer = layer;
            this.library = library;

            for (VPFCoverage cov : this.library.getCoverages())
            {
                if (cov.getName().equalsIgnoreCase(VPFConstants.LIBRARY_REFERENCE_COVERAGE))
                    this.referenceCoverage = new VPFCoverageRenderable(this.layer, cov);
                else
                    this.coverages.add(new VPFCoverageRenderable(this.layer, cov));
            }

            if (this.referenceCoverage != null)
            {
                this.referenceCoverage.enabled = true;
            }
        }

        public void assembleSymbols(DrawContext dc, double drawDistance, int maxTilesToDraw)
        {
            if (!this.enabled)
                return;

            this.assembleVisibleTiles(dc, drawDistance, maxTilesToDraw);

            if (this.referenceCoverage != null)
            {
                this.referenceCoverage.assembleSymbols(null);
            }

            for (VPFCoverageRenderable cr : this.coverages)
            {
                cr.assembleSymbols((cr.coverage.isTiled() ? this.currentTiles : null));
            }
        }

        public void drawTileExtents(DrawContext dc)
        {
            for (VPFTile tile : this.currentTiles)
            {
                Extent extent = tile.getExtent(dc.getGlobe(), dc.getVerticalExaggeration());
                if (extent instanceof Renderable)
                    ((Renderable) extent).render(dc);
            }
        }

        public void setCoverageEnabled(VPFCoverage coverage, boolean enabled)
        {
            VPFCoverageRenderable cr = this.getCoverageRenderable(coverage);
            if (cr != null)
                cr.enabled = enabled;

            this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
        }

        public VPFCoverageRenderable getCoverageRenderable(VPFCoverage coverage)
        {
            for (VPFCoverageRenderable cr : this.coverages)
            {
                if (cr.coverage.getFilePath().equals(coverage.getFilePath()))
                    return cr;
            }
            return null;
        }

        protected void assembleVisibleTiles(DrawContext dc, double drawDistance, int maxTilesToDraw)
        {
            this.currentTiles.clear();

            if (!this.library.hasTiledCoverages())
                return;

            Frustum frustum = dc.getView().getFrustumInModelCoordinates();
            Vec4 eyePoint = dc.getView().getEyePoint();

            for (VPFTile tile : this.library.getTiles())
            {
                Extent extent = tile.getExtent(dc.getGlobe(), dc.getVerticalExaggeration());
                double d = extent.getCenter().distanceTo3(eyePoint) - extent.getRadius();

                if (d < drawDistance && frustum.intersects(extent))
                    this.currentTiles.add(tile);
            }

            // Trim down list to four closest tiles
            while (this.currentTiles.size() > maxTilesToDraw)
            {
                int idx = -1;
                double maxDistance = 0;
                for (int i = 0; i < this.currentTiles.size(); i++)
                {
                    Extent extent = this.currentTiles.get(i).getExtent(dc.getGlobe(), dc.getVerticalExaggeration());
                    double distance = dc.getView().getEyePoint().distanceTo3(extent.getCenter());
                    if (distance > maxDistance)
                    {
                        maxDistance = distance;
                        idx = i;
                    }
                }
                this.currentTiles.remove(idx);
            }
        }
    }

    protected static class VPFCoverageRenderable
    {
        protected boolean enabled = false;
        protected VPFLayer layer;
        protected VPFCoverage coverage;
        protected Map<VPFTile, VPFSymbolCollection> tileCache;

        public VPFCoverageRenderable(VPFLayer layer, VPFCoverage coverage)
        {
            this.layer = layer;
            this.coverage = coverage;
            this.tileCache = Collections.synchronizedMap(new BoundedHashMap<VPFTile, VPFSymbolCollection>(6, true)
            {
                protected boolean removeEldestEntry(Map.Entry<VPFTile, VPFSymbolCollection> eldest)
                {
                    if (!super.removeEldestEntry(eldest))
                        return false;

                    dispose(eldest.getValue());
                    return true;
                }
            });
        }

        public void assembleSymbols(Iterable<? extends VPFTile> tiles)
        {
            if (!this.enabled)
                return;

            if (tiles == null)
            {
                this.doAssembleSymbols(NULL_TILE);
                return;
            }

            for (VPFTile tile : tiles)
            {
                this.doAssembleSymbols(tile);
            }
        }

        protected void doAssembleSymbols(VPFTile tile)
        {
            VPFSymbolCollection symbolCollection = this.tileCache.get(tile);
            if (symbolCollection != null)
            {
                this.layer.symbols.addAll(symbolCollection.getSymbols());
            }
            else
            {
                this.layer.requestQ.add(new RequestTask(this, tile));
            }
        }

        protected void dispose(VPFSymbolCollection renderInfo)
        {
            this.layer.disposalQ.add(renderInfo);
        }
    }

    protected static class VPFSymbolCollection implements Disposable
    {
        public static final VPFSymbolCollection EMPTY_SYMBOL_COLLECTION = new VPFSymbolCollection(null);

        protected final ArrayList<VPFSymbol> symbols = new ArrayList<VPFSymbol>();

        public VPFSymbolCollection(Collection<? extends VPFSymbol> symbols)
        {
            if (symbols != null)
                this.symbols.addAll(symbols);
        }

        public Collection<VPFSymbol> getSymbols()
        {
            return Collections.unmodifiableCollection(this.symbols);
        }

        public void dispose()
        {
            for (VPFSymbol s : this.symbols)
            {
                if (s == null)
                    continue;

                if (s.getMapObject() instanceof Disposable)
                {
                    ((Disposable) s.getMapObject()).dispose();
                }
            }

            this.symbols.clear();
        }
    }

    protected VPFSymbolCollection loadTileSymbols(VPFCoverage coverage, VPFTile tile)
    {
        VPFPrimitiveDataFactory primitiveDataFactory = new VPFBasicPrimitiveDataFactory(tile);
        VPFPrimitiveData primitiveData = primitiveDataFactory.createPrimitiveData(coverage);

        // The PrimitiveDataFactory returns null when there are no primitive data tables for this coverage tile. We
        // return the constant EMPTY_SYMBOL_COLLECTION to indicate that we have successfully loaded nothing the empty
        // contents of this coverage tile.
        if (primitiveData == null)
        {
            return VPFSymbolCollection.EMPTY_SYMBOL_COLLECTION;
        }

        VPFBasicSymbolFactory symbolFactory = new VPFBasicSymbolFactory(tile, primitiveData);
        symbolFactory.setStyleSupport(this.symbolSupport);

        ArrayList<VPFSymbol> list = new ArrayList<VPFSymbol>();

        // Create coverage renderables for one tile - if tile is null gets all coverage
        VPFFeatureClass[] array = VPFUtils.readFeatureClasses(coverage, new VPFFeatureTableFilter());
        for (VPFFeatureClass cls : array)
        {
            Collection<? extends VPFSymbol> symbols = cls.createFeatureSymbols(symbolFactory);
            if (symbols != null)
                list.addAll(symbols);
        }

        return new VPFSymbolCollection(list);
    }

    protected static class RequestTask implements Runnable, Comparable<RequestTask>
    {
        protected VPFCoverageRenderable coverageRenderable;
        protected VPFTile tile;

        protected RequestTask(VPFCoverageRenderable coverageRenderable, VPFTile tile)
        {
            this.coverageRenderable = coverageRenderable;
            this.tile = tile;
        }

        public void run()
        {
            VPFSymbolCollection symbols = this.coverageRenderable.layer.loadTileSymbols(
                this.coverageRenderable.coverage, (this.tile != NULL_TILE) ? this.tile : null);

            this.coverageRenderable.tileCache.put(this.tile, symbols);
            this.coverageRenderable.layer.firePropertyChange(AVKey.LAYER, null, this.coverageRenderable.layer);
        }

        /**
         * @param that the task to compare
         *
         * @return -1 if <code>this</code> less than <code>that</code>, 1 if greater than, 0 if equal
         *
         * @throws IllegalArgumentException if <code>that</code> is null
         */
        public int compareTo(RequestTask that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            return 0;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RequestTask that = (RequestTask) o;

            if (coverageRenderable != null ? !coverageRenderable.equals(that.coverageRenderable)
                : that.coverageRenderable != null)
                return false;
            //noinspection RedundantIfStatement
            if (tile != null ? !tile.equals(that.tile) : that.tile != null)
                return false;

            return true;
        }

        public int hashCode()
        {
            int result = coverageRenderable != null ? coverageRenderable.hashCode() : 0;
            result = 31 * result + (tile != null ? tile.hashCode() : 0);
            return result;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("coverageRenderable=").append(this.coverageRenderable.coverage.getName());
            sb.append(", tile=").append(this.tile);
            return sb.toString();
        }
    }

    // --- VPF Layer ----------------------------------------------------------------------

    public VPFLayer()
    {
        this(null);
    }

    public VPFLayer(VPFDatabase db)
    {
        this.setName("VPF Layer");
        this.setPickEnabled(false);
        if (db != null)
            this.setVPFDatabase(db);

        this.textRenderer.setCullTextEnabled(true);
        this.textRenderer.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
    }

    public VPFDatabase getVPFDatabase()
    {
        return this.db;
    }

    public void setVPFDatabase(VPFDatabase db)
    {
        this.db = db;
        this.initialize();

        this.db.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getPropertyName().equals(LIBRARY_CHANGED))
                {
                    VPFLibrary library = (VPFLibrary) event.getSource();
                    boolean enabled = (Boolean) event.getNewValue();
                    setLibraryEnabled(library, enabled);
                }
                else if (event.getPropertyName().equals(COVERAGE_CHANGED))
                {
                    VPFCoverage coverage = (VPFCoverage) event.getSource();
                    boolean enabled = (Boolean) event.getNewValue();
                    setCoverageEnabled(coverage, enabled);
                }
            }
        });
    }

    protected void initialize()
    {
        this.libraries = new ArrayList<VPFLibraryRenderable>();

        for (VPFLibrary lib : db.getLibraries())
        {
            this.libraries.add(new VPFLibraryRenderable(this, lib));
        }
    }

    public void setCoverageEnabled(VPFCoverage coverage, boolean enabled)
    {
        for (VPFLibraryRenderable lr : this.libraries)
        {
            lr.setCoverageEnabled(coverage, enabled);
        }
    }

    public void doPreRender(DrawContext dc)
    {
        // Assemble renderables lists
        this.assembleRenderables(dc);
        // Handle object disposal.
        this.handleDisposal();

        // Pre render renderable objects.
        for (Renderable r : this.renderableObjects)
        {
            if (r instanceof PreRenderable)
                ((PreRenderable) r).preRender(dc);
        }
    }

    public void doRender(DrawContext dc)
    {
        for (Renderable r : this.renderableObjects)       // Other renderables
        {
            r.render(dc);
        }

        this.textRenderer.render(dc, this.textObjects);   // Geo text

        if (this.drawTileExtents)
        {
            for (VPFLibraryRenderable lr : this.libraries)
            {
                lr.drawTileExtents(dc);
            }
        }
    }

    public void setLibraryEnabled(VPFLibrary library, boolean enabled)
    {
        VPFLibraryRenderable lr = this.getLibraryRenderable(library);
        if (lr != null)
            lr.enabled = enabled;

        this.firePropertyChange(AVKey.LAYER, null, this);
    }

    public VPFLibraryRenderable getLibraryRenderable(VPFLibrary library)
    {
        for (VPFLibraryRenderable lr : this.libraries)
        {
            if (lr.library.getFilePath().equals(library.getFilePath()))
                return lr;
        }
        return null;
    }

    public Iterable<VPFSymbol> getActiveSymbols()
    {
        return this.symbols;
    }

    protected void assembleRenderables(DrawContext dc)
    {
        this.symbols.clear();
        this.textObjects.clear();
        this.renderableObjects.clear();

        for (VPFLibraryRenderable lr : this.libraries)
        {
            lr.assembleSymbols(dc, this.drawDistance, this.maxTilesToDraw);
        }

        this.sortSymbols(this.symbols);

        // Dispatch renderable according to its class
        for (VPFSymbol symbol : this.symbols)
        {
            if (symbol.getMapObject() instanceof GeographicText)
                this.textObjects.add((GeographicText) symbol.getMapObject());
            else if (symbol.getMapObject() instanceof Renderable)
                this.renderableObjects.add((Renderable) symbol.getMapObject());
        }

        this.sendRequests();
        this.requestQ.clear();
    }

    protected void sortSymbols(List<VPFSymbol> list)
    {
        Collections.sort(list, new VPFSymbolComparator());
    }

    protected void handleDisposal()
    {
        Disposable disposable;
        while ((disposable = this.disposalQ.poll()) != null)
        {
            disposable.dispose();
        }
    }

    protected void sendRequests()
    {
        Runnable task;
        while ((task = this.requestQ.poll()) != null)
        {
            if (!WorldWind.getTaskService().isFull())
            {
                WorldWind.getTaskService().addTask(task);
            }
        }
    }
}
