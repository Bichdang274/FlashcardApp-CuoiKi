package com.example.flashcardapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.data.model.*;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<List<CardSetDto>> _cardSets = new MutableLiveData<>();
    public final LiveData<List<CardSetDto>> cardSets = _cardSets;
    private final MutableLiveData<List<FolderDto>> _folders = new MutableLiveData<>();
    public final LiveData<List<FolderDto>> folders = _folders;
    private final MutableLiveData<Map<String, Integer>> _progressMap = new MutableLiveData<>();
    public final LiveData<Map<String, Integer>> progressMap = _progressMap;
    private final MutableLiveData<Map<String, Integer>> _cardCounts = new MutableLiveData<>();
    public final LiveData<Map<String, Integer>> cardCounts = _cardCounts;
    private final MutableLiveData<ProfileDto> _currentUser = new MutableLiveData<>();
    public final LiveData<ProfileDto> currentUser = _currentUser;
    private final MutableLiveData<GlobalStats> _stats = new MutableLiveData<>();
    public final LiveData<GlobalStats> stats = _stats;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
        refresh();
    }

    public void refresh() {
        executor.execute(() -> {
            _currentUser.postValue(repo.getCurrentProfile());
            _cardSets.postValue(repo.getCardSets());
            _folders.postValue(repo.getFolders());
            List<CardProgressDto> allProgress = repo.getAllProgress();
            Map<String, Integer> pm = new HashMap<>();
            for (CardProgressDto p : allProgress) {
                if (p.known) {
                    Integer prev = pm.get(p.setId);
                    pm.put(p.setId, prev == null ? 1 : prev + 1);
                }
            }
            _progressMap.postValue(pm);
            int wordsLearned = 0;
            for (CardProgressDto p : allProgress) if (p.known) wordsLearned++;
            _stats.postValue(new GlobalStats(repo.getCurrentStreak(), repo.getTotalSessions(), wordsLearned));
            _cardCounts.postValue(repo.getCardCountsPerSet());
        });
    }

    public void createFolder(String name) { createFolder(name, "📁", "#A855F7,#EC4899"); }

    public void createFolder(String name, String emoji, String color) {
        executor.execute(() -> {
            repo.createFolder(name, emoji, color);
            _folders.postValue(repo.getFolders());
        });
    }

    public void deleteFolder(String folderId) {
        executor.execute(() -> {
            repo.deleteFolder(folderId);
            _folders.postValue(repo.getFolders());
        });
    }

    @Override
    protected void onCleared() { super.onCleared(); executor.shutdown(); }

    public static class GlobalStats {
        public final int streak, sessions, wordsLearned;
        public GlobalStats(int streak, int sessions, int wordsLearned) {
            this.streak = streak; this.sessions = sessions; this.wordsLearned = wordsLearned;
        }
    }
}
