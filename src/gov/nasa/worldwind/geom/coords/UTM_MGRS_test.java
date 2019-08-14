package gov.nasa.worldwind.geom.coords;

import org.junit.Test;

import gov.nasa.worldwind.geom.LatLon;

public class UTM_MGRS_test {

	private LatLon[] input0 = { 
			LatLon.fromDegrees(-74.37916, 155.02235),
			LatLon.fromDegrees(0, 0),
			LatLon.fromDegrees(0.1300, -0.2324),
			LatLon.fromDegrees(-45.6456, 23.3545),
			LatLon.fromDegrees(-12.7650, -33.8765),
			LatLon.fromDegrees(23.4578, -135.4545),
			LatLon.fromDegrees(77.3450,156.9876),
	};

	private LatLon[] MGRS_only = {
			LatLon.fromDegrees(-89.3454, -48.9306),
			LatLon.fromDegrees(-80.5434, -170.6540),
	};
	
	private LatLon[] noInverse = {
			LatLon.fromDegrees(90.0000, 177.0000),
			LatLon.fromDegrees(-90.0000, -177.0000),
			LatLon.fromDegrees(90.0000, 3.0000),
	};
	private String[] noInverseToMgrs = {
			"ZAH 00000 00000", "BAN 00000 00000", "ZAH 00000 00000"
	};
	
	private boolean isClose(double x, double y, double limit) {
		return (Math.abs(x - y) < limit);
	}
	
	private boolean isClose(LatLon a, LatLon b) {
		 double epsilonRad = Math.toRadians(9.0e-6);
		 return isClose(a, b, epsilonRad);
	}
 
	private boolean isClose(LatLon a, LatLon b, double limit) {
		 return isClose(a.latitude.radians, b.latitude.radians, limit)
				 && isClose(a.longitude.radians, b.longitude.radians, limit);
	}

	@Test
	public void test() {
		for (LatLon p : input0) {

			UTMCoord utm = UTMCoord.fromLatLon(p.latitude, p.longitude);
			MGRSCoord mgrs = MGRSCoord.fromLatLon(p.latitude, p.longitude);
			UTMCoord coord1 = UTMCoord.fromUTM(utm.getZone(), utm.getHemisphere(), utm.getEasting(), utm.getNorthing());
			System.out.println(p + " ==> " + " UTM: " + utm.toString() + ", MGRS: " + mgrs.toString());


			LatLon p1 = LatLon.fromRadians(coord1.getLatitude().radians, coord1.getLongitude().radians);
			assert(isClose(p, p1));

			MGRSCoord coord2 = MGRSCoord.fromString(mgrs.toString(), null);
			LatLon p2 = LatLon.fromRadians(coord2.getLatitude().radians, coord2.getLongitude().radians);
			assert(isClose(p.getLatitude().radians, p2.getLatitude().radians, 0.000020));
			assert(isClose(p.getLongitude().radians, p2.getLongitude().radians, 0.000020));
		}
		
		for (LatLon p : MGRS_only) {
			MGRSCoord mgrs = MGRSCoord.fromLatLon(p.latitude, p.longitude);

			System.out.println(p + " ==> " + "MGRS: " + mgrs.toString());

			MGRSCoord coord2 = MGRSCoord.fromString(mgrs.toString(), null);
			LatLon p2 = LatLon.fromRadians(coord2.getLatitude().radians, coord2.getLongitude().radians);
			assert(isClose(p, p2, 0.000020));
		}

		for (int i=0; i < noInverse.length; i++) {
			LatLon p = noInverse[i];
			MGRSCoord mgrs = MGRSCoord.fromLatLon(p.latitude, p.longitude);

			System.out.print(p + " ==> " + "MGRS: " + mgrs.toString());

			MGRSCoord coord2 = MGRSCoord.fromString(mgrs.toString(), null);
			LatLon p2 = LatLon.fromRadians(coord2.getLatitude().radians, coord2.getLongitude().radians);
			System.out.println(" ==> " + p2);
			assert(mgrs.toString().trim().equals(noInverseToMgrs[i]));
		}
	}
}
