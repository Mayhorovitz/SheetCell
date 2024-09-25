package expression.impl;

import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;
import sheet.api.SheetReadActions;

public class ABSExpression implements Expression {

    private final Expression exp;

    public ABSExpression(Expression argument) {
        this.exp = argument;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue expressionEffectiveValue = exp.eval(sheet);
        // Extract numeric value from the operand
        Double val = expressionEffectiveValue.extractValueWithExpectation(Double.class);


        double result = Math.abs(val);
        // Return the result as an EffectiveValue
        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
