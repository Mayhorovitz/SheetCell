package sheetsManagement.components.permissionsTable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.api.PermissionRequestDTO;
import dto.impl.PermissionRequestDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
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

public class PermissionsTableController {

    @FXML
    private TableView<PermissionRequestDTO> permissionsTable;
    @FXML
    private TableColumn<PermissionRequestDTO, String> usernameColumn;
    @FXML
    private TableColumn<PermissionRequestDTO, String> permissionTypeColumn;
    @FXML
    private TableColumn<PermissionRequestDTO, String> statusColumn;

    private SheetsManagementController mainController;

    // Initializes table columns and resizes them based on table width
    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("requestedPermission"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        permissionsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            usernameColumn.setPrefWidth(tableWidth * 0.33);
            permissionTypeColumn.setPrefWidth(tableWidth * 0.34);
            statusColumn.setPrefWidth(tableWidth * 0.33);
        });
    }

    // Sets the main controller for this component
    public void setMainController(SheetsManagementController mainController) {
        this.mainController = mainController;
    }

    // Loads permission requests for a specific sheet from the server
    public void loadPermissionsForSheet(String sheetName) {
        String finalUrl = HttpUrl
                .parse(Constants.PERMISSIONS_TABLE_PAGE)
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<PermissionRequestDTOImpl>>() {}.getType();


                    List<PermissionRequestDTOImpl> permissions = new Gson().fromJson(responseBody, listType);

                    Platform.runLater(() -> permissionsTable.getItems().setAll(permissions));
                } else {
                    Platform.runLater(() -> {
                    });
                }
            }
        });
    }
}
