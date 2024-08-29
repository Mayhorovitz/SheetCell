package expression.impl;


import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;

public class SubExpression extends TernaryExpression {

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        super(source, startIndex, endIndex);
    }

    @Override
    protected EffectiveValue eval(EffectiveValue source, EffectiveValue startIndex, EffectiveValue endIndex) {
        if (source == null && startIndex == null && endIndex == null) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }
        // extract the strings from the operands
        String extractSource = source.extractValueWithExpectation(String.class);
        Double extractStartIndex = startIndex.extractValueWithExpectation(Double.class);
        Double extractEndIndex = endIndex.extractValueWithExpectation(Double.class);

        // validate inputs
        if (extractSource == null || extractStartIndex == null || extractEndIndex == null ||
                extractStartIndex < 0 || extractEndIndex >= extractSource.length() || extractStartIndex > extractEndIndex ||
                !isInteger(extractStartIndex) || !isInteger(extractEndIndex)) {
            return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!");
        }

        // Convert indices to integers and perform the substring operation
        int start = (int) Math.floor(extractStartIndex);
        int end = (int) Math.floor(extractEndIndex);
        String result = extractSource.substring(start, end + 1);

        // Return the result as an EffectiveValue
        return new EffectiveValueImpl(CellType.STRING, result);
    }

    public static boolean isInteger(double number) {
        return number == Math.floor(number);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}