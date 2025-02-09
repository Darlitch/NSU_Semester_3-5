package geocoding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.GeoDataException;
import model.Location;

import java.util.ArrayList;
import java.util.List;

public class GeoData {
    private List<Hit> hits;
    private String locale;

    public static List<Location> parseJSON(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            GeoData geoData = mapper.readValue(json, GeoData.class);
            List<Location> locations = new ArrayList<Location>();
            List<Hit> hits = geoData.getHits();
            for (Hit hit : hits) {
                String name = hit.getName();
                String state = hit.getState();
                String country = hit.getCountry();
                String city = hit.getCity();
                Point point = hit.getPoint();
                double lat = point.getLat();
                double lng = point.getLng();
                locations.add(new Location(lat, lng, name + ", " + city + ", " + state + ", " + country));
            }
            return locations;
        } catch (JsonProcessingException e) {
            throw new GeoDataException("parse JSON failed", e);
        }

    }

    public List<Hit> getHits() {
        return hits;
    }

    public String getLocale() {
        return locale;
    }

    public void setHits(List<Hit> hits) {
        this.hits = hits;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
