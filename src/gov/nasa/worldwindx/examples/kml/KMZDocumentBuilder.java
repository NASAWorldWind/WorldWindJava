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
import gov.nasa.worldwind.util.Logging;

import javax.xml.stream.*;
import java.io.*;
import java.util.zip.*;

/**
 * Utility class to create KMZ documents. The builder creates a KMZ archive with one KML file inside the archive.
 * Objects that support export in KML format can be written to the KML file.
 *
 * @author pabercrombie
 * @version $Id: KMZDocumentBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMZDocumentBuilder
{
    protected ZipOutputStream zipStream;
    protected XMLStreamWriter writer;

    /**
     * Create a KMZ document using an OutputStream.
     *
     * @param stream Stream to receive KMZ output.
     *
     * @throws XMLStreamException If an exception is encountered while writing KML.
     * @throws IOException        If an exception occurs writing to the output stream.
     */
    public KMZDocumentBuilder(OutputStream stream) throws XMLStreamException, IOException
    {
        if (stream == null)
        {
            String message = Logging.getMessage("nullValue.OutputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zipStream = new ZipOutputStream(stream);
        this.startDocument();
    }

    /**
     * Get the name of the KML file inside of the KMZ archive.
     *
     * @return The name of the KML file inside of the KMZ.
     */
    protected String getMainFileName()
    {
        return "doc.kml";
    }

    /**
     * Start the KMZ document and write namespace declarations.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     * @throws IOException        If an exception occurs writing to the output stream.
     */
    protected void startDocument() throws XMLStreamException, IOException
    {
        // Create a zip entry for the main KML file
        this.zipStream.putNextEntry(new ZipEntry(this.getMainFileName()));

        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(this.zipStream);

        this.writer.writeStartDocument();
        this.writer.writeStartElement("kml");
        this.writer.writeDefaultNamespace(KMLConstants.KML_NAMESPACE);
        this.writer.setPrefix("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeNamespace("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeStartElement("Document");
    }

    /**
     * End the KMZ document.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     * @throws IOException        If an exception occurs writing to the output stream.
     */
    protected void endDocument() throws XMLStreamException, IOException
    {
        this.writer.writeEndElement(); // Document
        this.writer.writeEndElement(); // kml
        this.writer.writeEndDocument();

        this.writer.close();

        this.zipStream.closeEntry();
        this.zipStream.finish();
    }

    /**
     * Close the document builder.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     * @throws IOException        If an exception occurs closing the output stream.
     */
    public void close() throws XMLStreamException, IOException
    {
        this.endDocument();
    }

    /**
     * Write an {@link gov.nasa.worldwind.Exportable} object to the document. If the object does not support export in
     * KML format, it will be ignored.
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
            exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
        }
    }
}
