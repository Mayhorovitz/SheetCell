package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;


public class Client extends Application {

    private AppMainController appMainController;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("shticell");

        URL mainPageUrl = getClass().getResource("/main/AppMain.fxml"); // URL of the main page FXML
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainPageUrl);
            Parent root = fxmlLoader.load();
            appMainController = fxmlLoader.getController();

            Scene scene = new Scene(root, 700, 600);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Set the initial screen to Login page
            appMainController.switchToLogin();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert in case of failure to load
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load the application");
            alert.setContentText("An error occurred while trying to load the main page.");
            alert.showAndWait();
        }

    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        if (appMainController != null) {
            appMainController.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
