package com.example.flashcardapp.viewmodel;

public class MatchingTile {
    public final String id;
    public final String text;
    public final String cardId;
    public final String type;
    public final String emoji;

    public MatchingTile(String id, String text, String cardId, String type, String emoji) {
        this.id = id; this.text = text; this.cardId = cardId;
        this.type = type; this.emoji = emoji;
    }
}
