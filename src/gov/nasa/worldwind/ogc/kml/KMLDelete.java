/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Delete</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLDelete.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLDelete extends AbstractXMLEventParser implements KMLUpdateOperation
{
    protected List<KMLAbstractFeature> features = new ArrayList<KMLAbstractFeature>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLDelete(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractFeature)
            this.addFeature((KMLAbstractFeature) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    protected void addFeature(KMLAbstractFeature o)
    {
        this.features.add(o);
    }

    public List<KMLAbstractFeature> getFeatures()
    {
        return this.features;
    }

    public void applyOperation(KMLRoot targetRoot)
    {
        for (KMLAbstractFeature feature : this.features)
        {
            String targetId = feature.getTargetId();
            if (WWUtil.isEmpty(targetId))
                continue;

            Object o = targetRoot.getItemByID(targetId);
            if (o == null || !(o instanceof KMLAbstractFeature))
                continue;

            KMLAbstractFeature featureToDelete = (KMLAbstractFeature) o;

            Object p = featureToDelete.getParent();
            if (!(p instanceof KMLAbstractContainer))
                continue;

            ((KMLAbstractContainer) p).removeFeature(featureToDelete);
        }
    }
}
