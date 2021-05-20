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
package gov.nasa.worldwind.animation;

/**
 * @author jym
 * @version $Id: Animator.java 1171 2013-02-11 21:45:02Z dcollins $
 */

/**
 * The <code>Animator</code> interface provides a way to iterate through a series of values.  It can be used with
 * a simple interpolation function, or something more elaborate.  The <code>PropertyAccessor</code> class and its
 * interfaces can be used to agnostically attach to data members of any class.
*/
public interface Animator
{
    /**
     * Iterates to the next value.  The implementation is expected to apply that next value to the property
     * it is attached to.
     */
    void next();

    /**
     * Starts the <code>Animator</code>.  The implemenation should return <code>true</code> from <code>hasNext</code>
     */
    void start();

    /**
     * Stops the <code>Animator</code>.  The implmenentation should return <code>false</code> from <code>hasNext</code>
     */
    void stop();

    /**
     * Returns <code>true</code> if the <code>Animator</code> has more elements.
     *
     * @return <code>true</code> if the <code>Animator</code> has more elements.
     */
    boolean hasNext();

    /**
     * Set the value of the attached property to the value associated with this interpolant value.
     * @param interpolant A value between 0 and 1.
     */
    void set(double interpolant);
}
