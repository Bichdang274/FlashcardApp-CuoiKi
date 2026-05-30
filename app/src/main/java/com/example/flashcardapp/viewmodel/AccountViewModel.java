package com.example.flashcardapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.data.model.*;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<ProfileDto> _user = new MutableLiveData<>();
    public final LiveData<ProfileDto> user = _user;
    private final MutableLiveData<AccountStats> _stats = new MutableLiveData<>();
    public final LiveData<AccountStats> stats = _stats;
    private final MutableLiveData<List<FolderDto>> _folders = new MutableLiveData<>();
    public final LiveData<List<FolderDto>> folders = _folders;
    private final MutableLiveData<UpdateState> _updateState = new MutableLiveData<>(UpdateState.IDLE);
    public final LiveData<UpdateState> updateState = _updateState;
    private final MutableLiveData<String> _updateError = new MutableLiveData<>();
    public final LiveData<String> updateError = _updateError;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public enum UpdateState { IDLE, LOADING, SUCCESS, ERROR }

    public AccountViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
        loadData();
    }

    public void loadData() {
        executor.execute(() -> {
            _user.postValue(repo.getCurrentProfile());
            _folders.postValue(repo.getFolders());
            List<CardProgressDto> all = repo.getAllProgress();
            int totalCards = repo.getAllFlashcards().size();
            int wordsLearned = 0;
            for (CardProgressDto p : all) if (p.known) wordsLearned++;
            _stats.postValue(new AccountStats(wordsLearned, repo.getTotalSessions(), repo.getCurrentStreak(), totalCards));
        });
    }

    public void createFolder(String name, String emoji, String color) {
        executor.execute(() -> {
            repo.createFolder(name, emoji, color);
            loadData();
        });
    }

    public void deleteFolder(String folderId) {
        executor.execute(() -> {
            repo.deleteFolder(folderId);
            loadData();
        });
    }

    public void updateProfile(String name, String dob, String gender) {
        _updateState.postValue(UpdateState.LOADING);
        executor.execute(() -> {
            try {
                repo.updateProfile(name, dob, gender);
                _updateState.postValue(UpdateState.SUCCESS);
                loadData();
            } catch (Exception e) {
                _updateError.postValue(e.getMessage());
                _updateState.postValue(UpdateState.ERROR);
            }
        });
    }

    public Set<Long> getStudyDaySet() { return repo.getStudyDaySet(); }

    public void resetUpdateState() { _updateState.setValue(UpdateState.IDLE); }

    public void logout() { executor.execute(repo::signOut); }

    @Override
    protected void onCleared() { super.onCleared(); executor.shutdown(); }

    public static class AccountStats {
        public final int wordsLearned, sessions, streak, totalCards;
        public AccountStats(int wordsLearned, int sessions, int streak, int totalCards) {
            this.wordsLearned = wordsLearned; this.sessions = sessions;
            this.streak = streak; this.totalCards = totalCards;
        }
    }
}
