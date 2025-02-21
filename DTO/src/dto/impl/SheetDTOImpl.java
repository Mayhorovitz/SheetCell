package dto.impl;

import dto.api.SheetDTO;

import java.util.Map;

public class SheetDTOImpl implements SheetDTO {
    private final String name;
    private final String owner;
    private final int version;
    private final int rows;
    private final int cols;
    private final int rowHeight;
    private final int colWidth;
    private final Map<String, CellDTOImpl> cells;
    private final Map<String, RangeDTOImpl> ranges;


    public SheetDTOImpl(String name,String owner, int version, int rows, int cols, int rowHeight, int colWidth, Map<String, CellDTOImpl> cells, Map<String, RangeDTOImpl> ranges) {
        this.name = name;
        this.owner = owner;
        this.version = version;
        this.rows = rows;
        this.cols = cols;
        this.rowHeight = rowHeight;
        this.colWidth = colWidth;
        this.cells = cells;
        this.ranges = ranges;
    }

    // Getters
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    @Override
    public int getRowHeight() {
        return rowHeight;
    }

    @Override
    public int getColWidth() {
        return colWidth;
    }

    @Override
    public Map<String, RangeDTOImpl>  getRanges() {
        return ranges;
    }
    @Override
    public Map<String, CellDTOImpl> getCells() {
        return cells;
    }
}
