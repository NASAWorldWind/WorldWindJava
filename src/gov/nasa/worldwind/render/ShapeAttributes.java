/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.util.RestorableSupport;

/**
 * Holds common attributes for WorldWind shapes such as {@link gov.nasa.worldwind.render.Path}, {@link
 * gov.nasa.worldwind.render.Polygon}, and {@link gov.nasa.worldwind.render.SurfaceShape}. Changes made to the
 * attributes are applied to the shape when the <code>WorldWindow</code> renders the next frame. Instances of
 * <code>ShapeAttributes</code> may be shared by many shapes, thereby reducing the memory normally required to store
 * attributes for each shape.
 *
 * @author dcollins
 * @version $Id: ShapeAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ShapeAttributes extends Exportable
{
    /**
     * Returns a new ShapeAttributes instance of the same type as this ShapeAttributes who's properties are configured
     * exactly as this ShapeAttributes.
     *
     * @return a copy of this ShapeAttributes.
     */
    ShapeAttributes copy();

    /**
     * Copies the specified ShapeAttributes' properties into this object's properties. This does nothing if the
     * specified attributes is <code>null</code>.
     *
     * @param attributes the attributes to copy.
     */
    void copy(ShapeAttributes attributes);

    /**
     * Indicates whether some of the shape's attributes are unresolved.
     *
     * @return <code>true</code> to indicate that one or more attributes are unresolved, otherwise <code>false</code>.
     *
     * @see #setUnresolved(boolean)
     */
    boolean isUnresolved();

    /**
     * Specifies whether some of the shape's attributes are unresolved. This can be used to denote that a shape's
     * attributes are being retrieved from a non-local resource. During retrieval, the controlling code sets unresolved
     * to <code>true</code>, then sets it to <code>false</code> once the retrieval is complete. Code that interprets the
     * attributes knows that the attributes are complete when unresolved is <code>false</code>.
     *
     * @param unresolved <code>true</code> to specify that one or more attributes are unresolved, otherwise
     *                   <code>false</code>.
     *
     * @see #isUnresolved()
     */
    void setUnresolved(boolean unresolved);

    /**
     * Indicates whether the shape's interior geometry is drawn.
     *
     * @return <code>true</code> if the shape's interior is drawn, otherwise <code>false</code>.
     *
     * @see #setDrawInterior(boolean)
     */
    boolean isDrawInterior();

    /**
     * Specifies whether to draw the shape's interior geometry.
     *
     * @param draw <code>true</code> to draw the shape's interior, otherwise <code>false</code>.
     *
     * @see #isDrawInterior()
     */
    void setDrawInterior(boolean draw);

    /**
     * Indicates whether the shape's outline geometry is drawn.
     *
     * @return <code>true</code> if the shape's outline is drawn, otherwise <code>false</code>.
     *
     * @see #setDrawOutline(boolean)
     */
    boolean isDrawOutline();

    /**
     * Specifies whether to draw the shape's outline geometry.
     *
     * @param draw <code>true</code> to draw the shape's outline, otherwise <code>false</code>.
     *
     * @see #isDrawOutline()
     */
    void setDrawOutline(boolean draw);

    /**
     * Indicates whether the shape is rendered with smooth lines and edges.
     *
     * @return <code>true</code> if the shape is drawn with smooth lines and edges, otherwise <code>false</code>.
     *
     * @see #setEnableAntialiasing(boolean)
     */
    boolean isEnableAntialiasing();

    /**
     * Specifies whether the shape should be rendered with smooth lines and edges.
     *
     * @param enable <code>true</code> to draw the shape with smooth lines and edges, otherwise <code>false</code>.
     *
     * @see #isEnableAntialiasing()
     */
    void setEnableAntialiasing(boolean enable);

    /**
     * Indicates whether lighting is applied to the shape.
     *
     * @return <code>true</code> to apply lighting, otherwise <code>false</code>.
     *
     * @see #setEnableLighting(boolean)
     */
    boolean isEnableLighting();

    /**
     * Specifies whether to apply lighting to the shape. By default, the shape is lit using the
     * <code>DrawContext</code>'s standard lighting by calling {@link DrawContext#beginStandardLighting()} and {@link
     * DrawContext#endStandardLighting()} before and after the shape is rendered, respectively.
     *
     * @param enableLighting <code>true</code> to apply lighting, otherwise <code>false</code>.
     *
     * @see #isEnableLighting()
     */
    void setEnableLighting(boolean enableLighting);

    /**
     * Indicates the material properties of the shape's interior. If lighting is applied to the shape, this indicates
     * the interior's ambient, diffuse, and specular colors, its shininess, and the color of any emitted light.
     * Otherwise, the material's diffuse color indicates the shape's constant interior color.
     *
     * @return the material applied to the shape's interior.
     *
     * @see #setInteriorMaterial(Material)
     */
    Material getInteriorMaterial();

    /**
     * Specifies the material properties of the shape's interior. If lighting is applied to the shape, this specifies
     * the interior's ambient, diffuse, and specular colors, its shininess, and the color of any emitted light.
     * Otherwise, the material's diffuse color specifies the shape's constant interior color.
     *
     * @param material the material to apply to the shape's interior.
     *
     * @throws IllegalArgumentException if <code>material</code> is <code>null</code>.
     * @see #getInteriorMaterial()
     */
    void setInteriorMaterial(Material material);

    /**
     * Indicates the material properties of the shape's outline. If lighting is applied to the shape, this indicates the
     * outline's ambient, diffuse, and specular colors, its shininess, and the color of any emitted light. Otherwise,
     * the material's diffuse color indicates the shape's constant outline color.
     *
     * @return the material applied to the shape's outline.
     *
     * @see #setOutlineMaterial(Material)
     */
    Material getOutlineMaterial();

    /**
     * Specifies the material properties of the shape's outline. If lighting is applied to the shape, this specifies the
     * outline's ambient, diffuse, and specular colors, its shininess, and the color of any emitted light. Otherwise,
     * the material's diffuse color specifies as the shape's constant outline color.
     *
     * @param material the material to apply to the shape's outline.
     *
     * @throws IllegalArgumentException if <code>material</code> is <code>null</code>.
     * @see #getOutlineMaterial()
     */
    void setOutlineMaterial(Material material);

    /**
     * Indicates the opacity of the shape's interior as a floating-point value in the range 0.0 to 1.0.
     *
     * @return the interior opacity as a floating-point value from 0.0 to 1.0.
     *
     * @see #setInteriorOpacity(double)
     */
    double getInteriorOpacity();

    /**
     * Specifies the opacity of the shape's interior as a floating-point value in the range 0.0 to 1.0. A value of 1.0
     * specifies a completely opaque interior, and 0.0 specifies a completely transparent interior. Values in between
     * specify a partially transparent interior.
     *
     * @param opacity the interior opacity as a floating-point value from 0.0 to 1.0.
     *
     * @throws IllegalArgumentException if <code>opacity</code> is less than 0.0 or greater than 1.0.
     * @see #getInteriorOpacity()
     */
    void setInteriorOpacity(double opacity);

    /**
     * Indicates the opacity of the shape's outline as a floating-point value in the range 0.0 to 1.0.
     *
     * @return the outline opacity as a floating-point value from 0.0 to 1.0.
     *
     * @see #setOutlineOpacity(double)
     */
    double getOutlineOpacity();

    /**
     * Specifies the opacity of the shape's outline as a floating-point value in the range 0.0 to 1.0. A value of 1.0
     * specifies a completely opaque outline, and 0.0 specifies a completely transparent outline. Values in between
     * specify a partially transparent outline.
     *
     * @param opacity the outline opacity as a floating-point value from 0.0 to 1.0.
     *
     * @throws IllegalArgumentException if <code>opacity</code> is less than 0.0 or greater than 1.0.
     * @see #getOutlineOpacity()
     */
    void setOutlineOpacity(double opacity);

    /**
     * Indicates the line width (in pixels) used when rendering the shape's outline. The returned value is either zero
     * or a positive floating-point value.
     *
     * @return the line width in pixels.
     *
     * @see #setOutlineWidth(double)
     */
    double getOutlineWidth();

    /**
     * Specifies the line width (in pixels) to use when rendering the shape's outline. The specified <code>width</code>
     * must be zero or a positive floating-point value. Specifying a line width of zero disables the shape's outline.
     * The <code>width</code> may be limited by an implementation-defined maximum during rendering. The maximum width is
     * typically 10 pixels.
     *
     * @param width the line width in pixels.
     *
     * @throws IllegalArgumentException if <code>width</code> is less than zero.
     * @see #getOutlineWidth()
     */
    void setOutlineWidth(double width);

    /**
     * Indicates the number of times each bit in the outline stipple pattern is repeated before the next bit is used.
     *
     * @return the number of times each bit in the outline stipple pattern is repeated.
     *
     * @see #setOutlineStippleFactor(int)
     */
    int getOutlineStippleFactor();

    /**
     * Specifies the number of times each bit in the outline stipple pattern should be repeated before the next bit is
     * used. For example, if <code>factor</code> is 3, each bit is repeated 3 times before using the next bit. The
     * specified <code>factor</code> must be either zero or an integer greater than zero. The <code>factor</code> may be
     * limited by an implementation-defined maximum during rendering. The maximum stipple factor is typically 256.
     * <p>
     * To disable outline stippling, either specify a stipple factor of 0, or specify a stipple pattern of all 1 bits:
     * <code>0xFFFF</code>.
     *
     * @param factor the number of times each bit in the outline stipple pattern should be repeated.
     *
     * @throws IllegalArgumentException if <code>factor</code> is less than zero.
     * @see #getOutlineStippleFactor()
     * @see #setOutlineStipplePattern(short)
     */
    void setOutlineStippleFactor(int factor);

    /**
     * Indicates the 16-bit integer that defines which pixels are rendered in the shape's outline.
     *
     * @return a 16-bit integer whose bit pattern defines which pixels are rendered in the shape's outline.
     *
     * @see #setOutlineStipplePattern(short)
     */
    short getOutlineStipplePattern();

    /**
     * Specifies a 16-bit integer that defines which pixels are rendered in the shape's outline. Starting at the least
     * significant bit and moving to the most significant bit, the 16 bits define a repeating a pattern of which pixels
     * in the outline are rendered and which are suppressed. Each bit corresponds to a pixel in the shape's outline, and
     * the bit pattern repeats after reaching n*16 pixels, where n is the stipple factor. Each bit is repeated n-times
     * according to the outline stipple factor. For example, if the outline stipple factor is 3, each bit is repeated 3
     * times before using the next bit.
     * <p>
     * To disable outline stippling, either specify a stipple factor of 0, or specify a stipple pattern of all 1 bits:
     * <code>0xFFFF</code>.
     *
     * @param pattern a 16-bit integer whose bit pattern defines which pixels are rendered in the shape's outline.
     *
     * @see #getOutlineStipplePattern()
     * @see #setOutlineStippleFactor(int)
     */
    void setOutlineStipplePattern(short pattern);

    /**
     * Indicates the image source that is applied as a texture to the shape's interior.
     *
     * @return the source of the shape's texture, either a {@link String} path, a {@link java.net.URL}, a {@link
     *         java.awt.image.BufferedImage}, or <code>null</code>.
     *
     * @see #setImageSource(Object)
     */
    Object getImageSource();

    /**
     * Specifies the image source to apply as a texture to the shape's interior, or <code>null</code> to specify that
     * the shape should not have a texture. When not <code>null</code>, the texture replaces the shape's interior
     * material. The source type may be one of the following: <ul> <li>{@link String} containing a path to a local file,
     * or a resource on the classpath.</li> <li>{@link java.net.URL}</li> <li>{@link java.awt.image.BufferedImage}</li>
     * <li><code>null</code></li> </ul> If the image source is a file or a <code>URL</code>, it is read only when the
     * shape is rendered.
     *
     * @param imageSource the source of the shape's texture, either a <code>String</code> path, a <code>URL</code>, a
     *                    <code>BufferedImage</code>, or <code>null</code>.
     *
     * @see #getImageSource()
     */
    void setImageSource(Object imageSource);

    /**
     * Indicates the amount the shape's texture is scaled by as a floating-point value.
     *
     * @return the amount the shape's texture is scaled by as a floating-point value. This value is always greater
     *         than zero.
     *
     * @see #setImageScale(double)
     */
    double getImageScale();

    /**
     * Specifies the amount to scale the shape's texture as a floating-point value. A value of 1.0 specifies that the
     * texture should be applied without any scaling, a value greater than 1.0 specifies that the texture should be
     * magnified, and a value less than 1.0 specifies that the texture should be minified. For example, a scale of 2.0
     * magnifies the texture by a factor of 2x.
     *
     * @param scale the amount to scale the shape's texture as a floating-point value.
     *
     * @throws IllegalArgumentException if <code>scale</code> is less than or equal to zero.
     * @see #getImageScale()
     * @see #setImageSource(Object)
     */
    void setImageScale(double scale);

    /**
     * Saves the attributes' current state in the specified <code>RestorableSupport</code>. If the
     * <code>StateObject</code> is not <code>null</code> the state is appended to it. Otherwise the state is added to
     * the <code>RestorableSupport</code> root. This state can be restored later by calling {@link
     * #restoreState(gov.nasa.worldwind.util.RestorableSupport, gov.nasa.worldwind.util.RestorableSupport.StateObject)}.
     *
     * @param rs the <code>RestorableSupport</code> that receives the attributes' state.
     * @param so the <code>StateObject</code> the state is appended to, if not <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>rs</code> is <code>null</code>.
     */
    void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so);

    /**
     * Restores the state of any attributes contained in the specified <code>RestorableSupport</code>. If the
     * <code>StateObject</code> is not <code>null</code> it's searched for attribute state values, otherwise the
     * <code>RestorableSupport</code> root is searched.
     *
     * @param rs the <code>RestorableSupport</code> that contains the attributes' state.
     * @param so the <code>StateObject</code> to search for state values, if not <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>rs</code> is <code>null</code>.
     */
    void restoreState(RestorableSupport rs, RestorableSupport.StateObject so);
}
