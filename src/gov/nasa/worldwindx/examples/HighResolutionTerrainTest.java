/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: HighResolutionTerrainTest.java 1952 2014-04-20 19:04:18Z tgaskins $
 */
public class HighResolutionTerrainTest
{
    protected static ArrayList<Position> generateReferenceLocations(Sector sector, int numLats, int numLons)
    {
        ArrayList<Position> locations = new ArrayList<Position>();
        double dLat = (sector.getMaxLatitude().degrees - sector.getMinLatitude().degrees) / (numLats - 1);
        double dLon = (sector.getMaxLongitude().degrees - sector.getMinLongitude().degrees) / (numLons - 1);
        for (int j = 0; j < numLats; j++)
        {
            double lat = sector.getMinLatitude().degrees + j * dLat;

            for (int i = 0; i < numLons; i++)
            {
                double lon = sector.getMinLongitude().degrees + i * dLon;

                // Specify angles to five decimal places.
                locations.add(
                    Position.fromDegrees(Math.round(lat * 100000.0) / 100000.0, Math.round(lon * 100000.0) / 100000.0, 0));
            }
        }

        return locations;
    }

    protected static void writeReferencePositions(String filePath, ArrayList<Position> positions)
        throws FileNotFoundException
    {
        PrintStream os = new PrintStream(new File(filePath));

        for (Position pos : positions)
        {
            os.format("%.5f %.5f %.4f\n", pos.getLatitude().degrees, pos.getLongitude().degrees, pos.getElevation());
        }

        os.flush();
    }

    protected static ArrayList<Position> readReferencePositions(String filePath) throws FileNotFoundException
    {
        ArrayList<Position> positions = new ArrayList<Position>();
        Scanner scanner = new Scanner(new File(filePath));

        while (scanner.hasNextDouble())
        {
            double lat = scanner.nextDouble();
            double lon = scanner.nextDouble();
            double elevation = scanner.nextDouble();
            positions.add(Position.fromDegrees(lat, lon, elevation));
        }

        return positions;
    }

    protected static ArrayList<Position> computeElevations(ArrayList<Position> locations)
    {
        Sector sector = Sector.boundingSector(locations);
        HighResolutionTerrain hrt = new HighResolutionTerrain(new Earth(), sector, null, 1.0);

        ArrayList<Position> computedPositions = new ArrayList<Position>();
        for (LatLon latLon : locations)
        {
            Double elevation = hrt.getElevation(latLon);
            computedPositions.add(new Position(latLon, Math.round(elevation * 10000.0) / 10000.0));
        }

        return computedPositions;
    }

    protected static void testPositions(String name, ArrayList<Position> referencePositions,
        ArrayList<Position> testPositions)
    {
        int numMatches = 0;

        for (int i = 0; i < referencePositions.size(); i++)
        {
            if (!testPositions.get(i).equals(referencePositions.get(i)))
                System.out.println(
                    "MISMATCH: reference = " + referencePositions.get(i) + ", test = " + testPositions.get(i));
            else
                ++numMatches;
        }

        System.out.println(numMatches + " Matches for " + name);
    }

    protected static void generateReferenceValues(String filePath, Sector sector) throws FileNotFoundException
    {
        HighResolutionTerrain hrt = new HighResolutionTerrain(new Earth(), sector, null, 1.0);

        ArrayList<Position> referenceLocations = generateReferenceLocations(hrt.getSector(), 5, 5);
        ArrayList<Position> referencePositions = computeElevations(referenceLocations);
        writeReferencePositions(filePath, referencePositions);
    }

    public static void main(String[] args)
    {
        String testDataLocation = "testData/HighResolutionTerrain/";
        HashMap<String, Sector> sectors = new HashMap<String, Sector>();
        sectors.put(testDataLocation + "HRTOutputTest01.txt", Sector.fromDegrees(37.8, 38.3, -120, -119.3));
        sectors.put(testDataLocation + "HRTOutputTest02.txt",
            Sector.fromDegrees(32.34767, 32.77991, 70.88239, 71.47658));
        sectors.put(testDataLocation + "HRTOutputTest03.txt",
            Sector.fromDegrees(32.37825, 71.21130, 32.50050, 71.37926));

        try
        {
            if (args.length > 0 && args[0].equals("-generateTestData"))
            {
                for (Map.Entry<String, Sector> sector : sectors.entrySet())
                {
                    String filePath = sector.getKey();

                    generateReferenceValues(filePath, sector.getValue());
                }
            }

            for (Map.Entry<String, Sector> sector : sectors.entrySet())
            {
                String filePath = sector.getKey();

                ArrayList<Position> referencePositions = readReferencePositions(filePath);
                ArrayList<Position> computedPositions = computeElevations(referencePositions);
                testPositions(filePath, referencePositions, computedPositions);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
