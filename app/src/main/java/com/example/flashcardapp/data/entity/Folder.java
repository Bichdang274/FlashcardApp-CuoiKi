package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "folders")
public class Folder {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String emoji;
    public String color;
    public String wordIds; // JSON array of word IDs

    public Folder(@NonNull String id, String name, String emoji, String color, String wordIds) {
        this.id = id; this.name = name; this.emoji = emoji;
        this.color = color; this.wordIds = wordIds;
    }
}
