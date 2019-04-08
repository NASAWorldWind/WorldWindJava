/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL;
import javax.xml.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.UUID;

/** Holds attributes for {@link gov.nasa.worldwind.render.PointPlacemark}s. */
public class PointPlacemarkAttributes implements Exportable
{
    protected String imageAddress;
    protected BufferedImage image;
    protected Double scale;
    protected Double heading;
    protected String headingReference; // RELATIVE_TO_GLOBE, RELATIVE_TO_SCREEN
    protected Double pitch;
    protected Offset imageOffset;
    /**
     * Color to be blended with the image. This can be used to render the same image in different colors for different
     * placemarks.
     */
    protected Color imageColor;
    protected Double lineWidth;
    protected Material lineMaterial;
    protected int antiAliasHint = GL.GL_FASTEST;
    protected Font labelFont;
    protected Offset labelOffset;
    protected Material labelMaterial;
    protected Double labelScale;
    protected boolean usePointAsDefaultImage = false;
    protected boolean unresolved;
    protected boolean drawImage = true;
    protected boolean drawLabel = true;

    /** The image file to use for the placemark's icon if no image file is specified in the placemark attributes. */
    public static final String DEFAULT_IMAGE_PATH =
        Configuration.getStringValue("gov.nasa.worldwind.render.PointPlacemarkAttributes.DefaultImagePath",
            "images/pushpins/plain-yellow.png");
    /** The image offset to use if none specified. This value is that required by the default image. */
    public static final Offset DEFAULT_IMAGE_OFFSET = new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS);
    /** The image scale to use if none specified. This value is appropriate for the default image. */
    public static final Double DEFAULT_IMAGE_SCALE = 0.6;
    /** The label scale to use if none specified. */
    public static final Double DEFAULT_LABEL_SCALE = 1.0;
    /** The default image color. */
    protected static final Color DEFAULT_IMAGE_COLOR = Color.WHITE;
    /** The default label offset. This value is appropriate for the default image. */
    public static final Offset DEFAULT_LABEL_OFFSET = new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION);
    /** The default font to use for the placemark's label. */
    public static final Font DEFAULT_LABEL_FONT = Font.decode(
        Configuration.getStringValue("gov.nasa.worldwind.render.PointPlacemarkAttributes.DefaultLabelFont",
            "Arial-BOLD-14"));
    /** The default label color. */
    protected static final Color DEFAULT_LABEL_COLOR = Color.WHITE;
    /** The default line color. */
    protected static final Color DEFAULT_LINE_COLOR = Color.WHITE;

    /**
     * Constructs an instance with default values for image address, image offset, image scale, label offset, label font
     * and label color.
     */
    public PointPlacemarkAttributes()
    {
    }

    /**
     * Constructs an instance and initializes it to the values in a specified instance.
     *
     * @param attrs the instance from which to copy the initial attribute values of this. May be null.
     */
    public PointPlacemarkAttributes(PointPlacemarkAttributes attrs)
    {
        this.copy(attrs);
    }

    /**
     * Copies all values, including null values, from another instance to this one.
     *
     * @param attrs the instance to copy values from.
     */
    public void copy(PointPlacemarkAttributes attrs)
    {
        if (attrs != null)
        {
            this.setImageAddress(attrs.getImageAddress());
            this.setScale(attrs.getScale());
            this.setHeading(attrs.getHeading());
            this.setHeadingReference(attrs.getHeadingReference());
            this.setPitch(attrs.getPitch());
            this.setImageOffset(attrs.getImageOffset());
            this.setImageColor(attrs.getImageColor());
            this.setLineWidth(attrs.getLineWidth());
            this.setLineMaterial(attrs.getLineMaterial());
            this.setAntiAliasHint(attrs.getAntiAliasHint());
            this.setLabelFont(attrs.getLabelFont());
            this.setLabelOffset(attrs.getLabelOffset());
            this.setLabelMaterial(attrs.getLabelMaterial());
            this.setLabelScale(attrs.getLabelScale());
            this.setUsePointAsDefaultImage(attrs.isUsePointAsDefaultImage());
            this.setDrawImage(attrs.isDrawImage());
            this.setDrawLabel(attrs.isDrawLabel());

            // Calling setImage has side effects, so just assign the current value without calling setImage.
            this.image = attrs.image;
        }
    }

    /**
     * Returns this instance's line width.
     *
     * @return the line width.
     */
    public Double getLineWidth()
    {
        return lineWidth;
    }

    /**
     * Specifies the line width to use when rendering the optional placemark line.
     *
     * @param lineWidth the line width. May be null, in which case a width of 1 is used during rendering.
     */
    public void setLineWidth(Double lineWidth)
    {
        this.lineWidth = lineWidth;
    }

    /**
     * Returns the line color.
     *
     * @return the line color.
     */
    public Material getLineMaterial()
    {
        return this.lineMaterial;
    }

    /**
     * Returns the label diffuse component of the label's material color.
     *
     * @return the label's diffuse color.
     */
    public Color getLineColor()
    {
        return lineMaterial == null ? null : this.lineMaterial.getDiffuse();
    }

    /**
     * Sets the line color.
     *
     * @param lineColor the line color. May be null.
     */
    public void setLineMaterial(Material lineColor)
    {
        this.lineMaterial = lineColor;
    }

    /**
     * Sets the line color as a string in the form 0xAABBGGRR.
     *
     * @param lineColorString the line color. May be null.
     */
    public void setLineColor(String lineColorString)
    {
        this.setLineMaterial(new Material(WWUtil.decodeColorABGR(lineColorString)));
    }

    /**
     * Sets the image color. The image color is blended with the image, allowing the same image to be rendered in
     * different colors for different placemarks.
     *
     * @return The image color.
     *
     * @see #setImageColor(java.awt.Color)
     */
    public Color getImageColor()
    {
        return this.imageColor;
    }

    /**
     * Sets the image color. The image color is blended with the image, allowing the same image to be rendered in
     * different colors for different placemarks.
     *
     * @param imageColor New image color.
     *
     * @see #getImageColor()
     */
    public void setImageColor(Color imageColor)
    {
        this.imageColor = imageColor;
    }

    /**
     * Returns the anti-alias hint. See {@link #setAntiAliasHint(int)} for the recognized values.
     *
     * @return the anti-alias hint.
     */
    public int getAntiAliasHint()
    {
        return antiAliasHint;
    }

    /**
     * Specifies whether and how line anti-aliasing is performed. Recognized values are {@link GL#GL_NEAREST}, {@link
     * GL#GL_FASTEST}, and {@link GL#GL_NICEST}.
     *
     * @param antiAliasHint the anti-alias hint.
     */
    public void setAntiAliasHint(int antiAliasHint)
    {
        this.antiAliasHint = antiAliasHint;
    }

    /**
     * Returns the address of the placemark's image.
     *
     * @return the address of the placemark's image. May be null.
     */
    public String getImageAddress()
    {
        return this.imageAddress;
    }

    /**
     * Specifies the address of the placemark's image. The address may be a file path or a URL.
     *
     * @param address the address of the placemark's image. May be null, in which case a default image is used.
     */
    public void setImageAddress(String address)
    {
        this.imageAddress = address;
    }

    /**
     * Returns the {@link java.awt.image.BufferedImage} previously specified to {@link
     * #setImage(java.awt.image.BufferedImage)}.
     *
     * @return The image previously specified for this attribute bundle.
     */
    public BufferedImage getImage()
    {
        return image;
    }

    /**
     * Specifies a {@link java.awt.image.BufferedImage} for {@link gov.nasa.worldwind.render.PointPlacemark}s associated
     * with this attribute bundle. When this method is called, this attribute bundle's image address is automatically
     * set to a unique identifier for the image.
     *
     * @param image the buffered image to use for the associated point placemarks. May be null, in which case this
     *              attribute bundle's image address is set to null by this method.
     */
    public void setImage(BufferedImage image)
    {
        this.image = image;

        this.setImageAddress(this.image != null ? UUID.randomUUID().toString() : null);
    }

    /**
     * Returns the placemark image scale. See [@link #setScale} for its description.
     *
     * @return the placemark image scale.
     */
    public Double getScale()
    {
        return this.scale;
    }

    /**
     * Specifies the placemark image scale. The scale is applied to the placemark image's width and height during
     * rendering in order to control the rendered size of the image.
     *
     * @param scale the placemark image scale. May be null, in which case no scaling is applied.
     */
    public void setScale(Double scale)
    {
        this.scale = scale;
    }

    /**
     * Returns the placemark image heading.
     *
     * @return the placemark image heading.
     */
    public Double getHeading()
    {
        return this.heading;
    }

    /**
     * Specifies the placemark image heading, which is used by some placemark implementations to orient the placemark
     * image.
     *
     * @param heading the placemark heading in degrees clockwise from North. May be null, in which case no heading is
     *                applied during rendering.
     */
    public void setHeading(Double heading)
    {
        this.heading = heading;
    }

    /**
     * Indicates the heading reference.
     *
     * @return the heading reference.
     *
     * @see #setHeadingReference(String).
     */
    public String getHeadingReference()
    {
        return headingReference;
    }

    /**
     * Specifies the heading reference. If {@link gov.nasa.worldwind.avlist.AVKey#RELATIVE_TO_SCREEN}, the heading is
     * interpreted as relative to the screen and the placemark icon maintains the heading relative to the screen's
     * vertical edges. If {@link gov.nasa.worldwind.avlist.AVKey#RELATIVE_TO_GLOBE}, the heading is interpreted relative
     * to the globe and the placemark icon maintains the heading relative to the globe's north direction.
     * <p/>
     * The default heading reference is null, which {@link PointPlacemark} interprets as {@link
     * gov.nasa.worldwind.avlist.AVKey#RELATIVE_TO_SCREEN}.
     *
     * @param headingReference the heading reference. See the description for possible values.
     */
    public void setHeadingReference(String headingReference)
    {
        this.headingReference = headingReference;
    }

    /**
     * Indicates the placemark image pitch.
     *
     * @return the placemark image pitch.
     */
    public Double getPitch()
    {
        return this.pitch;
    }

    /**
     * Specifies the placemark image pitch.
     *
     * @param pitch the placemark pitch in degrees. May be null, in which case no pitch is applied during rendering.
     */
    public void setPitch(Double pitch)
    {
        this.pitch = pitch;
    }

    /**
     * Returns the image offset.
     *
     * @return the image offset.
     */
    public Offset getImageOffset()
    {
        return imageOffset;
    }

    /**
     * Specifies a location within the placemark image to align with the placemark point.
     *
     * @param offset the hot spot controlling the image's placement relative to the placemark point. May be null to
     *               indicate that the image's lower left corner is aligned with the placemark point.
     */
    public void setImageOffset(Offset offset)
    {
        this.imageOffset = offset;
    }

    /**
     * Indicates whether one or more members of <i>this</> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @return true if there are unresolved fields, false if no fields remain unresolved.
     */
    public boolean isUnresolved()
    {
        return unresolved;
    }

    /**
     * Specifies whether one or more fields of <i>this</> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @param unresolved true if there are unresolved fields, false if no fields remain unresolved.
     */
    public void setUnresolved(boolean unresolved)
    {
        this.unresolved = unresolved;
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font labelFont)
    {
        this.labelFont = labelFont;
    }

    /**
     * Returns the label offset.
     *
     * @return the label offset.
     */
    public Offset getLabelOffset()
    {
        return labelOffset;
    }

    /**
     * Specifies a location relative to the placemark's image at which to align the label. The label text begins at the
     * point indicated by the offset. A offset of (0, 0) pixels causes the text to start at the lower left corner of the
     * image. An offset of (1, 1) in fraction units causes the text to start at the upper right corner of the image. The
     * text would also start there if the offset is in units of pixels and the X and Y values are the image width and
     * height, respectively.
     * <p/>
     * If no offset is specified, the label is placed at the right edge of the image with the top of the text at about
     * the same level as the top of the image. (An offset of (X = 1.0, Y = 0.6, both in fraction units.)
     *
     * @param offset the hot spot controlling the image's placement relative to the placemark point. May be null to
     *               indicate the default label offset.
     */
    public void setLabelOffset(Offset offset)
    {
        this.labelOffset = offset;
    }

    /**
     * Returns the label material.
     *
     * @return the label material.
     */
    public Material getLabelMaterial()
    {
        return labelMaterial;
    }

    /**
     * Returns the label diffuse component of the label's material color.
     *
     * @return the label's diffuse color.
     */
    public Color getLabelColor()
    {
        return labelMaterial == null ? null : this.labelMaterial.getDiffuse();
    }

    /**
     * Sets the label material.
     *
     * @param color the line material. May be null.
     */
    public void setLabelMaterial(Material color)
    {
        this.labelMaterial = color;
    }

    /**
     * Sets the label color as a string in the form 0xAABBGGRR.
     *
     * @param labelColorString the line color. May be null.
     */
    public void setLabelColor(String labelColorString)
    {
        this.setLabelMaterial(new Material(WWUtil.decodeColorABGR(labelColorString)));
    }

    /**
     * Returns the placemark's label scale. See [@link #setLabelScale} for its description.
     *
     * @return the placemark label scale.
     */
    public Double getLabelScale()
    {
        return labelScale;
    }

    /**
     * Specifies the placemark label scale. The scale is applied to the placemark label's size after the base size is
     * determined by the font.
     *
     * @param scale the placemark image scale. May be null, in which case no scaling is applied.
     */
    public void setLabelScale(Double scale)
    {
        this.labelScale = scale;
    }

    /**
     * Indicates whether to draw a point when the current source image is null.
     *
     * @return true if a point is drawn when the current source image is null, otherwise false.
     *
     * @see #setUsePointAsDefaultImage(boolean)
     */
    public boolean isUsePointAsDefaultImage()
    {
        return usePointAsDefaultImage;
    }

    /**
     * Specifies whether to draw a point when the current source image is null. When drawing the point, the scale of the
     * currently active attributes specifies the diameter of the point in pixels. The point is drawn in the line color
     * of the currently active attributes.
     *
     * @param usePointAsDefaultImage true to draw a point when the current source image is null, otherwise false.
     *
     * @see #isUsePointAsDefaultImage()
     */
    public void setUsePointAsDefaultImage(boolean usePointAsDefaultImage)
    {
        this.usePointAsDefaultImage = usePointAsDefaultImage;
    }

    /** {@inheritDoc} */
    public String isExportFormatSupported(String format)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(format))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    /**
     * Indicates whether the placemark's image is drawn.
     *
     * @return <code>true</code> if the image is drawn, otherwise <code>false</code>.
     */
    public boolean isDrawImage()
    {
        return drawImage;
    }

    /**
     * Specifies whether to draw the placemark's image. When the image is not drawn, the placemark's label, if any, is
     * drawn relative to the placemark's position.
     *
     * @param drawImage <code>true</code> to draw the image, otherwise <code>false</code>.
     */
    public void setDrawImage(boolean drawImage)
    {
        this.drawImage = drawImage;
    }

    /**
     * Indicates whether the placemark's label is drawn.
     *
     * @return <code>true</code> if the label is drawn, otherwise <code>false</code>.
     */
    public boolean isDrawLabel()
    {
        return drawLabel;
    }

    /**
     * Specifies whether to draw the placemark's label.
     *
     * @param drawLabel <code>true</code> to draw the label, otherwise <code>false</code>.
     */
    public void setDrawLabel(boolean drawLabel)
    {
        this.drawLabel = drawLabel;
    }

    /**
     * Export the Placemark. The {@code output} object will receive the exported data. The type of this object depends
     * on the export format. The formats and object types supported by this class are:
     * <p/>
     * <pre>
     * Format                                         Supported output object types
     * ================================================================================
     * KML (application/vnd.google-earth.kml+xml)     java.io.Writer
     *                                                java.io.OutputStream
     *                                                javax.xml.stream.XMLStreamWriter
     * </pre>
     *
     * @param mimeType MIME type of desired export format.
     * @param output   An object that will receive the exported data. The type of this object depends on the export
     *                 format (see above).
     *
     * @throws IOException If an exception occurs writing to the output object.
     */
    public void export(String mimeType, Object output) throws IOException, UnsupportedOperationException
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

    /**
     * Export the placemark attributes to KML as a {@code <Style>} element. The {@code output} object will receive the
     * data. This object must be one of: java.io.Writer<br/> java.io.OutputStream<br/> javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws XMLStreamException
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

        xmlWriter.writeStartElement("Style");

        // Icon style
        xmlWriter.writeStartElement("IconStyle");

        final Color imageColor = this.getImageColor();
        if (imageColor != null)
        {
            xmlWriter.writeStartElement("color");
            xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(imageColor)));
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement("colorMode");
            xmlWriter.writeCharacters("normal");
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("scale");
        xmlWriter.writeCharacters(Double.toString((this.getScale())));
        xmlWriter.writeEndElement();

        final Double heading = this.getHeading();
        if (heading != null)
        {
            xmlWriter.writeStartElement("heading");
            xmlWriter.writeCharacters(Double.toString(this.getHeading()));
            xmlWriter.writeEndElement();
        }

        String imgAddress = this.getImageAddress();
        if (imgAddress != null)
        {
            xmlWriter.writeStartElement("Icon");
            xmlWriter.writeStartElement("href");
            xmlWriter.writeCharacters(imgAddress);
            xmlWriter.writeEndElement(); // href
            xmlWriter.writeEndElement(); // Icon
        }

        Offset offset = this.getImageOffset();
        if (offset != null)
        {
            KMLExportUtil.exportOffset(xmlWriter, offset, "hotSpot");
        }

        xmlWriter.writeEndElement(); // IconStyle

        // Label style
        xmlWriter.writeStartElement("LabelStyle");

        final Double labelScale = this.getLabelScale();
        if (labelScale != null)
        {
            xmlWriter.writeStartElement("scale");
            xmlWriter.writeCharacters(Double.toString(labelScale));
            xmlWriter.writeEndElement();
        }

        final Color labelColor = this.getLabelColor();
        if (labelColor != null)
        {
            xmlWriter.writeStartElement("color");
            xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(labelColor)));
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement("colorMode");
            xmlWriter.writeCharacters("normal");
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement(); // LabelStyle

        // Line style
        xmlWriter.writeStartElement("LineStyle");

        final Double lineWidth = this.getLineWidth();
        if (lineWidth != null)
        {
            xmlWriter.writeStartElement("width");
            xmlWriter.writeCharacters(Double.toString(lineWidth));
            xmlWriter.writeEndElement();
        }

        final Color lineColor = this.getLineColor();
        if (lineColor != null)
        {
            xmlWriter.writeStartElement("color");
            xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(lineColor)));
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement("colorMode");
            xmlWriter.writeCharacters("normal");
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement(); // LineStyle
        xmlWriter.writeEndElement(); // Style

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }
}