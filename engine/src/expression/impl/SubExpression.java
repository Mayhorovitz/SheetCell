package expression.impl;


import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.Sheet;
import sheet.api.SheetReadActions;
public class SubExpression extends TernaryExpression {

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        super(source, startIndex, endIndex);
    }

    @Override
    protected EffectiveValue eval(EffectiveValue source, EffectiveValue startIndex, EffectiveValue endIndex) {
        if (source == null && startIndex == null && endIndex == null) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }
        String sourceStr = source.extractValueWithExpectation(String.class);
        Double startIdx = startIndex.extractValueWithExpectation(Double.class);
        Double endIdx = endIndex.extractValueWithExpectation(Double.class);

        if (sourceStr == null || startIdx == null || endIdx == null ||
                startIdx < 0 || endIdx >= sourceStr.length() || startIdx > endIdx ||
                !isInteger(startIdx) || !isInteger(endIdx)) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
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
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}