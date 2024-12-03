package sheetView.components.readOnlyPopup;

import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import javafx.fxml.FXML;
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.sheet.SheetController;
import sheetView.main.UIModel;


public class ReadOnlyPopupController {


    @FXML
    private SheetController sheetController;

    @FXML
    private ActionLineController actionLineController;

    private SheetDTO sheetToDisplay;
    private UIModel uiModel;


    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setSheetToDisplay(SheetDTO sheetDTO) {
        this.sheetToDisplay = sheetDTO;
    }



    public void displaySheet() {
        if (sheetController != null) {
            sheetController.setReadOnly(true);

            if (uiModel == null) {
                uiModel = new UIModel();
            }
            sheetController.setUiModel(uiModel);

            // Set cell selection behavior
            sheetController.setOnCellSelected(cellId -> {
                CellDTOImpl selectedCell = sheetToDisplay.getCells().get(cellId);
                sheetController.highlightDependenciesAndInfluences(selectedCell);
                actionLineController.updateActionLine(selectedCell, cellId);
            });

            if (sheetToDisplay != null) {
                sheetController.initializeSheet(sheetToDisplay);
            } else {
                throw new RuntimeException("No sheet to display.");
            }
        } else {
            throw new RuntimeException("SheetController is not initialized.");
        }
    }

    private void initializeSheetController() {
        // Since sheetController is now injected via @FXML, we don't need to instantiate it manually.
        if (sheetController != null) {
            sheetController.setReadOnly(true);

            if (uiModel == null) {
                uiModel = new UIModel();
            }
            sheetController.setUiModel(uiModel);

            // Set cell selection behavior
            sheetController.setOnCellSelected(cellId -> {
                CellDTOImpl selectedCell = sheetToDisplay.getCells().get(cellId);
                sheetController.highlightDependenciesAndInfluences(selectedCell);
                actionLineController.updateActionLine(selectedCell, cellId);
            });
        } else {
            throw new RuntimeException("SheetController is not initialized.");
        }
    }
    @FXML
    public void initialize() {
        if (actionLineController != null) {
            actionLineController.setReadOnly(true);
        }
    }


    public void displayFilterSheet() {
        initializeSheetController();



        if (sheetToDisplay != null) {
            sheetController.initializeFilterSheet(sheetToDisplay);
        } else {
            throw new RuntimeException("No sheet to display.");
        }
    }
}
