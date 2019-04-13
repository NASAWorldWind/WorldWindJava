/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.util.List;

/**
 * Represents the KML <i>StyleSelector</i> element.
 *
 * @author tag
 * @version $Id: KMLAbstractStyleSelector.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractStyleSelector extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractStyleSelector(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Obtains the selector's effective style for a specified style type (<i>IconStyle</i>, <i>ListStyle</i>, etc.) and
     * state (<i>normal</i> or <i>highlight</i>). The returned style is the result of merging values from the specified
     * style selectors and style URL, with precedence given to style selectors.
     * <p>
     * Remote <i>styleUrls</i> that have not yet been resolved are not included in the result. In this case the returned
     * sub-style is marked with a field named {@link gov.nasa.worldwind.avlist.AVKey#UNRESOLVED}. The same is true when
     * a StyleMap refers to a Style other than one internal to the KML document.
     *
     * @param styleUrl       an applicable style URL. May be null.
     * @param styleSelectors a list of {@link gov.nasa.worldwind.ogc.kml.KMLAbstractStyleSelector}s to consider when
     *                       determining the effective attributes. May be null, in which case only the specified
     *                       <code>styleUrl</code> is considered.
     * @param styleState     the style mode, either \"normal\" or \"highlight\".
     * @param subStyle       an instance of the {@link gov.nasa.worldwind.ogc.kml.KMLAbstractSubStyle} class desired,
     *                       such as {@link gov.nasa.worldwind.ogc.kml.KMLIconStyle}. The effective style values are
     *                       accumulated and merged into this instance. The instance should not be one from within the
     *                       KML document because its values may be overridden and augmented. The instance specified is
     *                       the return value of this method.
     *
     * @return the sub-style values for the specified type and state. The reference returned is the same one passed in
     *         as the <code>subStyle</code> argument.
     *
     * @throws IllegalArgumentException if the sub-style parameter is null.
     */
    public static KMLAbstractSubStyle mergeSubStyles(KMLStyleUrl styleUrl,
        List<KMLAbstractStyleSelector> styleSelectors, String styleState, KMLAbstractSubStyle subStyle)
    {
        if (subStyle == null)
        {
            String message = Logging.getMessage("nullValue.SymbolIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (styleUrl != null)
        {
            KMLAbstractStyleSelector selector = styleUrl.resolveStyleUrl();
            if (selector != null)
                mergeSubStyles(null, selector, styleState, subStyle);
            else
                markUnresolved(true, subStyle);
        }

        if (styleSelectors != null)
        {
            for (KMLAbstractStyleSelector selector : styleSelectors)
            {
                mergeSubStyles(null, selector, styleState, subStyle);
            }
        }

        return subStyle;
    }

    /**
     * Obtains the selector's effective style for a specified style type (<i>IconStyle</i>, <i>ListStyle</i>, etc.) and
     * state (<i>normal</i> or <i>highlight</i>). The returned style is the result of merging values from the specified
     * style selector and style URL, with precedence given to style selector.
     * <p>
     * Remote <i>styleUrls</i> that have not yet been resolved are not included in the result. In this case the returned
     * sub-style is marked with the value {@link gov.nasa.worldwind.avlist.AVKey#UNRESOLVED}.
     *
     * @param styleUrl      an applicable style URL. May be null.
     * @param styleSelector the {@link gov.nasa.worldwind.ogc.kml.KMLAbstractStyleSelector} to consider when determining
     *                      the effective attributes. May be null, in which case only the specified
     *                      <code>styleUrl</code> is considered.
     * @param styleState    the style mode, either \"normal\" or \"highlight\".
     * @param subStyle      an instance of the {@link gov.nasa.worldwind.ogc.kml.KMLAbstractSubStyle} class desired,
     *                      such as {@link gov.nasa.worldwind.ogc.kml.KMLIconStyle}. The effective style values are
     *                      accumulated and merged into this instance. The instance should not be one from within the
     *                      KML document because its values may be overridden and augmented. The instance specified is
     *                      the return value of this method.
     *
     * @return the sub-style values for the specified type and state. The reference returned is the same one passed in
     *         as the <code>subStyle</code> parameter.
     *
     * @throws IllegalArgumentException if the sub-style parameter is null.
     */
    public static KMLAbstractSubStyle mergeSubStyles(KMLStyleUrl styleUrl,
        KMLAbstractStyleSelector styleSelector, String styleState, KMLAbstractSubStyle subStyle)
    {
        if (subStyle == null)
        {
            String message = Logging.getMessage("nullValue.SymbolIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (styleUrl != null)
        {
            KMLAbstractStyleSelector ss = styleUrl.resolveStyleUrl();
            if (ss != null)
                mergeSubStyles(null, ss, styleState, subStyle);
            else
                markUnresolved(true, subStyle);
        }

        if (styleSelector != null)
        {
            if (styleSelector instanceof KMLStyleMap)
                ((KMLStyleMap) styleSelector).mergeSubStyles(subStyle, styleState);
            else
                ((KMLStyle) styleSelector).mergeSubStyle(subStyle);
        }

        return subStyle;
    }

    /**
     * Marks a sub-style to indicate that a style URL associated with it has not yet been resolved, or removes the mark
     * if the style URL has been resolved.
     *
     * @param tf       true to indicate an unresolved style URL, otherwise false.
     * @param subStyle the sub-style to mark.
     */
    protected static void markUnresolved(boolean tf, KMLAbstractSubStyle subStyle)
    {
        if (!tf)
            subStyle.removeField(AVKey.UNRESOLVED);
        else
            subStyle.setField(AVKey.UNRESOLVED, System.currentTimeMillis());
    }
}
