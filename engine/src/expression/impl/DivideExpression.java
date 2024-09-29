package expression.impl;

import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;

public class DivideExpression extends BinaryExpression{

        public DivideExpression(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
            // Check for null or unknown values
            if (left == null || right == null || left.getCellType() == CellType.UNKNOWN || right.getCellType() == CellType.UNKNOWN) {
                return new EffectiveValueImpl(CellType.INVALID, Double.NaN);
            }
            // Extract numeric values from the operands
            Double leftValue = left.extractValueWithExpectation(Double.class);
            Double rightValue = right.extractValueWithExpectation(Double.class);
            if (leftValue == null || rightValue == null) {
                return new EffectiveValueImpl(CellType.INVALID, Double.NaN);
            }
            double res = leftValue / rightValue;
            // Return the result as an EffectiveValue
            return new EffectiveValueImpl(CellType.NUMERIC, res);
        }

        @Override
        public CellType getFunctionResultType() {
            return CellType.NUMERIC;
        }
    }
