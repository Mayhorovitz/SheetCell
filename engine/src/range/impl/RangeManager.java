package range.impl;


import sheet.api.Sheet;
import range.api.Range;
import dto.api.RangeDTO;
import dto.api.SheetDTO;
import engine.DTOFactory.DTOFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RangeManager {

    private DTOFactory dtoFactory;

    public RangeManager(DTOFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    public void addRange(Sheet sheet, String name, String range) {
        sheet.addRange(name, range);
    }

    public void deleteRange(Sheet sheet, String name) {
        sheet.deleteRange(name);
    }

    public RangeDTO getRange(Sheet sheet, String name) {
        Range range = sheet.getRange(name);
        if (range != null) {
            return dtoFactory.createRangeDTO(range);
        } else {
            throw new IllegalArgumentException("Range not found: " + name);
        }
    }

    public Collection<RangeDTO> getAllRanges(Sheet sheet) {
        Collection<Range> ranges = sheet.getAllRanges();
        return ranges.stream()
                .map(dtoFactory::createRangeDTO)
                .collect(Collectors.toList());
    }

    public SheetDTO sortSheetRangeByColumns(Sheet sheet, String range, String[] columns) {
        Sheet sortedSheet = sheet.sortSheet(range, columns);
        return dtoFactory.createSheetDTO(sortedSheet);
    }

    public List<String> getUniqueValuesInRangeColumn(Sheet sheet, String range, String column) {
        return sheet.getUniqueValuesInRangeColumn(range, column);
    }

    public SheetDTO filterSheetByValues(Sheet sheet, String range, String column, List<String> selectedValues) {
        Sheet filteredSheet = sheet.filterSheetByValues(range, column, selectedValues);
        return dtoFactory.createSheetDTO(filteredSheet);
    }
}
