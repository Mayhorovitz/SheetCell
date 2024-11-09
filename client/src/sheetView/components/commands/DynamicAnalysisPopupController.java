package sheetView.components.commands;

import com.google.gson.Gson;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import sheetView.components.sheet.SheetController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;


/**
 * Controller for the dynamic analysis popup, allowing the user to perform analysis on a selected cell.
 */
public class DynamicAnalysisPopupController {

    @FXML
    private Label selectedCellLabel;  // Label to display the selected cell ID

    @FXML
    private TextField minValueField;  // TextField for the minimum value input

    @FXML
    private TextField maxValueField;  // TextField for the maximum value input

    @FXML
    private TextField stepSizeField;  // TextField for the step size input
    @FXML
    private Button applyButton;

    @FXML
    private Slider valueSlider;  // Slider to adjust the value dynamically

    private SheetController sheetController;
    private String selectedCell;

    // Set the SheetController to allow interactions with the sheet
    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }


    // Initialize the dynamic analysis UI components
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

            // Initialize the temporary sheet with the current sheet data

        } catch (NumberFormatException e) {
            showError("Please enter valid numerical values for min, max, and step size.");
        }
    }

    // Handle slider value change to update the sheet temporarily
    private void handleSliderValueChanged(double newValue) {
        if (sheetController != null && selectedCell != null) {
            // Send the new value and selected cell to the server to get updated sheet data
            performDynamicAnalysis(selectedCell, newValue);
        }
    }
    private void performDynamicAnalysis(String cellId, double newValue) {
        String sheetName = sheetController.getCurrentSheet().getName();
        String finalUrl = Constants.DYNAMIC_ANALYSIS;

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
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to perform dynamic analysis: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String sheetJson = response.body().string();
                    SheetDTOImpl updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);
                    Platform.runLater(() -> {
                        // Display the updated sheet without changing the actual sheet controller's data
                        sheetController.displayTemporarySheet(updatedSheetDTO);
                    });
                } else {
                    Platform.runLater(() -> showError("Failed to perform dynamic analysis: " + response.message()));
                }
            }
        });
    }



    public void setSelectedCell(String cellId) {
        this.selectedCell = cellId;
        selectedCellLabel.setText("Selected Cell: " + cellId);

    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
