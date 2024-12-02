package sheetsManagement.components.commands.chat.chatroom;


import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import sheetsManagement.components.commands.CommandsController;
import sheetsManagement.components.commands.chat.api.HttpStatusUpdate;
import sheetsManagement.components.commands.chat.chatCommands.ChatCommandsController;
import sheetsManagement.components.commands.chat.chatarea.ChatAreaController;
import sheetsManagement.components.commands.chat.users.UsersListController;

import java.io.IOException;

public class ChatRoomMainController implements HttpStatusUpdate {

    @FXML private VBox usersListComponent;
    @FXML private UsersListController usersListComponentController;
    @FXML private VBox actionCommandsComponent;
    @FXML private ChatCommandsController actionCommandsComponentController;
    @FXML private GridPane chatAreaComponent;
    @FXML private ChatAreaController chatAreaComponentController;

    private CommandsController chatAppMainController;

    @FXML
    public void initialize() {
        usersListComponentController.setHttpStatusUpdate(this);
        actionCommandsComponentController.setChatCommands(this);
        chatAreaComponentController.setHttpStatusUpdate(this);

        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
    }

    public void close() throws IOException {
        usersListComponentController.close();
        chatAreaComponentController.close();
    }

    public void setActive() {
        usersListComponentController.startListRefresher();
        chatAreaComponentController.startListRefresher();
    }

    public void setInActive() {
        try {
            usersListComponentController.close();
            chatAreaComponentController.close();
        } catch (Exception ignored) {}
    }

    public void setChatAppMainController(CommandsController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
    }

    @Override
    public void updateHttpLine(String line) {
        System.out.println(line);
    }

    public void logout() {

    }
}
