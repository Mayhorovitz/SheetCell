
import cell.api.Cell;
import coordinate.Coordinate;
import engine.api.Engine;
import engine.impl.EngineImpl;
import sheet.api.Sheet;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static coordinate.CoordinateFactory.createCoordinate;

public class ConsoleUI {
    private Engine engine;
    private Scanner scanner;

    public ConsoleUI() {
        this.engine = new EngineImpl();
        this.scanner = new Scanner(System.in);
    }


    public void start() {
        while (true) {
            displayMenu();
            String command = scanner.nextLine();
            processCommand(command);
        }
    }

    public void displayMenu() {
        System.out.println("1. Load File");
        System.out.println("2. Display Spreadsheet");
        System.out.println("3. Display Cell");
        System.out.println("4. Update Cell");
        System.out.println("5. Display Versions");
        System.out.println("6. Exit");
    }

    public void processCommand(String command) {
        switch (command) {
            case "1":
                handleLoadFile();
                break;
            case "2":
                displaySpreadSheet();
                break;
            case "3":
                handleDisplayCell();
                break;

            case "6":
                engine.exit();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command");
        }
    }

    public void handleLoadFile() {
        System.out.println("Enter file path:");
        String filePath = scanner.nextLine();
        try {
            engine.loadFile(filePath);
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    public void displaySpreadSheet() {
        Sheet currentSheet = engine.getCurrentSpreadSheet();
        System.out.println("Spreadsheet version is: " + currentSheet.getVersion());
        String spreadSheetName = currentSheet.getName();
        System.out.println("Spreadsheet name is: " + spreadSheetName);
        printSpreadSheet(engine.getCurrentSpreadSheet());
    }


    public void handleDisplayCell() {
        System.out.println("Enter cell identifier (e.g., A1):");
        String cellId = scanner.nextLine();
        Cell currentCell = engine.getCellInfo(cellId);
        System.out.println("Cell ID: " + cellId);
        System.out.println("Original Value: " + currentCell.getOriginalValue());
        System.out.println("Effective Value: " + currentCell.getEffectiveValue().toString());
        System.out.println("The last modified version of the cell is: " + currentCell.getVersion());

        System.out.print("The dependents are: ");
        List<Cell> dependents = currentCell.getDependsOn();
        String dependentsOutput = (dependents != null && !dependents.isEmpty())
                ? dependents.stream()
                .map(cell -> cell.getCoordinate().toString())
                .collect(Collectors.joining(", "))
                : "None";
        System.out.println(dependentsOutput);

        System.out.print("The references are: ");
        List<Cell> references = currentCell.getInfluencingOn();
        String referencesOutput = (references != null && !references.isEmpty())
                ? references.stream()
                .map(cell -> cell.getCoordinate().toString())
                .collect(Collectors.joining(", "))
                : "None";
        System.out.println(referencesOutput);
    }



    private void printSpreadSheet(Sheet currentSpreadsheet) {
        int rows = currentSpreadsheet.getRows();
        int cols = currentSpreadsheet.getCols();

        printColumnHeaders(cols, currentSpreadsheet);

        for (int row = 0; row < rows; row++) {
            printRow(row, cols, currentSpreadsheet);
        }
    }

    private void printColumnHeaders(int numCols, Sheet currentSpreadsheet) {
        StringBuilder header = new StringBuilder();
        for (int col = 0; col < numCols; col++) {
            if (col > 0) {
                header.append('|');
            }
            int colWidth = currentSpreadsheet.getColWidth();
            header.append(String.format("%" + colWidth + "s", getColumnHeader(col)));
        }
        System.out.println(header.toString());
    }

    private void printRow(int row, int numCols, Sheet currentSpreadsheet) {
        System.out.print(String.format("%02d", row + 1));
        for (int col = 0; col < numCols; col++) {
            if (col > 0) {
                System.out.print('|');
            }
            Coordinate coordinate = createCoordinate(row, col);
            Cell cell = currentSpreadsheet.getCell(coordinate);
            String cellValue;
            if (cell != null) {
                 cellValue = cell.getEffectiveValue().toString();
            }
            else {
                cellValue = " ";
            }

            int colWidth = currentSpreadsheet.getColWidth();
            System.out.print(String.format("%" + colWidth + "s", cellValue));
        }
        System.out.println();
    }

    private String getColumnHeader(int col) {
        StringBuilder sb = new StringBuilder();
        while (col >= 0) {
            sb.insert(0, (char) ('A' + (col % 26)));
            col = col / 26 - 1;
        }
        return sb.toString();
    }
}