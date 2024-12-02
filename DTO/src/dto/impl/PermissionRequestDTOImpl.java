package dto.impl;

import dto.api.PermissionRequestDTO;
import dto.permission.PermissionStatus;
import dto.permission.PermissionType;


public class PermissionRequestDTOImpl implements PermissionRequestDTO {
    private String username;
    private String sheetName;
    private PermissionType requestedPermission;
    private PermissionStatus status;

    public PermissionRequestDTOImpl(String username, String sheetName, PermissionType requestedPermission, PermissionStatus status) {
        this.username = username;
        this.sheetName = sheetName;  // הגדרה של שם הגיליון
        this.requestedPermission = requestedPermission;
        this.status = status;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public String getSheetName() {
        return sheetName;  // מתודת getter עבור sheetName
    }
    @Override

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;  // מתודת setter עבור sheetName
    }

    @Override
    public PermissionType getRequestedPermission() {
        return requestedPermission;
    }

    @Override
    public void setRequestedPermission(PermissionType requestedPermission) {
        this.requestedPermission = requestedPermission;
    }

    @Override
    public PermissionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(PermissionStatus status) {
        this.status = status;
    }
}
