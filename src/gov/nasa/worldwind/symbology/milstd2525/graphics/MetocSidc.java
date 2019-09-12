/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

/**
 * SIDC constants for graphics in the "Meteorological and Oceanographic" scheme (MIL-STD-2525C Appendix C). The
 * constants in this interface are "masked" SIDCs. All fields except Scheme, Category, and Function ID are filled with
 * hyphens. (The other fields do not identity a type of graphic, they modify the graphic.)
 *
 * @author pabercrombie
 * @version $Id: MetocSidc.java 442 2012-03-10 19:48:24Z pabercrombie $
 */
public interface MetocSidc
{
    /** Low pressure center. */
    final String AMPHC_PRS_LOWCTR = "WAS-PL----P----";
    /** Cyclone center. */
    final String AMPHC_PRS_LOWCTR_CYC = "WAS-PC----P----";
    /** Tropopause low. */
    final String AMPHC_PRS_LOWCTR_TROPLW = "WAS-PLT---P----";
    /** High pressure center. */
    final String AMPHC_PRS_HGHCTR = "WAS-PH----P----";
    /** Anticyclone center. */
    final String AMPHC_PRS_HGHCTR_ACYC = "WAS-PA----P----";
    /** Tropopause high. */
    final String AMPHC_PRS_HGHCTR_TROPHG = "WAS-PHT---P----";
    /** Frontal systems. */
    final String AMPHC_PRS_FRNSYS = "WA-DPF-----L---";
    /** Cold front. */
    final String AMPHC_PRS_FRNSYS_CLDFRN = "WA-DPFC----L---";
    /** Upper cold front. */
    final String AMPHC_PRS_FRNSYS_CLDFRN_UPP = "WA-DPFCU---L---";
    /** Cold frontogenesis. */
    final String AMPHC_PRS_FRNSYS_CLDFRN_FRGS = "WA-DPFC-FG-L---";
    /** Cold frontolysis. */
    final String AMPHC_PRS_FRNSYS_CLDFRN_FRLS = "WA-DPFC-FY-L---";
    /** Warm front. */
    final String AMPHC_PRS_FRNSYS_WRMFRN = "WA-DPFW----L---";
    /** Upper warm front. */
    final String AMPHC_PRS_FRNSYS_WRMFRN_UPP = "WA-DPFWU---L---";
    /** Warm frontogenesis. */
    final String AMPHC_PRS_FRNSYS_WRMFRN_FRGS = "WA-DPFW-FG-L---";
    /** Warm frontolysis. */
    final String AMPHC_PRS_FRNSYS_WRMFRN_FRLS = "WA-DPFW-FY-L---";
    /** Occluded front. */
    final String AMPHC_PRS_FRNSYS_OCD = "WA-DPFO----L---";
    /** Upper occluded front. */
    final String AMPHC_PRS_FRNSYS_OCD_UPP = "WA-DPFOU---L---";
    /** Occluded frontolysis. */
    final String AMPHC_PRS_FRNSYS_OCD_FRLS = "WA-DPFO-FY-L---";
    /** Stationary front. */
    final String AMPHC_PRS_FRNSYS_STAT = "WA-DPFS----L---";
    /** Upper stationary front. */
    final String AMPHC_PRS_FRNSYS_STAT_UPP = "WA-DPFSU---L---";
    /** Stationary frontogenesis. */
    final String AMPHC_PRS_FRNSYS_STAT_FRGS = "WA-DPFS-FG-L---";
    /** Stationary frontolysis. */
    final String AMPHC_PRS_FRNSYS_STAT_FRLS = "WA-DPFS-FY-L---";
    /** Pressure systems, trough axis. */
    final String AMPHC_PRS_LNE_TRUAXS = "WA-DPXT----L---";
    /** Ridge axis. */
    final String AMPHC_PRS_LNE_RDGAXS = "WA-DPXR----L---";
    /** Severe squall line. */
    final String AMPHC_PRS_LNE_SSL = "WA-DPXSQ---L---";
    /** Instability line. */
    final String AMPHC_PRS_LNE_ISTB = "WA-DPXIL---L---";
    /** Shear line. */
    final String AMPHC_PRS_LNE_SHA = "WA-DPXSH---L---";
    /** Inter-tropical convergance zone. */
    final String AMPHC_PRS_LNE_ITCZ = "WA-DPXITCZ-L---";
    /** Convergance line. */
    final String AMPHC_PRS_LNE_CNGLNE = "WA-DPXCV---L---";
    /** Inter-tropical discontinuity. */
    final String AMPHC_PRS_LNE_ITD = "WA-DPXITD--L---";
    /** Turbulence - light. */
    final String AMPHC_TRB_LIT = "WAS-TL----P----";
    /** Turbulence - moderate. */
    final String AMPHC_TRB_MOD = "WAS-TM----P----";
    /** Turbulence - severe. */
    final String AMPHC_TRB_SVR = "WAS-TS----P----";
    /** Turbulence - extreme. */
    final String AMPHC_TRB_EXT = "WAS-TE----P----";
    /** Mountain waves. */
    final String AMPHC_TRB_MNTWAV = "WAS-T-MW--P----";
    /** Clear icing - light. */
    final String AMPHC_ICG_CLR_LIT = "WAS-ICL---P----";
    /** Clear icing - moderate. */
    final String AMPHC_ICG_CLR_MOD = "WAS-ICM---P----";
    /** Clear icing - severe. */
    final String AMPHC_ICG_CLR_SVR = "WAS-ICS---P----";
    /** Rime icing - light. */
    final String AMPHC_ICG_RIME_LIT = "WAS-IRL---P----";
    /** Rime icing - moderate. */
    final String AMPHC_ICG_RIME_MOD = "WAS-IRM---P----";
    /** Rime icing - severe. */
    final String AMPHC_ICG_RIME_SVR = "WAS-IRS---P----";
    /** Mixed icing - light. */
    final String AMPHC_ICG_MIX_LIT = "WAS-IML---P----";
    /** Mixed icing - moderate. */
    final String AMPHC_ICG_MIX_MOD = "WAS-IMM---P----";
    /** Mixed icing - severe. */
    final String AMPHC_ICG_MIX_SVR = "WAS-IMS---P----";
    /** Calm winds. */
    final String AMPHC_WND_CALM = "WAS-WC----P----";
    /** Wind plot. */
    final String AMPHC_WND_PLT = "WAS-WP----P----";
    /** Jet stream. */
    final String AMPHC_WND_JTSM = "WA-DWJ-----L---";
    /** Stream line. */
    final String AMPHC_WND_SMLNE = "WA-DWS-----L---";
    /** Clear sky. */
    final String AMPHC_CUDCOV_SYM_SKC = "WAS-CCCSCSP----";
    /** Few coverage. */
    final String AMPHC_CUDCOV_SYM_FEW = "WAS-CCCSFCP----";
    /** Scattered coverage. */
    final String AMPHC_CUDCOV_SYM_SCT = "WAS-CCCSSCP----";
    /** Broken coverage. */
    final String AMPHC_CUDCOV_SYM_BKN = "WAS-CCCSBCP----";
    /** Overcast coverage. */
    final String AMPHC_CUDCOV_SYM_OVC = "WAS-CCCSOCP----";
    /** Sky totally or partially obscured. */
    final String AMPHC_CUDCOV_SYM_STOPO = "WAS-CCCSOBP----";
    /** Rain - intermittent light. */
    final String AMPHC_WTH_RA_INMLIT = "WAS-WSR-LIP----";
    /** Rain - continuous light. */
    final String AMPHC_WTH_RA_INMLIT_CTSLIT = "WAS-WSR-LCP----";
    /** Rain - intermittent moderate. */
    final String AMPHC_WTH_RA_INMMOD = "WAS-WSR-MIP----";
    /** Rain - continuous moderate. */
    final String AMPHC_WTH_RA_INMMOD_CTSMOD = "WAS-WSR-MCP----";
    /** Rain - intermittent heavy. */
    final String AMPHC_WTH_RA_INMHVY = "WAS-WSR-HIP----";
    /** Rain - continuous heavy. */
    final String AMPHC_WTH_RA_INMHVY_CTSHVY = "WAS-WSR-HCP----";
    /** Freezing rain - light. */
    final String AMPHC_WTH_FZRA_LIT = "WAS-WSRFL-P----";
    /** Freezing rain - moderate/heavy. */
    final String AMPHC_WTH_FZRA_MODHVY = "WAS-WSRFMHP----";
    /** Rain showers - light. */
    final String AMPHC_WTH_RASWR_LIT = "WAS-WSRSL-P----";
    /** Rain showers - moderate/heavy. */
    final String AMPHC_WTH_RASWR_MODHVY = "WAS-WSRSMHP----";
    /** Rain showers - torrential. */
    final String AMPHC_WTH_RASWR_TOR = "WAS-WSRST-P----";
    /** Drizzle - intermittent light. */
    final String AMPHC_WTH_DZ_INMLIT = "WAS-WSD-LIP----";
    /** Drizzle - continuous light. */
    final String AMPHC_WTH_DZ_INMLIT_CTSLIT = "WAS-WSD-LCP----";
    /** Drizzle - intermittent moderate. */
    final String AMPHC_WTH_DZ_INMMOD = "WAS-WSD-MIP----";
    /** Drizzle - continuous moderate. */
    final String AMPHC_WTH_DZ_INMMOD_CTSMOD = "WAS-WSD-MCP----";
    /** Drizzle - intermittent heavy. */
    final String AMPHC_WTH_DZ_INMHVY = "WAS-WSD-HIP----";
    /** Drizzle - continuous heavy. */
    final String AMPHC_WTH_DZ_INMHVY_CTSHVY = "WAS-WSD-HCP----";
    /** Freezing drizzle - light. */
    final String AMPHC_WTH_FZDZ_LIT = "WAS-WSDFL-P----";
    /** Freezing drizzle - moderate/heavy. */
    final String AMPHC_WTH_FZDZ_MODHVY = "WAS-WSDFMHP----";
    /** Rain or drizzle and snow - light. */
    final String AMPHC_WTH_RASN_RDSLIT = "WAS-WSM-L-P----";
    /** Rain or drizzle and snow - moderate/heavy. */
    final String AMPHC_WTH_RASN_RDSMH = "WAS-WSM-MHP----";
    /** Rain and snow showers - light. */
    final String AMPHC_WTH_RASN_SWRLIT = "WAS-WSMSL-P----";
    /** Rain and snow showers - moderate/heavy. */
    final String AMPHC_WTH_RASN_SWRMOD = "WAS-WSMSMHP----";
    /** Snow - intermittent light. */
    final String AMPHC_WTH_SN_INMLIT = "WAS-WSS-LIP----";
    /** Snow - continuous light. */
    final String AMPHC_WTH_SN_INMLIT_CTSLIT = "WAS-WSS-LCP----";
    /** Snow - intermittent moderate. */
    final String AMPHC_WTH_SN_INMMOD = "WAS-WSS-MIP----";
    /** Snow - continuous moderate. */
    final String AMPHC_WTH_SN_INMMOD_CTSMOD = "WAS-WSS-MCP----";
    /** Snow - intermittent heavy. */
    final String AMPHC_WTH_SN_INMHVY = "WAS-WSS-HIP----";
    /** Snow - continuous heavy. */
    final String AMPHC_WTH_SN_INMHVY_CTSHVY = "WAS-WSS-HCP----";
    /** Blowing snow - light/moderate. */
    final String AMPHC_WTH_SN_BLSNLM = "WAS-WSSBLMP----";
    /** Blowing snow - heavy. */
    final String AMPHC_WTH_SN_BLSNHY = "WAS-WSSBH-P----";
    /** Snow grains. */
    final String AMPHC_WTH_SG = "WAS-WSSG--P----";
    /** Snow showers - light. */
    final String AMPHC_WTH_SSWR_LIT = "WAS-WSSSL-P----";
    /** Snow showers - moderate/heavy. */
    final String AMPHC_WTH_SSWR_MODHVY = "WAS-WSSSMHP----";
    /** Hail - light not associated with thunder. */
    final String AMPHC_WTH_HL_LIT = "WAS-WSGRL-P----";
    /** Hail - moderate/heavy not associated with thunder. */
    final String AMPHC_WTH_HL_MODHVY = "WAS-WSGRMHP----";
    /** Ice crystals (diamond dust). */
    final String AMPHC_WTH_IC = "WAS-WSIC--P----";
    /** Ice pellets - light. */
    final String AMPHC_WTH_PE_LIT = "WAS-WSPLL-P----";
    /** Ice pellets - moderate. */
    final String AMPHC_WTH_PE_MOD = "WAS-WSPLM-P----";
    /** Ice pellets - heavy. */
    final String AMPHC_WTH_PE_HVY = "WAS-WSPLH-P----";
    /** Thunderstorm - no precipitation. */
    final String AMPHC_WTH_STMS_TS = "WAS-WST-NPP----";
    /** Thunderstorm light to moderate with rain/snow - no hail. */
    final String AMPHC_WTH_STMS_TSLMNH = "WAS-WSTMR-P----";
    /** Thunderstorm heavy with rain/snow - no hail. */
    final String AMPHC_WTH_STMS_TSHVNH = "WAS-WSTHR-P----";
    /** Thunderstorm light to moderate - with hail. */
    final String AMPHC_WTH_STMS_TSLMWH = "WAS-WSTMH-P----";
    /** Thunderstorm heavy - with hail. */
    final String AMPHC_WTH_STMS_TSHVWH = "WAS-WSTHH-P----";
    /** Funnel cloud (tornado/waterspout). */
    final String AMPHC_WTH_STMS_FC = "WAS-WST-FCP----";
    /** Squall. */
    final String AMPHC_WTH_STMS_SQL = "WAS-WST-SQP----";
    /** Lightning. */
    final String AMPHC_WTH_STMS_LTG = "WAS-WST-LGP----";
    /** Fog - shallow patches. */
    final String AMPHC_WTH_FG_SHWPTH = "WAS-WSFGPSP----";
    /** Fog -shallow continuous. */
    final String AMPHC_WTH_FG_SHWCTS = "WAS-WSFGCSP----";
    /** Fog - patchy. */
    final String AMPHC_WTH_FG_PTHY = "WAS-WSFGP-P----";
    /** Fog - sky visible. */
    final String AMPHC_WTH_FG_SKYVSB = "WAS-WSFGSVP----";
    /** Fog - sky obscured. */
    final String AMPHC_WTH_FG_SKYOBD = "WAS-WSFGSOP----";
    /** Fog - freezing, sky visible. */
    final String AMPHC_WTH_FG_FZSV = "WAS-WSFGFVP----";
    /** Fog - freezing, sky not visible. */
    final String AMPHC_WTH_FG_FZSNV = "WAS-WSFGFOP----";
    /** Mist. */
    final String AMPHC_WTH_MIST = "WAS-WSBR--P----";
    /** Smoke. */
    final String AMPHC_WTH_FU = "WAS-WSFU--P----";
    /** Haze. */
    final String AMPHC_WTH_HZ = "WAS-WSHZ--P----";
    /** Dust/sand storm - light to moderate. */
    final String AMPHC_WTH_DTSD_LITMOD = "WAS-WSDSLMP----";
    /** Dust/sand storm - severe. */
    final String AMPHC_WTH_DTSD_SVR = "WAS-WSDSS-P----";
    /** Dust devil. */
    final String AMPHC_WTH_DTSD_DTDVL = "WAS-WSDD--P----";
    /** Blowing dust or sand. */
    final String AMPHC_WTH_DTSD_BLDTSD = "WAS-WSDB--P----";
    /** Tropical depression. */
    final String AMPHC_WTH_TPLSYS_TROPDN = "WAS-WSTSD-P----";
    /** Tropical storm. */
    final String AMPHC_WTH_TPLSYS_TROPSM = "WAS-WSTSS-P----";
    /** Hurricane/typhoon. */
    final String AMPHC_WTH_TPLSYS_HC = "WAS-WSTSH-P----";
    /** Tropical storm wind areas and date/time labels. */
    final String AMPHC_WTH_TPLSYS_TSWADL = "WA-DWSTSWA--A--";
    /** Volcanic eruption. */
    final String AMPHC_WTH_VOLERN = "WAS-WSVE--P----";
    /** Volcanic ash. */
    final String AMPHC_WTH_VOLERN_VOLASH = "WAS-WSVA--P----";
    /** Tropopause level. */
    final String AMPHC_WTH_TROPLV = "WAS-WST-LVP----";
    /** Freezing level. */
    final String AMPHC_WTH_FZLVL = "WAS-WSF-LVP----";
    /** Precipitation of unknown type and intensity. */
    final String AMPHC_WTH_POUTAI = "WAS-WSUKP-P----";
    /** Instrument flight rule (IFR). */
    final String AMPHC_BDAWTH_IFR = "WA-DBAIF----A--";
    /** Marginal visual flight rule (MVFR). */
    final String AMPHC_BDAWTH_MVFR = "WA-DBAMV----A--";
    /** Turbulence. */
    final String AMPHC_BDAWTH_TRB = "WA-DBATB----A--";
    /** Icing. */
    final String AMPHC_BDAWTH_ICG = "WA-DBAI-----A--";
    /** Liquid precipitation - non-convective continuous or intermittent. */
    final String AMPHC_BDAWTH_LPNCI = "WA-DBALPNC--A--";
    /** Liquid precipitation - convective. */
    final String AMPHC_BDAWTH_LPNCI_LPC = "WA-DBALPC---A--";
    /** Freezing/frozen precipitation. */
    final String AMPHC_BDAWTH_FZPPN = "WA-DBAFP----A--";
    /** Thunderstorms. */
    final String AMPHC_BDAWTH_TS = "WA-DBAT-----A--";
    /** Fog. */
    final String AMPHC_BDAWTH_FG = "WA-DBAFG----A--";
    /** Dust or sand. */
    final String AMPHC_BDAWTH_DTSD = "WA-DBAD-----A--";
    /** Operator-defined freeform. */
    final String AMPHC_BDAWTH_ODFF = "WA-DBAFF----A--";
    /** Isobar - surface. */
    final String AMPHC_ISP_ISB = "WA-DIPIB---L---";
    /** Contour - upper air. */
    final String AMPHC_ISP_CTUR = "WA-DIPCO---L---";
    /** Isotherm. */
    final String AMPHC_ISP_IST = "WA-DIPIS---L---";
    /** Isotach. */
    final String AMPHC_ISP_ISH = "WA-DIPIT---L---";
    /** Isodrosotherm. */
    final String AMPHC_ISP_ISD = "WA-DIPID---L---";
    /** Thickness. */
    final String AMPHC_ISP_THK = "WA-DIPTH---L---";
    /** Operator-defined freeform. */
    final String AMPHC_ISP_ODFF = "WA-DIPFF---L---";
    /** Surface dry without cracks or appreciable dust or loose sand. */
//    final String AMPHC_STOG_WOSMIC_SUFDRY = "WAS-GND-NCP----";
    /** Surface moist. */
    final String AMPHC_STOG_WOSMIC_SUFMST = "WAS-GNM---P----";
    /** Surface wet, standing water in small or large pools. */
    final String AMPHC_STOG_WOSMIC_SUFWET = "WAS-GNW-SWP----";
    /** Surface flooded. */
    final String AMPHC_STOG_WOSMIC_SUFFLD = "WAS-GNFL--P----";
    /** Surface frozen. */
    final String AMPHC_STOG_WOSMIC_SUFFZN = "WAS-GNFZ--P----";
    /** Glaze (thin ice) on ground. */
    final String AMPHC_STOG_WOSMIC_GLZGRD = "WAS-GNG-TIP----";
    /** Loose dry dust or sand not covering ground completely. */
    final String AMPHC_STOG_WOSMIC_LDNCGC = "WAS-GNLDN-P----";
    /** Thin loose dry dust or sand covering ground completely. */
    final String AMPHC_STOG_WOSMIC_TLDCGC = "WAS-GNLDTCP----";
    /** Moderate/thick loose dry dust or sand covering ground completely. */
    final String AMPHC_STOG_WOSMIC_MLDCGC = "WAS-GNLDMCP----";
    /** Extremely dry with cracks. */
    final String AMPHC_STOG_WOSMIC_EXTDWC = "WAS-GNDEWCP----";
    /** Predominately ice covered. */
    final String AMPHC_STOG_WSMIC_PDMIC = "WAS-GSI---P----";
    /** Compact or wet snow (with or without ice) covering less than one-half of ground. */
    final String AMPHC_STOG_WSMIC_CWSNLH = "WAS-GSSCL-P----";
    /** Compact or wet snow (with or without ice) covering at least one-half ground, but ground not completely covered. */
    final String AMPHC_STOG_WSMIC_CSNALH = "WAS-GSSCH-P----";
    /** Even layer of compact or wet snow covering ground completely. */
    final String AMPHC_STOG_WSMIC_ELCSCG = "WAS-GSSCCEP----";
    /** Uneven layer of compact or wet snow covering ground completely. */
    final String AMPHC_STOG_WSMIC_ULCSCG = "WAS-GSSCCUP----";
    /** Loose dry snow covering less than one-half of ground. */
    final String AMPHC_STOG_WSMIC_LDSNLH = "WAS-GSSLL-P----";
    /** Loose dry snow covering at least one-half ground, but ground not completely covered. */
    final String AMPHC_STOG_WSMIC_LDSALH = "WAS-GSSLH-P----";
    /** Even layer of loose dry snow covering ground completely. */
    final String AMPHC_STOG_WSMIC_ELDSCG = "WAS-GSSLCEP----";
    /** Uneven layer of loose dry snow covering ground completely. */
    final String AMPHC_STOG_WSMIC_ULDSCG = "WAS-GSSLCUP----";
    /** Snow covering ground completely; deep drifts. */
    final String AMPHC_STOG_WSMIC_SCGC = "WAS-GSSDC-P----";
    /** Icebergs. */
    final String OCA_ISYS_IB = "WOS-IB----P----";
    /** Many icebergs. */
    final String OCA_ISYS_IB_MNY = "WOS-IBM---P----";
    /** Belts and strips. */
    final String OCA_ISYS_IB_BAS = "WOS-IBBS--P----";
    /** Iceberg -general. */
    final String OCA_ISYS_IB_GNL = "WOS-IBG---P----";
    /** Many icebergs -general. */
    final String OCA_ISYS_IB_MNYGNL = "WOS-IBMG--P----";
    /** Bergy bit. */
    final String OCA_ISYS_IB_BB = "WOS-IBBB--P----";
    /** Many bergy bits. */
    final String OCA_ISYS_IB_MNYBB = "WOS-IBBBM-P----";
    /** Growler. */
    final String OCA_ISYS_IB_GWL = "WOS-IBGL--P----";
    /** Many growlers. */
    final String OCA_ISYS_IB_MNYGWL = "WOS-IBGLM-P----";
    /** Floeberg. */
    final String OCA_ISYS_IB_FBG = "WOS-IBF---P----";
    /** Ice island. */
    final String OCA_ISYS_IB_II = "WOS-IBII--P----";
    /** Bergy water. */
    final String OCA_ISYS_ICN_BW = "WOS-ICWB--P----";
    /** Water with radar targets. */
    final String OCA_ISYS_ICN_WWRT = "WOS-ICWR--P----";
    /** Ice free. */
    final String OCA_ISYS_ICN_IF = "WOS-ICIF--P----";
    /** Convergence. */
    final String OCA_ISYS_DYNPRO_CNG = "WOS-IDC---P----";
    /** Divergence. */
    final String OCA_ISYS_DYNPRO_DVG = "WOS-IDD---P----";
    /** Shearing or shear zone. */
    final String OCA_ISYS_DYNPRO_SHAZ = "WOS-IDS---P----";
    /** Ice drift (direction). */
    final String OCA_ISYS_DYNPRO_ID = "WO-DIDID---L---";
    /** Sea ice. */
    final String OCA_ISYS_SI = "WOS-II----P----";
    /** Ice thickness (observed). */
    final String OCA_ISYS_SI_ITOBS = "WOS-IITM--P----";
    /** Ice thickness (estimated). */
    final String OCA_ISYS_SI_ITEST = "WOS-IITE--P----";
    /** Melt puddles or flooded ice. */
    final String OCA_ISYS_SI_MPOFI = "WOS-IIP---P----";
    /** Limit of visual observation. */
    final String OCA_ISYS_LMT_LOVO = "WO-DILOV---L---";
    /** Limit of undercast. */
    final String OCA_ISYS_LMT_LOU = "WO-DILUC---L---";
    /** Limit of radar observation. */
    final String OCA_ISYS_LMT_LORO = "WO-DILOR---L---";
    /** Observed ice edge or boundary. */
    final String OCA_ISYS_LMT_OIEOB = "WO-DILIEO--L---";
    /** Estimated ice edge or boundary. */
    final String OCA_ISYS_LMT_EIEOB = "WO-DILIEE--L---";
    /** Ice edge or boundary from radar. */
    final String OCA_ISYS_LMT_IEOBFR = "WO-DILIER--L---";
    /** Openings in the ice, cracks. */
    final String OCA_ISYS_OITI_CRK = "WO-DIOC----L---";
    /** Cracks at a specific location. */
    final String OCA_ISYS_OITI_CRKASL = "WO-DIOCS---L---";
    /** Lead. */
    final String OCA_ISYS_OITI_LED = "WO-DIOL----L---";
    /** Frozen lead. */
    final String OCA_ISYS_OITI_FZLED = "WO-DIOLF---L---";
    /** Snow cover. */
    final String OCA_ISYS_SC = "WOS-ISC---P----";
    /** Sastrugi (with orientation). */
    final String OCA_ISYS_SC_SWO = "WOS-ISS---P----";
    /** Ridges or hummocks. */
    final String OCA_ISYS_TOPFTR_HUM = "WOS-ITRH--P----";
    /** Rafting. */
    final String OCA_ISYS_TOPFTR_RFTG = "WOS-ITR---P----";
    /** Jammed brash barrier. */
    final String OCA_ISYS_TOPFTR_JBB = "WOS-ITBB--P----";
    /** Soundings. */
    final String OCA_HYDGRY_DPH_SNDG = "WOS-HDS---P----";
    /** Depth curve. */
    final String OCA_HYDGRY_DPH_CRV = "WO-DHDDL---L---";
    /** Depth contour. */
    final String OCA_HYDGRY_DPH_CTUR = "WO-DHDDC---L---";
    /** Depth area. */
    final String OCA_HYDGRY_DPH_ARA = "WO-DHDDA----A--";
    /** Coastline. */
    final String OCA_HYDGRY_CSTHYD_CSTLN = "WO-DHCC----L---";
    /** Island. */
    final String OCA_HYDGRY_CSTHYD_ISND = "WO-DHCI-----A--";
    /** Beach. */
    final String OCA_HYDGRY_CSTHYD_BEH = "WO-DHCB-----A--";
    /** Water. */
    final String OCA_HYDGRY_CSTHYD_H2O = "WO-DHCW-----A--";
    /** Foreshore. */
    final String OCA_HYDGRY_CSTHYD_FSH1_FSH2 = "WO-DHCF----L---";
    /** Foreshore. */
    final String OCA_HYDGRY_CSTHYD_FSH1_FSH3 = "WO-DHCF-----A--";
    /** Berths (onshore). */
    final String OCA_HYDGRY_PRTHBR_PRT_BRHSO = "WOS-HPB-O-P----";
    /** Berths (anchor). */
    final String OCA_HYDGRY_PRTHBR_PRT_BRHSA = "WOS-HPB-A-P----";
    /** Anchorage. */
    final String OCA_HYDGRY_PRTHBR_PRT_ANCRG1 = "WOS-HPBA--P----";
    /** Anchorage. */
    final String OCA_HYDGRY_PRTHBR_PRT_ANCRG2 = "WO-DHPBA---L---";
    /** Anchorage. */
    final String OCA_HYDGRY_PRTHBR_PRT_ANCRG3 = "WO-DHPBA----A--";
    /** Call in point. */
    final String OCA_HYDGRY_PRTHBR_PRT_CIP = "WOS-HPCP--P----";
    /** Pier/wharf/quay. */
    final String OCA_HYDGRY_PRTHBR_PRT_PWQ = "WO-DHPBP---L---";
    /** Fishing harbor. */
    final String OCA_HYDGRY_PRTHBR_FSG_FSGHBR = "WOS-HPFH--P----";
    /** Fish stakes/traps/weirs. */
    final String OCA_HYDGRY_PRTHBR_FSG_FSTK1 = "WOS-HPFS--P----";
    /** Fish stakes/traps/weirs. */
    final String OCA_HYDGRY_PRTHBR_FSG_FSTK2 = "WOS-HPFS---L---";
    /** Fish stakes/traps/weirs. */
    final String OCA_HYDGRY_PRTHBR_FSG_FSTK3 = "WOS-HPFF----A--";
    /** Drydock. */
    final String OCA_HYDGRY_PRTHBR_FAC_DDCK = "WO-DHPMD----A--";
    /** Landing place. */
    final String OCA_HYDGRY_PRTHBR_FAC_LNDPLC = "WOS-HPML--P----";
    /** Offshore loading facility. */
    final String OCA_HYDGRY_PRTHBR_FAC_OSLF1 = "WO-DHPMO--P----";
    /** Offshore loading facility. */
    final String OCA_HYDGRY_PRTHBR_FAC_OSLF2 = "WO-DHPMO---L---";
    /** Offshore loading facility. */
    final String OCA_HYDGRY_PRTHBR_FAC_OSLF3 = "WO-DHPMO----A--";
    /** Ramp (above water). */
    final String OCA_HYDGRY_PRTHBR_FAC_RAMPAW = "WO-DHPMRA--L---";
    /** Ramp (below water). */
    final String OCA_HYDGRY_PRTHBR_FAC_RAMPBW = "WO-DHPMRB--L---";
    /** Landing ring. */
    final String OCA_HYDGRY_PRTHBR_FAC_LNDRNG = "WOS-HPM-R-P----";
    /** Ferry crossing. */
    final String OCA_HYDGRY_PRTHBR_FAC_FRYCSG = "WOS-HPM-FC-L---";
    /** Cable ferry crossing. */
    final String OCA_HYDGRY_PRTHBR_FAC_CFCSG = "WOS-HPM-CC-L---";
    /** Dolphin. */
    final String OCA_HYDGRY_PRTHBR_FAC_DOPN = "WOS-HPD---P----";
    /** Breakwater/groin/jetty (above water). */
    final String OCA_HYDGRY_PRTHBR_SHRLNE_BWGJAW = "WO-DHPSPA--L---";
    /** Breakwater/groin/jetty (below water). */
    final String OCA_HYDGRY_PRTHBR_SHRLNE_BWGJBW = "WO-DHPSPB--L---";
    /** Seawall. */
    final String OCA_HYDGRY_PRTHBR_SHRLNE_SW = "WO-DHPSPS--L---";
    /** Beacon. */
    final String OCA_HYDGRY_ATN_BCN = "WOS-HABA--P----";
    /** Buoy default. */
    final String OCA_HYDGRY_ATN_BUOY = "WOS-HABB--P----";
    /** Marker. */
    final String OCA_HYDGRY_ATN_MRK = "WOS-HABM--P----";
    /** Perches/stakes. */
    final String OCA_HYDGRY_ATN_PRH1_PRH2 = "WOS-HABP--P----";
    /** Perches/stakes. */
    final String OCA_HYDGRY_ATN_PRH1_PRH3 = "WO-DHABP----A--";
    /** Light. */
    final String OCA_HYDGRY_ATN_LIT = "WOS-HAL---P----";
    /** Leading line. */
    final String OCA_HYDGRY_ATN_LDGLNE = "WO-DHALLA--L---";
    /** Light vessel/lightship. */
    final String OCA_HYDGRY_ATN_LITVES = "WOS-HALV--P----";
    /** Lighthouse. */
    final String OCA_HYDGRY_ATN_LITHSE = "WOS-HALH--P----";
    /** Rock submergered. */
    final String OCA_HYDGRY_DANHAZ_RCKSBM = "WOS-HHRS--P----";
    /** Rock awashed. */
    final String OCA_HYDGRY_DANHAZ_RCKAWD = "WOS-HHRA--P----";
    /** Underwater danger/hazard. */
    final String OCA_HYDGRY_DANHAZ_UH2DAN = "WO-DHHD-----A--";
    /** Foul ground. */
    final String OCA_HYDGRY_DANHAZ_FLGRD1_FLGRD2 = "WOS-HHDF--P----";
    /** Foul ground. */
    final String OCA_HYDGRY_DANHAZ_FLGRD1_FLGRD3 = "WO-DHHDF----A--";
    /** Kelp/seaweed. */
    final String OCA_HYDGRY_DANHAZ_KLP1_KLP2 = "WO-DHHDK--P----";
    /** Kelp/seaweed. */
    final String OCA_HYDGRY_DANHAZ_KLP1_KLP3 = "WO-DHHDK----A--";
    /** Mine - naval (doubtful). */
    final String OCA_HYDGRY_DANHAZ_MNENAV_DBT = "WOS-HHDMDBP----";
    /** Mine - naval (definite). */
    final String OCA_HYDGRY_DANHAZ_MNENAV_DEFN = "WOS-HHDMDFP----";
    /** Snags/stumps. */
    final String OCA_HYDGRY_DANHAZ_SNAG = "WOS-HHDS--P----";
    /** Wreck (uncovers). */
    final String OCA_HYDGRY_DANHAZ_WRK_UCOV = "WOS-HHDWA-P----";
    /** Wreck (submerged). */
    final String OCA_HYDGRY_DANHAZ_WRK_SBM = "WOS-HHDWB-P----";
    /** Breakers. */
    final String OCA_HYDGRY_DANHAZ_BRKS = "WO-DHHDB---L---";
    /** Reef. */
    final String OCA_HYDGRY_DANHAZ_REEF = "WOS-HHDR---L---";
    /** Eddies/overfalls/tide rips. */
    final String OCA_HYDGRY_DANHAZ_EOTR = "WOS-HHDE--P----";
    /** Discolored water. */
    final String OCA_HYDGRY_DANHAZ_DCDH2O = "WO-DHHDD----A--";
    /** Sand. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_SD = "WOS-BFC-S-P----";
    /** Mud. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_MUD = "WOS-BFC-M-P----";
    /** Clay. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_CLAY = "WOS-BFC-CLP----";
    /** Silt. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_SLT = "WOS-BFC-SIP----";
    /** Stones. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_STNE = "WOS-BFC-STP----";
    /** Gravel. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_GVL = "WOS-BFC-G-P----";
    /** Pebbles. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_PBL = "WOS-BFC-P-P----";
    /** Cobbles. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_COBL = "WOS-BFC-CBP----";
    /** Rock. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_RCK = "WOS-BFC-R-P----";
    /** Coral. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_CRL = "WOS-BFC-COP----";
    /** Shell. */
    final String OCA_HYDGRY_BTMFAT_BTMCHR_SHE = "WOS-BFC-SHP----";
    /** Qualifying terms, fine. */
    final String OCA_HYDGRY_BTMFAT_QLFYTM_FNE = "WOS-BFQ-F-P----";
    /** Qualifying terms, medum. */
    final String OCA_HYDGRY_BTMFAT_QLFYTM_MDM = "WOS-BFQ-M-P----";
    /** Qualifying terms, coarse. */
    final String OCA_HYDGRY_BTMFAT_QLFYTM_CSE = "WOS-BFQ-C-P----";
    /** Water turbulence. */
    final String OCA_HYDGRY_TDECUR_H2OTRB = "WOS-TCCW--P----";
    /** Current flow - ebb. */
    final String OCA_HYDGRY_TDECUR_EBB = "WO-DTCCCFE-L---";
    /** Current flow - flood. */
    final String OCA_HYDGRY_TDECUR_FLOOD = "WO-DTCCCFF-L---";
    /** Tide data point. */
    final String OCA_HYDGRY_TDECUR_TDEDP = "WOS-TCCTD-P----";
    /** Tide gauge. */
    final String OCA_HYDGRY_TDECUR_TDEG = "WOS-TCCTG-P----";
    /** Bioluminescence, vdr level 1-2. */
    final String OCA_OCNGRY_BIOLUM_VDR1_2 = "WO-DOBVA----A--";
    /** Bioluminescence, vdr level 2-3. */
    final String OCA_OCNGRY_BIOLUM_VDR2_3 = "WO-DOBVB----A--";
    /** Bioluminescence, vdr level 3-4. */
    final String OCA_OCNGRY_BIOLUM_VDR3_4 = "WO-DOBVC----A--";
    /** Bioluminescence, vdr level 4-5. */
    final String OCA_OCNGRY_BIOLUM_VDR4_5 = "WO-DOBVD----A--";
    /** Bioluminescence, vdr level 5-6. */
    final String OCA_OCNGRY_BIOLUM_VDR5_6 = "WO-DOBVE----A--";
    /** Bioluminescence, vdr level 6-7. */
    final String OCA_OCNGRY_BIOLUM_VDR6_7 = "WO-DOBVF----A--";
    /** Bioluminescence, vdr level 7-8. */
    final String OCA_OCNGRY_BIOLUM_VDR7_8 = "WO-DOBVG----A--";
    /** Bioluminescence, vdr level 8-9. */
    final String OCA_OCNGRY_BIOLUM_VDR8_9 = "WO-DOBVH----A--";
    /** Bioluminescence, vdr level 9-10. */
    final String OCA_OCNGRY_BIOLUM_VDR9_0 = "WO-DOBVI----A--";
    /** Flat. */
    final String OCA_OCNGRY_BEHSPE_FLT = "WO-DBSF-----A--";
    /** Gentle. */
    final String OCA_OCNGRY_BEHSPE_GTL = "WO-DBSG-----A--";
    /** Moderate. */
    final String OCA_OCNGRY_BEHSPE_MOD = "WO-DBSM-----A--";
    /** Steep. */
    final String OCA_OCNGRY_BEHSPE_STP = "WO-DBST-----A--";
    /** Miw-bottom sediments, solid rock. */
    final String OCA_GPHY_MNEWBD_MIWBS_SLDRCK = "WO-DGMSR----A--";
    /** Miw-bottom sediments, clay. */
    final String OCA_GPHY_MNEWBD_MIWBS_CLAY = "WO-DGMSC----A--";
    /** Very coarse sand. */
    final String OCA_GPHY_MNEWBD_MIWBS_VCSESD = "WO-DGMSSVS--A--";
    /** Miw-bottom sediments, coarse sand. */
    final String OCA_GPHY_MNEWBD_MIWBS_CSESD = "WO-DGMSSC---A--";
    /** Miw-bottom sediments, medium sand. */
    final String OCA_GPHY_MNEWBD_MIWBS_MDMSD = "WO-DGMSSM---A--";
    /** Miw-bottom sediments, fine sand. */
    final String OCA_GPHY_MNEWBD_MIWBS_FNESD = "WO-DGMSSF---A--";
    /** Miw-bottom sediments, very fine sand. */
    final String OCA_GPHY_MNEWBD_MIWBS_VFNESD = "WO-DGMSSVF--A--";
    /** Miw-bottom sediments, very fine silt. */
    final String OCA_GPHY_MNEWBD_MIWBS_VFNSLT = "WO-DGMSIVF--A--";
    /** Miw-bottom sediments, file silt. */
    final String OCA_GPHY_MNEWBD_MIWBS_FNESLT = "WO-DGMSIF---A--";
    /** Miw-bottom sediments, medium silt. */
    final String OCA_GPHY_MNEWBD_MIWBS_MDMSLT = "WO-DGMSIM---A--";
    /** Miw-bottom sediments, coarse silt. */
    final String OCA_GPHY_MNEWBD_MIWBS_CSESLT = "WO-DGMSIC---A--";
    /** Boulders. */
    final String OCA_GPHY_MNEWBD_MIWBS_BLDS = "WO-DGMSB----A--";
    /** Cobbles, oyster shells. */
    final String OCA_GPHY_MNEWBD_MIWBS_COBLOS = "WO-DGMS-CO--A--";
    /** Pebbles, shells. */
    final String OCA_GPHY_MNEWBD_MIWBS_PBLSHE = "WO-DGMS-PH--A--";
    /** Sand and shells. */
    final String OCA_GPHY_MNEWBD_MIWBS_SD_SHE = "WO-DGMS-SH--A--";
    /** Miw-bottom sediments, land. */
    final String OCA_GPHY_MNEWBD_MIWBS_LND = "WO-DGML-----A--";
    /** No data. */
    final String OCA_GPHY_MNEWBD_MIWBS_NODAT = "WO-DGMN-----A--";
    /** Bottom roughness, smooth. */
    final String OCA_GPHY_MNEWBD_BTMRGN_SMH = "WO-DGMRS----A--";
    /** Bottom roughness, moderate. */
    final String OCA_GPHY_MNEWBD_BTMRGN_MOD = "WO-DGMRM----A--";
    /** Bottom roughness, rough. */
    final String OCA_GPHY_MNEWBD_BTMRGN_RGH = "WO-DGMRR----A--";
    /** Low. */
    final String OCA_GPHY_MNEWBD_CTRB_LW = "WO-DGMCL----A--";
    /** Medium. */
    final String OCA_GPHY_MNEWBD_CTRB_MDM = "WO-DGMCM----A--";
    /** High. */
    final String OCA_GPHY_MNEWBD_CTRB_HGH = "WO-DGMCH----A--";
    /** Impact burial, 0%. */
    final String OCA_GPHY_MNEWBD_IMTBUR_0 = "WO-DGMIBA---A--";
    /** Impact burial,  0-10%. */
    final String OCA_GPHY_MNEWBD_IMTBUR_0_10 = "WO-DGMIBB---A--";
    /** Impact burial,  10-20%. */
    final String OCA_GPHY_MNEWBD_IMTBUR_10_20 = "WO-DGMIBC---A--";
    /** Impact burial,  20-75%. */
    final String OCA_GPHY_MNEWBD_IMTBUR_20_75 = "WO-DGMIBD---A--";
    /** Impact burial, &gt;75%. */
    final String OCA_GPHY_MNEWBD_IMTBUR_75 = "WO-DGMIBE---A--";
    /** Miw bottom category, a. */
    final String OCA_GPHY_MNEWBD_MIWBC_A = "WO-DGMBCA---A--";
    /** Miw bottom category, b. */
    final String OCA_GPHY_MNEWBD_MIWBC_B = "WO-DGMBCB---A--";
    /** Miw bottom category, c. */
    final String OCA_GPHY_MNEWBD_MIWBC_C = "WO-DGMBCC---A--";
    /** Miw bottom type, a1. */
    final String OCA_GPHY_MNEWBD_MIWBT_A1 = "WO-DGMBTA---A--";
    /** Miw bottom type, a2. */
    final String OCA_GPHY_MNEWBD_MIWBT_A2 = "WO-DGMBTB---A--";
    /** Miw bottom type, a3. */
    final String OCA_GPHY_MNEWBD_MIWBT_A3 = "WO-DGMBTC---A--";
    /** Miw bottom type, b1. */
    final String OCA_GPHY_MNEWBD_MIWBT_B1 = "WO-DGMBTD---A--";
    /** Miw bottom type, b2. */
    final String OCA_GPHY_MNEWBD_MIWBT_B2 = "WO-DGMBTE---A--";
    /** Miw bottom type, b3. */
    final String OCA_GPHY_MNEWBD_MIWBT_B3 = "WO-DGMBTF---A--";
    /** Miw bottom type, c1. */
    final String OCA_GPHY_MNEWBD_MIWBT_C1 = "WO-DGMBTG---A--";
    /** Miw bottom type, c2. */
    final String OCA_GPHY_MNEWBD_MIWBT_C2 = "WO-DGMBTH---A--";
    /** Miw bottom type, c3. */
    final String OCA_GPHY_MNEWBD_MIWBT_C3 = "WO-DGMBTI---A--";
    /** Maritime limit boundary. */
    final String OCA_LMT_MARTLB = "WO-DL-ML---L---";
    /** Maritime area. */
    final String OCA_LMT_MARTAR = "WO-DL-MA----A--";
    /** Restricted area. */
    final String OCA_LMT_RSDARA = "WO-DL-RA---L---";
    /** Swept area. */
    final String OCA_LMT_SWPARA = "WO-DL-SA----A--";
    /** Training area. */
    final String OCA_LMT_TRGARA = "WO-DL-TA----A--";
    /** Operator-defined. */
    final String OCA_LMT_OD = "WO-DL-O-----A--";
    /** Submarine cable. */
    final String OCA_MMD_SUBCBL = "WO-DMCA----L---";
    /** Submerged crib. */
    final String OCA_MMD_SBMCRB = "WO-DMCC-----A--";
    /** Canal. */
    final String OCA_MMD_CNL = "WO-DMCD----L---";
    /** Ford. */
    final String OCA_MMD_FRD = "WOS-MF----P----";
    /** Lock. */
    final String OCA_MMD_LCK = "WOS-ML----P----";
    /** Oil/gas rig. */
    final String OCA_MMD_OLRG = "WOS-MOA---P----";
    /** Oil/gas rig field. */
    final String OCA_MMD_OLRGFD = "WO-DMOA-----A--";
    /** Pipelines/pipe. */
    final String OCA_MMD_PPELNE = "WO-DMPA----L---";
    /** Pile/piling/post. */
    final String OCA_MMD_PLE = "WOS-MPA---P----";
}
