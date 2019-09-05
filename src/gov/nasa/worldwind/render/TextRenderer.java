/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */
/**
 * @version $Id: TextRenderer.java 2387 2014-10-15 20:25:02Z tgaskins $
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLExtensions;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.packrect.*;
import com.jogamp.opengl.util.texture.TextureCoords;
import jogamp.opengl.Debug;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.nio.*;
import java.text.CharacterIterator;
import java.util.*;
import java.util.List;

// For debugging purposes


/** Renders bitmapped Java 2D text into an OpenGL window with high
    performance, full Unicode support, and a simple API. Performs
    appropriate caching of text rendering results in an OpenGL texture
    internally to avoid repeated font rasterization. The caching is
    completely automatic, does not require any user intervention, and
    has no visible controls in the public API. <P>

    Using the {@link TextRenderer TextRenderer} is simple. Add a
    "<code>TextRenderer renderer;</code>" field to your {@link
    com.jogamp.opengl.GLEventListener GLEventListener}. In your {@link
    com.jogamp.opengl.GLEventListener#init init} method, add:

    <PRE>
    renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));
    </PRE>

    <P> In the {@link com.jogamp.opengl.GLEventListener#display display} method of your
    {@link com.jogamp.opengl.GLEventListener GLEventListener}, add:
    <PRE>
    renderer.beginRendering(drawable.getWidth(), drawable.getHeight());
    // optionally set the color
    renderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
    renderer.draw("Text to draw", xPosition, yPosition);
    // ... more draw commands, color changes, etc.
    renderer.endRendering();
    </PRE>

    Unless you are sharing textures and display lists between OpenGL
    contexts, you do not need to call the {@link #dispose dispose}
    method of the TextRenderer; the OpenGL resources it uses
    internally will be cleaned up automatically when the OpenGL
    context is destroyed. <P>

    <b>Note</b> that the TextRenderer may cause the vertex and texture
    coordinate array buffer bindings to change, or to be unbound. This
    is important to note if you are using Vertex Buffer Objects (VBOs)
    in your application. <P>

    Internally, the renderer uses a rectangle packing algorithm to
    pack both glyphs and full Strings' rendering results (which are
    variable size) onto a larger OpenGL texture. The internal backing
    store is maintained using a {@link
    com.jogamp.opengl.util.awt.TextureRenderer TextureRenderer}. A least
    recently used (LRU) algorithm is used to discard previously
    rendered strings; the specific algorithm is undefined, but is
    currently implemented by flushing unused Strings' rendering
    results every few hundred rendering cycles, where a rendering
    cycle is defined as a pair of calls to {@link #beginRendering
    beginRendering} / {@link #endRendering endRendering}.

    @author John Burkey
    @author Kenneth Russell
*/
public class TextRenderer {
    private static final boolean DEBUG;

    static {
        Debug.initSingleton();
        DEBUG = Debug.isPropertyDefined("jogl.debug.TextRenderer", true);
    }

    // These are occasionally useful for more in-depth debugging
    private static boolean DISABLE_GLYPH_CACHE = false;
    private static final boolean DRAW_BBOXES = false;
    static
    {
        String arg = System.getProperty("gov.nasa.worldwind.textrender.useglyphcache");
        if (arg != null && arg.toLowerCase().startsWith("f"))
            DISABLE_GLYPH_CACHE = true;
    }

    static final int kSize = 256;

    // Every certain number of render cycles, flush the strings which
    // haven't been used recently
    private static final int CYCLES_PER_FLUSH = 100;

    // The amount of vertical dead space on the backing store before we
    // force a compaction
    private static final float MAX_VERTICAL_FRAGMENTATION = 0.7f;
    static final int kQuadsPerBuffer = 100;
    static final int kCoordsPerVertVerts = 3;
    static final int kCoordsPerVertTex = 2;
    static final int kVertsPerQuad = 4;
    static final int kTotalBufferSizeVerts = kQuadsPerBuffer * kVertsPerQuad;
    static final int kTotalBufferSizeCoordsVerts = kQuadsPerBuffer * kVertsPerQuad * kCoordsPerVertVerts;
    static final int kTotalBufferSizeCoordsTex = kQuadsPerBuffer * kVertsPerQuad * kCoordsPerVertTex;
    static final int kTotalBufferSizeBytesVerts = kTotalBufferSizeCoordsVerts * 4;
    static final int kTotalBufferSizeBytesTex = kTotalBufferSizeCoordsTex * 4;
    static final int kSizeInBytes_OneVertices_VertexData = kCoordsPerVertVerts * 4;
    static final int kSizeInBytes_OneVertices_TexData = kCoordsPerVertTex * 4;
    private final Font font;
    private final boolean antialiased;
    private final boolean useFractionalMetrics;

    // Whether we're attempting to use automatic mipmap generation support
    private boolean mipmap;
    private RectanglePacker packer;
    private boolean haveMaxSize;
    private final RenderDelegate renderDelegate;
    private TextureRenderer cachedBackingStore;
    private Graphics2D cachedGraphics;
    private FontRenderContext cachedFontRenderContext;
    private final Map<String, Rect> stringLocations = new HashMap<String, Rect>();
    private final GlyphProducer mGlyphProducer;

    private int numRenderCycles;

    // Need to keep track of whether we're in a beginRendering() /
    // endRendering() cycle so we can re-enter the exact same state if
    // we have to reallocate the backing store
    private boolean inBeginEndPair;
    private boolean isOrthoMode;
    private int beginRenderingWidth;
    private int beginRenderingHeight;
    private boolean beginRenderingDepthTestDisabled;

    // For resetting the color after disposal of the old backing store
    private boolean haveCachedColor;
    private float cachedR;
    private float cachedG;
    private float cachedB;
    private float cachedA;
    private Color cachedColor;
    private boolean needToResetColor;

    // For debugging only
    private Frame dbgFrame;

    // Debugging purposes only
    private boolean debugged;
    Pipelined_QuadRenderer mPipelinedQuadRenderer;

    //emzic: added boolean flag
    private boolean useVertexArrays = true;

    //emzic: added boolean flag
    private boolean isExtensionAvailable_GL_VERSION_1_5;
    private boolean checkFor_isExtensionAvailable_GL_VERSION_1_5;

    // Whether GL_LINEAR filtering is enabled for the backing store
    private boolean smoothing = true;

    /** Creates a new TextRenderer with the given font, using no
        antialiasing or fractional metrics, and the default
        RenderDelegate. Equivalent to <code>TextRenderer(font, false,
        false)</code>.

        @param font the font to render with
    */
    public TextRenderer(Font font) {
        this(font, false, false, null, false);
    }

    /** Creates a new TextRenderer with the given font, using no
        antialiasing or fractional metrics, and the default
        RenderDelegate. If <CODE>mipmap</CODE> is true, attempts to use
        OpenGL's automatic mipmap generation for better smoothing when
        rendering the TextureRenderer's contents at a distance.
        Equivalent to <code>TextRenderer(font, false, false)</code>.

        @param font the font to render with
        @param mipmap whether to attempt use of automatic mipmap generation
    */
    public TextRenderer(Font font, boolean mipmap) {
        this(font, false, false, null, mipmap);
    }

    /** Creates a new TextRenderer with the given Font, specified font
        properties, and default RenderDelegate. The
        <code>antialiased</code> and <code>useFractionalMetrics</code>
        flags provide control over the same properties at the Java 2D
        level. No mipmap support is requested. Equivalent to
        <code>TextRenderer(font, antialiased, useFractionalMetrics,
        null)</code>.

        @param font the font to render with
        @param antialiased whether to use antialiased fonts
        @param useFractionalMetrics whether to use fractional font
        metrics at the Java 2D level
    */
    public TextRenderer(Font font, boolean antialiased,
                        boolean useFractionalMetrics) {
        this(font, antialiased, useFractionalMetrics, null, false);
    }

    /** Creates a new TextRenderer with the given Font, specified font
        properties, and given RenderDelegate. The
        <code>antialiased</code> and <code>useFractionalMetrics</code>
        flags provide control over the same properties at the Java 2D
        level. The <code>renderDelegate</code> provides more control
        over the text rendered. No mipmap support is requested.

        @param font the font to render with
        @param antialiased whether to use antialiased fonts
        @param useFractionalMetrics whether to use fractional font
        metrics at the Java 2D level
        @param renderDelegate the render delegate to use to draw the
        text's bitmap, or null to use the default one
    */
    public TextRenderer(Font font, boolean antialiased,
                        boolean useFractionalMetrics, RenderDelegate renderDelegate) {
        this(font, antialiased, useFractionalMetrics, renderDelegate, false);
    }

    /** Creates a new TextRenderer with the given Font, specified font
        properties, and given RenderDelegate. The
        <code>antialiased</code> and <code>useFractionalMetrics</code>
        flags provide control over the same properties at the Java 2D
        level. The <code>renderDelegate</code> provides more control
        over the text rendered. If <CODE>mipmap</CODE> is true, attempts
        to use OpenGL's automatic mipmap generation for better smoothing
        when rendering the TextureRenderer's contents at a distance.

        @param font the font to render with
        @param antialiased whether to use antialiased fonts
        @param useFractionalMetrics whether to use fractional font
        metrics at the Java 2D level
        @param renderDelegate the render delegate to use to draw the
        text's bitmap, or null to use the default one
        @param mipmap whether to attempt use of automatic mipmap generation
    */
    public TextRenderer(Font font, boolean antialiased,
                        boolean useFractionalMetrics, RenderDelegate renderDelegate,
                        boolean mipmap) {
        this.font = font;
        this.antialiased = antialiased;
        this.useFractionalMetrics = useFractionalMetrics;
        this.mipmap = mipmap;

        // FIXME: consider adjusting the size based on font size
        // (it will already automatically resize if necessary)
        packer = new RectanglePacker(new Manager(), kSize, kSize);

        if (renderDelegate == null) {
            renderDelegate = new DefaultRenderDelegate();
        }

        this.renderDelegate = renderDelegate;

        mGlyphProducer = new GlyphProducer(font.getNumGlyphs());
    }

    /**
     * Returns the bounding rectangle of the given String, assuming it was rendered at the origin.See
     * {@link #getBounds(CharSequence) getBounds(CharSequence)}.
     *
     * @param str The string.
     * @return The bounding rectangle for str.
     */
    public Rectangle2D getBounds(String str) {
        return getBounds((CharSequence) str);
    }

    /**
     * Returns the bounding rectangle of the given CharSequence, assuming it was rendered at the origin.The coordinate
     * system of the returned rectangle is Java 2D's, with increasing Y coordinates in the downward direction.The
     * relative coordinate (0, 0) in the returned rectangle corresponds to the baseline of the leftmost character of the
     * rendered string, in similar fashion to the results returned by, for example,
     * {@link java.awt.font.GlyphVector#getVisualBounds}. Most applications will use only the width and height of the
     * returned Rectangle for the purposes of centering or justifying the String. It is not specified which Java 2D
     * bounds ({@link
     * java.awt.font.GlyphVector#getVisualBounds getVisualBounds},
     * {@link java.awt.font.GlyphVector#getPixelBounds getPixelBounds}, etc.) the returned bounds correspond to,
     * although every effort is made to ensure an accurate bound.
     *
     * @param str The string.
     * @return The bounds of the string.
     */
    public Rectangle2D getBounds(CharSequence str) {
        // FIXME: this should be more optimized and use the glyph cache
        Rect r = stringLocations.get(str);

        if (r != null) {
            TextData data = (TextData) r.getUserData();

            // Reconstitute the Java 2D results based on the cached values
            return new Rectangle2D.Double(-data.origin().x, -data.origin().y,
                                          r.w(), r.h());
        }

        // Must return a Rectangle compatible with the layout algorithm --
        // must be idempotent
        return normalize(renderDelegate.getBounds(str, font,
                                                  getFontRenderContext()));
    }

    /**
     * Returns the Font this renderer is using.
     *
     * @return The Font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * * Returns a FontRenderContext which can be used for external text-related size computations.This object should be
     * considered transient and may become invalidated between
     * {@link #beginRendering beginRendering} / {@link #endRendering endRendering} pairs.
     *
     * @return A FontRenderContext.
     */
    public FontRenderContext getFontRenderContext() {
        if (cachedFontRenderContext == null) {
            cachedFontRenderContext = getGraphics2D().getFontRenderContext();
        }

        return cachedFontRenderContext;
    }

    /** Begins rendering with this {@link TextRenderer TextRenderer}
        into the current OpenGL drawable, pushing the projection and
        modelview matrices and some state bits and setting up a
        two-dimensional orthographic projection with (0, 0) as the
        lower-left coordinate and (width, height) as the upper-right
        coordinate. Binds and enables the internal OpenGL texture
        object, sets the texture environment mode to GL_MODULATE, and
        changes the current color to the last color set with this
        TextRenderer via {@link #setColor setColor}. This method
        disables the depth test and is equivalent to
        beginRendering(width, height, true).

        @param width the width of the current on-screen OpenGL drawable
        @param height the height of the current on-screen OpenGL drawable
        @throws com.jogamp.opengl.GLException If an OpenGL context is not current when this method is called
    */
    public void beginRendering(int width, int height) throws GLException {
        beginRendering(width, height, true);
    }

    /** Begins rendering with this {@link TextRenderer TextRenderer}
        into the current OpenGL drawable, pushing the projection and
        modelview matrices and some state bits and setting up a
        two-dimensional orthographic projection with (0, 0) as the
        lower-left coordinate and (width, height) as the upper-right
        coordinate. Binds and enables the internal OpenGL texture
        object, sets the texture environment mode to GL_MODULATE, and
        changes the current color to the last color set with this
        TextRenderer via {@link #setColor setColor}. Disables the depth
        test if the disableDepthTest argument is true.

        @param width the width of the current on-screen OpenGL drawable
        @param height the height of the current on-screen OpenGL drawable
        @param disableDepthTest whether to disable the depth test
        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void beginRendering(int width, int height, boolean disableDepthTest)
        throws GLException {
        beginRendering(true, width, height, disableDepthTest);
    }

    /** Begins rendering of 2D text in 3D with this {@link TextRenderer
        TextRenderer} into the current OpenGL drawable. Assumes the end
        user is responsible for setting up the modelview and projection
        matrices, and will render text using the {@link #draw3D draw3D}
        method. This method pushes some OpenGL state bits, binds and
        enables the internal OpenGL texture object, sets the texture
        environment mode to GL_MODULATE, and changes the current color
        to the last color set with this TextRenderer via {@link
        #setColor setColor}.

        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void begin3DRendering() throws GLException {
        beginRendering(false, 0, 0, false);
    }

    /** Changes the current color of this TextRenderer to the supplied
        one. The default color is opaque white.

        @param color the new color to use for rendering text
        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void setColor(Color color) throws GLException {
        boolean noNeedForFlush = (haveCachedColor && (cachedColor != null) &&
                                  color.equals(cachedColor));

        if (!noNeedForFlush) {
            flushGlyphPipeline();
        }

        getBackingStore().setColor(color);
        haveCachedColor = true;
        cachedColor = color;
    }

    /** Changes the current color of this TextRenderer to the supplied
        one, where each component ranges from 0.0f - 1.0f. The alpha
        component, if used, does not need to be premultiplied into the
        color channels as described in the documentation for {@link
        com.jogamp.opengl.util.texture.Texture Texture}, although
        premultiplied colors are used internally. The default color is
        opaque white.

        @param r the red component of the new color
        @param g the green component of the new color
        @param b the blue component of the new color
        @param a the alpha component of the new color, 0.0f = completely
        transparent, 1.0f = completely opaque
        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void setColor(float r, float g, float b, float a)
        throws GLException {
        boolean noNeedForFlush = (haveCachedColor && (cachedColor == null) &&
                                  (r == cachedR) && (g == cachedG) && (b == cachedB) &&
                                  (a == cachedA));

        if (!noNeedForFlush) {
            flushGlyphPipeline();
        }

        getBackingStore().setColor(r, g, b, a);
        haveCachedColor = true;
        cachedR = r;
        cachedG = g;
        cachedB = b;
        cachedA = a;
        cachedColor = null;
    }

    /** Draws the supplied CharSequence at the desired location using
        the renderer's current color. The baseline of the leftmost
        character is at position (x, y) specified in OpenGL coordinates,
        where the origin is at the lower-left of the drawable and the Y
        coordinate increases in the upward direction.

        @param str the string to draw
        @param x the x coordinate at which to draw
        @param y the y coordinate at which to draw
        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void draw(CharSequence str, int x, int y) throws GLException {
        draw3D(str, x, y, 0, 1);
    }

    /**
     * * Draws the supplied String at the desired location using the renderer's current color.See
     * {@link #draw(CharSequence, int, int) draw(CharSequence, int, int)}.
     *
     * @param str The string to draw.
     * @param x The desired x location.
     * @param y The desired y location.
     */
    public void draw(String str, int x, int y) throws GLException {
        draw3D(str, x, y, 0, 1);
    }

    /** Draws the supplied CharSequence at the desired 3D location using
        the renderer's current color. The baseline of the leftmost
        character is placed at position (x, y, z) in the current
        coordinate system.

        @param str the string to draw
        @param x the x coordinate at which to draw
        @param y the y coordinate at which to draw
        @param z the z coordinate at which to draw
        @param scaleFactor a uniform scale factor applied to the width and height of the drawn rectangle
        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void draw3D(CharSequence str, float x, float y, float z,
                       float scaleFactor) {
        internal_draw3D(str, x, y, z, scaleFactor);
    }

    /**
     * * Draws the supplied String at the desired 3D location using the renderer's current color.See {@link #draw3D(CharSequence, float, float, float, float) draw3D(CharSequence, float, float,
     * float, float)}.
     *
     * @param str The string to draw.
     * @param x The x location.
     * @param y The y location.
     * @param z The z location.
     * @param scaleFactor The scale factor.
     */
    public void draw3D(String str, float x, float y, float z, float scaleFactor) {
        internal_draw3D(str, x, y, z, scaleFactor);
    }

    /**
     * Returns the pixel width of the given character.
     *
     * @param inChar The character to measure.
     * @return The pixel width.
     */
    public float getCharWidth(char inChar) {
        return mGlyphProducer.getGlyphPixelWidth(inChar);
    }

    /** Causes the TextRenderer to flush any internal caches it may be
        maintaining and draw its rendering results to the screen. This
        should be called after each call to draw() if you are setting
        OpenGL state such as the modelview matrix between calls to
        draw(). */
    public void flush() {
        flushGlyphPipeline();
    }

    /** Ends a render cycle with this {@link TextRenderer TextRenderer}.
        Restores the projection and modelview matrices as well as
        several OpenGL state bits. Should be paired with {@link
        #beginRendering beginRendering}.

        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void endRendering() throws GLException {
        endRendering(true);
    }

    /** Ends a 3D render cycle with this {@link TextRenderer TextRenderer}.
        Restores several OpenGL state bits. Should be paired with {@link
        #begin3DRendering begin3DRendering}.

        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void end3DRendering() throws GLException {
        endRendering(false);
    }

    /** Disposes of all resources this TextRenderer is using. It is not
        valid to use the TextRenderer after this method is called.

        @throws GLException If an OpenGL context is not current when this method is called
    */
    public void dispose() throws GLException {
        packer.dispose();
        packer = null;
        cachedBackingStore = null;
        cachedGraphics = null;
        cachedFontRenderContext = null;

        if (dbgFrame != null) {
            dbgFrame.dispose();
        }
    }

    //----------------------------------------------------------------------
    // Internals only below this point
    //

    private static Rectangle2D preNormalize(Rectangle2D src) {
        // Need to round to integer coordinates
        // Also give ourselves a little slop around the reported
        // bounds of glyphs because it looks like neither the visual
        // nor the pixel bounds works perfectly well
        int minX = (int) Math.floor(src.getMinX()) - 1;
        int minY = (int) Math.floor(src.getMinY()) - 1;
        int maxX = (int) Math.ceil(src.getMaxX()) + 1;
        int maxY = (int) Math.ceil(src.getMaxY()) + 1;
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }


    private Rectangle2D normalize(Rectangle2D src) {
        // Give ourselves a boundary around each entity on the backing
        // store in order to prevent bleeding of nearby Strings due to
        // the fact that we use linear filtering

        // NOTE that this boundary is quite heuristic and is related
        // to how far away in 3D we may view the text --
        // heuristically, 1.5% of the font's height
        int boundary = (int) Math.max(1, 0.015 * font.getSize());

        return new Rectangle2D.Double((int) Math.floor(src.getMinX() - boundary),
                                      (int) Math.floor(src.getMinY() - boundary),
                                      (int) Math.ceil(src.getWidth() + 2 * boundary),
                                      (int) Math.ceil(src.getHeight()) + 2 * boundary);
    }

    private TextureRenderer getBackingStore() {
        TextureRenderer renderer = (TextureRenderer) packer.getBackingStore();

        if (renderer != cachedBackingStore) {
            // Backing store changed since last time; discard any cached Graphics2D
            if (cachedGraphics != null) {
                cachedGraphics.dispose();
                cachedGraphics = null;
                cachedFontRenderContext = null;
            }

            cachedBackingStore = renderer;
        }

        return cachedBackingStore;
    }

    private Graphics2D getGraphics2D() {
        TextureRenderer renderer = getBackingStore();

        if (cachedGraphics == null) {
            cachedGraphics = renderer.createGraphics();

            // Set up composite, font and rendering hints
            cachedGraphics.setComposite(AlphaComposite.Src);
            cachedGraphics.setColor(Color.WHITE);
            cachedGraphics.setFont(font);
            cachedGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            (antialiased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                                             : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
            cachedGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                            (useFractionalMetrics
                                             ? RenderingHints.VALUE_FRACTIONALMETRICS_ON
                                             : RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
        }

        return cachedGraphics;
    }

    private void beginRendering(boolean ortho, int width, int height,
                                boolean disableDepthTestForOrtho) {
        GL2 gl = GLContext.getCurrentGL().getGL2();

        if (DEBUG && !debugged) {
            debug(gl);
        }

        inBeginEndPair = true;
        isOrthoMode = ortho;
        beginRenderingWidth = width;
        beginRenderingHeight = height;
        beginRenderingDepthTestDisabled = disableDepthTestForOrtho;

        if (ortho) {
            getBackingStore().beginOrthoRendering(width, height,
                                                  disableDepthTestForOrtho);
        } else {
            getBackingStore().begin3DRendering();
        }

        // Push client attrib bits used by the pipelined quad renderer
        gl.glPushClientAttrib((int) GL2.GL_ALL_CLIENT_ATTRIB_BITS);

        if (!haveMaxSize) {
            // Query OpenGL for the maximum texture size and set it in the
            // RectanglePacker to keep it from expanding too large
            int[] sz = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, sz, 0);
            packer.setMaxSize(sz[0], sz[0]);
            haveMaxSize = true;
        }

        if (needToResetColor && haveCachedColor) {
            if (cachedColor == null) {
                getBackingStore().setColor(cachedR, cachedG, cachedB, cachedA);
            } else {
                getBackingStore().setColor(cachedColor);
            }

            needToResetColor = false;
        }

        // Disable future attempts to use mipmapping if TextureRenderer
        // doesn't support it
        if (mipmap && !getBackingStore().isUsingAutoMipmapGeneration()) {
            if (DEBUG) {
                System.err.println("Disabled mipmapping in TextRenderer");
            }

            mipmap = false;
        }
    }

    /**
     * emzic: here the call to glBindBuffer crashes on certain graphicscard/driver combinations
     * this is why the ugly try-catch block has been added, which falls back to the old textrenderer
     *
     * @param ortho
     * @throws GLException
     */
    private void endRendering(boolean ortho) throws GLException {
        flushGlyphPipeline();

        inBeginEndPair = false;

        GL2 gl = GLContext.getCurrentGL().getGL2();

        // Pop client attrib bits used by the pipelined quad renderer
        gl.glPopClientAttrib();

        // The OpenGL spec is unclear about whether this changes the
        // buffer bindings, so preemptively zero out the GL_ARRAY_BUFFER
        // binding
        if (getUseVertexArrays() && is15Available(gl)) {
            try {
                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
            } catch (Exception e) {
                isExtensionAvailable_GL_VERSION_1_5 = false;
            }
        }

        if (ortho) {
            getBackingStore().endOrthoRendering();
        } else {
            getBackingStore().end3DRendering();
        }

        if (++numRenderCycles >= CYCLES_PER_FLUSH) {
            numRenderCycles = 0;

            if (DEBUG) {
                System.err.println("Clearing unused entries in endRendering()");
            }

            clearUnusedEntries();
        }
    }

    private void clearUnusedEntries() {
        final java.util.List<Rect> deadRects = new ArrayList<Rect>();

        // Iterate through the contents of the backing store, removing
        // text strings that haven't been used recently
        packer.visit(new RectVisitor() {
                @Override
                public void visit(Rect rect) {
                    TextData data = (TextData) rect.getUserData();

                    if (data.used()) {
                        data.clearUsed();
                    } else {
                        deadRects.add(rect);
                    }
                }
            });

        for (Rect r : deadRects) {
            packer.remove(r);
            stringLocations.remove(((TextData) r.getUserData()).string());

            int unicodeToClearFromCache = ((TextData) r.getUserData()).unicodeID;

            if (unicodeToClearFromCache > 0) {
                mGlyphProducer.clearCacheEntry(unicodeToClearFromCache);
            }

            //      if (DEBUG) {
            //        Graphics2D g = getGraphics2D();
            //        g.setComposite(AlphaComposite.Clear);
            //        g.fillRect(r.x(), r.y(), r.w(), r.h());
            //        g.setComposite(AlphaComposite.Src);
            //      }
        }

        // If we removed dead rectangles this cycle, try to do a compaction
        float frag = packer.verticalFragmentationRatio();

        if (!deadRects.isEmpty() && (frag > MAX_VERTICAL_FRAGMENTATION)) {
            if (DEBUG) {
                System.err.println(
                                   "Compacting TextRenderer backing store due to vertical fragmentation " +
                                   frag);
            }

            packer.compact();
        }

        if (DEBUG) {
            getBackingStore().markDirty(0, 0, getBackingStore().getWidth(),
                                        getBackingStore().getHeight());
        }
    }

    private void internal_draw3D(CharSequence str, float x, float y, float z,
                                 float scaleFactor) {
        for (Glyph glyph : mGlyphProducer.getGlyphs(str)) {
            float advance = glyph.draw3D(x, y, z, scaleFactor);
            x += advance * scaleFactor;
        }
    }

    private void flushGlyphPipeline() {
        if (mPipelinedQuadRenderer != null) {
            mPipelinedQuadRenderer.draw();
        }
    }

    private void draw3D_ROBUST(CharSequence str, float x, float y, float z,
                               float scaleFactor) {
        String curStr;
        if (str instanceof String) {
            curStr = (String) str;
        } else {
            curStr = str.toString();
        }

        // Look up the string on the backing store
        Rect rect = stringLocations.get(curStr);

        if (rect == null) {
            // Rasterize this string and place it on the backing store
            Graphics2D g = getGraphics2D();
            Rectangle2D origBBox = preNormalize(renderDelegate.getBounds(curStr, font, getFontRenderContext()));
            Rectangle2D bbox = normalize(origBBox);
            Point origin = new Point((int) -bbox.getMinX(),
                                     (int) -bbox.getMinY());
            rect = new Rect(0, 0, (int) bbox.getWidth(),
                            (int) bbox.getHeight(),
                            new TextData(curStr, origin, origBBox, -1));

            packer.add(rect);
            stringLocations.put(curStr, rect);

            // Re-fetch the Graphics2D in case the addition of the rectangle
            // caused the old backing store to be thrown away
            g = getGraphics2D();

            // OK, should now have an (x, y) for this rectangle; rasterize
            // the String
            int strx = rect.x() + origin.x;
            int stry = rect.y() + origin.y;

            // Clear out the area we're going to draw into
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(rect.x(), rect.y(), rect.w(), rect.h());
            g.setComposite(AlphaComposite.Src);

            // Draw the string
            renderDelegate.draw(g, curStr, strx, stry);

            if (DRAW_BBOXES) {
                TextData data = (TextData) rect.getUserData();
                // Draw a bounding box on the backing store
                g.drawRect(strx - data.origOriginX(),
                           stry - data.origOriginY(),
                           (int) data.origRect().getWidth(),
                           (int) data.origRect().getHeight());
                g.drawRect(strx - data.origin().x,
                           stry - data.origin().y,
                           rect.w(),
                           rect.h());
            }

            // Mark this region of the TextureRenderer as dirty
            getBackingStore().markDirty(rect.x(), rect.y(), rect.w(),
                                        rect.h());
        }

        // OK, now draw the portion of the backing store to the screen
        TextureRenderer renderer = getBackingStore();

        // NOTE that the rectangles managed by the packer have their
        // origin at the upper-left but the TextureRenderer's origin is
        // at its lower left!!!
        TextData data = (TextData) rect.getUserData();
        data.markUsed();

        Rectangle2D origRect = data.origRect();

        // Align the leftmost point of the baseline to the (x, y, z) coordinate requested
        renderer.draw3DRect(x - (scaleFactor * data.origOriginX()),
                            y - (scaleFactor * ((float) origRect.getHeight() - data.origOriginY())), z,
                            rect.x() + (data.origin().x - data.origOriginX()),
                            renderer.getHeight() - rect.y() - (int) origRect.getHeight() -
                              (data.origin().y - data.origOriginY()),
                            (int) origRect.getWidth(), (int) origRect.getHeight(), scaleFactor);
    }

    //----------------------------------------------------------------------
    // Debugging functionality
    //
    private void debug(GL gl) {
        dbgFrame = new Frame("TextRenderer Debug Output");

        GLCanvas dbgCanvas = new GLCanvas(new GLCapabilities(gl.getGLProfile()));
        dbgCanvas.setSharedContext(GLContext.getCurrent());
        dbgCanvas.addGLEventListener(new DebugListener(gl, dbgFrame));
        dbgFrame.add(dbgCanvas);

        final FPSAnimator anim = new FPSAnimator(dbgCanvas, 10);
        dbgFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Run this on another thread than the AWT event queue to
                    // make sure the call to Animator.stop() completes before
                    // exiting
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                anim.stop();
                            }
                        }).start();
                }
            });
        dbgFrame.setSize(kSize, kSize);
        dbgFrame.setVisible(true);
        anim.start();
        debugged = true;
    }

    /** Class supporting more full control over the process of rendering
        the bitmapped text. Allows customization of whether the backing
        store text bitmap is full-color or intensity only, the size of
        each individual rendered text rectangle, and the contents of
        each individual rendered text string. The default implementation
        of this interface uses an intensity-only texture, a
        closely-cropped rectangle around the text, and renders text
        using the color white, which is modulated by the set color
        during the rendering process. */
    public static interface RenderDelegate {
        /**
         * Indicates whether the backing store of this TextRenderer should be intensity-only (the default) or
         * full-color.
         *
         * @return whether or not intensity only is on.
         */
        public boolean intensityOnly();

        /**
         * Computes the bounds of the given String relative to the origin.
         *
         * @param str The string to process.
         * @param font The font to use.
         * @param frc The render context to use.
         * @return The bounds given the parameters.
         */
        public Rectangle2D getBounds(String str, Font font,
                                     FontRenderContext frc);

        /**
         * Computes the bounds of the given character sequence relative to the origin.
         *
         * @param str The string to process.
         * @param font The font to use.
         * @param frc The render context to use.
         * @return The bounds given the parameters.
         */
        public Rectangle2D getBounds(CharSequence str, Font font,
                                     FontRenderContext frc);

        /**
         * Computes the bounds of the given GlyphVector, already assumed to have been created for a particular Font,
         * relative to the origin.
         *
         * @param gv The string to process.
         * @param frc The render context to use.
         * @return The bounds given the parameters.
         */
        public Rectangle2D getBounds(GlyphVector gv, FontRenderContext frc);

        /**
         * * Render the passed character sequence at the designated location using the supplied Graphics2D instance.The
         * surrounding region will already have been cleared to the RGB color (0, 0, 0) with zero alpha.The initial
         * drawing context of the passed Graphics2D will be set to use AlphaComposite.Src, the color white, the Font
         * specified in the TextRenderer's constructor, and the rendering hints specified in the TextRenderer
         * constructor.Changes made by the end user may be visible in successive calls to this method, but are not
         * guaranteed to be preserved.Implementors of this method should reset the Graphics2D's state to that desired
         * each time this method is called, in particular those states which are not the defaults.
         *
         * @param graphics The canvas to draw on.
         * @param str The string to draw.
         * @param x The x location.
         * @param y The y location.
         */
        public void draw(Graphics2D graphics, String str, int x, int y);

        /**
         * * Render the passed GlyphVector at the designated location using the supplied Graphics2D instance.The
         * surrounding region will already have been cleared to the RGB color (0, 0, 0) with zero alpha.The initial
         * drawing context of the passed Graphics2D will be set to use AlphaComposite.Src, the color white, the Font
         * specified in the TextRenderer's constructor, and the rendering hints specified in the TextRenderer
         * constructor.Changes made by the end user may be visible in successive calls to this method, but are not
         * guaranteed to be preserved.Implementors of this method should reset the Graphics2D's state to that desired
         * each time this method is called, in particular those states which are not the defaults.
         *
         * @param graphics The canvas to draw on.
         * @param str The string to draw.
         * @param x The x location.
         * @param y The y location.
         */
        public void drawGlyphVector(Graphics2D graphics, GlyphVector str,
                                    int x, int y);
    }

    private static class CharSequenceIterator implements CharacterIterator {
        CharSequence mSequence;
        int mLength;
        int mCurrentIndex;

        CharSequenceIterator() {
        }

        CharSequenceIterator(CharSequence sequence) {
            initFromCharSequence(sequence);
        }

        public void initFromCharSequence(CharSequence sequence) {
            mSequence = sequence;
            mLength = mSequence.length();
            mCurrentIndex = 0;
        }

        @Override
        public char last() {
            mCurrentIndex = Math.max(0, mLength - 1);

            return current();
        }

        @Override
        public char current() {
            if ((mLength == 0) || (mCurrentIndex >= mLength)) {
                return CharacterIterator.DONE;
            }

            return mSequence.charAt(mCurrentIndex);
        }

        @Override
        public char next() {
            mCurrentIndex++;

            return current();
        }

        @Override
        public char previous() {
            mCurrentIndex = Math.max(mCurrentIndex - 1, 0);

            return current();
        }

        @Override
        public char setIndex(int position) {
            mCurrentIndex = position;

            return current();
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return mLength;
        }

        @Override
        public int getIndex() {
            return mCurrentIndex;
        }

        @Override
        public Object clone() {
            CharSequenceIterator iter = new CharSequenceIterator(mSequence);
            iter.mCurrentIndex = mCurrentIndex;

            return iter;
        }

        @Override
        public char first() {
            if (mLength == 0) {
                return CharacterIterator.DONE;
            }

            mCurrentIndex = 0;

            return current();
        }
    }

    // Data associated with each rectangle of text
    static class TextData {
        // Back-pointer to String this TextData describes, if it
        // represents a String rather than a single glyph
        private final String str;

        // If this TextData represents a single glyph, this is its
        // unicode ID
        int unicodeID;

        // The following must be defined and used VERY precisely. This is
        // the offset from the upper-left corner of this rectangle (Java
        // 2D coordinate system) at which the string must be rasterized in
        // order to fit within the rectangle -- the leftmost point of the
        // baseline.
        private final Point origin;

        // This represents the pre-normalized rectangle, which fits
        // within the rectangle on the backing store. We keep a
        // one-pixel border around entries on the backing store to
        // prevent bleeding of adjacent letters when using GL_LINEAR
        // filtering for rendering. The origin of this rectangle is
        // equivalent to the origin above.
        private final Rectangle2D origRect;

        private boolean used; // Whether this text was used recently

        TextData(String str, Point origin, Rectangle2D origRect, int unicodeID) {
            this.str = str;
            this.origin = origin;
            this.origRect = origRect;
            this.unicodeID = unicodeID;
        }

        String string() {
            return str;
        }

        Point origin() {
            return origin;
        }

        // The following three methods are used to locate the glyph
        // within the expanded rectangle coming from normalize()
        int origOriginX() {
            return (int) -origRect.getMinX();
        }

        int origOriginY() {
            return (int) -origRect.getMinY();
        }

        Rectangle2D origRect() {
            return origRect;
        }

        boolean used() {
            return used;
        }

        void markUsed() {
            used = true;
        }

        void clearUsed() {
            used = false;
        }
    }

    class Manager implements BackingStoreManager {
        private Graphics2D g;

        @Override
        public Object allocateBackingStore(int w, int h) {
            // FIXME: should consider checking Font's attributes to see
            // whether we're likely to need to support a full RGBA backing
            // store (i.e., non-default Paint, foreground color, etc.), but
            // for now, let's just be more efficient
            TextureRenderer renderer;

            if (renderDelegate.intensityOnly()) {
                renderer = TextureRenderer.createAlphaOnlyRenderer(w, h, mipmap);
            } else {
                renderer = new TextureRenderer(w, h, true, mipmap);
            }
            renderer.setSmoothing(smoothing);

            if (DEBUG) {
                System.err.println(" TextRenderer allocating backing store " +
                                   w + " x " + h);
            }

            return renderer;
        }

        @Override
        public void deleteBackingStore(Object backingStore) {
            ((TextureRenderer) backingStore).dispose();
        }

        @Override
        public boolean preExpand(Rect cause, int attemptNumber) {
            // Only try this one time; clear out potentially obsolete entries
            // NOTE: this heuristic and the fact that it clears the used bit
            // of all entries seems to cause cycling of entries in some
            // situations, where the backing store becomes small compared to
            // the amount of text on the screen (see the TextFlow demo) and
            // the entries continually cycle in and out of the backing
            // store, decreasing performance. If we added a little age
            // information to the entries, and only cleared out entries
            // above a certain age, this behavior would be eliminated.
            // However, it seems the system usually stabilizes itself, so
            // for now we'll just keep things simple. Note that if we don't
            // clear the used bit here, the backing store tends to increase
            // very quickly to its maximum size, at least with the TextFlow
            // demo when the text is being continually re-laid out.
            if (attemptNumber == 0) {
                if (DEBUG) {
                    System.err.println(
                        "Clearing unused entries in preExpand(): attempt number " +
                        attemptNumber);
                }

                if (inBeginEndPair) {
                    // Draw any outstanding glyphs
                    flush();
                }

                clearUnusedEntries();

                return true;
            }

            return false;
        }

        @Override
        public boolean additionFailed(Rect cause, int attemptNumber) {
            // Heavy hammer -- might consider doing something different
            packer.clear();
            stringLocations.clear();
            mGlyphProducer.clearAllCacheEntries();

            if (DEBUG) {
                System.err.println(
                                   " *** Cleared all text because addition failed ***");
            }

            if (attemptNumber == 0) {
                return true;
            }

            return false;
        }

        @Override
        public boolean canCompact() {
            return true;
        }

        @Override
        public void beginMovement(Object oldBackingStore, Object newBackingStore) {
            // Exit the begin / end pair if necessary
            if (inBeginEndPair) {
                // Draw any outstanding glyphs
                flush();

                GL2 gl = GLContext.getCurrentGL().getGL2();

                // Pop client attrib bits used by the pipelined quad renderer
                gl.glPopClientAttrib();

                // The OpenGL spec is unclear about whether this changes the
                // buffer bindings, so preemptively zero out the GL_ARRAY_BUFFER
                // binding
                if (getUseVertexArrays() && is15Available(gl)) {
                    try {
                        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
                    } catch (Exception e) {
                        isExtensionAvailable_GL_VERSION_1_5 = false;
                    }
                }

                if (isOrthoMode) {
                    ((TextureRenderer) oldBackingStore).endOrthoRendering();
                } else {
                    ((TextureRenderer) oldBackingStore).end3DRendering();
                }
            }

            TextureRenderer newRenderer = (TextureRenderer) newBackingStore;
            g = newRenderer.createGraphics();
        }

        @Override
        public void move(Object oldBackingStore, Rect oldLocation,
                         Object newBackingStore, Rect newLocation) {
            TextureRenderer oldRenderer = (TextureRenderer) oldBackingStore;
            TextureRenderer newRenderer = (TextureRenderer) newBackingStore;

            if (oldRenderer == newRenderer) {
                // Movement on the same backing store -- easy case
                g.copyArea(oldLocation.x(), oldLocation.y(), oldLocation.w(),
                           oldLocation.h(), newLocation.x() - oldLocation.x(),
                           newLocation.y() - oldLocation.y());
            } else {
                // Need to draw from the old renderer's image into the new one
                Image img = oldRenderer.getImage();
                g.drawImage(img, newLocation.x(), newLocation.y(),
                            newLocation.x() + newLocation.w(),
                            newLocation.y() + newLocation.h(), oldLocation.x(),
                            oldLocation.y(), oldLocation.x() + oldLocation.w(),
                            oldLocation.y() + oldLocation.h(), null);
            }
        }

        @Override
        public void endMovement(Object oldBackingStore, Object newBackingStore) {
            g.dispose();

            // Sync the whole surface
            TextureRenderer newRenderer = (TextureRenderer) newBackingStore;
            newRenderer.markDirty(0, 0, newRenderer.getWidth(),
                                  newRenderer.getHeight());

            // Re-enter the begin / end pair if necessary
            if (inBeginEndPair) {
                if (isOrthoMode) {
                    ((TextureRenderer) newBackingStore).beginOrthoRendering(beginRenderingWidth,
                                                                            beginRenderingHeight, beginRenderingDepthTestDisabled);
                } else {
                    ((TextureRenderer) newBackingStore).begin3DRendering();
                }

                // Push client attrib bits used by the pipelined quad renderer
                GL2 gl = GLContext.getCurrentGL().getGL2();
                gl.glPushClientAttrib((int) GL2.GL_ALL_CLIENT_ATTRIB_BITS);

                if (haveCachedColor) {
                    if (cachedColor == null) {
                        ((TextureRenderer) newBackingStore).setColor(cachedR,
                                                                     cachedG, cachedB, cachedA);
                    } else {
                        ((TextureRenderer) newBackingStore).setColor(cachedColor);
                    }
                }
            } else {
                needToResetColor = true;
            }
        }
    }

    public static class DefaultRenderDelegate implements RenderDelegate {
        @Override
        public boolean intensityOnly() {
            return true;
        }

        @Override
        public Rectangle2D getBounds(CharSequence str, Font font,
                                     FontRenderContext frc) {
            return getBounds(font.createGlyphVector(frc,
                                                    new CharSequenceIterator(str)),
                             frc);
        }

        @Override
        public Rectangle2D getBounds(String str, Font font,
                                     FontRenderContext frc) {
            return getBounds(font.createGlyphVector(frc, str), frc);
        }

        @Override
        public Rectangle2D getBounds(GlyphVector gv, FontRenderContext frc) {
            return gv.getVisualBounds();
        }

        @Override
        public void drawGlyphVector(Graphics2D graphics, GlyphVector str,
                                    int x, int y) {
            graphics.drawGlyphVector(str, x, y);
        }

        @Override
        public void draw(Graphics2D graphics, String str, int x, int y) {
            graphics.drawString(str, x, y);
        }
    }

    //----------------------------------------------------------------------
    // Glyph-by-glyph rendering support
    //

    // A temporary to prevent excessive garbage creation
    private final char[] singleUnicode = new char[1];

    /** A Glyph represents either a single unicode glyph or a
        substring of characters to be drawn. The reason for the dual
        behavior is so that we can take in a sequence of unicode
        characters and partition them into runs of individual glyphs,
        but if we encounter complex text and/or unicode sequences we
        don't understand, we can render them using the
        string-by-string method. <P>

        Glyphs need to be able to re-upload themselves to the backing
        store on demand as we go along in the render sequence.
    */

    class Glyph {
        // If this Glyph represents an individual unicode glyph, this
        // is its unicode ID. If it represents a String, this is -1.
        private int unicodeID;
        // If the above field isn't -1, then these fields are used.
        // The glyph code in the font
        private int glyphCode;
        // The GlyphProducer which created us
        private GlyphProducer producer;
        // The advance of this glyph
        private float advance;
        // The GlyphVector for this single character; this is passed
        // in during construction but cleared during the upload
        // process
        private GlyphVector singleUnicodeGlyphVector;
        // The rectangle of this glyph on the backing store, or null
        // if it has been cleared due to space pressure
        private Rect glyphRectForTextureMapping;
        // If this Glyph represents a String, this is the sequence of
        // characters
        private String str;
        // Whether we need a valid advance when rendering this string
        // (i.e., whether it has other single glyphs coming after it)
        private boolean needAdvance;

        // Creates a Glyph representing an individual Unicode character
        public Glyph(int unicodeID,
                     int glyphCode,
                     float advance,
                     GlyphVector singleUnicodeGlyphVector,
                     GlyphProducer producer) {
            this.unicodeID = unicodeID;
            this.glyphCode = glyphCode;
            this.advance = advance;
            this.singleUnicodeGlyphVector = singleUnicodeGlyphVector;
            this.producer = producer;
        }

        // Creates a Glyph representing a sequence of characters, with
        // an indication of whether additional single glyphs are being
        // rendered after it
        public Glyph(String str, boolean needAdvance) {
            this.str = str;
            this.needAdvance = needAdvance;
        }

        /** Returns this glyph's unicode ID */
        public int getUnicodeID() {
            return unicodeID;
        }

        /** Returns this glyph's (font-specific) glyph code */
        public int getGlyphCode() {
            return glyphCode;
        }

        /** Returns the advance for this glyph */
        public float getAdvance() {
            return advance;
        }

        /** Draws this glyph and returns the (x) advance for this glyph */
        public float draw3D(float inX, float inY, float z, float scaleFactor) {
            if (str != null) {
                draw3D_ROBUST(str, inX, inY, z, scaleFactor);
                if (!needAdvance) {
                    return 0;
                }
                // Compute and return the advance for this string
                GlyphVector gv = font.createGlyphVector(getFontRenderContext(), str);
                float totalAdvance = 0;
                for (int i = 0; i < gv.getNumGlyphs(); i++) {
                    totalAdvance += gv.getGlyphMetrics(i).getAdvance();
                }
                return totalAdvance;
            }

            // This is the code path taken for individual glyphs
            if (glyphRectForTextureMapping == null) {
                upload();
            }

            try {
                if (mPipelinedQuadRenderer == null) {
                    mPipelinedQuadRenderer = new Pipelined_QuadRenderer();
                }

                TextureRenderer renderer = getBackingStore();
                // Handles case where NPOT texture is used for backing store
                TextureCoords wholeImageTexCoords = renderer.getTexture().getImageTexCoords();
                float xScale = wholeImageTexCoords.right();
                float yScale = wholeImageTexCoords.bottom();

                Rect rect = glyphRectForTextureMapping;
                TextData data = (TextData) rect.getUserData();
                data.markUsed();

                Rectangle2D origRect = data.origRect();

                float x = inX - (scaleFactor * data.origOriginX());
                float y = inY - (scaleFactor * ((float) origRect.getHeight() - data.origOriginY()));

                int texturex = rect.x() + (data.origin().x - data.origOriginX());
                int texturey = renderer.getHeight() - rect.y() - (int) origRect.getHeight() -
                    (data.origin().y - data.origOriginY());
                int width = (int) origRect.getWidth();
                int height = (int) origRect.getHeight();

                float tx1 = xScale * texturex / renderer.getWidth();
                float ty1 = yScale * (1.0f -
                                      ((float) texturey / (float) renderer.getHeight()));
                float tx2 = xScale * (texturex + width) / renderer.getWidth();
                float ty2 = yScale * (1.0f -
                                      ((float) (texturey + height) / (float) renderer.getHeight()));

                mPipelinedQuadRenderer.glTexCoord2f(tx1, ty1);
                mPipelinedQuadRenderer.glVertex3f(x, y, z);
                mPipelinedQuadRenderer.glTexCoord2f(tx2, ty1);
                mPipelinedQuadRenderer.glVertex3f(x + (width * scaleFactor), y,
                                                  z);
                mPipelinedQuadRenderer.glTexCoord2f(tx2, ty2);
                mPipelinedQuadRenderer.glVertex3f(x + (width * scaleFactor),
                                                  y + (height * scaleFactor), z);
                mPipelinedQuadRenderer.glTexCoord2f(tx1, ty2);
                mPipelinedQuadRenderer.glVertex3f(x,
                                                  y + (height * scaleFactor), z);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return advance;
        }

        /** Notifies this glyph that it's been cleared out of the cache */
        public void clear() {
            glyphRectForTextureMapping = null;
        }

        private void upload() {
            GlyphVector gv = getGlyphVector();
            Rectangle2D origBBox = preNormalize(renderDelegate.getBounds(gv, getFontRenderContext()));
            Rectangle2D bbox = normalize(origBBox);
            Point origin = new Point((int) -bbox.getMinX(),
                                     (int) -bbox.getMinY());
            Rect rect = new Rect(0, 0, (int) bbox.getWidth(),
                                 (int) bbox.getHeight(),
                                 new TextData(null, origin, origBBox, unicodeID));
            packer.add(rect);
            glyphRectForTextureMapping = rect;
            Graphics2D g = getGraphics2D();
            // OK, should now have an (x, y) for this rectangle; rasterize
            // the glyph
            int strx = rect.x() + origin.x;
            int stry = rect.y() + origin.y;

            // Clear out the area we're going to draw into
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(rect.x(), rect.y(), rect.w(), rect.h());
            g.setComposite(AlphaComposite.Src);

            // Draw the string
            renderDelegate.drawGlyphVector(g, gv, strx, stry);

            if (DRAW_BBOXES) {
                TextData data = (TextData) rect.getUserData();
                // Draw a bounding box on the backing store
                g.drawRect(strx - data.origOriginX(),
                           stry - data.origOriginY(),
                           (int) data.origRect().getWidth(),
                           (int) data.origRect().getHeight());
                g.drawRect(strx - data.origin().x,
                           stry - data.origin().y,
                           rect.w(),
                           rect.h());
            }

            // Mark this region of the TextureRenderer as dirty
            getBackingStore().markDirty(rect.x(), rect.y(), rect.w(),
                                        rect.h());
            // Re-register ourselves with our producer
            producer.register(this);
        }

        private GlyphVector getGlyphVector() {
            GlyphVector gv = singleUnicodeGlyphVector;
            if (gv != null) {
                singleUnicodeGlyphVector = null; // Don't need this anymore
                return gv;
            }
            singleUnicode[0] = (char) unicodeID;
            return font.createGlyphVector(getFontRenderContext(), singleUnicode);
        }
    }

    class GlyphProducer {
        final int undefined = -2;
        FontRenderContext fontRenderContext;
        List<Glyph> glyphsOutput = new ArrayList<Glyph>();
        HashMap<String, GlyphVector> fullGlyphVectorCache = new HashMap<String, GlyphVector>();
        HashMap<Character, GlyphMetrics> glyphMetricsCache = new HashMap<Character, GlyphMetrics>();
        // The mapping from unicode character to font-specific glyph ID
        int[] unicodes2Glyphs;
        // The mapping from glyph ID to Glyph
        Glyph[] glyphCache;
        // We re-use this for each incoming string
        CharSequenceIterator iter = new CharSequenceIterator();

        GlyphProducer(int fontLengthInGlyphs) {
            unicodes2Glyphs = new int[512];
            glyphCache = new Glyph[fontLengthInGlyphs];
            clearAllCacheEntries();
        }

        public List<Glyph> getGlyphs(CharSequence inString) {
            glyphsOutput.clear();
            GlyphVector fullRunGlyphVector;
            fullRunGlyphVector = fullGlyphVectorCache.get(inString.toString());
            if (fullRunGlyphVector == null) {
                iter.initFromCharSequence(inString);
                fullRunGlyphVector = font.createGlyphVector(getFontRenderContext(), iter);
                fullGlyphVectorCache.put(inString.toString(), fullRunGlyphVector);
            }
            boolean complex = (fullRunGlyphVector.getLayoutFlags() != 0);
            if (complex || DISABLE_GLYPH_CACHE) {
                // Punt to the robust version of the renderer
                glyphsOutput.add(new Glyph(inString.toString(), false));
                return glyphsOutput;
            }

            int lengthInGlyphs = fullRunGlyphVector.getNumGlyphs();
            int i = 0;
            while (i < lengthInGlyphs) {
                Character letter = CharacterCache.valueOf(inString.charAt(i));
                GlyphMetrics metrics = glyphMetricsCache.get(letter);
                if (metrics == null) {
                    metrics = fullRunGlyphVector.getGlyphMetrics(i);
                    glyphMetricsCache.put(letter, metrics);
                }
                Glyph glyph = getGlyph(inString, metrics, i);
                if (glyph != null) {
                    glyphsOutput.add(glyph);
                    i++;
                } else {
                    // Assemble a run of characters that don't fit in
                    // the cache
                    StringBuilder buf = new StringBuilder();
                    while (i < lengthInGlyphs &&
                           getGlyph(inString, fullRunGlyphVector.getGlyphMetrics(i), i) == null) {
                        buf.append(inString.charAt(i++));
                    }
                    glyphsOutput.add(new Glyph(buf.toString(),
                                               // Any more glyphs after this run?
                                               i < lengthInGlyphs));
                }
            }
            return glyphsOutput;
        }

        public void clearCacheEntry(int unicodeID) {
            int glyphID = unicodes2Glyphs[unicodeID];
            if (glyphID != undefined) {
                Glyph glyph = glyphCache[glyphID];
                if (glyph != null) {
                    glyph.clear();
                }
                glyphCache[glyphID] = null;
            }
            unicodes2Glyphs[unicodeID] = undefined;
        }

        public void clearAllCacheEntries() {
            for (int i = 0; i < unicodes2Glyphs.length; i++) {
                clearCacheEntry(i);
            }
        }

        public void register(Glyph glyph) {
            unicodes2Glyphs[glyph.getUnicodeID()] = glyph.getGlyphCode();
            glyphCache[glyph.getGlyphCode()] = glyph;
        }

        public float getGlyphPixelWidth(char unicodeID) {
            Glyph glyph = getGlyph(unicodeID);
            if (glyph != null) {
                return glyph.getAdvance();
            }

            // Have to do this the hard / uncached way
            singleUnicode[0] = unicodeID;
            GlyphVector gv = font.createGlyphVector(fontRenderContext,
                                                                    singleUnicode);
            return gv.getGlyphMetrics(0).getAdvance();
        }

        // Returns a glyph object for this single glyph. Returns null
        // if the unicode or glyph ID would be out of bounds of the
        // glyph cache.
        private Glyph getGlyph(CharSequence inString,
                               GlyphMetrics glyphMetrics,
                               int index) {
            char unicodeID = inString.charAt(index);

            if (unicodeID >= unicodes2Glyphs.length) {
                return null;
            }

            int glyphID = unicodes2Glyphs[unicodeID];
            if (glyphID != undefined) {
                return glyphCache[glyphID];
            }

            // Must fabricate the glyph
            singleUnicode[0] = unicodeID;
            GlyphVector gv = font.createGlyphVector(getFontRenderContext(), singleUnicode);
            return getGlyph(unicodeID, gv, glyphMetrics);
        }

        // It's unclear whether this variant might produce less
        // optimal results than if we can see the entire GlyphVector
        // for the incoming string
        private Glyph getGlyph(int unicodeID) {
            if (unicodeID >= unicodes2Glyphs.length) {
                return null;
            }

            int glyphID = unicodes2Glyphs[unicodeID];
            if (glyphID != undefined) {
                return glyphCache[glyphID];
            }
            singleUnicode[0] = (char) unicodeID;
            GlyphVector gv = font.createGlyphVector(getFontRenderContext(), singleUnicode);
            return getGlyph(unicodeID, gv, gv.getGlyphMetrics(0));
        }

        private Glyph getGlyph(int unicodeID,
                               GlyphVector singleUnicodeGlyphVector,
                               GlyphMetrics metrics) {
            int glyphCode = singleUnicodeGlyphVector.getGlyphCode(0);
            // Have seen huge glyph codes (65536) coming out of some fonts in some Unicode situations
            if (glyphCode >= glyphCache.length) {
                return null;
            }
            Glyph glyph = new Glyph(unicodeID,
                                    glyphCode,
                                    metrics.getAdvance(),
                                    singleUnicodeGlyphVector,
                                    this);
            register(glyph);
            return glyph;
        }
    }

    private static class CharacterCache {
        private CharacterCache() {
        }

        static final Character CACHE[] = new Character[127 + 1];

        static {
            for (int i = 0; i < CACHE.length; i++) {
                CACHE[i] = (char) i;
            }
        }

        public static Character valueOf(char c) {
            if (c <= 127) { // must cache
                return CharacterCache.CACHE[c];
            }
            return c;
        }
    }

    class Pipelined_QuadRenderer {
        int mOutstandingGlyphsVerticesPipeline = 0;
        FloatBuffer mTexCoords;
        FloatBuffer mVertCoords;
        boolean usingVBOs;
        int mVBO_For_ResuableTileVertices;
        int mVBO_For_ResuableTileTexCoords;

        Pipelined_QuadRenderer() {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            mVertCoords = Buffers.newDirectFloatBuffer(kTotalBufferSizeCoordsVerts);
            mTexCoords = Buffers.newDirectFloatBuffer(kTotalBufferSizeCoordsTex);

            usingVBOs = getUseVertexArrays() && is15Available(gl);

            if (usingVBOs) {
                try {
                    int[] vbos = new int[2];
                    gl.glGenBuffers(2, IntBuffer.wrap(vbos));

                    mVBO_For_ResuableTileVertices = vbos[0];
                    mVBO_For_ResuableTileTexCoords = vbos[1];

                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,
                                    mVBO_For_ResuableTileVertices);
                    gl.glBufferData(GL2.GL_ARRAY_BUFFER, kTotalBufferSizeBytesVerts,
                                    null, GL2.GL_STREAM_DRAW); // stream draw because this is a single quad use pipeline

                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,
                                    mVBO_For_ResuableTileTexCoords);
                    gl.glBufferData(GL2.GL_ARRAY_BUFFER, kTotalBufferSizeBytesTex,
                                    null, GL2.GL_STREAM_DRAW); // stream draw because this is a single quad use pipeline
                } catch (Exception e) {
                    isExtensionAvailable_GL_VERSION_1_5 = false;
                    usingVBOs = false;
                }
            }
        }

        public void glTexCoord2f(float v, float v1) {
            mTexCoords.put(v);
            mTexCoords.put(v1);
        }

        public void glVertex3f(float inX, float inY, float inZ) {
            mVertCoords.put(inX);
            mVertCoords.put(inY);
            mVertCoords.put(inZ);

            mOutstandingGlyphsVerticesPipeline++;

            if (mOutstandingGlyphsVerticesPipeline >= kTotalBufferSizeVerts) {
                this.draw();
            }
        }

        private void draw() {
            if (useVertexArrays) {
                drawVertexArrays();
            } else {
                drawIMMEDIATE();
            }
        }

        private void drawVertexArrays() {
            if (mOutstandingGlyphsVerticesPipeline > 0) {
                GL2 gl = GLContext.getCurrentGL().getGL2();

                TextureRenderer renderer = getBackingStore();
                renderer.getTexture(); // triggers texture uploads.  Maybe this should be more obvious?

                mVertCoords.rewind();
                mTexCoords.rewind();

                gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

                if (usingVBOs) {
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,
                                    mVBO_For_ResuableTileVertices);
                    gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0,
                                       mOutstandingGlyphsVerticesPipeline * kSizeInBytes_OneVertices_VertexData,
                                       mVertCoords); // upload only the new stuff
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
                } else {
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 0, mVertCoords);
                }

                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

                if (usingVBOs) {
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,
                                    mVBO_For_ResuableTileTexCoords);
                    gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0,
                                       mOutstandingGlyphsVerticesPipeline * kSizeInBytes_OneVertices_TexData,
                                       mTexCoords); // upload only the new stuff
                    gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
                } else {
                    gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, mTexCoords);
                }

                gl.glDrawArrays(GL2.GL_QUADS, 0,
                                mOutstandingGlyphsVerticesPipeline);

                mVertCoords.rewind();
                mTexCoords.rewind();
                mOutstandingGlyphsVerticesPipeline = 0;
            }
        }

        private void drawIMMEDIATE() {
            if (mOutstandingGlyphsVerticesPipeline > 0) {
                TextureRenderer renderer = getBackingStore();
                renderer.getTexture(); // triggers texture uploads.  Maybe this should be more obvious?

                GL2 gl = GLContext.getCurrentGL().getGL2();
                gl.glBegin(GL2.GL_QUADS);

                try {
                    int numberOfQuads = mOutstandingGlyphsVerticesPipeline / 4;
                    mVertCoords.rewind();
                    mTexCoords.rewind();

                    for (int i = 0; i < numberOfQuads; i++) {
                        gl.glTexCoord2f(mTexCoords.get(), mTexCoords.get());
                        gl.glVertex3f(mVertCoords.get(), mVertCoords.get(),
                                      mVertCoords.get());

                        gl.glTexCoord2f(mTexCoords.get(), mTexCoords.get());
                        gl.glVertex3f(mVertCoords.get(), mVertCoords.get(),
                                      mVertCoords.get());

                        gl.glTexCoord2f(mTexCoords.get(), mTexCoords.get());
                        gl.glVertex3f(mVertCoords.get(), mVertCoords.get(),
                                      mVertCoords.get());

                        gl.glTexCoord2f(mTexCoords.get(), mTexCoords.get());
                        gl.glVertex3f(mVertCoords.get(), mVertCoords.get(),
                                      mVertCoords.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    gl.glEnd();
                    mVertCoords.rewind();
                    mTexCoords.rewind();
                    mOutstandingGlyphsVerticesPipeline = 0;
                }
            }
        }
    }

    class DebugListener implements GLEventListener {
        private GLU glu;
        private Frame frame;

        DebugListener(GL gl, Frame frame) {
            this.glu = GLU.createGLU(gl);
            this.frame = frame;
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);

            if (packer == null) {
                return;
            }

            TextureRenderer rend = getBackingStore();
            final int w = rend.getWidth();
            final int h = rend.getHeight();
            rend.beginOrthoRendering(w, h);
            rend.drawOrthoRect(0, 0);
            rend.endOrthoRendering();

            if ((frame.getWidth() != w) || (frame.getHeight() != h)) {
                EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            frame.setSize(w, h);
                        }
                    });
            }
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
            glu=null;
            frame=null;
        }

        // Unused methods
        @Override
        public void init(GLAutoDrawable drawable) {
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                            int height) {
        }

        public void displayChanged(GLAutoDrawable drawable,
                                   boolean modeChanged, boolean deviceChanged) {
        }
    }

    /**
     * Sets whether vertex arrays are being used internally for rendering, or whether text is rendered using the OpenGL
     * immediate mode commands.This is provided as a concession for certain graphics cards which have poor vertex array
     * performance. Defaults to true.
     *
     * @param useVertexArrays The desired value.
     */
    public void setUseVertexArrays(boolean useVertexArrays) {
        this.useVertexArrays = useVertexArrays;
    }

    /**
     * Indicates whether vertex arrays are being used internally for
     * rendering, or whether text is rendered using the OpenGL
     * immediate mode commands.Defaults to true.
     * @return whether userVertexArrays is on or off.
     */
    public final boolean getUseVertexArrays() {
        return useVertexArrays;
    }

    /**
     * Sets whether smoothing (i.e., GL_LINEAR filtering) is enabled in the backing TextureRenderer of this
     * TextRenderer.A few graphics cards do not behave well when this is enabled, resulting in fuzzy text. Defaults to
     * true.
     *
     * @param smoothing The new smoothing setting.
     */
    public void setSmoothing(boolean smoothing) {
        this.smoothing = smoothing;
        getBackingStore().setSmoothing(smoothing);
    }

    /**
     * Indicates whether smoothing is enabled in the backing TextureRenderer of this TextRenderer.A few graphics cards
     * do not behave well when this is enabled, resulting in fuzzy text. Defaults to true.
     *
     * @return The current smoothing setting.
     */
    public boolean getSmoothing() {
        return smoothing;
    }

    private boolean is15Available(GL gl) {
        if (!checkFor_isExtensionAvailable_GL_VERSION_1_5) {
            isExtensionAvailable_GL_VERSION_1_5 = gl.isExtensionAvailable(GLExtensions.VERSION_1_5);
            checkFor_isExtensionAvailable_GL_VERSION_1_5 = true;
        }
        return isExtensionAvailable_GL_VERSION_1_5;
    }
}
