package dto.impl;


import java.util.List;

public class CellDTOImpl implements dto.api.CellDTO {
    private final String identity;
    private final String originalValue;
    private final String effectiveValue;
    private final int version;
    private final String changedBy;
    private final String backgroundColor;
    private final String textColor;
    private final List<String> dependsOn; //
    private final List<String> influencingOn;

    public CellDTOImpl(String identity, String originalValue, String effectiveValue, int version, String changedBy, String backgroundColor, String textColor, List<String> dependsOn, List<String> influencingOn) {
        this.identity = identity;
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.version = version;
        this.changedBy = changedBy;
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
    public String getChangedBy() {
        return changedBy;
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
        return dependsOn;
    }

    @Override
    public List<String> getInfluencingOn() {
        return influencingOn;
    }

}
