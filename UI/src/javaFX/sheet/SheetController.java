package javaFX.sheet;

import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import javaFX.main.MainController;
import javaFX.main.UIModel;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import range.api.Range;
import sheet.api.Sheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetController {

    private Engine engine;
    private MainController mainController;
    private UIModel uiModel;  // Using UIModel to store dimensions
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Cell lastSelectedCell = null;
    @FXML
    private GridPane spreadsheetGrid;  // GridPane to display the sheet
    @FXML
    private ScrollPane spreadsheetScrollPane;  // ScrollPane to enable scrolling

    private final Map<String, Label> cellToLabel = new HashMap<>();  // Map to store cell labels for easy access

    private Consumer<String> onCellSelected;  // Listener for cell selection

    private boolean isReadOnly = false;  // Flag to indicate read-only mode

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setUiModel(UIModel uiModel) {
        this.uiModel = uiModel;

        // Bind the column and row dimensions to the model
        uiModel.colWidthProperty().addListener((observable, oldValue, newValue) -> {
            // Update column widths
            for (int i = 0; i < spreadsheetGrid.getColumnConstraints().size(); i++) {
                setColumnWidth(i, newValue.doubleValue());
            }
        });

        uiModel.rowHeightProperty().addListener((observable, oldValue, newValue) -> {
            // Update row heights
            for (int i = 0; i < spreadsheetGrid.getRowConstraints().size(); i++) {
                setRowHeight(i, newValue.doubleValue());
            }
        });
    }

    public void setOnCellSelected(Consumer<String> onCellSelected) {
        this.onCellSelected = onCellSelected;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public void setSpreadsheetGrid(GridPane gridPane) {
        this.spreadsheetGrid = gridPane;
    }

    public void displaySheet(Sheet sheet) {
        if (spreadsheetGrid.getChildren().isEmpty()) {
            // Only initialize if the grid is empty
            initializeSheet(sheet);
        } else {
            // Apply only changes, no full clear
            updateSheet(sheet);
        }

        spreadsheetScrollPane.setPannable(true);
    }

    public void initializeSheet(Sheet sheet) {
        cellToLabel.clear();
        // Clear the GridPane before loading the new file
        clearGridPane();

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        // Remove gaps between cells
        spreadsheetGrid.setHgap(0);
        spreadsheetGrid.setVgap(0);

        // Remove padding and border from the GridPane
        spreadsheetGrid.setPadding(Insets.EMPTY);
        spreadsheetGrid.setStyle("-fx-border-color: transparent;");

        // Get row height and column width from the sheet
        int rowHeightFromSheet = sheet.getRowHeight();
        int colWidthFromSheet = sheet.getColWidth();

        // Initialize rows and columns based on values from the sheet
        addColumnAndRowConstraints(numCols, colWidthFromSheet, numRows, rowHeightFromSheet);
        addColumnsAndRowHeaders(numCols, numRows);
        populateSheetGrid(sheet, numCols, numRows);
    }

    private void clearGridPane() {
        // Clear existing cells in the GridPane
        spreadsheetGrid.getChildren().clear();

        // Reset column and row constraints
        spreadsheetGrid.getColumnConstraints().clear();
        spreadsheetGrid.getRowConstraints().clear();
    }

    private void updateSheet(Sheet sheet) {
        int numRows = sheet.getRows();
        int numCols = sheet.getCols();

        // Update cells that have changed
        populateSheetGrid(sheet, numCols, numRows);
    }

    private void addColumnAndRowConstraints(int numCols, double colWidth, int numRows, double rowHeight) {
        for (int i = 0; i <= numCols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(colWidth);  // Set preferred width
            spreadsheetGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i <= numRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(rowHeight);  // Set preferred height
            spreadsheetGrid.getRowConstraints().add(rowConstraints);
        }
    }

    private void addColumnsAndRowHeaders(int numCols, int numRows) {
        // Adding column headers (A, B, C, etc.)
        for (int col = 1; col <= numCols; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            colHeader.setPrefWidth(spreadsheetGrid.getColumnConstraints().get(col - 1).getPrefWidth()); // Match column width
            colHeader.setPrefHeight(uiModel.getRowHeight()); // Match row height
            colHeader.setAlignment(Pos.CENTER); // Center the text
            spreadsheetGrid.add(colHeader, col, 0);  // Add column headers at the top (row 0)
        }

        // Adding row headers (1, 2, 3, etc.)
        for (int row = 1; row <= numRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            rowHeader.setPrefWidth(uiModel.getColWidth()); // Match column width
            rowHeader.setPrefHeight(spreadsheetGrid.getRowConstraints().get(row - 1).getPrefHeight()); // Match row height
            rowHeader.setAlignment(Pos.CENTER); // Center the text
            spreadsheetGrid.add(rowHeader, 0, row);  // Add row headers on the left (column 0)
        }
    }

    private void populateSheetGrid(Sheet sheet, int numCols, int numRows) {
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String cellID = getColumnName(col) + row;
                Coordinate coordinate = createCoordinate(row, col);
                Cell cell = sheet.getCell(coordinate);

                if (cellToLabel.containsKey(cellID)) {
                    Label existingLabel = cellToLabel.get(cellID);

                    // Update the value and reapply style
                    existingLabel.setText(cell != null ? cell.getEffectiveValue().toString() : "");
                    applyCellStyle(cell, existingLabel);

                } else {
                    // Create a new label if it doesn't exist
                    Label cellLabel = new Label(cell != null ? cell.getEffectiveValue().toString() : "");
                    cellLabel.setAlignment(Pos.CENTER);

                    // Set cell dimensions based on the UI model
                    cellLabel.setPrefHeight(uiModel.getRowHeight());
                    cellLabel.setPrefWidth(uiModel.getColWidth());
                    cellLabel.getStyleClass().add("cell");
                    applyCellStyle(cell, cellLabel);  // Apply styles like background and text color

                    cellToLabel.put(cellID, cellLabel);  // Store label in map for easy access

                    spreadsheetGrid.add(cellLabel, col, row);

                    // Add listener for clicks on the cell
                    final int finalRow = row;
                    final int finalCol = col;
                    cellLabel.setOnMouseClicked(event -> {
                        selectedRow = finalRow;
                        selectedCol = finalCol;
                        String selectedCellId = getColumnName(selectedCol) + selectedRow;

                        if (isReadOnly && onCellSelected != null) {
                            // In read-only mode, use the provided listener
                            onCellSelected.accept(selectedCellId);
                        } else if (mainController != null) {
                            // In normal mode, notify the main controller
                            mainController.handleCellSelection(selectedCellId);
                        }
                    });
                }
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
        resetCellBorders();  // Reset previous highlights
        if (selectedCell != null) {
            lastSelectedCell = selectedCell;
            highlightCells(selectedCell.getDependsOn(), "lightblue");
            highlightCells(selectedCell.getInfluencingOn(), "lightgreen");
        }
        Label selectedLabel = cellToLabel.get(getColumnName(selectedCol) + selectedRow);
        if (selectedLabel != null) {
            String currentStyle = selectedLabel.getStyle();
            selectedLabel.setStyle(currentStyle + "; -fx-border-color: blue; -fx-border-width: 3px;");
        }
    }

    private void highlightCells(List<Cell> cells, String color) {
        for (Cell cell : cells) {
            Label cellLabel = cellToLabel.get(getColumnName(cell.getCoordinate().getColumn()) + cell.getCoordinate().getRow());
            if (cellLabel != null) {
                String currentStyle = cellLabel.getStyle();
                String newStyle = currentStyle + ";-fx-border-color:" + color + ";-fx-border-width: 4px;-fx-border-style: dashed;";
                cellLabel.setStyle(newStyle);
            }
        }
    }

    public void clearPreviousHighlights() {
        if (lastSelectedCell != null) {
            clearHighlights(lastSelectedCell.getDependsOn());
            clearHighlights(lastSelectedCell.getInfluencingOn());
        }
    }

    public void clearHighlights(List<Cell> cells) {
        for (Cell cell : cells) {
            Label cellLabel = cellToLabel.get(getColumnName(cell.getCoordinate().getColumn()) + cell.getCoordinate().getRow());
            if (cellLabel != null) {
                String currentStyle = cellLabel.getStyle()
                        .replaceAll("-fx-border-color:.*?;", "")
                        .replaceAll("-fx-border-width:.*?;", "")
                        .replaceAll("-fx-border-style:.*?;", "");

                // Restore default border
                cellLabel.setStyle(currentStyle + "-fx-border-color: black; -fx-border-width: 1px; ");
            }
        }
    }

    private void resetCellBorders() {
        cellToLabel.values().forEach(label -> {
            // Reset borders without changing other styles
            String currentStyle = label.getStyle().replaceAll("-fx-border-color:.*?;", "")
                    .replaceAll("-fx-border-width:.*?;", "").replaceAll("-fx-border-style:.*?;", "");
            label.setStyle(currentStyle + "-fx-border-color: black; -fx-border-width: 1px;");
        });
    }

    // Highlight a specific cell with a style
    private void highlightCell(Coordinate cell, String style) {
        String cellID = getColumnName(cell.getColumn()) + cell.getRow();
        Label cellLabel = cellToLabel.get(cellID);
        if (cellLabel != null) {
            cellLabel.setStyle(cellLabel.getStyle() + style);
        }
    }

    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            Cell selectedCell = engine.getCurrentSheet().getCell(createCoordinate(selectedRow, selectedCol));
            selectedCell.setBackgroundColor(toHexString(color));  // Save color in the cell object

            Label selectedLabel = cellToLabel.get(cellID);
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

            Label selectedLabel = cellToLabel.get(cellID);
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

    public void setColumnWidth(int columnIndex, double width) {
        if (spreadsheetGrid.getColumnConstraints().size() > columnIndex) {
            ColumnConstraints colConstraints = spreadsheetGrid.getColumnConstraints().get(columnIndex);
            colConstraints.setPrefWidth(width);  // Set preferred width
        }
    }

    public void setRowHeight(int rowIndex, double height) {
        if (spreadsheetGrid.getRowConstraints().size() > rowIndex) {
            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(rowIndex);
            rowConstraints.setPrefHeight(height);  // Set preferred height
        }
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
        // First, reset any previously highlighted range
        resetRangeHighlight();

        // Extract the start and end coordinates of the range
        Coordinate start = createCoordinate(range.getFrom());
        Coordinate end = createCoordinate(range.getTo());

        int startRow = start.getRow();
        int startCol = start.getColumn();
        int endRow = end.getRow();
        int endCol = end.getColumn();

        // Loop through all the cells in the range and highlight them
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Coordinate cellCoordinate = createCoordinate(row, col);

                // Apply the highlight style
                highlightCell(cellCoordinate, "-fx-border-color: #00ffcc; -fx-border-width: 2px; -fx-background-color: rgba(0,255,204,0.28);");
            }
        }
    }

    public void resetRangeHighlight() {
        // Reset the style of all cells that were highlighted as a range
        cellToLabel.values().forEach(label -> {
            // Remove only the range highlight without affecting the base style
            String currentStyle = label.getStyle();
            // Remove the range highlight
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
        return selectedCol; // Return the currently selected column
    }

    public int getSelectedRowIndex() {
        return selectedRow; // Return the currently selected row
    }

    public void displayRawSheet(GridPane gridPane, Sheet sheet) {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        int numRows = sheet.getRows();
        int numCols = sheet.getCols();
        int rowHeight = sheet.getRowHeight();
        int colWidth = sheet.getColWidth();

        // Adding column headers (A, B, C, etc.)
        for (int col = 1; col <= numCols; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            colHeader.setPrefWidth(colWidth);  // Match column width
            colHeader.setAlignment(Pos.CENTER);
            gridPane.add(colHeader, col, 0); // Add to column headers in the top row
        }

        // Adding row headers (1, 2, 3, etc.)
        for (int row = 1; row <= numRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            rowHeader.setPrefHeight(rowHeight); // Match row height
            rowHeader.setAlignment(Pos.CENTER);
            gridPane.add(rowHeader, 0, row); // Add to row headers in the first column
        }

        // Display cell values
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String cellID = getColumnName(col) + row;
                Coordinate coordinate = createCoordinate(row, col);
                Cell cell = sheet.getCell(coordinate);

                // Create a label with the value, without additional styles
                Label cellLabel = new Label(cell != null ? cell.getEffectiveValue().toString() : "");
                cellLabel.setAlignment(Pos.CENTER);

                // Set simple cell dimensions
                cellLabel.setPrefHeight(rowHeight);
                cellLabel.setPrefWidth(colWidth);

                // Add a border around each cell
                cellLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-text-fill: black;");

                // Add the label to the grid
                gridPane.add(cellLabel, col, row);

                final int finalRow = row;
                final int finalCol = col;
                cellLabel.setOnMouseClicked(event -> {
                    selectedRow = finalRow;
                    selectedCol = finalCol;
                    String selectedCellId = getColumnName(selectedCol) + selectedRow;

                    if (onCellSelected != null) {
                        onCellSelected.accept(selectedCellId);
                    }
                });

            }
        }
    }


    public Engine getEngine() {
        return this.engine;
    }

    public UIModel getUiModel() {
        return this.uiModel;
    }
}
