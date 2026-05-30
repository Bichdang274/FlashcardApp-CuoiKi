package com.example.flashcardapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.SetProgress;
import java.util.List;

@Dao
public interface SetProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SetProgress progress);

    @Query("SELECT * FROM set_progress WHERE setId = :setId")
    SetProgress getProgressForSet(String setId);

    @Query("SELECT * FROM set_progress")
    LiveData<List<SetProgress>> getAllProgress();

    @Query("SELECT * FROM set_progress")
    List<SetProgress> getAllProgressSync();
}
