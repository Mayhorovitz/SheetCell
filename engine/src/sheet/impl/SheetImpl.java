package sheet.impl;

import cell.impl.CellImpl;
import coordinate.CoordinateImpl;
import sheet.api.Sheet;
import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static coordinate.CoordinateFactory.createCoordinate;

public class SheetImpl implements Sheet , Serializable {
    private String name;
    private int version;
    private int rows;
    private int cols;
    private int rowHeight;
    private int colWidth;
    private Map<Coordinate, Cell> activeCells;
    private List<Coordinate> cellsThatHaveChanged;


    public SheetImpl() {
        this.activeCells = new HashMap<>();
        this.cellsThatHaveChanged = new ArrayList<>();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Map<Coordinate, Cell> getActiveCells() {
        return activeCells;
    }

    public void setSheetVersion(int loadVersion) {
        this.version = loadVersion;
    }

    @Override
    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public void setCols(int cols) {
        this.cols = cols;
    }

    @Override
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    @Override
    public void setColWidth(int colWidth) {
        this.colWidth = colWidth;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
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
        return activeCells.get(coordinate);
    }

    public void addCell(Coordinate coordinate, Cell cell) {
        activeCells.put(coordinate, cell);
    }

    public void addCellThatChanged(Coordinate coordinate) {
        cellsThatHaveChanged.add(coordinate);
    }

    public Sheet updateCellValueAndCalculate(int row, int column, String value) {
        Coordinate coordinate = createCoordinate(row, column);

        SheetImpl newSheetVersion = copySheet();
        Cell newCell = new CellImpl(row, column, value, newSheetVersion.getVersion() + 1, newSheetVersion);
        newSheetVersion.activeCells.put(coordinate, newCell);

        try {
            List<Cell> cellsThatHaveChanged =
                    newSheetVersion
                            .orderCellsForCalculation()
                            .stream()
                            .filter(Cell::calculateEffectiveValue)
                            .collect(Collectors.toList());

            // חישוב מוצלח. עדכון הגרסה של הגיליון ושל התאים הרלוונטיים
            int newVersion = newSheetVersion.increaseVersion();
            cellsThatHaveChanged.forEach(cell -> cell.updateVersion(newVersion));

            return newSheetVersion;
        } catch (Exception e) {
            // התמודדות עם שגיאות בזמן ריצה שזוהו במהלך הקריאה
            return this;
        }
    }

    // פונקציה שממיינת תאים לחישוב
    private List<Cell> orderCellsForCalculation() {

        List<Cell> orderedCells = new ArrayList<>();
        Map<Cell, Boolean> visited = new HashMap<>();

        for (Cell cell : activeCells.values()) {
            if (!visited.containsKey(cell)) {
                topologicalSort(cell, visited, orderedCells);
            }
        }

        Collections.reverse(orderedCells); // התוצאה הסופית של המיון הטופולוגי
        return orderedCells;
    }

    private void topologicalSort(Cell cell, Map<Cell, Boolean> visited, List<Cell> orderedCells) {
        visited.put(cell, true);

        for (Cell neighbor : cell.getInfluencingOn()) {
            if (!visited.containsKey(neighbor)) {
                topologicalSort(neighbor, visited, orderedCells);
            } else if (visited.get(neighbor)) {
                throw new RuntimeException("Circular dependency detected");
            }
        }

        visited.put(cell, false);
        orderedCells.add(cell);
    }

    private SheetImpl copySheet() {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (SheetImpl) ois.readObject();
        } catch (Exception e) {
            // התמודדות עם שגיאות בזמן ריצה שזוהו במהלך הקריאה
            e.printStackTrace();
            return this;
        }
    }

    // פונקציה שמעלה את גרסת הגיליון
    private int increaseVersion() {
        return ++this.version;
    }

    @Override
    public void updateDependenciesAndInfluences() {
        for (Cell cell : activeCells.values()) {
            cell.resetDependencies();
            cell.resetInfluences();
        }
        for (Cell cell : activeCells.values()) {
            // Parse the cell's value to find references
            String originalValue = cell.getOriginalValue();
            List<Coordinate> referencedCellIds = extractReferences(originalValue);

            for (Coordinate referencedCellId : referencedCellIds) {
                Cell referencedCell = activeCells.get(referencedCellId);

                if (referencedCell != null) {
                    // Add current cell as a dependency for the referenced cell
                    referencedCell.getInfluencingOn().add(cell);

                    // Add current cell to the influences of the referenced cell
                    cell.getDependsOn().add(referencedCell);
                }

            }
        }

        // Check for cycles immediately after updating dependencies and influences
        try {
            orderCellsForCalculation();  // If this method fails, it means a cycle exists.
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    private List<Coordinate> extractReferences(String value) {
        List<Coordinate> references = new ArrayList<>();
        int i = 0;
        String upperValue = value.toUpperCase();

        while (i < upperValue.length()) {
            // Find the start of a REF command
            int start = upperValue.indexOf("{REF,", i);

            // If no more REF commands are found, break the loop
            if (start == -1) {
                break;
            }

            // Move the index to where the cell ID should start
            int cellIdStart = start + 5; // Move past "{REF,"

            // Skip any whitespace after "{REF,"
            while (cellIdStart < upperValue.length() && upperValue.charAt(cellIdStart) == ' ') {
                cellIdStart++;
            }

            // Find the end of the cell ID (it's before the next comma or closing brace)
            int cellIdEnd = cellIdStart;
            while (cellIdEnd < upperValue.length() && upperValue.charAt(cellIdEnd) != ',' && upperValue.charAt(cellIdEnd) != '}') {
                cellIdEnd++;
            }

            // Extract and add the cell ID if it's valid
            if (cellIdEnd > cellIdStart) {
                String cellId = upperValue.substring(cellIdStart, cellIdEnd).trim();
                references.add(createCoordinate(cellId)); // Assuming CellIdentifierImpl has this constructor
            }

            // Move the index to continue searching
            i = cellIdEnd;
        }

        return references;
    }

}


