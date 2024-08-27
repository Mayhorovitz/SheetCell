
package expression.impl;


import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.Sheet;
import sheet.api.SheetReadActions;

public class ConcatExpression extends BinaryExpression{

    private Expression left;
    private Expression right;

    public ConcatExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        if (left == null || right == null || left.getCellType() == CellType.UNKNOWN || right.getCellType() == CellType.UNKNOWN) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }
        String  value1 = left.extractValueWithExpectation(String .class);
        String  value2 = right.extractValueWithExpectation(String .class);
        if (value1 == null || value2 == null) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        String res = value1 + value2;
        return new EffectiveValueImpl(CellType.STRING, res);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}