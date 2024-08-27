package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import cell.api.EffectiveValue;

public abstract class BinaryExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public BinaryExpression(Expression argument1, Expression argument2) {
        this.left = argument1;
        this.right = argument2;
    }
    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue value1 = left.eval(sheet);
        EffectiveValue value2 = right.eval(sheet);
        return eval(sheet);
    }

    protected abstract EffectiveValue eval(EffectiveValue arg1, EffectiveValue arg2);
}