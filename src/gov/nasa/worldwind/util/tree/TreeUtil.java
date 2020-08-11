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

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;

/**
 * Utility methods for drawing tree controls.
 *
 * @author pabercrombie
 * @version $Id: TreeUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TreeUtil
{
    /**
     * Draw a rectangle in a unique pick color, and associate the color with a pickable object.
     *
     * @param dc           Draw context.
     * @param pickSupport  Pick support.
     * @param pickedObject Object to associate with pickable rectangle.
     * @param bounds       Bounds of the pickable rectangle.
     */
    public static void drawPickableRect(DrawContext dc, PickSupport pickSupport, Object pickedObject, Rectangle bounds)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Color color = dc.getUniquePickColor();
        int colorCode = color.getRGB();
        pickSupport.addPickableObject(colorCode, pickedObject);
        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

        drawRect(gl, bounds);
    }

    /**
     * Draw a rectangle.
     *
     * @param gl     GL
     * @param bounds Bounds of the rectangle, in GL coordinates.
     */
    public static void drawRect(GL2 gl, Rectangle bounds)
    {
        if (gl == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (bounds == null)
        {
            String message = Logging.getMessage("nullValue.BoundingBoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        gl.glRecti(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
    }

    public static void drawRectWithGradient(GL2 gl, Rectangle bounds, Color color1, Color color2, double opacity,
        String gradientDirection)
    {
        if (gl == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (bounds == null)
        {
            String message = Logging.getMessage("nullValue.BoundingBoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        gl.glBegin(GL2.GL_QUADS);

        if (AVKey.HORIZONTAL.equals(gradientDirection))
        {
            OGLUtil.applyColor(gl, color1, opacity, false);
            gl.glVertex2d(bounds.getMinX(), bounds.getMaxY());
            gl.glVertex2d(bounds.getMinX(), bounds.getMinY());

            OGLUtil.applyColor(gl, color2, opacity, false);
            gl.glVertex2d(bounds.getMaxX(), bounds.getMinY());
            gl.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
        }
        else
        {
            OGLUtil.applyColor(gl, color1, opacity, false);
            gl.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
            gl.glVertex2d(bounds.getMinX(), bounds.getMaxY());

            OGLUtil.applyColor(gl, color2, opacity, false);
            gl.glVertex2d(bounds.getMinX(), bounds.getMinY());
            gl.glVertex2d(bounds.getMaxX(), bounds.getMinY());
        }
        gl.glEnd();
    }
}
