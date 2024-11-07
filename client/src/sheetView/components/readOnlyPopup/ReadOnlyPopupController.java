package sheetView.components.readOnlyPopup;

import com.google.gson.Gson;
import dto.impl.CellDTOImpl;
import dto.api.SheetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.UIModel;
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.sheet.SheetController;
import util.http.HttpClientUtil;

import java.io.IOException;

/**
 * Controller for displaying a read-only version of a sheet.
 */
public class ReadOnlyPopupController {

    private static final String SERVER_URL = "http://localhost:8080/shticell";

    @FXML
    private GridPane versionGridPane;

    @FXML
    private ActionLineController actionLineController;

    private SheetController sheetController;
    private UIModel uiModel;

    private int versionNumber;
    private SheetDTO sheetToDisplay;

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setSheetToDisplay(SheetDTO sheetDTO) {
        this.sheetToDisplay = sheetDTO;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void displaySheet() {
        initializeSheetController();

        if (sheetToDisplay == null && versionNumber > 0) {
            // Retrieve sheet by version if not provided
            String finalUrl = HttpUrl.parse(SERVER_URL + "/getSheetVersion")
                    .newBuilder()
                    .addQueryParameter("version", String.valueOf(versionNumber))
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> throwRuntimeError("Failed to get sheet version: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        sheetToDisplay = new Gson().fromJson(response.body().string(), SheetDTO.class);
                        Platform.runLater(() -> {
                            if (sheetToDisplay != null) {
                                sheetController.initializeSheet(sheetToDisplay);
                            } else {
                                throwRuntimeError("No sheet to display.");
                            }
                        });
                    } else {
                        Platform.runLater(() -> throwRuntimeError("Failed to get sheet version: " + response.message()));
                    }
                }
            });
        } else if (sheetToDisplay != null) {
            sheetController.initializeSheet(sheetToDisplay);
        } else {
            throwRuntimeError("No sheet to display.");
        }
    }

    private void initializeSheetController() {
        // Initialize the SheetController
        sheetController = new SheetController();
        sheetController.setReadOnly(true);

        if (uiModel == null) {
            uiModel = new UIModel();
        }
        sheetController.setUiModel(uiModel);

        // Set the GridPane in SheetController
        sheetController.setSpreadsheetGrid(versionGridPane);

        // Set cell selection behavior
        sheetController.setOnCellSelected(cellId -> {
            String finalUrl = HttpUrl.parse(SERVER_URL + "/getCellInfo")
                    .newBuilder()
                    .addQueryParameter("cellId", cellId)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> throwRuntimeError("Failed to get cell info: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        CellDTOImpl selectedCellDTO = new Gson().fromJson(response.body().string(), CellDTOImpl.class);
                        Platform.runLater(() -> {
                            if (selectedCellDTO != null) {
                                sheetController.highlightDependenciesAndInfluences(selectedCellDTO);
                                actionLineController.updateActionLine(selectedCellDTO);
                            }
                        });
                    } else {
                        Platform.runLater(() -> throwRuntimeError("Failed to get cell info: " + response.message()));
                    }
                }
            });
        });
    }

    @FXML
    public void initialize() {
        if (actionLineController != null) {
            actionLineController.setReadOnly(true);
        }
    }

    // Utility method to throw a runtime error
    private void throwRuntimeError(String message) {
        throw new RuntimeException(message);
    }
}
