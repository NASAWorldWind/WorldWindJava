/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: RPFDataSeries.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public enum RPFDataSeries
{
    /* [Section 5.1.4, MIL-STD-2411-1] */
    DATA_SERIES_GN("GN", "GNC", "1:5,000,000", "Global Navigation Chart", "CADRG", 5000000),
    DATA_SERIES_JN("JN", "JNC", "1:2,000,000", "Jet Navigation Chart", "CADRG", 2000000),
    DATA_SERIES_OH("OH", "VHRC", "1:1,000,000", "VFR Helicopter Route Chart", "CADRG", 1000000),
    DATA_SERIES_ON("ON", "ONC", "1:1,000,000", "Operational Navigation Chart", "CADRG", 1000000),
    DATA_SERIES_OW("OW", "WAC", "1:1,000,000", "High Flying Chart - Host Nation", "CADRG", 1000000),
    DATA_SERIES_TP("TP", "TPC", "1:500,000", "Tactical Pilotage Chart", "CADRG", 500000),
    DATA_SERIES_LF("LF", "LFC-FR (Day)", "1:500,000", "Low Flying Chart (Day) - Host Nation", "CADRG", 500000),
    DATA_SERIES_L1("L1", "LFC-1", "1:500,000", "Low Flying Chart (TBD #1)", "CADRG", 500000),
    DATA_SERIES_L2("L2", "LFC-2", "1:500,000", "Low Flying Chart (TBD #2)", "CADRG", 500000),
    DATA_SERIES_L3("L3", "LFC-3", "1:500,000", "Low Flying Chart (TBD #3)", "CADRG", 500000),
    DATA_SERIES_L4("L4", "LFC-4", "1:500,000", "Low Flying Chart (TBD #4)", "CADRG", 500000),
    DATA_SERIES_L5("L5", "LFC-5", "1:500,000", "Low Flying Chart (TBD #5)", "CADRG", 500000),
    DATA_SERIES_LN("LN", "LFC (Night)", "1:500,000", "Low Flying Chart (Night) - Host Nation", "CADRG", 500000),
    DATA_SERIES_JG("JG", "JOG", "1:250,000", "Joint Operations Graphic", "CADRG", 250000),
    DATA_SERIES_JA("JA", "JOG-A", "1:250,000", "Joint Operations Graphic - Air", "CADRG", 250000),
    DATA_SERIES_JR("JR", "JOG-R", "1:250,000", "Joint Operations Graphic - Radar", "CADRG", 250000),
    DATA_SERIES_JO("JO", "OPG", "1:250,000", "Operational Planning Graphic", "CADRG", 250000),
    DATA_SERIES_VT("VT", "VTAC", "1:250,000", "VFR Terminal Area Chart", "CADRG", 250000),
    DATA_SERIES_F1("F1", "TFC-1", "1:250,000", "Transit Flying Chart (TBD #1)", "CADRG", 250000),
    DATA_SERIES_F2("F2", "TFC-2", "1:250,000", "Transit Flying Chart (TBD #2)", "CADRG", 250000),
    DATA_SERIES_F3("F3", "TFC-3", "1:250,000", "Transit Flying Chart (TBD #3)", "CADRG", 250000),
    DATA_SERIES_F4("F4", "TFC-4", "1:250,000", "Transit Flying Chart (TBD #4)", "CADRG", 250000),
    DATA_SERIES_F5("F5", "TFC-5", "1:250,000", "Transit Flying Chart (TBD #5)", "CADRG", 250000),
    DATA_SERIES_AT("AT", "ATC", "1:200,000", "Series 200 Air Target Chart", "CADRG", 200000),
    DATA_SERIES_VH("VH", "HRC", "1:125,000", "Helicopter Route Chart", "CADRG", 125000),
    DATA_SERIES_TN("TN", "TFC (Night)", "1:250,000", "Transit Flying Chart (Night) - Host Nation", "CADRG", 250000),
    DATA_SERIES_TR("TR", "TLM200", "1:200,000", "Topographic Line Map 1:200,000 scale", "CADRG", 200000),
    DATA_SERIES_TC("TC", "TLM 100", "1:100,000", "Topographic Line Map 1:100,0000 scale", "CADRG", 100000),
    DATA_SERIES_RV("RV", "Riverine", "1:50,000", "Riverine Map 1:50,000 scale", "CADRG", 50000),
    DATA_SERIES_TL("TL", "TLM 50", "1:50,000", "Topographic Line Map", "CADRG", 50000),
    DATA_SERIES_UL("UL", "TLM50-Other", "1:50,000", "Topographic Line Map (other 1:50,000 scale)", "CADRG", 50000),
    DATA_SERIES_TT("TT", "TLM25", "1:25,000", "Topographic Line Map 1:25,000 scale", "CADRG", 25000),
    DATA_SERIES_TQ("TQ", "TLM24", "1:24,000", "Topographic Line Map 1:24,000 scale", "CADRG", 24000),
    DATA_SERIES_HA("HA", "HA", "Various", "Harbor and Approach Charts", "CADRG", -1),
    DATA_SERIES_CO("CO", "CO", "Various", "Coastal Charts", "CADRG", -1),
    DATA_SERIES_OA("OA", "OPAREA", "Various", "Naval Range Operating Area Chart","CADRG", -1),
    DATA_SERIES_CG("CG", "CG", "Various",  "City Graphics", "CADRG", -1),
    DATA_SERIES_C1("C1", "CG", "1:10,000", "City Graphics", "CADRG", 10000),
    DATA_SERIES_C2("C2", "CG", "1:10,560", "City Graphics", "CADRG", 10560),
    DATA_SERIES_C3("C3", "CG", "1:11,000", "City Graphics", "CADRG", 11000),
    DATA_SERIES_C4("C4", "CG", "1:11,800", "City Graphics", "CADRG", 11800),
    DATA_SERIES_C5("C5", "CG", "1:12,000", "City Graphics", "CADRG", 12000),
    DATA_SERIES_C6("C6", "CG", "1:12,500", "City Graphics", "CADRG", 12500),
    DATA_SERIES_C7("C7", "CG", "1:12,800", "City Graphics", "CADRG", 12800),
    DATA_SERIES_C8("C8", "CG", "1:14,000", "City Graphics", "CADRG", 14000),
    DATA_SERIES_C9("C9", "CG", "1:14,700", "City Graphics", "CADRG", 14700),
    DATA_SERIES_CA("CA", "CG", "1:15,000", "City Graphics", "CADRG", 15000),
    DATA_SERIES_CB("CB", "CG", "1:15,500", "City Graphics", "CADRG", 15500),
    DATA_SERIES_CC("CC", "CG", "1:16,000", "City Graphics", "CADRG", 16000),
    DATA_SERIES_CD("CD", "CG", "1:16,666", "City Graphics", "CADRG", 16666),
    DATA_SERIES_CE("CE", "CG", "1:17,000", "City Graphics", "CADRG", 17000),
    DATA_SERIES_CF("CF", "CG", "1:17,500", "City Graphics", "CADRG", 17500),
    DATA_SERIES_CH("CH", "CG", "1:18,000", "City Graphics", "CADRG", 18000),
    DATA_SERIES_CJ("CJ", "CG", "1:20,000", "City Graphics", "CADRG", 20000),
    DATA_SERIES_CK("CK", "CG", "1:21,000", "City Graphics", "CADRG", 21000),
    DATA_SERIES_CL("CL", "CG", "1:21,120", "City Graphics", "CADRG", 21120),
    DATA_SERIES_CN("CN", "CG", "1:22,000", "City Graphics", "CADRG", 22000),
    DATA_SERIES_CP("CP", "CG", "1:23,000", "City Graphics", "CADRG", 23000),
    DATA_SERIES_CQ("CQ", "CG", "1:25,000", "City Graphics", "CADRG", 25000),
    DATA_SERIES_CR("CR", "CG", "1:26,000", "City Graphics", "CADRG", 26000),
    DATA_SERIES_CS("CS", "CG", "1:35,000", "City Graphics", "CADRG", 35000),
    DATA_SERIES_CT("CT", "CG", "1:36,000", "City Graphics", "CADRG", 36000),
    DATA_SERIES_CM("CM", "CM", "Various",  "Combat Charts", "CADRG", -1),
    DATA_SERIES_A1("A1", "CM", "1:10,000", "Combat Charts, 1:10,000 scale", "CADRG", 10000),
    DATA_SERIES_A2("A2", "CM", "1:25,000", "Combat Charts, 1:25,000 scale", "CADRG", 25000),
    DATA_SERIES_A3("A3", "CM", "1:50,000", "Combat Charts, 1:50,000 scale", "CADRG", 50000),
    DATA_SERIES_A4("A4", "CM", "1:100,000", "Combat Charts, 1:100,000 scale", "CADRG", 100000),
    DATA_SERIES_MI("MI", "MIM", "1:50,000", "Military Installation Maps", "CADRG", 50000),
    DATA_SERIES_M1("M1", "MIM", "Various", "Military Installation Map (TBD #1)", "CADRG", -1),
    DATA_SERIES_M2("M2", "MIM", "Various", "Military Installation Map (TBD #2)", "CADRG", -1),
    DATA_SERIES_VN("VN", "VNC", "1:500,000", "Visual Navigation Charts", "CADRG", 500000),
    DATA_SERIES_MM("MM", "---", "Various", "Miscellaneous Maps & Charts", "CADRG", -1),
    DATA_SERIES_I1("I1", "---", "10m", "Imagery, 10 meter resolution", "CIB", 10.0),
    DATA_SERIES_I2("I2", "---", "5m", "Imagery, 5 meter resolution", "CIB", 5.0),
    DATA_SERIES_I3("I3", "---", "2m", "Imagery, 2 meter resolution", "CIB", 2.0),
    DATA_SERIES_I4("I4", "---", "1m", "Imagery, 1 meter resolution", "CIB", 1.0),
    DATA_SERIES_I5("I5", "---", ".5m", "Imagery, .5 (half) meter resolution", "CIB", 0.5),
    DATA_SERIES_IV("IV", "---", "Various>10m", "Imagery, greater than 10 meter resolution", "CIB", -1),
    DATA_SERIES_D1("D1", "---", "100m", "Elevation Data from DTED level 1", "CDTED", 100.0),
    DATA_SERIES_D2("D1", "---", "30m", "Elevation Data from DTED level 2", "CDTED", 30.0),
    /* [Chart.php] */
    DATA_SERIES_TF("TF", "---", "1:250000", "Transit Fly (UK)", "CADRG", 250000),
    ;

    public final String seriesCode;
    public final String seriesAbbreviation;
    public final String scaleOrResolution;
    public final String dataSeries;
    public final String rpfDataType;
    public final double scaleOrGSD;

    private RPFDataSeries(String seriesCode, String seriesAbbreviation, String scaleOrResolution, String dataSeries,
        String rpfDataType, double scaleOrGSD)
    {
        this.rpfDataType = rpfDataType;
        this.seriesCode = seriesCode;
        this.seriesAbbreviation = seriesAbbreviation;
        this.scaleOrResolution = scaleOrResolution;
        this.dataSeries = dataSeries;
        this.scaleOrGSD = scaleOrGSD;
    }

    private static Map<String, RPFDataSeries> enumConstantDirectory = null;

    private static synchronized Map<String, RPFDataSeries> enumConstantDirectory()
    {
        if (enumConstantDirectory == null)
        {
            RPFDataSeries[] universe = RPFDataSeries.class.getEnumConstants();
            enumConstantDirectory = new HashMap<String, RPFDataSeries>(2 * universe.length);
            for (RPFDataSeries dataSeries : universe)
            {
                enumConstantDirectory.put(dataSeries.seriesCode, dataSeries);
            }
        }
        return enumConstantDirectory;
    }

    public static RPFDataSeries dataSeriesFor(String seriesCode)
    {
        if (seriesCode == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        RPFDataSeries dataSeries = enumConstantDirectory().get(seriesCode);
        if (dataSeries == null)
        {
            String message = Logging.getMessage("generic.EnumNotFound", seriesCode);
            Logging.logger().fine(message);
            throw new EnumConstantNotPresentException(RPFDataSeries.class, message);
        }
        return dataSeries;
    }

    public static boolean isDataSeriesCode(String seriesCode)
    {
        if (seriesCode == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return enumConstantDirectory().get(seriesCode) != null;
    }

    // ============== RPF Data Types ======================= //
    // ============== RPF Data Types ======================= //
    // ============== RPF Data Types ======================= //

    private static final String CADRG_DATA_TYPE = "CADRG";
    private static final String CIB_DATA_TYPE = "CIB";
    private static String[] RPF_DATA_TYPES = {"CADRG", "CIB"};

    public static boolean isRPFDataType(String rpfDataType)
    {
        if (rpfDataType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        for (String s : RPF_DATA_TYPES)
        {
            if (s.equalsIgnoreCase(rpfDataType))
                return true;
        }
        return false;
    }

    public static boolean isCADRGDataType(String rpfDataType)
    {
        if (rpfDataType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return CADRG_DATA_TYPE.equalsIgnoreCase(rpfDataType);
    }

    public static boolean isCIBDataType(String rpfDataType)
    {
        if (rpfDataType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return CIB_DATA_TYPE.equalsIgnoreCase(rpfDataType);
    }

    public static boolean isCADRGDataSeries(String seriesCode)
    {
        if (seriesCode == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        RPFDataSeries dataSeries = dataSeriesFor(seriesCode);
        return isCADRGDataType(dataSeries.rpfDataType);
    }

    public static boolean isCIBDataSeries(String seriesCode)
    {
        if (seriesCode == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        RPFDataSeries dataSeries = dataSeriesFor(seriesCode);
        return isCIBDataType(dataSeries.rpfDataType);
    }
}
