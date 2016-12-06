/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.*;
import java.util.*;

/**
 * @author ccrick
 * @version $Id: Symbology.java 132 2011-10-25 18:47:52Z ccrick $
 */
public class Frames extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            this.addFrameTypeSymbols();

            // Size the World Window to provide enough screen space for the symbols and center the World Window on the
            // screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void addFrameTypeSymbols()
        {
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Standard Frame Types");
            this.addFrameTypeSymbols(SymbologyConstants.STATUS_PRESENT, layer);
            // Add the symbol layer to the World Wind model.
            this.getWwd().getModel().getLayers().add(layer);

            layer = new RenderableLayer();
            layer.setName("Standard Frame Types (Anticipated)");
            layer.setEnabled(false);
            this.addFrameTypeSymbols(SymbologyConstants.STATUS_ANTICIPATED, layer);
            // Add the symbol layer to the World Wind model.
            this.getWwd().getModel().getLayers().add(layer);
        }

        protected void addFrameTypeSymbols(String status, RenderableLayer layer)
        {
            Iterator<String> symbolIds = this.getFrameTypeIterator(status).iterator();
            Iterator<Position> positions = this.getGridIterator(
                Sector.fromDegrees(39.5, 40.5, -120.5, -119.5), 14, 9, 3000).iterator();

            while (symbolIds.hasNext() && positions.hasNext())
            {
                String symbolId = symbolIds.next();
                TacticalSymbol symbol = new MilStd2525TacticalSymbol(symbolId, positions.next());
                symbol.setValue(AVKey.DISPLAY_NAME, symbolId);
                layer.addRenderable(symbol);
            }
        }

        protected Iterable<Position> getGridIterator(Sector sector, int numLatPoints, int numLonPoints, double altitude)
        {
            double minLat = sector.getMinLatitude().degrees;
            double maxLat = sector.getMaxLatitude().degrees;
            double minLon = sector.getMinLongitude().degrees;
            double maxLon = sector.getMaxLongitude().degrees;
            double latDelta = sector.getDeltaLatDegrees() / numLatPoints;
            double lonDelta = sector.getDeltaLonDegrees() / numLonPoints;

            ArrayList<Position> positions = new ArrayList<Position>();

            for (double lat = maxLat; lat >= minLat; lat -= latDelta)
            {
                for (double lon = minLon; lon <= maxLon; lon += lonDelta)
                {
                    positions.add(Position.fromDegrees(lat, lon, altitude));
                }
            }

            return positions;
        }

        protected Iterable<String> getFrameTypeIterator(String status)
        {
            return Arrays.asList(
                // Standard Identity Pending
                "SPZ" + status + "-----------",
                "SPP" + status + "-----------",
                "SPA" + status + "-----------",
                "SPG" + status + "U----------",
                "SPG" + status + "E----------",
                "SPG" + status + "I-----H----",
                "SPS" + status + "-----------",
                "SPU" + status + "-----------",
                "SPF" + status + "-----------",
                // Standard Identity Unknown
                "SUZ" + status + "-----------",
                "SUP" + status + "-----------",
                "SUA" + status + "-----------",
                "SUG" + status + "U----------",
                "SUG" + status + "E----------",
                "SUG" + status + "I-----H----",
                "SUS" + status + "-----------",
                "SUU" + status + "-----------",
                "SUF" + status + "-----------",
                // Standard Identity Friend
                "SFZ" + status + "-----------",
                "SFP" + status + "-----------",
                "SFA" + status + "-----------",
                "SFG" + status + "U----------",
                "SFG" + status + "E----------",
                "SFG" + status + "I-----H----",
                "SFS" + status + "-----------",
                "SFU" + status + "-----------",
                "SFF" + status + "-----------",
                // Standard Identity Neutral
                "SNZ" + status + "-----------",
                "SNP" + status + "-----------",
                "SNA" + status + "-----------",
                "SNG" + status + "U----------",
                "SNG" + status + "E----------",
                "SNG" + status + "I-----H----",
                "SNS" + status + "-----------",
                "SNU" + status + "-----------",
                "SNF" + status + "-----------",
                // Standard Identity Hostile
                "SHZ" + status + "-----------",
                "SHP" + status + "-----------",
                "SHA" + status + "-----------",
                "SHG" + status + "U----------",
                "SHG" + status + "E----------",
                "SHG" + status + "I-----H----",
                "SHS" + status + "-----------",
                "SHU" + status + "-----------",
                "SHF" + status + "-----------",
                // Standard Identity Assumed Friend
                "SAZ" + status + "-----------",
                "SAP" + status + "-----------",
                "SAA" + status + "-----------",
                "SAG" + status + "U----------",
                "SAG" + status + "E----------",
                "SAG" + status + "I-----H----",
                "SAS" + status + "-----------",
                "SAU" + status + "-----------",
                "SAF" + status + "-----------",
                // Standard Identity Suspect
                "SSZ" + status + "-----------",
                "SSP" + status + "-----------",
                "SSA" + status + "-----------",
                "SSG" + status + "U----------",
                "SSG" + status + "E----------",
                "SSG" + status + "I-----H----",
                "SSS" + status + "-----------",
                "SSU" + status + "-----------",
                "SSF" + status + "-----------",
                // Standard Identity Exercise Pending
                "SGZ" + status + "-----------",
                "SGP" + status + "-----------",
                "SGA" + status + "-----------",
                "SGG" + status + "U----------",
                "SGG" + status + "E----------",
                "SGG" + status + "I-----H----",
                "SGS" + status + "-----------",
                "SGU" + status + "-----------",
                "SGF" + status + "-----------",
                // Standard Identity Exercise Unknown
                "SWZ" + status + "-----------",
                "SWP" + status + "-----------",
                "SWA" + status + "-----------",
                "SWG" + status + "U----------",
                "SWG" + status + "E----------",
                "SWG" + status + "I-----H----",
                "SWS" + status + "-----------",
                "SWU" + status + "-----------",
                "SWF" + status + "-----------",
                // Standard Identity Exercise Friend
                "SDZ" + status + "-----------",
                "SDP" + status + "-----------",
                "SDA" + status + "-----------",
                "SDG" + status + "U----------",
                "SDG" + status + "E----------",
                "SDG" + status + "I-----H----",
                "SDS" + status + "-----------",
                "SDU" + status + "-----------",
                "SDF" + status + "-----------",
                // Standard Identity Exercise Neutral
                "SLZ" + status + "-----------",
                "SLP" + status + "-----------",
                "SLA" + status + "-----------",
                "SLG" + status + "U----------",
                "SLG" + status + "E----------",
                "SLG" + status + "I-----H----",
                "SLS" + status + "-----------",
                "SLU" + status + "-----------",
                "SLF" + status + "-----------",
                // Standard Identity Exercise Assumed Friend
                "SMZ" + status + "-----------",
                "SMP" + status + "-----------",
                "SMA" + status + "-----------",
                "SMG" + status + "U----------",
                "SMG" + status + "E----------",
                "SMG" + status + "I-----H----",
                "SMS" + status + "-----------",
                "SMU" + status + "-----------",
                "SMF" + status + "-----------",
                // Standard Identity Joker
                "SJZ" + status + "-----------",
                "SJP" + status + "-----------",
                "SJA" + status + "-----------",
                "SJG" + status + "U----------",
                "SJG" + status + "E----------",
                "SJG" + status + "I-----H----",
                "SJS" + status + "-----------",
                "SJU" + status + "-----------",
                "SJF" + status + "-----------",
                // Standard Identity Faker
                "SKZ" + status + "-----------",
                "SKP" + status + "-----------",
                "SKA" + status + "-----------",
                "SKG" + status + "U----------",
                "SKG" + status + "E----------",
                "SKG" + status + "I-----H----",
                "SKS" + status + "-----------",
                "SKU" + status + "-----------",
                "SKF" + status + "-----------"
            );
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -120);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 100000);

        ApplicationTemplate.start("World Wind MIL-STD-2525 Tactical Symbol Frame Types", AppFrame.class);
    }
}
