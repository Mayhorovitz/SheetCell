package javaFX.sortAndFilter;


import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SortPopupController {

    @FXML
    private TextField rangeField;  // TextField for range input

    @FXML
    private TextField columnsField;  // TextField for columns to sort by

    private SheetController sheetController;  // Reference to SheetController

    // Method to set the SheetController
    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    @FXML
    private void handleSortAction() {
        String range = rangeField.getText();
        String columns = columnsField.getText();

        // Validate the inputs (e.g., check if range and columns are in the correct format)
        if (validateInputs(range, columns)) {
            // Pass the range and columns to the SheetController to perform the sorting
            sheetController.sortRangeByColumns(range, columns);

            // Close the popup window
            Stage stage = (Stage) rangeField.getScene().getWindow();
            stage.close();
        } else {
            // Show error message to the user (you can add this functionality later)
            System.out.println("Invalid input.");
        }
    }

    // Validation method to check range and columns
    private boolean validateInputs(String range, String columns) {
        // Add validation logic here (e.g., regex check for valid range and column inputs)
        return !range.isEmpty() && !columns.isEmpty();
    }
}
