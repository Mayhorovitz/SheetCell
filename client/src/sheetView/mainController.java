package sheetView;

import com.google.gson.Gson;
import dto.api.CellDTO;
import dto.api.SheetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.commands.CommandsController;
import sheetView.components.ranges.RangeController;
import sheetView.components.readOnlyPopup.ReadOnlyPopupController;
import sheetView.components.sheet.SheetController;
import util.http.HttpClientUtil;

import java.io.IOException;

public class MainController {

    private static final String SERVER_URL = "http://localhost:8080/shticell";


    @FXML
    private ComboBox<String> versionSelector;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private SheetController sheetController;

    @FXML
    private RangeController rangeController;

    @FXML
    private ActionLineController actionLineController;

    private CommandsController commandsController;

    private UIModel uiModel;

    @FXML
    private void initialize() {
        uiModel = new UIModel();

        // Initialize controllers
        if (actionLineController != null) {
            actionLineController.setMainController(this);
        }
    }

    @FXML
    public void handleCellSelection(String cellId) {
        String finalUrl = HttpUrl
                .parse(SERVER_URL + "/getCellInfo")
                .newBuilder()
                .addQueryParameter("cellId", cellId)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Failed to get cell info: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    CellDTO selectedCellDTO = new Gson().fromJson(response.body().string(), CellDTO.class);
                    Platform.runLater(() -> {
                        if (selectedCellDTO != null) {
                            actionLineController.updateActionLine(selectedCellDTO);
                            sheetController.resetRangeHighlight();
                            sheetController.highlightDependenciesAndInfluences(selectedCellDTO);
                        } else {
                            actionLineController.updateActionLine(null);
                        }
                    });
                } else {
                    Platform.runLater(() -> showErrorAlert("Failed to get cell info: " + response.message()));
                }
            }
        });
    }

    public void handleUpdateCell(String newValue, String selectedCell) {
        if (selectedCell != null && !newValue.isEmpty()) {
            String finalUrl = HttpUrl
                    .parse(SERVER_URL + "/updateCell")
                    .newBuilder()
                    .addQueryParameter("sheetName", sheetName)
                    .addQueryParameter("cellId", selectedCell)
                    .addQueryParameter("newValue", newValue)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showErrorAlert("Failed to update cell: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String sheetJson = response.body().string();
                        SheetDTO updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTO.class);
                        Platform.runLater(() -> {
                            sheetController.displaySheet(updatedSheetDTO);
                            populateVersionSelector(sheetName);
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
    @FXML
    private void handleVersionSelection() {
        String selectedVersion = versionSelector.getValue();
        if (selectedVersion != null) {
            String finalUrl = HttpUrl
                    .parse(SERVER_URL + "/getSheetVersion")
                    .newBuilder()
                    .addQueryParameter("version", selectedVersion)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showErrorAlert("Failed to get sheet version: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String sheetJson = response.body().string();
                        SheetDTO selectedSheetDTO = new Gson().fromJson(sheetJson, SheetDTO.class);
                        Platform.runLater(() -> showSheetVersionPopup(selectedSheetDTO));
                    } else {
                        Platform.runLater(() -> showErrorAlert("Failed to get sheet version: " + response.message()));
                    }
                }
            });
        }
    }

    private void showSheetVersionPopup(SheetDTO sheetDTO) {
        try {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("View Only - Sheet Version");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaFX/readOnlyPopup/readOnlyPopup.fxml"));
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

    private void populateVersionSelector() {
        String finalUrl = HttpUrl
                .parse(SERVER_URL + "/getVersions")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Failed to get versions: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String[] versions = new Gson().fromJson(response.body().string(), String[].class);
                    Platform.runLater(() -> {
                        versionSelector.getItems().clear();
                        versionSelector.getItems().addAll(versions);
                    });
                } else {
                    Platform.runLater(() -> showErrorAlert("Failed to get versions: " + response.message()));
                }
            }
        });
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


}
