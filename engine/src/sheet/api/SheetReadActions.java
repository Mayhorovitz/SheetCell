package sheet.api;

import cell.api.Cell;
import coordinate.Coordinate;
import range.api.Range;
import java.util.*;

public interface SheetReadActions {
    int getVersion();
    Cell getCell(Coordinate coordinate);
    public Range getRange(String name);

    List <Cell> getCellsInRange(Range range);
}
