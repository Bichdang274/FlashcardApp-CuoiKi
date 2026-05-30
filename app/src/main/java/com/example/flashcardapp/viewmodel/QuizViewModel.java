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

public class QuizViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<List<QuizQuestion>> _questions = new MutableLiveData<>();
    public final LiveData<List<QuizQuestion>> questions = _questions;
    private final MutableLiveData<Integer> _currentIndex = new MutableLiveData<>(0);
    public final LiveData<Integer> currentIndex = _currentIndex;
    private final MutableLiveData<Integer> _selectedAnswer = new MutableLiveData<>(null);
    public final LiveData<Integer> selectedAnswer = _selectedAnswer;
    private final MutableLiveData<Integer> _score = new MutableLiveData<>(0);
    public final LiveData<Integer> score = _score;
    private final MutableLiveData<Boolean> _showResult = new MutableLiveData<>(false);
    public final LiveData<Boolean> showResult = _showResult;
    private final MutableLiveData<Integer> _timeLeft = new MutableLiveData<>(15);
    public final LiveData<Integer> timeLeft = _timeLeft;
    private final MutableLiveData<Integer> _streak = new MutableLiveData<>(0);
    public final LiveData<Integer> streak = _streak;
    private final MutableLiveData<Integer> _bestStreak = new MutableLiveData<>(0);
    public final LiveData<Integer> bestStreak = _bestStreak;
    private final MutableLiveData<Boolean> _showStreak = new MutableLiveData<>(false);
    public final LiveData<Boolean> showStreak = _showStreak;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable timerRunnable;
    private int timeLeftValue = 15;
    private int wrongCount = 0;

    public QuizViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
    }

    public QuizQuestion getCurrentQuestion() {
        List<QuizQuestion> q = _questions.getValue();
        Integer idx = _currentIndex.getValue();
        if (q == null || idx == null || idx >= q.size()) return null;
        return q.get(idx);
    }

    public void loadSet(String setId) {
        executor.execute(() -> buildQuestions(repo.getFlashcardsForSet(setId)));
    }

    public void loadFolder(String folderId) {
        executor.execute(() -> {
            List<FolderDto> folders = repo.getFolders();
            FolderDto folder = null;
            for (FolderDto f : folders) if (folderId.equals(f.id)) { folder = f; break; }
            buildQuestions(repo.getFlashcardsForFolder(folder != null ? folder.wordIds : Collections.emptyList()));
        });
    }

    private void buildQuestions(List<FlashcardDto> cards) {
        if (cards.isEmpty()) return;
        List<FlashcardDto> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        List<QuizQuestion> qs = new ArrayList<>();
        for (FlashcardDto card : shuffled) {
            List<FlashcardDto> wrong = new ArrayList<>();
            for (FlashcardDto c : cards) if (!c.id.equals(card.id)) wrong.add(c);
            Collections.shuffle(wrong);
            if (wrong.size() > 3) wrong = wrong.subList(0, 3);
            String type = Math.random() > 0.5 ? "en-to-vi" : "vi-to-en";
            List<FlashcardDto> options = new ArrayList<>(wrong);
            options.add(card);
            Collections.shuffle(options);
            qs.add(new QuizQuestion(card, options, options.indexOf(card), type));
        }
        // Post to main thread since we need to start timer from there
        handler.post(() -> {
            _questions.setValue(qs);
            _currentIndex.setValue(0);
            _selectedAnswer.setValue(null);
            _score.setValue(0);
            _showResult.setValue(false);
            _streak.setValue(0);
            _bestStreak.setValue(0);
            wrongCount = 0;
            startTimer();
        });
    }

    private void startTimer() {
        cancelTimer();
        timeLeftValue = 15;
        _timeLeft.setValue(15);
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timeLeftValue--;
                _timeLeft.setValue(timeLeftValue);
                if (timeLeftValue <= 0) {
                    _selectedAnswer.setValue(-1);
                    Integer s = _streak.getValue();
                    _streak.setValue(0);
                    wrongCount++;
                    handler.postDelayed(() -> advanceQuestion(), 1200);
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void cancelTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }

    public void selectAnswer(int index) {
        if (_selectedAnswer.getValue() != null) return;
        cancelTimer();
        _selectedAnswer.setValue(index);
        QuizQuestion q = getCurrentQuestion();
        boolean isCorrect = q != null && index == q.correctIndex;
        if (isCorrect) {
            Integer s = _score.getValue();
            _score.setValue(s == null ? 1 : s + 1);
            Integer newStreak = (_streak.getValue() == null ? 0 : _streak.getValue()) + 1;
            _streak.setValue(newStreak);
            Integer best = _bestStreak.getValue();
            if (best == null || newStreak > best) _bestStreak.setValue(newStreak);
            if (newStreak > 1 && newStreak % 3 == 0) {
                _showStreak.setValue(true);
                handler.postDelayed(() -> _showStreak.setValue(false), 1500);
            }
            handler.postDelayed(this::advanceQuestion, 1200);
        } else {
            Integer s = _streak.getValue();
            _streak.setValue(0);
            wrongCount++;
            handler.postDelayed(this::advanceQuestion, 1500);
        }
    }

    private void advanceQuestion() {
        _selectedAnswer.setValue(null);
        int idx = _currentIndex.getValue() != null ? _currentIndex.getValue() : 0;
        List<QuizQuestion> qs = _questions.getValue();
        if (qs != null && idx < qs.size() - 1) {
            _currentIndex.setValue(idx + 1);
            startTimer();
        } else {
            _showResult.setValue(true);
        }
    }

    public void nextQuestion() { advanceQuestion(); }

    public void restart() {
        cancelTimer();
        List<QuizQuestion> qs = _questions.getValue();
        if (qs != null) {
            List<FlashcardDto> cards = new ArrayList<>();
            for (QuizQuestion q : qs) cards.add(q.card);
            buildQuestions(cards);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimer();
        executor.shutdown();
    }

    public static class QuizQuestion {
        public final FlashcardDto card;
        public final List<FlashcardDto> options;
        public final int correctIndex;
        public final String type;
        public QuizQuestion(FlashcardDto card, List<FlashcardDto> options, int correctIndex, String type) {
            this.card = card; this.options = options; this.correctIndex = correctIndex; this.type = type;
        }
    }
}
