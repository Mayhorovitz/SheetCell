package main;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import dto.impl.SheetDTOImpl;
import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.MainController;
import sheetsManagement.SheetsManagementController;
import util.http.HttpClientUtil;
import login.LoginController;

import java.io.IOException;
import java.net.URL;

public class AppMainController {

    @FXML private Label userGreetingLabel;
    @FXML private AnchorPane mainPanel;

    private Parent loginComponent;
    private Parent sheetsManagementComponent;

    private LoginController loginController;
    private SheetsManagementController sheetsManagementController;

    private final StringProperty currentUserName;

    public AppMainController() {
        currentUserName = new SimpleStringProperty("Guest");
    }

    @FXML
    public void initialize() {
        userGreetingLabel.textProperty().bind(Bindings.concat("Hello ", currentUserName));

        loadLoginPage();
        loadSheetsManagementPage();
    }

    public String getUserName() {
        return this.currentUserName.get();
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource("/login/login.fxml");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSheetsManagementPage() {
        URL sheetsPageUrl = getClass().getResource("/sheetsManagement/sheetsManagement.fxml");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(sheetsPageUrl);
            sheetsManagementComponent = fxmlLoader.load();
            sheetsManagementController = fxmlLoader.getController();
            sheetsManagementController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToSheetsManagement() {
        setMainPanelTo(sheetsManagementComponent);
        sheetsManagementController.setActive();
    }

    public void switchToViewSheet(SheetSummaryDTO selectedSheet) {
        if (selectedSheet == null) {
            showError("Please select a sheet to view.");
            return;
        }

        String finalUrl = HttpUrl
                .parse("http://localhost:8080/shticell/getSheet")
                .newBuilder()
                .addQueryParameter("sheetName", selectedSheet.getName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to load sheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String sheetJson = response.body().string();

                    // המרה של JSON ל- SheetDTOImpl כדי לוודא שהממשק הופך למימוש
                    SheetDTOImpl sheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);

                    Platform.runLater(() -> showSheetView(sheetDTO));
                } else {
                    Platform.runLater(() -> showError("Failed to load sheet: " + response.message()));
                }
            }
        });
    }


    private void showSheetView(SheetDTO sheetDTO) {
        try {
            URL sheetPageUrl = getClass().getResource("/sheetView/main.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(sheetPageUrl);
            Parent sheetComponent = fxmlLoader.load();
            MainController mainController = fxmlLoader.getController();

            // הגדרת הגיליון ב-MainController
            mainController.setSheetDTO(sheetDTO);


            setMainPanelTo(sheetComponent);
        } catch (IOException e) {
            showError("Failed to load sheet view: " + e.getMessage());
        }
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set("Guest");
            setMainPanelTo(loginComponent);
        });
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void close() {

    }
}
