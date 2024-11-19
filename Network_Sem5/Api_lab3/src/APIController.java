import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class APIController {
    private static final String OPENWEATHER_API_KEY = "8a7a08eb796672a72dc63b9f3930fe8c";
    private static final String OPENTRIP_API_KEY = "5ae2e3f221c38a28845f05b6d2d32114e33e4b62b7b95599fefb0a6f";
    private static final String GRAPHHOPPER_API_KEY = "04a9c8ba-db4f-4131-82d5-8c0d8d22bbc5";

    private static final String GRAPHHOPPER_API_URL = "https://graphhopper.com/api/1/geocode?q={ADDRESS}&locale=ru&key={API_KEY}";
    private static final String OPENWEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?lat={LAT}&lng={LNG}&appid={API_KEY}";
    private static final String OPENTRIP_API_URL = "https://api.opentripmap.com/0.1/ru/places/bbox?lng_min={LNG_MIN}&lat_min={LAT_MIN}&lng_max={LNG_MAX}&lat_max={LAT_MAX}&format=geojson&apikey={API_KEY}";
    private static final String OPENTRIP_INFO_API_URL = "https://api.opentripmap.com/0.1/ru/places/xid/{XID}?apikey={API_KEY}";
    private OkHttpClient client;

    public APIController(OkHttpClient client) {
        this.client = client;
    }

    private void sendGetRequest(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    public void getLocationsByAddress(String address, Callback callback) {
        String apiUrl = GRAPHHOPPER_API_URL
                .replace("{ADDRESS}", URLEncoder.encode(address, StandardCharsets.UTF_8)
                .replace("{API_KEY}", GRAPHHOPPER_API_KEY));
        System.out.println("GraphHopper Url: " + apiUrl);
        sendGetRequest(apiUrl, callback);
    }

    public void getWeatherByCoordinates(double lat, double lng, Callback callback) {
        String apiUrl = OPENWEATHER_API_URL
                .replace("{LAT}", String.valueOf(lat))
                .replace("{LNG}", String.valueOf(lng))
                .replace("{API_KEY}", OPENWEATHER_API_KEY);
        System.out.println("OpenWeather Url: " + apiUrl);
        sendGetRequest(apiUrl, callback);
    }

    public void getInterestingPlacesByCoordinates(double lngMin, double lngMax, double latMin, double latMax, Callback callback) {
        String apiUrl = OPENTRIP_API_URL
                .replace("{LNG_MIN}", String.valueOf(lngMin))
                .replace("{LNG_MAX}", String.valueOf(lngMax))
                .replace("{LAT_MIN}", String.valueOf(latMin))
                .replace("{LAT_MAX}", String.valueOf(latMax))
                .replace("{API_KEY}", OPENTRIP_API_KEY);
        System.out.println("OpenTrip Url: " + apiUrl);
        sendGetRequest(apiUrl, callback);
    }

    public void getInfoAboutPlace(String XId, Callback callback) {
        String apiUrl = OPENTRIP_INFO_API_URL
                .replace("{XID}", XId)
                .replace("{API_KEY}", OPENTRIP_API_KEY);
        System.out.println("OpenTrip Info Url: " + apiUrl);
        sendGetRequest(apiUrl, callback);
    }
}
