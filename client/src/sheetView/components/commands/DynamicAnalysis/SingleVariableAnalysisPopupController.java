package sheetView.components.commands.DynamicAnalysis;


import com.google.gson.Gson;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetView.components.sheet.SheetController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;

public class SingleVariableAnalysisPopupController {

    @FXML
    private Label selectedCellLabel;

    @FXML
    private TextField minValueField;

    @FXML
    private TextField maxValueField;

    @FXML
    private TextField stepSizeField;

    @FXML
    private Button applyButton;

    @FXML
    private Slider valueSlider;

    private SheetController sheetController;
    private String selectedCell;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    public void setSelectedCell(String cellId) {
        this.selectedCell = cellId;
        selectedCellLabel.setText("Selected Cell: " + cellId);
    }

    @FXML
    private void initialize() {
        valueSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            handleSliderValueChanged(newValue.doubleValue());
        });
    }

    @FXML
    private void handleApplyButton() {
        try {
            double minValue = Double.parseDouble(minValueField.getText());
            double maxValue = Double.parseDouble(maxValueField.getText());
            double stepSize = Double.parseDouble(stepSizeField.getText());

            if (minValue >= maxValue) {
                showError("Minimum value must be less than maximum value.");
                return;
            }

            valueSlider.setMin(minValue);
            valueSlider.setMax(maxValue);
            valueSlider.setBlockIncrement(stepSize);
            valueSlider.setMajorTickUnit(stepSize);
            valueSlider.setValue(minValue);
            valueSlider.setShowTickMarks(true);
            valueSlider.setShowTickLabels(true);

            // Initialize the dynamic analysis with the initial value
            performDynamicAnalysis(selectedCell, minValue);

        } catch (NumberFormatException e) {
            showError("Please enter valid numerical values for min, max, and step size.");
        }
    }

    private void handleSliderValueChanged(double newValue) {
        if (sheetController != null && selectedCell != null) {
            performDynamicAnalysis(selectedCell, newValue);
        }
    }

    private void performDynamicAnalysis(String cellId, double newValue) {
        String sheetName = sheetController.getCurrentSheet().getName();
        String finalUrl = Constants.SINGLE_DYNAMIC_ANALYSIS; // Update the constant accordingly

        HttpUrl url = HttpUrl.parse(finalUrl);
        if (url == null) {
            showError("Invalid URL for dynamic analysis.");
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("sheetName", sheetName)
                .add("cellId", cellId)
                .add("newValue", String.valueOf(newValue))
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

    @FXML
    private void handleCloseButton() {
        // Restore the original sheet
        sheetController.displaySheet();

        // Close the popup window
        Stage stage = (Stage) selectedCellLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dynamic Analysis Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
