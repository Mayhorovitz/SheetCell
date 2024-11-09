package dto.api;


import java.util.List;

public interface CellDTO extends DTO {
    String getIdentity();
    String getOriginalValue();
    String getEffectiveValue();
    int getVersion();

    String getChangedBy();

    String getBackgroundColor();
    String getTextColor();
    List<String> getDependsOn();
    List<String> getInfluencingOn();
}
