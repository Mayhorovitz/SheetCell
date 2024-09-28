package javaFX.commands;

import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;

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

    private SheetController sheetController;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    @FXML
    private void initialize() {
        // Update Spinner ranges for column width (up to 100) and row height (up to 100)
        columnWidthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        rowHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));

        // Disable direct text input to make it strictly spinner-based
        columnWidthSpinner.setEditable(false);
        rowHeightSpinner.setEditable(false);

        // Add listeners to update size when the spinner value changes
        columnWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedColIndex = sheetController.getSelectedColumnIndex(); // Get the selected column index
                sheetController.setColumnWidth(selectedColIndex, newValue.doubleValue());
            }
        });

        rowHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedRowIndex = sheetController.getSelectedRowIndex(); // Get the selected row index
                sheetController.setRowHeight(selectedRowIndex, newValue.doubleValue());
            }
        });

        alignmentComboBox.getItems().addAll("Left", "Center", "Right");
        alignmentComboBox.setOnAction(event -> handleSetColumnAlignment());
    }

    @FXML
    private void handleApplyBackgroundColor() {
        Color selectedColor = bgColorPicker.getValue();
        if (selectedColor != null) {
            sheetController.applyBackgroundColorToSelectedCell(selectedColor);
        }
    }

    @FXML
    private void handleApplyTextColor() {
        Color selectedTextColor = textColorPicker.getValue();
        if (selectedTextColor != null) {
            sheetController.applyTextColorToSelectedCell(selectedTextColor);
        }
    }
    // Set column width when user selects a value from spinner
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
            // Add logic to get the selected column index
            int selectedColIndex = sheetController.getSelectedColumnIndex(); // You will need to implement this method
            sheetController.setColumnAlignment(selectedColIndex, alignment);
        }
    }

    // Reset the design of the selected cell
    @FXML
    private void handleResetCellDesign() {
        sheetController.resetCellDesign();
    }
}
