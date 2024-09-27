package javaFX.commands;

import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
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
                sheetController.setColumnWidth(newValue);
            }
        });

        rowHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                sheetController.setRowHeight(newValue);
            }
        });
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
            sheetController.setColumnWidth(colWidth);
        }
    }

    // Set row height when user selects a value from spinner
    @FXML
    private void handleSetRowHeight() {
        Integer rowHeight = rowHeightSpinner.getValue();
        if (rowHeight != null) {
            sheetController.setRowHeight(rowHeight);
        }
    }

    // Reset the design of the selected cell
    @FXML
    private void handleResetCellDesign() {
        sheetController.resetCellDesign();
    }
}
