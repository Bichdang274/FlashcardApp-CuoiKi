package com.example.flashcardapp.ui.study;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentStudyBinding;
import com.example.flashcardapp.viewmodel.StudyViewModel;
import java.util.Locale;

public class StudyFragment extends Fragment {
    private FragmentStudyBinding b;
    private StudyViewModel vm;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingSpeech = null;
    private boolean isFlipAnimating = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentStudyBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(StudyViewModel.class);
        setupTTS();

        String setId = getArguments() != null ? getArguments().getString("setId") : null;
        String folderId = getArguments() != null ? getArguments().getString("folderId") : null;
        if (setId != null && !setId.isEmpty()) vm.loadSet(setId);
        else if (folderId != null && !folderId.isEmpty()) vm.loadFolder(folderId);

        setupClickListeners(view);
        observeViewModel();
    }

    private void setupTTS() {
        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault());
                }
                tts.setSpeechRate(0.85f);
                ttsReady = true;
                if (pendingSpeech != null) {
                    tts.speak(pendingSpeech, TextToSpeech.QUEUE_FLUSH, null, "speak");
                    pendingSpeech = null;
                }
            }
        });
    }

    private void setupClickListeners(View view) {
        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.cardFront.setOnClickListener(v -> flipCard());
        b.cardBack.setOnClickListener(v -> flipCard());
        b.btnSpeak.setOnClickListener(v -> {
            boolean soundOn = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("sound", true);
            if (soundOn) {
                com.example.flashcardapp.data.model.FlashcardDto card = vm.getCurrentCard();
                if (card != null) {
                    if (ttsReady) tts.speak(card.english, TextToSpeech.QUEUE_FLUSH, null, "speak");
                    else pendingSpeech = card.english;
                }
            }
        });
        b.btnKnow.setOnClickListener(v -> vm.markKnown());
        b.btnLearning.setOnClickListener(v -> vm.markLearning());
        b.btnPrev.setOnClickListener(v -> vm.goPrev());
        b.btnNext.setOnClickListener(v -> vm.goNext());
        b.btnShuffle.setOnClickListener(v -> vm.shuffle());
        b.btnRestart.setOnClickListener(v -> vm.restart());
        b.btnHome.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    private void observeViewModel() {
        vm.cardSet.observe(getViewLifecycleOwner(), set -> b.tvSetTitle.setText(set != null ? set.title : "Học thẻ"));

        vm.cards.observe(getViewLifecycleOwner(), cards -> {
            b.studyContent.setVisibility(View.VISIBLE);
            Integer idx = vm.currentIndex.getValue();
            int i = idx != null ? idx : 0;
            updateProgress(i, cards.size());
            updateCard(i);
        });

        vm.currentIndex.observe(getViewLifecycleOwner(), idx -> {
            if (vm.cards.getValue() == null) return;
            int size = vm.cards.getValue().size();
            updateProgress(idx, size);
            updateCard(idx);
        });

        vm.isFlipped.observe(getViewLifecycleOwner(), flipped -> {
            if (Boolean.TRUE.equals(flipped)) {
                b.cardFront.setVisibility(View.GONE);
                b.cardBack.setVisibility(View.VISIBLE);
                b.layoutRateButtons.setVisibility(View.VISIBLE);
            } else {
                b.cardFront.setVisibility(View.VISIBLE);
                b.cardBack.setVisibility(View.GONE);
                b.layoutRateButtons.setVisibility(View.GONE);
            }
        });

        vm.showComplete.observe(getViewLifecycleOwner(), done -> { if (Boolean.TRUE.equals(done)) showCompleteScreen(); });

        vm.sessionStats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                b.tvKnownCount.setText(String.valueOf(stats.first));
                b.tvLearningCount.setText(String.valueOf(stats.second));
            }
        });
    }

    private void updateProgress(int idx, int total) {
        b.tvProgress.setText((idx + 1) + "/" + total);
        b.progressBar.setProgress(total > 0 ? ((idx + 1) * 100 / total) : 0);
    }

    private void updateCard(int idx) {
        if (vm.cards.getValue() == null || idx >= vm.cards.getValue().size()) return;
        com.example.flashcardapp.data.model.FlashcardDto card = vm.cards.getValue().get(idx);
        b.tvEmoji.setText(card.emoji);
        b.tvEnglish.setText(card.english);
        b.tvPhonetic.setText(card.phonetic);
        b.tvVietnamese.setText(card.vietnamese);
        b.tvExample.setText(card.example);
        b.tvExampleVi.setText(card.exampleVi);
        b.cardFront.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.card_in));
    }

    private void flipCard() {
        if (isFlipAnimating) return;
        isFlipAnimating = true;
        Animation flipOut = AnimationUtils.loadAnimation(getContext(), R.anim.flip_out);
        Animation flipIn = AnimationUtils.loadAnimation(getContext(), R.anim.flip_in);
        flipOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) {}
            @Override public void onAnimationRepeat(Animation a) {}
            @Override public void onAnimationEnd(Animation a) {
                vm.flip();
                View target = Boolean.TRUE.equals(vm.isFlipped.getValue()) ? b.cardBack : b.cardFront;
                target.startAnimation(flipIn);
                isFlipAnimating = false;
            }
        });
        View current = Boolean.TRUE.equals(vm.isFlipped.getValue()) ? b.cardBack : b.cardFront;
        current.startAnimation(flipOut);
    }

    private void showCompleteScreen() {
        b.studyContent.setVisibility(View.GONE);
        b.completeScreen.setVisibility(View.VISIBLE);
        android.util.Pair<Integer, Integer> stats = vm.sessionStats.getValue();
        int known = stats != null ? stats.first : 0;
        int learning = stats != null ? stats.second : 0;
        int total = vm.cards.getValue() != null ? vm.cards.getValue().size() : 0;
        int pct = total > 0 ? (known * 100 / total) : 0;
        b.tvCompleteEmoji.setText(pct >= 80 ? "🎉" : pct >= 50 ? "👍" : "💪");
        b.tvCompleteTitle.setText(pct >= 80 ? "Xuất sắc!" : pct >= 50 ? "Tốt lắm!" : "Cố lên!");
        b.tvCompleteSubtitle.setText("Bạn đã thuộc " + pct + "% số thẻ");
        b.tvStatsKnown.setText("✅ Thuộc: " + known + " thẻ");
        b.tvStatsLearning.setText("📖 Đang học: " + learning + " thẻ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        b = null;
    }
}
