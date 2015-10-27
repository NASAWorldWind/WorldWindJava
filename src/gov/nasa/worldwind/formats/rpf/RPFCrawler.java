/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.util.Logging;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import static java.util.logging.Level.FINE;

/**
 * @author dcollins
 * @version $Id: RPFCrawler.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFCrawler
{
    public static final String RPF_DIRECTORY = "RPF";
    public static final String RPF_OVERVIEW_EXTENSION = ".OVR";
    public static final String RPF_TOC_EXTENSION = ".TOC";

    public static interface RPFCrawlerListener
    {
        void fileFound(File file, boolean isTOCFile);

        void finished();
    }

    public abstract static class RPFGrouper implements RPFCrawlerListener
    {
        private final RPFFrameProperty groupType;

        public RPFGrouper(RPFFrameProperty groupType)
        {
            if (groupType == null)
            {
                String message = Logging.getMessage("nullValue.RPFFramePropertyTypeIsNull");
                Logging.logger().fine(message);
                throw new IllegalArgumentException(message);
            }
            this.groupType = groupType;
        }

        public abstract void addToGroup(Object groupKey, File rpfFile, RPFFrameFilename rpfFrameFilename);

        public void fileFound(File file, boolean isTOCFile)
        {
            if (isTOCFile)
                fileFoundTOC(file);
            else
                fileFoundRPF(file);
        }

        private void fileFoundTOC(File file)
        {
            RPFTOCFile rpftocFile = null;
            try
            {
                rpftocFile = RPFTOCFile.load(file);
            }
            catch (IOException e)
            {
                Logging.logger().fine(e.getMessage());
            }

            if (rpftocFile == null)
                return;

            List<RPFFrameFileIndexSection.RPFFrameFileIndexRecord> rpfRecords = extractRPFRecords(rpftocFile);
            if (rpfRecords == null || rpfRecords.isEmpty())
                return;

            RPFFrameFilename firstFrameFilename = null;
            try
            {
                firstFrameFilename = RPFFrameFilename.parseFilename(rpfRecords.get(0).getFrameFileName());
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("RPFCrawler.ExceptionParsingFilename", file.getPath());
                Logging.logger().log(FINE, message, e);
            }

            if (firstFrameFilename == null)
                return;

            Object groupKey = this.groupType.getValue(firstFrameFilename);
            for (RPFFrameFileIndexSection.RPFFrameFileIndexRecord record : rpfRecords)
            {
                String filePath = createAbsolutePath(file.getParentFile().getAbsolutePath(), record.getPathname(),
                    record.getFrameFileName());
                RPFFrameFilename rpfFrameFilename = rpfFrameFilenameFor(file);
                this.addToGroup(groupKey, new File(filePath), rpfFrameFilename);
            }
        }

        private void fileFoundRPF(File file)
        {
            RPFFrameFilename rpfFrameFilename = rpfFrameFilenameFor(file);
            if (rpfFrameFilename == null)
                return;
            Object groupKey = this.groupType.getValue(rpfFrameFilename);
            this.addToGroup(groupKey, file, rpfFrameFilename);
        }

        public void finished()
        {
        }

        private RPFFrameFilename rpfFrameFilenameFor(File file)
        {
            RPFFrameFilename rpfFrameFilename = null;
            try
            {
                rpfFrameFilename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("RPFCrawler.ExceptionParsingFilename", file.getPath());
                Logging.logger().log(FINE, message, e);
            }
            return rpfFrameFilename;
        }
    }

    private static class RPFRunner implements Runnable
    {
        private final RPFCrawler context;
        private final File directory;
        private final RPFCrawlerListener listener;
        private final boolean tocFileSearch;

        public RPFRunner(RPFCrawler context, File directory, RPFCrawlerListener listener, boolean tocFileSearch)
        {
            this.context = context;
            this.directory = directory;
            this.listener = listener;
            this.tocFileSearch = tocFileSearch;
        }

        public void run()
        {
            this.context.process(this.directory, listener, tocFileSearch, true);
            this.context.threadLock.lock();
            try
            {
                if (this.context.thread != this.context.deadThread)
                {
                    listener.finished();
                    this.context.thread = this.context.deadThread;
                }
            }
            finally
            {
                this.context.threadLock.unlock();
            }
        }
    }

    private final Thread deadThread = new Thread();
    private final Lock threadLock = new ReentrantLock();
    private volatile Thread thread = null;

    public RPFCrawler()
    {
    }

    private static String createAbsolutePath(String... pathElem)
    {
        StringBuilder sb = new StringBuilder();
        for (String str : pathElem)
        {
            if (str != null && str.length() > 0)
            {
                int startIndex = 0;
                if (str.startsWith("./") || str.startsWith(".\\"))
                    startIndex = 1;
                else if (!str.startsWith("/") && !str.startsWith("\\"))
                    sb.append(File.separatorChar);
                int endIndex;
                if (str.endsWith("/") || str.endsWith("\\"))
                    endIndex = str.length() - 1;
                else
                    endIndex = str.length();
                sb.append(str, startIndex, endIndex);
            }
        }
        if (sb.length() <= 0)
            return null;
        return sb.toString();
    }

    private static List<RPFFrameFileIndexSection.RPFFrameFileIndexRecord> extractRPFRecords(RPFTOCFile tocFile)
    {
        List<RPFFrameFileIndexSection.RPFFrameFileIndexRecord> rpfFiles
            = new LinkedList<RPFFrameFileIndexSection.RPFFrameFileIndexRecord>();
        if (tocFile != null
            && tocFile.getFrameFileIndexSection() != null
            && tocFile.getFrameFileIndexSection().getFrameFileIndexTable() != null
            && tocFile.getFrameFileIndexSection().getFrameFileIndexTable().size() > 0)
        {
            for (RPFFrameFileIndexSection.RPFFrameFileIndexRecord frameFileIndexRecord
                : tocFile.getFrameFileIndexSection().getFrameFileIndexTable())
            {
                if (frameFileIndexRecord != null
                    && frameFileIndexRecord.getFrameFileName() != null
                    && !frameFileIndexRecord.getFrameFileName().toUpperCase().endsWith(RPF_OVERVIEW_EXTENSION))
                {
                    rpfFiles.add(frameFileIndexRecord);
                }
            }
        }
        return rpfFiles;
    }

    private static boolean isRPFDirectory(File file)
    {
        return RPF_DIRECTORY.compareToIgnoreCase(file.getName()) == 0;
    }

    private static boolean isRPFFile(File file)
    {
        return RPFFrameFilename.isFilename(file.getName().toUpperCase());
    }

    private static boolean isTOCFile(File file)
    {
        return file.getName().toUpperCase().endsWith(RPF_TOC_EXTENSION);
    }

    private void process(File file, RPFCrawlerListener listener, boolean tocFileSearch, boolean inOwnThread)
    {
        this.threadLock.lock();
        try
        {
            if (inOwnThread && this.thread == deadThread)
                return;
        }
        finally
        {
            this.threadLock.unlock();
        }

        File[] children = file.listFiles();
        if (tocFileSearch)
        {
            if (children == null)
                return;
            if (isRPFDirectory(file))
                this.searchForTOC(children, listener, inOwnThread);
            else
                this.searchForDirectory(children, listener, true, inOwnThread);
        }
        else
        {
            if (RPFFrameFilename.isFilename(file.getName().toUpperCase()))
                listener.fileFound(file, false);
            if (children == null)
                return;
            this.searchForRPF(children, listener, inOwnThread);
        }
    }

    private void searchForTOC(File[] contents, RPFCrawlerListener listener, boolean inOwnThread)
    {
        for (File file : contents)
        {
            this.threadLock.lock();
            try
            {
                if (inOwnThread && this.thread == deadThread)
                    return;
            }
            finally
            {
                this.threadLock.unlock();
            }

            if (isTOCFile(file))
                listener.fileFound(file, true);
        }
    }

    private void searchForDirectory(File[] contents, RPFCrawlerListener listener, boolean tocFileSearch,
        boolean inOwnThread)
    {
        for (File file : contents)
        {
            this.threadLock.lock();
            try
            {
                if (inOwnThread && this.thread == deadThread)
                    return;
            }
            finally
            {
                this.threadLock.unlock();
            }

            if (file.isDirectory())
                this.process(file, listener, tocFileSearch, inOwnThread);
        }
    }

    private void searchForRPF(File[] contents, RPFCrawlerListener listener, boolean inOwnThread)
    {
        for (File file : contents)
        {
            this.threadLock.lock();
            try
            {
                if (inOwnThread && this.thread == deadThread)
                    return;
            }
            finally
            {
                this.threadLock.unlock();
            }

            if (isRPFFile(file))
                listener.fileFound(file, false);
            else if (file.isDirectory())
                this.process(file, listener, false, inOwnThread);
        }
    }

    public File[] invoke(File directory, boolean tocFileSearch)
    {
        File validDir = this.validateDirectory(directory);
        final Collection<File> results = new ArrayList<File>();
        this.process(validDir, new RPFCrawlerListener()
        {
            public void fileFound(File file, boolean isTOCFile)
            {
                if (file.exists())
                    results.add(file);
            }

            public void finished()
            {
            }
        }, tocFileSearch, false);
        File[] tocFileArray = new File[results.size()];
        results.toArray(tocFileArray);
        return tocFileArray;
    }

    public void invoke(File directory, RPFCrawlerListener listener, boolean tocFileSearch)
    {
        File validDir = this.validateDirectory(directory);
        this.process(validDir, listener, tocFileSearch, false);
    }

    public void start(File directory, RPFCrawlerListener listener, boolean tocFileSearch)
    {
        this.threadLock.lock();
        try
        {
            if (this.thread != null || this.thread == deadThread)
            {
                String message = Logging.getMessage("RPFCrawler.BadStart");
                Logging.logger().fine(message);
                throw new IllegalStateException(message);
            }
            File validDir = this.validateDirectory(directory);
            this.thread = new Thread(new RPFRunner(this, validDir, listener, tocFileSearch));
            this.thread.start();
        }
        finally
        {
            this.threadLock.unlock();
        }
    }

    public void stop()
    {
        this.threadLock.lock();
        try
        {
            this.thread = deadThread;
        }
        finally
        {
            this.threadLock.unlock();
        }
    }

    private File validateDirectory(File directory)
    {
        if (directory == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        String path = directory.getAbsolutePath();
        if (!path.endsWith("/") && !path.endsWith("\\"))
        {
            path = path + File.separatorChar;
            directory = new File(path);
        }
        if (!directory.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", directory.getPath());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return directory;
    }
}
