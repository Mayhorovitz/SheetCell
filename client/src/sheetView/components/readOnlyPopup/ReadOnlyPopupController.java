package sheetView.components.readOnlyPopup;

import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import sheetView.main.UIModel;
import sheetView.components.actionLine.ActionLineController;
import sheetView.components.sheet.SheetController;


public class ReadOnlyPopupController {


    @FXML
    private GridPane versionGridPane;

    @FXML
    private ActionLineController actionLineController;

    private SheetController sheetController;
    private UIModel uiModel;

    private int versionNumber;
    private SheetDTO sheetToDisplay;

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setSheetToDisplay(SheetDTO sheetDTO) {
        this.sheetToDisplay = sheetDTO;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void displaySheet() {
        initializeSheetController();



        if (sheetToDisplay != null) {
            sheetController.initializeSheet(sheetToDisplay);
        } else {
            throw new RuntimeException("No sheet to display.");
        }
    }

    private void initializeSheetController() {
        // Initialize the SheetController
        sheetController = new SheetController();
        sheetController.setReadOnly(true);

        if (uiModel == null) {
            uiModel = new UIModel();
        }
        sheetController.setUiModel(uiModel);

        // Set the GridPane in SheetController
        sheetController.setSpreadsheetGrid(versionGridPane);

        // Set cell selection behavior
        sheetController.setOnCellSelected(cellId -> {
            CellDTOImpl selectedCell = sheetToDisplay.getCells().get(cellId);
            sheetController.highlightDependenciesAndInfluences(selectedCell);
            actionLineController.updateActionLine(selectedCell, cellId);
        });

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
