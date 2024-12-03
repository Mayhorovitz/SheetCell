package sheet.impl;

import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateUtils;
import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import engine.DTOFactory.DTOFactory;
import sheet.api.Sheet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SheetManager {
    public static final int LOAD_VERSION = 1;

    private Map<String, Map<Integer, Sheet>> allSheets = new HashMap<>();
    private Map<String, Integer> currentSheetVersions = new HashMap<>();
    private DTOFactory dtoFactory;

    public SheetManager(DTOFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    public void addSheet(String sheetName, Sheet sheet) {
        Map<Integer, Sheet> versionsMap = new HashMap<>();
        sheet.setSheetVersion(LOAD_VERSION);
        versionsMap.put(LOAD_VERSION, sheet);
        allSheets.put(sheetName, versionsMap);
        currentSheetVersions.put(sheetName, LOAD_VERSION);

    }

    public boolean sheetExists(String sheetName) {
        return allSheets.containsKey(sheetName);
    }

    public Sheet getCurrentSheet(String sheetName) {
        if (!allSheets.containsKey(sheetName)) {
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist.");
        }
        int currentVersion = currentSheetVersions.get(sheetName);
        return allSheets.get(sheetName).get(currentVersion);
    }

    public void updateCell(String sheetName, String coordinateStr, String newValue, String userName) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate coordinate = CoordinateUtils.parseCellId(coordinateStr);
        CoordinateUtils.validateCoordinate(currentSheet, coordinate);

        Sheet newSheet = currentSheet.updateCellValueAndCalculate(coordinateStr, newValue, userName);
        int newVersion = currentSheetVersions.get(sheetName) + 1;
        newSheet.setSheetVersion(newVersion);
        allSheets.get(sheetName).put(newVersion, newSheet);
        currentSheetVersions.put(sheetName, newVersion);
    }

    public CellDTOImpl getCellInfo(String sheetName, String cellIdentifier) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate cellCoordinate = CoordinateUtils.parseCellId(cellIdentifier);
        CoordinateUtils.validateCoordinate(currentSheet, cellCoordinate);
        Cell cell = currentSheet.getCell(cellCoordinate);
        if (cell != null) {
            return dtoFactory.createCellDTO(cell);
        } else {
            return dtoFactory.createEmptyCellDTO(cellIdentifier);
        }
    }
    public Set<String> getAllSheetNames() {
        return allSheets.keySet();
    }
    public SheetDTO getCurrentSheetDTO(String sheetName) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        return dtoFactory.createSheetDTO(currentSheet);
    }

    public int getCurrentSheetVersion(String sheetName) {
        return currentSheetVersions.get(sheetName);
    }

    public SheetDTO getSheetDTOByVersion(String sheetName, int versionNumber) {
        if (!allSheets.get(sheetName).containsKey(versionNumber)) {
            throw new IllegalArgumentException("Invalid version number: " + versionNumber);
        }
        return dtoFactory.createSheetDTO(allSheets.get(sheetName).get(versionNumber));
    }

    // Additional methods for dynamic analysis and other sheet-related operations
}
