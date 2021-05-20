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

import com.jogamp.opengl.*;

/**
 * Illustrates how to capture OpenGL errors into the log during development. By defining a custom <code>{@link
 * gov.nasa.worldwind.WorldWindowGLDrawable}</code> and installing JOGL's {@link com.jogamp.opengl.DebugGL2},
 * applications can receive log error messages when an OpenGL error occurs. This technique is intended for use during
 * application development, and should not be used in a deployed application.
 *
 * @author dcollins
 * @version $Id: DebuggingGLErrors.java 3433 2015-10-01 19:40:46Z dcollins $
 */
public class DebuggingGLErrors extends ApplicationTemplate {

    static {
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
     * error logger.
     */
    public static class MyGLAutoDrawable extends WorldWindowGLAutoDrawable {

        /**
         * Constructs a new MyGLAutoDrawable, but otherwise does nothing.
         */
        public MyGLAutoDrawable() {
        }

        /**
         * Overridden to configure the OpenGL features used by the WorldWind SDK. See {@link
         * com.jogamp.opengl.GLEventListener#init(com.jogamp.opengl.GLAutoDrawable)}.
         *
         * @param glAutoDrawable the drawable
         */
        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            // Invoked when the GL context changes. The host machine capabilities may have changed, so re-configure the
            // OpenGL features used by the WorldWind SDK.
            super.init(glAutoDrawable);

            // Install the OpenGL error debugger. Under normal operation OpenGL errors are silently flagged. This
            // converts an OpenGL error into a Java exception indicating the problematic OpenGL method call. This
            // technique is intended for use during application development, and should not be used in a deployed
            // application.
            glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));
        }
    }

    public static void main(String[] args) {
        start("WorldWind Debugging GL Errors", AppFrame.class);
    }
}
