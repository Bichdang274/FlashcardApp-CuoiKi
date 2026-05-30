package com.example.flashcardapp.ui.onboarding;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentOnboardingBinding;
import com.example.flashcardapp.viewmodel.AuthViewModel;
import java.util.Arrays;
import java.util.List;

public class OnboardingFragment extends Fragment {
    private FragmentOnboardingBinding b;
    private AuthViewModel authVM;

    private final List<OnboardingData> pages = Arrays.asList(
        new OnboardingData("✨", "Chào mừng đến với\nFlashCard English!",
            "Ứng dụng học từ vựng tiếng Anh hiệu quả nhất với phương pháp học thông minh và thú vị.",
            "🌟 Hơn 50 từ vựng theo chủ đề", "#7C3AED", "#6D28D9", "#4338CA"),
        new OnboardingData("📖", "Học với\nFlash Cards",
            "Lật thẻ 3D để khám phá nghĩa, phiên âm và ví dụ sử dụng của từng từ vựng.",
            "🎴 Hiệu ứng lật thẻ 3D mượt mà", "#059669", "#0D9488", "#0891B2"),
        new OnboardingData("⚡", "Kiểm tra\nKiến thức",
            "Làm bài quiz trắc nghiệm có đồng hồ đếm ngược và trò chơi nối từ thú vị.",
            "⚡ Hệ thống combo & streak", "#EA580C", "#DC2626", "#BE185D"),
        new OnboardingData("🏆", "Theo dõi\nTiến độ",
            "Thuật toán Spaced Repetition giúp bạn ôn tập đúng lúc, ghi nhớ lâu dài hiệu quả hơn.",
            "📊 Spaced Repetition thông minh", "#2563EB", "#4338CA", "#7C3AED")
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentOnboardingBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authVM = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        b.viewPager.setAdapter(new OnboardingPagerAdapter(this, pages));
        b.dotsIndicator.attachTo(b.viewPager);
        updateRootGradient(pages.get(0));

        b.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                boolean isLast = position == pages.size() - 1;
                b.btnNext.setText(isLast ? "✨ Bắt đầu học ngay!" : "Tiếp theo");
                updateRootGradient(pages.get(position));
            }
        });

        b.btnNext.setOnClickListener(v -> {
            int current = b.viewPager.getCurrentItem();
            if (current < pages.size() - 1) {
                b.viewPager.setCurrentItem(current + 1);
            } else {
                authVM.setOnboardingDone();
                Navigation.findNavController(view).navigate(R.id.action_onboarding_to_login);
            }
        });

        b.tvSkip.setOnClickListener(v -> {
            authVM.setOnboardingDone();
            Navigation.findNavController(view).navigate(R.id.action_onboarding_to_login);
        });
    }

    private void updateRootGradient(OnboardingData page) {
        try {
            GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor(page.colorStart),
                          Color.parseColor(page.colorMid),
                          Color.parseColor(page.colorEnd)});
            b.getRoot().setBackground(grad);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
