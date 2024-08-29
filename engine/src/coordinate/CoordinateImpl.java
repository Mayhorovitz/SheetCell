package coordinate;

import java.io.Serializable;

public class CoordinateImpl implements Coordinate  , Serializable  {
    private final int row;
    private final int column;

//constructors
    public CoordinateImpl(int row, int column) {
        this.row = row;
        this.column = column;
    }
    //getters
    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }
    //Converts the column number to its corresponding letter
    @Override
    public String toString() {
        StringBuilder columnLetter = new StringBuilder();
        int tempColumn = column;

        while (tempColumn > 0) {
            tempColumn--;
            char letter = (char) ('A' + (tempColumn % 26));
            columnLetter.insert(0, letter);
            tempColumn = tempColumn / 26;
        }

        return columnLetter.toString() + row;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateImpl that = (CoordinateImpl) o;

        if (row != that.row) return false;
        return column == that.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }
}
