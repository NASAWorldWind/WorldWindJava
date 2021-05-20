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

package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;
import java.util.*;

/**
 * @author Patrick Murris
 * @version $Id: VPFSymbolSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFSymbolSupport
{
    protected GeoSymSupport geoSymSupport;

    public VPFSymbolSupport(String geoSymPath, String geoSymMimeType)
    {
        this.geoSymSupport = new GeoSymSupport(geoSymPath, geoSymMimeType);
    }

    public Iterable<? extends VPFSymbolKey> getSymbolKeys(VPFFeatureClass featureClass, String featureCode,
        AVList featureAttributes)
    {
        if (featureCode != null)
        {
            Iterable<? extends VPFSymbolKey> keys = this.doGetSymbolKeys(featureClass, featureCode, featureAttributes);
            if (keys != null)
                return keys;

            keys = this.geoSymSupport.getSymbolKeys(featureClass, featureCode, featureAttributes);
            if (keys != null)
                return keys;
        }

        return Arrays.asList(VPFSymbolKey.UNKNOWN_SYMBOL_KEY);
    }

    public Iterable<? extends VPFSymbolAttributes> getSymbolAttributes(VPFFeatureClass featureClass, VPFSymbolKey key)
    {
        Iterable<? extends VPFSymbolAttributes> attr = this.doGetAttributes(featureClass, key);
        if (attr != null)
            return attr;

        attr = this.geoSymSupport.getSymbolAttributes(featureClass, key);
        if (attr != null)
            return attr;

        if (key == VPFSymbolKey.UNKNOWN_SYMBOL_KEY)
        {
            attr = this.assembleGenericAttributes(featureClass, key);
        }

        return attr;
    }

    public Iterable<? extends VPFSymbolAttributes> getSymbolAttributes(VPFFeatureClass featureClass, String featureCode,
        AVList featureAttributes)
    {
        Iterable<? extends VPFSymbolKey> keys = this.getSymbolKeys(featureClass, featureCode, featureAttributes);
        if (keys == null)
            return null;

        ArrayList<VPFSymbolAttributes> attrList = new ArrayList<VPFSymbolAttributes>();

        for (VPFSymbolKey key : keys)
        {
            Iterable<? extends VPFSymbolAttributes> attr = this.getSymbolAttributes(featureClass, key);
            if (attr != null)
            {
                for (VPFSymbolAttributes a : attr)
                {
                    attrList.add(a);
                }
            }
        }

        return attrList;
    }

    public String getSymbolLabelText(VPFSymbolAttributes.LabelAttributes attr, AVList featureAttributes)
    {
        String text = null;

        // Look up label text.
        Object o = featureAttributes.getValue(attr.getAttributeName());
        if (o instanceof String)
        {
            String s = (String) o;
            if (s.length() > 0 && !s.equalsIgnoreCase("UNK"))
                text = s;
        }
        // Use abbreviation
        else if (o instanceof Number && attr.getAbbreviationTableId() > 0)
        {
            text = this.geoSymSupport.getAbbreviation(attr.getAbbreviationTableId(), ((Number) o).intValue());
        }

        if (text != null)
        {
            StringBuilder sb = new StringBuilder();

            if (attr.getPrepend() != null)
                sb.append(attr.getPrepend());

            sb.append(text);

            if (attr.getAppend() != null)
                sb.append(attr.getAppend());

            text = sb.toString();
        }

        return text;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Iterable<? extends VPFSymbolKey> doGetSymbolKeys(VPFFeatureClass featureClass, String featureCode,
        AVList featureAttributes)
    {
        if (featureClass.getType() == VPFFeatureType.TEXT)
        {
            Integer i = this.getSymbolId(featureAttributes);
            if (i != null)
            {
                return Arrays.asList(new VPFSymbolKey(i));
            }
        }

        return null;
    }

    protected Iterable<? extends VPFSymbolAttributes> doGetAttributes(VPFFeatureClass featureClass, VPFSymbolKey key)
    {
        if (featureClass.getType() == VPFFeatureType.TEXT)
        {
            return this.assembleTextAttributes(featureClass, key);
        }

        return null;
    }

    //**************************************************************//
    //********************  Text Attribute Assembly  ***************//
    //**************************************************************//

    protected Iterable<? extends VPFSymbolAttributes> assembleTextAttributes(VPFFeatureClass featureClass,
        VPFSymbolKey key)
    {
        VPFBufferedRecordData symbolTable = featureClass.getCoverage().getSymbolRelatedAttributeTable();
        if (symbolTable == null)
        {
            return null;
        }

        VPFRecord symbolRow = null;

        for (VPFRecord row : symbolTable)
        {
            Object o = row.getValue("symbol_id");
            if (o == null || !(o instanceof Number))
                continue;

            int rowSymbolId = ((Number) o).intValue();
            if (rowSymbolId == key.getSymbolCode())
            {
                symbolRow = row;
                break;
            }
        }

        if (symbolRow == null)
        {
            return null;
        }

        VPFSymbolAttributes attr = new VPFSymbolAttributes(VPFFeatureType.TEXT, key);
        attr.setDrawInterior(false);
        attr.setDrawOutline(false);

        VPFSymbolAttributes.LabelAttributes labelAttr = new VPFSymbolAttributes.LabelAttributes();

        String fontName = null;

        int i = (Integer) symbolRow.getValue("fon"); // Text font name.
        switch (i)
        {
            case 1:
                fontName = "Arial"; // System default
                break;
        }

        if (fontName != null)
        {
            // Ignore the 'sty' attribute - AWT does not provide the equivalent functionality to specify the font as
            // 'Kern', 'Proportional', or 'Constant'.
            int size = (Integer) symbolRow.getValue("size"); // Text font size in points.
            labelAttr.setFont(new Font(fontName, 0, size));
        }

        i = (Integer) symbolRow.getValue("col"); // Text color.
        switch (i)
        {
            case 1:
                labelAttr.setColor(Color.BLACK);
                break;
            case 4:
                labelAttr.setColor(Color.BLUE);
                break;
            case 9:
                labelAttr.setColor(new Color(0xA62A2A)); // Red-Brown
                break;
            case 12:
                labelAttr.setColor(Color.MAGENTA);
                break;
        }
        labelAttr.setBackgroundColor(WWUtil.computeContrastingColor(labelAttr.getColor()));

        attr.setLabelAttributes(new VPFSymbolAttributes.LabelAttributes[] {labelAttr});

        return Arrays.asList(attr);
    }

    protected Integer getSymbolId(AVList params)
    {
        Object o = params.getValue("symbol_id");
        return (o != null && o instanceof Number) ? ((Number) o).intValue() : null;
    }

    //**************************************************************//
    //********************  Generic Attribute Assembly  ************//
    //**************************************************************//

    protected Iterable<? extends VPFSymbolAttributes> assembleGenericAttributes(VPFFeatureClass featureClass,
        VPFSymbolKey key)
    {
        VPFSymbolAttributes attr = new VPFSymbolAttributes(featureClass.getType(), key);
        attr.setDrawInterior(false);
        attr.setDrawOutline(true);
        attr.setOutlineMaterial(Material.GRAY);
        attr.setIconImageSource("images/vpf_unknownsymbol-32x64.png");

        return Arrays.asList(attr);
    }
}
