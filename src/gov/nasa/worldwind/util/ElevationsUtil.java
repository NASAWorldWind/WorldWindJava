/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.ByteBufferRaster;

/**
* @author Lado Garakanidze
* @version $Id: ElevationsUtil.java 1171 2013-02-11 21:45:02Z dcollins $
*/

public class ElevationsUtil
{
   public static final double DTED_DEFAULT_MISSING_SIGNAL = -32767d;
   public static final double SRTM_DEFAULT_MISSING_SIGNAL = -32768d;
   public static final double DEM_DEFAULT_MISSING_SIGNAL = -9999d;

   protected static final double[] knownMissingSignals = {
       DTED_DEFAULT_MISSING_SIGNAL, SRTM_DEFAULT_MISSING_SIGNAL, DEM_DEFAULT_MISSING_SIGNAL
   };

   /**
    * Checks if the <code>value</code> is one of the well-known "nodata" value used in digital elevation model files
    * to specify missing areas / voids.
    *
    * @param value a value to check
    * @return <code>TRUE</code>, if the value is one of the well known "nodata" values
    *
    */
   public static boolean isKnownMissingSignal(Double value)
   {
       if( null != value )
       {
           for(double signal : knownMissingSignals )
           {
               if( value == signal )
                   return true;
           }
       }
       return false;
   }

   /**
    * Rectify elevation raster. For best performance each elevation raster must have correct parameters and values set.
    * The <code>rectify()</code> operation validates that correct Elevation min and max values are set or calculated.
    * All values that beyond min/max and voids, must be marked with "Missing Signal" (aka "nodata" value).
    *
    * @param raster A DataRaster to rectify
    * @throws IllegalArgumentException if <code>raster</code> is <code>null</code>
    */
   public static void rectify(ByteBufferRaster raster) throws IllegalArgumentException
   {
       if( null == raster )
       {
           String msg = Logging.getMessage("nullValue.RasterIsNull");
           Logging.logger().finest(msg);
           throw new IllegalArgumentException(msg);
       }

       int width = raster.getWidth();
       int height = raster.getHeight();

       if( width == 0 || height == 0 )
       {
           // nothing to do
           return;
       }

       double[] minmax= raster.getExtremes();
       if( null == minmax )
       {
           // nothing to do
           return;
       }

       Double minValue = minmax[0];
       Double maxValue = minmax[1];

       Double missingDataSignal = AVListImpl.getDoubleValue(raster, AVKey.MISSING_DATA_SIGNAL, null);

       // check if the minimum value is one of the well known NODATA values
       if (ElevationsUtil.isKnownMissingSignal(minValue)
           || (missingDataSignal != null && missingDataSignal.equals(minValue))
           )
       {
           missingDataSignal = minValue;
           raster.setTransparentValue(missingDataSignal);

           minmax = raster.getExtremes();
           if (null != minmax)
           {
               minValue = minmax[0];
               maxValue = minmax[1];
           }
       }

       BufferWrapper bufferWrapper = raster.getBuffer();
       // Allocate a buffer to hold one row of scalar values.
       double[] array = new double[width];

       boolean needsConversion = false;
       double conversionValue = 1d;

       if( raster.hasKey(AVKey.ELEVATION_UNIT) )
       {
           String unit = raster.getStringValue(AVKey.ELEVATION_UNIT);
           if( AVKey.UNIT_METER.equalsIgnoreCase(unit) )
           {
               needsConversion = false;
           }
           else if( AVKey.UNIT_FOOT.equalsIgnoreCase(unit) )
           {
               needsConversion = true;
               conversionValue = WWMath.convertFeetToMeters(1);
               minValue = WWMath.convertFeetToMeters(minValue);
               maxValue = WWMath.convertFeetToMeters(maxValue);
               raster.setValue(AVKey.ELEVATION_UNIT, AVKey.UNIT_METER);
           }
           else
           {
               needsConversion = false;
               String msg = Logging.getMessage("generic.UnrecognizedElevationUnit", unit);
               Logging.logger().warning(msg);
           }
       }

       boolean rasterHasVoids = false;

       for (int j = 0; j < height; j++)
       {
           bufferWrapper.getDouble( j * width, array, 0, width );
           boolean commitChanges = false;

           for (int i = 0; i < width; i++)
           {
               double value = array[i];

               if( null != missingDataSignal && value == missingDataSignal )
               {
                   rasterHasVoids = true;
               }
               else
               {
                   if( needsConversion )
                   {
                       value *= conversionValue;
                       commitChanges = true;
                       array[i] = value;
                   }

                   if( value < minValue || value > maxValue )
                   {
                       rasterHasVoids = true;

                       if( null != missingDataSignal)
                       {
                           array[i] = missingDataSignal;
                           commitChanges = true;
                       }
                   }
               }
           }

           if( commitChanges )
               bufferWrapper.putDouble( j * width, array, 0, width );
       }

       if( rasterHasVoids )
       {
           if( missingDataSignal != null )
               raster.setValue(AVKey.MISSING_DATA_SIGNAL, missingDataSignal );
       }
       else
       {
           raster.removeKey(AVKey.MISSING_DATA_SIGNAL);
       }

       raster.setValue(AVKey.ELEVATION_MIN, minValue);
       raster.setValue(AVKey.ELEVATION_MAX, maxValue);
   }
}