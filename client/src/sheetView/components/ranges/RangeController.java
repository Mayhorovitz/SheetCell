package sheetView.components.ranges;

import dto.api.RangeDTO;
import dto.impl.RangeDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.MainController;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Collection;

/**
 * Controller for managing ranges in the spreadsheet.
 */
public class RangeController {
    private static final String SERVER_URL = "http://localhost:8080/shticell";

    private MainController mainController;

    @FXML
    private TextField rangeNameField, rangeCellsField;

    @FXML
    private ListView<String> rangeListView;  // ListView to display available ranges

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Method to handle adding a new range
    @FXML
    public void handleAddRange() {
        String rangeName = rangeNameField.getText();
        String rangeCells = rangeCellsField.getText().toUpperCase();
        String sheetName = mainController.getCurrentSheetName();

        // Validate user input
        if (rangeName.isEmpty() || rangeCells.isEmpty()) {
            showErrorAlert("Please enter both a range name and range cells.");
            return;
        }

        String finalUrl = HttpUrl.parse(SERVER_URL + "/addRange")
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("rangeName", rangeName)
                .addQueryParameter("range", rangeCells)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Error adding range: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        updateRangeListView();
                        clearInputFields();
                    });
                } else {
                    Platform.runLater(() -> showErrorAlert("Error adding range: " + response.message()));
                }
            }
        });
    }

    // Method to handle deleting a range
    @FXML
    public void handleDeleteRange() {
        String selectedRange = rangeListView.getSelectionModel().getSelectedItem();
        String sheetName = mainController.getCurrentSheetName();
        if (selectedRange == null) {
            showErrorAlert("Please select a range to delete.");
            return;
        }

        String rangeName = selectedRange.split(":")[0];  // Extract the range name

        String finalUrl = HttpUrl.parse(SERVER_URL + "/deleteRange")
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Error deleting range: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> updateRangeListView());
                } else {
                    Platform.runLater(() -> showErrorAlert("Error deleting range: " + response.message()));
                }
            }
        });
    }


    // Method to handle range selection from the ListView
        @FXML
        public void handleRangeSelection () {
            String selectedRange = rangeListView.getSelectionModel().getSelectedItem();
            if (selectedRange != null) {
                String rangeName = selectedRange.split(":")[0];

                // חיפוש הטווח מתוך ה- SheetDTO והדגשתו
                RangeDTO range = mainController.getCurrentSheet().getRanges().get(rangeName);
                if (range != null) {
                    Platform.runLater(() -> mainController.getSheetController().highlightRange(range));
                }
            }
        }

    // Method to update the ListView with all ranges from the current sheet
    public void updateRangeListView() {
        rangeListView.getItems().clear();  // Clear existing items
        Collection<RangeDTOImpl> ranges = mainController.getCurrentSheet().getRanges().values();

        for (RangeDTO range : ranges) {
            rangeListView.getItems().add(range.getName() + ": " + range.getFrom() + " to " + range.getTo());
        }
    }

    // Utility method to show error alerts
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Range Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility method to clear input fields
    private void clearInputFields() {
        rangeNameField.clear();
        rangeCellsField.clear();
    }
}
