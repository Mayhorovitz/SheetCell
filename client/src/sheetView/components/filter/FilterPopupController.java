package sheetView.components.filter;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.UIModel;
import sheetView.components.readOnlyPopup.ReadOnlyPopupController;
import sheetView.components.sheet.SheetController;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the filter popup, allowing users to filter data based on column values.
 */
public class FilterPopupController {

    @FXML
    private TextField rangeTextField;

    @FXML
    private TextField columnTextField;

    @FXML
    private VBox valuesContainer;

    private List<Integer> originalRowNumbers = new ArrayList<>();

    private SheetController sheetController;
    private UIModel uiModel;

    private String selectedRange;
    private String selectedColumn;
    private List<String> uniqueValues;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
        this.uiModel = sheetController.getUiModel();
    }

    @FXML
    private void handleLoadValues() {
        selectedRange = rangeTextField.getText().toUpperCase();
        selectedColumn = columnTextField.getText().toUpperCase();

        if (selectedRange == null || selectedRange.isEmpty() || selectedColumn == null || selectedColumn.isEmpty()) {
            showError("Please enter both range and column.");
            return;
        }

        String finalUrl = HttpUrl.parse("http://localhost:8080/shticell/getUniqueValues")
                .newBuilder()
                .addQueryParameter("sheetName", "currentSheet") // Replace with actual sheet name
                .addQueryParameter("range", selectedRange)
                .addQueryParameter("column", selectedColumn)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Error loading values: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    uniqueValues = new Gson().fromJson(response.body().string(), List.class);
                    Platform.runLater(() -> {
                        if (uniqueValues.isEmpty()) {
                            showError("No values found in the specified column and range.");
                            return;
                        }

                        valuesContainer.getChildren().clear();
                        for (String value : uniqueValues) {
                            CheckBox checkBox = new CheckBox(value);
                            valuesContainer.getChildren().add(checkBox);
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Error loading values: " + response.message()));
                }
            }
        });
    }

    @FXML
    private void handleApplyFilter() {
        List<String> selectedValues = valuesContainer.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

        if (selectedValues.isEmpty()) {
            showError("Please select at least one value to filter.");
            return;
        }

        String finalUrl = HttpUrl.parse("http://localhost:8080/shticell/filterSheet")
                .newBuilder()
                .addQueryParameter("sheetName", "currentSheet") // Replace with actual sheet name
                .addQueryParameter("range", selectedRange)
                .addQueryParameter("column", selectedColumn)
                .addQueryParameter("selectedValues", new Gson().toJson(selectedValues))
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Error applying filter: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    SheetDTO filteredSheetDTO = new Gson().fromJson(response.body().string(), SheetDTO.class);
                    Platform.runLater(() -> {
                        try {
                            // Open ReadOnlyPopup to display the filtered sheet
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/readOnlyPopup/readOnlyPopup.fxml"));
                            VBox root = loader.load();

                            ReadOnlyPopupController popupController = loader.getController();
                            popupController.setUiModel(uiModel);
                            popupController.setSheetToDisplay(filteredSheetDTO);

                            popupController.displaySheet();

                            Stage stage = new Stage();
                            stage.setTitle("Filtered Sheet");
                            stage.setScene(new Scene(root));
                            stage.show();

                            // Close the filter popup
                            Stage currentStage = (Stage) valuesContainer.getScene().getWindow();
                            currentStage.close();

                        } catch (IOException e) {
                            showError("Error loading filtered sheet view: " + e.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Error applying filter: " + response.message()));
                }
            }
        });
    }

    private void showError(String message) {
        // Display an error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Filter Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
