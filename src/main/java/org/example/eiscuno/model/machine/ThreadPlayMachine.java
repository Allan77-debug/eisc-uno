package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.deck.Deck;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private Deck deck;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private TurnEndCallback callback;

    public ThreadPlayMachine(Table table, Player machinePlayer, Deck deck, ImageView tableImageView, TurnEndCallback callback) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.deck = deck;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (true) {
            if (hasPlayerPlayed) {
                try {
                    Thread.sleep((long) (2000 + Math.random() * 2000)); // Espera de 2 a 4 segundos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playTurn();
                hasPlayerPlayed = false;
            }
        }
    }

    private void playTurn() {
        Card cardToPlay = findPlayableCard(); // Buscar una carta jugable

        if (cardToPlay != null) { // La máquina tiene una carta jugable
            System.out.println("Machine plays a card: " + cardToPlay.getValue() + " of " + cardToPlay.getColor());
            table.addCardOnTheTable(cardToPlay); // Jugar la carta
            tableImageView.setImage(cardToPlay.getImage());
            machinePlayer.removeCard(machinePlayer.getCardsPlayer().indexOf(cardToPlay));
        } else { // No tiene cartas jugables
            if (!deck.isEmpty()) { // Solo toma una carta si el mazo no está vacío
                Card newCard = deck.takeCard();
                machinePlayer.addCard(newCard);
                System.out.println("Machine takes a card: " + newCard.getValue() + " of " +
                        (newCard.getColor() != null ? newCard.getColor() : "ANY"));
                System.out.println("Machine ends turn after taking a card.");
            } else {
                System.out.println("Deck is empty. Machine skips turn.");
            }
        }

        // Notificar al controlador que el turno de la máquina ha terminado
        if (callback != null) {
            callback.onMachineTurnEnd();
        }
    }




    private void putCardOnTheTable() {
        Card cardToPlay = findPlayableCard();
        if (cardToPlay != null) {
            table.addCardOnTheTable(cardToPlay);
            tableImageView.setImage(cardToPlay.getImage());
            machinePlayer.removeCard(machinePlayer.getCardsPlayer().indexOf(cardToPlay));
        } else {
            if (!deck.isEmpty()) {
                System.out.println("Machine takes a card.");
                machinePlayer.addCard(deck.takeCard());
            } else {
                System.out.println("Deck is empty. Machine skips turn.");
            }
        }
    }
    private Card findPlayableCard() {
        Card topCard = table.getCurrentCardOnTheTable();
        for (Card card : machinePlayer.getCardsPlayer()) {
            if (card.getColor().equals(topCard.getColor()) || card.getValue().equals(topCard.getValue())) {
                return card;
            }
        }
        return null; // No playable card
    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }
}