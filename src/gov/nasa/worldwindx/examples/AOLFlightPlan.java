package gov.nasa.worldwindx.examples;

import java.util.*;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.geojson.*;

public class AOLFlightPlan {

    private GeoJSONPoint gcs_location;
    private String callsign;
    private String registration_id;
    private Position lla;
    private String gufi;
    private ArrayList<OperationVolume> opVolumes;
    private String flight_comments;
    private GeoJSONPoint controller_location;
    private ArrayList<Registration> registrations;

    public AOLFlightPlan(AVList attrs) {
        attrs.getEntries().forEach((e) -> {
            switch (e.getKey()) {
                case "gcs_location":
                    this.gcs_location = (GeoJSONPoint) e.getValue();
                    break;
                case "callsign":
                    this.callsign = e.getValue().toString();
                    break;
                case "lla":
                    Object[] coords = (Object[]) e.getValue();
                    this.lla = Position.fromDegrees((Double) coords[0], (Double) coords[1], (Double) coords[2]);
                    break;
                case "gufi":
                    this.gufi = e.getValue().toString();
                    break;
                case "operation_volumes":
                    Object[] volumes = (Object[]) e.getValue();
                    this.opVolumes = new ArrayList<>();
                    for (Object o : volumes) {
                        this.opVolumes.add(new OperationVolume((AVList) o));
                    }
                    break;
                case "flight_comments":
                    this.flight_comments = e.getValue().toString();
                    break;
                case "controller_location":
                    this.controller_location = (GeoJSONPoint) e.getValue();
                    break;
                case "aircraft_comments":
                case "aircraftType":
                case "created_by":
                case "decision_time":
                case "faa_rule":
                case "metadata":
                case "primary_contact_phone":
                case "priority_op":
                case "state":
                case "substate":
                case "user_id":
                case "uss_name":
                case "uss_instance_id":
                    break;
                case "registration":
                    Object[] regList = (Object[]) e.getValue();
                    this.registrations = new ArrayList<>();
                    for (Object o : regList) {
                        this.registrations.add(new Registration((AVList) o));
                    }
                    break;
                default:
                    System.out.println(e.getKey() + "," + e.getValue());
                    break;
            }
        });
    }
    
    public String getGufi() {
        return this.gufi;
    }

    public String getCallSign() {
        return this.callsign;
    }
    
    public ArrayList<OperationVolume> getOpVolumes() {
        return this.opVolumes;
    }

}
