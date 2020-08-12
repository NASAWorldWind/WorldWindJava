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

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import gov.nasa.worldwind.terrain.WCSElevationModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.io.File;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WCSElevationModelCreationTest
{
    @Test
    public void test001()
    {
        WCSElevationModel elevationModel = this.createWCSElevationModel(
            new WCS100Capabilities("testData/WCS/WCSCapabilities003.xml"),
            new WCS100DescribeCoverage("testData/WCS/WCSDescribeCoverage001.xml"));

        assertEquals("Incorrect number of levels", 5, elevationModel.getLevels().getNumLevels());
        double bestResolution = elevationModel.getBestResolution(Sector.FULL_SPHERE) * 180.0 / Math.PI;
        assertTrue("Incorrect best resolution", bestResolution > 0.0083 && bestResolution < 0.0084);

        assertEquals("Min elevation incorrect", -11000.0, elevationModel.getMinElevation(), 0.0);
        assertEquals("Max elevation incorrect", 8850.0, elevationModel.getMaxElevation(), 0.0);

        assertEquals("Incorrect dataset name", "WW:NASA_SRTM30_900m_Tiled",
            elevationModel.getLevels().getFirstLevel().getDataset());
        assertEquals("Incorrect format suffix", ".tif",
            elevationModel.getLevels().getFirstLevel().getFormatSuffix());
        assertEquals("Incorrect cache name",
            "worldwind26.arc.nasa.gov" + File.separator + "_wms2" + File.separator + "WW_NASA_SRTM30_900m_Tiled",
            elevationModel.getLevels().getFirstLevel().getCacheName());
    }

    @Test
    public void testRestoreState()
    {
        WCSElevationModel origElevationModel = this.createWCSElevationModel(
            new WCS100Capabilities("testData/WCS/WCSCapabilities003.xml"),
            new WCS100DescribeCoverage("testData/WCS/WCSDescribeCoverage001.xml"));

        String restorableState = origElevationModel.getRestorableState();

        WCSElevationModel newElevationModel = new WCSElevationModel(restorableState);

        assertEquals("Incorrect number of levels",
            origElevationModel.getLevels().getNumLevels(),
            newElevationModel.getLevels().getNumLevels());

        double epsilon = 0.0001;
        double origBestResolution = origElevationModel.getBestResolution(Sector.FULL_SPHERE);
        double newBestResolution = newElevationModel.getBestResolution(Sector.FULL_SPHERE);
        assertTrue("Incorrect best resolution", Math.abs(origBestResolution - newBestResolution) < epsilon);

        assertEquals("Min elevation incorrect", origElevationModel.getMinElevation(),
            newElevationModel.getMinElevation(), 0.0);
        assertEquals("Max elevation incorrect", origElevationModel.getMaxElevation(),
            newElevationModel.getMaxElevation(), 0.0);

        assertEquals("Incorrect dataset name",
            origElevationModel.getLevels().getFirstLevel().getDataset(),
            newElevationModel.getLevels().getFirstLevel().getDataset());
        assertEquals("Incorrect format suffix",
            origElevationModel.getLevels().getFirstLevel().getFormatSuffix(),
            newElevationModel.getLevels().getFirstLevel().getFormatSuffix());
        assertEquals("Incorrect cache name",
            origElevationModel.getLevels().getFirstLevel().getCacheName(),
            newElevationModel.getLevels().getFirstLevel().getCacheName());
    }

    private WCSElevationModel createWCSElevationModel(WCS100Capabilities caps, WCS100DescribeCoverage coverage)
    {
        try
        {
            caps.parse();
            coverage.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        AVList params = new AVListImpl();
        params.setValue(AVKey.DOCUMENT, coverage);
        return new WCSElevationModel(caps, params);
    }
}
