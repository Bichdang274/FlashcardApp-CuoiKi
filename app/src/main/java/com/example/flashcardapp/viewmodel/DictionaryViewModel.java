package com.example.flashcardapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.DictionaryApiManager;
import com.example.flashcardapp.data.AppDatabase;
import com.example.flashcardapp.data.entity.SavedWord;
import com.example.flashcardapp.data.model.FolderDto;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DictionaryViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<SavedWord>> _savedWords = new MutableLiveData<>(Collections.emptyList());
    public final LiveData<List<SavedWord>> savedWords = _savedWords;

    private final MutableLiveData<List<FolderDto>> _folders = new MutableLiveData<>(Collections.emptyList());
    public final LiveData<List<FolderDto>> folders = _folders;

    private final MutableLiveData<DictionaryApiManager.DictEntry> _lookupResult = new MutableLiveData<>();
    public final LiveData<DictionaryApiManager.DictEntry> lookupResult = _lookupResult;

    private final MutableLiveData<String> _lookupError = new MutableLiveData<>();
    public final LiveData<String> lookupError = _lookupError;

    private final MutableLiveData<Boolean> _lookupLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> lookupLoading = _lookupLoading;

    public DictionaryViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
        db = AppDatabase.getInstance(app);
        loadAll();
    }

    public void loadAll() {
        executor.execute(() -> {
            String uid = repo.getCurrentUserId();
            _savedWords.postValue(uid != null ? db.savedWordDao().getAllForUser(uid) : Collections.emptyList());
            _folders.postValue(repo.getFolders());
        });
    }

    public void refreshFolders() {
        executor.execute(() -> _folders.postValue(repo.getFolders()));
    }

    // ─── Dictionary API ──────────────────────────────────
    public void lookupWord(String word) {
        if (word == null || word.trim().isEmpty()) return;
        _lookupLoading.postValue(true);
        _lookupResult.postValue(null);
        _lookupError.postValue(null);
        executor.execute(() -> {
            try {
                retrofit2.Response<List<DictionaryApiManager.DictEntry>> res =
                    DictionaryApiManager.getApi().lookup(word.trim()).execute();
                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                    _lookupResult.postValue(res.body().get(0));
                } else {
                    _lookupError.postValue("Không tìm thấy từ \"" + word.trim() + "\"");
                }
            } catch (Exception e) {
                _lookupError.postValue("Lỗi kết nối mạng");
            } finally {
                _lookupLoading.postValue(false);
            }
        });
    }

    public void clearLookup() {
        _lookupResult.setValue(null);
        _lookupError.setValue(null);
    }

    // ─── Saved words ─────────────────────────────────────
    public void saveCurrentLookup() {
        DictionaryApiManager.DictEntry entry = _lookupResult.getValue();
        if (entry == null) return;
        executor.execute(() -> {
            String uid = repo.getCurrentUserId();
            if (uid == null) return;
            if (db.savedWordDao().getByWordForUser(entry.word, uid) != null) return;
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
            SavedWord sw = new SavedWord(
                "dict_" + UUID.randomUUID().toString(),
                uid, entry.word, entry.getPhonetic(), pos, def, example,
                System.currentTimeMillis()
            );
            db.savedWordDao().insert(sw);
            _savedWords.postValue(db.savedWordDao().getAllForUser(uid));
        });
    }

    public void deleteSavedWord(String id) {
        executor.execute(() -> {
            String uid = repo.getCurrentUserId();
            db.savedWordDao().deleteById(id);
            _savedWords.postValue(uid != null ? db.savedWordDao().getAllForUser(uid) : Collections.emptyList());
        });
    }

    // ─── Folder operations ───────────────────────────────
    public void addSavedWordToFolder(SavedWord word, String folderId) {
        executor.execute(() -> {
            List<FolderDto> foldList = _folders.getValue();
            if (foldList == null) return;
            FolderDto folder = null;
            for (FolderDto f : foldList) if (folderId.equals(f.id)) { folder = f; break; }
            if (folder == null) return;
            List<String> newIds = new ArrayList<>(folder.wordIds);
            if (!newIds.contains(word.id)) newIds.add(word.id);
            repo.updateFolderWords(folderId, newIds);
            _folders.postValue(repo.getFolders());
        });
    }

    public void createFolder(String name) {
        executor.execute(() -> {
            repo.createFolder(name, "📁", "#A855F7,#EC4899");
            _folders.postValue(repo.getFolders());
        });
    }

    public void createFolderAndAddWord(String name, SavedWord word) {
        executor.execute(() -> {
            FolderDto folder = repo.createFolder(name, "📁", "#A855F7,#EC4899");
            if (folder != null && folder.id != null) {
                repo.updateFolderWords(folder.id, java.util.Collections.singletonList(word.id));
            }
            _folders.postValue(repo.getFolders());
        });
    }

    @Override
    protected void onCleared() { super.onCleared(); executor.shutdown(); }
}
