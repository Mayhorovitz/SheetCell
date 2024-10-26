package sheet.impl;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import range.api.Range;
import range.impl.RangeImpl;
import sheet.api.Sheet;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetImpl implements Sheet, Serializable {
    private String name;
    private int version;
    private int rows;
    private int cols;
    private int rowHeight;
    private int colWidth;
    private final Map<Coordinate, Cell> activeCells;
    private List<Cell> cellsThatHaveChanged;
    private final Map<String, Range> ranges;
    private Map<String, List<Cell>> rangeUsageMap; // מפת טווחים לתאים המשתמשים בהם

    // Constructors
    public SheetImpl() {
        this.activeCells = new HashMap<>();
        this.cellsThatHaveChanged = new ArrayList<>();
        this.ranges = new HashMap<>();
        this.rangeUsageMap = new HashMap<>(); // אתחול המפה


    }

    // Setters
    @Override
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Sheet name cannot be null or empty.");
        }
        this.name = name;
    }

    public void setSheetVersion(int loadVersion) {
        if (loadVersion < 1) {
            throw new IllegalArgumentException("Sheet version must be at least 1.");
        }
        this.version = loadVersion;
    }

    @Override
    public void setRows(int rows) {
        if (rows < 1) {
            throw new IllegalArgumentException("Number of rows must be at least 1.");
        }
        this.rows = rows;
    }

    @Override
    public void setCols(int cols) {
        if (cols < 1) {
            throw new IllegalArgumentException("Number of columns must be at least 1.");
        }
        this.cols = cols;
    }

    @Override
    public void setRowHeight(int rowHeight) {
        if (rowHeight < 1) {
            throw new IllegalArgumentException("Row height must be at least 1.");
        }
        this.rowHeight = rowHeight;
    }

    @Override
    public void setColWidth(int colWidth) {
        if (colWidth < 1) {
            throw new IllegalArgumentException("Column width must be at least 1.");
        }
        this.colWidth = colWidth;
    }

    // Getters
    public Map<Coordinate, Cell> getActiveCells() {
        return activeCells;
    }

    public List<Cell> getCellsThatHaveChanged() {
        return cellsThatHaveChanged;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    @Override
    public int getRowHeight() {
        return rowHeight;
    }

    @Override
    public int getColWidth() {
        return colWidth;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Cell getCell(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null.");
        }

        if (coordinate.getRow() < 1 || coordinate.getRow() > rows || coordinate.getColumn() < 1 || coordinate.getColumn() > cols) {
            throw new IllegalArgumentException("Coordinate " + coordinate + " is out of bounds. Sheet size is " + rows + " rows and " + cols + " columns.");
        }

        return activeCells.get(coordinate);
    }
    @Override
    public void addCell(Coordinate coordinate, Cell cell) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null.");
        }
        if (cell == null) {
        return;

        }


        activeCells.put(coordinate, cell);
    }

    public void addCellThatChanged(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        cellsThatHaveChanged.add(cell);
    }

    //Update a cell's value and recalculate the sheet.
    @Override
    public Sheet updateCellValueAndCalculate(String cellId, String value) {
        if (cellId == null || value == null) {
            throw new IllegalArgumentException("Cell ID and value cannot be null.");
        }

        Coordinate coordinate = createCoordinate(cellId);
        SheetImpl newSheetVersion = copySheet();
        newSheetVersion.cellsThatHaveChanged.clear();

        // Preserve existing cell styling if the cell already exists
        Cell existingCell = newSheetVersion.getCell(coordinate);
        String existingBackgroundColor = existingCell != null ? existingCell.getBackgroundColor() : "#FFFFFF"; // Default: white
        String existingTextColor = existingCell != null ? existingCell.getTextColor() : "#000000"; // Default: black

        // Create a new cell with the updated value and increment the version
        Cell newCell = new CellImpl(coordinate, value, newSheetVersion.getVersion() + 1, newSheetVersion);

        // Preserve the existing cell colors in the new cell
        newCell.setBackgroundColor(existingBackgroundColor);
        newCell.setTextColor(existingTextColor);

        // Add the updated cell to the activeCells map
        newSheetVersion.activeCells.put(coordinate, newCell);

        boolean dependenciesNeedUpdate = true;  // Flag to determine if dependencies should be updated

        try {
            if (dependenciesNeedUpdate) {
                newSheetVersion.updateDependenciesAndInfluences();
            }

            // Calculate effective values for cells that changed and update their versions
            List<Cell> cellsThatHaveChanged = newSheetVersion.orderCellsForCalculation()
                    .stream()
                    .filter(Cell::calculateEffectiveValue)
                    .collect(Collectors.toList());

            newSheetVersion.cellsThatHaveChanged = cellsThatHaveChanged;
            int newVersion = newSheetVersion.increaseVersion();
            cellsThatHaveChanged.forEach(cell -> cell.updateVersion(newVersion));

            return newSheetVersion;
        } catch (Exception e) {
            throw new IllegalStateException("Error updating cell value: " + e.getMessage());
        }
    }


    //order the cells to calculate the new effective values
    public List<Cell> orderCellsForCalculation() {
        List<Cell> orderedCells = new ArrayList<>();
        Map<Cell, Boolean> visited = new HashMap<>();

        try {
            // perform topological sort to order cells
            for (Cell cell : activeCells.values()) {
                if (!visited.containsKey(cell)) {
                    topologicalSort(cell, visited, orderedCells);
                }
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error during cell calculation order: " + e.getMessage(), e);
        }

        Collections.reverse(orderedCells);
        return orderedCells;
    }

    private void topologicalSort(Cell cell, Map<Cell, Boolean> visited, List<Cell> orderedCells) {
        visited.put(cell, true); // mark cell as visited

        // recursively sort the influencing cells
        for (Cell neighbor : cell.getInfluencingOn()) {
            if (!visited.containsKey(neighbor)) {
                topologicalSort(neighbor, visited, orderedCells);
            } else if (visited.get(neighbor)) {
                throw new IllegalStateException("Circular dependency detected involving cell: " + cell.getCoordinate().toString());
            }
        }

        visited.put(cell, false);
        orderedCells.add(cell);
    }

    private List<Coordinate> extractRefs(String value) {
        List<Coordinate> references = new ArrayList<>();
        String upperValue = value.toUpperCase();

        // Regular expression to match {REF,<cellId>}
        Pattern pattern = Pattern.compile("\\{REF,\\s*([^,\\s}]+)\\s*}");
        Matcher matcher = pattern.matcher(upperValue);

        while (matcher.find()) {
            String cellId = matcher.group(1).trim();
            references.add(createCoordinate(cellId));
        }

        return references;
    }

    // Copy sheet to create a new version
    private SheetImpl copySheet() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (SheetImpl) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Error during sheet copy: " + e.getMessage(), e);
        }
    }

    private int increaseVersion() {
        return ++this.version;
    }

    public void updateDependenciesAndInfluences() {
        // Reset dependencies and influences for all cells
        for (Cell cell : activeCells.values()) {
            cell.resetDependencies();
            cell.resetInfluences();
        }

        // Clear the range usage map
        rangeUsageMap.clear();

        // Recalculate dependencies and influences
        for (Cell cell : activeCells.values()) {
            String originalValue = cell.getOriginalValue();

            // Extract individual cell references
            List<Coordinate> influences = extractRefs(originalValue);

            List<String> rangeNames = extractRangeRefs(originalValue);
            for (String rangeName : rangeNames) {
                Range range = getRange(rangeName);
                if (range != null) {
                    // Add the current cell to the list of cells using this range
                    rangeUsageMap.computeIfAbsent(rangeName, k -> new ArrayList<>()).add(cell);

                    // Add all cells within the range as dependencies (influences)
                    List<Coordinate> rangeCoordinates = getRangeCoordinates(range);
                    influences.addAll(rangeCoordinates);
                } else {
                    throw new IllegalArgumentException("Range " + rangeName + " not found.");
                }
            }

            // Every dependent cell becomes an influence on the current cell
            for (Coordinate influenceCoordinate : influences) {
                Cell influenceCell = activeCells.get(influenceCoordinate);
                if (influenceCell != null) {
                    influenceCell.getInfluencingOn().add(cell);
                    cell.getDependsOn().add(influenceCell);
                }
            }
        }
    }

    // Helper function to extract range references from cell value
    private List<String> extractRangeRefs(String value) {
        List<String> rangeRefs = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\{(AVERAGE|SUM),\\s*([^,\\s}]+)\\s*}");
        Matcher matcher = pattern.matcher(value);

        while (matcher.find()) {
            String rangeName = matcher.group(2).trim();
            rangeRefs.add(rangeName);
        }

        return rangeRefs;
    }

    // Helper function to get all coordinates of cells within a range
    private List<Coordinate> getRangeCoordinates(Range range) {
        List<Coordinate> coordinates = new ArrayList<>();
        Coordinate start = createCoordinate(range.getFrom());
        Coordinate end = createCoordinate(range.getTo());

        for (int row = start.getRow(); row <= end.getRow(); row++) {
            for (int col = start.getColumn(); col <= end.getColumn(); col++) {
                coordinates.add(createCoordinate(row, col));
            }
        }

        return coordinates;
    }

    // Checking whether the rows and columns in the range are within the sheet boundaries
    private boolean isRangeWithinSheetBounds(Coordinate startCell, Coordinate endCell) {

        return startCell.getRow() >= 1 && startCell.getRow() <= rows &&
                endCell.getRow() >= 1 && endCell.getRow() <= rows &&
                startCell.getColumn() >= 1 && startCell.getColumn() <= cols &&
                endCell.getColumn() >= 1 && endCell.getColumn() <= cols;
    }

    //add new range
    @Override
    public void addRange(String name, String range) {
        if (ranges.containsKey(name)) {
            throw new IllegalArgumentException("Range with name '" + name + "' already exists.");
        }
        List<Coordinate> coordinates = extractCells(range);

        Coordinate startCell = coordinates.get(0);
        Coordinate endCell = coordinates.get(1);

        if (!isRangeWithinSheetBounds(startCell, endCell)) {
            throw new IllegalArgumentException("Range '" + name + "' is out of sheet bounds.");
        }

        ranges.put(name, new RangeImpl(name, startCell, endCell, this));
    }

    @Override
    public List<Cell> getCellsInRange(Range range) {
        List<Cell> cellsInRange = new ArrayList<>();
        Coordinate start = createCoordinate(range.getFrom());
        Coordinate end = createCoordinate(range.getTo());

        for (int row = start.getRow(); row <= end.getRow(); row++) {
            for (int col = start.getColumn(); col <= end.getColumn(); col++) {
                Cell cell = getCell(createCoordinate(row, col));
                if (cell != null) {
                    cellsInRange.add(cell);
                }
            }
        }
        return cellsInRange;
    }

    // Check if there are cells that use the range in the map
    @Override
    public void deleteRange(String name) {
        if (!ranges.containsKey(name)) {
            throw new IllegalArgumentException("Range not found.");
        }
        // Check if there are cells that use the range in the map
        if (rangeUsageMap.containsKey(name) && !rangeUsageMap.get(name).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete range '" + name + "' because it is in use.");
        }

        ranges.remove(name);
    }


    @Override
    public Range getRange(String name) {
        return ranges.get(name);
    }

    @Override
    public Collection<Range> getAllRanges() {
        return ranges.values();
    }

    // Extract the start and end cells
    private List<Coordinate> extractCells(String rangeCells) {

        String[] cellRange = rangeCells.split("\\.\\."); // Using regex to split by ".."
        if (cellRange.length != 2) {
            throw new IllegalArgumentException("Please specify the range in the format 'A1..B3'.");
        }


        String startCell = cellRange[0].trim();
        String endCell = cellRange[1].trim();

        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(createCoordinate((startCell)));
        coordinates.add(createCoordinate((endCell)));

        return coordinates;
    }

    @Override

    public Sheet filterSheetByValues(String range, String column, List<String> selectedValues, List<Integer> originalRowNumbers) {
        SheetImpl filteredSheet = new SheetImpl();
        filteredSheet.setName(this.name + " - Filtered");
        filteredSheet.setRowHeight(this.rowHeight);
        filteredSheet.setColWidth(this.colWidth);
        filteredSheet.setSheetVersion(this.version);

        List<Coordinate> rangeCoordinates = extractCells(range);
        Coordinate startCell = rangeCoordinates.get(0);
        Coordinate endCell = rangeCoordinates.get(1);

        int colIndex = getColumnIndexFromName(column);

        int startCol = startCell.getColumn();
        int endCol = endCell.getColumn();
        filteredSheet.setCols(this.cols);
        filteredSheet.setRows(this.rows);

        for (int row = startCell.getRow(); row <= endCell.getRow(); row++) {
            Coordinate coordinate = createCoordinate(row, colIndex);
            Cell cell = getCell(coordinate);
            if (cell != null) {
                String value = cell.getEffectiveValue().toString();
                if (selectedValues.contains(value)) {
                    originalRowNumbers.add(row);

                    for (int col = startCol; col <= endCol; col++) {
                        Coordinate cellCoordinate = createCoordinate(row, col);
                        Cell originalCell = getCell(cellCoordinate);
                        if (originalCell != null) {
                            Cell newCell = originalCell;
                            filteredSheet.addCell(cellCoordinate, newCell);
                        }
                    }
                }
            }
        }

        return filteredSheet;
    }


    @Override
    public Sheet sortSheet(String range, String[] columnsToSort) {
        SheetImpl sortedSheet = copySheet();

        List<Coordinate> coordinates = extractCells(range);
        Coordinate startCell = coordinates.get(0);
        Coordinate endCell = coordinates.get(1);

        int startRow = startCell.getRow();
        int endRow = endCell.getRow();
        int startCol = startCell.getColumn();
        int endCol = endCell.getColumn();

        List<List<Cell>> rowsInRange = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            List<Cell> rowCells = new ArrayList<>();
            for (int col = startCol; col <= endCol; col++) {
                Coordinate coordinate = createCoordinate(row, col);
                Cell cell = this.getCell(coordinate);
                rowCells.add(cell);
            }
            rowsInRange.add(rowCells);
        }

        // Sort the rows based on specified columns
        rowsInRange.sort((row1, row2) -> {
            for (String colToSort : columnsToSort) {
                int colIndex = getColumnIndexFromName(colToSort) - startCol;

                if (colIndex >= 0 && colIndex < row1.size()) {
                    Cell cell1 = row1.get(colIndex);
                    Cell cell2 = row2.get(colIndex);

                    try {
                        Double value1 = cell1 != null ? Double.parseDouble(cell1.getEffectiveValue().toString()) : Double.MAX_VALUE;
                        Double value2 = cell2 != null ? Double.parseDouble(cell2.getEffectiveValue().toString()) : Double.MAX_VALUE;

                        int comparison = value1.compareTo(value2);
                        if (comparison != 0) {
                            return comparison;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            return 0;
        });

        // Update the sorted sheet with new cell positions
        for (int row = startRow; row <= endRow; row++) {
            List<Cell> sortedRow = rowsInRange.get(row - startRow);
            for (int col = startCol; col <= endCol; col++) {
                Coordinate coordinate = createCoordinate(row, col);
                sortedSheet.addCell(coordinate, sortedRow.get(col - startCol));
            }
        }

        return sortedSheet;
    }

    @Override
    public List<String> getUniqueValuesInRangeColumn(String range, String column) {
        List<Coordinate> rangeCoordinates = extractCells(range);
        Coordinate startCell = rangeCoordinates.get(0);
        Coordinate endCell = rangeCoordinates.get(1);

        int colIndex = getColumnIndexFromName(column);

        // Check if column is within range
        if (colIndex < startCell.getColumn() || colIndex > endCell.getColumn()) {
            throw new IllegalArgumentException("Column " + column + " is not within the specified range.");
        }

        Set<String> uniqueValues = new HashSet<>();

        for (int row = startCell.getRow(); row <= endCell.getRow(); row++) {
            Coordinate coordinate = createCoordinate(row, colIndex);
            Cell cell = getCell(coordinate);
            if (cell != null) {
                String value = cell.getEffectiveValue().toString();
                uniqueValues.add(value);
            }
        }

        return new ArrayList<>(uniqueValues);
    }


    private int getColumnIndexFromName(String columnName) {
        int index = 0;
        for (char c : columnName.toUpperCase().toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index;
    }

}
