package org.example.eiscuno.model.table;

import org.example.eiscuno.model.card.Card;

import java.util.ArrayList;

/**
 * Represents the table in the Uno game where cards are played.
 */
public class Table {
    private ArrayList<Card> cardsTable;

    /**
     * Constructs a new Table object with no cards on it.
     */
    public Table() {
        this.cardsTable = new ArrayList<>();
    }

    /**
     * Adds a card to the table.
     *
     * @param card The card to be added to the table.
     */
    public void addCardOnTheTable(Card card) {
        // Agregar la carta a la mesa
        this.cardsTable.add(card);
        System.out.println("Card added to the table: " + card.getValue() +
                (card.getColor() != null ? " of " + card.getColor() : " of ANY"));
    }

    /**
     * Retrieves the current card on the table.
     *
     * @return The card currently on the table.
     * @throws IllegalStateException if there are no cards on the table.
     */
    public Card getCurrentCardOnTheTable() {
        if (cardsTable.isEmpty()) {
            throw new IllegalStateException("No hay cartas en la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1);
    }
}
