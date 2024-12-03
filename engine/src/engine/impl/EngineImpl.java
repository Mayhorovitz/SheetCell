package engine.impl;

import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import dto.api.PermissionRequestDTO;
import dto.api.RangeDTO;
import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import dto.impl.PermissionRequestDTOImpl;
import dto.impl.SheetSummaryDTO;
import dto.permission.PermissionStatus;
import dto.permission.PermissionType;
import engine.DTOFactory.DTOFactory;
import engine.DTOFactory.DTOFactoryImpl;
import engine.api.Engine;
import engine.file.FileLoader;
import permission.PermissionRequest;
import permission.PermissionsManager;
import range.api.Range;
import sheet.api.Sheet;
import sheet.impl.SheetManager;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 1;

    private Map<String, Map<Integer, Sheet>> allSheets = new HashMap<>();
    private Map<String, Integer> currentSheetVersions = new HashMap<>();
    private final DTOFactory dtoFactory = new DTOFactoryImpl();
    private Map<String, PermissionsManager> permissionsManagers = new HashMap<>();
    private final SheetManager sheetManager =  new SheetManager(dtoFactory);


    // Handles loading an XML file into the engine.
    @Override
    public void loadFile(InputStream inputStream, String owner) throws Exception {
        FileLoader fileLoader = new FileLoader();
        Sheet sheet = fileLoader.loadSheetFromXML(inputStream, owner);
        String sheetName = sheet.getName();

        if (sheetManager.sheetExists(sheetName)) {
            throw new IllegalArgumentException("A sheet with the name '" + sheetName + "' already exists.");
        }
        sheet.setOwner(owner);
        sheetManager.addSheet(sheetName, sheet);


        PermissionsManager permissionsManager = new PermissionsManager(owner);
        permissionsManager.addPermission(owner, PermissionType.OWNER);
        permissionsManagers.put(sheetName, permissionsManager);
    }

    @Override
    public int getCurrentSheetVersion(String sheetName) {
        return currentSheetVersions.get(sheetName);
    }
    @Override
    public PermissionsManager getPermissionsManager(String sheetName) {
        if (!permissionsManagers.containsKey(sheetName)) {
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist.");
        }
        return permissionsManagers.get(sheetName);
    }


    private int extractRowFromCoordinate(String coordinateString) {
        String rowPart = coordinateString.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    //  Extracts column index from a cell coordinate string.

    private int extractColumnFromCoordinate(String coordinateString) {
        String columnPart = coordinateString.replaceAll("[^A-Za-z]", "");
        return convertColumnToIndex(columnPart);
    }

    // Converts a column letter to its corresponding index.

    public int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

    //  Updates a cell's value.
    @Override
    public void updateCell(String sheetName, String coordinate, String newValue,String userName) {
        if (coordinate == null || newValue == null) {
            throw new IllegalArgumentException("Cell location and value cannot be null.");
        }

        if (!allSheets.containsKey(sheetName)) {
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist.");
        }

        Map<Integer, Sheet> sheetVersions = allSheets.get(sheetName);
        int currentVersion = currentSheetVersions.get(sheetName);
        Sheet currentSheet = sheetVersions.get(currentVersion);
        Sheet newSheet = currentSheet.updateCellValueAndCalculate(coordinate, newValue, userName);

        int newVersion = currentVersion + 1;
        newSheet.setSheetVersion(newVersion);
        sheetVersions.put(newVersion, newSheet);
        currentSheetVersions.put(sheetName, newVersion);
    }

    //  Retrieves the current sheet.
    private Sheet getCurrentSheet(String sheetName) {
        if (!allSheets.containsKey(sheetName)) {
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist.");
        }

        int currentVersion = currentSheetVersions.get(sheetName);
        Map<Integer, Sheet> sheetVersions = allSheets.get(sheetName);
        return sheetVersions.get(currentVersion);
    }
    //  Retrieves cell information as a DTO.
    @Override
    public CellDTOImpl getCellInfo(String sheetName, String cellIdentifier) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate cellCoordinate = new CoordinateImpl(extractRowFromCoordinate(cellIdentifier), extractColumnFromCoordinate(cellIdentifier));
        validateCoordinate(currentSheet, cellCoordinate);
        Cell cell = currentSheet.getCell(cellCoordinate);
        if (cell != null) {
            return dtoFactory.createCellDTO(cell);
        } else {
            return dtoFactory.createEmptyCellDTO(cellIdentifier);
        }
    }
    // Validates that a coordinate is within the sheet bounds.

    private void validateCoordinate(Sheet currentSheet, Coordinate coordinate) {
        if (coordinate.getRow() < 1 || coordinate.getRow() > currentSheet.getRows() ||
                coordinate.getColumn() < 1 || coordinate.getColumn() > currentSheet.getCols()) {
            throw new IllegalArgumentException("Cell location " + coordinate + " is out of bounds.");
        }
    }

    // Adds a range to the sheet.
    @Override
    public void addRangeToSheet(String sheetName, String name, String range) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        currentSheet.addRange(name, range);
    }

    //Deletes a range from the sheet.
    @Override
    public void deleteRangeFromSheet(String sheetName, String name) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        currentSheet.deleteRange(name);
    }

    //  Retrieves a range from the sheet as a DTO
    @Override
    public RangeDTO getRangeFromSheet(String sheetName, String name) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Range range = currentSheet.getRange(name);
        if (range != null) {
            return dtoFactory.createRangeDTO(range);
        } else {
            throw new IllegalArgumentException("Range not found: " + name);
        }
    }

    // Retrieves all ranges from the sheet as DTOs
    @Override
    public Collection<RangeDTO> getAllRangesFromSheet(String sheetName) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Collection<Range> ranges = currentSheet.getAllRanges();
        return ranges.stream()
                .map(dtoFactory::createRangeDTO)
                .collect(Collectors.toList());
    }

    //  Sorts a range by specified columns and returns the sorted sheet as a DTO
    @Override
    public SheetDTO sortSheetRangeByColumns(String sheetName, String range, String[] columns) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Sheet sortedSheet = currentSheet.sortSheet(range, columns);
        return dtoFactory.createSheetDTO(sortedSheet);
    }

    // Gets unique values in a range column.
    @Override
    public List<String> getUniqueValuesInRangeColumn(String sheetName, String range, String column) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        return currentSheet.getUniqueValuesInRangeColumn(range, column);
    }

    //  Filters a sheet by values and returns the filtered sheet as a DTO.
    @Override
    public SheetDTO filterSheetByValues(String sheetName, String range, String column, List<String> selectedValues) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Sheet filteredSheet = currentSheet.filterSheetByValues(range, column, selectedValues);
        return dtoFactory.createSheetDTO(filteredSheet);
    }


    @Override
    public SheetDTO getSheetDTOByVersion(String sheetName, int versionNumber) {
            if (!allSheets.get(sheetName).containsKey(versionNumber)) {
                throw new IllegalArgumentException("Invalid version number: " + versionNumber);
            }
            return dtoFactory.createSheetDTO(allSheets.get(sheetName).get(versionNumber));
    }

    // Retrieves the current sheet as a DTO.
    @Override
    public SheetDTO getCurrentSheetDTO(String sheetName) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        return dtoFactory.createSheetDTO(currentSheet);
    }


    private Coordinate parseCellId(String cellId) {
        int row = extractRowFromCoordinate(cellId);
        int column = extractColumnFromCoordinate(cellId);
        return new CoordinateImpl(row, column);
    }


    @Override
    public void updateCellBackgroundColor(String sheetName, String cellId, String colorHex) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate coordinate = parseCellId(cellId);
        validateCoordinate(currentSheet, coordinate);

        Cell cell = currentSheet.getCell(coordinate);
        if (cell == null) {
            throw new IllegalArgumentException("Cell " + cellId + " does not exist.");
        }

        // Update the cell's background color
        cell.setBackgroundColor(colorHex);

    }


    @Override
    public void updateCellTextColor(String sheetName, String cellId, String colorHex) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate coordinate = parseCellId(cellId);
        validateCoordinate(currentSheet, coordinate);

        Cell cell = currentSheet.getCell(coordinate);
        if (cell == null) {
            throw new IllegalArgumentException("Cell " + cellId + " does not exist.");
        }

        // Update the cell's text color
        cell.setTextColor(colorHex);

    }

    @Override
    public void resetCellDesign(String sheetName, String cellId) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        Coordinate coordinate = parseCellId(cellId);
        validateCoordinate(currentSheet, coordinate);

        Cell cell = currentSheet.getCell(coordinate);
        if (cell == null) {
            throw new IllegalArgumentException("Cell " + cellId + " does not exist.");
        }

        // Reset the cell's text and background colors to default
        cell.setTextColor("#000000");       // Black text
        cell.setBackgroundColor("#FFFFFF"); // White background

    }


    @Override
    public Collection<SheetSummaryDTO> getAllSheetsSummary(String currentUserName) {
        List<SheetSummaryDTO> sheetSummaryDTOs = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Sheet>> entry : allSheets.entrySet()) {
            String sheetName = entry.getKey();
            int currentVersion = currentSheetVersions.get(sheetName);
            Sheet currentSheet = entry.getValue().get(currentVersion);

            PermissionType permissionType = getPermissionsManager(sheetName).getUserPermission(currentUserName);

            String size = currentSheet.getRows() + "x" + currentSheet.getCols();
            SheetSummaryDTO summaryDTO = new SheetSummaryDTO(
                    currentSheet.getName(),
                    currentSheet.getOwner(),
                    size,
                    permissionType.toString()
            );
            sheetSummaryDTOs.add(summaryDTO);
        }
        return sheetSummaryDTOs;
    }

    @Override
    public void submitPermissionRequest(String sheetName, String requesterUsername, PermissionType requestedPermission) {
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);
        permissionsManager.submitPermissionRequest(requesterUsername, requestedPermission);
    }
    @Override
    public List<PermissionRequest> getPermissionRequests(String sheetName) {
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);
        return permissionsManager.getPermissionRequests();
    }
    @Override
    public void handlePermissionRequest(String sheetName, int requestIndex, String approverUsername, PermissionStatus status) {
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);
        permissionsManager.handlePermissionRequest(requestIndex, approverUsername, status);
    }
    @Override
    public void handleResponseRequest(String sheetName, String requesterUsername, String approverUsername, PermissionStatus status) {
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);

        List<PermissionRequest> requests = permissionsManager.getPermissionRequests();
        Optional<PermissionRequest> requestOptional = requests.stream()
                .filter(request -> request.getUsername().equals(requesterUsername) && request.getStatus() == PermissionStatus.PENDING)
                .findFirst();

        if (requestOptional.isEmpty()) {
            throw new IllegalArgumentException("No pending request found for user '" + requesterUsername + "' in sheet '" + sheetName + "'.");
        }

        PermissionRequest request = requestOptional.get();
        if (!approverUsername.equals(permissionsManager.getOwnerUsername())) {
            throw new IllegalArgumentException("Only the owner can handle permission requests.");
        }

        request.setStatus(status);
        if (status == PermissionStatus.APPROVED) {
            permissionsManager.grantPermission(requesterUsername, request.getRequestedPermission());
        }

    }

    @Override
    public List<PermissionRequestDTO> getPermissionRequestsDTO(String sheetName) {
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);
        return permissionsManager.getPermissionRequests().stream()
                .map(request -> new PermissionRequestDTOImpl(
                        request.getUsername(),
                        sheetName,
                        request.getRequestedPermission(),
                        request.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionRequestDTO> getPendingRequestsForOwner(String ownerUsername) {
        List<PermissionRequestDTO> pendingRequests = new ArrayList<>();

        for (Map.Entry<String, PermissionsManager> entry : permissionsManagers.entrySet()) {
            String sheetName = entry.getKey();
            PermissionsManager permissionsManager = entry.getValue();

            if (permissionsManager.getOwnerUsername().equals(ownerUsername)) {
                List<PermissionRequest> requests = permissionsManager.getPermissionRequests();
                for (PermissionRequest request : requests) {
                    if (request.getStatus() == PermissionStatus.PENDING) {
                        PermissionRequestDTO requestDTO = new PermissionRequestDTOImpl(
                                request.getUsername(),
                                sheetName,
                                request.getRequestedPermission(),
                                request.getStatus()
                        );
                        pendingRequests.add(requestDTO);
                    }
                }
            }
        }

        return pendingRequests;
    }

    @Override
    public int getLatestVersion(String sheetName) {
        return currentSheetVersions.get(sheetName);
    }

    @Override
    public SheetDTO performDynamicAnalysis(String sheetName, Map<String, Double> cellValues) {
            Sheet currentSheet = getCurrentSheet(sheetName);
            // Create a deep copy of the current sheet to avoid modifying the original
            Sheet tempSheet = currentSheet.copySheet();

        try {
            // Update each cell with the new value
            for (Map.Entry<String, Double> entry : cellValues.entrySet()) {
                String cellId = entry.getKey();
                String newValue = String.valueOf(entry.getValue());
                Coordinate coordinate = parseCellId(cellId);
                validateCoordinate(tempSheet, coordinate);

                Cell cell = tempSheet.getCell(coordinate);
                if (cell == null) {
                    throw new IllegalArgumentException("Cell " + cellId + " does not exist in the sheet.");
                } else {
                    if (!isNumeric(cell.getOriginalValue())) {
                        throw new IllegalArgumentException("Cell " + cellId + " does not contain a numeric value.");
                    }
                }
                    // Update the cell value
                    cell.setOriginalValue(newValue);

            }

            // Recalculate the sheet
            tempSheet.updateDependenciesAndInfluences();
            for (Cell c : tempSheet.orderCellsForCalculation()) {
                c.calculateEffectiveValue();
            }

            return dtoFactory.createSheetDTO(tempSheet);
        } catch (Exception e) {
            throw new IllegalStateException("Error during dynamic analysis: " + e.getMessage(), e);
        }
    }

    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public SheetDTO performSingleDynamicAnalysis(String sheetName, String cellId, String newValue) {
        Sheet currentSheet = getCurrentSheet(sheetName);
        // Create a deep copy of the current sheet
        Sheet tempSheet = currentSheet.copySheet();

        try {
            Coordinate coordinate = parseCellId(cellId);
            validateCoordinate(tempSheet, coordinate);

            Cell cell = tempSheet.getCell(coordinate);
            if (cell == null) {
                throw new IllegalArgumentException("Cell " + cellId + " does not exist in the sheet.");
            } else {
                if (!isNumeric(cell.getOriginalValue())) {
                    throw new IllegalArgumentException("Cell " + cellId + " does not contain a numeric value.");
                }
                // Update the cell value
                cell.setOriginalValue(newValue);
            }

            // Recalculate the sheet
            tempSheet.updateDependenciesAndInfluences();
            for (Cell c : tempSheet.orderCellsForCalculation()) {
                c.calculateEffectiveValue();
            }

            return dtoFactory.createSheetDTO(tempSheet);
        } catch (Exception e) {
            throw new IllegalStateException("Error during dynamic analysis: " + e.getMessage(), e);
        }
    }



}
