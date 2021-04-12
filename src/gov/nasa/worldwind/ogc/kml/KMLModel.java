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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

/**
 * Represents the KML <i>Model</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLModel extends KMLAbstractGeometry implements KMLMutable
{
    private static final String LOCATION_KEY="Location";
    private static final String SCALE_KEY="Scale";
    
    /** Flag to indicate that the link has been fetched from the hash map. */
    protected boolean linkFetched = false;
    protected KMLLink link;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLModel(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }
    
    public void setLocation(KMLLocation loc) {
        this.setField(LOCATION_KEY, loc);
    }

    public KMLLocation getLocation()
    {
        return (KMLLocation) this.getField(LOCATION_KEY);
    }

    public KMLOrientation getOrientation()
    {
        return (KMLOrientation) this.getField("Orientation");
    }
    
    public void setScale(KMLScale scale) {
        this.setField(SCALE_KEY, scale);
    }
    
    public KMLScale getScale()
    {
        return (KMLScale) this.getField(SCALE_KEY);
    }

    public KMLLink getLink()
    {
        if (!this.linkFetched)
        {
            this.link = (KMLLink) this.getField("Link");
            this.linkFetched = true;
        }
        return this.link;
    }

    public KMLResourceMap getResourceMap()
    {
        return (KMLResourceMap) this.getField("ResourceMap");
    }

    @Override
    public void setPosition(Position position) {
        KMLLocation loc = this.getLocation();
        if (loc == null) {
            loc = new KMLLocation(this.getNamespaceURI());
            this.setLocation(loc);
        }
        loc.setPosition(position);
    }

    @Override
    public Position getPosition() {
        KMLLocation loc = this.getLocation();
        if (loc != null) {
            return loc.getPosition();
        }

        return null;
    }

    @Override
    public void setScale(Vec4 scale) {
        KMLScale curScale = this.getScale();
        if (curScale == null) {
            curScale = new KMLScale(this.getNamespaceURI());
            setScale(curScale);
        }
        curScale.setScale(scale);
    }
}
