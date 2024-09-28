package javaFX.sheet;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import javaFX.main.MainController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import range.api.Range;
import sheet.api.Sheet;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Label> cellIDtoLabel = new HashMap<>();  // Map to store cell labels for easy access


    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    // Method to create and display the sheet
    public void displaySheet(Sheet sheet) {
        clearSheet();  // Clear the existing grid

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        addColumnAndRowConstraints(numCols, sheet.getColWidth(), numRows, sheet.getRowHeight());
        addColumnsAndRowHeaders(numCols, numRows);
        populateSheetGrid(sheet, numCols, numRows);

        spreadsheetScrollPane.setPannable(true);  // Allow panning via mouse drag
    }

    // Helper method to add column and row constraints
    private void addColumnAndRowConstraints(int numCols, int colWidth, int numRows, int rowHeight) {
        for (int i = 0; i <= numCols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(colWidth);
            spreadsheetGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i <= numRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(rowHeight);
            spreadsheetGrid.getRowConstraints().add(rowConstraints);
        }
    }

    // Method to add headers for rows and columns
    private void addColumnsAndRowHeaders(int numCols, int numRows) {
        for (int col = 1; col <= numCols; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
            spreadsheetGrid.add(colHeader, col, 0);  // Add column headers at the top
        }

        for (int row = 1; row <= numRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
            spreadsheetGrid.add(rowHeader, 0, row);  // Add row headers on the left
        }
    }


    // Populate the grid with the actual cells
    private void populateSheetGrid(Sheet sheet, int numCols, int numRows) {
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String cellID = getColumnName(col) + row;
                Coordinate coordinate = createCoordinate(row, col);
                Cell cell = sheet.getCell(coordinate);

                // Create the label for the cell and bind its value
                Label cellLabel = new Label(cell != null ? cell.getEffectiveValue().toString() : "");
                cellLabel.setAlignment(Pos.CENTER);
                cellLabel.setPrefHeight(sheet.getRowHeight());
                cellLabel.setPrefWidth(sheet.getColWidth());
                cellLabel.getStyleClass().add("cell");
                applyCellStyle(cell, cellLabel);  // Apply styles like background and text color

                cellIDtoLabel.put(cellID, cellLabel);  // Store the label in the map for easy access
                spreadsheetGrid.add(cellLabel, col, row);

                // Handle cell clicks
                final int finalRow = row;
                final int finalCol = col;
                cellLabel.setOnMouseClicked(event -> {
                    selectedRow = finalRow;
                    selectedCol = finalCol;
                    mainController.handleCellSelection(cellID);
                    highlightDependenciesAndInfluences(cell);  // Highlight cell dependencies and influences
                });
            }
        }
    }


    private void applyCellStyle(Cell cell, Label label) {
        // Always apply the border style
        String borderStyle = "-fx-border-color: black; -fx-border-width: 1px;";

        if (cell != null) {
            // Apply the saved background and text colors along with the border
            label.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; %s",
                    cell.getBackgroundColor(), cell.getTextColor(), borderStyle
            ));
        } else {
            // Default to white background and black text, always with the border
            label.setStyle("-fx-background-color: white; -fx-text-fill: black;" + borderStyle);
        }
    }



    // Convert column index to name (A, B, C, etc.)
    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex > 0) {
            int remainder = (colIndex - 1) % 26;
            columnName.insert(0, (char) (remainder + 'A'));
            colIndex = (colIndex - 1) / 26;
        }
        return columnName.toString();
    }


    public void highlightDependenciesAndInfluences(Cell selectedCell) {
        resetCellBorders();  // Clear previous highlights

        if (selectedCell != null) {
            // Highlight dependencies
            selectedCell.getDependsOn().forEach(dependentCell ->
                    highlightCell(dependentCell, "-fx-border-color: lightblue; -fx-border-width: 4px;"));

            // Highlight influences
            selectedCell.getInfluencingOn().forEach(influencingCell ->
                    highlightCell(influencingCell, "-fx-border-color: lightgreen; -fx-border-width: 4px;"));

            // Highlight selected cell (without overriding the style, only adding the blue border)
            Label selectedLabel = cellIDtoLabel.get(getColumnName(selectedCol) + selectedRow);
            if (selectedLabel != null) {
                // Append the border without overwriting other styles
                String currentStyle = selectedLabel.getStyle();
                selectedLabel.setStyle(currentStyle + "; -fx-border-color: blue; -fx-border-width: 3px;");
            }
        }
    }


    private void resetCellBorders() {
        cellIDtoLabel.values().forEach(label -> {
            // Keep the original style and only reset the border-related styles
            String currentStyle = label.getStyle().replaceAll("-fx-border-color:.*?;", "")
                    .replaceAll("-fx-border-width:.*?;", "");
            label.setStyle(currentStyle + "-fx-border-color: black; -fx-border-width: 1px;");
        });
    }


    // Highlight a specific cell with a style
    private void highlightCell(Cell cell, String style) {
        String cellID = getColumnName(cell.getCoordinate().getColumn()) + cell.getCoordinate().getRow();
        Label cellLabel = cellIDtoLabel.get(cellID);
        if (cellLabel != null) {
            cellLabel.setStyle(cellLabel.getStyle() + style);
        }
    }

    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            Cell selectedCell = engine.getCurrentSheet().getCell(createCoordinate(selectedRow, selectedCol));
            selectedCell.setBackgroundColor(toHexString(color));  // Save color in the cell object

            Label selectedLabel = cellIDtoLabel.get(cellID);
            if (selectedLabel != null) {
                // Keep existing styles, just append the background color
                String currentStyle = selectedLabel.getStyle();
                selectedLabel.setStyle(currentStyle + "-fx-background-color: " + toHexString(color) + ";");
            }
        }
    }

    public void applyTextColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            Cell selectedCell = engine.getCurrentSheet().getCell(createCoordinate(selectedRow, selectedCol));
            selectedCell.setTextColor(toHexString(color));  // Save color in the cell object

            Label selectedLabel = cellIDtoLabel.get(cellID);
            if (selectedLabel != null) {
                // Keep existing styles, just append the text color
                String currentStyle = selectedLabel.getStyle();
                selectedLabel.setStyle(currentStyle + "-fx-text-fill: " + toHexString(color) + ";");
            }
        }
    }


    // Convert Color to hex string
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Set column width
    public void setColumnWidth(int columnIndex, double width) {
        if (spreadsheetGrid.getColumnConstraints().size() > columnIndex) {
            ColumnConstraints colConstraints = spreadsheetGrid.getColumnConstraints().get(columnIndex);
            colConstraints.setPrefWidth(width);
            colConstraints.setMinWidth(width);  // Ensure minimum width is set as well
            colConstraints.setMaxWidth(Double.MAX_VALUE);  // Allow it to expand

            // עדכון התאים בעמודה עם הרוחב החדש
            spreadsheetGrid.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == columnIndex && node instanceof Label) {
                    ((Label) node).setPrefWidth(width);
                    ((Label) node).setMinWidth(width);
                }
            });
        }

        // בקשת רענון תצוגה כדי לוודא שכל העמודה מתעדכנת
        spreadsheetGrid.requestLayout();
    }

    // Set row height
    public void setRowHeight(int rowIndex, double height) {
        if (spreadsheetGrid.getRowConstraints().size() > rowIndex) {
            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(rowIndex);
            rowConstraints.setPrefHeight(height);
            rowConstraints.setMinHeight(height);  // Ensure minimum height is set as well
            rowConstraints.setMaxHeight(Double.MAX_VALUE);  // Allow it to expand

            // עדכון התאים בשורה עם הגובה החדש
            spreadsheetGrid.getChildren().forEach(node -> {
                if (GridPane.getRowIndex(node) == rowIndex && node instanceof Label) {
                    ((Label) node).setPrefHeight(height);
                    ((Label) node).setMinHeight(height);
                }
            });
        }

        // בקשת רענון תצוגה כדי לוודא שכל השורה מתעדכנת
        spreadsheetGrid.requestLayout();
    }


    public void setColumnAlignment(int colIndex, String alignment) {
        Pos pos;
        switch (alignment) {
            case "Left":
                pos = Pos.CENTER_LEFT;
                break;
            case "Right":
                pos = Pos.CENTER_RIGHT;
                break;
            default:
                pos = Pos.CENTER;
                break;
        }

        // Apply the alignment for all cells in the column
        spreadsheetGrid.getChildren().forEach(node -> {
            if (GridPane.getColumnIndex(node) == colIndex && node instanceof Label) {
                ((Label) node).setAlignment(pos);
            }
        });
    }

    public void resetCellDesign() {
        if (selectedRow != -1 && selectedCol != -1) {
            Coordinate coordinate = createCoordinate(selectedRow, selectedCol);
            Cell selectedCell = engine.getCurrentSheet().getCell(coordinate);

            // Reset the cell's colors to default (black text, white background)
            selectedCell.setTextColor(toHexString(Color.BLACK));
            selectedCell.setBackgroundColor(toHexString(Color.WHITE));

            // Update the specific cell's appearance in the grid
            spreadsheetGrid.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == selectedRow && GridPane.getColumnIndex(node) == selectedCol)
                    .findFirst()
                    .ifPresent(node -> {
                        if (node instanceof Label) {
                            Label cellLabel = (Label) node;
                            // Apply the default black text and white background
                            String currentStyle = cellLabel.getStyle();
                            // Replace only the text color and background, without affecting other styles
                            currentStyle = currentStyle.replaceAll("-fx-text-fill:.*?;", "");
                            currentStyle = currentStyle.replaceAll("-fx-background-color:.*?;", "");
                            cellLabel.setStyle(currentStyle + "-fx-text-fill: black; -fx-background-color: white;");
                        }
                    });
        }
    }
    public void highlightRange(Range range) {
        // קודם נוודא שאם יש טווח שהיה מודגש בעבר, נאפס אותו
        resetRangeHighlight();

        // Extract the start and end coordinates of the range
        Coordinate start = createCoordinate(range.getFrom());
        Coordinate end = createCoordinate(range.getTo());

        int startRow = start.getRow();
        int startCol = start.getColumn();
        int endRow = end.getRow();
        int endCol = end.getColumn();

        // Loop through all the cells in the range and highlight them using highlightCell
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Coordinate cellCoordinate = createCoordinate(row, col);
                Cell cell = engine.getCurrentSheet().getCell(cellCoordinate);

                if (cell != null) {
                    // Use highlightCell to apply the highlight style
                    highlightCell(cell, "-fx-border-color: #00ffcc; -fx-border-width: 2px; -fx-background-color: rgba(0,255,204,0.28);");
                }
            }
        }
    }

    public void resetRangeHighlight() {
        // איפוס הסטייל של כל התאים שהיו מסומנים כהדגשה של Range
        cellIDtoLabel.values().forEach(label -> {
            // ננקה רק את ההדגשה של ה-Range מבלי לפגוע בעיצוב הבסיסי
            String currentStyle = label.getStyle();
            // מסירים רק את ההדגשה של ה-Range
            currentStyle = currentStyle.replaceAll("-fx-background-color: rgba\\(0,255,204,0.28\\);", "");
            currentStyle = currentStyle.replaceAll("-fx-border-color: #00ffcc; -fx-border-width: 2px;", "");
            label.setStyle(currentStyle);
        });
    }
    // Clear the sheet display
    public void clearSheet() {
        spreadsheetGrid.getChildren().clear();
        spreadsheetGrid.getColumnConstraints().clear();
        spreadsheetGrid.getRowConstraints().clear();
    }


    public int getSelectedColumnIndex() {
        return selectedCol; // or any logic to fetch the currently selected column
    }

    public int getSelectedRowIndex() {
        return selectedRow; // or any logic to fetch the currently selected row
    }
}
