/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.segmentplane;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: SegmentPlaneAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SegmentPlaneAttributes
{
    public static class GeometryAttributes
    {
        private boolean visible;
        private boolean pickEnabled;
        private Material material;
        private double opacity;
        private double size;
        private double pickSize;
        private Vec4 offset;

        public GeometryAttributes(Material material, double opacity)
        {
            if (material == null)
            {
                String message = Logging.getMessage("nullValue.MaterialIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (opacity < 0.0 || opacity > 1.0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.visible = true;
            this.pickEnabled = true;
            this.material = material;
            this.opacity = opacity;
            this.size = 1.0;
            this.pickSize = 1.0;
            this.offset = Vec4.ZERO;
        }

        public GeometryAttributes()
        {
            this(Material.WHITE, 1.0);
        }

        public GeometryAttributes copy()
        {
            return this.copyTo(new GeometryAttributes());
        }

        public GeometryAttributes copyTo(GeometryAttributes copy)
        {
            copy.setVisible(this.isVisible());
            copy.setEnablePicking(this.isEnablePicking());
            copy.setMaterial(this.getMaterial());
            copy.setOpacity(this.getOpacity());
            copy.setSize(this.getSize());
            copy.setPickSize(this.getPicksize());
            copy.setOffset(this.getOffset());

            return copy;
        }

        public boolean isVisible()
        {
            return visible;
        }

        public void setVisible(boolean visible)
        {
            this.visible = visible;
        }

        public boolean isEnablePicking()
        {
            return this.pickEnabled;
        }

        public void setEnablePicking(boolean enable)
        {
            this.pickEnabled = enable;
        }

        public Material getMaterial()
        {
            return this.material;
        }

        public void setMaterial(Material material)
        {
            if (material == null)
            {
                String message = Logging.getMessage("nullValue.MaterialIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.material = material;
        }

        public double getOpacity()
        {
            return this.opacity;
        }

        public void setOpacity(double opacity)
        {
            if (opacity < 0.0 || opacity > 1.0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.opacity = opacity;
        }

        public double getSize()
        {
            return this.size;
        }

        public void setSize(double size)
        {
            if (size < 0.0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "size < 0");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.size = size;
        }

        public double getPicksize()
        {
            return this.pickSize;
        }

        public void setPickSize(double size)
        {
            if (size < 0.0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "size < 0");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.pickSize = size;
        }

        public Vec4 getOffset()
        {
            return this.offset;
        }

        public void setOffset(Vec4 vec4)
        {
            if (vec4 == null)
            {
                String message = Logging.getMessage("nullValue.Vec4IsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.offset = vec4;
        }
    }

    public static class LabelAttributes
    {
        private boolean visible;
        private java.awt.Color color;
        private java.awt.Font font;
        private String horizontalAlignment;
        private String verticalAlignment;
        private double minActiveDistance;
        private double maxActiveDistance;
        private Vec4 offset;

        public LabelAttributes(Color color, Font font, String horizontalAlignment, String verticalAlignment)
        {
            if (color == null)
            {
                String message = Logging.getMessage("nullValue.ColorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (font == null)
            {
                String message = Logging.getMessage("nullValue.FontIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (horizontalAlignment == null)
            {
                String message = Logging.getMessage("nullValue.HorizontalAlignmentIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (verticalAlignment == null)
            {
                String message = Logging.getMessage("nullValue.VerticalAlignmentIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.visible = true;
            this.color = color;
            this.font = font;
            this.horizontalAlignment = horizontalAlignment;
            this.verticalAlignment = verticalAlignment;
            this.minActiveDistance = 0;
            this.maxActiveDistance = Double.MAX_VALUE;
            this.offset = Vec4.ZERO;
        }

        public LabelAttributes()
        {
            this(Color.WHITE, Font.decode("Arial-12"), AVKey.LEFT, AVKey.BOTTOM);
        }

        public LabelAttributes copy()
        {
            return this.copyTo(new LabelAttributes());
        }

        protected LabelAttributes copyTo(LabelAttributes copy)
        {
            copy.setVisible(this.isVisible());
            copy.setColor(this.getColor());
            copy.setFont(this.getFont());
            copy.setHorizontalAlignment(this.getHorizontalAlignment());
            copy.setVerticalAlignment(this.getVerticalAlignment());
            copy.setMinActiveDistance(this.getMinActiveDistance());
            copy.setMaxActiveDistance(this.getMaxActiveDistance());
            copy.setOffset(this.getOffset());

            return copy;
        }

        public boolean isVisible()
        {
            return this.visible;
        }

        public void setVisible(boolean visible)
        {
            this.visible = visible;
        }

        public Color getColor()
        {
            return this.color;
        }

        public void setColor(Color color)
        {
            if (color == null)
            {
                String message = Logging.getMessage("nullValue.ColorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.color = color;
        }

        public Font getFont()
        {
            return this.font;
        }

        public void setFont(Font font)
        {
            if (font == null)
            {
                String message = Logging.getMessage("nullValue.FontIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.font = font;
        }

        public String getHorizontalAlignment()
        {
            return this.horizontalAlignment;
        }

        public void setHorizontalAlignment(String horizontalAlignment)
        {
            if (horizontalAlignment == null)
            {
                String message = Logging.getMessage("nullValue.HorizontalAlignmentIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.horizontalAlignment = horizontalAlignment;
        }

        public String getVerticalAlignment()
        {
            return this.verticalAlignment;
        }

        public void setVerticalAlignment(String verticalAlignment)
        {
            if (verticalAlignment == null)
            {
                String message = Logging.getMessage("nullValue.VerticalAlignmentIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.verticalAlignment = verticalAlignment;
        }

        public double getMinActiveDistance()
        {
            return this.minActiveDistance;
        }

        public void setMinActiveDistance(double distance)
        {
            this.minActiveDistance = distance;
        }

        public double getMaxActiveDistance()
        {
            return this.maxActiveDistance;
        }

        public void setMaxActiveDistance(double distance)
        {
            this.maxActiveDistance = distance;
        }

        public Vec4 getOffset()
        {
            return this.offset;
        }

        public void setOffset(Vec4 vec4)
        {
            if (vec4 == null)
            {
                String message = Logging.getMessage("nullValue.Vec4IsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.offset = vec4;
        }

        public String getText(SegmentPlane segmentPlane, Position position, AVList values)
        {
            if (segmentPlane == null)
            {
                String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (position == null)
            {
                String message = Logging.getMessage("nullValue.PositionIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Lat ").append(position.getLatitude().toString());
            sb.append("\n");
            sb.append("Lon ").append(position.getLongitude());
            sb.append("\n");
            sb.append("Alt ").append(position.getElevation()).append("m");

            return sb.toString();
        }
    }

    private Map<Object, GeometryAttributes> geometryAttributes;
    private Map<Object, LabelAttributes> labelAttributes;

    public SegmentPlaneAttributes()
    {
        this.geometryAttributes = new HashMap<Object, GeometryAttributes>();
        this.labelAttributes = new HashMap<Object, LabelAttributes>();
    }

    public SegmentPlaneAttributes copy()
    {
        SegmentPlaneAttributes copy = new SegmentPlaneAttributes();

        Map<Object, GeometryAttributes> geometryAttributesMap = new HashMap<Object, GeometryAttributes>();
        for (Map.Entry<Object, GeometryAttributes> entry : this.geometryAttributes.entrySet())
        {
            geometryAttributesMap.put(entry.getKey(), entry.getValue().copy());
        }
        copy.setAllGeometryAttributes(geometryAttributesMap);

        Map<Object, LabelAttributes> labelAttributesMap = new HashMap<Object, LabelAttributes>();
        for (Map.Entry<Object, LabelAttributes> entry : this.labelAttributes.entrySet())
        {
            labelAttributesMap.put(entry.getKey(), entry.getValue().copy());
        }
        copy.setAllLabelAttributes(labelAttributesMap);

        return copy;
    }

    public Map<Object, GeometryAttributes> getAllGeometryAttributes()
    {
        return Collections.unmodifiableMap(this.geometryAttributes);
    }

    public void setAllGeometryAttributes(Map<Object, ? extends GeometryAttributes> map)
    {
        if (map == null)
        {
            String message = Logging.getMessage("nullValue.MapIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.geometryAttributes.clear();
        this.geometryAttributes.putAll(map);
    }

    public Map<Object, LabelAttributes> getAllLabelAttributes()
    {
        return Collections.unmodifiableMap(this.labelAttributes);
    }

    public void setAllLabelAttributes(Map<Object, ? extends LabelAttributes> map)
    {
        if (map == null)
        {
            String message = Logging.getMessage("nullValue.MapIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.labelAttributes.clear();
        this.labelAttributes.putAll(map);
    }

    public GeometryAttributes getGeometryAttributes(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.geometryAttributes.get(key);
    }

    public void setGeometryAttributes(Object key, GeometryAttributes attributes)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.geometryAttributes.put(key, attributes);
    }

    public LabelAttributes getLabelAttributes(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.labelAttributes.get(key);
    }

    public void setLabelAttributes(Object key, LabelAttributes attributes)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.labelAttributes.put(key, attributes);
    }

    public static void applyGeometryAttributes(DrawContext dc, GeometryAttributes attributes, boolean enableMaterial)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!dc.isPickingMode())
        {
            applyMaterial(dc, attributes.getMaterial(), attributes.getOpacity(), enableMaterial);
        }
    }

    public static void applyGeometryAttributesAsLine(DrawContext dc, GeometryAttributes attributes)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        applyLineWidth(dc, attributes.getSize(), attributes.getPicksize());
    }

    protected static void applyMaterial(DrawContext dc, Material material, double opacity, boolean enableMaterial)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

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

    protected static void applyLineWidth(DrawContext dc, double lineWidth, double pickLineWidth)
    {
        GL gl = dc.getGL();

        if (dc.isPickingMode())
        {
            gl.glLineWidth((float) pickLineWidth);
        }
        else
        {
            gl.glLineWidth((float) lineWidth);
        }
    }
}
