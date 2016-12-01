/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: GetRecordsRequest.java 1540 2013-08-15 22:15:48Z dcollins $
 */
public class GetRecordsRequest
{
    protected int startPosition = 1;
    protected int maxRecords = 10;
    protected String searchText;

    public GetRecordsRequest()
    {
    }

    public GetRecordsRequest(GetRecordsRequest request)
    {
        if (request == null)
        {
            String msg = Logging.getMessage("nullValue.RequestIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.startPosition = request.startPosition;
        this.maxRecords = request.maxRecords;
        this.searchText = request.searchText;
    }

    public int getStartPosition()
    {
        return this.startPosition;
    }

    public void setStartPosition(int startPosition)
    {
        this.startPosition = startPosition;
    }

    public int getMaxRecords()
    {
        return this.maxRecords;
    }

    public void setMaxRecords(int maxRecords)
    {
        this.maxRecords = maxRecords;
    }

    public String getSearchText()
    {
        return this.searchText;
    }

    public void setSearchText(String searchText)
    {
        this.searchText = searchText;
    }

    public String toXMLString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        sb.append("<csw:GetRecords");
        sb.append(" service=\"CSW\"");
        sb.append(" version=\"2.0.2\"");
        sb.append(" resultType=\"results\"");
        sb.append(" outputFormat=\"application/xml\"");
        sb.append(" outputSchema=\"http://www.isotc211.org/2005/gmd\"");
        sb.append(" startPosition=\"").append(startPosition).append("\"");
        sb.append(" maxRecords=\"").append(maxRecords).append("\"");
        sb.append(" xmlns:ogc=\"http://www.opengis.net/ogc\"");
        sb.append(" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"");
        sb.append(" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"");
        sb.append(">");

        sb.append("<csw:Query");
        sb.append(" typeNames=\"gmd:MD_Metadata\"");
        sb.append(">");

        sb.append("<csw:ElementSetName");
        sb.append(" typeNames=\"gmd:MD_Metadata\"");
        sb.append(">");
        sb.append("full");
        sb.append("</csw:ElementSetName>");

        sb.append("<csw:Constraint");
        sb.append(" version=\"1.1.0\"");
        sb.append(">");

        sb.append("<ogc:Filter>");

        if (!WWUtil.isEmpty(this.searchText))
        {
            sb.append("<ogc:And>");
            sb.append("<ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">");
            sb.append("<ogc:PropertyName>csw:AnyText</ogc:PropertyName>");
            sb.append("<ogc:Literal>").append(this.searchText).append("</ogc:Literal>");
            sb.append("</ogc:PropertyIsLike>");
        }

        sb.append("<ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">");
        sb.append("<ogc:PropertyName>csw:AnyText</ogc:PropertyName>");
        sb.append("<ogc:Literal>urn:ogc:serviceType:WebMapService:*</ogc:Literal>");
        sb.append("</ogc:PropertyIsLike>");

        if (!WWUtil.isEmpty(this.searchText))
        {
            sb.append("</ogc:And>");
        }

        sb.append("</ogc:Filter>");

        sb.append("</csw:Constraint>");

        sb.append("</csw:Query>");

        sb.append("</csw:GetRecords>");

        return sb.toString();
    }
}
