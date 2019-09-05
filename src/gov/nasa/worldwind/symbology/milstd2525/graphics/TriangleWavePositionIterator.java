/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Iterator that computes the positions required to draw a triangle wave along a line specified by control positions.
 * The generated wave looks like this:
 * <pre>
 *           /\             /\         &lt;--- Amplitude
 *          /  \           /  \
 * ________/    \_________/    \_____
 * ^            ^
 * | Wave length|
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: TriangleWavePositionIterator.java 423 2012-03-02 21:43:57Z pabercrombie $
 */
public class TriangleWavePositionIterator implements Iterator
{
    /** Initial state. */
    protected static final int STATE_FIRST = 0;
    /** Drawing connecting line between waves. */
    protected static final int STATE_LINE = 1;
    /** About to draw wave. */
    protected static final int STATE_WAVE_START = 2;
    /** At peak of wave. */
    protected static final int STATE_TOOTH_PEAK = 3;
    /** Current state of the state machine. */
    protected int state = STATE_FIRST;

    /** Control positions. */
    protected Iterator<? extends Position> positions;

    /** Globe used to compute geographic positions. */
    protected Globe globe;
    /** Amplitude of the wave, in meters. */
    protected double amplitude;
    /** Wavelength, as a geographic angle. */
    protected Angle halfWaveLength;

    /** Current position. */
    protected Position thisPosition;
    /** Position of the next control point. */
    protected Position nextControlPosition;
    /** First position along the line. */
    protected Position firstPosition;
    /** End position for the current wave. */
    protected Position waveEndPosition;

    /** Distance (in meters) to the next wave start or end. */
    protected double thisStep;

    /**
     * Create a new iterator to compute the positions of a triangle wave.
     *
     * @param positions  Control positions for the triangle wave line.
     * @param waveLength Distance (in meters) between waves.
     * @param amplitude  Amplitude (in meters) of the wave. This is the distance from the base line to the tip of each
     *                   triangular wave.
     * @param globe      Globe used to compute geographic positions.
     */
    public TriangleWavePositionIterator(Iterable<? extends Position> positions, double waveLength, double amplitude,
        Globe globe)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (waveLength <= 0 || amplitude <= 0)
        {
            String message = Logging.getMessage("generic.LengthIsInvalid");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.globe = globe;
        this.amplitude = amplitude;

        this.halfWaveLength = Angle.fromRadians(waveLength / (2.0 * this.globe.getRadius()));
        this.thisStep = this.halfWaveLength.degrees;

        this.positions = positions.iterator();
        this.thisPosition = this.positions.next();
        this.firstPosition = this.thisPosition;
        this.nextControlPosition = this.thisPosition;
    }

    /** {@inheritDoc} */
    public boolean hasNext()
    {
        return this.nextControlPosition != null;
    }

    /** {@inheritDoc} */
    public Position next()
    {
        Position ret;

        // The iterator is implemented as a state machine. For each call to next() we return the appropriate
        // position, and transition to the next state.
        //
        //           /\             /\
        //          /  \           /  \
        // ________/    \_________/    \_____
        // ^      ^  ^  ^
        // |      |  |  |
        // Line   |  |  |
        //        |  |  |
        //        Wave start
        //           |  |
        //           Wave peak
        //              |
        //              Line

        switch (this.state)
        {
            // First call to the iterator. Just return the starting position.
            case STATE_FIRST:
                ret = this.thisPosition;
                this.state = STATE_LINE;
                break;

            // Draw a straight segment between waves. Compute the next point and return.
            case STATE_LINE:
                ret = this.computeNext();
                break;

            // Draw a wave. Compute the end position of the wave, then compute and return the peak position.
            case STATE_WAVE_START:
                Position prevPos = this.thisPosition; // Keep track of where the wave starts
                this.waveEndPosition = this.computeNext();

                Vec4 thisPoint = this.globe.computePointFromLocation(prevPos);
                Vec4 pNext = this.globe.computePointFromLocation(this.waveEndPosition);

                LatLon ll = LatLon.interpolateGreatCircle(0.5, prevPos, this.waveEndPosition);
                Vec4 midPoint = this.globe.computePointFromLocation(ll);

                Vec4 vAB = pNext.subtract3(thisPoint);

                Vec4 normal = this.globe.computeSurfaceNormalAtLocation(ll.latitude, ll.longitude);
                Vec4 perpendicular = vAB.cross3(normal);
                perpendicular = perpendicular.normalize3().multiply3(this.amplitude);

                // Compute the two points that form the tooth.
                Vec4 toothPoint = midPoint.add3(perpendicular);

                ret = this.globe.computePositionFromPoint(toothPoint);
                break;

            // Return previously computed wave end position, and transition to Line state.
            case STATE_TOOTH_PEAK:
                ret = this.waveEndPosition;
                this.state = STATE_LINE;
                break;

            default:
                throw new IllegalStateException();
        }

        return ret;
    }

    /**
     * Compute the next position along the line, and transition the state machine to the next state (if appropriate).
     * <p>
     * If the current state is STATE_LINE, this method returns either the next control point (if it is less than
     * waveLength meters from the current position, or a position waveLength meters along the control line. The state
     * machine will transition to STATE_WAVE_START only if the returned position is a full wavelength from the current
     * position.
     * <p>
     * If the current state is STATE_WAVE_START, this method returns a position waveLength meters from the current
     * position along the control line, and transitions to state STATE_WAVE_PEAK.
     *
     * @return next position along the line.
     */
    protected Position computeNext()
    {
        Angle distToNext = LatLon.greatCircleDistance(this.thisPosition, this.nextControlPosition);
        double diff = distToNext.degrees - this.thisStep;

        while (diff < 0)
        {
            if (this.positions.hasNext())
            {
                this.thisPosition = this.nextControlPosition;
                this.nextControlPosition = this.positions.next();

                // If we're drawing a line segment between waves then return the current control point and do not
                // transition states. We retain all of the control points between waves in order to keep the line
                // as close to the application's specification as possible.
                if (this.state == STATE_LINE)
                {
                    this.thisStep -= distToNext.degrees;
                    return this.thisPosition;
                }
            }
            // Handle a polygon that is not closed.
            else if (this.firstPosition != null && !this.firstPosition.equals(this.nextControlPosition))
            {
                this.thisPosition = this.nextControlPosition;
                this.nextControlPosition = this.firstPosition;
                this.firstPosition = null;

                if (this.state == STATE_LINE)
                {
                    this.thisStep -= distToNext.degrees;
                    return this.thisPosition;
                }
            }
            else
            {
                Position next = this.nextControlPosition;
                this.nextControlPosition = null;
                return next;
            }

            // The tooth wraps around a corner. Adjust step size.
            this.thisStep -= distToNext.degrees;

            distToNext = LatLon.greatCircleDistance(this.thisPosition, this.nextControlPosition);
            diff = distToNext.degrees - thisStep;
        }

        Angle azimuth = LatLon.greatCircleAzimuth(this.thisPosition, this.nextControlPosition);
        LatLon ll = LatLon.greatCircleEndPosition(this.thisPosition, azimuth, Angle.fromDegrees(this.thisStep));

        // Transition to the next state. If we were drawing a line we are now drawing a wave. If we were starting a
        // wave, we're now at the wave peak.
        switch (this.state)
        {
            case STATE_LINE:
                this.state = STATE_WAVE_START;
                break;
            case STATE_WAVE_START:
                this.state = STATE_TOOTH_PEAK;
                break;
            default:
                throw new IllegalStateException();
        }

        this.thisStep = this.halfWaveLength.degrees;

        this.thisPosition = new Position(ll, 0);
        return this.thisPosition;
    }

    /** Not supported. */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
