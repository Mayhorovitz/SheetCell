package permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Manages permissions for a specific sheet.
public class PermissionsManager {
    private final String ownerUsername;
    private final Map<String, PermissionType> userPermissions;
    private final List<PermissionRequest> permissionRequests;

    public PermissionsManager(String ownerUsername) {
        this.ownerUsername = ownerUsername;
        this.userPermissions = new HashMap<>();
        this.permissionRequests = new ArrayList<>();

        // The owner has OWNER permission by default.
        userPermissions.put(ownerUsername, PermissionType.OWNER);
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    // Adds permission for a user.
    public synchronized void addPermission(String username, PermissionType permissionType) {
        if (username == null || permissionType == null) {
            throw new IllegalArgumentException("Username and permission type cannot be null.");
        }
        PermissionRequest request = new PermissionRequest(username, permissionType, PermissionStatus.APPROVED);
        permissionRequests.add(request);    }

    // Submits a permission request from a user.
    public synchronized void submitPermissionRequest(String requesterUsername, PermissionType requestedPermission) {
        PermissionRequest request = new PermissionRequest(requesterUsername, requestedPermission, PermissionStatus.PENDING);
        permissionRequests.add(request);
    }

    public synchronized List<PermissionRequest> getPermissionRequests() {
        // Return a copy to prevent external modification.
        return new ArrayList<>(permissionRequests);
    }

    public synchronized void handlePermissionRequest(int requestIndex, String approverUsername, PermissionStatus status) {
        if (!approverUsername.equals(ownerUsername)) {
            throw new IllegalArgumentException("Only the owner can handle permission requests.");
        }

        if (requestIndex < 0 || requestIndex >= permissionRequests.size()) {
            throw new IndexOutOfBoundsException("Invalid request index.");
        }

        PermissionRequest request = permissionRequests.get(requestIndex);

        if (request.getStatus() != PermissionStatus.PENDING) {
            throw new IllegalStateException("Request has already been handled.");
        }

        request.setStatus(status);

        if (status == PermissionStatus.APPROVED) {
            // Grant the requested permission to the user.
            userPermissions.put(request.getUsername(), request.getRequestedPermission());
        }
    }

    public synchronized PermissionType getUserPermission(String username) {
        return userPermissions.getOrDefault(username, PermissionType.NONE);
    }

    public synchronized Map<String, PermissionType> getUserPermissions() {
        return new HashMap<>(userPermissions);
    }
}
