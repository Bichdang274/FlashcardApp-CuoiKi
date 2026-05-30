package com.example.flashcardapp.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_words")
public class SavedWord {
    @PrimaryKey
    @NonNull
    public String id;          // "dict_" + UUID
    public String userId;
    public String word;
    public String phonetic;
    public String partOfSpeech;
    public String definition;
    public String example;
    public long savedAt;

    public SavedWord(@NonNull String id, String userId, String word, String phonetic,
                     String partOfSpeech, String definition, String example, long savedAt) {
        this.id = id;
        this.userId = userId;
        this.word = word;
        this.phonetic = phonetic;
        this.partOfSpeech = partOfSpeech;
        this.definition = definition;
        this.example = example;
        this.savedAt = savedAt;
    }
}
