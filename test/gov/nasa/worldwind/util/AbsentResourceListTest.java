/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Tests operation of AbsentResourceList.
 *
 * @author tag
 * @version $Id: AbsentResourceListTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbsentResourceListTest
{
    public static class Tests extends TestCase
    {
        protected void addResources(AbsentResourceList list, int numResources)
        {
            this.markResourcesAbsent(list, numResources);
        }

        protected void markResourcesAbsent(AbsentResourceList list, int numResources)
        {
            for (int i = 0; i < numResources; i++)
            {
                list.markResourceAbsent(i);
            }
        }

        protected void testResourcesAbsent(AbsentResourceList list, int numResources)
        {
            for (int i = 0; i < numResources; i++)
            {
                assertTrue("Resource " + i + " not considered absent ", list.isResourceAbsent(i));
            }
        }

        protected void testResourcesNotAbsent(AbsentResourceList list, int numResources)
        {
            for (int i = 0; i < numResources; i++)
            {
                assertTrue("Resource " + i + " considered absent ", !list.isResourceAbsent(i));
            }
        }

        /** Tests addition of resources to the list. */
        public void testResourceAddition()
        {
            int numResources = 100;
            AbsentResourceList list = new AbsentResourceList(numResources, 2);

            this.addResources(list, numResources);
            this.testResourcesAbsent(list, numResources);
        }

        /** Tests whether resources are considered not absent after initial check interval expires. */
        public void testCheckInitialInterval()
        {
            int numResources = 100;
            int checkInterval = 250;
            AbsentResourceList list = new AbsentResourceList(numResources, 2, checkInterval, 60000);

            try
            {
                this.addResources(list, numResources);
                this.testResourcesAbsent(list, numResources);
                Thread.sleep((long) (1.01 * checkInterval));
                this.testResourcesNotAbsent(list, numResources);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        /** Tests whether resources are considered absent after maximum number of tries. */
        public void testMaxTries()
        {
            int numResources = 100;
            int maxTries = 2;
            int checkInterval = 250;
            AbsentResourceList list = new AbsentResourceList(numResources, maxTries, checkInterval, 60000);

            // Mark resources absent max-tries times and ensure they're considered absent.
            for (int i = 0; i < maxTries; i++)
            {
                this.markResourcesAbsent(list, numResources);
            }
            this.testResourcesAbsent(list, numResources);

            // Increase max-tries and ensure the resources are now not absent. Must wait for check interval to expire.
            list.setMaxTries(maxTries + 1);
            try
            {
                Thread.sleep((long) (1.01 * checkInterval));
                this.testResourcesNotAbsent(list, numResources);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            // Mark them absent again and ensure that they're now considered absent.
            this.markResourcesAbsent(list, numResources);
            this.testResourcesAbsent(list, numResources);
        }

        /** Tests whether resources are considered not absent after try-again interval expires. */
        public void testCheckTryAgainInterval()
        {
            int numResources = 100;
            int maxTries = 2;
            int tryAgainInterval = 500;
            AbsentResourceList list = new AbsentResourceList(numResources, maxTries, 250, tryAgainInterval);

            for (int i = 0; i < maxTries; i++)
            {
                this.markResourcesAbsent(list, numResources);
            }

            try
            {
                Thread.sleep((long) (1.01 * tryAgainInterval));
                this.testResourcesNotAbsent(list, numResources);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        /** Tests whether a specified list size is adhered to. */
        public void testListSize()
        {
            int maxListSize = 100;
            AbsentResourceList list = new AbsentResourceList(maxListSize, 2, 250, 60000);

            this.markResourcesAbsent(list, maxListSize + 1); // should eject first resource, 0, from list
            assertTrue("Oldest resource not considered absent ", !list.isResourceAbsent(0));
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
