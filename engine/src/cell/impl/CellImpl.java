package cell.impl;

import cell.api.Cell;
import cell.api.EffectiveValue;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import expression.api.Expression;
import expression.parser.FunctionParser;
import javafx.scene.paint.Color;
import sheet.api.SheetReadActions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CellImpl implements Cell , Serializable {

    private final Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    private final List<Cell> dependsOn;
    private final List<Cell> influencingOn;
    private Color backgroundColor; // Default white background
    private Color textColor;       // Default black text
    private SheetReadActions sheet;

//constructors
    public CellImpl(int row, int column, String originalValue, int version, SheetReadActions sheet )  {
        this.coordinate = new CoordinateImpl(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependsOn = new ArrayList<>();
        this.influencingOn = new ArrayList<>();
        this.sheet = sheet;
    }

    public CellImpl(Coordinate coordinate, String originalValue, int version, SheetReadActions sheet )  {
        this.coordinate = coordinate;
        this.originalValue = originalValue;
        this.version = version;
        this.dependsOn = new ArrayList<>();
        this.influencingOn = new ArrayList<>();

        this.sheet = sheet;
    }

    //getters

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public EffectiveValue getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public void updateVersion(int newVersion){
        this.version = newVersion;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<Cell> getDependsOn() {
        return dependsOn;
    }

    @Override
    public List<Cell> getInfluencingOn() {
        return influencingOn;
    }

    // calculates and updates the effective value of a cell based on its original value
    @Override
    public boolean calculateEffectiveValue() {
        Expression expression = FunctionParser.parseExpression(originalValue);
        // get the new effective value
        EffectiveValue newEffectiveValue = expression.eval(sheet);
        if (!newEffectiveValue.equals(effectiveValue)) {
            //update the effective value to the new one if they are different
            effectiveValue = newEffectiveValue;
            return true;
        }

        return false;
    }


    @Override
    public void resetDependencies() {
        this.dependsOn.clear(); // איפוס רשימת התאים שהתא תלוי בהם
    }

    @Override
    public void resetInfluences() {
        this.influencingOn.clear(); // איפוס רשימת התאים שהתא משפיע עליהם
    }


}
