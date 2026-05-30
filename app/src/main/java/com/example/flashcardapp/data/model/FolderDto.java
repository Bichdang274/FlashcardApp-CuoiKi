package com.example.flashcardapp.data.model;

import java.util.List;

public class FolderDto {
    public final String id;
    public final String userId;
    public final String name;
    public final String emoji;
    public final String color;
    public final List<String> wordIds;

    public FolderDto(String id, String userId, String name, String emoji,
                     String color, List<String> wordIds) {
        this.id = id; this.userId = userId; this.name = name;
        this.emoji = emoji; this.color = color; this.wordIds = wordIds;
    }
}
