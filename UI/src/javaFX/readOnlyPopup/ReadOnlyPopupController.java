package javaFX.readOnlyPopup;

import cell.api.Cell;
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
    private int versionNumber;
    private UIModel uiModel;

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void displayVersionSheet() {
        // עכשיו, כל הנתונים הוגדרו, נוכל לאתחל את ה-SheetController
        initializeSheetController();

        // הצגת הגיליון
        Sheet selectedSheet = null;
        try {
            selectedSheet = engine.getSheetByVersion(versionNumber);
        } catch (InvalidVersionException e) {
            throw new RuntimeException(e);
        }
        sheetController.initializeSheet(selectedSheet);
    }

    private void initializeSheetController() {
        // Initialize the SheetController
        sheetController = new SheetController();
        sheetController.setEngine(engine);
        sheetController.setReadOnly(true);

        // ודא שה-uiModel לא null
        if (uiModel == null) {
            uiModel = new UIModel();
        }
        sheetController.setUiModel(uiModel);

        // Set the GridPane in SheetController
        sheetController.setSpreadsheetGrid(versionGridPane);

        // Set cell selection behavior
        sheetController.setOnCellSelected(cellId -> {
            Cell selectedCell = null;
            try {
                selectedCell = engine.getSheetByVersion(versionNumber).getCell(createCoordinate(cellId));
            } catch (InvalidVersionException e) {
                throw new RuntimeException(e);
            }
            sheetController.highlightDependenciesAndInfluences(selectedCell);
            actionLineController.updateActionLine(selectedCell);
        });
    }

    @FXML
    public void initialize() {
        // כאן, ה-actionLineController כבר הוזרק
        if (actionLineController != null) {
            actionLineController.setEngine(engine);
            actionLineController.setReadOnly(true);
        }
    }
}
