/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.util.Logging;

import java.awt.geom.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: MilStd2525Util.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd2525Util
{
    protected static final Offset CLOVER_OFFSET = Offset.fromFraction(0.0625, 0.0625);
    protected static final Size CLOVER_SIZE = Size.fromFraction(0.890625, 0.890625);
    protected static final Offset CLOVER_C2_HQ_OFFSET = Offset.fromFraction(0.0, -0.0546875);

    protected static final Offset CLOVER_UP_OFFSET = Offset.fromFraction(0.03125, 0.1875);
    protected static final Size CLOVER_UP_SIZE = Size.fromFraction(0.9375, 0.8046875);

    protected static final Offset CLOVER_DOWN_OFFSET = Offset.fromFraction(0.03125, 0.0078125);
    protected static final Size CLOVER_DOWN_SIZE = Size.fromFraction(0.9375, 0.8046875);

    protected static final Offset ARCH_UP_OFFSET = Offset.fromFraction(0.15625, 0.1953125);
    protected static final Size ARCH_UP_SIZE = Size.fromFraction(0.6875, 0.734375);

    protected static final Offset ARCH_DOWN_OFFSET = Offset.fromFraction(0.15625, 0.0703125);
    protected static final Size ARCH_DOWN_SIZE = Size.fromFraction(0.6875, 0.734375);

    protected static final Offset CIRCLE_OFFSET = Offset.fromFraction(0.125, 0.125);
    protected static final Size CIRCLE_SIZE = Size.fromFraction(0.75, 0.75);

    protected static final Offset RECTANGLE_OFFSET = Offset.fromFraction(0.0390625, 0.1875);
    protected static final Size RECTANGLE_SIZE = Size.fromFraction(0.921875, 0.625);
    protected static final Offset RECTANGLE_C2_HQ_OFFSET = Offset.fromFraction(0.0, -0.3);

    protected static final Offset HAT_UP_OFFSET = Offset.fromFraction(0.15625, 0.1953125);
    protected static final Size HAT_UP_SIZE = Size.fromFraction(0.6875, 0.734375);

    protected static final Offset HAT_DOWN_OFFSET = Offset.fromFraction(0.15625, 0.0703125);
    protected static final Size HAT_DOWN_SIZE = Size.fromFraction(0.6875, 0.734375);

    protected static final Offset SQUARE_OFFSET = Offset.fromFraction(0.15625, 0.15625);
    protected static final Size SQUARE_SIZE = Size.fromFraction(0.6875, 0.6875);
    protected static final Offset SQUARE_C2_HQ_OFFSET = Offset.fromFraction(0.0, -0.22728);

    protected static final Offset TENT_UP_OFFSET = Offset.fromFraction(0.15625, 0.1875);
    protected static final Size TENT_UP_SIZE = Size.fromFraction(0.6875, 0.8046875);

    protected static final Offset TENT_DOWN_OFFSET = Offset.fromFraction(0.15625, 0.0);
    protected static final Size TENT_DOWN_SIZE = Size.fromFraction(0.6875, 0.8046875);

    protected static final Offset DIAMOND_OFFSET = Offset.fromFraction(0.046875, 0.046875);
    protected static final Size DIAMOND_SIZE = Size.fromFraction(0.90625, 0.90625);
    protected static final Offset DIAMOND_C2_HQ_OFFSET = Offset.fromFraction(0.0, -0.05172);

    public static class SymbolInfo
    {
        public Offset iconOffset;
        public Size iconSize;
        public Offset offset;
        public boolean isGroundSymbol;
    }

    public static SymbolInfo computeTacticalSymbolInfo(String symbolId)
    {
        if (symbolId == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        SymbolCode symbolCode = new SymbolCode(symbolId);
        SymbolInfo symbolInfo = new SymbolInfo();

        String scheme = symbolCode.getScheme();
        String si = symbolCode.getStandardIdentity();
        String bd = symbolCode.getBattleDimension();
        String fi = symbolCode.getFunctionId();

        // Clover, Clover Up, and Clover Down.
        if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_UNKNOWN)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_UNKNOWN)))
        {
            // Clover icon.
            if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_UNKNOWN)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SURFACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SOF)))
            {
                symbolInfo.iconOffset = CLOVER_OFFSET;
                symbolInfo.iconSize = CLOVER_SIZE;
            }
            // Clover icon for Special C2 Headquarters symbols. Must appear before Clover icon for Ground symbols.
            else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_UNKNOWN)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && fi != null && fi.toUpperCase().equalsIgnoreCase("UH----"))
            {
                symbolInfo.iconOffset = CLOVER_OFFSET;
                symbolInfo.iconSize = CLOVER_SIZE;
                symbolInfo.offset = CLOVER_C2_HQ_OFFSET;
                symbolInfo.isGroundSymbol = true;
            }
            // Clover icon for Ground symbols.
            else if ((bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND))
                || (scheme != null && (scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS)
                || scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))))
            {
                symbolInfo.iconOffset = CLOVER_OFFSET;
                symbolInfo.iconSize = CLOVER_SIZE;
                symbolInfo.isGroundSymbol = true;
            }
            // Clover Up icon (Clover without a bottom leaf).
            else if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SPACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_AIR)))
            {
                symbolInfo.iconOffset = CLOVER_UP_OFFSET;
                symbolInfo.iconSize = CLOVER_UP_SIZE;
            }
            // Clover Down icon (Clover without a top leaf).
            else if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SUBSURFACE))
            {
                symbolInfo.iconOffset = CLOVER_DOWN_OFFSET;
                symbolInfo.iconSize = CLOVER_DOWN_SIZE;
            }
        }
        // Arch Up, Arch Down, Circle, and Rectangle.
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_ASSUMED_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_JOKER)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FAKER)))
        {
            // Arch Up icon.
            if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SPACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_AIR)))
            {
                symbolInfo.iconOffset = ARCH_UP_OFFSET;
                symbolInfo.iconSize = ARCH_UP_SIZE;
            }
            // Arch Down icon.
            else if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SUBSURFACE)))
            {
                symbolInfo.iconOffset = ARCH_DOWN_OFFSET;
                symbolInfo.iconSize = ARCH_DOWN_SIZE;
            }
            // Circle icon.
            else if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_UNKNOWN)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SURFACE)))
            {
                symbolInfo.iconOffset = CIRCLE_OFFSET;
                symbolInfo.iconSize = CIRCLE_SIZE;
            }
            // Circle icon for Ground Symbols.
            else if ((scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && fi != null && fi.matches("E....."))
                || (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_INTELLIGENCE)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)))
            {
                symbolInfo.iconOffset = CIRCLE_OFFSET;
                symbolInfo.iconSize = CIRCLE_SIZE;
                symbolInfo.isGroundSymbol = true;
            }
            // Rectangle icon.
            else if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SOF))
            {
                symbolInfo.iconOffset = RECTANGLE_OFFSET;
                symbolInfo.iconSize = RECTANGLE_SIZE;
            }
            // Rectangle icon for Special C2 Headquarters symbols. Must appear before Rectangle icon for Ground symbols.
            else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FRIEND)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && fi != null && fi.equalsIgnoreCase("UH----"))
            {
                symbolInfo.iconOffset = RECTANGLE_OFFSET;
                symbolInfo.iconSize = RECTANGLE_SIZE;
                symbolInfo.offset = RECTANGLE_C2_HQ_OFFSET;
                symbolInfo.isGroundSymbol = true;
            }
            // Rectangle icon for Ground symbols.
            else if ((scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && (fi == null || (fi.equalsIgnoreCase("-----") || fi.toUpperCase().matches("U.....")
                || fi.toUpperCase().matches("I....."))))
                || (scheme != null && (scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS)
                || scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))))
            {
                symbolInfo.iconOffset = RECTANGLE_OFFSET;
                symbolInfo.iconSize = RECTANGLE_SIZE;
                symbolInfo.isGroundSymbol = true;
            }
        }
        // Hat Up, Hat Down, and Square.
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_NEUTRAL)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_NEUTRAL)))
        {
            // Hat Up icon (tall rectangle without a bottom edge).
            if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SPACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_AIR)))
            {
                symbolInfo.iconOffset = HAT_UP_OFFSET;
                symbolInfo.iconSize = HAT_UP_SIZE;
            }
            // Hat Down icon (tall rectangle without a top edge).
            else if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SUBSURFACE))
            {
                symbolInfo.iconOffset = HAT_DOWN_OFFSET;
                symbolInfo.iconSize = HAT_DOWN_SIZE;
            }
            // Square icon.
            else if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_UNKNOWN)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SURFACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SOF)))
            {
                symbolInfo.iconOffset = SQUARE_OFFSET;
                symbolInfo.iconSize = SQUARE_SIZE;
            }
            // Square icon for Special C2 Headquarters symbols. Must appear before Square icon for Ground symbols.
            else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_NEUTRAL)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && fi != null && fi.equalsIgnoreCase("UH----"))
            {
                symbolInfo.iconOffset = SQUARE_OFFSET;
                symbolInfo.iconSize = SQUARE_SIZE;
                symbolInfo.offset = SQUARE_C2_HQ_OFFSET;
                symbolInfo.isGroundSymbol = true;
            }
            // Square icon for Ground symbols.
            else if ((bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND))
                || (scheme != null && (scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS)
                || scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))))
            {
                symbolInfo.iconOffset = SQUARE_OFFSET;
                symbolInfo.iconSize = SQUARE_SIZE;
                symbolInfo.isGroundSymbol = true;
            }
        }
        // Tent Up, Tent Down, Diamond.
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_HOSTILE)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_SUSPECT)))
        {
            // Tent Up icon.
            if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SPACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_AIR)))
            {
                symbolInfo.iconOffset = TENT_UP_OFFSET;
                symbolInfo.iconSize = TENT_UP_SIZE;
            }
            // Tent Down icon.
            else if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SUBSURFACE))
            {
                symbolInfo.iconOffset = TENT_DOWN_OFFSET;
                symbolInfo.iconSize = TENT_DOWN_SIZE;
            }
            // Diamond icon.
            else if (bd != null && (bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_UNKNOWN)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SEA_SURFACE)
                || bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_SOF)))
            {
                symbolInfo.iconOffset = DIAMOND_OFFSET;
                symbolInfo.iconSize = DIAMOND_SIZE;
            }
            // Diamond icon for Special C2 Headquarters symbols. Must appear before Diamond icon for Ground symbols.
            else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
                && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_HOSTILE)
                && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND)
                && fi != null && fi.equalsIgnoreCase("UH----"))
            {
                symbolInfo.iconOffset = DIAMOND_OFFSET;
                symbolInfo.iconSize = DIAMOND_SIZE;
                symbolInfo.offset = DIAMOND_C2_HQ_OFFSET;
                symbolInfo.isGroundSymbol = true;
            }
            // Diamond icon for Ground symbols.
            else if ((bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND))
                || (scheme != null && (scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS)
                || scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))))
            {
                symbolInfo.iconOffset = DIAMOND_OFFSET;
                symbolInfo.iconSize = DIAMOND_SIZE;
                symbolInfo.isGroundSymbol = true;
            }
        }

        return symbolInfo;
    }

    /**
     * Compute screen points required to draw a leader line on a tactical symbol. This method returns two points that
     * will draw a line out from the center of the symbol.
     *
     * @param dc          Current draw context.
     * @param symbolPoint Symbol position in model coordinates.
     * @param heading     Direction of movement, as a bearing clockwise from North.
     * @param length      Length of the indicator line, in pixels.
     *
     * @return List of screen points that describe the speed leader line.
     */
    public static List<? extends Point2D> computeCenterHeadingIndicatorPoints(DrawContext dc, Vec4 symbolPoint,
        Angle heading, double length)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (heading == null)
        {
            String msg = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        View view = dc.getView();
        Vec4 dir = computeDirectionOfMovement(dc, symbolPoint, heading, length);

        // Project geographic points into screen space.
        Vec4 pt1 = view.project(symbolPoint);
        Vec4 pt2 = view.project(symbolPoint.add3(dir));

        return Arrays.asList(
            new Point2D.Double(0, 0),
            new Point2D.Double(pt2.x - pt1.x, pt2.y - pt1.y));
    }

    /**
     * Compute screen points required to draw a leader line on a tactical ground symbol. This method returns three
     * points that will draw a line down from the base of the symbol and then out in the direction of movement.
     *
     * @param dc          Current draw context.
     * @param symbolPoint Symbol position in model coordinates.
     * @param heading     Direction of movement, as a bearing clockwise from North.
     * @param length      Length of the indicator line, in pixels.
     * @param frameHeight Height of the symbol's bounding rectangle, in pixels.
     *
     * @return List of screen points that describe the speed leader line.
     */
    public static List<? extends Point2D> computeGroundHeadingIndicatorPoints(DrawContext dc, Vec4 symbolPoint,
        Angle heading, double length, double frameHeight)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (heading == null)
        {
            String msg = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        View view = dc.getView();
        Vec4 dir = computeDirectionOfMovement(dc, symbolPoint, heading, length);

        // Project geographic points into screen space.
        Vec4 pt1 = view.project(symbolPoint);
        Vec4 pt2 = view.project(symbolPoint.add3(dir));

        return Arrays.asList(
            new Point2D.Double(0, 0),
            new Point2D.Double(0, -frameHeight / 2d),
            new Point2D.Double(pt2.x - pt1.x, -frameHeight / 2d + (pt2.y - pt1.y)));
    }

    /**
     * Compute a vector in the direction that a symbol is moving.
     *
     * @param dc          Current draw context.
     * @param symbolPoint Symbol position in model coordinates.
     * @param heading     Heading as a bearing clockwise from North.
     * @param length      Length of the leader line, in pixels. The computed vector will have magnitude equal to this
     *                    distance in pixels multiplied by the size of a pixel (in meters) at the position of the symbol
     *                    relative to the current view.
     *
     * @return A vector that points in the direction of a symbol's movement.
     */
    protected static Vec4 computeDirectionOfMovement(DrawContext dc, Vec4 symbolPoint, Angle heading, double length)
    {
        View view = dc.getView();
        Globe globe = dc.getGlobe();

        double pixelSize = view.computePixelSizeAtDistance(view.getEyePoint().distanceTo3(symbolPoint));

        // Compute a vector in the direction of the heading.
        Position position = globe.computePositionFromPoint(symbolPoint);
        Matrix surfaceOrientation = globe.computeSurfaceOrientationAtPosition(position);
        Vec4 dir = new Vec4(heading.sin(), heading.cos());

        return dir.transformBy3(surfaceOrientation).normalize3().multiply3(length * pixelSize);
    }

    /**
     * Determines a default color to apply to a symbol. MIL-STD-2525C section 5.5.1.1 (pg. 37) states that obstacles
     * should be displayed in green, friendly entities in black or blue, and hostile entities in red. This method return
     * green for obstacles and neutral entities, black for friendly entities, red for hostile entities, and yellow for
     * unknown and pending entities.
     *
     * @param symbolCode Symbol for which to determine color.
     *
     * @return Default material for the specified symbol.
     */
    public static Material getDefaultGraphicMaterial(SymbolCode symbolCode)
    {
        if (symbolCode == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (isObstacle(symbolCode))
            return MilStd2525Constants.MATERIAL_OBSTACLE;

        String id = symbolCode.getStandardIdentity();
        if (SymbologyConstants.STANDARD_IDENTITY_FRIEND.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_ASSUMED_FRIEND.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND.equalsIgnoreCase(id))
        {
            return MilStd2525Constants.MATERIAL_FRIEND;
        }
        else if (SymbologyConstants.STANDARD_IDENTITY_HOSTILE.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_SUSPECT.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_JOKER.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_FAKER.equalsIgnoreCase(id))
        {
            return MilStd2525Constants.MATERIAL_HOSTILE;
        }
        else if (SymbologyConstants.STANDARD_IDENTITY_NEUTRAL.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_EXERCISE_NEUTRAL.equalsIgnoreCase(id))
        {
            return MilStd2525Constants.MATERIAL_NEUTRAL;
        }

        // Default to Unknown
        return MilStd2525Constants.MATERIAL_UNKNOWN;
    }

    /**
     * Indicates whether or not a symbol code identifiers an Obstacle. Obstacles defined in the Mobility and
     * Survivability category of MIL-STD-2525C Appendix B.
     *
     * @param symbolCode Symbol code to test.
     *
     * @return True if the symbol code represents an obstacle, otherwise false.
     */
    protected static boolean isObstacle(SymbolCode symbolCode)
    {
        if (symbolCode == null)
            return false;

        String scheme = symbolCode.getScheme();
        String category = symbolCode.getCategory();
        String functionId = symbolCode.getFunctionId();

        // Obstacle function IDs start with "O".
        return SymbologyConstants.SCHEME_TACTICAL_GRAPHICS.equalsIgnoreCase(scheme)
            && SymbologyConstants.CATEGORY_MOBILITY_SURVIVABILITY.equalsIgnoreCase(category)
            && (functionId.charAt(0) == 'o' || functionId.charAt(0) == 'O');
    }
}
