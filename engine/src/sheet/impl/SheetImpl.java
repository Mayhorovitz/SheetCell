package sheet.impl;

import cell.impl.CellImpl;
import sheet.api.Sheet;
import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SheetImpl implements Sheet {
    private String name;
    private int version;
    private int rows;
    private int cols;
    private int rowHeight;
    private int colWidth;
    private Map<Coordinate, Cell> activeCells;

    public SheetImpl() {
        this.activeCells = new HashMap<>();
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
    void setSheetVersion(int loadVersion){
        this.version = loadVersion;
    }
    @Override
    public void setRows(int rows) {
        this.rows = rows;
    }
    @Override
    public void setCols(int cols) {
        this.cols = cols;
    }
    @Override
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }
    @Override
    public void setColWidth(int colWidth) {
        this.colWidth = colWidth;
    }
    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int getVersion() {

        return 0;
    }

    @Override
    public Cell getCell(int row, int column) {
        return activeCells.get(CoordinateFactory.createCoordinate(row, column));
    }
    public void addCell(Coordinate coordinate, Cell cell) {
        activeCells.put(coordinate, cell);
    }

    @Override
    public Sheet updateCellValueAndCalculate(int row, int column, String value) {

        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);

        SheetImpl newSheetVersion = copySheet();
        Cell newCell = new CellImpl(row, column, value, newSheetVersion.getVersion() + 1, newSheetVersion);
        newSheetVersion.activeCells.put(coordinate, newCell);

        try {
            List<Cell> cellsThatHaveChanged =
                    newSheetVersion
                            .orderCellsForCalculation()
                            .stream()
                            .filter(Cell::calculateEffectiveValue)
                            .collect(Collectors.toList());

            // successful calculation. update sheet and relevant cells version
            // int newVersion = newSheetVersion.increaseVersion();
            // cellsThatHaveChanged.forEach(cell -> cell.updateVersion(newVersion));

            return newSheetVersion;
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            return this;
        }
    }

    private List<Cell> orderCellsForCalculation() {
        // data structure 1 0 1: Topological sort...
        // build graph from the cells. each cell is a node. each cell that has ref(s) constitutes an edge
        // handle case of circular dependencies -> should fail
        return null;
    }

    private SheetImpl copySheet() {
        // lots of options here:
        // 1. implement clone all the way (yac... !)
        // 2. implement copy constructor for CellImpl and SheetImpl

        // 3. how about serialization ?
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (SheetImpl) ois.readObject();
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            return this;
        }
    }
}

