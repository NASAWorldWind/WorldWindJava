/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.io;

import gov.nasa.worldwind.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Implements the {@link KMLDoc} interface for KMZ files read from a stream.
 *
 * @author tag
 * @version $Id: KMZInputStream.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMZInputStream implements KMLDoc
{
    /** The zip stream created for the specified input stream. */
    protected ZipInputStream zipStream;

    /**
     * The next entry to read from the input stream. This field refers to the first entry in the stream when the
     * constructor returns, and after each entry is copied to the temp dir it is updated to point to the subsequent
     * entry. If null, the stream contains no more entries.
     */
    protected ZipEntry currentEntry;

    /** A mapping of the files in the KMZ stream to their location in the temporary directory. */
    protected Map<String, File> files;

    /** The directory to hold files copied from the stream. Both the directory and the files copied there are temporary. */
    protected File tempDir;

    /**
     * Constructs a KMZInputStream instance. Upon return, the new instance's current-entry field refers to the first
     * entry in the stream.
     *
     * @param sourceStream the input stream to read from.
     *
     * @throws IllegalArgumentException if the specified stream is null.
     * @throws java.io.IOException      if an error occurs while accessing the stream.
     */
    public KMZInputStream(InputStream sourceStream) throws IOException
    {
        if (sourceStream == null)
        {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zipStream = new ZipInputStream(sourceStream);
        this.files = new HashMap<String, File>();
        this.moveToNextEntry();
    }

    /**
     * Moves this stream to its next zip entry and updates this stream's current-entry field.
     *
     * @throws IOException if an exception occurs while attempting to more the stream to the next entry.
     */
    protected void moveToNextEntry() throws IOException
    {
        ZipEntry nextEntry = this.zipStream.getNextEntry();

        // Close the stream if there's nothing more to read from it.
        if (nextEntry == null && this.currentEntry != null)
            this.zipStream.close();

        this.currentEntry = nextEntry;
    }

    /**
     * Returns an {@link java.io.InputStream} to the first KML file within the stream.
     *
     * @return an input stream positioned to the first KML file in the stream, or null if the stream does not contain a
     *         KML file.
     *
     * @throws IOException if an error occurs while reading the stream.
     */
    public synchronized InputStream getKMLStream() throws IOException
    {
        // Iterate through the stream's entries to find the KML file. It will normally be the first entry, but there's
        // no guarantee of that. If another file is encountered before the KML file, copy it to temp dir created to
        // capture the KMZ document's directory hierarchy.

        while (this.currentEntry != null)
        {
            if (this.currentEntry.getName().toLowerCase().endsWith(".kml"))
            {
                String kmlEntryName = this.currentEntry.getName();
                this.copyCurrentEntryToTempDir(); // copies the current entry to a temp file and adds it to this.files
                File file = this.files.get(kmlEntryName);
                return file != null ? new FileInputStream(file) : null;
            }
            else
            {
                this.copyCurrentEntryToTempDir();
            }
        }

        return null;
    }

    /**
     * Returns an {@link InputStream} to a specified file within the KMZ stream. The file's path is resolved relative to
     * the internal root of the KMZ file represented by the stream.
     * <p/>
     * Note: Since relative references to files outside the stream have no meaning, this class does not resolve relative
     * references to files in other KMZ archives. For example, it does not resolve references like this:
     * <i>../other.kmz/file.png</i>.
     *
     * @param path the path of the requested file.
     *
     * @return an input stream positioned to the start of the requested file, or null if the file does not exist or the
     *         specified path is absolute.
     *
     * @throws IllegalArgumentException if the path is null.
     * @throws IOException              if an error occurs while attempting to read the input stream.
     */
    public synchronized InputStream getSupportFileStream(String path) throws IOException
    {
        // This method is called by the native WebView implementation to resolve resources in KMZ balloons. It may
        // not perform any synchronization with the EDT (such as calling invokeAndWait), or it will introduce a
        // potential deadlock when called by the WebView's native UI thread.

        if (path == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Regardless of whether the next entry in the stream is the requested file, copy each entry encountered to
        // the temporary directory created to represent the directory hierarchy in the KMZ document. If the most
        // recently copied file is the requested file, return it and suspend reading and copying until it's again
        // necessary. This prevents bogging down performance until everything is copied.

        File file = files.get(path);
        if (file != null)
            return new FileInputStream(file);

        if (this.currentEntry == null) // no more entries in the zip stream
            return null;

        this.copyCurrentEntryToTempDir();

        // If the file just copied is the one requested, a recursive call will pick it up immediately.
        return this.getSupportFileStream(path);
    }

    /**
     * Returns an absolute path to a specified file within the KMZ stream. The file's path is resolved relative to the
     * internal root of the KMZ file represented by the stream.
     * <p/>
     * Note: Since relative references to files outside the stream have no meaning, this class does not resolve relative
     * references to files in other KMZ archives. For example, it does not resolve references like this:
     * <i>../other.kmz/file.png</i>.
     *
     * @param path the path of the requested file.
     *
     * @return an absolute path for the requested file, or null if the file does not exist or the specified path is
     *         absolute.
     *
     * @throws IllegalArgumentException if the path is null.
     * @throws IOException              if an error occurs while attempting to create a temporary file.
     */
    public synchronized String getSupportFilePath(String path) throws IOException
    {
        // This method is called by the native WebView implementation to resolve resources in KMZ balloons. It may
        // not perform any synchronization with the EDT (such as calling invokeAndWait), or it will introduce a
        // potential deadlock when called by the WebView's native UI thread.

        if (path == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Regardless of whether the next entry in the stream is the requested file, copy each entry encountered to
        // the temporary directory created to represent the directory hierarchy in the KMZ document. If the most
        // recently copied file is the requested file, return it and suspend reading and copying until it's again
        // necessary. This prevents bogging down performance until everything is copied.

        File file = files.get(path);
        if (file != null)
            return file.getPath();

        if (this.currentEntry == null) // no more entries in the zip stream
            return null;

        this.copyCurrentEntryToTempDir();

        // If the file just copied is the one requested, a recursive call will pick it up immediately. If not, the next
        // entry will be retrieved.
        return this.getSupportFilePath(path);
    }

    /**
     * Copies a file from the input stream to the temporary area created to represent the KMZ contents. If that area
     * does not yet exists, it is created.
     * <p/>
     * Note: This method should be called only by another synchronized method of this instance.
     *
     * @throws IOException if an error occurs during the copy.
     */
    protected void copyCurrentEntryToTempDir() throws IOException
    {
        if (this.currentEntry == null) // no more entries in the zip stream
            return;

        if (this.currentEntry.isDirectory())
        {
            this.moveToNextEntry();
            return;
        }

        if (this.tempDir == null)
            this.tempDir = WWIO.makeTempDir();

        if (this.tempDir == null) // unlikely to occur, but define a reaction
        {
            String message = Logging.getMessage("generic.UnableToCreateTempDir", this.tempDir);
            Logging.logger().warning(message);
            return;
        }

        // Create the path for the temp file and ensure all directories leading to it exist.
        String tempFileName = this.tempDir + File.separator + this.currentEntry.getName();
        WWIO.makeParentDirs(tempFileName);

        // Copy the entry.
        File outFile = new File(tempFileName);
        outFile.deleteOnExit();
        WWIO.saveBuffer(WWIO.readStreamToBuffer(this.zipStream), outFile);
        this.files.put(this.currentEntry.getName(), outFile);

        // Increment the stream to the next entry.
        this.moveToNextEntry();
    }
//
//    public static void main(String[] args)
//    {
//        try
//        {
//            InputStream is = new FileInputStream(new File("/Users/tag/NoBackup/kml/ocean_model_locations.kmz"));
//            KMZInputStream zStream = new KMZInputStream(is);
//            InputStream kmlStream = zStream.getKMLStream();
//            is = zStream.getSupportFileStream("files/camera_mode.png");
//            is = zStream.getSupportFileStream("files/3DBuildingsLayer3.png");
//            is = zStream.getSupportFileStream("files/camera_mode.png");
//            if (kmlStream != null)
//            {
//                KMLRoot root = KMLRoot.create(kmlStream);
//                root.parse();
//            }
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        catch (XMLStreamException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
