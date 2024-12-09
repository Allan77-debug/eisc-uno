// Controlador mejorado usando SOLID
package org.example.eiscuno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.machine.TurnEndCallback;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

public class GameUnoController implements TurnEndCallback {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    private boolean isPlayerTurn;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    @FXML
    public void initialize() {
        initVariables();
        this.gameUno.startGame();
        this.isPlayerTurn = true;

        // Mostrar la carta inicial en la mesa
        try {
            Card initialCard = this.table.getCurrentCardOnTheTable();
            this.tableImageView.setImage(initialCard.getImage());
        } catch (IllegalStateException e) {
            System.out.println("Error inicializando la mesa: " + e.getMessage());
        }

        renderHumanPlayerCards();
        initThreads();
    }

    @Override
    public void onMachineTurnEnd() {
        this.isPlayerTurn = true; // Cambia el turno al jugador humano
        System.out.println("Machine's turn has ended. It's now your turn!");
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
        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        // Pasa el controlador actual como callback
        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.deck, this.tableImageView, this);
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
     * Checks if a card can be played based on the current table card.
     *
     * @param card the card to check
     * @return true if the card can be played, false otherwise
     */
    private boolean canPlayCard(Card card) {
        Card topCard = this.table.getCurrentCardOnTheTable();
        return card.getColor().equals(topCard.getColor()) || card.getValue().equals(topCard.getValue());
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

        endPlayerTurn(); // Finalizar el turno del jugador
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

        if (!deck.isEmpty()) { // Si es el turno del jugador, puede tomar una carta
            Card newCard = this.deck.takeCard();
            this.humanPlayer.addCard(newCard);
            System.out.println("You took a card: " + newCard.getValue() + " of " +
                    (newCard.getColor() != null ? newCard.getColor() : "ANY"));
            renderHumanPlayerCards();
            endPlayerTurn(); // Finaliza el turno del jugador después de tomar una carta
        } else {
            System.out.println("The deck is empty. You cannot take a card.");
        }
    }

    private void endPlayerTurn() {
        this.isPlayerTurn = false; // Cambia el turno al oponente
        threadPlayMachine.setHasPlayerPlayed(true); // Permite que la máquina juegue
        System.out.println("Player's turn has ended. Machine is now playing.");
    }


    @FXML
    void onHandleUno(ActionEvent event) {
        if (this.humanPlayer.getCardsPlayer().size() == 1) {
            System.out.println("Player says UNO!");
        } else {
            System.out.println("You cannot declare UNO!");
        }
    }
}
