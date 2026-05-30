package com.example.flashcardapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.StudySession;
import java.util.List;

@Dao
public interface StudySessionDao {
    @Insert
    void insert(StudySession session);

    @Query("SELECT COUNT(*) FROM study_sessions")
    int getTotalSessions();

    @Query("SELECT * FROM study_sessions ORDER BY date DESC LIMIT 30")
    List<StudySession> getRecentSessions();
}
