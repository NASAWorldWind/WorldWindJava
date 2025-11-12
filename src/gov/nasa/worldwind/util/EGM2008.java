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

import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.geom.Angle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class EGM2008
{
    public static final int N_ROW_MARKERS = 2; // The beginning and end of each row of latitude data is a flag of some sort
    public static final int N_LONGITUDE_COLS = 8640 + N_ROW_MARKERS; // Number of float32s in a row of data in the data file.
    public static final int N_LATITUDE_ROWS = 4321; // Number of rows.
    public static final double GRID_RESOLUTION = 2.5d / 60d; // 2.5 minute grid
    public static final double CELL_AREA = GRID_RESOLUTION * GRID_RESOLUTION;
    protected static final long CACHE_SIZE = EGM2008.N_LONGITUDE_COLS * 4 * 45 * 15; // Cache 15 degrees worth of offsets.
    public static final int N_LAT_ROW_BYTES = N_LONGITUDE_COLS * 4; // Offsets are float32

    protected String offsetsFilePath;
    protected BufferWrapper deltas;
    protected final BasicMemoryCache offsetCache;

    protected class GridCell
    {
        public double x1;
        public double y1;
        public double x2;
        public double y2;

        public GridCell(double x1, double y1)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x1 + EGM2008.GRID_RESOLUTION;
            this.y2 = y1 + EGM2008.GRID_RESOLUTION;
        }

        public GridCell()
        {
            this(0, 0);
        }

        public GridCell intersect(GridCell that)
        {
            GridCell intersection = new GridCell();
            intersection.x1 = Math.max(this.x1, that.x1);
            intersection.x2 = Math.min(this.x2, that.x2);
            intersection.y1 = Math.max(this.y1, that.y1);
            intersection.y2 = Math.min(this.y2, that.y2);
            return intersection;
        }

        @Override
        public String toString()
        {
            return String.format("%5.2f,%5.2f,%5.2f,%5.2f", x1, y1, x2, y2);
        }

        public double area()
        {
            return (this.x2 - this.x1) * (this.y2 - this.y1);
        }
    }

    /**
     * Allows the retrieval of geoid offsets from the EGM2008 2.5 Minute Interpolation Grid sourced from the
     * National Geospatial-Intelligence Agency Office of Geomatics (https://earth-info.nga.mil/).
     *
     * The EGM2008 data path. This data file is not included in the SDK due to its size. The data may be downloaded here:
     * https://builds.worldwind.arc.nasa.gov/artifactory/EGM2008-Data/egm2008_25.dat
     *
     * @param offsetsFilePath a path pointing to a file with the geoid offsets.
     */
    public EGM2008(String offsetsFilePath)
    {
        if (offsetsFilePath == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        File test = new File(offsetsFilePath);
        if (test.exists())
        {
            this.offsetsFilePath = offsetsFilePath;
        }
        else
        {
            Class c = EGM2008.class;
            URL url = c.getResource("/" + offsetsFilePath);
            if (url != null)
            {
                test = WWIO.getFileForLocalAddress(url);
                this.offsetsFilePath = test.getAbsolutePath();
            }
            else
            {
                this.offsetsFilePath = null;
            }
        }
        this.offsetCache = new BasicMemoryCache((EGM2008.CACHE_SIZE * 8) / 10, EGM2008.CACHE_SIZE);
        this.offsetCache.setName(EGM2008.class.getName());
    }

    public float getOffset(Angle lat, Angle lon) throws IOException
    {
        return this.getOffset((float) lat.degrees, (float) lon.degrees);
    }

    public int getLatRow(double lat)
    {
        // Compute the row in the data file corresponding to a given latitude.
        // Latitude row zero in the data corresponds to 90 degrees latitude (north pole) and increases southward
        // Longitude column zero in the data corresponds to 0 degrees of longitude and increases eastward
        float lat180 = 90f - (float) lat;
        return (int) Math.floor(lat180 / EGM2008.GRID_RESOLUTION);
    }

    public int getLonCol(double lon)
    {
        // Compute the column in the data file corresponding to a given latitude and longitude.
        // Latitude row zero in the data corresponds to 90 degrees latitude (north pole) and increases southward
        // Longitude column zero in the data corresponds to 0 degrees of longitude and increases eastward
        float lon360 = (float) lon;
        if (lon < 0)
        {
            lon360 = lon360 + 360;
        }
        return (int) Math.floor(lon360 / EGM2008.GRID_RESOLUTION);
    }

    public float[][] getLatRows(int latRow) throws IOException
    {
        int[] interpRowIndices =
        {
            latRow, latRow + 1
        };
        float[][] latDataArray = new float[2][];
        boolean retrievalRequired = false;
        for (int i = 0; i < interpRowIndices.length; i++)
        {
            if (interpRowIndices[i] < EGM2008.N_LATITUDE_ROWS)
            {
                float[] latData = (float[]) this.offsetCache.getObject(interpRowIndices[i]);
                latDataArray[i] = latData;
                if (latData == null)
                {
                    retrievalRequired = true;
                }
            }
        }
        if (retrievalRequired)
        {
            try (RandomAccessFile offsetFile = new RandomAccessFile(this.offsetsFilePath, "r"))
            {
                for (int i = 0; i < interpRowIndices.length; i++)
                {
                    if (interpRowIndices[i] < EGM2008.N_LATITUDE_ROWS && latDataArray[i] == null)
                    {
                        offsetFile.seek(interpRowIndices[i] * EGM2008.N_LAT_ROW_BYTES);
                        byte[] latByteData = new byte[EGM2008.N_LAT_ROW_BYTES];
                        offsetFile.read(latByteData);
                        ByteBuffer latByteBuffer = ByteBuffer.wrap(latByteData).order(ByteOrder.LITTLE_ENDIAN);
                        FloatBuffer latFloatBuffer = latByteBuffer.asFloatBuffer();
                        float[] latData = new float[EGM2008.N_LONGITUDE_COLS];
                        latFloatBuffer.get(latData);
                        this.offsetCache.add(interpRowIndices[i], latData, EGM2008.N_LAT_ROW_BYTES);
                        latDataArray[i] = latData;
                    }
                }
            }
        }
        return latDataArray;
    }

    public float getOffset(double lat, double lon) throws IOException
    {
        if (this.offsetsFilePath == null)
        {
            return 0f;
        }
        int latRow = this.getLatRow(lat);
        int lonCol = this.getLonCol(lon);

        float[][] latDataArray = getLatRows(latRow);

        float baseOffset = latDataArray[0][lonCol + EGM2008.N_ROW_MARKERS / 2];
        if (latDataArray[1] == null)
        {
            return baseOffset;
        }

        // Interpolate with surrounding offset cells
        float lat180 = 90f - (float) lat;
        float lon360 = (float) lon;
        if (lon < 0)
        {
            lon360 = lon360 + 360;
        }
        GridCell offsetCell = new GridCell(lon360, lat180);
        double baseLat = ((double) latRow) * EGM2008.GRID_RESOLUTION;
        double baseLon = ((double) lonCol) * EGM2008.GRID_RESOLUTION;
        float interpOffset = 0;
        for (int x = 0; x < 2; x++)
        {
            double cellLon = baseLon + ((double) x) * EGM2008.GRID_RESOLUTION;
            for (int y = 0; y < 2; y++)
            {
                float cellOffset = latDataArray[y][lonCol + EGM2008.N_ROW_MARKERS / 2 + x];
                double cellLat = baseLat + ((double) y) * EGM2008.GRID_RESOLUTION;
                GridCell interpCell = new GridCell(cellLon, cellLat);
                GridCell intersection = offsetCell.intersect(interpCell);
                interpOffset += cellOffset * (intersection.area() / EGM2008.CELL_AREA);
            }
        }
        return interpOffset;
    }

    public boolean isEGMDataAvailable()
    {
        return this.offsetsFilePath != null;
    }
}
