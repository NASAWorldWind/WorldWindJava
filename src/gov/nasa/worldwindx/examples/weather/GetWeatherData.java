package gov.nasa.worldwindx.examples.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

@JsonIgnoreProperties(ignoreUnknown = true)
class Rain {
    @JsonProperty("3h")
    private double rain_level = 0;

    public double getRain_level() {
        return rain_level;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Snow {
    @JsonProperty("3h")
    private double snow_level = 0;

    public double getSnow_level() {
        return snow_level;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Cloudiness {
    @JsonProperty("all")
    private double cloudness = 0;

    public double getCloudness() {
        return cloudness;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Wind {
    @JsonProperty("speed")
    private double speed = 0;

    @JsonProperty("deg")
    private double degree = 0;

    public double getSpeed() {
        return speed;
    }

    public double getDegree() {
        return degree;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class MainData {
    @JsonProperty("temp")
    private double temperature = 0;

    public double getTemperature()
    {
        return temperature;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Coordinates {
    @JsonProperty("lon")
    double lon = 0;

    @JsonProperty("lat")
    double lat = 0;

    public double getLon()
    {
        return lon;
    }

    public double getLat()
    {
        return lat;
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class WeatherData {

    @JsonProperty("rain")
    private Rain rain;

    @JsonProperty("snow")
    private Snow snow;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("clouds")
    private Cloudiness cloudiness;

    @JsonProperty("main")
    private MainData mainData;

    @JsonProperty("coord")
    private Coordinates coordinates;

    @Override
    public String toString() {
        return "WeatherData{\n" +
            "   coords: {" + "longitude: " + coordinates.getLon() + ", latitude: " + coordinates.getLat() + "}\n" +
            "   rain: " + rain.getRain_level() + "\n" +
                "   snow: " + snow.getSnow_level() + "\n" +
                "   wind: {" + "speed: " + wind.getSpeed() + " meter/sec" + ", degree: " + wind.getDegree() + "}\n" +
                "   cloudiness: " + cloudiness.getCloudness() + "%" + "\n" +
                "   temperature: " + mainData.getTemperature()   +
                "\n}";
    }

    public WeatherData() {
        coordinates = new Coordinates();
        rain = new Rain();
        snow = new Snow();
        wind = new Wind();
        cloudiness = new Cloudiness();
        mainData = new MainData();
    }

    public MainData getMainData()
    {
        return mainData;
    }
}

public class GetWeatherData
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static void sendGet(double longitude, double latitude) throws Exception
    {
        String apiKey = "86e4cd676d8af3484ceafecb0377a1b3"; //Attention : every user should have their own apiKey!
        String units = "metric";
        String lang = "ru";
        String accuracyLevel = "accurate";

        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude +"&lon=" + longitude +
                "&type=" + accuracyLevel  + "&units=" + units + "&lang=" + lang +
                "&APPID="+apiKey;

        HttpClient client;
        client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        try {
            FileWriter writer = new FileWriter("weatherdata.txt", true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write("\n" + result.toString());
            bufferWriter.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }

    }

    public static void updateData(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int width, int height)
        throws Exception
    {
        try {
            FileWriter fstream1 = new FileWriter("weatherdata.txt");
            BufferedWriter out1 = new BufferedWriter(fstream1);
            out1.write(Integer.toString(width*height));
            out1.close();
        } catch (Exception e)
        {System.err.println("Error in file cleaning: " + e.getMessage());}

        double lat_step = (maxLatitude - minLatitude) / (width - 1);
        double lon_step = (maxLongitude - minLongitude) / (height - 1);

        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j)
                sendGet(minLongitude + j * lon_step, maxLatitude - i * lat_step);
    }

    public static void main(String[] args) throws Exception
    {

        int width = 6;
        int height = 6; //grid size
        double minLatitude = 41;//59.7;
        double maxLatitude = 71;//60.2;
        double minLongitude = 27;
        double maxLongitude = 142;//31.09;

        updateData(minLatitude,maxLatitude,minLongitude,maxLongitude,width,height);
    }
}
