package sheetView.components.commands;


import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sheetView.components.filter.FilterPopupController;
import sheetView.components.sheet.SheetController;
import sheetView.components.sort.SortPopupController;

import java.io.IOException;

/**
 * Controller for the commands panel, handling styling and data manipulation actions.
 */
public class CommandsController {

    @FXML
    private ColorPicker bgColorPicker;  // Background color picker
    @FXML
    private ColorPicker textColorPicker;  // Text color picker
    @FXML
    private Spinner<Integer> columnWidthSpinner;  // Column width spinner
    @FXML
    private Spinner<Integer> rowHeightSpinner;  // Row height spinner
    @FXML
    private ComboBox<String> alignmentComboBox;  // Dropdown for alignment
    @FXML
    private Button analyzeButton;

    private SheetController sheetController;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    @FXML
    private void initialize() {
        // Update Spinner ranges for column width and row height
        columnWidthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 10));
        rowHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 10));

        // Disable direct text input to make it strictly spinner-based
        columnWidthSpinner.setEditable(false);
        rowHeightSpinner.setEditable(false);

        // Add listeners to update size when the spinner value changes
        columnWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedColIndex = sheetController.getSelectedColumnIndex();
                sheetController.setColumnWidth(selectedColIndex, newValue.doubleValue());
            }
        });

        rowHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedRowIndex = sheetController.getSelectedRowIndex();
                sheetController.setRowHeight(selectedRowIndex, newValue.doubleValue());
            }
        });

        alignmentComboBox.getItems().addAll("Left", "Center", "Right");
        alignmentComboBox.setOnAction(event -> handleSetColumnAlignment());
    }

    @FXML
    private void handleApplyBackgroundColor() {
        if (!sheetController.isReadOnly()) {
            Color selectedColor = bgColorPicker.getValue();
        if (selectedColor != null) {
            sheetController.applyBackgroundColorToSelectedCell(selectedColor);
        }
        } else {
            showErrorAlert("You do not have permission to change the cell design.");
        }
    }

    @FXML
    private void handleApplyTextColor() {
        if (!sheetController.isReadOnly()) {
            Color selectedTextColor = textColorPicker.getValue();
            if (selectedTextColor != null) {
                sheetController.applyTextColorToSelectedCell(selectedTextColor);
            }

    } else {
            showErrorAlert("You do not have permission to change the cell design.");
    }
    }

    @FXML
    private void handleSetColumnWidth() {
        Integer colWidth = columnWidthSpinner.getValue();
        if (colWidth != null) {
            // Add logic to get the selected column index
            int selectedColIndex = sheetController.getSelectedColumnIndex(); // You will need to implement this method
            sheetController.setColumnWidth(selectedColIndex, colWidth.doubleValue());
        }
    }

    // Set row height when user selects a value from spinner
    @FXML
    private void handleSetRowHeight() {
        Integer rowHeight = rowHeightSpinner.getValue();
        if (rowHeight != null) {
            // Add logic to get the selected row index
            int selectedRowIndex = sheetController.getSelectedRowIndex(); // You will need to implement this method
            sheetController.setRowHeight(selectedRowIndex, rowHeight.doubleValue());
        }
    }

    // Set column alignment when user selects a value from the combo box
    @FXML
    private void handleSetColumnAlignment() {
        String alignment = alignmentComboBox.getValue();
        if (alignment != null) {
            int selectedColIndex = sheetController.getSelectedColumnIndex();
            sheetController.setColumnAlignment(selectedColIndex, alignment);
        }
    }

    // Reset the design of the selected cell
    @FXML
    private void handleResetCellDesign() {
        if (!sheetController.isReadOnly()) {

            sheetController.resetCellDesign();

    } else {
        showErrorAlert("You do not have permission to change the cell design.");
    }
    }

    @FXML
    public void handleSortButton() {
        try {
            // Load the FXML for the sort popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/sort/SortPopup.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the SheetController to it
            SortPopupController sortPopupController = loader.getController();
            sortPopupController.setSheetController(sheetController);

            // Create a new stage for the popup
            Stage stage = new Stage();
            stage.setTitle("Sort Data");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFilterButton() {
        try {
            // Load the FXML for the filter popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/filter/FilterPopup.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the SheetController to it
            FilterPopupController filterPopupController = loader.getController();
            filterPopupController.setSheetController(sheetController);

            // Create a new stage for the popup
            Stage stage = new Stage();
            stage.setTitle("Filter Data");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDynamicAnalysis() {
        SheetDTO currentSheet = sheetController.getCurrentSheet();
        String selectedCell = sheetController.getSelectedCellIndex();
        CellDTOImpl cellDTO = sheetController.getCellDTO(selectedCell);
        if (cellDTO == null || selectedCell.isEmpty()) {
            showErrorAlert("Please select a cell before starting dynamic analysis.");
            return;
        }
        if (!isNumeric(cellDTO.getOriginalValue())) {
            showErrorAlert("Selected cell contains a function. Please select a numerical cell.");
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/commands/DynamicAnalysisPopup.fxml"));
            Parent root = loader.load();

            DynamicAnalysisPopupController dynamicAnalysisPopupController = loader.getController();
            dynamicAnalysisPopupController.setSheetController(sheetController);
            dynamicAnalysisPopupController.setSelectedCell(selectedCell);

            Stage stage = new Stage();
            stage.setTitle("Dynamic Analysis");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(event -> sheetController.displayOriginalSheet(currentSheet));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
