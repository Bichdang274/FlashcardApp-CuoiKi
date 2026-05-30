package com.example.flashcardapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentRegisterBinding;
import com.example.flashcardapp.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding b;
    private AuthViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentRegisterBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        b.btnRegister.setOnClickListener(v -> {
            String name = b.etName.getText() != null ? b.etName.getText().toString().trim() : "";
            String email = b.etEmail.getText() != null ? b.etEmail.getText().toString().trim() : "";
            String pass = b.etPassword.getText() != null ? b.etPassword.getText().toString() : "";
            String confirm = b.etConfirm.getText() != null ? b.etConfirm.getText().toString() : "";
            if (name.isEmpty()) { b.etName.setError("Vui lòng nhập tên"); return; }
            if (email.isEmpty()) { b.etEmail.setError("Vui lòng nhập email"); return; }
            if (pass.length() < 6) { b.etPassword.setError("Mật khẩu tối thiểu 6 ký tự"); return; }
            if (!pass.equals(confirm)) { b.etConfirm.setError("Mật khẩu không khớp"); return; }
            vm.register(name, email, pass);
        });

        b.tvLogin.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        vm.authState.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.Loading) {
                b.btnRegister.setEnabled(false);
                b.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof AuthViewModel.AuthState.Registered) {
                b.btnRegister.setEnabled(true);
                b.progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Đăng ký thành công!")
                    .setMessage("Bạn đã đăng ký thành công.\nHãy đăng nhập để bắt đầu học ngay!")
                    .setPositiveButton("Đăng nhập ngay", (d, w) -> Navigation.findNavController(view).popBackStack())
                    .setCancelable(false)
                    .show();
            } else if (state instanceof AuthViewModel.AuthState.Error) {
                b.btnRegister.setEnabled(true);
                b.progressBar.setVisibility(View.GONE);
                Snackbar.make(view, ((AuthViewModel.AuthState.Error) state).message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
