// Controlador mejorado usando SOLID
package org.example.eiscuno.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.machine.TurnEndCallback;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.unoenum.EISCUnoEnum;
import org.example.eiscuno.sounds.Sounds;

import java.util.Objects;

public class GameUnoController implements TurnEndCallback {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private Button deckButton;

    @FXML
    private Button unoButton;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Circle colorCircle;

    @FXML
    private ComboBox<String> colorComboBox;

    @FXML
    private Button confirmColorButton;

    private PauseTransition unoTimer;
    private Sounds gamemusic;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    GameUno gameUno;
    private int posInitCardToShow;
    private boolean isPlayerTurn;
    private GameUnoController gameController;

    private ThreadPlayMachine threadPlayMachine;

    /**
     * Initialize the Game Controller and methods
     *
     */
    @FXML
    public void initialize() {
        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/images/gamebg.png")).toExternalForm());
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true));
        mainPane.setBackground(new Background(background));
        initVariables();
        updateUnoButtonVisibility();
        setupColorSelection();
        disableColorSelection();
        this.gameUno.startGame();
        setButtonGraphics();
        gamemusic = new Sounds();
        gamemusic.loadSound("src/main/resources/org/example/eiscuno/audio/gametheme.wav");
        gamemusic.loopSound();
        gamemusic.lowerVolume(0.01);
        this.isPlayerTurn = true;

        // Show the initial card on the table
        try {
            Card initialCard = this.table.getCurrentCardOnTheTable();
            this.tableImageView.setImage(initialCard.getImage());
        } catch (IllegalStateException e) {
            System.out.println("Error inicializando la mesa: " + e.getMessage());
        }

        renderMachineCards();
        renderHumanPlayerCards();
        initThreads();
        if (colorCircle == null) {
            throw new IllegalStateException("colorCircle no está inicializado");
        }
        updateColorCircle(this.table.getCurrentCardOnTheTable().getColor());
    }

    /**
     * Configure the images into buttons
     */
    private void setButtonGraphics() {
        Image deckImage = new Image(getClass().getResource(EISCUnoEnum.DECK_OF_CARDS.getFilePath()).toString());
        ImageView deckImageView = new ImageView(deckImage);
        deckImageView.setFitWidth(100);
        deckImageView.setFitHeight(120);
        deckButton.setGraphic(deckImageView);

        Image unoImage = new Image(getClass().getResource(EISCUnoEnum.BUTTON_UNO.getFilePath()).toString());
        ImageView unoImageView = new ImageView(unoImage);
        unoImageView.setFitWidth(100);
        unoImageView.setFitHeight(50);
        unoButton.setGraphic(unoImageView);
    }



    /**
     * Setting color that user choosed in the game
     */
    private void setupColorSelection() {
        // Configura los colores disponibles en el ComboBox
        colorComboBox.getItems().addAll("RED", "YELLOW", "BLUE", "GREEN");
        colorComboBox.setDisable(true); // Deshabilitar inicialmente
        confirmColorButton.setDisable(true); // Deshabilitar inicialmente

        confirmColorButton.setOnAction(event -> {
            String selectedColor = colorComboBox.getValue();
            if (selectedColor != null) {
                System.out.println("Player selected color: " + selectedColor);
                table.getCurrentCardOnTheTable().setColor(selectedColor);
                updateColorCircle(selectedColor);
                disableColorSelection(); // Deshabilitar selección después de confirmar
                endPlayerTurn(); // Finalizar turno después de elegir el color
            }
        });
    }

    /**
     * Disable select to choose color
     */
    private void disableColorSelection() {
        colorComboBox.setDisable(true);
        colorComboBox.setVisible(false);
        confirmColorButton.setDisable(true);
        confirmColorButton.setVisible(false);
    }

    /**
     * Show select to choose color
     */
    private void enableColorSelection() {
        colorComboBox.setDisable(false);
        colorComboBox.setVisible(true);
        confirmColorButton.setDisable(false);
        confirmColorButton.setVisible(true);
    }

    /**
     * Update color of circle color in the game
     * @param color String of color options
     */
    public void updateColorCircle(String color) {
        if (color == null) {
            color = "BLACK"; // Asigna un valor por defecto cuando el color es null
        }

        switch (color) {
            case "RED":
                colorCircle.setFill(javafx.scene.paint.Color.RED);
                break;
            case "YELLOW":
                colorCircle.setFill(javafx.scene.paint.Color.YELLOW);
                break;
            case "BLUE":
                colorCircle.setFill(javafx.scene.paint.Color.BLUE);
                break;
            case "GREEN":
                colorCircle.setFill(javafx.scene.paint.Color.GREEN);
                break;
            default:
                colorCircle.setFill(javafx.scene.paint.Color.BLACK); // Color por defecto
                break;
        }
    }

    /**
     * The machine completed turn
     */
    @Override
    public void onMachineTurnEnd() {
        Platform.runLater(() -> {
            this.isPlayerTurn = true; // Cambia el turno al jugador humano
            renderMachineCards(); // Actualiza las cartas de la maquina
            System.out.println("Machine's turn has ended. It's now your turn!");
        });
    }

    /**
     * Method for update uno button change of status
     */
    private void updateUnoButtonVisibility() {
        if (this.humanPlayer.getCardCount() == 1) {
            unoButton.setVisible(true);
        } else {
            unoButton.setVisible(false); // Ocultar botón UNO
        }
    }

    /**
     * Initialize differents vars of game
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    /**
     * Initialize the threads used on game
     */
    private void initThreads() {
        ThreadSingUNOMachine threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();
        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.humanPlayer,
                this.deck, this.tableImageView, this, this);
        threadPlayMachine.start();
    }


    /**
     * Renders the human player's cards in the grid pane.
     */
    private void renderHumanPlayerCards() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] visibleCards = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < visibleCards.length; i++) {
            Card card = visibleCards[i];
            ImageView cardImageView = card.getCard();
            attachCardClickHandler(cardImageView, card);
            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    /**
     * Renders the machine's cards as "hidden" in the grid pane.
     */
    private void renderMachineCards() {
        this.gridPaneCardsMachine.getChildren().clear(); // Limpia las cartas actuales

        // Obtener las cartas visibles de la máquina
        Card[] hiddenCards = this.machinePlayer.getCardsPlayer().toArray(new Card[0]);
        int numVisibleCards = Math.min(4, hiddenCards.length); // Limitar a un máximo de 4 cartas

        for (int i = 0; i < numVisibleCards; i++) {
            // Crear un ImageView para mostrar el dorso de la carta
            ImageView hiddenCardImageView = new ImageView(new Image(getClass().getResource(EISCUnoEnum.CARD_UNO.getFilePath()).toString()));
            hiddenCardImageView.setFitWidth(70);
            hiddenCardImageView.setFitHeight(100);
            hiddenCardImageView.setPreserveRatio(true);

            // Agregar la carta tapada al gridPane
            this.gridPaneCardsMachine.add(hiddenCardImageView, i, 0);
        }
    }


    /**
     * Attaches a click handler to a card's ImageView for playing the card.
     *
     * @param cardImageView the ImageView of the card
     * @param card the Card object
     */
    private void attachCardClickHandler(ImageView cardImageView, Card card) {
        cardImageView.setOnMouseClicked((MouseEvent event) -> {
            if (!isPlayerTurn) { // Verificar si no es el turno del jugador
                System.out.println("You cannot play a card. It's not your turn!");
                return;
            }

            if (canPlayCard(card)) {
                playCard(card);
            } else {
                System.out.println("Cannot play this card!");
            }
        });
    }

    /**
     * Plays a card and updates the game state.
     * @param card the card to play
     */
    private void playCard(Card card) {

        table.addCardOnTheTable(card); // Add card on the table
        tableImageView.setImage(card.getImage()); // Update image of card
        humanPlayer.removeCard(findCardIndexInHand(card)); // Delete card of hand player
        renderHumanPlayerCards(); // Update the visible cards of user
        deck.addToDiscardPile(card);

        if (card.isSpecial() && (card.getValue().equals("Wild") || card.getValue().equals("+4"))) {
            System.out.println("Special card played: " + card.getValue());
            enableColorSelection();
            return;
        }
        updateColorCircle(card.getColor());
        handleSpecialCard(card);

        // If donot repeat turn, skip it
        if (!card.getValue().equals("Skip")) {
            endPlayerTurn(); // Finalizar el turno del jugador
        }

        if (humanPlayer.getCardCount() == 1) {
            System.out.println("Player has only one card left! Starting UNO timer...");
            startUnoTimer(); // Start counter of UNO
        }

        if (humanPlayer.getCardCount() == 0) {
            showWinAlert(); // Show alert en reset game
        }
    }

    /**
     * Show the alert when there winner of the game
     */
    private void showWinAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("¡Victoria!");
            alert.setHeaderText("¡Felicidades!");
            alert.setContentText("Has ganado la partida. El juego se reiniciará.");
            alert.showAndWait();

            resetGame(); // Reset game after show alert
        });
    }

    /**
     * Reset game and prepare all elements again
     */
    public void resetGame() {
        // Initialaze table and cards
        this.deck = new Deck();
        this.table = new Table();

        // Initialize players and game
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.gameUno.startGame();
        try {
            Card initialCard = this.table.getCurrentCardOnTheTable();
            this.tableImageView.setImage(initialCard.getImage());
        } catch (IllegalStateException e) {
            System.out.println("Error inicializando la mesa: " + e.getMessage());
        }

        // Reset variables
        this.posInitCardToShow = 0;
        this.isPlayerTurn = true;
        updateUnoButtonVisibility();
        disableColorSelection();

        renderMachineCards();
        renderHumanPlayerCards();

        initThreads();
        System.out.println("El juego se ha reiniciado.");
    }


    /**
     * Checks if a card can be played based on the current table card.
     * @param card the card to check
     * @return true if the card can be played, false otherwise
     */
    private boolean canPlayCard(Card card) {
        Card topCard = this.table.getCurrentCardOnTheTable();

        // Allow play cards same color
        if (topCard.getColor() != null && card.getColor() != null && card.getColor().equals(topCard.getColor())) {
            return true;
        }

        // Allow play card with same value (Include special cards)
        if (card.getValue().equals(topCard.getValue())) {
            return true;
        }

        // Allow play "Wild" and "+4" in any moment
        if (card.isSpecial() && (card.getValue().equals("Wild") || card.getValue().equals("+4"))) {
            return true;
        }
        // The card isnot playable
        return false;
    }

    /**
     * Method to know what is the special card
     * @param card card to evalue
     */
    private void handleSpecialCard(Card card) {
        switch (card.getValue()) {
            case "Skip": // Ceder turno
                System.out.println("Opponent's turn is skipped!");
                if (isPlayerTurn) {
                    System.out.println("Player repeats the turn due to Skip card!");
                    // Evitar finalizar el turno del jugador
                    renderHumanPlayerCards();
                    return; // Salir del metodo sin finalizar el turno
                } else {
                    System.out.println("Machine repeats the turn due to Skip card!");
                    threadPlayMachine.setSkipTurn(true); //la máquina debe repetir turno
                }
                break;
            case "Reverse": // Reversa
                System.out.println("Direction reversed!");
                break;
            case "+2": // Tomar 2 cartas
                System.out.println("Opponent takes 2 cards!");
                for (int i = 0; i < 2; i++) {
                    if (!deck.isEmpty()) {
                        machinePlayer.addCard(deck.takeCard());
                    }
                }
                break;
            case "+4": // Tomar 4 cartas
                System.out.println("Opponent takes 4 cards!");
                for (int i = 0; i < 4; i++) {
                    if (!deck.isEmpty()) {
                        machinePlayer.addCard(deck.takeCard());
                    }
                }
                System.out.println("Choose a color!");
                break;
            case "Wild": // Cambio de color
                System.out.println("Choose a color!");
                break;
        }
    }


    /**
     * Finds the index of a card in the human player's hand.
     *
     * @param card the card to find
     * @return the index of the card, or -1 if not found
     */
    private int findCardIndexInHand(Card card) {
        return this.humanPlayer.getCardsPlayer().indexOf(card);
    }

    /**
     * Handles the action of moving back to the previous set of cards.
     * @param event the action event triggered by the back button
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            renderHumanPlayerCards();
        }
    }

    /**
     * Handles the action of moving to the next set of cards.
     * @param event the action event triggered by the next button
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            renderHumanPlayerCards();
        }
    }

    /**
     * Handles the action of taking a card from the deck.
     * @param event the action event triggered by the take card button
     */
    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!isPlayerTurn) { // Check if it's not the player's turn
            System.out.println("You cannot take a card. It's not your turn!");
            return;
        }

        if (!deck.isEmpty()) { // If it's the player's turn, they can take a card
            Card newCard = this.deck.takeCard();
            this.humanPlayer.addCard(newCard);
            System.out.println("You took a card: " + newCard.getValue() + " of " +
                    (newCard.getColor() != null ? newCard.getColor() : "ANY"));
            renderHumanPlayerCards();
            endPlayerTurn(); // End the player's turn after taking a card
        } else {
            deck.reshuffleDeck();
        }
    }

    /**
     * Ends the player's turn and updates the game state accordingly.
     */
    private void endPlayerTurn() {
        updateUnoButtonVisibility();
        if (humanPlayer.getCardCount() == 0) {
            System.out.println("Player has no more cards! You win!");
            return;
        }
        this.isPlayerTurn = false; // Change the turn to the opponent
        threadPlayMachine.setHasPlayerPlayed(true); // Allow the machine to play
        System.out.println("Player's turn has ended. Machine is now playing.");
    }

    /**
     * Handles the action of pressing the UNO button.
     * @param event the action event triggered by the UNO button
     */
    @FXML
    void onHandleUno(ActionEvent event) {
        if (this.humanPlayer.getCardCount() == 1) {
            System.out.println("UNO button pressed in time!");
            if (unoTimer != null) {
                unoTimer.stop(); // Stop the timer if the button was pressed
            }
        } else {
            System.out.println("You can't press UNO now! You have more than one card.");
        }
    }

    /**
     * Starts the UNO timer with a random delay.
     */
    private void startUnoTimer() {
        if (unoTimer != null) {
            unoTimer.stop(); // Stop any previous timer if it exists
        }

        // Create a random delay between 2 and 4 seconds
        int randomTime = 2000 + (int) (Math.random() * 2000);

        unoTimer = new PauseTransition(Duration.millis(randomTime));
        unoTimer.setOnFinished(event -> {
            // Penalty if the player did not press the UNO button in time
            if (this.humanPlayer.getCardCount() == 1) {
                System.out.println("You didn't press UNO in time! Taking a penalty card...");
                Platform.runLater(() -> {
                    takePenaltyCard();
                    endPlayerTurn();
                });
            }
        });

        unoTimer.play(); // Start the timer
    }

    /**
     * Takes a penalty card if the player fails to press the UNO button in time.
     */
    private void takePenaltyCard() {
        if (!deck.isEmpty()) {
            Card penaltyCard = deck.takeCard();
            this.humanPlayer.addCard(penaltyCard);
            System.out.println("Penalty card: " + penaltyCard.getValue() +
                    " of " + (penaltyCard.getColor() != null ? penaltyCard.getColor() : "ANY"));
            renderHumanPlayerCards();
        }
    }

    /**
     * Handles the action of exiting the game.
     * @param event the action event triggered by the exit button
     */
    @FXML
    private void onHandleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

}
