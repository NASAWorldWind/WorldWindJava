/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BasicQuadTreeTest
{
    /** Tests incremental removal of all items from the tree. */
    @Test
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
    @Test
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

    private static int countItemsInTree(BasicQuadTree<Integer> tree)
    {
        // Counts only unique items.
        Set<Integer> map = new HashSet<Integer>();

        for (Integer i : tree)
        {
            map.add(i);
        }

        return map.size();
    }
}
