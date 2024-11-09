package engine.impl;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import dto.api.DTOFactory;
import dto.api.PermissionRequestDTO;
import dto.api.RangeDTO;
import dto.api.SheetDTO;
import dto.impl.CellDTOImpl;
import dto.impl.DTOFactoryImpl;
import dto.impl.PermissionRequestDTOImpl;
import dto.impl.SheetSummaryDTO;
import engine.api.Engine;
import generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import permission.PermissionRequest;
import permission.PermissionStatus;
import permission.PermissionType;
import permission.PermissionsManager;
import range.api.Range;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 1;

    private Map<String, Map<Integer, Sheet>> allSheets = new HashMap<>();
    private Map<String, Integer> currentSheetVersions = new HashMap<>();
    private DTOFactory dtoFactory = new DTOFactoryImpl();
    private Map<String, PermissionsManager> permissionsManagers = new HashMap<>();


    // Handles loading an XML file into the engine.
    @Override
    public void loadFile(InputStream inputStream, String owner) throws Exception {
        STLSheet loadedSheetFromXML = loadXMLFile(inputStream);
        String sheetName = loadedSheetFromXML.getName();

        if (allSheets.containsKey(sheetName)) {
            throw new IllegalArgumentException("A sheet with the name '" + sheetName + "' already exists.");
        }

        Map<Integer, Sheet> versionsMap = new HashMap<>();
        Sheet currentSheet = STLSheetToSheet(loadedSheetFromXML, owner);
        currentSheet.setSheetVersion(LOAD_VERSION);
        currentSheet.setOwner(owner);
        versionsMap.put(LOAD_VERSION, currentSheet);
        allSheets.put(sheetName, versionsMap);
        currentSheetVersions.put(sheetName, LOAD_VERSION);

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

    private STLSheet loadXMLFile(InputStream inputStream) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            STLSheet sheet = (STLSheet) unmarshaller.unmarshal(inputStream);
            validateSheet(sheet);
            return sheet;
        } catch (JAXBException e) {
            throw new IOException("An error occurred while loading the XML file.", e);
        }
    }


    //  Validates the loaded STLSheet object.

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

    //  Extracts row number from a cell coordinate string.

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

    //  Converts an STLSheet object into a Sheet object.
    private Sheet STLSheetToSheet(STLSheet stlSheet, String owner) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());

        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());

        // Process ranges
        if (stlSheet.getSTLRanges() != null && stlSheet.getSTLRanges().getSTLRange() != null) {
            for (STLRange range : stlSheet.getSTLRanges().getSTLRange()) {
                String name = range.getName();
                String from = range.getSTLBoundaries().getFrom();
                String to = range.getSTLBoundaries().getTo();
                newSheet.addRange(name, from + ".." + to);
            }
        }

        // Process cells
        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            String originalValue = stlCell.getSTLOriginalValue();
            int row = stlCell.getRow();
            String column = stlCell.getColumn();
            int col = convertColumnToIndex(column);

            Coordinate coordinate = new CoordinateImpl(row, col);
            Cell cell = new CellImpl(row, col, originalValue, 1, owner,newSheet);
            newSheet.addCell(coordinate, cell);
        }

        newSheet.updateDependenciesAndInfluences();

        // Calculate the effective values for each cell
        for (Cell cell : newSheet.orderCellsForCalculation()) {
            cell.calculateEffectiveValue();
            newSheet.addCellThatChanged(cell);
        }

        return newSheet;
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
        public SheetDTO performDynamicAnalysis(String sheetName, String cellId, String newValue) {
            Sheet currentSheet = getCurrentSheet(sheetName);
            // Create a deep copy of the current sheet to avoid modifying the original
            Sheet tempSheet = currentSheet.copySheet();

            Coordinate coordinate = parseCellId(cellId);
            validateCoordinate(tempSheet, coordinate);

            Cell cell = tempSheet.getCell(coordinate);
            if (cell == null) {
                // If the cell does not exist, create it
                cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), newValue, 1, tempSheet.getOwner(), tempSheet);
                tempSheet.addCell(coordinate, cell);
            } else {
                // Update the cell value
                cell.setOriginalValue(newValue);
            }

            // Recalculate the sheet
            tempSheet.updateDependenciesAndInfluences();
            for (Cell c : tempSheet.orderCellsForCalculation()) {
                c.calculateEffectiveValue();
            }

            return dtoFactory.createSheetDTO(tempSheet);


    }

}
