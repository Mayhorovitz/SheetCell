package engine.api;


import cell.api.Cell;
import sheet.api.Sheet;

import java.util.Map;

public interface Engine {

    void loadSheet(String filePath) throws Exception;

    Sheet getSheetState();

    Cell getCellInfo(String cellId);

    void updateCell(String cellId, String newValue);

    void exitProgram();

    int getCurrentVersion();

    Map<Integer, Sheet> getSpreadSheetVersionHistory();
}
