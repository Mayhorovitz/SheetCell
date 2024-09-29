package expression.impl;

import cell.api.Cell;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;
import range.api.Range;
import sheet.api.SheetReadActions;

import java.util.List;

public class AverageExpression implements Expression {
    private String range;

    public AverageExpression(String range) {
        this.range = range;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        int counter = 0;
        Range range = sheet.getRange(this.range);
        if (range == null) {
            return new EffectiveValueImpl(CellType.INVALID, Double.NaN);
        }
        double sum = 0;
        List<Cell> cellsInRange = sheet.getCellsInRange(range);
        for (Cell cell : cellsInRange) {
            EffectiveValue cellValue = cell.getEffectiveValue();
            // Extract numeric value
            if (cellValue == null) {
                sum += 0;
            } else {
                if(cellValue.getCellType() == CellType.NUMERIC) {
                    counter++;
                    Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                        sum += numericValue; // Add to sum if it's a valid number

                }
            }

        }
        double res = sum /counter;
        return new EffectiveValueImpl(CellType.NUMERIC, res);

    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

}

