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

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.xml.*;

import java.util.Map;

/**
 * The abstract base class for most KML classes. Provides parsing and access to the <i>id</i> and <i>targetId</i> fields
 * of KML elements.
 *
 * @author tag
 * @version $Id: KMLAbstractObject.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractObject extends AbstractXMLEventParser implements MessageListener
{
    public static final String MSG_BOX_CHANGED = "KMLAbstractObject.BoxChanged";
    public static final String MSG_GEOMETRY_CHANGED = "KMLAbstractObject.GeometryChanged";
    public static final String MSG_LINK_CHANGED = "KMLAbstractObject.LinkChanged";
    public static final String MSG_STYLE_CHANGED = "KMLAbstractObject.StyleChanged";
    public static final String MSG_TIME_CHANGED = "KMLAbstractObject.TimeChanged";
    public static final String MSG_VIEW_CHANGED = "KMLAbstractObject.ViewChanged";

    protected KMLAbstractObject()
    {
        super();
    }

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Returns the id of this object, if any.
     *
     * @return the id of this object, or null if it's not specified in the element.
     */
    public String getId()
    {
        return (String) this.getField("id");
    }

    /**
     * Returns the target-id of this object, if any.
     *
     * @return the targetId of this object, or null if it's not specified in the element.
     */
    public String getTargetId()
    {
        return (String) this.getField("targetId");
    }

    @Override
    public KMLRoot getRoot()
    {
        XMLEventParser root = super.getRoot();
        return root instanceof KMLRoot ? (KMLRoot) root : null;
    }

    public void onMessage(Message msg)
    {
        // Empty implementation
    }

    public void onChange(Message msg)
    {
        if (this.getParent() != null)
            ((KMLAbstractObject) this.getParent()).onChange(msg);
    }

    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (sourceValues == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        for (Map.Entry<String, Object> entry : sourceValues.getFields().getEntries())
        {
            this.setField(entry.getKey(), entry.getValue());
        }
    }
}
