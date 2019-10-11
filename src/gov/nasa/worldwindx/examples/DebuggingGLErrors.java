/*
 * Copyright (C) 2015 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
