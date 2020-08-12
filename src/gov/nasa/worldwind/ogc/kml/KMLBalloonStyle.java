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

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>BalloonStyle</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLBalloonStyle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLBalloonStyle extends KMLAbstractSubStyle
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLBalloonStyle(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Specifies this <code>KMLBalloonStyle</code>'s background color as a hexadecimal string in the form: aabbggrr
     * (deprecated). The <code>color</code> style attribute was Deprecated in KML 2.1 and replaced by
     * <code>bgColor</code>.
     *
     * @return the background color as a hexadecimal string.
     */
    public String getColor()
    {
        return (String) this.getField("color");
    }

    /**
     * Specifies this <code>KMLBalloonStyle</code>'s background color as a hexadecimal string in the form: aabbggrr.
     * This is the preferred attribute for encoding a balloon's background color since KML 2.1.
     *
     * @return the background color as a hexadecimal string.
     */
    public String getBgColor()
    {
        return (String) this.getField("bgColor");
    }

    public String getTextColor()
    {
        return (String) this.getField("textColor");
    }

    /**
     * Get the <i>text</i> field.
     *
     * @return Balloon text field.
     */
    public String getText()
    {
        return (String) this.getField("text");
    }

    public String getDisplayMode()
    {
        return (String) this.getField("displayMode");
    }

    /**
     * Does the style have at least one BalloonStyle field set? This method tests for the existence of the BalloonStyle
     * content fields (text, displayMode, bgColor, etc).
     *
     * @return True if at least one of the BalloonStyle fields is set (text, displayMode, bgColor, etc).
     */
    public boolean hasStyleFields()
    {
        return this.hasField("text")
            || this.hasField("bgColor")
            || this.hasField("textColor")
            || this.hasField("color")
            || this.hasField("displayMode");
    }
}
