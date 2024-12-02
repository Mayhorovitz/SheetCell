package permission;

import dto.permission.PermissionStatus;
import dto.permission.PermissionType;

public class PermissionRequest {
    private final String username;
    private final PermissionType requestedPermission;
    private PermissionStatus status;

    public PermissionRequest(String username, PermissionType requestedPermission, PermissionStatus status) {
        this.username = username;
        this.requestedPermission = requestedPermission;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public PermissionType getRequestedPermission() {
        return requestedPermission;
    }

    public PermissionStatus getStatus() {
        return status;
    }

    public void setStatus(PermissionStatus status) {
        this.status = status;
    }
}
