package engine.file;

import cell.api.Cell;
import cell.impl.CellImpl;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import coordinate.CoordinateUtils;
import generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;

import java.io.IOException;
import java.io.InputStream;

public class FileLoader {

    private static final int MAX_ROWS = 50;
    private static final int MAX_COLS = 20;

    public Sheet loadSheetFromXML(InputStream inputStream, String owner) throws IOException {
        STLSheet stlSheet = loadXMLFile(inputStream);
        validateSheet(stlSheet);
        return convertSTLSheetToSheet(stlSheet, owner);
    }

    private STLSheet loadXMLFile(InputStream inputStream) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (STLSheet) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new IOException("An error occurred while loading the XML file.", e);
        }
    }

    private void validateSheet(STLSheet sheet) {
        STLLayout layout = sheet.getSTLLayout();
        if (layout.getRows() < 1 || layout.getRows() > MAX_ROWS ||
                layout.getColumns() < 1 || layout.getColumns() > MAX_COLS) {
            throw new IllegalArgumentException("Invalid sheet layout: Rows and columns must be within allowed range.");
        }

        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            String column = cell.getColumn();
            int columnIndex = CoordinateUtils.convertColumnToIndex(column);

            if (row < 1 || row > layout.getRows() || columnIndex < 1 || columnIndex > layout.getColumns()) {
                throw new IllegalArgumentException("Invalid cell location: Cell at row " + row + ", column " + column + " is out of bounds.");
            }
        }
    }
    //  Converts an STLSheet object into a Sheet object.
    private Sheet convertSTLSheetToSheet(STLSheet stlSheet, String owner) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());

        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());

        // Process ranges
        if (stlSheet.getSTLRanges() != null && stlSheet.getSTLRanges().getSTLRange() != null) {
            for (STLRange range : stlSheet.getSTLRanges().getSTLRange()) {
                String name = range.getName();
                String from = range.getSTLBoundaries().getFrom();
                String to = range.getSTLBoundaries().getTo();
                newSheet.addRange(name, from + ".." + to);
            }
        }

        // Process cells
        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            String originalValue = stlCell.getSTLOriginalValue();
            int row = stlCell.getRow();
            String column = stlCell.getColumn();
            int col = CoordinateUtils.convertColumnToIndex(column);

            Coordinate coordinate = new CoordinateImpl(row, col);
            Cell cell = new CellImpl(row, col, originalValue, 1, owner, newSheet);
            newSheet.addCell(coordinate, cell);
        }

        newSheet.updateDependenciesAndInfluences();

        // Calculate the effective values for each cell
        for (Cell cell : newSheet.orderCellsForCalculation()) {
            cell.calculateEffectiveValue();
            newSheet.addCellThatChanged(cell);
        }

        return newSheet;
    }
}
