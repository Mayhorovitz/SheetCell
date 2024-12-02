package dto.api;


import dto.permission.PermissionStatus;
import dto.permission.PermissionType;

public interface PermissionRequestDTO {
    String getUsername();
    void setUsername(String username);
    String getSheetName();
    void setSheetName(String sheetName);
    PermissionType getRequestedPermission();
    void setRequestedPermission(PermissionType requestedPermission);
    PermissionStatus getStatus();
    void setStatus(PermissionStatus status);
}
