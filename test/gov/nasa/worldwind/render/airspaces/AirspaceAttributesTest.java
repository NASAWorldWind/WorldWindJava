/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.RestorableSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class AirspaceAttributesTest
{
    @Test
    public void testDefaultConstructor()
    {
        String stateInXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<restorableState>"
            + "<stateObject name=\"drawInterior\">true</stateObject>"
            + "<stateObject name=\"drawOutline\">false</stateObject>"
            + "<stateObject name=\"enableAntialiasing\">false</stateObject>"
            + "<stateObject name=\"enableLighting\">true</stateObject>"
            + "<stateObject name=\"interiorMaterial\">"
            + "<stateObject name=\"ambient\">0X4C4C4CFF</stateObject>"
            + "<stateObject name=\"diffuse\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"outlineMaterial\">"
            + "<stateObject name=\"ambient\">0X000000FF</stateObject>"
            + "<stateObject name=\"diffuse\">0X000000FF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"interiorOpacity\">1.0</stateObject>"
            + "<stateObject name=\"outlineOpacity\">1.0</stateObject>"
            + "<stateObject name=\"outlineWidth\">1.0</stateObject>"
            + "</restorableState>";
        RestorableSupport rs = RestorableSupport.parse(stateInXml);
        AirspaceAttributes expected = new BasicAirspaceAttributes();
        expected.restoreState(rs, null);

        AirspaceAttributes actual = new BasicAirspaceAttributes();

        assertEquals(expected, actual);
    }

    @Test
    public void testOtherConstructor()
    {
        String stateInXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<restorableState>"
            + "<stateObject name=\"drawInterior\">true</stateObject>"
            + "<stateObject name=\"drawOutline\">false</stateObject>"
            + "<stateObject name=\"enableAntialiasing\">false</stateObject>"
            + "<stateObject name=\"enableLighting\">true</stateObject>"
            + "<stateObject name=\"interiorMaterial\">"
            + "<stateObject name=\"ambient\">0X4C0000FF</stateObject>"
            + "<stateObject name=\"diffuse\">0XFF0000FF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"outlineMaterial\">"
            + "<stateObject name=\"ambient\">0X000000FF</stateObject>"
            + "<stateObject name=\"diffuse\">0X000000FF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"interiorOpacity\">0.5</stateObject>"
            + "<stateObject name=\"outlineOpacity\">1.0</stateObject>"
            + "<stateObject name=\"outlineWidth\">1.0</stateObject>"
            + "</restorableState>";
        RestorableSupport rs = RestorableSupport.parse(stateInXml);
        AirspaceAttributes expected = new BasicAirspaceAttributes();
        expected.restoreState(rs, null);

        AirspaceAttributes actual = new BasicAirspaceAttributes(Material.RED, 0.5);

        assertEquals(expected, actual);
    }

    @Test
    public void testPropertyAccessors()
    {
        AirspaceAttributes attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(Material.RED);
        attrs.setOutlineMaterial(Material.GREEN);
        attrs.setInteriorOpacity(0.5);
        attrs.setOutlineOpacity(0.75);
        attrs.setOutlineWidth(10.0);

        assertEquals(attrs.getInteriorMaterial(), Material.RED);
        assertEquals(attrs.getOutlineMaterial(), Material.GREEN);
        assertEquals(attrs.getInteriorOpacity(), 0.5, 0.0);
        assertEquals(attrs.getOutlineOpacity(), 0.75, 0.0);
        assertEquals(attrs.getOutlineWidth(), 10.0, 0.0);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedPropertyAccessors()
    {
        // Set and get using the deprecated property accessors.
        AirspaceAttributes attrs = new BasicAirspaceAttributes();
        attrs.setMaterial(Material.RED);
        attrs.setOutlineMaterial(Material.GREEN);
        attrs.setOpacity(0.5);
        attrs.setOutlineOpacity(0.75);
        attrs.setOutlineWidth(10.0);

        assertEquals(attrs.getMaterial(), Material.RED);
        assertEquals(attrs.getOutlineMaterial(), Material.GREEN);
        assertEquals(attrs.getOpacity(), 0.5, 0.0);
        assertEquals(attrs.getOutlineOpacity(), 0.75, 0.0);
        assertEquals(attrs.getOutlineWidth(), 10.0, 0.0);

        // Set using the deprecated property accessors, get using the current property accessors.
        attrs = new BasicAirspaceAttributes();
        attrs.setMaterial(Material.RED);
        attrs.setOutlineMaterial(Material.GREEN);
        attrs.setOpacity(0.5);
        attrs.setOutlineOpacity(0.75);
        attrs.setOutlineWidth(10.0);

        assertEquals(attrs.getInteriorMaterial(), Material.RED);
        assertEquals(attrs.getOutlineMaterial(), Material.GREEN);
        assertEquals(attrs.getInteriorOpacity(), 0.5, 0.0);
        assertEquals(attrs.getOutlineOpacity(), 0.75, 0.0);
        assertEquals(attrs.getOutlineWidth(), 10.0, 0.0);

        // Set using the current property accessors, get using the deprecated property accessors.
        attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(Material.RED);
        attrs.setOutlineMaterial(Material.GREEN);
        attrs.setInteriorOpacity(0.5);
        attrs.setOutlineOpacity(0.75);
        attrs.setOutlineWidth(10.0);

        assertEquals(attrs.getMaterial(), Material.RED);
        assertEquals(attrs.getOutlineMaterial(), Material.GREEN);
        assertEquals(attrs.getOpacity(), 0.5, 0.0);
        assertEquals(attrs.getOutlineOpacity(), 0.75, 0.0);
        assertEquals(attrs.getOutlineWidth(), 10.0, 0.0);
    }

    @Test
    public void testRestoreState()
    {
        AirspaceAttributes expected = new BasicAirspaceAttributes();
        expected.setDrawInterior(false);
        expected.setDrawOutline(true);
        expected.setInteriorMaterial(Material.RED);
        expected.setOutlineMaterial(Material.GREEN);
        expected.setInteriorOpacity(0.5);
        expected.setOutlineOpacity(0.75);
        expected.setOutlineWidth(10.0);

        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        expected.getRestorableState(rs, rs.addStateObject("attributes"));
        String stateInXml = rs.getStateAsXml();

        rs = RestorableSupport.parse(stateInXml);
        AirspaceAttributes actual = new BasicAirspaceAttributes();
        actual.restoreState(rs, rs.getStateObject("attributes"));

        assertEquals(expected, actual);
    }

    @Test
    public void testRestoreDeprecatedState()
    {
        AirspaceAttributes expected = new BasicAirspaceAttributes();
        expected.setDrawInterior(false);
        expected.setDrawOutline(true);
        expected.setInteriorMaterial(Material.RED);
        expected.setOutlineMaterial(Material.GREEN);
        expected.setInteriorOpacity(0.5);
        expected.setOutlineOpacity(0.75);
        expected.setOutlineWidth(10.0);

        String stateInXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<restorableState>"
            + "<stateObject name=\"drawInterior\">false</stateObject>"
            + "<stateObject name=\"drawOutline\">true</stateObject>"
            + "<stateObject name=\"material\">"
            + "<stateObject name=\"ambient\">0X4C0000FF</stateObject>"
            + "<stateObject name=\"diffuse\">0XFF0000FF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"outlineMaterial\">"
            + "<stateObject name=\"ambient\">0X4C00FF</stateObject>"
            + "<stateObject name=\"diffuse\">0XFF00FF</stateObject>"
            + "<stateObject name=\"specular\">0XFFFFFFFF</stateObject>"
            + "<stateObject name=\"emission\">0X0000FF</stateObject>"
            + "<stateObject name=\"shininess\">80.0</stateObject>"
            + "</stateObject>"
            + "<stateObject name=\"opacity\">0.5</stateObject>"
            + "<stateObject name=\"outlineOpacity\">0.75</stateObject>"
            + "<stateObject name=\"outlineWidth\">10.0</stateObject>"
            + "</restorableState>";
        RestorableSupport rs = RestorableSupport.parse(stateInXml);
        AirspaceAttributes actual = new BasicAirspaceAttributes();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }
}
