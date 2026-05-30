package com.example.flashcardapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.flashcardapp.R;
import com.example.flashcardapp.viewmodel.AuthViewModel;

public class SplashFragment extends Fragment {
    private AuthViewModel authVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authVM = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            if (authVM.isLoggedIn()) {
                Navigation.findNavController(view).navigate(R.id.action_splash_to_main);
            } else if (authVM.isOnboardingDone()) {
                Navigation.findNavController(view).navigate(R.id.action_splash_to_login);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_splash_to_onboarding);
            }
        }, 1200);
    }
}
