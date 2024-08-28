package engine.impl;


import cell.api.EffectiveValue;
import cell.impl.CellImpl;
import expression.api.Expression;
import generated.*;
import cell.api.Cell;
import engine.api.Engine;
import sheet.api.Sheet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import coordinate.Coordinate;
import coordinate.CoordinateFactory;
import sheet.api.SheetReadActions;
import sheet.impl.SheetImpl;

import java.io.File;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static coordinate.CoordinateFactory.createCoordinate;
import static expression.parser.FunctionParser.parseExpression;


public class EngineImpl implements Engine {

    public static final int MAX_ROWS = 50;
    public static final int MAX_COLS = 20;
    public static final int LOAD_VERSION = 0;

    private Map<Integer, Sheet> allSheets;
    int currentSheetVersion = LOAD_VERSION;

    @Override
    public void loadFile(String filePath) throws Exception {
        STLSheet loadedSheetFromXML = loadXMLFile(filePath);
        allSheets = new HashMap<>();
        Sheet currentSheet = STLSheetToSheet(loadedSheetFromXML);
        currentSheet.setSheetVersion(LOAD_VERSION);


        allSheets.put(LOAD_VERSION, currentSheet);
    }

    public STLSheet loadXMLFile(String filePath) throws Exception {
        File xmlFile = new File(filePath);
        //check if file exist
        if (!xmlFile.exists() || !xmlFile.isFile()) {
            System.out.println("File does not exist or is not a valid file.");
            return null;
        }
        //check if file is an XML file
        if (!filePath.endsWith(".xml")) {
            System.out.println("The file is not an XML file.");
            return null;
        }

        try {

            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            STLSheet sheet = (STLSheet) unmarshaller.unmarshal(xmlFile);

            if (!validateSheet(sheet)) {
                System.out.println("The XML file is invalid according to the application's rules.");
                return null;
            }

            System.out.println("The XML file has been successfully loaded into the system.");
            return sheet;

        } catch (JAXBException e) {
            System.out.println("An error occurred while loading the XML file.");
            e.printStackTrace();
        }
        return null;
    }


    private boolean validateSheet(STLSheet sheet) {
        STLLayout layout = sheet.getSTLLayout();
        if (layout.getRows() < 1 || layout.getRows() > MAX_ROWS ||
                layout.getColumns() < 1 || layout.getColumns() > MAX_COLS) {
            System.out.println("Invalid sheet layout: Rows and columns must be within allowed range.");
            return false;
        }
        for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
            int row = cell.getRow();
            String column = cell.getColumn();
            int columnIndex = convertColumnToIndex(column);

            if (row < 1 || row > layout.getRows() || columnIndex < 1 || columnIndex > layout.getColumns()) {
                System.out.println("Invalid cell location: Cell at row " + row + ", column " + column + " is out of bounds.");
                return false;
            }

        }

        return true;
    }


    private int convertColumnToIndex(String column) {
        int result = 0;
        for (char c : column.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }


    private Sheet STLSheetToSheet(STLSheet stlSheet) {
        Sheet newSheet = new SheetImpl();
        newSheet.setName(stlSheet.getName());

        STLLayout layout = stlSheet.getSTLLayout();
        newSheet.setRows(layout.getRows());
        newSheet.setCols(layout.getColumns());
        STLSize size = layout.getSTLSize();
        newSheet.setRowHeight(size.getRowsHeightUnits());
        newSheet.setColWidth(size.getColumnWidthUnits());


        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {

            String originalValue = stlCell.getSTLOriginalValue();
            int row = stlCell.getRow();
            String column = stlCell.getColumn();
            int col = convertColumnToIndex(column);

            Coordinate coordinate = createCoordinate(row, col);
            Cell cell = new CellImpl(row, col, originalValue, 0, newSheet);
            newSheet.addCell(coordinate, cell);

        }
    // עדכון תלויות והשפעות אחרי שכל התאים נוספו
    newSheet.updateDependenciesAndInfluences();

    // חישוב הערכים האפקטיביים לפי סדר החישוב הנכון
    for (Cell cell : newSheet.orderCellsForCalculation()) {
        cell.calculateEffectiveValue();
        newSheet.addCellThatChanged(cell.getCoordinate());
    }
        return newSheet;
    }

    public void updateCell(String cellId, String newValue){
        Sheet sheet = getCurrentSpreadSheet();
        Sheet newSheet =  sheet.updateCellValueAndCalculate(cellId, newValue);
        allSheets.put(LOAD_VERSION, newSheet);

    }
    @Override
    public Sheet getCurrentSpreadSheet(){
        return allSheets.get(LOAD_VERSION);
    }
    @Override
    public Cell getCellInfo(String cellIdentifier){
        Coordinate cellCoordinate = createCoordinate(cellIdentifier);
        Cell cell = allSheets.get(currentSheetVersion).getCell(cellCoordinate);
        return cell;
    }
    @Override
    public void exit() {
        System.exit(0);
    }
}

