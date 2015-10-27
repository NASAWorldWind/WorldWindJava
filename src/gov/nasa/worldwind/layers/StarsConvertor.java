/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.*;
import java.util.ArrayList;

/**
 * Converts a star background based on a subset of ESA Hipparcos catalog to ByteBuffer.
 *
 * @author Patrick Murris
 * @version $Id: StarsConvertor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class StarsConvertor
{
    private static final float DEFAULT_RADIUS = 6356752 * 10;        // Earth radius x 10

    /**
     * Convert star tsv text file to binary dat file
     *
     * @param tsvFileName name of tsv text star file
     */
    public static void convertTsvToDat(String tsvFileName)
    {
        String datFileName = WWIO.replaceSuffix(tsvFileName, ".dat");

        convertTsvToDat(tsvFileName, datFileName, DEFAULT_RADIUS);
    }

    /**
     * Convert star tsv text file to binary dat file
     *
     * @param tsvFileName name of tsv text star file
     * @param radius      radius of star sphere
     */
    public static void convertTsvToDat(String tsvFileName, float radius)
    {
        String datFileName = WWIO.replaceSuffix(tsvFileName, ".dat");

        convertTsvToDat(tsvFileName, datFileName, radius);
    }

    /**
     * Convert star tsv text file to binary dat file
     *
     * @param tsvFileName name of tsv text star file
     * @param datFileName name of dat binary star file
     */
    public static void convertTsvToDat(String tsvFileName, String datFileName)
    {
        convertTsvToDat(tsvFileName, datFileName, DEFAULT_RADIUS);
    }

    /**
     * Convert star tsv text file to binary dat file
     *
     * @param tsvFileName name of tsv text star file
     * @param datFileName name of dat binary star file
     * @param radius      radius of star sphere
     */
    public static void convertTsvToDat(String tsvFileName, String datFileName, float radius)
    {
        //Convert the Tsv Star file to a ByteBuffer in little-endian order
        ByteBuffer bbuf = convertTsvToByteBuffer(tsvFileName, radius);

        try
        {
            WWIO.saveBuffer(bbuf, new File(datFileName));
        }
        catch (IOException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.ExceptionAttemptingToWriteTo", datFileName), e);
        }
    }

    /**
     * Converts a Stars tsv file to a ByteBuffer with radius DEFAULT_RADIUS
     *
     * @param starsFileName filename of tsv file
     *
     * @return ByteBuffer with interleaved color and vertex positions as floats in little-endian order
     */
    public static ByteBuffer convertTsvToByteBuffer(String starsFileName)
    {
        return convertTsvToByteBuffer(starsFileName, DEFAULT_RADIUS);
    }

    /**
     * Converts a Stars tsv file to a ByteBuffer
     *
     * @param starsFileName filename of tsv file
     * @param radius        radius of the sphere on which to paint stars
     *
     * @return ByteBuffer with interleaved color and vertex positions as floats in little-endian order
     */
    public static ByteBuffer convertTsvToByteBuffer(String starsFileName, float radius)
    {
        try
        {
            ArrayList<Float> tmpBuffer = new ArrayList<Float>();

            InputStream starsStream = StarsConvertor.class.getResourceAsStream("/" + starsFileName);

            if (starsStream == null)
            {
                File starsFile = new File(starsFileName);
                if (starsFile.exists())
                {
                    starsStream = new FileInputStream(starsFile);
                }
            }

            if (starsStream == null)
                // TODO: logger error
                return null;

            BufferedReader starsReader = new BufferedReader(new InputStreamReader(starsStream));

            String line;
            int idxRAhms = 2;        // Catalog field indices
            int idxDEdms = 3;
            int idxVmag = 4;
            int idxBV = 5;
            double longitude;
            double latitude;
            boolean isData = false;

            //Add the radius as the first value
            tmpBuffer.add(radius);

            while ((line = starsReader.readLine()) != null)
            {
                if (line.length() < 3)
                    continue;
                if (line.substring(0, 1).equals("#"))
                    continue;
                if (isData) // Star data here
                {
                    // Split data in ';' separated values
                    String[] starData = line.trim().split(";");
                    String RAhms, DEdms, Vmag, BV;
                    RAhms = starData[idxRAhms];    // Right Asc in H, min, sec 	"00 01 35.85"
                    DEdms = starData[idxDEdms];    // Declinaison Degre min sec	"-77 03 55.1"
                    Vmag = starData[idxVmag];    // Apparent magnitude	" 4.78"
                    // B-V spectral color " 1.254" (may be missing)
                    BV = idxBV < starData.length ? starData[idxBV] : "";

                    // compute RAhms into longitude
                    double RAh = Double.parseDouble(RAhms.substring(0, 2));
                    double RAm = Double.parseDouble(RAhms.substring(3, 5));
                    double RAs = Double.parseDouble(RAhms.substring(6));
                    longitude = (RAh * 15) + (RAm * .25) + (RAs * 0.0041666) - 180;
                    // compute DEdms into latitude
                    String DEsign = DEdms.substring(0, 1);
                    double DEd = Double.parseDouble(DEdms.substring(1, 3));
                    double DEm = Double.parseDouble(DEdms.substring(4, 6));
                    double DEs = Double.parseDouble(DEdms.substring(7));
                    latitude = DEd + (DEm / 60) + (DEs / 3600);
                    if (DEsign.equals("-"))
                        latitude *= -1;
                    // compute aparent magnitude -1.5 - 10 to grayscale 0 - 255
                    double VM = Double.parseDouble(Vmag);
                    double Vdec = 255 - ((VM + 1.5) * 255 / 10);
                    if (Vdec > 255)
                        Vdec = 255;
                    Vdec /= 255;    // scale back to 0.0 - 1.0
                    // convert B-V  -0.5 - 4 for rgb color select
                    double BVdec;
                    try
                    {
                        BVdec = Double.parseDouble(BV);
                    }
                    catch (Exception e)
                    {
                        BVdec = 0;
                    }

                    // Star color
                    Color color = BVColor(BVdec);
                    tmpBuffer.add(color.getRed() / 255f * (float) Vdec);
                    tmpBuffer.add(color.getGreen() / 255f * (float) Vdec);
                    tmpBuffer.add(color.getBlue() / 255f * (float) Vdec);

                    // Place vertex for point star
                    Vec4 pos = SphericalToCartesian(latitude, longitude, radius);
                    tmpBuffer.add((float) pos.getX());
                    tmpBuffer.add((float) pos.getY());
                    tmpBuffer.add((float) pos.getZ());
                }

                // Data starting next line
                if (line.substring(0, 3).equals("---"))
                    isData = true;
            }

            starsReader.close();

            ByteBuffer buf = Buffers.newDirectByteBuffer(tmpBuffer.size() * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer fBuf = buf.asFloatBuffer();

            for (Float fVal : tmpBuffer)
            {
                fBuf.put(fVal);
            }

            buf.rewind();

            return buf;
        }
        catch (IOException e)
        {
            // TODO: Log proper message
            //String message = WorldWind.retrieveErrMsg("generic.IOExceptionWhileLoadingData");
            String message = "IOException while loading stars data from " + starsFileName;
            Logging.logger().severe(message);
        }
        catch (Exception e)
        {
            String message = "Error while loading stars data from " + starsFileName;
            Logging.logger().severe(message);
        }

        return null;
    }

    /**
     * Converts position in spherical coordinates (lat/lon/radius) to cartesian (XYZ) coordinates.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param radius    Radius
     *
     * @return the corresponding Point
     */
    private static Vec4 SphericalToCartesian(double latitude, double longitude, float radius)
    {
        latitude *= Math.PI / 180.0f;
        longitude *= Math.PI / 180.0f;

        double radCosLat = radius * Math.cos(latitude);

        return new Vec4(
            radCosLat * Math.sin(longitude),
            (double) radius * Math.sin(latitude),
            radCosLat * Math.cos(longitude));
    }

    /**
     * Returns the corresponding B-V color
     *
     * @param BV the star B-V decimal value (-.5 .. 4)
     *
     * @return the corresponding Color
     */
    private static Color BVColor(double BV)
    {
        // TODO: interpolate between values
        if (BV < 0)
            return new Color(.635f, .764f, .929f);            // Light blue
        else if (BV < .5)
            return new Color(1f, 1f, 1f);                // White
        else if (BV < 1)
            return new Color(1f, .984f, .266f);            // Yellow
        else if (BV < 1.5)
            return new Color(.964f, .725f, .0784f);    // Orange
        else
            return new Color(.921f, .376f, .0392f);                // Redish
    }

    public static void main(String[] args)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        int status = fileChooser.showOpenDialog(null);
        if (status != JFileChooser.APPROVE_OPTION)
            return;

        File[] files = fileChooser.getSelectedFiles();
        if (files == null)
        {
            System.out.println("No files selected");
            return;
        }

        String ans;
        ans = JOptionPane.showInputDialog("Enter star sphere radius?", DEFAULT_RADIUS);

        float radius;

        while (true)
        {
            try
            {
                radius = Float.parseFloat(ans);
                break;
            }
            catch (NumberFormatException e)
            {
                String message = Logging.getMessage("generic.NumberFormatException");
                Logging.logger().warning(message);

                ans = JOptionPane.showInputDialog(
                    "<html><font color=#ff0000>INVALID VALUE: Please enter a floating point number."
                        + "</font><br>Enter star sphere radius?</html>", DEFAULT_RADIUS);
            }
        }

        for (File file : files)
        {
            convertTsvToDat(file.getAbsolutePath(), radius);
        }
    }
}
