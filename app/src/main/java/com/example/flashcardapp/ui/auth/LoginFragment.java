package com.example.flashcardapp.ui.auth;

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
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentLoginBinding;
import com.example.flashcardapp.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding b;
    private AuthViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentLoginBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        b.ivLogo.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_up));
        b.cardForm.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_up));

        b.btnLogin.setOnClickListener(v -> {
            String email = b.etEmail.getText() != null ? b.etEmail.getText().toString().trim() : "";
            String pass = b.etPassword.getText() != null ? b.etPassword.getText().toString() : "";
            if (email.isEmpty()) { b.etEmail.setError("Vui lòng nhập email"); return; }
            if (pass.isEmpty()) { b.etPassword.setError("Vui lòng nhập mật khẩu"); return; }
            vm.login(email, pass);
        });

        b.tvRegister.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.registerDest));

        vm.authState.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.Loading) {
                b.btnLogin.setEnabled(false);
                b.progressBar.setVisibility(View.VISIBLE);
                b.tvBtnText.setText("");
            } else if (state instanceof AuthViewModel.AuthState.Success) {
                Navigation.findNavController(view).navigate(R.id.action_login_to_main);
            } else if (state instanceof AuthViewModel.AuthState.Error) {
                b.btnLogin.setEnabled(true);
                b.progressBar.setVisibility(View.GONE);
                b.tvBtnText.setText("Đăng nhập");
                Snackbar.make(view, ((AuthViewModel.AuthState.Error) state).message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
