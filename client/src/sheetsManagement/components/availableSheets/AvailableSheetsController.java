package sheetsManagement.components.availableSheets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sheetsManagement.SheetsManagementController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class AvailableSheetsController {

    @FXML
    private TableView<SheetSummaryDTO> availableSheetsTable;
    @FXML
    private TableColumn<SheetSummaryDTO, String> sheetNameColumn;
    @FXML
    private TableColumn<SheetSummaryDTO, String> ownerColumn;
    @FXML
    private TableColumn<SheetSummaryDTO, String> sheetSizeColumn;
    @FXML
    private TableColumn<SheetSummaryDTO, String> permissionTypeColumn;

    private SheetsManagementController mainController;

    private SheetSummaryDTO selectedSheet;

    // Initializes the columns and sets listeners for table width and selection changes
    @FXML
    public void initialize() {
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        sheetSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));

        availableSheetsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            sheetNameColumn.setPrefWidth(tableWidth * 0.25);
            ownerColumn.setPrefWidth(tableWidth * 0.25);
            sheetSizeColumn.setPrefWidth(tableWidth * 0.25);
            permissionTypeColumn.setPrefWidth(tableWidth * 0.25);
        });

        availableSheetsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedSheet = newValue;
                mainController.updatePermissionsTable(newValue);
            }
        });

        loadAvailableSheets();
    }

    // Returns the currently selected sheet
    public SheetSummaryDTO getSelectedSheet() {
        return selectedSheet;
    }

    // Sets the main controller to enable communication with other components
    public void setMainController(SheetsManagementController mainController) {
        this.mainController = mainController;
    }

    // Loads the available sheets from the server asynchronously
    private void loadAvailableSheets() {
        String finalUrl = HttpUrl
                .parse(Constants.AVAILABLE_SHEETS_PAGE)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to load available sheets: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<SheetSummaryDTO>>() {}.getType();
                    List<SheetSummaryDTO> sheets = new Gson().fromJson(responseBody, listType);

                    Platform.runLater(() -> availableSheetsTable.getItems().setAll(sheets));
                } else {
                    Platform.runLater(() -> showError("Failed to load available sheets: " + response.message()));
                }
            }
        });
    }

    // Returns the table view containing available sheets
    public TableView<SheetSummaryDTO> getAvailableSheetsTable() {
        return availableSheetsTable;
    }

    // Updates the available sheets table with a new list of sheets
    public void updateAvailableSheets(List<SheetSummaryDTO> availableSheets) {
        availableSheetsTable.getItems().setAll(availableSheets);
    }

    // Shows an error alert with the given message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Refreshes the sheets table by reloading data from the server
    public void refreshSheetsTable() {
        loadAvailableSheets();
    }
}
