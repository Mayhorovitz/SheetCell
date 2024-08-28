package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import cell.api.EffectiveValue;

public abstract class TernaryExpression implements Expression {

    private final Expression argument1;
    private final Expression argument2;
    private final Expression argument3;

    public TernaryExpression(Expression argument1, Expression argument2, Expression argument3) {
        this.argument1 = argument1;
        this.argument2 = argument2;
        this.argument3 = argument3;
    }

    @Override
    public EffectiveValue eval(SheetReadActions spreadSheet) {
        return eval(
                argument1.eval(spreadSheet),
                argument2.eval(spreadSheet),
                argument3.eval(spreadSheet)
        );
    }

    protected abstract EffectiveValue eval(EffectiveValue arg1, EffectiveValue arg2, EffectiveValue arg3);
}