package expression.impl;

public class SubExpression {
}
package expressions.expressionsimpl;

import expressions.api.Expression;
import spreadsheet.cell.api.EffectiveValue;
import spreadsheet.api.ReadOnlySpreadSheet;

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
    public EffectiveValue evaluate(ReadOnlySpreadSheet spreadSheet) {
        return evaluate(
                argument1.evaluate(spreadSheet),
                argument2.evaluate(spreadSheet),
                argument3.evaluate(spreadSheet)
        );
    }

    protected abstract EffectiveValue evaluate(EffectiveValue arg1, EffectiveValue arg2, EffectiveValue arg3);
}

package expressions.expressionsimpl;

import expressions.api.Expression;
import spreadsheet.api.ReadOnlySpreadSheet;
import spreadsheet.cell.api.CellType;
import spreadsheet.cell.api.EffectiveValue;
import spreadsheet.cell.impl.EffectiveValueImpl;

public class Sub extends TernaryExpression {

    public Sub(Expression source, Expression startIndex, Expression endIndex) {
        super(source, startIndex, endIndex);
    }

    @Override
    protected EffectiveValue evaluate(EffectiveValue source, EffectiveValue startIndex, EffectiveValue endIndex) {
        if (source == null && startIndex == null && endIndex == null) {
            return new EffectiveValueImpl(CellType.INVALID_VALUE, "!UNDEFINED!");
        }
        String sourceStr = source.extractValueWithExpectation(String.class);
        Double startIdx = startIndex.extractValueWithExpectation(Double.class);
        Double endIdx = endIndex.extractValueWithExpectation(Double.class);

        if (sourceStr == null || startIdx == null || endIdx == null ||
                startIdx < 0 || endIdx >= sourceStr.length() || startIdx > endIdx ||
                !isInteger(startIdx) || !isInteger(endIdx)) {
            return new EffectiveValueImpl(CellType.INVALID_VALUE, "!UNDEFINED!");
        }

        int start = (int) Math.floor(startIdx);
        int end = (int) Math.floor(endIdx);
        String result = sourceStr.substring(start, end +1);
        return new EffectiveValueImpl(CellType.STRING, result);

    }

    public static boolean isInteger(double number) {
        // Check if the number is an integer
        return number == Math.floor(number);
    }

    @Override
    public CellType getFunctionResultType(ReadOnlySpreadSheet spreadSheet) {
        return CellType.STRING;
    }
}