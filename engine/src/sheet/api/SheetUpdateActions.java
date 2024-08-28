package sheet.api;

public interface SheetUpdateActions {
    Sheet updateCellValueAndCalculate(String cellId, String value);
}
