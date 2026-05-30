package com.example.flashcardapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.CardSet;
import java.util.List;

@Dao
public interface CardSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CardSet> sets);

    @Query("SELECT * FROM card_sets")
    LiveData<List<CardSet>> getAllSets();

    @Query("SELECT * FROM card_sets")
    List<CardSet> getAllSetsSync();

    @Query("SELECT * FROM card_sets WHERE id = :id")
    CardSet getSetById(String id);
}
