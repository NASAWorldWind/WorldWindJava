/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 *
 * Provides classes for loading web content, laying out and rendering the content as an OpenGL texture, and interacting
 * with the rendered content.
 * <p>
 * {@link gov.nasa.worldwind.render.AbstractBrowserBalloon} uses WebView to implement a balloon that contains web
 * content. See {@link gov.nasa.worldwindx.examples.Balloons} for an example of usage.
 * <p>
 * To use WebView:
 * <ul>
 * <li>Create an instance of {@link gov.nasa.worldwind.util.webview.WebView} using {@link
 * gov.nasa.worldwind.util.webview.WebViewFactory}
 * </li>
 * <li>Loading content into the WebView using {@link gov.nasa.worldwind.util.webview.WebView#setHTMLString}</li>
 * <li>Load the WebView into an OpenGL texture using {@link
 * gov.nasa.worldwind.util.webview.WebView#getTextureRepresentation}
 * </li>
 * <li>Draw the texture in WorldWind</li>
 * <li>(Optional) Forward user input to the WebView to make the page interactive using {@link
 * gov.nasa.worldwind.util.webview.WebView#sendEvent}
 * </li>
 * </ul>
 *
 *
 */
package gov.nasa.worldwind.util.webview;
