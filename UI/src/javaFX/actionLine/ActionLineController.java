package javaFX.actionLine;

import cell.api.Cell;
import engine.api.Engine;
import javaFX.main.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ActionLineController {

    private Engine engine;

    @FXML
    private Label selectedCellId;

    @FXML
    private Label originalValueLabel;

    @FXML
    private TextField newValueField;

    @FXML
    private Label lastUpdateCellVersion;

    @FXML
    private Button updateButton;

    private MainController mainController;

    private boolean isReadOnly = false;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void updateActionLine(Cell selectedCell, String cellId) {
        if (selectedCell != null) {
            selectedCellId.setText(selectedCell.getCoordinate().toString());
            originalValueLabel.setText(selectedCell.getOriginalValue());
            newValueField.setText("");
            lastUpdateCellVersion.setText(String.valueOf(selectedCell.getVersion()));
        } else {
            selectedCellId.setText(cellId);
            originalValueLabel.setText("");
            newValueField.setText("");
            lastUpdateCellVersion.setText("N/A");
        }
    }

    @FXML
    private void handleUpdateCell() {
        String newValue = newValueField.getText();
        String selectedCell = selectedCellId.getText();

        mainController.handleUpdateCell(newValue, selectedCell);
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        if (updateButton != null) {
            updateButton.setDisable(readOnly);
        }
        if (newValueField != null) {
            newValueField.setEditable(!readOnly);
        }
    }

    @FXML
    private void initialize() {
        // Update button and field based on read-only status
        if (updateButton != null) {
            updateButton.setDisable(isReadOnly);
        }
        if (newValueField != null) {
            newValueField.setEditable(!isReadOnly);
        }
    }

    // Function to display an error (if needed)
    private void showErrorAlert(String message) {
        mainController.showErrorAlert("Error: " + message);
    }
}
