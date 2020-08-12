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
package gov.nasa.worldwind.util;

import com.jogamp.nativewindow.*;
import com.jogamp.opengl.*;
import java.util.List;

/**
 * BasicGLCapabilitiesChooser provides an implementation of {@link com.jogamp.opengl.GLCapabilitiesChooser} for use
 * with WorldWindow implementations (for example, WorldWindowGLCanvas and WorldWindowGLJPanel).
 * <p>
 * BasicGLCapabilitiesChooser extends the behavior of the default GLCapabilitiesChooser by implementing a fallback
 * behavior when device supported stereo is requested but is not supported by the hardware. In this case,
 * BasicGLCapabilitiesChooser attempts to find a match to the desired capabilities, but without device supported stereo.
 * When the application does not request device supported stereo, BasicGLCapabilitiesChooser provides the same behavior
 * as DefaultGLCapabilitiesChooser.
 *
 * @author dcollins
 * @version $Id: BasicGLCapabilitiesChooser.java 1739 2013-12-04 03:38:19Z dcollins $
 */
public class BasicGLCapabilitiesChooser extends DefaultGLCapabilitiesChooser
{
    /** Constructs a new <code>BasicGLCapabilitiesChooser</code>, but otherwise does nothing. */
    public BasicGLCapabilitiesChooser()
    {
    }

    /**
     * Overridden to provide a fallback behavior when device supported stereo requested but is not supported by the
     * hardware. When the <code>desired</code> GL capabilities does not specify device supported stereo, this calls
     * DefaultGLCapabilitiesChooser.chooseCapabilities.
     *
     * @param desired                       the desired GL capabilities.
     * @param available                     the list of available GL capabilities on the graphics device.
     * @param windowSystemRecommendedChoice an index into the list of available GL capabilities indicating the window
     *                                      system's recommended capabilities, or -1 to indicate no recommendation.
     *
     * @return an index into the list of available GL capabilities.
     */
    @Override
    public int chooseCapabilities(CapabilitiesImmutable desired,
        List<? extends CapabilitiesImmutable> available, int windowSystemRecommendedChoice)
    {
        if (desired instanceof GLCapabilities && ((GLCapabilities) desired).getStereo())
        {
            return this.chooseStereoCapabilities(desired, available, windowSystemRecommendedChoice);
        }

        return super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
    }

    /**
     * Attempts to use the superclass functionality to find a match to the desired GL capabilities in the list of
     * available capabilities. This assumes that the desired GL capabilities request device supported stereo. If the
     * superclass cannot find a match, this attempts to find a match to the desired capabilities, but without device
     * supported stereo.
     *
     * @param desired                       the desired GL capabilities.
     * @param available                     the list of available GL capabilities on the graphics device.
     * @param windowSystemRecommendedChoice an index into the list of available GL capabilities indicating the window
     *                                      system's recommended capabilities, or -1 to indicate no recommendation.
     *
     * @return an index into the list of available GL capabilities.
     */
    protected int chooseStereoCapabilities(CapabilitiesImmutable desired,
        List<? extends CapabilitiesImmutable> available, int windowSystemRecommendedChoice)
    {
        try
        {
            return super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
        }
        catch (NativeWindowException e)  // superclass cannot find a match
        {
            Logging.logger().warning(Logging.getMessage("generic.StereoNotSupported"));
        }

        GLCapabilities fallback = (GLCapabilities) desired.cloneMutable();
        fallback.setStereo(false);

        return super.chooseCapabilities(fallback, available, windowSystemRecommendedChoice);
    }
}
