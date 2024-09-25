package javaFX.sheet;


import cell.api.Cell;
import sheet.api.Sheet;

import java.util.List;

public class SheetModel {

    private Sheet currentSheet;

    public void setCurrentSheet(Sheet sheet) {
        this.currentSheet = sheet;
    }

    public List<Cell> getCells() {
        return currentSheet != null ? currentSheet.getActiveCells().values().stream().toList() : List.of();
    }

    public int getRows() {
        return currentSheet.getRows();
    }

    public int getCols() {
        return currentSheet.getCols();
    }
}
