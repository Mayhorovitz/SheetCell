package engine.impl;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import engine.api.Engine;
import range.api.Range;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;
import generated.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 1;

    private Map<Integer, Sheet> allSheets;
    int currentSheetVersion;

    //handle loading file request
    @Override
    public void loadFile(String filePath) throws Exception {
        STLSheet loadedSheetFromXML = loadXMLFile(filePath);
        allSheets = new HashMap<>();
        Sheet currentSheet = STLSheetToSheet(loadedSheetFromXML);
        currentSheet.setSheetVersion(LOAD_VERSION);
        currentSheetVersion = LOAD_VERSION;

        allSheets.put(LOAD_VERSION, currentSheet);
    }

    public int getCurrentSheetVersion() {
        return currentSheetVersion;
    }

    //load xml file to the engine
    public STLSheet loadXMLFile(String filePath) throws IOException {
        File xmlFile = new File(filePath);
        if (!xmlFile.exists() || !xmlFile.isFile()) {
            throw new IOException("File does not exist or is not a valid file.");
        }
        if (!filePath.endsWith(".xml")) {
            throw new IOException("The file is not an XML file.");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            STLSheet sheet = (STLSheet) unmarshaller.unmarshal(xmlFile);
            validateSheet(sheet);
            return sheet;
        } catch (JAXBException e) {
            throw new IOException("An error occurred while loading the XML file.", e);
        }
    }

    //check the validation of the file
    private void validateSheet(STLSheet sheet) {
        STLLayout layout = sheet.getSTLLayout();
        if (layout.getRows() < 1 || layout.getRows() > MAX_ROWS ||
                layout.getColumns() < 1 || layout.getColumns() > MAX_COLS) {
            throw new IllegalArgumentException("Invalid sheet layout: Rows and columns must be within allowed range.");
        }


        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            String column = cell.getColumn();
            int columnIndex = convertColumnToIndex(column);

            if (row < 1 || row > layout.getRows() || columnIndex < 1 || columnIndex > layout.getColumns()) {
                throw new IllegalArgumentException("Invalid cell location: Cell at row " + row + ", column " + column + " is out of bounds.");
            }
        }
    }


    private int extractRowFromCoordinate(String coordinateString) {
        String rowPart = coordinateString.replaceAll("[^0-9]", ""); // Extract the numeric part for row
        return Integer.parseInt(rowPart); // Convert it to integer
    }

    private int extractColumnFromCoordinate(String coordinateString) {
        String columnPart = coordinateString.replaceAll("[^A-Za-z]", ""); // Extract the letter part for column
        return convertColumnToIndex(columnPart); // Convert it to column index
    }

    public int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

    //converts an STLSheet object into a Sheet object
    private Sheet STLSheetToSheet(STLSheet stlSheet) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());

        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());

        // Process the new ranges
        for (STLRange range : stlSheet.getSTLRanges().getSTLRange()) {
            String name = range.getName();
            String from =  range.getSTLBoundaries().getFrom();
            String to = range.getSTLBoundaries().getTo();
            newSheet.addRange(name, from + ".." + to);

        }

        // Existing logic for processing cells
        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            String originalValue = stlCell.getSTLOriginalValue();
            int row = stlCell.getRow();
            String column = stlCell.getColumn();
            int col = convertColumnToIndex(column);

            Coordinate coordinate = new CoordinateImpl(row, col); // Use CoordinateImpl here
            Cell cell = new CellImpl(row, col, originalValue, 1, newSheet);
            newSheet.addCell(coordinate, cell);
        }

        newSheet.updateDependenciesAndInfluences();
        // Calculate the effective values for each cell in the correct order
        for (Cell cell : newSheet.orderCellsForCalculation()) {
            cell.calculateEffectiveValue();
            newSheet.addCellThatChanged(cell);
        }

        return newSheet;
    }

    //handle update cell request update the cell value
    public void updateCell(String coordinate, String newValue) {
        if (coordinate == null || newValue == null) {
            throw new IllegalArgumentException("Cell location and value cannot be null.");
        }

        Sheet sheet = getCurrentSheet();
        Sheet newSheet = sheet.updateCellValueAndCalculate(coordinate, newValue);
        currentSheetVersion = newSheet.getVersion();
        allSheets.put(currentSheetVersion, newSheet);
    }


    @Override
    public Sheet getCurrentSheet() {
        if (allSheets == null || allSheets.isEmpty()) {
            throw new IllegalStateException("No file has been loaded. Please load a file first.");
        }
        return allSheets.get(currentSheetVersion);
    }

    @Override
    public Cell getCellInfo(String cellIdentifier) {
        if (allSheets == null || allSheets.isEmpty()) {
            throw new IllegalStateException("No file has been loaded. Please load a file first.");
        }
        Coordinate cellCoordinate = new CoordinateImpl(extractRowFromCoordinate(cellIdentifier), extractColumnFromCoordinate(cellIdentifier));
        validateCoordinate(cellCoordinate);
        return allSheets.get(currentSheetVersion).getCell(cellCoordinate);
    }

    private void validateCoordinate(Coordinate coordinate) {
        if (coordinate.getRow() < 1 || coordinate.getRow() > getCurrentSheet().getRows() ||
                coordinate.getColumn() < 1 || coordinate.getColumn() > getCurrentSheet().getCols()) {
            throw new IllegalArgumentException("Cell location " + coordinate + " is out of bounds.");
        }
    }

    public Sheet getSheetByVersion(int version) {
        if (!allSheets.containsKey(version)) {
            throw new IllegalArgumentException("Invalid version number: " + version);
        }
        return allSheets.get(version);
    }

    @Override
    public void saveSystemState(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath + ".dat"))) {
            oos.writeObject(this.allSheets);
            oos.writeInt(this.currentSheetVersion);
            System.out.println("System state has been saved.");
        }
    }

    @Override
    public void loadSystemState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath + ".dat"))) {
            this.allSheets = (Map<Integer, Sheet>) ois.readObject();
            this.currentSheetVersion = ois.readInt();
            System.out.println("System state has been loaded.");
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public void addRangeToSheet(String name, String range) {
        Sheet currentSheet = getCurrentSheet();
        currentSheet.addRange(name, range);
    }
    @Override
    public void deleteRangeFromSheet(String name) {
        Sheet currentSheet = getCurrentSheet();
        currentSheet.deleteRange(name);
    }
    @Override
    public Range getRangeFromSheet(String name) {
        Sheet currentSheet = getCurrentSheet();
        return currentSheet.getRange(name);
    }
    @Override
    public Collection<Range> getAllRangesFromSheet() {
        Sheet currentSheet = getCurrentSheet();
        return currentSheet.getAllRanges();
    }
     // Sorts a sheet range by specified columns.
    @Override
    public Sheet sortSheetRangeByColumns(String range, String[] columns) {
        Sheet currentSheet = getCurrentSheet();

        Sheet sortSheet = currentSheet.sortSheet(range, columns);

        return sortSheet;
    }
//Gets unique values in a range column.
    @Override
    public List<String> getUniqueValuesInRangeColumn(String range, String column) {
        Sheet currentSheet = getCurrentSheet();
        return currentSheet.getUniqueValuesInRangeColumn(range, column);
    }

    @Override
    public Sheet filterSheetByValues(String range, String column, List<String> selectedValues, List<Integer> originalRowNumbers) {
        Sheet currentSheet = getCurrentSheet();
        return currentSheet.filterSheetByValues(range, column, selectedValues, originalRowNumbers);
    }


}
