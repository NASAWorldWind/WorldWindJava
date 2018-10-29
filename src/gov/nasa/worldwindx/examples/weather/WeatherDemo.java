package gov.nasa.worldwindx.examples.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import java.awt.*;
import gov.nasa.worldwindx.examples.analytics.*;

import java.io.*;
import java.util.*;
import static gov.nasa.worldwindx.examples.analytics.AnalyticSurface.createGridPointAttributes;
import static gov.nasa.worldwindx.examples.weather.GetWeatherData.updateData;

public class WeatherDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer analyticSurfaceLayer;

        public AppFrame()
        {
            this.initAnalyticSurfaceLayer();
        }

        protected void initAnalyticSurfaceLayer()
        {
            this.analyticSurfaceLayer = new RenderableLayer();
            this.analyticSurfaceLayer.setPickEnabled(false);
            this.analyticSurfaceLayer.setName("Weather Surface");
            insertBeforePlacenames(this.getWwd(), this.analyticSurfaceLayer);


            Thread t = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        createPrecipitationSurface(analyticSurfaceLayer);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    public static Iterable<? extends AnalyticSurface.GridPointAttributes> createUserColorGradientValues(ArrayList<Color> colors)
    {
        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
            = new ArrayList<AnalyticSurface.GridPointAttributes>();


        for (int i = 0; i < colors.size(); i++)
        {
            attributesList.add(createGridPointAttributes(20, colors.get(i)));// можно добавить высоту рельефа
        }

        return attributesList;
    }

    protected static void initColors (ArrayList<Color> colors) throws IOException
    {

        File file = new File("weatherdata.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        int weatherdata_size = Integer.parseInt(br.readLine());
        /*if (scanner.nextInt() != colors.size())
            throw new IllegalArgumentException();*/

        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < weatherdata_size; ++i)
        {
            WeatherData data = mapper.readValue(br.readLine(), WeatherData.class);
            double temp = data.getMainData().getTemperature();

            if (temp < -12)
                colors.add(i, new Color(24, 42, 255));

            if (temp >= -12 && temp < -7)
                colors.add(i, new Color(12, 245, 255));

            if (temp >= -7 && temp < -2)
                colors.add(i, new Color(11, 255, 176));

            if (temp >= -2 && temp < 3)
                colors.add(i, new Color(114, 255, 6));

            if (temp >=3 && temp < 8)
                colors.add(i, new Color(255, 208, 7));

            if (temp >=8 && temp < 13)
                colors.add(i, new Color(255, 87, 10));

            if (temp >=13 && temp < 18)
                colors.add(i, new Color(255, 10, 5));

            if (temp >= 18)
                colors.add(i, new Color(255, 4, 175));
        }


    }

    protected static void createPrecipitationSurface(final RenderableLayer outLayer) throws Exception
    {
        int width = 6;
        int height = 6; //grid size
        double minLatitude = 41;//59.7;
        double maxLatitude = 71;//60.2;
        double minLongitude = 27;
        double maxLongitude = 142;//31.09;

        //updateData(minLatitude, maxLatitude, minLongitude, maxLongitude, width, height); //if necessary


        final AnalyticSurface surface = new AnalyticSurface();

        Sector tmpSector = Sector.fromDegrees(minLatitude,maxLatitude,minLongitude,maxLongitude);
        surface.setSector(tmpSector);
        surface.setDimensions(width, height);

        ArrayList<Color> colors = new ArrayList<Color>();
        initColors(colors);

        surface.setValues(createUserColorGradientValues(colors));
        surface.setVerticalScale(5e3);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawOutline(false);
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.6);
        surface.setSurfaceAttributes(attr);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                surface.setClientLayer(outLayer);
                outLayer.addRenderable(surface);
            }
        });
    }

    public static void main(String[] args)
    {

        ApplicationTemplate.start("Weather Demo Example", AppFrame.class);
    }
}
