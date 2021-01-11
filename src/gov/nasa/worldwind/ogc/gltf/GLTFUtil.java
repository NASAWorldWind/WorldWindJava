package gov.nasa.worldwind.ogc.gltf;

public class GLTFUtil {
    
    public static int getInt(Object value) {
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        System.out.println("Type not implemented.");
        return Integer.MAX_VALUE;
    }

    public static double getDouble(Object value) {
        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        }
        System.out.println("Type not implemented.");
        return Double.NaN;
    }

    public static int[] retrieveIntArray(Object[] objectArray) {
        int[] intArray = new int[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            intArray[i]=GLTFUtil.getInt(objectArray[i]);
        }
        return intArray;
    }
    
    public static double[] retrieveDoubleArray(Object[] objectArray) {
        double[] doubleArray = new double[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            doubleArray[i]=GLTFUtil.getDouble(objectArray[i]);
        }
        return doubleArray;
        
    }
}
