/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerPath;
import gov.nasa.worldwindx.applications.worldwindow.util.WWOUnitsFormat;

import java.awt.*;
import java.util.Iterator;

/**
 * @author tag
 * @version $Id: CoordinatesDisplay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CoordinatesDisplay extends AbstractOnDemandLayerFeature
{
    public CoordinatesDisplay()
    {
        this(null);
    }

    public CoordinatesDisplay(Registry registry)
    {
        super("Coordinates", Constants.FEATURE_COORDINATES_DISPLAY,
            "gov/nasa/worldwindx/applications/worldwindow/images/coordinates-64x64.png", null, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();
    }

    @Override
    protected Layer createLayer()
    {
        Layer layer = this.doCreateLayer();

        layer.setPickEnabled(false);

        return layer;
    }

    @Override
    protected void addLayer(LayerPath path)
    {
        controller.addInternalActiveLayer(this.layer);
    }

    @Override
    protected void removeLayer()
    {
        this.controller.getWWPanel().removeLayer(this.layer);
    }

    protected Layer doCreateLayer()
    {
        ScreenAnnotation anno = new ScreenAnnotation("Dummy Text", new Point(100, 100));
        anno.setAlwaysOnTop(true);

        AnnotationAttributes attrs = anno.getAttributes();
        attrs.setTextColor(Color.WHITE);
        attrs.setFont(Font.decode("Consolas-Bold-15"));
        attrs.setEffect(AVKey.TEXT_EFFECT_OUTLINE);

        attrs.setFrameShape(AVKey.SHAPE_NONE);
        attrs.setLeader(AVKey.SHAPE_NONE);
        attrs.setBackgroundColor(Color.BLACK);
        attrs.setBorderColor(new Color(0.1f, 0.1f, 0.1f, 0f));
        attrs.setBorderWidth(0d);
        attrs.setCornerRadius(5);
        attrs.setInsets(new Insets(10, 0, 0, 10));

        int width = 340, height = 200;
        attrs.setSize(new Dimension(width, height));
        attrs.setTextAlign(AVKey.RIGHT);
        attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attrs.setDrawOffset(new Point(-width / 2, -height));

        CoordAnnotationLayer layer = new CoordAnnotationLayer();
        layer.setValue(Constants.SCREEN_LAYER, true);
        layer.setPickEnabled(false);
        layer.addAnnotation(anno);
        layer.setName(this.getName());

        return layer;
    }

    private class CoordAnnotationLayer extends AnnotationLayer
    {
        @Override
        public void render(DrawContext dc)
        {
            Iterator<Annotation> iter = this.getAnnotations().iterator();
            Annotation anno = iter.next();
            if (anno != null && anno instanceof ScreenAnnotation)
            {
                anno.setText(formatText(dc));
                Dimension wwSize = controller.getWWPanel().getSize();
                ((ScreenAnnotation) anno).setScreenPoint(new Point(wwSize.width, wwSize.height));
            }

            super.render(dc);
        }
    }

    private Position getCurrentPosition(DrawContext dc)
    {
        if (dc.getPickedObjects() == null)
            return null;

        PickedObject po = dc.getPickedObjects().getTerrainObject();
        return po != null ? po.getPosition() : null;
    }

    private String formatText(DrawContext dc)
    {
        StringBuilder sb = new StringBuilder();

        Position eyePosition = dc.getView().getEyePosition();
        if (eyePosition != null)
        {
            WWOUnitsFormat units = this.controller.getUnits();
            String origFormat = units.getFormat(UnitsFormat.FORMAT_EYE_ALTITUDE);
            String tempFormat = origFormat;

            if (Math.abs(eyePosition.getElevation() * units.getLengthUnitsMultiplier()) < 10)
            {
                tempFormat = " %,6.3f %s";
                units.setFormat(UnitsFormat.FORMAT_EYE_ALTITUDE, tempFormat);
            }

            sb.append(this.controller.getUnits().eyeAltitudeNL(eyePosition.getElevation()));

            if (!tempFormat.equals(origFormat))
                units.setFormat(UnitsFormat.FORMAT_EYE_ALTITUDE, origFormat);
        }
        else
        {
            sb.append("Altitude\n");
        }

        Position currentPosition = getCurrentPosition(dc);
        if (currentPosition != null)
        {
            sb.append(this.controller.getUnits().latitudeNL(currentPosition.getLatitude()));
            sb.append(this.controller.getUnits().longitudeNL(currentPosition.getLongitude()));
            sb.append(this.controller.getUnits().terrainHeightNL(currentPosition.getElevation(),
                this.controller.getWWd().getSceneController().getVerticalExaggeration()));
        }
        else
        {
            sb.append(this.controller.getUnits().getStringValue(UnitsFormat.LABEL_LATITUDE)).append("\n");
            sb.append(this.controller.getUnits().getStringValue(UnitsFormat.LABEL_LONGITUDE)).append("\n");
            sb.append(this.controller.getUnits().getStringValue(UnitsFormat.LABEL_TERRAIN_HEIGHT)).append("\n");
        }

        sb.append(this.controller.getUnits().pitchNL(computePitch(dc.getView())));
        sb.append(this.controller.getUnits().headingNL(computeHeading(dc.getView())));

        String datum = this.controller.getUnits().datumNL();

        if (controller.getUnits().isShowUTM())
        {
            sb.append(datum);
            if (currentPosition != null)
            {
                try
                {
                    UTMCoord utm = UTMCoord.fromLatLon(currentPosition.getLatitude(), currentPosition.getLongitude(),
                        this.controller.getUnits().isShowWGS84() ? null : "NAD27");

                    sb.append(this.controller.getUnits().utmZoneNL(utm.getZone()));
                    sb.append(this.controller.getUnits().utmEastingNL(utm.getEasting()));
                    sb.append(this.controller.getUnits().utmNorthingNL(utm.getNorthing()));
                }
                catch (Exception e)
                {
                    sb.append(String.format(
                        this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_ZONE) + "\n"));
                    sb.append(String.format(
                        this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_EASTING) + "\n"));
                    sb.append(String.format(
                        this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_NORTHING) + "\n"));
                }
            }
            else
            {
                sb.append(
                    String.format(this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_ZONE) + "\n"));
                sb.append(String.format(
                    this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_EASTING) + "\n"));
                sb.append(String.format(
                    this.controller.getUnits().getStringValue(UnitsFormat.LABEL_UTM_NORTHING) + "\n"));
            }
        }
        else
        {
            sb.append(datum);
        }

        return sb.toString();
    }

    private double computeHeading(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;

        return orbitView.getHeading().getDegrees();
    }

    private double computePitch(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;

        return orbitView.getPitch().getDegrees();
    }
}
