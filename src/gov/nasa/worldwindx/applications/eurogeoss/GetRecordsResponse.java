/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GetRecordsResponse.java 1584 2013-09-05 23:39:15Z dcollins $
 */
public class GetRecordsResponse
{
    protected int numberOfRecordsMatched;
    protected int numberOfRecordsReturned;
    protected int nextRecord;
    protected Collection<Record> records = new ArrayList<Record>();

    public GetRecordsResponse()
    {
    }

    public GetRecordsResponse(XMLEventReader reader) throws XMLStreamException
    {
        this.parseElement(reader);
    }

    public int getNumberOfRecordsMatched()
    {
        return this.numberOfRecordsMatched;
    }

    public void setNumberOfRecordsMatched(int numberOfRecordsMatched)
    {
        this.numberOfRecordsMatched = numberOfRecordsMatched;
    }

    public int getNumberOfRecordsReturned()
    {
        return this.numberOfRecordsReturned;
    }

    public void setNumberOfRecordsReturned(int numberOfRecordsReturned)
    {
        this.numberOfRecordsReturned = numberOfRecordsReturned;
    }

    public int getNextRecord()
    {
        return this.nextRecord;
    }

    public void setNextRecord(int nextRecord)
    {
        this.nextRecord = nextRecord;
    }

    public Collection<Record> getRecords()
    {
        return this.records;
    }

    public void setRecords(Collection<Record> records)
    {
        this.records = records;
    }

    protected void parseElement(XMLEventReader reader) throws XMLStreamException
    {
        int depth = 0;

        while (reader.hasNext())
        {
            XMLEvent nextEvent = reader.peek();

            if (nextEvent.isStartElement())
            {
                StartElement startElement = nextEvent.asStartElement();
                String localName = startElement.getName().getLocalPart();
                ++depth;

                if (localName.equals("SearchResults"))
                {
                    Iterator attributes = startElement.getAttributes();

                    while (attributes.hasNext())
                    {
                        Attribute attr = (Attribute) attributes.next();
                        String attrName = attr.getName().getLocalPart();

                        if (attrName.equals("numberOfRecordsMatched"))
                        {
                            this.numberOfRecordsMatched = WWUtil.convertStringToInteger(attr.getValue());
                        }
                        else if (attrName.equals("numberOfRecordsReturned"))
                        {
                            this.numberOfRecordsReturned = WWUtil.convertStringToInteger(attr.getValue());
                        }
                        else if (attrName.equals("nextRecord"))
                        {
                            this.nextRecord = WWUtil.convertStringToInteger(attr.getValue());
                        }
                    }

                    reader.nextEvent(); // consume the event
                }
                else if (localName.equals(("MD_Metadata")))
                {
                    this.records.add(new Record(reader));
                }
                else
                {
                    reader.nextEvent(); // consume the event
                }
            }
            else if (nextEvent.isEndElement())
            {
                if (--depth > 0)
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
