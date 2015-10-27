/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.*;

/**
 * Base class for controllers to animate the view to look at KML features. Each controller includes logic to animate to
 * a {@code LookAt} or {@code Camera} view, or to animate to a default view a KML feature that does not define a view.
 * Subclasses of this base class implement animator logic for particular types of {@link View}, for example {@link
 * OrbitView}.
 * <p/>
 * An application that provides a custom View implementation can extend this base class to provide a KML controller for
 * the custom view.
 *
 * @author pabercrombie
 * @version $Id: KMLViewController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLViewController
{
    /** Default altitude from which to view a KML feature. */
    public static final double DEFAULT_VIEW_ALTITUDE = 10000;

    /** Default altitude from which to view a KML feature. */
    protected double viewAltitude = DEFAULT_VIEW_ALTITUDE;

    /** WorldWindow that holds the view to animate. */
    protected WorldWindow wwd;

    /**
     * Convenience method to create a new view controller appropriate for the <code>WorldWindow</code>'s current
     * <code>View</code>. Accepted view types are as follows: <ul> <li>{@link gov.nasa.worldwind.view.orbit.OrbitView}</li>
     * <li>{@link gov.nasa.worldwind.view.firstperson.BasicFlyView}</li> </ul>. If the <code>View</code> is not one of
     * the recognized types, this returns <code>null</code> and logs a warning.
     *
     * @param wwd the <code>WorldWindow</code> to create a view controller for.
     *
     * @return A new view controller, or <code>null</code> if the <code>WorldWindow</code>'s <code>View</code> type is
     *         not one of the recognized types.
     */
    public static KMLViewController create(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        View view = wwd.getView();

        if (view instanceof OrbitView)
            return new KMLOrbitViewController(wwd);
        else if (view instanceof BasicFlyView)
            return new KMLFlyViewController(wwd);
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
            return null; // Unknown view
        }
    }

    /**
     * Create the view controller.
     *
     * @param wwd WorldWindow that holds the view to animate.
     */
    protected KMLViewController(WorldWindow wwd)
    {
        this.wwd = wwd;
    }

    /**
     * Animate the view to look at a KML feature. If the feature defines a {@code LookAt} or {@code Camera}, the view
     * will animate to the view specified in KML. Otherwise the view will animate to a default position looking straight
     * down at the feature from an altitude that brings the entire feature into view.
     *
     * @param feature Feature to look at.
     */
    public void goTo(KMLAbstractFeature feature)
    {
        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // First look for a KML view in the feature
        KMLAbstractView kmlView = feature.getView();
        if (kmlView instanceof KMLLookAt)
            this.goTo((KMLLookAt) kmlView);
        else if (kmlView instanceof KMLCamera)
            this.goTo((KMLCamera) kmlView);
        else
            this.goToDefaultView(feature);
    }

    /**
     * Animates the <code>View</code> attached to this controller's WorldWindow to look at the specified KML
     * <code>view</code>. The <code>view</code> may be one of the following types: <ul> <li>{@link
     * gov.nasa.worldwind.ogc.kml.KMLLookAt}</li> <li>{@link gov.nasa.worldwind.ogc.kml.KMLCamera}</li> </ul> If the
     * <code>view</code> is not <code>null</code> and is not one of the recognized types, this logs a warning but
     * otherwise does nothing.
     *
     * @param view the KML view to animate to.
     *
     * @throws IllegalArgumentException if <code>view</code> is <code>null</code>.
     */
    public void goTo(KMLAbstractView view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (view instanceof KMLLookAt)
            this.goTo((KMLLookAt) view);
        else if (view instanceof KMLCamera)
            this.goTo((KMLCamera) view);
        else
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
    }

    /**
     * Animate the view to the position described by a KML {@code LookAt}.
     *
     * @param lookAt KML LookAt that describes the desired view.
     */
    protected abstract void goTo(KMLLookAt lookAt);

    /**
     * Animate the view to the position described by a KML {@code Camera}.
     *
     * @param camera KML Camera that describes the desired view.
     */
    protected abstract void goTo(KMLCamera camera);

    /**
     * Move the view to look at a KML feature. The view will be adjusted to look at the bounding sector that contains
     * all of the feature's points.
     *
     * @param feature Feature to look at.
     */
    public void goToDefaultView(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark)
        {
            this.goToDefaultPlacemarkView((KMLPlacemark) feature);
        }
        else if (feature instanceof KMLGroundOverlay)
        {
            this.goToDefaultGroundOverlayView((KMLGroundOverlay) feature);
        }
    }

    /**
     * Go to a default view of a Placemark.
     *
     * @param placemark Placemark to look at
     */
    protected void goToDefaultPlacemarkView(KMLPlacemark placemark)
    {
        View view = this.wwd.getView();
        List<Position> positions = new ArrayList<Position>();

        // Find all the points in the placemark. We want to bring the entire placemark into view.
        KMLAbstractGeometry geometry = placemark.getGeometry();
        KMLUtil.getPositions(view.getGlobe(), geometry, positions);

        this.goToDefaultView(positions);
    }

    /**
     * Go to a default view of  GroundOverlay.
     *
     * @param overlay Overlay to look at
     */
    protected void goToDefaultGroundOverlayView(KMLGroundOverlay overlay)
    {
        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        List<? extends Position> corners = overlay.getPositions().list;

        String altitudeMode = overlay.getAltitudeMode() != null ? overlay.getAltitudeMode() : "clampToGround";
        corners = KMLUtil.computeAltitude(this.wwd.getModel().getGlobe(), corners, altitudeMode);

        this.goToDefaultView(corners);
    }

    /**
     * Go to a view of a list of positions. This method computes a view looking straight down from an altitude that
     * brings all of the positions into view.
     *
     * @param positions List of positions to bring into view
     */
    protected void goToDefaultView(List<? extends Position> positions)
    {
        View view = this.wwd.getView();

        // If there is only one point, move the view over that point, maintaining the current elevation.
        if (positions.size() == 1) // Only one point
        {
            Position pos = positions.get(0);
            view.goTo(pos, pos.getAltitude() + this.getViewAltitude());
        }
        else if (positions.size() > 1)// Many points
        {
            // Compute the sector that bounds all of the points in the list. Move the view so that this entire
            // sector is visible.
            Sector sector = Sector.boundingSector(positions);
            Globe globe = view.getGlobe();
            double ve = this.wwd.getSceneController().getVerticalExaggeration();

            // Find the highest point in the geometry. Make sure that our bounding cylinder encloses this point.
            double maxAltitude = this.findMaxAltitude(positions);

            double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
            double minElevation = minAndMaxElevations[0];
            double maxElevation = Math.max(minAndMaxElevations[1], maxAltitude);

            Extent extent = Sector.computeBoundingCylinder(globe, ve, sector, minElevation, maxElevation);
            if (extent == null)
            {
                String message = Logging.getMessage("nullValue.SectorIsNull");
                Logging.logger().warning(message);
                return;
            }
            Angle fov = view.getFieldOfView();

            Position centerPos = new Position(sector.getCentroid(), maxAltitude);
            double zoom = extent.getRadius() / (fov.tanHalfAngle() * fov.cosHalfAngle()) + this.getViewAltitude();

            view.goTo(centerPos, zoom);
        }
    }

    /**
     * Get the maximum altitude in a list of positions.
     *
     * @param positions List of positions to search for max altitude.
     *
     * @return The maximum elevation in the list of positions. Returns {@code Double.MIN_VALUE} if {@code positions} is
     *         empty.
     */
    protected double findMaxAltitude(List<? extends Position> positions)
    {
        double maxAltitude = -Double.MAX_VALUE;
        for (Position p : positions)
        {
            double altitude = p.getAltitude();
            if (altitude > maxAltitude)
                maxAltitude = altitude;
        }

        return maxAltitude;
    }

    /**
     * Get the default altitude for viewing a KML placemark when the globe flies to a placemark. This setting is only
     * used if the placemark does not specify a view.
     *
     * @return Default altitude from which to view a placemark.
     */
    public double getViewAltitude()
    {
        return this.viewAltitude;
    }

    /**
     * Set the default altitude for viewing a KML placemark when the globe flies to a placemark. This setting is only
     * used if the placemark does not specify a view.
     *
     * @param viewAltitude Default altitude from which to view a placemark.
     */
    public void setViewAltitude(double viewAltitude)
    {
        this.viewAltitude = viewAltitude;
    }
}
