/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: RPFFileIndex.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFileIndex
{
    private final Table rpfFileTable;
    private final Table waveletTable;
    private final Table directoryTable;
    private final IndexProperties properties;
    private static final String FILE_ID = "RPF_FILE_INDEX";
    private static final String VERSION = "VERSION_0_1";
    private static final int FILE_ID_LENGTH = 16;
    private static final int VERSION_LENGTH = 16;

    public RPFFileIndex()
    {
        this.rpfFileTable = new Table();
        this.waveletTable = new Table();
        this.directoryTable = new Table();
        this.rpfFileTable.setRecordFactory(new RecordFactory() {
            public Record newRecord(long key) {
                return new RPFFileRecord(key);
            }
        });
        this.waveletTable.setRecordFactory(new RecordFactory() {
            public Record newRecord(long key) {
                return new WaveletRecord(key);
            }
        });
        this.directoryTable.setRecordFactory(new RecordFactory() {
            public Record newRecord(long key) {
                return new DirectoryRecord(key);
            }
        });
        this.properties = new IndexProperties();
    }

    public Table getRPFFileTable()
    {
        return this.rpfFileTable;
    }

    public Table getWaveletTable()
    {
        return this.waveletTable;
    }

    public Table getDirectoryTable()
    {
        return this.directoryTable;
    }

    public IndexProperties getIndexProperties()
    {
        return properties;
    }

    public File getRPFFile(long key)
    {
        if (key == Table.INVALID_KEY)
        {
            String message = "key is invalid: " + key;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = null;
        RPFFileRecord rec = (RPFFileRecord) this.rpfFileTable.getRecord(key);
        if (rec != null)
        {
            file = getFile(rec.getFilename(), rec.getDirectorySecondaryKey());
        }
        return file;
    }

    public File getWaveletFile(long key)
    {
        if (key == Table.INVALID_KEY)
        {
            String message = "key is invalid: " + key;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = null;
        WaveletRecord rec = (WaveletRecord) this.waveletTable.getRecord(key);
        if (rec != null)
        {
            file = getFile(rec.getFilename(), rec.getDirectorySecondaryKey());
        }
        return file;
    }

    private File getFile(String filename, long directoryKey)
    {
        File file = null;
        if (directoryKey != Table.INVALID_KEY)
        {
            DirectoryRecord dirRec = (DirectoryRecord) this.directoryTable.getRecord(directoryKey);
            file = new File(dirRec != null ? dirRec.getPath() : null, filename);
        }
        return file;
    }

    public Record createRPFFileRecord(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Attempt to create a directory record from the File's parent-file.
        String directory = file.getParent();
        Record dirRecord = createDirectoryRecord(directory);

        // Attempt to create a file record from the File's name.
        String filename = file.getName();
        Record record = this.rpfFileTable.createRecord();
        ((RPFFileRecord) record).filename = filename;
        ((RPFFileRecord) record).directorySecondaryKey = dirRecord != null ? dirRecord.getKey() : Table.INVALID_KEY;    

        return record;
    }

    public Record createWaveletRecord(File file, long rpfFileKey)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Attempt to create a directory record from the File's parent-file.
        String directory = file.getParent();
        Record dirRecord = createDirectoryRecord(directory);

        // Attempt to create a file record from the File's name.
        String filename = file.getName();
        Record record = this.waveletTable.createRecord();
        ((WaveletRecord) record).filename = filename;
        ((WaveletRecord) record).directorySecondaryKey = dirRecord != null ? dirRecord.getKey() : Table.INVALID_KEY;

        // Attempt to update the RPFFileRecord.
        Record rpfRecord = this.rpfFileTable.getRecord(rpfFileKey);
        if (rpfRecord != null)
        {
            ((RPFFileRecord) rpfRecord).waveletSecondaryKey = record.getKey();
            ((WaveletRecord) record).rpfFileSecondaryKey = rpfRecord.getKey();
        }

        return record;
    }

    private synchronized Record createDirectoryRecord(String path)
    {
        Record record = null;
        if (path != null)
        {
            for (Record rec : this.directoryTable.getRecords())
            {
                if (((DirectoryRecord) rec).path.equals(path))
                {
                    record = rec;
                    break;
                }
            }

            if (record == null)
            {
                record = this.directoryTable.createRecord();
                ((DirectoryRecord) record).path = path;
            }
        }
        return record;
    }

    public void updateBoundingSector()
    {
        Sector bs = null;
        for (Record rec : this.rpfFileTable.getRecords())
        {
            RPFFileRecord rpfRec = (RPFFileRecord) rec;
            Sector fs = rpfRec.getSector();
            // If the entry has no sector, then ignore it.
            if (fs != null)
                bs = (bs != null ? bs.union(fs) : fs);
        }

        if (bs != null)
        {
            bs = Sector.fromDegrees(
                clamp(bs.getMinLatitude().degrees, -90d, 90d),
                clamp(bs.getMaxLatitude().degrees, -90d, 90d),
                clamp(bs.getMinLongitude().degrees, -180d, 180d),
                clamp(bs.getMaxLongitude().degrees, -180d, 180d));
        }
        this.properties.setBoundingSector(bs);
    }

    private static double clamp(double x, double min, double max)
    {
        return x < min ? min : x > max ? max : x;
    }

    public void load(java.nio.ByteBuffer buffer) throws IOException
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String fileId = getString(buffer, FILE_ID_LENGTH);
        if (!FILE_ID.equals(fileId))
        {
            String message = "buffer does not contain an RPFFileIndex";
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        //noinspection UnusedDeclaration
        String version = getString(buffer, VERSION_LENGTH);

        LocationSection locationSection = new LocationSection(buffer);
        this.properties.load(buffer, locationSection.getInformationSectionLocation());
        this.rpfFileTable.load(buffer, locationSection.getRPFFileTableSectionLocation());
        this.waveletTable.load(buffer, locationSection.getWaveletTableSectionLocation());
        this.directoryTable.load(buffer, locationSection.getDirectoryTableSectionLocation());
    }

    public java.nio.ByteBuffer save() throws IOException
    {
        java.nio.ByteBuffer informationSectionBuffer = this.properties.save();
        java.nio.ByteBuffer rpfFileTableBuffer = this.rpfFileTable.save();
        java.nio.ByteBuffer waveletTableBuffer = this.waveletTable.save();
        java.nio.ByteBuffer directoryTableBuffer = this.directoryTable.save();

        int location = FILE_ID_LENGTH + VERSION_LENGTH;
        LocationSection locationSection = new LocationSection();
        location += locationSection.locationSectionLength;
        locationSection.setInformationSection(informationSectionBuffer.limit(), location);
        location += informationSectionBuffer.limit();
        locationSection.setRPFFileTableSection(rpfFileTableBuffer.limit(), location);
        location += rpfFileTableBuffer.limit();
        locationSection.setWaveletTableSection(waveletTableBuffer.limit(), location);
        location += waveletTableBuffer.limit();
        locationSection.setDirectoryTableSection(directoryTableBuffer.limit(), location);
        location += directoryTableBuffer.limit();
        
        java.nio.ByteBuffer locationSectionBuffer = locationSection.save();

        int length =
            FILE_ID_LENGTH + VERSION_LENGTH
            + locationSectionBuffer.limit()
            + informationSectionBuffer.limit()
            + rpfFileTableBuffer.limit()
            + waveletTableBuffer.limit()
            + directoryTableBuffer.limit();
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(length);

        putString(buffer, FILE_ID, FILE_ID_LENGTH);
        putString(buffer, VERSION, VERSION_LENGTH);
        buffer.put(locationSectionBuffer);
        buffer.put(informationSectionBuffer);
        buffer.put(rpfFileTableBuffer);
        buffer.put(waveletTableBuffer);
        buffer.put(directoryTableBuffer);

        buffer.flip();
        return buffer;
    }

    public static class Table
    {
        private final List<Record> records;
        private final Map<Long, Record> keyIndex;
        private RecordFactory recordFactory;
        private volatile long uniqueKey = INVALID_KEY;
        static final long INVALID_KEY = -1L;

        public Table()
        {
            this.records = new ArrayList<Record>();
            this.keyIndex = new HashMap<Long, Record>();
            this.recordFactory = new DefaultRecordFactory();
        }

        public final List<Record> getRecords()
        {
            return this.records;
        }

        public final Record getRecord(long key)
        {
            Record found = null;
            if (key != INVALID_KEY)
            {
                found = this.keyIndex.get(key);
            }
            return found;
        }

        public RecordFactory getRecordFactory()
        {
            return this.recordFactory;
        }

        public void setRecordFactory(RecordFactory recordFactory)
        {
            if (recordFactory == null)
            {
                String message = "RecordFactory is null";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            
            this.recordFactory = recordFactory;
        }

        public Record createRecord()
        {
            long key = createUniqueKey();
            return newRecord(key);
        }

        Record newRecord(long key)
        {
            Record rec = this.recordFactory.newRecord(key);
            putRecord(key, rec);
            return rec;
        }

        private void putRecord(long key, Record record)
        {
            this.records.add(record);
            this.keyIndex.put(key, record);
        }

        private synchronized long createUniqueKey()
        {
            return ++this.uniqueKey;
        }

        void load(java.nio.ByteBuffer buffer, int location) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            // Clear any existing records.
            this.records.clear();
            this.keyIndex.clear();

            int savePos = buffer.position();
            buffer.position(location);

            //noinspection UnusedDeclaration
            int componentLength = buffer.getInt();
            int recordTableOffset = buffer.getInt();
            int numRecords = buffer.getInt();
            
            buffer.position(location + recordTableOffset);
            for (int i = 0; i < numRecords; i++)
            {
                long key = buffer.getLong();
                Record rec = newRecord(key);
                rec.load(buffer);
            }

            buffer.position(savePos);
        }

        java.nio.ByteBuffer save() throws IOException
        {
            List<Record> records = this.getRecords();
            int numRecords = records.size();

            int headerLength = 3 * Integer.SIZE / 8;
            int length = headerLength;
            for (Record rec : records)
            {
                length += (Long.SIZE + rec.getSizeInBits()) / 8;
            }

            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(length);
            buffer.putInt(length); // component length
            buffer.putInt(headerLength); // record table offset
            buffer.putInt(numRecords); // num records
            for (Record rec : records)
            {
                buffer.putLong(rec.getKey());
                rec.save(buffer);
            }
            buffer.flip();
            return buffer;
        }
    }

    public static class Record
    {
        private final long key;

        public Record(long key)
        {
            this.key = key;
        }

        public final long getKey()
        {
            return this.key;
        }

        void load(java.nio.ByteBuffer buffer) throws IOException
        {
        }

        void save(java.nio.ByteBuffer buffer) throws IOException
        {
        }

        int getSizeInBits()
        {
            return 0;
        }
    }

    public static interface RecordFactory
    {
        Record newRecord(long key);
    }

    private static class DefaultRecordFactory implements RecordFactory
    {
        public Record newRecord(long key)
        {
            return new Record(key);
        }
    }

    public static class RPFFileRecord extends Record
    {
        private String filename;
        private long directorySecondaryKey = Table.INVALID_KEY;
        private long waveletSecondaryKey = Table.INVALID_KEY;
        private Angle minLatitude;
        private Angle maxLatitude;
        private Angle minLongitude;
        private Angle maxLongitude;
        private static int FILENAME_LENGTH = 12;
        private static int SIZE =
            (FILENAME_LENGTH * Byte.SIZE) // Filename.
            + Long.SIZE // Directory path secondary key.
            + Long.SIZE // Wavelet file secondary key.
            + (4 * Double.SIZE); // min-latitude, max-latitude, min-longitude, and max-longitude.

        public RPFFileRecord(long key)
        {
            super(key);
        }

        public String getFilename()
        {
            return this.filename;
        }

        public void setFilename(String filename)
        {
            this.filename = filename;
        }

        public long getDirectorySecondaryKey()
        {
            return this.directorySecondaryKey;
        }

        public long getWaveletSecondaryKey()
        {
            return this.waveletSecondaryKey;
        }

        public Sector getSector()
        {
            Sector sector = null;
            if (this.minLatitude != null
                && this.maxLatitude != null
                && this.minLongitude != null
                && this.maxLongitude != null)
            {
                sector = new Sector(
                    this.minLatitude, this.maxLatitude,
                    this.minLongitude, this.maxLongitude);
            }
            return sector;
        }

        public void setSector(Sector sector)
        {
            this.minLatitude = sector != null ? sector.getMinLatitude() : null;
            this.maxLatitude = sector != null ? sector.getMaxLatitude() : null;
            this.minLongitude = sector != null ? sector.getMinLongitude() : null;
            this.maxLongitude = sector != null ? sector.getMaxLongitude() : null;
        }

        void load(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.filename = getString(buffer, FILENAME_LENGTH);
            this.directorySecondaryKey = buffer.getLong();
            this.waveletSecondaryKey = buffer.getLong();
            this.minLatitude = getAngle(buffer);
            this.maxLatitude = getAngle(buffer);
            this.minLongitude = getAngle(buffer);
            this.maxLongitude = getAngle(buffer);
        }

        void save(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            putString(buffer, this.filename, FILENAME_LENGTH);
            buffer.putLong(this.directorySecondaryKey);
            buffer.putLong(this.waveletSecondaryKey);
            putAngle(buffer, this.minLatitude);
            putAngle(buffer, this.maxLatitude);
            putAngle(buffer, this.minLongitude);
            putAngle(buffer, this.maxLongitude);
        }

        int getSizeInBits()
        {
            return SIZE + super.getSizeInBits();
        }
    }

    public static class WaveletRecord extends Record
    {
        private String filename;
        private long directorySecondaryKey = Table.INVALID_KEY;
        private long rpfFileSecondaryKey = Table.INVALID_KEY;
        private static int FILENAME_LENGTH = 16;
        private static int SIZE =
            (FILENAME_LENGTH * Byte.SIZE) // Filename.
            + Long.SIZE // Directory path secondary key.
            + Long.SIZE; // RPF file secondary key.

        public WaveletRecord(long key)
        {
            super(key);
        }

        public String getFilename()
        {
            return this.filename;
        }

        public void setFilename(String filename)
        {
            this.filename = filename;
        }

        public long getDirectorySecondaryKey()
        {
            return this.directorySecondaryKey;
        }

        public long getRPFFileSecondaryKey()
        {
            return this.rpfFileSecondaryKey;
        }

        void load(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.filename = getString(buffer, FILENAME_LENGTH);
            this.directorySecondaryKey = buffer.getLong();
            this.rpfFileSecondaryKey = buffer.getLong();
        }

        void save(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            putString(buffer, this.filename, FILENAME_LENGTH);
            buffer.putLong(this.directorySecondaryKey);
            buffer.putLong(this.rpfFileSecondaryKey);
        }

        int getSizeInBits()
        {
            return SIZE + super.getSizeInBits();
        }
    }

    public static class DirectoryRecord extends Record
    {
        private String path;
        private static int PATH_LENGTH = 512;
        private static int SIZE =
            (PATH_LENGTH * Byte.SIZE); // Path.

        public DirectoryRecord(long key)
        {
            super(key);
        }

        public String getPath()
        {
            return this.path;
        }

        public void setPath(String path)
        {
            if (path == null)
            {
                String message = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.path = path;
        }

        void load(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.path = getString(buffer, PATH_LENGTH);
        }

        void save(java.nio.ByteBuffer buffer) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            putString(buffer, this.path, PATH_LENGTH);
        }

        int getSizeInBits()
        {
            return SIZE + super.getSizeInBits();
        }
    }

    public static class IndexProperties
    {
        public String rootPath;
        public String dataSeriesIdentifier;
        public String description;
        public Angle minLatitude;
        public Angle maxLatitude;
        public Angle minLongitude;
        public Angle maxLongitude;
        private static int ROOT_PATH_LENGTH = 512;
        private static int DATA_SERIES_ID_LENGTH = 512;
        private static int DESCRIPTION_LENGTH = 4096;
        private static int SIZE =
            Integer.SIZE // Section length.
            + (ROOT_PATH_LENGTH * Byte.SIZE) // Root path.
            + (DATA_SERIES_ID_LENGTH * Byte.SIZE) // Data series identifier.
            + (DESCRIPTION_LENGTH * Byte.SIZE) // Description.
            + (4 * Double.SIZE); // min-latitude, max-latitude, min-longitude, and max-longitude.

        public IndexProperties()
        {
        }

        public String getRootPath()
        {
            return this.rootPath;
        }

        public void setRootPath(String rootPath)
        {
            this.rootPath = rootPath;
        }

        public String getDataSeriesIdentifier()
        {
            return this.dataSeriesIdentifier;
        }

        public void setDataSeriesIdentifier(String dataSeriesIdentifier)
        {
            this.dataSeriesIdentifier = dataSeriesIdentifier;
        }

        public String getDescription()
        {
            return this.description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public Sector getBoundingSector()
        {
            Sector sector = null;
            if (this.minLatitude != null
                && this.maxLatitude != null
                && this.minLongitude != null
                && this.maxLongitude != null)
            {
                sector = new Sector(
                    this.minLatitude, this.maxLatitude,
                    this.minLongitude, this.maxLongitude);
            }
            return sector;
        }

        public void setBoundingSector(Sector sector)
        {
            this.minLatitude = sector != null ? sector.getMinLatitude() : null;
            this.maxLatitude = sector != null ? sector.getMaxLatitude() : null;
            this.minLongitude = sector != null ? sector.getMinLongitude() : null;
            this.maxLongitude = sector != null ? sector.getMaxLongitude() : null;
        }

        void load(java.nio.ByteBuffer buffer, int location) throws IOException
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.ByteBufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            int savePos = buffer.position();
            buffer.position(location);

            //noinspection UnusedDeclaration
            int length = buffer.getInt();
            this.rootPath = getString(buffer, ROOT_PATH_LENGTH);
            this.dataSeriesIdentifier = getString(buffer, DATA_SERIES_ID_LENGTH);
            this.description = getString(buffer, DESCRIPTION_LENGTH);
            this.minLatitude = getAngle(buffer);
            this.maxLatitude = getAngle(buffer);
            this.minLongitude = getAngle(buffer);
            this.maxLongitude = getAngle(buffer);

            buffer.position(savePos);
        }

        java.nio.ByteBuffer save() throws IOException
        {
            int length = SIZE / 8;
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(length);
            buffer.putInt(length);
            putString(buffer, this.rootPath, ROOT_PATH_LENGTH);
            putString(buffer, this.dataSeriesIdentifier, DATA_SERIES_ID_LENGTH);
            putString(buffer, this.description, DESCRIPTION_LENGTH);
            putAngle(buffer, this.minLatitude);
            putAngle(buffer, this.maxLatitude);
            putAngle(buffer, this.minLongitude);
            putAngle(buffer, this.maxLongitude);
            buffer.flip();
            return buffer;
        }
    }

    private static class LocationSection
    {
        public int locationSectionLength;
        public int componentLocationTableOffset;
        public int numOfComponentLocationRecords;

        private java.util.Map<Integer, ComponentLocationRecord> table =
            new java.util.HashMap<Integer, ComponentLocationRecord>();

        public LocationSection()
        {
            for (int i = 1; i <= 4; i++)
                this.table.put(i, new ComponentLocationRecord(i, -1, -1));
            this.locationSectionLength = (3 * Integer.SIZE / 8) + (this.table.size() * 3 * Integer.SIZE / 8);
            this.componentLocationTableOffset = (3 * Integer.SIZE / 8);
            this.numOfComponentLocationRecords = this.table.size();
        }

        public LocationSection(java.nio.ByteBuffer buffer) throws IOException
        {
            int savePos = buffer.position();

            this.locationSectionLength = buffer.getInt();
            this.componentLocationTableOffset = buffer.getInt();
            this.numOfComponentLocationRecords = buffer.getInt();

            buffer.position(savePos + this.componentLocationTableOffset);
            for (int i = 0; i < this.numOfComponentLocationRecords; i++)
            {
                int id = buffer.getInt();
                table.put(id, new ComponentLocationRecord(id,
                    buffer.getInt(), // length
                    buffer.getInt()  // location
                ));
            }

            buffer.position(savePos);
        }

        public java.nio.ByteBuffer save() throws IOException
        {
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(this.locationSectionLength);
            buffer.putInt(this.locationSectionLength);
            buffer.putInt(this.componentLocationTableOffset);
            buffer.putInt(this.numOfComponentLocationRecords);

            Collection<ComponentLocationRecord> records = table.values();
            ComponentLocationRecord[] recordArray = new ComponentLocationRecord[records.size()];
            records.toArray(recordArray);

            buffer.position(this.componentLocationTableOffset);
            for (int i = 0; i < this.numOfComponentLocationRecords; i++)
            {
                ComponentLocationRecord rec = recordArray[i];
                buffer.putInt(rec.getId());
                buffer.putInt(rec.getLength());
                buffer.putInt(rec.getLocation());
            }

            buffer.flip();
            return buffer;
        }

        public int getInformationSectionLocation()
        {
            return getLocation(1);
        }

        public int getInformationSectionLength()
        {
            return getLength(1);
        }

        public void setInformationSection(int length, int location)
        {
            set(1, length, location);
        }

        public int getRPFFileTableSectionLocation()
        {
            return getLocation(2);
        }

        public int getRPFFileTableSectionLength()
        {
            return getLength(2);
        }

        public void setRPFFileTableSection(int length, int location)
        {
            set(2, length, location);
        }

        public int getWaveletTableSectionLocation()
        {
            return getLocation(3);
        }

        public int getWaveletTableSectionLength()
        {
            return getLength(3);
        }

        public void setWaveletTableSection(int length, int location)
        {
            set(3, length, location);
        }

        public int getDirectoryTableSectionLocation()
        {
            return getLocation(4);
        }

        public int getDirectoryTableSectionLength()
        {
            return getLength(4);
        }

        public void setDirectoryTableSection(int length, int location)
        {
            set(4, length, location);
        }

        private int getLocation(int componentID)
        {
            ComponentLocationRecord rec = this.getRecord(componentID);
            return (null != rec) ? rec.getLocation() : 0;
        }

        private int getLength(int componentID)
        {
            ComponentLocationRecord rec = this.getRecord(componentID);
            return (null != rec) ? rec.getLength() : 0;
        }

        private void set(int componentID, int length, int location)
        {
            ComponentLocationRecord rec = this.getRecord(componentID);
            if (rec != null)
            {
                rec.length = length;
                rec.location = location;
            }
        }

        private ComponentLocationRecord getRecord(int componentID)
        {
            if(table.containsKey(componentID))
                return table.get(componentID);
            return null;
        }

        public static class ComponentLocationRecord
        {
            private int  id;
            private int length;
            private int location;

            public ComponentLocationRecord(int id, int length, int location)
            {
                this.id = id;
                this.length = length;
                this.location = location;
            }

            public int getId()
            {
                return id;
            }

            public int getLength()
            {
                return length;
            }

            public int getLocation()
            {
                return location;
            }
        }
    }

    private static final String CHARACTER_ENCODING = "UTF-8";

    private static String getString(java.nio.ByteBuffer buffer, int len) throws IOException
    {
        String s = null;
        if (buffer != null && buffer.remaining() >= len)
        {
            byte[] dest = new byte[len];
            buffer.get(dest, 0, len);
            s = new String(dest, CHARACTER_ENCODING).trim();
        }
        return s;
    }

    private static void putString(java.nio.ByteBuffer buffer, String s, int len) throws IOException
    {
        if (buffer != null)
        {
            byte[] src = new byte[len];
            if (s != null)
            {
                byte[] utfBytes = s.getBytes(CHARACTER_ENCODING);
                if (utfBytes != null)
                {
                    System.arraycopy(utfBytes, 0, src, 0, utfBytes.length);
                }
            }
            buffer.put(src, 0, len);
        }
    }

    private static Angle getAngle(java.nio.ByteBuffer buffer)
    {
        // The binary description of an angle needs to distinguish between
        // non-null and null. We use Double.NaN as a marker for a null-value.
        Angle a = null;
        if (buffer != null)
        {
            double value = buffer.getDouble();
            if (!Double.isNaN(value))
            {
                a = Angle.fromDegrees(value);
            }
        }
        return a;
    }

    private static void putAngle(java.nio.ByteBuffer buffer, Angle angle)
    {
        // The binary description of an angle needs to distinguish between
        // non-null and null. We use Double.NaN as a marker for a null-value.
        if (buffer != null)
        {
            double value = angle != null ? angle.degrees : Double.NaN;
            buffer.putDouble(value);
        }
    }
}
