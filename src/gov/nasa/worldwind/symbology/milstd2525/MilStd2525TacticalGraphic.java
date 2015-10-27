/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.symbology.TacticalGraphic;

/**
 * Interface to describe tactical graphics defined by <a href="http://www.assistdocs.com/search/document_details.cfm?ident_number=114934">MIL-STD-2525</a>.
 * See the TacticalGraphic <a title="Tactical Graphic Usage Guide" href="http://goworldwind.org/developers-guide/symbology/tactical-graphics/"
 * target="_blank">Usage Guide</a> for instructions on using TacticalGraphic in an application.
 * <p/>
 * The following table lists the modifiers supported by 2525 graphics. Note that not all graphics support all modifiers.
 * <table width="100%"> <tr><th>Field</th><th>Modifier key</th><th>Data type</th><th>Description</th></tr>
 * <tr><td>A</td><td>SymbologyConstants.SYMBOL</td><td>String</td><td>SIDC for a MIL-STD-2525 Tactical Symbol</td></tr>
 * <tr><td>B</td><td>SymbologyConstants.ECHELON</td><td>String</td><td>Echelon</td></tr>
 * <tr><td>C</td><td>SymbologyConstants.QUANTITY</td><td>String</td><td>Quantity</td></tr>
 * <tr><td>H</td><td>SymbologyConstants.ADDITIONAL_INFO</td><td>String</td><td>Additional information</td></tr>
 * <tr><td>Q</td><td>SymbologyConstants.DIRECTION_OF_MOVEMENT</td><td>{@link gov.nasa.worldwind.geom.Angle}</td><td>Direction
 * indicator</td></tr> <tr><td>T</td><td>SymbologyConstants.UNIQUE_DESIGNATION</td><td>String</td><td>Unique
 * designation</td></tr> <tr><td>V</td><td>SymbologyConstants.TYPE</td><td>String</td><td>Type</td></tr>
 * <tr><td>W</td><td>SymbologyConstants.DATE_TIME_GROUP</td><td>String</td><td>Date/time</td></tr>
 * <tr><td>X</td><td>SymbologyConstants.ALTITUDE_DEPTH</td><td>Double</td><td>Altitude/depth</td></tr>
 * <tr><td>AM</td><td>SymbologyConstants.DISTANCE</td><td>Double</td><td>Radius, length or width of rectangle.</td></tr>
 * <tr><td>AN</td><td>SymbologyConstants.AZIMUTH</td><td>Angle</td><td>Azimuth</td></tr> </table>
 * <p/>
 * Here's an example of setting modifiers during construction of a graphic:
 * <pre>
 * AVList modifiers = new AVListImpl();
 * modifiers.setValue(SymbologyConstants.UNIQUE_DESIGNATION, "X469"); // Field T
 * modifiers.setValue(SymbologyConstants.DATE_TIME_GROUP, "10095900ZJAN92); // Field W
 * modifiers.setValue(SymbologyConstants.ADDITIONAL_INFO, "Anthrax Suspected"); // Field H
 * modifiers.setValue(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(30.0)); // Field Q
 *
 * Position position = Position.fromDegrees(35.1026, -118.348, 0);
 *
 * // Create the graphic with the modifier list
 * TacticalGraphic graphic = factory.createGraphic("GHMPNEB----AUSX", positions, modifiers);
 * </pre>
 * <p/>
 * Some graphics support multiple instances of a modifier. For example, 2525 uses the field code W for a date/time
 * modifier. Some graphics support multiple timestamps, in which case the fields are labeled W, W1, W2, etc. An
 * application can pass an {@link Iterable} to <code>setModifier</code> if multiple values are required to specify the
 * modifier. Here's an example of how to specify two timestamps:
 * <pre>
 * String startDate = ...
 * String endData = ...
 *
 * graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList(startDate, endDate));
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: MilStd2525TacticalGraphic.java 555 2012-04-25 18:59:29Z pabercrombie $
 */
public interface MilStd2525TacticalGraphic extends TacticalGraphic
{
    /**
     * Indicates the current value of graphic's Status/Operational Condition field.
     *
     * @return this graphic's Status/Operational Condition field.
     *
     * @see #setStatus(String)
     */
    String getStatus();

    /**
     * Specifies this graphic's Status/Operational Condition field. A graphic's Status defines whether the represented
     * object exists at the time the symbol was generated, or is anticipated to exist in the future. Additionally, a
     * graphic's Status can define its operational condition. The recognized values depend on the graphic's scheme:
     * <p/>
     * <strong>Tactical graphics</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_SUSPECTED</li> <li>STATUS_PRESENT</li> <li>STATUS_KNOWN</li> </ul>
     * <p/>
     * <strong>Meteorological and Oceanographic</strong>
     * <p/>
     * <ul> <li>Not supported</li> </ul>
     * <p/>
     * <strong>Emergency Management</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_PRESENT</li> </ul>
     *
     * @param value the new value for the Status/Operational Condition field.
     *
     * @throws IllegalArgumentException if the specified value is <code>null</code> or is not one of the accepted status
     *                                  values.
     */
    void setStatus(String value);
}
