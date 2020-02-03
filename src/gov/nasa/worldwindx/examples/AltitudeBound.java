package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.*;

public class AltitudeBound {

    private double altitude_value;
    private String vertical_reference;
    private String units_of_measure;
    private String source;

    public AltitudeBound(AVList attrs) {
        attrs.getEntries().forEach((e) -> {
            switch (e.getKey()) {
                case "altitude_value":
                    this.altitude_value = (double) e.getValue();
                    break;
                case "vertical_reference":
                    this.vertical_reference=e.getValue().toString();
                    break;
                case "units_of_measure":
                    this.units_of_measure=e.getValue().toString();
                    break;
                case "source":
                    this.source=e.getValue().toString();
                    break;
                default:
                    System.out.println("Unknown attribute.");
                    break;
            }
        });
    }
    
    public double getAltitudeValue() {
        return altitude_value;
    }
}
