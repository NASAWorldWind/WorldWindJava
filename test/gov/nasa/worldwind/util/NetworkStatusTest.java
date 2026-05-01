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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.*;

import static org.junit.jupiter.api.Assertions.*;
public class NetworkStatusTest
{
    private BasicNetworkStatus netStat;

    @BeforeEach
    public void setUp()
    {
        this.netStat = new BasicNetworkStatus();
    }

    @AfterEach
    public void tearDown()
    {
    }

    @Test
    public void testSetAttemptLimit()
    {
        int limit = 5;
        this.netStat.setAttemptLimit(limit);
        int ai = this.netStat.getAttemptLimit();
        assertEquals(ai, limit, "Set attempt limit test ");
    }

    @Test
    public void testSetTryAgainInterval()
    {
        long interval = 200;
        this.netStat.setTryAgainInterval(interval);
        long tai = this.netStat.getTryAgainInterval();
        assertEquals(tai, interval, "Set try again interval test ");
    }

    @Disabled
    @Test
    public void testNetworkAvailable()
    {
        boolean tf = this.netStat.isNetworkUnavailable();
        assertFalse(tf, "Network unavailable test ");
    }

    @Disabled
    @Test
    public void testWorldWindAvailable()
    {
        boolean tf = this.netStat.isWorldWindServerUnavailable();
        assertFalse(tf, "WorldWind server unavailable test ");
    }

    @Test
    public void testHostAvailable() throws MalformedURLException
    {
        String hostName = "nasa.gov";

        boolean tf = this.netStat.isHostUnavailable(new URL("https://" + hostName + "/path?abc=123"));
        assertFalse(tf, "Host unavailable test ");
    }

    @Test
    public void testHostLimitReached() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.makeHostUnavailable(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertTrue(tf, "Host limit reached test ");
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
        assertFalse(tf, "Host limit not reached test ");
    }

    @Test
    public void testHostReavailable() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.makeHostUnavailable(url);
        this.netStat.logAvailableHost(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertFalse(tf, "Host reavailable test ");
    }

    @Test
    public void testHostTryAgain() throws MalformedURLException, InterruptedException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.netStat.setTryAgainInterval(100);
        this.makeHostUnavailable(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertTrue(tf, "Host try again test A");

        Thread.sleep(netStat.getTryAgainInterval());
        tf = this.netStat.isHostUnavailable(url);
        assertFalse(tf, "Host try again test B");
    }

    @Test
    public void testNetworkAvailableAfterSuccessLogged() throws MalformedURLException
    {
        String hostName = "nasa.gov";
        URL url = new URL("https://" + hostName + "/path?abc=123");

        this.makeHostUnavailable(url);
        this.netStat.logAvailableHost(url);
        boolean tf = this.netStat.isHostUnavailable(url);
        assertFalse(tf, "Network available after success test ");
    }

    private void makeHostUnavailable(URL url)
    {
        for (int i = 0; i <= this.netStat.getAttemptLimit(); i++)
        {
            this.netStat.logUnavailableHost(url);
        }
    }
}
