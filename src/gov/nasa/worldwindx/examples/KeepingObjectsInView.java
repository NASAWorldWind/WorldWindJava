/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * KeepingObjectsInView demonstrates keeping a set of scene elements visible by using the utility class {@link
 * gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport}. To run this demonstration, execute this class' main
 * method, then follow the on-screen instructions.
 * <p>
 * The key functionality demonstrated by KeepingObjectsVisible is found in the internal classes {@link
 * KeepingObjectsInView.ViewController} and {@link gov.nasa.worldwindx.examples.KeepingObjectsInView.ViewAnimator}.
 *
 * @author dcollins
 * @version $Id: KeepingObjectsInView.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class KeepingObjectsInView extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        protected Iterable<?> objectsToTrack;
        protected ViewController viewController;
        protected RenderableLayer helpLayer;

        public AppFrame() {
            // Create an iterable of the objects we want to keep in view.
            this.objectsToTrack = createObjectsToTrack();
            // Set up a view controller to keep the objects in view.
            this.viewController = new ViewController(this.getWwd());
            this.viewController.setObjectsToTrack(this.objectsToTrack);
            // Set up a layer to render the objects we're tracking.
            this.addObjectsToWorldWindow(this.objectsToTrack);
            // Set up swing components to toggle the view controller's behavior.
            this.initSwingComponents();

            // Set up a one-shot timer to zoom to the objects once the app launches.
            Timer timer = new Timer(1000, (ActionEvent e) -> {
                enableHelpAnnotation();
                viewController.gotoScene();
            });
            timer.setRepeats(false);
            timer.start();
        }

        protected void enableHelpAnnotation() {
            if (this.helpLayer != null) {
                return;
            }

            this.helpLayer = new RenderableLayer();
            this.helpLayer.addRenderable(createHelpAnnotation(getWwd()));
            insertBeforePlacenames(this.getWwd(), this.helpLayer);
        }

        protected void disableHelpAnnotation() {
            if (this.helpLayer == null) {
                return;
            }

            this.getWwd().getModel().getLayers().remove(this.helpLayer);
            this.helpLayer.removeAllRenderables();
            this.helpLayer = null;
        }

        protected void addObjectsToWorldWindow(Iterable<?> objectsToTrack) {
            // Set up a layer to render the icons. Disable WWIcon view clipping, since view tracking works best when an
            // icon's screen rectangle is known even when the icon is outside the view frustum.
            IconLayer iconLayer = new IconLayer();
            iconLayer.setViewClippingEnabled(false);
            iconLayer.setName("Icons To Track");
            insertBeforePlacenames(this.getWwd(), iconLayer);

            // Set up a layer to render the markers.
            RenderableLayer shapesLayer = new RenderableLayer();
            shapesLayer.setName("Shapes to Track");
            insertBeforePlacenames(this.getWwd(), shapesLayer);

            // Add the objects to track to the layers.
            for (Object o : objectsToTrack) {
                if (o instanceof WWIcon) {
                    iconLayer.addIcon((WWIcon) o);
                } else if (o instanceof Renderable) {
                    shapesLayer.addRenderable((Renderable) o);
                }
            }

            // Set up a SelectListener to drag the spheres.
            this.getWwd().addSelectListener(new SelectListener() {
                protected BasicDragger dragger = new BasicDragger(getWwd());

                @Override
                public void selected(SelectEvent event) {
                    // Delegate dragging computations to a dragger.
                    this.dragger.selected(event);

                    if (event.getEventAction().equals(SelectEvent.DRAG)) {
                        disableHelpAnnotation();
                        viewController.sceneChanged();
                    }
                }
            });
        }

        protected void initSwingComponents() {
            // Create a checkbox to enable/disable the view controller.
            JCheckBox checkBox = new JCheckBox("Enable view tracking", true);
            checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkBox.addActionListener((ActionEvent event) -> {
                boolean selected = ((AbstractButton) event.getSource()).isSelected();
                viewController.setEnabled(selected);
            });
            JButton button = new JButton("Go to objects");
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener((ActionEvent event) -> {
                viewController.gotoScene();
            });
            Box box = Box.createVerticalBox();
            box.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // top, left, bottom, right
            box.add(checkBox);
            box.add(Box.createVerticalStrut(5));
            box.add(button);

            this.getControlPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static Iterable<?> createObjectsToTrack() {
        ArrayList<Object> objects = new ArrayList<>();
        Sector sector = Sector.fromDegrees(35, 45, -110, -100);

        for (int i = 0; i < 3; i++) {
            LatLon randLocation1, randLocation2;

            // Add a UserFacingIcon.
            randLocation1 = randomLocation(sector);
            WWIcon icon = new UserFacingIcon("gov/nasa/worldwindx/examples/images/antenna.png",
                    new Position(randLocation1, 0));
            icon.setSize(new Dimension(64, 64));
            icon.setValue(AVKey.FEEDBACK_ENABLED, Boolean.TRUE);
            objects.add(icon);

            // Add a SphereAirspace.
            randLocation1 = randomLocation(sector);
            Airspace airspace = new SphereAirspace(randLocation1, 50000d);
            airspace.setAltitude(0d);
            airspace.setTerrainConforming(true);
            airspace.setAttributes(new BasicAirspaceAttributes(Material.GREEN, 1d));
            objects.add(airspace);

            // Add a Path.
            randLocation1 = randomLocation(sector);
            randLocation2 = randomLocation(sector);
            Path path = new Path(Arrays.asList(randLocation1, randLocation2), 0d);
            path.setSurfacePath(true);
            var attrs = new BasicShapeAttributes();
            attrs.setOutlineWidth(3);
            attrs.setOutlineMaterial(new Material(Color.RED));
            path.setAttributes(attrs);
            objects.add(path);

            // Add a SurfaceCircle.
            randLocation1 = randomLocation(sector);
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.BLUE);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.BLUE)));
            attrs.setInteriorOpacity(0.5);
            SurfaceCircle circle = new SurfaceCircle(attrs, randLocation1, 50000d);
            objects.add(circle);
        }

        return objects;
    }

    protected static LatLon randomLocation(Sector sector) {
        return new LatLon(
                Angle.mix(Math.random(), sector.getMinLatitude(), sector.getMaxLatitude()),
                Angle.mix(Math.random(), sector.getMinLongitude(), sector.getMaxLongitude()));
    }

    public static Annotation createHelpAnnotation(WorldWindow wwd) {
        String text = "The view tracks the antenna icons,"
                + " the <font color=\"#DD0000\">red</font> lines,"
                + " the <font color=\"#00DD00\">green</font> spheres,"
                + " and the <font color=\"#0000DD\">blue</font> circles."
                + " Drag any object out of the window to track it.";
        Rectangle viewport = ((Component) wwd).getBounds();
        Point screenPoint = new Point(viewport.width / 2, viewport.height / 3);

        AnnotationAttributes attr = new AnnotationAttributes();
        attr.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attr.setFont(Font.decode("Arial-Bold-16"));
        attr.setTextAlign(AVKey.CENTER);
        attr.setTextColor(Color.WHITE);
        attr.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        attr.setBackgroundColor(new Color(0, 0, 0, 127)); // 50% transparent black
        attr.setBorderColor(Color.LIGHT_GRAY);
        attr.setLeader(AVKey.SHAPE_NONE);
        attr.setCornerRadius(0);
        attr.setSize(new Dimension(350, 0));

        return new ScreenAnnotation(text, screenPoint, attr);
    }

    //**************************************************************//
    //********************  View Controller  ***********************//
    //**************************************************************//
    public static class ViewController {

        protected static final double SMOOTHING_FACTOR = 0.96;

        protected boolean enabled = true;
        protected WorldWindow wwd;
        protected ViewAnimator animator;
        protected Iterable<?> objectsToTrack;

        public ViewController(WorldWindow wwd) {
            this.wwd = wwd;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;

            if (this.animator != null) {
                this.animator.stop();
                this.animator = null;
            }
        }

        public Iterable<?> getObjectsToTrack() {
            return this.objectsToTrack;
        }

        public void setObjectsToTrack(Iterable<?> iterable) {
            this.objectsToTrack = iterable;
        }

        public boolean isSceneContained(View view) {
            ExtentVisibilitySupport vs = new ExtentVisibilitySupport();
            this.addExtents(vs);

            return vs.areExtentsContained(view);
        }

        public Vec4[] computeViewLookAtForScene(View view) {
            Globe globe = this.wwd.getModel().getGlobe();
            double ve = this.wwd.getSceneController().getVerticalExaggeration();

            ExtentVisibilitySupport vs = new ExtentVisibilitySupport();
            this.addExtents(vs);

            return vs.computeViewLookAtContainingExtents(globe, ve, view);
        }

        public Position computePositionFromPoint(Vec4 point) {
            return this.wwd.getModel().getGlobe().computePositionFromPoint(point);
        }

        public void gotoScene() {
            Vec4[] lookAtPoints = this.computeViewLookAtForScene(this.wwd.getView());
            if (lookAtPoints == null || lookAtPoints.length != 3) {
                return;
            }

            Position centerPos = this.wwd.getModel().getGlobe().computePositionFromPoint(lookAtPoints[1]);
            double zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);

            this.wwd.getView().stopAnimations();
            this.wwd.getView().goTo(centerPos, zoom);
        }

        public void sceneChanged() {
            OrbitView view = (OrbitView) this.wwd.getView();

            if (!this.isEnabled()) {
                return;
            }

            if (this.isSceneContained(view)) {
                return;
            }

            if (this.animator == null || !this.animator.hasNext()) {
                this.animator = new ViewAnimator(SMOOTHING_FACTOR, view, this);
                this.animator.start();
                view.stopAnimations();
                view.addAnimator(this.animator);
                view.firePropertyChange(AVKey.VIEW, null, view);
            }
        }

        protected void addExtents(ExtentVisibilitySupport vs) {
            // Compute screen extents for WWIcons which have feedback information from their IconRenderer.
            Iterable<?> iterable = this.getObjectsToTrack();
            if (iterable == null) {
                return;
            }

            ArrayList<ExtentHolder> extentHolders = new ArrayList<>();
            ArrayList<ExtentVisibilitySupport.ScreenExtent> screenExtents
                    = new ArrayList<>();

            for (Object o : iterable) {
                if (o == null) {
                    continue;
                }

                if (o instanceof ExtentHolder) {
                    extentHolders.add((ExtentHolder) o);
                } else if (o instanceof AVList) {
                    AVList avl = (AVList) o;

                    Object b = avl.getValue(AVKey.FEEDBACK_ENABLED);
                    if (b == null || !Boolean.TRUE.equals(b)) {
                        continue;
                    }

                    if (avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null) {
                        screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
                                (Vec4) avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
                                (Rectangle) avl.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
                    }
                }
            }

            if (!extentHolders.isEmpty()) {
                Globe globe = this.wwd.getModel().getGlobe();
                double ve = this.wwd.getSceneController().getVerticalExaggeration();
                vs.setExtents(ExtentVisibilitySupport.extentsFromExtentHolders(extentHolders, globe, ve));
            }

            if (!screenExtents.isEmpty()) {
                vs.setScreenExtents(screenExtents);
            }
        }
    }

    //**************************************************************//
    //********************  View Animator  *************************//
    //**************************************************************//
    public static class ViewAnimator extends BasicAnimator {

        protected static final double LOCATION_EPSILON = 1.0e-9;
        protected static final double ALTITUDE_EPSILON = 0.1;

        protected OrbitView view;
        protected ViewController viewController;
        protected boolean haveTargets;
        protected Position centerPosition;
        protected double zoom;

        public ViewAnimator(final double smoothing, OrbitView view, ViewController viewController) {
            super(() -> 1d - smoothing);

            this.view = view;
            this.viewController = viewController;
        }

        @Override
        public void stop() {
            super.stop();
            this.haveTargets = false;
        }

        @Override
        protected void setImpl(double interpolant) {
            this.updateTargetValues();

            if (!this.haveTargets) {
                this.stop();
                return;
            }

            if (this.valuesMeetCriteria(this.centerPosition, this.zoom)) {
                this.view.setCenterPosition(this.centerPosition);
                this.view.setZoom(this.zoom);
                this.stop();
            } else {
                Position newCenterPos = Position.interpolateGreatCircle(interpolant, this.view.getCenterPosition(),
                        this.centerPosition);
                double newZoom = WWMath.mix(interpolant, this.view.getZoom(), this.zoom);
                this.view.setCenterPosition(newCenterPos);
                this.view.setZoom(newZoom);
            }

            this.view.firePropertyChange(AVKey.VIEW, null, this);
        }

        protected void updateTargetValues() {
            if (this.viewController.isSceneContained(this.view)) {
                return;
            }

            Vec4[] lookAtPoints = this.viewController.computeViewLookAtForScene(this.view);
            if (lookAtPoints == null || lookAtPoints.length != 3) {
                return;
            }

            this.centerPosition = this.viewController.computePositionFromPoint(lookAtPoints[1]);
            this.zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);
            if (this.zoom < view.getZoom()) {
                this.zoom = view.getZoom();
            }

            this.haveTargets = true;
        }

        protected boolean valuesMeetCriteria(Position centerPos, double zoom) {
            Angle cd = LatLon.greatCircleDistance(this.view.getCenterPosition(), centerPos);
            double ed = Math.abs(this.view.getCenterPosition().getElevation() - centerPos.getElevation());
            double zd = Math.abs(this.view.getZoom() - zoom);

            return cd.degrees < LOCATION_EPSILON
                    && ed < ALTITUDE_EPSILON
                    && zd < ALTITUDE_EPSILON;
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("Keeping Objects In View", AppFrame.class);
    }
}
