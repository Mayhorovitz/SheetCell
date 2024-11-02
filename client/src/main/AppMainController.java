package main;

import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import login.LoginController;
import sheetView.MainController;
import sheetsManagement.SheetsManagementController;

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

        try {
            URL sheetPageUrl = getClass().getResource("/sheetView/main.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(sheetPageUrl);
            Parent sheetComponent = fxmlLoader.load();
            MainController mainController = fxmlLoader.getController();

            mainController.setSheetDTO(sheetDTO);
            mainController.setRangeController(sheetsManagementController.getRangeController());
            mainController.setSheetController(sheetsManagementController.getSheetController());
            mainController.setActionLineController(sheetsManagementController.getActionLineController());

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

    public void updateHttpLine(String data) {
    }

    public void close() {
    }

    public void showError(String s) {
    }

    public void showMessage(String s) {
    }
}
