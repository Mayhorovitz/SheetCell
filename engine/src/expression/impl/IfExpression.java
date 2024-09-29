package expression.impl;

import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;

public class IfExpression extends TernaryExpression {



    public IfExpression(Expression condition, Expression thenExpression, Expression elseExpression) {
        super(condition, thenExpression, elseExpression);
    }

    @Override
    protected EffectiveValue eval(EffectiveValue conditionValue, EffectiveValue thenValue, EffectiveValue elseValue) {

        if (conditionValue.getCellType() != CellType.BOOLEAN) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }
        boolean conditionResult = (Boolean) conditionValue.getValue();
        EffectiveValue result;

        if (conditionResult) {
            result = thenValue;
        } else {
            result = elseValue;
        }

        if (!result.getCellType().equals(result.getCellType())) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }

        return result;

    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;    }
}
