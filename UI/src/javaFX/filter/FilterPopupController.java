package javaFX.filter;

import engine.api.Engine;
import javaFX.main.UIModel;
import javaFX.readOnlyPopup.ReadOnlyPopupController;
import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sheet.api.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterPopupController {

    @FXML
    private TextField rangeTextField;

    @FXML
    private TextField columnTextField;

    @FXML
    private VBox valuesContainer;

    private List<Integer> originalRowNumbers = new ArrayList<>();


    private SheetController sheetController;
    private Engine engine;
    private UIModel uiModel;

    private String selectedRange;
    private String selectedColumn;
    private List<String> uniqueValues;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
        this.engine = sheetController.getEngine();
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

        try {
            uniqueValues = engine.getUniqueValuesInRangeColumn(selectedRange, selectedColumn);

            if (uniqueValues.isEmpty()) {
                showError("No values found in the specified column and range.");
                return;
            }

            valuesContainer.getChildren().clear();

            for (String value : uniqueValues) {
                CheckBox checkBox = new CheckBox(value);
                valuesContainer.getChildren().add(checkBox);
            }

        } catch (Exception e) {
            showError("Error loading values: " + e.getMessage());
        }
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

        try {
            Sheet filteredSheet = engine.filterSheetByValues(selectedRange, selectedColumn, selectedValues, originalRowNumbers);

            // Open ReadOnlyPopup to display the filtered sheet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaFX/readOnlyPopup/readOnlyPopup.fxml"));
            VBox root = loader.load();

            ReadOnlyPopupController popupController = loader.getController();
            popupController.setEngine(engine);
            popupController.setUiModel(uiModel);
            popupController.setSheetToDisplay(filteredSheet);

            // העברת originalRowNumbers ל-ReadOnlyPopupController

            popupController.displayFilterSheet();



            Stage stage = new Stage();
            stage.setTitle("Filtered Sheet");
            stage.setScene(new Scene(root));
            stage.show();

            // Close the filter popup
            Stage currentStage = (Stage) valuesContainer.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showError("Error applying filter: " + e.getMessage());
        }
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
