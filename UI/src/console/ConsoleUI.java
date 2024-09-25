package console;

import cell.api.Cell;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import engine.api.Engine;
import engine.exceptions.InvalidFileFormatException;
import engine.exceptions.InvalidSheetLayoutException;
import engine.exceptions.NoFileLoadedException;
import engine.impl.EngineImpl;
import sheet.api.Sheet;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;


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
        System.out.println("7. Save System State");
        System.out.println("8. Load System State");
    }

    public void processCommand(String command) {
        try {
        switch (command) {
            case "1":
                handleLoadFile();
                break;
            case "2":
                handleDisplaySheet();
                break;
            case "3":
                handleDisplayCell();
                break;
            case "4":
                handleUpdateCell();
                break;
            case "5":
                handleDisplayVersions();
                break;
            case "6":
                engine.exit();
                System.exit(0);
                break;
            case "7":
                handleSaveSystemState();
                break;

            case "8":
                handleLoadSystemState();
                break;

            default:
                System.out.println("Please enter a number between 1 and 6");
        }
    } catch (NoFileLoadedException e) {
        System.out.println("Error: " + e.getMessage());
            handleLoadFile();
        } catch (Exception e) {
        System.out.println("An unexpected error occurred: " + e.getMessage());
    }
    }

    public void handleLoadFile() {
        System.out.println("Enter file path:");
        String filePath = scanner.nextLine();
        try {
            engine.loadFile(filePath);
            System.out.println("File loaded successfully.");
        } catch (InvalidFileFormatException | InvalidSheetLayoutException e) {
            System.out.println("Error loading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private void handleDisplaySheet() {
        try {
            displaySheet(engine.getCurrentSheet());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void displaySheet(Sheet sheet) {
        System.out.println("Sheet Version: " + sheet.getVersion());
        System.out.println("Sheet Name: " + sheet.getName());

        int rowCount = sheet.getRows();
        int columnCount = sheet.getCols();
        int columnWidth = sheet.getColWidth();
        int rowHeight = sheet.getRowHeight();

        printColumnHeaders(columnCount, columnWidth);

        for (int row = 1; row <= rowCount; row++) {
            printRow(row, columnCount, columnWidth, sheet);
            printRowSpacing(rowHeight, columnCount, columnWidth); // Print the spacing rows after each data row
        }
    }

    private void printColumnHeaders(int columnCount, int columnWidth) {
        System.out.print("  |");
        for (int col = 0; col < columnCount; col++) {
            String columnHeader = String.valueOf((char) ('A' + col));
            int padding = (columnWidth - columnHeader.length()) / 2;
            System.out.print(" ".repeat(Math.max(0, padding)) + columnHeader + " ".repeat(Math.max(0, columnWidth - padding - columnHeader.length())));
            if (col < columnCount - 1) {
                System.out.print("|");
            }
        }
        System.out.println("|");
    }

    private void printRow(int row, int columnCount, int columnWidth, Sheet sheet) {
        System.out.printf("%02d|", row);
        for (int col = 0; col < columnCount; col++) {
            Coordinate cellCoordinate = new CoordinateImpl(row, col + 1);
            Cell cell = sheet.getActiveCells().get(cellCoordinate);
            if (cell != null) {
                System.out.printf("%-" + columnWidth + "s", cell.getEffectiveValue().getValue());
            } else {
                System.out.printf("%-" + columnWidth + "s", " ");
            }
            if (col < columnCount - 1) {
                System.out.print("|");
            }
        }
        System.out.println("|");
    }

    private void printRowSpacing(int rowHeight, int columnCount, int columnWidth) {
        for (int i = 1; i < rowHeight; i++) {
            System.out.print("  |");
            for (int col = 0; col < columnCount; col++) {
                System.out.print(" ".repeat(columnWidth));
                if (col < columnCount - 1) {
                    System.out.print("|");
                }
            }
            System.out.println("|");
        }
    }


    private void handleDisplayCell() {
        System.out.println("Please provide the cell ID (A1):");
        String cellId = scanner.nextLine().toUpperCase();

        try {
            Cell selectedCell = engine.getCellInfo(cellId);
            printGenerlCellInformation(selectedCell);
            System.out.println("Last modified in version: " + selectedCell.getVersion());

            printDependents(selectedCell);
            printInfluences(selectedCell);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("An error occurred while displaying cell details: " + ex.getMessage());
        }
    }

    private void printDependents(Cell cell) {
        System.out.print("Dependencies: ");
        List<Cell> dependents = cell.getDependsOn();
        if (dependents != null && !dependents.isEmpty()) {
            for (int i = 0; i < dependents.size(); i++) {
                System.out.print(dependents.get(i).getCoordinate().toString());
                if (i < dependents.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        } else {
            System.out.println("None");
        }
    }

    private void printInfluences(Cell cell) {
        System.out.print("Influences: ");
        List<Cell> influences = cell.getInfluencingOn();
        if (influences != null && !influences.isEmpty()) {
            for (int i = 0; i < influences.size(); i++) {
                System.out.print(influences.get(i).getCoordinate().toString());
                if (i < influences.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        } else {
            System.out.println("None");
        }
    }

    private void printGenerlCellInformation(Cell cell){

        System.out.println("Cell ID: " + cell.getCoordinate());
        System.out.println("Original Value: " + cell.getOriginalValue());
        System.out.println("Effective Value: " + cell.getEffectiveValue().toString());
    }

    private void handleUpdateCell() {
        System.out.println("Please provide the cell ID (A1):");
        String coordinate = scanner.nextLine().toUpperCase();

        try {
            Cell currentCell = engine.getCellInfo(coordinate);
            if (currentCell != null) {
                printGenerlCellInformation(currentCell);
            }
            System.out.print("Enter new value: ");
            String newValue = scanner.nextLine().trim();
            engine.updateCell(coordinate, newValue);
            System.out.println("Cell updated successfully.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println("Error updating cell: " + e.getMessage());
        }
    }
    private void handleDisplayVersions() {
        try {
            System.out.println("Version | Changed Cells Count");
            System.out.println("----------------------------");
            for (int i = 1; i <= engine.getCurrentSheetVersion(); i++) {
                try {
                    System.out.printf("%7d | %17d%n", i, engine.getSheetByVersion(i).getCellsThatHaveChanged().size());
                } catch (IllegalArgumentException e) {
                    System.out.printf("%7d | %17s%n", i, "Invalid Version");
                }
            }
            while (true) {
                System.out.println("Enter the version number to view, or 'q' to quit:");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("q")) {
                    break;
                }

                try {
                    int versionNumber = Integer.parseInt(input);
                    if (versionNumber > 0 && versionNumber <= engine.getCurrentSheetVersion()) {
                        displaySheet(engine.getSheetByVersion(versionNumber));
                    } else {
                        System.out.println("Invalid version number. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid version number or 'q' to quit.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }
    private void handleSaveSystemState() {
        System.out.println("Enter the file path to save the system state:");
        String filePath = scanner.nextLine().trim();
        try {
            engine.saveSystemState(filePath);
            System.out.println("System state saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving system state: " + e.getMessage());
        }
    }
    private void handleLoadSystemState() {
        System.out.println("Enter the file path to load the system state from:");
        String filePath = scanner.nextLine().trim();
        try {
            engine.loadSystemState(filePath);
            System.out.println("System state loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading system state: " + e.getMessage());
        }
    }

}
