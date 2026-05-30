package com.example.flashcardapp.data.model;

public class FlashcardDto {
    public final String id;
    public final String setId;
    public final String english;
    public final String vietnamese;
    public final String phonetic;
    public final String example;
    public final String exampleVi;
    public final String emoji;

    public FlashcardDto(String id, String setId, String english, String vietnamese,
                        String phonetic, String example, String exampleVi, String emoji) {
        this.id = id; this.setId = setId; this.english = english;
        this.vietnamese = vietnamese; this.phonetic = phonetic;
        this.example = example; this.exampleVi = exampleVi; this.emoji = emoji;
    }
}
