package org.example.eiscuno.model.machine;

import org.example.eiscuno.model.card.Card;

import java.util.ArrayList;

/**
 * The `ThreadSingUNOMachine` class implements the `Runnable` interface and is responsible for
 * monitoring the human player's cards. If the human player has only one card left, it prints "UNO".
 */
public class ThreadSingUNOMachine implements Runnable {
    private ArrayList<Card> cardsPlayer;

    /**
     * Constructor for `ThreadSingUNOMachine`.
     *
     * @param cardsPlayer the list of cards held by the human player
     */
    public ThreadSingUNOMachine(ArrayList<Card> cardsPlayer) {
        this.cardsPlayer = cardsPlayer;
    }

    /**
     * The `run` method is executed when the thread is started. It continuously checks if the human player
     * has only one card left and prints "UNO" if true. The thread sleeps for a random duration between checks.
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep((long) (Math.random() * 5000)); // Sleep for a random duration up to 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hasOneCardTheHumanPlayer();
        }
    }

    /**
     * Checks if the human player has only one card left. If true, prints "UNO".
     */
    private void hasOneCardTheHumanPlayer() {
        if (cardsPlayer.size() == 1) {
            System.out.println("UNO");
        }
    }
}