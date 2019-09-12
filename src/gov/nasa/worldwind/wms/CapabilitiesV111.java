/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.wms;

import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import java.util.ArrayList;

/**
 * Version-dependent class for gathering information from a wms capabilities document.
 *
 * @author Tom Gaskins
 * @version $Id: CapabilitiesV111.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class CapabilitiesV111 extends Capabilities
{
    public CapabilitiesV111(Document doc, XPath xpath)
    {
        super(doc, xpath);
    }

    @Override
    public BoundingBox getLayerGeographicBoundingBox(Element layer)
    {
        Element e = this.getElement(layer, "ancestor-or-self::wms:Layer/wms:LatLonBoundingBox");

        return e == null ? null : BoundingBox.createFromStrings("CRS:84",
            this.getText(e, "@wms:minx"), this.getText(e, "@wms:maxx"),
            this.getText(e, "@wms:miny"), this.getText(e, "@wms:maxy"),
            null, null);
    }

    public BoundingBox[] getLayerBoundingBoxes(Element layer)
    {
        Element[] es = this.getElements(layer, "ancestor-or-self::wms:Layer/wms:BoundingBox");
        if (es == null)
            return null;

        ArrayList<BoundingBox> bboxes = new ArrayList<BoundingBox>();
        ArrayList<String> crses = new ArrayList<String>();

        for (Element e : es)
        {
            if (e == null)
                continue;

            BoundingBox bb = BoundingBox.createFromStrings(this.getBoundingBoxSRS(e),
                this.getBoundingBoxMinx(e), this.getBoundingBoxMaxx(e),
                this.getBoundingBoxMiny(e), this.getBoundingBoxMaxy(e),
                this.getBoundingBoxResx(e), this.getBoundingBoxResy(e));

            if (bb != null)
            {
                // Add the bbox only if the ancestor's crs is not one of those in the node's crs.
                if (bb.getCrs() != null && !crses.contains(bb.getCrs()))
                {
                    crses.add(bb.getCrs());
                    bboxes.add(bb);
                }
            }
        }

        return bboxes.size() > 0 ? bboxes.toArray(new BoundingBox[bboxes.size()]) : null;
    }

    @Override
    public String getLayerMaxScaleDenominator(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:ScaleHint/@wms:max");
    }

    @Override
    public String getLayerMinScaleDenominator(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:ScaleHint/@wms:min");
    }
}
