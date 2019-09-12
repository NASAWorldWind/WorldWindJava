/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: FileTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileTree implements Iterable<File>
{
    private File root;
    private int mode = FILES_AND_DIRECTORIES;

    public static final int FILES_ONLY = 1;
    public static final int DIRECTORIES_ONLY = 2;
    public static final int FILES_AND_DIRECTORIES = 3;

    public FileTree()
    {
        this(null);
    }

    public FileTree(File root)
    {
        this.root = root;
    }

    public File getRoot()
    {
        return this.root;
    }

    public void setRoot(File root)
    {
        this.root = root;
    }

    public int getMode()
    {
        return this.mode;
    }

    public void setMode(int mode)
    {
        if (!validate(mode))
            throw new IllegalArgumentException("mode:" + mode);

        this.mode = mode;
    }

    public List<File> asList()
    {
        return asList(null);
    }

    public List<File> asList(FileFilter fileFilter)
    {
        return makeList(this.root, fileFilter, this.mode);
    }

    public Iterator<File> iterator()
    {
        return iterator(null);
    }

    public Iterator<File> iterator(FileFilter fileFilter)
    {
        return new FileTreeIterator(this.root, fileFilter, this.mode);
    }

    private static List<File> makeList(File root, FileFilter fileFilter, int mode)
    {
        Queue<File> dirs = new LinkedList<File>();
        if (isDirectory(root))
            dirs.offer(root);

        LinkedList<File> result = new LinkedList<File>();
        while (dirs.peek() != null)
            expand(dirs.poll(), fileFilter, mode, result, dirs);

        return result;
    }

    private static class FileTreeIterator implements Iterator<File> {
        private final Queue<File> dirs = new LinkedList<File>();
        private final Queue<File> files = new LinkedList<File>();
        private final FileFilter fileFilter;
        private final int mode;

        private FileTreeIterator(File root, FileFilter fileFilter, int mode) {
            if (isDirectory(root))
                this.dirs.offer(root);
            this.fileFilter = fileFilter;
            this.mode = mode;
        }

        public boolean hasNext() {
            if (this.files.peek() == null)
                expandUntilFilesFound();
            return this.files.peek() != null;
        }

        public File next() {
            if (this.files.peek() == null) {
                expandUntilFilesFound();
                if (this.files.peek() == null)
                    throw new NoSuchElementException();
            }
            return this.files.poll();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void expandUntilFilesFound() {
            while (this.dirs.peek() != null && this.files.peek() == null)
                expand(this.dirs.poll());
        }

        private void expand(File directory) {
            if (directory != null) {
                FileTree.expand(directory, this.fileFilter, this.mode, this.files, this.dirs);
            }
        }
    }

    private static void expand(File file, FileFilter fileFilter, int mode,
                               Queue<File> outFiles, Queue<File> outDirs)
    {
        if (file != null)
        {
            File[] list = file.listFiles();
            if (list != null)
            {
                for (File child : list)
                {
                    if (child != null)
                    {
                        boolean isDir = child.isDirectory();
                        if (isDir)
                        {
                            outDirs.offer(child);
                        }
                        
                        if ((!isDir && isDisplayFiles(mode)) || (isDir && isDisplayDirectories(mode)))
                        {
                            if (fileFilter == null || fileFilter.accept(child))
                            {
                                outFiles.offer(child);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isDirectory(File file)
    {
        return file != null && file.exists() && file.isDirectory();
    }

    private static boolean isDisplayFiles(int mode)
    {
        return mode == FILES_ONLY || mode == FILES_AND_DIRECTORIES;
    }

    private static boolean isDisplayDirectories(int mode)
    {
        return mode == DIRECTORIES_ONLY || mode == FILES_AND_DIRECTORIES;
    }

    private static boolean validate(int mode)
    {
        return mode == FILES_ONLY
            || mode == DIRECTORIES_ONLY
            || mode == FILES_AND_DIRECTORIES;
    }
}
