package com.example.flashcardapp.data.model;

public class CardProgressDto {
    public final String id;
    public final String userId;
    public final String cardId;
    public final String setId;
    public final boolean known;
    public final int repetitions;
    public final double easeFactor;
    public final int intervalDays;

    public CardProgressDto(String id, String userId, String cardId, String setId,
                           boolean known, int repetitions, double easeFactor, int intervalDays) {
        this.id = id; this.userId = userId; this.cardId = cardId; this.setId = setId;
        this.known = known; this.repetitions = repetitions;
        this.easeFactor = easeFactor; this.intervalDays = intervalDays;
    }

    public CardProgressDto withSM2(boolean known, int repetitions, double easeFactor, int intervalDays) {
        return new CardProgressDto(id, userId, cardId, setId, known, repetitions, easeFactor, intervalDays);
    }
}
