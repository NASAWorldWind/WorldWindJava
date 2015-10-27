/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

/**
 * SIDC constants for graphics in the "Tactical Graphics" scheme (MIL-STD-2525C Appendix B). The constants in this
 * interface are "masked" SIDCs. All fields except Scheme, Category, and Function ID are filled with hyphens. (The other
 * fields do not identity a type of graphic, they modify the graphic.)
 *
 * @author pabercrombie
 * @version $Id: TacGrpSidc.java 429 2012-03-04 23:35:53Z pabercrombie $
 */
public interface TacGrpSidc
{
    ///////////////////////////////
    // Tasks
    ///////////////////////////////

    /** Block */
    final String TSK_BLK = "G-T-B----------";
    /** Breach */
    final String TSK_BRH = "G-T-H----------";
    /** Bypass */
    final String TSK_BYS = "G-T-Y----------";
    /** Canalize */
    final String TSK_CNZ = "G-T-C----------";
    /** Clear */
    final String TSK_CLR = "G-T-X----------";
    /** Contain */
    final String TSK_CNT = "G-T-J----------";
    /** Counterattack (CATK) */
    final String TSK_CATK = "G-T-K----------";
    /** Counterattack By Fire */
    final String TSK_CATK_CATKF = "G-T-KF---------";
    /** Delay */
    final String TSK_DLY = "G-T-L----------";
    /** Destroy */
    final String TSK_DSTY = "G-T-D----------";
    /** Disrupt */
    final String TSK_DRT = "G-T-T----------";
    /** Fix */
    final String TSK_FIX = "G-T-F----------";
    /** Follow And Assume */
    final String TSK_FLWASS = "G-T-A----------";
    /** Follow And Support */
    final String TSK_FLWASS_FLWSUP = "G-T-AS---------";
    /** Interdict */
    final String TSK_ITDT = "G-T-I----------";
    /** Isolate */
    final String TSK_ISL = "G-T-E----------";
    /** Neutralize */
    final String TSK_NEUT = "G-T-N----------";
    /** Occupy */
    final String TSK_OCC = "G-T-O----------";
    /** Penetrate */
    final String TSK_PNE = "G-T-P----------";
    /** Relief In Place (RIP) */
    final String TSK_RIP = "G-T-R----------";
    /** Retain */
    final String TSK_RTN = "G-T-Q----------";
    /** Retirement */
    final String TSK_RTM = "G-T-M----------";
    /** Secure */
    final String TSK_SCE = "G-T-S----------";
    /** Screen */
    final String TSK_SEC_SCN = "G-T-US---------";
    /** Guard */
    final String TSK_SEC_GUD = "G-T-UG---------";
    /** Cover */
    final String TSK_SEC_COV = "G-T-UC---------";
    /** Seize */
    final String TSK_SZE = "G-T-Z----------";
    /** Withdraw */
    final String TSK_WDR = "G-T-W----------";
    /** Withdraw Under Pressure */
    final String TSK_WDR_WDRUP = "G-T-WP---------";

    ///////////////////////////////////////////
    // Command, Control, and General Manuever
    ///////////////////////////////////////////

    /** Datum */
    final String C2GM_GNL_PNT_USW_UH2_DTM = "G-G-GPUUD------";
    /** Brief Contact */
    final String C2GM_GNL_PNT_USW_UH2_BCON = "G-G-GPUUB------";
    /** Lost Contact */
    final String C2GM_GNL_PNT_USW_UH2_LCON = "G-G-GPUUL------";
    /** Sinker */
    final String C2GM_GNL_PNT_USW_UH2_SNK = "G-G-GPUUS------";
    /** Sonobuoy */
    final String C2GM_GNL_PNT_USW_SNBY = "G-G-GPUY-------";
    /** Pattern Center */
    final String C2GM_GNL_PNT_USW_SNBY_PTNCTR = "G-G-GPUYP------";
    /** Directional Frequency Analyzing And Recording (DIFAR) */
    final String C2GM_GNL_PNT_USW_SNBY_DIFAR = "G-G-GPUYD------";
    /** Low Frequency Analyzing And Recording (LOFAR) */
    final String C2GM_GNL_PNT_USW_SNBY_LOFAR = "G-G-GPUYL------";
    /** Command Active Sonobuoy System (CASS) */
    final String C2GM_GNL_PNT_USW_SNBY_CASS = "G-G-GPUYC------";
    /** Directional Command Active Sonobuoy System (DICASS) */
    final String C2GM_GNL_PNT_USW_SNBY_DICASS = "G-G-GPUYS------";
    /** Bathythermograph Transmitting (BT) */
    final String C2GM_GNL_PNT_USW_SNBY_BT = "G-G-GPUYB------";
    /** ANM */
    final String C2GM_GNL_PNT_USW_SNBY_ANM = "G-G-GPUYA------";
    /** Vertical Line Array Difar (VLAD) */
    final String C2GM_GNL_PNT_USW_SNBY_VLAD = "G-G-GPUYV------";
    /** ATAC */
    final String C2GM_GNL_PNT_USW_SNBY_ATAC = "G-G-GPUYT------";
    /** Range Only (RO) */
    final String C2GM_GNL_PNT_USW_SNBY_RO = "G-G-GPUYR------";
    /** Kingpin */
    final String C2GM_GNL_PNT_USW_SNBY_KGP = "G-G-GPUYK------";
    /** Sonobuoy-Expired */
    final String C2GM_GNL_PNT_USW_SNBY_EXP = "G-G-GPUYX------";
    /** Search */
    final String C2GM_GNL_PNT_USW_SRH = "G-G-GPUS-------";
    /** Search Area */
    final String C2GM_GNL_PNT_USW_SRH_ARA = "G-G-GPUSA------";
    /** DIP Position */
    final String C2GM_GNL_PNT_USW_SRH_DIPPSN = "G-G-GPUSD------";
    /** Search Center */
    final String C2GM_GNL_PNT_USW_SRH_CTR = "G-G-GPUSC------";
    /** Reference Point */
    final String C2GM_GNL_PNT_REFPNT = "G-G-GPR--------";
    /** Navigational Reference Point */
    final String C2GM_GNL_PNT_REFPNT_NAVREF = "G-G-GPRN-------";
    /** Special Point */
    final String C2GM_GNL_PNT_REFPNT_SPLPNT = "G-G-GPRS-------";
    /** DLRP */
    final String C2GM_GNL_PNT_REFPNT_DLRP = "G-G-GPRD-------";
    /** Point Of Intended Movement (PIM) */
    final String C2GM_GNL_PNT_REFPNT_PIM = "G-G-GPRP-------";
    /** Marshall Point */
    final String C2GM_GNL_PNT_REFPNT_MRSH = "G-G-GPRM-------";
    /** Waypoint */
    final String C2GM_GNL_PNT_REFPNT_WAP = "G-G-GPRW-------";
    /** Corridor Tab */
    final String C2GM_GNL_PNT_REFPNT_CRDRTB = "G-G-GPRC-------";
    /** Point Of Interest */
    final String C2GM_GNL_PNT_REFPNT_PNTINR = "G-G-GPRI-------";
    /** Aim Point */
    final String C2GM_GNL_PNT_WPN_AIMPNT = "G-G-GPWA-------";
    /** Drop Point */
    final String C2GM_GNL_PNT_WPN_DRPPNT = "G-G-GPWD-------";
    /** Entry Point */
    final String C2GM_GNL_PNT_WPN_ENTPNT = "G-G-GPWE-------";
    /** Ground Zero */
    final String C2GM_GNL_PNT_WPN_GRDZRO = "G-G-GPWG-------";
    /** MSL Detect Point */
    final String C2GM_GNL_PNT_WPN_MSLPNT = "G-G-GPWM-------";
    /** Impact Point */
    final String C2GM_GNL_PNT_WPN_IMTPNT = "G-G-GPWI-------";
    /** Predicted Impact Point */
    final String C2GM_GNL_PNT_WPN_PIPNT = "G-G-GPWP-------";
    /** Formation */
    final String C2GM_GNL_PNT_FRMN = "G-G-GPF--------";
    /** Harbor (General) */
    final String C2GM_GNL_PNT_HBR = "G-G-GPH--------";
    /** Point Q */
    final String C2GM_GNL_PNT_HBR_PNTQ = "G-G-GPHQ-------";
    /** Point A */
    final String C2GM_GNL_PNT_HBR_PNTA = "G-G-GPHA-------";
    /** Point Y */
    final String C2GM_GNL_PNT_HBR_PNTY = "G-G-GPHY-------";
    /** Point X */
    final String C2GM_GNL_PNT_HBR_PNTX = "G-G-GPHX-------";
    /** Route */
    final String C2GM_GNL_PNT_RTE = "G-G-GPO--------";
    /** Rendezvous */
    final String C2GM_GNL_PNT_RTE_RDV = "G-G-GPOZ-------";
    /** Diversions */
    final String C2GM_GNL_PNT_RTE_DVSN = "G-G-GPOD-------";
    /** Waypoint */
    final String C2GM_GNL_PNT_RTE_WAP = "G-G-GPOW-------";
    /** PIM */
    final String C2GM_GNL_PNT_RTE_PIM = "G-G-GPOP-------";
    /** Point R */
    final String C2GM_GNL_PNT_RTE_PNTR = "G-G-GPOR-------";
    /** Air Control */
    final String C2GM_GNL_PNT_ACTL = "G-G-GPA--------";
    /** Combat Air Patrol (CAP) */
    final String C2GM_GNL_PNT_ACTL_CAP = "G-G-GPAP-------";
    /** Airborne Early Warning (AEW) */
    final String C2GM_GNL_PNT_ACTL_ABNEW = "G-G-GPAW-------";
    /** Tanking */
    final String C2GM_GNL_PNT_ACTL_TAK = "G-G-GPAK-------";
    /** Antisubmarine Warfare, Fixed Wing */
    final String C2GM_GNL_PNT_ACTL_ASBWF = "G-G-GPAA-------";
    /** Antisubmarine Warfare, Rotary Wing */
    final String C2GM_GNL_PNT_ACTL_ASBWR = "G-G-GPAH-------";
    /** Sucap - Fixed Wing */
    final String C2GM_GNL_PNT_ACTL_SUWF = "G-G-GPAB-------";
    /** Sucap - Rotary Wing */
    final String C2GM_GNL_PNT_ACTL_SUWR = "G-G-GPAC-------";
    /** IW - Fixed Wing */
    final String C2GM_GNL_PNT_ACTL_MIWF = "G-G-GPAD-------";
    /** MIW - Rotary Wing */
    final String C2GM_GNL_PNT_ACTL_MIWR = "G-G-GPAE-------";
    /** Strike Ip */
    final String C2GM_GNL_PNT_ACTL_SKEIP = "G-G-GPAS-------";
    /** Tacan */
    final String C2GM_GNL_PNT_ACTL_TCN = "G-G-GPAT-------";
    /** Tomcat */
    final String C2GM_GNL_PNT_ACTL_TMC = "G-G-GPAO-------";
    /** Rescue */
    final String C2GM_GNL_PNT_ACTL_RSC = "G-G-GPAR-------";
    /** Replenish */
    final String C2GM_GNL_PNT_ACTL_RPH = "G-G-GPAL-------";
    /** Unmanned Aerial System (UAS/UA) */
    final String C2GM_GNL_PNT_ACTL_UA = "G-G-GPAF-------";
    /** VTUA */
    final String C2GM_GNL_PNT_ACTL_VTUA = "G-G-GPAG-------";
    /** Orbit */
    final String C2GM_GNL_PNT_ACTL_ORB = "G-G-GPAI-------";
    /** Orbit - Figure Eight */
    final String C2GM_GNL_PNT_ACTL_ORBF8 = "G-G-GPAJ-------";
    /** Orbit - Race Track */
    final String C2GM_GNL_PNT_ACTL_ORBRT = "G-G-GPAM-------";
    /** Orbit - Random, Closed */
    final String C2GM_GNL_PNT_ACTL_ORBRD = "G-G-GPAN-------";
    /** Action Points (General) */
    final String C2GM_GNL_PNT_ACTPNT = "G-G-GPP--------";
    /** Check Point */
    final String C2GM_GNL_PNT_ACTPNT_CHKPNT = "G-G-GPPK-------";
    /** Contact Point */
    final String C2GM_GNL_PNT_ACTPNT_CONPNT = "G-G-GPPC-------";
    /** Coordination Point */
    final String C2GM_GNL_PNT_ACTPNT_CRDPNT = "G-G-GPPO-------";
    /** Decision Point */
    final String C2GM_GNL_PNT_ACTPNT_DCNPNT = "G-G-GPPD-------";
    /** Linkup Point */
    final String C2GM_GNL_PNT_ACTPNT_LNKUPT = "G-G-GPPL-------";
    /** Passage Point */
    final String C2GM_GNL_PNT_ACTPNT_PSSPNT = "G-G-GPPP-------";
    /** Rally Point */
    final String C2GM_GNL_PNT_ACTPNT_RAYPNT = "G-G-GPPR-------";
    /** Release Point */
    final String C2GM_GNL_PNT_ACTPNT_RELPNT = "G-G-GPPE-------";
    /** Start Point */
    final String C2GM_GNL_PNT_ACTPNT_STRPNT = "G-G-GPPS-------";
    /** Amnesty Point */
    final String C2GM_GNL_PNT_ACTPNT_AMNPNT = "G-G-GPPA-------";
    /** Waypoint */
    final String C2GM_GNL_PNT_ACTPNT_WAP = "G-G-GPPW-------";
    /** EA Surface Control Station */
    final String C2GM_GNL_PNT_SCTL = "G-G-GPC--------";
    /** Unmanned Surface Vehicle (USV) Control Station */
    final String C2GM_GNL_PNT_SCTL_USV = "G-G-GPCU-------";
    /** Remote Multimission Vehicle (RMV) Usv Control Station */
    final String C2GM_GNL_PNT_SCTL_USV_RMV = "G-G-GPCUR------";
    /** USV - Antisubmarine Warfare Control Station */
    final String C2GM_GNL_PNT_SCTL_USV_ASW = "G-G-GPCUA------";
    /** USV - Surface Warfare Control Station */
    final String C2GM_GNL_PNT_SCTL_USV_SUW = "G-G-GPCUS------";
    /** USV - Mine Warfare Control Station */
    final String C2GM_GNL_PNT_SCTL_USV_MIW = "G-G-GPCUM------";
    /** ASW Control Station */
    final String C2GM_GNL_PNT_SCTL_ASW = "G-G-GPCA-------";
    /** SUW Control Station */
    final String C2GM_GNL_PNT_SCTL_SUW = "G-G-GPCS-------";
    /** MIW Control Station */
    final String C2GM_GNL_PNT_SCTL_MIW = "G-G-GPCM-------";
    /** Picket Control Station */
    final String C2GM_GNL_PNT_SCTL_PKT = "G-G-GPCP-------";
    /** Rendezvous Control Point */
    final String C2GM_GNL_PNT_SCTL_RDV = "G-G-GPCR-------";
    /** Rescue Control Point */
    final String C2GM_GNL_PNT_SCTL_RSC = "G-G-GPCC-------";
    /** Replenishment Control Point */
    final String C2GM_GNL_PNT_SCTL_REP = "G-G-GPCE-------";
    /** Noncombatant Control Station */
    final String C2GM_GNL_PNT_SCTL_NCBTT = "G-G-GPCN-------";
    /** Subsurface Control Station */
    final String C2GM_GNL_PNT_UCTL = "G-G-GPB--------";
    /** Unmanned Underwater Vehicle (UUV) Control Station */
    final String C2GM_GNL_PNT_UCTL_UUV = "G-G-GPBU-------";
    /** UUV - Antisubmarine Warfare Control Station */
    final String C2GM_GNL_PNT_UCTL_UUV_ASW = "G-G-GPBUA------";
    /** UUV - Surface Warfare Control Station */
    final String C2GM_GNL_PNT_UCTL_UUV_SUW = "G-G-GPBUS------";
    /** UUV - Mine Warfare Control Station */
    final String C2GM_GNL_PNT_UCTL_UUV_MIW = "G-G-GPBUM------";
    /** Submarine Control Station */
    final String C2GM_GNL_PNT_UCTL_SBSTN = "G-G-GPBS-------";
    /** ASW Submarine Control Station */
    final String C2GM_GNL_PNT_UCTL_SBSTN_ASW = "G-G-GPBSA------";
    /** Boundaries */
    final String C2GM_GNL_LNE_BNDS = "G-G-GLB--------";
    /** Forward Line of Own Troops */
    final String C2GM_GNL_LNE_FLOT = "G-G-GLF--------";
    /** Line Of Contact */
    final String C2GM_GNL_LNE_LOC = "G-G-GLC--------";
    /** Phase Line */
    final String C2GM_GNL_LNE_PHELNE = "G-G-GLP--------";
    /** Light Line */
    final String C2GM_GNL_LNE_LITLNE = "G-G-GLL--------";
    /** Areas */
    final String C2GM_GNL_ARS = "G-G-GA---------";
    /** General Area */
    final String C2GM_GNL_ARS_GENARA = "G-G-GAG--------";
    /** Assembly Area */
    final String C2GM_GNL_ARS_ABYARA = "G-G-GAA--------";
    /** Engagement Area */
    final String C2GM_GNL_ARS_EMTARA = "G-G-GAE--------";
    /** Fortified Area */
    final String C2GM_GNL_ARS_FTFDAR = "G-G-GAF--------";
    /** Drop Zone */
    final String C2GM_GNL_ARS_DRPZ = "G-G-GAD--------";
    /** Extraction Zone (EZ) */
    final String C2GM_GNL_ARS_EZ = "G-G-GAX--------";
    /** Landing Zone (LZ) */
    final String C2GM_GNL_ARS_LZ = "G-G-GAL--------";
    /** Pickup Zone (PZ) */
    final String C2GM_GNL_ARS_PZ = "G-G-GAP--------";
    /** Search Area/Reconnaissance Area */
    final String C2GM_GNL_ARS_SRHARA = "G-G-GAS--------";
    /** Limited Access Area */
    final String C2GM_GNL_ARS_LAARA = "G-G-GAY--------";
    /** Airfield Zone */
    final String C2GM_GNL_ARS_AIRFZ = "G-G-GAZ--------";
    /** Air Control Point (ACP) */
    final String C2GM_AVN_PNT_ACP = "G-G-APP--------";
    /** Communications Checkpoint (CCP) */
    final String C2GM_AVN_PNT_COMMCP = "G-G-APC--------";
    /** Pull-Up Point (PUP) */
    final String C2GM_AVN_PNT_PUP = "G-G-APU--------";
    /** Downed Aircrew Pickup Point */
    final String C2GM_AVN_PNT_DAPP = "G-G-APD--------";
    /** Air Corridor */
    final String C2GM_AVN_LNE_ACDR = "G-G-ALC--------";
    /** Minimum Risk Route (MRR) */
    final String C2GM_AVN_LNE_MRR = "G-G-ALM--------";
    /** Standard-Use Army Aircraft Flight Route (SAAFR) */
    final String C2GM_AVN_LNE_SAAFR = "G-G-ALS--------";
    /** Unmanned Aircraft (UA) Route */
    final String C2GM_AVN_LNE_UAR = "G-G-ALU--------";
    /** Low Level Transit Route (LLTR) */
    final String C2GM_AVN_LNE_LLTR = "G-G-ALL--------";
    /** Restricted Operations Zone (ROZ) */
    final String C2GM_AVN_ARS_ROZ = "G-G-AAR--------";
    /** Short-Range Air Defense Engagement Zone (SHORADEZ) */
    final String C2GM_AVN_ARS_SHRDEZ = "G-G-AAF--------";
    /** High Density Airspace Control Zone (HIDACZ) */
    final String C2GM_AVN_ARS_HIDACZ = "G-G-AAH--------";
    /** Missile Engagement Zone (MEZ) */
    final String C2GM_AVN_ARS_MEZ = "G-G-AAM--------";
    /** Low Altitude Mez */
    final String C2GM_AVN_ARS_MEZ_LAMEZ = "G-G-AAML-------";
    /** High Altitude Mez */
    final String C2GM_AVN_ARS_MEZ_HAMEZ = "G-G-AAMH-------";
    /** Weapons Free Zone */
    final String C2GM_AVN_ARS_WFZ = "G-G-AAW--------";
    /** Dummy (Deception/Decoy) */
    final String C2GM_DCPN_DMY = "G-G-PD---------";
    /** Axis  Of Advance For Feint */
    final String C2GM_DCPN_AAFF = "G-G-PA---------";
    /** Direction Of Attack For Feint */
    final String C2GM_DCPN_DAFF = "G-G-PF---------";
    /** Decoy Mined Area */
    final String C2GM_DCPN_DMA = "G-G-PM---------";
    /** Decoy Mined Area,  Fenced */
    final String C2GM_DCPN_DMAF = "G-G-PY---------";
    /** Dummy Minefield (Static) */
    final String C2GM_DCPN_DMYMS = "G-G-PN---------";
    /** Dummy Minefield (Dynamic) */
    final String C2GM_DCPN_DMYMD = "G-G-PC---------";
    /** Target Reference Point (TRP) */
    final String C2GM_DEF_PNT_TGTREF = "G-G-DPT--------";
    /** Observation Post/Outpost */
    final String C2GM_DEF_PNT_OBSPST = "G-G-DPO--------";
    /** Combat  Outpost */
    final String C2GM_DEF_PNT_OBSPST_CBTPST = "G-G-DPOC-------";
    /** Observation Post Occupied By Dismounted Scouts Or Reconnaissance */
    final String C2GM_DEF_PNT_OBSPST_RECON = "G-G-DPOR-------";
    /** Forward Observer Position */
    final String C2GM_DEF_PNT_OBSPST_FWDOP = "G-G-DPOF-------";
    /** Sensor Outpost/Listening Post (OP/Lp) */
    final String C2GM_DEF_PNT_OBSPST_SOP = "G-G-DPOS-------";
    /** Cbrn Observation Post (Dismounted) */
    final String C2GM_DEF_PNT_OBSPST_CBRNOP = "G-G-DPON-------";
    /** Forward Edge Of Battle Area (FEBA) */
    final String C2GM_DEF_LNE_FEBA = "G-G-DLF--------";
    /** Principal Direction Of Fire (PDF) */
    final String C2GM_DEF_LNE_PDF = "G-G-DLP--------";
    /** Battle Position */
    final String C2GM_DEF_ARS_BTLPSN = "G-G-DAB--------";
    /** Prepared But Not Occupied */
    final String C2GM_DEF_ARS_BTLPSN_PBNO = "G-G-DABP-------";
    /** Engagement Area */
    final String C2GM_DEF_ARS_EMTARA = "G-G-DAE--------";
    /** Point Of Departure */
    final String C2GM_OFF_PNT_PNTD = "G-G-OPP--------";
    /** Axis Of Advance */
    final String C2GM_OFF_LNE_AXSADV = "G-G-OLA--------";
    /** Aviation */
    final String C2GM_OFF_LNE_AXSADV_AVN = "G-G-OLAV-------";
    /** Airborne */
    final String C2GM_OFF_LNE_AXSADV_ABN = "G-G-OLAA-------";
    /** Attack, Rotary Wing */
    final String C2GM_OFF_LNE_AXSADV_ATK = "G-G-OLAR-------";
    /** Ground */
    final String C2GM_OFF_LNE_AXSADV_GRD = "G-G-OLAG-------";
    /** Main Attack */
    final String C2GM_OFF_LNE_AXSADV_GRD_MANATK = "G-G-OLAGM------";
    /** Supporting Attack */
    final String C2GM_OFF_LNE_AXSADV_GRD_SUPATK = "G-G-OLAGS------";
    /** Aviation */
    final String C2GM_OFF_LNE_DIRATK_AVN = "G-G-OLKA-------";
    /** Main Ground Attack */
    final String C2GM_OFF_LNE_DIRATK_GRD_MANATK = "G-G-OLKGM------";
    /** Supporting Ground Attack */
    final String C2GM_OFF_LNE_DIRATK_GRD_SUPATK = "G-G-OLKGS------";
    /** Final Coordination Line */
    final String C2GM_OFF_LNE_FCL = "G-G-OLF--------";
    /** Infiltration Lane */
    final String C2GM_OFF_LNE_INFNLE = "G-G-OLI--------";
    /** Limit Of Advance */
    final String C2GM_OFF_LNE_LMTADV = "G-G-OLL--------";
    /** Line Of Departure */
    final String C2GM_OFF_LNE_LD = "G-G-OLT--------";
    /** Line Of Departure/Line Of Contact (LD/LC) */
    final String C2GM_OFF_LNE_LDLC = "G-G-OLC--------";
    /** Probable Line Of Deployment (PLD) */
    final String C2GM_OFF_LNE_PLD = "G-G-OLP--------";
    /** Assault Position */
    final String C2GM_OFF_ARS_ASTPSN = "G-G-OAA--------";
    /** Attack Position */
    final String C2GM_OFF_ARS_ATKPSN = "G-G-OAK--------";
    /** Attack By Fire Position */
    final String C2GM_OFF_ARS_AFP = "G-G-OAF--------";
    /** Support By Fire Position */
    final String C2GM_OFF_ARS_SFP = "G-G-OAS--------";
    /** Objective */
    final String C2GM_OFF_ARS_OBJ = "G-G-OAO--------";
    /** Penetration Box */
    final String C2GM_OFF_ARS_PBX = "G-G-OAP--------";
    /** Ambush */
    final String C2GM_SPL_LNE_AMB = "G-G-SLA--------";
    /** Holding Line */
    final String C2GM_SPL_LNE_HGL = "G-G-SLH--------";
    /** Release Line */
    final String C2GM_SPL_LNE_REL = "G-G-SLR--------";
    /** Bridgehead */
    final String C2GM_SPL_LNE_BRGH = "G-G-SLB--------";
    /** Area */
    final String C2GM_SPL_ARA = "G-G-SA---------";
    /** Area Of Operations (AO) */
    final String C2GM_SPL_ARA_AOO = "G-G-SAO--------";
    /** Airhead */
    final String C2GM_SPL_ARA_AHD = "G-G-SAA--------";
    /** Encirclement */
    final String C2GM_SPL_ARA_ENCMT = "G-G-SAE--------";
    /** Named */
    final String C2GM_SPL_ARA_NAI = "G-G-SAN--------";
    /** Targeted Area Of Interest (TAI) */
    final String C2GM_SPL_ARA_TAI = "G-G-SAT--------";

    ///////////////////////////////////////////
    // Mobility/Survivability
    ///////////////////////////////////////////

    /** Belt */
    final String MOBSU_OBST_GNL_BLT = "G-M-OGB--------";
    /** Line */
    final String MOBSU_OBST_GNL_LNE = "G-M-OGL--------";
    /** Zone */
    final String MOBSU_OBST_GNL_Z = "G-M-OGZ--------";
    /** Obstacle Free Area */
    final String MOBSU_OBST_GNL_OFA = "G-M-OGF--------";
    /** Obstacle Restricted Area */
    final String MOBSU_OBST_GNL_ORA = "G-M-OGR--------";
    /** Abatis */
    final String MOBSU_OBST_ABS = "G-M-OS---------";
    /** Antitank Ditch, Under Construction */
    final String MOBSU_OBST_ATO_ATD_ATDUC = "G-M-OADU-------";
    /** Antitank Ditch, Complete */
    final String MOBSU_OBST_ATO_ATD_ATDC = "G-M-OADC-------";
    /** Antitank Ditch Reinforced With Antitank Mines */
    final String MOBSU_OBST_ATO_ATDATM = "G-M-OAR--------";
    /** Fixed And Prefabricated */
    final String MOBSU_OBST_ATO_TDTSM_FIXPFD = "G-M-OAOF-------";
    /** Moveable */
    final String MOBSU_OBST_ATO_TDTSM_MVB = "G-M-OAOM-------";
    /** Moveable And Prefabricated */
    final String MOBSU_OBST_ATO_TDTSM_MVBPFD = "G-M-OAOP-------";
    /** Antitank Wall */
    final String MOBSU_OBST_ATO_ATW = "G-M-OAW--------";
    /** Booby Trap */
    final String MOBSU_OBST_BBY = "G-M-OB---------";
    /** Unspecified Mine */
    final String MOBSU_OBST_MNE_USPMNE = "G-M-OMU--------";
    /** Antitank Mine (AT) */
    final String MOBSU_OBST_MNE_ATMNE = "G-M-OMT--------";
    /** Antitank Mine With Antihandling Device */
    final String MOBSU_OBST_MNE_ATMAHD = "G-M-OMD--------";
    /** Antitank Mine (Directional) */
    final String MOBSU_OBST_MNE_ATMDIR = "G-M-OME--------";
    /** Antipersonnel (AP) Mines */
    final String MOBSU_OBST_MNE_APMNE = "G-M-OMP--------";
    /** Wide Area Mines */
    final String MOBSU_OBST_MNE_WAMNE = "G-M-OMW--------";
    /** Mine Cluster */
    final String MOBSU_OBST_MNE_MCLST = "G-M-OMC--------";
    /** Static Depiction */
    final String MOBSU_OBST_MNEFLD_STC = "G-M-OFS--------";
    /** Dynamic Depiction */
    final String MOBSU_OBST_MNEFLD_DYN = "G-M-OFD--------";
    /** Gap */
    final String MOBSU_OBST_MNEFLD_GAP = "G-M-OFG--------";
    /** Mined Area */
    final String MOBSU_OBST_MNEFLD_MNDARA = "G-M-OFA--------";
    /** Block */
    final String MOBSU_OBST_OBSEFT_BLK = "G-M-OEB--------";
    /** Fix */
    final String MOBSU_OBST_OBSEFT_FIX = "G-M-OEF--------";
    /** Turn */
    final String MOBSU_OBST_OBSEFT_TUR = "G-M-OET--------";
    /** Disrupt */
    final String MOBSU_OBST_OBSEFT_DRT = "G-M-OED--------";
    /** Unexploded Ordnance Area (UXO) */
    final String MOBSU_OBST_UXO = "G-M-OU---------";
    /** Planned */
    final String MOBSU_OBST_RCBB_PLND = "G-M-ORP--------";
    /** Explosives, State Of Readiness 1 (Safe) */
    final String MOBSU_OBST_RCBB_SAFE = "G-M-ORS--------";
    /** Explosives, State Of Readiness 2 (Armed-But Passable) */
    final String MOBSU_OBST_RCBB_ABP = "G-M-ORA--------";
    /** Roadblock Complete (Executed) */
    final String MOBSU_OBST_RCBB_EXCD = "G-M-ORC--------";
    /** Trip Wire */
    final String MOBSU_OBST_TRIPWR = "G-M-OT---------";
    /** Wire Obstacle */
    final String MOBSU_OBST_WREOBS = "G-M-OW---------";
    /** Unspecified */
    final String MOBSU_OBST_WREOBS_USP = "G-M-OWU--------";
    /** Single Fence */
    final String MOBSU_OBST_WREOBS_SNGFNC = "G-M-OWS--------";
    /** Double Fence */
    final String MOBSU_OBST_WREOBS_DBLFNC = "G-M-OWD--------";
    /** Double Apron Fence */
    final String MOBSU_OBST_WREOBS_DAFNC = "G-M-OWA--------";
    /** Low Wire Fence */
    final String MOBSU_OBST_WREOBS_LWFNC = "G-M-OWL--------";
    /** High Wire Fence */
    final String MOBSU_OBST_WREOBS_HWFNC = "G-M-OWH--------";
    /** Single Concertina */
    final String MOBSU_OBST_WREOBS_CCTA_SNG = "G-M-OWCS-------";
    /** Double Strand Concertina */
    final String MOBSU_OBST_WREOBS_CCTA_DBLSTD = "G-M-OWCD-------";
    /** Triple Strand Concertina */
    final String MOBSU_OBST_WREOBS_CCTA_TRISTD = "G-M-OWCT-------";
    /** Low Tower */
    final String MOBSU_OBST_AVN_TWR_LOW = "G-M-OHTL-------";
    /** High Tower */
    final String MOBSU_OBST_AVN_TWR_HIGH = "G-M-OHTH-------";
    /** Overhead Wire/Power Line */
    final String MOBSU_OBST_AVN_OHWIRE = "G-M-OHO--------";
    /** Bypass Easy */
    final String MOBSU_OBSTBP_DFTY_ESY = "G-M-BDE--------";
    /** Bypass Difficult */
    final String MOBSU_OBSTBP_DFTY_DFT = "G-M-BDD--------";
    /** Bypass Impossible */
    final String MOBSU_OBSTBP_DFTY_IMP = "G-M-BDI--------";
    /** Crossing Site/Water Crossing */
    final String MOBSU_OBSTBP_CSGSTE = "G-M-BC---------";
    /** Assault Crossing Area */
    final String MOBSU_OBSTBP_CSGSTE_ASTCA = "G-M-BCA--------";
    /** Bridge or Gap */
    final String MOBSU_OBSTBP_CSGSTE_BRG = "G-M-BCB--------";
    /** Ferry */
    final String MOBSU_OBSTBP_CSGSTE_FRY = "G-M-BCF--------";
    /** Ford Easy */
    final String MOBSU_OBSTBP_CSGSTE_FRDESY = "G-M-BCE--------";
    /** Ford Difficult */
    final String MOBSU_OBSTBP_CSGSTE_FRDDFT = "G-M-BCD--------";
    /** Lane */
    final String MOBSU_OBSTBP_CSGSTE_LANE = "G-M-BCL--------";
    /** Raft Site */
    final String MOBSU_OBSTBP_CSGSTE_RFT = "G-M-BCR--------";
    /** Engineer Regulating Point */
    final String MOBSU_OBSTBP_CSGSTE_ERP = "G-M-BCP--------";
    /** Earthwork, Small Trench Or Fortification */
    final String MOBSU_SU_ESTOF = "G-M-SE---------";
    /** Fort */
    final String MOBSU_SU_FRT = "G-M-SF---------";
    /** Fortified Line */
    final String MOBSU_SU_FTFDLN = "G-M-SL---------";
    /** Foxhole, Emplacement Or Weapon Site */
    final String MOBSU_SU_FEWS = "G-M-SW---------";
    /** Strong Point */
    final String MOBSU_SU_STRGPT = "G-M-SP---------";
    /** Surface Shelter */
    final String MOBSU_SU_SUFSHL = "G-M-SS---------";
    /** Underground Shelter */
    final String MOBSU_SU_UGDSHL = "G-M-SU---------";
    /** Minimum Safe Distance Zones */
    final String MOBSU_CBRN_MSDZ = "G-M-NM---------";
    /** Nuclear Detonations Ground Zero */
    final String MOBSU_CBRN_NDGZ = "G-M-NZ---------";
    /** Fallout Producing */
    final String MOBSU_CBRN_FAOTP = "G-M-NF---------";
    /** Radioactive Area */
    final String MOBSU_CBRN_RADA = "G-M-NR---------";
    /** Biologically Contaminated Area */
    final String MOBSU_CBRN_BIOCA = "G-M-NB---------";
    /** Chemically Contaminated Area */
    final String MOBSU_CBRN_CMLCA = "G-M-NC---------";
    /** Biological Release Event */
    final String MOBSU_CBRN_REEVNT_BIO = "G-M-NEB--------";
    /** Chemical Release Event */
    final String MOBSU_CBRN_REEVNT_CML = "G-M-NEC--------";
    /** Decon Site/Point (Unspecified) */
    final String MOBSU_CBRN_DECONP_USP = "G-M-NDP--------";
    /** Alternate Decon Site/Point (Unspecified) */
    final String MOBSU_CBRN_DECONP_ALTUSP = "G-M-NDA--------";
    /** Decon Site/Point (Troops) */
    final String MOBSU_CBRN_DECONP_TRP = "G-M-NDT--------";
    /** Decon , */
    final String MOBSU_CBRN_DECONP_EQT = "G-M-NDE--------";
    /** Decon Site/Point (Equipment And Troops) */
    final String MOBSU_CBRN_DECONP_EQTTRP = "G-M-NDB--------";
    /** Decon Site/Point (Operational Decontamination) */
    final String MOBSU_CBRN_DECONP_OPDECN = "G-M-NDO--------";
    /** Decon Site/Point (Thorough Decontamination) */
    final String MOBSU_CBRN_DECONP_TRGH = "G-M-NDD--------";
    /** Dose Rate Contour Lines */
    final String MOBSU_CBRN_DRCL = "G-M-NL---------";

    /////////////////////////////////////////////////
    // Fire Support
    /////////////////////////////////////////////////

    /** Point/Single Target */
    final String FSUPP_PNT_TGT_PTGT = "G-F-PTS--------";
    /** Nuclear Target */
    final String FSUPP_PNT_TGT_NUCTGT = "G-F-PTN--------";
    /** Fire Support Station */
    final String FSUPP_PNT_C2PNT_FSS = "G-F-PCF--------";
    /** Survey Control Point */
    final String FSUPP_PNT_C2PNT_SCP = "G-F-PCS--------";
    /** Firing Point */
    final String FSUPP_PNT_C2PNT_FP = "G-F-PCB--------";
    /** Reload Point */
    final String FSUPP_PNT_C2PNT_RP = "G-F-PCR--------";
    /** Hide Point */
    final String FSUPP_PNT_C2PNT_HP = "G-F-PCH--------";
    /** Launch Point */
    final String FSUPP_PNT_C2PNT_LP = "G-F-PCL--------";
    /** Linear Target */
    final String FSUPP_LNE_LNRTGT = "G-F-LT---------";
    /** Linear Smoke Target */
    final String FSUPP_LNE_LNRTGT_LSTGT = "G-F-LTS--------";
    /** Final Protective Fire (FPF) */
    final String FSUPP_LNE_LNRTGT_FPF = "G-F-LTF--------";
    /** Fire Support Coordination Line (FSCL) */
    final String FSUPP_LNE_C2LNE_FSCL = "G-F-LCF--------";
    /** Coordinated Fire Line (CFL) */
    final String FSUPP_LNE_C2LNE_CFL = "G-F-LCC--------";
    /** No-Fire Line (NFL) */
    final String FSUPP_LNE_C2LNE_NFL = "G-F-LCN--------";
    /** Restrictive */
    final String FSUPP_LNE_C2LNE_RFL = "G-F-LCR--------";
    /** Munition Flight Path (MFP) */
    final String FSUPP_LNE_C2LNE_MFP = "G-F-LCM--------";
    /** Area Target */
    final String FSUPP_ARS_ARATGT = "G-F-AT---------";
    /** Rectangular Target */
    final String FSUPP_ARS_ARATGT_RTGTGT = "G-F-ATR--------";
    /** Circular Target */
    final String FSUPP_ARS_ARATGT_CIRTGT = "G-F-ATC--------";
    /** Series Or Group Of Targets */
    final String FSUPP_ARS_ARATGT_SGTGT = "G-F-ATG--------";
    /** Smoke */
    final String FSUPP_ARS_ARATGT_SMK = "G-F-ATS--------";
    /** Bomb Area */
    final String FSUPP_ARS_ARATGT_BMARA = "G-F-ATB--------";
    /** Fire Support Area (FSA), Irregular */
    final String FSUPP_ARS_C2ARS_FSA_IRR = "G-F-ACSI-------";
    /** Fire Support Area (FSA), Rectangular */
    final String FSUPP_ARS_C2ARS_FSA_RTG = "G-F-ACSR-------";
    /** Fire Support Area (FSA), Circular */
    final String FSUPP_ARS_C2ARS_FSA_CIRCLR = "G-F-ACSC-------";
    /** Airspace Coordination Area (ACA), Irregular */
    final String FSUPP_ARS_C2ARS_ACA_IRR = "G-F-ACAI-------";
    /** Airspace Coordination Area (ACA), Rectangular */
    final String FSUPP_ARS_C2ARS_ACA_RTG = "G-F-ACAR-------";
    /** Airspace Coordination Area (ACA), Circular */
    final String FSUPP_ARS_C2ARS_ACA_CIRCLR = "G-F-ACAC-------";
    /** Free Fire Area (FFA), Irregular */
    final String FSUPP_ARS_C2ARS_FFA_IRR = "G-F-ACFI-------";
    /** Free Fire Area (FFA), Rectangular */
    final String FSUPP_ARS_C2ARS_FFA_RTG = "G-F-ACFR-------";
    /** Free Fire Area (FFA), Circular */
    final String FSUPP_ARS_C2ARS_FFA_CIRCLR = "G-F-ACFC-------";
    /** No Fire Area (NFA), Irregular */
    final String FSUPP_ARS_C2ARS_NFA_IRR = "G-F-ACNI-------";
    /** No Fire Area (NFA), Rectangular */
    final String FSUPP_ARS_C2ARS_NFA_RTG = "G-F-ACNR-------";
    /** No , Circular */
    final String FSUPP_ARS_C2ARS_NFA_CIRCLR = "G-F-ACNC-------";
    /** Restrictive Fire Area (RFA), Irregular */
    final String FSUPP_ARS_C2ARS_RFA_IRR = "G-F-ACRI-------";
    /** Restrictive Fire Area (RFA), Rectangular */
    final String FSUPP_ARS_C2ARS_RFA_RTG = "G-F-ACRR-------";
    /** Restrictive Fire Area (RFA), Circular */
    final String FSUPP_ARS_C2ARS_RFA_CIRCLR = "G-F-ACRC-------";
    /** Position Area For Artillery (PAA), Rectangular */
    final String FSUPP_ARS_C2ARS_PAA_RTG = "G-F-ACPR-------";
    /** Position Area For Artillery (PAA), Circular */
    final String FSUPP_ARS_C2ARS_PAA_CIRCLR = "G-F-ACPC-------";
    /** Sensor Zone, Irregular */
    final String FSUPP_ARS_C2ARS_SNSZ_IRR = "G-F-ACEI-------";
    /** Sensor Zone, Rectangular */
    final String FSUPP_ARS_C2ARS_SNSZ_RTG = "G-F-ACER-------";
    /** Sensor Zone ,  Circular */
    final String FSUPP_ARS_C2ARS_SNSZ_CIRCLR = "G-F-ACEC-------";
    /** Dead Space Area (DA),  Irregular */
    final String FSUPP_ARS_C2ARS_DA_IRR = "G-F-ACDI-------";
    /** Dead Space Area (DA),  Rectangular */
    final String FSUPP_ARS_C2ARS_DA_RTG = "G-F-ACDR-------";
    /** Dead Space Area (DA),  Circular */
    final String FSUPP_ARS_C2ARS_DA_CIRCLR = "G-F-ACDC-------";
    /** Zone Of Responsibility (ZOR), Irregular */
    final String FSUPP_ARS_C2ARS_ZOR_IRR = "G-F-ACZI-------";
    /** Zone Of Responsibility (ZOR), Rectangular */
    final String FSUPP_ARS_C2ARS_ZOR_RTG = "G-F-ACZR-------";
    /** Zone Of Responsibility (ZOR), Circular */
    final String FSUPP_ARS_C2ARS_ZOR_CIRCLR = "G-F-ACZC-------";
    /** Target Build Up Area (TBA), Irregular */
    final String FSUPP_ARS_C2ARS_TBA_IRR = "G-F-ACBI-------";
    /** Target Build Up Area (TBA),Rectangular */
    final String FSUPP_ARS_C2ARS_TBA_RTG = "G-F-ACBR-------";
    /** Target Build Up Area (TBA), Circular */
    final String FSUPP_ARS_C2ARS_TBA_CIRCLR = "G-F-ACBC-------";
    /** Target , Irregular */
    final String FSUPP_ARS_C2ARS_TVAR_IRR = "G-F-ACVI-------";
    /** Target Value Area (TVAR), Rectangular */
    final String FSUPP_ARS_C2ARS_TVAR_RTG = "G-F-ACVR-------";
    /** Target Value Area (TVAR), Circular */
    final String FSUPP_ARS_C2ARS_TVAR_CIRCLR = "G-F-ACVC-------";
    /** Terminally Guided Munition Footprint (TGMF) */
    final String FSUPP_ARS_C2ARS_TGMF = "G-F-ACT--------";
    /** Artillery Target Intelligence (ATI) Zone, Irregular */
    final String FSUPP_ARS_TGTAQZ_ATIZ_IRR = "G-F-AZII-------";
    /** Artillery Target Intelligence (ATI) Zone, Rectangular */
    final String FSUPP_ARS_TGTAQZ_ATIZ_RTG = "G-F-AZIR-------";
    /** Call For Fire Zone (CFFZ), Irregular */
    final String FSUPP_ARS_TGTAQZ_CFFZ_IRR = "G-F-AZXI-------";
    /** Call For Fire Zone (CFFZ), Rectangular */
    final String FSUPP_ARS_TGTAQZ_CFFZ_RTG = "G-F-AZXR-------";
    /** Censor Zone,  Irregular */
    final String FSUPP_ARS_TGTAQZ_CNS_IRR = "G-F-AZCI-------";
    /** Censor Zone, Rectangular */
    final String FSUPP_ARS_TGTAQZ_CNS_RTG = "G-F-AZCR-------";
    /** Critical Friendly Zone (CFZ), Irregular */
    final String FSUPP_ARS_TGTAQZ_CFZ_IRR = "G-F-AZFI-------";
    /** Critical Friendly Zone (CFZ), Rectangular */
    final String FSUPP_ARS_TGTAQZ_CFZ_RTG = "G-F-AZFR-------";
    /** Weapon/Sensor Range Fan, Circular */
    final String FSUPP_ARS_WPNRF_CIRCLR = "G-F-AXC--------";
    /** Weapon/Sensor Range Fan, Sector */
    final String FSUPP_ARS_WPNRF_SCR = "G-F-AXS--------";
    /** Blue Kill Box,  Circular */
    final String FSUPP_ARS_KLBOX_BLUE_CIRCLR = "G-F-AKBC-------";
    /** Blue Kill Box, Irregular */
    final String FSUPP_ARS_KLBOX_BLUE_IRR = "G-F-AKBI-------";
    /** Blue , Rectangular */
    final String FSUPP_ARS_KLBOX_BLUE_RTG = "G-F-AKBR-------";
    /** Purple Kill Box, Circular */
    final String FSUPP_ARS_KLBOX_PURPLE_CIRCLR = "G-F-AKPC-------";
    /** Purple Kill Box, Irregular */
    final String FSUPP_ARS_KLBOX_PURPLE_IRR = "G-F-AKPI-------";
    /** Purple Kill Box, Rectangular */
    final String FSUPP_ARS_KLBOX_PURPLE_RTG = "G-F-AKPR-------";

    ////////////////////////////////////////////////
    // Combat Service Support
    ////////////////////////////////////////////////

    /** Ambulance Exchange Point */
    final String CSS_PNT_AEP = "G-S-PX---------";
    /** Cannibalization Point */
    final String CSS_PNT_CBNP = "G-S-PC---------";
    /** Casualty Collection Point */
    final String CSS_PNT_CCP = "G-S-PY---------";
    /** Civilian Collection Point */
    final String CSS_PNT_CVP = "G-S-PT---------";
    /** Detainee Collection Point */
    final String CSS_PNT_DCP = "G-S-PD---------";
    /** Enemy Prisoner Of War (EPW) Collection Point */
    final String CSS_PNT_EPWCP = "G-S-PE---------";
    /** Logistics Release Point (LRP) */
    final String CSS_PNT_LRP = "G-S-PL---------";
    /** Maintenance Collection Point */
    final String CSS_PNT_MCP = "G-S-PM---------";
    /** Rearm, Refuel And Resupply Point */
    final String CSS_PNT_RRRP = "G-S-PR---------";
    /** Refuel On The Move (ROM) Point */
    final String CSS_PNT_ROM = "G-S-PU---------";
    /** Traffic Control Post (TCP) */
    final String CSS_PNT_TCP = "G-S-PO---------";
    /** Trailer Transfer Point */
    final String CSS_PNT_TTP = "G-S-PI---------";
    /** Unit Maintenance Collection Point */
    final String CSS_PNT_UMC = "G-S-PN---------";
    /** General */
    final String CSS_PNT_SPT_GNL = "G-S-PSZ--------";
    /** Class I */
    final String CSS_PNT_SPT_CLS1 = "G-S-PSA--------";
    /** Class Ii */
    final String CSS_PNT_SPT_CLS2 = "G-S-PSB--------";
    /** Class Iii */
    final String CSS_PNT_SPT_CLS3 = "G-S-PSC--------";
    /** Class Iv */
    final String CSS_PNT_SPT_CLS4 = "G-S-PSD--------";
    /** Class V */
    final String CSS_PNT_SPT_CLS5 = "G-S-PSE--------";
    /** Class Vi */
    final String CSS_PNT_SPT_CLS6 = "G-S-PSF--------";
    /** Class Vii */
    final String CSS_PNT_SPT_CLS7 = "G-S-PSG--------";
    /** Class Viii */
    final String CSS_PNT_SPT_CLS8 = "G-S-PSH--------";
    /** Class Ix */
    final String CSS_PNT_SPT_CLS9 = "G-S-PSI--------";
    /** Class X */
    final String CSS_PNT_SPT_CLS10 = "G-S-PSJ--------";
    /** Ammunition Supply Point (ASP) */
    final String CSS_PNT_AP_ASP = "G-S-PAS--------";
    /** Ammunition Transfer Point (ATP) */
    final String CSS_PNT_AP_ATP = "G-S-PAT--------";
    /** Moving Convoy */
    final String CSS_LNE_CNY_MCNY = "G-S-LCM--------";
    /** Halted Convoy */
    final String CSS_LNE_CNY_HCNY = "G-S-LCH--------";
    /** Main Supply Route */
    final String CSS_LNE_SLPRUT_MSRUT = "G-S-LRM--------";
    /** Alternate Supply Route */
    final String CSS_LNE_SLPRUT_ASRUT = "G-S-LRA--------";
    /** One-Way Traffic */
    final String CSS_LNE_SLPRUT_1WTRFF = "G-S-LRO--------";
    /** Alternating Traffic */
    final String CSS_LNE_SLPRUT_ATRFF = "G-S-LRT--------";
    /** Two-Way Traffic */
    final String CSS_LNE_SLPRUT_2WTRFF = "G-S-LRW--------";
    /** Detainee Holding Area */
    final String CSS_ARA_DHA = "G-S-AD---------";
    /** Enemy Prisoner Of War (EPW) Holding Area */
    final String CSS_ARA_EPWHA = "G-S-AE---------";
    /** Forward Arming And Refueling Area (FARP) */
    final String CSS_ARA_FARP = "G-S-AR---------";
    /** Refugee Holding Area */
    final String CSS_ARA_RHA = "G-S-AH---------";
    /** Brigade (BSA) */
    final String CSS_ARA_SUPARS_BSA = "G-S-ASB--------";
    /** Division (DSA) */
    final String CSS_ARA_SUPARS_DSA = "G-S-ASD--------";
    /** Regimental (RSA) */
    final String CSS_ARA_SUPARS_RSA = "G-S-ASR--------";

    //////////////////////////////////////////////
    // Other
    //////////////////////////////////////////////

    /** Ditched Aircraft */
    final String OTH_ER_DTHAC = "G-O-ED---------";
    /** Person In Water */
    final String OTH_ER_PIW = "G-O-EP---------";
    /** Distressed Vessel */
    final String OTH_ER_DSTVES = "G-O-EV---------";
    /** Sea Mine-Like */
    final String OTH_HAZ_SML = "G-O-HM---------";
    /** Navigational */
    final String OTH_HAZ_NVGL = "G-O-HN---------";
    /** Iceberg */
    final String OTH_HAZ_IB = "G-O-HI---------";
    /** Oil Rig */
    final String OTH_HAZ_OLRG = "G-O-HO---------";
    /** Bottom Return/Non-Milco */
    final String OTH_SSUBSR_BTMRTN = "G-O-SB---------";
    /** Installation/Manmade */
    final String OTH_SSUBSR_BTMRTN_INS = "G-O-SBM--------";
    /** Seabed Rock/Stone,  Obstacle,Other */
    final String OTH_SSUBSR_BTMRTN_SBRSOO = "G-O-SBN--------";
    /** Wreck,  Non Dangerous */
    final String OTH_SSUBSR_BTMRTN_WRKND = "G-O-SBW--------";
    /** Wreck,  Dangerous */
    final String OTH_SSUBSR_BTMRTN_WRKD = "G-O-SBX--------";
    /** Marine Life */
    final String OTH_SSUBSR_MARLFE = "G-O-SM---------";
    /** Sea Anomaly (Wake, Current, Knuckle) */
    final String OTH_SSUBSR_SA = "G-O-SS---------";
    /** Bearing Line */
    final String OTH_BERLNE = "G-O-B----------";
    /** Electronic Bearing Line */
    final String OTH_BERLNE_ELC = "G-O-BE---------";
    /** Acoustic Bearing Line */
    final String OTH_BERLNE_ACU = "G-O-BA---------";
    /** Torpedo, Bearing Line */
    final String OTH_BERLNE_TPD = "G-O-BT---------";
    /** Electro-Optical Intercept */
    final String OTH_BERLNE_EOPI = "G-O-BO---------";
    /** Acoustic Fix */
    final String OTH_FIX_ACU = "G-O-FA---------";
    /** Electro-Magnetic Fix */
    final String OTH_FIX_EM = "G-O-FE---------";
    /** Electro-Optical Fix */
    final String OTH_FIX_EOP = "G-O-FO---------";
}
