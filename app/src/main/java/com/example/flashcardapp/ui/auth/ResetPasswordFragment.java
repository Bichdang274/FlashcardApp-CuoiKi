package com.example.flashcardapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentResetPasswordBinding;

public class ResetPasswordFragment extends Fragment {
    private FragmentResetPasswordBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentResetPasswordBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.btnReset.setOnClickListener(v -> {
            String pass = b.etPassword.getText() != null ? b.etPassword.getText().toString() : "";
            String confirm = b.etConfirm.getText() != null ? b.etConfirm.getText().toString() : "";
            if (pass.length() < 6) { b.etPassword.setError("Tối thiểu 6 ký tự"); return; }
            if (!pass.equals(confirm)) { b.etConfirm.setError("Mật khẩu không khớp"); return; }
            b.successView.setVisibility(View.VISIBLE);
            b.btnReset.postDelayed(() -> Navigation.findNavController(view).navigate(R.id.loginDest), 2000);
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
