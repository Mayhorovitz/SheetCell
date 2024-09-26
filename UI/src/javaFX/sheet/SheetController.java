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
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    // Function to display the sheet in the GridPane
    public void displaySheet(Sheet sheet) {
        spreadsheetGrid.getChildren().clear();  // Clear existing content

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        for (int row = 0; row <= numRows; row++) {
            for (int col = 0; col <= numCols; col++) {
                if (row == 0 && col == 0) {
                    continue;  // Skip top-left corner
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

                    // Pass the entire cell object to createCellPane so colors are applied
                    StackPane cellPane = createCellPane(
                            cell != null ? cell.getEffectiveValue().toString() : "",
                            colWidth,
                            rowHeight,
                            cell // <-- pass the Cell object so colors can be applied
                    );

                    // Add event to handle cell clicks
                    final int finalRow = row;
                    final int finalCol = col;
                    cellPane.setOnMouseClicked(event -> {
                        selectedRow = finalRow;
                        selectedCol = finalCol;
                        String cellId = getColumnName(finalCol) + finalRow;
                        mainController.handleCellSelection(cellId);

                        // Highlight dependencies and influences
                        highlightDependenciesAndInfluences(cell);
                    });

                    spreadsheetGrid.add(cellPane, col, row);
                }
            }
        }

        // Enabling panning in the ScrollPane
        spreadsheetScrollPane.setPannable(true);  // Allows panning with mouse drag
    }


    // Use this method when creating cell panes to apply colors
    private StackPane createCellPane(String value, int colWidth, int rowHeight, Cell cell) {
        StackPane cellPane = new StackPane();
        Label cellLabel = new Label(value);
        cellLabel.setStyle("-fx-padding: 5px; -fx-alignment: center; -fx-wrap-text: true;");

        if (cell != null) {
            String backgroundColor = cell.getBackgroundColor(); // Now this is a string
            String textColor = cell.getTextColor(); // Now this is a string

            cellPane.setStyle("-fx-background-color: " + backgroundColor + ";");
            cellLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-padding: 5px; -fx-alignment: center; -fx-wrap-text: true;");
        } else {
            cellPane.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        cellPane.getChildren().add(cellLabel);
        cellPane.setMinWidth(colWidth);
        cellPane.setMinHeight(rowHeight);
        cellPane.setStyle(cellPane.getStyle() + "; -fx-border-color: lightgray; -fx-border-width: 1px;");
        GridPane.setHgrow(cellPane, Priority.ALWAYS);
        GridPane.setVgrow(cellPane, Priority.ALWAYS);
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
        // Clear previous highlights and restore their original styles
        spreadsheetGrid.getChildren().forEach(node -> {
            int row = GridPane.getRowIndex(node);
            int col = GridPane.getColumnIndex(node);
            Coordinate coordinate = createCoordinate(row, col);
            Cell cell = engine.getCurrentSheet().getCell(coordinate);

            // Restore original colors for each cell
            if (cell != null) {
                node.setStyle("-fx-background-color: " + cell.getBackgroundColor() + ";" + // Use string directly
                        "-fx-text-fill: " + cell.getTextColor() + ";" + // Use string directly
                        "-fx-border-color: lightgray; -fx-border-width: 1px;");
            }
        });

        // Highlight the cells that the selected cell depends on (light blue dashed border)
        for (Cell dependentCell : selectedCell.getDependsOn()) {
            highlightCell(dependentCell, "-fx-border-color: lightblue; -fx-border-width: 5px; -fx-border-style: dashed;");
        }

        // Highlight the cells that the selected cell influences (light green dashed border)
        for (Cell influencingCell : selectedCell.getInfluencingOn()) {
            highlightCell(influencingCell, "-fx-border-color: lightgreen; -fx-border-width: 5px; -fx-border-style: dashed;");
        }

        // Add solid blue border to the selected cell
        int selectedRow = selectedCell.getCoordinate().getRow();
        int selectedCol = selectedCell.getCoordinate().getColumn();

        spreadsheetGrid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                .findFirst()
                .ifPresent(node -> {
                    node.setStyle(node.getStyle() + "; -fx-border-color: blue; -fx-border-width: 3px;");
                });
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

    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);

            selectedCell.setBackgroundColor(toHexString(color)); // Convert Color to hex string
            displaySheet(engine.getCurrentSheet());
        }
    }

    public void applyTextColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);

            selectedCell.setTextColor(toHexString(color)); // Convert Color to hex string
            displaySheet(engine.getCurrentSheet());
        }
    }


    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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

    private Color stringToColor(String colorString) {
        return Color.web(colorString); // Convert from hex string to Color object
    }
}
