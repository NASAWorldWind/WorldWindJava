/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: Pedestal.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Pedestal extends UserFacingIcon
{
    private double spacingPixels = 2d;
    private double scale = 1d;

    public Pedestal(String iconPath, Position iconPosition)
    {
        super(iconPath, iconPosition);
    }

    public double getSpacingPixels()
    {
        return spacingPixels;
    }

    public void setSpacingPixels(double spacingPixels)
    {
        this.spacingPixels = spacingPixels;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }

    /**
     * Returns an XML state document String describing the public attributes of this Pedestal.
     *
     * @return XML state document string describing this Pedestal.
     */
    public String getRestorableState()
    {
        RestorableSupport restorableSupport = null;

        // Try to parse the superclass' xml state document, if it defined one.
        String superStateInXml = super.getRestorableState();
        if (superStateInXml != null)
        {
            try
            {
                restorableSupport = RestorableSupport.parse(superStateInXml);
            }
            catch (Exception e)
            {
                // Parsing the document specified by the superclass failed.
                String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", superStateInXml);
                Logging.logger().severe(message);
            }
        }

        // Create our own state document from scratch.
        if (restorableSupport == null)
            restorableSupport = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (restorableSupport == null)
            return null;

        restorableSupport.addStateValueAsDouble("spacingPixels", this.spacingPixels);
        restorableSupport.addStateValueAsDouble("scale", this.scale);

        return restorableSupport.getStateAsXml();
    }

    /**
     * Restores publicly settable attribute values found in the specified XML state document String. The
     * document specified by <code>stateInXml</code> must be a well formed XML document String, or this will throw an
     * IllegalArgumentException. Unknown structures in <code>stateInXml</code> are benign, because they will
     * simply be ignored.
     *
     * @param stateInXml an XML document String describing a Pedestal.
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or if <code>stateInXml</code> is not
     *                                  a well formed XML document String.
     */
    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Allow the superclass to restore it's state.
        try
        {
            super.restoreState(stateInXml);
        }
        catch (Exception e)
        {
            // Superclass will log the exception.
        }

        RestorableSupport restorableSupport;
        try
        {
            restorableSupport = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        Double spacingPixelsState = restorableSupport.getStateValueAsDouble("spacingPixels");
        if (spacingPixelsState != null)
            setSpacingPixels(spacingPixelsState);

        Double scaleState = restorableSupport.getStateValueAsDouble("scale");
        if (scaleState != null)
            setScale(scaleState);
    }
}
