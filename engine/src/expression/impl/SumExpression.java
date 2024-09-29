package expression.impl;

import cell.api.Cell;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;
import range.api.Range;
import range.impl.RangeImpl;
import sheet.api.SheetReadActions;
import java.util.*;


public class SumExpression implements Expression {
    private String range;

    public SumExpression(String range) {
        this.range = range;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
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
                    Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                    sum += numericValue; // Add to sum if it's a valid number

                }
            }

        }
        return new EffectiveValueImpl(CellType.NUMERIC, sum);

    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

}

