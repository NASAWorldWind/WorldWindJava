/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;

/**
 * Abstract base class for retrieval post-processors. Verifies the retrieval operation and dispatches the content to the
 * a subclasses content handlers.
 * <p>
 * Subclasses are expected to override the methods necessary to handle their particular post-processing operations.
 *
 * @author Tom Gaskins
 * @version $Id: AbstractRetrievalPostProcessor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractRetrievalPostProcessor implements RetrievalPostProcessor
{
    /** Holds miscellaneous parameters examined by this and subclasses. */
    protected AVList avList;
    /** The retriever associated with the post-processor. Only non-null after {@link #run(Retriever)} is called. */
    protected Retriever retriever;

    /**
     * Abstract method that subclasses must provide to identify the output file for the post-processor's retrieval
     * content.
     *
     * @return the output file.
     */
    abstract protected File doGetOutputFile();

    /** Create a default post-processor. */
    public AbstractRetrievalPostProcessor()
    {
    }

    /**
     * Create a post-processor and pass it attributes that can be examined during content handling.
     *
     * @param avList an attribute-value list with values that might be used during post-processing.
     */
    public AbstractRetrievalPostProcessor(AVList avList)
    {
        this.avList = avList;
    }

    /**
     * Runs the post-processor.
     *
     * @param retriever the retriever to associate with the post-processor.
     *
     * @return a buffer containing the downloaded data, perhaps converted during content handling. null is returned if a
     *         fatal problem occurred during post-processing.
     *
     * @throws IllegalArgumentException if the retriever is null.
     */
    public ByteBuffer run(Retriever retriever)
    {
        if (retriever == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.retriever = retriever;

        if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
        {
            this.handleUnsuccessfulRetrieval();
            return null;
        }

        if (!this.validateResponseCode())
        {
            this.handleInvalidResponseCode();
            return null;
        }

        return this.handleSuccessfulRetrieval();
    }

    /**
     * Returns the retriever associarted with this post-processor.
     *
     * @return the retriever associated with the post-processor, or null if no retriever is associated.
     */
    public Retriever getRetriever()
    {
        return this.retriever;
    }

    /**
     * Called when the retrieval state is other than {@link Retriever#RETRIEVER_STATE_SUCCESSFUL}. Can be overridden by
     * subclasses to handle special error cases. The default implementation calls {@link #markResourceAbsent()} if the
     * retrieval state is {@link Retriever#RETRIEVER_STATE_ERROR}.
     */
    protected void handleUnsuccessfulRetrieval()
    {
        if (this.getRetriever().getState().equals(Retriever.RETRIEVER_STATE_ERROR))
            this.markResourceAbsent();
    }

    /**
     * Process the retrieved data if it has been retrieved successfully.
     *
     * @return a buffer containing the downloaded data, perhaps converted during content handling.
     */
    protected ByteBuffer handleSuccessfulRetrieval()
    {
        try
        {
            return this.handleContent();
        }
        catch (Exception e)
        {
            this.handleContentException(e);
            return null;
        }
    }

    /**
     * Checks the retrieval response code.
     *
     * @return true if the response code is the OK value for the protocol, e.g., ({@link HttpURLConnection#HTTP_OK} for
     *         HTTP protocol), otherwise false.
     */
    protected boolean validateResponseCode()
    {
        //noinspection SimplifiableIfStatement
        if (this.getRetriever() instanceof HTTPRetriever)
            return this.validateHTTPResponseCode();
        else if (this.getRetriever() instanceof JarRetriever)
            return this.validateJarResponseCode();
        else if (this.getRetriever() instanceof LocalRasterServerRetriever)
            return true;

        return false;
    }

    /**
     * Checks the retrieval's HTTP response code. Must only be called when the retriever is a subclass of {@link
     * gov.nasa.worldwind.retrieve.HTTPRetriever}.
     *
     * @return true if the response code is {@link HttpURLConnection#HTTP_OK}, otherwise false.
     */
    protected boolean validateHTTPResponseCode()
    {
        HTTPRetriever htr = (HTTPRetriever) this.getRetriever();

        return htr.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    /**
     * Checks the retrieval's HTTP response code. Must only be called when the retriever is a subclass of {@link
     * gov.nasa.worldwind.retrieve.HTTPRetriever}.
     *
     * @return true if the response code is {@link HttpURLConnection#HTTP_OK}, otherwise false.
     */
    protected boolean validateJarResponseCode()
    {
        JarRetriever htr = (JarRetriever) this.getRetriever();

        return htr.getResponseCode() == HttpURLConnection.HTTP_OK; // Re-using the HTTP response code for OK
    }

    /**
     * Handle the case of an invalid response code. Subclasses can override this method to handle special cases. The
     * default implementation calls {@link #markResourceAbsent()} and logs the contents of the retrieval buffer if it
     * contains content of type "text".
     */
    protected void handleInvalidResponseCode()
    {
        this.markResourceAbsent();

        if (this.isWMSException())
            this.handleWMSExceptionContent();

        else if (this.isPrimaryContentType("text", this.getRetriever().getContentType()))
            this.logTextBuffer(this.getRetriever().getBuffer()); // the buffer might contain error info, so log it
    }

    /**
     * Marks the retrieval target absent. Subclasses should override this method if they keep track of absent-resources.
     * The default implementation does nothing.
     */
    protected void markResourceAbsent()
    {
    }

    /**
     * Saves the retrieved and possibly transformed data. The data may have been transformed during content handling.
     * <p>
     * The default implementation of this method simply calls {@link #saveBuffer(java.nio.ByteBuffer)} with an argument
     * of null.
     *
     * @return true if the buffer was saved, false if the output file could not be determined or already exists and not
     *         overwritten.
     *
     * @throws IOException if an IO error occurs while attempting to save the buffer.
     */
    protected boolean saveBuffer() throws IOException
    {
        return this.saveBuffer(null);
    }

    /**
     * Saves the retrieved and possibly transformed data. The data may have been transformed during content handling.
     * The data is not saved if the output file already exists unless {@link #overwriteExistingFile()} returns true.
     *
     * @param buffer the buffer to save.
     *
     * @return true if the buffer was saved, false if the output file could not be determined or already exists and not
     *         overwritten.
     *
     * @throws IOException if an IO error occurred when attempting to save the buffer.
     */
    protected boolean saveBuffer(ByteBuffer buffer) throws IOException
    {
        File outFile = this.getOutputFile();

        if (outFile == null)
            return false;

        if (outFile.exists() && !this.overwriteExistingFile())
            return false;

        synchronized (this.getFileLock()) // synchronize with read of file in another class
        {
            WWIO.saveBuffer(buffer != null ? buffer : this.getRetriever().getBuffer(), outFile);
        }

        return true;
    }

    /**
     * Determines and returns the output file for the retrieved data.
     *
     * @return the output file, or null if a file could not be determined.
     */
    protected File getOutputFile()
    {
        File outFile = this.doGetOutputFile();

        if (outFile != null && this.isDeleteOnExit(outFile))
            outFile.deleteOnExit();

        return outFile;
    }

    /**
     * Indicates whether the retrieved data should be written to the output file if a file of the same name already
     * exists. The default implementation of this method returns false (files are not overwritten).
     *
     * @return true if an existing file should be overwritten, otherwise false.
     */
    protected boolean overwriteExistingFile()
    {
        return false;
    }

    /**
     * Indicates whether the output file should have its delete-on-exit flag set so that it's deleted when the JVM
     * terminates.
     *
     * @param outFile the output file.
     *
     * @return true if the output file's delete-on-exit flag should be set, otherwise false.
     */
    protected boolean isDeleteOnExit(File outFile)
    {
        return !outFile.exists() && this.avList != null && this.avList.getValue(AVKey.DELETE_CACHE_ON_EXIT) != null;
    }

    /**
     * Returns an object that can be used to synchronize writing to the output file. Superclasses should override this
     * method and return the object used as a lock by other objects that read or otherwise interact with the output
     * file.
     *
     * @return an object to use for read/write synchronization, or null if no lock is needed.
     */
    protected Object getFileLock()
    {
        return this;
    }

    protected boolean isPrimaryContentType(String typeOfContent, String contentType)
    {
        if (WWUtil.isEmpty(contentType) || WWUtil.isEmpty(typeOfContent))
            return false;

        return contentType.trim().toLowerCase().startsWith(typeOfContent);
    }

    protected boolean isWMSException()
    {
        String contentType = this.getRetriever().getContentType();

        if (WWUtil.isEmpty(contentType))
            return false;

        return contentType.trim().equalsIgnoreCase("application/vnd.ogc.se_xml");
    }

    /**
     * Process the retrieved data. Dispatches content handling to content-type specific handlers: {@link
     * #handleZipContent()} for content types containing "zip", {@link #handleTextContent()} for content types starting
     * with "text", and {@link #handleImageContent()} for contents types starting with "image".
     *
     * @return a buffer containing the retrieved data, which may have been transformed during content handling.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleContent() throws IOException
    {
        String contentType = this.getRetriever().getContentType();
        if (WWUtil.isEmpty(contentType))
        {
            // Try to determine the content type from the URL's suffix, if any.
            String suffix = WWIO.getSuffix(this.getRetriever().getName().split(";")[0]);
            if (!WWUtil.isEmpty(suffix))
                contentType = WWIO.makeMimeTypeForSuffix(suffix);

            if (WWUtil.isEmpty(contentType))
            {
                Logging.logger().severe(Logging.getMessage("nullValue.ContentTypeIsNullOrEmpty"));
                return null;
            }
        }
        contentType = contentType.trim().toLowerCase();

        if (this.isWMSException())
            return this.handleWMSExceptionContent();

        if (contentType.contains("zip"))
            return this.handleZipContent();

        if (this.isPrimaryContentType("text", contentType))
            return this.handleTextContent();

        if (this.isPrimaryContentType("image", contentType))
            return this.handleImageContent();

        if (this.isPrimaryContentType("application", contentType))
            return this.handleApplicationContent();

        return this.handleUnknownContentType();
    }

    /**
     * Reacts to exceptions occurring during content handling. Subclasses may override this method to perform special
     * exception handling. The default implementation logs a message specific to the exception.
     *
     * @param e the exception to handle.
     */
    protected void handleContentException(Exception e)
    {
        if (e instanceof ClosedByInterruptException)
        {
            Logging.logger().log(java.util.logging.Level.FINE,
                Logging.getMessage("generic.OperationCancelled",
                    "retrieval post-processing for " + this.getRetriever().getName()), e);
        }
        else if (e instanceof IOException)
        {
            this.markResourceAbsent();
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.ExceptionWhileSavingRetreivedData", this.getRetriever().getName()), e);
        }
    }

    /**
     * Handles content types that are not recognized by the content handler. Subclasses may override this method to
     * handle such cases. The default implementation logs an error message and returns null.
     *
     * @return null if no further processing should occur, otherwise the retrieved data, perhaps transformed.
     */
    protected ByteBuffer handleUnknownContentType()
    {
        Logging.logger().log(java.util.logging.Level.WARNING,
            Logging.getMessage("generic.UnknownContentType", this.getRetriever().getContentType()));

        return null;
    }

    /**
     * Handles Text content. If the content type is text/xml, {@link #handleXMLContent()} is called. If the content type
     * is text/html, {@link #handleHTMLContent()} is called. For all other sub-types the content is logged as a message
     * with level {@link java.util.logging.Level#SEVERE}.
     *
     * @return a buffer containing the retrieved text.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleTextContent() throws IOException
    {
        String contentType = this.getRetriever().getContentType().trim().toLowerCase();

        if (contentType.contains("xml"))
            return this.handleXMLContent();

        if (contentType.contains("html"))
            return this.handleHTMLContent();

        this.logTextBuffer(this.getRetriever().getBuffer());

        return null;
    }

    /**
     * Handles XML content. The default implementation only calls {@link #logTextBuffer(java.nio.ByteBuffer)} and
     * returns.
     *
     * @return a buffer containing the retrieved XML.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleXMLContent() throws IOException
    {
        this.logTextBuffer(this.getRetriever().getBuffer());

        return null;
    }

    /**
     * Handles HTML content. The default implementation only calls {@link #logTextBuffer(java.nio.ByteBuffer)} and
     * returns.
     *
     * @return a buffer containing the retrieved HTML.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleHTMLContent() throws IOException
    {
        this.logTextBuffer(this.getRetriever().getBuffer());

        return null;
    }

    /**
     * Log the content of a buffer as a String. If the buffer is null or empty, nothing is logged. Only the first 2,048
     * characters of the buffer are included in the log message.
     *
     * @param buffer the content to log. The content is assumed to be of type "text".
     */
    protected void logTextBuffer(ByteBuffer buffer)
    {
        if (buffer == null || !buffer.hasRemaining())
            return;

        Logging.logger().warning(WWIO.byteBufferToString(buffer, 2048, null));
    }

    /**
     * Handles zipped content. The default implementation saves the data to the retriever's output file without
     * unzipping it.
     *
     * @return a buffer containing the retrieved data.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleZipContent() throws IOException
    {
        File outFile = this.getOutputFile();
        if (outFile == null)
            return null;

        this.saveBuffer();

        return this.getRetriever().getBuffer();
    }

    /**
     * Handles application content. The default implementation saves the retrieved data without modification via {@link
     * #saveBuffer()} without.
     *
     * @return a buffer containing the retrieved data.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleApplicationContent() throws IOException
    {
        this.saveBuffer();

        return this.getRetriever().getBuffer();
    }

    /**
     * Handles WMS exceptions.
     *
     * @return a buffer containing the retrieved XML.
     */
    protected ByteBuffer handleWMSExceptionContent()
    {
        // TODO: Parse the xml and include only the message text in the log message.

        StringBuilder sb = new StringBuilder(this.getRetriever().getName());

        sb.append("\n");
        sb.append(WWIO.byteBufferToString(this.getRetriever().getBuffer(), 2048, null));
        Logging.logger().warning(sb.toString());

        return null;
    }

    /**
     * Handles image content. The default implementation simply saves the retrieved data via {@link #saveBuffer()},
     * first converting it to DDS if the suffix of the output file is .dds.
     * <p>
     * The default implementation of this method returns immediately if the output file cannot be determined or it
     * exists and {@link #overwriteExistingFile()} returns false.
     *
     * @return a buffer containing the retrieved data.
     *
     * @throws IOException if an IO error occurs while processing the data.
     */
    protected ByteBuffer handleImageContent() throws IOException
    {
        // BE CAREFUL: This method may be overridden by subclasses to handle special image cases. It's also implemented
        // to handle elevations as images correctly (just save them to the filestore).

        File outFile = this.getOutputFile();
        if (outFile == null || (outFile.exists() && !this.overwriteExistingFile()))
            return this.getRetriever().getBuffer();

        if (outFile.getPath().endsWith("dds"))
            return this.saveDDS();

        BufferedImage image = this.transformPixels();

        if (image != null)
        {
            synchronized (this.getFileLock()) // synchronize with read of file in another class
            {
                ImageIO.write(image, this.getRetriever().getContentType().split("/")[1], outFile);
            }
        }
        else
            this.saveBuffer();

        return this.getRetriever().getBuffer();
    }

    /**
     * Transform the retrieved data in some purpose-specific way. May be overridden by subclasses to perform special
     * transformations. The default implementation calls {@link ImageUtil#mapTransparencyColors(java.awt.image.BufferedImage,
     * int[])} if the attribute-value list specified at construction contains transparency colors (includes the {@link
     * AVKey#TRANSPARENCY_COLORS} key).
     *
     * @return returns the transformed data if a transform is performed, otherwise returns the original data.
     */
    protected BufferedImage transformPixels()
    {
        if (this.avList != null)
        {
            int[] colors = (int[]) this.avList.getValue(AVKey.TRANSPARENCY_COLORS);
            if (colors != null)
                return ImageUtil.mapTransparencyColors(this.getRetriever().getBuffer(), colors);
        }

        return null;
    }

    /**
     * Saves a DDS image file after first converting any other image format to DDS.
     *
     * @return the converted image data if a conversion is performed, otherwise the original image data.
     *
     * @throws IOException if an IO error occurs while converting or saving the image.
     */
    protected ByteBuffer saveDDS() throws IOException
    {
        ByteBuffer buffer = this.getRetriever().getBuffer();

        if (!this.getRetriever().getContentType().contains("dds"))
            buffer = this.convertToDDS();

        this.saveBuffer(buffer);

        return buffer;
    }

    /**
     * Converts an image to DDS. If the image format is not originally DDS, calls {@link #transformPixels()} to perform
     * any defined image transform.
     *
     * @return the converted image data if a conversion is performed, otherwise the original image data.
     *
     * @throws IOException if an IO error occurs while converting the image.
     */
    protected ByteBuffer convertToDDS() throws IOException
    {
        ByteBuffer buffer;

        BufferedImage image = this.transformPixels();
        if (image != null)
            buffer = DDSCompressor.compressImage(image);
        else
            buffer = DDSCompressor.compressImageBuffer(this.getRetriever().getBuffer());

        return buffer;
    }
}
