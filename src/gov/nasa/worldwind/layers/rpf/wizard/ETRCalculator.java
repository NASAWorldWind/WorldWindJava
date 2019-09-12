/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
