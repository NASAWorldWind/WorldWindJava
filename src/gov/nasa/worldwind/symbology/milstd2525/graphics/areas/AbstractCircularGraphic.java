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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Base class for circular area graphics.
 *
 * @author pabercrombie
 * @version $Id: AbstractCircularGraphic.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractCircularGraphic extends AbstractMilStd2525TacticalGraphic
    implements TacticalCircle, PreRenderable
{
    protected SurfaceCircle circle;

    /**
     * Create a new circular area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AbstractCircularGraphic(String sidc)
    {
        super(sidc);
        this.circle = this.createShape();
    }

    /** {@inheritDoc} */
    public double getRadius()
    {
        return this.circle.getRadius();
    }

    /** {@inheritDoc} */
    public void setRadius(double radius)
    {
        this.circle.setRadius(radius);
        this.onModifierChanged();
        this.reset();
    }

    /** {@inheritDoc} */
    public Position getPosition()
    {
        return this.getReferencePosition();
    }

    /** {@inheritDoc} */
    public void setPosition(Position position)
    {
        this.moveTo(position);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point, which determines the center of the
     *                  circle.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterator<? extends Position> iterator = positions.iterator();
        if (!iterator.hasNext())
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.circle.setCenter(iterator.next());
        this.reset();
    }

    /** {@inheritDoc} */
    @Override
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier) && (value instanceof Double))
            this.setRadius((Double) value);
        else
            super.setModifier(modifier, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier))
            return this.getRadius();
        else
            return super.getModifier(modifier);
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(new Position(this.circle.getCenter(), 0));
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.circle.getReferencePosition();
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.determineActiveAttributes();

        this.circle.preRender(dc);
    }

    /**
     * Render the polygon.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.circle.render(dc);
    }

    /**
     * Invoked when the position or radius of the circle changes. This implementation does nothing, but subclasses can
     * override to invalid state when the graphic is changed.
     */
    protected void reset()
    {
        // Do nothing, but allow subclasses to override.
    }

    /** {@inheritDoc} Overridden to apply the delegate owner to shapes used to draw the circle. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Apply the delegate owner to the circle, if an owner has been set. If no owner is set, make this graphic the
        // circle's owner.
        Object owner = this.getDelegateOwner();
        if (owner == null)
            owner = this;

        this.circle.setDelegateOwner(owner);
        if (this.labels != null)
        {
            for (TacticalGraphicLabel label : this.labels)
            {
                label.setDelegateOwner(owner);
            }
        }
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.circle.setDelegateOwner(owner);
    }

    protected SurfaceCircle createShape()
    {
        SurfaceCircle circle = new SurfaceCircle();
        circle.setDelegateOwner(this.getActiveDelegateOwner());
        circle.setAttributes(this.activeShapeAttributes);
        return circle;
    }
}
