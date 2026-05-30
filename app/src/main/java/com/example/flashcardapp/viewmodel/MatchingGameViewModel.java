package com.example.flashcardapp.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.data.model.*;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchingGameViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<List<MatchingTile>> _tiles = new MutableLiveData<>();
    public final LiveData<List<MatchingTile>> tiles = _tiles;
    private final MutableLiveData<Set<String>> _matched = new MutableLiveData<>(new HashSet<>());
    public final LiveData<Set<String>> matched = _matched;
    private final MutableLiveData<String> _selectedId = new MutableLiveData<>(null);
    public final LiveData<String> selectedId = _selectedId;
    private final MutableLiveData<List<String>> _wrongIds = new MutableLiveData<>(Collections.emptyList());
    public final LiveData<List<String>> wrongIds = _wrongIds;
    private final MutableLiveData<Integer> _moves = new MutableLiveData<>(0);
    public final LiveData<Integer> moves = _moves;
    private final MutableLiveData<Integer> _time = new MutableLiveData<>(0);
    public final LiveData<Integer> time = _time;
    private final MutableLiveData<Boolean> _isFinished = new MutableLiveData<>(false);
    public final LiveData<Boolean> isFinished = _isFinished;
    private final MutableLiveData<Integer> _combo = new MutableLiveData<>(0);
    public final LiveData<Integer> combo = _combo;
    private final MutableLiveData<Integer> _bestCombo = new MutableLiveData<>(0);
    public final LiveData<Integer> bestCombo = _bestCombo;
    private final MutableLiveData<Boolean> _showCombo = new MutableLiveData<>(false);
    public final LiveData<Boolean> showCombo = _showCombo;
    private final MutableLiveData<String> _title = new MutableLiveData<>("");
    public final LiveData<String> title = _title;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable timerRunnable;

    public MatchingGameViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
    }

    public void loadSet(String setId) {
        executor.execute(() -> {
            List<CardSetDto> sets = repo.getCardSets();
            for (CardSetDto s : sets) if (s.id.equals(setId)) { _title.postValue(s.title); break; }
            List<FlashcardDto> cards = repo.getFlashcardsForSet(setId);
            handler.post(() -> buildTiles(cards));
        });
    }

    public void loadFolder(String folderId) {
        executor.execute(() -> {
            List<FolderDto> folders = repo.getFolders();
            FolderDto folder = null;
            for (FolderDto f : folders) if (folderId.equals(f.id)) { folder = f; break; }
            _title.postValue(folder != null ? folder.name : "");
            List<FlashcardDto> cards = repo.getFlashcardsForFolder(folder != null ? folder.wordIds : Collections.emptyList());
            handler.post(() -> buildTiles(cards));
        });
    }

    private void buildTiles(List<FlashcardDto> cards) {
        List<FlashcardDto> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        int count = Math.min(6, shuffled.size());
        List<MatchingTile> tileList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FlashcardDto card = shuffled.get(i);
            tileList.add(new MatchingTile("en-" + card.id, card.english, card.id, "english", card.emoji));
            tileList.add(new MatchingTile("vi-" + card.id, card.vietnamese, card.id, "vietnamese", null));
        }
        Collections.shuffle(tileList);
        _tiles.setValue(tileList);
        _matched.setValue(new HashSet<>());
        _selectedId.setValue(null);
        _wrongIds.setValue(Collections.emptyList());
        _moves.setValue(0);
        _combo.setValue(0);
        _isFinished.setValue(false);
        startTimer();
    }

    public void onTileClick(String tileId) {
        Set<String> matchedSet = _matched.getValue();
        List<String> wrongList = _wrongIds.getValue();
        List<MatchingTile> tileList = _tiles.getValue();
        if (tileList == null) return;

        MatchingTile tile = null;
        for (MatchingTile t : tileList) if (t.id.equals(tileId)) { tile = t; break; }
        if (tile == null) return;

        if (matchedSet != null && matchedSet.contains(tile.cardId)) return;
        if (wrongList != null && wrongList.contains(tileId)) return;

        String selected = _selectedId.getValue();
        if (tileId.equals(selected)) { _selectedId.setValue(null); return; }
        if (selected == null) { _selectedId.setValue(tileId); return; }

        MatchingTile firstTile = null;
        for (MatchingTile t : tileList) if (t.id.equals(selected)) { firstTile = t; break; }
        if (firstTile == null) return;

        Integer movesVal = _moves.getValue();
        _moves.setValue(movesVal == null ? 1 : movesVal + 1);

        if (firstTile.cardId.equals(tile.cardId) && !firstTile.type.equals(tile.type)) {
            Set<String> newMatched = matchedSet != null ? new HashSet<>(matchedSet) : new HashSet<>();
            newMatched.add(tile.cardId);
            _matched.setValue(newMatched);
            _selectedId.setValue(null);
            Integer c = _combo.getValue();
            int newCombo = (c == null ? 0 : c) + 1;
            _combo.setValue(newCombo);
            Integer best = _bestCombo.getValue();
            if (best == null || newCombo > best) _bestCombo.setValue(newCombo);
            if (newCombo >= 2) {
                _showCombo.setValue(true);
                handler.postDelayed(() -> _showCombo.setValue(false), 900);
            }
            if (newMatched.size() == tileList.size() / 2) {
                cancelTimer();
                _isFinished.setValue(true);
            }
        } else {
            _combo.setValue(0);
            final String sel = selected;
            final String tId = tileId;
            _wrongIds.setValue(Arrays.asList(sel, tId));
            handler.postDelayed(() -> {
                _wrongIds.setValue(Collections.emptyList());
                _selectedId.setValue(null);
            }, 700);
        }
    }

    private void startTimer() {
        cancelTimer();
        _time.setValue(0);
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                Boolean finished = _isFinished.getValue();
                if (finished != null && finished) return;
                Integer t = _time.getValue();
                _time.setValue(t == null ? 1 : t + 1);
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void cancelTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimer();
        executor.shutdown();
    }
}
