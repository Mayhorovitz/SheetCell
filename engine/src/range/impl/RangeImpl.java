package range.impl;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import coordinate.CoordinateFactory;
import range.api.Range;
import sheet.api.Sheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RangeImpl implements Range, Serializable {

    private final String name;
    private final Coordinate fromCoordinate;
    private final Coordinate toCoordinate;
    private final List<Cell> cells;

    public RangeImpl(String name, String startCellId, String endCellId, Sheet sheet) {
        this.name = name;
        this.fromCoordinate = CoordinateFactory.createCoordinate(startCellId);
        this.toCoordinate = CoordinateFactory.createCoordinate(endCellId);
        this.cells = new ArrayList<>();

        // Validate range and load cells
        validateAndLoadRange(sheet);
    }

    private void validateAndLoadRange(Sheet sheet) {
        // Get row and column limits of the range
        int startRow = Math.min(fromCoordinate.getRow(), toCoordinate.getRow());
        int endRow = Math.max(fromCoordinate.getRow(), toCoordinate.getRow());
        int startCol = Math.min(fromCoordinate.getColumn(), toCoordinate.getColumn());
        int endCol = Math.max(fromCoordinate.getColumn(), toCoordinate.getColumn());

        // Load all cells in the range and ensure no gaps exist
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Coordinate coordinate = CoordinateFactory.createCoordinate(row, col);
                Cell cell = sheet.getCell(coordinate);

                if (cell == null) {
                    cell = new CellImpl(row, col, "", sheet.getVersion(), sheet);
                    sheet.addCell(coordinate, cell);
                }
                cells.add(cell);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFrom() {
        return fromCoordinate.toString();
    }

    @Override
    public String getTo() {
        return toCoordinate.toString();
    }

    @Override
    public List<Cell> getCells() {
        return cells;
    }
}
