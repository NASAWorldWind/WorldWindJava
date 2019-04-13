/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import javax.xml.stream.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

/**
 * Draws an image parallel to the screen at a specified screen location relative to the WorldWindow. If no image is
 * specified, a filled rectangle is drawn in its place.
 *
 * @author tag
 * @version $Id: ScreenImage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenImage extends WWObjectImpl implements Renderable, Exportable
{
    protected Object imageSource;
    protected BasicWWTexture texture;
    protected OrderedImage orderedImage = new OrderedImage();
    protected PickSupport pickSupport = new PickSupport();
    protected double opacity = 1d;
    protected Double rotation;
    protected Color color = Color.WHITE;
    protected Object delegateOwner;

    protected Size size = new Size();
    protected Offset screenOffset;
    protected Offset imageOffset;
    protected Offset rotationOffset;

    // Values computed once per frame and reused during the frame as needed.
    protected long frameNumber = -1;         // Identifies frame used to calculate these values
    protected int width;                     // Width of scaled image
    protected int height;                    // Height of scaled image
    protected int originalImageWidth;        // Width of unscaled image
    protected int originalImageHeight;       // Height of unscaled image
    protected Point rotationPoint;
    /**
     * Indicates the location of this screen image in the viewport (on the screen) in OpenGL coordinates. This property
     * is computed in <code>computeOffsets</code> and used in <code>draw</code> Initially <code>null</code>.
     */
    protected Point screenLocation;
    /**
     * Indicates the location of this screen image in the viewport (on the screen) in AWT coordinates. This property is
     * assigned in <code>setScreenLocation</code> and <code>computeOffsets</code>. In <code>computeOffsets</code>, this
     * is computed by converting the <code>screenLocation</code> from OpenGL coordinates to AWT coordinates. Initially
     * <code>null</code>.
     */
    protected Point awtScreenLocation;
    protected double dx;
    protected double dy;
    protected Layer pickLayer;

    protected class OrderedImage implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            ScreenImage.this.draw(dc);
        }

        public void render(DrawContext dc)
        {
            ScreenImage.this.draw(dc);
        }
    }

    /**
     * Returns the location of the image on the screen. The position is relative to the upper-left corner of the World
     * Window. The point specified by the image offset will be aligned to this point. If the position was specified as
     * an offset, it may change if the viewport size changes. The value returned by this method is the most recently
     * computed screen location. Call {@link #getScreenLocation(DrawContext)} to ensure an accurate result based on the
     * current viewport.
     *
     * @return the current screen position.
     *
     * @see #getScreenLocation(DrawContext)
     * @see #getImageOffset()
     * @see #getScreenOffset()
     */
    public Point getScreenLocation()
    {
        return this.awtScreenLocation;
    }

    /**
     * Returns the location of the image on the screen. The position is relative to the upper-left corner of the World
     * Window. The image is centered on this position.
     *
     * @param dc The DrawContext in which the image will be drawn.
     *
     * @return the current screen position.
     */
    public Point getScreenLocation(DrawContext dc)
    {
        this.computeOffsets(dc);
        return this.awtScreenLocation;
    }

    /**
     * Convenience method to specify the location of the image on the screen. The specified <code>screenLocation</code>
     * is relative to the upper-left corner of the WorldWindow, and the image is centered on this location.
     *
     * @param screenLocation the screen location on which to center the image. May be null, in which case the image is
     *                       not displayed.
     *
     * @see #setScreenOffset(Offset)
     * @see #setImageOffset(Offset)
     */
    public void setScreenLocation(Point screenLocation)
    {
        // Use units PIXELS for the X screen offset, and and INSET_PIXELS for the Y screen offset. The Offset is in
        // OpenGL coordinates with the origin in the lower-left corner, but the Point is in AWT coordinates with the
        // origin in the upper-left corner. This offset translates the origin from the lower-left to the upper-left
        // corner.
        this.screenOffset = new Offset(screenLocation.getX(), screenLocation.getY(), AVKey.PIXELS, AVKey.INSET_PIXELS);
        this.imageOffset = new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION);

        // Set cached screen location to the initial screen location so that it can be retrieved if getScreenLocation()
        // is called before the image is rendered. This maintains backward compatibility with the previous behavior of
        // ScreenImage.
        this.awtScreenLocation = new Point(screenLocation);
    }

    /**
     * Get the offset of the point on the screen to align with the image offset point.
     *
     * @return Offset of the image point that will be aligned to the image offset point.
     *
     * @see #getImageOffset()
     */
    public Offset getScreenOffset()
    {
        return screenOffset;
    }

    /**
     * Set the offset of the image relative to the viewport. The screen point identified by this offset will be aligned
     * to the image point identified by the image offset.
     *
     * @param screenOffset The screen offset.
     *
     * @see #setImageOffset(Offset)
     */
    public void setScreenOffset(Offset screenOffset)
    {
        this.screenOffset = screenOffset;
    }

    /**
     * Get the offset of the point on the image to align with the screen offset point.
     *
     * @return Offset of the image point that will be aligned to the screen offset point.
     *
     * @see #getScreenOffset()
     */
    public Offset getImageOffset()
    {
        return imageOffset;
    }

    /**
     * Set the image offset point. This point will be aligned to the screen point identified by the screen offset.
     *
     * @param imageOffset Offset that identifies a point on the image to align with the screen offset point.
     *
     * @see #setScreenOffset(Offset)
     */
    public void setImageOffset(Offset imageOffset)
    {
        this.imageOffset = imageOffset;
    }

    /**
     * Get the rotation applied to the image.
     *
     * @return Rotation in decimal degrees, or null if there is no rotation.
     *
     * @see #getRotationOffset()
     */
    public Double getRotation()
    {
        return rotation;
    }

    /**
     * Specifies a rotation to be applied to the image.
     *
     * @param rotation Rotation in decimal degrees.
     *
     * @see #setRotationOffset(Offset)
     */
    public void setRotation(Double rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Get the point about which the image is rotated.
     *
     * @return Rotation point in image coordinates, or null if there is no rotation point set. The origin of the
     *         coordinate system is at the lower left corner of the image.
     *
     * @see #getRotation()
     */
    public Offset getRotationOffset()
    {
        return rotationOffset;
    }

    /**
     * Set the point on the image about which rotation is performed.
     *
     * @param rotationOffset Rotation offset.
     *
     * @see #setRotation(Double)
     */
    public void setRotationOffset(Offset rotationOffset)
    {
        this.rotationOffset = rotationOffset;
    }

    /**
     * Get the dimension to apply to the image.
     *
     * @return Image dimension.
     */
    public Size getSize()
    {
        return size;
    }

    /**
     * Set a dynamic dimension to apply to the image. The dimension allows the image to be scaled relative to the
     * viewport size.
     *
     * @param size Image dimension. May not be null.
     */
    public void setSize(Size size)
    {
        if (size == null)
        {
            String msg = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.size = size;
    }

    /**
     * Returns the current image source.
     *
     * @return the current image source.
     *
     * @see #getImageSource()
     */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Specifies the image source, which may be either a file path {@link String} or a {@link
     * java.awt.image.BufferedImage}. If the image is not already in memory, it will be loaded in the background.
     *
     * @param imageSource the image source, either a file path {@link String} or a {@link
     *                    java.awt.image.BufferedImage}.
     *
     * @throws IllegalArgumentException if the <code>imageSource</code> is null.
     */
    public void setImageSource(Object imageSource)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.texture = null; // New image source, we need to load a new texture
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded and return null.
     *
     * @return The texture, or null if the texture is not yet available.
     */
    protected BasicWWTexture initializeTexture()
    {
        Object imageSource = this.getImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.texture = new BasicWWTexture(imageURL, true);
                this.texture.setUseAnisotropy(false);
            }
        }
        else if (imageSource != null)
        {
            this.texture = new BasicWWTexture(imageSource, true);
            return this.texture;
        }

        return null;
    }

    /**
     * Returns the opacity of the surface. A value of 1 or greater means the surface is fully opaque, a value of 0 means
     * that the surface is fully transparent.
     *
     * @return the surface opacity.
     */
    public double getOpacity()
    {
        return opacity;
    }

    /**
     * If no image is set, or if the image is not yet available, a rectangle will be drawn in this color.
     *
     * @return The color for the default rectangle.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the color of the rectangle drawn when the image cannot be drawn. The image may not be drawn because it has
     * not been loaded, or because no image has been set.
     *
     * @param defaultColor New color for the default rectangle.
     */
    public void setColor(Color defaultColor)
    {
        this.color = defaultColor;
    }

    /**
     * Sets the opacity of the surface. A value of 1 or greater means the surface is fully opaque, a value of 0 means
     * that the surface is fully transparent.
     *
     * @param opacity a positive value indicating the opacity of the surface.
     *
     * @throws IllegalArgumentException if the specified opacity is less than zero.
     */
    public void setOpacity(double opacity)
    {
        if (opacity < 0)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.opacity = opacity;
    }

    /**
     * Returns the width of the source image after dynamic scaling has been applied. If no image has been specified, but
     * a dimension has been specified, the width of the dimension is returned.
     *
     * @param dc the current draw context.
     *
     * @return the source image width after scaling.
     *
     * @see #getSize()
     */
    public int getImageWidth(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.computeOffsets(dc);
        return this.width;
    }

    /**
     * Returns the height of the image after dynamic scaling has been applied. If no image has been specified, but a
     * dimension has been specified, the height of the dimension is returned.
     *
     * @param dc the current draw context.
     *
     * @return the source image height after scaling.
     *
     * @see #getSize()
     */
    public int getImageHeight(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.computeOffsets(dc);
        return this.height;
    }

    /**
     * Indicates the object included in {@link gov.nasa.worldwind.event.SelectEvent}s when this object is picked.
     *
     * @return the object identified as the picked object.
     */
    public Object getDelegateOwner()
    {
        return delegateOwner;
    }

    /**
     * Specify the object to identify as the picked object when this shape is picked.
     *
     * @param delegateOwner the object included in {@link gov.nasa.worldwind.event.SelectEvent}s as the picked object.
     */
    public void setDelegateOwner(Object delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }

    /**
     * Compute the image size, rotation, and position based on the current viewport size. This method updates the
     * calculated values for screen point, rotation point, width, and height. The calculation is not performed if the
     * values have already been calculated for this frame.
     *
     * @param dc DrawContext into which the image will be rendered.
     */
    protected void computeOffsets(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() != this.frameNumber)
        {
            final BasicWWTexture texture = this.getTexture();

            final int viewportWidth = dc.getView().getViewport().width;
            final int viewportHeight = dc.getView().getViewport().height;

            // Compute image size
            if (texture != null)
            {
                this.originalImageWidth = texture.getWidth(dc);
                this.originalImageHeight = texture.getHeight(dc);
            }
            else if (this.getImageSource() == null) // If no image source is set, draw a rectangle
            {
                this.originalImageWidth = 1;
                this.originalImageHeight = 1;
            }
            else // If an image source is set, but the image is not available yet, don't draw anything
            {
                this.frameNumber = dc.getFrameTimeStamp();
                return;
            }

            if (this.size != null)
            {
                Dimension d = this.size.compute(this.originalImageWidth, this.originalImageHeight,
                    viewportWidth, viewportHeight);
                this.width = d.width;
                this.height = d.height;
            }
            else
            {
                this.width = this.originalImageWidth;
                this.height = this.originalImageHeight;
            }

            // Compute rotation
            Offset rotationOffset = this.getRotationOffset();

            // If no rotation offset is set, rotate around the center of the image.
            if (rotationOffset != null)
            {
                // The KML specification according to both OGC and Google states that the rotation point is specified in
                // a coordinate system with the origin at the lower left corner of the screen (0.5, 0.5 is the center
                // of the screen). But Google Earth interprets the point in a coordinate system with origin at the lower
                // left corner of the image (0.5, 0.5 is the center of the image), so we'll do that too.
                Point.Double pointD = rotationOffset.computeOffset(this.width, this.height, null, null);
                rotationPoint = new Point((int) pointD.x, (int) pointD.y);
            }
            else
            {
                this.rotationPoint = new Point(this.width, this.height);
            }

            // Compute position
            if (this.screenOffset != null)
            {
                // Compute the screen location in OpenGL coordinates. There is no need to convert from AWT to OpenGL
                // coordinates because the Offset is already in OpenGL coordinates with its origin in the lower-left
                // corner.
                Point.Double pointD = this.screenOffset.computeOffset(viewportWidth, viewportHeight, null, null);
                this.screenLocation = new Point((int) pointD.x, (int) (pointD.y));
            }
            else
            {
                this.screenLocation = new Point(viewportWidth / 2, viewportHeight / 2);
            }

            // Convert the screen location from OpenGL to AWT coordinates and store the result in awtScreenLocation. The
            // awtScreenLocation property is used in getScreenLocation to indicate the screen location in AWT
            // coordinates.
            this.awtScreenLocation = new Point(this.screenLocation.x, viewportHeight - this.screenLocation.y);

            Point.Double overlayPoint;
            if (this.imageOffset != null)
                overlayPoint = this.imageOffset.computeOffset(this.width, this.height, null, null);
            else
                overlayPoint = new Point.Double(this.originalImageWidth / 2.0, this.originalImageHeight / 2.0);

            this.dx = -overlayPoint.x;
            this.dy = -overlayPoint.y;

            this.frameNumber = dc.getFrameTimeStamp();
        }
    }

    /**
     * Get the texture for this image. The texture is loaded on a background thread. This method will return null until
     * the texture has been loaded.
     *
     * @return The texture or null if the texture is not yet available.
     */
    protected BasicWWTexture getTexture()
    {
        if (this.texture != null)
            return this.texture;
        else
            return this.initializeTexture();
    }

    public void render(DrawContext dc)
    {
        this.computeOffsets(dc);
        this.doRender(dc);
    }

    @SuppressWarnings({"UnusedParameters"})
    public void pick(DrawContext dc, Point pickPoint)
    {
        this.doRender(dc);
    }

    protected void doRender(DrawContext dc)
    {
        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        dc.addOrderedRenderable(this.orderedImage);
    }

    protected void draw(DrawContext dc)
    {
        if (this.screenLocation == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_TRANSFORM_BIT
                | GL2.GL_VIEWPORT_BIT
                | GL2.GL_CURRENT_BIT);
            attribsPushed = true;

            // Don't depth buffer.
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Suppress any fully transparent image pixels
            gl.glEnable(GL2.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            // Apply the screen location transform. The screen location is in OpenGL coordinates with the origin in the
            // lower-left corner, so there is no need to translate from AWT to OpenGL coordinates here.
            gl.glTranslated(this.screenLocation.x + this.dx, this.screenLocation.y + this.dy, 0d);

            Double rotation = this.getRotation();
            if (rotation != null)
            {
                gl.glTranslated(rotationPoint.x, rotationPoint.y, 0);
                gl.glRotated(rotation, 0, 0, 1);
                gl.glTranslated(-rotationPoint.x, -rotationPoint.y, 0);
            }

            double xscale = (double) this.getImageWidth(dc) / originalImageWidth;
            double yscale = (double) this.getImageHeight(dc) / originalImageHeight;

            if (!dc.isPickingMode())
            {
                // Draw either an image or a filled rectangle
                boolean drawImage = this.getTexture() != null;

                gl.glEnable(GL.GL_TEXTURE_2D);
                if (drawImage)
                {
                    if (this.getTexture().bind(dc))
                        gl.glColor4d(1d, 1d, 1d, this.opacity);
                    else
                        drawImage = false; // Can't bind texture, draw rectangle instead
                }

                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

                if (drawImage)
                {
                    TextureCoords texCoords = this.getTexture().getTexCoords();
                    gl.glScaled(xscale * this.originalImageWidth, yscale * this.originalImageHeight, 1d);
                    dc.drawUnitQuad(texCoords);
                    gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
                }
                else
                {
                    // Set color of the rectangle that will be drawn instead of an image
                    final Color color = this.getColor();
                    float[] colorRGB = color.getRGBColorComponents(null);
                    gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], (double) color.getAlpha() / 255);

                    // Don't have texture, just draw a rectangle
                    gl.glScaled(xscale, yscale, 1d);
                    dc.drawUnitQuad();
                }
            }
            else
            {
                this.pickSupport.clearPickList();
                this.pickSupport.beginPicking(dc);
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                this.pickSupport.addPickableObject(colorCode, this.delegateOwner != null ? this.delegateOwner : this,
                    null, false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                gl.glScaled(xscale * this.originalImageWidth, yscale * this.originalImageHeight, 1d);
                dc.drawUnitQuad();
                this.pickSupport.endPicking(dc);
                this.pickSupport.resolvePick(dc, dc.getPickPoint(), this.pickLayer);
            }
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
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
     * Export the screen image. The {@code output} object will receive the exported data. The type of this object
     * depends on the export format. The formats and object types supported by this class are:
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
     * @throws java.io.IOException If an exception occurs writing to the output object.
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

    /**
     * Export the screen image to KML as a {@code <ScreenOverlay>} element. The {@code output} object will receive the
     * data. This object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter.
     * <p>
     * The image path can only be exported if the image source is a path or URL. If the image source is a BufferedImage,
     * for example, the image will not be exported and no icon reference will be written into the ScreenOverlay tag.
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
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

        xmlWriter.writeStartElement("ScreenOverlay");

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters("1");
        xmlWriter.writeEndElement();

        String imgSrcString = null;
        Object imageSource = this.getImageSource();
        if (imageSource instanceof String)
            imgSrcString = (String) imageSource;
        else if (imageSource instanceof URL)
            imgSrcString = imageSource.toString();

        // We can only export a link to the image if the image source is a path or URL. 
        if (imgSrcString != null)
        {
            xmlWriter.writeStartElement("Icon");
            xmlWriter.writeStartElement("href");
            xmlWriter.writeCharacters(imgSrcString);
            xmlWriter.writeEndElement(); // href
            xmlWriter.writeEndElement(); // Icon
        }
        else
        {
            // No image string, try to export the color
            Color color = this.getColor();
            if (color != null)
            {
                xmlWriter.writeStartElement("color");
                xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(color)));
                xmlWriter.writeEndElement();
            }
        }

        KMLExportUtil.exportOffset(xmlWriter, this.getImageOffset(), "overlayXY");
        KMLExportUtil.exportOffset(xmlWriter, this.getScreenOffset(), "screenXY");

        Double rotation = this.getRotation();
        if (rotation != null)
        {
            xmlWriter.writeStartElement("rotation");
            xmlWriter.writeCharacters(rotation.toString());
            xmlWriter.writeEndElement();  // rotation
        }

        KMLExportUtil.exportOffset(xmlWriter, this.getRotationOffset(), "rotationXY");

        KMLExportUtil.exportDimension(xmlWriter, this.getSize(), "size");

        xmlWriter.writeEndElement(); // ScreenOverlay

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }
}
