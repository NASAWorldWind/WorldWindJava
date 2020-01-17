package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.avlist.AVKey;
import java.util.*;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.util.measure.LengthMeasurer;
import gov.nasa.worldwind.globes.Globe;

public class AnimatedObject {

    private class ObjectPropertyAccessor implements PropertyAccessor.PositionAccessor, PropertyAccessor.AngleAccessor {

        @Override
        public Position getPosition() {
            return object.getPosition();
        }

        @Override
        public boolean setPosition(Position value) {
            object.setPosition(value);
            return true;
        }

        @Override
        public Angle getAngle() {
            return object.getHeading();
        }

        @Override
        public boolean setAngle(Angle value) {
            object.setHeading(value);
            return true;
        }

    }
    private static final int DEFAULT_REDRAW_REQUESTED = 17; // ~ 60 FPS
    private final Animatable object;
    private Path route;
    private final AnimationController animationController;
    private double velocity;
    private Iterator<? extends Position> positionIterator;
    private Position destination;

    public AnimatedObject(Animatable object) {
        this.animationController = new AnimationController();
        this.object = object;
        this.object.setRedrawRequested(DEFAULT_REDRAW_REQUESTED);
    }

    public Animatable getObject() {
        return this.object;
    }

    public void setRoute(Path route) {
        this.route = route;
        this.positionIterator = null;
    }

    public Path getRoute() {
        return this.route;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getVelocity() {
        return this.velocity;
    }

    protected Position getNextPosition() {
        if (this.positionIterator == null || !this.positionIterator.hasNext()) {
            this.positionIterator = this.route.getPositions().iterator();
        }
        if (this.positionIterator.hasNext()) {
            return this.positionIterator.next();
        }

        return null;
    }

    protected void createNextLeg(Globe globe) {
        this.animationController.clear();
        Position curPos = this.object.getPosition();
        this.destination = this.getNextPosition();
        LengthMeasurer measurer = new LengthMeasurer(new Position[]{curPos, destination});
        long travelTime = (long) ((measurer.getLength(globe) / this.velocity) * 1000);
        ObjectPropertyAccessor accessor = new ObjectPropertyAccessor();
        PositionAnimator posAnimator = new PositionAnimator(new ScheduledInterpolator(travelTime), curPos, destination, accessor);
        this.animationController.put(posAnimator, posAnimator);
        Angle startHeading = object.getHeading();
        Angle endHeading = LatLon.greatCircleAzimuth(destination, curPos);
        AngleAnimator headingAnimator = new AngleAnimator(new ScheduledInterpolator(1000), startHeading, endHeading, accessor);
        this.animationController.put(headingAnimator, headingAnimator);
    }

    public void startAnimation(Globe globe) {
        Position p1 = this.getNextPosition();
        this.object.setPosition(p1);
        createNextLeg(globe);
    }

    public void stepAnimation(Globe globe) {
        if (!this.animationController.stepAnimators()) {
            createNextLeg(globe);
        }
    }

    public String getMetadata() {
        return this.object.getField(AVKey.ANIMATION_META_DATA).toString();
    }

    public void setMetadata(String data) {
        this.object.setField(AVKey.ANIMATION_META_DATA, data);
    }
}
