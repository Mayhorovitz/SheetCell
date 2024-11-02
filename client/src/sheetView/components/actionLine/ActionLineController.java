package sheetView.components.actionLine;

import dto.api.CellDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sheetView.MainController;

/**
 * Controller for the action line, allowing users to view and edit cell values.
 */
public class ActionLineController {

    @FXML
    private Label selectedCellId;  // Label to display selected cell ID

    @FXML
    private Label originalValueLabel;  // Label to display original value

    @FXML
    private TextField newValueField;  // TextField for editing new value

    @FXML
    private Label lastUpdateCellVersion;  // Label to display last update version

    @FXML
    private Button updateButton;  // Update Cell button

    private MainController mainController;

    private boolean isReadOnly = false;  // Flag to indicate read-only mode

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    public void updateActionLine(CellDTO selectedCellDTO) {
        if (selectedCellDTO != null) {
            selectedCellId.setText(selectedCellDTO.getIdentity());
            originalValueLabel.setText(selectedCellDTO.getOriginalValue());
            newValueField.setText("");
            lastUpdateCellVersion.setText(String.valueOf(selectedCellDTO.getVersion()));
        } else {
            selectedCellId.setText("");
            originalValueLabel.setText("");
            newValueField.setText("");
            lastUpdateCellVersion.setText("N/A");
        }
    }

    @FXML
    private void handleUpdateCell() {
        String newValue = newValueField.getText();
        String selectedCell = selectedCellId.getText();

        if (mainController != null) {
            mainController.handleUpdateCell(newValue, selectedCell);
        }
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
        if (mainController != null) {
            mainController.showErrorAlert("Error: " + message);
        }
    }
}
