package org.example.eiscuno.model.game;

import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

/**
 * Represents a game of Uno.
 * This class manages the game logic and interactions between players, deck, and the table.
 */
public class GameUno implements IGameUno {

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;

    /**
     * Constructs a new GameUno instance.
     * @param humanPlayer   The human player participating in the game.
     * @param machinePlayer The machine player participating in the game.
     * @param deck          The deck of cards used in the game.
     * @param table         The table where cards are placed during the game.
     */
    public GameUno(Player humanPlayer, Player machinePlayer, Deck deck, Table table) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deck = deck;
        this.table = table;
    }

    /**
     * Starts the Uno game by distributing cards to players.
     * The human player and the machine player each receive 10 cards from the deck.
     */
    @Override
    public void startGame() {
        for (int i = 0; i < 5; i++) {
            humanPlayer.addCard(this.deck.takeCard());
            machinePlayer.addCard(this.deck.takeCard());
        }

        // Colocar una carta inicial en la mesa que no sea especial
        Card initialCard;
        do {
            initialCard = this.deck.takeCard();
        } while (initialCard.isSpecial());

        this.table.addCardOnTheTable(initialCard);
    }



    /**
     * Allows a player to draw a specified number of cards from the deck.
     * @param player        The player who will draw cards.
     * @param numberOfCards The number of cards to draw.
     */
    @Override
    public void eatCard(Player player, int numberOfCards) {
        for (int i = 0; i < numberOfCards; i++) {
            player.addCard(this.deck.takeCard());
        }
    }

    /**
     * Places a card on the table during the game.
     * @param card The card to be placed on the table.
     */
    @Override
    public void playCard(Card card) {
        this.table.addCardOnTheTable(card);
    }

    /**
     * Handles the scenario when a player shouts "Uno", forcing the other player to draw a card.
     * @param playerWhoSang The player who shouted "Uno".
     */
    @Override
    public void haveSungOne(String playerWhoSang) {
        if (playerWhoSang.equals("HUMAN_PLAYER")) {
            machinePlayer.addCard(this.deck.takeCard());
        } else {
            humanPlayer.addCard(this.deck.takeCard());
        }
    }

    /**
     * Retrieves the current visible cards for any player starting from a specific position.
     * @param player The player whose cards are to be retrieved.
     * @param posInitCardToShow The initial position of the cards to show.
     * @return An array of cards visible to the player.
     */
    private Card[] getCurrentVisibleCards(Player player, int posInitCardToShow) {
        int totalCards = player.getCardsPlayer().size();
        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);
        Card[] cards = new Card[numVisibleCards];

        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = player.getCard(posInitCardToShow + i);
        }

        return cards;
    }

    /**
     * Gets the current visible cards for the human player starting from the specified position.
     * @param posInitCardToShow the initial position to start showing the cards
     * @return an array of currently visible cards for the human player
     */
    public Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) {
        return getCurrentVisibleCards(this.humanPlayer, posInitCardToShow);
    }

    /**
     * Gets the current visible cards for the machine starting from the specified position.
     * @param posInitCardToShow the initial position to start showing the cards
     * @return an array of currently visible cards for the machine
     */
    public Card[] getCurrentVisibleCardsMachine(int posInitCardToShow) {
        return getCurrentVisibleCards(this.machinePlayer, posInitCardToShow);
    }


    /**
     * Checks if the game is over.
     * @return True if the deck is empty, indicating the game is over; otherwise, false.
     */
    @Override
    public Boolean isGameOver() {
        return null;
    }
}
