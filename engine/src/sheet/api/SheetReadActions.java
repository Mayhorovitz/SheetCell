package sheet.api;

import cell.api.Cell;

public interface SheetReadActions {
    int getVersion();
    Cell getCell(int row, int column);

}
