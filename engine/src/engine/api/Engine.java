package engine.api;


import dto.impl.CellDTOImpl;
import dto.api.PermissionRequestDTO;
import dto.api.RangeDTO;
import dto.api.SheetDTO;
import dto.impl.SheetSummaryDTO;
import permission.PermissionRequest;
import dto.permission.PermissionStatus;
import dto.permission.PermissionType;
import permission.PermissionsManager;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Engine {

    void loadFile(InputStream inputStream, String owner) throws Exception;
    int getCurrentSheetVersion(String sheetName);
    void updateCell(String sheetName, String coordinate, String newValue, String userName);
    CellDTOImpl getCellInfo(String sheetName, String cellIdentifier);
    void addRangeToSheet(String sheetName, String name, String range);
    void deleteRangeFromSheet(String sheetName, String name);
    RangeDTO getRangeFromSheet(String sheetName, String name);
    Collection<RangeDTO> getAllRangesFromSheet(String sheetName);
    SheetDTO sortSheetRangeByColumns(String sheetName, String range, String[] columns);
    List<String> getUniqueValuesInRangeColumn(String sheetName, String range, String column);
    SheetDTO filterSheetByValues(String sheetName, String range, String column, List<String> selectedValues);
    SheetDTO getSheetDTOByVersion(String sheetName, int versionNumber);
    SheetDTO getCurrentSheetDTO(String sheetName);
    void updateCellBackgroundColor(String sheetName, String cellId, String colorHex);
    void updateCellTextColor(String sheetName, String cellId, String colorHex);
    void resetCellDesign(String sheetName, String cellId);
    PermissionsManager getPermissionsManager(String sheetName);
    Collection<SheetSummaryDTO> getAllSheetsSummary(String currentUserName);
    void submitPermissionRequest(String sheetName, String requesterUsername, PermissionType requestedPermission);
    List<PermissionRequest> getPermissionRequests(String sheetName);
    void handlePermissionRequest(String sheetName, int requestIndex, String approverUsername, PermissionStatus status);
    void handleResponseRequest(String sheetName, String requesterUsername, String approverUsername, PermissionStatus status);
    List<PermissionRequestDTO> getPermissionRequestsDTO(String sheetName);
    List<PermissionRequestDTO> getPendingRequestsForOwner(String ownerUsername);
    int getLatestVersion(String sheetName);
    SheetDTO performDynamicAnalysis(String sheetName, Map<String, Double> cellValues);

    SheetDTO performSingleDynamicAnalysis(String sheetName, String cellId, String newValue);
}
