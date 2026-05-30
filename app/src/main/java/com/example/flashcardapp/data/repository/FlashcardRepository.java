package com.example.flashcardapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import com.example.flashcardapp.data.AppDatabase;
import com.example.flashcardapp.data.entity.*;
import com.google.gson.Gson;
import java.util.*;

public class FlashcardRepository {
    private final AppDatabase db;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public FlashcardRepository(Context context) {
        db = AppDatabase.getInstance(context);
        prefs = context.getSharedPreferences("flashcard_prefs", Context.MODE_PRIVATE);
    }

    // ─── Auth ────────────────────────────────────────────
    public User register(String name, String email, String password) throws Exception {
        User existing = db.userDao().findByEmail(email);
        if (existing != null) throw new Exception("Email đã được sử dụng");
        User user = new User(UUID.randomUUID().toString(), name, email, password,
            String.valueOf(System.currentTimeMillis()));
        db.userDao().insert(user);
        return user;
    }

    public User login(String email, String password) throws Exception {
        User user = db.userDao().findByEmail(email);
        if (user == null) throw new Exception("Email không tồn tại");
        if (!user.password.equals(password)) throw new Exception("Mật khẩu không đúng");
        saveCurrentUserId(user.id);
        return user;
    }

    public void logout() {
        prefs.edit().remove("current_user_id").apply();
    }

    public User getCurrentUser() {
        String id = prefs.getString("current_user_id", null);
        if (id == null) return null;
        return db.userDao().findById(id);
    }

    private void saveCurrentUserId(String id) {
        prefs.edit().putString("current_user_id", id).apply();
    }

    public boolean isLoggedIn() { return prefs.contains("current_user_id"); }
    public boolean isOnboardingDone() { return prefs.getBoolean("onboarding_done", false); }
    public void setOnboardingDone() { prefs.edit().putBoolean("onboarding_done", true).apply(); }

    // ─── Card Sets ───────────────────────────────────────
    public LiveData<List<CardSet>> getAllSets() { return db.cardSetDao().getAllSets(); }
    public List<CardSet> getAllSetsSync() { return db.cardSetDao().getAllSetsSync(); }
    public CardSet getSetById(String id) { return db.cardSetDao().getSetById(id); }

    // ─── Flashcards ──────────────────────────────────────
    public LiveData<List<Flashcard>> getCardsForSet(String setId) { return db.flashcardDao().getCardsForSet(setId); }
    public List<Flashcard> getCardsForSetSync(String setId) { return db.flashcardDao().getCardsForSetSync(setId); }
    public LiveData<List<Flashcard>> searchCards(String query) { return db.flashcardDao().searchCards(query); }

    // ─── Folders ─────────────────────────────────────────
    public LiveData<List<Folder>> getAllFolders() { return db.folderDao().getAllFolders(); }
    public List<Folder> getAllFoldersSync() { return db.folderDao().getAllFoldersSync(); }

    public Folder createFolder(String name, String emoji, String color) {
        Folder folder = new Folder(UUID.randomUUID().toString(), name, emoji, color, "[]");
        db.folderDao().insert(folder);
        return folder;
    }

    public void deleteFolder(Folder folder) { db.folderDao().delete(folder); }

    public void addWordToFolder(String folderId, String wordId) {
        Folder folder = db.folderDao().getFolderById(folderId);
        if (folder == null) return;
        String[] arr = gson.fromJson(folder.wordIds, String[].class);
        List<String> ids = arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();
        if (!ids.contains(wordId)) {
            ids.add(wordId);
            db.folderDao().insert(new Folder(folder.id, folder.name, folder.emoji, folder.color, gson.toJson(ids)));
        }
    }

    // ─── Progress ────────────────────────────────────────
    public List<CardProgress> getProgressForSet(String setId) { return db.cardProgressDao().getProgressForSet(setId); }
    public void saveCardProgress(CardProgress progress) { db.cardProgressDao().insert(progress); }
    public void saveSetProgress(String setId, int known, int total) {
        db.setProgressDao().insert(new SetProgress(setId, known, total, System.currentTimeMillis()));
    }
    public LiveData<List<SetProgress>> getAllSetProgress() { return db.setProgressDao().getAllProgress(); }
    public List<SetProgress> getAllSetProgressSync() { return db.setProgressDao().getAllProgressSync(); }
    public int getKnownCount(String setId) { return db.cardProgressDao().getKnownCountForSet(setId); }

    // ─── Sessions ────────────────────────────────────────
    public void saveSession(String setId, int cardsStudied, int known) {
        db.studySessionDao().insert(new StudySession(setId, System.currentTimeMillis(), cardsStudied, known));
    }
    public int getTotalSessions() { return db.studySessionDao().getTotalSessions(); }

    // ─── Streak ──────────────────────────────────────────
    public int getCurrentStreak() { return prefs.getInt("streak", 0); }

    public void updateStreak() {
        long lastStudy = prefs.getLong("last_study_date", 0);
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        long lastDay = lastStudy / (1000 * 60 * 60 * 24);
        long diff = today - lastDay;
        int streak;
        if (diff == 0) streak = prefs.getInt("streak", 1);
        else if (diff == 1) streak = prefs.getInt("streak", 0) + 1;
        else streak = 1;
        prefs.edit().putInt("streak", streak).putLong("last_study_date", System.currentTimeMillis()).apply();
    }
}
