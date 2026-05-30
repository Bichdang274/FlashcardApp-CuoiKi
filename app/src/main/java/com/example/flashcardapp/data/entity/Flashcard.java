package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "flashcards")
public class Flashcard {
    @PrimaryKey
    @NonNull
    public String id;
    public String setId;
    public String english;
    public String vietnamese;
    public String phonetic;
    public String example;
    public String exampleVi;
    public String emoji;

    public Flashcard(@NonNull String id, String setId, String english, String vietnamese,
                     String phonetic, String example, String exampleVi, String emoji) {
        this.id = id; this.setId = setId; this.english = english;
        this.vietnamese = vietnamese; this.phonetic = phonetic;
        this.example = example; this.exampleVi = exampleVi; this.emoji = emoji;
    }
}
