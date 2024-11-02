package dto.api;


import cell.api.Cell;
import range.api.Range;
import sheet.api.Sheet;

public interface DTOFactory {
    SheetDTO createSheetDTO(Sheet sheet);
    CellDTO createCellDTO(Cell cell);
    CellDTO createEmptyCellDTO(String identity);
    RangeDTO createRangeDTO(Range range);
}
