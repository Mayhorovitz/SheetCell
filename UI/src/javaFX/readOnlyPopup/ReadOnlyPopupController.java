package javaFX.readOnlyPopup;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import engine.exceptions.InvalidVersionException;
import javaFX.actionLine.ActionLineController;
import javaFX.main.UIModel;
import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import sheet.api.Sheet;

import static coordinate.CoordinateFactory.createCoordinate;

public class ReadOnlyPopupController {

    @FXML
    private GridPane versionGridPane;

    @FXML
    private ActionLineController actionLineController;

    private Engine engine;
    private SheetController sheetController;
    private UIModel uiModel;

    private int versionNumber;
    private Sheet sheetToDisplay;

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }


    public void setSheetToDisplay(Sheet sheet) {
        this.sheetToDisplay = sheet;
    }


    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }


    public void displaySheet() {
        initializeSheetController();


        if (sheetToDisplay == null && versionNumber > 0) {
            try {
                sheetToDisplay = engine.getSheetByVersion(versionNumber);
            } catch (InvalidVersionException e) {
                throw new RuntimeException(e);
            }
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
        sheetController.setEngine(engine);
        sheetController.setReadOnly(true);

        if (uiModel == null) {
            uiModel = new UIModel();
        }
        sheetController.setUiModel(uiModel);

        // Set the GridPane in SheetController
        sheetController.setSpreadsheetGrid(versionGridPane);

        // Set cell selection behavior
        sheetController.setOnCellSelected(cellId -> {
            Coordinate coordinate = createCoordinate(cellId);
            Cell selectedCell = sheetToDisplay.getCell(coordinate);
            sheetController.highlightDependenciesAndInfluences(selectedCell);
            actionLineController.updateActionLine(selectedCell, cellId);
        });
    }

    @FXML
    public void initialize() {
        if (actionLineController != null) {
            actionLineController.setEngine(engine);
            actionLineController.setReadOnly(true);
        }
    }

    public void displayFilterSheet() {
        initializeSheetController();

        if (sheetToDisplay == null && versionNumber > 0) {
            try {
                sheetToDisplay = engine.getSheetByVersion(versionNumber);
            } catch (InvalidVersionException e) {
                throw new RuntimeException(e);
            }
        }

        if (sheetToDisplay != null) {
            sheetController.initializeFilterSheet(sheetToDisplay);
        } else {
            throw new RuntimeException("No sheet to display.");
        }
    }

}
