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

package gov.nasa.worldwindx.examples.kml;

import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.gx.GXConstants;

import javax.xml.stream.*;
import java.io.*;

/**
 * Utility class to create KML documents.
 *
 * @author pabercrombie
 * @version $Id: KMLDocumentBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLDocumentBuilder
{
    protected XMLStreamWriter writer;

    /**
     * Create a KML document using a Writer.
     *
     * @param writer Writer to receive KML output.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    public KMLDocumentBuilder(Writer writer) throws XMLStreamException
    {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        this.startDocument();
    }

    /**
     * Create a KML document using an OutputStream.
     *
     * @param stream Stream to receive KML output.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    public KMLDocumentBuilder(OutputStream stream) throws XMLStreamException
    {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        this.startDocument();
    }

    /**
     * Start the KML document and write namespace declarations.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    protected void startDocument() throws XMLStreamException
    {
        this.writer.writeStartDocument();
        this.writer.writeStartElement("kml");
        this.writer.writeDefaultNamespace(KMLConstants.KML_NAMESPACE);
        this.writer.setPrefix("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeNamespace("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeStartElement("Document");
    }

    /**
     * End the KML document.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    protected void endDocument() throws XMLStreamException
    {
        this.writer.writeEndElement(); // Document
        this.writer.writeEndElement(); // kml
        this.writer.writeEndDocument();

        this.writer.close();
    }

    /**
     * Close the document builder.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    public void close() throws XMLStreamException
    {
        this.endDocument();
        this.writer.close();
    }

    /**
     * Write an {@link Exportable} object to the document. If the object does not support export in KML format, it will
     * be ignored.
     *
     * @param exportable Object to export in KML.
     *
     * @throws IOException If an error is encountered while writing KML.
     */
    public void writeObject(Exportable exportable) throws IOException
    {
        String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
        if (Exportable.FORMAT_SUPPORTED.equals(supported) || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported))
        {
            exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
        }
    }

    /**
     * Write a list of {@link Exportable} objects to the document. If any objects do not support export in KML format,
     * they will be ignored.
     *
     * @param exportables List of objects to export in KML.
     *
     * @throws IOException If an error is encountered while writing KML.
     */
    public void writeObjects(Exportable... exportables) throws IOException
    {
        for (Exportable exportable : exportables)
        {
            String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
            if (Exportable.FORMAT_SUPPORTED.equals(supported)
                || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported))
            {
                exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
            }
        }
    }
}
