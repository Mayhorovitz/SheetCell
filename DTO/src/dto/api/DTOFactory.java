package dto.api;


import cell.api.Cell;
import dto.impl.CellDTOImpl;
import dto.impl.RangeDTOImpl;
import range.api.Range;
import sheet.api.Sheet;

public interface DTOFactory {
    SheetDTO createSheetDTO(Sheet sheet);
    CellDTOImpl createCellDTO(Cell cell);
    CellDTOImpl createEmptyCellDTO(String identity);
    RangeDTOImpl createRangeDTO(Range range);
}
