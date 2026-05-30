package com.example.flashcardapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.Flashcard;
import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Flashcard> cards);

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    LiveData<List<Flashcard>> getCardsForSet(String setId);

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    List<Flashcard> getCardsForSetSync(String setId);

    @Query("SELECT * FROM flashcards WHERE english LIKE '%' || :query || '%' OR vietnamese LIKE '%' || :query || '%'")
    LiveData<List<Flashcard>> searchCards(String query);
}
