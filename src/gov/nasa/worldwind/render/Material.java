/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: Material.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Material
{
    private final Color ambient;
    private final Color diffuse;
    private final Color specular;
    private final Color emission;
    private final double shininess;

    public static final Material WHITE = new Material(Color.WHITE);
    public static final Material LIGHT_GRAY = new Material(Color.LIGHT_GRAY);
    public static final Material GRAY = new Material(Color.GRAY);
    public static final Material DARK_GRAY = new Material(Color.DARK_GRAY);
    public static final Material BLACK = new Material(Color.BLACK);
    public static final Material RED = new Material(Color.RED);
    public static final Material PINK = new Material(Color.PINK);
    public static final Material ORANGE = new Material(Color.ORANGE);
    public static final Material YELLOW = new Material(Color.YELLOW);
    public static final Material GREEN = new Material(Color.GREEN);
    public static final Material MAGENTA = new Material(Color.MAGENTA);
    public static final Material CYAN = new Material(Color.CYAN);
    public static final Material BLUE = new Material(Color.BLUE);

    public Material(Color specular, Color diffuse, Color ambient, Color emission, float shininess)
    {
        if (specular == null || diffuse == null || ambient == null || emission == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.emission = emission;
        this.shininess = shininess;
    }

    public Material(Color color, float shininess)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ambient = this.makeDarker(color);
        this.diffuse = color;
        this.specular = new Color(255, 255, 255, color.getAlpha());
        this.emission = new Color(0, 0, 0, color.getAlpha());
        this.shininess = shininess;
    }

    public Material(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ambient = this.makeDarker(color);
        this.diffuse = color;
        this.specular = new Color(255, 255, 255, color.getAlpha());
        this.emission = new Color(0, 0, 0, color.getAlpha());
        this.shininess = 80.0f;
    }

    public final Color getAmbient()
    {
        return this.ambient;
    }

    public final Color getDiffuse()
    {
        return this.diffuse;
    }

    public final Color getSpecular()
    {
        return this.specular;
    }

    public final Color getEmission()
    {
        return this.emission;
    }

    public final double getShininess()
    {
        return this.shininess;
    }

    public void apply(GL2 gl, int face)
    {
        if (gl == null)
        {
            String msg = Logging.getMessage("nullValue.GLIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        glMaterial(gl, face, GL2.GL_AMBIENT, this.ambient);
        glMaterial(gl, face, GL2.GL_DIFFUSE, this.diffuse);
        glMaterial(gl, face, GL2.GL_SPECULAR, this.specular);
        glMaterial(gl, face, GL2.GL_EMISSION, this.emission);
        gl.glMaterialf(face, GL2.GL_SHININESS, (float) this.shininess);
    }

    public void apply(GL2 gl, int face, float alpha)
    {
        if (gl == null)
        {
            String msg = Logging.getMessage("nullValue.GLIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // The alpha value at a vertex is taken only from the diffuse material's alpha channel. Therefore we specify
        // alpha for the diffuse value, and alpha=0 for ambient, specular and emission values.
        glMaterial(gl, face, GL2.GL_AMBIENT, this.ambient, 0.0f);
        glMaterial(gl, face, GL2.GL_DIFFUSE, this.diffuse, alpha);
        glMaterial(gl, face, GL2.GL_SPECULAR, this.specular, 0.0f);
        glMaterial(gl, face, GL2.GL_EMISSION, this.emission, 0.0f);
        gl.glMaterialf(face, GL2.GL_SHININESS, (float) this.shininess);
    }

    protected void glMaterial(GL2 gl, int face, int name, Color color)
    {
        if (gl == null)
        {
            String msg = Logging.getMessage("nullValue.GLIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        gl.glMaterialfv(face, name, compArray, 0);
    }

    protected void glMaterial(GL2 gl, int face, int name, Color color, float alpha)
    {
        if (gl == null)
        {
            String msg = Logging.getMessage("nullValue.GLIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        compArray[3] = alpha;
        gl.glMaterialfv(face, name, compArray, 0);
    }

    //protected void glMaterialPremult(GL2 gl, int face, int name, Color color)
    //{
    //    float[] compArray = new float[4];
    //    color.getRGBComponents(compArray);
    //    compArray[0] = compArray[0] * compArray[3];
    //    compArray[1] = compArray[1] * compArray[3];
    //    compArray[2] = compArray[2] * compArray[3];
    //    gl.glMaterialfv(face, name, compArray, 0);
    //}

    //protected void glMaterialfvPremult(GL2 gl, int face, int name, Color color, float alpha)
    //{
    //    float[] compArray = new float[4];
    //    color.getRGBColorComponents(compArray);
    //    compArray[0] = compArray[0] * alpha;
    //    compArray[1] = compArray[1] * alpha;
    //    compArray[2] = compArray[2] * alpha;
    //    compArray[3] = alpha;
    //    gl.glMaterialfv(face, name, compArray, 0);
    //}

    protected Color makeDarker(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        float factor = 0.3f;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        return new Color(
            Math.max(0, (int) (r * factor)),
            Math.max(0, (int) (g * factor)),
            Math.max(0, (int) (b * factor)),
            a);
    }

    public void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        String encodedColor = RestorableSupport.encodeColor(this.ambient);
        if (encodedColor != null)
            rs.addStateValueAsString(so, "ambient", encodedColor);

        encodedColor = RestorableSupport.encodeColor(this.diffuse);
        if (encodedColor != null)
            rs.addStateValueAsString(so, "diffuse", encodedColor);

        encodedColor = RestorableSupport.encodeColor(this.specular);
        if (encodedColor != null)
            rs.addStateValueAsString(so, "specular", encodedColor);

        encodedColor = RestorableSupport.encodeColor(this.emission);
        if (encodedColor != null)
            rs.addStateValueAsString(so, "emission", encodedColor);

        rs.addStateValueAsDouble(so, "shininess", this.shininess);
    }

    public Material restoreState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        double shininess = this.getShininess();
        Double d = rs.getStateValueAsDouble(so, "shininess");
        if (d != null)
            shininess = d;

        String as = rs.getStateValueAsString(so, "ambient");
        Color ambient = RestorableSupport.decodeColor(as);
        if (ambient == null)
            ambient = this.getAmbient();

        String ds = rs.getStateValueAsString(so, "diffuse");
        Color diffuse = RestorableSupport.decodeColor(ds);
        if (diffuse == null)
            diffuse = this.getDiffuse();

        String ss = rs.getStateValueAsString(so, "specular");
        Color specular = RestorableSupport.decodeColor(ss);
        if (specular == null)
            specular = this.getSpecular();

        String es = rs.getStateValueAsString(so, "emission");
        Color emission = RestorableSupport.decodeColor(es);
        if (emission == null)
            emission = this.getEmission();

        return new Material(specular, diffuse, ambient, emission, (float) shininess);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Material that = (Material) o;

        if (Double.compare(this.shininess, that.shininess) != 0)
            return false;
        if (this.ambient != null ? !this.ambient.equals(that.ambient) : that.ambient != null)
            return false;
        if (this.diffuse != null ? !this.diffuse.equals(that.diffuse) : that.diffuse != null)
            return false;
        if (this.specular != null ? !this.specular.equals(that.specular) : that.specular != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.emission != null ? !this.emission.equals(that.emission) : that.emission != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp = (this.shininess != +0.0d) ? Double.doubleToLongBits(this.shininess) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.ambient != null ? this.ambient.hashCode() : 0);
        result = 31 * result + (this.diffuse != null ? this.diffuse.hashCode() : 0);
        result = 31 * result + (this.specular != null ? this.specular.hashCode() : 0);
        result = 31 * result + (this.emission != null ? this.emission.hashCode() : 0);
        return result;
    }
}
