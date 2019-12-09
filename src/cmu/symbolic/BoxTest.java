package cmu.symbolic;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.geom.Angle;

public class BoxTest {
	
	private static Box create_box(double startLat, double startLon, double endLat, double endLon, double leftWidth, double rightWidth) {
		LatLon startLoc = LatLon.fromDegrees(startLat, startLon);
		LatLon endLoc = LatLon.fromDegrees(endLat, endLon);
		return new Box(startLoc, endLoc, leftWidth, rightWidth);
	}
	
	public static void set_widths(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, double newLeftWidth, double newRightWidth) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		box.setWidths(newLeftWidth, newRightWidth);
	}
	
	public static void set_corner_azimuths(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, double newBeginLeftAzimuthDegrees, double newBeginRightAzimuthDegrees,
			double newEndLeftAzimuthDegrees, double newEndRightAzimuthDegrees) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		Angle beginLeftAzimuth = Angle.fromDegrees(newBeginLeftAzimuthDegrees);
		Angle beginRightAzimuth = Angle.fromDegrees(newBeginLeftAzimuthDegrees);
		Angle endLeftAzimuth = Angle.fromDegrees(newEndLeftAzimuthDegrees);
		Angle endRightAzimuth = Angle.fromDegrees(newEndRightAzimuthDegrees);
		box.setCornerAzimuths(beginLeftAzimuth, beginRightAzimuth, endLeftAzimuth, endRightAzimuth);
	}
	
	public static void set_enable_caps(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, boolean enableCaps) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		box.setEnableCaps(enableCaps);
	}
	
	public static void set_enable_start_cap(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, boolean enableCap) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		box.setEnableStartCap(enableCap);
	}
	
	public static void set_enable_end_cap(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, boolean enableCap) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		box.setEnableEndCap(enableCap);
	}

	public static void set_enable_center_line(double startLat, double startLon, double endLat, double endLon, 
			double leftWidth, double rightWidth, boolean enableCenterLine) {
		Box box = create_box(startLat, startLon, endLat, endLon, leftWidth, rightWidth);
		box.setEnableEndCap(enableCenterLine);
	}
	
	public static void main(String[] args) {
		// Disable graphical environment
		System.setProperty("java.awt.headless", "true");
		
		set_widths(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0, 1.0);
		set_corner_azimuths(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 45, 45, 45, 45);
		set_enable_caps(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, true);
		set_enable_start_cap(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, true);
		set_enable_end_cap(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, true);
		set_enable_center_line(1.0, 2.0, 1.0, 2.0, 1.0, 2.0, true);
	}
}