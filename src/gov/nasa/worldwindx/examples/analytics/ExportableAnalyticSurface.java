/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.analytics;

import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import javax.xml.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

/**
 * An extension of AnalyticSurface that enables export of the surface to a KML ground overlay. To successfully export
 * the surface, the export image path and export image name fields of instances of this class must be specified.
 * @author tag
 * @version $Id: ExportableAnalyticSurface.java 1352 2013-05-20 18:41:16Z tgaskins $
 */
public class ExportableAnalyticSurface extends AnalyticSurface implements Exportable
{
    protected String exportImagePath;
    protected String exportImageName;
    protected int exportImageWidth = 1024;
    protected int exportImageHeight = 1024;

    /**
     * Specifies the path at which to write this surface's exported image. Used only when exporting this surface as a
     * KML ground overlay. This path should generally identify the directory holding the KML file that is being created.
     * The exportImageName field must also be set. See {@link #setExportImageName(String)}.
     *
     * @param path The directory in which to store the exported ground overlay image. This field must be non-null when
     *             this surface is exported.
     *
     * @see #setExportImageName(String)
     */
    public void setExportImagePath(String path)
    {
        this.exportImagePath = path;
    }

    /**
     * Indicates the directory in which this surface's export image is stored when exporting to KML.
     *
     * @return This surface's export image path.
     */
    public String getExportImagePath()
    {
        return this.exportImagePath;
    }

    /**
     * Indicates the name to give this surface's ground overlay image when the surface is exported to KML.
     *
     * @return This surface's export image name.
     */
    public String getExportImageName()
    {
        return exportImageName;
    }

    /**
     * Specifies the name of the ground overlay image file to create when this surface is exported to KML. The exported
     * image is written to a file with this name in the directory identified by this surface's exportImagePath field.
     *
     * @param exportImageName The name of the file in which to write the exported image. This field must be non-null
     *                        when this surface is exported.
     *
     * @see #setExportImagePath(String)
     */
    public void setExportImageName(String exportImageName)
    {
        this.exportImageName = exportImageName;
    }

    /**
     * Indicates the image width of the ground overlay image when this surface is exported to KML. The default is 1024.
     * @return The export image width.
     */
    public int getExportImageWidth()
    {
        return exportImageWidth;
    }

    /**
     * Specifies the image width of the ground overlay image when this surface is exported to KML. The default is 1024.
     * @param exportImageWidth The export image width.
     */
    public void setExportImageWidth(int exportImageWidth)
    {
        this.exportImageWidth = exportImageWidth;
    }

    /**
     * Indicates the image height of the ground overlay image when this surface is exported to KML. The default is 1024.
     * @return The export image height.
     */
    public int getExportImageHeight()
    {
        return exportImageHeight;
    }

    /**
     * Specifies the image width of the ground overlay image when this surface is exported to KML. The default is 1024.
     * @param exportImageHeight The export image height.
     */
    public void setExportImageHeight(int exportImageHeight)
    {
        this.exportImageHeight = exportImageHeight;
    }

    /**
     * Export's this surface's color values as a KML GroundOverlay. Only this surface's color values, outline color and
     * outline width are used to create the ground overly image. The image is exported as clamp-to-ground. The following
     * fields of this surface must be set prior to calling this method: exportImagePath and exportImageName. Optionally
     * the exportImageWidth and exportImageHeight fields may be set to indicate the size of the exported ground overlay
     * image. These values are both 1024 by default. The surface's opacity attributes are ignored, but any opacity
     * values specified in the surface's color values are captured in the ground overlay image.
     * <p>
     * If color values have not been specified for the surface then the interior if the output image is blank. The image
     * outline is exported only if the surface's drawOutline file is true.
     *
     * @param mimeType Desired export format. Only "application/vnd.google-earth.kml+xml" is supported.
     * @param output   Object that will receive the exported data. The type of this object depends on the export format.
     *                 All formats should support {@code java.io.OutputStream}. Text based format (for example, XML
     *                 formats) should also support {@code java.io.Writer}. Certain formats may also support other
     *                 object types.
     *
     * @throws java.io.IOException if an error occurs while writing the output file or its image.
     * @see #setExportImageName(String)
     * @see #setExportImagePath(String)
     * @see #setExportImageWidth(int)
     * @see #setExportImageHeight(int)
     */
    public void export(String mimeType, Object output) throws IOException
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (output == null)
        {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            try
            {
                exportAsKML(output);
            }
            catch (XMLStreamException e)
            {
                Logging.logger().throwing(getClass().getName(), "export", e);
                throw new IOException(e);
            }
        }
        else
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }
    }

    public String isExportFormatSupported(String format)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(format))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    /**
     * Export the surface image to KML as a {@code <GroundOverlay>} element. The {@code output} object will receive the
     * data. This object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException
     *                             If an exception occurs while writing the KML
     * @throws java.io.IOException if an exception occurs while exporting the data.
     */
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        if (this.exportImagePath == null || this.exportImageName == null)
        {
            String message = Logging.getMessage("Export.UnableToExportImageSource", "Image path or name unspecified");
            Logging.logger().severe(message);
        }
        else
        {
            File file = new File(this.exportImagePath + "/" + this.exportImageName);
            BufferedImage image = this.createImage(this.exportImageWidth, this.exportImageHeight);
            try
            {
                String suffix = WWIO.getSuffix(this.exportImageName);
                ImageIO.write(image, suffix, file);
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("Export.UnableToExportImageSource", file.getAbsolutePath(), e);
                Logging.logger().severe(message);
            }
        }

        xmlWriter.writeStartElement("GroundOverlay");

        // Write geometry
        xmlWriter.writeStartElement("Icon");
        xmlWriter.writeStartElement("href");
        if (this.exportImageName != null)
            xmlWriter.writeCharacters(this.exportImageName);
        xmlWriter.writeEndElement(); // href
        xmlWriter.writeEndElement();  // Icon

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters("clampToGround");
        xmlWriter.writeEndElement();

        exportKMLLatLonBox(xmlWriter);

        xmlWriter.writeEndElement(); // GroundOverlay

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }

    protected void exportKMLLatLonBox(XMLStreamWriter xmlWriter) throws XMLStreamException
    {
        xmlWriter.writeStartElement("LatLonBox");
        xmlWriter.writeStartElement("north");
        xmlWriter.writeCharacters(Double.toString(this.sector.getMaxLatitude().getDegrees()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("south");
        xmlWriter.writeCharacters(Double.toString(this.sector.getMinLatitude().getDegrees()));
        xmlWriter.writeEndElement(); // south

        xmlWriter.writeStartElement("east");
        xmlWriter.writeCharacters(Double.toString(this.sector.getMinLongitude().getDegrees()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("west");
        xmlWriter.writeCharacters(Double.toString(this.sector.getMaxLongitude().getDegrees()));
        xmlWriter.writeEndElement(); // west
        xmlWriter.writeEndElement(); // LatLonBox
    }

    /**
     * Create an exportable image from this surface's colors and attributes. Note that only the surface's color values
     * used to draw the interior, the surface's interior material attribute is ignored. Also ignored are the surface's
     * opacity and shadow attributes.
     *
     * @param imageWidth  The width of the image to create.
     * @param imageHeight The height of the image to create.
     *
     * @return An ARGB image reflecting this surface's color values and outline attributes.
     */
    public BufferedImage createImage(int imageWidth, int imageHeight)
    {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        if (this.surfaceAttributes.drawInterior && this.values != null)
        {
            ArrayList<Color> colorGrid = new ArrayList<Color>(this.width * this.height);
            for (GridPointAttributes gridPoint : this.values)
            {
                colorGrid.add(gridPoint.getColor());
            }

            double sectorHeight = this.sector.getDeltaLat().radians;
            double sectorWidth = this.sector.getDeltaLon().radians;
            double pixelWidth = sectorHeight / imageHeight;
            double pixelHeight = sectorWidth / imageWidth;
            double colorCellWidth = sectorWidth / (this.width - 1);
            double colorCellHeight = sectorHeight / (this.height - 1);

            for (int y = 0; y < imageHeight; y++)
            {
                double lat = (y + 0.5) * pixelWidth;

                int row = (int) (lat / colorCellHeight);

                for (int x = 0; x < imageWidth; x++)
                {
                    double lon = (x + 0.5) * pixelHeight;

                    int col = (int) (lon / colorCellWidth);

                    Color sw = colorGrid.get((row + 1) * this.width + col);
                    Color se = colorGrid.get((row + 1) * this.width + col + 1);
                    Color ne = colorGrid.get(row * this.width + col + 1);
                    Color nw = colorGrid.get(row * this.width + col);

                    double s = (lon - col * colorCellWidth) / colorCellWidth;
                    double t = (lat - row * colorCellHeight) / colorCellHeight;

                    int colorInt = ImageUtil.interpolateColor(s, 1.0 - t, sw.getRGB(), se.getRGB(), nw.getRGB(),
                        ne.getRGB());
                    image.setRGB(x, y, colorInt);
                }
            }
        }

        if (this.surfaceAttributes.drawOutline)
        {
            Graphics2D g = image.createGraphics();

            g.setPaint(this.surfaceAttributes.outlineMaterial.getDiffuse());
            g.setStroke(new BasicStroke((float) this.surfaceAttributes.outlineWidth));
            g.drawRect(0, 0, imageWidth - 1, imageHeight - 1);
        }

        return image;
    }
}
