package com.example.flashcardapp.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.flashcardapp.databinding.FragmentOnboardingPageBinding;

public class OnboardingPageFragment extends Fragment {
    private FragmentOnboardingPageBinding b;

    public static OnboardingPageFragment newInstance(OnboardingData data) {
        OnboardingPageFragment f = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putString("emoji", data.emoji);
        args.putString("title", data.title);
        args.putString("subtitle", data.subtitle);
        args.putString("tip", data.tip);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentOnboardingPageBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            b.tvEmoji.setText(getArguments().getString("emoji"));
            b.tvTitle.setText(getArguments().getString("title"));
            b.tvSubtitle.setText(getArguments().getString("subtitle"));
            String tip = getArguments().getString("tip");
            b.tvTip.setText(tip != null ? tip : "");
        }
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
