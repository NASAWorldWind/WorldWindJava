/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.*;

import java.net.*;

/**
 * @author tag
 * @version $Id: NetworkStatusTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NetworkStatusTest
{
    public static class Tests extends TestCase
    {
        private BasicNetworkStatus netStat;

        @Before
        public void setUp()
        {
            this.netStat = new BasicNetworkStatus();
        }

        @After
        public void tearDown()
        {
        }

        public void testSetAttemptLimit()
        {
            int limit = 5;
            this.netStat.setAttemptLimit(limit);
            int ai = this.netStat.getAttemptLimit();
            assertEquals("Set attempt limit test ", ai, limit);
        }

        public void testSetTryAgainInterval()
        {
            long interval = 200;
            this.netStat.setTryAgainInterval(interval);
            long tai = this.netStat.getTryAgainInterval();
            assertEquals("Set try again interval test ", tai, interval);
        }

        public void testNetworkAvailable()
        {
            boolean tf = this.netStat.isNetworkUnavailable();
            assertFalse("Network unavailable test ", tf);
        }

        public void testWorldWindAvailable()
        {
            boolean tf = this.netStat.isWorldWindServerUnavailable();
            assertFalse("World Wind server unavailable test ", tf);
        }

        public void testHostAvailable() throws MalformedURLException
        {
            String hostName = "nasa.gov";

            boolean tf = this.netStat.isHostUnavailable(new URL("http://" + hostName + "/path?abc=123"));
            assertFalse("Host unavailable test ", tf);
        }

        public void testHostLimitReached() throws MalformedURLException
        {
            String hostName = "nasa.gov";
            URL url = new URL("http://" + hostName + "/path?abc=123");

            this.makeHostUnavailable(url);
            boolean tf = this.netStat.isHostUnavailable(url);
            assertTrue("Host limit reached test ", tf);
        }

        public void testHostLimitNotReached() throws MalformedURLException
        {
            String hostName = "nasa.gov";
            URL url = new URL("http://" + hostName + "/path?abc=123");

            for (int i = 0; i < this.netStat.getAttemptLimit() - 1; i++)
            {
                this.netStat.logUnavailableHost(url);
            }

            boolean tf = this.netStat.isHostUnavailable(url);
            assertFalse("Host limit not reached test ", tf);
        }

        public void testHostReavailable() throws MalformedURLException
        {
            String hostName = "nasa.gov";
            URL url = new URL("http://" + hostName + "/path?abc=123");

            this.makeHostUnavailable(url);
            this.netStat.logAvailableHost(url);
            boolean tf = this.netStat.isHostUnavailable(url);
            assertFalse("Host reavailable test ", tf);
        }

        public void testHostTryAgain() throws MalformedURLException, InterruptedException
        {
            String hostName = "nasa.gov";
            URL url = new URL("http://" + hostName + "/path?abc=123");

            this.netStat.setTryAgainInterval(100);
            this.makeHostUnavailable(url);
            boolean tf = this.netStat.isHostUnavailable(url);
            assertTrue("Host try again test A", tf);

            Thread.sleep(netStat.getTryAgainInterval());
            tf = this.netStat.isHostUnavailable(url);
            assertFalse("Host try again test B", tf);
        }

        public void testNetworkAvailableAfterSuccessLogged() throws MalformedURLException
        {
            String hostName = "nasa.gov";
            URL url = new URL("http://" + hostName + "/path?abc=123");

            this.makeHostUnavailable(url);
            this.netStat.logAvailableHost(url);
            boolean tf = this.netStat.isHostUnavailable(url);
            assertFalse("Network available after success test ", tf);
        }

        private void makeHostUnavailable(URL url)
        {
            for (int i = 0; i <= this.netStat.getAttemptLimit(); i++)
            {
                this.netStat.logUnavailableHost(url);
            }
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
