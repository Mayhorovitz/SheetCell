package expression.impl;
import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;
import sheet.api.Sheet;
import sheet.api.SheetReadActions;
import coordinate.Coordinate;


    public class RefExpression implements Expression {

        private final Coordinate coordinate;

        public RefExpression(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public EffectiveValue eval(SheetReadActions sheet) {
            // error handling if the cell is empty or not found
            return sheet.getCell(coordinate).getEffectiveValue();
        }

        @Override
        public CellType getFunctionResultType() {
            return CellType.UNKNOWN;
        }
    }
