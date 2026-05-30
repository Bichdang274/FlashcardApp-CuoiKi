package com.example.flashcardapp.ui.set;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.flashcardapp.R;
import com.example.flashcardapp.adapter.FlashcardListAdapter;
import com.example.flashcardapp.databinding.FragmentSetDetailBinding;
import com.example.flashcardapp.viewmodel.StudyViewModel;

public class SetDetailFragment extends Fragment {
    private FragmentSetDetailBinding b;
    private StudyViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentSetDetailBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(StudyViewModel.class);
        String setId = getArguments() != null ? getArguments().getString("setId") : null;
        if (setId == null) return;
        vm.loadSet(setId);

        FlashcardListAdapter adapter = new FlashcardListAdapter();
        b.rvCards.setAdapter(adapter);
        b.rvCards.setLayoutManager(new LinearLayoutManager(getContext()));

        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.btnStudy.setOnClickListener(v -> {
            Bundle a = new Bundle(); a.putString("setId", setId);
            Navigation.findNavController(view).navigate(R.id.studyDest, a);
        });
        b.btnQuiz.setOnClickListener(v -> {
            Bundle a = new Bundle(); a.putString("setId", setId);
            Navigation.findNavController(view).navigate(R.id.quizDest, a);
        });
        b.btnGame.setOnClickListener(v -> {
            Bundle a = new Bundle(); a.putString("setId", setId);
            Navigation.findNavController(view).navigate(R.id.gameDest, a);
        });

        vm.cardSet.observe(getViewLifecycleOwner(), set -> {
            if (set != null) { b.tvTitle.setText(set.title); b.tvSubtitle.setText(set.titleVi); b.tvEmoji.setText(set.emoji); }
        });
        vm.cards.observe(getViewLifecycleOwner(), cards -> {
            adapter.submitList(cards);
            b.tvCardCount.setText(cards.size() + " thẻ");
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
