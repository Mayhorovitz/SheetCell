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
    private Map<Coordinate, Cell> activeCells;
    private List<Cell> cellsThatHaveChanged;
    private Map<String, Range> ranges;

    // Constructors
    public SheetImpl() {
        this.activeCells = new HashMap<>();
        this.cellsThatHaveChanged = new ArrayList<>();
        this.ranges = new HashMap<>();

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
        return activeCells.get(coordinate);
    }

    @Override
    public void addCell(Coordinate coordinate, Cell cell) {
        if (coordinate == null || cell == null) {
            throw new IllegalArgumentException("Coordinate and Cell cannot be null.");
        }

        // שמירה על צבע הרקע וצבע הטקסט בתא הנוכחי
        if (activeCells.containsKey(coordinate)) {
            Cell existingCell = activeCells.get(coordinate);
            cell.setBackgroundColor(existingCell.getBackgroundColor());
            cell.setTextColor(existingCell.getTextColor());
        }

        activeCells.put(coordinate, cell);
    }

    public void addCellThatChanged(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        cellsThatHaveChanged.add(cell);
    }

    @Override
    public Sheet updateCellValueAndCalculate(String cellId, String value) {
        if (cellId == null || value == null) {
            throw new IllegalArgumentException("Cell ID and value cannot be null.");
        }

        Coordinate coordinate = createCoordinate(cellId);
        SheetImpl newSheetVersion = copySheet();
        newSheetVersion.cellsThatHaveChanged.clear();

        // שמירת העיצוב הקיים של התא, אם קיים תא כזה
        Cell existingCell = newSheetVersion.getCell(coordinate);
        String existingBackgroundColor = existingCell != null ? existingCell.getBackgroundColor() : "#FFFFFF"; // ברירת מחדל: לבן
        String existingTextColor = existingCell != null ? existingCell.getTextColor() : "#000000"; // ברירת מחדל: שחור

        // יצירת תא חדש עם הערך המעודכן ושמירת גרסה חדשה
        Cell newCell = new CellImpl(coordinate, value, newSheetVersion.getVersion() + 1, newSheetVersion);

        // שמירת צבעי התא הקיימים בתא החדש
        newCell.setBackgroundColor(existingBackgroundColor);
        newCell.setTextColor(existingTextColor);

        // הוספת התא המעודכן למפה
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

        visited.put(cell, false); // mark cell as processed
        orderedCells.add(cell); // add cell to the ordered list
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
        //rest lists
        for (Cell cell : activeCells.values()) {
            cell.resetDependencies();
            cell.resetInfluences();
        }
        //get all cells with refs
        for (Cell cell : activeCells.values()) {
            String originalValue = cell.getOriginalValue();
            List<Coordinate> influences = extractRefs(originalValue);

            for (Coordinate influencesCoordinate : influences) {
                Cell influenceCell = activeCells.get(influencesCoordinate);

                if (influenceCell != null) {
                    influenceCell.getInfluencingOn().add(cell);
                    cell.getDependsOn().add(influenceCell);
                }
            }
        }
    }

    @Override
    public void addRange(String name, String startCell, String endCell) {
        if (ranges.containsKey(name)) {
            throw new IllegalArgumentException("Range with this name already exists.");
        }
        ranges.put(name, new RangeImpl(name, startCell, endCell, this));
    }

    @Override
    public void deleteRange(String name) {
        if (!ranges.containsKey(name)) {
            throw new IllegalArgumentException("Range not found.");
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

}
