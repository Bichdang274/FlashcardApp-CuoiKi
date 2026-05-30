package com.example.flashcardapp.viewmodel;

import android.app.Application;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.data.model.*;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<List<FlashcardDto>> _cards = new MutableLiveData<>();
    public final LiveData<List<FlashcardDto>> cards = _cards;
    private final MutableLiveData<Integer> _currentIndex = new MutableLiveData<>(0);
    public final LiveData<Integer> currentIndex = _currentIndex;
    private final MutableLiveData<Boolean> _isFlipped = new MutableLiveData<>(false);
    public final LiveData<Boolean> isFlipped = _isFlipped;
    private final MutableLiveData<Boolean> _showComplete = new MutableLiveData<>(false);
    public final LiveData<Boolean> showComplete = _showComplete;
    private final MutableLiveData<Pair<Integer, Integer>> _sessionStats = new MutableLiveData<>(new Pair<>(0, 0));
    public final LiveData<Pair<Integer, Integer>> sessionStats = _sessionStats;
    private final MutableLiveData<CardSetDto> _cardSet = new MutableLiveData<>();
    public final LiveData<CardSetDto> cardSet = _cardSet;
    private String setId = "";
    private final Map<String, CardProgressDto> progressMap = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StudyViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
    }

    public FlashcardDto getCurrentCard() {
        List<FlashcardDto> c = _cards.getValue();
        Integer idx = _currentIndex.getValue();
        if (c == null || idx == null || idx >= c.size()) return null;
        return c.get(idx);
    }

    public void loadSet(String setId) {
        this.setId = setId;
        executor.execute(() -> {
            List<CardSetDto> sets = repo.getCardSets();
            for (CardSetDto s : sets) if (s.id.equals(setId)) { _cardSet.postValue(s); break; }
            _cards.postValue(repo.getFlashcardsForSet(setId));
            for (CardProgressDto p : repo.getProgressForSet(setId)) progressMap.put(p.cardId, p);
        });
    }

    public void loadFolder(String folderId) {
        executor.execute(() -> {
            List<FolderDto> folders = repo.getFolders();
            FolderDto folder = null;
            for (FolderDto f : folders) if (folderId.equals(f.id)) { folder = f; break; }
            if (folder != null) {
                _cardSet.postValue(new CardSetDto("folder", folder.name, folder.name,
                    folder.emoji, folder.color, "#A855F7"));
                _cards.postValue(repo.getFlashcardsForFolder(folder.wordIds));
            }
        });
    }

    public void flip() {
        Boolean flipped = _isFlipped.getValue();
        _isFlipped.setValue(flipped == null || !flipped);
    }

    public void markKnown() { rate(5); }
    public void markLearning() { rate(1); }

    private void rate(int quality) {
        FlashcardDto card = getCurrentCard();
        if (card == null) return;
        CardProgressDto p = progressMap.get(card.id);
        if (p == null) p = new CardProgressDto(null, null, card.id, setId, false, 0, 2.5, 1);
        CardProgressDto updated = updateSM2(p, quality);
        progressMap.put(card.id, updated);
        final CardProgressDto toSave = updated;
        executor.execute(() -> repo.saveCardProgress(toSave));
        Pair<Integer, Integer> stats = _sessionStats.getValue();
        int k = stats != null ? stats.first : 0;
        int l = stats != null ? stats.second : 0;
        _sessionStats.setValue(quality >= 3 ? new Pair<>(k + 1, l) : new Pair<>(k, l + 1));
        goNext();
    }

    public void goNext() {
        _isFlipped.setValue(false);
        int idx = _currentIndex.getValue() != null ? _currentIndex.getValue() : 0;
        List<FlashcardDto> c = _cards.getValue();
        if (c != null && idx < c.size() - 1) {
            _currentIndex.setValue(idx + 1);
        } else {
            _showComplete.setValue(true);
            finishSession();
        }
    }

    public void goPrev() {
        int idx = _currentIndex.getValue() != null ? _currentIndex.getValue() : 0;
        if (idx > 0) { _isFlipped.setValue(false); _currentIndex.setValue(idx - 1); }
    }

    public void shuffle() {
        List<FlashcardDto> c = _cards.getValue();
        if (c != null) {
            List<FlashcardDto> shuffled = new ArrayList<>(c);
            Collections.shuffle(shuffled);
            _cards.setValue(shuffled);
        }
        _currentIndex.setValue(0);
        _isFlipped.setValue(false);
    }

    public void restart() {
        _currentIndex.setValue(0);
        _isFlipped.setValue(false);
        _sessionStats.setValue(new Pair<>(0, 0));
        _showComplete.setValue(false);
    }

    private void finishSession() {
        List<FlashcardDto> c = _cards.getValue();
        Pair<Integer, Integer> stats = _sessionStats.getValue();
        int total = c != null ? c.size() : 0;
        int known = stats != null ? stats.first : 0;
        String sid = setId.isEmpty() ? "folder_study" : setId;
        executor.execute(() -> repo.saveSession(sid, total, known));
    }

    private CardProgressDto updateSM2(CardProgressDto p, int quality) {
        float ef = (float) p.easeFactor;
        int reps = p.repetitions;
        int interval = p.intervalDays;
        if (quality >= 3) {
            if (reps == 0) interval = 1;
            else if (reps == 1) interval = 6;
            else interval = (int)(interval * ef);
            reps++;
        } else {
            reps = 0;
            interval = 1;
        }
        ef = ef + 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f);
        if (ef < 1.3f) ef = 1.3f;
        return p.withSM2(quality >= 3, reps, ef, interval);
    }

    @Override
    protected void onCleared() { super.onCleared(); executor.shutdown(); }
}
