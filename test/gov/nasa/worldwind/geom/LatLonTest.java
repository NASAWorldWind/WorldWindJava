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

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class LatLonTest
{
    private final static double DISTANCE_THRESHOLD = 1e-10;
    private final static double AZIMUTH_THRESHOLD = 1e-5;
    private final static double TOLERANCE = 0.1;

    private Globe globe;

    @BeforeEach
    public void setUp()
    {
        this.globe = new Earth();
    }

    @AfterEach
    public void tearDown()
    {
        this.globe = null;
    }

    //////////////////////////////////////////////////////////
    // Test equivalent points. Distance should always be 0.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 0.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points A");
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, -180.0);
        LatLon end = LatLon.fromDegrees(0.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points B");
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 360.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points C");
    }

    @Test
    public void testGreatCircleDistance_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Equivalent points");
    }

    //////////////////////////////////////////////////////////
    // Test antipodal points. Distance should always be 180.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Trivial antipodal points A");
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Trivial antipodal points B");
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsC()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, -180.0);
        LatLon end = LatLon.fromDegrees(90.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Trivial antipodal points C");
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Antipodal points A");
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(12.0, -93.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Antipodal points B");
    }

    //////////////////////////////////////////////////////////
    // Test points known to be a certain angular distance apart.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_KnownDistance()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 45.0);
        LatLon end = LatLon.fromDegrees(36.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(54.0, distance, DISTANCE_THRESHOLD, "Known spherical distance");
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseToZero()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(1.3988468832247915e-7, distance, DISTANCE_THRESHOLD, "Known spherical distance (close to zero)");
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseTo180()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(180.0, distance, DISTANCE_THRESHOLD, "Known spherical distance (close to 180)");
    }

    //////////////////////////////////////////////////////////
    // Test points that have caused problems.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_ProblemPointsA()
    {
        LatLon begin = LatLon.fromDegrees(36.0, -118.0);
        LatLon end = LatLon.fromDegrees(36.0, -117.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals(0.8090134466773318, distance, DISTANCE_THRESHOLD, "Problem points A");
    }

    //////////////////////////////////////////////////////////
    // Test trivial Azimuth angles.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleAzimuth_TrivialNorth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(90, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial North greatCircleAzimuth");
    }

    @Test
    public void testGreatCircleAzimuth_TrivialEast()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 90.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(90.0, azimuth, AZIMUTH_THRESHOLD, "Trivial East greatCircleAzimuth");
    }

    @Test
    public void testGreatCircleAzimuth_TrivialSouth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(-90.0, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(180.0, azimuth, AZIMUTH_THRESHOLD, "Trivial South greatCircleAzimuth");
    }

    @Test
    public void testGreatCircleAzimuth_TrivialWest()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, -90.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(-90.0, azimuth, AZIMUTH_THRESHOLD, "Trivial West greatCircleAzimuth");
    }

    //////////////////////////////////////////////////////////
    // Test Azimuth angles between equivalent points.
    // Azimuth should always be 0 or 360.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleAzimuth_TrivialEquivalentPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial equivalent points A");
    }

    //@Test
    //@Test public void testGreatCircleAzimuth_TrivialEquivalentPointsB()
    //{
    //    LatLon begin = LatLon.fromDegrees(0.0, -180.0);
    //    LatLon end   = LatLon.fromDegrees(0.0, 180.0);
    //    double greatCircleAzimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
    //    assertEquals(0.0, greatCircleAzimuth, THRESHOLD, "Trivial equivalent points B");
    //}

    @Test
    public void testGreatCircleAzimuth_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial equivalent points C");
    }

    //@Test
    //@Test public void testGreatCircleAzimuth_TrivialEquivalentPointsD()
    //{
    //    LatLon begin = LatLon.fromDegrees(90.0, 0.0);
    //    LatLon end   = LatLon.fromDegrees(90.0, 45.0);
    //    double greatCircleAzimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
    //    assertEquals(0.0, greatCircleAzimuth, THRESHOLD, "Trivial equivalent points D");
    //}

    @Test
    public void testGreatCircleAzimuth_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Equivalent points");
    }

    //////////////////////////////////////////////////////////
    // Test points known to have a certain Azimuth.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, -180.0);
        LatLon end = LatLon.fromDegrees(90.0, 180.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth A");
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(-90.0, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth B");
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(-135.6329170237546, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth C");
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(135.6329170162944, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth D");
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthE()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(53.0902505, -67.1064558);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals(-21.38356223882703, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth E");
    }

    //////////////////////////////////////////////////////////
    // Test trivial Azimuths and distances.
    // End point should be equivalent to begin point.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleEndPosition_TrivialDistanceA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial distance A (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial distance A (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_TrivialDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial distance B (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial distance B (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth A (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth A (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth B (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth B (lon)");
    }

    //////////////////////////////////////////////////////////
    // Test antipodal points.
    // End point should be antipodal to begin point.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleEndPosition_TrivialAntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points A (lat)");
        assertEquals(180.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points A (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points B (lat)");
        assertEquals(180.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points B (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAntipodalPointsC()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(90.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points C (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial antipodal points C (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_AntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(-53.0902505, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Antipodal points A (lat)");
        assertEquals(-67.1064558, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Antipodal points A (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_AntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        double azimuthRadians = Math.toRadians(-90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(12.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Antipodal points B (lat)");
        assertEquals(-93.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Antipodal points B (lon)");
    }

    //////////////////////////////////////////////////////////
    // Test known points.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleEndPosition_KnownPointsA()
    {
        LatLon begin = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuthRadians = Math.toRadians(15.2204311);
        double distanceRadians = Math.toRadians(-88.7560694);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(-36.63477988750917, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Known points A (lat)");
        assertEquals(131.98550742812412, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Known points A (lon)");
    }

    @Test
    public void testGreatCircleEndPosition_KnownPointsB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-68.4055227);
        double distanceRadians = Math.toRadians(10.53630354);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(55.7426290038835, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Known points B (lat)");
        assertEquals(95.313127193979270, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Known points B (lon)");
    }

    //////////////////////////////////////////////////////////
    // Test equivalent points. Distance should always be 0.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbDistance_RhumbDistance_TrivialEquivalentPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 0.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points A");
    }

    @Test
    public void testRhumbDistance_TrivialEquivalentPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, -180.0);
        LatLon end = LatLon.fromDegrees(0.0, 180.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points B");
    }

    @Test
    public void testRhumbDistance_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 360.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Trivial equivalent points C");
    }

    @Test
    public void testRhumbDistance_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(0.0, distance, DISTANCE_THRESHOLD, "Equivalent points");
    }

    //////////////////////////////////////////////////////////
    // Test points known to be a certain angular distance apart.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbDistance_KnownDistance()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 45.0);
        LatLon end = LatLon.fromDegrees(36.0, 180.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(54.11143196539475, distance, 1e-5, "Known spherical distance"); // Custom threshold
    }

    @Test
    public void testRhumbDistance_KnownDistanceCloseToZero()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(1.398846933590201e-7, distance, DISTANCE_THRESHOLD, "Known spherical distance (close to zero)");
    }

    @Test
    public void testRhumbDistance_KnownDistanceCloseTo180()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(180.28382072652187, distance, DISTANCE_THRESHOLD, "Known spherical distance (close to 180)");
    }

    //////////////////////////////////////////////////////////
    // Test points that have caused problems.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbDistance_ProblemPointsA()
    {
        LatLon begin = LatLon.fromDegrees(36.0, -118.0);
        LatLon end = LatLon.fromDegrees(36.0, -117.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals(0.8090169943749475, distance, DISTANCE_THRESHOLD, "Problem points A");
    }

    //////////////////////////////////////////////////////////
    // Test trivial Azimuth angles.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbAzimuth_TrivialNorth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(90, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial North rhumbAzimuth");
    }

    @Test
    public void testRhumbAzimuth_TrivialEast()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 90.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(90.0, azimuth, AZIMUTH_THRESHOLD, "Trivial East rhumbAzimuth");
    }

    @Test
    public void testRhumbAzimuth_TrivialSouth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(-90.0, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(180.0, azimuth, AZIMUTH_THRESHOLD, "Trivial South rhumbAzimuth");
    }

    @Test
    public void testRhumbAzimuth_TrivialWest()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, -90.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(-90.0, azimuth, AZIMUTH_THRESHOLD, "Trivial West rhumbAzimuth");
    }

    //////////////////////////////////////////////////////////
    // Test Azimuth angles between equivalent points.
    // Azimuth should always be 0 or 360.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbAzimuth_TrivialEquivalentPointsA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial equivalent points A");
    }

    //@Test
    //@Test public void testRhumbAzimuth_TrivialEquivalentPointsB()
    //{
    //    LatLon begin = LatLon.fromDegrees(0.0, -180.0);
    //    LatLon end   = LatLon.fromDegrees(0.0, 180.0);
    //    double rhumbAzimuth = LatLon.rhumbAzimuth(begin, end).degrees;
    //    assertEquals(0.0, rhumbAzimuth, THRESHOLD, "Trivial equivalent points B");
    //}

    @Test
    public void testRhumbAzimuth_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Trivial equivalent points C");
    }

    //@Test
    //@Test public void testRhumbAzimuth_TrivialEquivalentPointsD()
    //{
    //    LatLon begin = LatLon.fromDegrees(90.0, 0.0);
    //    LatLon end   = LatLon.fromDegrees(90.0, 45.0);
    //    double rhumbAzimuth = LatLon.rhumbAzimuth(begin, end).degrees;
    //    assertEquals(0.0, rhumbAzimuth, THRESHOLD, "Trivial equivalent points D");
    //}

    @Test
    public void testRhumbAzimuth_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Equivalent points");
    }

    //////////////////////////////////////////////////////////
    // Test points known to have a certain Azimuth.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumbAzimuth_KnownAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, -180.0);
        LatLon end = LatLon.fromDegrees(90.0, 180.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(0.0, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth A");
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(-124.94048502315054, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth B");
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(-135.63291443992495, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth C");
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(82.34987931207793, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth D");
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthE()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(53.0902505, -67.1064558);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals(-64.05846977747626, azimuth, AZIMUTH_THRESHOLD, "Known Azimuth E");
    }

    //////////////////////////////////////////////////////////
    // Test trivial Azimuths and distances.
    // End point should be equivalent to begin point.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumEndPosition_TrivialDistanceA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial distance A (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial distance A (lon)");
    }

    @Test
    public void testRhumEndPosition_TrivialDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial distance B (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial distance B (lon)");
    }

    @Test
    public void testRhumEndPosition_TrivialAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth A (lat)");
        assertEquals(0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth A (lon)");
    }

    @Test
    public void testRhumEndPosition_TrivialAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Trivial Azimuth B (lat)");
        assertEquals(0.0, end.getLongitude().degrees, 1e-1, "Trivial Azimuth B (lon)"); // Custom threshold
    }

    //////////////////////////////////////////////////////////
    // Test known points.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumEndPosition_KnownPointsA()
    {
        LatLon begin = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuthRadians = Math.toRadians(15.2204311);
        double distanceRadians = Math.toRadians(88.7560694);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(32.55251684755035, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Known points A (lat)");
        assertEquals(-40.62266365697857, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Known points A (lon)");
    }

    @Test
    public void testRhumEndPosition_KnownPointsB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-68.4055227);
        double distanceRadians = Math.toRadians(10.53630354);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals(56.9679782407693, end.getLatitude().degrees, DISTANCE_THRESHOLD, "Known points B (lat)");
        assertEquals(95.78434282105843, end.getLongitude().degrees, DISTANCE_THRESHOLD, "Known points B (lon)");
    }

    //////////////////////////////////////////////////////////
    // Test problem points.
    //////////////////////////////////////////////////////////

    @Test
    public void testRhumEndPosition_ProblemPointsA()
    {
        // This specific lat/lon and distance identified a floating point error
        Angle initialLat = Angle.fromDegrees(4.076552742498428);
        Angle initialLon = Angle.fromDegrees(-21.377644877408443);
        Angle azimuth = Angle.fromDegrees(90.0);
        Angle distance = Angle.fromDegrees(8.963656110719409);
        LatLon begin = LatLon.fromRadians(initialLat.getRadians(), initialLon.getRadians());
        LatLon end = LatLon.rhumbEndPosition(begin, azimuth, distance);
        assertEquals(initialLat.getDegrees(), end.getLatitude().getDegrees(), DISTANCE_THRESHOLD, "Problem points A (lat)");
        assertEquals(-12.391252821313167, end.getLongitude().getDegrees(), DISTANCE_THRESHOLD, "Problem points A (lon)");
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceA()
    {
        LatLon begin = LatLon.fromDegrees(30.608879, -102.118357);
        LatLon end = LatLon.fromDegrees(34.413929, -97.022765);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(638027.750, distance, TOLERANCE, "Known ellipsoidal distance A");
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(9.2118, -79.5180);
        LatLon end = LatLon.fromDegrees(48.4216, -122.3352);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(5900926.896, distance, TOLERANCE, "Known ellipsoidal distance B");
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceC()
    {
        LatLon begin = LatLon.fromDegrees(-31.9236, 116.1231);
        LatLon end = LatLon.fromDegrees(23.6937, 121.9831);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(6186281.864, distance, TOLERANCE, "Known ellipsoidal distance C");
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceD()
    {
        LatLon begin = LatLon.fromDegrees(51.4898, 0.0539);
        LatLon end = LatLon.fromDegrees(42.3232, -71.0974);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(5296396.967, distance, TOLERANCE, "Known ellipsoidal distance D");
    }

    @Test
    public void testEllipsoidalDistance_Antipodal()
    {
        // See https://forum.worldwindcentral.com/showthread.php?45479-Potential-bug-in-ellipsoidalDistance
        LatLon begin = LatLon.fromDegrees(-12.720360910785889, 57.91244852568739);
        LatLon end = LatLon.fromDegrees(12.186856600402097, -121.90490684689753);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(1.9937004080007866E7, distance, TOLERANCE, "Antipodal");
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(30.000000, -102.000000);
        LatLon end = LatLon.fromDegrees(34.000000, -97.000000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(45.50583, theta.degrees, TOLERANCE, "Known ellipsoidal Azimuth A");
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(9.0000, -79.0000);
        LatLon end = LatLon.fromDegrees(48.0000, -122.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(Angle.normalizedLongitude(Angle.fromDegrees(325.10111)).degrees, theta.degrees, TOLERANCE, "Known ellipsoidal Azimuth B");
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-32.0000, 116.0000);
        LatLon end = LatLon.fromDegrees(23.0000, 122.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(6.75777, theta.degrees, TOLERANCE, "Known ellipsoidal Azimuth C");
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(51.5000, 0.0000);
        LatLon end = LatLon.fromDegrees(42.0000, -71.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals(Angle.normalizedLongitude(Angle.fromDegrees(287.95372)).degrees, theta.degrees, TOLERANCE, "Known ellipsoidal Azimuth D");
    }
}
