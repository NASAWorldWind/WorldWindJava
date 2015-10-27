/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwindx.applications.sar.render.ScreenElevationLine;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;

/**
 * Display one or two contour lines depicting lower and upper cloud ceiling around a list of positions.
 * 
 * @author Patrick Murris
 * @version $Id: CloudCeiling.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CloudCeiling implements Restorable
{
    public static String DELTA_MODE_PLUS = "Sar.CloudCeiling.DeltaModePlus";
    public static String DELTA_MODE_MINUS = "Sar.CloudCeiling.DeltaModeMinus";
    public static String DELTA_MODE_BOTH = "Sar.CloudCeiling.DeltaModeBoth";

    private WorldWindow wwd;
    private RenderableLayer layer = new RenderableLayer();
    private ContourLinePolygon[] lines = new ContourLinePolygon[2];
    private ScreenElevationLine[] screenLines = new ScreenElevationLine[2];
    private ElevationPlane[] planes = new ElevationPlane[2];
    private String name = "";
    private boolean enabled = true;
    private boolean showExtent = false;
    private String elevationUnit = SAR2.UNIT_IMPERIAL;
    private Color color = Color.WHITE;
    private String pattern = PatternFactory.PATTERN_CIRCLES;
    private double patternSize = 150;
    private double planeOpacity = .3;

    // Elevation
    private double elevationBase = 0;
    private double elevationDelta = 0;
    private String deltaMode = DELTA_MODE_PLUS;
    // Extent
    private ArrayList<? extends LatLon> centerPositions;
    private ArrayList<LatLon> extentPositions;
    private double radius = 10e3;
    private SurfacePolygon shape;

    public CloudCeiling(WorldWindow wwd)
    {
        this.wwd = wwd;
        this.lines[0] = new ContourLinePolygon();
        this.lines[1] = new ContourLinePolygon();
        this.screenLines[0] = new ScreenElevationLine();
        this.screenLines[1] = new ScreenElevationLine();
        this.planes[0] = new ElevationPlane();
        this.planes[1] = new ElevationPlane();
        this.setPatternSize(this.patternSize);
        this.setColor(this.color);
        this.updateElevations();
        this.layer.addRenderable(lines[0]);
        this.layer.addRenderable(lines[1]);
        this.layer.addRenderable(screenLines[0]);
        this.layer.addRenderable(screenLines[1]);
        this.layer.addRenderable(planes[0]);
        this.layer.addRenderable(planes[1]);
        this.layer.setPickEnabled(false);
        this.wwd.getModel().getLayers().add(this.layer);
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        this.lines[0].setEnabled(enabled);
        this.screenLines[0].setEnabled(enabled);
        this.planes[0].setVisible(enabled);
        if (this.elevationDelta > 0)
        {
            this.lines[1].setEnabled(enabled);
            this.screenLines[1].setEnabled(enabled);
            this.planes[1].setVisible(enabled);
        }
    }

    public Color getColor()
    {
        return this.color;
    }

    public void setColor(Color color)
    {
        this.color = color;
        this.lines[0].setColor(color.darker());
        this.lines[1].setColor(color.brighter());
        this.screenLines[0].setColor(color.darker());
        this.screenLines[1].setColor(color.brighter());
        this.planes[0].setImageSource(PatternFactory.createPattern(this.pattern,
            computeAlphaColor(color.darker(), this.planeOpacity)));
        this.planes[1].setImageSource(PatternFactory.createPattern(this.pattern,
            computeAlphaColor(color.brighter(), this.planeOpacity)));
    }

    public double getPlaneOpacity()
    {
        return this.planeOpacity;
    }

    public void setPlaneOpacity(double opacity)
    {
        if (this.planeOpacity == opacity)
            return;

        this.planeOpacity = opacity;
        this.setColor(this.getColor()); // re apply color
    }

    public String getPattern()
    {
        return this.pattern;
    }

    public void setPattern(String pattern)
    {
        if (this.pattern.equals(pattern))
                return;

        this.pattern = pattern;
        this.setColor(this.getColor()); // re apply color
    }

    public double getPatternSize()
    {
        return this.patternSize;
    }

    public void setPatternSize(double sizeInMeter)
    {
        this.patternSize = sizeInMeter;
        this.planes[0].setImageSize(sizeInMeter);
        this.planes[1].setImageSize(sizeInMeter);
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String unit)
    {
        if (!this.elevationUnit.equals(unit))
        {
            if (SAR2.UNIT_IMPERIAL.equals(unit))
            {
                this.elevationBase = SAR2.metersToFeet(this.elevationBase);
                this.elevationDelta = SAR2.metersToFeet(this.elevationDelta);
            }
            else
            {
                this.elevationBase = SAR2.feetToMeters(this.elevationBase);
                this.elevationDelta = SAR2.feetToMeters(this.elevationDelta);
            }
            this.elevationUnit = unit;
        }
    }

    public double getElevationBase()
    {
        return this.elevationBase;
    }

    public void setElevationBase(double elevation)
    {
        if (this.elevationBase == elevation)
            return;

        this.elevationBase = elevation;
        this.updateElevations();
    }

    public double getElevationDelta()
    {
        return this.elevationDelta;
    }

    public void setElevationDelta(double elevation)
    {
        if (this.elevationDelta == elevation)
            return;

        this.elevationDelta = elevation;
        this.updateElevations();
    }

    public String getDeltaMode()
    {
        return this.deltaMode;
    }

    public void setDeltaMode(String mode)
    {
        if (this.deltaMode.equals(mode))
            return;

        this.deltaMode = mode;
        this.updateElevations();
    }
    
    public void relocateLayerOnTop()
    {
        this.wwd.getModel().getLayers().remove(this.layer);
        this.wwd.getModel().getLayers().add(this.layer);
    }

    private void updateElevations()
    {
        // Contour lines elevation is in meter, so convert if we are using feet
        double unitConvertion = SAR2.UNIT_IMPERIAL.equals(this.elevationUnit) ? SAR2.feetToMeters(1) : 1;
        if (this.deltaMode.equals(DELTA_MODE_PLUS))
        {
            lines[0].setElevation(this.elevationBase * unitConvertion);
            lines[1].setElevation((this.elevationBase + this.elevationDelta) * unitConvertion);
        }
        else if (this.deltaMode.equals(DELTA_MODE_MINUS))
        {
            lines[0].setElevation((this.elevationBase - this.elevationDelta) * unitConvertion);
            lines[1].setElevation(this.elevationBase * unitConvertion);
        }
        else if (this.deltaMode.equals(DELTA_MODE_BOTH))
        {
            lines[0].setElevation((this.elevationBase - this.elevationDelta) * unitConvertion);
            lines[1].setElevation((this.elevationBase + this.elevationDelta) * unitConvertion);
        }

        if (this.elevationDelta > 0)
            lines[1].setEnabled(this.isEnabled());
        else
            lines[1].setEnabled(false);

        // Synchonize screen lines and planes
        screenLines[0].setElevation(lines[0].getElevation());
        screenLines[1].setElevation(lines[1].getElevation());
        planes[0].setAltitude(lines[0].getElevation());
        planes[1].setAltitude(lines[1].getElevation());
        screenLines[1].setEnabled(lines[1].isEnabled());
        planes[1].setVisible(lines[1].isEnabled());
    }

    public double getRadius()
    {
        return this.radius;
    }

    public void setRadius(double radius)
    {
        if (this.radius == radius)
            return;

        this.radius = radius;
        this.updateExtent();
    }

    public ArrayList<? extends LatLon> getPositions()
    {
        return this.centerPositions;
    }

    public void setPositions(ArrayList<? extends LatLon> newPositions)
    {
        boolean changed = false;
        if (this.centerPositions != null && this.centerPositions.size() == newPositions.size())
        {
            for (int i = 0; i < newPositions.size(); i++)
                if (!this.centerPositions.get(i).equals(newPositions.get(i)))
                    changed = true;
        }
        else
            changed = true;

        if (!changed)
            return;

        this.centerPositions = newPositions;
        this.updateExtent();
    }

    private void updateExtent()
    {
        if (this.centerPositions == null || this.radius <= 0)
            return;

        // Compute cloud ceiling perimeter extent positions
        this.computeExtentPositions();

        // Update contour lines and planes
        this.lines[0].setPositions(this.extentPositions);
        this.lines[1].setPositions(this.extentPositions);
        this.planes[0].setLocations(this.extentPositions);
        this.planes[1].setLocations(this.extentPositions);
        updateExtentShape();
    }

    private void updateExtentShape()
    {
        if (this.shape != null)
        {
            this.layer.removeRenderable(this.shape);
        }
        if (this.enabled && this.showExtent && this.extentPositions != null && this.extentPositions.size() > 0)
        {
            this.shape = new SurfacePolygon(this.extentPositions);
            ShapeAttributes attr = new BasicShapeAttributes();
            attr.setDrawOutline(false);
            attr.setInteriorMaterial(Material.WHITE);
            attr.setInteriorOpacity(0.1);
            this.shape.setAttributes(attr);
            this.layer.addRenderable(this.shape);
        }
    }

    /**
     * Compute the positions of a perimeter line surrounding the track center positions at radius distance.
     */
    private void computeExtentPositions()
    {
        Globe globe = this.wwd.getModel().getGlobe();
        this.extentPositions = new ArrayList<LatLon>();
        Angle heading = Angle.ZERO;
        int cpn = 0; // Current position number
        // Start cap
        if (this.centerPositions.size() > 1)
            heading = LatLon.greatCircleAzimuth(this.centerPositions.get(cpn), this.centerPositions.get(cpn + 1));
        this.extentPositions.addAll(computeArcPositions(globe, this.centerPositions.get(cpn),
            heading.addDegrees(90), heading.addDegrees(270), this.radius));
        // Follow path one way
        while (cpn < this.centerPositions.size() - 1)
        {
            Angle previousHeading = heading;
            heading = LatLon.greatCircleAzimuth(this.centerPositions.get(cpn), this.centerPositions.get(cpn + 1));
            Angle nextHeading = heading;
            if (cpn < this.centerPositions.size() - 2)
                nextHeading = LatLon.greatCircleAzimuth(this.centerPositions.get(cpn + 1), this.centerPositions.get(cpn + 2));
            this.extentPositions.addAll(computeLinePositions(globe, this.centerPositions.get(cpn),
                this.centerPositions.get(cpn + 1), previousHeading, heading, nextHeading, radius));
            cpn++;
        }
        // End cap - turn around
        heading = normalizedHeading(heading.addDegrees(180));
        cpn = this.centerPositions.size() - 1;
        this.extentPositions.addAll(computeArcPositions(globe, this.centerPositions.get(cpn),
            heading.addDegrees(90), heading.addDegrees(270), this.radius));
        // Follow path the other way
        while (cpn > 0)
        {
            Angle previousHeading = heading;
            heading = LatLon.greatCircleAzimuth(this.centerPositions.get(cpn), this.centerPositions.get(cpn - 1));
            Angle nextHeading = heading;
            if (cpn > 1)
                nextHeading = LatLon.greatCircleAzimuth(this.centerPositions.get(cpn - 1), this.centerPositions.get(cpn - 2));
            this.extentPositions.addAll(computeLinePositions(globe, this.centerPositions.get(cpn),
                this.centerPositions.get(cpn - 1), previousHeading, heading, nextHeading, radius));
            cpn--;
        }
        // Close polygon
        this.extentPositions.add(this.extentPositions.get(0));
    }

    private static ArrayList<LatLon> computeArcPositions(Globe globe, LatLon center, Angle start, Angle end, double radius)
    {
        start = normalizedHeading(start);
        end = normalizedHeading(end);
        end = end.degrees > start.degrees ? end : end.addDegrees(360);
        ArrayList<LatLon> positions = new ArrayList<LatLon>();
        Angle radiusAngle = Angle.fromRadians(radius / globe.getRadiusAt(center));
        //positions.add(LatLon.greatCircleEndPosition(center, start, radiusAngle)); // Skip first pos
        positions.add(LatLon.greatCircleEndPosition(center, Angle.midAngle(start, end), radiusAngle));
        positions.add(LatLon.greatCircleEndPosition(center, end, radiusAngle));
        return positions;
    }

    private static ArrayList<LatLon> computeLinePositions(Globe globe, LatLon p1, LatLon p2, Angle previousHeading,
        Angle heading, Angle nextHeading, double radius)
    {
        ArrayList<LatLon> positions = new ArrayList<LatLon>();
        Angle radiusAngle = Angle.fromRadians(radius / globe.getRadiusAt(p1));
        // Skip first pos
//        if (isClockwiseHeadingChange(previousHeading, heading))
//            positions.add(LatLon.greatCircleEndPosition(p1, heading.subtractDegrees(90), radiusAngle));
//        else
//            positions.add(LatLon.greatCircleEndPosition(p1,
//                computeMidHeading(previousHeading, heading).subtractDegrees(90), radiusAngle));

        if (isClockwiseHeadingChange(heading, nextHeading))
        {
            positions.add(LatLon.greatCircleEndPosition(p2, heading.subtractDegrees(90), radiusAngle));
            if (!heading.equals(nextHeading))
                positions.addAll(computeArcPositions(globe, p2, heading.subtractDegrees(90),
                    nextHeading.subtractDegrees(90), radius));
        }
        else
            positions.add(LatLon.greatCircleEndPosition(p2,
                computeMidHeading(heading, nextHeading).subtractDegrees(90), radiusAngle));

        return positions;
    }

    private static boolean isClockwiseHeadingChange(Angle from, Angle to)
    {
        double a1 = normalizedHeading(from).degrees;
        double a2 = normalizedHeading(to).degrees;
        return (a2 > a1 && a2 - a1 < 180);
    }

    private static Angle computeMidHeading(Angle a1, Angle a2)
    {
        a1 = normalizedHeading(a1);
        a2 = normalizedHeading(a2);
        if (a1.degrees < a2.degrees && a2.degrees - a1.degrees > 180)
            a1 = a1.addDegrees(360);
        else if (a2.degrees < a1.degrees && a1.degrees - a2.degrees > 180)
            a2 = a2.addDegrees(360);
        return Angle.midAngle(a1, a2);
    }

    private static Angle normalizedHeading(Angle heading)
    {
        double a = heading.degrees % 360;
        while (a < 0)
            a += 360;
        return Angle.fromDegrees(a);
    }

    private static Color computePremultipliedAlphaColor(Color color, double opacity)
    {
        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        compArray[3] = (float)WWMath.clamp(opacity, 0, 1);
        return new Color(
            compArray[0] * compArray[3],
            compArray[1] * compArray[3],
            compArray[2] * compArray[3],
            compArray[3]);
    }

    private static Color computeAlphaColor(Color color, double opacity)
    {
        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        compArray[3] = (float)WWMath.clamp(opacity, 0, 1);
        return new Color(
            compArray[0],
            compArray[1],
            compArray[2],
            compArray[3]);
    }

    // *** Restorable interface ***

    public String getRestorableState()
    {
        RestorableSupport restorableSupport = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (restorableSupport == null)
            return null;

        restorableSupport.addStateValueAsString("name", this.name);
        
        if (this.color != null)
        {
            String encodedColor = RestorableSupport.encodeColor(this.color);
            if (encodedColor != null)
                restorableSupport.addStateValueAsString("color", encodedColor);
        }

        if (this.centerPositions != null)
        {
            // Create the base "positions" state object.
            RestorableSupport.StateObject positionsStateObj = restorableSupport.addStateObject("positions");
            if (positionsStateObj != null)
            {
                for (LatLon p : this.centerPositions)
                {
                    // Save each position only if all parts (latitude, longitude) can be saved.
                    if (p != null && p.getLatitude() != null && p.getLongitude() != null)
                    {
                        // Create a nested "position" element underneath the base "positions".
                        RestorableSupport.StateObject pStateObj =
                            restorableSupport.addStateObject(positionsStateObj, "position");
                        if (pStateObj != null)
                        {
                            restorableSupport.addStateValueAsDouble(pStateObj, "latitudeDegrees",
                                p.getLatitude().degrees);
                            restorableSupport.addStateValueAsDouble(pStateObj, "longitudeDegrees",
                                p.getLongitude().degrees);
                        }
                    }
                }
            }
        }

        restorableSupport.addStateValueAsString("elevationUnit", this.elevationUnit);
        restorableSupport.addStateValueAsString("deltaMode", this.deltaMode);
        restorableSupport.addStateValueAsDouble("elevationDelta", this.elevationDelta);
        restorableSupport.addStateValueAsDouble("elevationBase", this.elevationBase);
        restorableSupport.addStateValueAsDouble("radius", this.radius);
        restorableSupport.addStateValueAsBoolean("enabled", this.enabled);
        restorableSupport.addStateValueAsBoolean("showExtent", this.showExtent);
        restorableSupport.addStateValueAsString("pattern", this.pattern);
        restorableSupport.addStateValueAsDouble("patternSize", this.patternSize);
        restorableSupport.addStateValueAsDouble("planeOpacity", this.planeOpacity);

        return restorableSupport.getStateAsXml();
    }

    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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

        // Get the base "positions" state object.
        RestorableSupport.StateObject positionsStateObj = restorableSupport.getStateObject("positions");
        if (positionsStateObj != null)
        {
            ArrayList<LatLon> newPositions = new ArrayList<LatLon>();
            // Get the nested "position" states beneath the base "positions".
            RestorableSupport.StateObject[] positionStateArray =
                restorableSupport.getAllStateObjects(positionsStateObj, "position");
            if (positionStateArray != null && positionStateArray.length != 0)
            {
                for (RestorableSupport.StateObject pStateObj : positionStateArray)
                {
                    if (pStateObj != null)
                    {
                        // Restore each position only if all parts are available.
                        Double latitudeState = restorableSupport.getStateValueAsDouble(pStateObj, "latitudeDegrees");
                        Double longitudeState = restorableSupport.getStateValueAsDouble(pStateObj, "longitudeDegrees");
                        if (latitudeState != null && longitudeState != null)
                            newPositions.add(LatLon.fromDegrees(latitudeState, longitudeState));
                    }
                }
            }

            // Even if there are no actual positions specified, we set positions as an empty list.
            // An empty set of positions is still a valid state.
            this.centerPositions = newPositions;
        }

        String nameState = restorableSupport.getStateValueAsString("name");
        if (nameState != null)
            this.name = nameState;

        String elevationUnitState = restorableSupport.getStateValueAsString("elevationUnit");
        if (elevationUnitState != null)
            this.elevationUnit = elevationUnitState;

        String deltaModeState = restorableSupport.getStateValueAsString("deltaMode");
        if (deltaModeState != null)
            this.deltaMode = deltaModeState;

        Double elevationDeltaState = restorableSupport.getStateValueAsDouble("elevationDelta");
        if (elevationDeltaState != null)
            this.elevationDelta = elevationDeltaState;

        Double elevationBaseState = restorableSupport.getStateValueAsDouble("elevationBase");
        if (elevationBaseState != null)
            this.elevationBase = elevationBaseState;

        Double radiusState = restorableSupport.getStateValueAsDouble("radius");
        if (radiusState != null)
            this.radius = radiusState;

        Boolean enabledState = restorableSupport.getStateValueAsBoolean("enabled");
        if (enabledState != null)
            setEnabled(enabledState);

        Double patternSizeState = restorableSupport.getStateValueAsDouble("patternSize");
        if (patternSizeState != null)
            this.setPatternSize(patternSizeState);

        Double planeOpacityState = restorableSupport.getStateValueAsDouble("planeOpacity");
        if (patternSizeState != null)
            this.planeOpacity = planeOpacityState;

        String patternState = restorableSupport.getStateValueAsString("pattern");
        if (patternState != null)
            this.pattern = patternState;

        String colorState = restorableSupport.getStateValueAsString("color");
        if (colorState != null)
        {
            Color color = RestorableSupport.decodeColor(colorState);
            if (color != null)
                setColor(color); // this applies the pattern and plane opacity too
        }

        Boolean showExtentState = restorableSupport.getStateValueAsBoolean("showExtent");
        if (showExtentState != null)
            this.showExtent = showExtentState;


        this.updateElevations();
        this.updateExtent();

    }

}
