package com.example.flashcardapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.CardProgress;
import java.util.List;

@Dao
public interface CardProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CardProgress progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CardProgress> progressList);

    @Query("SELECT * FROM card_progress WHERE setId = :setId")
    List<CardProgress> getProgressForSet(String setId);

    @Query("SELECT COUNT(*) FROM card_progress WHERE setId = :setId AND known = 1")
    int getKnownCountForSet(String setId);
}
