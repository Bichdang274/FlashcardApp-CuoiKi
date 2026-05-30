package com.example.flashcardapp.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentVerifyOtpBinding;
import com.google.android.material.snackbar.Snackbar;
import java.util.Arrays;
import java.util.List;

public class VerifyOTPFragment extends Fragment {
    private FragmentVerifyOtpBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentVerifyOtpBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String email = getArguments() != null ? getArguments().getString("email", "") : "";
        b.tvEmail.setText("Mã OTP đã được gửi đến\n" + email);
        b.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        List<EditText> otpInputs = Arrays.asList(b.etOtp1, b.etOtp2, b.etOtp3, b.etOtp4, b.etOtp5, b.etOtp6);
        for (int idx = 0; idx < otpInputs.size(); idx++) {
            final int nextIdx = idx + 1;
            otpInputs.get(idx).addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && nextIdx < otpInputs.size()) otpInputs.get(nextIdx).requestFocus();
                }
            });
        }

        b.btnVerify.setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (EditText et : otpInputs) otp.append(et.getText().toString());
            if (otp.length() < 6) {
                Snackbar.make(view, "Vui lòng nhập đủ 6 số OTP", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(view).navigate(R.id.resetPasswordDest);
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
