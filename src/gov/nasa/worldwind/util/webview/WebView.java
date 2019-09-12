/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 * WebView provides an interface for loading web content, laying out and rendering the content as an OpenGL texture, and
 * interacting with the rendered content. This functionality is divided into four main tasks: <ul> <li>Loading web
 * content into a WebView's frame.</li> <li>Sending input events to the WebView's frame.</li> <li>Receiving information
 * about links in the WebView's frame.</li> <li>Receiving a rendered representation of the WebView's frame.</li> </ul>
 * <p>
 * A WebView is configured by specifying its text content and the size of the WebView's frame. The text may be an HTML
 * document, an HTML fragment, simple text, <code>null</code>, or another text format supported by the implementation.
 * The size of the WebView's frame is specified in pixels, and may not exceed an implementation-defined maximum. Most
 * implementations define the maximum value to be 4096 - the maximum texture size on most platforms.
 * <p>
 * The user can interact with the WebView using the mouse and keyboard. The application must send input events to the
 * WebView's frame because WebView is not associated with any windowing system. Input events are received and processed
 * in an implementation-defined manner. Applications can suppress WebView navigation by drawing the link rectangles
 * during the picking phase, and consuming link clicked <code>SelectEvents</code> before they are sent to the WebView.
 * <p>
 * The WebView provides a representation of itself as an OpenGL texture. On machines that support non-power-of-two sized
 * textures, this texture has dimensions equal to the WebView's frame size. Otherwise, the texture's dimensions are the
 * smallest power-of-two that captures the WebView's frame size. The WebView's texture representation is standard
 * two-dimensional OpenGL texture that may be mapped onto any OpenGL primitive using texture coordinates.
 * <p>
 * When the WebView's texture representation changes as a result of an internal event it fires a property change event
 * with the key {@link gov.nasa.worldwind.avlist.AVKey#REPAINT}. This can happen from web content loading, user
 * interaction, or from a programmatic change such as JavaScript.
 *
 * @author dcollins
 * @version $Id: WebView.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface WebView extends AVList, Disposable
{
    /**
     * Specifies this <code>WebView's</code> HTML content as a string. The specified <code>htmlString</code> may be one
     * of the following:
     * <ul> <li>HTML document</li> <li>HTML fragment</li> <li>Simple text</li> <li><code>null</code></li> </ul>
     * <p>
     * The WebView displays nothing if <code>htmlString</code> is <code>null</code> or empty. If the
     * <code>htmlString</code> contains relative paths, they are not resolved and are interpreted as unresolved
     * references.
     * <p>
     * If the application sends input events to the WebView, the user may navigate away from the specified HTML content
     * by interacting with links or buttons in the content.
     *
     * @param htmlString the WebView's HTML text content, or <code>null</code> to display an empty
     *                   <code>WebView</code>.
     */
    void setHTMLString(String htmlString);

    /**
     * Specifies this <code>WebView's</code> HTML content as a string. The specified <code>htmlString</code> may be one
     * of the following:
     * <ul> <li>HTML document</li> <li>HTML fragment</li> <li>Simple text</li> <li><code>null</code></li> </ul>
     * <p>
     * The WebView displays nothing if <code>htmlString</code> is <code>null</code> or empty. The <code>baseURL</code>
     * is used to resolve relative paths in the specified <code>htmlString</code>. If the <code>baseURL</code> is
     * <code>null</code>, relative paths are not resolved and are interpreted as unresolved references.
     * <p>
     * If the application sends input events to the WebView, the user may navigate away from the specified HTML content
     * by interacting with links or buttons in the content. Once the user navigates away from the content specified
     * here, the <code>htmlString</code> and <code>baseURL</code> are no longer used.
     *
     * @param htmlString the WebView's HTML text content, or <code>null</code> to display an empty
     *                   <code>WebView</code>.
     * @param baseURL    the <code>URL</code> used to resolve relative paths in the <code>htmlString</code>, or
     *                   <code>null</code> to indicate that relative paths should be interpreted as unresolved
     *                   references.
     */
    void setHTMLString(String htmlString, URL baseURL);

    /**
     * Specifies this <code>WebView's</code> HTML content as a string. The specified <code>htmlString</code> may be one
     * of the following:
     * <ul> <li>HTML document</li> <li>HTML fragment</li> <li>Simple text</li> <li><code>null</code></li> </ul>
     * <p>
     * The WebView displays nothing if <code>htmlString</code> is <code>null</code> or empty. The
     * <code>WebResourceResolver</code> is used to resolve relative paths in the specified <code>htmlString</code>. If
     * the <code>WebResourceResolver</code> is <code>null</code>, relative paths are not resolved and are interpreted as
     * unresolved references.
     * <p>
     * If the application sends input events to the WebView, the user may navigate away from the specified HTML content
     * by interacting with links or buttons in the content. Once the user navigates away from the content specified
     * here, the <code>htmlString</code> and <code>resourceResolver</code> are no longer used.
     *
     * @param htmlString       the WebView's HTML text content, or <code>null</code> to display an empty
     *                         <code>WebView</code>.
     * @param resourceResolver the <code>WebResourceResolver</code> used to resolve relative paths in the
     *                         <code>htmlString</code>, or <code>null</code> to indicate that relative paths should be
     *                         interpreted as unresolved references.
     */
    void setHTMLString(String htmlString, WebResourceResolver resourceResolver);

    /**
     * Returns the size in pixels of this WebView's frame. This returns <code>null</code> if this WebView's frame size
     * is unspecified.
     *
     * @return the size of this WebView's frame in pixels, or <code>null</code> if it's unspecified.
     *
     * @see #setFrameSize(java.awt.Dimension)
     */
    Dimension getFrameSize();

    /**
     * Specifies the size in pixels of this WebView's frame.
     *
     * @param size the size of this WebView's frame in pixels.
     *
     * @throws IllegalArgumentException if <code>size</code> is <code>null</code>, if the width or height are less than
     *                                  one, or if the width or height exceed the implementation-defined maximum.
     */
    void setFrameSize(Dimension size);

    /**
     * Returns the size in pixels of this WebView's current content. This returns <code>null</code> if the size of this
     * WebView's current content is unknown, either because it has not finished loading or because its size cannot be
     * determined. This WebView attempts to determine its current content's size each time an HTML frame is loaded and
     * its layout is performed. If the content is plain text or has no HTML frames, this WebView determines its content
     * size after the text is loaded.
     * <p>
     * The returned size changes as this WebView navigates to new content or navigates within its history, and always
     * reflects the size of the current content. The returned size is limited by this WebView's minimum content size.
     * See {@link #getMinContentSize()} for more information on how the minimum content size is used.
     *
     * @return the size of this WebView's content limited by the {@code minContentSize}, or <code>null</code> if this
     *         WebView's content size is unknown.
     *
     * @see #getMinContentSize()
     */
    Dimension getContentSize();

    /**
     * Returns the minimum size in pixels of this WebView's content area.
     * <p>
     * HTML content can expand to fit its frame, so it is impossible to determine the size of content without laying out
     * the content in a frame of some size. The minimum content size determines the size of the frame used to compute
     * the content layout and size. If the content is simple text, the text will wrap to the minimum content width.
     *
     * @return The minimum size of the WebView content area.
     */
    Dimension getMinContentSize();

    /**
     * Specifies the minimum size in pixels of this WebView content area. See {@link #getMinContentSize()} for more
     * information on how this value is interpreted.
     *
     * @param size Minimum size of the WebView content area.
     */
    void setMinContentSize(Dimension size);

    /**
     * Returns the URL of this WebView's current content, or <code>null</code> if the current content is the HTML string
     * specified by <code>setHTMLString</code>. The returned URL changes as this WebView navigates to new content or
     * navigates within its history, and always reflects the URL of the current content.
     *
     * @return the URL of this WebView's current content, or <code>null</code> if the current content is the HTML string
     *         specified by <code>setHTMLString</code>.
     */
    URL getContentURL();

    /**
     * Returns an iterable of <code>AVList</code> elements describing this <code>WebView's</code> visible links. The
     * returned iterable has no elements if this <code>WebView</code> has no links, or if none of the links are
     * currently in the <code>WebView's</code> visible area. Each <code>AVList</code> describes the parameters for one
     * link as follows:
     * <ul> <li><code>AVKey.URL</code> - a <code>String</code> containing the link's destination.</li>
     * <li><code>AVKey.MIME_TYPE</code> - a <code>String</code> mime type describing the content type of the link's
     * destination.</li> <li><code>AVKey.TARGET</code> - the link's target frame, one of the following: <code>_blank,
     * _self, _parent, _top</code>. See the <a href="http://www.w3.org/TR/html401/types.html#type-frame-target">W3C
     * documentation</a> on frame target names.</li> <li><code>AVKey.BOUNDS</code> - a <code>java.awt.Rectangle</code>
     * representing the link's bounding rectangle.</li> <li><code>AVKey.RECTANGLES</code> - an array of one or more
     * <code>java.awt.Rectangle</code> instances representing the link's separate pickable rectangles.</li> </ul>
     * <p>
     * The link rectangles are in the <code>WebView</code>'s local coordinate system, and are clipped to the
     * <code>WebView's</code> visible area. The <code>WebView</code>'s coordinate system has its origin in the lower
     * left corner with the X-axis pointing right and the Y-axis pointing up. Multi-line links are represented as one
     * <code>AVList</code> with multiple pickable rectangles.
     *
     * @return an <code>Iterable</code> of <code>AVList</code> parameters describing this <code>WebView's</code> visible
     *         links.
     */
    Iterable<AVList> getLinks();

    /**
     * Returns a layed out and rendered representation of the WebView's content as a {@link
     * gov.nasa.worldwind.render.WWTexture}. The texture's image source is the WebView, and its dimensions are large
     * enough to capture the WebView's frame size (see {@link #setFrameSize(java.awt.Dimension)}.
     * <p>
     * On machines that support non-power-of-two sized textures, the texture's dimensions are always equal to the
     * WebView's frame size. Otherwise, the texture's dimensions are the smallest power-of-two that captures the
     * WebView's frame size.
     *
     * @param dc The draw context the WebView is associated with.
     *
     * @return a rendered representation of the WebView's frame as a <code>WWTexture</code>.
     */
    WWTexture getTextureRepresentation(DrawContext dc);

    /**
     * Called when this WebView is activated or deactivated. The WebView only receives input events when it is active.
     *
     * @param active <code>true</code> if this WebView is being activated. <code>false</code> if this WebView is being
     *               deactivated.
     *
     * @see #sendEvent
     */
    void setActive(boolean active);

    /**
     * Indicates whether or not this WebView is active. The WebView only receives input events when it is active.
     *
     * @return <code>true</code> if this WebView is active, <code>false</code> if not.
     */
    boolean isActive();

    /**
     * Sends the specified input event to the WebView. Which events the WebView's responds to and how it responds is
     * implementation-defined. Typical implementations respond to {@link java.awt.event.KeyEvent}, {@link
     * java.awt.event.MouseEvent}, and {@link java.awt.event.MouseWheelEvent}.
     * <p>
     * The screen coordinates for a <code>MouseEvent</code> must be transformed into the WebView's local coordinate
     * system, which has its origin in the lower left corner with the X-axis pointing right and the Y-axis pointing up.
     * <p>
     * This does nothing if the specified event is <code>null</code>.
     * <p>
     * Users of the WebView must call {@link #setActive} before sending input events to the WebView. The WebView can be
     * activated and deactivated any number of times. For example, a controller might call <code>setActive(true)</code>
     * when the mouse enters the WebView texture, and call <code>setActive(false)</code> when the mouse exits the
     * texture.
     *
     * @param event the event to send.
     *
     * @see #setActive
     */
    void sendEvent(InputEvent event);

    /** Navigate the WebView to the previous page in the browsing history. Has no effect if there is no previous page. */
    void goBack();

    /** Navigate the WebView to the next page in the browsing history. Has no effect if there is no next page. */
    void goForward();

    /**
     * Specifies a background color for the WebView. Implementations are not required to support setting the background
     * color. Implementations that do not support setting the background color may simply ignore attempts to set the
     * color.
     *
     * @param color Color to apply to the background.
     */
    void setBackgroundColor(Color color);

    /**
     * Indicates the current WebView background color. Implementations are not required to support setting the
     * background color.
     *
     * @return the color applied to the WebView background, or <code>null</code> if no color has been set.
     */
    Color getBackgroundColor();
}
