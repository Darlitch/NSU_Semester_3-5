import exceptions.ViewModelException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Api_lab3");
        ViewModel vm = new ViewModel();
        OkHttpClient client = vm.getClient();
        VBox vbox = vm.createUI();
        Scene scene = new Scene(vbox, 400, 400);
        stage.setScene((scene));

        stage.setOnCloseRequest(event -> {
            try {
                if (client != null) {
                    client.dispatcher().executorService().shutdown();
                    client.connectionPool().evictAll();
                    if (client.cache() != null) {
                        client.cache().close();
                    }
                }
            } catch (IOException e) {
                throw new ViewModelException("Error when closing window", e);
            }
        });
        stage.show();
    }
}