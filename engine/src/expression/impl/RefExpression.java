package expression.impl;
import cell.api.Cell;
import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import sheet.api.SheetReadActions;
import coordinate.Coordinate;


    public class RefExpression implements Expression {

        private final Coordinate coordinate;

        public RefExpression(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public EffectiveValue eval(SheetReadActions sheet) {
            Cell cell = sheet.getCell(coordinate);
            if (cell == null || cell.getEffectiveValue() == null) {
              return null;
            }
            return sheet.getCell(coordinate).getEffectiveValue();
        }

        @Override
        public CellType getFunctionResultType() {
            return CellType.UNKNOWN;
        }
    }
