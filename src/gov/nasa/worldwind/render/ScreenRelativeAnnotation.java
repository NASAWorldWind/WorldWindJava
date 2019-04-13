/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Provides a screen annotation positioned relatively to the window rather than absolutely. The annotation's position is
 * specified as fractional locations along the window's X and Y axes. When the window is resized the annotation is
 * repositioned according to its relative coordinates.
 * <p>
 * The annotation can be forced to remain fully visible even if its relative position would place a portion of it
 * outside the window. X and Y margins can be specified to ensure distance between the annotation's edges and the window
 * edges.
 *
 * @author tag
 * @version $Id: ScreenRelativeAnnotation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenRelativeAnnotation extends ScreenAnnotation
{
    private static Point DUMMY_POINT = new Point();

    private double xFraction;
    private double yFraction;
    private int xMargin = 5;
    private int yMargin = 5;
    private boolean keepFullyVisible = true;

    /**
     * Create an annotation with spedified text and relative position.
     *
     * @param text      the text to display in the annotation.
     * @param xFraction the relative X position of the annotation. A value of 0 indicates the window's left edge, a
     *                  value of 1 indicates its right edge. The annotation is centered horizontally on this position
     *                  prior to applying any X offest specified in the annotation's attributes.
     * @param yFraction the relative Y position of the annotation. A value of 0 indicates the window's bottom edge, a
     *                  value of 1 indicates the window's top edge. The annotation's lower edge is justified to this
     *                  position prior to applying any Y offset specified in the annotation's attributes.
     *
     * @throws IllegalArgumentException if the text string is null.
     */
    public ScreenRelativeAnnotation(String text, double xFraction, double yFraction)
    {
        super(text, DUMMY_POINT);

        this.init(xFraction, yFraction);
    }

    private void init(double xFraction, double yFraction)
    {
        this.xFraction = xFraction;
        this.yFraction = yFraction;
    }

    /**
     * Indicates whether the annotation is kept fully visible in the window.
     *
     * @return true if annotation is kept fully visible, otherwise false.
     */
    public boolean isKeepFullyVisible()
    {
        return this.keepFullyVisible;
    }

    /**
     * Specifies whether to adjust the annotation's position so that it is always fully visible when the window has
     * sufficient area to display it, even if it would be fully or partially obscured when placed according to its
     * relative coordinates.
     *
     * @param keepFullyVisible true to keep the annotation fully visible, otherwise false.
     */
    public void setKeepFullyVisible(boolean keepFullyVisible)
    {
        this.keepFullyVisible = keepFullyVisible;
    }

    /**
     * Returns the annotation's relative X position.
     *
     * @return the annotation's relative X position, as specified to the constructor or {@link #setXFraction(double)}.
     */
    public double getXFraction()
    {
        return this.xFraction;
    }

    /**
     * Specifies the annotation's relative X position. A value of 0 indicates the window's left edge, a value of 1
     * indicates its right edge.
     *
     * @param xFraction the annotation's relative X position.
     */
    public void setXFraction(double xFraction)
    {
        this.xFraction = xFraction;
    }

    /**
     * Returns the annotation's relative Y position.
     *
     * @return the annotation's relative Y position, as specified to the constructor or {@link #setYFraction(double)}.
     */
    public double getYFraction()
    {
        return this.yFraction;
    }

    /**
     * Specifies the annotation's relative Y position. A value of 0 indicates the window's lower edge, a value of 1
     * indicates its top edge.
     *
     * @param yFraction the annotation's relative Y position.
     */
    public void setYFraction(double yFraction)
    {
        this.yFraction = yFraction;
    }

    /**
     * Returns the annotation's X margin.
     *
     * @return the annotation's X margin, in pixels.
     */
    public int getXMargin()
    {
        return this.xMargin;
    }

    /**
     * Specifies the annotation's X margin, the minimum distance to maintain between the annotation's leading and
     * trailing edges and the respective window edge. Used only when the <code>keepFullyVisible</code> flag is true.
     *
     * @param xMargin the X margin, in pixels.
     */
    public void setXMargin(int xMargin)
    {
        this.xMargin = xMargin;
    }

    /**
     * Returns the annotation's Y margin.
     *
     * @return the annotation's Y margin, in pixels.
     */
    public int getYMargin()
    {
        return this.yMargin;
    }

    /**
     * Specifies the annotation's Y margin, the minimum distance to maintain between the annotation's top and bottom
     * edges and the respective window edge. Used only when the <code>keepFullyVisible</code> flag is true.
     *
     * @param yMargin the Y margin, in pixels.
     */
    public void setYMargin(int yMargin)
    {
        this.yMargin = yMargin;
    }

    /**
     * Computes and returns the screen point in pixels corresponding to the annotation's relative position coordinates
     * and the current window size.
     *
     * @return the pixel coordinates corresponding to the annotation's relative coordinates. The pixel coordinate origin
     *         is the lower left of the window.
     */
    @Override
    protected Point getScreenPoint(DrawContext dc)
    {
        Rectangle vp = dc.getView().getViewport();

        double x = vp.getX() + this.xFraction * vp.getWidth();
        double y = vp.getY() + this.yFraction * vp.getHeight();

        Point size = this.computeSize(dc);
        double[] offset = this.computeOffset(dc);

        if (this.keepFullyVisible)
        {
            // Compute the eventual screen position
            double xx = x - size.x / 2 + offset[0];
            double yy = y + offset[1];

            // See if it extends the annotation beyond the window edges, adjust the screen point if it does
            double dE = (vp.x + vp.getWidth()) - (xx + size.x + this.xMargin);
            double dN = (vp.y + vp.getHeight()) - (yy + size.y + this.yMargin);

            if (dE < 0)
                x += dE;

            if (xx < vp.x + xMargin)
                x = vp.x + this.xMargin + size.x / 2;

            if (dN < 0)
                y += dN;

            if (yy < vp.y + this.yMargin)
                y = vp.y + this.yMargin;
        }

        Point p = new Point((int) x, (int) y);
        super.setScreenPoint(p);

        return p;
    }

    //**************************************************************//
    //********************  Restorable State  **********************//
    //**************************************************************//

    /**
     * Returns an XML state document String describing the public attributes of this ScreenAnnotation.
     *
     * @return XML state document string describing this ScreenAnnotation.
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

        restorableSupport.addStateValueAsDouble("xFraction", this.getXFraction());
        restorableSupport.addStateValueAsDouble("yFraction", this.getYFraction());

        restorableSupport.addStateValueAsInteger("xMargin", this.getXMargin());
        restorableSupport.addStateValueAsInteger("yMargin", this.getYMargin());

        restorableSupport.addStateValueAsBoolean("keepFullyVisible", this.isKeepFullyVisible());

        return restorableSupport.getStateAsXml();
    }

    /**
     * Restores publicly settable attribute values found in the specified XML state document String. The document
     * specified by <code>stateInXml</code> must be a well formed XML document String, or this will throw an
     * IllegalArgumentException. Unknown structures in <code>stateInXml</code> are benign, because they will simply be
     * ignored.
     *
     * @param stateInXml an XML document String describing a ScreenAnnotation.
     *
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or if <code>stateInXml</code> is not a well
     *                                  formed XML document String.
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

        Double xFractionRS = restorableSupport.getStateValueAsDouble("xFraction");
        if (xFractionRS != null)
            this.setXFraction(xFractionRS);

        Double yFractionRS = restorableSupport.getStateValueAsDouble("yFraction");
        if (xFractionRS != null)
            this.setYFraction(yFractionRS);

        Integer xMarginRS = restorableSupport.getStateValueAsInteger("xMargin");
        if (xFractionRS != null)
            this.setXMargin(xMarginRS);

        Integer yMarginRS = restorableSupport.getStateValueAsInteger("yMargin");
        if (xFractionRS != null)
            this.setYMargin(yMarginRS);

        Boolean keepVisibleRS = restorableSupport.getStateValueAsBoolean("keepFullyVisible");
        if (keepVisibleRS != null)
            this.setKeepFullyVisible(keepVisibleRS);
    }
}
