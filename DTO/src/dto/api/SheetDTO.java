package dto.api;

import java.util.Map;

public interface SheetDTO extends DTO {
    String getName();

    String getOwner();

    int getVersion();
    int getRows();
    int getCols();
    int getRowHeight();
    int getColWidth();
    Map<String, CellDTO> getCells();
}
