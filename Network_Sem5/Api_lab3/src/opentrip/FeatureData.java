package opentrip;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.OpenTripException;

import java.util.List;

public class FeatureData {
    private String type;
    private List<Feature> features;

    public static List<Properties> parseJSON(String str) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FeatureData featureData = mapper.readValue(str, FeatureData.class);
            return featureData.getFeatures().stream().map(Feature::getProperties).toList();
        } catch (JsonProcessingException e) {
            throw new OpenTripException("OpenTrip parseJSON failed", e);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
