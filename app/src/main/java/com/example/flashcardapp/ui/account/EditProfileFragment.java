package com.example.flashcardapp.ui.account;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.FragmentEditProfileBinding;
import com.example.flashcardapp.viewmodel.AccountViewModel;
import java.util.Calendar;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding b;
    private AccountViewModel vm;
    private String selectedGender = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentEditProfileBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        vm.user.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (b.etName.getText() == null || b.etName.getText().toString().isEmpty()) {
                b.etName.setText(user.name);
            }
            b.tvEmail.setText(user.email);
            String initial = (user.name != null && !user.name.isEmpty())
                ? String.valueOf(user.name.charAt(0)).toUpperCase() : "U";
            b.tvAvatar.setText(initial);

            if (!user.dob.isEmpty() && (b.tvDob.getText() == null || b.tvDob.getText().toString().isEmpty())) {
                b.tvDob.setText(user.dob);
            }
            if (!user.gender.isEmpty() && selectedGender.isEmpty()) {
                selectedGender = user.gender;
                updateGenderUI();
            }
        });

        vm.updateState.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case LOADING:
                    b.btnSave.setEnabled(false);
                    b.progressBar.setVisibility(View.VISIBLE);
                    b.tvBtnText.setVisibility(View.INVISIBLE);
                    break;
                case SUCCESS:
                    vm.resetUpdateState();
                    Toast.makeText(requireContext(), "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).popBackStack();
                    break;
                case ERROR:
                    b.btnSave.setEnabled(true);
                    b.progressBar.setVisibility(View.GONE);
                    b.tvBtnText.setVisibility(View.VISIBLE);
                    vm.updateError.observe(getViewLifecycleOwner(), msg ->
                        Toast.makeText(requireContext(), msg != null ? msg : "Có lỗi xảy ra", Toast.LENGTH_LONG).show());
                    vm.resetUpdateState();
                    break;
                default:
                    b.btnSave.setEnabled(true);
                    b.progressBar.setVisibility(View.GONE);
                    b.tvBtnText.setVisibility(View.VISIBLE);
                    break;
            }
        });

        b.layoutDob.setOnClickListener(v -> showDatePicker());

        b.genderNam.setOnClickListener(v -> { selectedGender = "Nam"; updateGenderUI(); });
        b.genderNu.setOnClickListener(v -> { selectedGender = "Nữ"; updateGenderUI(); });
        b.genderKhac.setOnClickListener(v -> { selectedGender = "Khác"; updateGenderUI(); });
        b.genderKhongMuon.setOnClickListener(v -> { selectedGender = "Không muốn chia sẻ"; updateGenderUI(); });

        b.btnSave.setOnClickListener(v -> {
            String name = b.etName.getText() != null ? b.etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                b.etName.setError("Vui lòng nhập tên");
                return;
            }
            String dob = b.tvDob.getText() != null ? b.tvDob.getText().toString().trim() : "";
            vm.updateProfile(name, dob, selectedGender);
        });

        b.btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        b.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String current = b.tvDob.getText() != null ? b.tvDob.getText().toString() : "";
        if (!current.isEmpty() && current.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = current.split("/");
            cal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
        }
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            b.tvDob.setText(String.format("%02d/%02d/%04d", day, month + 1, year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateGenderUI() {
        int active = R.drawable.bg_gender_selected;
        int inactive = R.drawable.bg_gender_default;
        b.genderNam.setBackgroundResource("Nam".equals(selectedGender) ? active : inactive);
        b.genderNu.setBackgroundResource("Nữ".equals(selectedGender) ? active : inactive);
        b.genderKhac.setBackgroundResource("Khác".equals(selectedGender) ? active : inactive);
        b.genderKhongMuon.setBackgroundResource("Không muốn chia sẻ".equals(selectedGender) ? active : inactive);

        int activeColor = getResources().getColor(android.R.color.white, null);
        int inactiveColor = getResources().getColor(R.color.text_primary, null);
        b.genderNam.setTextColor("Nam".equals(selectedGender) ? activeColor : inactiveColor);
        b.genderNu.setTextColor("Nữ".equals(selectedGender) ? activeColor : inactiveColor);
        b.genderKhac.setTextColor("Khác".equals(selectedGender) ? activeColor : inactiveColor);
        b.genderKhongMuon.setTextColor("Không muốn chia sẻ".equals(selectedGender) ? activeColor : inactiveColor);
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); b = null; }
}
