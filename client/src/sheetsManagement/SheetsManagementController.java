package sheetsManagement;

import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import main.AppMainController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetsManagement.components.availableSheets.AvailableSheetRefresher;
import sheetsManagement.components.availableSheets.AvailableSheetsController;
import sheetsManagement.components.commands.CommandsController;
import sheetsManagement.components.permissionsTable.PermissionsTableController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;

public class SheetsManagementController {

    private AppMainController mainController;
    private AvailableSheetRefresher availableSheetRefresher;
    private Timer refresherTimer;

    @FXML
    private AvailableSheetsController availableSheetsController;
    @FXML
    private PermissionsTableController permissionsTableController;
    @FXML
    private CommandsController commandsController;
    @FXML
    private VBox uploadSheetBox;
    @FXML
    private Button uploadSheetButton;

    @FXML
    public void initialize() {
        // Set the main controller for the subcomponents
        availableSheetsController.setMainController(this);
        permissionsTableController.setMainController(this);
        commandsController.setMainController(this);

        uploadSheetButton.setOnAction(event -> handleUploadSheet());

        availableSheetsController.getAvailableSheetsTable().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                permissionsTableController.loadPermissionsForSheet(newValue.getName());
            }
        });

        // Start the refresher to update the available sheets periodically
        availableSheetRefresher = new AvailableSheetRefresher(this::updateAvailableSheets);
        refresherTimer = new Timer(true);
        refresherTimer.schedule(availableSheetRefresher, 0, 5000); // Refresh every 5 seconds
    }

    public void setMainController(AppMainController mainController) {
        this.mainController = mainController;
        availableSheetsController.setMainController(this);
        permissionsTableController.setMainController(this);
        commandsController.setMainController(this);
    }

    private void handleUploadSheet() {
        // File chooser to select XML file for uploading
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                String finalUrl = Constants.UPLOAD_SHEET_PAGE;
                MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                multipartBodyBuilder.addFormDataPart("file", selectedFile.getName(), RequestBody.create(selectedFile, MediaType.parse("text/xml")));
                RequestBody requestBody = multipartBodyBuilder.build();
                Request request = new Request.Builder().url(finalUrl).post(requestBody).build();

                HttpClientUtil.getHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> showError("Failed to upload the sheet: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> showMessage("Sheet uploaded successfully."));
                        } else {
                            Platform.runLater(() -> showError("Failed to upload the sheet: " + response.message()));
                        }
                    }
                });
            } catch (Exception e) {
                showError("An error occurred while uploading the sheet: " + e.getMessage());
            }
        }
    }

    private void updateAvailableSheets(List<SheetSummaryDTO> availableSheets) {
        availableSheetsController.getAvailableSheetsTable().getItems().setAll(availableSheets);
    }

    public void updatePermissionsTable(SheetSummaryDTO newValue) {
        permissionsTableController.loadPermissionsForSheet(newValue.getName());
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public AvailableSheetsController getAvailableSheetsController() {
        return availableSheetsController;
    }

    public void handleViewSheet() {
        SheetSummaryDTO selectedSheet = availableSheetsController.getAvailableSheetsTable().getSelectionModel().getSelectedItem();
        if (selectedSheet != null) {
            String permissionType = selectedSheet.getPermissionType();
            if (permissionType.equals("NONE")) {
                showError("You do not have permission to view this sheet.");
            } else {
                boolean isReadOnly = permissionType.equals("READER");
                mainController.switchToViewSheet(selectedSheet, isReadOnly);
            }
        } else {
            showError("Please select a sheet to view.");
        }
    }


    public String getCurrentUserName() {
        return mainController.getUserName();
    }

    public void setActive() {

        if (availableSheetsController != null) {
            availableSheetsController.refreshSheetsTable(); // לדוגמה: לרענן את טבלת הגיליונות
        }
    }
}
