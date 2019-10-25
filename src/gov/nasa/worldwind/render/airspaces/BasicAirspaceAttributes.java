/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;

/**
 * Basic implementation of the {@link gov.nasa.worldwind.render.airspaces.AirspaceAttributes} interface.
 * AirspaceAttributes was originally designed as a special purpose attribute bundle for {@link Airspace} shapes, but is
 * now redundant subclass of {@link gov.nasa.worldwind.render.BasicShapeAttributes}. BasicAirspaceAttributes is still
 * supported to ensure backward compatibility with earlier versions of WorldWind. Usage of methods unique to
 * AirspaceAttributes should be replaced with the equivalent methods in ShapeAttributes.
 *
 * @author tag
 * @version $Id: BasicAirspaceAttributes.java 2318 2014-09-17 18:26:33Z tgaskins $
 */
public class BasicAirspaceAttributes extends BasicShapeAttributes implements AirspaceAttributes
{
    /**
     * Creates a new BasicAirspaceAttributes with the default attributes. The default attributes differ from
     * BasicShapeAttributes, and are as follows:
     * <table> <caption style="font-weight: bold;">Default Attributes</caption><tr><th>Attribute</th><th>Default Value</th></tr> <tr><td>unresolved</td><td><code>true</code></td></tr>
     * <tr><td>drawInterior</td><td><code>true</code></td></tr> <tr><td>drawOutline</td><td><code>false</code></td></tr>
     * <tr><td>enableAntialiasing</td><td><code>false</code></td></tr> <tr><td>enableLighting</td><td><code>true</code></td></tr>
     * <tr><td>interiorMaterial</td><td>{@link gov.nasa.worldwind.render.Material#WHITE}</td></tr>
     * <tr><td>outlineMaterial</td><td>{@link gov.nasa.worldwind.render.Material#BLACK}</td></tr>
     * <tr><td>interiorOpacity</td><td>1.0</td></tr> <tr><td>outlineOpacity</td><td>1.0</td></tr>
     * <tr><td>outlineWidth</td><td>1.0</td></tr> <tr><td>outlineStippleFactor</td><td>0</td></tr>
     * <tr><td>outlineStipplePattern</td><td>0xF0F0</td></tr> <tr><td>imageSource</td><td><code>null</code></td></tr>
     * <tr><td>imageScale</td><td>1.0</td></tr> </table>
     */
    public BasicAirspaceAttributes()
    {
        // Configure this AirspaceAttributes to preserve the original defaults of BasicAirspaceAttributes and
        // AirspaceRenderer.

        this.drawOutline = false;
        this.enableAntialiasing = false;
        this.enableLighting = true;
    }

    /**
     * Creates a new BasicAirspaceAttributes with the specified interior material and interior opacity. All other
     * attributes are set to the default values, which differ from BasicShapeAttributes, and are as follows:
     * <table> <caption style="font-weight: bold;">Default Attributes</caption><tr><th>Attribute</th><th>Default Value</th></tr> <tr><td>unresolved</td><td><code>true</code></td></tr>
     * <tr><td>drawInterior</td><td><code>true</code></td></tr> <tr><td>drawOutline</td><td><code>false</code></td></tr>
     * <tr><td>enableAntialiasing</td><td><code>false</code></td></tr> <tr><td>enableLighting</td><td><code>true</code></td></tr>
     * <tr><td>interiorMaterial</td><td>material</td></tr> <tr><td>outlineMaterial</td><td>{@link
     * gov.nasa.worldwind.render.Material#BLACK}</td></tr> <tr><td>interiorOpacity</td><td>opacity</td></tr>
     * <tr><td>outlineOpacity</td><td>1.0</td></tr> <tr><td>outlineWidth</td><td>1.0</td></tr>
     * <tr><td>outlineStippleFactor</td><td>0</td></tr> <tr><td>outlineStipplePattern</td><td>0xF0F0</td></tr>
     * <tr><td>imageSource</td><td><code>null</code></td></tr> <tr><td>imageScale</td><td>1.0</td></tr> </table>
     * 
     * @param material Material to apply.
     * @param opacity the opacity to set.
     */
    public BasicAirspaceAttributes(Material material, double opacity)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (opacity < 0.0 || opacity > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity=" + opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Configure this AirspaceAttributes to preserve the original defaults of BasicAirspaceAttributes and
        // AirspaceRenderer.

        this.drawOutline = false;
        this.enableAntialiasing = false;
        this.enableLighting = true;
        this.interiorMaterial = material;
        this.interiorOpacity = opacity;
    }

    /**
     * Creates a new <code>BasicAirspaceAttributes</code> configured with the specified
     * {@link gov.nasa.worldwind.render.ShapeAttributes}.
     *
     * @param attributes the attributes to configure the new <code>BasicAirspaceAttributes</code> with.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is <code>null</code>.
     */
    public BasicAirspaceAttributes(ShapeAttributes attributes)
    {
        super(attributes);
    }

    /**
     * Creates a new <code>BasicAirspaceAttributes</code> configured with the specified <code>attributes</code>.
     *
     * @param attributes the attributes to configure the new <code>BasicAirspaceAttributes</code> with.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is <code>null</code>.
     */
    public BasicAirspaceAttributes(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    /** {@inheritDoc} */
    public AirspaceAttributes copy()
    {
        return new BasicAirspaceAttributes(this);
    }

    /** {@inheritDoc} */
    public void copy(ShapeAttributes attributes)
    {
        super.copy(attributes);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getInteriorMaterial()} instead.
     */
    @Deprecated
    public Material getMaterial()
    {
        return this.getInteriorMaterial();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)} instead.
     */
    @Deprecated
    public void setMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setInteriorMaterial(material);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getInteriorOpacity()} instead.
     */
    @Deprecated
    public double getOpacity()
    {
        return this.getInteriorOpacity();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #setInteriorOpacity(double)} instead.
     */
    @Deprecated
    public void setOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setInteriorOpacity(opacity);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link Material#apply(com.jogamp.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    @Deprecated
    public void applyInterior(DrawContext dc, boolean enableMaterial)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.applyMaterial(dc, this.getInteriorMaterial(), this.getInteriorOpacity(), enableMaterial);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link Material#apply(com.jogamp.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    @Deprecated
    public void applyOutline(DrawContext dc, boolean enableMaterial)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.applyMaterial(dc, this.getOutlineMaterial(), this.getOutlineOpacity(), enableMaterial);

        GL gl = dc.getGL();
        gl.glLineWidth((float) this.getOutlineWidth());
    }

    /** {@inheritDoc} */
    public void restoreState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        super.restoreState(rs, so);

        this.restoreDeprecatedState(rs, so);
    }

    protected void restoreDeprecatedState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        // Restore deprecated interior material state used prior to integration with ShapeAttributes.
        RestorableSupport.StateObject mo = rs.getStateObject(so, "material");
        if (mo != null)
            this.setInteriorMaterial(this.getInteriorMaterial().restoreState(rs, mo));

        // Restore deprecated interior opacity state used prior to integration with ShapeAttributes.
        Double d = rs.getStateValueAsDouble(so, "opacity");
        if (d != null)
            this.setInteriorOpacity(d);
    }

    protected void applyMaterial(DrawContext dc, Material material, double opacity, boolean enableMaterial)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (material != null)
        {
            if (enableMaterial)
            {
                material.apply(gl, GL2.GL_FRONT_AND_BACK, (float) opacity);
            }
            else
            {
                float[] compArray = new float[4];
                material.getDiffuse().getRGBComponents(compArray);
                compArray[3] = (float) opacity;
                gl.glColor4fv(compArray, 0);
            }
        }
    }
}
