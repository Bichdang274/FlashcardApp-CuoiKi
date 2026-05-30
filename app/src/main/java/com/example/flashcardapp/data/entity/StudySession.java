package com.example.flashcardapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_sessions")
public class StudySession {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String setId;
    public long date;
    public int cardsStudied;
    public int knownCount;

    public StudySession(String setId, long date, int cardsStudied, int knownCount) {
        this.setId = setId; this.date = date;
        this.cardsStudied = cardsStudied; this.knownCount = knownCount;
    }
}
