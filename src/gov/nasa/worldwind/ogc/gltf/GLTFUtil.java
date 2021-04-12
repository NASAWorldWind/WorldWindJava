/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import java.awt.Color;

@TypeScriptImports(imports = "./GLTFMaterial,./GLTFPBRMetallicRoughness,../../shapes/ShapeAttributes,../../util/Color")
public class GLTFUtil {

    public static int getInt(Object value) {
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        System.out.println("Type not implemented.");
        return Integer.MAX_VALUE;
    }
    
    // shim function for TypeScript conversion
    public static AVListImpl asAVList(Object value) {
        if (value instanceof AVListImpl) {
            return (AVListImpl) value;
        }
        return null;
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
            intArray[i] = GLTFUtil.getInt(objectArray[i]);
        }
        return intArray;
    }

    public static double[] retrieveDoubleArray(Object[] objectArray) {
        double[] doubleArray = new double[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            doubleArray[i] = GLTFUtil.getDouble(objectArray[i]);
        }
        return doubleArray;

    }

    public static ShapeAttributes computeMaterialAttrs(ShapeAttributes dest, GLTFMaterial material) {
        GLTFPBRMetallicRoughness pmr = material.getPbrMetallicRoughness();
        if (pmr != null) {
            if (pmr.getBaseColorFactor() != null) {
                double[] baseColor = pmr.getBaseColorFactor();
                Color newColor = new Color((float) baseColor[0], (float) baseColor[1], (float) baseColor[2],
                        (float) baseColor[3]);
                dest.setInteriorMaterial(new Material(newColor));
            }
        }
        return dest;
    }
}
