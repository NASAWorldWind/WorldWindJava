/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.ArrayList;

/**
 * Renders a plane model at a position with a given heading. The plane is parallel to the ground. An optional 'shadow'
 * shape is rendered on the ground.
 *
 * @author Patrick Murris
 * @version $Id: PlaneModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaneModel implements Renderable {

    private Position position;
    private Angle heading;

    private Double length = 100d;
    private Double width = 100d;
    private Color color = Color.YELLOW;
    private boolean showShadow = true;
    private double shadowScale = 1d;
    private Color shadowColor = Color.YELLOW;

    private Path planeModel;
    private Path shadowModel;

    /**
     * Renders a plane model with the defaul dimensions and color.
     */
    public PlaneModel() {
    }

    /**
     * Renders a plane model with the specified dimensions and color.
     *
     * @param length the plane length in meters
     * @param width the plane width in meter.
     * @param color the plane color.
     */
    public PlaneModel(Double length, Double width, Color color) {
        this.length = length;
        this.width = width;
        this.color = color;
    }

    public void setPosition(Position pos) {
        if (pos == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.position = pos;
        clearRenderables();
    }

    public Position getPosition() {
        return this.position;
    }

    public void setHeading(Angle head) {
        if (head == null) {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.heading = head;
        clearRenderables();
    }

    public Angle getHeading() {
        return this.heading;
    }

    public void setShowShadow(boolean state) {
        this.showShadow = state;
    }

    public boolean getShowShadow() {
        return this.showShadow;
    }

    public double getShadowScale() {
        return this.shadowScale;
    }

    public void setShadowScale(double shadowScale) {
        this.shadowScale = shadowScale;
        clearRenderables();
    }

    public Color getShadowColor() {
        return this.shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        clearRenderables();
    }

    public void render(DrawContext dc) {
        if (dc == null) {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (this.position == null || this.heading == null) {
            return;
        }

        //renderPlane(dc);
        if (this.planeModel == null) {
            createRenderables(dc);
        }

        this.planeModel.render(dc);
        if (this.showShadow && this.shadowModel != null) {
            this.shadowModel.render(dc);
        }
    }

    private void createRenderables(DrawContext dc) {
        ArrayList<LatLon> positions = computePlaneShape(dc, this.width, this.length);
        this.planeModel = new Path(positions, this.position.getElevation());
        this.planeModel.setPathType(AVKey.LINEAR);
        this.planeModel.setFollowTerrain(false);
        this.planeModel.setNumSubsegments(1);
        var attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(this.color));
        this.planeModel.setAttributes(attrs);

        positions = computePlaneShape(dc, this.shadowScale * this.width, this.shadowScale * this.length);
        this.shadowModel = new Path(positions, this.position.getElevation());
        this.shadowModel.setPathType(AVKey.LINEAR);
        this.shadowModel.setSurfacePath(true);
        attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(this.shadowColor));
        this.shadowModel.setAttributes(attrs);
    }

    private void clearRenderables() {
        this.planeModel = null;
        this.shadowModel = null;
    }

    private ArrayList<LatLon> computePlaneShape(DrawContext dc, double width, double length) {
        ArrayList<LatLon> positions = new ArrayList<>();
        LatLon center = this.position;
        double hl = length / 2;
        double hw = width / 2;
        double radius = dc.getGlobe().getRadius();

        // triangle head point
        LatLon p = LatLon.rhumbEndPosition(center, this.heading.radians, hl / radius);
        positions.add(p);
        // triangle base points
        double d = Math.sqrt(hw * hw + hl * hl);
        double a = Math.PI / 2 + Math.asin(hl / d);
        p = LatLon.rhumbEndPosition(center, this.heading.radians + a, d / radius);
        positions.add(p);
        p = LatLon.rhumbEndPosition(center, this.heading.radians - a, d / radius);
        positions.add(p);
        // close shape
        positions.add(positions.get(0));

        return positions;
    }
    /*
    private void renderPlane(DrawContext dc)
    {
        GL gl = dc.getGL();
        // Save gl state
        gl.glPushAttrib(GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();

        gl.glDisable(com.jogamp.opengl.GL.GL_TEXTURE_2D);
        gl.glColor3d(this.color.getRed() / 255d, this.color.getGreen() / 255d, this.color.getBlue() / 255d);

        // Placement and orientation
        Vec4 pos = dc.getGlobe().computePointFromPosition(this.position);
        gl.glTranslated(pos.x, pos.y, pos.z);
        gl.glRotated(this.position.getLongitude().degrees, 0, 1, 0);
        gl.glRotated(-this.position.getLatitude().degrees, 1, 0, 0);
        gl.glRotated(-this.heading.degrees, 0, 0, 1);

        // Draw
        Double hw = this.width / 2;
        Double hl = this.length / 2;
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(-hw, -hl, 0);
        gl.glVertex3d(0, hl, 0);
        gl.glVertex3d(hw, -hl, 0);
        gl.glVertex3d(-hw, -hl, 0);
        gl.glEnd();

        // Restore gl state
        gl.glPopMatrix();
        gl.glPopAttrib();

    }
     */
}
