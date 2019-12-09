package cmu.symbolic;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.airspaces.PolyArc;
import gov.nasa.worldwind.geom.Angle;
import java.util.Arrays;
import java.util.List;

public class PolyArcTest {
	
	private static PolyArc create_polyarc(double lat, double lon, double radius, double leftAzimuthDeg, double rightAzimuthDeg) {
		LatLon location = LatLon.fromDegrees(lat, lon);
		Angle leftAzimuth = Angle.fromDegrees(leftAzimuthDeg);
		Angle rightAzimuth = Angle.fromDegrees(rightAzimuthDeg);
		List<LatLon> locations = Arrays.asList(location);
		return new PolyArc(locations, radius, leftAzimuth, rightAzimuth);
	}
	
	public static void set_radius(double lat, double lon, double radius, double leftAzimuthDeg, double rightAzimuthDeg, double newRadius) {
		PolyArc polyarc = create_polyarc(lat, lon, radius, leftAzimuthDeg, rightAzimuthDeg);
		polyarc.setRadius(newRadius);
	}
	
	public static void set_azimuths(double lat, double lon, double radius, double leftAzimuthDeg, double rightAzimuthDeg, double newLeftAzimuth, double newRightAzimuth) {
		PolyArc polyarc = create_polyarc(lat, lon, radius, leftAzimuthDeg, rightAzimuthDeg);
		Angle leftAzimuth = Angle.fromDegrees(newLeftAzimuth);
		Angle rightAzimuth = Angle.fromDegrees(newRightAzimuth);
		polyarc.setAzimuths(leftAzimuth, rightAzimuth);
	}
	
	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "true");
		set_radius(1.0, 2.0, 1.0, 2.0, 1.0, 2.0);
		set_azimuths(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0);
	}

}
