
import cell.api.Cell;
import cell.api.EffectiveValue;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import engine.api.Engine;
import engine.exceptions.*;
import engine.impl.EngineImpl;
import sheet.api.Sheet;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


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
            printSheet(engine.getCurrentSheet());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void handleDisplayCell() {
        System.out.println("Enter cell identifier (e.g., A1):");
        String coordinate = scanner.nextLine().toUpperCase();
        try {
            Cell currentCell = engine.getCellInfo(coordinate);
            printGenerlCellInformation(currentCell);
            System.out.println("The last modified version of the cell is: " + currentCell.getVersion());

            System.out.print("Dependents on: ");
            List<Cell> dependents = currentCell.getDependsOn();
            String dependentsOutput = (dependents != null && !dependents.isEmpty())
                    ? dependents.stream()
                    .map(cell -> cell.getCoordinate().toString())
                    .collect(Collectors.joining(", "))
                    : "None";
            System.out.println(dependentsOutput);

            System.out.print("Influencing on: ");
            List<Cell> references = currentCell.getInfluencingOn();
            String referencesOutput = (references != null && !references.isEmpty())
                    ? references.stream()
                    .map(cell -> cell.getCoordinate().toString())
                    .collect(Collectors.joining(", "))
                    : "None";
            System.out.println(referencesOutput);

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println("Error display cell: " + e.getMessage());
        }

    }
    private void printGenerlCellInformation(Cell cell){

        System.out.println("Cell ID: " + cell.getCoordinate());
        System.out.println("Original Value: " + cell.getOriginalValue());
        System.out.println("Effective Value: " + cell.getEffectiveValue().toString());
    }
    private void printSheet(Sheet sheet) {
        System.out.println("Sheet version is: " + sheet.getVersion());
        System.out.println("Sheet name is: " + sheet.getName());
        int numRows = sheet.getRows();
        int numCols = sheet.getCols();
        int widthCol = sheet.getColWidth();
        int heightRow = sheet.getRowHeight();

        System.out.print("  ");
        System.out.print("|");
        for (int col = 0; col < numCols; col++) {
            String header = String.valueOf((char) ('A' + col));
            int padding = (widthCol - header.length()) / 2; // Calculate padding for centering
            System.out.print(" ".repeat(Math.max(0, padding))); // Add left padding
            System.out.print(header); // Print the column letter
            System.out.print(" ".repeat(Math.max(0, widthCol - padding - header.length()))); // Add right padding
            if (col < numCols - 1) {
                System.out.print("|");
            }
        }
        System.out.println("|"); // Add separator after last column

        // Print each row
        for (int row = 1; row <= numRows; row++) {
            System.out.printf("%02d", row); // Row number
            System.out.print("|");

            for (int col = 0; col < numCols; col++) {
                Coordinate cellID = new CoordinateImpl(row, col + 1);
                Cell currentCell = sheet.getActiveCells().get(cellID);
                if (currentCell == null) {
                    System.out.printf("%-" + widthCol + "s", ' ');
                } else {
                    EffectiveValue cellContent = currentCell.getEffectiveValue(); // Retrieve cell content
                    System.out.printf("%-" + widthCol + "s", cellContent.getValue());
                }

                if (col < numCols - 1) {
                    System.out.print("|");
                }
            }
            System.out.println("|");


            for (int h = 1; h < heightRow; h++) {
                System.out.print("  ");
                System.out.print("|");
                for (int col = 0; col < numCols; col++) {
                    System.out.print(" ".repeat(widthCol));
                    if (col < numCols - 1) {
                        System.out.print("|");
                    }
                }
                System.out.println("|");
            }
        }
    }

    private void handleUpdateCell() {
        System.out.println("Enter cell identifier (e.g., A1):");
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
                        printSheet(engine.getSheetByVersion(versionNumber));
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
