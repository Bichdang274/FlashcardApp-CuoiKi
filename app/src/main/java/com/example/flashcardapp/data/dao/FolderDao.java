package com.example.flashcardapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.flashcardapp.data.entity.Folder;
import java.util.List;

@Dao
public interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Folder folder);

    @Delete
    void delete(Folder folder);

    @Query("SELECT * FROM folders")
    LiveData<List<Folder>> getAllFolders();

    @Query("SELECT * FROM folders")
    List<Folder> getAllFoldersSync();

    @Query("SELECT * FROM folders WHERE id = :id")
    Folder getFolderById(String id);
}
