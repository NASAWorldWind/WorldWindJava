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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class AbsentResourceListTest
{
    /** Tests addition of resources to the list. */
    @Test
    public void testResourceAddition() throws InterruptedException
    {
        int numResources = 100;
        int checkInterval = 250;
        AbsentResourceList list = new AbsentResourceList(numResources, 2, checkInterval, 60000);

        addResources(list, numResources);
        assertResourcesAbsent(list, numResources);
    }

    /** Tests whether resources are considered not absent after initial check interval expires. */
    @Test
    public void testCheckInitialInterval()
    {
        int numResources = 100;
        int checkInterval = 250;
        AbsentResourceList list = new AbsentResourceList(numResources, 2, checkInterval, 60000);

        try
        {
            addResources(list, numResources);
            assertResourcesAbsent(list, numResources);
            Thread.sleep((long) (1.01 * checkInterval));
            assertResourcesNotAbsent(list, numResources);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /** Tests whether resources are considered absent after maximum number of tries. */
    @Test
    public void testMaxTries()
    {
        int numResources = 100;
        int maxTries = 2;
        int checkInterval = 250;
        AbsentResourceList list = new AbsentResourceList(numResources, maxTries, checkInterval, 60000);

        // Mark resources absent max-tries times and ensure they're considered absent.
        for (int i = 0; i < maxTries; i++)
        {
            markResourcesAbsent(list, numResources);
        }
        assertResourcesAbsent(list, numResources);

        // Increase max-tries and ensure the resources are now not absent. Must wait for check interval to expire.
        list.setMaxTries(maxTries + 1);
        try
        {
            Thread.sleep((long) (1.01 * checkInterval));
            assertResourcesNotAbsent(list, numResources);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // Mark them absent again and ensure that they're now considered absent.
        markResourcesAbsent(list, numResources);
        assertResourcesAbsent(list, numResources);
    }

    /** Tests whether resources are considered not absent after try-again interval expires. */
    @Test
    public void testCheckTryAgainInterval()
    {
        int numResources = 100;
        int maxTries = 2;
        int tryAgainInterval = 500;
        AbsentResourceList list = new AbsentResourceList(numResources, maxTries, 250, tryAgainInterval);

        for (int i = 0; i < maxTries; i++)
        {
            markResourcesAbsent(list, numResources);
        }

        try
        {
            Thread.sleep((long) (1.01 * tryAgainInterval));
            assertResourcesNotAbsent(list, numResources);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /** Tests whether a specified list size is adhered to. */
    @Test
    public void testListSize()
    {
        int maxListSize = 100;
        AbsentResourceList list = new AbsentResourceList(maxListSize, 2, 250, 60000);

        markResourcesAbsent(list, maxListSize + 1); // should eject first resource, 0, from list
        assertTrue("Oldest resource not considered absent ", !list.isResourceAbsent(0));
    }

    private static void addResources(AbsentResourceList list, int numResources)
    {
        markResourcesAbsent(list, numResources);
    }

    private static void markResourcesAbsent(AbsentResourceList list, int numResources)
    {
        for (int i = 0; i < numResources; i++)
        {
            list.markResourceAbsent(i);
        }
    }

    private static void assertResourcesAbsent(AbsentResourceList list, int numResources)
    {
        for (int i = 0; i < numResources; i++)
        {
            assertTrue("Resource " + i + " not considered absent ", list.isResourceAbsent(i));
        }
    }

    private static void assertResourcesNotAbsent(AbsentResourceList list, int numResources)
    {
        for (int i = 0; i < numResources; i++)
        {
            assertTrue("Resource " + i + " considered absent ", !list.isResourceAbsent(i));
        }
    }
}
