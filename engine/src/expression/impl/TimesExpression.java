package expression.impl;
import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.Sheet;
import sheet.api.SheetReadActions;

public class TimesExpression  extends BinaryExpression {

    public TimesExpression(Expression argument1, Expression argument2) {
        super(argument1, argument2);
    }

    @Override
    public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        if (left == null || right == null || left.getCellType() == CellType.UNKNOWN || right.getCellType() == CellType.UNKNOWN) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        Double value1 = left.extractValueWithExpectation(Double.class);
        Double value2 = right.extractValueWithExpectation(Double.class);
        if (value1 == null || value2 == null) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        double res = value1*value2;
        return new EffectiveValueImpl(CellType.NUMERIC, res);
    }
    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}