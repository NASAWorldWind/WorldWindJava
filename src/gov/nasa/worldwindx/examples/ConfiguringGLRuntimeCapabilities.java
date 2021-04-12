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
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * Illustrates how to specify the OpenGL features WorldWind uses by configuring a <code>{@link
 * GLRuntimeCapabilities}</code>. By defining a custom <code>{@link gov.nasa.worldwind.WorldWindowGLDrawable}</code> and
 * setting properties on the GLRuntimeCapabilities attached to its SceneController, applications can specify which
 * OpenGL features WorldWind uses during rendering.
 *
 * @author dcollins
 * @version $Id: ConfiguringGLRuntimeCapabilities.java 3432 2015-10-01 19:40:30Z dcollins $
 */
public class ConfiguringGLRuntimeCapabilities extends ApplicationTemplate
{
    static
    {
        // Modify the configuration to specify our custom WorldWindowGLDrawable. Normally, an application would specify
        // this in a configuration file. For example, via the standard WorldWind XML configuration file:
        //
        //    <WorldWindConfiguration version="1">
        //        ...
        //        <Property name="gov.nasa.worldwind.avkey.WorldWindowClassName" value="MyGLAutoDrawableClassName"/>
        //        ...
        //    </WorldWindConfiguration>
        //
        // Or via the legacy WorldWind properties file:
        //
        //    ...
        //    gov.nasa.worldwind.avkey.WorldWindowClassName=MyGLAutoDrawableClassName
        //    ...
        //

        Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, MyGLAutoDrawable.class.getName());
    }

    /**
     * Subclass of {@link gov.nasa.worldwind.WorldWindowGLAutoDrawable} which overrides the method {@link
     * gov.nasa.worldwind.WorldWindowGLAutoDrawable#init(com.jogamp.opengl.GLAutoDrawable)} to configure the OpenGL
     * features used by the WorldWind SDK.
     */
    public static class MyGLAutoDrawable extends WorldWindowGLAutoDrawable
    {
        /** Constructs a new MyGLAutoDrawable, but otherwise does nothing. */
        public MyGLAutoDrawable()
        {
        }

        /**
         * Overridden to configure the OpenGL features used by the WorldWind SDK. See {@link
         * com.jogamp.opengl.GLEventListener#init(GLAutoDrawable)}.
         *
         * @param glAutoDrawable the drawable
         */
        public void init(GLAutoDrawable glAutoDrawable)
        {
            // Invoked when the GL context changes. The host machine capabilities may have changed, so re-configure the
            // OpenGL features used by the WorldWind SDK.
            super.init(glAutoDrawable);
            this.configureGLRuntimeCaps();
        }

        /** Configures the OpenGL runtime features used by the WorldWind SDK. */
        protected void configureGLRuntimeCaps()
        {
            // Get a reference to the OpenGL Runtime Capabilities associated with this WorldWindow's SceneController.
            SceneController sc = this.getSceneController();
            if (sc == null)
                return;

            // Note: if your application uses a WWJ version prior to SVN revision #12956, then replace any calls to
            // SceneController.getGLRuntimeCapabilities() with
            //  SceneController.getDrawContext().getGLRuntimeCapabilities().

            GLRuntimeCapabilities glrc = sc.getGLRuntimeCapabilities();
            if (glrc == null)
            {
                String message = Logging.getMessage("nullValue.GLRuntimeCapabilitiesIsNull");
                Logging.logger().warning(message);
                return;
            }

            // Configure which OpenGL features may be used by the WorldWind SDK. Configuration values for features
            // which are not available on the host machine are ignored. This example shows configuration of the OpenGL
            // framebuffer objects feature.
            glrc.setFramebufferObjectEnabled(this.isEnableFramebufferObjects());
        }

        /**
         * Returns true if the WorldWind SDK should enable use of OpenGL framebuffer objects (if available), and false
         * otherwise.
         *
         * @return true ot enable use of GL framebuffer objects; false otherwise.
         */
        protected boolean isEnableFramebufferObjects()
        {
            // Applications inject their logic for determining whether or not to enable use of OpenGL framebuffer
            // objects in the WorldWind SDK. If OpenGL framebuffer objects are not available on the host machine,
            // this setting is ignored.
            return false;
        }
    }

    public static void main(String[] args)
    {
        start("WorldWind Configuring GL Runtime Capabilities", AppFrame.class);
    }
}
