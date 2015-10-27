/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;

import java.awt.*;
import java.awt.geom.*;

/**
 * Implementation of balloon using {@link Annotation}.
 *
 * @author pabercrombie
 * @version $Id: AbstractAnnotationBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractAnnotationBalloon extends AbstractBalloon
{
    /**
     * Create a new annotation balloon.
     *
     * @param text Balloon text. May not be null.
     */
    public AbstractAnnotationBalloon(String text)
    {
        super(text);
    }

    /** {@inheritDoc} */
    public Rectangle getBounds(DrawContext dc)
    {
        return this.getAnnotation().getBounds(dc);
    }

    /**
     * Create an annotation to render the balloon.
     *
     * @return The new annotation.
     */
    protected abstract Annotation createAnnotation();

    /**
     * Get the annotation used to render the balloon.
     *
     * @return The annotation that renders this balloon.
     */
    protected abstract Annotation getAnnotation();

    /**
     * Compute the annotation position, and set it in the annotation.
     *
     * @param dc Draw context.
     */
    protected abstract void computePosition(DrawContext dc);

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        if (!this.isVisible())
            return;

        this.determineActiveAttributes();
        this.applyAttributesToAnnotation();

        // Set position
        this.computePosition(dc);

        this.computeOffsets(dc);
        this.getAnnotation().render(dc);
    }

    /** Apply the balloon attributes to the annotation. */
    protected void applyAttributesToAnnotation()
    {
        Annotation annotation = this.getAnnotation();

        Object delegateOwner = this.getDelegateOwner();
        annotation.setDelegateOwner(delegateOwner != null ? delegateOwner : this);

        annotation.setAlwaysOnTop(this.isAlwaysOnTop());
        annotation.setPickEnabled(this.isPickEnabled());

        String text = this.getDecodedText();
        if (text != null)
            annotation.setText(text);

        annotation.setMinActiveAltitude(this.getMinActiveAltitude());
        annotation.setMaxActiveAltitude(this.getMaxActiveAltitude());

        AnnotationAttributes annotationAttrs = annotation.getAttributes();

        annotationAttrs.setHighlighted(this.isHighlighted());
        annotationAttrs.setVisible(this.isVisible());

        BalloonAttributes balloonAttrs = this.getActiveAttributes();

        if (balloonAttrs != null)
        {
            annotationAttrs.setTextColor(balloonAttrs.getTextColor());
            annotationAttrs.setBorderWidth(balloonAttrs.getOutlineWidth());
            annotationAttrs.setBorderStippleFactor(balloonAttrs.getOutlineStippleFactor());
            annotationAttrs.setBorderStipplePattern(balloonAttrs.getOutlineStipplePattern());
            annotationAttrs.setCornerRadius(balloonAttrs.getCornerRadius());
            annotationAttrs.setLeader(balloonAttrs.getLeaderShape());
            annotationAttrs.setLeaderGapWidth(balloonAttrs.getLeaderWidth());
            annotationAttrs.setFont(balloonAttrs.getFont());
            annotationAttrs.setFrameShape(balloonAttrs.getBalloonShape());
            annotationAttrs.setInsets(balloonAttrs.getInsets());

            // Configure the annotation to use the fixed size we compute and specify in computeOffsets(). Otherwise the
            // annotation adjusts its width and height to fit the text, and therefore won't always be the size we've
            // configured it to be.
            annotationAttrs.setAdjustWidthToText(AVKey.SIZE_FIXED);

            // Annotation attributes does not have an antialiasing enable/disable flag. Map the antialiasing flag to the
            // annotation attributes' antialias hint.
            annotationAttrs.setAntiAliasHint(
                balloonAttrs.isEnableAntialiasing() ? Annotation.ANTIALIAS_NICEST : Annotation.ANTIALIAS_FASTEST);

            annotationAttrs.setImageSource(balloonAttrs.getImageSource());
            annotationAttrs.setImageOffset(balloonAttrs.getImageOffset());
            annotationAttrs.setImageOpacity(balloonAttrs.getImageOpacity());
            annotationAttrs.setImageRepeat(balloonAttrs.getImageRepeat());
            annotationAttrs.setImageScale(balloonAttrs.getImageScale());

            // Annotation attributes does not have a separate interior opacity control, and do not have a flag to
            // disable drawing the interior. We use the annotation's background color to accomplish both by storing the
            // interior opacity in the background color alpha channel, and setting the background color to transparent
            // black if interior drawing is disabled.
            if (balloonAttrs.isDrawInterior() && balloonAttrs.getInteriorOpacity() > 0)
            {
                Color color = balloonAttrs.getInteriorMaterial().getDiffuse();
                double opacity = balloonAttrs.getInteriorOpacity();
                annotationAttrs.setBackgroundColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255)));
            }
            else
            {
                annotationAttrs.setBackgroundColor(new Color(0, 0, 0, 0));
            }

            // Annotation attributes does not have a separate outline opacity control, and do not have a flag to disable
            // drawing the outline. We use the annotation's background color to accomplish both by storing the outline
            // opacity in the border color alpha channel, and setting the border color to transparent black if outline
            // drawing is disabled.
            if (balloonAttrs.isDrawOutline() && balloonAttrs.getOutlineOpacity() > 0)
            {
                Color color = balloonAttrs.getOutlineMaterial().getDiffuse();
                double opacity = balloonAttrs.getOutlineOpacity();
                annotationAttrs.setBorderColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255)));
            }
            else
            {
                annotationAttrs.setBorderColor(new Color(0, 0, 0, 0));
            }
        }

        annotation.setAttributes(annotationAttrs);
    }

    /**
     * Compute the position and offsets, and set in them in the annotation.
     *
     * @param dc DrawContext in which the balloon is being rendered.
     */
    protected void computeOffsets(DrawContext dc)
    {
        Annotation annotation = this.getAnnotation();
        BalloonAttributes balloonAttrs = this.getActiveAttributes();
        AnnotationAttributes annotationAttrs = annotation.getAttributes();

        if (balloonAttrs != null)
        {
            // Compute the balloon's preferred size and the current screen viewport size.
            Dimension prefSize = annotation.getPreferredSize(dc);
            Rectangle viewport = dc.getView().getViewport();

            // Compute the balloon's current size on screen, and its offset in screen coordinates.
            Dimension screenSize = balloonAttrs.getSize().compute(prefSize.width, prefSize.height, viewport.width,
                viewport.height);
            Point2D.Double screenOffset = balloonAttrs.getOffset().computeOffset(screenSize.width, screenSize.height,
                1.0, 1.0);

            // Apply the computed balloon size and offset to the internal annotation's attributes. Adjust the screen
            // offset so that an offset of (0, 0) pixels maps to the annotation's lower left corner, and an offset of
            // (1, 1) fractions maps to the annotation's upper right corner. We do this to present a consistent meaning
            // for offset throughout the World Wind SDK. By default, annotation's offset is relative to it's bottom
            // center. We apply an additional offset of width/2 to compensate for this.
            annotationAttrs.setSize(screenSize);
            annotationAttrs.setDrawOffset(
                new Point((int) -screenOffset.x + screenSize.width / 2, (int) -screenOffset.y));
        }
    }
}
