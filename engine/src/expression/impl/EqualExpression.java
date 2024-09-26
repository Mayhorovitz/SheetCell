package expression.impl;

import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;

public class EqualExpression extends BinaryExpression {

    public EqualExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        if (left == null || right == null) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        // Check for type mismatch
        if (left.getCellType() != right.getCellType()) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        // Compare values based on type
        if (left.getCellType() == CellType.NUMERIC) {
            Double leftValue = left.extractValueWithExpectation(Double.class);
            Double rightValue = right.extractValueWithExpectation(Double.class);
            if (leftValue == null || rightValue == null) {
                return new EffectiveValueImpl(CellType.BOOLEAN, false);
            }
            return new EffectiveValueImpl(CellType.BOOLEAN, leftValue.equals(rightValue));
        } else if (left.getCellType() == CellType.STRING) {
            String leftValue = left.extractValueWithExpectation(String.class);
            String rightValue = right.extractValueWithExpectation(String.class);
            if (leftValue == null || rightValue == null) {
                return new EffectiveValueImpl(CellType.BOOLEAN, false);
            }
            return new EffectiveValueImpl(CellType.BOOLEAN, leftValue.equals(rightValue));
        } else if (left.getCellType() == CellType.BOOLEAN) {
            Boolean leftValue = left.extractValueWithExpectation(Boolean.class);
            Boolean rightValue = right.extractValueWithExpectation(Boolean.class);
            if (leftValue == null || rightValue == null) {
                return new EffectiveValueImpl(CellType.BOOLEAN, false);
            }
            return new EffectiveValueImpl(CellType.BOOLEAN, leftValue.equals(rightValue));
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, false);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
