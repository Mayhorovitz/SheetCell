package expression.impl;
import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;

    public class EqualExpression extends BinaryExpression {

        public  EqualExpression(Expression left, Expression right) {
            super(left, right);
        }

        public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
            if (left == null || right == null) {
                return new EffectiveValueImpl(CellType.UNKNOWN, "UNKNOWN");
            }
            if (left.getCellType() == CellType.UNKNOWN|| right.getCellType() == CellType.UNKNOWN) {
                return new EffectiveValueImpl(CellType.UNKNOWN, "UNKNOWN");
            }

            boolean res = (left.getCellType() == right.getCellType());
            return new EffectiveValueImpl(CellType.BOOLEAN, res);
        }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
    }

