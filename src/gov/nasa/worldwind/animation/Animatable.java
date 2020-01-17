package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Angle;

public interface Animatable {
    public void setPosition(Position position);
    public Position getPosition();
    public int getRedrawRequested();
    public void setRedrawRequested(int redrawRequested);
    public Angle getHeading();
    public void setHeading(Angle heading);
    public void setField(String keyName, Object value);
    public Object getField(String keyName);
}

