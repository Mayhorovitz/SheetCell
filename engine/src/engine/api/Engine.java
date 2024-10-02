package engine.api;


import cell.api.Cell;
import engine.exceptions.InvalidVersionException;
import range.api.Range;
import sheet.api.Sheet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface Engine {

    void loadFile(String filePath) throws Exception;
    void saveSystemState(String filePath) throws IOException;
    void loadSystemState(String filePath) throws IOException, ClassNotFoundException;
    Sheet getCurrentSheet();
    Cell getCellInfo(String cellIdentifier);
    void updateCell(String cellId, String newValue);
    int getCurrentSheetVersion();
    Sheet getSheetByVersion(int version) throws InvalidVersionException;
    void exit();

    void addRangeToSheet(String name, String range);

    void deleteRangeFromSheet(String name);
    Range getRangeFromSheet(String name);
    Collection<Range> getAllRangesFromSheet();
    int convertColumnToIndex(String colPart);

    Sheet sortSheetRangeByColumns(String range, String[] columns);


    List<String> getUniqueValuesInRangeColumn(String range, String column);

    Sheet filterSheetByValues(String range, String column, List<String> selectedValues, List<Integer> originalRowNumbers);
}
