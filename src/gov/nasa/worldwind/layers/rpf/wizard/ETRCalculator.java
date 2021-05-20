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
package gov.nasa.worldwind.layers.rpf.wizard;

/**
 * @author dcollins
 * @version $Id: ETRCalculator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ETRCalculator
{
    private int step = -1;
    private int numSteps = -1;
    private int stepsNeededForEstimate = 1;
    private long startTime = -1;
    private long updateFrequency = 1000;
    private long etr = -1;
    private long nextUpdateTime = -1;

    public ETRCalculator()
    {}

    public int getStep()
    {
        return this.step;
    }

    public void setStep(int step)
    {
        this.step = step < 0 ? -1 : step;
    }

    public int getNumSteps()
    {
        return this.numSteps;
    }

    public void setNumSteps(int numSteps)
    {
        this.numSteps = numSteps < 0 ? -1 : numSteps;
    }

    public double getStepsNeededForEstimate()
    {
        return this.stepsNeededForEstimate;
    }

    public void setStepsNeededForEstimate(int stepsNeededForEstimate)
    {
        this.stepsNeededForEstimate = stepsNeededForEstimate < 1 ? 1 : stepsNeededForEstimate;
    }

    public long getStartTime()
    {
        return this.startTime;
    }

    public void setStartTime(long timeMillis)
    {
        this.startTime = timeMillis;
        this.nextUpdateTime = this.startTime + this.updateFrequency;
    }

    public long getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    public void setUpdateFrequency(long updateFrequencyMillis)
    {
        this.updateFrequency = updateFrequencyMillis < 0 ? 0 : updateFrequencyMillis;
    }

    public long getEstimatedTimeRemaining()
    {
        if (this.step >= 0
            && this.step >= this.stepsNeededForEstimate
            && this.numSteps >= 0
            && this.startTime >= 0)
        {
            long time = System.currentTimeMillis();
            if (this.nextUpdateTime < time)
            {
                this.nextUpdateTime = time + this.updateFrequency;

                double elapsed = time - this.startTime;
                double pctComplete = this.step / (double) (this.numSteps - 1);
                this.etr = (long) (elapsed / pctComplete - elapsed);
            }
        }
        else
        {
            this.etr = -1;
        }

        return this.etr < 0 ? -1 : this.etr;
    }
}
