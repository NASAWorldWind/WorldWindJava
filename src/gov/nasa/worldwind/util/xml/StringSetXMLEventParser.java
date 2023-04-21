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

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses a list of XML strings.
 *
 * @author tag
 * @version $Id: StringSetXMLEventParser.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class StringSetXMLEventParser extends AbstractXMLEventParser implements Iterable<String>
{
    protected QName elementName;
    protected Set<String> strings = new HashSet<String>();

    public StringSetXMLEventParser()
    {
    }

    public StringSetXMLEventParser(String namespaceUri)
    {
        super(namespaceUri);
    }

    /**
     * Create a parser. All sub-elements of a specified name are parsed as strings and retained.
     *
     * @param namespaceURI the namespace URI to attach to this parser. May be null.
     * @param elementName  the name of the sub-elements that contain the strings.
     */
    public StringSetXMLEventParser(String namespaceURI, QName elementName)
    {
        super(namespaceURI);

        this.elementName = elementName;
    }

    @Override
    public XMLEventParser newInstance() throws Exception
    {
        StringSetXMLEventParser copy = (StringSetXMLEventParser) super.newInstance();
        if (copy != null)
            copy.elementName = this.elementName;

        return copy;
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent listEvent, Object... args) throws XMLStreamException
    {
        this.strings.clear();

        return super.parse(ctx, listEvent, args);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, this.elementName))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.addString(s);
        }
    }

    public Iterator<String> iterator()
    {
        return this.strings.iterator();
    }

    public Set<String> getStrings()
    {
        return this.strings;
    }

    protected void addString(String string)
    {
        this.strings.add(string);
    }
}
