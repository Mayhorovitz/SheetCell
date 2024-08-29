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
        EffectiveValue leftValue  = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);
        return eval( leftValue , rightValue);
    }

    protected abstract EffectiveValue eval(EffectiveValue arg1, EffectiveValue arg2);
}