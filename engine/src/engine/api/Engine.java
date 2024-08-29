package engine.api;


import cell.api.Cell;
import engine.exceptions.InvalidVersionException;
import sheet.api.Sheet;

import java.io.IOException;

public interface Engine {

    void loadFile(String filePath) throws Exception;
    void saveSystemState(String filePath) throws IOException;
    void loadSystemState(String filePath) throws IOException, ClassNotFoundException;
    Sheet getCurrentSheet();
    Cell getCellInfo(String cellIdentifier);
    void updateCell(String cellId, String newValue);
    int getCurrentSheetVersion();
    Sheet getSheetByVersion(int version) throws InvalidVersionException, InvalidVersionException;
    void exit();
}
