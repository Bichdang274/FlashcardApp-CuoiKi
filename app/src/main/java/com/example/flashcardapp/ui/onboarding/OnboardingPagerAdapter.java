package com.example.flashcardapp.ui.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class OnboardingPagerAdapter extends FragmentStateAdapter {
    private final List<OnboardingData> pages;

    public OnboardingPagerAdapter(@NonNull Fragment fragment, List<OnboardingData> pages) {
        super(fragment);
        this.pages = pages;
    }

    @Override
    public int getItemCount() { return pages.size(); }

    @NonNull @Override
    public Fragment createFragment(int position) {
        return OnboardingPageFragment.newInstance(pages.get(position));
    }
}
