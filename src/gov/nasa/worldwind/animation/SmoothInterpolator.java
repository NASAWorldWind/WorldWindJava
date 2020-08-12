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
package gov.nasa.worldwind.animation;

/**
 * @author jym
 * @version $Id: SmoothInterpolator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SmoothInterpolator extends ScheduledInterpolator
{
    private boolean useMidZoom = true;
    private final int MAX_SMOOTHING = 3;
    private final double START = this.useMidZoom ? 0.0 : 0.6;
    private final double STOP = 1.0;

    public SmoothInterpolator(long lengthMillis)
    {
        super(lengthMillis);
    }

    public double nextInterpolant()
    {
        double interpolant = super.nextInterpolant();
        return basicInterpolant(interpolant,
            this.START, this.STOP, this.MAX_SMOOTHING);
    }

    protected static double basicInterpolant(double interpolant, double startInterpolant,
        double stopInterpolant,
        int maxSmoothing)
    {
        double normalizedInterpolant = AnimationSupport.interpolantNormalized(interpolant, startInterpolant,
            stopInterpolant);
        return AnimationSupport.interpolantSmoothed(normalizedInterpolant, maxSmoothing);
    }

     // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    // Map amount range [startAmount, stopAmount] to [0, 1] when amount is inside range.
    
}
