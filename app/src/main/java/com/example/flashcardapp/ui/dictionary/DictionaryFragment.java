package com.example.flashcardapp.ui.dictionary;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.flashcardapp.DictionaryApiManager;
import com.example.flashcardapp.adapter.SavedWordAdapter;
import com.example.flashcardapp.data.entity.SavedWord;
import com.example.flashcardapp.data.model.FolderDto;
import com.example.flashcardapp.databinding.FragmentDictionaryBinding;
import com.example.flashcardapp.viewmodel.DictionaryViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DictionaryFragment extends Fragment {
    private FragmentDictionaryBinding b;
    private DictionaryViewModel vm;
    private SavedWordAdapter adapter;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingSpeech = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        b = FragmentDictionaryBinding.inflate(i, c, false); return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.US);
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED)
                    tts.setLanguage(Locale.getDefault());
                tts.setSpeechRate(0.85f);
                ttsReady = true;
                if (pendingSpeech != null) {
                    tts.speak(pendingSpeech, TextToSpeech.QUEUE_FLUSH, null, "tts");
                    pendingSpeech = null;
                }
            }
        });

        adapter = new SavedWordAdapter(
            word -> speakWord(word),
            word -> showFolderPicker(word),
            id -> { vm.deleteSavedWord(id); }
        );
        b.rvSavedWords.setAdapter(adapter);
        b.rvSavedWords.setLayoutManager(new LinearLayoutManager(getContext()));

        // Nút 📖 cuối ô tìm kiếm
        TextInputLayout til = (TextInputLayout) b.etSearch.getParent().getParent();
        til.setEndIconOnClickListener(v -> triggerLookup());

        // Nút Search trên bàn phím ảo
        b.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { triggerLookup(); return true; }
            return false;
        });

        // Nút đóng, lưu, phát âm trong card kết quả
        b.btnLookupClose.setOnClickListener(v -> {
            vm.clearLookup();
            b.cardLookup.setVisibility(View.GONE);
        });
        b.btnLookupSpeak.setOnClickListener(v -> speakWord(b.tvLookupWord.getText().toString()));
        b.btnLookupSave.setOnClickListener(v -> {
            vm.saveCurrentLookup();
            Snackbar.make(b.getRoot(), "Đã lưu từ \"" + b.tvLookupWord.getText() + "\"", Snackbar.LENGTH_SHORT).show();
        });

        // Observe
        vm.lookupLoading.observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                b.cardLookup.setVisibility(View.VISIBLE);
                b.lookupProgress.setVisibility(View.VISIBLE);
                b.lookupContent.setVisibility(View.GONE);
                b.tvLookupError.setVisibility(View.GONE);
            } else {
                b.lookupProgress.setVisibility(View.GONE);
            }
        });

        vm.lookupResult.observe(getViewLifecycleOwner(), entry -> {
            if (entry == null) return;
            b.cardLookup.setVisibility(View.VISIBLE);
            b.lookupContent.setVisibility(View.VISIBLE);
            b.tvLookupError.setVisibility(View.GONE);
            b.tvLookupWord.setText(entry.word);
            b.tvLookupPhonetic.setText(entry.getPhonetic());
            String pos = "", def = "", example = "";
            if (entry.meanings != null && !entry.meanings.isEmpty()) {
                DictionaryApiManager.DictEntry.Meaning m = entry.meanings.get(0);
                pos = m.partOfSpeech != null ? m.partOfSpeech : "";
                if (m.definitions != null && !m.definitions.isEmpty()) {
                    DictionaryApiManager.DictEntry.Meaning.Definition d = m.definitions.get(0);
                    def = d.definition != null ? d.definition : "";
                    example = d.example != null ? d.example : "";
                }
            }
            b.tvLookupPos.setText(pos);
            b.tvLookupPos.setVisibility(pos.isEmpty() ? View.GONE : View.VISIBLE);
            b.tvLookupDef.setText(def);
            b.tvLookupExample.setText(example.isEmpty() ? "" : "\"" + example + "\"");
            b.tvLookupExample.setVisibility(example.isEmpty() ? View.GONE : View.VISIBLE);
        });

        vm.lookupError.observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            b.cardLookup.setVisibility(View.VISIBLE);
            b.lookupProgress.setVisibility(View.GONE);
            b.lookupContent.setVisibility(View.GONE);
            b.tvLookupError.setVisibility(View.VISIBLE);
            b.tvLookupError.setText(error);
        });

        vm.savedWords.observe(getViewLifecycleOwner(), words -> {
            adapter.submitList(words);
            b.tvWordCount.setText(words.size() + " từ");
            b.tvEmpty.setVisibility(words.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void triggerLookup() {
        String query = b.etSearch.getText() != null ? b.etSearch.getText().toString().trim() : "";
        if (query.isEmpty()) return;
        vm.lookupWord(query);
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.etSearch.getWindowToken(), 0);
    }

    private void speakWord(String word) {
        if (word == null || word.isEmpty()) return;
        boolean soundOn = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("sound", true);
        if (!soundOn) return;
        if (ttsReady) tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "tts");
        else pendingSpeech = word;
    }

    private void showFolderPicker(SavedWord word) {
        List<FolderDto> folders = vm.folders.getValue();
        if (folders == null) folders = new ArrayList<>();
        String[] items = new String[folders.size() + 1];
        for (int i = 0; i < folders.size(); i++) items[i] = folders.get(i).emoji + " " + folders.get(i).name;
        items[folders.size()] = "➕  Tạo thư mục mới";
        final List<FolderDto> finalFolders = folders;
        new AlertDialog.Builder(requireContext())
            .setTitle("Thêm \"" + word.word + "\" vào")
            .setItems(items, (d, i) -> {
                if (i < finalFolders.size()) {
                    vm.addSavedWordToFolder(word, finalFolders.get(i).id);
                    Snackbar.make(b.getRoot(),
                        "Đã thêm vào " + finalFolders.get(i).emoji + " " + finalFolders.get(i).name,
                        Snackbar.LENGTH_SHORT).show();
                } else {
                    showCreateFolderDialog(word);
                }
            })
            .setNegativeButton("Hủy", null).show();
    }

    private void showCreateFolderDialog(SavedWord word) {
        if (getContext() == null) return;
        EditText input = new EditText(getContext());
        input.setHint("Tên thư mục");
        input.setTextColor(getResources().getColor(android.R.color.black, null));
        input.setHintTextColor(0x99000000);
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(60, 20, 60, 0);
        container.addView(input);
        new AlertDialog.Builder(getContext())
            .setTitle("Tạo thư mục mới")
            .setView(container)
            .setPositiveButton("Tạo", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) return;
                if (word != null) vm.createFolderAndAddWord(name, word);
                else vm.createFolder(name);
            })
            .setNegativeButton("Hủy", null).show();
    }

    @Override
    public void onResume() { super.onResume(); vm.loadAll(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        b = null;
    }
}
