package com.example.flashcardapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentMainBinding;
import com.example.flashcardapp.viewmodel.AuthViewModel;

public class MainFragment extends Fragment {
    private FragmentMainBinding b;
    private AuthViewModel authVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentMainBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authVM = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        NavHostFragment navHost = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.innerNavHost);
        if (navHost != null) {
            NavigationUI.setupWithNavController(b.bottomNav, navHost.getNavController());
        }

        authVM.authState.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.LoggedOut) {
                Navigation.findNavController(view).navigate(R.id.action_main_to_login);
            }
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
