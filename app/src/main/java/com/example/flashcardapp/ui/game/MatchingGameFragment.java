package com.example.flashcardapp.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentMatchingGameBinding;
import com.example.flashcardapp.viewmodel.MatchingGameViewModel;
import java.util.Locale;

public class MatchingGameFragment extends Fragment {
    private FragmentMatchingGameBinding b;
    private MatchingGameViewModel vm;
    private TileAdapter tileAdapter;
    private String setId = "";
    private String folderId = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentMatchingGameBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(MatchingGameViewModel.class);

        setId = getArguments() != null ? getArguments().getString("setId", "") : "";
        folderId = getArguments() != null ? getArguments().getString("folderId", "") : "";

        tileAdapter = new TileAdapter(tileId -> vm.onTileClick(tileId));
        b.rvTiles.setAdapter(tileAdapter);
        b.rvTiles.setLayoutManager(new GridLayoutManager(getContext(), 3));

        if (!setId.isEmpty()) vm.loadSet(setId);
        else if (!folderId.isEmpty()) vm.loadFolder(folderId);

        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.btnRestart.setOnClickListener(v -> reload());
        b.btnPlayAgain.setOnClickListener(v -> { b.completionOverlay.setVisibility(View.GONE); reload(); });
        b.btnBackHome.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        observeViewModel();
    }

    private void reload() {
        if (!setId.isEmpty()) vm.loadSet(setId);
        else if (!folderId.isEmpty()) vm.loadFolder(folderId);
    }

    private void observeViewModel() {
        vm.tiles.observe(getViewLifecycleOwner(), tiles -> tileAdapter.submitList(tiles));

        vm.matched.observe(getViewLifecycleOwner(), matched -> {
            int total = (vm.tiles.getValue() != null ? vm.tiles.getValue().size() : 0) / 2;
            int size = matched != null ? matched.size() : 0;
            b.tvPairsCounter.setText(size + "/" + total + " cặp");
            b.tvPairs.setText(size + "/" + total);
            b.progressBar.setProgress(total > 0 ? (size * 100 / total) : 0);
            tileAdapter.updateMatched(matched != null ? matched : new java.util.HashSet<>());
        });

        vm.selectedId.observe(getViewLifecycleOwner(), selected -> tileAdapter.updateSelected(selected));

        vm.wrongIds.observe(getViewLifecycleOwner(), wrong -> tileAdapter.updateWrong(wrong != null ? wrong : java.util.Collections.emptyList()));

        vm.time.observe(getViewLifecycleOwner(), time -> {
            if (time == null) return;
            int min = time / 60, sec = time % 60;
            b.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
        });

        vm.moves.observe(getViewLifecycleOwner(), moves -> b.tvMoves.setText(moves + " bước"));

        vm.showCombo.observe(getViewLifecycleOwner(), show -> {
            if (Boolean.TRUE.equals(show)) {
                Integer combo = vm.combo.getValue();
                b.tvComboOverlay.setText("⚡ " + (combo != null ? combo : 0) + " combo!");
                b.tvComboOverlay.setVisibility(View.VISIBLE);
                b.tvComboOverlay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_up));
            } else {
                b.tvComboOverlay.setVisibility(View.GONE);
            }
        });

        vm.isFinished.observe(getViewLifecycleOwner(), finished -> { if (Boolean.TRUE.equals(finished)) showCompletion(); });

        vm.title.observe(getViewLifecycleOwner(), title -> b.tvResultTitle.setText(title));
    }

    private void showCompletion() {
        Integer time = vm.time.getValue();
        Integer moves = vm.moves.getValue();
        Integer matchedSize = vm.matched.getValue() != null ? vm.matched.getValue().size() : 0;
        int t = time != null ? time : 0;
        int m = moves != null ? moves : 0;
        int accuracy = m > 0 ? Math.min(matchedSize * 100 / m, 100) : 100;
        Integer bestCombo = vm.bestCombo.getValue();
        int min = t / 60, sec = t % 60;
        b.tvResultTime.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
        b.tvResultAccuracy.setText(accuracy + "%");
        b.tvResultCombo.setText((bestCombo != null ? bestCombo : 0) + "x");
        b.completionOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
