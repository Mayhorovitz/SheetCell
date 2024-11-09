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
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.commands.CommandsController;
import sheetView.components.ranges.RangeController;
import sheetView.components.readOnlyPopup.ReadOnlyPopupController;
import sheetView.components.sheet.SheetController;
import util.http.HttpClientUtil;

import java.io.IOException;

public class SheetViewMainController {

    private static final String SERVER_URL = "http://localhost:8080/shticell";

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


    @FXML
    private void initialize() {
        uiModel = new UIModel();

        actionLineController.setMainController(this);
        sheetController.setMainController(this);
        sheetController.setUiModel(uiModel);
        commandsController.setSheetController(sheetController);
        rangeController.setMainController(this);
        versionRefresher = new VersionRefresher(this);


    }

    public void setSheetDTO(SheetDTO sheetDTO, boolean isReadOnly) {
        this.currentSheet = sheetDTO;
        this.isReadOnly = isReadOnly;
        rangeController.updateRangeListView();

        if (sheetController != null && sheetDTO != null) {
            sheetController.initializeSheet(sheetDTO);
            actionLineController.setVersionSelectorItems(sheetDTO.getVersion());
            actionLineController.setReadOnly(isReadOnly);
            versionRefresher.startRefreshing();

        }
    }

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
            actionLineController.updateActionLine(null , cellId);
        }
    }

    public void handleUpdateCell(String newValue, String selectedCell) {
        if (selectedCell != null && !newValue.isEmpty()) {
            // בדיקת אם המשתמש נמצא על הגרסה העדכנית ביותר
            int currentVersion = currentSheet.getVersion();
            if (!isOnLatestVersion(currentVersion)) {
                showErrorAlert("Update failed: You are not viewing the latest version of the sheet. Please update to the latest version and try again.");
                return;
            }

            // ביצוע העדכון אם המשתמש על הגרסה העדכנית ביותר
            String finalUrl = SERVER_URL + "/updateCell";

            HttpUrl url = HttpUrl.parse(finalUrl);
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
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showErrorAlert("Failed to update cell: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
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

    private boolean isOnLatestVersion(int currentVersion) {
        // פונקציה לבדיקת אם המשתמש על הגרסה העדכנית ביותר
        return currentVersion == versionRefresher.getLatestVersion();
    }


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


    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setRangeController(RangeController rangeController) {
        this.rangeController = rangeController;
    }

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    public UIModel getUIModel() {
        return this.uiModel;
    }

    public SheetController getSheetController() {
        return sheetController;
    }

    public void setActionLineController(ActionLineController actionLineController) {
        this.actionLineController = actionLineController;
    }


    public String getCurrentSheetName() {
        return currentSheet.getName();
    }

    public SheetDTO getCurrentSheet() {
        return this.currentSheet;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public void stopVersionRefresher() {
        if (versionRefresher != null) {
            versionRefresher.stopRefreshing();
        }
    }

    public void showVersionUpdateHint(int latestVersion) {
        actionLineController.updateVersionSelector(latestVersion);
    }

    public void updateCurrentSheet(SheetDTOImpl updatedSheetDTO) {
        this.currentSheet = updatedSheetDTO;
        sheetController.setCurrentSheet(updatedSheetDTO);
        sheetController.displaySheet();
        actionLineController.setVersionSelectorItems(updatedSheetDTO.getVersion());
        rangeController.updateRangeListView();
    }

    public void handleReturnToSheetManagment() {
        mainController.switchToSheetsManagement();

    }

    public void setMainController(AppMainController appMainController) {
        this.mainController = appMainController;
    }
}

