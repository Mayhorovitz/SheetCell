package javaFX.commands;

import engine.api.Engine;
import javafx.fxml.FXML;
import javaFX.main.MainController;

public class CommandsController {

    private MainController mainController;
    private Engine engine;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleSort() {
        // Logic to handle sorting using the engine
    }

    @FXML
    public void handleFilter() {
        // Logic to handle filtering using the engine
    }
}
