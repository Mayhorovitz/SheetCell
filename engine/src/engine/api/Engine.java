package engine.api;


import cell.api.Cell;
import engine.exceptions.InvalidVersionException;
import range.api.Range;
import sheet.api.Sheet;

import java.io.IOException;
import java.util.Collection;

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
    void addRangeToSheet(String name, String startCell, String endCell);
    void deleteRangeFromSheet(String name);
    Range getRangeFromSheet(String name);
    Collection<Range> getAllRangesFromSheet();
    int convertColumnToIndex(String colPart);
}
