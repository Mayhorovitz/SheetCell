package sheetView.components.ranges;

import com.google.gson.Gson;
import dto.api.RangeDTO;
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
        String sheetName = mainController.getSheetController().getCurrentSheetName();

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
        String sheetName = mainController.getSheetController().getCurrentSheetName();
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
    public void handleRangeSelection() {
        String selectedRange = rangeListView.getSelectionModel().getSelectedItem();
        String sheetName = mainController.getSheetController().getCurrentSheetName();
        if (selectedRange != null) {
            String rangeName = selectedRange.split(":")[0];

            String finalUrl = HttpUrl.parse(SERVER_URL + "/getAllRanges")
                    .newBuilder()
                    .addQueryParameter("sheetName", sheetName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showErrorAlert("Error retrieving range: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        RangeDTO[] ranges = new Gson().fromJson(response.body().string(), RangeDTO[].class);
                        for (RangeDTO range : ranges) {
                            if (range.getName().equals(rangeName)) {
                                Platform.runLater(() -> mainController.getSheetController().highlightRange(range));
                            }
                        }
                    } else {
                        Platform.runLater(() -> showErrorAlert("Error retrieving range: " + response.message()));
                    }
                }
            });
        }
    }

    // Method to update the ListView with all ranges from the current sheet
    public void updateRangeListView() {
        rangeListView.getItems().clear();  // Clear existing items
        String sheetName = mainController.getSheetController().getCurrentSheetName();

        String finalUrl = HttpUrl.parse(SERVER_URL + "/getAllRanges")
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Error retrieving ranges: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    RangeDTO[] ranges = new Gson().fromJson(response.body().string(), RangeDTO[].class);
                    Platform.runLater(() -> {
                        for (RangeDTO range : ranges) {
                            rangeListView.getItems().add(range.getName() + ": " + range.getFrom() + " to " + range.getTo());
                        }
                    });
                } else {
                    Platform.runLater(() -> showErrorAlert("Error retrieving ranges: " + response.message()));
                }
            }
        });
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
