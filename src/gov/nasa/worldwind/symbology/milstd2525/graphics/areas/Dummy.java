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

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Dummy graphic (hierarchy 2.X.2.3.1, SIDC: G*GPPD----****X).
 *
 * @author pabercrombie
 * @version $Id: Dummy.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Dummy extends AbstractMilStd2525TacticalGraphic
{
    protected Path path;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_DCPN_DMY);
    }

    public Dummy(String sidc)
    {
        super(sidc);
        this.path = this.createPath();
        this.path.setAttributes(this.getActiveShapeAttributes());
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.path.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.path.setDelegateOwner(owner);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The dummy graphic requires exactly three control points. Any positions beyond the first three will be ignored.
     *
     * @throws IllegalArgumentException if less than three control points are provided.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            Position pt1 = iterator.next();
            Position pt2 = iterator.next();
            Position pt3 = iterator.next();

            this.path.setPositions(Arrays.asList(pt2, pt1, pt3));
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.path.getPositions();
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.path.getReferencePosition();
    }

    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        attributes.setOutlineWidth(2.0);
        attributes.setOutlineStippleFactor(15);
        attributes.setOutlineStipplePattern((short) 0xAAAA);
    }

    protected Path createPath()
    {
        Path path = new Path();
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        return path;
    }
}
