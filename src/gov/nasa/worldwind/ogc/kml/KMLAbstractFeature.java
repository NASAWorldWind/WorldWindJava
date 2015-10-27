/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;
import gov.nasa.worldwind.util.xml.atom.*;
import gov.nasa.worldwind.util.xml.xal.XALAddressDetails;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Feature</i> element and provides access to its contents.
 * <p/>
 * <code>KMLAbstractFeature</code> implements the <code>KMLRenderable</code> interface, but does not actually render
 * anything. Subclasses should override the methods <code>{@link #doPreRender(gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext,
 * gov.nasa.worldwind.render.DrawContext)}</code> and <code>{@link #doRender(gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext,
 * gov.nasa.worldwind.render.DrawContext)}</code> to render their contents. If the <code>visibility</code> property is
 * set to <code>false</code>, this does not call <code>doPreRender</code> and <code>doRender</code> during rendering.
 *
 * @author tag
 * @version $Id: KMLAbstractFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractFeature extends KMLAbstractObject implements KMLRenderable
{
    /** The style selectors specified in the KML Feature element. Is empty if no selectors were specified. */
    protected List<KMLAbstractStyleSelector> styleSelectors = new ArrayList<KMLAbstractStyleSelector>();
    /**
     * The visibility flag for the feature. This field is determined from the visibility element of the KML feature
     * initially, but the client may set it directly, in which case it may then differ from the visibility field in the
     * <code>fields</code> table.
     */
    protected Boolean visibility; // may be different from the visibility field if application has set it explicitly
    /** The region specified in the KML Feature element. Is null if no region was specified. */
    protected KMLRegion region;
    /** A balloon explicitly associated with this feature by the client. This is not a KML field of the Feature element. */
    protected Balloon balloon; // not a KML schema field, merely a convenience field of this class

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractFeature(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractView)
            this.setView((KMLAbstractView) o);
        else if (o instanceof KMLAbstractTimePrimitive)
            this.setTimePrimitive((KMLAbstractTimePrimitive) o);
        else if (o instanceof KMLAbstractStyleSelector)
            this.addStyleSelector((KMLAbstractStyleSelector) o);
        else if (o instanceof KMLRegion)
            this.setRegion((KMLRegion) o);
        else if (o instanceof Boolean && event.asStartElement().getName().getLocalPart().equalsIgnoreCase("visibility"))
            this.setVisibility((Boolean) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    /**
     * Indicates whether this <code>KMLAbstractFeature</code> is enabled for rendering. This returns <code>null</code>
     * if no visibility is specified. This indicates the default visibility of <code>true</code> should be used.
     *
     * @return <code>true</code> or <code>null</code> to draw feature shape, otherwise <code>false</code>. The default
     *         value is <code>true</code>.
     *
     * @see #setVisibility(Boolean)
     */
    public Boolean getVisibility()
    {
        return this.visibility;
    }

    /**
     * Specifies whether this <code>KMLAbstractFeature</code> is enabled for rendering.
     *
     * @param visibility <code>true</code> or <code>null</code> to draw this feature, otherwise <code>false</code>. The
     *                   default value is <code>true</code>.
     *
     * @see #getVisibility()
     */
    public void setVisibility(Boolean visibility)
    {
        this.visibility = visibility;
    }

    public Boolean getOpen()
    {
        return (Boolean) this.getField("open");
    }

    public AtomPerson getAuthor()
    {
        return (AtomPerson) this.getField("author");
    }

    public AtomLink getLink()
    {
        return (AtomLink) this.getField("link");
    }

    public String getAddress()
    {
        return (String) this.getField("address");
    }

    public XALAddressDetails getAddressDetails()
    {
        return (XALAddressDetails) this.getField("AddressDetails");
    }

    public String getPhoneNumber()
    {
        return (String) this.getField("phoneNumber");
    }

    public Object getSnippet()
    {
        Object o = this.getField("snippet");
        if (o != null)
            return o;

        return this.getField("Snippet");
    }

    public String getSnippetText()
    {
        Object o = this.getField("snippet");
        if (o != null)
            return ((String) o).trim();

        KMLSnippet snippet = (KMLSnippet) this.getSnippet();
        if (snippet != null && snippet.getCharacters() != null)
            return snippet.getCharacters().trim(); // trim because string parser might not have parsed it

        return null;
    }

    public String getDescription()
    {
        return (String) this.getField("description");
    }

    protected void setView(KMLAbstractView o)
    {
        this.setField("AbstractView", o);
    }

    public KMLAbstractView getView()
    {
        return (KMLAbstractView) this.getField("AbstractView");
    }

    protected void setTimePrimitive(KMLAbstractTimePrimitive o)
    {
        this.setField("AbstractTimePrimitive", o);
    }

    public KMLAbstractTimePrimitive getTimePrimitive()
    {
        return (KMLAbstractTimePrimitive) this.getField("AbstractTimePrimitive");
    }

    public KMLStyleUrl getStyleUrl()
    {
        return (KMLStyleUrl) this.getField("styleUrl");
    }

    protected void addStyleSelector(KMLAbstractStyleSelector o)
    {
        this.styleSelectors.add(o);
    }

    public List<KMLAbstractStyleSelector> getStyleSelectors()
    {
        return this.styleSelectors;
    }

    public boolean hasStyleSelectors()
    {
        return this.getStyleSelectors() != null && this.getStyleSelectors().size() > 0;
    }

    public boolean hasStyle()
    {
        return this.hasStyleSelectors() || this.getStyleUrl() != null;
    }

    public KMLRegion getRegion()
    {
        return this.region;
    }

    protected void setRegion(KMLRegion region)
    {
        this.region = region;
    }

    public KMLExtendedData getExtendedData()
    {
        return (KMLExtendedData) this.getField("ExtendedData");
    }

    /**
     * Set the balloon associated with this feature.
     * <p/>
     * Note: Balloon is not a field in the KML Feature element. It's a direct field of this class and enables the client
     * to associate a balloon with the feature.
     *
     * @param balloon New balloon.
     */
    public void setBalloon(Balloon balloon)
    {
        this.balloon = balloon;
    }

    /**
     * Get the balloon associated with this feature, if any.
     *
     * @return The balloon associated with the feature. Returns null if there is no associated balloon.
     */
    public Balloon getBalloon()
    {
        return this.balloon;
    }

    /** {@inheritDoc} */
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isFeatureActive(tc, dc))
            return;

        this.doPreRender(tc, dc);
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isFeatureActive(tc, dc))
            return;

        this.doRender(tc, dc);
    }

    /**
     * Indicates whether this <code>KMLAbstractFeature</code> is active and should be rendered on the specified
     * <code>DrawContext</code>. This returns <code>true</code> if the following conditions are all <code>true</code>:
     * <p/>
     * <ul> <li>This feature's <code>visibility</code> is unspecified (<code>null</code>) or is set to
     * <code>true</code>.</li> <li>This feature as no Region and does not inherit a Region from an ancestor, or its
     * Region is active for the specified <code>DrawContext</code>.</li> </ul>
     * <p/>
     * If this feature has no Region, this inherits the Region of its nearest ancestor by using the Region on the top of
     * the KML traversal context's region stack (if any). If there is no ancestor Region this feature is assumed to be
     * the <code>DrawContext's</cod> view and is rendered according to its <code>visibility</code> flag. A Region is
     * considered active if it is visible, and the <code>DrawContext</code> meets the Region's level of detail
     * criteria.
     *
     * @param tc the current KML traversal context. Specifies an inherited Region (if any) and a detail hint.
     * @param dc the current draw context. Used to determine whether this feature's Region is active.
     *
     * @return <code>true</code> if this feature should be rendered, otherwise <code>false</code>.
     */
    protected boolean isFeatureActive(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getVisibility() != null && !this.getVisibility())
            return false;

        KMLRegion region = this.getRegion();
        if (region == null)
            region = tc.peekRegion();

        return region == null || region.isActive(tc, dc);
    }

    /**
     * Called from <code>preRender</code> if this <code>KMLAbstractFeature</code>'s <code>visibility</code> is not set
     * to <code>false</code>. Subclasses should override this method to pre-render their content.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
    {
        // Subclasses override to implement render behavior.
    }

    /**
     * Called from <code>render</code> if this <code>KMLAbstractFeature</code>'s <code>visibility</code> is not set to
     * <code>false</code>. Subclasses should override this method to render their content.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        // Subclasses override to implement render behavior.
    }

    /**
     * Draws the <code>{@link gov.nasa.worldwind.render.Balloon}</code> associated with this KML feature. This does
     * nothing if there is no <code>Balloon</code> associated with this feature.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void renderBalloon(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getBalloon() != null)
            this.getBalloon().render(dc);
    }

    /**
     * Obtains the effective values for a specified sub-style (<i>IconStyle</i>, <i>ListStyle</i>, etc.) and state
     * (<i>normal</i> or <i>highlight</i>). The returned style is the result of merging values from this feature
     * instance's style selectors and its styleUrl, if any, with precedence given to style selectors.
     * <p/>
     * A remote <i>styleUrl</i> that has not yet been resolved is not included in the result. In this case the returned
     * sub-style is marked with the value {@link gov.nasa.worldwind.avlist.AVKey#UNRESOLVED}. The same is true when a
     * StyleMap style selector contains a reference to an external Style and that reference has not been resolved.
     *
     * @param styleState the style mode, either \"normal\" or \"highlight\".
     * @param subStyle   an instance of the sub-style desired, such as {@link gov.nasa.worldwind.ogc.kml.KMLIconStyle}.
     *                   The effective sub-style values are accumulated and merged into this instance. The instance
     *                   should not be one from within the KML document because its values are overridden and augmented;
     *                   it's just an independent variable in which to return the merged attribute values. For
     *                   convenience, the instance specified is returned as the return value of this method.
     *
     * @return the sub-style values for the specified type and state. The reference returned is the one passed in as the
     *         <code>subStyle</code> argument.
     */
    public KMLAbstractSubStyle getSubStyle(KMLAbstractSubStyle subStyle, String styleState)
    {
        return KMLAbstractStyleSelector.mergeSubStyles(this.getStyleUrl(), this.getStyleSelectors(), styleState,
            subStyle);
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLAbstractFeature))
        {
            String message = Logging.getMessage("KML.InvalidElementType", sourceValues.getClass().getName());
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        super.applyChange(sourceValues);

        KMLAbstractFeature sourceFeature = (KMLAbstractFeature) sourceValues;

        if (sourceValues.hasField("visibility"))
            this.setVisibility((Boolean) sourceFeature.getField("visibility"));

        if (sourceFeature.getRegion() != null)
            this.setRegion(sourceFeature.getRegion());

        if (sourceFeature.getStyleSelectors() != null && sourceFeature.getStyleSelectors().size() > 0)
        {
            this.mergeStyleSelectors(sourceFeature);
            this.onChange(new Message(KMLAbstractObject.MSG_STYLE_CHANGED, this));
        }
    }

    /**
     * Merge a list of incoming style selectors with the current list. If an incoming selector has the same ID as an
     * existing one, replace the existing one, otherwise just add the incoming one.
     *
     * @param sourceFeature the incoming style selectors.
     */
    protected void mergeStyleSelectors(KMLAbstractFeature sourceFeature)
    {
        // Make a copy of the existing list so we can modify it as we traverse the copy.
        List<KMLAbstractStyleSelector> styleSelectorsCopy =
            new ArrayList<KMLAbstractStyleSelector>(this.getStyleSelectors().size());
        styleSelectorsCopy.addAll(this.getStyleSelectors());

        for (KMLAbstractStyleSelector sourceSelector : sourceFeature.getStyleSelectors())
        {
            String id = sourceSelector.getId();
            if (!WWUtil.isEmpty(id))
            {
                for (KMLAbstractStyleSelector existingSelector : styleSelectorsCopy)
                {
                    String currentId = existingSelector.getId();
                    if (!WWUtil.isEmpty(currentId) && currentId.equals(id))
                    {
                        this.getStyleSelectors().remove(existingSelector);
                    }
                }
            }

            this.getStyleSelectors().add(sourceSelector);
        }
    }
}
