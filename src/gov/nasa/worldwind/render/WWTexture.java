/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * Represents a texture derived from an image source such as an image file or a {@link java.awt.image.BufferedImage}.
 * <p>
 * The interface contains a method, {@link #isTextureInitializationFailed()} to determine whether the instance failed to
 * convert an image source to a texture. If such a failure occurs, the method returns true and no further attempts are
 * made to create the texture.
 *
 * @author tag
 * @version $Id: WWTexture.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface WWTexture
{
    /**
     * Returns the texture's image source.
     *
     * @return the texture's image source.
     */
    public Object getImageSource();

    /**
     * Makes this texture the current texture for rendering.
     * <p>
     * If the implementing instance's internal texture has not been created from its image source, the implementing
     * class determines when the texture is retrieved and available.
     * <p>
     * If a texture cannot be created from its image source it cannot be bound. This method returns an indication of
     * whether the texture was bound or was not bound due to a failure during creation.
     *
     * @param dc the current draw context.
     *
     * @return true if the texture was bound, otherwise false.
     */
    public boolean bind(DrawContext dc);

    /**
     * Applies any necessary transformations to the texture prior to its being rendered. A common transformation is
     * mapping texture coordinates from a flipped or non-square state to conventionally oriented OpenGL values.
     *
     * @param dc the current draw context.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void applyInternalTransform(DrawContext dc);

    /**
     * Indicates whether the texture is currently available for use without regenerating it from its image source.
     *
     * @param dc the current draw context
     *
     * @return true if the texture is available and consistent with its image source, otherwise false.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    boolean isTextureCurrent(DrawContext dc);

    /**
     * Returns the texture's width.
     *
     * @param dc the current draw context
     *
     * @return the texture's width, or 0 if the texture's size is currently unknown.
     */
    int getWidth(DrawContext dc);

    /**
     * Returns the texture's height
     *
     * @param dc the current draw context
     *
     * @return the texture's height, or 0 if the texture's size is currently unknown.
     */
    int getHeight(DrawContext dc);

    /**
     * Returns the texture's texture coordinates, which may be other than [0,0],[1,1] if the texture size is not a power
     * of two or the texture must be flipped when rendered.
     *
     * @return returns the texture's texture coordinates.
     */
    TextureCoords getTexCoords();

    /**
     * Indicates whether an attempt to initialize the texture failed, which occurs when the image source is a
     * non-existent image file or for other reasons specific to the image source.
     *
     * @return true if texture initialization failed, otherwise false.
     */
    boolean isTextureInitializationFailed();
}
