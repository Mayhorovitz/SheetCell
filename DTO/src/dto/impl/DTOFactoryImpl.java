package dto.impl;


import cell.api.Cell;
import dto.api.DTOFactory;
import dto.api.SheetDTO;
import range.api.Range;
import sheet.api.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DTOFactoryImpl implements DTOFactory {

    @Override
    public SheetDTO createSheetDTO(Sheet sheet) {
        // המרת התאים למפת CellDTOImpl
        Map<String, CellDTOImpl> cellDTOs = sheet.getActiveCells().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> createCellDTO(e.getValue())
                ));

        Map<String, RangeDTOImpl> rangeDTOs = sheet.getAllRanges().stream()
                .collect(Collectors.toMap(
                        Range::getName,
                        this::createRangeDTO
                ));

        return new SheetDTOImpl(
                sheet.getName(),
                sheet.getOwner(),
                sheet.getVersion(),
                sheet.getRows(),
                sheet.getCols(),
                sheet.getRowHeight(),
                sheet.getColWidth(),
                cellDTOs,
                rangeDTOs
        );
    }

    public CellDTOImpl createCellDTO(Cell cell) {
        List<String> dependsOnIds = cell.getDependsOn().stream()
                .map(depCell -> depCell.getCoordinate().toString())
                .collect(Collectors.toList());

        List<String> influencingOnIds = cell.getInfluencingOn().stream()
                .map(infCell -> infCell.getCoordinate().toString())
                .collect(Collectors.toList());

        return new dto.impl.CellDTOImpl(
                cell.getCoordinate().toString(),
                cell.getOriginalValue(),
                cell.getEffectiveValue().toString(),
                cell.getVersion(),
                cell.getChangedBy(),
                cell.getBackgroundColor(),
                cell.getTextColor(),
                dependsOnIds,
                influencingOnIds
        );
    }

    @Override
    public CellDTOImpl createEmptyCellDTO(String identity) {
        return new dto.impl.CellDTOImpl(identity, "", "", 0, "", "#FFFFFF", "#000000", new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public RangeDTOImpl createRangeDTO(Range range) {
        return new RangeDTOImpl(
                range.getName(),
                range.getFrom(),
                range.getTo()
        );
    }
}
