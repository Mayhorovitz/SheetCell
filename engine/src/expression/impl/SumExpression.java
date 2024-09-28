package expression.impl;

import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;
import range.impl.RangeImpl;
import sheet.api.SheetReadActions;

public class SumExpression implements Expression {
    private RangeImpl range;

    public SumExpression (RangeImpl range){
        this.range = range;
    }
    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        if (range == null){
            return new EffectiveValueImpl(CellType.INVALID,Double.NaN);
        }
        for (CellIdentifierImpl cellIdentifier : range.getCellsInRange()) {
            EffectiveValue cellValue = spreadSheet.getCellEffectiveValue(cellIdentifier);
            // Extract numeric value
            if (cellValue == null){
                sum += 0;
            }
            else {
                Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                if (numericValue != null) {
                    sum += numericValue; // Add to sum if it's a valid number
                }
            }


            @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
