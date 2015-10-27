/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.combine;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.glu.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: ShapeCombiner.java 2413 2014-10-30 21:33:37Z dcollins $
 */
public class ShapeCombiner
{
    protected Globe globe;
    protected double resolution;

    public ShapeCombiner(Globe globe, double resolution)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.globe = globe;
        this.resolution = resolution;
    }

    public Globe getGlobe()
    {
        return this.globe;
    }

    public double getResolution()
    {
        return this.resolution;
    }

    public ContourList union(Combinable... shapes)
    {
        if (shapes == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        CombineContext cc = this.createContext();

        try
        {
            this.union(cc, shapes);
        }
        finally
        {
            cc.dispose(); // releases GLU tessellator resources
        }

        return cc.getContours();
    }

    public ContourList intersection(Combinable... shapes)
    {
        if (shapes == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        CombineContext cc = this.createContext();

        try
        {
            if (shapes.length == 1)
                this.union(cc, shapes); // equivalent to the identity of the first shape
            else if (shapes.length > 1)
                this.intersection(cc, shapes);
        }
        finally
        {
            cc.dispose(); // releases GLU tessellator resources
        }

        return cc.getContours();
    }

    public ContourList difference(Combinable... shapes)
    {
        if (shapes == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        CombineContext cc = this.createContext();
        try
        {
            if (shapes.length == 1)
                this.union(cc, shapes); // equivalent to the identity of the first shape
            else if (shapes.length > 1)
                this.difference(cc, shapes);
        }
        finally
        {
            cc.dispose(); // releases GLU tessellator resources
        }

        return cc.getContours();
    }

    protected CombineContext createContext()
    {
        return new CombineContext(this.globe, this.resolution);
    }

    protected void union(CombineContext cc, Combinable... shapes)
    {
        GLUtessellator tess = cc.getTessellator();

        try
        {
            GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
            GLU.gluTessBeginPolygon(tess, null);

            for (Combinable combinable : shapes)
            {
                combinable.combine(cc);
            }
        }
        finally
        {
            GLU.gluTessEndPolygon(tess);
        }
    }

    protected void reverseUnion(CombineContext cc, Combinable... shapes)
    {
        GLUtessellator tess = cc.getTessellator();

        try
        {
            GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
            GLU.gluTessNormal(tess, 0, 0, -1); // reverse the winding order of the tessellated boundaries
            GLU.gluTessBeginPolygon(tess, null);

            for (Combinable combinable : shapes)
            {
                combinable.combine(cc);
            }
        }
        finally
        {
            GLU.gluTessEndPolygon(tess);
        }
    }

    protected void intersection(CombineContext cc, Combinable... shapes)
    {
        // Limit this operation to the intersection of the bounding regions. Since this is an intersection operation,
        // shapes outside of this region can be ignored or simplified.
        this.assembleBoundingSectors(cc, shapes);

        // Exit immediately if the bounding regions do not intersect.
        Sector sector = Sector.intersection(cc.getBoundingSectors());
        if (sector == null)
            return;

        cc.setSector(sector);

        // Compute the intersection of the first two shapes.
        this.intersection(cc, shapes[0], shapes[1]);

        // When the caller has specified more than two shapes, repeatedly compute the intersection of the current
        // contours with the next shape. This has the effect of progressively computing the intersection of all shapes.
        if (shapes.length > 2)
        {
            for (int i = 2; i < shapes.length; i++)
            {
                ContourList result = new ContourList(cc.getContours());
                cc.removeAllContours();

                this.intersection(cc, result, shapes[i]);
            }
        }
    }

    protected void intersection(CombineContext cc, Combinable a, Combinable b)
    {
        GLUtessellator tess = cc.getTessellator();

        try
        {
            GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ABS_GEQ_TWO);
            GLU.gluTessBeginPolygon(tess, null);

            a.combine(cc);
            b.combine(cc);
        }
        finally
        {
            GLU.gluTessEndPolygon(tess);
        }
    }

    protected void difference(CombineContext cc, Combinable... shapes)
    {
        // Limit this operation to the region bounding the shape that we're subtracting from. Since this is a difference
        // operation, shapes outside of this region can be ignored or simplified.
        Combinable a = shapes[0];
        this.assembleBoundingSectors(cc, a);

        // Exit immediately if the first shape has no bounding region.
        if (cc.getBoundingSectors().size() == 0)
            return;

        cc.setSector(cc.getBoundingSectors().get(0));

        // Compute the union of all shapes except the first, but reverse the winding order of the resultant contours.
        this.reverseUnion(cc, Arrays.copyOfRange(shapes, 1, shapes.length));
        Combinable b = new ContourList(cc.getContours());
        cc.removeAllContours(); // clear the context's contour list; we use it to store the final contours below

        // Combine the first shape with the union of all shapes exception the first. Since the union has its winding
        // order reversed, this has the effect of subtracting the union from the first shape.
        GLUtessellator tess = cc.getTessellator();
        try
        {
            GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE);
            GLU.gluTessNormal(tess, 0, 0, 1); // restore the GLU tessellator's normal vector
            GLU.gluTessBeginPolygon(tess, null);

            a.combine(cc);
            b.combine(cc);
        }
        finally
        {
            GLU.gluTessEndPolygon(tess);
        }
    }

    protected void assembleBoundingSectors(CombineContext cc, Combinable... shapes)
    {
        try
        {
            cc.setBoundingSectorMode(true);

            for (Combinable combinable : shapes)
            {
                combinable.combine(cc);
            }
        }
        finally
        {
            cc.setBoundingSectorMode(false);
        }
    }
}