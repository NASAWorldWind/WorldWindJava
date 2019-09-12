/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

/**
 * Converter used to translate Transverse Mercator coordinates to and from geodetic latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id: TMCoordConverter.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TMCoord, UTMCoordConverter, MGRSCoordConverter
 */

/**
 * Ported to Java from the NGA GeoTrans code tranmerc.c and tranmerc.h
 *
 * @author Garrett Headley, Patrick Murris
 */
class TMCoordConverter
{
    public final static int TRANMERC_NO_ERROR = 0x0000;
    private final static int TRANMERC_LAT_ERROR = 0x0001;
    private final static int TRANMERC_LON_ERROR = 0x0002;
    public final static int TRANMERC_EASTING_ERROR = 0x0004;
    public final static int TRANMERC_NORTHING_ERROR = 0x0008;
    private final static int TRANMERC_ORIGIN_LAT_ERROR = 0x0010;
    private final static int TRANMERC_CENT_MER_ERROR = 0x0020;
    private final static int TRANMERC_A_ERROR = 0x0040;
    private final static int TRANMERC_INV_F_ERROR = 0x0080;
    private final static int TRANMERC_SCALE_FACTOR_ERROR = 0x0100;
    public final static int TRANMERC_LON_WARNING = 0x0200;

    private final static double PI = 3.14159265358979323; /* PI     */
    public final static double PI_OVER = (PI / 2.0);            /* PI over 2 */
    private final static double MAX_LAT = ((PI * 89.99) / 180.0);    /* 90 degrees in radians */
    private final static double MAX_DELTA_LONG = ((PI * 90) / 180.0);    /* 90 degrees in radians */
    private final static double MIN_SCALE_FACTOR = 0.3;
    private final static double MAX_SCALE_FACTOR = 3.0;

    /* Ellipsoid Parameters, default to WGS 84  */
    private double TranMerc_a = 6378137.0;              /* Semi-major axis of ellipsoid i meters */
    private double TranMerc_f = 1 / 298.257223563;      /* Flattening of ellipsoid  */
    private double TranMerc_es = 0.0066943799901413800; /* Eccentricity (0.08181919084262188000) squared */
    private double TranMerc_ebs = 0.0067394967565869;   /* Second Eccentricity squared */

    /* Transverse_Mercator projection Parameters */
    private double TranMerc_Origin_Lat = 0.0;           /* Latitude of origin in radians */
    private double TranMerc_Origin_Long = 0.0;          /* Longitude of origin in radians */
    private double TranMerc_False_Northing = 0.0;       /* False northing in meters */
    private double TranMerc_False_Easting = 0.0;        /* False easting in meters */
    private double TranMerc_Scale_Factor = 1.0;         /* Scale factor  */

    /* Isometeric to geodetic latitude parameters, default to WGS 84 */
    private double TranMerc_ap = 6367449.1458008;
    private double TranMerc_bp = 16038.508696861;
    private double TranMerc_cp = 16.832613334334;
    private double TranMerc_dp = 0.021984404273757;
    private double TranMerc_ep = 3.1148371319283e-005;

    /* Maximum variance for easting and northing values for WGS 84. */
    private double TranMerc_Delta_Easting = 40000000.0;
    private double TranMerc_Delta_Northing = 40000000.0;

    private double Easting;
    private double Northing;
    private double Longitude;
    private double Latitude;

    TMCoordConverter()
    {
    }

    public double getA()
    {
        return TranMerc_a;
    }

    public double getF()
    {
        return TranMerc_f;
    }

    /**
     * The function Set_Tranverse_Mercator_Parameters receives the ellipsoid parameters and Tranverse Mercator
     * projection parameters as inputs, and sets the corresponding state variables. If any errors occur, the error
     * code(s) are returned by the function, otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param a                Semi-major axis of ellipsoid, in meters
     * @param f                Flattening of ellipsoid
     * @param Origin_Latitude  Latitude in radians at the origin of the projection
     * @param Central_Meridian Longitude in radians at the center of the projection
     * @param False_Easting    Easting/X at the center of the projection
     * @param False_Northing   Northing/Y at the center of the projection
     * @param Scale_Factor     Projection scale factor
     *
     * @return error code
     */
    public long setTransverseMercatorParameters(double a, double f, double Origin_Latitude,
        double Central_Meridian,
        double False_Easting, double False_Northing, double Scale_Factor)
    {
        double tn;        /* True Meridianal distance constant  */
        double tn2;
        double tn3;
        double tn4;
        double tn5;
        double TranMerc_b; /* Semi-minor axis of ellipsoid, in meters */
        double inv_f = 1 / f;
        long Error_Code = TRANMERC_NO_ERROR;

        if (a <= 0.0)
        { /* Semi-major axis must be greater than zero */
            Error_Code |= TRANMERC_A_ERROR;
        }
        if ((inv_f < 250) || (inv_f > 350))
        { /* Inverse flattening must be between 250 and 350 */
            Error_Code |= TRANMERC_INV_F_ERROR;
        }
        if ((Origin_Latitude < -MAX_LAT) || (Origin_Latitude > MAX_LAT))
        { /* origin latitude out of range */
            Error_Code |= TRANMERC_ORIGIN_LAT_ERROR;
        }
        if ((Central_Meridian < -PI) || (Central_Meridian > (2 * PI)))
        { /* origin longitude out of range */
            Error_Code |= TRANMERC_CENT_MER_ERROR;
        }
        if ((Scale_Factor < MIN_SCALE_FACTOR) || (Scale_Factor > MAX_SCALE_FACTOR))
        {
            Error_Code |= TRANMERC_SCALE_FACTOR_ERROR;
        }
        if (Error_Code == TRANMERC_NO_ERROR)
        { /* no errors */
            TranMerc_a = a;
            TranMerc_f = f;
            TranMerc_Origin_Lat = 0;
            TranMerc_Origin_Long = 0;
            TranMerc_False_Northing = 0;
            TranMerc_False_Easting = 0;
            TranMerc_Scale_Factor = 1;

            /* Eccentricity Squared */
            TranMerc_es = 2 * TranMerc_f - TranMerc_f * TranMerc_f;
            /* Second Eccentricity Squared */
            TranMerc_ebs = (1 / (1 - TranMerc_es)) - 1;

            TranMerc_b = TranMerc_a * (1 - TranMerc_f);
            /*True meridianal constants  */
            tn = (TranMerc_a - TranMerc_b) / (TranMerc_a + TranMerc_b);
            tn2 = tn * tn;
            tn3 = tn2 * tn;
            tn4 = tn3 * tn;
            tn5 = tn4 * tn;

            TranMerc_ap = TranMerc_a * (1.e0 - tn + 5.e0 * (tn2 - tn3) / 4.e0
                + 81.e0 * (tn4 - tn5) / 64.e0);
            TranMerc_bp = 3.e0 * TranMerc_a * (tn - tn2 + 7.e0 * (tn3 - tn4)
                / 8.e0 + 55.e0 * tn5 / 64.e0) / 2.e0;
            TranMerc_cp = 15.e0 * TranMerc_a * (tn2 - tn3 + 3.e0 * (tn4 - tn5) / 4.e0) / 16.0;
            TranMerc_dp = 35.e0 * TranMerc_a * (tn3 - tn4 + 11.e0 * tn5 / 16.e0) / 48.e0;
            TranMerc_ep = 315.e0 * TranMerc_a * (tn4 - tn5) / 512.e0;

            convertGeodeticToTransverseMercator(MAX_LAT, MAX_DELTA_LONG);

            TranMerc_Delta_Easting = getEasting();
            TranMerc_Delta_Northing = getNorthing();

            convertGeodeticToTransverseMercator(0, MAX_DELTA_LONG);
            TranMerc_Delta_Easting = getEasting();

            TranMerc_Origin_Lat = Origin_Latitude;
            if (Central_Meridian > PI)
                Central_Meridian -= (2 * PI);
            TranMerc_Origin_Long = Central_Meridian;
            TranMerc_False_Northing = False_Northing;
            TranMerc_False_Easting = False_Easting;
            TranMerc_Scale_Factor = Scale_Factor;
        }
        return (Error_Code);
    }

    /**
     * The function Convert_Geodetic_To_Transverse_Mercator converts geodetic (latitude and longitude) coordinates to
     * Transverse Mercator projection (easting and northing) coordinates, according to the current ellipsoid and
     * Transverse Mercator projection coordinates.  If any errors occur, the error code(s) are returned by the function,
     * otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param Latitude  Latitude in radians
     * @param Longitude Longitude in radians
     *
     * @return error code
     */
    public long convertGeodeticToTransverseMercator(double Latitude, double Longitude)
    {
        double c;       /* Cosine of latitude                          */
        double c2;
        double c3;
        double c5;
        double c7;
        double dlam;    /* Delta longitude - Difference in Longitude       */
        double eta;     /* constant - TranMerc_ebs *c *c                   */
        double eta2;
        double eta3;
        double eta4;
        double s;       /* Sine of latitude                        */
        double sn;      /* Radius of curvature in the prime vertical       */
        double t;       /* Tangent of latitude                             */
        double tan2;
        double tan3;
        double tan4;
        double tan5;
        double tan6;
        double t1;      /* Term in coordinate conversion formula - GP to Y */
        double t2;      /* Term in coordinate conversion formula - GP to Y */
        double t3;      /* Term in coordinate conversion formula - GP to Y */
        double t4;      /* Term in coordinate conversion formula - GP to Y */
        double t5;      /* Term in coordinate conversion formula - GP to Y */
        double t6;      /* Term in coordinate conversion formula - GP to Y */
        double t7;      /* Term in coordinate conversion formula - GP to Y */
        double t8;      /* Term in coordinate conversion formula - GP to Y */
        double t9;      /* Term in coordinate conversion formula - GP to Y */
        double tmd;     /* True Meridional distance                        */
        double tmdo;    /* True Meridional distance for latitude of origin */
        long Error_Code = TRANMERC_NO_ERROR;
        double temp_Origin;
        double temp_Long;

        if ((Latitude < -MAX_LAT) || (Latitude > MAX_LAT))
        {  /* Latitude out of range */
            Error_Code |= TRANMERC_LAT_ERROR;
        }
        if (Longitude > PI)
            Longitude -= (2 * PI);
        if ((Longitude < (TranMerc_Origin_Long - MAX_DELTA_LONG))
            || (Longitude > (TranMerc_Origin_Long + MAX_DELTA_LONG)))
        {
            if (Longitude < 0)
                temp_Long = Longitude + 2 * PI;
            else
                temp_Long = Longitude;
            if (TranMerc_Origin_Long < 0)
                temp_Origin = TranMerc_Origin_Long + 2 * PI;
            else
                temp_Origin = TranMerc_Origin_Long;
            if ((temp_Long < (temp_Origin - MAX_DELTA_LONG))
                || (temp_Long > (temp_Origin + MAX_DELTA_LONG)))
                Error_Code |= TRANMERC_LON_ERROR;
        }
        if (Error_Code == TRANMERC_NO_ERROR)
        { /* no errors */
            /*
             *  Delta Longitude
             */
            dlam = Longitude - TranMerc_Origin_Long;

            if (Math.abs(dlam) > (9.0 * PI / 180))
            { /* Distortion will result if Longitude is more than 9 degrees from the Central Meridian */
                Error_Code |= TRANMERC_LON_WARNING;
            }

            if (dlam > PI)
                dlam -= (2 * PI);
            if (dlam < -PI)
                dlam += (2 * PI);
            if (Math.abs(dlam) < 2.e-10)
                dlam = 0.0;

            s = Math.sin(Latitude);
            c = Math.cos(Latitude);
            c2 = c * c;
            c3 = c2 * c;
            c5 = c3 * c2;
            c7 = c5 * c2;
            t = Math.tan(Latitude);
            tan2 = t * t;
            tan3 = tan2 * t;
            tan4 = tan3 * t;
            tan5 = tan4 * t;
            tan6 = tan5 * t;
            eta = TranMerc_ebs * c2;
            eta2 = eta * eta;
            eta3 = eta2 * eta;
            eta4 = eta3 * eta;

            /* radius of curvature in prime vertical */
            // sn = SPHSN(Latitude);
            sn = TranMerc_a / Math.sqrt(1 - TranMerc_es * Math.pow(Math.sin(Latitude), 2));

            /* True Meridianal Distances */
            // tmd = SPHTMD(Latitude);
            tmd = TranMerc_ap * Latitude
                - TranMerc_bp * Math.sin(2.0 * Latitude)
                + TranMerc_cp * Math.sin(4.0 * Latitude)
                - TranMerc_dp * Math.sin(6.0 * Latitude)
                + TranMerc_ep * Math.sin(8.0 * Latitude);
            /*  Origin  */

            // tmdo = SPHTMD (TranMerc_Origin_Lat);
            tmdo = TranMerc_ap * TranMerc_Origin_Lat
                - TranMerc_bp * Math.sin(2.0 * TranMerc_Origin_Lat)
                + TranMerc_cp * Math.sin(4.0 * TranMerc_Origin_Lat)
                - TranMerc_dp * Math.sin(6.0 * TranMerc_Origin_Lat)
                + TranMerc_ep * Math.sin(8.0 * TranMerc_Origin_Lat);

            /* northing */
            t1 = (tmd - tmdo) * TranMerc_Scale_Factor;
            t2 = sn * s * c * TranMerc_Scale_Factor / 2.e0;
            t3 = sn * s * c3 * TranMerc_Scale_Factor * (5.e0 - tan2 + 9.e0 * eta
                + 4.e0 * eta2) / 24.e0;

            t4 = sn * s * c5 * TranMerc_Scale_Factor * (61.e0 - 58.e0 * tan2
                + tan4 + 270.e0 * eta - 330.e0 * tan2 * eta + 445.e0 * eta2
                + 324.e0 * eta3 - 680.e0 * tan2 * eta2 + 88.e0 * eta4
                - 600.e0 * tan2 * eta3 - 192.e0 * tan2 * eta4) / 720.e0;

            t5 = sn * s * c7 * TranMerc_Scale_Factor * (1385.e0 - 3111.e0 *
                tan2 + 543.e0 * tan4 - tan6) / 40320.e0;

            Northing = TranMerc_False_Northing + t1 + Math.pow(dlam, 2.e0) * t2
                + Math.pow(dlam, 4.e0) * t3 + Math.pow(dlam, 6.e0) * t4
                + Math.pow(dlam, 8.e0) * t5;

            /* Easting */
            t6 = sn * c * TranMerc_Scale_Factor;
            t7 = sn * c3 * TranMerc_Scale_Factor * (1.e0 - tan2 + eta) / 6.e0;
            t8 = sn * c5 * TranMerc_Scale_Factor * (5.e0 - 18.e0 * tan2 + tan4
                + 14.e0 * eta - 58.e0 * tan2 * eta + 13.e0 * eta2 + 4.e0 * eta3
                - 64.e0 * tan2 * eta2 - 24.e0 * tan2 * eta3) / 120.e0;
            t9 = sn * c7 * TranMerc_Scale_Factor * (61.e0 - 479.e0 * tan2
                + 179.e0 * tan4 - tan6) / 5040.e0;

            Easting = TranMerc_False_Easting + dlam * t6 + Math.pow(dlam, 3.e0) * t7
                + Math.pow(dlam, 5.e0) * t8 + Math.pow(dlam, 7.e0) * t9;
        }
        return (Error_Code);
    }

    /** @return Easting/X at the center of the projection */
    public double getEasting()
    {
        return Easting;
    }

    /** @return Northing/Y at the center of the projection */
    public double getNorthing()
    {
        return Northing;
    }

    /**
     * The function Convert_Transverse_Mercator_To_Geodetic converts Transverse Mercator projection (easting and
     * northing) coordinates to geodetic (latitude and longitude) coordinates, according to the current ellipsoid and
     * Transverse Mercator projection parameters.  If any errors occur, the error code(s) are returned by the function,
     * otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param Easting  Easting/X in meters
     * @param Northing Northing/Y in meters
     *
     * @return error code
     */
    public long convertTransverseMercatorToGeodetic(double Easting, double Northing)
    {
        double c;       /* Cosine of latitude                          */
        double de;      /* Delta easting - Difference in Easting (Easting-Fe)    */
        double dlam;    /* Delta longitude - Difference in Longitude       */
        double eta;     /* constant - TranMerc_ebs *c *c                   */
        double eta2;
        double eta3;
        double eta4;
        double ftphi;   /* Footpoint latitude                              */
        int i;       /* Loop iterator                   */
        double s;       /* Sine of latitude                        */
        double sn;      /* Radius of curvature in the prime vertical       */
        double sr;      /* Radius of curvature in the meridian             */
        double t;       /* Tangent of latitude                             */
        double tan2;
        double tan4;
        double t10;     /* Term in coordinate conversion formula - GP to Y */
        double t11;     /* Term in coordinate conversion formula - GP to Y */
        double t12;     /* Term in coordinate conversion formula - GP to Y */
        double t13;     /* Term in coordinate conversion formula - GP to Y */
        double t14;     /* Term in coordinate conversion formula - GP to Y */
        double t15;     /* Term in coordinate conversion formula - GP to Y */
        double t16;     /* Term in coordinate conversion formula - GP to Y */
        double t17;     /* Term in coordinate conversion formula - GP to Y */
        double tmd;     /* True Meridional distance                        */
        double tmdo;    /* True Meridional distance for latitude of origin */
        long Error_Code = TRANMERC_NO_ERROR;

        if ((Easting < (TranMerc_False_Easting - TranMerc_Delta_Easting))
            || (Easting > (TranMerc_False_Easting + TranMerc_Delta_Easting)))
        { /* Easting out of range  */
            Error_Code |= TRANMERC_EASTING_ERROR;
        }
        if ((Northing < (TranMerc_False_Northing - TranMerc_Delta_Northing))
            || (Northing > (TranMerc_False_Northing + TranMerc_Delta_Northing)))
        { /* Northing out of range */
            Error_Code |= TRANMERC_NORTHING_ERROR;
        }

        if (Error_Code == TRANMERC_NO_ERROR)
        {
            /* True Meridional Distances for latitude of origin */
            // tmdo = SPHTMD(TranMerc_Origin_Lat);
            tmdo = TranMerc_ap * TranMerc_Origin_Lat
                - TranMerc_bp * Math.sin(2.0 * TranMerc_Origin_Lat)
                + TranMerc_cp * Math.sin(4.0 * TranMerc_Origin_Lat)
                - TranMerc_dp * Math.sin(6.0 * TranMerc_Origin_Lat)
                + TranMerc_ep * Math.sin(8.0 * TranMerc_Origin_Lat);

            /*  Origin  */
            tmd = tmdo + (Northing - TranMerc_False_Northing) / TranMerc_Scale_Factor;

            /* First Estimate */
            //sr = SPHSR(0.e0);
            sr = TranMerc_a * (1.e0 - TranMerc_es) /
                Math.pow(Math.sqrt(1.e0 - TranMerc_es * Math.pow(Math.sin(0.e0), 2)), 3);

            ftphi = tmd / sr;

            for (i = 0; i < 5; i++)
            {
                // t10 = SPHTMD (ftphi);
                t10 = TranMerc_ap * ftphi
                    - TranMerc_bp * Math.sin(2.0 * ftphi)
                    + TranMerc_cp * Math.sin(4.0 * ftphi)
                    - TranMerc_dp * Math.sin(6.0 * ftphi)
                    + TranMerc_ep * Math.sin(8.0 * ftphi);
                // sr = SPHSR(ftphi);
                sr = TranMerc_a * (1.e0 - TranMerc_es) /
                    Math.pow(Math.sqrt(1.e0 - TranMerc_es * Math.pow(Math.sin(ftphi), 2)), 3);
                ftphi = ftphi + (tmd - t10) / sr;
            }

            /* Radius of Curvature in the meridian */
            // sr = SPHSR(ftphi);
            sr = TranMerc_a * (1.e0 - TranMerc_es) /
                Math.pow(Math.sqrt(1.e0 - TranMerc_es * Math.pow(Math.sin(ftphi), 2)), 3);

            /* Radius of Curvature in the meridian */
            // sn = SPHSN(ftphi);
            sn = TranMerc_a / Math.sqrt(1.e0 - TranMerc_es * Math.pow(Math.sin(ftphi), 2));

            /* Sine Cosine terms */
            s = Math.sin(ftphi);
            c = Math.cos(ftphi);

            /* Tangent Value  */
            t = Math.tan(ftphi);
            tan2 = t * t;
            tan4 = tan2 * tan2;
            eta = TranMerc_ebs * Math.pow(c, 2);
            eta2 = eta * eta;
            eta3 = eta2 * eta;
            eta4 = eta3 * eta;
            de = Easting - TranMerc_False_Easting;
            if (Math.abs(de) < 0.0001)
                de = 0.0;

            /* Latitude */
            t10 = t / (2.e0 * sr * sn * Math.pow(TranMerc_Scale_Factor, 2));
            t11 = t * (5.e0 + 3.e0 * tan2 + eta - 4.e0 * Math.pow(eta, 2)
                - 9.e0 * tan2 * eta) / (24.e0 * sr * Math.pow(sn, 3)
                * Math.pow(TranMerc_Scale_Factor, 4));
            t12 = t * (61.e0 + 90.e0 * tan2 + 46.e0 * eta + 45.E0 * tan4
                - 252.e0 * tan2 * eta - 3.e0 * eta2 + 100.e0
                * eta3 - 66.e0 * tan2 * eta2 - 90.e0 * tan4
                * eta + 88.e0 * eta4 + 225.e0 * tan4 * eta2
                + 84.e0 * tan2 * eta3 - 192.e0 * tan2 * eta4)
                / (720.e0 * sr * Math.pow(sn, 5) * Math.pow(TranMerc_Scale_Factor, 6));
            t13 = t * (1385.e0 + 3633.e0 * tan2 + 4095.e0 * tan4 + 1575.e0
                * Math.pow(t, 6)) / (40320.e0 * sr * Math.pow(sn, 7) * Math.pow(TranMerc_Scale_Factor, 8));
            Latitude = ftphi - Math.pow(de, 2) * t10 + Math.pow(de, 4) * t11 - Math.pow(de, 6) * t12
                + Math.pow(de, 8) * t13;

            t14 = 1.e0 / (sn * c * TranMerc_Scale_Factor);

            t15 = (1.e0 + 2.e0 * tan2 + eta) / (6.e0 * Math.pow(sn, 3) * c *
                Math.pow(TranMerc_Scale_Factor, 3));

            t16 = (5.e0 + 6.e0 * eta + 28.e0 * tan2 - 3.e0 * eta2
                + 8.e0 * tan2 * eta + 24.e0 * tan4 - 4.e0
                * eta3 + 4.e0 * tan2 * eta2 + 24.e0
                * tan2 * eta3) / (120.e0 * Math.pow(sn, 5) * c
                * Math.pow(TranMerc_Scale_Factor, 5));

            t17 = (61.e0 + 662.e0 * tan2 + 1320.e0 * tan4 + 720.e0
                * Math.pow(t, 6)) / (5040.e0 * Math.pow(sn, 7) * c
                * Math.pow(TranMerc_Scale_Factor, 7));

            /* Difference in Longitude */
            dlam = de * t14 - Math.pow(de, 3) * t15 + Math.pow(de, 5) * t16 - Math.pow(de, 7) * t17;

            /* Longitude */
            Longitude = TranMerc_Origin_Long + dlam;

            if (Math.abs(Latitude) > (90.0 * PI / 180.0))
                Error_Code |= TRANMERC_NORTHING_ERROR;

            if ((Longitude) > (PI))
            {
                Longitude -= (2 * PI);
                if (Math.abs(Longitude) > PI)
                    Error_Code |= TRANMERC_EASTING_ERROR;
            }

            if (Math.abs(dlam) > (9.0 * PI / 180) * Math.cos(Latitude))
            { /* Distortion will result if Longitude is more than 9 degrees from the Central Meridian at the equator */
                /* and decreases to 0 degrees at the poles */
                /* As you move towards the poles, distortion will become more significant */
                Error_Code |= TRANMERC_LON_WARNING;
            }

            if (Latitude > 1.0e10)
                Error_Code |= TRANMERC_LON_WARNING;
        }
        return (Error_Code);
    }

    /** @return Latitude in radians. */
    public double getLatitude()
    {
        return Latitude;
    }

    /** @return Longitude in radians. */
    public double getLongitude()
    {
        return Longitude;
    }
} // end TMConverter class
