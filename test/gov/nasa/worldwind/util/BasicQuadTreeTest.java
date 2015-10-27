/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;
import junit.framework.*;
import junit.textui.TestRunner;

import java.util.*;

/**
 * @author tag
 * @version $Id: BasicQuadTreeTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicQuadTreeTest
{
    public static class Tests extends TestCase
    {
        protected int countItemsInTree(BasicQuadTree<Integer> tree)
        {
            // Counts only unique items.
            Set<Integer> map = new HashSet<Integer>();

            for (Integer i : tree)
            {
                map.add(i);
            }

            return map.size();
        }

        /** Tests incremental removal of all items from the tree. */
        public void testFullRemoval()
        {
            int numItems = 1000;
            BasicQuadTree<Integer> tree = new BasicQuadTree<Integer>(5, Sector.FULL_SPHERE, null);

            for (int i = 1; i <= numItems; i++)
            {
                tree.add(i, new double[] {i % 90, i % 180}, Integer.toString(i));
            }
            assertEquals("Item count incorrect at start ", countItemsInTree(tree), numItems);

            // Remove icons one at a time then verify the count.
            for (int i = numItems; i > 0; i--)
            {
                tree.remove(i);
                assertEquals("Item count incorrect ", countItemsInTree(tree), i - 1);
            }
        }

        /** Tests removal of named items from the tree. */
        public void testIndividualRemoval()
        {
            int numItems = 1000;
            BasicQuadTree<Integer> tree = new BasicQuadTree<Integer>(5, Sector.FULL_SPHERE, null);

            for (int i = 1; i <= numItems; i++)
            {
                tree.add(i, new double[] {i % 90, i % 180}, Integer.toString(i));
            }

            // Remove icons one at a time and verify removal.
            for (int i = numItems; i > 0; i--)
            {
                tree.removeByName(Integer.toString(i));
                Integer item = tree.getByName(Integer.toString(i));
                assertNull("Item not fully removed from tree ", item);
            }
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
