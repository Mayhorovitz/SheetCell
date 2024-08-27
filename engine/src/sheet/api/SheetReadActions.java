package sheet.api;

import cell.api.Cell;
import coordinate.Coordinate;

public interface SheetReadActions {
    int getVersion();
    Cell getCell(Coordinate coordinate);

}
