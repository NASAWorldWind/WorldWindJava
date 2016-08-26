/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import gov.nasa.worldwind.terrain.WCSElevationModel;
import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.xml.stream.XMLStreamException;
import java.io.File;

/**
 * @author tag
 * @version $Id: WCSElevationModelCreationTest.java 2137 2014-07-10 01:05:52Z pabercrombie $
 */
@RunWith(Enclosed.class)
public class WCSElevationModelCreationTest
{
    public static class Tests extends TestCase
    {
        public void test001()
        {
            WCSElevationModel elevationModel = this.createWCSElevationModel(
                new WCS100Capabilities("testData/WCS/WCSCapabilities003.xml"),
                new WCS100DescribeCoverage("testData/WCS/WCSDescribeCoverage001.xml"));

            assertEquals("Incorrect number of levels", 5, elevationModel.getLevels().getNumLevels());
            double bestResolution = elevationModel.getBestResolution(Sector.FULL_SPHERE) * 180.0 / Math.PI;
            assertTrue("Incorrect best resolution", bestResolution > 0.0083 && bestResolution < 0.0084);

            assertEquals("Min elevation incorrect", -11000.0, elevationModel.getMinElevation());
            assertEquals("Max elevation incorrect", 8850.0, elevationModel.getMaxElevation());

            assertEquals("Incorrect dataset name", "WW:NASA_SRTM30_900m_Tiled",
                elevationModel.getLevels().getFirstLevel().getDataset());
            assertEquals("Incorrect format suffix", ".tif",
                elevationModel.getLevels().getFirstLevel().getFormatSuffix());
            assertEquals("Incorrect cache name",
                "worldwind26.arc.nasa.gov" + File.separator + "_wms2" + File.separator + "WW_NASA_SRTM30_900m_Tiled",
                elevationModel.getLevels().getFirstLevel().getCacheName());
        }

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

            assertEquals("Min elevation incorrect",
                origElevationModel.getMinElevation(),
                newElevationModel.getMinElevation());
            assertEquals("Max elevation incorrect",
                origElevationModel.getMaxElevation(),
                newElevationModel.getMaxElevation());

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

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
