/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

import static gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc.*;
import static gov.nasa.worldwind.symbology.milstd2525.graphics.TacticalGraphicSymbol.LabelLayout;

/**
 * Object to provide default label layouts for MIL-STD-2525C tactical point graphics. The layout is used to arrange text
 * modifiers around the icon.
 *
 * @author pabercrombie
 * @version $Id: DefaultLabelLayouts.java 552 2012-04-25 16:51:16Z pabercrombie $
 */
public class DefaultLabelLayouts
{
    /** Map to hold layouts. */
    protected Map<String, List<LabelLayout>> layouts = new HashMap<String, List<LabelLayout>>();

    /** Create the map and populate it with the default layouts. */
    public DefaultLabelLayouts()
    {
        this.populateMap();
    }

    /**
     * Indicates the layout for a particular type of graphic.
     *
     * @param sidc Symbol code of the graphic.
     *
     * @return Map that represents the label layout. The keys indicate the modifier key (unique designation, additional
     *         info, etc.). The values are lists of LabelLayout. Most modifiers will only specify a single layout, but
     *         some graphics support multiple instances of the same modifier, in which case the list will contain
     *         multiple layouts.
     */
    public List<LabelLayout> get(String sidc)
    {
        List<LabelLayout> layout = this.layouts.get(sidc);
        return layout != null ? layout : Collections.<LabelLayout>emptyList();
    }

    /** Populate the map with the default layouts. */
    protected void populateMap()
    {
        // The C2GM.GNL.PNT.HBR graphic supports the H modifier in the center of the graphic.
        this.layouts.put(C2GM_GNL_PNT_HBR,
            this.createLayout(SymbologyConstants.ADDITIONAL_INFORMATION, Offset.CENTER, Offset.CENTER));

        // C2GM.GNL.PNT.ACTPNT.DCNPNT supports the T modifier in the center of the graphic.
        this.layouts.put(C2GM_GNL_PNT_ACTPNT_DCNPNT,
            this.createLayout(SymbologyConstants.UNIQUE_DESIGNATION, Offset.CENTER, Offset.CENTER));

        // Most pentagon shaped graphics support the same modifiers around the pentagon.
        List<LabelLayout> layout = new ArrayList<LabelLayout>();
        this.addLayout(layout, SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(1.1, 1.0),
            Offset.fromFraction(0.0, 1.0));
        this.addLayout(layout, SymbologyConstants.ADDITIONAL_INFORMATION,
            Offset.TOP_CENTER,
            Offset.BOTTOM_CENTER);
        this.addLayout(layout, SymbologyConstants.HOSTILE_ENEMY,
            Offset.fromFraction(1.1, 0.35),
            Offset.fromFraction(0.0, 0.0));
        this.addLayout(layout, SymbologyConstants.DATE_TIME_GROUP,
            Offset.fromFraction(-0.1, 0.8),
            Offset.fromFraction(1.0, 0.0),
            Offset.fromFraction(-0.1, 0.8),
            Offset.fromFraction(1.0, 1.0));

        // Apply this layout to all the pentagon graphics that use it.
        this.putAll(layout,
            C2GM_GNL_PNT_ACTPNT_CHKPNT,
            C2GM_GNL_PNT_ACTPNT_LNKUPT,
            C2GM_GNL_PNT_ACTPNT_PSSPNT,
            C2GM_GNL_PNT_ACTPNT_RAYPNT,
            C2GM_GNL_PNT_ACTPNT_RELPNT,
            C2GM_GNL_PNT_ACTPNT_STRPNT,
            C2GM_GNL_PNT_ACTPNT_AMNPNT,
            C2GM_OFF_PNT_PNTD,
            MOBSU_OBSTBP_CSGSTE_ERP,
            MOBSU_CBRN_DECONP_USP,
            MOBSU_CBRN_DECONP_ALTUSP,
            MOBSU_CBRN_DECONP_TRP,
            MOBSU_CBRN_DECONP_EQT,
            MOBSU_CBRN_DECONP_EQTTRP,
            MOBSU_CBRN_DECONP_OPDECN,
            MOBSU_CBRN_DECONP_TRGH,
            FSUPP_PNT_C2PNT_SCP,
            FSUPP_PNT_C2PNT_FP,
            FSUPP_PNT_C2PNT_RP,
            FSUPP_PNT_C2PNT_HP,
            FSUPP_PNT_C2PNT_LP,
            CSS_PNT_CBNP,
            CSS_PNT_CCP,
            CSS_PNT_CVP,
            CSS_PNT_DCP,
            CSS_PNT_EPWCP,
            CSS_PNT_LRP,
            CSS_PNT_MCP,
            CSS_PNT_RRRP,
            CSS_PNT_ROM,
            CSS_PNT_TCP,
            CSS_PNT_TTP,
            CSS_PNT_UMC,
            CSS_PNT_SPT_GNL,
            CSS_PNT_SPT_CLS1,
            CSS_PNT_SPT_CLS2,
            CSS_PNT_SPT_CLS3,
            CSS_PNT_SPT_CLS4,
            CSS_PNT_SPT_CLS5,
            CSS_PNT_SPT_CLS6,
            CSS_PNT_SPT_CLS7,
            CSS_PNT_SPT_CLS8,
            CSS_PNT_SPT_CLS9,
            CSS_PNT_SPT_CLS10,
            CSS_PNT_AP_ASP,
            CSS_PNT_AP_ATP);

        // C2GM.GNL.PNT.ACTPNT supports all the normal pentagon graphic modifiers, and also supports H1 in the
        // middle of the pentagon.
        layout = new ArrayList<LabelLayout>(layout);
        this.addLayout(layout, SymbologyConstants.ADDITIONAL_INFORMATION,
            Offset.TOP_CENTER,
            Offset.BOTTOM_CENTER,
            Offset.fromFraction(0.5, 0.9),
            Offset.TOP_CENTER);
        this.layouts.put(C2GM_GNL_PNT_ACTPNT, layout);

        // CSS.PNT.AEP supports all the normal pentagon graphic modifiers, and also supports T1 in the
        // middle of the pentagon.
        layout = new ArrayList<LabelLayout>(layout);
        this.addLayout(layout, SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(1.1, 1.0),
            Offset.fromFraction(0.0, 1.0),
            Offset.CENTER,
            Offset.CENTER);
        this.addLayout(layout, SymbologyConstants.ADDITIONAL_INFORMATION,
            Offset.TOP_CENTER,
            Offset.BOTTOM_CENTER);
        this.layouts.put(CSS_PNT_AEP, layout);

        // The Chemical and Biological release graphics support the same modifiers.
        layout = new ArrayList<LabelLayout>();
        this.addLayout(layout, SymbologyConstants.LOCATION,
            Offset.fromFraction(0.5, -0.1),
            Offset.TOP_CENTER);
        this.addLayout(layout, SymbologyConstants.DATE_TIME_GROUP,
            Offset.fromFraction(0.0, 1.0),
            Offset.fromFraction(1.0, 1.0));
        this.addLayout(layout, SymbologyConstants.ADDITIONAL_INFORMATION,
            Offset.fromFraction(1.0, 1.0),
            Offset.fromFraction(0.0, 1.0));
        this.addLayout(layout, SymbologyConstants.HOSTILE_ENEMY,
            Offset.fromFraction(1.0, 0.0),
            Offset.fromFraction(0.0, 0.0));
        this.addLayout(layout, SymbologyConstants.TYPE,
            Offset.LEFT_CENTER,
            Offset.RIGHT_CENTER);
        this.addLayout(layout, SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(0.0, 0.0),
            Offset.fromFraction(1.0, 0.0));
        this.layouts.put(MOBSU_CBRN_REEVNT_BIO, layout);
        this.layouts.put(MOBSU_CBRN_REEVNT_CML, layout);

        // The Nuclear graphic is mostly the same as chem/bio, but also supports the quantity modifier.
        layout = new ArrayList<LabelLayout>(layout);
        this.addLayout(layout, SymbologyConstants.QUANTITY,
            Offset.TOP_CENTER,
            Offset.BOTTOM_CENTER);
        this.layouts.put(MOBSU_CBRN_NDGZ, layout);

        // C2GM.GNL.PNT.REFPNT.PNTINR supports the T modifier
        layout = this.createLayout(SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(0.5, 0.7), Offset.CENTER);
        this.layouts.put(C2GM_GNL_PNT_REFPNT_PNTINR, layout);

        // Square flag
        layout = this.createLayout(SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(0.5, 0.65), Offset.CENTER);
        this.layouts.put(C2GM_GNL_PNT_ACTPNT_CONPNT, layout);

        // X shaped graphics, T on left
        layout = this.createLayout(SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(0.75, 0.5),
            Offset.LEFT_CENTER);
        this.layouts.put(C2GM_GNL_PNT_ACTPNT_WAP, layout);
        this.layouts.put(FSUPP_PNT_C2PNT_FSS, layout);

        // Cross shaped graphics, T in upper right quad
        layout = this.createLayout(SymbologyConstants.UNIQUE_DESIGNATION,
            Offset.fromFraction(0.75, 0.75),
            Offset.fromFraction(0.0, 0.0));
        this.layouts.put(C2GM_DEF_PNT_TGTREF, layout);
        this.layouts.put(FSUPP_PNT_TGT_NUCTGT, layout);

        // FSUPP.PNT.TGT.PTGT is also cross shaped. In addition T in the upper right quad, it supports H and H1 in
        // the lower quads.
        layout = new ArrayList<LabelLayout>(layout);
        this.addLayout(layout, SymbologyConstants.ADDITIONAL_INFORMATION,
            Offset.fromFraction(0.75, 0.25),
            Offset.fromFraction(0.0, 1.0),
            Offset.fromFraction(0.25, 0.25),
            Offset.fromFraction(1.0, 1.0));
        this.layouts.put(FSUPP_PNT_TGT_PTGT, layout);

        // Tower graphics use the altitude modifier
        layout = this.createLayout(SymbologyConstants.ALTITUDE_DEPTH,
            Offset.fromFraction(0.75, 0.75),
            Offset.fromFraction(0.0, 0.0));
        this.layouts.put(MOBSU_OBST_AVN_TWR_LOW, layout);
        this.layouts.put(MOBSU_OBST_AVN_TWR_HIGH, layout);
    }

    /**
     * Create a simple layout map and populate it with one key value pair.
     *
     * @param key     Modifier key.
     * @param offset  Offset within the image at which to place the label.
     * @param hotspot Offset within the label to align with the label point in the image.
     *
     * @return New map, populated with one entry for the key/value pair specified in the parameters.
     */
    protected List<LabelLayout> createLayout(String key, Offset offset, Offset hotspot)
    {
        LabelLayout layout = new LabelLayout(key);
        layout.add(offset, hotspot);

        return Arrays.asList(layout);
    }

    /**
     * Add a layout to a layout map, possibly replacing an existing layout.
     *
     * @param layoutList List to which to add an entry.
     * @param key        Modifier key.
     * @param offsets    List of offsets from which to create one or more LabelLayout objects. The offsets are specified
     *                   in pairs: first the image offset and then the label offset. If multiple pairs are provided,
     *                   then multiple LabelLayouts will be created and added to the map.
     *
     * @throws IllegalArgumentException if offsets does not have even length.
     */
    protected void addLayout(List<LabelLayout> layoutList, String key, Offset... offsets)
    {
        if (offsets.length % 2 != 0)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", offsets.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LabelLayout layout = new LabelLayout(key);
        for (int i = 0; i < offsets.length; i += 2)
        {
            Offset offset = offsets[i];
            Offset hotspot = offsets[i + 1];

            layout.add(offset, hotspot);
        }

        layoutList.add(layout);
    }

    /**
     * Map one value to many keys.
     *
     * @param value Value to add.
     * @param keys  Keys that map to the value.
     */
    protected void putAll(List<LabelLayout> value, String... keys)
    {
        for (String sidc : keys)
        {
            this.layouts.put(sidc, value);
        }
    }
}
