package org.example.eiscuno.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import org.example.eiscuno.view.GameUnoStage;
import org.example.eiscuno.view.WelcomeStage;

import java.io.IOException;
import java.util.Objects;

public class WelcomeController {
    public Button playbutton;
    @FXML
    private BorderPane welcomepane;
    @FXML
    public void initialize() {
        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/images/unobg.png")).toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true));
        welcomepane.setBackground(new Background(background));

        Image play = new Image(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/images/boton.png")).toExternalForm());
        BackgroundImage playtxt = new BackgroundImage(play,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true));
        playbutton.setBackground(new Background(playtxt));

    }
    public void onHandlePlayGame(javafx.event.ActionEvent actionEvent) throws IOException {
        WelcomeStage.deleteInstance();
        GameUnoStage.getInstance();

    }
}
