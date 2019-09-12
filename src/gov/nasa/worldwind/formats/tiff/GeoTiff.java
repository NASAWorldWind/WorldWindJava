/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

/**
 * @author Lado Garakanidze
 * @version $Id: GeoTiff.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class GeoTiff
{
    public static final int Undefined   = 0;
    public static final int UserDefined = 32767;
    
    // Geotiff extension tags
    public interface Tag
    {
        public static final int MODEL_PIXELSCALE        = 33550;
        public static final int MODEL_TIEPOINT          = 33922;
        public static final int MODEL_TRANSFORMATION    = 34264;
        public static final int GEO_KEY_DIRECTORY       = 34735;
        public static final int GEO_DOUBLE_PARAMS       = 34736;
        public static final int GEO_ASCII_PARAMS        = 34737;

        public static final int GDAL_NODATA             = 42113;
    }


    public interface GeoKeyHeader
    {
        public static final int KeyDirectoryVersion = 1;
        public static final int KeyRevision = 1;
        public static final int MinorRevision = 0;
        
    }

    public interface GeoKey
    {
        public static final int ModelType           = 1024; // see GeoTiff.ModelType values
        public static final int RasterType          = 1025; // see GeoTiff.RasterType values

        public static final int GeographicType      = 2048; // see GeoTiff.GCS for values or Section 6.3.2.1 Codes
        // GeoKey Requirements for User-Defined geographic CS:
        public static final int GeogCitation            = 2049; // ASCII
        public static final int GeogGeodeticDatum       = 2050; // SHORT, See section 6.3.2.2 Geodetic Datum Codes
        public static final int GeogPrimeMeridian       = 2051; // SHORT, Section 6.3.2.4 Codes
        public static final int GeogLinearUnits         = 2052; // Double, See GeoTiff.Unit.Liner or Section 6.3.1.3 Codes
        public static final int GeogLinearUnitSize      = 2053; // Double, meters
        public static final int GeogAngularUnits        = 2054; // Short, See GeoTiff.Units.Angular or Section 6.3.1.4 Codes
        public static final int GeogAngularUnitSize     = 2055; // Double, radians
        public static final int GeogEllipsoid           = 2056; // Short, See Section 6.3.2.3 Codes 
        public static final int GeogAzimuthUnits        = 2060; // Short, Section 6.3.1.4 Codes
        public static final int GeogPrimeMeridianLong   = 2061; // DOUBLE, See GeoTiff.Units.Angular

        // 6.2.3 Projected CS Parameter Keys
        public static final int ProjectedCSType             = 3072; /*  Section 6.3.3.1 codes */
        public static final int PCSCitation                 = 3073; /*  documentation */
        public static final int Projection                  = 3074; /*  Section 6.3.3.2 codes */
        public static final int ProjCoordTrans              = 3075; /*  Section 6.3.3.3 codes */
        public static final int ProjLinearUnits             = 3076; /*  Section 6.3.1.3 codes */
        public static final int ProjLinearUnitSize          = 3077; /*  meters */
        public static final int ProjStdParallel1            = 3078; /*  GeogAngularUnit */
        public static final int ProjStdParallel2            = 3079; /*  GeogAngularUnit */
        public static final int ProjNatOriginLong           = 3080; /*  GeogAngularUnit */
        public static final int ProjNatOriginLat            = 3081; /*  GeogAngularUnit */
        public static final int ProjFalseEasting            = 3082; /*  ProjLinearUnits */
        public static final int ProjFalseNorthing           = 3083; /*  ProjLinearUnits */
        public static final int ProjFalseOriginLong         = 3084; /*  GeogAngularUnit */
        public static final int ProjFalseOriginLat          = 3085; /*  GeogAngularUnit */
        public static final int ProjFalseOriginEasting      = 3086; /*  ProjLinearUnits */
        public static final int ProjFalseOriginNorthing     = 3087; /*  ProjLinearUnits */
        public static final int ProjCenterLong              = 3088; /*  GeogAngularUnit */
        public static final int ProjCenterLat               = 3089; /*  GeogAngularUnit */
        public static final int ProjCenterEasting           = 3090; /*  ProjLinearUnits */
        public static final int ProjCenterNorthing          = 3091; /*  ProjLinearUnits */
        public static final int ProjScaleAtNatOrigin        = 3092; /*  ratio */
        public static final int ProjScaleAtCenter           = 3093; /*  ratio */
        public static final int ProjAzimuthAngle            = 3094; /*  GeogAzimuthUnit */
        public static final int ProjStraightVertPoleLong    = 3095; /*  GeogAngularUnit */
        // Aliases:
        public static final int ProjStdParallel = ProjStdParallel1;
        public static final int ProjOriginLong = ProjNatOriginLong;
        public static final int ProjOriginLat = ProjNatOriginLat;
        public static final int ProjScaleAtOrigin = ProjScaleAtNatOrigin;

        // 6.2.4 Vertical CS Keys
        public static final int VerticalCSType    = 4096; /* Section 6.3.4.1 codes */
        public static final int VerticalCitation  = 4097; /* ASCII */
        public static final int VerticalDatum     = 4098; /* Section 6.3.4.2 codes */
        public static final int VerticalUnits     = 4099; /* Section 6.3.1.3 codes */
    }

    public interface ModelType
    {
        public static final int Undefined   =     0;
        public static final int Projected   =     1;
        public static final int Geographic  =     2;
        public static final int Geocentric  =     3;
        public static final int UserDefined = 32767;

        public static final int DEFAULT = Geographic;
    }

    public interface RasterType
    {
        public static final int Undefined           =     0; // highly not recomended to use
        public static final int RasterPixelIsArea   =     1;
        public static final int RasterPixelIsPoint  =     2;
        public static final int UserDefined         = 32767; // highly not recomended to use
    }


    public interface Unit
    {
        public static final int Undefined   =     0;
        public static final int UserDefined = 32767;

        //6.3.1.3 Linear Units Codes
        public interface Linear
        {
            public static final int Meter                       = 9001;
            public static final int Foot                        = 9002;
            public static final int Foot_US_Survey              = 9003;
            public static final int Foot_Modified_American      = 9004;
            public static final int Foot_Clarke                 = 9005;
            public static final int Foot_Indian                 = 9006;
            public static final int Link                        = 9007;
            public static final int Link_Benoit                 = 9008;
            public static final int Link_Sears                  = 9009;
            public static final int Chain_Benoit                = 9010;
            public static final int Chain_Sears                 = 9011;
            public static final int Yard_Sears                  = 9012;
            public static final int Yard_Indian                 = 9013;
            public static final int Fathom                      = 9014;
            public static final int Mile_International_Nautical = 9015;
        }

        // 6.3.1.4 Angular Units Codes
        // These codes shall be used for any key that requires specification of an angular unit of measurement.
        public interface Angular
        {
            public static final int Angular_Radian              = 9101;
            public static final int Angular_Degree              = 9102;
            public static final int Angular_Arc_Minute          = 9103;
            public static final int Angular_Arc_Second          = 9104;
            public static final int Angular_Grad                = 9105;
            public static final int Angular_Gon                 = 9106;
            public static final int Angular_DMS                 = 9107;
            public static final int Angular_DMS_Hemisphere      = 9108;
        }
    }

    // Geogrphic Coordinate System (GCS)
    public interface GCS
    {
        public static final int Undefined   = 0;
        public static final int UserDefined = 32767;

        public static final int NAD_83      = 4269;
        public static final int WGS_72      = 4322;
        public static final int WGS_72BE    = 4324;
        public static final int WGS_84      = 4326;

        public static final int DEFAULT = WGS_84;
    }

    // Geogrphic Coordinate System Ellipsoid (GCSE)
    public interface GCSE
    {
        public static final int WGS_84 = 4030;
    }

    // Projected Coordinate System (PCS)
    public interface PCS
    {
        public static final int Undefined   =     0;
        public static final int UserDefined = 32767;
    }

    public enum ProjectedCS
    {
        Undefined( 0, 0, "Undefined");

        private String name;
        private int    epsg;
        private int    datum;

        private ProjectedCS(int epsg, int datum, String name )
        {
            this.name = name;
            this.epsg = epsg;
            this.datum = datum;
        }

        public int getEPSG()
        {
            return this.epsg;
        }

        public int getDatum()
        {
            return this.datum;
        }

        public String getName()
        {
            return this.name;
        }
    }



    // Vertical Coordinate System (VCS) 
    public interface VCS
    {
        // [ 1, 4999] = Reserved
        // [ 5000, 5099] = EPSG Ellipsoid Vertical CS Codes
        // [ 5100, 5199] = EPSG Orthometric Vertical CS Codes
        // [ 5200, 5999] = Reserved EPSG
        // [ 6000, 32766] = Reserved
        // [32768, 65535] = Private User Implementations
        
        public static final int Undefined   =     0;
        public static final int UserDefined = 32767;

        public static final int Airy_1830_ellipsoid = 5001;
        public static final int Airy_Modified_1849_ellipsoid = 5002;
        public static final int ANS_ellipsoid = 5003;
        public static final int Bessel_1841_ellipsoid = 5004;
        public static final int Bessel_Modified_ellipsoid = 5005;
        public static final int Bessel_Namibia_ellipsoid = 5006;
        public static final int Clarke_1858_ellipsoid = 5007;
        public static final int Clarke_1866_ellipsoid = 5008;
        public static final int Clarke_1880_Benoit_ellipsoid = 5010;
        public static final int Clarke_1880_IGN_ellipsoid = 5011;
        public static final int Clarke_1880_RGS_ellipsoid = 5012;
        public static final int Clarke_1880_Arc_ellipsoid = 5013;
        public static final int Clarke_1880_SGA_1922_ellipsoid = 5014;
        public static final int Everest_1830_1937_Adjustment_ellipsoid = 5015;
        public static final int Everest_1830_1967_Definition_ellipsoid = 5016;
        public static final int Everest_1830_1975_Definition_ellipsoid = 5017;
        public static final int Everest_1830_Modified_ellipsoid = 5018;
        public static final int GRS_1980_ellipsoid = 5019;
        public static final int Helmert_1906_ellipsoid = 5020;
        public static final int INS_ellipsoid = 5021;
        public static final int International_1924_ellipsoid = 5022;
        public static final int International_1967_ellipsoid = 5023;
        public static final int Krassowsky_1940_ellipsoid = 5024;
        public static final int NWL_9D_ellipsoid = 5025;
        public static final int NWL_10D_ellipsoid = 5026;
        public static final int Plessis_1817_ellipsoid = 5027;
        public static final int Struve_1860_ellipsoid = 5028;
        public static final int War_Office_ellipsoid = 5029;
        public static final int WGS_84_ellipsoid = 5030;
        public static final int GEM_10C_ellipsoid = 5031;
        public static final int OSU86F_ellipsoid = 5032;
        public static final int OSU91A_ellipsoid = 5033;
        // Orthometric Vertical CS;
        public static final int Newlyn = 5101;
        public static final int North_American_Vertical_Datum_1929 = 5102;
        public static final int North_American_Vertical_Datum_1988 = 5103;
        public static final int Yellow_Sea_1956 = 5104;
        public static final int Baltic_Sea = 5105;
        public static final int Caspian_Sea = 5106;
            
        public static final int DEFAULT = Undefined;
    }
}

