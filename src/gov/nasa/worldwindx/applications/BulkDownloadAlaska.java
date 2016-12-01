/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: BulkDownloadAlaska.java 1911 2014-04-09 23:14:09Z tgaskins $
 *          <p/>
 *          This class downloads specially configured imagery and elevations for the World Wind iOS TAIGA application.
 */
public class BulkDownloadAlaska
{
    public static void main(String[] args)
    {
        try
        {
            // Use three sectors to avoid capturing a lot of ocean area.
            ArrayList<Sector> sectors = new ArrayList<Sector>(3);
            sectors.add(Sector.fromDegrees(59, 71.4, -168.2, -141));
            sectors.add(Sector.fromDegrees(51, 59, -180, -151.7));
            sectors.add(Sector.fromDegrees(54.6, 60.5, -141, -130));

            BulkRetrievable layer;
            BulkRetrievalThread thread;
            AVListImpl params = new AVListImpl();

            for (Sector sector : sectors)
            {
                layer = (BulkRetrievable) BasicFactory.create(AVKey.LAYER_FACTORY, "config/Earth/BMNG256.xml");
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();

                params.setValue(AVKey.NUM_LEVELS, 9); // More than 9 levels is too large for TAIGA
                layer = (BulkRetrievable) BasicFactory.create(AVKey.LAYER_FACTORY, "config/Earth/Landsat256.xml");
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();

                params.setValue(AVKey.NUM_LEVELS, 8); // More than 8 levels is too large for TAIGA
                layer = (BulkRetrievable) BasicFactory.create(AVKey.LAYER_FACTORY,
                    "config/Earth/AlaskaFAASectionals.xml", params);
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();

                params.setValue(AVKey.NUM_LEVELS, 9); // More than 9 levels is too large for TAIGA
                CompoundElevationModel cem = (CompoundElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                    "config/Earth/EarthElevations256.xml");
                layer = (BulkRetrievable) cem.getElevationModels().get(0);
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();
            }


        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
