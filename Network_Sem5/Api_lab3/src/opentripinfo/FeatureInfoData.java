package opentripinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.OpenTripException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureInfoData {
    private String xid;
    private String name;
    private Address address;
    private String rate;
    private String osm; // идентификатор в OpenStreetMap
    private String kinds; // Категория
    private Sources sources;
    private String otm; // идентификатор в OpenTripMap
    private PlaceInfo placeInfo;
    private String image;
    private Preview preview;
    private Point point;
    private WikipediaExtracts wikipediaExtracts;

    public static String parseJSON(String str) {
        try {
          String res = null;
          ObjectMapper mapper = new ObjectMapper();
          FeatureInfoData featureInfoData = mapper.readValue(str, FeatureInfoData.class);
          if (featureInfoData.name != null && !featureInfoData.name.isEmpty()) {
              res = "- " + featureInfoData.name;
          }
          WikipediaExtracts wikipediaExtracts = featureInfoData.getWikipediaExtracts();
          if (wikipediaExtracts != null) {
              res += "\n-- " + wikipediaExtracts.getText();
          }
          return res;
        } catch (JsonProcessingException e) {
            throw new OpenTripException("Error parsing OpenTripInfo JSON ", e);
        }
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getOsm() {
        return osm;
    }

    public void setOsm(String osm) {
        this.osm = osm;
    }

    public String getKinds() {
        return kinds;
    }

    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    public Sources getSources() {
        return sources;
    }

    public void setSources(Sources sources) {
        this.sources = sources;
    }

    public String getOtm() {
        return otm;
    }

    public void setOtm(String otm) {
        this.otm = otm;
    }

    public PlaceInfo getPlaceInfo() {
        return placeInfo;
    }

    public void setPlaceInfo(PlaceInfo placeInfo) {
        this.placeInfo = placeInfo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public WikipediaExtracts getWikipediaExtracts() {
        return wikipediaExtracts;
    }

    public void setWikipediaExtracts(WikipediaExtracts wikipediaExtracts) {
        this.wikipediaExtracts = wikipediaExtracts;
    }



}
