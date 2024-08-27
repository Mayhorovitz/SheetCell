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
        Double valueToAbs = expressionEffectiveValue.extractValueWithExpectation(Double.class);

        if(valueToAbs == null) {
            throw new IllegalArgumentException("Invalid argument in function " + this.getClass().getSimpleName() + ".\n"
                    + "the argument expected is from type " + Number.class.getSimpleName() + " but the argument is from type - " + expressionEffectiveValue.getCellType() + ".");
        }

        double result = Math.abs(valueToAbs);

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
