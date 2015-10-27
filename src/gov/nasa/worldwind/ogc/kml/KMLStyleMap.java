/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>StyleMap</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLStyleMap.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLStyleMap extends KMLAbstractStyleSelector
{
    protected List<KMLPair> pairs = new ArrayList<KMLPair>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLStyleMap(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLPair)
            this.addPair((KMLPair) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public List<KMLPair> getPairs()
    {
        return this.pairs;
    }

    protected void addPair(KMLPair pair)
    {
        this.pairs.add(pair);
    }

    /**
     * Returns a specified style from the style map.
     *
     * @param styleState the style key, either {@link KMLConstants#NORMAL} or {@link KMLConstants#HIGHLIGHT}. If null,
     *                   {@link KMLConstants#NORMAL} is used.
     *
     * @return the requested style, or null if it does not exist in the map.
     */
    public KMLAbstractStyleSelector getStyleFromMap(String styleState)
    {
        if (styleState == null)
            styleState = KMLConstants.NORMAL;

        for (KMLPair pair : this.pairs)
        {
            if (pair.getKey().equals(styleState))
                return pair.getStyleSelector();
        }

        return null;
    }

    /**
     * Returns a specified style URL from the style map.
     *
     * @param styleState the style key, either {@link KMLConstants#NORMAL} or {@link KMLConstants#HIGHLIGHT}. If null,
     *                   {@link KMLConstants#NORMAL} is used.
     *
     * @return the requested style URL, or null if it does not exist in the map.
     */
    public KMLStyleUrl getStyleUrlFromMap(String styleState)
    {
        if (styleState == null)
            styleState = KMLConstants.NORMAL;

        for (KMLPair pair : this.pairs)
        {
            if (pair.getKey().equals(styleState))
                return pair.getStyleUrl();
        }

        return null;
    }

    /**
     * Obtains the map's effective style for a specified style type (<i>IconStyle</i>, <i>ListStyle</i>, etc.) and state
     * (<i>normal</i> or <i>highlight</i>). The returned style is the result of merging values from the map's style
     * selectors and style URL for the indicated sub-style type, with precedence given to style selectors.
     * <p/>
     * Remote <i>styleUrls</i> that have not yet been resolved are not included in the result. In this case the returned
     * sub-style is marked with the value {@link gov.nasa.worldwind.avlist.AVKey#UNRESOLVED}.
     *
     * @param styleState the style mode, either \"normal\" or \"highlight\".
     * @param subStyle   an instance of the {@link gov.nasa.worldwind.ogc.kml.KMLAbstractSubStyle} class desired, such
     *                   as {@link gov.nasa.worldwind.ogc.kml.KMLIconStyle}. The effective style values are accumulated
     *                   and merged into this instance. The instance should not be one from within the KML document
     *                   because its values may be overridden and augmented. The instance specified is the return value
     *                   of this method.
     *
     * @return the sub-style values for the specified type and state. The reference returned is the same one passed in
     *         as the <code>subStyle</code> argument.
     */
    public KMLAbstractSubStyle mergeSubStyles(KMLAbstractSubStyle subStyle, String styleState)
    {
        KMLStyleUrl styleUrl = this.getStyleUrlFromMap(styleState);
        KMLAbstractStyleSelector selector = this.getStyleFromMap(styleState);
        if (selector == null && styleUrl == null)
            return subStyle;
        else
            subStyle.setField(KMLConstants.STYLE_STATE, styleState); // identify which style state it is

        return KMLAbstractStyleSelector.mergeSubStyles(styleUrl, selector, styleState, subStyle);
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLStyleMap))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        super.applyChange(sourceValues);

        KMLStyleMap sourceMap = (KMLStyleMap) sourceValues;

        if (sourceMap.getPairs() != null && sourceMap.getPairs().size() > 0)
            this.pairs = sourceMap.getPairs();

        this.onChange(new Message(KMLAbstractObject.MSG_STYLE_CHANGED, this));
    }

    /**
     * Merge a list of incoming pairs with the current list. If an incoming pair has the same ID as an
     * existing one, replace the existing one, otherwise just add the incoming one.
     *
     * @param sourceMap the incoming pairs.
     */
    protected void mergePairs(KMLStyleMap sourceMap)
    {
        // Make a copy of the existing list so we can modify it as we traverse the copy.
        List<KMLPair> pairsCopy = new ArrayList<KMLPair>(this.getPairs().size());
        Collections.copy(pairsCopy, this.getPairs());

        for (KMLPair sourcePair : sourceMap.getPairs())
        {
            String id = sourcePair.getId();
            if (!WWUtil.isEmpty(id))
            {
                for (KMLPair existingPair : pairsCopy)
                {
                    String currentId = existingPair.getId();
                    if (!WWUtil.isEmpty(currentId) && currentId.equals(id))
                    {
                        this.getPairs().remove(existingPair);
                    }
                }
            }

            this.getPairs().add(sourcePair);
        }
    }
}
