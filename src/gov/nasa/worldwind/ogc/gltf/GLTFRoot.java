/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf;

import java.io.*;
import java.net.URL;

import gov.nasa.worldwind.ogc.gltf.impl.*;
import gov.nasa.worldwind.animation.Animatable;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.formats.json.*;

public class GLTFRoot extends GLTFAbstractObject implements GLTFRenderable, Highlightable { //, Animatable {

    /**
     * The input stream underlying the event reader.
     */
    protected InputStream eventStream;
    
    /**
     * Reference to the ColladaDoc representing the COLLADA file.
     */
    protected GLTFDoc gltfDoc;
    
    /**
     * The parser context for the document.
     */
    protected GLTFParserContext parserContext;
    
    /**
     * Indicates whether or not the COLLADA model is highlighted.
     */
    protected boolean highlighted;

    /**
     * Flag to indicate that the scene has been retrieved from the hash map.
     */
    protected boolean sceneFetched = false;
    /**
     * Cached COLLADA scene.
     */
    protected GLTFScene scene;

    protected int redrawRequested = 0;

    /**
     * Create a new <code>ColladaRoot</code> for a {@link File}.
     *
     * @param docSource the File containing the document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException if an error occurs while reading the Collada document.
     */
    public GLTFRoot(File docSource) throws IOException {
        super(docSource);

        if (docSource == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.gltfDoc = new GLTFDoc(docSource);

        this.initialize();
    }


    /**
     * Called just before the constructor returns. If overriding this method be sure to invoke
     * <code>super.initialize()</code>.
     *
     * @throws java.io.IOException if an I/O error occurs attempting to open the document source.
     */
    protected void initialize() throws IOException {
//        this.eventStream = new BufferedInputStream(this.getGLTFDoc().getInputStream());
//        this.eventReader = this.createReader(this.eventStream);
//        if (this.eventReader == null) {
//            throw new WWRuntimeException(Logging.getMessage("XML.UnableToOpenDocument", this.getColladaDoc()));
//        }

        this.parserContext = this.createParserContext();
    }



    /**
     * Invoked during {@link #initialize()} to create the parser context. The parser context is created by the global
     * {@link gov.nasa.worldwind.util.xml.XMLEventParserContextFactory}.
     *
     * @param reader the reader to associate with the parser context.
     *
     * @return a new parser context.
     */
    protected GLTFParserContext createParserContext() throws IOException { // XMLEventReader reader) {
//        ColladaParserContext ctx = (ColladaParserContext) XMLEventParserContextFactory.createParserContext(ColladaConstants.COLLADA_MIME_TYPE,
//                this.getNamespaceURI());
//        GLTFParserContext ctx=this.gltfDoc.createEventParserContext();
//        if (ctx == null) {
//            // Register a parser context for this root's default namespace
//            String[] mimeTypes = new String[]{ColladaConstants.COLLADA_MIME_TYPE};
//            XMLEventParserContextFactory.addParserContext(mimeTypes, new ColladaParserContext(this.getNamespaceURI()));
//            ctx = (ColladaParserContext) XMLEventParserContextFactory.createParserContext(ColladaConstants.COLLADA_MIME_TYPE,
//                    this.getNamespaceURI());
//        }
//        ctx.setEventReader(reader);

        return this.gltfDoc.createEventParserContext();
    }
    
    /**
     * Indicates the document that is the source of this root.
     *
     * @return The source of the COLLADA content.
     */
    protected JSONDoc getGLTFDoc() {
        return this.gltfDoc;
    }
    
    /**
     * Indicates the <i>scene</i> contained in this document.
     *
     * @return The COLLADA <i>scene</i>, or null if there is no scene.
     */
    public GLTFScene getScene() {
//        if (!this.sceneFetched) {
//            this.scene = (ColladaScene) this.getField("scene");
//            this.sceneFetched = true;
//        }
//        return this.scene;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    /**
     * {@inheritDoc} Setting root COLLADA root highlighted causes all parts of the COLLADA model to highlight.
     */
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public Box getLocalExtent(GLTFTraversalContext tc) {
//        if (tc == null)
//        {
//            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        ColladaScene scene = this.getScene();
//
//        return scene != null ? scene.getLocalExtent(tc) : null;
        return null;
    }



    /**
     * Creates a Collada root for an untyped source. The source must be either a {@link File} or a {@link String}
     * identifying either a file path or a {@link URL}. Null is returned if the source type is not recognized.
     *
     * @param docSource either a {@link File} or a {@link String} identifying a file path or {@link URL}.
     *
     * @return a new {@link ColladaRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException if an error occurs while reading the source.
     */
    public static GLTFRoot create(Object docSource) throws IOException {
        if (docSource == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        if (docSource instanceof File) {
//            return new GLTFRoot((File) docSource);
//        } else if (docSource instanceof URL) {
//            return new GLTFRoot((URL) docSource);
        // } else 
        if (docSource instanceof String) {
            File file = new File((String) docSource);
            if (file.exists()) {
                return new GLTFRoot(file);
            }

//            URL url = WWIO.makeURL(docSource);
//            if (url != null) {
//                return new GLTFRoot(url);
//            }
        } else if (docSource instanceof InputStream) {
//            return new GLTFRoot((InputStream) docSource);
        }

        return null;
    }

    /**
     * Starts document parsing. This method initiates parsing of the COLLADA document and returns when the full document
     * has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise null.
     *
     * @throws XMLStreamException if an exception occurs while attempting to read the event stream.
     */
    public GLTFRoot parse(Object... args) throws IOException { // throws XMLStreamException {
        GLTFParserContext ctx = this.parserContext;

        try {
            for (JSONEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent()) {
                if (event == null) {
                    continue;
                }

                if (event.isStartObject()) {
                    super.parse(ctx, event);
                    return this;
                }
            }
        } finally {
            this.closeEventStream();
        }
        return null;
    }

    /**
     * Closes the event stream associated with this context's XML event reader.
     */
    protected void closeEventStream() {
        try {
            this.eventStream.close();
            this.eventStream = null;
        } catch (IOException e) {
            String message = Logging.getMessage("generic.ExceptionClosingXmlEventReader");
            Logging.logger().warning(message);
        }
    }

    /**
     * Creates and parses a Collada root for an untyped source.The source must be either a {@link File} or a
     * {@link String} identifying either a file path or a {@link URL}. Null is returned if the source type is not
     * recognized.
     *
     * @param docSource either a {@link File} or a {@link String} identifying a file path or {@link URL}.
     *
     * @return a new {@link ColladaRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws javax.xml.stream.XMLStreamException if the XML stream is not readable.
     * @throws IOException if an error occurs while reading the source.
     */
    public static GLTFRoot createAndParse(Object docSource) throws IOException { // XMLStreamException {
        GLTFRoot gltfRoot = GLTFRoot.create(docSource);

        if (gltfRoot == null) {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                    docSource.toString());
            throw new IllegalArgumentException(message);
        }

        gltfRoot.parse();

        return gltfRoot;
    }

    /**
     * {@inheritDoc} Renders the scene contained in this document.
     */
    @Override
    public void preRender(GLTFTraversalContext tc, DrawContext dc) {
//        tc.multiplyMatrix(this.getMatrix());
//
//        // COLLADA doc contains at most one scene. See COLLADA spec pg 5-67.
//        ColladaScene scene = this.getScene();
//        if (scene != null)
//            scene.preRender(tc, dc);
    }

    /**
     * {@inheritDoc} Renders the scene contained in this document.
     */
    @Override
    public void render(GLTFTraversalContext tc, DrawContext dc) {
//        tc.multiplyMatrix(this.getMatrix());
//
//        GLTFScene scene = this.getScene();
//        if (scene != null) {
//            scene.render(tc, dc);
//            dc.setRedrawRequested(this.redrawRequested);
//        }
    }
}
