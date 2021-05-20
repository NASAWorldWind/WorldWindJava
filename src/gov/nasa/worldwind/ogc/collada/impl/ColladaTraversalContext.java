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

package gov.nasa.worldwind.ogc.collada.impl;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.util.Logging;

import java.util.Stack;

/**
 * Context to keep track of state while a COLLADA document is traversed. The traversal context keeps track of the
 * transform matrix stack that determines how COLLADA nodes are rendered.
 *
 * @author pabercrombie
 * @version $Id: ColladaTraversalContext.java 661 2012-06-26 18:02:23Z pabercrombie $
 */
public class ColladaTraversalContext
{
    /** Transform matrix stack. */
    protected Stack<Matrix> matrixStack = new Stack<Matrix>();

    /** Create a new traversal context. The traversal matrix stack initially contains one element: the identity matrix. */
    public ColladaTraversalContext()
    {
        this.matrixStack = new Stack<Matrix>();
        this.matrixStack.push(Matrix.IDENTITY);
    }

    /**
     * Returns the matrix at the top of the matrix stack, but does not modify the stack.
     *
     * @return The matrix at the top of the matrix stack.
     */
    public Matrix peekMatrix()
    {
        return this.matrixStack.peek();
    }

    /** Clone the matrix at the top of the matrix stack and push the clone onto the stack. */
    public void pushMatrix()
    {
        this.matrixStack.push(this.peekMatrix());
    }

    /**
     * Push a matrix onto the stack.
     *
     * @param m Matrix to add to the stack. This matrix becomes the new top matrix.
     */
    public void pushMatrix(Matrix m)
    {
        if (m == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.matrixStack.push(m);
    }

    /**
     * Removes the matrix at the top of the matrix stack.
     *
     * @return The matrix that was at the top of the stack.
     */
    public Matrix popMatrix()
    {
        return this.matrixStack.pop();
    }

    /**
     * Multiply the matrix at the top of the stack with another matrix. The product becomes the new top matrix.
     *
     * @param m Matrix to multiply. Multiplication is performed as top * m.
     */
    public void multiplyMatrix(Matrix m)
    {
        if (m == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Matrix top = this.popMatrix();
        this.pushMatrix(top.multiply(m));
    }

    /** Reset the context so that it may be used for a fresh traversal. */
    public void initialize()
    {
        this.matrixStack.clear();
        this.pushMatrix(Matrix.IDENTITY);
    }
}
