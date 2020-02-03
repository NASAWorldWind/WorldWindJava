package gov.nasa.worldwindx.examples;

import java.io.*;
import java.util.*;

import gov.nasa.worldwind.formats.geojson.*;
import gov.nasa.worldwind.avlist.*;

public class TestParser {

    private ArrayList<AOLFlightPlan> plans = new ArrayList<>();
    private ArrayList<AOLPosition> positions = new ArrayList<>();

    public void parseMessages(String path) throws Exception {
        File messageDir = new File(path);
        FilenameFilter filter = (File directory, String fileName) -> fileName.startsWith("message") && fileName.endsWith(".json");
        File[] messageList = messageDir.listFiles(filter);
        for (File f : messageList) {
            GeoJSONDoc messageJson = new GeoJSONDoc(f);
            messageJson.parse();
            Object root = messageJson.getRootObject();
            if (root instanceof Object[]) {
                Object[] rootArray = (Object[]) root;
                for (Object o : rootArray) {
                    if (o instanceof AVList) {
                        AVList avl = (AVList) o;
                        Set<Map.Entry<String, Object>> entries = avl.getEntries();
                        entries.forEach((e) -> {
                            switch (e.getKey()) {
                                case "MessageAolFlightPlan":
                                    plans.add(new AOLFlightPlan((AVList) e.getValue()));
                                    break;
                                case "MessageAolPosition":
                                    positions.add(new AOLPosition((AVList) e.getValue()));
                                    break;
                                default:
                                    System.out.println("Unknown key:" + e.getKey());
                                    break;
                            }
                        });
                    }
                }
            }
        }
    }
    
    public ArrayList<AOLFlightPlan> getPlans() {
        return this.plans;
    }

    public ArrayList<AOLPosition> getPositions() {
        return this.positions;
    }

    public static void main(String[] args) {
        try {
            TestParser tp = new TestParser();
            tp.parseMessages("/home/mpeterson/d/temp/aol-data");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
