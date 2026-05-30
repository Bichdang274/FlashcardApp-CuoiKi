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
import com.example.flashcardapp.databinding.FragmentForgotPasswordBinding;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentForgotPasswordBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.btnSend.setOnClickListener(v -> {
            String email = b.etEmail.getText() != null ? b.etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) { b.etEmail.setError("Vui lòng nhập email"); return; }
            Bundle bundle = new Bundle();
            bundle.putString("email", email);
            Navigation.findNavController(view).navigate(R.id.verifyOTPDest, bundle);
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
