/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.ogc.kml.io.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.zip.*;

/**
 * Parses a KML or KMZ document and provides access to its contents. Instructions for parsing KML/KMZ files and streams
 * are given in the Description section of {@link gov.nasa.worldwind.ogc.kml}.
 *
 * @author tag
 * @version $Id: KMLRoot.java 1951 2014-04-20 18:57:50Z tgaskins $
 */
public class KMLRoot extends KMLAbstractObject implements KMLRenderable
{
    /** Reference to the KMLDoc representing the KML or KMZ file. */
    protected KMLDoc kmlDoc;
    /** The event reader used to parse the document's XML. */
    protected XMLEventReader eventReader;
    /** The input stream underlying the event reader. */
    protected InputStream eventStream;
    /** The parser context for the document. */
    protected KMLParserContext parserContext;
    /**
     * The <code>PropertyChangeSupport</code> that receives property change events this KMLRoot listens for, and sends
     * property change events to this KMLRoot's listeners. Lazily initialized in <code>getChangeSupport</code>.
     * Initially <code>null</code>.
     */
    protected PropertyChangeSupport propertyChangeSupport;
    /**
     * Indicates this KML root's detail hint. Modifies the default relationship of KML scene resolution to screen
     * resolution as viewing distance changes. Values greater than 0 increase the resolution. Values less than 0
     * decrease the resolution. Initially 0.
     */
    protected double detailHint;
    /** Flag to indicate that the feature has been fetched from the hash map. */
    protected boolean featureFetched = false;
    protected KMLAbstractFeature feature;

    /** Flag to indicate that the network link control element has been fetched from the hash map. */
    protected boolean linkControlFetched = false;
    protected KMLNetworkLinkControl networkLinkControl;

    protected AbsentResourceList absentResourceList = new AbsentResourceList();

    /**
     * Creates a KML root for an untyped source. The source must be either a {@link File}, a {@link URL}, a {@link
     * InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     *
     * @param docSource either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String} identifying
     *                  a file path or URL.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     */
    public static KMLRoot create(Object docSource) throws IOException
    {
        return create(docSource, true);
    }

    /**
     * Creates a KML root for an untyped source. The source must be either a {@link File}, a {@link URL}, a {@link
     * InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     *
     * @param docSource      either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String}
     *                       identifying a file path or URL.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     */
    public static KMLRoot create(Object docSource, boolean namespaceAware) throws IOException
    {
        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (docSource instanceof File)
            return new KMLRoot((File) docSource, namespaceAware);
        else if (docSource instanceof URL)
            return new KMLRoot((URL) docSource, null, namespaceAware);
        else if (docSource instanceof InputStream)
            return new KMLRoot((InputStream) docSource, null, namespaceAware);
        else if (docSource instanceof String)
        {
            File file = new File((String) docSource);
            if (file.exists())
                return new KMLRoot(file, namespaceAware);

            URL url = WWIO.makeURL(docSource);
            if (url != null)
                return new KMLRoot(url, null, namespaceAware);
        }

        return null;
    }

    /**
     * Creates a KML root for an untyped source and parses it. The source must be either a {@link File}, a {@link URL},
     * a {@link InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     * <p/>
     * Note: Because there are so many incorrectly formed KML files in distribution, it's often not possible to parse
     * with a namespace aware parser. This method first tries to use a namespace aware parser, but if a severe problem
     * occurs during parsing, it will try again using a namespace unaware parser. Namespace unaware parsing typically
     * bypasses many problems, but it also causes namespace qualified elements in the XML to be unrecognized.
     *
     * @param docSource either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String} identifying
     *                  a file path or URL.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     * @throws javax.xml.stream.XMLStreamException
     *                                  if the KML file has severe errors.
     */
    public static KMLRoot createAndParse(Object docSource) throws IOException, XMLStreamException
    {
        KMLRoot kmlRoot = KMLRoot.create(docSource);

        if (kmlRoot == null)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                docSource.toString());
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Try with a namespace aware parser.
            kmlRoot.parse();
        }
        catch (XMLStreamException e)
        {
            // Try without namespace awareness.
            kmlRoot = KMLRoot.create(docSource, false);
            kmlRoot.parse();
        }

        return kmlRoot;
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files from
     * either files or input streams.
     *
     * @param docSource the KMLDoc instance representing the KML document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(KMLDoc docSource) throws IOException
    {
        this(docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files from
     * either files or input streams.
     *
     * @param docSource      the KMLDoc instance representing the KML document.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(KMLDoc docSource, boolean namespaceAware) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.kmlDoc = docSource;

        this.initialize(namespaceAware);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link File}.
     *
     * @param docSource the File containing the document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(File docSource) throws IOException
    {
        this(docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link File}.
     *
     * @param docSource      the File containing the document.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(File docSource, boolean namespaceAware) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWIO.isContentType(docSource, KMLConstants.KML_MIME_TYPE))
            this.kmlDoc = new KMLFile(docSource);
        else if (WWIO.isContentType(docSource, KMLConstants.KMZ_MIME_TYPE))
        {
            try
            {
                this.kmlDoc = new KMZFile(docSource);
            }
            catch (ZipException e)
            {
                // We've encountered some zip files that will not open with ZipFile, but will open
                // with ZipInputStream. Try again, this time opening treating the file as a stream.
                // See WWJINT-282.
                this.kmlDoc = new KMZInputStream(new FileInputStream(docSource));
            }
        }
        else
            throw new WWUnrecognizedException(Logging.getMessage("KML.UnrecognizedKMLFileType"));

        this.initialize(namespaceAware);
    }

    /**
     * Create a new <code>KMLRoot</code> for an {@link InputStream}.
     *
     * @param docSource   the input stream containing the document.
     * @param contentType the content type of the stream data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML
     *                    and {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is treated as KML for any other
     *                    value or a value of null.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(InputStream docSource, String contentType) throws IOException
    {
        this(docSource, contentType, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for an {@link InputStream}.
     *
     * @param docSource      the input stream containing the document.
     * @param contentType    the content type of the stream data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain
     *                       KML and {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is treated as KML for any
     *                       other value or a value of null.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(InputStream docSource, String contentType, boolean namespaceAware) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (contentType != null && contentType.equals(KMLConstants.KMZ_MIME_TYPE))
            this.kmlDoc = new KMZInputStream(docSource);
        else if (contentType == null && docSource instanceof ZipInputStream)
            this.kmlDoc = new KMZInputStream(docSource);
        else
            this.kmlDoc = new KMLInputStream(docSource, null);

        this.initialize(namespaceAware);
    }

    /**
     * Create a <code>KMLRoot</code> for a {@link URL}.
     *
     * @param docSource   the URL identifying the document.
     * @param contentType the content type of the data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML and
     *                    {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. Any other non-null value causes the content to be
     *                    treated as plain KML. If null is specified the content type is read from the server or other
     *                    end point of the URL. When a content type is specified, the content type returned by the URL's
     *                    end point is ignored. You can therefore force the content to be treated as KML or KMZ
     *                    regardless of what a server declares it to be.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the document.
     */
    public KMLRoot(URL docSource, String contentType) throws IOException
    {
        this(docSource, contentType, true);
    }

    /**
     * Create a <code>KMLRoot</code> for a {@link URL}.
     *
     * @param docSource      the URL identifying the document.
     * @param contentType    the content type of the data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML and
     *                       {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. Any other non-null value causes the content to
     *                       be treated as plain KML. If null is specified the content type is read from the server or
     *                       other end point of the URL. When a content type is specified, the content type returned by
     *                       the URL's end point is ignored. You can therefore force the content to be treated as KML or
     *                       KMZ regardless of what a server declares it to be.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the document.
     */
    public KMLRoot(URL docSource, String contentType, boolean namespaceAware) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URLConnection conn = docSource.openConnection();
        if (contentType == null)
            contentType = conn.getContentType();

        if (!(KMLConstants.KMZ_MIME_TYPE.equals(contentType) || KMLConstants.KML_MIME_TYPE.equals(contentType)))
            contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(docSource.getPath()));

        if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
            this.kmlDoc = new KMZInputStream(conn.getInputStream());
        else
            this.kmlDoc = new KMLInputStream(conn.getInputStream(), WWIO.makeURI(docSource));

        this.initialize(namespaceAware);
    }

    /**
     * Create a new <code>KMLRoot</code> with a specific namespace. (The default namespace is defined by {@link
     * gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
     *
     * @param namespaceURI the default namespace URI.
     * @param docSource    the KML source specified via a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files
     *                     from either files or input streams.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws java.io.IOException      if an I/O error occurs attempting to open the document source.
     */
    public KMLRoot(String namespaceURI, KMLDoc docSource) throws IOException
    {
        this(namespaceURI, docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> with a specific namespace. (The default namespace is defined by {@link
     * gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
     *
     * @param namespaceURI   the default namespace URI.
     * @param docSource      the KML source specified via a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ
     *                       files from either files or input streams.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws java.io.IOException      if an I/O error occurs attempting to open the document source.
     */
    public KMLRoot(String namespaceURI, KMLDoc docSource, boolean namespaceAware) throws IOException
    {
        super(namespaceURI);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.kmlDoc = docSource;
        this.initialize(namespaceAware);
    }

    /**
     * Called just before the constructor returns. If overriding this method be sure to invoke
     * <code>super.initialize(boolean)</code>.
     *
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws java.io.IOException if an I/O error occurs attempting to open the document source.
     */
    protected void initialize(boolean namespaceAware) throws IOException
    {
        this.eventStream = this.getKMLDoc().getKMLStream();
        this.eventReader = this.createReader(this.eventStream, namespaceAware);
        if (this.eventReader == null)
            throw new WWRuntimeException(Logging.getMessage("XML.UnableToOpenDocument", this.getKMLDoc()));

        this.parserContext = this.createParserContext(this.eventReader);
    }

    /**
     * Creates the event reader. Called from the constructor.
     *
     * @param docSource      the document source to create a reader for. The type can be any of those supported by
     *                       {@link WWXML#openEventReader(Object)}.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @return a new event reader, or null if the source type cannot be determined.
     */
    protected XMLEventReader createReader(Object docSource, boolean namespaceAware)
    {
        return WWXML.openEventReader(docSource, namespaceAware);
    }

    /**
     * Invoked during {@link #initialize(boolean)} to create the parser context. The parser context is created by the
     * global {@link XMLEventParserContextFactory}.
     *
     * @param reader the reader to associate with the parser context.
     *
     * @return a new parser context.
     */
    protected KMLParserContext createParserContext(XMLEventReader reader)
    {
        KMLParserContext ctx = (KMLParserContext)
            XMLEventParserContextFactory.createParserContext(KMLConstants.KML_MIME_TYPE, this.getNamespaceURI());

        if (ctx == null)
        {
            // Register a parser context for this root's default namespace
            String[] mimeTypes = new String[] {KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE};
            XMLEventParserContextFactory.addParserContext(mimeTypes, new KMLParserContext(this.getNamespaceURI()));
            ctx = (KMLParserContext)
                XMLEventParserContextFactory.createParserContext(KMLConstants.KML_MIME_TYPE, this.getNamespaceURI());
        }

        ctx.setEventReader(reader);

        return ctx;
    }

    /**
     * Specifies the object to receive notifications of important occurrences during parsing, such as exceptions and the
     * occurrence of unrecognized element types.
     * <p/>
     * The default notification listener writes a message to the log, and otherwise does nothing.
     *
     * @param listener the listener to receive notifications. Specify null to indicate no listener.
     *
     * @see gov.nasa.worldwind.util.xml.XMLParserNotification
     */
    public void setNotificationListener(final XMLParserNotificationListener listener)
    {
        if (listener == null)
        {
            this.parserContext.setNotificationListener(null);
        }
        else
        {
            this.parserContext.setNotificationListener(new XMLParserNotificationListener()
            {
                public void notify(XMLParserNotification notification)
                {
                    // Set up so the user sees the notification coming from the root rather than the parser
                    notification.setSource(KMLRoot.this);
                    listener.notify(notification);
                }
            });
        }
    }

    /**
     * Returns the KML document for this <code>KMLRoot</code>.
     *
     * @return the KML document for this root.
     */
    public KMLDoc getKMLDoc()
    {
        return this.kmlDoc;
    }

    /**
     * Finds a named element in the document.
     *
     * @param id the element's identifier. If null, null is returned.
     *
     * @return the element requested, or null if there is no corresponding element in the document.
     */
    public Object getItemByID(String id)
    {
        return id != null ? this.getParserContext().getIdTable().get(id) : null;
    }

    public String getSupportFilePath(String link) throws IOException
    {
        return this.getKMLDoc().getSupportFilePath(link);
    }

    /**
     * Resolves a reference to a remote or local element of the form address#identifier, where "address" identifies a
     * local or remote document, including the current document, and and "identifier" is the id of the desired element.
     * <p/>
     * If the address part identifies the current document, the document is searched for the specified identifier.
     * Otherwise the document is retrieved, opened and searched for the identifier. If the address refers to a remote
     * document and the document has not previously been retrieved and cached locally, retrieval is initiated and this
     * method returns <code>null</code>. Once the document is successfully retrieved, subsequent calls to this method
     * return the identified element, if it exists.
     * <p/>
     * If the link does not contain an identifier part, this initiates a retrieval for document referenced by the
     * address part and returns <code>null</code>. Once the document is retrieved this opens the the document as a
     * <code>KMLRoot</code>. Subsequent calls to this method return the opened document, if it exists.
     *
     * @param link the document address in the form address#identifier.
     *
     * @return the requested document, the requested or element within a document, or <code>null</code> if the document
     *         or the element are not found.
     *
     * @throws IllegalArgumentException if the <code>link</code> is <code>null</code>.
     */
    public Object resolveReference(String link)
    {
        if (link == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (absentResourceList.isResourceAbsent(link))
            return null;

        // Store remote files in the World Wind cache by default. This provides backward compatibility with applications
        // depending on resolveReference's behavior prior to the addition of the cacheRemoteFile parameter.
        Object o = this.resolveReference(link, true);

        if (o == null)
            absentResourceList.markResourceAbsent(link);
        else
            absentResourceList.unmarkResourceAbsent(link);

        return o;
    }

    /**
     * Resolves a reference to a remote or local element of the form address#identifier, where "address" identifies a
     * local or remote document, including the current document, and and "identifier" is the id of the desired element.
     * <p/>
     * If the address part identifies the current document, the document is searched for the specified identifier.
     * Otherwise the document is retrieved, opened and searched for the identifier. If the address refers to a remote
     * document and the document has not previously been retrieved and cached locally, retrieval is initiated and this
     * method returns <code>null</code>. Once the document is successfully retrieved, subsequent calls to this method
     * return the identified element, if it exists.
     * <p/>
     * If the link does not contain an identifier part, this initiates a retrieval for document referenced by the
     * address part and returns <code>null</code>. Once the document is retrieved this opens the the document as a
     * <code>KMLRoot</code>. Subsequent calls to this method return the opened document, if it exists.
     * <p/>
     * The <code>cacheRemoteFile</code> parameter specifies whether to store a retrieved remote document in the World
     * Wind cache or in a temporary location. This parameter has no effect if the document exists locally. The temporary
     * location for a retrieved document does not persist between runtime sessions, and subsequent invocations of this
     * method may not return the same temporary location.
     *
     * @param link            the document address in the form address#identifier.
     * @param cacheRemoteFile <code>true</code> to store remote documents in the World Wind cache, or <code>false</code>
     *                        to store remote documents in a temporary location. Has no effect if the address is a local
     *                        document.
     *
     * @return the requested document, the requested or element within a document, or <code>null</code> if the document
     *         or the element are not found.
     *
     * @throws IllegalArgumentException if the <code>link</code> is <code>null</code>.
     */
    public Object resolveReference(String link, boolean cacheRemoteFile)
    {
        if (link == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String[] linkParts = link.split("#");
            String linkBase = linkParts[0];
            String linkRef = linkParts.length > 1 ? linkParts[1] : null;

            // See if it's a reference to an internal element.
            if (WWUtil.isEmpty(linkBase) && !WWUtil.isEmpty(linkRef))
                return this.getItemByID(linkRef);

            // Interpret the path relative to the current document.
            String path = this.getSupportFilePath(linkBase);
            if (path == null)
                path = linkBase;

            // See if it's an already found and parsed KML file.
            Object o = WorldWind.getSessionCache().get(path);
            if (o != null && o instanceof KMLRoot)
                return linkRef != null ? ((KMLRoot) o).getItemByID(linkRef) : o;

            URL url = WWIO.makeURL(path);
            if (url == null)
            {
                // See if the reference can be resolved to a local file.
                o = this.resolveLocalReference(path, linkRef);
            }

            // If we didn't find a local file, treat it as a remote reference.
            if (o == null)
                o = this.resolveRemoteReference(path, linkRef, cacheRemoteFile);

            if (o != null)
                return o;

            // If the reference was not resolved as a remote reference, look for a local element identified by the
            // reference string. This handles the case of malformed internal references that omit the # sign at the
            // beginning of the reference.
            return this.getItemByID(link);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }

        return null;
    }

    /**
     * Resolves a reference to a local element identified by address and identifier, where {@code linkBase} identifies a
     * document, including the current document, and {@code linkRef} is the id of the desired element.
     * <p/>
     * If {@code linkBase} refers to a local KML or KMZ file and {@code linkRef} is non-null, the return value is the
     * element identified by {@code linkRef}. If {@code linkRef} is null, the return value is a parsed {@link KMLRoot}
     * for the KML file identified by {@code linkBase}.
     * <p/>
     * If {@code linkBase} refers a local file that is not a KML or KMZ file then {@code linkBase} is returned. If
     * {@code linkBase} cannot be resolved to a local file then null is returned.
     *
     * @param linkBase the address of the document containing the requested element.
     * @param linkRef  the element's identifier.
     *
     * @return the requested element, or null if the element is not found.
     *
     * @throws IllegalArgumentException if the address is null.
     */
    public Object resolveLocalReference(String linkBase, String linkRef)
    {
        if (linkBase == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            File file = new File(linkBase);
            if (!file.exists())
                return null;

            // Determine whether the file is a KML or KMZ. If it's not just return the original address.
            if (!WWIO.isContentType(file, KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE))
                return linkBase;

            // Attempt to open and parse the KML/Z file, trying both namespace aware and namespace unaware stream
            // readers if necessary.
            KMLRoot refRoot = KMLRoot.createAndParse(file);
            // An exception is thrown if parsing fails, so no need to check for null.

            // Add the parsed file to the session cache so it doesn't have to be parsed again.
            WorldWind.getSessionCache().put(linkBase, refRoot);

            // Now check the newly opened KML/Z file for the referenced item, if a reference was specified.
            if (linkRef != null)
                return refRoot.getItemByID(linkRef);
            else
                return refRoot;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
            Logging.logger().warning(message);
            return null;
        }
    }

    /**
     * Resolves a reference to a remote element identified by address and identifier, where {@code linkBase} identifies
     * a remote document, and {@code linkRef} is the id of the desired element. This method retrieves resources
     * asynchronously using the {@link gov.nasa.worldwind.cache.FileStore}.
     * <p/>
     * The return value is null if the file is not yet available in the FileStore. If {@code linkBase} refers to a KML
     * or KMZ file and {@code linkRef} is non-null, the return value is the element identified by {@code linkRef}. If
     * {@code linkBase} refers to a KML or KMZ and {@code linkRef} is null, the return value is a parsed {@link KMLRoot}
     * for the KML file identified by {@code linkBase}. Otherwise the return value is a {@link URL} to the file in the
     * file cache.
     *
     * @param linkBase the address of the document containing the requested element.
     * @param linkRef  the element's identifier.
     *
     * @return URL to the requested file, parsed KMLRoot, or KML feature. Returns null if the document is not yet
     *         available in the FileStore.
     *
     * @throws IllegalArgumentException if the {@code linkBase} is null.
     */
    public Object resolveRemoteReference(String linkBase, String linkRef)
    {
        if (linkBase == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Store remote files in the World Wind cache by default. This provides backward compatibility with applications
        // depending on resolveRemoteReference's behavior prior to the addition of the cacheRemoteFile parameter.
        return this.resolveRemoteReference(linkBase, linkRef, true);
    }

    /**
     * Resolves a reference to a remote element identified by address and identifier, where {@code linkBase} identifies
     * a remote document, and {@code linkRef} is the id of the desired element. This method retrieves resources
     * asynchronously using the {@link gov.nasa.worldwind.cache.FileStore}.
     * <p/>
     * The return value is null if the file is not yet available in the FileStore. If {@code linkBase} refers to a KML
     * or KMZ file and {@code linkRef} is non-null, the return value is the element identified by {@code linkRef}. If
     * {@code linkBase} refers to a KML or KMZ and {@code linkRef} is null, the return value is a parsed {@link KMLRoot}
     * for the KML file identified by {@code linkBase}. Otherwise the return value is a {@link URL} to the file in the
     * file cache or a temporary location, depending on the value of <code>cacheRemoteFile</code>.
     * <p/>
     * The <code>cacheRemoteFile</code> parameter specifies whether to store a retrieved remote file in the World Wind
     * cache or in a temporary location. This parameter has no effect if the file exists locally. The temporary location
     * for a retrieved file does not persist between runtime sessions, and subsequent invocations of this method may not
     * return the same temporary location.
     *
     * @param linkBase        the address of the document containing the requested element.
     * @param linkRef         the element's identifier.
     * @param cacheRemoteFile <code>true</code> to store remote files in the World Wind cache, or <code>false</code> to
     *                        store remote files in a temporary location. Has no effect if the address is a local file.
     *
     * @return URL to the requested file, parsed KMLRoot, or KML feature. Returns null if the document is not yet
     *         available in the FileStore.
     *
     * @throws IllegalArgumentException if the {@code linkBase} is null.
     */
    public Object resolveRemoteReference(String linkBase, String linkRef, boolean cacheRemoteFile)
    {
        if (linkBase == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // See if it's in the cache. If not, requestFile will start another thread to retrieve it and return null.
            URL url = WorldWind.getDataFileStore().requestFile(linkBase, cacheRemoteFile);
            if (url == null)
                return null;

            // It's in the cache. If it's a KML/Z, try to parse it so we can search for the specified reference. If it's
            // not KML/Z, just return the url for the cached file.
            String contentType = WorldWind.getDataFileStore().getContentType(linkBase);
            if (contentType == null)
            {
                String suffix = WWIO.getSuffix(linkBase.split(";")[0]); // strip of trailing garbage
                if (!WWUtil.isEmpty(suffix))
                    contentType = WWIO.makeMimeTypeForSuffix(suffix);
            }

            if (!this.canParseContentType(contentType))
                return url;

            // If the file is a KML or KMZ document, attempt to open it. We can't open it as a File with createAndParse
            // because the KMLRoot that will be created needs to have the remote address in order to resolve any
            // relative references within it, so we have to implement the namespace-aware/namespace-unaware attempts
            // here.
            KMLRoot refRoot;
            try
            {
                // Try to parse with a namespace-aware event stream.
                refRoot = this.parseCachedKMLFile(url, linkBase, contentType, true);
            }
            catch (XMLStreamException e)
            {
                // Well that didn't work, so try with a namespace-unaware event stream. If this attempt fails this
                // method logs the exception and returns null.
                refRoot = this.parseCachedKMLFile(url, linkBase, contentType, false);
            }

            // If the file could not be parsed as KML, then just return the URL.
            if (refRoot == null)
                return url;

            // Add the parsed file to the session cache so it doesn't have to be parsed again.
            WorldWind.getSessionCache().put(linkBase, refRoot);

            // Now check the newly opened KML/Z file for the referenced item, if a reference was specified.
            if (linkRef != null)
                return refRoot.getItemByID(linkRef);
            else
                return refRoot;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
            Logging.logger().warning(message);
            return null;
        }
    }

    /**
     * Resolves a NetworkLink to a local or remote KML document. This method retrieves remote resources asynchronously
     * using the {@link gov.nasa.worldwind.cache.FileStore}.
     * <p/>
     * The return value is a parsed KMLRoot representing the linked document. The return value is null if the linked
     * file is not a KML file, or is not yet available in the FileStore.
     * <p/>
     * The <code>cacheRemoteFile</code> parameter specifies whether to store a retrieved remote file in the World Wind
     * cache or in a temporary location. This parameter has no effect if the file exists locally. The temporary location
     * for a retrieved file does not persist between runtime sessions, and subsequent invocations of this method may not
     * return the same temporary location.
     *
     * @param link            the address to resolve
     * @param cacheRemoteFile <code>true</code> to store remote files in the World Wind cache, or <code>false</code> to
     *                        store remote files in a temporary location. Has no effect if the address is a local file.
     * @param updateTime      the time at which the link was last updated. If a cached file exists for the specified
     *                        resource, the file must have been retrieved after the link update time. Otherwise, the
     *                        cache entry is considered invalid, and the file is deleted and retrieved again.
     *
     * @return URL to the requested file, parsed KMLRoot, or KML feature. Returns null if the document is not yet
     *         available in the FileStore.
     *
     * @throws IllegalArgumentException if the {@code link} is null.
     */
    public Object resolveNetworkLink(String link, boolean cacheRemoteFile, long updateTime)
    {
        if (link == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object o = null;
        try
        {
            // Interpret the path relative to the current document.
            String path = this.getSupportFilePath(link);
            if (path == null)
                path = link;

            // If the file is eligible for caching, check the session cache to see if it has already been retrieved and
            // parsed.
            if (cacheRemoteFile)
            {
                o = WorldWind.getSessionCache().get(path);
                if (o instanceof KMLRoot)
                    return o;
            }

            URL url = WWIO.makeURL(path);
            if (url == null)
            {
                // See if the reference can be resolved to a local file.
                o = this.resolveLocalReference(path, null);
            }

            // If we didn't find a local file, treat it as a remote reference.
            if (o == null)
            {
                url = WorldWind.getDataFileStore().requestFile(path, cacheRemoteFile);
                if (url != null)
                {
                    // Check the file's modification time against the link update time. If the file was last modified
                    // earlier than the link update time then we need to remove the cached file from the file store,
                    // and start a new file retrieval.
                    File file = new File(url.toURI());
                    if (file.lastModified() < updateTime)
                    {
                        WorldWind.getDataFileStore().removeFile(link);
                    }
                }

                // Call resolveRemoteReference to retrieve and parse the file.
                o = this.resolveRemoteReference(path, null, cacheRemoteFile);
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }

        return o;
    }

    /**
     * Check a cached resource for expiration. If the resource is expired, evict it from the cache.
     *
     * @param link           Link that identifies the resource to check for expiration. This is the same link that was
     *                       passed to resolveReference to retrieve the resource.
     * @param expirationTime Time at which the resource expires, in milliseconds since the Epoch. If the current system
     *                       time is greater than the expiration time, then the resource will be evicted.
     */
    public void evictIfExpired(String link, long expirationTime)
    {
        try
        {
            URL url = WorldWind.getDataFileStore().requestFile(link, false);
            if (url != null)
            {
                // Check the file's modification time against the link update time. If the file was last modified
                // earlier than the link update time then we need to remove the cached file from the file store,
                // and start a new file retrieval.
                File file = new File(url.toURI());

                if (file.lastModified() < expirationTime)
                    WorldWind.getDataFileStore().removeFile(link);
            }
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }
    }

    /**
     * Returns the expiration time of a file retrieved by {@link #resolveReference(String) resolveReference} or {@link
     * #resolveNetworkLink(String, boolean, long) resolveNetworkLink}.
     *
     * @param link the address of the file (the same address as was previously passed to resolveReference). If null,
     *             zero is returned.
     *
     * @return The expiration time of the file, in milliseconds since the Epoch (January 1, 1970, 00:00:00 GMT). Zero
     *         indicates that there is no expiration time. Returns zero if te resource identified by {@code link} has
     *         not been retrieved.
     */
    public long getExpiration(String link)
    {
        try
        {
            if (link == null)
                return 0;

            // Interpret the path relative to the current document.
            String path = this.getSupportFilePath(link);
            if (path == null)
                path = link;

            return WorldWind.getDataFileStore().getExpirationTime(path);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }

        return 0;
    }

    /**
     * Determines if a MIME type can be parsed as KML or KMZ. Parsable types are the KML and KMZ MIME types, as well as
     * "text/plain" and "text/xml".
     *
     * @param mimeType Type to test. May be null.
     *
     * @return {@code true} if {@code mimeType} can be parsed as KML.
     */
    protected boolean canParseContentType(String mimeType)
    {
        return KMLConstants.KML_MIME_TYPE.equals(mimeType) || KMLConstants.KMZ_MIME_TYPE.equals(mimeType)
            || "text/plain".equals(mimeType) || "text/xml".equals(mimeType);
    }

    /**
     * Open and parse the specified file expressed as a file: URL..
     *
     * @param url            the URL of the file to open, expressed as a URL with a scheme of "file".
     * @param linkBase       the original address of the document if the file is a retrieved and cached file.
     * @param contentType    the mime type of the file's content, either a KML or KMZ mime type.
     * @param namespaceAware specifies whether to use a namespace aware event reader.
     *
     * @return A {@code KMLRoot} representing the file's KML contents.
     *
     * @throws IOException        if an I/O error occurs during opening and parsing.
     * @throws XMLStreamException if a server parsing error is encountered.
     */
    protected KMLRoot parseCachedKMLFile(URL url, String linkBase, String contentType, boolean namespaceAware)
        throws IOException, XMLStreamException
    {
        KMLDoc kmlDoc;

        InputStream refStream = url.openStream();

        if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
            kmlDoc = new KMZInputStream(refStream);
        else // Attempt to parse as KML
            kmlDoc = new KMLInputStream(refStream, WWIO.makeURI(linkBase));

        try
        {
            KMLRoot refRoot = new KMLRoot(kmlDoc, namespaceAware);
            refRoot = refRoot.parse(); // also closes the URL's stream
            return refRoot;
        }
        catch (XMLStreamException e)
        {
            refStream.close(); // parsing failed, so explicitly close the stream
            throw e;
        }
    }

    /**
     * Starts document parsing. This method initiates parsing of the KML document and returns when the full document has
     * been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if an exception occurs while attempting to read the event stream.
     */
    public KMLRoot parse(Object... args) throws XMLStreamException
    {
        KMLParserContext ctx = this.parserContext;

        try
        {
            for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
            {
                if (event == null)
                    continue;

                // Allow a <kml> element in any namespace
                if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("kml"))
                {
                    super.parse(ctx, event, args);
                    return this;
                }
                // Allow the document to start without a <kml> element. There are many such files around.
                else if (event.isStartElement() && ctx.getParser(event) != null)
                {
                    this.doParseEventContent(ctx, event, args);
                    return this;
                }
            }
        }
        finally
        {
            ctx.getEventReader().close();
            this.closeEventStream();
        }

        return null;
    }

    /** Closes the event stream associated with this context's XML event reader. */
    protected void closeEventStream()
    {
        try
        {
            this.eventStream.close();
            this.eventStream = null;
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionClosingXmlEventReader");
            Logging.logger().warning(message);
        }
    }

    protected XMLEventParserContext getParserContext()
    {
        return this.parserContext;
    }

    /**
     * Returns the <code>hint</code> attribute of the <code>KML</code> element (the document root).
     *
     * @return the hint attribute, or null if the attribute is not specified.
     */
    public String getHint()
    {
        return (String) this.getField("hint");
    }

    /**
     * Returns the {@link gov.nasa.worldwind.ogc.kml.KMLNetworkLinkControl} element if the document root contains it.
     *
     * @return the element if it is specified in the document, otherwise null.
     */
    public KMLNetworkLinkControl getNetworkLinkControl()
    {
        if (!linkControlFetched)
        {
            this.networkLinkControl = (KMLNetworkLinkControl) this.getField("NetworkLinkControl");
            this.linkControlFetched = true;
        }

        return this.networkLinkControl;
    }

    /**
     * Returns the KML <code>Feature</code> element contained in the document root.
     *
     * @return the feature element if it is specified in the document, otherwise null.
     */
    public KMLAbstractFeature getFeature()
    {
        if (!this.featureFetched)
        {
            this.feature = findFeature();
            this.featureFetched = true;
        }

        return this.feature;
    }

    /**
     * Searches this root's fields for the KML Feature element.
     *
     * @return the feature element, or null if none was found.
     */
    protected KMLAbstractFeature findFeature()
    {
        if (!this.hasFields())
            return null;

        for (Map.Entry<String, Object> entry : this.getFields().getEntries())
        {
            if (entry.getValue() instanceof KMLAbstractFeature)
                return (KMLAbstractFeature) entry.getValue();
        }

        return null;
    }

    /**
     * Indicates this KML root's detail hint, which is described in <code>{@link #setDetailHint(double)}</code>.
     *
     * @return the detail hint.
     *
     * @see #setDetailHint(double)
     */
    public double getDetailHint()
    {
        return this.detailHint;
    }

    /**
     * Specifies this KML root's detail hint. The detail hint modifies the default relationship of KML scene resolution
     * to screen resolution as the viewing distance changes. Values greater than 0 cause KML elements with a level of
     * detail to appear at higher resolution at greater distances than normal, but at an increased performance cost.
     * Values less than 0 decrease the default resolution at any given distance. The default value is 0. Values
     * typically range between -0.5 and 0.5.
     * <p/>
     * The top level KML root's detail hint is inherited by all KML elements beneath that root, including any descendant
     * KML roots loaded by network links. If this KML root has been loaded by a network link, its detail hint is
     * ignored.
     *
     * @param detailHint the degree to modify the default relationship of KML scene resolution to screen resolution as
     *                   viewing distance changes. Values greater than 0 increase the resolution. Values less than 0
     *                   decrease the resolution. The default value is 0.
     */
    public void setDetailHint(double detailHint)
    {
        this.detailHint = detailHint;
    }

    /** Request any scene containing this KML document be repainted. */
    public void requestRedraw()
    {
        this.firePropertyChange(AVKey.REPAINT, null, null);
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getFeature() != null)
            this.getFeature().preRender(tc, dc);
    }

    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getFeature() != null)
            this.getFeature().render(tc, dc);
    }

    //**********************************************************************
    //********************* Property change support ************************
    //**********************************************************************

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to forward the message to the root feature.
     *
     * @param msg The message that was received.
     */
    @Override
    public void onMessage(Message msg)
    {
        if (this.getFeature() != null)
            this.getFeature().onMessage(msg);
    }

    /**
     * Adds the specified property change listener that will be called for all list changes.
     *
     * @param listener the listener to call.
     *
     * @throws IllegalArgumentException if <code>listener</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     * Removes the specified property change listener.
     *
     * @param listener the listener to remove.
     *
     * @throws IllegalArgumentException if <code>listener</code> is null.
     * @see java.beans.PropertyChangeSupport
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     * Fire a property change event.
     *
     * @param propertyChangeEvent Event to fire.
     */
    public void firePropertyChange(java.beans.PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.getChangeSupport().firePropertyChange(propertyChangeEvent);
    }

    /**
     * Fire a property change event.
     *
     * @param propertyName Name of the property change changed.
     * @param oldValue     The previous value of the property.
     * @param newValue     The new value of the property.
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Get the PropertyChangeSupport object for this KML object. The support object will be created if it does not
     * already exist.
     *
     * @return PropertyChangeSupport for this KML object.
     */
    protected synchronized PropertyChangeSupport getChangeSupport()
    {
        if (this.propertyChangeSupport == null)
            this.propertyChangeSupport = new PropertyChangeSupport(this);
        return this.propertyChangeSupport;
    }
}
