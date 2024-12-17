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

import java.util.Objects;

public class GameUnoController implements TurnEndCallback {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

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


    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    private boolean isPlayerTurn;
    private GameUnoController gameController;

    private ThreadPlayMachine threadPlayMachine;

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

        this.isPlayerTurn = true;

        // Mostrar la carta inicial en la mesa
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

    // Configurar las imagenes de los botones
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

    private void disableColorSelection() {
        colorComboBox.setDisable(true);
        colorComboBox.setVisible(false);
        confirmColorButton.setDisable(true);
        confirmColorButton.setVisible(false);
    }

    private void enableColorSelection() {
        colorComboBox.setDisable(false);
        colorComboBox.setVisible(true);
        confirmColorButton.setDisable(false);
        confirmColorButton.setVisible(true);
    }


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


    @Override
    public void onMachineTurnEnd() {
        Platform.runLater(() -> {
            this.isPlayerTurn = true; // Cambia el turno al jugador humano
            renderMachineCards(); // Actualiza las cartas de la maquina
            System.out.println("Machine's turn has ended. It's now your turn!");
        });
    }

    // Method to update the visibility of uno button
    private void updateUnoButtonVisibility() {
        if (this.humanPlayer.getCardCount() == 1) {
            unoButton.setVisible(true);
        } else {
            unoButton.setVisible(false); // Ocultar botón UNO
        }
    }

    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

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
     *
     * @param card the card to play
     */
    private void playCard(Card card) {

        table.addCardOnTheTable(card); // Agregar la carta a la mesa
        tableImageView.setImage(card.getImage()); // Actualizar la imagen en la mesa
        humanPlayer.removeCard(findCardIndexInHand(card)); // Eliminar la carta de la mano del jugador
        renderHumanPlayerCards(); // Actualizar las cartas visibles del jugador
        deck.addToDiscardPile(card);

        if (card.isSpecial() && (card.getValue().equals("Wild") || card.getValue().equals("+4"))) {
            System.out.println("Special card played: " + card.getValue());
            enableColorSelection();
            return;
        }

        updateColorCircle(card.getColor());

        // Manejar efectos especiales
        handleSpecialCard(card);

        // Si no es un turno repetido finaliza el turno
        if (!card.getValue().equals("Skip")) {
            endPlayerTurn(); // Finalizar el turno del jugador
        }

        if (humanPlayer.getCardCount() == 1) {
            System.out.println("Player has only one card left! Starting UNO timer...");
            startUnoTimer(); // Iniciar el temporizador de UNO
        }

        if (humanPlayer.getCardCount() == 0) {
            showWinAlert(); // Mostrar alerta y reiniciar juego
        }
    }

    private void showWinAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("¡Victoria!");
            alert.setHeaderText("¡Felicidades!");
            alert.setContentText("Has ganado la partida. El juego se reiniciará.");
            alert.showAndWait();

            resetGame(); // Reiniciar el juego después de mostrar la alerta
        });
    }

    public void resetGame() {
        // Inicializa el mazo y la mesa
        this.deck = new Deck();  // Crea un nuevo mazo y lo inicializa automáticamente
        this.table = new Table();  // Crea la mesa vacía

        // Inicializa los jugadores
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");

        // Inicializa el juego
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);


        this.gameUno.startGame();
        try {
            Card initialCard = this.table.getCurrentCardOnTheTable();
            this.tableImageView.setImage(initialCard.getImage());
        } catch (IllegalStateException e) {
            System.out.println("Error inicializando la mesa: " + e.getMessage());
        }

        // Reinicia las variables de estado del juego
        this.posInitCardToShow = 0;
        this.isPlayerTurn = true;

        // Actualiza la visibilidad del botón UNO
        updateUnoButtonVisibility();

        // Deshabilita la selección de color
        disableColorSelection();

        // Renderiza las cartas de los jugadores
        renderMachineCards();
        renderHumanPlayerCards();

        // Reinicia los hilos
        initThreads();

        System.out.println("El juego se ha reiniciado.");
    }


    /**
     * Checks if a card can be played based on the current table card.
     *
     * @param card the card to check
     * @return true if the card can be played, false otherwise
     */
    private boolean canPlayCard(Card card) {
        Card topCard = this.table.getCurrentCardOnTheTable();

        // Permitir jugar cartas del mismo color
        if (topCard.getColor() != null && card.getColor() != null && card.getColor().equals(topCard.getColor())) {
            return true;
        }

        // Permitir jugar cartas con el mismo valor (incluidas cartas especiales)
        if (card.getValue().equals(topCard.getValue())) {
            return true;
        }

        // Permitir jugar "Wild" y "+4" en cualquier momento
        if (card.isSpecial() && (card.getValue().equals("Wild") || card.getValue().equals("+4"))) {
            return true;
        }

        // La carta no es jugable
        return false;
    }


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

    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            renderHumanPlayerCards();
        }
    }

    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            renderHumanPlayerCards();
        }
    }

    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!isPlayerTurn) { // Verificar si no es el turno del jugador
            System.out.println("You cannot take a card. It's not your turn!");
            return;
        }

        if (!deck.isEmpty()) { // Si es el turno del jugador puede tomar una carta
            Card newCard = this.deck.takeCard();
            this.humanPlayer.addCard(newCard);
            System.out.println("You took a card: " + newCard.getValue() + " of " +
                    (newCard.getColor() != null ? newCard.getColor() : "ANY"));
            renderHumanPlayerCards();
            endPlayerTurn(); // Finaliza el turno del jugador después de tomar una carta
        } else {
            deck.reshuffleDeck();
        }
    }

    private void endPlayerTurn() {
        updateUnoButtonVisibility();
        if (humanPlayer.getCardCount() == 0) {
            System.out.println("Player has no more cards! You win!");
            return;
        }
        this.isPlayerTurn = false; // Cambia el turno al oponente
        threadPlayMachine.setHasPlayerPlayed(true); // Permite que la maquina juegue
        System.out.println("Player's turn has ended. Machine is now playing.");
    }

    @FXML
    void onHandleUno(ActionEvent event) {
        if (this.humanPlayer.getCardCount() == 1) {
            System.out.println("UNO button pressed in time!");
            if (unoTimer != null) {
                unoTimer.stop(); // Detener el temporizador si presionó el botón
            }
        } else {
            System.out.println("You can't press UNO now! You have more than one card.");
        }
    }

    private void startUnoTimer() {
        if (unoTimer != null) {
            unoTimer.stop(); // Detiene el temporizador previo si existiera
        }

        // Crear un tiempo aleatorio entre 2 y 4 segundos
        int randomTime = 2000 + (int) (Math.random() * 2000);

        unoTimer = new PauseTransition(Duration.millis(randomTime));
        unoTimer.setOnFinished(event -> {
            // Penalización si el jugador no presionó el botón UNO
            if (this.humanPlayer.getCardCount() == 1) {
                System.out.println("You didn't press UNO in time! Taking a penalty card...");
                Platform.runLater(() -> {
                    takePenaltyCard();
                    endPlayerTurn();
                });
            }
        });

        unoTimer.play(); // Iniciar el temporizador
    }

    // Método para tomar una carta como penalización
    private void takePenaltyCard() {
        if (!deck.isEmpty()) {
            Card penaltyCard = deck.takeCard();
            this.humanPlayer.addCard(penaltyCard);
            System.out.println("Penalty card: " + penaltyCard.getValue() +
                    " of " + (penaltyCard.getColor() != null ? penaltyCard.getColor() : "ANY"));
            renderHumanPlayerCards();
        }
    }


    // Method to close game
    @FXML
    private void onHandleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
