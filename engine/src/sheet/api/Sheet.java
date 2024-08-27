package sheet.api;


import cell.api.Cell;
import coordinate.Coordinate;

public interface Sheet extends SheetReadActions, SheetUpdateActions {

    void setName(String name);

    void setRows(int rows);

    void setCols(int columns);

    void setRowHeight(int rowsHeightUnits);

    void setColWidth(int columnWidthUnits);

    void setVersion(int version);
    public void addCell(Coordinate coordinate, Cell cell);

    void setSheetVersion(int loadVersion);
}
