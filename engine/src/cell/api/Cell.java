package cell.api;

import coordinate.Coordinate;

import java.util.List;


    public interface Cell {
        // getters
        String getBackgroundColor();
        void setBackgroundColor(String backgroundColor);
        String getTextColor();
        void setTextColor(String textColor); // Fixed semicolon
        Coordinate getCoordinate();
        String getOriginalValue();

        String getChangedBy();

        EffectiveValue getEffectiveValue();
        int getVersion();
        List<Cell> getDependsOn();
        List<Cell> getInfluencingOn();
        void updateVersion(int newVersion);
        boolean calculateEffectiveValue();
        void resetDependencies();
        void resetInfluences();

        void setOriginalValue(String value);
    }
