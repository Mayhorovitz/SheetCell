package javaFX.sheet;

import dto.api.CellDTO;
import dto.api.RangeDTO;
import dto.api.SheetDTO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for displaying and interacting with the spreadsheet grid.
 */
public class SheetController {
    private Engine engine;
    private MainController mainController;
    private UIModel uiModel;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private CellDTO lastSelectedCell = null;

    @FXML
    private GridPane spreadsheetGrid;
    @FXML
    private ScrollPane spreadsheetScrollPane;

    private final Map<String, Label> cellToLabel = new HashMap<>();

    private Consumer<String> onCellSelected;

    private boolean isReadOnly = false;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    public Engine getEngine() {
        return this.engine;
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
    public UIModel getUiModel() {
        return this.uiModel;
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

    public int getSelectedColumnIndex() {
        return selectedCol;
    }

    public int getSelectedRowIndex() {
        return selectedRow;
    }

    public void displaySheet(SheetDTO sheetDTO) {
        if (spreadsheetGrid.getChildren().isEmpty()) {
            // Only initialize if the grid is empty
            initializeSheet(sheetDTO);
        } else {
            updateSheet(sheetDTO);
        }

        spreadsheetScrollPane.setPannable(true);
    }

    public void initializeSheet(SheetDTO sheetDTO) {
        cellToLabel.clear();
        clearGridPane();

        int numRows = sheetDTO.getRows();
        int numCols = sheetDTO.getCols();

        spreadsheetGrid.setHgap(0);
        spreadsheetGrid.setVgap(0);

        // Remove padding and border from the GridPane
        spreadsheetGrid.setPadding(Insets.EMPTY);
        spreadsheetGrid.setStyle("-fx-border-color: transparent;");

        // Get row height and column width from the sheet
        int rowHeightFromSheet = sheetDTO.getRowHeight();
        int colWidthFromSheet = sheetDTO.getColWidth();

        // Initialize rows and columns based on values from the sheet
        addColumnAndRowConstraints(numCols, colWidthFromSheet, numRows, rowHeightFromSheet);
        addColumnsAndRowHeaders(numCols, numRows);
        populateSheetGrid(sheetDTO, numCols, numRows);
    }

    private void clearGridPane() {
        // Clear existing cells in the GridPane
        spreadsheetGrid.getChildren().clear();

        // Reset column and row constraints
        spreadsheetGrid.getColumnConstraints().clear();
        spreadsheetGrid.getRowConstraints().clear();
    }

    private void updateSheet(SheetDTO sheetDTO) {
        int numRows = sheetDTO.getRows();
        int numCols = sheetDTO.getCols();

        // Update cells that have changed
        populateSheetGrid(sheetDTO, numCols, numRows);
    }

    private void addColumnAndRowConstraints(int numCols, double colWidth, int numRows, double rowHeight) {
        // Add ColumnConstraints (including header column at index 0)
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
        for (int col = 1; col <= numCols; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; "
                    + "-fx-border-color: black; -fx-border-width: 1px;");
            colHeader.setAlignment(Pos.CENTER);

            ColumnConstraints colConstraints = spreadsheetGrid.getColumnConstraints().get(col);
            colHeader.prefWidthProperty().bind(colConstraints.prefWidthProperty());

            RowConstraints headerRowConstraints = spreadsheetGrid.getRowConstraints().get(0);
            colHeader.prefHeightProperty().bind(headerRowConstraints.prefHeightProperty());

            spreadsheetGrid.add(colHeader, col, 0);
        }

        for (int row = 1; row <= numRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; "
                    + "-fx-border-color: black; -fx-border-width: 1px;");
            rowHeader.setAlignment(Pos.CENTER); // Center the text

            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(row);
            rowHeader.prefHeightProperty().bind(rowConstraints.prefHeightProperty());

            ColumnConstraints headerColConstraints = spreadsheetGrid.getColumnConstraints().get(0);
            rowHeader.prefWidthProperty().bind(headerColConstraints.prefWidthProperty());

            spreadsheetGrid.add(rowHeader, 0, row);
        }
    }

    private void populateSheetGrid(SheetDTO sheetDTO, int numCols, int numRows) {
        Map<String, CellDTO> cells = sheetDTO.getCells();

        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String cellID = getColumnName(col) + row;
                CellDTO cellDTO = cells.get(cellID);

                if (cellToLabel.containsKey(cellID)) {
                    Label existingLabel = cellToLabel.get(cellID);

                    // Update the value and reapply style
                    existingLabel.setText(cellDTO != null ? cellDTO.getEffectiveValue() : "");
                    applyCellStyle(cellDTO, existingLabel);

                } else {
                    // Create a new label if it doesn't exist
                    Label cellLabel = new Label(cellDTO != null ? cellDTO.getEffectiveValue() : "");
                    cellLabel.setAlignment(Pos.CENTER);

                    cellLabel.setPrefHeight(uiModel.getRowHeight());
                    cellLabel.setPrefWidth(uiModel.getColWidth());
                    cellLabel.getStyleClass().add("cell");
                    applyCellStyle(cellDTO, cellLabel);

                    cellToLabel.put(cellID, cellLabel);

                    spreadsheetGrid.add(cellLabel, col, row);

                    // Add listener for clicks on the cell
                    final int finalRow = row;
                    final int finalCol = col;
                    cellLabel.setOnMouseClicked(event -> {
                        selectedRow = finalRow;
                        selectedCol = finalCol;
                        String selectedCellId = getColumnName(selectedCol) + selectedRow;

                        if (isReadOnly && onCellSelected != null) {
                            onCellSelected.accept(selectedCellId);
                        } else if (mainController != null) {
                            mainController.handleCellSelection(selectedCellId);
                        }
                    });
                }
            }
        }
    }

    private void applyCellStyle(CellDTO cellDTO, Label label) {
        String borderStyle = "-fx-border-color: black; -fx-border-width: 1px;";

        if (cellDTO != null) {
            label.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; %s",
                    cellDTO.getBackgroundColor(), cellDTO.getTextColor(), borderStyle
            ));
        } else {
            // Default to white background and black text, always with the border
            label.setStyle("-fx-background-color: white; -fx-text-fill: black;" + borderStyle);
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

    public void highlightDependenciesAndInfluences(CellDTO selectedCellDTO) {
        resetCellBorders();  // Reset previous highlights
        if (selectedCellDTO != null) {
            lastSelectedCell = selectedCellDTO;
            highlightCells(selectedCellDTO.getDependsOn(), "lightblue");
            highlightCells(selectedCellDTO.getInfluencingOn(), "lightgreen");
        }
        Label selectedLabel = cellToLabel.get(getColumnName(selectedCol) + selectedRow);
        if (selectedLabel != null) {
            String currentStyle = selectedLabel.getStyle();
            selectedLabel.setStyle(currentStyle + "; -fx-border-color: blue; -fx-border-width: 3px;");
        }
    }

    private void highlightCells(List<String> cellIds, String color) {
        for (String cellId : cellIds) {
            Label cellLabel = cellToLabel.get(cellId);
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

    public void clearHighlights(List<String> cellIds) {
        for (String cellId : cellIds) {
            Label cellLabel = cellToLabel.get(cellId);
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
    private void highlightCell(String cellId, String style) {
        Label cellLabel = cellToLabel.get(cellId);
        if (cellLabel != null) {
            cellLabel.setStyle(cellLabel.getStyle() + style);
        }
    }

    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            String colorHex = toHexString(color);
            // Notify the engine to update the cell's background color
            mainController.getEngine().updateCellBackgroundColor(cellID, colorHex);

            Label selectedLabel = cellToLabel.get(cellID);
            if (selectedLabel != null) {
                // Keep existing styles, just append the background color
                String currentStyle = selectedLabel.getStyle();
                selectedLabel.setStyle(currentStyle + "-fx-background-color: " + colorHex + ";");
            }
        }
    }

    public void applyTextColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            String colorHex = toHexString(color);
            // Notify the engine to update the cell's text color
            mainController.getEngine().updateCellTextColor(cellID, colorHex);

            Label selectedLabel = cellToLabel.get(cellID);
            if (selectedLabel != null) {
                // Keep existing styles, just append the text color
                String currentStyle = selectedLabel.getStyle();
                selectedLabel.setStyle(currentStyle + "-fx-text-fill: " + colorHex + ";");
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
            String cellID = getColumnName(selectedCol) + selectedRow;
            // Notify the engine to reset the cell's design
            mainController.getEngine().resetCellDesign(cellID);

            // Update the specific cell's appearance in the grid
            Label cellLabel = cellToLabel.get(cellID);
            if (cellLabel != null) {
                String currentStyle = cellLabel.getStyle();
                // Replace only the text color and background, without affecting other styles
                currentStyle = currentStyle.replaceAll("-fx-text-fill:.*?;", "");
                currentStyle = currentStyle.replaceAll("-fx-background-color:.*?;", "");
                cellLabel.setStyle(currentStyle + "-fx-text-fill: black; -fx-background-color: white;");
            }
        }
    }

    public void highlightRange(RangeDTO rangeDTO) {
        // First, reset any previously highlighted range
        resetRangeHighlight();

        // Extract the start and end coordinates of the range
        String startCellId = rangeDTO.getFrom();
        String endCellId = rangeDTO.getTo();

        int startRow = extractRowFromCellId(startCellId);
        int startCol = extractColumnFromCellId(startCellId);
        int endRow = extractRowFromCellId(endCellId);
        int endCol = extractColumnFromCellId(endCellId);

        // Loop through all the cells in the range and highlight them
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                String cellId = getColumnName(col) + row;

                // Apply the highlight style
                highlightCell(cellId, "-fx-border-color: #00ffcc; -fx-border-width: 2px; -fx-background-color: rgba(0,255,204,0.28);");
            }
        }
    }

    public void resetRangeHighlight() {
        cellToLabel.values().forEach(label -> {
            String currentStyle = label.getStyle();
            // Remove the range highlight
            currentStyle = currentStyle.replaceAll("-fx-background-color: rgba\\(0,255,204,0.28\\);", "");
            currentStyle = currentStyle.replaceAll("-fx-border-color: #00ffcc; -fx-border-width: 2px;", "");
            label.setStyle(currentStyle);
        });
    }

    public void initializeFilterSheet(SheetDTO sheetDTO) {
        cellToLabel.clear();
        clearGridPane();

        Map<String, CellDTO> activeCells = sheetDTO.getCells();

        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;

        for (String cellId : activeCells.keySet()) {
            int row = extractRowFromCellId(cellId);
            int col = extractColumnFromCellId(cellId);
            if (row < minRow) minRow = row;
            if (row > maxRow) maxRow = row;
            if (col < minCol) minCol = col;
            if (col > maxCol) maxCol = col;
        }

        int numRows = maxRow - minRow + 1;
        int numCols = maxCol - minCol + 1;

        // Remove gaps between cells
        spreadsheetGrid.setHgap(0);
        spreadsheetGrid.setVgap(0);

        // Remove padding and border from the GridPane
        spreadsheetGrid.setPadding(Insets.EMPTY);
        spreadsheetGrid.setStyle("-fx-border-color: transparent;");

        // Get row height and column width from the sheet
        int rowHeightFromSheet = sheetDTO.getRowHeight();
        int colWidthFromSheet = sheetDTO.getColWidth();

        // Initialize rows and columns based on values from the sheet
        addColumnAndRowConstraints(numCols, colWidthFromSheet, numRows, rowHeightFromSheet);
        addFilterColumnsAndRowHeaders(minCol, maxCol, minRow, maxRow);
        populateFilterSheetGrid(sheetDTO, numCols, numRows, minCol, minRow);
    }

    private void addFilterColumnsAndRowHeaders(int minCol, int maxCol, int minRow, int maxRow) {
        // Adding column headers
        for (int col = minCol; col <= maxCol; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            colHeader.setPrefWidth(uiModel.getColWidth());
            colHeader.setPrefHeight(uiModel.getRowHeight());
            colHeader.setAlignment(Pos.CENTER);
            spreadsheetGrid.add(colHeader, col - minCol + 1, 0);
        }

        for (int row = minRow; row <= maxRow; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-border-color: black; -fx-border-width: 1px;");
            rowHeader.setPrefWidth(uiModel.getColWidth());
            rowHeader.setPrefHeight(uiModel.getRowHeight());
            rowHeader.setAlignment(Pos.CENTER);
            spreadsheetGrid.add(rowHeader, 0, row - minRow + 1);
        }
    }

    private void populateFilterSheetGrid(SheetDTO sheetDTO, int numCols, int numRows, int minCol, int minRow) {
        Map<String, CellDTO> activeCells = sheetDTO.getCells();

        // Build the column and row headers using original indices
        for (String cellId : activeCells.keySet()) {
            int row = extractRowFromCellId(cellId);
            int col = extractColumnFromCellId(cellId);
            int rowIndex = row - minRow + 1;
            int colIndex = col - minCol + 1;

            CellDTO cellDTO = activeCells.get(cellId);

            Label cellLabel = new Label(cellDTO.getEffectiveValue());
            cellLabel.setAlignment(Pos.CENTER);

            // Set cell dimensions based on the UI model
            cellLabel.setPrefHeight(uiModel.getRowHeight());
            cellLabel.setPrefWidth(uiModel.getColWidth());
            cellLabel.getStyleClass().add("cell");
            applyCellStyle(cellDTO, cellLabel);
            cellToLabel.put(cellId, cellLabel);

            spreadsheetGrid.add(cellLabel, colIndex, rowIndex);

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

    private int extractRowFromCellId(String cellId) {
        String rowPart = cellId.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    private int extractColumnFromCellId(String cellId) {
        String columnPart = cellId.replaceAll("[^A-Za-z]", "");
        return convertColumnToIndex(columnPart);
    }

    private int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

}
