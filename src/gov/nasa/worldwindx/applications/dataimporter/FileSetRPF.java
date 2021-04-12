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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.rpf.RPFDataSeries;

/**
 * A file set specific to RPF data.
 *
 * @author tag
 * @version $Id: FileSetRPF.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetRPF extends FileSet
{
    FileSetRPF(String rpfSuffixCode)
    {
        this.assignRPFMetadata(rpfSuffixCode);
    }

    public void assignRPFMetadata(String rpfSuffixCode)
    {
        RPFDataSeries series = RPFDataSeries.dataSeriesFor(rpfSuffixCode);
        if (series != null)
        {
            this.setValue(FileSet.FILE_SET_CODE, series.seriesCode);
            this.setValue(FileSet.FILE_SET_ABBREVIATION, series.seriesAbbreviation);
            this.setValue(FileSet.FILE_SET_SCALE, series.scaleOrResolution);
            this.setValue(FileSet.FILE_SET_GSD, series.scaleOrGSD);
            this.setValue(AVKey.DATASET_NAME, series.dataSeries);
            this.setValue(AVKey.DATASET_TYPE, series.rpfDataType);
            this.setValue(AVKey.DISPLAY_NAME, series.dataSeries);
        }
    }
}
