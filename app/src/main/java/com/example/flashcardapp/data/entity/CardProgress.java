package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "card_progress")
public class CardProgress {
    @PrimaryKey
    @NonNull
    public String cardId;
    public String setId;
    public boolean known;
    public int repetitions;
    public float easeFactor;
    public int interval;
    public long nextReviewDate;

    public CardProgress(@NonNull String cardId, String setId, boolean known, int repetitions,
                        float easeFactor, int interval, long nextReviewDate) {
        this.cardId = cardId; this.setId = setId; this.known = known;
        this.repetitions = repetitions; this.easeFactor = easeFactor;
        this.interval = interval; this.nextReviewDate = nextReviewDate;
    }
}
