package javaFX.sheet;

import cell.api.Cell;
import engine.api.Engine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sheet.api.Sheet;
import javaFX.main.MainController;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetController {

    private MainController mainController;  // קישור ל-MainController

    @FXML
    private TableView<ObservableList<String>> spreadsheetTable;  // טבלה שתציג את הגיליון

    public void setMainController(MainController mainController) {
        this.mainController = mainController;  // קישור ל-MainController
    }

    public void displaySheet(Sheet sheet) {
        spreadsheetTable.getColumns().clear();  // ננקה עמודות קיימות
        spreadsheetTable.getItems().clear();  // ננקה נתונים קיימים

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        // יצירת עמודות בצורה דינמית לפי מספר העמודות בגיליון
        for (int col = 1; col <= numCols; col++) {
            final int columnIndex = col - 1;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(getColumnName(col));
            column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(columnIndex)));
            spreadsheetTable.getColumns().add(column);
        }

        // יצירת השורות בצורה דינמית לפי מספר השורות בגיליון
        for (int row = 1; row <= numRows; row++) {
            ObservableList<String> rowData = FXCollections.observableArrayList();
            for (int col = 1; col <= numCols; col++) {
                Cell cell = sheet.getCell(createCoordinate(row, col));
                rowData.add(cell != null ? cell.getEffectiveValue().toString() : "");
            }
            spreadsheetTable.getItems().add(rowData);  // הוספת השורה לטבלה
        }
    }

    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex > 0) {
            int remainder = (colIndex - 1) % 26;
            columnName.insert(0, (char) (remainder + 'A'));
            colIndex = (colIndex - 1) / 26;
        }
        return columnName.toString();
    }
}
