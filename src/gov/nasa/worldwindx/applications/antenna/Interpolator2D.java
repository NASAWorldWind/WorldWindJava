/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.antenna;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents a 2D function defined by sample points and interpolates values within the domain of the samples. Each
 * sample is defined by two coordinates, (s, t) and a function value, r, such that f(s,t) = r. Once the function's
 * sample points are added to the interpolator, the interpolator determines values at any coordinate within the sample
 * domain. An option exists to interpolate outside the sample domain by wrapping from maximum back to minimum values.
 * See {@link #setWrapS(boolean)} for a description of this option.
 *
 * @author tag
 * @version $Id: Interpolator2D.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Interpolator2D
{
    protected boolean wrapS = false;
    protected boolean wrapT = false;
    protected Double minS;
    protected Double maxS;
    protected Double minT;
    protected Double maxT;
    protected Double minValue;
    protected Double maxValue;

    // Retain the input tuples in a sorted map of sorted maps. The outer map represents the S axis, the inner the T.
    protected TreeMap<Double, TreeMap<Double, Double>> map = new TreeMap<Double, TreeMap<Double, Double>>();

    /**
     * Indicates whether this interpolator interpolates between maximum and minimum first coordinates if a first
     * coordinate outside the current range of first coordinates is specified. See {@link #setWrapS(boolean)} for a
     * description of wrapping and the use of this flag.
     *
     * @return the first coordinate's wrap flag.
     */
    public boolean isWrapS()
    {
        return wrapS;
    }

    /**
     * Specifies whether this interpolator interpolates between maximum and minimum first coordinates if a first
     * coordinate outside the current range of first coordinates is specified. For example, if the first coordinate
     * represents a circular angle, and the minimum first coordinate is 0 while the maximum first coordinate is 350,
     * when a value at a first coordinate of 355 is requested, interpolation should likely be performed between 350 and
     * 0. If the wrap flag is true, interpolation is performed. If it is false, null is returned from {@link
     * #getValue(double, double)}.
     * <p/>
     * The default wrap S flag is <code>false</code>.
     *
     * @param wrapS the first coordinate's wrap flag.
     *
     * @see #setWrapS(boolean)
     */
    public void setWrapS(boolean wrapS)
    {
        this.wrapS = wrapS;
    }

    /**
     * Indicates whether this interpolator interpolates between maximum and minimum second coordinates if a second
     * coordinate outside the current range of second coordinates is specified. See {@link #setWrapS(boolean)} for a
     * description of wrapping and the use of wrap flags.
     *
     * @return the second coordinate's wrap flag.
     *
     * @see #setWrapS(boolean)
     */
    public boolean isWrapT()
    {
        return wrapT;
    }

    /**
     * Specifies whether this interpolator interpolates between maximum and minimum second coordinates if a second
     * coordinate outside the current range of second coordinates is specified. See {@link #setWrapS(boolean)} for a
     * description of wrapping and the use of wrap flags.
     * <p/>
     * The default T wrap flag is <code>false</code>.
     *
     * @param wrapT the second coordinate's wrap flag.
     *
     * @see #setWrapS(boolean)
     */
    public void setWrapT(boolean wrapT)
    {
        this.wrapT = wrapT;
    }

    /**
     * Adds a value to this interpolator.
     *
     * @param s the value's first coordinate.
     * @param t the value's second coordinate.
     * @param r the value to add.
     */
    public void addValue(double s, double t, double r)
    {
        TreeMap<Double, Double> tMap = this.map.get(s);
        if (tMap == null)
        {
            tMap = new TreeMap<Double, Double>();
            this.map.put(s, tMap);
        }

        tMap.put(t, r);

        if (this.maxValue == null || r > this.maxValue)
            this.maxValue = r;

        if (this.minValue == null || r < this.minValue)
            this.minValue = r;

        if (this.maxS == null || s > this.maxS)
            this.maxS = s;

        if (this.minS == null || s < this.minS)
            this.minS = s;

        if (this.maxT == null || t > this.maxT)
            this.maxT = t;

        if (this.minT == null || t < this.minT)
            this.minT = t;
    }

    /**
     * Returns the minimum value within this interpolator.
     *
     * @return this interpolator's minimum value, or null if the interpolator contains no values.
     */
    public Double getMinValue()
    {
        return minValue;
    }

    /**
     * Returns the maximum value within this interpolator.
     *
     * @return this interpolator's maximum value, or null if the interpolator contains no values.
     */
    public Double getMaxValue()
    {
        return maxValue;
    }

    /**
     * Returns the minimum first coordinate within this interpolator.
     *
     * @return this interpolator's minimum first coordinate, or null if the interpolator contains no values.
     */
    public Double getMinS()
    {
        return minS;
    }

    /**
     * Returns the maximum first coordinate within this interpolator.
     *
     * @return this interpolator's maximum first coordinate, or null if the interpolator contains no values.
     */
    public Double getMaxS()
    {
        return maxS;
    }

    /**
     * Returns the minimum second coordinate within this interpolator.
     *
     * @return this interpolator's minimum second coordinate, or null if the interpolator contains no values.
     */
    public Double getMinT()
    {
        return minT;
    }

    /**
     * Returns the maximum second coordinate within this interpolator.
     *
     * @return this interpolator's maximum second coordinate, or null if the interpolator contains no values.
     */
    public Double getMaxT()
    {
        return maxT;
    }

    /**
     * Determines and returns a value from this interpolator using bi-linear interpolation.
     *
     * @param s the first coordinate.
     * @param t the second coordinate.
     *
     * @return the value at the specified coordinates, or null if a specified coordinate is outside the range  of this
     *         interpolator and the corresponding wrap flag is false.
     *
     * @see #setWrapS(boolean)
     * @see #setWrapT(boolean)
     */
    public Double getValue(double s, double t)
    {
        // Use bi-linear interpolation to determine the value at the specified coordinates.

        Map.Entry<Double, TreeMap<Double, Double>> sMinEntry = this.map.floorEntry(s);
        if (sMinEntry == null && this.wrapS)
            sMinEntry = this.map.lastEntry(); // wrap
        else if (sMinEntry == null)
            return null;

        Map.Entry<Double, Double> sMintMinEntry = sMinEntry.getValue().floorEntry(t);
        if (sMintMinEntry == null && this.wrapT)
            sMintMinEntry = sMinEntry.getValue().lastEntry(); // wrap
        else if (sMintMinEntry == null)
            return null;

        Map.Entry<Double, Double> sMintMaxEntry = sMinEntry.getValue().ceilingEntry(t);
        if (sMintMaxEntry == null && this.wrapT)
            sMintMaxEntry = sMinEntry.getValue().firstEntry(); // wrap
        else if (sMintMaxEntry == null)
            return null;

        Map.Entry<Double, TreeMap<Double, Double>> sMaxEntry = this.map.ceilingEntry(s);
        if (sMaxEntry == null && this.wrapS)
            sMaxEntry = this.map.firstEntry(); // wrap
        else if (sMaxEntry == null)
            return null;

        Map.Entry<Double, Double> sMaxtMinEntry = sMaxEntry.getValue().floorEntry(t);
        if (sMaxtMinEntry == null && this.wrapT)
            sMaxtMinEntry = sMaxEntry.getValue().lastEntry(); // wrap
        else if (sMaxtMinEntry == null)
            return null;

        Map.Entry<Double, Double> sMaxtMaxEntry = sMaxEntry.getValue().ceilingEntry(t);
        if (sMaxtMaxEntry == null && this.wrapT)
            sMaxtMaxEntry = sMaxEntry.getValue().firstEntry();
        else if (sMaxtMaxEntry == null)
            return null;

        double r00 = sMintMinEntry.getValue();
        double r10 = sMaxtMinEntry.getValue();
        double r01 = sMintMaxEntry.getValue();
        double r11 = sMaxtMaxEntry.getValue();

        double s0 = sMinEntry.getKey();
        double s1 = sMaxEntry.getKey();

        double t0 = sMintMinEntry.getKey();
        double t1 = sMaxtMaxEntry.getKey();

        double as = (s1 - s0) != 0 ? (s - s0) / (s1 - s0) : 0;
        double rs0 = as * r10 + (1 - as) * r00;
        double rs1 = as * r11 + (1 - as) * r01;

        double at = (t1 - t0) != 0 ? (t - t0) / (t1 - t0) : 0;
        double r = at * rs1 + (1 - at) * rs0;

        return r;
    }

    public void addFromStream(InputStream is)
    {
        if (is == null)
            return;

        // Recognize three successive floating-point values separated by white space, in the order s, t, r, where r is
        // the function value at the corresponding s and t.
        String fp = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"; // float pattern
        Pattern pattern = Pattern.compile("\\s*" + fp + "\\s+" + fp + "\\s+" + fp);

        Scanner scanner = new Scanner(is);

        try
        {
            while (scanner.hasNextLine())
            {
                String scannedLine = scanner.findInLine(pattern);
                if (scannedLine == null)
                {
                    scanner.nextLine();
                    continue;
                }

                String numbers[] = scannedLine.split("\\s+");
                if (numbers.length < 3)
                    continue;

                double theta = Double.parseDouble(numbers[0]);
                double phi = Double.parseDouble(numbers[1]);
                double r = Double.parseDouble(numbers[2]);

                if (phi < 0)
                    phi += 360;

                this.addValue(theta, phi, r);
            }
        }
        finally
        {
            scanner.close();
        }
    }

    public void addFromFile(File file) throws FileNotFoundException
    {
        if (file == null || !file.exists())
            return;

        this.addFromStream(new FileInputStream(file));
    }
//
//    public void addFromFile(File file) throws FileNotFoundException
//    {
//        if (file == null || !file.exists())
//            return;
//
//        // Recognize three successive floating-point values separated by white space, in the order s, t, r, where r is
//        // the function value at the corresponding s and t.
//        String fp = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"; // float pattern
//        Pattern pattern = Pattern.compile("\\s*" + fp + "\\s+" + fp + "\\s+" + fp);
//
//        Scanner scanner = new Scanner(file);
//
//        try
//        {
//            while (scanner.hasNextLine())
//            {
//                String scannedLine = scanner.findInLine(pattern);
//                if (scannedLine == null)
//                {
//                    scanner.nextLine();
//                    continue;
//                }
//
//                String numbers[] = scannedLine.split("\\s+");
//                if (numbers.length < 3)
//                    continue;
//
//                double theta = Double.parseDouble(numbers[0]);
//                double phi = Double.parseDouble(numbers[1]);
//                double r = Double.parseDouble(numbers[2]);
//
//                if (phi < 0)
//                    phi += 360;
//
//                this.addValue(theta, phi, r);
//            }
//        }
//        finally
//        {
//            scanner.close();
//        }
//    }
//
//    public static void main(String[] args)
//    {
//        Sampler2D interpolator = new Sampler2D();
//
//        try
//        {
//            interpolator.addFromFile(new File("/Users/tag/Desktop/Sandbar/ant_test.csv"));
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
////
////        interpolator.addValue(0, 0, 0);
////        interpolator.addValue(1, 0, 1);
////        interpolator.addValue(0, 1, 1);
////        interpolator.addValue(1, 1, 1);
////
////        double r = interpolator.getValue(0.5, 0.5);
////        r = interpolator.getValue(0, 0);
////        r = interpolator.getValue(1, 0);
////        r = interpolator.getValue(0, 1);
////        r = interpolator.getValue(1, 1);
//
//        return;
//    }
}
