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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL2;

/**
 * @author dcollins
 * @version $Id: AbstractAnnotationLayout.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractAnnotationLayout implements AnnotationLayoutManager
{
    protected OGLStackHandler stackHandler;
    protected PickSupport pickSupport;

    protected AbstractAnnotationLayout()
    {
        this.stackHandler = new OGLStackHandler();
    }

    public PickSupport getPickSupport()
    {
        return this.pickSupport;
    }

    public void setPickSupport(PickSupport pickSupport)
    {
        this.pickSupport = pickSupport;
    }

    public void beginDrawAnnotations(DrawContext dc, java.awt.Rectangle bounds)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (bounds == null)
        {
            String message = Logging.getMessage("nullValue.RectangleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.stackHandler.pushModelview(gl);
    }

    public void endDrawAnnotations(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.stackHandler.pop(gl);
    }

    protected java.awt.Dimension getAnnotationSize(DrawContext dc, Annotation annotation)
    {
        try
        {
            return annotation.getPreferredSize(dc);
        }
        catch (Exception e)
        {
            // Trap and log exceptions thrown by computing an annotation's preferred size. This will prevent one
            // annotation from throwing an exception and preventing all other anotations from reporting their
            // preferred size.
            String message = Logging.getMessage("generic.ExceptionWhileComputingSize", annotation);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        return null;
    }

    protected void drawAnnotation(DrawContext dc, Annotation annotation, int width, int height, double opacity,
        Position pickPosition)
    {
        try
        {
            if (this.pickSupport != null)
                annotation.setPickSupport(this.pickSupport);

            annotation.draw(dc, width, height, opacity, pickPosition);
        }
        catch (Exception e)
        {
            // Trap and log exceptions thrown by rendering an annotation. This will prevent one annotation from
            // throwing an exception and preventing all other anotations from rendering.
            String message = Logging.getMessage("generic.ExceptionWhileRenderingAnnotation", annotation);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }
}
