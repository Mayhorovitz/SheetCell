package dto.impl;

public class SheetSummaryDTO {
    private final String name;
    private final String owner;
    private final String size;
    private final String permissionType;

    // Constructor
    public SheetSummaryDTO(String name, String owner, String size, String permissionType) {
        this.name = name;
        this.owner = owner;
        this.size = size;
        this.permissionType = permissionType;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getSize() {
        return size;
    }

    public String getPermissionType() {
        return permissionType;
    }
}
