package sheetsManagement.components.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.impl.PermissionRequestDTOImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetsManagement.SheetsManagementController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PermissionsRequestsController {

    @FXML
    private TableView<PermissionRequestDTOImpl> permissionsRequestsTable;
    @FXML
    private TableColumn<PermissionRequestDTOImpl, String> usernameColumn;
    @FXML
    private TableColumn<PermissionRequestDTOImpl, String> permissionTypeColumn;
    @FXML
    private TableColumn<PermissionRequestDTOImpl, String> sheetNameColumn;
    @FXML
    private Button approveButton;
    @FXML
    private Button denyButton;

    private SheetsManagementController mainController;
    private Timer refresherTimer;

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("requestedPermission"));
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));

        approveButton.setOnAction(event -> handleApproveRequest());
        denyButton.setOnAction(event -> handleDenyRequest());

        startRequestsRefresher();
    }

    public void setMainController(SheetsManagementController mainController) {
        this.mainController = mainController;
    }

    private void handleApproveRequest() {
        PermissionRequestDTOImpl selectedRequest = permissionsRequestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showError("Please select a request to approve.");
            return;
        }

        updateRequestStatus(selectedRequest, "APPROVED");
    }

    private void handleDenyRequest() {
        PermissionRequestDTOImpl selectedRequest = permissionsRequestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showError("Please select a request to deny.");
            return;
        }

        updateRequestStatus(selectedRequest, "DENIED");
    }

    private void updateRequestStatus(PermissionRequestDTOImpl request, String status) {
        String finalUrl = Constants.PERMISSION_RESPONSE_PAGE;

        RequestBody requestBody = new FormBody.Builder()
                .add("sheetName", request.getSheetName())
                .add("requesterUsername", request.getUsername())
                .add("status", status)
                .build();

        Request httpRequest = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        HttpClientUtil.runAsync(httpRequest, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to update request status: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        showMessage("Request status updated successfully.");
                        permissionsRequestsTable.getItems().remove(request);
                    });
                } else {
                    Platform.runLater(() -> showError("Failed to update request status: " + response.message()));
                }
            }
        });
    }

    public void startRequestsRefresher() {
        if (refresherTimer == null) {
            refresherTimer = new Timer(true);
            refresherTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loadPendingRequests();
                }
            }, 0, 5000);
        }
    }



    private void loadPendingRequests() {
        String finalUrl = Constants.OWNED_SHEETS_PENDING_REQUESTS_PAGE + "?ownerUsername=" + mainController.getCurrentUserName();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to load pending requests: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<PermissionRequestDTOImpl> pendingRequests = new Gson().fromJson(responseBody, new TypeToken<List<PermissionRequestDTOImpl>>() {}.getType());

                    Platform.runLater(() -> permissionsRequestsTable.setItems(FXCollections.observableArrayList(pendingRequests)));
                } else {
                    Platform.runLater(() -> showError("Failed to load pending requests: " + response.message()));
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void stopRequestsRefresher() {
        if (refresherTimer != null) {
            refresherTimer.cancel();
            refresherTimer = null; // מבטיח שלא נשמור את הטיימר הישן בזיכרון
        }
    }


    @FXML
    private void handleClosePopup() {
        // קבלת ה-stage הנוכחי וסגירתו
        Stage stage = (Stage) permissionsRequestsTable.getScene().getWindow();
        stage.close();
    }
}
