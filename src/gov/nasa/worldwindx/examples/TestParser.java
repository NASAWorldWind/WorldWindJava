package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.formats.geojson.*;
import gov.nasa.worldwind.avlist.*;
import java.util.*;

public class TestParser {
    
    public static void main(String[] args) {
        try {
            GeoJSONDoc test = new GeoJSONDoc("/home/mpeterson/d/foo/aol-data/message.json");
            test.parse();
            Object root = test.getRootObject();
            ArrayList<AOLFlightPlan> plans=new ArrayList<>();
            if (root instanceof Object[]) {
                Object[] rootArray = (Object[]) root;
                for (Object o : rootArray) {
                    if (o instanceof AVList) {
                        AVList avl = (AVList) o;
                        Set<Map.Entry<String, Object>> entries = avl.getEntries();
                        entries.forEach((e) -> {
                            if (e.getKey().equalsIgnoreCase("MessageAolFlightPlan")) {
                                plans.add(new AOLFlightPlan((AVList) e.getValue()));
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
