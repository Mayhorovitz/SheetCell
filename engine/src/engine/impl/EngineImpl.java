package engine.impl;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import engine.api.Engine;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;
import generated.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static coordinate.CoordinateFactory.createCoordinate;

public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 1;

    private Map<Integer, Sheet> allSheets;
    int currentSheetVersion;

    //hendle loading file request
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
        if (!xmlFile.exists() || !xmlFile.isFile()) {//file noe exist
            throw new IOException("File does not exist or is not a valid file.");
        }
        if (!filePath.endsWith(".xml")) {//not XML file
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
        // validate the number of rows and columns in the sheet layout
        STLLayout layout = sheet.getSTLLayout();
        if (layout.getRows() < 1 || layout.getRows() > MAX_ROWS ||
                layout.getColumns() < 1 || layout.getColumns() > MAX_COLS) {
            throw new IllegalArgumentException("Invalid sheet layout: Rows and columns must be within allowed range.");
        }
        // validate the position of each cell in the sheet
        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            String column = cell.getColumn();
            int columnIndex = convertColumnToIndex(column);

            if (row < 1 || row > layout.getRows() || columnIndex < 1 || columnIndex > layout.getColumns()) {
                throw new IllegalArgumentException("Invalid cell location: Cell at row " + row + ", column " + column + " is out of bounds.");
            }
        }
    }

    private int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }
    //converts an STLSheet object into a Sheet object.
    private Sheet STLSheetToSheet(STLSheet stlSheet) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());
        // Set the dimensions and size of the new sheet
        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());
        // create cell for each cell in the STLSheet and add it to the new Sheet
        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            String originalValue = stlCell.getSTLOriginalValue();
            int row = stlCell.getRow();
            String column = stlCell.getColumn();
            int col = convertColumnToIndex(column);

            Coordinate coordinate = createCoordinate(row, col);
            Cell cell = new CellImpl(row, col, originalValue, 0, newSheet);
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
    //handle display cell request
    @Override
    public Cell getCellInfo(String cellIdentifier) {
        if (allSheets == null || allSheets.isEmpty()) {
            throw new IllegalStateException("No file has been loaded. Please load a file first.");
        }
        Coordinate cellCoordinate = createCoordinate(cellIdentifier);
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
    //saves the current state of the system including all sheets and their versions to a .dat file.
    @Override
    public void saveSystemState(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath + ".dat"))) {
            // Serialize the map of all sheets and the current sheet version
            oos.writeObject(this.allSheets);
            oos.writeInt(this.currentSheetVersion);
            System.out.println("System state has been saved.");
        }
    }
    //loads the system state including all sheets and their versions from a file
    @Override
    public void loadSystemState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath + ".dat"))) {
            // Deserialize the map of all sheets and the current sheet version
            this.allSheets = (Map<Integer, Sheet>) ois.readObject();
            this.currentSheetVersion = ois.readInt();
            System.out.println("System state has been loaded.");
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}
