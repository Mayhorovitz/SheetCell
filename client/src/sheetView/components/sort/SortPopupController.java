package sheetView.components.sort;

import com.google.gson.Gson;
import dto.api.SheetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import sheetView.UIModel;
import sheetView.components.readOnlyPopup.ReadOnlyPopupController;
import sheetView.components.sheet.SheetController;
import util.http.HttpClientUtil;

import java.io.IOException;

/**
 * Controller for the sort popup, allowing users to sort data within a range.
 */
public class SortPopupController {

    private static final String SERVER_URL = "http://localhost:8080/shticell";

    @FXML
    private TextField rangeTextField;  // TextField for range input
    @FXML
    private TextField columnsTextField;  // TextField for columns input
    @FXML
    private Button sortButton;

    private SheetController sheetController;
    private UIModel uiModel;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
        this.uiModel = sheetController.getUiModel();
    }

    @FXML
    private void handleSort() {
        String range = rangeTextField.getText().toUpperCase();
        String columns = columnsTextField.getText().toUpperCase();

        if (range != null && !range.isEmpty() && columns != null && !columns.isEmpty()) {
            String finalUrl = HttpUrl.parse(SERVER_URL + "/sortSheetRange")
                    .newBuilder()
                    .addQueryParameter("sheetName", sheetController.getUiModel().getSheetName())
                    .addQueryParameter("range", range)
                    .addQueryParameter("columns", columns)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showError("Error sorting sheet: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        SheetDTO sortedSheetDTO = new Gson().fromJson(response.body().string(), SheetDTO.class);
                        Platform.runLater(() -> showSortedSheetPopup(sortedSheetDTO));
                    } else {
                        Platform.runLater(() -> showError("Error sorting sheet: " + response.message()));
                    }
                }
            });
        } else {
            // Handle invalid input
            showError("Please enter valid range and columns.");
        }
    }

    private void showSortedSheetPopup(SheetDTO sortedSheetDTO) {
        try {
            // Open the ReadOnlyPopup to display the sorted sheet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetView/components/readOnlyPopup/readOnlyPopup.fxml"));
            VBox root = loader.load();

            ReadOnlyPopupController popupController = loader.getController();
            popupController.setUiModel(uiModel);
            popupController.setSheetToDisplay(sortedSheetDTO);
            popupController.displaySheet();

            Stage stage = new Stage();
            stage.setTitle("Sorted Sheet - View Only");
            stage.setScene(new Scene(root, 800, 600));  // Set desired size
            stage.show();

            // Close the sort popup
            Stage currentStage = (Stage) sortButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showError("Failed to display sorted sheet: " + e.getMessage());
        }
    }

    // Utility method to show error alerts
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Sort Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
