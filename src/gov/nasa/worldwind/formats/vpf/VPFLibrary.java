/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.*;

import java.io.File;
import java.util.*;
import java.util.regex.*;

/**
 * DIGEST Part 2, Annex C.2.2.2.4 and C.2.3.5
 *
 * @author dcollins
 * @version $Id: VPFLibrary.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFLibrary extends AVListImpl
{
    private VPFDatabase database;
    private VPFBoundingBox bounds;
    private Map<String, VPFCoverage> coverageMap = new HashMap<String, VPFCoverage>();
    private Map<Integer, VPFTile> tileMap = new HashMap<Integer, VPFTile>();
    private VPFTile[] tiles;
    private VPFBufferedRecordData libraryHeaderTable;
    private VPFBufferedRecordData coverageAttributeTable;
    private VPFBufferedRecordData geographicReferenceTable;

    protected VPFLibrary(VPFDatabase database)
    {
        if (database == null)
        {
            String message = Logging.getMessage("nullValue.DatabaseIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.database = database;
    }

    /**
     * Constructs a VPF Library from the specified VPF Database and library name. This initializes the Library Header
     * Table, the Coverage Attribute Table, and the Geographic Reference Table.
     *
     * @param database the Database which the Library resides in.
     * @param name     the Library's name.
     *
     * @return a new Library from the specified Database with the specified name.
     *
     * @throws IllegalArgumentException if the database is null, or if the name is null or empty.
     */
    public static VPFLibrary fromFile(VPFDatabase database, String name)
    {
        if (database == null)
        {
            String message = Logging.getMessage("nullValue.DatabaseIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(name))
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = new File(database.getFilePath(), name);
        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        // Library tables.
        VPFBufferedRecordData lht = VPFUtils.readTable(new File(file, VPFConstants.LIBRARY_HEADER_TABLE));
        if (lht == null)
        {
            String message = Logging.getMessage("VPF.LibraryHeaderTableMissing");
            throw new WWRuntimeException(message);
        }

        VPFBufferedRecordData cat = VPFUtils.readTable(new File(file, VPFConstants.COVERAGE_ATTRIBUTE_TABLE));
        if (cat == null)
        {
            String message = Logging.getMessage("VPF.CoverageAttributeTableMissing");
            throw new WWRuntimeException(message);
        }

        VPFBufferedRecordData grt = VPFUtils.readTable(new File(file, VPFConstants.GEOGRAPHIC_REFERENCE_TABLE));
        if (grt == null)
        {
            String message = Logging.getMessage("VPF.GeographicReferenceTableMissing");
            throw new WWRuntimeException(message);
        }

        VPFLibrary library = new VPFLibrary(database);
        library.setLibraryHeaderTable(lht);
        library.setCoverageAttributeTable(cat);
        library.setGeographicReferenceTable(grt);

        // Library metadata attributes.
        VPFRecord record = database.getLibraryAttributeTable().getRecord("library_name", name);
        if (record != null)
            library.bounds = VPFUtils.getExtent(record);

        record = lht.getRecord(1);
        if (record != null)
        {
            VPFUtils.checkAndSetValue(record, "library_name", AVKey.DISPLAY_NAME, library);
            VPFUtils.checkAndSetValue(record, "description", AVKey.DESCRIPTION, library);
        }

        // Library Coverages.
        Collection<VPFCoverage> col = createCoverages(library, cat);
        if (col != null)
            library.setCoverages(col);

        // Library tiles.
        VPFCoverage cov = library.getCoverage(VPFConstants.TILE_REFERENCE_COVERAGE);
        if (cov != null)
        {
            VPFTile[] tiles = createTiles(cov);
            if (tiles != null)
            {
                library.setTiles(tiles);
            }
            else
            {
                String message = Logging.getMessage("VPF.NoTilesInTileReferenceCoverage");
                Logging.logger().warning(message);
            }
        }

        // Coverage tiled attributes.
        for (VPFCoverage coverage : library.getCoverages())
        {
            boolean tiled = isCoverageTiled(library, coverage);
            coverage.setTiled(tiled);
        }

        return library;
    }

    public VPFDatabase getDatabase()
    {
        return this.database;
    }

    /**
     * Returns the text name of this Library.
     *
     * @return name of this Library.
     */
    public String getName()
    {
        return this.getStringValue(AVKey.DISPLAY_NAME);
    }

    /**
     * Returns a text description of this Library.
     *
     * @return description of this Library.
     */
    public String getDescription()
    {
        return this.getStringValue(AVKey.DESCRIPTION);
    }

    public String getFilePath()
    {
        StringBuilder sb = new StringBuilder(this.database.getFilePath());
        sb.append(File.separator);
        sb.append(this.getName());
        return sb.toString();
    }

    public VPFBoundingBox getBounds()
    {
        return this.bounds;
    }

    /**
     * Returns the number of Coverages associated with this Library. If this Library is not associated with any
     * Coverage, this returns 0.
     *
     * @return number of Coverages associated with this Library.
     */
    public int getNumCoverages()
    {
        return this.coverageMap.size();
    }

    public boolean containsCoverage(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.coverageMap.containsKey(name);
    }

    public VPFCoverage getCoverage(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.coverageMap.get(name);
    }

    public Set<String> getCoverageNames()
    {
        return Collections.unmodifiableSet(this.coverageMap.keySet());
    }

    public Collection<VPFCoverage> getCoverages()
    {
        return Collections.unmodifiableCollection(this.coverageMap.values());
    }

    public void setCoverages(Collection<? extends VPFCoverage> collection)
    {
        this.removeAllCoverages();

        if (collection != null)
            this.addAllCoverages(collection);
    }

    public void addCoverage(VPFCoverage coverage)
    {
        if (coverage == null)
        {
            String message = Logging.getMessage("nullValue.CoverageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.coverageMap.put(coverage.getName(), coverage);
    }

    public void addAllCoverages(Collection<? extends VPFCoverage> collection)
    {
        if (collection == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (VPFCoverage cov : collection)
        {
            this.addCoverage(cov);
        }
    }

    public void removeCoverage(VPFCoverage coverage)
    {
        if (coverage == null)
        {
            String message = Logging.getMessage("nullValue.CoverageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.coverageMap.remove(coverage.getName());
    }

    public void removeAllCoverages()
    {
        this.coverageMap.clear();
    }

    public boolean hasTiledCoverages()
    {
        return this.getCoverage(VPFConstants.TILE_REFERENCE_COVERAGE) != null;
    }

    public int getNumTiles()
    {
        return (this.tiles != null) ? this.tiles.length : 0;
    }

    public VPFTile[] getTiles()
    {
        if (this.tiles == null)
            return null;

        VPFTile[] copy = new VPFTile[this.tiles.length];
        System.arraycopy(this.tiles, 0, copy, 0, this.tiles.length);
        return copy;
    }

    public void setTiles(VPFTile[] array)
    {
        if (array == null)
        {
            this.tiles = null;
            this.tileMap.clear();
            return;
        }

        this.tiles = new VPFTile[array.length];
        System.arraycopy(array, 0, this.tiles, 0, array.length);

        this.tileMap.clear();
        for (VPFTile tile : array)
        {
            this.tileMap.put(tile.getId(), tile);
        }
    }

    public VPFTile getTile(int tileId)
    {
        return this.tileMap.get(tileId);
    }

    public String getProductType()
    {
        if (this.libraryHeaderTable == null || this.libraryHeaderTable.getNumRecords() == 0)
            return null;

        VPFRecord record = this.libraryHeaderTable.getRecord(1);
        Object o = (record != null) ? record.getValue("product_type") : null;
        return (o != null) ? o.toString() : null;
    }

    public double getMapScale()
    {
        if (this.libraryHeaderTable == null || this.libraryHeaderTable.getNumRecords() == 0)
            return 0;

        VPFRecord record = this.libraryHeaderTable.getRecord(1);
        Object o = (record != null) ? record.getValue("scale") : null;
        return (o != null && o instanceof Number) ? ((Number) o).doubleValue() : 0;
    }

    public Angle computeArcLengthFromMapDistance(double millimeters)
    {
        if (this.geographicReferenceTable == null || this.geographicReferenceTable.getNumRecords() == 0)
            return null;

        VPFRecord record = this.geographicReferenceTable.getRecord(1);
        if (record == null)
            return null;

        String s = (String) record.getValue("units");
        Double unitsCoefficient = parseUnitsCoefficient(s);
        if (unitsCoefficient == null)
        {
            String message = Logging.getMessage("VPF.UnrecognizedUnits", s);
            Logging.logger().severe(message);
            return null;
        }

        s = (String) record.getValue("ellipsoid_detail");
        double[] ellipsoidParams = parseEllipsoidDetail(s);
        if (ellipsoidParams == null || ellipsoidParams.length != 2)
        {
            String message = Logging.getMessage("VPF.UnrecognizedEllipsoidDetail", s);
            Logging.logger().severe(message);
            return null;
        }

        // Multiply the distance in map units (millimeters) by the map scale to determine a real world offset in meters,
        // then divide by the Globe's radius to get offset length in arc radians.
        double meters = this.getMapScale() * millimeters / 1000d;
        double radius = unitsCoefficient * Math.max(ellipsoidParams[0], ellipsoidParams[1]);
        return Angle.fromRadians(meters / radius);
    }

    public VPFBufferedRecordData getLibraryHeaderTable()
    {
        return this.libraryHeaderTable;
    }

    public void setLibraryHeaderTable(VPFBufferedRecordData table)
    {
        if (table == null)
        {
            String message = Logging.getMessage("nullValue.TableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.libraryHeaderTable = table;
    }

    public VPFBufferedRecordData getGeographicReferenceTable()
    {
        return this.geographicReferenceTable;
    }

    public void setGeographicReferenceTable(VPFBufferedRecordData table)
    {
        if (table == null)
        {
            String message = Logging.getMessage("nullValue.TableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.geographicReferenceTable = table;
    }

    public VPFBufferedRecordData getCoverageAttributeTable()
    {
        return this.coverageAttributeTable;
    }

    public void setCoverageAttributeTable(VPFBufferedRecordData table)
    {
        if (table == null)
        {
            String message = Logging.getMessage("nullValue.TableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.coverageAttributeTable = table;
    }

    protected static Double parseUnitsCoefficient(String value)
    {
        if (WWUtil.isEmpty(value))
            return null;

        if (value.toLowerCase().startsWith("f"))
        {
            return 1d / WWMath.METERS_TO_FEET;
        }
        else if (value.toLowerCase().startsWith("m"))
        {
            return 1d;
        }
        else
        {
            return null;
        }
    }

    protected static double[] parseEllipsoidDetail(String value)
    {
        if (WWUtil.isEmpty(value))
            return null;

        Pattern pattern = Pattern.compile("[A][=](.+)\\s+[B][=](.+)\\s+?(.+)?");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            return null;

        String aString = matcher.group(1);
        String bString = matcher.group(2);
        if (WWUtil.isEmpty(aString) || WWUtil.isEmpty(bString))
            return null;

        Double a = WWUtil.convertStringToDouble(aString);
        Double b = WWUtil.convertStringToDouble(bString);
        if (a == null || b == null)
            return null;

        return new double[] {a, b};
    }

    //**************************************************************//
    //********************  Coverage Assembly  *********************//
    //**************************************************************//

    protected static Collection<VPFCoverage> createCoverages(VPFLibrary library, VPFBufferedRecordData table)
    {
        ArrayList<VPFCoverage> list = new ArrayList<VPFCoverage>();

        for (VPFRecord row : table)
        {
            String name = (String) row.getValue("coverage_name");
            if (name != null)
            {
                VPFCoverage coverage = VPFUtils.readCoverage(library, name);
                if (coverage != null)
                    list.add(coverage);
            }
        }

        return list;
    }

    protected static boolean isCoverageTiled(VPFLibrary lib, VPFCoverage cov)
    {
        if (cov.getName().equals(VPFConstants.TILE_REFERENCE_COVERAGE))
            return false;

        if (lib == null || lib.getCoverage(VPFConstants.TILE_REFERENCE_COVERAGE) == null)
            return false;

        VPFTile[] tiles = lib.getTiles();
        if (tiles == null)
            return false;

        for (VPFTile tile : tiles)
        {
            File tmp = new File(cov.getFilePath(), tile.getName());
            if (tmp.exists())
                return true;
        }

        return false;
    }

    //**************************************************************//
    //********************  Tile Assembly  *************************//
    //**************************************************************//

    protected static VPFTile[] createTiles(VPFCoverage coverage)
    {
        VPFFeatureClassSchema[] schemas = coverage.getFeatureClasses(new VPFFeatureTableFilter());
        if (schemas == null || schemas.length == 0)
            return null;

        VPFFeatureClassSchema tileRefSchema = null;
        for (VPFFeatureClassSchema s : schemas)
        {
            if (s.getClassName().equalsIgnoreCase(VPFConstants.TILE_REFERENCE_COVERAGE))
            {
                tileRefSchema = s;
                break;
            }
        }

        if (tileRefSchema == null)
            return null;

        VPFFeatureClassFactory factory = new VPFBasicFeatureClassFactory();
        VPFFeatureClass areaClass = factory.createFromSchema(coverage, tileRefSchema);
        return createTiles(areaClass);
    }

    protected static VPFTile[] createTiles(VPFFeatureClass featureClass)
    {
        VPFPrimitiveDataFactory primitiveFactory = new VPFBasicPrimitiveDataFactory(null);
        VPFPrimitiveData primitiveData = primitiveFactory.createPrimitiveData(featureClass.getCoverage());
        if (primitiveData == null)
            return null;

        VPFFeatureFactory featureFactory = new VPFBasicFeatureFactory(null, primitiveData);
        Collection<? extends VPFFeature> features = featureClass.createFeatures(featureFactory);
        if (features == null || features.size() == 0)
            return null;

        VPFTile[] tiles = new VPFTile[features.size()];

        int index = 0;
        for (VPFFeature feature : features)
        {
            String tileName = feature.getStringValue("tile_name");
            if (tileName != null)
                tileName = fixTileName(tileName);

            tiles[index++] = new VPFTile(feature.getId(), tileName, feature.getBounds());
        }

        return tiles;
    }

    protected static String fixTileName(String s)
    {
        s = s.toLowerCase();
        s = s.replace("\\", File.separator);
        return s;
    }
}
