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

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.ogc.OGCServiceInformation;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses a WMS Service element.
 *
 * @author tag
 * @version $Id: WMSServiceInformation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSServiceInformation extends OGCServiceInformation
{
    protected QName MAX_WIDTH;
    protected QName MAX_HEIGHT;
    protected QName LAYER_LIMIT;

    protected int maxWidth;
    protected int maxHeight;
    protected int layerLimit;

    public WMSServiceInformation(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        MAX_WIDTH = new QName(this.getNamespaceURI(), "MaxWidth");
        MAX_HEIGHT = new QName(this.getNamespaceURI(), "MaxHeight");
        LAYER_LIMIT = new QName(this.getNamespaceURI(), "LayerLimit");
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, MAX_WIDTH))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.maxWidth = d.intValue();
        }
        else if (ctx.isStartElement(event, MAX_HEIGHT))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.maxHeight = d.intValue();
        }
        else if (ctx.isStartElement(event, LAYER_LIMIT))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.layerLimit = d.intValue();
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    protected void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    protected void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());

        sb.append("Max width = ").append(this.getMaxWidth());
        sb.append(" Max height = ").append(this.getMaxHeight()).append("\n");

        return sb.toString();
    }
}
