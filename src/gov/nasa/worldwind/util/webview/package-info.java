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
 * @deprecated 
 */
package gov.nasa.worldwind.util.webview;
