package sheetsManagement.components.commands.chat.chatCommands;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import sheetsManagement.components.commands.chat.chatroom.ChatRoomMainController;

public class ChatCommandsController {

    private ChatRoomMainController chatCommands;
    private final BooleanProperty autoUpdates;
    @FXML private ToggleButton autoUpdatesButton;

    public ChatCommandsController() {
        autoUpdates = new SimpleBooleanProperty();
    }

    @FXML
    public void initialize() {
        autoUpdates.bind(autoUpdatesButton.selectedProperty());
    }

    public ReadOnlyBooleanProperty autoUpdatesProperty() {
        return autoUpdates;
    }


    @FXML
    void quitClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setChatCommands(ChatRoomMainController chatRoomMainController) {
        this.chatCommands = chatRoomMainController;
    }
}
