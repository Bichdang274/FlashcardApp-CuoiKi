package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "card_sets")
public class CardSet {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String titleVi;
    public String emoji;
    public String gradient;
    public String accentColor;

    public CardSet(@NonNull String id, String title, String titleVi, String emoji,
                   String gradient, String accentColor) {
        this.id = id; this.title = title; this.titleVi = titleVi;
        this.emoji = emoji; this.gradient = gradient; this.accentColor = accentColor;
    }
}
