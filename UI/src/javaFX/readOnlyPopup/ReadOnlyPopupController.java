package javaFX.readOnlyPopup;

import dto.api.CellDTO;
import dto.api.SheetDTO;
import engine.api.Engine;
import javaFX.actionLine.ActionLineController;
import javaFX.main.UIModel;
import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

/**
 * Controller for displaying a read-only version of a sheet.
 */
public class ReadOnlyPopupController {

    @FXML
    private GridPane versionGridPane;

    @FXML
    private ActionLineController actionLineController;

    private Engine engine;
    private SheetController sheetController;
    private UIModel uiModel;

    private int versionNumber;
    private SheetDTO sheetToDisplay;

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setSheetToDisplay(SheetDTO sheetDTO) {
        this.sheetToDisplay = sheetDTO;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void displaySheet() {
        initializeSheetController();

        if (sheetToDisplay == null && versionNumber > 0) {
            // Retrieve sheet by version if not provided
            sheetToDisplay = engine.getSheetDTOByVersion(versionNumber);
        }

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
            CellDTO selectedCellDTO = engine.getCellInfo(cellId);
            sheetController.highlightDependenciesAndInfluences(selectedCellDTO);
            actionLineController.updateActionLine(selectedCellDTO);
        });
    }

    @FXML
    public void initialize() {
        if (actionLineController != null) {
            actionLineController.setReadOnly(true);
        }
    }
}
