package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.geom.LatLon;

import java.util.Calendar;

/**
 * @author Michael de Hoog - from http://www.psa.es/sdg/sunpos.htm
 * @version $Id: SunCalculator.java 10406 2009-04-22 18:28:45Z patrickmurris $
 */
public class SunCalculator {

    public static LatLon subsolarPoint(Calendar time) {
        // Main variables
        double elapsedJulianDays;
        double decimalHours;
        double eclipticLongitude;
        double eclipticObliquity;
        double rightAscension, declination;

        // Calculate difference in days between the current Julian Day 
        // and JD 2451545.0, which is noon 1 January 2000 Universal Time
        {
            // Calculate time of the day in UT decimal hours
            decimalHours = time.get(Calendar.HOUR_OF_DAY)
                    + (time.get(Calendar.MINUTE) + time.get(Calendar.SECOND) / 60.0)
                    / 60.0;
            // Calculate current Julian Day
            long aux1 = (time.get(Calendar.MONTH) - 14) / 12;
            long aux2 = (1461 * (time.get(Calendar.YEAR) + 4800 + aux1)) / 4
                    + (367 * (time.get(Calendar.MONTH) - 2 - 12 * aux1)) / 12
                    - (3 * ((time.get(Calendar.YEAR) + 4900 + aux1) / 100)) / 4
                    + time.get(Calendar.DAY_OF_MONTH) - 32075;
            double julianDate = (double) (aux2) - 0.5 + decimalHours / 24.0;
            // Calculate difference between current Julian Day and JD 2451545.0 
            elapsedJulianDays = julianDate - 2451545.0;
        }

        // Calculate ecliptic coordinates (ecliptic longitude and obliquity of the 
        // ecliptic in radians but without limiting the angle to be less than 2*Pi 
        // (i.e., the result may be greater than 2*Pi)
        {
            double omega = 2.1429 - 0.0010394594 * elapsedJulianDays;
            double meanLongitude = 4.8950630 + 0.017202791698 * elapsedJulianDays; // Radians
            double meanAnomaly = 6.2400600 + 0.0172019699 * elapsedJulianDays;
            eclipticLongitude = meanLongitude + 0.03341607
                    * Math.sin(meanAnomaly) + 0.00034894
                    * Math.sin(2 * meanAnomaly) - 0.0001134 - 0.0000203
                    * Math.sin(omega);
            eclipticObliquity = 0.4090928 - 6.2140e-9 * elapsedJulianDays
                    + 0.0000396 * Math.cos(omega);
        }

        // Calculate celestial coordinates ( right ascension and declination ) in radians 
        // but without limiting the angle to be less than 2*Pi (i.e., the result may be 
        // greater than 2*Pi)
        {
            double sin_EclipticLongitude = Math.sin(eclipticLongitude);
            double dY = Math.cos(eclipticObliquity) * sin_EclipticLongitude;
            double dX = Math.cos(eclipticLongitude);
            rightAscension = Math.atan2(dY, dX);
            if (rightAscension < 0.0) {
                rightAscension = rightAscension + Math.PI * 2.0;
            }
            declination = Math.asin(Math.sin(eclipticObliquity)
                    * sin_EclipticLongitude);
        }

        double greenwichMeanSiderealTime = 6.6974243242 + 0.0657098283
                * elapsedJulianDays + decimalHours;
        double longitude = rightAscension
                - Math.toRadians(greenwichMeanSiderealTime * 15.0);

        longitude += Math.PI;

        while (declination > Math.PI / 2.0) {
            declination -= Math.PI;
        }
        while (declination <= -Math.PI / 2.0) {
            declination += Math.PI;
        }
        while (longitude > Math.PI) {
            longitude -= Math.PI * 2.0;
        }
        while (longitude <= -Math.PI) {
            longitude += Math.PI * 2.0;
        }

        return LatLon.fromRadians(declination, longitude);
    }
}
