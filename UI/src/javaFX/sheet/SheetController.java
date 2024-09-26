package javaFX.sheet;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javaFX.main.MainController;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.control.ColorPicker;
import sheet.api.Sheet;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetController {

    private Engine engine;
    private MainController mainController;

    @FXML
    private GridPane spreadsheetGrid;  // GridPane to display the sheet
    @FXML
    private ScrollPane spreadsheetScrollPane;  // ScrollPane to enable scrolling

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Function to display the sheet in the GridPane
    public void displaySheet(Sheet sheet) {
        spreadsheetGrid.getChildren().clear();  // Clear existing content

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        int colWidth = sheet.getColWidth();
        int rowHeight = sheet.getRowHeight();

        // Adding row numbers on the first column and column headers
        for (int row = 0; row <= numRows; row++) {
            for (int col = 0; col <= numCols; col++) {
                if (row == 0 && col == 0) {
                    // Leave the top-left corner blank for alignment
                    continue;
                } else if (row == 0) {
                    // Add column headers (A, B, C, ...)
                    Label colHeader = new Label(getColumnName(col));
                    colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    colHeader.setMinWidth(colWidth);
                    spreadsheetGrid.add(colHeader, col, 0);
                } else if (col == 0) {
                    // Add row headers (1, 2, 3, ...)
                    Label rowHeader = new Label(String.valueOf(row));
                    rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    rowHeader.setMinHeight(rowHeight);
                    spreadsheetGrid.add(rowHeader, 0, row);
                } else {
                    // Add data cells
                    Coordinate coordinate = createCoordinate(row, col);
                    Cell cell = sheet.getCell(coordinate);

                    StackPane cellPane = createCellPane(cell != null ? cell.getEffectiveValue().toString() : "", colWidth, rowHeight);

                    // Add event to handle cell clicks
                    final int finalRow = row;
                    final int finalCol = col;
                    cellPane.setOnMouseClicked(event -> {
                        String cellId = getColumnName(finalCol) + finalRow;
                        mainController.handleCellSelection(cellId);
                    });

                    // Add the cell to the grid
                    spreadsheetGrid.add(cellPane, col, row);
                }
            }
        }

        // Enabling panning in the ScrollPane
        spreadsheetScrollPane.setPannable(true);  // Allows panning with mouse drag
    }

    // Function to create a cell pane with a value, width and height
    private StackPane createCellPane(String value, int colWidth, int rowHeight) {
        StackPane cellPane = new StackPane();
        Label cellLabel = new Label(value);
        cellLabel.setStyle("-fx-padding: 5px; -fx-alignment: center;");  // Default center alignment

        cellPane.getChildren().add(cellLabel);
        cellPane.setMinWidth(colWidth);
        cellPane.setMinHeight(rowHeight);
        cellPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;");
        GridPane.setHgrow(cellPane, Priority.ALWAYS);  // Allow the cell to grow horizontally
        GridPane.setVgrow(cellPane, Priority.ALWAYS);  // Allow the cell to grow vertically
        return cellPane;
    }

    // Function to create a column name (A, B, C, ...)
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
