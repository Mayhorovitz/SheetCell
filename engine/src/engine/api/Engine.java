package engine.api;


import cell.api.Cell;
import sheet.api.Sheet;

import java.util.Map;

public interface Engine {

    void loadFile(String filePath) throws Exception;

    void exit();
    Sheet getCurrentSpreadSheet();

    public Cell getCellInfo(String cellIdentifier);

    void updateCell(String cellId, String newValue);
}
