package dto.api;

import dto.impl.CellDTOImpl;
import dto.impl.RangeDTOImpl;

import java.util.Map;

public interface SheetDTO extends DTO {
    String getName();

    String getOwner();

    int getVersion();
    int getRows();
    int getCols();
    int getRowHeight();
    int getColWidth();

    Map<String, RangeDTOImpl>  getRanges();

    Map<String, CellDTOImpl> getCells();
}
