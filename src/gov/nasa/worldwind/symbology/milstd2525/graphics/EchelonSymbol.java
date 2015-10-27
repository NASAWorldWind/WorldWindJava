/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.AbstractTacticalSymbol;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Tactical symbol implementation to render the echelon modifier as part of a tactical graphic.
 *
 * @author pabercrombie
 * @version $Id: EchelonSymbol.java 2196 2014-08-06 19:42:15Z tgaskins $
 */
public class EchelonSymbol extends AbstractTacticalSymbol
{
    protected static final Offset DEFAULT_OFFSET = Offset.fromFraction(0.5, -0.5);

    /** Identifier for this graphic. */
    protected String sidc;
    /** The label is drawn along a line from the label position to the orientation position. */
    protected Position orientationPosition;

    /** Rotation to apply to symbol, computed each frame. */
    protected Angle rotation;

    /**
     * Constructs a new symbol with the specified position. The position specifies the latitude, longitude, and altitude
     * where this symbol is drawn on the globe. The position's altitude component is interpreted according to the
     * altitudeMode.
     *
     * @param sidc MIL-STD-2525C sidc code.
     *
     * @throws IllegalArgumentException if {@code sidc} is null, or does not contain a value for the Echelon field.
     */
    public EchelonSymbol(String sidc)
    {
        super();

        if (sidc == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        SymbolCode symbolCode = new SymbolCode(sidc);
        String echelon = symbolCode.getEchelon();
        if (SymbolCode.isFieldEmpty(echelon))
        {
            String msg = Logging.getMessage("Symbology.InvalidSymbolCode", sidc);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.sidc = sidc;

        this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        this.setOffset(DEFAULT_OFFSET);

        // Configure this tactical point graphic's icon retriever and modifier retriever with either the
        // configuration value or the default value (in that order of precedence).
        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
            MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        this.setIconRetriever(new MilStd2525ModifierRetriever(iconRetrieverPath));
    }

    /**
     * Indicates the orientation position. The label oriented on a line drawn from the label's position to the
     * orientation position.
     *
     * @return Position used to orient the label. May be null.
     */
    public Position getOrientationPosition()
    {
        return this.orientationPosition;
    }

    /**
     * Specifies the orientation position. The label is oriented on a line drawn from the label's position to the
     * orientation position. If the orientation position is null then the label is drawn with no rotation.
     *
     * @param orientationPosition Draw label oriented toward this position.
     */
    public void setOrientationPosition(Position orientationPosition)
    {
        this.orientationPosition = orientationPosition;
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        SymbolCode symbolCode = new SymbolCode(this.sidc);
        String echelon = symbolCode.getEchelon();

        return "-" + echelon;
    }

    @Override
    protected AVList assembleIconRetrieverParameters(AVList params)
    {
        params = super.assembleIconRetrieverParameters(params);

        if (params == null)
            params = new AVListImpl();

        Material material = this.getActiveAttributes().getTextModifierMaterial();
        if (material != null)
            params.setValue(AVKey.COLOR, material.getDiffuse());

        return params;
    }

    @Override
    protected void computeTransform(DrawContext dc, OrderedSymbol osym)
    {
        super.computeTransform(dc, osym);

        boolean orientationReversed = false;
        if (this.orientationPosition != null)
        {
            // TODO apply altitude mode to orientation position
            // Project the orientation point onto the screen
            Vec4 orientationPlacePoint = dc.computeTerrainPoint(this.orientationPosition.getLatitude(),
                this.orientationPosition.getLongitude(), 0);
            Vec4 orientationScreenPoint = dc.getView().project(orientationPlacePoint);

            this.rotation = this.computeRotation(osym.screenPoint, orientationScreenPoint);

            orientationReversed = (osym.screenPoint.x <= orientationScreenPoint.x);
        }

        if (this.getOffset() != null && this.iconRect != null)
        {
            Point2D offsetPoint = this.getOffset().computeOffset(this.iconRect.getWidth(), this.iconRect.getHeight(),
                null, null);

            // If a rotation is applied to the image, then rotate the offset as well. An offset in the x direction
            // will move the image along the orientation line, and a offset in the y direction will move the image
            // perpendicular to the orientation line.
            if (this.rotation != null)
            {
                double dy = offsetPoint.getY();

                // If the orientation is reversed we need to adjust the vertical offset to compensate for the flipped
                // image. For example, if the offset normally aligns the top of the image with the place point then without
                // this adjustment the bottom of the image would align with the place point when the orientation is
                // reversed.
                if (orientationReversed)
                {
                    dy = -(dy + this.iconRect.getHeight());
                }

                Vec4 pOffset = new Vec4(offsetPoint.getX(), dy);
                Matrix rot = Matrix.fromRotationZ(this.rotation.multiply(-1));

                pOffset = pOffset.transformBy3(rot);

                offsetPoint = new Point((int) pOffset.getX(), (int) pOffset.getY());
            }

            osym.dx = -this.iconRect.getX() - offsetPoint.getX();
            osym.dy = -(this.iconRect.getY() - offsetPoint.getY());
        }
        else
        {
            osym.dx = 0;
            osym.dy = 0;
        }
    }

    /** Overridden to apply rotation. */
    @Override
    protected void drawIcon(DrawContext dc)
    {
        boolean matrixPushed = false;
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        try
        {
            if (this.rotation != null)
            {
                gl.glPushMatrix();
                gl.glRotated(this.rotation.degrees, 0, 0, 1);
                matrixPushed = true;
            }

            // Don't depth buffer the echelon symbol. It should behave the same as text.
            gl.glDisable(GL.GL_DEPTH_TEST);

            super.drawIcon(dc);
        }
        finally
        {
            gl.glEnable(GL.GL_DEPTH_TEST);

            if (matrixPushed)
                gl.glPopMatrix();
        }
    }

    /**
     * Compute the amount of rotation to apply to a label in order to keep it oriented toward its orientation position.
     *
     * @param screenPoint            Geographic position of the text, projected onto the screen.
     * @param orientationScreenPoint Orientation position, projected onto the screen.
     *
     * @return The rotation angle to apply when drawing the label.
     */
    protected Angle computeRotation(Vec4 screenPoint, Vec4 orientationScreenPoint)
    {
        // Determine delta between the orientation position and the label position
        double deltaX = screenPoint.x - orientationScreenPoint.x;
        double deltaY = screenPoint.y - orientationScreenPoint.y;

        if (deltaX != 0)
        {
            double angle = Math.atan(deltaY / deltaX);
            return Angle.fromRadians(angle);
        }
        else
        {
            return Angle.POS90; // Vertical label
        }
    }
}
