package cell.api;

import java.util.List;
import coordinate.Coordinate;
import javafx.scene.paint.Color;


    public interface Cell {
        // getters
        String getBackgroundColor();
        void setBackgroundColor(String backgroundColor);
        String getTextColor();
        void setTextColor(String textColor); // Fixed semicolon
        Coordinate getCoordinate();
        String getOriginalValue();
        EffectiveValue getEffectiveValue();
        int getVersion();
        List<Cell> getDependsOn();
        List<Cell> getInfluencingOn();
        void updateVersion(int newVersion);
        boolean calculateEffectiveValue();
        void resetDependencies();
        void resetInfluences();
    }
