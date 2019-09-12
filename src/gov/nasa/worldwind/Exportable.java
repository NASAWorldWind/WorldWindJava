/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import java.io.IOException;

/**
 * Exportable marks an object that can be exported in different data formats. Implementing classes may support one or
 * more export formats. Formats are identified by MIME type. Call {@link #isExportFormatSupported(String)} to determine
 * if an object supports export in a certain format.
 * <p>
 * Example of use:
 * <pre>
 * // Export a PointPlacemark in KML format
 * PointPlacemark placemark;
 * StringWriter kml = new StringWriter();
 * placemark.export(KMLConstants.KML_MIME_TYPE, kml);
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: Exportable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Exportable
{
    /**
     * Returned by {@link #isExportFormatSupported(String)} if the object does support export to the given format.
     */
    public static final String FORMAT_SUPPORTED = "Export.FormatSupported";

    /**
     * Returned by {@link #isExportFormatSupported(String)} if the object does not support export to the given format.
     */
    public static final String FORMAT_NOT_SUPPORTED = "Export.FormatNotSupported";

    /**
     * Returned by {@link #isExportFormatSupported(String)} if the object contains some objects that support does not
     * support export to the given format, but others that do not. For example, a Layer might contain some objects that
     * support the export format, and some that do not.
     */
    public static final String FORMAT_PARTIALLY_SUPPORTED = "Export.FormatPartiallySupported";

    /**
     * Does this object support a certain export format?
     *
     * @param mimeType Desired export format.
     *
     * @return One of {@link #FORMAT_SUPPORTED}, {@link #FORMAT_NOT_SUPPORTED}, or {@link #FORMAT_PARTIALLY_SUPPORTED}.
     *
     * @see #export(String, Object)
     */
    String isExportFormatSupported(String mimeType);

    /**
     * Exports the object to a format.
     *
     * @param mimeType Desired export format. Call {@link #isExportFormatSupported(String)} to make sure that the object
     *                 supports the format before trying to export, or be prepared to handle {@code
     *                 UnsupportedOperationException}.
     * @param output   Object that will receive the exported data. The type of this object depends on the export format.
     *                 All formats should support {@code java.io.OutputStream}. Text based format (for example, XML
     *                 formats) should also support {@code java.io.Writer}. Certain formats may also support other
     *                 object types.
     *
     * @throws IOException                   if an exception occurs while exporting the data.
     * @throws UnsupportedOperationException if the format is not supported by this object, or if the {@code output}
     *                                       argument is not of a supported type.
     * @see #isExportFormatSupported(String)
     */
    void export(String mimeType, Object output) throws IOException, UnsupportedOperationException;
}
