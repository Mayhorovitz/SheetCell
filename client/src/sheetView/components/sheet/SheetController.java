package sheetView.components.sheet;

import dto.api.RangeDTO;
import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetView.main.SheetViewMainController;
import sheetView.main.UIModel;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Controller for displaying and interacting with the spreadsheet grid.
 */
public class SheetController {

    private SheetViewMainController sheetViewMainController;
    private UIModel uiModel;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private CellDTOImpl lastSelectedCell = null;
    private SheetDTO currentSheet;


    @FXML
    private GridPane spreadsheetGrid;
    @FXML
    private ScrollPane spreadsheetScrollPane;

    private final Map<String, Label> cellToLabel = new HashMap<>();

    private Consumer<String> onCellSelected;

    private boolean isReadOnly = false;

    public SheetDTO getCurrentSheet(){
        return currentSheet;
    }

    public void setMainController(SheetViewMainController sheetViewMainController) {
        this.sheetViewMainController = sheetViewMainController;
    }

    public void setCurrentSheet(SheetDTO currentSheet) {
        this.currentSheet = currentSheet;
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

    public void displaySheet() {
        if (spreadsheetGrid.getChildren().isEmpty()) {
            // Only initialize if the grid is empty
            initializeSheet(currentSheet);
        } else {
            updateSheet(currentSheet);
        }

        spreadsheetScrollPane.setPannable(true);
    }
    private void initializeSheetBase(SheetDTO sheetDTO, int numRows, int numCols, int rowHeight, int colWidth) {
        cellToLabel.clear();
        clearGridPane();

        spreadsheetGrid.setHgap(0);
        spreadsheetGrid.setVgap(0);
        spreadsheetGrid.setPadding(Insets.EMPTY);
        spreadsheetGrid.setStyle("-fx-border-color: transparent;");

        addColumnAndRowConstraints(numCols, colWidth, numRows, rowHeight);
    }

    public void initializeSheet(SheetDTO sheetDTO) {
        this.currentSheet = sheetDTO;

        int numRows = sheetDTO.getRows();
        int numCols = sheetDTO.getCols();
        int rowHeight = sheetDTO.getRowHeight();
        int colWidth = sheetDTO.getColWidth();

        initializeSheetBase(sheetDTO, numRows, numCols, rowHeight, colWidth);
        addHeaders(1, numCols, 1, numRows, 0, 0);
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
    // Helper method to add headers
    private void addHeaders(int startCol, int endCol, int startRow, int endRow, int colOffset, int rowOffset) {
        // Add the top-left corner cell (usually left empty or used as needed)
        Label cornerLabel = new Label();
        cornerLabel.getStyleClass().add("corner-header");
        spreadsheetGrid.add(cornerLabel, colOffset, rowOffset);

        // Adding column headers (start from colOffset + 1)
        for (int col = startCol; col <= endCol; col++) {
            Label colHeader = new Label(getColumnName(col));
            colHeader.getStyleClass().add("column-header");
            colHeader.setAlignment(Pos.CENTER);

            ColumnConstraints colConstraints = spreadsheetGrid.getColumnConstraints().get(col - startCol + colOffset + 1);
            colHeader.prefWidthProperty().bind(colConstraints.prefWidthProperty());

            RowConstraints headerRowConstraints = spreadsheetGrid.getRowConstraints().get(rowOffset);
            colHeader.prefHeightProperty().bind(headerRowConstraints.prefHeightProperty());

            spreadsheetGrid.add(colHeader, col - startCol + colOffset + 1, rowOffset);
        }

        // Adding row headers (start from rowOffset + 1)
        for (int row = startRow; row <= endRow; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.getStyleClass().add("row-header");
            rowHeader.setAlignment(Pos.CENTER);

            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(row - startRow + rowOffset + 1);
            rowHeader.prefHeightProperty().bind(rowConstraints.prefHeightProperty());

            ColumnConstraints headerColConstraints = spreadsheetGrid.getColumnConstraints().get(colOffset);
            rowHeader.prefWidthProperty().bind(headerColConstraints.prefWidthProperty());

            spreadsheetGrid.add(rowHeader, colOffset, row - startRow + rowOffset + 1);
        }
    }


    // Helper method to process individual cells
    private void processCell(CellDTOImpl cellDTO, int row, int col, int gridRowIndex, int gridColIndex) {
        String cellID = getColumnName(col) + row;

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

            spreadsheetGrid.add(cellLabel, gridColIndex, gridRowIndex);

            // Add listener for clicks on the cell
            final int finalRow = row;
            final int finalCol = col;
            cellLabel.setOnMouseClicked(event -> {
                selectedRow = finalRow;
                selectedCol = finalCol;
                String selectedCellId = getColumnName(selectedCol) + selectedRow;

                if (isReadOnly && onCellSelected != null) {
                    onCellSelected.accept(selectedCellId);
                } else if (sheetViewMainController != null) {
                    sheetViewMainController.handleCellSelection(selectedCellId);
                }
            });
        }
    }

    // Refactored populateSheetGrid method
    private void populateSheetGrid(SheetDTO sheetDTO, int numCols, int numRows) {
        Map<String, CellDTOImpl> cells = sheetDTO.getCells();

        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String cellID = getColumnName(col) + row;
                CellDTOImpl cellDTO = cells.get(cellID);
                processCell(cellDTO, row, col, row, col);
            }
        }
    }

    // Refactored populateFilterSheetGrid method
    private void populateFilterSheetGrid(SheetDTO sheetDTO, int minCol, int minRow) {
        Map<String, CellDTOImpl> activeCells = sheetDTO.getCells();

        for (String cellId : activeCells.keySet()) {
            int row = extractRowFromCellId(cellId);
            int col = extractColumnFromCellId(cellId);
            int gridRowIndex = row - minRow + 1;
            int gridColIndex = col - minCol + 1;

            CellDTOImpl cellDTO = activeCells.get(cellId);

            processCell(cellDTO, row, col, gridRowIndex, gridColIndex);
        }
    }


    private void applyCellStyle(CellDTOImpl cellDTO, Label label) {
        // Remove any previous highlight classes
        label.getStyleClass().removeAll("cell", "cell-custom");

        label.getStyleClass().add("cell");

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


    public void highlightDependenciesAndInfluences(CellDTOImpl selectedCellDTO) {
        resetCellHighlights();  // Reset previous highlights

        if (selectedCellDTO != null) {
            lastSelectedCell = selectedCellDTO;

            highlightCells(selectedCellDTO.getDependsOn(), "cell-dependency");
            highlightCells(selectedCellDTO.getInfluencingOn(), "cell-influence");
        }

        Label selectedLabel = cellToLabel.get(getColumnName(selectedCol) + selectedRow);
        if (selectedLabel != null) {
            selectedLabel.getStyleClass().add("cell-selected");
        }
    }

    private void highlightCells(List<String> cellIds, String styleClass) {
        for (String cellId : cellIds) {
            Label cellLabel = cellToLabel.get(cellId);
            if (cellLabel != null) {
                cellLabel.getStyleClass().add(styleClass);
            }
        }
    }

    public void clearPreviousHighlights() {
        if (lastSelectedCell != null) {
            resetCellHighlights();
        }
    }

    private void resetCellHighlights() {
        cellToLabel.values().forEach(label -> {
            label.getStyleClass().removeAll("cell-selected", "cell-dependency", "cell-influence", "cell-range-highlight");
        });
    }



    public void applyBackgroundColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            String colorHex = toHexString(color);

            HttpUrl url = HttpUrl.parse(Constants.UPDATE_BACKGROUND);
            if (url == null) {
                sheetViewMainController.showErrorAlert("Invalid URL for updating background color.");
                return;
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("sheetName", currentSheet.getName())
                    .add("cellId", cellID)
                    .add("colorHex", colorHex)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to update cell background color: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            Label selectedLabel = cellToLabel.get(cellID);
                            if (selectedLabel != null) {
                                String currentStyle = selectedLabel.getStyle();
                                // Update the background color inline
                                selectedLabel.setStyle(currentStyle + "-fx-background-color: " + colorHex + ";");
                            }
                        });
                    } else {
                        Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to update cell background color: " + response.message()));
                    }
                }
            });
        }
    }


    public void applyTextColorToSelectedCell(Color color) {
        if (selectedRow != -1 && selectedCol != -1) {
            String cellID = getColumnName(selectedCol) + selectedRow;
            String colorHex = toHexString(color);

            HttpUrl url = HttpUrl.parse(Constants.UPDATE_TEXT);
            if (url == null) {
                sheetViewMainController.showErrorAlert("Invalid URL for updating text color.");
                return;
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("sheetName", currentSheet.getName())
                    .add("cellId", cellID)
                    .add("colorHex", colorHex)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to update cell text color: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            Label selectedLabel = cellToLabel.get(cellID);
                            if (selectedLabel != null) {
                                String currentStyle = selectedLabel.getStyle();
                                // Update the text color inline
                                selectedLabel.setStyle(currentStyle + "-fx-text-fill: " + colorHex + ";");
                            }
                        });
                    } else {
                        Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to update cell text color: " + response.message()));
                    }
                }
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
        Pos pos = switch (alignment) {
            case "Left" -> Pos.CENTER_LEFT;
            case "Right" -> Pos.CENTER_RIGHT;
            default -> Pos.CENTER;
        };

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

            String finalUrl = HttpUrl.parse(Constants.RESET_CELL_DESIGN)
                    .newBuilder()
                    .addQueryParameter("sheetName", currentSheet.getName())
                    .addQueryParameter("cellId", cellID)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to reset cell design: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            Label cellLabel = cellToLabel.get(cellID);
                            if (cellLabel != null) {
                                // Reset inline styles for background and text color
                                String currentStyle = cellLabel.getStyle();
                                // Remove background color
                                currentStyle = currentStyle.replaceAll("-fx-background-color: #[A-Fa-f0-9]{6};", "");
                                // Remove text color
                                currentStyle = currentStyle.replaceAll("-fx-text-fill: #[A-Fa-f0-9]{6};", "");
                                cellLabel.setStyle(currentStyle);
                            }
                        });
                    } else {
                        Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to reset cell design: " + response.message()));
                    }
                }
            });
        }
    }


    public void highlightRange(RangeDTO rangeDTO) {
        resetRangeHighlight();

        String startCellId = rangeDTO.getFrom();
        String endCellId = rangeDTO.getTo();

        int startRow = extractRowFromCellId(startCellId);
        int startCol = extractColumnFromCellId(startCellId);
        int endRow = extractRowFromCellId(endCellId);
        int endCol = extractColumnFromCellId(endCellId);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                String cellId = getColumnName(col) + row;

                Label cellLabel = cellToLabel.get(cellId);
                if (cellLabel != null) {
                    cellLabel.getStyleClass().add("cell-range-highlight");
                }
            }
        }
    }

    public void resetRangeHighlight() {
        cellToLabel.values().forEach(label -> {
            label.getStyleClass().remove("cell-range-highlight");
        });
    }
    public void initializeFilterSheet(SheetDTO sheetDTO) {
        Map<String, CellDTOImpl> activeCells = sheetDTO.getCells();

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

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
        int rowHeight = sheetDTO.getRowHeight();
        int colWidth = sheetDTO.getColWidth();

        initializeSheetBase(sheetDTO, numRows, numCols, rowHeight, colWidth);
        addHeaders(minCol, maxCol, minRow, maxRow, 0, 0);
        populateFilterSheetGrid(sheetDTO, minCol, minRow);
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

    public boolean isReadOnly() {
        return sheetViewMainController.isReadOnly();
    }


public String getSelectedCellIndex() {
    if (selectedRow != -1 && selectedCol != -1) {
        return getColumnName(selectedCol) + selectedRow;
    }

    return "";
}


    public void displayTemporarySheet(SheetDTO sheetDTO) {

        // Clear the grid and cell labels
        cellToLabel.clear();
        clearGridPane();

        // Initialize the sheet grid with the updated sheetDTO
        initializeSheet(sheetDTO);

    }

    public CellDTOImpl getCellDTO(String cellId) {
        // Fetch cell data from the current sheet
        return currentSheet.getCells().get(cellId);
    }

    public void displayOriginalSheet(SheetDTO originalSheet) {
        if (currentSheet != null) {
            this.currentSheet = originalSheet;
            displaySheet();
        }
    }

}