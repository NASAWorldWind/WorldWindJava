/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.nitfs.*;
import gov.nasa.worldwind.formats.rpf.*;
import gov.nasa.worldwind.formats.wvt.WaveletCodec;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author dcollins
 * @version $Id: RPFTiledImageProcessor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFTiledImageProcessor
{
    private int numThreads = -1;
    private final PropertyChangeSupport propertyChangeSupport;
    private final Object fileLock = new Object();
    private volatile boolean doStop = false;

    private static final int DEFAULT_WAVELET_SIZE = 256;

    public static final String BEGIN_SUB_TASK = "BeginSubTask";
    public static final String END_SUB_TASK = "EndSubTask";
    public static final String SUB_TASK_NUM_STEPS = "SubTaskNumSteps";
    public static final String SUB_TASK_STEP_COMPLETE = "SubTaskStepComplete";
    public static final String SUB_TASK_STEP_FAILED = "SubTaskStepFailed";

    public RPFTiledImageProcessor()
    {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public int getThreadPoolSize()
    {
        return this.numThreads;
    }

    public void setThreadPoolSize(int size)
    {
        this.numThreads = size;
    }

    public RPFFileIndex makeFileIndex(File rootFile, String dataSeriesId, String description,
                                      Iterable<File> fileIterable)
    {
        if (rootFile == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dataSeriesId == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (fileIterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RPFFileIndex result = null;
        if (!this.doStop)
        {
            RPFFileIndex fileIndex = new RPFFileIndex();
            fileIndex.getIndexProperties().setRootPath(rootFile.getAbsolutePath());
            fileIndex.getIndexProperties().setDataSeriesIdentifier(dataSeriesId);
            fileIndex.getIndexProperties().setDescription(description);

            // Populate the index with the list of RPF files.
            for (File file : fileIterable)
            {
                fileIndex.createRPFFileRecord(file);
            }

            // Process RPF file records.
            int waveletWidth = DEFAULT_WAVELET_SIZE;
            int waveletHeight = DEFAULT_WAVELET_SIZE;
            processFileIndex(fileIndex, waveletWidth, waveletHeight);

            // Update the RPF bounding sector.
            fileIndex.updateBoundingSector();

            if (!this.doStop)
            {
                result = fileIndex;
            }
        }
        return result;
    }

    public Layer makeLayer(RPFFileIndex fileIndex)
    {
        if (fileIndex == null)
        {
            String message = "RPFFileIndex is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (fileIndex.getIndexProperties() == null)
        {
            String message = "RPFFileIndex.IndexProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer result = null;
        if (!this.doStop)
        {
            String rootPath = fileIndex.getIndexProperties().getRootPath();
            String dataSeriesId = fileIndex.getIndexProperties().getDataSeriesIdentifier();

            // Save the RPFFileIndex to the file cache.
            File indexFile = WorldWind.getDataFileStore().newFile(RPFTiledImageLayer.getFileIndexCachePath(rootPath, dataSeriesId));
            saveFileIndex(fileIndex, indexFile);

            // Create tiled imagery.
            AVList params = new AVListImpl();
            params.setValue(RPFTiledImageLayer.RPF_ROOT_PATH, rootPath);
            params.setValue(RPFTiledImageLayer.RPF_DATA_SERIES_ID, dataSeriesId);
            params.setValue(RPFGenerator.RPF_FILE_INDEX, fileIndex);
            Collection<Tile> tileList = RPFTiledImageLayer.createTopLevelTiles(params);
            RPFGenerator generator = new RPFGenerator(params);
            createTiledImagery(tileList, generator);

            // Return the layer.
            if (!this.doStop)
            {
                result = new RPFTiledImageLayer(params);
            }
        }
        return result;
    }

    public void stop()
    {
        this.doStop = true;
    }

    private String makeWaveletCachePath(RPFFileIndex fileIndex, long rpfFileKey)
    {
        String path = null;
        if (fileIndex != null && fileIndex.getIndexProperties() != null && rpfFileKey != -1)
        {
            File rpfFile = fileIndex.getRPFFile(rpfFileKey);
            if (rpfFile != null)
            {
                String rpfFilePath = rpfFile.getPath();
                String rootPath = fileIndex.getIndexProperties().getRootPath();
                int index = rpfFilePath.lastIndexOf(rootPath);
                String partialPath = rpfFilePath.substring(index + rootPath.length(), rpfFilePath.length());

                StringBuilder sb = new StringBuilder();
                sb.append(WWIO.formPath(
                    fileIndex.getIndexProperties().getRootPath(),
                    fileIndex.getIndexProperties().getDataSeriesIdentifier(),
                    "wavelet"));
                sb.append(File.separator);
                sb.append(partialPath);
                sb.append(WaveletCodec.WVT_EXT);
                path = sb.toString();
            }
        }
        return path;
    }

    private void processFileIndex(final RPFFileIndex fileIndex, final int waveletWidth, final int waveletHeight)
    {
        RPFFileIndex.Table table = fileIndex.getRPFFileTable();
        Collection<RPFFileIndex.Record> recordList = table.getRecords();
        if (recordList != null)
        {
            firePropertyChange(BEGIN_SUB_TASK, null, null);
            firePropertyChange(SUB_TASK_NUM_STEPS, null, recordList.size());

            Collection<Runnable> tasks = new ArrayList<Runnable>();
            for (final RPFFileIndex.Record record : recordList)
            {
                tasks.add(new Runnable() {
                    public void run() {
                        File file = fileIndex.getRPFFile(record.getKey());
                        try {
                            processRecord(fileIndex, record, waveletWidth, waveletHeight);
                            firePropertyChange(SUB_TASK_STEP_COMPLETE, null, file.getName());
                        } catch (Throwable t) {
                            String message = String.format("Exception while processing file: %s", file);
                            Logging.logger().log(java.util.logging.Level.SEVERE, message, t);
                            firePropertyChange(SUB_TASK_STEP_FAILED, null, file.getName());
                        }
                    }
                });
            }

            if (this.numThreads > 1)
                runAsynchronously(tasks, this.numThreads, true);
            else
                run(tasks);

            firePropertyChange(END_SUB_TASK, null, null);
        }
    }

    private void processRecord(RPFFileIndex fileIndex, RPFFileIndex.Record record,
                               int waveletWidth, int waveletHeight) throws IOException
    {
        if (fileIndex == null)
        {
            String message = "RPFFileIndex is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (record == null)
        {
            String message = "RPFFileIndex.Record is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = null;
        RPFImageFile rpfImageFile = null;
        if (!this.doStop)
        {
            // Load the RPF image file.
            file = fileIndex.getRPFFile(record.getKey());
            rpfImageFile = RPFImageFile.load(file);

            // Create an attribute for the file's sector.
            Sector sector = getFileSector(rpfImageFile);
            if (sector != null)
            {
                ((RPFFileIndex.RPFFileRecord) record).setSector(sector);
            }
        }

        File waveletFile = null;
        if (!this.doStop)
        {
            // Create the wavelet file path.
            synchronized (this.fileLock)
            {
                String cachePath = makeWaveletCachePath(fileIndex, record.getKey());
                waveletFile = WorldWind.getDataFileStore().newFile(cachePath);
            }

            // Create a record for the wavelet file.
            if (waveletFile != null)
            {
                fileIndex.createWaveletRecord(waveletFile, record.getKey());
            }
        }

        WaveletCodec wavelet = null;
        if (!this.doStop)
        {
            // If the wavelet file is not null, and the source RPF file is newer than the wavelet file,
            // then create a new wavelet file.
            if (waveletFile != null && (file != null && file.lastModified() > waveletFile.lastModified()))
            {
                // Get the RPF image file as a BufferedImage.
                BufferedImage bi = rpfImageFile.getBufferedImage();

                // Must deproject it...
                bi = deproject(file, bi);

                // Get coverage information from the transform.
                // Create the wavelet from the RPF BufferedImage.
                if (bi != null)
                {
                    wavelet = createWavelet(bi, waveletWidth, waveletHeight);
                    //noinspection UnusedAssignment
                    bi = null;
                }
            }
            //noinspection UnusedAssignment
            rpfImageFile = null;
        }

        if (!this.doStop)
        {
            // If a wavelet has been created,
            // then write the wavelet to file.
            if (wavelet != null)
            {
                ByteBuffer buffer = WaveletCodec.save(wavelet);
                if (buffer != null)
                {
                    WWIO.saveBuffer(buffer, waveletFile);
                    //noinspection UnusedAssignment
                    buffer = null;
                }
                //noinspection UnusedAssignment
                wavelet = null;
            }
        }
    }

    private WaveletCodec createWavelet(BufferedImage image, int waveletWidth, int waveletHeight)
    {
        int waveletImgType;
        switch (image.getType())
        {
        case BufferedImage.TYPE_BYTE_GRAY:
            waveletImgType = BufferedImage.TYPE_BYTE_GRAY;
            break;
        case BufferedImage.TYPE_INT_BGR:
        case BufferedImage.TYPE_INT_RGB:
            waveletImgType = BufferedImage.TYPE_3BYTE_BGR;
            break;
        case BufferedImage.TYPE_INT_ARGB:
            waveletImgType = BufferedImage.TYPE_4BYTE_ABGR;
            break;
        default:
            waveletImgType = BufferedImage.TYPE_3BYTE_BGR;
            break;
        }

        BufferedImage scaledImage = new BufferedImage(waveletWidth, waveletHeight, waveletImgType);
        scaleImage(image, scaledImage);
        return WaveletCodec.encode(scaledImage);
    }

    private BufferedImage scaleImage(BufferedImage srcImage, BufferedImage destImage)
    {
        double sx = (double) destImage.getWidth() / (double) srcImage.getWidth();
        double sy = (double) destImage.getHeight() / (double) srcImage.getHeight();
        Graphics2D g2d = (Graphics2D) destImage.getGraphics();
        g2d.scale(sx, sy);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(srcImage, 0, 0, null);
        return destImage;
    }

    private Sector getFileSector(RPFFile rpfFile)
    {
        // Attempt to get the file's coverage from the RPFFile.
        Sector sector = null;
        if (rpfFile != null)
        {

            // We'll first attempt to compute the Sector, if possible, from the filename (if it exists) by using
            // the conventions for CADRG and CIB filenames. It has been observed that for polar frame files in
            // particular that coverage information in the file itself is sometimes unreliable.
            File file = rpfFile.getFile();
            if (file != null)
                sector = sectorFromFilename(file);

            // Can't compute the Sector;  see if the RPFFile contains coverage information.
            if (sector == null)
                sector = sectorFromHeader(rpfFile);
        }
        return sector;
    }

    private Sector sectorFromHeader(RPFFile rpfFile)
    {
        Sector sector = null;
        try
        {
            if (rpfFile != null)
            {
                NITFSImageSegment imageSegment = (NITFSImageSegment) rpfFile.getNITFSSegment(NITFSSegmentType.IMAGE_SEGMENT);
                RPFFrameFileComponents comps = imageSegment.getUserDefinedImageSubheader().getRPFFrameFileComponents();
                Angle minLat = comps.swLowerleft.getLatitude();
                Angle maxLat = comps.neUpperRight.getLatitude();
                Angle minLon = comps.swLowerleft.getLongitude();
                Angle maxLon = comps.neUpperRight.getLongitude();
                // This sector spans the longitude boundary. In order to render this sector,
                // we must adjust the longitudes such that minLon<maxLon.
                if (Angle.crossesLongitudeBoundary(minLon, maxLon))
                {
                    if (minLon.compareTo(maxLon) > 0)
                    {
                        double degrees = 360 + maxLon.degrees;
                        maxLon = Angle.fromDegrees(degrees);
                    }
                }
                sector = new Sector(minLat, maxLat, minLon, maxLon);
            }
        }
        catch (Exception e)
        {
            // Computing the file's coverage failed. Log the condition and return null.
            // This at allows the coverage to be re-computed at a later time.
            String message = String.format("Exception while getting file sector: %s", rpfFile != null ? rpfFile.getFile() : "");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            sector = null;
        }
        return sector;
    }

    private Sector sectorFromFilename(File file)
    {
        Sector sector = null;
        try
        {
            if (file != null && file.getName() != null)
            {
                // Parse the filename, using the conventions for CADRG and CIB filenames.
                RPFFrameFilename rpfFilename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
                // Get the dataseries associated with that code.
                RPFDataSeries ds = RPFDataSeries.dataSeriesFor(rpfFilename.getDataSeriesCode());
                // Create a transform to compute coverage information.
                RPFFrameTransform tx = RPFFrameTransform.createFrameTransform(
                    rpfFilename.getZoneCode(), ds.rpfDataType, ds.scaleOrGSD);
                // Get coverage information from the transform.
                sector = tx.computeFrameCoverage(rpfFilename.getFrameNumber());
            }
        }
        catch (Exception e)
        {
            // Computing the file's coverage failed. Log the condition and return null.
            // This at allows the coverage to be re-computed at a later time.
            String message = String.format("Exception while computing file sector: %s", file);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            sector = null;
        }
        return sector;
    }

    //private boolean isPolarFile(File file)
    //{
    //    boolean isPolar = false;
    //    try
    //    {
    //        if (file != null && file.getName() != null)
    //        {
    //            // Parse the filename, using the conventions for CADRG and CIB filenames.
    //            RPFFrameFilename rpfFilename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
    //            // Get the dataseries associated with that code.
    //            char zoneCode = rpfFilename.getZoneCode();
    //            // Ignore polar zones.
    //            if (zoneCode == '9' || zoneCode == 'J')
    //                isPolar = true;
    //        }
    //    }
    //    catch (Exception e)
    //    {
    //        // Computing the file's zone failed.
    //        isPolar = false;
    //    }
    //    return isPolar;
    //}

    private void createTiledImagery(Collection<Tile> tileList, RPFGenerator generator)
    {
        firePropertyChange(BEGIN_SUB_TASK, null, null);
        firePropertyChange(SUB_TASK_NUM_STEPS, null, tileList.size());

        Collection<Runnable> tasks = new ArrayList<Runnable>();
        final RPFGenerator.RPFServiceInstance service = generator.getServiceInstance();
        for (final Tile tile : tileList)
        {
            tasks.add(new Runnable() {
                public void run() {
                    try {
                        createTileImage(tile, service);
                        firePropertyChange(SUB_TASK_STEP_COMPLETE, null, tile.getPath());
                    } catch (Throwable t) {
                        String message = String.format("Exception while processing image: %s", tile.getPath());
                        Logging.logger().log(java.util.logging.Level.SEVERE, message, t);
                        firePropertyChange(SUB_TASK_STEP_FAILED, null, tile.getPath());
                    }
                }
            });
        }

        if (this.numThreads > 1)
            runAsynchronously(tasks, this.numThreads, true);
        else
            run(tasks);

        firePropertyChange(END_SUB_TASK, null, null);
    }

    private void createTileImage(Tile tile, RPFGenerator.RPFServiceInstance service) throws Exception
    {
        if (tile == null)
        {
            String message = "Tile is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (service == null)
        {
            String message = "RPFGenerator.RPFServiceInstance is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File outFile = null;
        if (!this.doStop)
        {
            synchronized (this.fileLock)
            {
                outFile = WorldWind.getDataFileStore().newFile(tile.getPath());
            }
        }

        BufferedImage image = null;
        if (!this.doStop)
        {
            URL url = tile.getResourceURL();
            if (url != null)
            {
                image = service.serviceRequest(url);
            }
        }

        if (!this.doStop)
        {
            // If an image has been created,
            // then convert it to DDS and write it to file.
            if (image != null)
            {
                ByteBuffer buffer = DDSCompressor.compressImage(image);
                if (buffer != null && outFile != null)
                {
                    WWIO.saveBuffer(buffer, outFile);
                }
            }
        }
    }

    private void saveFileIndex(RPFFileIndex fileIndex, File file)
    {
        try
        {
            ByteBuffer buffer = null;
            if (fileIndex != null)
            {
                buffer = fileIndex.save();
            }

            if (buffer != null && file != null)
            {
                WWIO.saveBuffer(buffer, file);
            }
        }
        catch (Exception e)
        {
            String message = String.format("Exception while saving RPFFileIndex: %s", file);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    private BufferedImage deproject(File file, BufferedImage image)
    {
        // Need a RPFFrameTransform object and a frame-number to perform the deprojection...
        RPFFrameFilename fframe = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
        RPFDataSeries ds = RPFDataSeries.dataSeriesFor(fframe.getDataSeriesCode());
        RPFFrameTransform tx = RPFFrameTransform.createFrameTransform(fframe.getZoneCode(),
            ds.rpfDataType, ds.scaleOrGSD);
        RPFFrameTransform.RPFImage[] images = tx.deproject(fframe.getFrameNumber(), image);
        if (images.length == 1)
            return images[0].getImage();

        // NOTE we are using explicit knowledge of the order of the two images produced in the deprojection step...
        BufferedImage westImage = images[0].getImage();
        BufferedImage eastImage = images[1].getImage();
        BufferedImage outImage = new BufferedImage(westImage.getWidth()+eastImage.getWidth(), westImage.getHeight(),
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) outImage.getGraphics();
        g2d.drawImage(westImage, 0, 0, null);
        g2d.drawImage(eastImage, westImage.getWidth(), 0, null);
        return outImage;
    }

    private void run(Iterable<Runnable> taskIterable)
    {
        try
        {
            if (taskIterable != null)
            {
                for (Runnable task : taskIterable)
                {
                    if (!this.doStop)
                    {
                        task.run();
                    }
                }
            }
        }
        catch (Exception e)
        {
            String message = "Exception while executing tasks";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    private void runAsynchronously(Iterable<Runnable> taskIterable, int threadPoolSize, boolean blockUntilFinished)
    {
        try
        {
            if (taskIterable != null)
            {
                ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

                for (Runnable task : taskIterable)
                {
                    if (!this.doStop)
                    {
                        if (task != null)
                        {
                            executor.submit(task);
                        }
                    }
                }
                executor.shutdown();

                // Attempt to block this thread until all Runnables
                // have completed execution.
                while (blockUntilFinished && !executor.awaitTermination(1000L, TimeUnit.MILLISECONDS))
                {}
            }
        }
        catch (Exception e)
        {
            String message = "Exception while executing tasks";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
