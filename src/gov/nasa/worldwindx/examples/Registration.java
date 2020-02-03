package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.*;

public class Registration {

    private String registration_id;
    private String registration_location;

    public Registration(AVList attrs) {
        attrs.getEntries().forEach((e) -> {
            switch (e.getKey()) {
                case "registration_id":
                    this.registration_id = e.getValue().toString();
                    break;
                case "registration_location":
                    this.registration_location = e.getValue().toString();
                    break;
                default:
                    System.out.println("Unknown attribute.");
                    break;
            }
        });
    }
}
