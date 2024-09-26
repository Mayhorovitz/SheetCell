package javaFX.commands;

import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class CommandsController {

    @FXML
    private ColorPicker bgColorPicker;  // Background color picker
    @FXML
    private ColorPicker textColorPicker;  // Text color picker
    @FXML
    private TextField columnWidthInput;  // Column width input
    @FXML
    private TextField rowHeightInput;  // Row height input

    private SheetController sheetController;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
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

    // Set column width
    @FXML
    private void handleSetColumnWidth() {
        try {
            int colWidth = Integer.parseInt(columnWidthInput.getText());
            sheetController.setColumnWidth(colWidth);
        } catch (NumberFormatException e) {
            System.out.println("Invalid column width input.");
        }
    }

    // Set row height
    @FXML
    private void handleSetRowHeight() {
        try {
            int rowHeight = Integer.parseInt(rowHeightInput.getText());
            sheetController.setRowHeight(rowHeight);
        } catch (NumberFormatException e) {
            System.out.println("Invalid row height input.");
        }
    }

    // Reset the design of the selected cell
    @FXML
    private void handleResetCellDesign() {
    }
}
