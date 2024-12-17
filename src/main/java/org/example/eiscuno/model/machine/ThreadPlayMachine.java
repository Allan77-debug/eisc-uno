package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.example.eiscuno.controller.GameUnoController;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.deck.Deck;

import java.util.logging.Level;
import java.util.logging.Logger;

/** * This class represents the machine's turn logic in the UNO game. */
public class ThreadPlayMachine extends Thread {
    private final Table table;
    private final Player machinePlayer;
    private final Deck deck;
    private final ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private final TurnEndCallback callback;
    private final Player humanPlayer; // Nueva referencia
    private static final Logger LOGGER = Logger.getLogger(ThreadPlayMachine.class.getName());
    private final GameUnoController gameController;

    /**
     * Constructor to initialize the ThreadPlayMachine with required parameters.
     * @param table the game table
     * @param machinePlayer the machine player
     * @param humanPlayer the human player
     * @param deck the deck of cards
     * @param tableImageView the ImageView representing the card on the table
     * @param callback the callback to notify when the machine's turn ends
     * @param gameController the game controller
     */
    public ThreadPlayMachine(Table table, Player machinePlayer, Player humanPlayer, Deck deck,
                             ImageView tableImageView, TurnEndCallback callback, GameUnoController gameController) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.deck = deck;
        this.tableImageView = tableImageView;
        this.humanPlayer = humanPlayer;
        this.hasPlayerPlayed = false;
        this.callback = callback;
        this.gameController = gameController;
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!Thread.currentThread().isInterrupted()) {
                if (hasPlayerPlayed) {
                    playTurn(); // Ejecuta la lógica del turno
                    hasPlayerPlayed = false; // Resetea la bandera después de completar el turno
                }
                try {
                    wait(); // Espera hasta que se notifique
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Thread was interrupted", e);
                    Thread.currentThread().interrupt(); // Restablece el estado de interrupción
                }
            }
        }
    }

    /**
     * Plays the machine's turn.
     */
    private void playTurn() {
        do {

            try {
                Thread.sleep((long) (2000 + Math.random() * 2000)); // Espera de 2 a 4 segundos entre turnos
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Thread was interrupted", e);
                Thread.currentThread().interrupt(); // Restablece el estado de interrupción

            }

            this.skipTurn = false; // Reinicia la bandera al inicio del turno
            Card cardToPlay = findPlayableCard(); // Buscar una carta jugable

            if (cardToPlay != null) {
                System.out.println("Machine plays a card: " + cardToPlay.getValue() +
                        (cardToPlay.getColor() != null ? " of " + cardToPlay.getColor() : ""));
                gameController.updateColorCircle(cardToPlay.getColor());
                table.addCardOnTheTable(cardToPlay); // Jugar la carta
                tableImageView.setImage(cardToPlay.getImage());
                machinePlayer.removeCard(machinePlayer.getCardsPlayer().indexOf(cardToPlay));
                deck.addToDiscardPile(cardToPlay); // Agregar la carta al mazo de descarte

                // Manejar efectos de cartas especiales
                if (cardToPlay.isSpecial()) {
                    handleSpecialCardEffect(cardToPlay);
                }

            }
            else { // No tiene cartas jugables
                if (!deck.isEmpty()) { // Solo toma una carta si el mazo no está vacío
                    Card newCard = deck.takeCard();
                    machinePlayer.addCard(newCard);
                    System.out.println("Machine takes a card: " + newCard.getValue() + " of " +
                            (newCard.getColor() != null ? newCard.getColor() : "ANY"));
                } else {
                    deck.reshuffleDeck();
                }
            }

            if (machinePlayer.getCardCount() == 0) {
                showWinAlert();
            }

            // Actualiza las cartas de la máquina en la interfaz
            if (callback != null && !skipTurn) { // Llamar callback solo si no se repite el turno
                callback.onMachineTurnEnd();
            }


        } while (skipTurn); // Si skipTurn es true repetir el turno
    }

    /**
     * Shows an alert when the machine wins the game.
     */
    private void showWinAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("¡Derrota!");
            alert.setContentText("Has perdido. El juego se reiniciará.");
            alert.showAndWait();

            gameController.resetGame();
        });
    }

    /**
     * Handles the effects of special cards.
     * @param card the special card being played
     */
    private void handleSpecialCardEffect(Card card) {
        switch (card.getValue()) {
            case "Skip":
                System.out.println("Player's turn is skipped!");
                this.skipTurn = true;
                break;
            case "Reverse":
                System.out.println("Direction reversed!");
                break;
            case "+2":
                System.out.println("Player takes 2 cards!");
                for (int i = 0; i < 2; i++) {
                    if (!deck.isEmpty()) {
                        humanPlayer.addCard(deck.takeCard());
                    }
                }
                break;
            case "+4":
                System.out.println("Player takes 4 cards!");
                for (int i = 0; i < 4; i++) {
                    if (!deck.isEmpty()) {
                        humanPlayer.addCard(deck.takeCard());
                    }
                }
                System.out.println("Machine chooses a color!");
                String newColor = chooseRandomColor();
                table.getCurrentCardOnTheTable().setColor(newColor); // Cambiar color en la mesa
                System.out.println("New color: " + newColor);
                gameController.updateColorCircle(newColor);
                break;
            case "Wild":
                System.out.println("Machine chooses a color!");
                String chosenColor = chooseRandomColor();
                table.getCurrentCardOnTheTable().setColor(chosenColor); // Cambiar color en la mesa
                System.out.println("New color: " + chosenColor);
                gameController.updateColorCircle(chosenColor);
                break;
        }
    }

    private volatile boolean skipTurn; // Nueva bandera para Skip

    /**
     * Sets the flag to skip the turn.
     * @param skipTurn the new value for the skipTurn flag
     */
    public void setSkipTurn(boolean skipTurn) {
        this.skipTurn = skipTurn;
    }

    /**
     * Chooses a random color.
     * @return the randomly chosen color
     */
    private String chooseRandomColor() {
        String[] colors = {"RED", "YELLOW", "BLUE", "GREEN"};
        int index = (int) (Math.random() * colors.length);
        System.out.println("Machine chooses color: " + colors[index]);
        return colors[index];
    }

    /**
     * Finds a playable card from the machine's hand.
     * @return the playable card or null if no playable card is found
     */
    private Card findPlayableCard() {
        Card topCard = table.getCurrentCardOnTheTable();

        for (Card card : machinePlayer.getCardsPlayer()) {
            // Verificar si la carta es especial y tiene color
            if (card.isSpecial() && card.getColor() != null) {
                if (card.getColor().equals(topCard.getColor())) {
                    return card; // Carta especial jugable si coincide en color
                }
            }

            // Verificar si la carta tiene el mismo color o valor
            if (card.getColor() != null && (card.getColor().equals(topCard.getColor()) || card.getValue().equals(topCard.getValue()))) {
                return card; // Carta normal jugable
            }

            // Verificar si la carta es "Wild" o "+4", siempre jugables
            if (card.isSpecial() && (card.getValue().equals("Wild") || card.getValue().equals("+4"))) {
                return card;
            }
        }
        return null; // No hay cartas jugables
    }

    /**
     * Setting the player played
     * @param hasPlayerPlayed boolean to know is player has played
     */
    public synchronized void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
        notify(); // Notificar al hilo para que ejecute el turno
    }
}