package model;

public class Location {
    private final double lat; // ширина
    private final double lng; // долгота
    private final String name;
    private String weather;
    private String interestingPlaces;

    public Location(double lat, double lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }
    public double getLat() {
        return lat;
    }
    public double getLng() {
        return lng;
    }
    public String getName() {
        return name;
    }
    public String getWeather() {
        return weather;
    }
    public String getInterestingPlaces() {
        return interestingPlaces;
    }
    public void setWeather(String weather) {
        this.weather = weather;
    }
    public void setInterestingPlaces(String interestingPlaces) {
        this.interestingPlaces = interestingPlaces;
    }
}
