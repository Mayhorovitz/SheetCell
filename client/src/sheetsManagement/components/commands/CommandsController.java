package sheetsManagement.components.commands;

import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetsManagement.SheetsManagementController;
import sheetsManagement.components.commands.chat.chatroom.ChatRoomMainController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CommandsController {

    private SheetsManagementController mainController;

    @FXML
    private VBox commandsBox;
    @FXML
    private Button viewSheetButton;
    @FXML
    private Button requestPermissionButton;
    @FXML
    private Button ackDenyPermissionButton;
    @FXML
    private Button chatButton;



    @FXML
    public void initialize() {
        viewSheetButton.setOnAction(event -> handleViewSheet());
        requestPermissionButton.setOnAction(event -> handleRequestPermission());
        ackDenyPermissionButton.setOnAction(event -> handleAckDenyPermissionRequest());
        chatButton.setOnAction(event -> handleChatEnter());

    }


    public void setMainController(SheetsManagementController mainController) {
        this.mainController = mainController;
    }

    private void handleViewSheet() {
        SheetSummaryDTO selectedSheet = mainController.getAvailableSheetsController().getSelectedSheet();
        if (selectedSheet != null) {
            mainController.handleViewSheet();
        } else {
            showError("Please select a sheet to view.");
        }
    }

    private void handleRequestPermission() {
        // Show a dialog for selecting a sheet and permission type
        SheetSummaryDTO selectedSheet = mainController.getAvailableSheetsController().getSelectedSheet();
        if (selectedSheet == null) {
            showError("Please select a sheet to request permission.");
            return;
        }

        // Check if the current user is the owner of the selected sheet
        if (selectedSheet.getOwner().equals(mainController.getCurrentUserName())) {
            showError("You are already the owner of this sheet. You cannot request permission for your own sheet.");
            return;
        }

        // Create a choice dialog for the user to select permission type
        List<String> permissionTypes = FXCollections.observableArrayList("READER", "WRITER");
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(permissionTypes.getFirst(), permissionTypes);
        choiceDialog.setTitle("Request Permission");
        choiceDialog.setHeaderText("Select Permission Type");
        choiceDialog.setContentText("Choose the permission type:");

        Optional<String> result = choiceDialog.showAndWait();
        result.ifPresent(selectedPermissionType -> {
            String finalUrl = Constants.PERMISSION_REQUEST_PAGE;
            RequestBody requestBody = new FormBody.Builder()
                    .add("sheetName", selectedSheet.getName())
                    .add("requesterUsername", mainController.getCurrentUserName())
                    .add("requestedPermission", selectedPermissionType)
                    .build();

            Request request = new Request.Builder()
                    .url(finalUrl)
                    .post(requestBody)
                    .build();

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> showError("Failed to request permission: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response)  {
                    try {
                        if (!response.isSuccessful()) {
                            Platform.runLater(() -> showError("Failed to request permission: " + response.message()));

                        }
                    } finally {
                        response.close();
                    }
                }
            });
        });
    }

    private void handleAckDenyPermissionRequest() {
        showPermissionsRequestsPopup();
    }

    private void showPermissionsRequestsPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetsManagement/components/commands/permissionsRequests.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Pending Permission Requests");
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(commandsBox.getScene().getWindow());

            PermissionsRequestsController permissionsRequestsController = loader.getController();
            permissionsRequestsController.setMainController(mainController);

            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            permissionsRequestsController.startRequestsRefresher();

            popupStage.showAndWait();

            permissionsRequestsController.stopRequestsRefresher();
        } catch (IOException e) {
            showError("Failed to open permissions requests: " + e.getMessage());
        }
    }

    private void handleChatEnter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetsManagement/components/commands/chat/chatroom/chat-room-main.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Chat room");
            popupStage.initModality(Modality.WINDOW_MODAL);

            ChatRoomMainController chatRoomController = loader.getController();
            chatRoomController.setChatAppMainController(this);

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            chatRoomController.setActive();

            popupStage.showAndWait();

            chatRoomController.setInActive();


        } catch (IOException e) {
            showError("Failed to open chat: " + e.getMessage());
        }


    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
