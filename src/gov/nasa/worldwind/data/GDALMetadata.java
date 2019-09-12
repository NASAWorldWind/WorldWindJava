/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;
import org.gdal.gdal.Dataset;
import org.gdal.osr.SpatialReference;

import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $
 */

public class GDALMetadata
{
    protected static final String NITF_ONAME = "NITF_ONAME";
    protected static final String NITF_ISORCE = "NITF_ISORCE";
    protected static final String NITF_IREP = "NITF_IREP";
    protected static final String NITF_ABPP = "NITF_ABPP";
    protected static final String NITF_FBKGC = "NITF_FBKGC";
    protected static final String NITF_DYNAMIC_RANGE = "NITF_USE00A_DYNAMIC_RANGE";

    protected GDALMetadata()
    {
    }

    public static AVList extractExtendedAndFormatSpecificMetadata(Dataset ds, AVList extParams, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == extParams)
        {
            extParams = new AVListImpl();
        }

        try
        {
            Hashtable dict = ds.GetMetadata_Dict("");
            if (null != dict)
            {
                Enumeration keys = dict.keys();
                while (keys.hasMoreElements())
                {
                    Object o = keys.nextElement();
                    if (null != o && o instanceof String)
                    {
                        String key = (String) o;
                        Object value = dict.get(key);
                        if (!WWUtil.isEmpty(value))
                        {
                            extParams.setValue(key, value);
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.FINE, t.getMessage(), t);
        }

        return mapExtendedMetadata(ds, extParams, params);
    }

    static protected AVList mapExtendedMetadata(Dataset ds, AVList extParams, AVList params)
    {
        params = (null == params) ? new AVListImpl() : params;

        if (null == extParams)
        {
            return params;
        }

        convertToWorldWind(extParams, params);

        String drvName = (null != ds) ? ds.GetDriver().getShortName() : "";

        if ("NITF".equals(drvName))
        {
            mapNITFMetadata(extParams, params);
        }

        return params;
    }

    protected static void mapNITFMetadata(AVList extParams, AVList params)
    {
        if (extParams.hasKey(NITF_ONAME))
        {
            // values: GeoEye, DigitalGlobe
        }

        if (extParams.hasKey(NITF_ISORCE))
        {
            // values: GEOEYE1,DigitalGlobe
        }

        if (extParams.hasKey(NITF_IREP))
        {
            // values: RGB/LUT/MONO/MULTI
        }

        // Extract Actual Bit-Per-Pixel
        if (extParams.hasKey(NITF_ABPP))
        {
            Object o = extParams.getValue(NITF_ABPP);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                Integer abpp = WWUtil.convertStringToInteger((String) o);
                if (null != abpp)
                    params.setValue(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL, abpp);
            }
        }

        if (extParams.hasKey(NITF_DYNAMIC_RANGE))
        {
            Object o = extParams.getValue(NITF_DYNAMIC_RANGE);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                Double maxPixelValue = WWUtil.convertStringToDouble((String) o);
                if (null != maxPixelValue)
                    params.setValue(AVKey.RASTER_BAND_MAX_PIXEL_VALUE, maxPixelValue);
            }
        }

        if (extParams.hasKey(NITF_FBKGC))
        {
            Object o = extParams.getValue(NITF_FBKGC);
            if (!WWUtil.isEmpty(o) && o instanceof String)
            {
                try
                {
                    String[] s = ((String) o).split(",");
                    if (null != s)
                    {
//                        if( s.length == 3 )
//                        {
//                            Color bgc = new Color( Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), 0xFF );
//                            long color = 0xFFFFFFFFL & bgc.getRGB();
//                            params.setValue(AVKey.MISSING_DATA_SIGNAL, (double)color );
//                        }
//                        else if( s.length == 1 )
//                        {
//                            int color = Integer.parseInt(s[0]);
//                            params.setValue(AVKey.MISSING_DATA_SIGNAL, (double)color );
//                        }
                    }
                }
                catch (Exception e)
                {
                    String msg = Logging.getMessage("generic.CannotCreateColor", o);
                    Logging.logger().severe(msg);
                }
            }
        }
    }

    public static AVList convertToWorldWind(AVList extParams, AVList destParams)
    {
        if (null == destParams)
        {
            destParams = new AVListImpl();
        }

        if (null == extParams)
        {
            return destParams;
        }

        String proj = null, zone = null, ellps = null, datum = null, units = null;
        Integer epsg = null;

        if (extParams.hasKey("GEOTIFF_CHAR__ProjectedCSTypeGeoKey"))
        {
            proj = extParams.getStringValue("GEOTIFF_CHAR__ProjectedCSTypeGeoKey");
            proj = (null != proj) ? proj.toUpperCase() : null;

            int idx = (null != proj) ? proj.indexOf("ZONE_") : -1;
            if (idx != -1)
            {
                zone = proj.substring(idx + 5, proj.length());
                zone = (null != zone) ? zone.toUpperCase() : null;
            }
        }

        if (null == proj && extParams.hasKey("IMG__PROJECTION_NAME"))
        {
            proj = extParams.getStringValue("IMG__PROJECTION_NAME");
            proj = (null != proj) ? proj.toUpperCase() : null;
        }

        if (null == zone && extParams.hasKey("IMG__PROJECTION_ZONE"))
        {
            zone = extParams.getStringValue("IMG__PROJECTION_ZONE");
            zone = (null != zone) ? zone.toUpperCase() : null;
        }

        if (null != proj && proj.contains("UTM"))
        {
            destParams.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_PROJECTED);
            destParams.setValue(AVKey.PROJECTION_NAME, AVKey.PROJECTION_UTM);

            if (null != zone)
            {
                if (zone.endsWith("N"))
                {
                    destParams.setValue(AVKey.PROJECTION_HEMISPHERE, AVKey.NORTH);
                    zone = zone.substring(0, zone.length() - 1);
                }
                else if (zone.endsWith("S"))
                {
                    destParams.setValue(AVKey.PROJECTION_HEMISPHERE, AVKey.SOUTH);
                    zone = zone.substring(0, zone.length() - 1);
                }

                Integer i = WWUtil.makeInteger(zone.trim());
                if (i != null && i >= 1 && i <= 60)
                {
                    destParams.setValue(AVKey.PROJECTION_ZONE, i);
                }
            }
        }

        if (extParams.hasKey("IMG__SPHEROID_NAME"))
        {
            String s = extParams.getStringValue("IMG__SPHEROID_NAME");
            if (s != null)
            {
                s = s.toUpperCase();
                if (s.contains("WGS") && s.contains("84"))
                {
                    ellps = datum = "WGS84";
                    destParams.setValue(AVKey.PROJECTION_DATUM, datum);
                }
            }
        }

        if (extParams.hasKey("IMG__HORIZONTAL_UNITS"))
        {
            String s = extParams.getStringValue("IMG__HORIZONTAL_UNITS");
            if (s != null)
            {
                s = s.toLowerCase();
                if (s.contains("meter") || s.contains("metre"))
                {
                    units = AVKey.UNIT_METER;
                }
                if (s.contains("feet") || s.contains("foot"))
                {
                    units = AVKey.UNIT_FOOT;
                }

                if (null != units)
                {
                    destParams.setValue(AVKey.PROJECTION_UNITS, units);
                }
            }
        }

        if (extParams.hasKey("GEOTIFF_NUM__3072__ProjectedCSTypeGeoKey"))
        {
            String s = extParams.getStringValue("GEOTIFF_NUM__3072__ProjectedCSTypeGeoKey");
            if (s != null)
            {
                epsg = WWUtil.makeInteger(s.trim());
            }
        }

        if (null == epsg && extParams.hasKey("GEO__ProjectedCSTypeGeoKey"))
        {
            String s = extParams.getStringValue("GEO__ProjectedCSTypeGeoKey");
            if (s != null)
            {
                epsg = WWUtil.makeInteger(s.trim());
            }
        }

        if (null != epsg)
        {
            destParams.setValue(AVKey.PROJECTION_EPSG_CODE, epsg);
        }

        StringBuffer proj4 = new StringBuffer();

        if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(destParams.getValue(AVKey.COORDINATE_SYSTEM)))
        {
            //        +proj=utm +zone=38 +ellps=WGS84 +datum=WGS84 +units=m

            if (AVKey.PROJECTION_UTM.equals(destParams.getValue(AVKey.PROJECTION_NAME)))
            {
                proj4.append("+proj=utm");
            }

            if (destParams.hasKey(AVKey.PROJECTION_ZONE))
            {
                proj4.append(" +zone=").append(destParams.getValue(AVKey.PROJECTION_ZONE));
            }

            if (destParams.hasKey(AVKey.PROJECTION_DATUM))
            {
                proj4.append(" +ellps=").append(destParams.getValue(AVKey.PROJECTION_DATUM));
                proj4.append(" +datum=").append(destParams.getValue(AVKey.PROJECTION_DATUM));
            }

            if (destParams.hasKey(AVKey.PROJECTION_UNITS))
            {
                proj4.append(" +units=").append(
                    AVKey.UNIT_METER.equals(destParams.getValue(AVKey.PROJECTION_UNITS)) ? "m" : "f"
                );
            }

            try
            {
                SpatialReference srs = new SpatialReference();
                srs.ImportFromProj4(proj4.toString());
                destParams.setValue(AVKey.SPATIAL_REFERENCE_WKT, srs.ExportToWkt());
            }
            catch (Throwable t)
            {
                Logging.logger().log(java.util.logging.Level.FINEST, t.getMessage(), t);
            }
        }

        return destParams;
    }
}
