package expression.api;

import cell.api.CellType;
import cell.api.EffectiveValue;
import sheet.api.SheetReadActions;

public interface Expression {
    EffectiveValue eval(SheetReadActions sheet);
    CellType getFunctionResultType();
}
