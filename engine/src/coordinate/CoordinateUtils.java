package coordinate;


import sheet.api.Sheet;

public class CoordinateUtils {

    public static Coordinate parseCellId(String cellId) {
        int row = extractRowFromCoordinate(cellId);
        int column = extractColumnFromCoordinate(cellId);
        return new CoordinateImpl(row, column);
    }

    public static int extractRowFromCoordinate(String coordinateString) {
        String rowPart = coordinateString.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    public static int extractColumnFromCoordinate(String coordinateString) {
        String columnPart = coordinateString.replaceAll("[^A-Za-z]", "");
        return convertColumnToIndex(columnPart);
    }

    public static int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

    public static void validateCoordinate(Sheet sheet, Coordinate coordinate) {
        if (coordinate.getRow() < 1 || coordinate.getRow() > sheet.getRows() ||
                coordinate.getColumn() < 1 || coordinate.getColumn() > sheet.getCols()) {
            throw new IllegalArgumentException("Cell location " + coordinate + " is out of bounds.");
        }
    }
}
