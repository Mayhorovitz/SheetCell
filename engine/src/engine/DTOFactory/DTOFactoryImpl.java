package engine.DTOFactory;

import cell.api.Cell;
import dto.impl.CellDTOImpl;
import dto.impl.RangeDTOImpl;
import dto.impl.SheetDTOImpl;
import dto.api.SheetDTO;
import range.api.Range;
import sheet.api.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DTOFactoryImpl implements DTOFactory {

    // Creates a SheetDTO from a given Sheet, including cells and ranges
    @Override
    public SheetDTO createSheetDTO(Sheet sheet) {
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

    // Creates a CellDTOImpl for a given Cell with dependencies and influences
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

    // Creates an empty CellDTOImpl with default values for a specified identity
    @Override
    public CellDTOImpl createEmptyCellDTO(String identity) {
        return new dto.impl.CellDTOImpl(identity, "", "", 0, "", "#FFFFFF", "#000000", new ArrayList<>(), new ArrayList<>());
    }

    // Creates a RangeDTOImpl for a given Range with its name, start, and end coordinates
    @Override
    public RangeDTOImpl createRangeDTO(Range range) {
        return new RangeDTOImpl(
                range.getName(),
                range.getFrom(),
                range.getTo()
        );
    }
}
