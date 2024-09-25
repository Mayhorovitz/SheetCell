package sheet.impl;

import cell.impl.CellImpl;
import sheet.api.Sheet;
import cell.api.Cell;
import coordinate.Coordinate;

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
    //constructors
    public SheetImpl() {
        this.activeCells = new HashMap<>();
        this.cellsThatHaveChanged = new ArrayList<>();
    }
//setters
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


    //getters
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

    public void addCell(Coordinate coordinate, Cell cell) {
        if (coordinate == null || cell == null) {
            throw new IllegalArgumentException("Coordinate and Cell cannot be null.");
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
        SheetImpl updatedSheet = copySheet();
        updatedSheet.cellsThatHaveChanged.clear();

        Cell updatedCell = new CellImpl(coordinate, value, updatedSheet.getVersion() + 1, updatedSheet);
        updatedSheet.activeCells.put(coordinate, updatedCell);

        try {
            // Calculate the effective values for the cells in the correct order
            List<Cell> orderedCells = updatedSheet.calculateInPlace();
            List<Cell> changedCells = new ArrayList<>();

            for (Cell cell : orderedCells) {
                if (cell.calculateEffectiveValue()) {
                    changedCells.add(cell);
                }
            }

            updatedSheet.cellsThatHaveChanged = changedCells;
            int newVersion = updatedSheet.increaseVersion();
            changedCells.forEach(cell -> cell.updateVersion(newVersion));

            return updatedSheet;
        } catch (Exception e) {
            throw new IllegalStateException("Error updating cell value: " + e.getMessage());
        }
    }
    //calculates the effective values  and orders the cells based on their dependencies
    public List<Cell> calculateInPlace() {
        List<Cell> orderedCells = new ArrayList<>();
        Set<Coordinate> processedCoordinates = new HashSet<>();
        // Evaluate dependencies for each cell and order them
        for (Cell cell : activeCells.values()) {
            evaluateDependencies(cell, orderedCells, processedCoordinates);
        }

        return orderedCells;
    }
    //evaluates the dependencies of a cell
    private void evaluateDependencies(Cell cell, List<Cell> orderedCells, Set<Coordinate> processedCoordinates) {
        if (processedCoordinates.contains(cell.getCoordinate())) {
            return;
        }

        String value = cell.getOriginalValue();
        List<Coordinate> referencedCoords = findReferences(value);

        for (Coordinate coord : referencedCoords) {
            Cell referencedCell = activeCells.get(coord);
            if (referencedCell != null && !processedCoordinates.contains(coord)) {
                evaluateDependencies(referencedCell, orderedCells, processedCoordinates);
            }
        }

        orderedCells.add(cell);
        processedCoordinates.add(cell.getCoordinate());
    }


    //copy sheet to create new version
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

    // Extract references from the cell's value
    private List<Coordinate> findReferences(String value) {
        List<Coordinate> references = new ArrayList<>();
        String valueUpperCase = value.toUpperCase();

        Pattern refPattern = Pattern.compile("\\{REF,\\s*([^,\\s}]+)\\s*}");
        Matcher matcher = refPattern.matcher(valueUpperCase);

        while (matcher.find()) {
            String cellId = matcher.group(1).trim();
            references.add(createCoordinate(cellId));
        }

        return references;
    }
}