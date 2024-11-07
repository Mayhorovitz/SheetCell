package dto.impl;


import coordinate.Coordinate;

import java.util.List;

public class CellDTOImpl implements dto.api.CellDTO {
    private String identity;
    private String originalValue;
    private String effectiveValue;
    private int version;
    private String backgroundColor;
    private String textColor;
    private List<String> dependsOn; //
    private List<String> influencingOn;

    public CellDTOImpl(String identity, String originalValue, String effectiveValue, int version, String backgroundColor, String textColor, List<String> dependsOn, List<String> influencingOn) {
        this.identity = identity;
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.version = version;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.dependsOn = dependsOn;
        this.influencingOn = influencingOn;
    }

    // Getters
    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public String getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public String getTextColor() {
        return textColor;
    }

    @Override
    public List<String> getDependsOn() {
        // Map each Cell in dependsOn to its cell ID (e.g., "A1", "B2")
        return dependsOn;
    }

    @Override
    public List<String> getInfluencingOn() {
        // Map each Cell in influencingOn to its cell ID (e.g., "A1", "B2")
        return influencingOn;
    }

    // Helper method to convert Coordinate to cell ID string (e.g., "A1")
    private String coordinateToCellId(Coordinate coordinate) {
        return getColumnName(coordinate.getColumn()) + coordinate.getRow();
    }

    // Helper method to convert column index to column name (e.g., 1 -> "A")
    private String getColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        while (colIndex > 0) {
            int remainder = (colIndex - 1) % 26;
            columnName.insert(0, (char) (remainder + 'A'));
            colIndex = (colIndex - 1) / 26;
        }
        return columnName.toString();
    }
}
