package expression.impl;

import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.SheetReadActions;

public class AndExpression extends BinaryExpression {

    public AndExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        if (left.getCellType() != CellType.BOOLEAN || right.getCellType() != CellType.BOOLEAN) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        Boolean leftValue = left.extractValueWithExpectation(Boolean.class);
        Boolean rightValue = right.extractValueWithExpectation(Boolean.class);

        if (leftValue == null || rightValue == null) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, leftValue && rightValue);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
