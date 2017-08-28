/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

import gov.nasa.worldwind.render.Offset;

import java.util.*;

import static gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc.*;

/**
 * Object to provide default offsets for MIL-STD-2525C tactical point graphics. The offset is used to align a point on
 * the graphic with the graphic's geographic position. This class automatically populates the map with values
 * appropriate for the point graphic images supplied by WorldWind.
 *
 * @author pabercrombie
 * @version $Id: DefaultOffsets.java 542 2012-04-24 19:08:12Z pabercrombie $
 * @see gov.nasa.worldwind.symbology.AbstractTacticalSymbol#setOffset(gov.nasa.worldwind.render.Offset)
 */
public class DefaultOffsets
{
    /** Offset to align the center of the graphic with the geographic position. */
    protected static Offset DEFAULT_OFFSET = Offset.CENTER;

    /** Offset to align a point 25% up from the bottom edge with the geographic position. */
    public static final Offset OFFSET_BOTTOM_QUARTER = Offset.fromFraction(0.5, 0.25);

    /** Offset for the Drop Point graphic (2.X.2.1.1.3.2). */
    public static final Offset OFFSET_C2GM_GNL_PNT_WPN_DRPPNT = Offset.fromFraction(0.5, 0.17);

    /** Offset for the Antitank Mine with Anti-handling Device graphic (2.X.3.1.5.3). */
    public static final Offset OFFSET_MOBSU_OBST_MNE_ATMAHD = Offset.fromFraction(0.5, 0.75);
    /** Offset for the Antipersonnel (AP) Mines graphic (2.X.3.1.5.5). */
    public static final Offset OFFSET_MOBSU_OBST_MNE_APMNE = Offset.fromFraction(0.5, 0.35);
    /** Offset for the Wide Area Mines graphic (2.X.3.1.5.6). */
    public static final Offset OFFSET_MOBSU_OBST_MNE_WAMNE = Offset.fromFraction(0.5, 0.71);

    /** Offset for the Sea Mine-Like Hazard graphic (2.X.6.2.1). */
    public static final Offset OFFSET_OTH_HAZ_SML = Offset.fromFraction(0.5, 0.33);

    /** Map to store defaults. */
    protected Map<String, Offset> offsets = new HashMap<String, Offset>();

    /** Create the map and populate it with the default offsets. */
    public DefaultOffsets()
    {
        this.populate();
    }

    /**
     * Determine the default offset for a graphic.
     *
     * @param sidc Masked SIDC for a point graphic.
     *
     * @return Default offset for the specified graphic.
     */
    public Offset get(String sidc)
    {
        Offset offset = this.offsets.get(sidc);
        return offset != null ? offset : DEFAULT_OFFSET;
    }

    /** Populate the map with default offsets. */
    protected void populate()
    {
        // A bunch of graphics are anchored on the bottom edge
        this.putAll(Offset.BOTTOM_CENTER,
            C2GM_GNL_PNT_USW_UH2_BCON,
            C2GM_GNL_PNT_USW_UH2_LCON,
            C2GM_GNL_PNT_USW_UH2_SNK,
            C2GM_GNL_PNT_USW_SNBY,
            C2GM_GNL_PNT_USW_SNBY_BT,

            C2GM_GNL_PNT_REFPNT_PNTINR,
            C2GM_GNL_PNT_WPN_ENTPNT,
            C2GM_GNL_PNT_WPN_GRDZRO,
            C2GM_GNL_PNT_WPN_MSLPNT,

            C2GM_GNL_PNT_ACTPNT,
            C2GM_GNL_PNT_ACTPNT_CHKPNT,
            C2GM_GNL_PNT_ACTPNT_CONPNT,
            C2GM_GNL_PNT_ACTPNT_LNKUPT,
            C2GM_GNL_PNT_ACTPNT_PSSPNT,
            C2GM_GNL_PNT_ACTPNT_RAYPNT,
            C2GM_GNL_PNT_ACTPNT_RELPNT,
            C2GM_GNL_PNT_ACTPNT_STRPNT,
            C2GM_GNL_PNT_ACTPNT_AMNPNT,

            C2GM_AVN_PNT_DAPP,
            C2GM_OFF_PNT_PNTD,

            MOBSU_OBST_ATO_TDTSM_FIXPFD,
            MOBSU_OBST_ATO_TDTSM_MVB,
            MOBSU_OBST_ATO_TDTSM_MVBPFD,
            MOBSU_OBST_AVN_TWR_LOW,
            MOBSU_OBST_AVN_TWR_HIGH,
            MOBSU_OBSTBP_CSGSTE_ERP,

            MOBSU_CBRN_NDGZ,
            MOBSU_CBRN_FAOTP,
            MOBSU_CBRN_REEVNT_BIO,
            MOBSU_CBRN_REEVNT_CML,
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

            CSS_PNT_AEP,
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
            CSS_PNT_AP_ATP,

            OTH_ER_DTHAC,
            OTH_ER_PIW,
            OTH_ER_DSTVES,

            OTH_SSUBSR_BTMRTN,
            OTH_SSUBSR_BTMRTN_INS,
            OTH_SSUBSR_BTMRTN_SBRSOO,
            OTH_SSUBSR_SA,

            EmsSidc.NATEVT_GEO_AVL,
            EmsSidc.NATEVT_GEO_LNDSLD,
            EmsSidc.NATEVT_GEO_SBSDNC,
            EmsSidc.NATEVT_GEO_VLCTHT,
            EmsSidc.NATEVT_HYDMET_DRGHT,
            EmsSidc.NATEVT_HYDMET_FLD,
            EmsSidc.NATEVT_HYDMET_INV,
            EmsSidc.NATEVT_HYDMET_TSNMI,
            EmsSidc.NATEVT_INFST_BIRD,
            EmsSidc.NATEVT_INFST_INSCT,
            EmsSidc.NATEVT_INFST_MICROB,
            EmsSidc.NATEVT_INFST_REPT,
            EmsSidc.NATEVT_INFST_RDNT
        );

        // Sonobouy and a few other graphics are anchored a point 25% up from the bottom edge.
        this.putAll(OFFSET_BOTTOM_QUARTER,
            C2GM_GNL_PNT_USW_SNBY,
            C2GM_GNL_PNT_USW_SNBY_PTNCTR,
            C2GM_GNL_PNT_USW_SNBY_DIFAR,
            C2GM_GNL_PNT_USW_SNBY_LOFAR,
            C2GM_GNL_PNT_USW_SNBY_CASS,
            C2GM_GNL_PNT_USW_SNBY_DICASS,
            C2GM_GNL_PNT_USW_SNBY_BT,
            C2GM_GNL_PNT_USW_SNBY_ANM,
            C2GM_GNL_PNT_USW_SNBY_VLAD,
            C2GM_GNL_PNT_USW_SNBY_ATAC,
            C2GM_GNL_PNT_USW_SNBY_RO,
            C2GM_GNL_PNT_USW_SNBY_KGP,
            C2GM_GNL_PNT_USW_SNBY_EXP,
            MOBSU_OBST_BBY,
            MOBSU_OBST_MNE_ATMDIR);

        // A handful of graphics have unique offsets
        this.offsets.put(C2GM_GNL_PNT_WPN_DRPPNT, OFFSET_C2GM_GNL_PNT_WPN_DRPPNT);
        this.offsets.put(MOBSU_OBST_MNE_ATMAHD, OFFSET_MOBSU_OBST_MNE_ATMAHD);
        this.offsets.put(MOBSU_OBST_MNE_APMNE, OFFSET_MOBSU_OBST_MNE_APMNE);
        this.offsets.put(MOBSU_OBST_MNE_WAMNE, OFFSET_MOBSU_OBST_MNE_WAMNE);
        this.offsets.put(OTH_HAZ_SML, OFFSET_OTH_HAZ_SML);
        this.offsets.put(OTH_SSUBSR_MARLFE, Offset.LEFT_CENTER);
    }

    /**
     * Map one value to many keys.
     *
     * @param value Value to add.
     * @param keys  Keys that map to the value.
     */
    protected void putAll(Offset value, String... keys)
    {
        for (String sidc : keys)
        {
            this.offsets.put(sidc, value);
        }
    }
}
