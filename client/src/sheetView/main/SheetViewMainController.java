package sheetView.main;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.AppMainController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.commands.CommandsController;
import sheetView.components.ranges.RangeController;
import sheetView.components.readOnlyPopup.ReadOnlyPopupController;
import sheetView.components.sheet.SheetController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;

public class SheetViewMainController {

    @FXML
    private SheetController sheetController;

    @FXML
    private RangeController rangeController;

    @FXML
    private ActionLineController actionLineController;
    @FXML
    private CommandsController commandsController;

    private AppMainController mainController;

    private SheetDTO currentSheet;

    private UIModel uiModel;
    private boolean isReadOnly;
    private VersionRefresher versionRefresher;

    // Initialize UI components and set controller references
    @FXML
    private void initialize() {
        uiModel = new UIModel();

        actionLineController.setMainController(this);
        sheetController.setMainController(this);
        sheetController.setUiModel(uiModel);
        commandsController.setSheetController(sheetController);
        rangeController.setMainController(this);
    }

    // Set the current sheet and configure read-only mode
    public void setSheetDTO(SheetDTO sheetDTO, boolean isReadOnly) {
        this.currentSheet = sheetDTO;
        this.isReadOnly = isReadOnly;
        rangeController.updateRangeListView();

        if (sheetController != null && sheetDTO != null) {
            sheetController.initializeSheet(sheetDTO);
            actionLineController.setVersionSelectorItems(sheetDTO.getVersion());
            actionLineController.setReadOnly(isReadOnly);
            versionRefresher = new VersionRefresher(this);
        }
    }

    // Start the version refresher to keep the sheet up to date
    public void startVersionRefresher() {
        if (versionRefresher != null) {
            versionRefresher.startRefreshing();
        }
    }

    // Stop the version refresher
    public void stopVersionRefresher() {
        if (versionRefresher != null) {
            versionRefresher.stopRefreshing();
        }
    }

    // Handle selection of a cell and update related information
    @FXML
    public void handleCellSelection(String cellId) {
        try {
            CellDTOImpl selectedCell = currentSheet.getCells().get(cellId);
            if (selectedCell != null) {
                actionLineController.updateActionLine(selectedCell, cellId);
                sheetController.resetRangeHighlight();
                sheetController.highlightDependenciesAndInfluences(selectedCell);
            } else {
                actionLineController.updateActionLine(null, cellId);
                sheetController.highlightDependenciesAndInfluences(selectedCell);
            }
        } catch (IllegalArgumentException e) {
            actionLineController.updateActionLine(null, cellId);
        }
    }

    // Handle updating a cell's value if allowed
    public void handleUpdateCell(String newValue, String selectedCell) {
        if (selectedCell != null && !newValue.isEmpty()) {
            int currentVersion = currentSheet.getVersion();
            if (!isOnLatestVersion(currentVersion)) {
                showErrorAlert("Update failed: You are not viewing the latest version of the sheet. Please update to the latest version and try again.");
                return;
            }

            HttpUrl url = HttpUrl.parse(Constants.UPDATE_CELL);
            if (url == null) {
                showErrorAlert("Invalid URL for updating cell.");
                return;
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("sheetName", currentSheet.getName())
                    .add("cellId", selectedCell)
                    .add("newValue", newValue)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> showErrorAlert("Failed to update cell: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String sheetJson = response.body().string();
                        SheetDTOImpl updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);
                        Platform.runLater(() -> {
                            updateCurrentSheet(updatedSheetDTO);
                        });
                    } else {
                        Platform.runLater(() -> showErrorAlert("Failed to update cell: " + response.message()));
                    }
                }
            });
        } else {
            showErrorAlert("Please select a cell and enter a new value.");
        }
    }

    // Check if the user is on the latest version of the sheet
    private boolean isOnLatestVersion(int currentVersion) {
        return currentVersion == versionRefresher.getLatestVersion();
    }

    // Display a read-only popup with a specific sheet version
    public void showSheetVersionPopup(SheetDTO sheetDTO) {
        try {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("View Only - Sheet Version");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/readOnlyPopup/readOnlyPopup.fxml"));
            VBox root = loader.load();

            ReadOnlyPopupController popupController = loader.getController();
            popupController.setUiModel(uiModel);
            popupController.setSheetToDisplay(sheetDTO);
            popupController.displaySheet();

            Scene scene = new Scene(root, 800, 600);
            popupStage.setScene(scene);
            popupStage.show();
        } catch (IOException e) {
            showErrorAlert("Failed to load sheet version view: " + e.getMessage());
        }
    }

    // Show an error alert with a specific message
    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Set the range controller reference
    public void setRangeController(RangeController rangeController) {
        this.rangeController = rangeController;
    }

    // Set the sheet controller reference
    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    // Get the UI model
    public UIModel getUIModel() {
        return this.uiModel;
    }

    // Get the sheet controller
    public SheetController getSheetController() {
        return sheetController;
    }

    // Set the action line controller reference
    public void setActionLineController(ActionLineController actionLineController) {
        this.actionLineController = actionLineController;
    }

    // Get the current sheet name
    public String getCurrentSheetName() {
        return currentSheet.getName();
    }

    // Get the current sheet
    public SheetDTO getCurrentSheet() {
        return this.currentSheet;
    }

    // Check if the sheet is in read-only mode
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    // Show a hint that a new version is available
    public void showVersionUpdateHint(int latestVersion) {
        actionLineController.updateVersionSelector(latestVersion);
    }

    // Update the current sheet with a new version
    public void updateCurrentSheet(SheetDTOImpl updatedSheetDTO) {
        this.currentSheet = updatedSheetDTO;
        sheetController.setCurrentSheet(updatedSheetDTO);
        sheetController.displaySheet();
        actionLineController.setVersionSelectorItems(updatedSheetDTO.getVersion());
        rangeController.updateRangeListView();
    }

    // Handle return to sheet management view
    public void handleReturnToSheetManagment() {
        mainController.switchToSheetsManagement();
    }

    // Set the main application controller reference
    public void setMainController(AppMainController appMainController) {
        this.mainController = appMainController;
    }
}
