package javaFX.sheet;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import javaFX.main.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import range.api.Range;
import sheet.api.Sheet;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetController {

    private Engine engine;
    private MainController mainController;
    private int selectedRow = -1;
    private int selectedCol = -1;

    @FXML
    private GridPane spreadsheetGrid;  // GridPane to display the sheet
    @FXML
    private ScrollPane spreadsheetScrollPane;  // ScrollPane to enable scrolling

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void displaySheet(Sheet sheet) {
        // Clear any previous content from the grid and constraints
        spreadsheetGrid.getChildren().clear();
        spreadsheetGrid.getColumnConstraints().clear();
        spreadsheetGrid.getRowConstraints().clear();

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        // Initialize row and column constraints based on the current Sheet data
        for (int row = 0; row <= numRows; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(sheet.getRowHeight());
            rowConstraints.setPrefHeight(sheet.getRowHeight());
            rowConstraints.setMaxHeight(sheet.getRowHeight());  // Set max height
            spreadsheetGrid.getRowConstraints().add(rowConstraints);
        }

        for (int col = 0; col <= numCols; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(sheet.getColWidth());
            colConstraints.setPrefWidth(sheet.getColWidth());
            colConstraints.setMaxWidth(sheet.getColWidth());  // Set max width
            spreadsheetGrid.getColumnConstraints().add(colConstraints);
        }

        // Populate the grid with headers and cells
        for (int row = 0; row <= numRows; row++) {
            for (int col = 0; col <= numCols; col++) {
                if (row == 0 && col == 0) {
                    continue;  // Skip the top-left corner
                } else if (row == 0) {
                    // Add column headers (A, B, C, ...)
                    Label colHeader = new Label(getColumnName(col));
                    colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    spreadsheetGrid.add(colHeader, col, 0);
                } else if (col == 0) {
                    // Add row headers (1, 2, 3, ...)
                    Label rowHeader = new Label(String.valueOf(row));
                    rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                    spreadsheetGrid.add(rowHeader, 0, row);
                } else {
                    // Add data cells
                    Coordinate coordinate = createCoordinate(row, col);
                    Cell cell = sheet.getCell(coordinate);

                    // Ensure we keep the cell's styling (background, text color, etc.)
                    StackPane cellPane = createCellPane(
                            cell != null ? cell.getEffectiveValue().toString() : "",
                            cell // Pass cell object to apply styling correctly
                    );

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

        // Allow panning via mouse drag
        spreadsheetScrollPane.setPannable(true);
    }

    // פונקציה זו יוצרת את התא ושומרת על העיצוב
    private StackPane createCellPane(String value, Cell cell) {
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
                node.setStyle("-fx-background-color: " + cell.getBackgroundColor() + ";" +
                        "-fx-text-fill: " + cell.getTextColor() + ";" +
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

        spreadsheetGrid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col)
                .findFirst()
                .ifPresent(node -> node.setStyle(style));
    }

    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);

            // שמור את צבע הרקע בתא
            selectedCell.setBackgroundColor(toHexString(color));

            // עדכון התצוגה של התא הנוכחי בלבד, כך שלא נדרש לעדכן את כל הגיליון
            spreadsheetGrid.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                    .findFirst()
                    .ifPresent(node -> {
                        StackPane cellPane = (StackPane) node;
                        cellPane.setStyle("-fx-background-color: " + toHexString(color) + ";"); // החלת צבע הרקע
                    });
        }
    }

    public void applyTextColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);

            // שמור את צבע הטקסט בתא
            selectedCell.setTextColor(toHexString(color));

            // עדכון התצוגה של התא הנוכחי בלבד, כך שלא נדרש לעדכן את כל הגיליון
            spreadsheetGrid.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                    .findFirst()
                    .ifPresent(node -> {
                        StackPane cellPane = (StackPane) node;
                        Label cellLabel = (Label) cellPane.getChildren().get(0);
                        cellLabel.setStyle("-fx-text-fill: " + toHexString(color) + ";"); // החלת צבע הטקסט
                    });
        }
    }


    // Convert Color to hex string
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Change the width of a specific column and update the column constraints
    public void setColumnWidth(int newColWidth) {
        if (selectedCol != -1) {
            ColumnConstraints colConstraints = spreadsheetGrid.getColumnConstraints().get(selectedCol);
            colConstraints.setMinWidth(newColWidth);
            colConstraints.setPrefWidth(newColWidth);
            colConstraints.setMaxWidth(newColWidth);

            // Refresh only the column's constraints without clearing the grid
            spreadsheetGrid.getChildren().forEach(node -> {
                int col = GridPane.getColumnIndex(node);
                if (col == selectedCol) {
                    ((Region) node).setMinWidth(newColWidth);
                    ((Region) node).setPrefWidth(newColWidth);
                }
            });
        }
    }

    // Change the height of a specific row and update the row constraints
    public void setRowHeight(int newRowHeight) {
        if (selectedRow != -1) {
            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(selectedRow);
            rowConstraints.setMinHeight(newRowHeight);
            rowConstraints.setPrefHeight(newRowHeight);
            rowConstraints.setMaxHeight(newRowHeight);

            // Refresh only the row's constraints without clearing the grid
            spreadsheetGrid.getChildren().forEach(node -> {
                int row = GridPane.getRowIndex(node);
                if (row == selectedRow) {
                    ((Region) node).setMinHeight(newRowHeight);
                    ((Region) node).setPrefHeight(newRowHeight);
                }
            });
        }
    }

    public void resetCellDesign() {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);
            selectedCell.setTextColor(toHexString(Color.BLACK));
            selectedCell.setBackgroundColor(toHexString(Color.WHITE));

            // Update the specific cell in the grid
            spreadsheetGrid.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                    .findFirst()
                    .ifPresent(node -> {
                        Label cellLabel = (Label) ((StackPane) node).getChildren().get(0);
                        cellLabel.setStyle("-fx-text-fill: black;");
                        node.setStyle("-fx-background-color: white;");
                    });
        }
    }

    public void highlightRange(Range range) {
        Coordinate start = createCoordinate(range.getFrom());
        Coordinate end = createCoordinate(range.getTo());

        int startRow = start.getRow();
        int startCol = start.getColumn();
        int endRow = end.getRow();
        int endCol = end.getColumn();


    }
    public void clearSheet() {
        spreadsheetGrid.getChildren().clear();  // Clear the sheet grid
        spreadsheetGrid.getColumnConstraints().clear(); // Clear column constraints
        spreadsheetGrid.getRowConstraints().clear();    // Clear row constraints
    }
}
