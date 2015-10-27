/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * A path to a node in a {@link Tree}. The path is expressed as a list of strings.
 *
 * @author tag
 * @version $Id: TreePath.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TreePath extends ArrayList<String>
{
    /** Create an empty tree path. */
    public TreePath()
    {
    }

    /**
     * Create a tre path.
     *
     * @param initialPath Base tree path.
     * @param args        Additional path elements to append to {@code initialPath}.
     */
    public TreePath(TreePath initialPath, String... args)
    {
        this.addAll(initialPath);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    /**
     * Create a tre path.
     *
     * @param initialPathEntry The first entry in the path.
     * @param args             Additional path entries.
     */
    public TreePath(String initialPathEntry, String... args)
    {
        this.add(initialPathEntry);

        for (String pathElement : args)
        {
            if (!WWUtil.isEmpty(pathElement))
                this.add(pathElement);
        }
    }

    /**
     * Create a tree path from a list.
     *
     * @param initialPathEntries Entries in the path.
     */
    public TreePath(List<String> initialPathEntries)
    {
        this.addAll(initialPathEntries);
    }

    /**
     * Retrieves the a sub-section of this path from the first element to the second to last element.
     *
     * @return a new TreePath that contains the entries in this path, excluding the final entry.
     */
    public TreePath lastButOne()
    {
        return this.subPath(0, this.size() - 1);
    }

    /**
     * Retrieves a subsection of the path.
     *
     * @param start first index (inclusive) of the sub-path
     * @param end   last index (exclusive) of the sub-path
     *
     * @return A new path made up of path elements between {@code start} and {@code end}.
     */
    public TreePath subPath(int start, int end)
    {
        return new TreePath(this.subList(start, end));
    }

    /**
     * Determines if a path is empty.
     *
     * @param path Path to test.
     *
     * @return {@code true} if {@code path} contains no entries, {@code path} is {@code null}, or if the first entry of
     *         {@code path} is {@code null} or an empty string.
     */
    public static boolean isEmptyPath(TreePath path)
    {
        return path == null || path.size() == 0 || WWUtil.isEmpty(path.get(0));
    }

    @Override
    public String toString()
    {
        if (this.size() == 0)
            return "<empty path>";

        StringBuilder sb = new StringBuilder();

        for (String s : this)
        {
            if (WWUtil.isEmpty(s))
                s = "<empty>";

            if (sb.length() == 0)
                sb.append(s);
            else
                sb.append("/").append(s);
        }

        return sb.toString();
    }
}
