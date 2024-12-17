package org.example.eiscuno.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class WelcomeStage extends Stage {

    /**
     * Constructs a new WelcomeStage instance.
     * <p>This constructor initializes the stage with a predefined FXML layout,
     * applies a CSS stylesheet for styling, and configures the stage to be non-resizable
     * and undecorated. The stage's dimensions are set to match the primary screen size.</p>
     *
     * @throws IOException if there is an issue loading the FXML resource or stylesheet.
     */
    public WelcomeStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/WelcomeView.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/styles.css")).toExternalForm());
        setResizable(false);
        setTitle("EISC UNO");
        initStyle(StageStyle.UNDECORATED);
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        setWidth(850);
        setHeight(600);
        setScene(scene);
        show();
    }
    /**
     * Holds the singleton instance of the {@code WelcomeStage}.
     * This is a private static class to ensure lazy initialization and thread safety.
     */
    private static class WelcomeStageHolder {
        private static WelcomeStage INSTANCE;
    }
    /**
     * Retrieves the singleton instance of the WelcomeStage.
     * <p>If no instance exists, it initializes a new one. Otherwise, it returns the existing instance.</p>
     *
     * @return the singleton instance of {@code WelcomeStage}.
     * @throws IOException if there is an issue creating a new instance.
     */

    public static void deleteInstance() {
        WelcomeStage.WelcomeStageHolder.INSTANCE.close();
        WelcomeStage.WelcomeStageHolder.INSTANCE = null;
    }
    public static WelcomeStage getInstance() throws IOException {
        return WelcomeStage.WelcomeStageHolder.INSTANCE != null ?
                WelcomeStage.WelcomeStageHolder.INSTANCE :
                (WelcomeStage.WelcomeStageHolder.INSTANCE = new WelcomeStage());

    }

    /**
     * Holder class for the singleton instance of GameUnoStage.
     * This class ensures lazy initialization of the singleton instance.
     */


}



