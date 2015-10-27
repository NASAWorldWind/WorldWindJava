/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
