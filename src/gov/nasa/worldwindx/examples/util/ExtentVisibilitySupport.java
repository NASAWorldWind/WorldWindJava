/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewUtil;

import java.awt.*;
import java.util.ArrayList;

/**
 * ExtentVisibilitySupport provides visibility tests and computations on objects with 3D extents in model coordinates,
 * and on objects with 2D extents in screen coordinates. The caller configures ExtentVisibilitySupport with an Iterable
 * of {@link gov.nasa.worldwind.geom.Extent} instances and {@link gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport.ScreenExtent}
 * instances by invoking {@link #setExtents(Iterable)} and {@link #setScreenExtents(Iterable)}, respectively. These
 * Iterables defines ExtentVisibilitySupport's scene elements. ExtentVisibilitySupport keeps a direct reference to these
 * Iterables; it does not attempt to copy or modify them in any way. Any null elements in these Iterables are ignored.
 * The static convenience method {@link #extentsFromExtentHolders(Iterable, gov.nasa.worldwind.globes.Globe, double)}
 * makes it easy to for callers to convert an Iterable of Extents references to an Iterable of {@link
 * gov.nasa.worldwind.geom.ExtentHolder} references.
 * <p/>
 * The method {@link #areExtentsContained(gov.nasa.worldwind.View)} provides a mechanism for callers to test whether or
 * not the currently configured scene is entirely contained within a certain {@link gov.nasa.worldwind.View}. This
 * method tests containment on both the model coordinate extents and screen coordinate extents.
 * <p/>
 * The method {@link #computeViewLookAtContainingExtents(gov.nasa.worldwind.globes.Globe, double,
 * gov.nasa.worldwind.View)} returns a viewing coordinate system which contains the currently configured scene, while
 * preserving the current view's orientation to the globe (heading and pitch). This has the effect of computing the pan
 * and zoom parameters necessary for a View to contain the current scene. Depending on the current scene, the computed
 * view may represent a best-estimate, and not the final parameters necessary to contain the scene. If the scene
 * contains model coordinate extents which depend on the view, or screen coordinate extents, the caller should invoke
 * this method iteratively (after applying view changes from the previous call) until the returned view coordinate
 * system converges on values which contain the scene.
 *
 * @author dcollins
 * @version $Id: ExtentVisibilitySupport.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.geom.Extent
 * @see gov.nasa.worldwind.geom.ExtentHolder
 * @see gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport.ScreenExtent
 */
public class ExtentVisibilitySupport
{
    /**
     * ScreenExtent represents a screen object's enclosing bounding box in screen coordinates, and that object's
     * reference point in model coordinates. ExtentVisibilitySupport assumes that the relationship between the model
     * reference point and the screen bounds are predicable: projecting the reference point into screen coordinates
     * should yield a screen point with a predictable location relative to the screen bounds.
     */
    public static class ScreenExtent
    {
        protected Vec4 modelReferencePoint;
        protected Rectangle screenBounds;

        /**
         * Constructs a new ScreenExtent with the specified model coordinate reference point and screen coordinate
         * bounding box. Either value may be null.
         *
         * @param modelReferencePoint the model coordinate reference point. A null reference is accepted.
         * @param screenBounds        the screen coordinate bounding rectangle. A null reference is accepted.
         */
        public ScreenExtent(Vec4 modelReferencePoint, Rectangle screenBounds)
        {
            this.modelReferencePoint = modelReferencePoint;
            this.screenBounds = (screenBounds != null) ? new Rectangle(screenBounds) : null;
        }

        /**
         * Returns the model coordinate reference point. This may return null, indicating this ScreenExtent has no model
         * coordinate reference point.
         *
         * @return the reference point in model coordinates. May be null.
         */
        public Vec4 getModelReferencePoint()
        {
            return this.modelReferencePoint;
        }

        /**
         * Returns the screen coordinate bounding rectangle. This may return null, indicating the ScreenExtent has no
         * screen coordinate bounds.
         *
         * @return the bounding rectangle in screen coordinates. May be null.
         */
        public Rectangle getScreenBounds()
        {
            return (this.screenBounds != null) ? new Rectangle(this.screenBounds) : null;
        }
    }

    protected static final double EPSILON = 1.0e-6;
    protected static final double SCREEN_POINT_PADDING_PIXELS = 4;

    protected Iterable<? extends Extent> extentIterable;
    protected Iterable<? extends ScreenExtent> screenExtentIterable;

    /** Constructs a new ExtentVisibilitySupport, but otherwise does nothing. */
    public ExtentVisibilitySupport()
    {
    }

    /**
     * Converts the specified Iterable of {@link gov.nasa.worldwind.geom.ExtentHolder} references to a new Iterable of
     * {@link gov.nasa.worldwind.geom.Extent} references. The new Extents are constructed from the specified
     * ExtentHolders by invoking {@link gov.nasa.worldwind.geom.ExtentHolder#getExtent(gov.nasa.worldwind.globes.Globe,
     * double)} with the specified Globe and vertical exaggeration. This ignores any null ExtentHolders in the specified
     * Iterable, and any null Extents returned by the ExtentHolders. This returns null if the Iterable is null or
     * empty.
     *
     * @param extentHolders        Iterable of ExtentHolders used to construct the new Extents.
     * @param globe                the Globe used to construct the Extents.
     * @param verticalExaggeration the vertical exaggeration used to construct the new Extents.
     *
     * @return a new Iterable of Extents constructed from the specified ExtentHolder, Globe, and vertical exaggeration.
     */
    public static Iterable<Extent> extentsFromExtentHolders(Iterable<? extends ExtentHolder> extentHolders,
        Globe globe, double verticalExaggeration)
    {
        if (extentHolders == null)
            return null;

        ArrayList<Extent> list = new ArrayList<Extent>();

        for (ExtentHolder eh : extentHolders)
        {
            if (eh == null)
                continue;

            Extent e = eh.getExtent(globe, verticalExaggeration);
            if (e == null)
                continue;

            list.add(e);
        }

        if (list.isEmpty())
            return null;

        return list;
    }

    /**
     * Returns this ExtentVisibilitySupport's Iterable of Extents.
     *
     * @return the Iterable of Extents this ExtentVisibilitySupport operates on, or null if none exists.
     */
    public Iterable<? extends Extent> getExtents()
    {
        return this.extentIterable;
    }

    /**
     * Sets this ExtentVisibilitySupport's Iterable of Extents.
     *
     * @param extents the Iterable of Extents defining this ExtentVisibilitySupport's model coordinate scene elements.
     *                This iterable can be null, indicating that this ExtentVisibilitySupport has no model coordinate
     *                scene.
     */
    public void setExtents(Iterable<? extends Extent> extents)
    {
        this.extentIterable = extents;
    }

    /**
     * Returns this ExtentVisibilitySupport's Iterable of ScreenExtents.
     *
     * @return the Iterable of ScreenExtents this ExtentVisibilitySupport operates on, or null if none exists.
     */
    public Iterable<? extends ScreenExtent> getScreenExtents()
    {
        return this.screenExtentIterable;
    }

    /**
     * Sets this ExtentVisibilitySupport's Iterable of ScreenExtents.
     *
     * @param screenExtents the Iterable of ScreenExtents defining this ExtentVisibilitySupport's screen coordinate
     *                      scene elements. This iterable can be null, indicating that this ExtentVisibilitySupport has
     *                      no screen coordinate scene.
     */
    public void setScreenExtents(Iterable<? extends ScreenExtent> screenExtents)
    {
        this.screenExtentIterable = screenExtents;
    }

    /**
     * Returns true if the model coordinates scene elements are completely inside the space enclosed by the specified
     * {@link gov.nasa.worldwind.geom.Frustum} in model coordinates, and the screen coordinate scene elements are
     * completely inside the viewport rectangle. Otherwise, this returns false. If this ExtentVisibilitySupport has no
     * scene elements, this returns true.
     *
     * @param frustum  the Frustum with which to test for containment, in model coordinates.
     * @param viewport the viewport rectangle with which to test for containment, in screen coordinates.
     *
     * @return true if the scene elements are completely inside the Frustum and viewport rectangle, or if this has no
     *         scene elements. false if any scene element is partially or completely outside the Frustum or viewport
     *         rectangle.
     *
     * @throws IllegalArgumentException if either the the Frustum or the viewport are null.
     */
    public boolean areExtentsContained(Frustum frustum, Rectangle viewport)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport == null)
        {
            String message = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport.getWidth() <= 0d)
        {
            String message = Logging.getMessage("Geom.ViewportWidthInvalid", viewport.getWidth());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport.getHeight() <= 0d)
        {
            String message = Logging.getMessage("Geom.ViewportHeightInvalid", viewport.getHeight());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterable<? extends Extent> extents = this.getExtents();
        if (extents != null)
        {
            for (Extent e : extents)
            {
                if (e == null)
                    continue;

                if (!frustum.contains(e))
                    return false;
            }
        }

        Iterable<? extends ScreenExtent> screenExtents = this.getScreenExtents();
        if (screenExtents != null)
        {
            for (ScreenExtent se : screenExtents)
            {
                if (se == null)
                    continue;

                if (se.getScreenBounds() != null && !viewport.contains(se.getScreenBounds()))
                    return false;

                if (se.getModelReferencePoint() != null && !frustum.contains(se.getModelReferencePoint()))
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the model coordinates scene elements are completely inside the space enclosed by the specified
     * {@link gov.nasa.worldwind.View} in model coordinates, and the screen coordinate scene elements are completely
     * inside the View's viewport rectangle. Otherwise, this returns false. If this ExtentVisibilitySupport has no scene
     * elements, this returns true.
     *
     * @param view The View with which to test for containment.
     *
     * @return true if the scene elements are completely inside the View and it's viewport rectangle, or if this has no
     *         scene elements. false if any scene element is partially or completely outside the View or it's viewport
     *         rectangle.
     *
     * @throws IllegalArgumentException if either the the Frustum or the viewport are null.
     */
    public boolean areExtentsContained(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.areExtentsContained(view.getFrustumInModelCoordinates(), view.getViewport());
    }

    /**
     * Returns an array of View look-at vectors optimal for viewing the the scene elements, or null if this has no scene
     * elements. The returned look-at vectors define a new viewing coordinate system as follows: The vector at index 0
     * defines the eye point, the vector at index 1 defines the reference center point, and the value at index 2 defines
     * the up vector.
     * <p/>
     * The specified eye point, reference center point, and up vector define the input view coordinate system, and the
     * returned view coordinates preserve the input view coordinates' orientation relative to the specified Globe. The
     * field-of-view, viewport and clip distances define the input view projection parameters, and are used to compute
     * the optimal look-at vectors to view the scene elements.
     *
     * @param globe                the Globe the scene elements are related to.
     * @param verticalExaggeration the vertical exaggeration of the scene.
     * @param eyePoint             the current eye point, in model coordinates.
     * @param centerPoint          the scene's current reference center point, in model coordinates.
     * @param upVector             the current direction of the up vector, in model coordinates.
     * @param fieldOfView          the horizontal field of view.
     * @param viewport             the viewport bounds, in window coordinates (screen pixels).
     * @param nearClipDistance     the near clipping plane distance, in model coordinates.
     * @param farClipDistance      the far clipping plane distance, in model coordinates.
     *
     * @return an array of View look-at vectors  optimal for viewing the scene elements, or null if this has no scene
     *         elements.
     *
     * @throws IllegalArgumentException if any of the globe, eyePoint, centerPoint, upVector, field-of-view, or viewport
     *                                  are null, if if the eye point and reference center point are coincident, if the
     *                                  up vector and the line of sight are parallel, or if any of the view projection
     *                                  parameters are out-of-range.
     */
    public Vec4[] computeViewLookAtContainingExtents(Globe globe, double verticalExaggeration,
        Vec4 eyePoint, Vec4 centerPoint, Vec4 upVector, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (eyePoint == null)
        {
            String message = Logging.getMessage("nullValue.EyeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (centerPoint == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (upVector == null)
        {
            String message = Logging.getMessage("nullValue.UpIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (fieldOfView == null)
        {
            String message = Logging.getMessage("nullValue.FOVIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport == null)
        {
            String message = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String message = this.validate(eyePoint, centerPoint, upVector, fieldOfView, viewport,
            nearClipDistance, farClipDistance);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Gather the model coordinate and screen coordinate extents associated with this ExtentVisibilitySupport.
        Iterable<? extends Extent> modelExtents = this.getExtents();
        Iterable<? extends ScreenExtent> screenExtents = this.getScreenExtents();

        // Compute a new view center point optimal for viewing the extents on the specified Globe.
        Vec4 newCenterPoint = this.computeCenterPoint(globe, verticalExaggeration, modelExtents, screenExtents);
        if (newCenterPoint == null)
            newCenterPoint = centerPoint;

        // Compute a local model coordinate origin transforms at the current center position and at the new center
        // position. We transform the view's model coordinates from the current local origin to the new local origin in
        // order to preserve the view's orientation relative to the Globe.
        Position centerPos = globe.computePositionFromPoint(centerPoint);
        Position newCenterPos = globe.computePositionFromPoint(newCenterPoint);
        Matrix localCoords = globe.computeSurfaceOrientationAtPosition(centerPos);
        Matrix newLocalCoords = globe.computeSurfaceOrientationAtPosition(newCenterPos);

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eyePoint, centerPoint, upVector);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        // Compute the eye point and up vector in model coordinates at the new center position on the Globe, while
        // preserving the view's orientation relative to the Globe. We accomplish this by transforming the identity
        // eye point and up vector by the matrix which maps identity model coordinates to the model coordinates at the
        // new center position on the Globe.
        Matrix m = Matrix.IDENTITY;
        m = m.multiply(newLocalCoords);
        m = m.multiply(localCoords.getInverse());
        m = m.multiply(modelview.getInverse());
        Vec4 newEyePoint = Vec4.UNIT_W.transformBy4(m);
        Vec4 newUpVector = Vec4.UNIT_Y.transformBy4(m);

        // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
        // change in modelview parameters.
        Matrix newModelview = Matrix.fromViewLookAt(newEyePoint, newCenterPoint, newUpVector);
        if (screenExtents != null)
            screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection, viewport);

        // Compute the optimal eye point for viewing the extents on the specified Globe.
        Vec4 p = this.computeEyePoint(newEyePoint, newCenterPoint, newUpVector, fieldOfView, viewport, nearClipDistance,
            farClipDistance, modelExtents, screenExtents);
        if (p != null)
            newEyePoint = p;

        return new Vec4[] {newEyePoint, newCenterPoint, newUpVector};
    }

    /**
     * Returns an array of View look-at vectors optimal for viewing the the scene elements, or null if this has no scene
     * elements. The returned look-at vectors define a new viewing coordinate system as follows: The vector at index 0
     * defines the eye point, the vector at index 1 defines the reference center point, and the value at index 2 defines
     * the up vector.
     * <p/>
     * The specified View defines both the input view coordinate system, and the input view projection parameters. The
     * returned view coordinates preserve the input view's orientation relative to the specified Globe, and are optimal
     * for the View's projection parameters.
     *
     * @param globe                the Globe the scene elements are related to.
     * @param verticalExaggeration the vertical exaggeration of the scene.
     * @param view                 the View defining the import view coordinate system and view projection parameters.
     *
     * @return an array of View look-at vectors  optimal for viewing the scene elements, or null if this has no scene
     *         elements.
     *
     * @throws IllegalArgumentException if either the globe or view are null.
     */
    public Vec4[] computeViewLookAtContainingExtents(Globe globe, double verticalExaggeration, View view)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 eye = view.getEyePoint();
        Vec4 center = view.getCenterPoint();
        Vec4 up = view.getUpVector();

        if (center == null)
            center = eye.add3(view.getForwardVector());

        return this.computeViewLookAtContainingExtents(globe, verticalExaggeration, eye, center, up,
            view.getFieldOfView(), view.getViewport(), view.getNearClipDistance(), view.getFarClipDistance());
    }

    protected String validate(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance)
    {
        Vec4 f = center.subtract3(eye).normalize3();
        Vec4 u = up.normalize3();

        if (eye.distanceTo3(center) <= EPSILON)
            return Logging.getMessage("Geom.EyeAndCenterInvalid", eye, center);

        if (f.dot3(u) >= 1d - EPSILON)
            return Logging.getMessage("Geom.UpAndLineOfSightInvalid", up, f);

        if (fieldOfView.compareTo(Angle.ZERO) < 0 || fieldOfView.compareTo(Angle.POS180) > 0)
            return Logging.getMessage("Geom.ViewFrustum.FieldOfViewOutOfRange");

        if (viewport.getWidth() <= 0d)
            return Logging.getMessage("Geom.ViewportWidthInvalid", viewport.getWidth());

        if (viewport.getHeight() <= 0d)
            return Logging.getMessage("Geom.ViewportHeightInvalid", viewport.getHeight());

        if (nearClipDistance < 0d || farClipDistance < 0d || nearClipDistance > farClipDistance)
            return Logging.getMessage("Geom.ViewFrustum.ClippingDistanceOutOfRange");

        return null;
    }

    //**************************************************************//
    //********************  Center Point Computation  **************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected Vec4 computeCenterPoint(Globe globe, double verticalExaggeration,
        Iterable<? extends Extent> modelExtents, Iterable<? extends ScreenExtent> screenExtents)
    {
        ArrayList<Vec4> list = new ArrayList<Vec4>();

        if (modelExtents != null)
        {
            for (Extent e : modelExtents)
            {
                if (e == null || e.getCenter() == null)
                    continue;

                list.add(e.getCenter());
            }
        }

        if (screenExtents != null)
        {
            for (ScreenExtent se : screenExtents)
            {
                if (se == null || se.getModelReferencePoint() == null)
                    continue;

                list.add(se.getModelReferencePoint());
            }
        }

        if (list.isEmpty())
            return null;

        return Vec4.computeAveragePoint(list);
    }

    //**************************************************************//
    //********************  Eye Point Computation  *****************//
    //**************************************************************//

    protected Vec4 computeEyePoint(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance,
        Iterable<? extends Extent> modelExtents, Iterable<? extends ScreenExtent> screenExtents)
    {
        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        Vec4 newEye = null;

        // Compute the eye point which contains the specified model coordinate extents. We compute the model coordinate
        // eye point first to provide a baseline eye point which the screen extent computation can be compared against.
        Vec4 p = this.computeEyePointForModelExtents(eye, center, up, fieldOfView, viewport, modelExtents);
        if (p != null)
        {
            newEye = p;

            // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
            // change in modelview parameters.
            Matrix newModelview = Matrix.fromViewLookAt(newEye, center, up);
            if (screenExtents != null)
            {
                screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection,
                    viewport);
            }
        }

        // Compute the eye point which contains the specified screen extents. If the model extent eye point is null, or
        // if it's closer to the center position than the screen extent eye point.
        p = this.computeEyePointForScreenExtents((newEye != null) ? newEye : eye, center, up, fieldOfView, viewport,
            nearClipDistance, farClipDistance, screenExtents);
        if (p != null && (newEye == null || newEye.distanceTo3(center) < p.distanceTo3(center)))
        {
            newEye = p;
        }

        return newEye;
    }

    //**************************************************************//
    //********************  Model Extent Support  ******************//
    //**************************************************************//

    protected Vec4 computeEyePointForModelExtents(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, Iterable<? extends Extent> modelExtents)
    {
        if (modelExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        Angle verticalFieldOfView = ViewUtil.computeVerticalFieldOfView(fieldOfView, viewport);
        double hcos = fieldOfView.cosHalfAngle();
        double htan = fieldOfView.tanHalfAngle();
        double vcos = verticalFieldOfView.cosHalfAngle();
        double vtan = verticalFieldOfView.tanHalfAngle();

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // Compute the smallest distance from the center point needed to contain the model coordinate extents in the
        // viewport.
        for (Extent e : modelExtents)
        {
            if (e == null || e.getCenter() == null)
                continue;

            Vec4 p = e.getCenter().transformBy4(modelview);

            d = (p.z - c.z) + (Math.abs(p.x) + (e.getRadius() / hcos)) / htan;
            if (maxDistance < d)
                maxDistance = d;

            d = (p.z - c.z) + (Math.abs(p.y) + (e.getRadius() / vcos)) / vtan;
            if (maxDistance < d)
                maxDistance = d;
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    //**************************************************************//
    //********************  Screen Extent Support  *****************//
    //**************************************************************//

    protected Vec4 computeEyePointForScreenExtents(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, double nearClipDistance, double farClipDistance,
        Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        Vec4 newEye;

        // Compute the eye point which contains the specified model coordinate reference points before computing the eye
        // point which contains the screen bounds. The screen bound computation only resolves intersections between
        // the screen bound and the viewport, it does not attempt to find the nearest eye point containing the bounds.
        // By first computing the nearest eye point containing the model coordinate reference points, we provide a
        // minimum distance eye point to the next computation. The final result is the nearest eye point which contains
        // the screen bounds.
        newEye = this.computeEyePointForScreenReferencePoints(eye, center, up, fieldOfView, viewport, screenExtents);
        if (newEye == null)
            return null;

        // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
        // change in modelview parameters.
        Matrix newModelview = Matrix.fromViewLookAt(newEye, center, up);
        screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection, viewport);

        // Compute the eye point which contains the specified screen coordinate bounding rectangles.
        Vec4 p = this.computeEyePointForScreenBounds(newEye, center, up, fieldOfView, viewport, nearClipDistance,
            farClipDistance, screenExtents);
        if (p != null)
            newEye = p;

        return newEye;
    }

    protected Vec4 computeEyePointForScreenReferencePoints(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        Angle verticalFieldOfView = ViewUtil.computeVerticalFieldOfView(fieldOfView, viewport);
        double htan = fieldOfView.tanHalfAngle();
        double vtan = verticalFieldOfView.tanHalfAngle();

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // Compute the smallest distance from the center point needed to contain the screen extent's model coordinate
        // reference points visible in the viewport.
        for (ScreenExtent se : screenExtents)
        {
            if (se == null || se.getModelReferencePoint() == null)
                continue;

            Vec4 p = se.getModelReferencePoint().transformBy4(modelview);
            double metersPerPixel = ViewUtil.computePixelSizeAtDistance(-p.z, fieldOfView, viewport);
            double metersOffset = SCREEN_POINT_PADDING_PIXELS * metersPerPixel;

            d = (p.z - c.z) + (metersOffset + Math.abs(p.x)) / htan;
            if (maxDistance < d)
                maxDistance = d;

            d = (p.z - c.z) + (metersOffset + Math.abs(p.y)) / vtan;
            if (maxDistance < d)
                maxDistance = d;
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    protected Vec4 computeEyePointForScreenBounds(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, double nearClipDistance, double farClipDistance,
        Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // If possible, estimate an eye distance which makes the entire screen bounds visible.
        for (ScreenExtent se : screenExtents)
        {
            if (se == null || se.getModelReferencePoint() == null || se.getScreenBounds() == null)
                continue;

            Vec4 ep = se.getModelReferencePoint().transformBy4(modelview);
            Vec4 sp = ViewUtil.project(se.getModelReferencePoint(), modelview, projection, viewport);
            Rectangle r = se.getScreenBounds();

            if (r.getWidth() < viewport.getWidth()
                && (r.getMinX() < viewport.getMinX() || r.getMaxX() > viewport.getMaxX()))
            {
                double x0 = Math.abs(viewport.getCenterX() - sp.x);
                double x1 = (r.getMinX() < viewport.getMinX()) ?
                    (viewport.getMinX() - r.getMinX()) :
                    (r.getMaxX() - viewport.getMaxX());

                if (x1 < x0)
                {
                    d = (ep.z - c.z) + Math.abs(ep.z) * x0 / (x0 - x1);
                    if (maxDistance < d)
                        maxDistance = d;
                }
            }

            if (r.getHeight() < viewport.getHeight()
                && (r.getMinY() < viewport.getMinY() || r.getMaxY() > viewport.getMaxY()))
            {
                double y0 = Math.abs(viewport.getCenterY() - sp.y);
                double y1 = (r.getMinY() < viewport.getMinY()) ?
                    (viewport.getMinY() - r.getMinY()) :
                    (r.getMaxY() - viewport.getMaxY());

                if (y1 < y0)
                {
                    d = (ep.z - c.z) + Math.abs(ep.z) * y0 / (y0 - y1);
                    if (maxDistance < d)
                        maxDistance = d;
                }
            }
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    protected Iterable<ScreenExtent> translateScreenExtents(Iterable<? extends ScreenExtent> screenExtents,
        Matrix oldModelview, Matrix newModelview, Matrix projection, Rectangle viewport)
    {
        ArrayList<ScreenExtent> adjustedScreenExtents = new ArrayList<ScreenExtent>();

        for (ScreenExtent se : screenExtents)
        {
            if (se.getModelReferencePoint() != null && se.getScreenBounds() != null)
            {
                Vec4 sp1 = ViewUtil.project(se.getModelReferencePoint(), oldModelview, projection, viewport);
                Vec4 sp2 = ViewUtil.project(se.getModelReferencePoint(), newModelview, projection, viewport);
                Vec4 d = sp2.subtract3(sp1);

                Rectangle newBounds = new Rectangle(se.getScreenBounds());
                newBounds.translate((int) d.x, (int) d.y);

                adjustedScreenExtents.add(new ScreenExtent(se.getModelReferencePoint(), newBounds));
            }
            else if (se.getModelReferencePoint() != null)
            {
                adjustedScreenExtents.add(se);
            }
        }

        return adjustedScreenExtents;
    }
}
