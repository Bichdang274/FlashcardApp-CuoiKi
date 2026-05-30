package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "set_progress")
public class SetProgress {
    @PrimaryKey
    @NonNull
    public String setId;
    public int knownCards;
    public int totalCards;
    public long lastStudied;

    public SetProgress(@NonNull String setId, int knownCards, int totalCards, long lastStudied) {
        this.setId = setId; this.knownCards = knownCards;
        this.totalCards = totalCards; this.lastStudied = lastStudied;
    }
}
