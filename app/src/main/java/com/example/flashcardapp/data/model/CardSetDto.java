package com.example.flashcardapp.data.model;

public class CardSetDto {
    public final String id;
    public final String title;
    public final String titleVi;
    public final String emoji;
    public final String gradient;
    public final String accentColor;

    public CardSetDto(String id, String title, String titleVi, String emoji,
                      String gradient, String accentColor) {
        this.id = id; this.title = title; this.titleVi = titleVi;
        this.emoji = emoji; this.gradient = gradient; this.accentColor = accentColor;
    }
}
