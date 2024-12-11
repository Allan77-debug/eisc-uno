package org.example.eiscuno.model.deck;

import org.example.eiscuno.model.unoenum.EISCUnoEnum;
import org.example.eiscuno.model.card.Card;

import java.util.Collections;
import java.util.Stack;

/**
 * Represents a deck of Uno cards.
 */
public class Deck {
    private Stack<Card> deckOfCards;

    /**
     * Constructs a new deck of Uno cards and initializes it.
     */
    public Deck() {
        deckOfCards = new Stack<>();
        initializeDeck();
        System.out.println("Deck initialized with " + deckOfCards.size() + " cards.");
    }


    /**
     * Initializes the deck with cards based on the EISCUnoEnum values.
     */
    private void initializeDeck() {
        for (EISCUnoEnum cardEnum : EISCUnoEnum.values()) {
            String value = getCardValue(cardEnum.name());
            String color = getCardColor(cardEnum.name());

            // Saltar cartas Reverse
            if ("Reverse".equals(value)) {
                System.out.println("Skipping Reverse card: " + cardEnum.name());
                continue;
            }

            if (value == null) {
                System.out.println("Skipping invalid card: " + cardEnum.name());
                continue; // Saltar cartas no validas
            }

            Card card = new Card(cardEnum.getFilePath(), value, color);
            System.out.println("Adding card: " + value + " of " + (color != null ? color : "ANY"));
            deckOfCards.push(card);
        }
        Collections.shuffle(deckOfCards);
    }



    private String getCardValue(String name) {
        if (name.endsWith("0")) {
            return "0";
        } else if (name.endsWith("1")) {
            return "1";
        } else if (name.endsWith("2") && !name.contains("WILD_DRAW")) { // Diferenciar "+2" de números
            return "2";
        } else if (name.endsWith("3")) {
            return "3";
        } else if (name.endsWith("4") && !name.equals("FOUR_WILD_DRAW")) { // Diferenciar "+4"
            return "4";
        } else if (name.endsWith("5")) {
            return "5";
        } else if (name.endsWith("6")) {
            return "6";
        } else if (name.endsWith("7")) {
            return "7";
        } else if (name.endsWith("8")) {
            return "8";
        } else if (name.endsWith("9")) {
            return "9";
        } else if (name.startsWith("SKIP")) {
            return "Skip";
        } else if (name.startsWith("RESERVE")) {
            return "Reverse";
        } else if (name.startsWith("TWO_WILD_DRAW")) {
            return "+2";
        } else if (name.equals("FOUR_WILD_DRAW")) {
            return "+4";
        } else if (name.equals("WILD")) {
            return "Wild";
        } else {
            return null; // Para cartas no válidas
        }
    }


    private String getCardColor(String name) {
        if(name.startsWith("GREEN")){
            return "GREEN";
        } else if(name.startsWith("YELLOW")){
            return "YELLOW";
        } else if(name.startsWith("BLUE")){
            return "BLUE";
        } else if(name.startsWith("RED")){
            return "RED";
        } else if (name.endsWith("GREEN")) {
            return "GREEN";
        } else if (name.endsWith("YELLOW")) {
            return "YELLOW";
        } else if (name.endsWith("RED")) {
            return "RED";
        } else if (name.endsWith("BLUE")) {
            return "BLUE";
        } else {
            return null;
        }
    }


    /**
     * Takes a card from the top of the deck.
     *
     * @return the card from the top of the deck
     * @throws IllegalStateException if the deck is empty
     */
    public Card takeCard() {
        if (deckOfCards.isEmpty()) {
            throw new IllegalStateException("No hay más cartas en el mazo.");
        }
        Card card = deckOfCards.pop();
        System.out.println("Card taken: " + card.getValue() + " of " +
                (card.getColor() != null ? card.getColor() : "ANY") + ". Remaining cards: " + deckOfCards.size());
        return card;
    }


    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck is empty, false otherwise
     */
    public boolean isEmpty() {
        return deckOfCards.isEmpty();
    }
}
