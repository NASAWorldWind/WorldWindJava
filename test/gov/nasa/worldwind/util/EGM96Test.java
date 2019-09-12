/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Angle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EGM96Test
{
    /**
     * The acceptable difference two double values may have and still satisfy an {@code assertEquals} method.
     */
    private static final double DELTA = 1e-6;

    /**
     * The EGM96 data path.
     */
    private static final String OFFSETS_FILE_PATH = "config/EGM96.dat";

    /**
     * Tests the determination of the EGM offset value using a latitude value that should match a grid point.
     */
    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testGetOffset_VerticalInterpolationTopGridPoint() throws IOException
    {

        // New EGM96 instance with EGM96 dataset
        EGM96 egm96 = new EGM96(OFFSETS_FILE_PATH);
        // The EGM96 data has an interval of 0.25 degrees in both the horizontal and vertical dimensions. To test and
        // demonstrate the fractional value being determined is correct, this setup will isolate a vertical
        // interpolation by landing on the horizontal grid points (e.g. longitudes ending with 0.0, 0.25, 0.5, 0.75)
        Angle longitude = Angle.fromDegrees(-105.0);
        // This is a non-interpolated baseline latitude for the top grid point of our testing points
        Angle latitude = Angle.fromDegrees(38.75);

        // Find the row and column values using the identical method the getOffset method uses
        // Code below is directly copied from the getOffset method accept where static class references were added
        double lat = latitude.degrees;
        double lon = longitude.degrees >= 0 ? longitude.degrees : longitude.degrees + 360;
        int topRow = (int) ((90 - lat) / EGM96.INTERVAL.degrees);
        if (lat <= -90)
            topRow = EGM96.NUM_ROWS - 2;
        int bottomRow = topRow + 1;
        // Note that the number of columns does not repeat the column at 0 longitude, so we must force the right
        // column to 0 for any longitude that's less than one interval from 360, and force the left column to the
        // last column of the grid.
        int leftCol = (int) (lon / EGM96.INTERVAL.degrees);
        int rightCol = leftCol + 1;
        if (lon >= 360 - EGM96.INTERVAL.degrees)
        {
            leftCol = EGM96.NUM_COLS - 1;
            rightCol = 0;
        }
        // Determine the functions determination of the top lat and left lon
        double latTop = 90 - topRow * EGM96.INTERVAL.degrees;
        double lonLeft = leftCol * EGM96.INTERVAL.degrees;

        // Ensure the top latitude matches our expected latitude
        // This shows that the method has determined a row and column to query the dataset that corresponds with our
        // latitude value
        assertEquals("latitude matches after index conversion", latitude.degrees, latTop, DELTA);

        // Using the confirmed latitude value from above (via the topRow and leftCol values), find the actual node value
        double latGridPointOffset = egm96.gePostOffset(topRow, leftCol) / 100d; // the other method converts to meters
        // Use the interpolation method to determine the offset value
        double latOffset = egm96.getOffset(latitude, longitude);

        // Ensure that they are equal
        assertEquals("interpolated matches actual latitude", latGridPointOffset, latOffset, DELTA);
    }

    /**
     * Tests the determination of the EGM offset value using a latitude value between grid points. This method will use
     * the bilinear interpolation method to calculate the offset value.
     */
    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testGetOffset_VerticalInterpolationPoint() throws IOException
    {

        // New EGM96 instance
        EGM96 egm96 = new EGM96(OFFSETS_FILE_PATH);
        // The EGM96 data has an interval of 0.25 degrees in both the horizontal and vertical dimensions. To test and
        // demonstrate the fractional value being determined is correct, this setup will isolate a vertical
        // interpolation by landing on the horizontal grid points (e.g. longitudes ending with .0, 0.25, 0.5, 0.75)
        Angle longitude = Angle.fromDegrees(-105.0);
        // This is a non-interpolated baseline latitude for the top grid point of our testing points, it is closer to
        // the top grid point
        Angle latitude = Angle.fromDegrees(38.72);

        // Find the row and column values using the identical method the getOffset method uses
        // Code below is directly copied from the getOffset method accept where static class references were added
        double lat = latitude.degrees;
        double lon = longitude.degrees >= 0 ? longitude.degrees : longitude.degrees + 360;
        int topRow = (int) ((90 - lat) / EGM96.INTERVAL.degrees);
        if (lat <= -90)
            topRow = EGM96.NUM_ROWS - 2;
        int bottomRow = topRow + 1;
        // Note that the number of columns does not repeat the column at 0 longitude, so we must force the right
        // column to 0 for any longitude that's less than one interval from 360, and force the left column to the
        // last column of the grid.
        int leftCol = (int) (lon / EGM96.INTERVAL.degrees);
        int rightCol = leftCol + 1;
        if (lon >= 360 - EGM96.INTERVAL.degrees)
        {
            leftCol = EGM96.NUM_COLS - 1;
            rightCol = 0;
        }
        // Determine the functions determination of the top lat and left lon
        double latTop = 90 - topRow * EGM96.INTERVAL.degrees;
        // Need the bottom grid value for our own linear interpolation determination
        double latBottom = 90 - bottomRow * EGM96.INTERVAL.degrees;
        double lonLeft = leftCol * EGM96.INTERVAL.degrees;

        // Find the offset values of the top and bottom grid points
        double bottomOffsetValue = egm96.gePostOffset(bottomRow, leftCol) / 100d;
        double topOffsetValue = egm96.gePostOffset(topRow, leftCol) / 100d;

        // Ensure the top latitude matches our expected latitude
        // This shows that the method has determined a row and column to query the dataset that corresponds with our
        // latitude value
        assertEquals("top latitude matches after index conversion", 38.75, latTop, DELTA);
        assertEquals("bottom latitude matches after index conversion", 38.5, latBottom, DELTA);

        // The calculated EGM96 offset
        double latOffset = egm96.getOffset(latitude, longitude);
        double manuallyCalculatedV = (lat - latBottom) / (latTop - latBottom);
        double manuallyCalculatedInterpolationValue = (topOffsetValue - bottomOffsetValue) * manuallyCalculatedV
            + bottomOffsetValue;

        // Ensure that they are equal
        assertEquals("interpolated matches actual latitude", manuallyCalculatedInterpolationValue, latOffset, DELTA);
    }

    /**
     * Tests the determination of the EGM offset value using a longitude value that should match a grid point.
     */
    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testGetOffset_HorizontalInterpolationLeftGridPoint() throws IOException
    {

        // New EGM96 instance
        EGM96 egm96 = new EGM96(OFFSETS_FILE_PATH);
        // The EGM96 data has an interval of 0.25 degrees in both the horizontal and vertical dimensions. To test and
        // demonstrate the fractional value being determined is correct, this setup will isolate a horizontal
        // interpolation by landing on the vertical grid points (e.g. latitudes ending with .0, 0.25, 0.5, 0.75)
        Angle latitude = Angle.fromDegrees(38.75);
        // This is a non-interpolated baseline latitude for the left grid point of our testing points
        Angle longitude = Angle.fromDegrees(-105.0);

        // Find the row and column values using the identical method the getOffset method uses
        // Code below is directly copied from the getOffset method accept where static class references were added
        double lat = latitude.degrees;
        double lon = longitude.degrees >= 0 ? longitude.degrees : longitude.degrees + 360;
        int topRow = (int) ((90 - lat) / EGM96.INTERVAL.degrees);
        if (lat <= -90)
            topRow = EGM96.NUM_ROWS - 2;
        int bottomRow = topRow + 1;
        // Note that the number of columns does not repeat the column at 0 longitude, so we must force the right
        // column to 0 for any longitude that's less than one interval from 360, and force the left column to the
        // last column of the grid.
        int leftCol = (int) (lon / EGM96.INTERVAL.degrees);
        int rightCol = leftCol + 1;
        if (lon >= 360 - EGM96.INTERVAL.degrees)
        {
            leftCol = EGM96.NUM_COLS - 1;
            rightCol = 0;
        }
        // Determine the functions determination of the top lat and left lon
        double latTop = 90 - topRow * EGM96.INTERVAL.degrees;
        double lonLeft = leftCol * EGM96.INTERVAL.degrees;

        // Ensure the top latitude matches our expected latitude
        // This shows that the method has determined a row and column to query the dataset that corresponds with our
        // latitude value
        assertEquals("longitude matches after index conversion", longitude.degrees + 360d, lonLeft, DELTA);

        // Using the confirmed longitude value from above (via the topRow and leftCol values), find the actual node
        // value
        double lonGridPointOffset = egm96.gePostOffset(topRow, leftCol) / 100d; // the other method converts to meters
        // Use the interpolation method to determine the offset value
        double lonOffset = egm96.getOffset(latitude, longitude);

        // Ensure that they are equal
        assertEquals("interpolated matches actual longitude", lonGridPointOffset, lonOffset, DELTA);
    }

    /**
     * Tests the determination of the EGM offset value using a longitude value between grid points. This method will use
     * the bilinear interpolation method to calculate the offset value.
     */
    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testGetOffset_HorizontalInterpolationPoint() throws IOException
    {

        // New EGM96 instance
        EGM96 egm96 = new EGM96(OFFSETS_FILE_PATH);
        // The EGM96 data has an interval of 0.25 degrees in both the horizontal and vertical dimensions. To test and
        // demonstrate the fractional value being determined is correct, this setup will isolate a horizontal
        // interpolation by landing on the vertical grid points (e.g. latitudes ending with .0, 0.25, 0.5, 0.75)
        Angle latitude = Angle.fromDegrees(38.75);
        // This is a baseline longitude for the left grid point of our testing points, it is closer to the left grid
        // point
        Angle longitude = Angle.fromDegrees(-104.9);

        // Find the row and column values using the identical method the getOffset method uses
        // Code below is directly copied from the getOffset method accept where static class references were added
        double lat = latitude.degrees;
        double lon = longitude.degrees >= 0 ? longitude.degrees : longitude.degrees + 360;
        int topRow = (int) ((90 - lat) / EGM96.INTERVAL.degrees);
        if (lat <= -90)
            topRow = EGM96.NUM_ROWS - 2;
        int bottomRow = topRow + 1;
        // Note that the number of columns does not repeat the column at 0 longitude, so we must force the right
        // column to 0 for any longitude that's less than one interval from 360, and force the left column to the
        // last column of the grid.
        int leftCol = (int) (lon / EGM96.INTERVAL.degrees);
        int rightCol = leftCol + 1;
        if (lon >= 360 - EGM96.INTERVAL.degrees)
        {
            leftCol = EGM96.NUM_COLS - 1;
            rightCol = 0;
        }
        // Determine the functions determination of the top lat and left lon
        double latTop = 90 - topRow * EGM96.INTERVAL.degrees;
        double lonLeft = leftCol * EGM96.INTERVAL.degrees;
        // Need the right longitude for our own interpolation testing
        double lonRight = rightCol * EGM96.INTERVAL.degrees;

        // Find the offset values of the top and bottom grid points
        double leftOffsetValue = egm96.gePostOffset(topRow, leftCol) / 100d;
        double rightOffsetValue = egm96.gePostOffset(topRow, rightCol) / 100d;

        // Ensure the left longitude matches our expected longitude
        // This shows that the method has determined a row and column to query the dataset that corresponds with our
        // longitude value
        assertEquals("left longitude matches after index conversion", -105d + 360d, lonLeft, DELTA);
        assertEquals("right longitude matches after index conversion", -104.75 + 360d, lonRight, DELTA);

        // The calculated EGM96 offset
        double lonOffset = egm96.getOffset(latitude, longitude);
        double manuallyCalculatedH = (lon - lonLeft) / (lonRight - lonLeft);
        double manuallyCalculatedInterpolationValue = (rightOffsetValue - leftOffsetValue) * manuallyCalculatedH
            + leftOffsetValue;

        // Ensure that they are equal
        assertEquals("interpolated matches actual longitude", manuallyCalculatedInterpolationValue, lonOffset, DELTA);
    }
}
