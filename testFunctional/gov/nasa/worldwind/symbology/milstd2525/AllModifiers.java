/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Test program that renders MIL-STD-2525C symbols with all combinations of graphic modifiers.
 *
 * @author pabercrombie
 * @version $Id: AllModifiers.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class AllModifiers extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            this.addSymbols();

            // Size the World Window to provide enough screen space for the symbols and center the World Window on the
            // screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void addSymbols()
        {
            this.addSymbolLayer("s-a-c----------", "WAR Symbols");
        }

        protected void addSymbolLayer(String sidc, String layerName)
        {
            RenderableLayer layer = new RenderableLayer();
            layer.setName(layerName);

            double startLat = 40;
            double startLon = -120;
            double dLat = -0.1;
            double dLon = 0.1;
            double lat = startLat;
            double lon = startLon;

            StringBuilder sb = new StringBuilder(sidc);

            for (String si : standardIdentities)
            {
                sb.setCharAt(1, si.charAt(0));
                sb.setCharAt(3, 'p'); // Present status

                for (String mod : modifiers)
                {
                    sb.replace(10, 12, mod);
                    String symbolId = sb.toString().toUpperCase();

                    this.addSymbol(symbolId, lat, lon, layer);
                    lon += dLon;
                }
                lat += dLat;
                lon = startLon;
            }

            // Add symbols with operation condition modifiers
            sb = new StringBuilder(sidc);
            for (String si : standardIdentities)
            {
                sb.setCharAt(1, si.charAt(0));

                for (String mod : operationalCondition)
                {
                    sb.setCharAt(3, mod.charAt(0));
                    String symbolId = sb.toString().toUpperCase();

                    // Add a symbol with the "normal" operation condition modifier.
                    this.addSymbol(symbolId, lat, lon, layer);
                    lon += dLon;

                    // Add another symbol using the alternate display.
                    TacticalSymbol symbol = this.addSymbol(symbolId, lat, lon, layer);
                    symbol.setModifier(SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE, true);
                    symbol.setValue(AVKey.DISPLAY_NAME, symbolId + " (Alternate Operational Condition");

                    lon += dLon;
                }
                lat += dLat;
                lon = startLon;
            }

            // Add the symbol layer to the World Wind model.
            this.getWwd().getModel().getLayers().add(layer);
        }

        protected TacticalSymbol addSymbol(String sidc, double lat, double lon, RenderableLayer layer)
        {
            Position pos = Position.fromDegrees(lat, lon, 8000);
            TacticalSymbol symbol = new MilStd2525TacticalSymbol(sidc, pos);
            symbol.setValue(AVKey.DISPLAY_NAME, sidc);
            symbol.setShowLocation(false);
            layer.addRenderable(symbol);

            return symbol;
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -119.85);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 77000);

        start("World Wind All MIL-STD-2525 Tactical Symbols", AppFrame.class);
    }

    protected static java.util.List<String> standardIdentities = Arrays.asList(
        "u",
        "f",
        "n",
        "h"
    );

    protected static List<String> operationalCondition = Arrays.asList(
        "c", // Present, fully capable
        "d", // Present, damaged
        "x", // Present, destroyed
        "f"  // Present, full to capacity
    );

    /** All modifiers codes from MIL-STD-2525C Table A-II, pg. 52. */
    protected static java.util.List<String> modifiers = Arrays.asList(
        "--", /* Null */
        "-A", /* Team/Crew */
        "-B", /* Squad */
        "-C", /* Section */
        "-D", /* Platoon/Detachment */
        "-E", /* Company/Battery/Troop */
        "-F", /* Battalion/Squadron */
        "-G", /* Regiment/Group */
        "-H", /* Brigade */
        "-I", /* Division */
        "-J", /* Corps/Mef */
        "-K", /* Army */
        "-L", /* Army Group/Front */
        "-M", /* Region */
        "-N", /* Command */
        "AA", /* HQ Team/Crew */
        "AB", /* HQ Squad */
        "AC", /* HQ Section */
        "AD", /* HQ Platoon/Detachment */
        "AE", /* HQ Company/Battery/Troop */
        "AF", /* HQ Battalion/Squadron */
        "AG", /* HQ Regiment/Group */
        "AH", /* HQ Brigade */
        "AI", /* HQ Division */
        "AJ", /* HQ Corps/Mef */
        "AK", /* HQ Army */
        "AL", /* HQ Army Group/Front */
        "AM", /* HQ Region */
        "AN", /* HQ Command */
        "BA", /* TF HQ Team/Crew */
        "BB", /* TF HQ Squad */
        "BC", /* TF HQ Section */
        "BD", /* TF HQ Platoon/Detachment */
        "BE", /* TF HQ Company/Battery/Troop */
        "BF", /* TF HQ Battalion/Squadron */
        "BG", /* TF HQ Regiment/Group */
        "BH", /* TF HQ Brigade */
        "BI", /* TF HQ Division */
        "BJ", /* TF HQ Corps/Mef */
        "BK", /* TF HQ Army */
        "BL", /* TF HQ Army Group/Front */
        "BM", /* TF HQ Region */
        "BN", /* TF HQ Command */
        "CA", /* FD HQ Team/Crew */
        "CB", /* FD HQ Squad */
        "CC", /* FD HQ Section */
        "CD", /* FD HQ Platoon/Detachment */
        "CE", /* FD HQ Company/Battery/Troop */
        "CF", /* FD HQ Battalion/Squadron */
        "CG", /* FD HQ Regiment/Group */
        "CH", /* FD HQ Brigade */
        "CI", /* FD HQ Division */
        "CJ", /* FD HQ Corps/Mef*/
        "CK", /* FD HQ Army */
        "CL", /* FD HQ Army Group/Front */
        "CM", /* FD HQ Region */
        "CN", /* FD HQ Command */
        "DA", /* FD/TF HQ Team/Crew */
        "DB", /* FD/TF HQ Squad */
        "DC", /* FD/TF HQ Section */
        "DD", /* FD/TF HQ Platoon/Detachment */
        "DE", /* FD/TF HQ Company/Battery/Troop */
        "DF", /* FD/TF HQ Battalion/Squadron */
        "DG", /* FD/TF HQ Regiment/Group */
        "DH", /* FD/TF HQ Brigade */
        "DI", /* FD/TF HQ Division */
        "DJ", /* FD/TF HQ Corps/Mef */
        "DK", /* FD/TF HQ Army */
        "DL", /* FD/TF HQ Army Group/Front */
        "DM", /* FD/TF HQ Region */
        "DN", /* FD/TF HQ Command */
        "EA", /* TF Team/Crew */
        "EB", /* TF Squad */
        "EC", /* TF Section */
        "ED", /* TF Platoon/Detachment */
        "EE", /* TF Company/Battery/Troop */
        "EF", /* TF Battalion/Squadron */
        "EG", /* TF Regiment/Group */
        "EH", /* TF Brigade */
        "EI", /* TF Division */
        "EJ", /* TF Corps/Mef */
        "EK", /* TF Army */
        "EL", /* TF Army Group/Front */
        "EM", /* TF Region */
        "EN", /* TF Command */
        "FA", /* FD Team/Crew */
        "FB", /* FD Squad */
        "FC", /* FD Section */
        "FD", /* FD Platoon/Detachment */
        "FE", /* FD Company/Battery/Troop */
        "FF", /* FD Battalion/Squadron */
        "FG", /* FD Regiment/Group */
        "FH", /* FD Brigade */
        "FI", /* FD Division */
        "FJ", /* FD Corps/Mef */
        "FK", /* FD Army */
        "FL", /* FD Army Group/Front */
        "FM", /* FD Region */
        "FN", /* FD Command */
        "GA", /* FD/TF Team/Crew */
        "GB", /* FD/TF Squad */
        "GC", /* FD/TF Section */
        "GD", /* FD/TF Platoon/Detachment */
        "GE", /* FD/TF Company/Battery/Troop */
        "GF", /* FD/TF Battalion/Squadron */
        "GG", /* FD/TF Regiment/Group */
        "GH", /* FD/TF Brigade */
        "GI", /* FD/TF Division */
        "GJ", /* FD/TF Corps/Mef */
        "GK", /* FD/TF Army */
        "GL", /* FD/TF Army Group/Front */
        "GM", /* FD/TF Region */
        "GN", /* FD/TF Command */
        "H-", /* Feint Dummy Installation */
//        "HB", /* Feint Dummy Installation */ // TODO not implemented yet
        "MO", /* Mobility Wheeled/Limited Cross Country */
        "MP", /* Mobility Cross Country */
        "MQ", /* Mobility Tracked */
        "MR", /* Mobility Wheeled And Tracked Combination */
        "MS", /* Mobility Towed */
        "MT", /* Mobility Rail */
        "MU", /* Mobility Over The Snow */
        "MV", /* Mobility Sled */
        "MW", /* Mobility Pack Animals */
        "MX", /* Mobility Barge */
        "MY", /* Mobility Amphibious */
        "NS", /* Towed Array (Short) */
        "NL" /* Towed Array (Long) */
    );
}
