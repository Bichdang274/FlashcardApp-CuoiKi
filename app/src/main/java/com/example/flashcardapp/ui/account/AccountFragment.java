package com.example.flashcardapp.ui.account;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.NotificationReceiver;
import com.example.flashcardapp.R;
import com.example.flashcardapp.adapter.VocabularyAdapter;
import com.example.flashcardapp.data.model.FlashcardDto;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import com.example.flashcardapp.databinding.FragmentAccountBinding;
import com.example.flashcardapp.viewmodel.AccountViewModel;
import com.example.flashcardapp.viewmodel.AuthViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding b;
    private AccountViewModel vm;
    private AuthViewModel authVM;
    private SharedPreferences prefs;
    private TextToSpeech tts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private NavController getRootNav() {
        NavHostFragment host = (NavHostFragment) requireActivity().getSupportFragmentManager()
            .findFragmentById(R.id.navHostFragment);
        return host != null ? host.getNavController() : null;
    }

    private final ActivityResultLauncher<String> notifPermLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                enableNotification();
            } else {
                b.toggleNotification.setChecked(false);
                prefs.edit().putBoolean("notif", false).apply();
                Toast.makeText(requireContext(), "Cần cấp quyền thông báo để sử dụng tính năng này", Toast.LENGTH_LONG).show();
            }
        });

    private final ActivityResultLauncher<Intent> exactAlarmLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> enableNotification());

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentAccountBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        authVM = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.ENGLISH);
        });

        vm.loadData();

        vm.user.observe(getViewLifecycleOwner(), user -> {
            b.tvName.setText(user != null ? user.name : "Người dùng");
            b.tvEmail.setText(user != null ? user.email : "");
            String avatar = (user != null && user.name != null && !user.name.isEmpty())
                ? String.valueOf(user.name.charAt(0)).toUpperCase() : "U";
            b.tvAvatar.setText(avatar);
        });
        vm.stats.observe(getViewLifecycleOwner(), stats -> {
            b.tvWordsLearned.setText(String.valueOf(stats.wordsLearned));
            b.tvStreak.setText(String.valueOf(stats.streak));
            b.tvSessions.setText(String.valueOf(stats.sessions));
            b.tvWordsTotal.setText("/" + stats.totalCards);
            int pct = stats.totalCards > 0
                ? (int) Math.round(stats.wordsLearned * 100.0 / stats.totalCards) : 0;
            b.tvProgressPercent.setText(pct + "%");
            b.progressTotal.setMax(stats.totalCards > 0 ? stats.totalCards : 100);
            b.progressTotal.setProgress(stats.wordsLearned);
            b.badgeActive.setVisibility(stats.streak >= 3 ? View.VISIBLE : View.GONE);
        });
        vm.folders.observe(getViewLifecycleOwner(), folders -> b.tvSetsCount.setText(String.valueOf(folders.size())));

        setupToggles();

        b.btnProfile.setOnClickListener(v -> {
            NavController nav = getRootNav();
            if (nav != null) nav.navigate(R.id.action_main_to_editProfile);
        });
        b.btnAbout.setOnClickListener(v -> showAboutBottomSheet());
        b.cardWordsLearned.setOnClickListener(v -> showVocabularyBottomSheet());
        b.cardFolders.setOnClickListener(v -> showFoldersBottomSheet());
        b.cardStreak.setOnClickListener(v -> showStreakBottomSheet());

        b.btnLogout.setOnClickListener(v -> authVM.logout());
        b.tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        authVM.authState.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.Error) {
                Toast.makeText(requireContext(),
                    ((AuthViewModel.AuthState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.loadData();
    }

    private void setupToggles() {
        b.toggleNotification.setChecked(prefs.getBoolean("notif", false));
        b.toggleSound.setChecked(prefs.getBoolean("sound", true));
        boolean isDark = prefs.getBoolean("dark", false);
        b.toggleDark.setChecked(isDark);
        b.tvDarkModeSubtitle.setText(isDark ? "Đang bật chế độ tối" : "Đang bật chế độ sáng");

        b.toggleNotification.setOnCheckedChangeListener((btn, on) -> {
            if (on) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                    if (!am.canScheduleExactAlarms()) {
                        Toast.makeText(requireContext(), "Cần cấp quyền 'Báo thức & nhắc nhở' để nhắc đúng giờ", Toast.LENGTH_LONG).show();
                        exactAlarmLauncher.launch(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                    } else {
                        enableNotification();
                    }
                } else {
                    enableNotification();
                }
            } else {
                NotificationReceiver.cancel(requireContext());
                prefs.edit().putBoolean("notif", false).apply();
                Toast.makeText(requireContext(), "Đã tắt nhắc nhở học tập", Toast.LENGTH_SHORT).show();
            }
        });

        b.toggleSound.setOnCheckedChangeListener((btn, on) -> prefs.edit().putBoolean("sound", on).apply());

        b.toggleDark.setOnCheckedChangeListener((btn, on) -> {
            prefs.edit().putBoolean("dark", on).apply();
            b.tvDarkModeSubtitle.setText(on ? "Đang bật chế độ tối" : "Đang bật chế độ sáng");
            AppCompatDelegate.setDefaultNightMode(on ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            requireActivity().recreate();
        });
    }

    // ─── About bottom sheet ───────────────────────────────────────
    private void showAboutBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_about, null);
        v.findViewById(R.id.btnClose).setOnClickListener(x -> sheet.dismiss());
        sheet.setContentView(v);
        sheet.show();
    }

    // ─── Vocabulary bottom sheet ──────────────────────────────────
    private void showVocabularyBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_vocabulary, null);

        TextView tabLearned = v.findViewById(R.id.tabLearned);
        TextView tabUnlearned = v.findViewById(R.id.tabUnlearned);
        View layoutEmpty = v.findViewById(R.id.layoutEmpty);
        RecyclerView rv = v.findViewById(R.id.rvWords);

        VocabularyAdapter adapter = new VocabularyAdapter(tts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnClose).setOnClickListener(x -> sheet.dismiss());

        final boolean[] showingLearned = {false};

        SupabaseRepository repo = new SupabaseRepository(requireContext());

        Runnable loadData = () -> executor.execute(() -> {
            List<FlashcardDto> allCards = repo.getAllFlashcards();
            Set<String> knownIds = new java.util.HashSet<>();
            for (com.example.flashcardapp.data.model.CardProgressDto p : repo.getAllProgress()) {
                if (p.known) knownIds.add(p.cardId);
            }

            List<FlashcardDto> learned = new ArrayList<>();
            List<FlashcardDto> unlearned = new ArrayList<>();
            for (FlashcardDto c : allCards) {
                if (knownIds.contains(c.id)) learned.add(c);
                else unlearned.add(c);
            }

            requireActivity().runOnUiThread(() -> {
                tabLearned.setText("Đã học (" + learned.size() + ")");
                tabUnlearned.setText("Chưa học (" + unlearned.size() + ")");

                List<FlashcardDto> current = showingLearned[0] ? learned : unlearned;
                adapter.setItems(current);

                boolean isEmpty = current.isEmpty();
                layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                tabLearned.setOnClickListener(x -> {
                    if (showingLearned[0]) return;
                    showingLearned[0] = true;
                    tabLearned.setBackgroundResource(R.drawable.bg_tab_selected);
                    tabLearned.setTextColor(getResources().getColor(android.R.color.white, null));
                    tabUnlearned.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tabUnlearned.setTextColor(0xFF8899AA);
                    adapter.setItems(learned);
                    boolean empty = learned.isEmpty();
                    layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                });

                tabUnlearned.setOnClickListener(x -> {
                    if (!showingLearned[0]) return;
                    showingLearned[0] = false;
                    tabUnlearned.setBackgroundResource(R.drawable.bg_tab_selected);
                    tabUnlearned.setTextColor(getResources().getColor(android.R.color.white, null));
                    tabLearned.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tabLearned.setTextColor(0xFF8899AA);
                    adapter.setItems(unlearned);
                    boolean empty = unlearned.isEmpty();
                    layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                });
            });
        });

        loadData.run();
        sheet.setContentView(v);
        sheet.show();
    }

    // ─── Folders bottom sheet ─────────────────────────────────────
    private void showFoldersBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_my_folders, null);

        TextView tvCount = v.findViewById(R.id.tvFolderCount);
        View layoutEmpty = v.findViewById(R.id.layoutEmpty);
        RecyclerView rv = v.findViewById(R.id.rvFolders);

        v.findViewById(R.id.btnClose).setOnClickListener(x -> sheet.dismiss());
        v.findViewById(R.id.btnCreateFolder).setOnClickListener(x -> {
            sheet.dismiss();
            showCreateFolderDialog();
        });

        vm.folders.observe(getViewLifecycleOwner(), folders -> {
            if (folders == null) return;
            int count = folders.size();
            tvCount.setText(count + " thư mục");
            if (count == 0) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });

        sheet.setContentView(v);
        sheet.show();
    }

    private void showCreateFolderDialog() {
        if (getContext() == null) return;
        EditText input = new EditText(getContext());
        input.setHint("Tên thư mục");
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(60, 20, 60, 0);
        container.addView(input);
        new AlertDialog.Builder(requireContext())
            .setTitle("Tạo thư mục mới")
            .setView(container)
            .setPositiveButton("Tạo", (d, w) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) vm.createFolder(name, "📁", "#7C3AED");
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    // ─── Streak bottom sheet ──────────────────────────────────────
    private void showStreakBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_streak, null);

        TextView tvCount = v.findViewById(R.id.tvStreakCount);
        LinearLayout calendarGrid = v.findViewById(R.id.calendarGrid);

        v.findViewById(R.id.btnClose).setOnClickListener(x -> sheet.dismiss());

        vm.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            tvCount.setText(String.valueOf(stats.streak));
        });

        Set<Long> studyDays = vm.getStudyDaySet();
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);

        buildCalendarGrid(calendarGrid, studyDays, today);

        sheet.setContentView(v);
        sheet.show();
    }

    private void buildCalendarGrid(LinearLayout grid, Set<Long> studyDays, long today) {
        grid.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        int cellSize = (int) (38 * dp);
        int margin = (int) (4 * dp);

        // today = most recent (bottom-right), today-29 = oldest (top-left)
        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, (int)(4 * dp));
            rowLayout.setLayoutParams(rowParams);

            for (int col = 0; col < 5; col++) {
                int dayIndex = row * 5 + col;
                long dayNum = today - (29 - dayIndex);
                boolean studied = studyDays.contains(dayNum);
                boolean isToday = (dayNum == today);

                android.widget.TextView cell = new android.widget.TextView(requireContext());
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(cellSize, cellSize);
                cellParams.setMargins(margin, 0, margin, 0);
                cell.setLayoutParams(cellParams);
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);

                if (studied) {
                    cell.setBackgroundResource(R.drawable.bg_streak_day_active);
                    cell.setText("✓");
                    cell.setTextColor(0xFFFFFFFF);
                    cell.setTypeface(null, android.graphics.Typeface.BOLD);
                } else if (isToday) {
                    cell.setBackgroundResource(R.drawable.bg_streak_day_today);
                    cell.setText("·");
                    cell.setTextColor(0xFFFFFFFF);
                    cell.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
                } else {
                    cell.setBackgroundResource(R.drawable.bg_streak_day_inactive);
                    cell.setText("");
                }

                rowLayout.addView(cell);
            }
            grid.addView(rowLayout);
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xóa tài khoản")
            .setMessage("Tất cả dữ liệu học tập, thư mục và tiến trình của bạn sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa tài khoản", (d, w) -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận lần cuối")
                    .setMessage("Bạn có chắc chắn muốn xóa tài khoản không?")
                    .setPositiveButton("Có, xóa ngay", (d2, w2) -> authVM.deleteAccount())
                    .setNegativeButton("Hủy", null)
                    .show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void enableNotification() {
        NotificationReceiver.schedule(requireContext());
        prefs.edit().putBoolean("notif", true).apply();
        Toast.makeText(requireContext(),
            "Đã bật nhắc nhở — sẽ thông báo lúc " + NotificationReceiver.NOTIF_HOUR + ":00 mỗi ngày",
            Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) NotificationReceiver.showNotification(requireContext());
        }, 3000);
        suggestDisableBatteryOptimization();
    }

    private void suggestDisableBatteryOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        PowerManager pm = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        String pkg = requireContext().getPackageName();
        if (!pm.isIgnoringBatteryOptimizations(pkg)) {
            new AlertDialog.Builder(requireContext())
                .setTitle("Để nhắc nhở đúng giờ")
                .setMessage("Tắt tối ưu pin cho app này để thông báo 20:00 không bị trễ hoặc mất.")
                .setPositiveButton("Tắt tối ưu pin", (d, w) -> startActivity(
                    new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:" + pkg))))
                .setNegativeButton("Để sau", null)
                .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        b = null;
    }
}
