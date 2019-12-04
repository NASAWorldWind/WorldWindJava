/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.webview.WebResourceResolver;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.*;

/**
 * An implementation of {@link Balloon} that applies a {@link KMLBalloonStyle} to the balloon. Rather than fully
 * implementing the Balloon interface, this class provides a thin wrapper around another Balloon implementation and adds
 * the logic for styling the Balloon according to the KML style. All Balloon methods on this class pass through to the
 * contained Balloon.
 * <p>
 * To use KML Balloon, first create a Balloon of the desired type, and then create the KML Balloon. For example:
 * <pre>
 * <code>
 *   KMLPlacemark myPlacemark = ...;
 *   Position placemarkPosition = ...;
 *
 *   // Create a BrowserBalloon for the placemark.
 *   GlobeBalloon globeBalloon = new GlobeBrowserBalloon(myPlacemark.getName(), placemarkPosition);
 *
 *   // Create a KML Balloon to apply the placemark's KML BalloonStyle to the browser balloon.
 *   KMLGlobeBalloonImpl kmlBalloon = new KMLGlobeBalloonImpl(globeBalloon, myPlacemark);
 * </code>
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: KMLAbstractBalloon.java 1555 2013-08-20 13:33:12Z pabercrombie $
 */
public abstract class KMLAbstractBalloon implements Balloon, WebResourceResolver, PropertyChangeListener
{
    public static final String DISPLAY_MODE_HIDE = "hide";
    public static final String DISPLAY_MODE_DEFAULT = "default";

    protected KMLAbstractFeature parent;
    protected String displayMode = DISPLAY_MODE_DEFAULT;
    /** Indicates that the balloon has default text loaded, rather than text supplied by the BalloonStyle. */
    protected boolean usingDefaultText;
    protected boolean normalAttributesResolved;
    protected boolean highlightAttributesResolved;

    /** Text when balloon is not highlighted. */
    protected String normalText;
    /** Text when balloon is highlighted. */
    protected String highlightText;

    /**
     * Create a globe attached Balloon Impl object for a KML feature.
     *
     * @param feature Feature to create balloon annotation for.
     */
    public KMLAbstractBalloon(KMLAbstractFeature feature)
    {
        if (feature == null)
        {
            String msg = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.parent = feature;
    }

    /**
     * Initialize the object.
     *
     * @param balloon The balloon contained in this wrapper object.
     */
    @SuppressWarnings("deprecation")
    protected void initialize(Balloon balloon)
    {
        balloon.setTextDecoder(this.createTextDecoder(this.parent));
        balloon.setValue(AVKey.CONTEXT, this.parent);

        // Configure this balloon to resolve relative paths in the KML balloon HTML via its resolve() method.
        if (balloon instanceof AbstractBrowserBalloon)
        {
            ((AbstractBrowserBalloon) balloon).setResourceResolver(this);
        }

        // Listen for balloon property changes. These will be forwarded to the parent KMLRoot so that the balloon
        // can trigger a repaint when its contents have changed.
        balloon.addPropertyChangeListener(this);
    }

    /**
     * Get the Balloon object that is contained in the KMLBalloon object.
     *
     * @return The balloon contained by this object.
     */
    protected abstract Balloon getBalloon();

    /**
     * Render the balloon. This method will attempt to resolve the balloon style, if it has not already been resolved.
     *
     * @param dc Draw context
     */
    public void render(DrawContext dc)
    {
        Balloon balloon = this.getBalloon();
        if (balloon.isHighlighted() && !this.highlightAttributesResolved)
        {
            this.makeAttributesCurrent(KMLConstants.HIGHLIGHT);
        }
        else if (!this.normalAttributesResolved)
        {
            this.makeAttributesCurrent(KMLConstants.NORMAL);
        }

        this.determineActiveText();

        if (!WWUtil.isEmpty(this.getText()) && !DISPLAY_MODE_HIDE.equals(this.getDisplayMode()))
            balloon.render(dc);
    }

    /** Determine the balloon text for this frame, depending on the balloon highlight state. */
    protected void determineActiveText()
    {
        String activeText = null;

        // If the balloon is highlighted, use the highlight text.
        if (this.isHighlighted())
        {
            activeText = this.highlightText;
        }

        // If the balloon is not highlighted, or there is no highlight text, use the normal text.
        if (activeText == null)
        {
            activeText = this.normalText;
        }

        // Set the text if it does not match the active text.
        if (activeText != null && !activeText.equals(this.getText()))
        {
            this.setText(activeText);
        }
    }

    /**
     * Update the balloon attributes to match the KML BalloonStyle.
     *
     * @param attrType Type of attributes to update. Either {@link KMLConstants#NORMAL} or {@link
     *                 KMLConstants#HIGHLIGHT}.
     */
    protected void makeAttributesCurrent(String attrType)
    {
        BalloonAttributes attrs = this.getInitialBalloonAttributes();

        KMLBalloonStyle balloonStyle = (KMLBalloonStyle) this.parent.getSubStyle(new KMLBalloonStyle(null), attrType);

        String displayMode = balloonStyle.getDisplayMode();
        if (displayMode != null)
            this.setDisplayMode(displayMode);

        this.assembleBalloonAttributes(balloonStyle, attrs);
        if (balloonStyle.hasField(AVKey.UNRESOLVED))
            attrs.setUnresolved(true);
        else
            attrs.setUnresolved(false);

        if (KMLConstants.NORMAL.equals(attrType))
        {
            this.getBalloon().setAttributes(attrs);

            // Set balloon text. If the style does not provide text, set the default text, if it has not been set
            // already. We use a field to track if the default text has been set to avoid continually resetting default
            // text if the style cannot be resolved.
            String text = balloonStyle.getText();
            if (text != null)
            {
                if (this.mustAddHyperlinks(text))
                    text = this.addHyperlinks(text);

                this.getBalloon().setText(text);
                this.normalText = text;
            }
            else if (!this.usingDefaultText)
            {
                text = this.createDefaultBalloonText();
                if (this.mustAddHyperlinks(text))
                    text = this.addHyperlinks(text);

                this.getBalloon().setText(text);
                this.usingDefaultText = true;
                this.normalText = text;
            }

            if (!attrs.isUnresolved() || !balloonStyle.hasFields())
                this.normalAttributesResolved = true;
        }
        else
        {
            this.getBalloon().setHighlightAttributes(attrs);

            String text = balloonStyle.getText();
            if (this.mustAddHyperlinks(text))
                text = this.addHyperlinks(text);

            this.highlightText = text;

            if (!attrs.isUnresolved() || !balloonStyle.hasFields())
                this.highlightAttributesResolved = true;
        }
    }

    /**
     * Build a default balloon text string for the feature.
     *
     * @return Default balloon text.
     */
    protected String createDefaultBalloonText()
    {
        StringBuilder sb = new StringBuilder();

        // Create default text for features that have a description
        String name = this.parent.getName();
        String description = this.parent.getDescription();
        if (!WWUtil.isEmpty(name))
            sb.append("<b>").append(name).append("</b>");

        if (!WWUtil.isEmpty(description))
            sb.append("<br/>").append(description);

        KMLExtendedData extendedData = this.parent.getExtendedData();
        if (extendedData != null)
        {
            List<KMLData> data = extendedData.getData();
            if (data != null && !data.isEmpty())
            {
                this.createDefaultExtendedDataText(sb, data);
            }

            List<KMLSchemaData> schemaData = extendedData.getSchemaData();
            if (schemaData != null && !schemaData.isEmpty())
            {
                this.createDefaultSchemaDataText(sb, schemaData);
            }
        }

        return sb.toString();
    }

    /**
     * Build a default balloon text string for the feature's extended data. This implementation builds a simple data
     * table.
     *
     * @param sb   Extended data string will be appended to this StringBuilder.
     * @param data The feature's extended data.
     */
    protected void createDefaultExtendedDataText(StringBuilder sb, List<KMLData> data)
    {
        sb.append("<p/><table border=\"1\">");
        for (KMLData item : data)
        {
            String value = item.getValue();
            if (!WWUtil.isEmpty(value))
            {
                String name = item.getName() != null ? item.getName() : "";
                sb.append("<tr><td>$[").append(name).append("/displayName]</td><td>").append(value).append(
                    "</td></tr>");
            }
        }
        sb.append("</table>");
    }

    /**
     * Build a default balloon text string for the feature's schema data.  This implementation builds a simple data
     * table.
     *
     * @param sb   Extended data string will be appended to this StringBuilder.
     * @param data The feature's schema data.
     */
    protected void createDefaultSchemaDataText(StringBuilder sb, List<KMLSchemaData> data)
    {
        sb.append("<p/><table border=\"1\">");
        for (KMLSchemaData schemaData : data)
        {
            KMLSchema schema = (KMLSchema) this.parent.getRoot().resolveReference(schemaData.getSchemaUrl());

            for (KMLSimpleData simpleData : schemaData.getSimpleData())
            {
                String value = simpleData.getCharacters();

                if (!WWUtil.isEmpty(value))
                {
                    String dataName = simpleData.getName() != null ? simpleData.getName() : "";
                    sb.append("<tr><td>");
                    // Insert the schema name, if the schema can be resolved. Otherwise just use the data name.
                    if (schema != null && !WWUtil.isEmpty(schema.getName()) && !WWUtil.isEmpty(dataName))
                    {
                        sb.append("$[").append(schema.getName()).append("/").append(dataName).append("/displayName]");
                    }
                    else
                    {
                        sb.append(dataName);
                    }

                    sb.append("</td><td>").append(value).append("</td><td>");
                }
            }
        }
        sb.append("</table>");
    }

    /**
     * Determines if URLs in the balloon text should be converted to hyperlinks. The Google KML specification states the
     * GE will add hyperlinks to balloon text that does not contain HTML formatting. This method searches for a
     * &lt;html&gt; tag in the content to determine if the content is HTML or plain text.
     *
     * @param text Balloon text to process.
     *
     * @return True if URLs should be converted links. Returns true if a &lt;html&gt; tag is found in the text.
     */
    protected boolean mustAddHyperlinks(String text)
    {
        return text != null
            && !text.contains("<html")
            && !text.contains("<HTML");
    }

    /**
     * Add hyperlink tags to URLs in the balloon text. The text may include some simple HTML markup. This method
     * attempts to identify URLs in the text while not altering URLs that are already linked.
     * <p>
     * This method is conservative about what is identified as a URL, in order to avoid adding links to text that the
     * user did not intend to be linked. Only HTTP and HTTPS URLs are recognised, as well as text that begins with www.
     * (in which case a http:// prefix will be prepended). Some punctuation characters that are valid URL characters
     * (such as parentheses) are not treated as URL characters here because users may expect the punctuation to separate
     * the URL from text.
     *
     * @param text Text to process. Each URL in the text will be replaced with &lt;a href="url" target="_blank"&gt; url
     *             &lt;/a&gt;
     *
     * @return Text with hyperlinks added.
     */
    protected String addHyperlinks(String text)
    {
        // Regular expression to match a http(s) URL, or an entire anchor tag. Note that this does not match all valid
        // URLs. It is designed to match obvious URLs that occur in KML balloons, with minimal chance of matching text
        // the user did not intend to be a link.
        String regex =
            "<a\\s.*?</a>"               // Match all text between anchor tags
                + "|"                    // or
                + "[^'\"]"               // Non-quote (avoids matching quoted urls in code)
                + "("                    // Capture group 1
                + "(?:https?://|www\\.)" // HTTP(S) protocol or www. (non-capturing group)
                + "[a-z0-9.$%&#+/_-]+"   // Match until a non-URL character
                + ")";

        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        while (matcher.find())
        {
            // If the match is a URL then group 1 holds the matched URL. If group 1 is null then the match is an anchor
            // tag, in which case we just skip it to avoid adding links to text that is already part of a link.
            String url = matcher.group(1);
            if (url != null)
            {
                String prefix = url.toLowerCase().startsWith("www") ? "http://" : "";
                matcher.appendReplacement(sb, "<a href=\"" + prefix + "$1\" target=\"_blank\">$1</a>");
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Get the default attributes applied to the balloon. These attributes will be modified by {@link
     * #assembleBalloonAttributes(gov.nasa.worldwind.ogc.kml.KMLBalloonStyle, gov.nasa.worldwind.render.BalloonAttributes)
     * assembleBalloonAttributes} to reflect the settings in the KML <i>BalloonStyle</i>.
     *
     * @return Initial balloon attributes.
     */
    protected BalloonAttributes getInitialBalloonAttributes()
    {
        BalloonAttributes attrs;

        if (this.isHighlighted())
        {
            attrs = this.getHighlightAttributes();

            // Copy the normal attributes if there are no highlight attributes
            if (attrs == null && this.getAttributes() != null)
            {
                attrs = new BasicBalloonAttributes(this.getAttributes());
            }
        }
        else
        {
            attrs = this.getAttributes();
        }

        if (attrs == null)
            attrs = new BasicBalloonAttributes();

        return attrs;
    }

    /**
     * Apply a KML <i>BalloonStyle</i> to the balloon attributes object.
     *
     * @param style             KML style to apply.
     * @param balloonAttributes Attributes to modify.
     */
    protected void assembleBalloonAttributes(KMLBalloonStyle style, BalloonAttributes balloonAttributes)
    {
        // Attempt to use the bgColor property. This is the preferred method for encoding a BalloonStyle's background
        // color since KML 2.1, therefore we give it priority.
        String bgColor = style.getBgColor();

        // If the bgColor property is null, attempt to use the deprecated color property. color was deprecated in
        // KML 2.1, but must be supported for backward compatibility. See the KML 2.1 reference, section 7.1.3.
        if (bgColor == null)
            bgColor = style.getColor();

        if (bgColor != null)
            balloonAttributes.setInteriorMaterial(new Material(WWUtil.decodeColorABGR(bgColor)));

        String textColor = style.getTextColor();
        if (textColor != null)
            balloonAttributes.setTextColor(WWUtil.decodeColorABGR(textColor));
    }

    /**
     * Create the text decoder that will process the text in the balloon.
     *
     * @param feature Feature to decode text for.
     *
     * @return New text decoder.
     */
    protected TextDecoder createTextDecoder(KMLAbstractFeature feature)
    {
        return new KMLBalloonTextDecoder(feature);
    }

    /**
     * Get the balloon display mode, either {@link #DISPLAY_MODE_DEFAULT} or {@link #DISPLAY_MODE_HIDE}.
     *
     * @return The current display mode.
     *
     * @see #setDisplayMode(String)
     */
    public String getDisplayMode()
    {
        return this.displayMode;
    }

    /**
     * Set the balloon's display mode, either {@link #DISPLAY_MODE_DEFAULT} or {@link #DISPLAY_MODE_HIDE}. When the mode
     * is {@link #DISPLAY_MODE_HIDE}, the balloon will not be drawn.
     *
     * @param displayMode New display mode.
     *
     * @see #getDisplayMode()
     */
    public void setDisplayMode(String displayMode)
    {
        if (displayMode == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.displayMode = displayMode;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation resolves relative resource paths by calling <code>{@link
     * gov.nasa.worldwind.ogc.kml.io.KMLDoc#getSupportFilePath(String)}</code> on the parent
     * <code>KMLAbstractFeature's</code> <code>KMLDoc</code>. This is necessary to correctly resolve relative references
     * in a KMZ archive.
     * <p>
     * This returns <code>null</code> if the specified <code>address</code> is <code>null</code>.
     */
    public URL resolve(String address)
    {
        if (address == null)
            return null;

        try
        {
            // Resolve the relative path against the KMLDoc, and convert it to a URL. We use makeURL variant that
            // accepts a default protocol, because we know the path is an absolute file path. If the path does not
            // define a valid URL, makeURL returns null and the balloon treats this as an unresolved resource.
            String absolutePath = this.parent.getRoot().getKMLDoc().getSupportFilePath(address);
            if (!WWUtil.isEmpty(absolutePath))
            {
                File file = new File(absolutePath);
                return file.toURI().toURL();
            }
        }
        catch (IOException e)
        {
            Logging.logger().log(Level.WARNING, Logging.getMessage("KML.UnableToResolvePath", address), e.getMessage());
        }

        return null;
    }

    /**
     * Forward property change events to the parent KMLRoot.
     *
     * @param evt Event to forward.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        this.parent.getRoot().firePropertyChange(evt);
    }

    //***************************************************************************//
    //**********************  Balloon implementation  ***************************//
    //**************************************************************************//

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public boolean isHighlighted()
    {
        return this.getBalloon().isHighlighted();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setHighlighted(boolean highlighted)
    {
        this.getBalloon().setHighlighted(highlighted);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public boolean isAlwaysOnTop()
    {
        return this.getBalloon().isAlwaysOnTop();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.getBalloon().setAlwaysOnTop(alwaysOnTop);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public boolean isPickEnabled()
    {
        return this.getBalloon().isPickEnabled();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setPickEnabled(boolean enable)
    {
        this.getBalloon().setPickEnabled(enable);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public String getText()
    {
        return this.getBalloon().getText();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setText(String text)
    {
        this.getBalloon().setText(text);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public BalloonAttributes getAttributes()
    {
        return this.getBalloon().getAttributes();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setAttributes(BalloonAttributes attrs)
    {
        this.getBalloon().setAttributes(attrs);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public BalloonAttributes getHighlightAttributes()
    {
        return this.getBalloon().getHighlightAttributes();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setHighlightAttributes(BalloonAttributes attrs)
    {
        this.getBalloon().setHighlightAttributes(attrs);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public TextDecoder getTextDecoder()
    {
        return this.getBalloon().getTextDecoder();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setTextDecoder(TextDecoder decoder)
    {
        this.getBalloon().setTextDecoder(decoder);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Object getDelegateOwner()
    {
        return this.getBalloon().getDelegateOwner();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setDelegateOwner(Object owner)
    {
        this.getBalloon().setDelegateOwner(owner);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public boolean isVisible()
    {
        return this.getBalloon().isVisible();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setVisible(boolean visible)
    {
        this.getBalloon().setVisible(visible);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Rectangle getBounds(DrawContext dc)
    {
        return this.getBalloon().getBounds(dc);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public double getMinActiveAltitude()
    {
        return this.getBalloon().getMinActiveAltitude();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setMinActiveAltitude(double minActiveAltitude)
    {
        this.getBalloon().setMinActiveAltitude(minActiveAltitude);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public double getMaxActiveAltitude()
    {
        return this.getBalloon().getMaxActiveAltitude();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setMaxActiveAltitude(double maxActiveAltitude)
    {
        this.getBalloon().setMaxActiveAltitude(maxActiveAltitude);
    }

    //***************************************************************************//
    //**********************  AVList implementation  ***************************//
    //**************************************************************************//

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Object setValue(String key, Object value)
    {
        return this.getBalloon().setValue(key, value);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public AVList setValues(AVList avList)
    {
        return this.getBalloon().setValues(avList);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Object getValue(String key)
    {
        return this.getBalloon().getValue(key);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Collection<Object> getValues()
    {
        return this.getBalloon().getValues();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public String getStringValue(String key)
    {
        return this.getBalloon().getStringValue(key);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.getBalloon().getEntries();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public boolean hasKey(String key)
    {
        return this.getBalloon().hasKey(key);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Object removeKey(String key)
    {
        return this.getBalloon().removeKey(key);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.getBalloon().addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.getBalloon().removePropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.getBalloon().addPropertyChangeListener(listener);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.getBalloon().removePropertyChangeListener(listener);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.getBalloon().firePropertyChange(propertyName, oldValue, newValue);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.getBalloon().firePropertyChange(propertyChangeEvent);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public AVList copy()
    {
        return this.getBalloon().copy();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public AVList clearList()
    {
        return this.getBalloon().clearList();
    }
}
