package sheetView.components.actionLine;

import dto.impl.CellDTOImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sheetView.SheetViewMainController;

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

    @FXML
    private ComboBox<String> versionSelector;  // ComboBox for selecting version

    private SheetViewMainController sheetViewMainController;

    private boolean isReadOnly = false;  // Flag to indicate read-only mode

    public void setMainController(SheetViewMainController sheetViewMainController) {
        this.sheetViewMainController = sheetViewMainController;
    }

    public void updateActionLine(CellDTOImpl selectedCell, String cellId) {
        if (selectedCell != null) {
            selectedCellId.setText(selectedCell.getIdentity());
            originalValueLabel.setText(selectedCell.getOriginalValue());
            newValueField.setText("");
            lastUpdateCellVersion.setText(String.valueOf(selectedCell.getVersion()) + " " + selectedCell.getChangedBy());
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

        if (sheetViewMainController != null) {
            sheetViewMainController.handleUpdateCell(newValue, selectedCell);
        }
    }

    @FXML
    private void handleVersionSelection() {
        String selectedVersion = versionSelector.getValue();

        if (sheetViewMainController != null) {
            sheetViewMainController.handleVersionSelection(selectedVersion);
        }
    }

    public void setVersionSelectorItems(int currentVersion) {
        versionSelector.getItems().clear();
        for (int i = 1; i <= currentVersion; i++) {
            versionSelector.getItems().add(String.valueOf(i));
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

        // Set listener for version selection changes
        if (versionSelector != null) {
            versionSelector.setOnAction(event -> handleVersionSelection());
        }
    }

    // Function to display an error (if needed)
    private void showErrorAlert(String message) {
        if (sheetViewMainController != null) {
            sheetViewMainController.showErrorAlert("Error: " + message);
        }
    }
}
