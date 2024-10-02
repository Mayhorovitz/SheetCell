package javaFX.sort;

import engine.api.Engine;
import javaFX.main.UIModel;
import javaFX.readOnlyPopup.ReadOnlyPopupController;
import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sheet.api.Sheet;

import java.io.IOException;

public class SortPopupController {

    @FXML
    private TextField rangeTextField;  // TextField for range input
    @FXML
    private TextField columnsTextField;  // TextField for columns input
    @FXML
    private Button sortButton;  // Sort button

    private SheetController sheetController;
    private Engine engine;
    private UIModel uiModel;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
        this.engine = sheetController.getEngine();
        this.uiModel = sheetController.getUiModel();
    }

    @FXML
    private void handleSort() {
        String range = rangeTextField.getText();
        String columns = columnsTextField.getText();

        if (range != null && !range.isEmpty() && columns != null && !columns.isEmpty()) {
            String[] columnArray = columns.split(",");
            Sheet sortedSheet = engine.sortSheetRangeByColumns(range, columnArray);

            // Open the ReadOnlyPopup to display the sorted sheet
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaFX/readOnlyPopup/readOnlyPopup.fxml"));
                VBox root = loader.load();

                ReadOnlyPopupController popupController = loader.getController();
                popupController.setEngine(engine);
                popupController.setUiModel(uiModel);  // Pass the uiModel
                popupController.setSheetToDisplay(sortedSheet);  // Set the sorted sheet to display
                popupController.displaySheet();

                Stage stage = new Stage();
                stage.setTitle("Sorted Sheet - View Only");
                stage.setScene(new Scene(root, 800, 600));  // Set desired size
                stage.show();

                // Close the sort popup
                Stage currentStage = (Stage) sortButton.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Handle invalid input
            System.out.println("Please enter valid range and columns.");
        }
    }
}
