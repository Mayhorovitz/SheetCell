package engine.impl;

import engine.api.Engine;
import engine.exceptions.*;

import sheet.api.Sheet;
import sheet.impl.SheetImpl;
import generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import coordinate.Coordinate;
import cell.impl.CellImpl;
import cell.api.Cell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static coordinate.CoordinateFactory.createCoordinate;

public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 1;

    private Map<Integer, Sheet> allSheets;
    int currentSheetVersion;

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

    public STLSheet loadXMLFile(String filePath) throws InvalidFileFormatException, InvalidSheetLayoutException, InvalidCellLocationException {
        File xmlFile = new File(filePath);
        if (!xmlFile.exists() || !xmlFile.isFile()) {
            throw new InvalidFileFormatException("File does not exist or is not a valid file.");
        }
        if (!filePath.endsWith(".xml")) {
            throw new InvalidFileFormatException("The file is not an XML file.");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            STLSheet sheet = (STLSheet) unmarshaller.unmarshal(xmlFile);

            if (!validateSheet(sheet)) {
                throw new InvalidSheetLayoutException("The XML file is invalid according to the application's rules.");
            }

            return sheet;

        } catch (JAXBException e) {
            throw new InvalidFileFormatException("An error occurred while loading the XML file.");
        }
    }

    private boolean validateSheet(STLSheet sheet) throws InvalidSheetLayoutException, InvalidCellLocationException {
        STLLayout layout = sheet.getSTLLayout();
        if (layout.getRows() < 1 || layout.getRows() > MAX_ROWS ||
                layout.getColumns() < 1 || layout.getColumns() > MAX_COLS) {
            throw new InvalidSheetLayoutException("Invalid sheet layout: Rows and columns must be within allowed range.");
        }

        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            String column = cell.getColumn();
            int columnIndex = convertColumnToIndex(column);

            if (row < 1 || row > layout.getRows() || columnIndex < 1 || columnIndex > layout.getColumns()) {
                throw new InvalidCellLocationException("Invalid cell location: Cell at row " + row + ", column " + column + " is out of bounds.");
            }
        }

        return true;
    }

    private int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

    private Sheet STLSheetToSheet(STLSheet stlSheet) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());

        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());

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

        for (Cell cell : newSheet.orderCellsForCalculation()) {
            cell.calculateEffectiveValue();
            newSheet.addCellThatChanged(cell);
        }

        return newSheet;
    }

    public void updateCell(String cellId, String newValue) {
        Sheet sheet = getCurrentSpreadSheet();
        Sheet newSheet = sheet.updateCellValueAndCalculate(cellId, newValue);
        currentSheetVersion = newSheet.getVersion();
        allSheets.put(currentSheetVersion, newSheet);
    }

    @Override
    public Sheet getCurrentSpreadSheet() {
        if (allSheets == null || allSheets.isEmpty()) {
            throw new NoFileLoadedException("No file has been loaded. Please load a file first.");
        }
        return allSheets.get(currentSheetVersion);
    }
    @Override
    public Cell getCellInfo(String cellIdentifier) {
        if (allSheets == null || allSheets.isEmpty()) {
            throw new NoFileLoadedException("No file has been loaded. Please load a file first.");
        }
        Coordinate cellCoordinate = createCoordinate(cellIdentifier);
        return allSheets.get(currentSheetVersion).getCell(cellCoordinate);
    }

    public Sheet getSheetByVersion(int version) throws InvalidVersionException {
        if (!allSheets.containsKey(version)) {
            throw new InvalidVersionException("Invalid version number: " + version);
        }
        return allSheets.get(version);
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}
