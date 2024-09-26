package expression.impl;

import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;

public class BiggerExpression extends BinaryExpression {

    public BiggerExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        if (left == null || right == null || left.getCellType() != CellType.NUMERIC || right.getCellType() != CellType.NUMERIC) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        Double leftValue = left.extractValueWithExpectation(Double.class);
        Double rightValue = right.extractValueWithExpectation(Double.class);

        if (leftValue == null || rightValue == null) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, leftValue >= rightValue);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
