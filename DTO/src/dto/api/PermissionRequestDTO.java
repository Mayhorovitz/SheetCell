package dto.api;

import permission.PermissionStatus;
import permission.PermissionType;

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
