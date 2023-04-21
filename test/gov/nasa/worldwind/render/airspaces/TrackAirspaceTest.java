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
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TrackAirspaceTest
{
    @Test
    public void testRestoreState()
    {
        TrackAirspace expected = new TrackAirspace();
        // AbstractAirspace properties
        expected.setVisible(false);
        expected.setHighlighted(true);
        expected.setEnableBatchRendering(false);
        expected.setEnableBatchPicking(false);
        expected.setEnableDepthOffset(true);
        expected.setOutlinePickWidth(31);
        expected.setAlwaysOnTop(true);
        expected.setDrawSurfaceShape(true);
        expected.setEnableLevelOfDetail(false);
        // TrackAirspace properties
        expected.setEnableInnerCaps(false);
        expected.setEnableCenterLine(true);
        expected.setSmallAngleThreshold(Angle.fromDegrees(21));
        expected.addLeg(LatLon.fromDegrees(1, 2), LatLon.fromDegrees(3, 4), 1, 2, 3, 4);
        expected.addLeg(LatLon.fromDegrees(3, 4), LatLon.fromDegrees(5, 6), 5, 6, 7, 8);
        expected.addLeg(LatLon.fromDegrees(5, 6), LatLon.fromDegrees(7, 8), 9, 10, 11, 12);
        expected.addLeg(LatLon.fromDegrees(11, 12), LatLon.fromDegrees(13, 14), 11, 12, 13, 14);
        String stateInXml = expected.getRestorableState();

        TrackAirspace actual = new TrackAirspace();
        actual.restoreState(stateInXml);

        assertTrackAirspaceEquals(expected, actual);
    }

    private static void assertTrackAirspaceEquals(TrackAirspace expected, TrackAirspace actual)
    {
        assertAbstractAirspaceEquals(expected, actual);
        assertEquals(expected.isEnableInnerCaps(), actual.isEnableInnerCaps());
        assertEquals(expected.isEnableCenterLine(), actual.isEnableCenterLine());
        assertEquals(expected.getSmallAngleThreshold(), actual.getSmallAngleThreshold());

        List<Box> expectedLegs = expected.getLegs();
        List<Box> actualLegs = actual.getLegs();
        assertEquals(expectedLegs.size(), actualLegs.size());

        for (int i = 0; i < expectedLegs.size(); i++)
        {
            assertLegEquals(expectedLegs.get(i), actualLegs.get(i));
        }
    }

    private static void assertAbstractAirspaceEquals(AbstractAirspace expected, AbstractAirspace actual)
    {
        assertEquals(expected.isVisible(), actual.isVisible());
        assertEquals(expected.getAttributes(), actual.getAttributes());
        assertEquals(expected.getHighlightAttributes(), actual.getHighlightAttributes());
        assertEquals(expected.isHighlighted(), actual.isHighlighted());
        assertEquals(expected.getAltitudes()[0], actual.getAltitudes()[0], 0.0);
        assertEquals(expected.getAltitudes()[1], actual.getAltitudes()[1], 0.0);
        assertEquals(expected.isTerrainConforming()[0], actual.isTerrainConforming()[0]);
        assertEquals(expected.isTerrainConforming()[1], actual.isTerrainConforming()[1]);
        assertEquals(expected.getAltitudeDatum()[0], actual.getAltitudeDatum()[0]);
        assertEquals(expected.getAltitudeDatum()[1], actual.getAltitudeDatum()[1]);
        assertEquals(expected.getGroundReference(), actual.getGroundReference());
        assertEquals(expected.isEnableBatchRendering(), actual.isEnableBatchRendering());
        assertEquals(expected.isEnableBatchPicking(), actual.isEnableBatchPicking());
        assertEquals(expected.isEnableDepthOffset(), actual.isEnableDepthOffset());
        assertEquals(expected.getOutlinePickWidth(), actual.getOutlinePickWidth());
        assertEquals(expected.isAlwaysOnTop(), actual.isAlwaysOnTop());
        assertEquals(expected.isDrawSurfaceShape(), actual.isDrawSurfaceShape());
        assertEquals(expected.isEnableLevelOfDetail(), actual.isEnableLevelOfDetail());
    }

    private static void assertLegEquals(Box expected, Box actual)
    {
        assertEquals(expected.getLocations()[0], actual.getLocations()[0]);
        assertEquals(expected.getLocations()[1], actual.getLocations()[1]);
        assertEquals(expected.getWidths()[0], actual.getWidths()[0], 0.0);
        assertEquals(expected.getWidths()[1], actual.getWidths()[1], 0.0);
    }
}
