/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.util.WWXML;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: Record.java 1584 2013-09-05 23:39:15Z dcollins $
 */
public class Record
{
    protected String title;
    // Use LinkedHashSet to eliminate duplicates and preserve insertion order
    protected Collection<OnlineResource> wmsOnlineResources = new LinkedHashSet<OnlineResource>();
    protected OnlineResource currentResource;
    protected LinkedList<String> nameStack = new LinkedList<String>();

    public Record()
    {
    }

    public Record(XMLEventReader reader) throws XMLStreamException
    {
        this.parseElement(reader);
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Collection<OnlineResource> getWmsOnlineResources()
    {
        return this.wmsOnlineResources;
    }

    public void setWmsOnlineResources(Collection<OnlineResource> wmsOnlineResources)
    {
        this.wmsOnlineResources = wmsOnlineResources;
    }

    protected void parseElement(XMLEventReader reader) throws XMLStreamException
    {
        while (reader.hasNext())
        {
            XMLEvent nextEvent = reader.peek();

            if (nextEvent.isStartElement())
            {
                StartElement startElement = nextEvent.asStartElement();
                String localName = startElement.getName().getLocalPart();
                this.nameStack.addLast(localName);

                if (localName.equals("title") && this.nameStack.contains("identificationInfo"))
                {
                    this.title = WWXML.readCharacters(reader).trim();
                }
                else if (localName.equals("CI_OnlineResource") && this.nameStack.contains("distributionInfo"))
                {
                    OnlineResource resource = new OnlineResource(reader);
                    if (resource.isWMSOnlineResource())
                        this.wmsOnlineResources.add(resource);
                }
                else
                {
                    reader.nextEvent(); // consume the event
                }
            }
            else if (nextEvent.isEndElement())
            {
                this.nameStack.removeLast();
                if (this.nameStack.size() > 0)
                {
                    reader.nextEvent(); // consume the event
                }
                else
                {
                    break; // stop parsing at the end element corresponding to the root start element
                }
            }
            else
            {
                reader.nextEvent(); // consume the event
            }
        }
    }
}
