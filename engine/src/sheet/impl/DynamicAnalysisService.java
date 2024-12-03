package sheet.impl;

import sheet.api.Sheet;
import coordinate.Coordinate;
import dto.api.SheetDTO;
import engine.DTOFactory.DTOFactory;
import coordinate.CoordinateUtils;
import cell.api.Cell;

import java.util.Map;

public class DynamicAnalysisService {

    private final DTOFactory dtoFactory;

    public DynamicAnalysisService(DTOFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    public SheetDTO performDynamicAnalysis(Sheet sheet, Map<String, Double> cellValues) {
        Sheet tempSheet = sheet.copySheet();

        try {
            for (Map.Entry<String, Double> entry : cellValues.entrySet()) {
                String cellId = entry.getKey();
                String newValue = String.valueOf(entry.getValue());
                Coordinate coordinate = CoordinateUtils.parseCellId(cellId);
                CoordinateUtils.validateCoordinate(tempSheet, coordinate);

                Cell cell = tempSheet.getCell(coordinate);
                if (cell == null) {
                    throw new IllegalArgumentException("Cell " + cellId + " does not exist in the sheet.");
                } else {
                    if (!isNumeric(cell.getOriginalValue())) {
                        throw new IllegalArgumentException("Cell " + cellId + " does not contain a numeric value.");
                    }
                }
                cell.setOriginalValue(newValue);
            }

            tempSheet.updateDependenciesAndInfluences();
            for (Cell c : tempSheet.orderCellsForCalculation()) {
                c.calculateEffectiveValue();
            }

            return dtoFactory.createSheetDTO(tempSheet);
        } catch (Exception e) {
            throw new IllegalStateException("Error during dynamic analysis: " + e.getMessage(), e);
        }
    }

    public SheetDTO performSingleDynamicAnalysis(Sheet sheet, String cellId, String newValue) {
        Sheet tempSheet = sheet.copySheet();

        try {
            Coordinate coordinate = CoordinateUtils.parseCellId(cellId);
            CoordinateUtils.validateCoordinate(tempSheet, coordinate);

            Cell cell = tempSheet.getCell(coordinate);
            if (cell == null) {
                throw new IllegalArgumentException("Cell " + cellId + " does not exist in the sheet.");
            } else {
                if (!isNumeric(cell.getOriginalValue())) {
                    throw new IllegalArgumentException("Cell " + cellId + " does not contain a numeric value.");
                }
                cell.setOriginalValue(newValue);
            }

            tempSheet.updateDependenciesAndInfluences();
            for (Cell c : tempSheet.orderCellsForCalculation()) {
                c.calculateEffectiveValue();
            }

            return dtoFactory.createSheetDTO(tempSheet);
        } catch (Exception e) {
            throw new IllegalStateException("Error during dynamic analysis: " + e.getMessage(), e);
        }
    }

    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
