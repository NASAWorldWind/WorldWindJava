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

import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.util.Logging;

import java.util.Map;

/**
 * Represents the KML <i>Style</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLStyle.java 1528 2013-07-31 01:00:32Z pabercrombie $
 */
public class KMLStyle extends KMLAbstractStyleSelector
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLStyle(String namespaceURI)
    {
        super(namespaceURI);
    }

    public KMLIconStyle getIconStyle()
    {
        return (KMLIconStyle) this.getField(KMLConstants.ICON_STYLE_FIELD);
    }

    public KMLLabelStyle getLabelStyle()
    {
        return (KMLLabelStyle) this.getField(KMLConstants.LABEL_STYLE_FIELD);
    }

    public KMLLineStyle getLineStyle()
    {
        return (KMLLineStyle) this.getField(KMLConstants.LINE_STYLE_FIELD);
    }

    public KMLPolyStyle getPolyStyle()
    {
        return (KMLPolyStyle) this.getField(KMLConstants.POLY_STYLE_FIELD);
    }

    public KMLBalloonStyle getBaloonStyle()
    {
        return (KMLBalloonStyle) this.getField(KMLConstants.BALOON_STYLE_FIELD);
    }

    public KMLListStyle getListStyle()
    {
        return (KMLListStyle) this.getField(KMLConstants.LIST_STYLE_FIELD);
    }

    /**
     * {@inheritDoc} Overridden to handle deprecated {@code labelColor} field. The {@code labelColor} field is
     * deprecated, and has been replaced by {@code LabelStyle}. If {@code labelColor} is set this method will apply the
     * color to the {@code LabelStyle}, creating a new {@code LabelStyle} if necessary.
     */
    @Override
    public void setField(String keyName, Object value)
    {
        if ("labelColor".equals(keyName))
        {
            KMLLabelStyle labelStyle = this.getLabelStyle();
            if (labelStyle == null)
            {
                labelStyle = new KMLLabelStyle(this.getNamespaceURI());
                this.setField(KMLConstants.LABEL_STYLE_FIELD, labelStyle);
            }
            labelStyle.setField("color", value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    /**
     * Adds the sub-style fields of a specified sub-style to this one's fields if they don't already exist.
     *
     * @param subStyle the sub-style to merge with this one.
     *
     * @return the substyle passed in as the parameter.
     *
     * @throws IllegalArgumentException if the sub-style parameter is null.
     */
    public KMLAbstractSubStyle mergeSubStyle(KMLAbstractSubStyle subStyle)
    {
        if (subStyle == null)
        {
            String message = Logging.getMessage("nullValue.SymbolIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.hasFields())
            return subStyle;

        Class subStyleClass = subStyle.getClass();
        for (Map.Entry<String, Object> field : this.getFields().getEntries())
        {
            if (field.getValue() != null && field.getValue().getClass().equals(subStyleClass))
            {
                this.overrideFields(subStyle, (KMLAbstractSubStyle) field.getValue());
            }
        }

        return subStyle;
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLStyle))
        {
            String message = Logging.getMessage("KML.InvalidElementType", sourceValues.getClass().getName());
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        super.applyChange(sourceValues);

        this.onChange(new Message(KMLAbstractObject.MSG_STYLE_CHANGED, this));
    }
}
