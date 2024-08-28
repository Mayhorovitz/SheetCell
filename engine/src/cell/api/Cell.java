package cell.api;

import java.util.List;
import coordinate.Coordinate;
import sheet.api.Sheet;

public interface Cell {

    Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);
    void setEffectiveValue(EffectiveValue effectiveValue);
    EffectiveValue getEffectiveValue();
    int getVersion();
    List<Cell> getDependsOn();
    List<Cell> getInfluencingOn();

    void updateVersion(int newVersion);

    boolean calculateEffectiveValue();

    void resetDependencies();

    void resetInfluences();
}
