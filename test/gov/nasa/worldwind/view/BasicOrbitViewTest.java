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
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.view.orbit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BasicOrbitViewTest
{
    //////////////////////////////////////////////////////////
    // Persistence Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testRestore_NewInstance()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        assignExampleValues(orbitView);

        String stateInXml = orbitView.getRestorableState();
        orbitView = new BasicOrbitView();
        orbitView.restoreState(stateInXml);

        BasicOrbitView expected = new BasicOrbitView();
        assignExampleValues(expected);
        assertOrbitViewEquals(expected, orbitView);
    }

    @Test
    public void testRestore_SameInstance()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        assignExampleValues(orbitView);

        String stateInXml = orbitView.getRestorableState();
        assignNullValues(orbitView);
        orbitView.restoreState(stateInXml);

        BasicOrbitView expected = new BasicOrbitView();
        assignExampleValues(expected);
        assertOrbitViewEquals(expected, orbitView);
    }

    @Test
    public void testRestore_EmptyStateDocument()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        assignExampleValues(orbitView);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        orbitView.restoreState(emptyStateInXml);

        // No attributes should have changed.
        BasicOrbitView expected = new BasicOrbitView();
        assignExampleValues(expected);
        assertOrbitViewEquals(expected, orbitView);
    }

    @Test
    public void testRestore_InvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            OrbitView orbitView = new BasicOrbitView();
            orbitView.restoreState(badStateInXml);
            fail("Expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testRestore_PartialStateDocument()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        assignNullValues(orbitView);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"fieldOfView\">10.5</stateObject>" +
                "<stateObject name=\"zoom\">1000.5</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        orbitView.restoreState(partialStateInXml);

        BasicOrbitView expected = new BasicOrbitView();
        assignNullValues(expected);
        expected.setFieldOfView(Angle.fromDegrees(10.5));
        expected.setZoom(1000.5);
        assertOrbitViewEquals(expected, orbitView);
    }

    @Test
    public void testLegacyStateDocument()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        assignNullValues(orbitView);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"orbitViewLimits\">" +
                "    <stateObject name=\"minHeadingDegrees\">-1.0</stateObject>" +
                "    <stateObject name=\"maxHeadingDegrees\">1.0</stateObject>" +
                "    <stateObject name=\"minPitchDegrees\">-40.0</stateObject>" +
                "    <stateObject name=\"maxPitchDegrees\">-20.0</stateObject>" +
                "    <stateObject name=\"centerLocationLimits\">" +
                "        <stateObject name=\"minLatitudeDegrees\">-10.0</stateObject>" +
                "        <stateObject name=\"maxLatitudeDegrees\">10.0</stateObject>" +
                "        <stateObject name=\"minLongitudeDegrees\">30.0</stateObject>" +
                "        <stateObject name=\"maxLongitudeDegrees\">50.0</stateObject>" +
                "    </stateObject>" +
                "    <stateObject name=\"minCenterElevation\">-10.0</stateObject>" +
                "    <stateObject name=\"maxCenterElevation\">100.0</stateObject>" +
                "    <stateObject name=\"minZoom\">1000.0</stateObject>" +
                "    <stateObject name=\"maxZoom\">2000.0</stateObject>" +
                "</stateObject>" +
                "</restorableState>";
        orbitView.restoreState(partialStateInXml);

        BasicOrbitView expected = new BasicOrbitView();
        assignNullValues(expected);
        OrbitViewLimits limits = new BasicOrbitViewLimits();
        limits.setCenterLocationLimits(Sector.fromDegrees(-10, 10, 30, 50));
        limits.setCenterElevationLimits(-10, 100);
        limits.setHeadingLimits(Angle.fromDegrees(-1), Angle.fromDegrees(1));
        limits.setPitchLimits(Angle.fromDegrees(-40), Angle.fromDegrees(-20));
        limits.setZoomLimits(1000, 2000);
        expected.setOrbitViewLimits(limits);

        assertOrbitViewEquals(expected, orbitView);
    }

    @Test
    public void testRestore_OldVersionStateDocument()
    {
        BasicOrbitView orbitView = new BasicOrbitView();
        String stateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"center\">" +
                "<stateObject name=\"latitude\">-20.5</stateObject>" +
                "<stateObject name=\"longitude\">30.5</stateObject>" +
                "<stateObject name=\"elevation\">100.5</stateObject>" +
                "</stateObject>" +
                "<stateObject name=\"heading\">-40.5</stateObject>" +
                "<stateObject name=\"pitch\">50.5</stateObject>" +
                "<stateObject name=\"zoom\">1000.5</stateObject>" +
                "<stateObject name=\"fieldOfView\">10.5</stateObject>" +
                "</restorableState>";
        orbitView.restoreState(stateInXml);

        BasicOrbitView expected = new BasicOrbitView();
        expected.setCenterPosition(Position.fromDegrees(-20.5, 30.5, 100.5));
        expected.setHeading(Angle.fromDegrees(-40.5));
        expected.setPitch(Angle.fromDegrees(50.5));
        expected.setZoom(1000.5);
        expected.setFieldOfView(Angle.fromDegrees(10.5));

        assertOrbitViewEquals(expected, orbitView);
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    private static void assignExampleValues(OrbitView orbitView)
    {
        OrbitViewLimits limits = new BasicOrbitViewLimits();
        limits.setCenterLocationLimits(Sector.fromDegrees(-10, 10, 30, 50));
        limits.setCenterElevationLimits(-10, 100);
        limits.setHeadingLimits(Angle.fromDegrees(-10), Angle.fromDegrees(10));
        limits.setPitchLimits(Angle.fromDegrees(20), Angle.fromDegrees(40));
        limits.setZoomLimits(1000, 2000);
        orbitView.setOrbitViewLimits(limits);

        orbitView.setCenterPosition(Position.fromDegrees(-5.5, 30.5, 90.5));
        orbitView.setHeading(Angle.fromDegrees(-1.5));
        orbitView.setPitch(Angle.fromDegrees(30.5));
        orbitView.setZoom(1000.5);
        orbitView.setFieldOfView(Angle.fromDegrees(10.5));

        orbitView.setDetectCollisions(false);
    }

    private static void assignNullValues(OrbitView orbitView)
    {
        orbitView.setCenterPosition(Position.fromDegrees(0.0, 0.0, 0.0));
        orbitView.setHeading(Angle.fromDegrees(0.0));
        orbitView.setPitch(Angle.fromDegrees(0.0));
        orbitView.setZoom(0.0);
        orbitView.setFieldOfView(Angle.fromDegrees(0.0));

        orbitView.setDetectCollisions(true);

        orbitView.setOrbitViewLimits(new BasicOrbitViewLimits());
    }

    private static void assertOrbitViewEquals(OrbitView expected, OrbitView actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Actual is null", actual);

        assertEquals("center", expected.getCenterPosition(), actual.getCenterPosition());
        assertEquals("heading", expected.getHeading(), actual.getHeading());
        assertEquals("pitch", expected.getPitch(), actual.getPitch());
        assertEquals("zoom", expected.getZoom(), actual.getZoom(), 0.0);
        assertEquals("fieldOfView", expected.getFieldOfView(), actual.getFieldOfView());

        assertEquals("detectCollisions", expected.isDetectCollisions(), actual.isDetectCollisions());
        assertEquals("nearClipDistance", expected.getNearClipDistance(), actual.getNearClipDistance(), 0.0);
        assertEquals("farClipDistance", expected.getFarClipDistance(), actual.getFarClipDistance(), 0.0);

        assertOrbitViewLimitsEquals(expected.getOrbitViewLimits(), actual.getOrbitViewLimits());
    }

    private static void assertOrbitViewLimitsEquals(OrbitViewLimits expected, OrbitViewLimits actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Actual is null", actual);

        assertEquals("centerLocationLimits", expected.getCenterLocationLimits(), actual.getCenterLocationLimits());

        for (int i = 0; i < 2; i++)
        {
            assertEquals("centerElevationLimits[" + i + "]", expected.getCenterElevationLimits()[i],
                actual.getCenterElevationLimits()[i], 0.0);
            assertEquals("headingLimits[" + i + "]", expected.getHeadingLimits()[i], actual.getHeadingLimits()[i]);
            assertEquals("pitchLimits[" + i + "]", expected.getPitchLimits()[i], actual.getPitchLimits()[i]);
            assertEquals("zoomLimits[" + i + "]", expected.getZoomLimits()[i], actual.getZoomLimits()[i], 0.0);
        }
    }
}
