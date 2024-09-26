package javaFX.sheet;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javaFX.main.MainController;
import sheet.api.Sheet;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetController {

    private Engine engine;
    private MainController mainController;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isRowSelected = false;
    private boolean isColumnSelected = false;

    @FXML
    private GridPane spreadsheetGrid;  // GridPane to display the sheet
    @FXML
    private ScrollPane spreadsheetScrollPane;  // ScrollPane to enable scrolling

    private int colWidth = 100;  // Default column width
    private int rowHeight = 30;  // Default row height

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Function to display the sheet in the GridPane
    public void displaySheet(Sheet sheet) {
        spreadsheetGrid.getChildren().clear();  // Clear existing content

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        // Adding row numbers on the first column and column headers
        for (int row = 0; row <= numRows; row++) {
            for (int col = 0; col <= numCols; col++) {
                if (row == 0 && col == 0) {
                    continue;  // Skip top-left corner
                } else if (row == 0) {
                    // Add column headers (A, B, C, ...)
                    Label colHeader = new Label(getColumnName(col));
                    colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    colHeader.setMinWidth(colWidth);
                    final int finalCol = col;
                    colHeader.setOnMouseClicked(event -> {
                        isColumnSelected = true;
                        isRowSelected = false;
                        selectedCol = finalCol;
                        selectedRow = -1;
                    });
                    spreadsheetGrid.add(colHeader, col, 0);
                } else if (col == 0) {
                    // Add row headers (1, 2, 3, ...)
                    Label rowHeader = new Label(String.valueOf(row));
                    rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    rowHeader.setMinHeight(rowHeight);
                    final int finalRow = row;
                    rowHeader.setOnMouseClicked(event -> {
                        isRowSelected = true;
                        isColumnSelected = false;
                        selectedRow = finalRow;
                        selectedCol = -1;
                    });
                    spreadsheetGrid.add(rowHeader, 0, row);
                } else {
                    // Add data cells
                    Coordinate coordinate = createCoordinate(row, col);
                    Cell cell = sheet.getCell(coordinate);

                    StackPane cellPane = createCellPane(cell != null ? cell.getEffectiveValue().toString() : "", colWidth, rowHeight);

                    final int finalRow = row;
                    final int finalCol = col;
                    cellPane.setOnMouseClicked(event -> {
                        isRowSelected = false;
                        isColumnSelected = false;
                        selectedRow = finalRow;
                        selectedCol = finalCol;
                        // Handle cell selection in MainController if needed
                        String cellId = getColumnName(finalCol) + finalRow;
                        mainController.handleCellSelection(cellId);
                    });

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
        cellLabel.setStyle("-fx-padding: 5px; -fx-alignment: center; -fx-wrap-text: true;");  // Default center alignment with wrap

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

    // Highlight dependencies and influences of a selected cell
    public void highlightDependenciesAndInfluences(Cell selectedCell) {
        // Clear previous highlights
        spreadsheetGrid.getChildren().forEach(node -> node.setStyle("-fx-border-color: lightgray;"));

        // Highlight the cells that the selected cell depends on (light blue)
        for (Cell dependentCell : selectedCell.getDependsOn()) {
            highlightCell(dependentCell, "-fx-background-color: lightblue;");
        }

        // Highlight the cells that the selected cell influences (light green)
        for (Cell influencingCell : selectedCell.getInfluencingOn()) {
            highlightCell(influencingCell, "-fx-background-color: lightgreen;");
        }

        // Highlight the selected cell itself
        highlightCell(selectedCell, "-fx-background-color: yellow;");
    }

    // Highlight a specific cell
    private void highlightCell(Cell cell, String style) {
        int row = cell.getCoordinate().getRow();
        int col = cell.getCoordinate().getColumn();

        // Find the StackPane representing the cell in the GridPane
        spreadsheetGrid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col)
                .findFirst()
                .ifPresent(node -> node.setStyle(style));
    }

    // Apply background color to selected cell
    public void applyBackgroundColorToSelectedCell(Color color) {
        applyStyleToSelectedCell("-fx-background-color: " + toRgbString(color) + ";");
    }

    // Apply text color to selected cell
    public void applyTextColorToSelectedCell(Color color) {
        applyStyleToSelectedCell("-fx-text-fill: " + toRgbString(color) + ";");
    }

    // Apply custom style to the selected cell
    private void applyStyleToSelectedCell(String style) {
        if (selectedRow != -1 && selectedCol != -1) {
            spreadsheetGrid.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                    .findFirst()
                    .ifPresent(node -> node.setStyle(style));
        }
    }

    // Convert Color to RGB String
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Set column width or row height based on the selection
    public void applySizeChange(int newSize) {
        if (isColumnSelected) {
            setColumnWidth(newSize);
        } else if (isRowSelected) {
            setRowHeight(newSize);
        }
    }

    // Change the width of a specific column
    public void setColumnWidth(int newColWidth) {
        if (selectedCol != -1) {
            colWidth = newColWidth;
            displaySheet(engine.getCurrentSheet());
        }
    }

    // Change the height of a specific row
    public void setRowHeight(int newRowHeight) {
        if (selectedRow != -1) {
            rowHeight = newRowHeight;
            displaySheet(engine.getCurrentSheet());
        }
    }

    // Reset the design of the selected cell
    public void resetCellDesign() {
        applyStyleToSelectedCell("-fx-background-color: white; -fx-text-fill: black;");
    }
}
