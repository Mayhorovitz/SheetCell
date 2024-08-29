package expression.impl;


import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.SheetReadActions;

public class IdentityExpression implements Expression {

    private final Object value;
    private final CellType type;

    public IdentityExpression(Object value, CellType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        return new EffectiveValueImpl(type, value);
    }

    @Override
    public CellType getFunctionResultType() {
        return type;
    }
}
