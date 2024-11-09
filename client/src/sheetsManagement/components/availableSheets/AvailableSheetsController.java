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
import java.util.Timer;

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
    private AvailableSheetRefresher availableSheetRefresher;
    private Timer refresherTimer;

    @FXML
    public void initialize() {
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        sheetSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));

        // מאזין לגודל הטבלה שמעדכן את רוחב העמודות באופן יחסי
        availableSheetsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            sheetNameColumn.setPrefWidth(tableWidth * 0.25); // 25% מרוחב הטבלה
            ownerColumn.setPrefWidth(tableWidth * 0.25); // 25% מרוחב הטבלה
            sheetSizeColumn.setPrefWidth(tableWidth * 0.25); // 25% מרוחב הטבלה
            permissionTypeColumn.setPrefWidth(tableWidth * 0.25); // 25% מרוחב הטבלה
        });

        availableSheetsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mainController.updatePermissionsTable(newValue); // עדכון טבלת ההרשאות
            }
        });

        // רענון תקופתי של גיליונות זמינים
        availableSheetRefresher = new AvailableSheetRefresher(this::updateAvailableSheets);
        refresherTimer = new Timer(true);
        refresherTimer.schedule(availableSheetRefresher, 0, 5000); // רענון כל 5 שניות

        loadAvailableSheets();
    }

    public void setMainController(SheetsManagementController mainController) {
        this.mainController = mainController;
    }

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

    public TableView<SheetSummaryDTO> getAvailableSheetsTable() {
        return availableSheetsTable;
    }

    private void updateAvailableSheets(List<SheetSummaryDTO> availableSheets) {
        availableSheetsTable.getItems().setAll(availableSheets);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshSheetsTable() {
        loadAvailableSheets(); // רענון הנתונים מהשרת
    }
}
