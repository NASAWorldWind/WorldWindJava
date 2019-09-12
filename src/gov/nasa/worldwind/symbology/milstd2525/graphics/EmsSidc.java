/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

/**
 * SIDC constants for graphics in the "Emergency Management" scheme (MIL-STD-2525C Appendix G). The constants in this
 * interface are "masked" SIDCs. All fields except Scheme, Category, and Function ID are filled with hyphens. (The other
 * fields do not identity a type of graphic, they modify the graphic.)
 * <p>
 * Note: this interface only defines constants for tactical graphics in Appendix G.
 *
 * @author pabercrombie
 * @version $Id$
 */
public interface EmsSidc
{
    /** Aftershock. */
    final String NATEVT_GEO_AFTSHK = "E-N-AA---------";
    /** Avalanche. */
    final String NATEVT_GEO_AVL = "E-N-AB---------";
    /** Earthquake epicenter. */
    final String NATEVT_GEO_EQKEPI = "E-N-AC---------";
    /** Landslide. */
    final String NATEVT_GEO_LNDSLD = "E-N-AD---------";
    /** Subsidence. */
    final String NATEVT_GEO_SBSDNC = "E-N-AE---------";
    /** Volcanic threat. */
    final String NATEVT_GEO_VLCTHT = "E-N-AG---------";
    /** Drought. */
    final String NATEVT_HYDMET_DRGHT = "E-N-BB---------";
    /** Flood. */
    final String NATEVT_HYDMET_FLD = "E-N-BC---------";
    /** Inversion. */
    final String NATEVT_HYDMET_INV = "E-N-BF---------";
    /** Tsunami. */
    final String NATEVT_HYDMET_TSNMI = "E-N-BM---------";
    /** Bird infestation. */
    final String NATEVT_INFST_BIRD = "E-N-CA---------";
    /** Insect infestation. */
    final String NATEVT_INFST_INSCT = "E-N-CB---------";
    /** Microbial infestation. */
    final String NATEVT_INFST_MICROB = "E-N-CC---------";
    /** Reptile infestation. */
    final String NATEVT_INFST_REPT = "E-N-CD---------";
    /** Rodent infestation. */
    final String NATEVT_INFST_RDNT = "E-N-CE---------";
}
