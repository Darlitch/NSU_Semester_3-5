import exceptions.ViewModelException;
import geocoding.GeoData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Location;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import opentrip.FeatureData;
import opentrip.Properties;
import opentripinfo.FeatureInfoData;
import openweather.WeatherData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ViewModel {
    private static final int MAX_PLACE_QUANTITY = 5;
    private final OkHttpClient client;
    private final APIController apiController;
    private Button searchButton;
    private ListView<String> resultList;

    public ViewModel() {
        client = new OkHttpClient();
        apiController = new APIController(client);
    }
    public OkHttpClient getClient() {
        return client;
    }
    public VBox createUI() {
        VBox vbox = new VBox(10); // 10 пикселей
        vbox.setPadding(new Insets(20));
        Label label = new Label("Enter location name:");
        TextField input = new TextField();
        searchButton = new Button("Search");
        resultList = new ListView<>();
        resultList.setPrefHeight(200);
        searchButton.setOnAction(event -> search(input));
        vbox.getChildren().addAll(label, input, searchButton, resultList);
        return vbox;
    }

    private void search(TextField input) {
        String inputStr = input.getText();
        Platform.runLater(() -> resultList.getItems().clear());
        searchButton.setDisable(true);
        resultList.setOnMouseClicked(null);

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new ViewModelException("response failed", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        List<Location> locations = GeoData.parseJSON(responseString);
                        Platform.runLater(() -> {
                           resultList.getItems().clear();
                           if (locations.isEmpty()) {
                               resultList.getItems().add("No results");
                           } else {
                               for (Location location : locations) {
                                   resultList.getItems().add(location.getName());
                               }
                           }
                           searchButton.setDisable(false);
                           resultList.setOnMouseClicked(event -> handleResultList(locations));
                        });
                    } catch (IOException e) {
                        throw new ViewModelException("search failed", e);
                    }
                } else {
                    Platform.runLater(() -> {
                        resultList.getItems().clear();
                        resultList.getItems().add("Error" + response.code());
                        searchButton.setDisable(false);
                    });
                }
            }
        };
        apiController.getLocationsByAddress(inputStr, callback);
    }

    private void handleResultList(List<Location> locations) {
        resultList.setOnMouseClicked(null);
        searchButton.setDisable(true);
        int selectedIndex = resultList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= locations.size()) {
            searchButton.setDisable(false);
            return;
        }
        Location location = locations.get(selectedIndex);
        resultList.getItems().clear();

        ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
        futures.add(new CompletableFuture<>());

        Callback weatherCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new ViewModelException("Weather response failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    String weather = WeatherData.parseJSON(responseString);
                    futures.getFirst().complete(weather);
                } else {
                    futures.getFirst().complete("Get Weather response failed: " + response.code());
                }
            }
        };
        apiController.getWeatherByCoordinates(location.getLat(), location.getLng(), weatherCallback);

        Callback placesCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Platform.runLater(() ->{
                    resultList.getItems().add("Get interesting places failed");
                    searchButton.setDisable(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    List<Properties> places = FeatureData.parseJSON(responseString);
                    futures.add(new CompletableFuture<>());
                    futures.get(1).complete("Interesting places:");
//                    futures.get(0).complete("Interesting places:");

                    int placeCount = 0;
                    for (Properties place : places) {
                        if (place.getName() != null && !place.getName().isEmpty()) {
                            CompletableFuture<String> future = new CompletableFuture<>();
                            handlePlaceInfo(place, future);
                            futures.add(future);
                            placeCount++;
                            if (placeCount >= MAX_PLACE_QUANTITY) {
                                break;
                            }
                        }
                    }

                    CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                    allOf.thenAccept(voidResult -> {
                        for (CompletableFuture<String> future : futures) {
                            try {
                                String text = future.get();
                                Platform.runLater(() -> resultList.getItems().add(text));
                            } catch (ExecutionException | InterruptedException e) {
                                Platform.runLater(() -> {
                                    resultList.getItems().clear();
                                    resultList.getItems().add("Get interesting places failed");
                                });
                            }
                        }
                        searchButton.setDisable(false);
                    });
                } else {
                    Platform.runLater(() ->{
                        resultList.getItems().add("Get interesting places failed");
                        searchButton.setDisable(false);
                    });
                }
            }
        };
        apiController.getInterestingPlacesByCoordinates(
                location.getLng() - 0.01, location.getLng() + 0.01,
                location.getLat() - 0.01, location.getLat() + 0.01,
                placesCallback);
    }

    private void handlePlaceInfo(Properties place, CompletableFuture<String> future) {
        Callback placeCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new ViewModelException("Get place info failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    String placeInfo = FeatureInfoData.parseJSON(responseString);
                    future.complete(placeInfo);
                } else {
                    throw new ViewModelException("Get place info failed: " + response.code());
                }
            }
        };
        apiController.getInfoAboutPlace(place.getXid(), placeCallback);
    }
}
