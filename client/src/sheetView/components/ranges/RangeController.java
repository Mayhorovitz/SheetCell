package sheetView.components.ranges;

import com.google.gson.Gson;
import dto.api.RangeDTO;
import dto.impl.RangeDTOImpl;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetView.main.SheetViewMainController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Collection;


public class RangeController {

    private SheetViewMainController sheetViewMainController;

    @FXML
    private TextField rangeNameField, rangeCellsField;

    @FXML
    private ListView<String> rangeListView;

    public void setMainController(SheetViewMainController sheetViewMainController) {
        this.sheetViewMainController = sheetViewMainController;
    }

    // Method to handle adding a new range
    @FXML
    public void handleAddRange() {
        boolean isReadOnly = sheetViewMainController.isReadOnly();
        if (!sheetViewMainController.isReadOnly()) {
            String rangeName = rangeNameField.getText();
            String rangeCells = rangeCellsField.getText().toUpperCase();
            String sheetName = sheetViewMainController.getCurrentSheetName();

            // Validate user input
            if (rangeName.isEmpty() || rangeCells.isEmpty()) {
                showErrorAlert("Please enter both a range name and range cells.");
                return;
            }


            HttpUrl url = HttpUrl.parse(Constants.ADD_RANGE);
            if (url == null) {
                showErrorAlert("Invalid URL for adding range.");
                return;
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("sheetName", sheetName)
                    .add("rangeName", rangeName)
                    .add("range", rangeCells)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            // Execute the asynchronous request
            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> showErrorAlert("Error adding range: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String sheetJson = response.body().string();
                        SheetDTOImpl updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);
                        Platform.runLater(() -> {
                            sheetViewMainController.setSheetDTO(updatedSheetDTO, isReadOnly);
                            updateRangeListView();
                            clearInputFields();
                        });
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Unknown error";

                        Platform.runLater(() -> showErrorAlert(errorMessage));
                    }
                }
            });

        }   else {
      showErrorAlert("You do not have permission to add a range.");
    }
    }


    // Method to handle deleting a range
    @FXML
    public void handleDeleteRange() {
        boolean isReadOnly = sheetViewMainController.isReadOnly();
        if (!sheetViewMainController.isReadOnly()) {
        String selectedRange = rangeListView.getSelectionModel().getSelectedItem();
        if (selectedRange == null) {
            showErrorAlert("Please select a range to delete.");
            return;
        }

        String rangeName = selectedRange.split(":")[0];  // Extract the range name
        String sheetName = sheetViewMainController.getCurrentSheetName();


        HttpUrl url = HttpUrl.parse(Constants.DELETE_RANGE);
        if (url == null) {
            showErrorAlert("Invalid URL for deleting range.");
            return;
        }

        // Creating the form body with the necessary parameters
        RequestBody formBody = new FormBody.Builder()
                .add("sheetName", sheetName)
                .add("rangeName", rangeName)
                .build();

        // Creating the POST request
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // Executing the asynchronous request
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showErrorAlert("Error deleting range: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String sheetJson = response.body().string();
                    SheetDTOImpl updatedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);
                    Platform.runLater(() -> sheetViewMainController.setSheetDTO(updatedSheetDTO,isReadOnly));
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";

                    Platform.runLater(() -> showErrorAlert(errorMessage));
                }
            }
        });
        }   else {
            showErrorAlert("You do not have permission to add a range.");
        }
    }


    // Method to handle range selection from the ListView
        @FXML
        public void handleRangeSelection () {
            String selectedRange = rangeListView.getSelectionModel().getSelectedItem();
            if (selectedRange != null) {
                String rangeName = selectedRange.split(":")[0];

                RangeDTO range = sheetViewMainController.getCurrentSheet().getRanges().get(rangeName);
                if (range != null) {
                    Platform.runLater(() -> sheetViewMainController.getSheetController().highlightRange(range));
                }
            }
        }

    // Method to update the ListView with all ranges from the current sheet
    public void updateRangeListView() {
        rangeListView.getItems().clear();
        Collection<RangeDTOImpl> ranges = sheetViewMainController.getCurrentSheet().getRanges().values();

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
