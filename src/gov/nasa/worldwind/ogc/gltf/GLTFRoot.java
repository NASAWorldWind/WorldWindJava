/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.ogc.gltf.impl.GLTFRenderer;
import java.io.*;
import java.net.URL;

import gov.nasa.worldwind.ogc.gltf.impl.*;
import gov.nasa.worldwind.animation.Animatable;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.typescript.*;
import java.util.ArrayList;

@TypeScriptImports(imports = "../../render/DrawContext,../json/JSONEvent,../json/JSONEventParserContext,../../util/Logger,../../geom/BoundingBox,./GLTFScene,./GLTFDoc,./GLTFParserContext,./GLTFAbstractObject,./impl/GLTFRenderable,../../render/Highlightable,../json/JSONDoc,./impl/GLTFTraversalContext")

public class GLTFRoot extends GLTFAbstractObject implements GLTFRenderable, Highlightable { //, Animatable {

    /**
     * The input stream underlying the event reader.
     */
//    protected InputStream eventStream;
    /**
     * Reference to the ColladaDoc representing the COLLADA file.
     */
    protected GLTFDoc gltfDoc;

    /**
     * The parser context for the document.
     */
    protected JSONEventParserContext parserContext;

    /**
     * Indicates whether or not the COLLADA model is highlighted.
     */
    protected boolean highlighted;

    /**
     * Cached COLLADA scene.
     */
    protected GLTFScene defaultScene;

    protected int redrawRequested = 0;

    protected GLTFNode[] nodes;

    protected GLTFAccessor[] accessors;
    protected GLTFBuffer[] buffers;
    protected GLTFScene[] scenes;
    protected GLTFBufferView[] bufferViews;
    protected GLTFAsset asset;
    protected GLTFMesh[] meshes;
    protected GLTFMaterial[] materials;
    protected GLTFCamera[] cameras;
    protected int sceneIdx;
    protected boolean assembled;

    /**
     * This shape's heading, positive values are clockwise from north. Null is
     * an allowed value.
     */
    protected Angle heading;
    /**
     * This shape's pitch (often called tilt), its rotation about the model's X
     * axis. Positive values are clockwise. Null is an allowed value.
     */
    protected Angle pitch;
    /**
     * This shape's roll, its rotation about the model's Y axis. Positive values
     * are clockwise. Null is an allowed Value.
     */
    protected Angle roll;

    /**
     * A scale to apply to the model. Null is an allowed value.
     */
    protected Vec4 modelScale;

    protected GLTFRenderer renderer;
    /**
     * Transform matrix computed from the document's scale and orientation. This
     * matrix is computed and cached during when the document is rendered.
     */
    protected Matrix matrix;

    /**
     * This shape's altitude mode. May be one of {@link WorldWind#CLAMP_TO_GROUND}, {@link
     * WorldWind#RELATIVE_TO_GROUND}, or {@link WorldWind#ABSOLUTE}.
     */
    protected int altitudeMode = WorldWind.CLAMP_TO_GROUND;

    /**
     * This shape's geographic location. The altitude is relative to this shapes
     * altitude mode.
     */
    protected Position position;

    /**
     * Create a new <code>ColladaRoot</code> for a {@link File}.
     *
     * @param docSource the File containing the document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException if an error occurs while reading the Collada
     * document.
     */
    @TypeScript(skipMethod = true)
    public GLTFRoot(File docSource) throws IOException {
        super();

        if (docSource == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.gltfDoc = new GLTFDoc(docSource);
        this.initialize();
    }

    public GLTFRoot(String jsonString) throws IOException {
        super();
        if (jsonString == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.gltfDoc = new GLTFDoc(jsonString);

        this.initialize();

    }

    /**
     * Called just before the constructor returns. If overriding this method be
     * sure to invoke <code>super.initialize()</code>.
     *
     * @throws java.io.IOException if an I/O error occurs attempting to open the
     * document source.
     */
    protected void initialize() throws IOException {
        this.renderer = new GLTFRenderer(this);
        this.parserContext = this.createParserContext();
    }

    /**
     * Invoked during {@link #initialize()} to create the parser context. The
     * parser context is created by the global
     * {@link gov.nasa.worldwind.util.xml.XMLEventParserContextFactory}.
     *
     * @param reader the reader to associate with the parser context.
     *
     * @return a new parser context.
     */
    protected JSONEventParserContext createParserContext() throws IOException { // XMLEventReader reader) {
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

        return this.gltfDoc.createEventParserContext(this, null);
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
    public GLTFScene getDefaultScene() {
        return this.defaultScene;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    /**
     * {@inheritDoc} Setting root COLLADA root highlighted causes all parts of
     * the COLLADA model to highlight.
     */
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * Specifies this shape's altitude mode, one of
     * {@link WorldWind#ABSOLUTE}, {@link WorldWind#RELATIVE_TO_GROUND} or
     * {@link WorldWind#CLAMP_TO_GROUND}.
     * <p>
     * Note: If the altitude mode is unrecognized, {@link WorldWind#ABSOLUTE} is
     * used.
     * <p>
     * Note: Subclasses may recognize additional altitude modes or may not
     * recognize the ones described above.
     *
     * @param altitudeMode the altitude mode. The default value is
     * {@link WorldWind#ABSOLUTE}.
     */
    public void setAltitudeMode(int altitudeMode) {
        this.altitudeMode = altitudeMode;
    }

    /**
     * Indicates this shape's geographic position.
     *
     * @return this shape's geographic position. The position's altitude is
     * relative to this shape's altitude mode.
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Specifies this shape's geographic position. The position's altitude is
     * relative to this shape's altitude mode.
     *
     * @param position this shape's geographic position.
     *
     * @throws IllegalArgumentException if the position is null.
     */
    public void setPosition(Position position) {
        if (position == null) {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
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
     * Creates a Collada root for an untyped source. The source must be either a
     * {@link File} or a {@link String} identifying either a file path or a
     * {@link URL}. Null is returned if the source type is not recognized.
     *
     * @param docSource either a {@link File} or a {@link String} identifying a
     * file path or {@link URL}.
     *
     * @return a new {@link ColladaRoot} for the specified source, or null if
     * the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException if an error occurs while reading the source.
     */
//    public static GLTFRoot create(Object docSource) throws IOException {
//        if (docSource == null) {
//            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        if (docSource instanceof File) {
//            return new GLTFRoot((File) docSource);
//        } else if (docSource instanceof URL) {
//            return new GLTFRoot((URL) docSource);
//        // } else 
//        if (docSource instanceof String) {
//            File file = new File((String) docSource);
//            if (file.exists()) {
//                return new GLTFRoot(file);
//            }
//
//            URL url = WWIO.makeURL(docSource);
//            if (url != null) {
//                return new GLTFRoot(url);
//            }
//        } else if (docSource instanceof InputStream) {
//            return new GLTFRoot((InputStream) docSource);
//        }
//
//        return null;
//    }
    /**
     * Starts document parsing. This method initiates parsing of the COLLADA
     * document and returns when the full document has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise null.
     *
     * @throws XMLStreamException if an exception occurs while attempting to
     * read the event stream.
     */
    public GLTFRoot parse() throws IOException {
        GLTFParserContext ctx = (GLTFParserContext) this.parserContext;

        try {
            for (JSONEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent()) {
                if (event == null) {
                    continue;
                }

                if (event.isStartObject()) {
                    AVListImpl parsedObject = (AVListImpl) super.parse(ctx, event);
                    Object[] values = new Object[0];
                    for (String propName : parsedObject.getKeys()) {
                        Object value = parsedObject.getValue(propName);
                        if (value instanceof Object[]) {
                            values = (Object[]) value;
                        }
                        switch (propName) {
                            case GLTFParserContext.KEY_NODES:
                                this.nodes = new GLTFNode[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.nodes[i] = new GLTFNode((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_ACCESSORS:
                                this.accessors = new GLTFAccessor[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.accessors[i] = new GLTFAccessor((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_BUFFERS:
                                this.buffers = new GLTFBuffer[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.buffers[i] = new GLTFBuffer((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_SCENES:
                                this.scenes = new GLTFScene[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.scenes[i] = new GLTFScene((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_BUFFER_VIEWS:
                                this.bufferViews = new GLTFBufferView[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.bufferViews[i] = new GLTFBufferView((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_ASSET:
                                this.asset = new GLTFAsset((AVListImpl) value);
                                break;
                            case GLTFParserContext.KEY_MESHES:
                                this.meshes = new GLTFMesh[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.meshes[i] = new GLTFMesh((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_SCENE:
                                this.sceneIdx = GLTFUtil.getInt(value);
                                break;
                            case GLTFParserContext.KEY_MATERIALS:
                                this.materials = new GLTFMaterial[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.materials[i] = new GLTFMaterial((AVListImpl) values[i]);
                                }
                                break;
                            case GLTFParserContext.KEY_CAMERAS:
                                this.cameras = new GLTFCamera[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    this.cameras[i] = new GLTFCamera((AVListImpl) values[i]);
                                }
                                break;
                            default:
                                System.out.println("GLTFRoot: Unsupported " + propName);
                                break;
                        }
                    }
                    this.assembleGeometry();
                    return this;
                }
            }
        } finally {
            // this.closeEventStream();
        }
        return null;
    }

    /**
     * Closes the event stream associated with this context's XML event reader.
     */
//    protected void closeEventStream() {
//        try {
//            this.eventStream.close();
//            this.eventStream = null;
//        } catch (IOException e) {
//            String message = Logging.getMessage("generic.ExceptionClosingXmlEventReader");
//            Logging.logger().warning(message);
//        }
//    }
    /**
     * Creates and parses a Collada root for an untyped source.The source must
     * be either a {@link File} or a {@link String} identifying either a file
     * path or a {@link URL}. Null is returned if the source type is not
     * recognized.
     *
     * @param docSource either a {@link File} or a {@link String} identifying a
     * file path or {@link URL}.
     *
     * @return a new {@link ColladaRoot} for the specified source, or null if
     * the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws javax.xml.stream.XMLStreamException if the XML stream is not
     * readable.
     * @throws IOException if an error occurs while reading the source.
     */
    public static GLTFRoot createAndParse(Object docSource) throws IOException { // XMLStreamException {
        // GLTFRoot gltfRoot = GLTFRoot.create(docSource);
        GLTFRoot gltfRoot = null;
        if (docSource instanceof String) {
            gltfRoot = new GLTFRoot((String) docSource);
        }

        if (gltfRoot == null) {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                    docSource.toString());
            throw new IllegalArgumentException(message);
        }

        gltfRoot.parse();
        return gltfRoot;
    }

    public GLTFNode[] getNodes() {
        return this.nodes;
    }

    protected void assembleGeometry() {
        if (this.assembled) {
            return;
        }

        this.defaultScene = this.scenes[this.sceneIdx];
        GLTFNode[] sceneNodes = this.defaultScene.setSceneNodes(this);
        for (GLTFNode node : sceneNodes) {
            node.assembleGeometry(this);
        }
    }

    public GLTFMesh getMeshForIdx(int idx) {
        return this.meshes[idx];
    }

    public GLTFAccessor getAccessorForIdx(int idx) {
        return this.accessors[idx];
    }

    public GLTFBufferView getBufferViewForIdx(int idx) {
        return this.bufferViews[idx];
    }

    public GLTFBuffer getBufferForIdx(int idx) {
        return this.buffers[idx];
    }

    /**
     * Indicates this shape's scale, if any.
     *
     * @return this shape's scale, or null if no scale has been specified.
     */
    public Vec4 getModelScale() {
        return this.modelScale;
    }

    /**
     * Specifies this shape's scale. The scale is applied to the shape's model
     * definition in the model's coordinate system prior to oriented and
     * positioning the model.
     *
     * @param modelScale this shape's scale. May be null, in which case no
     * scaling is applied.
     */
    public void setModelScale(Vec4 modelScale) {
        this.modelScale = modelScale;
        this.reset();
    }

    /**
     * Clear cached values. Values will be recomputed the next time this
     * document is rendered.
     */
    protected void reset() {
        this.matrix = null;
    }

    /**
     * Indicates the scale factored applied to this document. The scale is
     * specified by the <code>asset</code>/<code>unit</code> element.
     *
     * @return Scale applied to the document. Returns 1.0 if the document does
     * not specify a scale.
     */
    protected double getScale() {
//        if (!this.scaleFetched) {
//            this.scale = this.computeScale();
//            this.scaleFetched = true;
//        }
//        return this.scale;
        return 1;
    }

    /**
     * Indicates the transform matrix applied to this document.
     *
     * @return Transform matrix.
     */
    public Matrix getMatrix() {
        // If the matrix has already been computed then just return the cached value.
        if (this.matrix != null) {
            return this.matrix;
        }

        Matrix m = Matrix.IDENTITY;

        if (this.heading != null) {
            m = m.multiply(Matrix.fromRotationZ(Angle.POS360.subtract(this.heading)));
        }

        if (this.pitch != null) {
            m = m.multiply(Matrix.fromRotationX(this.pitch));
        }

        if (this.roll != null) {
            m = m.multiply(Matrix.fromRotationY(this.roll));
        }

        // Apply scaling factor to convert file units to meters.
        double scale = this.getScale();
        m = m.multiply(Matrix.fromScale(scale));

        if (this.modelScale != null) {
            m = m.multiply(Matrix.fromScale(this.modelScale));
        }

        this.matrix = m;
        System.out.println(m.toString());
        return m;
    }

    /**
     * {@inheritDoc} Renders the scene contained in this document.
     */
    @Override
    public void preRender(GLTFTraversalContext tc, DrawContext dc) {
        tc.multiplyMatrix(this.getMatrix());

        // COLLADA doc contains at most one scene. See COLLADA spec pg 5-67.
        GLTFScene scene = this.getDefaultScene();
        if (scene != null) {
            this.renderer.preRender(scene, tc, dc);
        }
    }

    /**
     * {@inheritDoc} Renders the scene contained in this document.
     */
    @Override
    public void render(GLTFTraversalContext tc, DrawContext dc) {
        tc.multiplyMatrix(this.getMatrix());

        GLTFScene scene = this.getDefaultScene();
        if (scene != null) {
            this.renderer.render(scene, tc, dc);
            dc.setRedrawRequested(this.redrawRequested);
        }
    }
}
