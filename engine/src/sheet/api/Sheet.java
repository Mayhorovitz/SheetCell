package sheet.api;


import cell.api.Cell;
import coordinate.Coordinate;

import java.util.List;
import java.util.Map;

public interface Sheet extends SheetReadActions, SheetUpdateActions {

    void setName(String name);

    void setRows(int rows);

    void setCols(int columns);

    void setRowHeight(int rowsHeightUnits);

    void setColWidth(int columnWidthUnits);

    void setVersion(int version);

    public void addCell(Coordinate coordinate, Cell cell);

    void setSheetVersion(int loadVersion);

    String getName();
    
    int getRows();

    int getCols();

    int getColWidth();

    int getRowHeight();

    Sheet updateCellValueAndCalculate(int row, int column, String value);
    Map<Coordinate, Cell> getActiveCells();
}
