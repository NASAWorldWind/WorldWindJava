package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

public interface KMLMutable {
    public void setPosition(Position position);
    public Position getPosition();
    public void setScale(Vec4 modelScale);
}
