package expression.impl;

import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import expression.api.Expression;
import sheet.api.SheetReadActions;

public class NotExpression implements Expression {

    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue value = expression.eval(sheet);
        if (value == null || value.getCellType() != CellType.BOOLEAN) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        Boolean boolValue = value.extractValueWithExpectation(Boolean.class);
        if (boolValue == null) {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        return new EffectiveValueImpl(CellType.BOOLEAN, !boolValue);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
