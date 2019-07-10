package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.mercator.MercatorTileUrlBuilder;
import java.net.MalformedURLException;
import java.net.URL;

public class MapboxLayer extends BasicMercatorTiledImageLayer {

    public MapboxLayer()
    {
        super("mb", "Earth/Mapbox", 19, 256, false, ".png", new URLBuilder());
        MapboxLayer.this.setDetailHint(-1.8);
    }

    private static class URLBuilder extends MercatorTileUrlBuilder
    {
        private String accessToken;

        @Override
        protected URL getMercatorURL(int x, int y, int z) throws MalformedURLException
        {
            String urlPostfix = (this.accessToken != null) ? "?access_token=" + this.accessToken : "";
            return new URL("https://api.mapbox.com/styles/v1/mapbox/streets-v11/tiles/256/" + z + "/" + x + "/" + y + urlPostfix);
        }
    }

    public void setAccessToken(String apiKey)
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        urlBuilder.accessToken = apiKey;
    }

    public String getAccessToken()
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        return urlBuilder.accessToken;
    }

    @Override
    public String toString()
    {
        return "Mapbox";
    }
}