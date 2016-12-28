/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class LatLonTest
{
    private final static double DISTANCE_THRESHOLD = 1e-10;
    private final static double AZIMUTH_THRESHOLD = 1e-5;
    private final static double TOLERANCE = 0.1;

    private Globe globe;

    @Before
    public void setUp()
    {
        this.globe = new Earth();
    }

    @After
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
        assertEquals("Trivial equivalent points A", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, -180.0);
        LatLon end = LatLon.fromDegrees(0.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Trivial equivalent points B", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 360.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Trivial equivalent points C", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Equivalent points", 0.0, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Trivial antipodal points A", 180.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Trivial antipodal points B", 180.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsC()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, -180.0);
        LatLon end = LatLon.fromDegrees(90.0, 180.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Trivial antipodal points C", 180.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Antipodal points A", 180.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(12.0, -93.0);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Antipodal points B", 180.0, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Known spherical distance", 54.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseToZero()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Known spherical distance (close to zero)", 1.3988468832247915e-7, distance,
            DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseTo180()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double distance = LatLon.greatCircleDistance(begin, end).degrees;
        assertEquals("Known spherical distance (close to 180)", 180.0, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Problem points A", 0.8090134466773318, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Trivial North greatCircleAzimuth", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_TrivialEast()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 90.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Trivial East greatCircleAzimuth", 90.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_TrivialSouth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(-90.0, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Trivial South greatCircleAzimuth", 180.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_TrivialWest()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, -90.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Trivial West greatCircleAzimuth", -90.0, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Trivial equivalent points A", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    //@Test
    //@Test public void testGreatCircleAzimuth_TrivialEquivalentPointsB()
    //{
    //    LatLon begin = LatLon.fromDegrees(0.0, -180.0);
    //    LatLon end   = LatLon.fromDegrees(0.0, 180.0);
    //    double greatCircleAzimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
    //    assertEquals("Trivial equivalent points B", 0.0, greatCircleAzimuth, THRESHOLD);
    //}

    @Test
    public void testGreatCircleAzimuth_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Trivial equivalent points C", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    //@Test
    //@Test public void testGreatCircleAzimuth_TrivialEquivalentPointsD()
    //{
    //    LatLon begin = LatLon.fromDegrees(90.0, 0.0);
    //    LatLon end   = LatLon.fromDegrees(90.0, 45.0);
    //    double greatCircleAzimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
    //    assertEquals("Trivial equivalent points D", 0.0, greatCircleAzimuth, THRESHOLD);
    //}

    @Test
    public void testGreatCircleAzimuth_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Equivalent points", 0.0, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Known Azimuth A", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth B", -90.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth C", -135.6329170237546, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth D", 135.6329170162944, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_KnownAzimuthE()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(53.0902505, -67.1064558);
        double azimuth = LatLon.greatCircleAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth E", -21.38356223882703, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Trivial distance A (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial distance A (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_TrivialDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial distance B (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial distance B (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial Azimuth A (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial Azimuth A (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial Azimuth B (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial Azimuth B (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
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
        assertEquals("Trivial antipodal points A (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial antipodal points A (lon)", 180.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial antipodal points B (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial antipodal points B (lon)", 180.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_TrivialAntipodalPointsC()
    {
        LatLon begin = LatLon.fromDegrees(-90.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial antipodal points C (lat)", 90.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial antipodal points C (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_AntipodalPointsA()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Antipodal points A (lat)", -53.0902505, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Antipodal points A (lon)", -67.1064558, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_AntipodalPointsB()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        double azimuthRadians = Math.toRadians(-90.0);
        double distanceRadians = Math.toRadians(180.0);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Antipodal points B (lat)", 12.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Antipodal points B (lon)", -93.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
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
        assertEquals("Known points A (lat)", -36.63477988750917, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Known points A (lon)", 131.98550742812412, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testGreatCircleEndPosition_KnownPointsB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-68.4055227);
        double distanceRadians = Math.toRadians(10.53630354);
        LatLon end = LatLon.greatCircleEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Known points B (lat)", 55.7426290038835, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Known points B (lon)", 95.313127193979270, end.getLongitude().degrees, DISTANCE_THRESHOLD);
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
        assertEquals("Trivial equivalent points A", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumbDistance_TrivialEquivalentPointsB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, -180.0);
        LatLon end = LatLon.fromDegrees(0.0, 180.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals("Trivial equivalent points B", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumbDistance_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 360.0);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals("Trivial equivalent points C", 0.0, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumbDistance_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals("Equivalent points", 0.0, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Known spherical distance", 54.11143196539475, distance, 1e-5); // Custom threshold
    }

    @Test
    public void testRhumbDistance_KnownDistanceCloseToZero()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals("Known spherical distance (close to zero)", 1.398846933590201e-7, distance, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumbDistance_KnownDistanceCloseTo180()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double distance = LatLon.rhumbDistance(begin, end).degrees;
        assertEquals("Known spherical distance (close to 180)", 180.28382072652187, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Problem points A", 0.8090169943749475, distance, DISTANCE_THRESHOLD);
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
        assertEquals("Trivial North rhumbAzimuth", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_TrivialEast()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, 90.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Trivial East rhumbAzimuth", 90.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_TrivialSouth()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(-90.0, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Trivial South rhumbAzimuth", 180.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_TrivialWest()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        LatLon end = LatLon.fromDegrees(0.0, -90.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Trivial West rhumbAzimuth", -90.0, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Trivial equivalent points A", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    //@Test
    //@Test public void testRhumbAzimuth_TrivialEquivalentPointsB()
    //{
    //    LatLon begin = LatLon.fromDegrees(0.0, -180.0);
    //    LatLon end   = LatLon.fromDegrees(0.0, 180.0);
    //    double rhumbAzimuth = LatLon.rhumbAzimuth(begin, end).degrees;
    //    assertEquals("Trivial equivalent points B", 0.0, rhumbAzimuth, THRESHOLD);
    //}

    @Test
    public void testRhumbAzimuth_TrivialEquivalentPointsC()
    {
        LatLon begin = LatLon.fromDegrees(90.0, 0.0);
        LatLon end = LatLon.fromDegrees(90.0, 0.0);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Trivial equivalent points C", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    //@Test
    //@Test public void testRhumbAzimuth_TrivialEquivalentPointsD()
    //{
    //    LatLon begin = LatLon.fromDegrees(90.0, 0.0);
    //    LatLon end   = LatLon.fromDegrees(90.0, 45.0);
    //    double rhumbAzimuth = LatLon.rhumbAzimuth(begin, end).degrees;
    //    assertEquals("Trivial equivalent points D", 0.0, rhumbAzimuth, THRESHOLD);
    //}

    @Test
    public void testRhumbAzimuth_EquivalentPoints()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Equivalent points", 0.0, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Known Azimuth A", 0.0, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        LatLon end = LatLon.fromDegrees(-53.0902505, -67.1064558);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth B", -124.94048502315054, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(-12.0000001, 86.9999999);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth C", -135.63291443992495, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(11.9999999, -93.0000001);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth D", 82.34987931207793, azimuth, AZIMUTH_THRESHOLD);
    }

    @Test
    public void testRhumbAzimuth_KnownAzimuthE()
    {
        LatLon begin = LatLon.fromDegrees(-12.0, 87.0);
        LatLon end = LatLon.fromDegrees(53.0902505, -67.1064558);
        double azimuth = LatLon.rhumbAzimuth(begin, end).degrees;
        assertEquals("Known Azimuth E", -64.05846977747626, azimuth, AZIMUTH_THRESHOLD);
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
        assertEquals("Trivial distance A (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial distance A (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumEndPosition_TrivialDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(0.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial distance B (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial distance B (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumEndPosition_TrivialAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(0.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial Azimuth A (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial Azimuth A (lon)", 0.0, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumEndPosition_TrivialAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(0.0, 0.0);
        double azimuthRadians = Math.toRadians(90.0);
        double distanceRadians = Math.toRadians(360.0);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Trivial Azimuth B (lat)", 0.0, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Trivial Azimuth B (lon)", 0.0, end.getLongitude().degrees,
            1e-1); // Custom threshold
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
        assertEquals("Known points A (lat)", 32.55251684755035, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Known points A (lon)", -40.62266365697857, end.getLongitude().degrees, DISTANCE_THRESHOLD);
    }

    @Test
    public void testRhumEndPosition_KnownPointsB()
    {
        LatLon begin = LatLon.fromDegrees(53.0902505, 112.8935442);
        double azimuthRadians = Math.toRadians(-68.4055227);
        double distanceRadians = Math.toRadians(10.53630354);
        LatLon end = LatLon.rhumbEndPosition(begin, azimuthRadians, distanceRadians);
        assertEquals("Known points B (lat)", 56.9679782407693, end.getLatitude().degrees, DISTANCE_THRESHOLD);
        assertEquals("Known points B (lon)", 95.78434282105843, end.getLongitude().degrees, DISTANCE_THRESHOLD);
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
        assertEquals("Problem points A (lat)", initialLat.getDegrees(), end.getLatitude().getDegrees(),
            DISTANCE_THRESHOLD);
        assertEquals("Problem points A (lon)", -12.391252821313167, end.getLongitude().getDegrees(),
            DISTANCE_THRESHOLD);
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceA()
    {
        LatLon begin = LatLon.fromDegrees(30.608879, -102.118357);
        LatLon end = LatLon.fromDegrees(34.413929, -97.022765);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal distance A", 638027.750, distance, TOLERANCE);
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceB()
    {
        LatLon begin = LatLon.fromDegrees(9.2118, -79.5180);
        LatLon end = LatLon.fromDegrees(48.4216, -122.3352);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal distance B", 5900926.896, distance, TOLERANCE);
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceC()
    {
        LatLon begin = LatLon.fromDegrees(-31.9236, 116.1231);
        LatLon end = LatLon.fromDegrees(23.6937, 121.9831);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal distance C", 6186281.864, distance, TOLERANCE);
    }

    @Test
    public void testEllipsoidalDistance_KnownDistanceD()
    {
        LatLon begin = LatLon.fromDegrees(51.4898, 0.0539);
        LatLon end = LatLon.fromDegrees(42.3232, -71.0974);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal distance D", 5296396.967, distance, TOLERANCE);
    }

    @Test
    public void testEllipsoidalDistance_Antipodal()
    {
        // See https://forum.worldwindcentral.com/showthread.php?45479-Potential-bug-in-ellipsoidalDistance
        LatLon begin = LatLon.fromDegrees(-12.720360910785889, 57.91244852568739);
        LatLon end = LatLon.fromDegrees(12.186856600402097, -121.90490684689753);
        double distance = LatLon.ellipsoidalDistance(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Antipodal", 1.9937004080007866E7, distance, TOLERANCE);
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthA()
    {
        LatLon begin = LatLon.fromDegrees(30.000000, -102.000000);
        LatLon end = LatLon.fromDegrees(34.000000, -97.000000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal Azimuth A", 45.50583, theta.degrees, TOLERANCE);
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthB()
    {
        LatLon begin = LatLon.fromDegrees(9.0000, -79.0000);
        LatLon end = LatLon.fromDegrees(48.0000, -122.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal Azimuth B", Angle.normalizedLongitude(Angle.fromDegrees(325.10111)).degrees,
            theta.degrees, TOLERANCE);
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthC()
    {
        LatLon begin = LatLon.fromDegrees(-32.0000, 116.0000);
        LatLon end = LatLon.fromDegrees(23.0000, 122.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal Azimuth C", 6.75777, theta.degrees, TOLERANCE);
    }

    @Test
    public void testEllipsoidalForwardAzimuth_KnownAzimuthD()
    {
        LatLon begin = LatLon.fromDegrees(51.5000, 0.0000);
        LatLon end = LatLon.fromDegrees(42.0000, -71.0000);
        Angle theta = LatLon.ellipsoidalForwardAzimuth(begin, end, globe.getEquatorialRadius(),
            globe.getPolarRadius());
        assertEquals("Known ellipsoidal Azimuth D", Angle.normalizedLongitude(Angle.fromDegrees(287.95372)).degrees,
            theta.degrees, TOLERANCE);
    }
}
