/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class NetworkStatusTest
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

    @Test
    public void testSetAttemptLimit()
    {
        int limit = 5;
        this.netStat.setAttemptLimit(limit);
        int ai = this.netStat.getAttemptLimit();
        assertEquals("Set attempt limit test ", ai, limit);
    }

    @Test
    public void testSetTryAgainInterval()
    {
        long interval = 200;
        this.netStat.setTryAgainInterval(interval);
        long tai = this.netStat.getTryAgainInterval();
        assertEquals("Set try again interval test ", tai, interval);
    }

    @Ignore
    @Test
    public void testNetworkAvailable()
    {
        boolean tf = this.netStat.isNetworkUnavailable();
        assertFalse("Network unavailable test ", tf);
    }

    @Ignore
    @Test
    public void testWorldWindAvailable()
    {
        boolean tf = this.netStat.isWorldWindServerUnavailable();
        assertFalse("WorldWind server unavailable test ", tf);
    }

    @Test
    public void testHostAvailable() throws MalformedURLException
    {
        String hostName = "nasa.gov";

        boolean tf = this.netStat.isHostUnavailable(new URL("https://" + hostName + "/path?abc=123"));
        assertFalse("Host unavailable test ", tf);
    }

    @Test
    public void testHostLimitReached() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.makeHostUnavailable(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertTrue("Host limit reached test ", tf);
    }

    @Test
    public void testHostLimitNotReached() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        for (int i = 0; i < this.netStat.getAttemptLimit() - 1; i++)
        {
            this.netStat.logUnavailableHost(url);
        }

        boolean tf = this.netStat.isHostUnavailable(url);
        assertFalse("Host limit not reached test ", tf);
    }

    @Test
    public void testHostReavailable() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.makeHostUnavailable(url);
        this.netStat.logAvailableHost(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertFalse("Host reavailable test ", tf);
    }

    @Test
    public void testHostTryAgain() throws MalformedURLException, InterruptedException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.netStat.setTryAgainInterval(100);
        this.makeHostUnavailable(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertTrue("Host try again test A", tf);

        Thread.sleep(netStat.getTryAgainInterval());
        tf = this.netStat.isHostUnavailable(url);
        assertFalse("Host try again test B", tf);
    }

    @Test
    public void testNetworkAvailableAfterSuccessLogged() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

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
