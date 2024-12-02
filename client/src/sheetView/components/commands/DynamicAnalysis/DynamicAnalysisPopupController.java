package sheetView.components.commands.DynamicAnalysis;

import com.google.gson.Gson;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetView.components.sheet.SheetController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DynamicAnalysisPopupController {

    @FXML
    private VBox cellsContainer;
    private SheetController sheetController;
    private List<CellAnalysisParameters> cellParametersList = new ArrayList<>();

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    @FXML
    private void handleAddCellButton() {
        addCellControls();
    }

    private void addCellControls() {
        CellAnalysisParameters cellParams = new CellAnalysisParameters();

        HBox cellControls = new HBox(8);

        // TextField for Cell ID
        TextField cellIdField = new TextField();
        cellIdField.setPromptText("Cell ID");
        cellIdField.setPrefWidth(40);
        cellParams.setCellIdField(cellIdField);

        // TextField for Min Value
        TextField minValueField = new TextField();
        minValueField.setPromptText("Min");
        minValueField.setPrefWidth(40);
        cellParams.setMinValueField(minValueField);

        // TextField for Max Value
        TextField maxValueField = new TextField();
        maxValueField.setPromptText("Max");
        maxValueField.setPrefWidth(40);
        cellParams.setMaxValueField(maxValueField);

        // TextField for Step Size
        TextField stepSizeField = new TextField();
        stepSizeField.setPromptText("Step");
        stepSizeField.setPrefWidth(40);
        cellParams.setStepSizeField(stepSizeField);

        // Slider
        Slider valueSlider = new Slider();
        valueSlider.setShowTickMarks(true);
        valueSlider.setShowTickLabels(true);
        cellParams.setValueSlider(valueSlider);

        // Add controls to HBox
        cellControls.getChildren().addAll(
                new Label("Cell ID:"), cellIdField,
                new Label("Min:"), minValueField,
                new Label("Max:"), maxValueField,
                new Label("Step:"), stepSizeField,
                valueSlider
        );

        // Add HBox to cellsContainer
        cellsContainer.getChildren().add(cellControls);

        // Add to list
        cellParametersList.add(cellParams);

        // Add listener to slider
        valueSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleSliderValueChanged();
        });
    }

    @FXML
    private void handleStartAnalysisButton() {
        for (CellAnalysisParameters cellParams : cellParametersList) {
            try {
                String cellId = cellParams.getCellIdField().getText();
                if (cellId == null || cellId.isEmpty()) {
                    showError("Please enter a valid Cell ID.");
                    return;
                }
                cellParams.setCellId(cellId);

                // Parse min, max, step size
                double minValue = Double.parseDouble(cellParams.getMinValueField().getText());
                double maxValue = Double.parseDouble(cellParams.getMaxValueField().getText());
                double stepSize = Double.parseDouble(cellParams.getStepSizeField().getText());

                if (minValue >= maxValue) {
                    showError("Minimum value must be less than maximum value for cell " + cellId);
                    return;
                }

                // Configure slider
                Slider slider = cellParams.getValueSlider();
                slider.setMin(minValue);
                slider.setMax(maxValue);
                slider.setBlockIncrement(stepSize);
                slider.setMajorTickUnit(stepSize);
                slider.setValue(minValue);

            } catch (NumberFormatException e) {
                showError("Please enter valid numerical values for min, max, and step size for cell " + cellParams.getCellIdField().getText());
                return;
            }
        }

        handleSliderValueChanged();
    }

    private void handleSliderValueChanged() {
        // Collect current values from all sliders
        Map<String, Double> cellValues = new HashMap<>();
        for (CellAnalysisParameters cellParams : cellParametersList) {
            String cellId = cellParams.getCellIdField().getText();
            double newValue = cellParams.getValueSlider().getValue();
            cellValues.put(cellId, newValue);
        }

        // Send the new values to the server
        performDynamicAnalysis(cellValues);
    }

    private void performDynamicAnalysis(Map<String, Double> cellValues) {
        String sheetName = sheetController.getCurrentSheet().getName();
        String finalUrl = Constants.DYNAMIC_ANALYSIS;

        HttpUrl url = HttpUrl.parse(finalUrl);
        if (url == null) {
            showError("Invalid URL for dynamic analysis.");
            return;
        }

        // Build JSON payload
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(cellValues);

        RequestBody formBody = new FormBody.Builder()
                .add("sheetName", sheetName)
                .add("cellValues", jsonPayload)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to perform dynamic analysis: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String sheetJson = response.body().string();
                    SheetDTOImpl updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);
                    Platform.runLater(() -> {
                        sheetController.displayTemporarySheet(updatedSheetDTO);
                    });
                } else {
                    String errorMessage = response.body().string();
                    Platform.runLater(() -> showError("Failed to perform dynamic analysis: " + errorMessage));
                }
            }
        });
    }




    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dynamic Analysis Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
