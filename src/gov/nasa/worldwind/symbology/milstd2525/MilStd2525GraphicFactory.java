/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.*;
import gov.nasa.worldwind.util.Logging;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Graphic factory to create tactical graphics for the MIL-STD-2525 symbol set.
 *
 * @author pabercrombie
 * @version $Id: MilStd2525GraphicFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd2525GraphicFactory implements TacticalGraphicFactory
{
    /** Map to associate MIL-STD-2525C function codes with implementation classes. */
    protected Map<String, Class> classMap = new ConcurrentHashMap<String, Class>();

    /** Create a new factory. */
    public MilStd2525GraphicFactory()
    {
        this.populateClassMap();
    }

    /** Populate the map that maps function IDs to implementation classes. */
    protected void populateClassMap()
    {
        // All point graphics are handled by one class
        this.mapClass(MilStd2525PointGraphic.class, MilStd2525PointGraphic.getSupportedGraphics());

        // Command/Control/General Maneuver

        this.mapClass(Boundary.class, Boundary.getSupportedGraphics());
        this.mapClass(PhaseLine.class, PhaseLine.getSupportedGraphics());
        this.mapClass(ForwardLineOfOwnTroops.class, ForwardLineOfOwnTroops.getSupportedGraphics());
        this.mapClass(LineOfContact.class, LineOfContact.getSupportedGraphics());
        this.mapClass(BasicArea.class, BasicArea.getSupportedGraphics());
        this.mapClass(AirfieldZone.class, AirfieldZone.getSupportedGraphics());
        this.mapClass(FortifiedArea.class, FortifiedArea.getSupportedGraphics());
        this.mapClass(WeaponsFreeZone.class, WeaponsFreeZone.getSupportedGraphics());
        this.mapClass(AviationZone.class, AviationZone.getSupportedGraphics());
        this.mapClass(Route.class, Route.getSupportedGraphics());
        this.mapClass(RoutePoint.class, RoutePoint.getSupportedGraphics());
        this.mapClass(PullUpPoint.class, PullUpPoint.getSupportedGraphics());
        this.mapClass(OffenseArea.class, OffenseArea.getSupportedGraphics());
        this.mapClass(CombatSupportArea.class, CombatSupportArea.getSupportedGraphics());
        this.mapClass(SpecialInterestArea.class, SpecialInterestArea.getSupportedGraphics());
        this.mapClass(Airhead.class, Airhead.getSupportedGraphics());
        this.mapClass(DirectionOfAttack.class, DirectionOfAttack.getSupportedGraphics());
        this.mapClass(DirectionOfAttackAviation.class, DirectionOfAttackAviation.getSupportedGraphics());
        this.mapClass(Aviation.class, Aviation.getSupportedGraphics());
        this.mapClass(Airborne.class, Airborne.getSupportedGraphics());
        this.mapClass(MainAttack.class, MainAttack.getSupportedGraphics());
        this.mapClass(AttackRotaryWing.class, AttackRotaryWing.getSupportedGraphics());
        this.mapClass(SupportingAttack.class, SupportingAttack.getSupportedGraphics());
        this.mapClass(Dummy.class, Dummy.getSupportedGraphics());
        this.mapClass(SupportByFirePosition.class, SupportByFirePosition.getSupportedGraphics());
        this.mapClass(Ambush.class, Ambush.getSupportedGraphics());
        this.mapClass(ForwardEdgeOfBattleArea.class, ForwardEdgeOfBattleArea.getSupportedGraphics());
        this.mapClass(BattlePosition.class, BattlePosition.getSupportedGraphics());
        this.mapClass(PrincipleDirectionOfFire.class, PrincipleDirectionOfFire.getSupportedGraphics());
        this.mapClass(Encirclement.class, Encirclement.getSupportedGraphics());
        this.mapClass(SearchArea.class, SearchArea.getSupportedGraphics());
        this.mapClass(InfiltrationLane.class, InfiltrationLane.getSupportedGraphics());
        this.mapClass(AdvanceForFeint.class, AdvanceForFeint.getSupportedGraphics());
        this.mapClass(DirectionOfAttackForFeint.class, DirectionOfAttackForFeint.getSupportedGraphics());
        this.mapClass(HoldingLine.class, HoldingLine.getSupportedGraphics());
        this.mapClass(LimitedAccessArea.class, LimitedAccessArea.getSupportedGraphics());

        // Mobility/survivability

        this.mapClass(MinimumSafeDistanceZones.class, MinimumSafeDistanceZones.getSupportedGraphics());
        this.mapClass(FilledArea.class, FilledArea.getSupportedGraphics());
        this.mapClass(DoseRateContourLine.class, DoseRateContourLine.getSupportedGraphics());

        // Fire support

        this.mapClass(RectangularTarget.class, RectangularTarget.getSupportedGraphics());
        this.mapClass(LinearTarget.class, LinearTarget.getSupportedGraphics());
        this.mapClass(RectangularFireSupportArea.class, RectangularFireSupportArea.getSupportedGraphics());
        this.mapClass(CircularFireSupportArea.class, CircularFireSupportArea.getSupportedGraphics());
        this.mapClass(IrregularFireSupportArea.class, IrregularFireSupportArea.getSupportedGraphics());
        this.mapClass(Smoke.class, Smoke.getSupportedGraphics());
        this.mapClass(CircularRangeFan.class, CircularRangeFan.getSupportedGraphics());
        this.mapClass(SectorRangeFan.class, SectorRangeFan.getSupportedGraphics());
        this.mapClass(CircularPositionArea.class, CircularPositionArea.getSupportedGraphics());
        this.mapClass(RectangularPositionArea.class, RectangularPositionArea.getSupportedGraphics());
        this.mapClass(GroupOfTargets.class, GroupOfTargets.getSupportedGraphics());
        this.mapClass(AttackByFirePosition.class, AttackByFirePosition.getSupportedGraphics());
        this.mapClass(FireSupportLine.class, FireSupportLine.getSupportedGraphics());
        this.mapClass(MunitionFlightPath.class, MunitionFlightPath.getSupportedGraphics());
    }

    /**
     * Specifies the class that the factory will instantiate for a given symbol identifier. This will override the
     * default implementation class, and can be used to customize the behavior of the factory without needing to extend
     * the class.
     *
     * @param sidc  Masked symbol identifier.
     * @param clazz Implementation class. This class must have a constructor that accepts a string argument.
     *
     * @see gov.nasa.worldwind.symbology.milstd2525.SymbolCode#toMaskedString()
     */
    public void setImplementationClass(String sidc, Class clazz)
    {
        this.classMap.put(sidc, clazz);
    }

    /**
     * Associate an implementation class with one or more symbol identifiers.
     *
     * @param clazz Class that implements one or more tactical graphics.
     * @param ids   Masked symbol IDs of the graphics implemented by {@code clazz}.
     */
    protected void mapClass(Class clazz, List<String> ids)
    {
        for (String sidc : ids)
        {
            this.classMap.put(sidc, clazz);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param sidc MIL-STD-2525 symbol identification code (SIDC).
     */
    @SuppressWarnings({"unchecked"})
    public MilStd2525TacticalGraphic createGraphic(String sidc, Iterable<? extends Position> positions,
        AVList modifiers)
    {
        SymbolCode symbolCode = new SymbolCode(sidc);

        Class clazz = this.getClassForCode(symbolCode);
        if (clazz == null)
        {
            return null;
        }

        if (!MilStd2525TacticalGraphic.class.isAssignableFrom(clazz))
        {
            String msg = Logging.getMessage("Symbology.CannotCast", clazz, MilStd2525TacticalGraphic.class);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        MilStd2525TacticalGraphic graphic;
        try
        {
            Constructor ct = clazz.getConstructor(String.class);
            graphic = (MilStd2525TacticalGraphic) ct.newInstance(sidc);

            if (positions != null)
            {
                graphic.setPositions(positions);
            }
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("Symbology.ExceptionCreatingGraphic", e.getMessage());
            Logging.logger().severe(msg);
            throw new WWRuntimeException(e);
        }

        if (modifiers != null)
        {
            this.setModifiers(graphic, modifiers);
        }

        return graphic;
    }

    /**
     * {@inheritDoc}
     *
     * @param sidc MIL-STD-2525 symbol identification code (SIDC).
     */
    public TacticalPoint createPoint(String sidc, Position position, AVList params)
    {
        TacticalGraphic graphic = this.createGraphic(sidc, Arrays.asList(position), params);
        if (graphic instanceof TacticalPoint)
        {
            return (TacticalPoint) graphic;
        }
        else if (graphic != null)
        {
            String className = graphic.getClass().getName();
            String msg = Logging.getMessage("Symbology.CannotCast", className, TacticalPoint.class.getName());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return null;
    }

    /** {@inheritDoc} */
    public TacticalCircle createCircle(String sidc, Position center, double radius, AVList modifiers)
    {
        TacticalGraphic graphic = this.createPoint(sidc, center, modifiers);
        if (graphic instanceof TacticalCircle)
        {
            TacticalCircle circle = (TacticalCircle) graphic;
            circle.setRadius(radius);
            return circle;
        }
        else if (graphic != null)
        {
            String className = graphic.getClass().getName();
            String msg = Logging.getMessage("Symbology.CannotCast", className, TacticalCircle.class.getName());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return null;
    }

    /** {@inheritDoc} */
    public TacticalQuad createQuad(String sidc, Iterable<? extends Position> positions, AVList modifiers)
    {
        TacticalGraphic graphic = this.createGraphic(sidc, positions, modifiers);
        if (graphic instanceof TacticalQuad)
        {
            return (TacticalQuad) graphic;
        }
        else if (graphic != null)
        {
            String className = graphic.getClass().getName();
            String msg = Logging.getMessage("Symbology.CannotCast", className, TacticalQuad.class.getName());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return null;
    }

    /** {@inheritDoc} */
    public TacticalRoute createRoute(String sidc, Iterable<? extends TacticalPoint> controlPoints,
        AVList modifiers)
    {
        TacticalGraphic graphic = this.createGraphic(sidc, null, modifiers);
        if (graphic instanceof TacticalRoute)
        {
            TacticalRoute route = (TacticalRoute) graphic;
            route.setControlPoints(controlPoints);
            return route;
        }
        else if (graphic != null)
        {
            String className = graphic.getClass().getName();
            String msg = Logging.getMessage("Symbology.CannotCast", className, TacticalRoute.class.getName());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return null;
    }

    /** {@inheritDoc} */
    public boolean isSupported(String sidc)
    {
        SymbolCode symbolCode = new SymbolCode(sidc);
        String key = symbolCode.toMaskedString();
        return this.classMap.containsKey(key);
    }

    protected void setModifiers(TacticalGraphic graphic, AVList props)
    {
        for (Map.Entry<String, Object> entry : props.getEntries())
        {
            graphic.setModifier(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get the implementation class that implements a particular graphic.
     *
     * @param symbolCode Parsed SIDC that identifies the graphic.
     *
     * @return The implementation class for the specified SIDC, or {@code null} if no implementation class is found.
     */
    protected Class getClassForCode(SymbolCode symbolCode)
    {
        String key = symbolCode.toMaskedString();
        return key != null ? this.classMap.get(key) : null;
    }
}
