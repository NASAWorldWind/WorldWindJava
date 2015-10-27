/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.util.Logging;

import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: FileStoreDataSet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileStoreDataSet
{
    public static final String HOUR = "gov.nasa.worldwindx.examples.util.cachecleaner.HOUR";
    public static final String DAY = "gov.nasa.worldwindx.examples.util.cachecleaner.DAY";
    public static final String WEEK = "gov.nasa.worldwindx.examples.util.cachecleaner.WEEK";
    public static final String MONTH = "gov.nasa.worldwindx.examples.util.cachecleaner.MONTH";
    public static final String YEAR = "gov.nasa.worldwindx.examples.util.cachecleaner.YEAR";

    protected static class LeafInfo
    {
        long lastUsed;
        long size;
    }

    protected final File root;
    protected final String cacheRootPath;

    protected boolean fileGranularity = false; // delete only files individually out of date or all files in a directory
    protected ArrayList<File> exclusionList = new ArrayList<File>();
    protected ArrayList<LeafInfo> leafDirs = new ArrayList<LeafInfo>();
    protected LeafInfo[] sortedLeafDirs;

    public FileStoreDataSet(File root, String cacheRootPath)
    {
        if (root == null)
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.root = root;
        this.cacheRootPath = cacheRootPath;
        this.update();
    }

    protected void update()
    {
        this.leafDirs.clear();
        findLeaves(this.root, this.leafDirs);
        if (this.leafDirs.size() == 0)
            return;

        this.sortedLeafDirs = new LeafInfo[this.leafDirs.size()];
        this.sortedLeafDirs = this.leafDirs.toArray(this.sortedLeafDirs);
        Arrays.sort(this.sortedLeafDirs, new Comparator<LeafInfo>()
        {
            public int compare(LeafInfo leafA, LeafInfo leafB)
            {
                return leafA.lastUsed < leafB.lastUsed ? -1 : leafA.lastUsed == leafB.lastUsed ? 0 : 1;
            }
        });
    }

    public boolean isFileGranularity()
    {
        return fileGranularity;
    }

    public void setFileGranularity(boolean fileGranularity)
    {
        this.fileGranularity = fileGranularity;
    }

    public String getPath()
    {
        return root.getPath();
    }

    public String getName()
    {
        String name = this.cacheRootPath == null ? this.getPath() : this.getPath().replace(
            this.cacheRootPath.subSequence(0, this.cacheRootPath.length()), "".subSequence(0, 0));
        return name.startsWith("/") ? name.substring(1) : name;
    }

    public List<File> getExclusions()
    {
        return Collections.unmodifiableList(this.exclusionList);
    }

    public void setExclusions(Iterable<? extends File> exclusions)
    {
        this.exclusionList.clear();
        if (exclusions != null)
        {
            for (File file : exclusions)
            {
                this.exclusionList.add(file);
            }
        }
    }

    public long getSize()
    {
        long size = 0;

        for (LeafInfo leaf : this.leafDirs)
        {
            size += leaf.size;
        }

        return size;
    }

    public long getOutOfScopeSize(String unit, int interval)
    {
        if (unit == null)
        {
            String message = Logging.getMessage("nullValue.TimeUnit");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        long previousTime = computeTimeOffset(unit, interval);

        long size = 0;
        for (LeafInfo leaf : this.sortedLeafDirs)
        {
            if (leaf.lastUsed > previousTime)
                break;

            size += leaf.size;
        }

        return size;
    }

    public long getLastModified()
    {
        return this.sortedLeafDirs[this.sortedLeafDirs.length - 1].lastUsed;
    }

    public void deleteOutOfScopeFiles(String unit, int interval, boolean echo)
    {
        if (unit == null)
        {
            String message = Logging.getMessage("nullValue.TimeUnit");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.deleteFiles(this.root, this.exclusionList, computeTimeOffset(unit, interval), echo);
        this.update();
    }

    @SuppressWarnings( {"ResultOfMethodCallIgnored"})
    protected void deleteFiles(File dir, List<File> exclusions, long timeBoundary, boolean echo)
    {
        if (!dir.isDirectory())
            return;

        boolean directoryOutOfDate = dir.lastModified() < timeBoundary;

        File[] files = dir.listFiles();
        for (File file : files)
        {
            // If the file is in the list of excluded files, then ignore it. If the file is a directory its
            // descendants will also be ignored.
            if (exclusions.contains(file))
                continue;

            if (file.isFile())
            {
                if (this.isFileGranularity())
                {
                    if (file.lastModified() < timeBoundary)
                    {
                        file.delete();
                        if (echo)
                            System.out.println("Deleting FILE: " + file.getPath());
                    }
                }
                else if (directoryOutOfDate)
                {
                    file.delete();
                    if (echo)
                        System.out.println("Deleting FILE: " + file.getPath());
                }
            }
            else if (file.isDirectory())
            {
                this.deleteFiles(file, exclusions, timeBoundary, echo); // recurse until files are encountered
                if (file.list().length == 0)
                {
                    file.delete();
                    if (echo)
                        System.out.println("Deleting DIRECTORY: " + file.getPath());
                }
            }
        }
    }

    @SuppressWarnings( {"ResultOfMethodCallIgnored"})
    public void delete(boolean echo)
    {
        deleteOutOfScopeFiles(HOUR, 0, echo);
        File[] files = this.root.listFiles();
        if (files.length == 0)
            this.root.delete();
    }

    protected static long computeTimeOffset(String unit, int interval)
    {
        GregorianCalendar cal = new GregorianCalendar();

        if (interval == 0)
        {
        }
        else if (unit.equals(HOUR))
        {
            cal.add(Calendar.HOUR, -interval);
        }
        else if (unit.equals(DAY))
        {
            cal.add(Calendar.DAY_OF_YEAR, -interval);
        }
        else if (unit.equals(WEEK))
        {
            cal.add(Calendar.WEEK_OF_YEAR, -interval);
        }
        else if (unit.equals(MONTH))
        {
            cal.add(Calendar.MONTH, -interval);
        }
        else if (unit.equals(YEAR))
        {
            cal.add(Calendar.YEAR, -interval);
        }

        return cal.getTimeInMillis();
    }

    protected static void findLeaves(File dir, ArrayList<LeafInfo> leaves)
    {
        if (!dir.isDirectory())
            return;

        File[] subDirs = dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });

        if (subDirs.length == 0)
        {
            LeafInfo li = new LeafInfo();
            li.lastUsed = dir.lastModified();
            li.size = computeDirectorySize(dir);
            leaves.add(li);
        }
        else
        {
            for (File subDir : subDirs)
            {
                findLeaves(subDir, leaves);
            }
        }
    }

    protected static long computeDirectorySize(File dir)
    {
        long size = 0;

        File[] files = dir.listFiles();
        for (File file : files)
        {
            try
            {
                FileInputStream fis = new FileInputStream(file);
                size += fis.available();
                fis.close();
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileComputingSize", file.getAbsolutePath());
                Logging.logger().fine(message);
            }
        }

        return size;
    }

    /**
     * Find all of the data set directories in a cache root.
     *
     * @param cacheRoot Cache root to search.
     *
     * @return List of data sets in the specified cache.
     */
    public static List<FileStoreDataSet> getDataSets(File cacheRoot)
    {
        if (cacheRoot == null)
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<FileStoreDataSet> datasets = new ArrayList<FileStoreDataSet>();

        File[] cacheDirs = FileStoreDataSet.listDirs(cacheRoot);
        for (File cacheDir : cacheDirs)
        {
            if (cacheDir.getName().equals("license"))
                continue;

            File[] subDirs = FileStoreDataSet.listDirs(cacheDir);
            if (subDirs.length == 0)
            {
                datasets.add(new FileStoreDataSet(cacheDir, cacheRoot.getPath()));
            }
            else
            {
                // If the directory should be treated as a single dataset, add just one entry to the list.
                if (isSingleDataSet(subDirs))
                {
                    datasets.add(new FileStoreDataSet(cacheDir, cacheRoot.getPath()));
                }
                // Otherwise add each subdirectory as a separate data set.
                else
                {
                    for (File sd : subDirs)
                    {
                        FileStoreDataSet ds = new FileStoreDataSet(sd, cacheRoot.getPath());
                        datasets.add(ds);
                    }
                }
            }
        }

        return datasets;
    }

    /**
     * List all of the sub-directories in a parent directory.
     *
     * @param parent Parent directory to search.
     *
     * @return All sub-directories under {@code parent}.
     */
    protected static File[] listDirs(File parent)
    {
        return parent.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });
    }

    /**
     * Determines if a list of sub-directories should be treated as a single data set. This implementation returns
     * {@code true} if all of the sub-directories have numeric names. In this case, the numeric directories are most
     * likely used by the cache implementation to group files in a single data set. The numeric directory names do not
     * provide meaningful grouping to the user.
     *
     * @param subDirs List of sub-directories to test.
     *
     * @return {@code true} if the directories should be treated as a single data set.
     */
    protected static boolean isSingleDataSet(File[] subDirs)
    {
        boolean onlyNumericDirs = true;

        for (File sd : subDirs)
        {
            if (!isNumeric(sd.getName()))
                onlyNumericDirs = false;
        }

        return onlyNumericDirs;
    }

    /**
     * Determines if a string contains only digits.
     *
     * @param s String to test.
     *
     * @return {@code true} if {@code s} contains only digits.
     */
    protected static boolean isNumeric(String s)
    {
        for (char c : s.toCharArray())
        {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }
}
