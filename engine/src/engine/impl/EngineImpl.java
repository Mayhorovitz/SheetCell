package engine.impl;

import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateUtils;
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
import range.impl.RangeManager;
import sheet.api.Sheet;
import sheet.impl.DynamicAnalysisService;
import sheet.impl.SheetManager;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {

    private final DTOFactory dtoFactory;
    private final FileLoader fileLoader;
    private final SheetManager sheetManager;
    private final Map<String, PermissionsManager> permissionsManagers;
    private final DynamicAnalysisService dynamicAnalysisService;
    private final RangeManager rangeManager;

    public EngineImpl() {
        this.dtoFactory = new DTOFactoryImpl();
        this.fileLoader = new FileLoader();
        this.sheetManager = new SheetManager(dtoFactory);
        this.permissionsManagers = new HashMap<>();
        this.dynamicAnalysisService = new DynamicAnalysisService(dtoFactory);
        this.rangeManager = new RangeManager(dtoFactory);
    }

    @Override
    public void loadFile(InputStream inputStream, String owner) throws Exception {
        Sheet sheet = fileLoader.loadSheetFromXML(inputStream, owner);
        String sheetName = sheet.getName();

        if (sheetManager.sheetExists(sheetName)) {
            throw new IllegalArgumentException("A sheet with the name '" + sheetName + "' already exists.");
        }

        sheet.setSheetVersion(1);
        sheet.setOwner(owner);
        sheetManager.addSheet(sheetName, sheet);

        // Initialize PermissionsManager for the new sheet
        PermissionsManager permissionsManager = new PermissionsManager(owner);
        // Owner has OWNER permission by default in PermissionsManager constructor
        permissionsManagers.put(sheetName, permissionsManager);
    }

    @Override
    public int getCurrentSheetVersion(String sheetName) {
        return sheetManager.getCurrentSheetVersion(sheetName);
    }

    @Override
    public PermissionsManager getPermissionsManager(String sheetName) {
        PermissionsManager permissionsManager = permissionsManagers.get(sheetName);
        if (permissionsManager == null) {
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist.");
        }
        return permissionsManager;
    }

    @Override
    public void updateCell(String sheetName, String coordinate, String newValue, String userName) {
        // Permission check
        PermissionsManager permissionsManager = getPermissionsManager(sheetName);
        PermissionType userPermission = permissionsManager.getUserPermission(userName);
        if (userPermission != PermissionType.WRITER && userPermission != PermissionType.OWNER) {
            throw new IllegalArgumentException("User '" + userName + "' does not have permission to update cells in this sheet.");
        }

        sheetManager.updateCell(sheetName, coordinate, newValue, userName);
    }

    @Override
    public CellDTOImpl getCellInfo(String sheetName, String cellIdentifier) {
        return sheetManager.getCellInfo(sheetName, cellIdentifier);
    }

    @Override
    public void addRangeToSheet(String sheetName, String name, String range) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        rangeManager.addRange(sheet, name, range);
    }

    @Override
    public void deleteRangeFromSheet(String sheetName, String name) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        rangeManager.deleteRange(sheet, name);
    }

    @Override
    public RangeDTO getRangeFromSheet(String sheetName, String name) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return rangeManager.getRange(sheet, name);
    }

    @Override
    public Collection<RangeDTO> getAllRangesFromSheet(String sheetName) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return rangeManager.getAllRanges(sheet);
    }

    @Override
    public SheetDTO sortSheetRangeByColumns(String sheetName, String range, String[] columns) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return rangeManager.sortSheetRangeByColumns(sheet, range, columns);
    }

    @Override
    public List<String> getUniqueValuesInRangeColumn(String sheetName, String range, String column) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return rangeManager.getUniqueValuesInRangeColumn(sheet, range, column);
    }

    @Override
    public SheetDTO filterSheetByValues(String sheetName, String range, String column, List<String> selectedValues) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return rangeManager.filterSheetByValues(sheet, range, column, selectedValues);
    }

    @Override
    public SheetDTO getSheetDTOByVersion(String sheetName, int versionNumber) {
        return sheetManager.getSheetDTOByVersion(sheetName, versionNumber);
    }

    @Override
    public SheetDTO getCurrentSheetDTO(String sheetName) {
        return sheetManager.getCurrentSheetDTO(sheetName);
    }

    @Override
    public void updateCellBackgroundColor(String sheetName, String cellId, String colorHex) {
        // Permission check (if required)
        Cell cell =updateCell(sheetName, cellId);
        cell.setBackgroundColor(colorHex);
    }
private Cell updateCell(String sheetName, String cellId) {
    Sheet sheet = sheetManager.getCurrentSheet(sheetName);
    Coordinate coordinate = CoordinateUtils.parseCellId(cellId);
    CoordinateUtils.validateCoordinate(sheet, coordinate);

    Cell cell = sheet.getCell(coordinate);
    if (cell == null) {
        throw new IllegalArgumentException("Cell " + cellId + " does not exist.");
    }
    return cell;
}
    @Override
    public void updateCellTextColor(String sheetName, String cellId, String colorHex) {
        // Permission check (if required)
        Cell cell =updateCell(sheetName, cellId);

        cell.setTextColor(colorHex);
    }

    @Override
    public void resetCellDesign(String sheetName, String cellId) {
        // Permission check (if required)
        Cell cell =updateCell(sheetName, cellId);
        cell.setTextColor("#000000"); // Black text
        cell.setBackgroundColor("#FFFFFF"); // White background
    }

    @Override
    public Collection<SheetSummaryDTO> getAllSheetsSummary(String currentUserName) {
        List<SheetSummaryDTO> sheetSummaryDTOs = new ArrayList<>();
        for (String sheetName : sheetManager.getAllSheetNames()) {
            Sheet sheet = sheetManager.getCurrentSheet(sheetName);
            PermissionsManager permissionsManager = getPermissionsManager(sheetName);
            PermissionType permissionType = permissionsManager.getUserPermission(currentUserName);

            String size = sheet.getRows() + "x" + sheet.getCols();
            SheetSummaryDTO summaryDTO = new SheetSummaryDTO(
                    sheet.getName(),
                    sheet.getOwner(),
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

        if (!approverUsername.equals(permissionsManager.getOwnerUsername())) {
            throw new IllegalArgumentException("Only the owner can handle permission requests.");
        }

        PermissionRequest request = requestOptional.get();
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
        return sheetManager.getCurrentSheetVersion(sheetName);
    }

    @Override
    public SheetDTO performDynamicAnalysis(String sheetName, Map<String, Double> cellValues) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return dynamicAnalysisService.performDynamicAnalysis(sheet, cellValues);
    }

    @Override
    public SheetDTO performSingleDynamicAnalysis(String sheetName, String cellId, String newValue) {
        Sheet sheet = sheetManager.getCurrentSheet(sheetName);
        return dynamicAnalysisService.performSingleDynamicAnalysis(sheet, cellId, newValue);
    }
}
