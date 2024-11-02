package dto.impl;

public class SheetSummaryDTO {
    private String name;
    private String owner;
    private String size; // גודל הדף, נניח שהוא יוצג כ-"5x7"
    private String permissionType;

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
