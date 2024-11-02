package dto.impl;

import dto.api.CellDTO;
import dto.api.SheetDTO;

import java.util.Map;

public class SheetDTOImpl implements SheetDTO {
    private String name;
    private String owner;
    private int version;
    private int rows;
    private int cols;
    private int rowHeight;
    private int colWidth;
    private Map<String, CellDTO> cells;

    public SheetDTOImpl(String name,String owner, int version, int rows, int cols, int rowHeight, int colWidth, Map<String, CellDTO> cells) {
        this.name = name;
        this.owner = owner;
        this.version = version;
        this.rows = rows;
        this.cols = cols;
        this.rowHeight = rowHeight;
        this.colWidth = colWidth;
        this.cells = cells;
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
    public Map<String, CellDTO> getCells() {
        return cells;
    }
}
