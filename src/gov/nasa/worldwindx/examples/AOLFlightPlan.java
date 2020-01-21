package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.geojson.*;

public class AOLFlightPlan {

    private GeoJSONPoint gcs_location;
    private String callsign;
    private String registration_id;
    
    public AOLFlightPlan(AVList attribs) {
        System.out.println(attribs);
        attribs.getEntries().forEach((e) -> {
            switch (e.getKey()) {
                case "gcs_location":
                    this.gcs_location=(GeoJSONPoint) e.getValue();
                    break;
                case "callsign":
                    this.callsign=e.getValue().toString();
                    break;
                case "registration":
                    System.out.println(e.getValue());
                    break;
            }
        });
    }

}
