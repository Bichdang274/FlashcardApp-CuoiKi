package com.example.flashcardapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.flashcardapp.BuildConfig;
import com.example.flashcardapp.SupabaseManager;
import com.example.flashcardapp.SupabaseManager.*;
import com.example.flashcardapp.data.model.*;
import org.json.JSONObject;
import retrofit2.Response;
import java.util.*;

public class SupabaseRepository {
    private final SharedPreferences prefs;
    private final SupabaseManager.SupabaseApi api;
    private final String apiKey;
    private final Context appContext;

    public SupabaseRepository(Context context) {
        appContext = context.getApplicationContext();
        prefs = context.getSharedPreferences("flashcard_prefs", Context.MODE_PRIVATE);
        api = SupabaseManager.getApi();
        apiKey = BuildConfig.SUPABASE_KEY;
    }

    // ─── Auth ────────────────────────────────────────────
    public ProfileDto signIn(String email, String password) throws Exception {
        Response<AuthResponse> res = api.signIn(new SignInBody(email, password)).execute();
        AuthResponse body = res.body();
        if (res.isSuccessful() && body != null && body.access_token != null) {
            String uid = body.user != null ? body.user.id : "";
            String token = "Bearer " + body.access_token;
            String name;
            try {
                List<ProfileResponse> profiles = api.getProfile(token, apiKey, "eq." + uid, "*").execute().body();
                name = (profiles != null && !profiles.isEmpty() && profiles.get(0).name != null)
                    ? profiles.get(0).name : email.split("@")[0];
            } catch (Exception e) {
                name = email.split("@")[0];
            }
            prefs.edit()
                .putString("access_token", body.access_token)
                .putString("user_id", uid)
                .putString("user_email", email)
                .putString("user_name", name)
                .apply();
            return new ProfileDto(uid, name, email, prefs.getString("user_dob_" + uid, ""), prefs.getString("user_gender_" + uid, ""));
        } else {
            String errBody = res.errorBody() != null ? res.errorBody().string() : null;
            throw new Exception(parseErrorBody(errBody));
        }
    }

    public void signUp(String name, String email, String password) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        Response<AuthResponse> res = api.signUp(new SignUpBody(email, password, data)).execute();
        if (!res.isSuccessful()) {
            String errBody = res.errorBody() != null ? res.errorBody().string() : null;
            throw new Exception(parseErrorBody(errBody));
        }
    }

    public void signOut() {
        prefs.edit()
            .remove("access_token")
            .remove("user_id")
            .remove("user_email")
            .remove("user_name")
            .apply();
    }

    public void deleteAccount() throws Exception {
        String uid = getCurrentUserId();
        if (uid == null) throw new Exception("Chưa đăng nhập");
        String token = getToken();

        try { api.deleteProgressByUser(token, apiKey, "eq." + uid).execute(); } catch (Exception ignored) {}
        try { api.deleteSessionsByUser(token, apiKey, "eq." + uid).execute(); } catch (Exception ignored) {}
        try { api.deleteFoldersByUser(token, apiKey, "eq." + uid).execute(); } catch (Exception ignored) {}
        try { api.deleteProfileById(token, apiKey, "eq." + uid).execute(); } catch (Exception ignored) {}

        try { api.deleteUserRpc(token, apiKey, new HashMap<>()).execute(); } catch (Exception ignored) {}
        try { api.deleteUser(token, apiKey).execute(); } catch (Exception ignored) {}

        try {
            com.example.flashcardapp.data.AppDatabase.getInstance(appContext).clearAllTables();
        } catch (Exception ignored) {}

        prefs.edit().clear().apply();
        signOut();
    }

    public boolean isLoggedIn() { return prefs.contains("access_token"); }
    public boolean isOnboardingDone() { return prefs.getBoolean("onboarding_done", false); }
    public void setOnboardingDone() { prefs.edit().putBoolean("onboarding_done", true).apply(); }
    public String getCurrentUserId() { return prefs.getString("user_id", null); }
    private String getToken() { return "Bearer " + prefs.getString("access_token", ""); }

    public ProfileDto getCurrentProfile() {
        String uid = getCurrentUserId();
        if (uid == null) return null;
        String email = prefs.getString("user_email", "");
        String name = prefs.getString("user_name", email != null ? email.split("@")[0] : "");
        String dob = prefs.getString("user_dob_" + uid, "");
        String gender = prefs.getString("user_gender_" + uid, "");
        return new ProfileDto(uid, name != null ? name : "", email != null ? email : "", dob, gender);
    }

    public void updateProfile(String name, String dob, String gender) throws Exception {
        String uid = getCurrentUserId();
        if (uid == null) throw new Exception("Chưa đăng nhập");
        Response<Void> res = api.updateProfile(getToken(), apiKey, "return=minimal", "eq." + uid,
            new SupabaseManager.ProfileUpdateBody(name)).execute();
        if (!res.isSuccessful()) {
            String errBody = res.errorBody() != null ? res.errorBody().string() : null;
            throw new Exception(parseErrorBody(errBody));
        }
        prefs.edit()
            .putString("user_name", name)
            .putString("user_dob_" + uid, dob != null ? dob : "")
            .putString("user_gender_" + uid, gender != null ? gender : "")
            .apply();
    }

    private String uid() {
        String uid = getCurrentUserId();
        return uid != null ? uid : "guest";
    }

    public Set<Long> getStudyDaySet() {
        String raw = prefs.getString("study_days_" + uid(), "");
        Set<Long> days = new HashSet<>();
        if (raw == null || raw.isEmpty()) return days;
        for (String part : raw.split(",")) {
            try { days.add(Long.parseLong(part.trim())); } catch (NumberFormatException ignored) {}
        }
        return days;
    }

    // ─── Card Sets ───────────────────────────────────────
    public List<CardSetDto> getCardSets() {
        try {
            List<CardSetResponse> list = api.getCardSets(getToken(), apiKey, "*").execute().body();
            if (list == null) return Collections.emptyList();
            List<CardSetDto> result = new ArrayList<>();
            for (CardSetResponse r : list)
                result.add(new CardSetDto(r.id, r.title, r.title_vi, r.emoji, r.gradient, r.accent_color));
            return result;
        } catch (Exception e) {
            Log.e("Supabase", "getCardSets FAIL: " + e.getMessage()); return Collections.emptyList();
        }
    }

    // ─── Flashcards ──────────────────────────────────────
    public List<FlashcardDto> getFlashcardsForSet(String setId) {
        try {
            return toFlashcardDtos(api.getFlashcards(getToken(), apiKey, "eq." + setId, "*").execute().body());
        } catch (Exception e) {
            Log.e("Supabase", "getFlashcards FAIL: " + e.getMessage()); return Collections.emptyList();
        }
    }

    public List<FlashcardDto> searchFlashcards(String query) {
        try {
            return toFlashcardDtos(api.searchFlashcards(getToken(), apiKey,
                "(english.ilike.*" + query + "*,vietnamese.ilike.*" + query + "*)", "*").execute().body());
        } catch (Exception e) {
            Log.e("Supabase", "search FAIL: " + e.getMessage()); return Collections.emptyList();
        }
    }

    public List<FlashcardDto> getAllFlashcards() {
        try {
            return toFlashcardDtos(api.getAllFlashcards(getToken(), apiKey, "*").execute().body());
        } catch (Exception e) {
            Log.e("Supabase", "getAllFlashcards FAIL: " + e.getMessage()); return Collections.emptyList();
        }
    }

    public List<FlashcardDto> getFlashcardsForFolder(List<String> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) return Collections.emptyList();
        List<String> remoteIds = new ArrayList<>();
        List<String> localIds = new ArrayList<>();
        for (String id : wordIds) {
            if (id.startsWith("dict_")) localIds.add(id);
            else remoteIds.add(id);
        }
        List<FlashcardDto> result = new ArrayList<>();
        if (!remoteIds.isEmpty()) {
            try {
                String filter = "in.(" + join(remoteIds, ",") + ")";
                result.addAll(toFlashcardDtos(api.getFlashcardsByIds(getToken(), apiKey, filter, "*").execute().body()));
            } catch (Exception e) {
                Log.e("Supabase", "getFlashcardsForFolder remote FAIL: " + e.getMessage());
            }
        }
        if (!localIds.isEmpty()) {
            try {
                List<com.example.flashcardapp.data.entity.SavedWord> local =
                    com.example.flashcardapp.data.AppDatabase.getInstance(appContext)
                        .savedWordDao().getByIds(localIds);
                for (com.example.flashcardapp.data.entity.SavedWord w : local) {
                    result.add(new FlashcardDto(w.id, "user_dict", w.word, w.definition,
                        w.phonetic, w.example, "", "📖"));
                }
            } catch (Exception e) {
                Log.e("Room", "getFlashcardsForFolder local FAIL: " + e.getMessage());
            }
        }
        return result;
    }

    public Map<String, Integer> getCardCountsPerSet() {
        List<FlashcardDto> all = getAllFlashcards();
        Map<String, Integer> counts = new HashMap<>();
        for (FlashcardDto c : all) {
            Integer prev = counts.get(c.setId);
            counts.put(c.setId, prev == null ? 1 : prev + 1);
        }
        return counts;
    }

    // ─── Progress ────────────────────────────────────────
    public List<CardProgressDto> getAllProgress() {
        try {
            String uid = getCurrentUserId();
            if (uid == null) return Collections.emptyList();
            List<ProgressResponse> list = api.getProgress(getToken(), apiKey, "eq." + uid, "*").execute().body();
            if (list == null) return Collections.emptyList();
            List<CardProgressDto> result = new ArrayList<>();
            for (ProgressResponse r : list)
                result.add(new CardProgressDto(null, null, r.card_id, r.set_id, r.known, r.repetitions, r.ease_factor, r.interval_days));
            return result;
        } catch (Exception e) {
            Log.e("Supabase", "getProgress FAIL: " + e.getMessage()); return Collections.emptyList();
        }
    }

    public List<CardProgressDto> getProgressForSet(String setId) {
        List<CardProgressDto> all = getAllProgress();
        List<CardProgressDto> result = new ArrayList<>();
        for (CardProgressDto p : all) if (setId.equals(p.setId)) result.add(p);
        return result;
    }

    public void saveCardProgress(CardProgressDto p) {
        try {
            String uid = getCurrentUserId();
            if (uid == null) return;
            updateStreak();
            api.upsertProgress(getToken(), apiKey, "resolution=merge-duplicates",
                new ProgressBody(uid, p.cardId, p.setId, p.known, p.repetitions, p.easeFactor, p.intervalDays)).execute();
        } catch (Exception ignored) {}
    }

    // ─── Folders ─────────────────────────────────────────
    public List<FolderDto> getFolders() {
        try {
            String uid = getCurrentUserId();
            if (uid == null) return Collections.emptyList();
            List<FolderResponse> list = api.getFolders(getToken(), apiKey, "eq." + uid, "*").execute().body();
            if (list == null) return Collections.emptyList();
            List<FolderDto> result = new ArrayList<>();
            for (FolderResponse r : list)
                result.add(new FolderDto(r.id, r.user_id, r.name, r.emoji, r.color,
                    r.word_ids != null ? r.word_ids : Collections.emptyList()));
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public FolderDto createFolder(String name, String emoji, String color) {
        try {
            String uid = getCurrentUserId();
            if (uid == null) return null;
            Response<List<FolderResponse>> res = api.createFolder(
                getToken(), apiKey, "return=representation",
                new FolderBody(uid, name, emoji, color)).execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                FolderResponse r = res.body().get(0);
                return new FolderDto(r.id, r.user_id, r.name, r.emoji, r.color,
                    r.word_ids != null ? r.word_ids : Collections.emptyList());
            }
            return null;
        } catch (Exception e) {
            Log.e("Supabase", "createFolder FAIL: " + e.getMessage()); return null;
        }
    }

    public void deleteFolder(String folderId) {
        try { api.deleteFolder(getToken(), apiKey, "eq." + folderId).execute(); }
        catch (Exception e) { Log.e("Supabase", "deleteFolder FAIL: " + e.getMessage()); }
    }

    public void updateFolderWords(String folderId, List<String> wordIds) {
        try { api.updateFolderWords(getToken(), apiKey, "eq." + folderId, new FolderUpdateBody(wordIds)).execute(); }
        catch (Exception e) { Log.e("Supabase", "updateFolderWords FAIL: " + e.getMessage()); }
    }

    // ─── Sessions ────────────────────────────────────────
    public void saveSession(String setId, int cardsStudied, int knownCount) {
        String uid = getCurrentUserId();
        if (uid == null) return;
        updateStreak();
        try {
            api.insertSession(getToken(), apiKey, new SessionBody(uid, setId, cardsStudied, knownCount)).execute();
        } catch (Exception ignored) {}
    }

    public int getTotalSessions() {
        try {
            String uid = getCurrentUserId();
            if (uid == null) return 0;
            List<IdResponse> list = api.getSessions(getToken(), apiKey, "eq." + uid, "id").execute().body();
            return list != null ? list.size() : 0;
        } catch (Exception e) { return 0; }
    }

    // ─── Streak ──────────────────────────────────────────
    private long localDayNumber() {
        long offset = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis());
        return (System.currentTimeMillis() + offset) / (1000L * 60 * 60 * 24);
    }

    public int getCurrentStreak() {
        String uid = uid();
        long lastStudy = prefs.getLong("last_study_date_" + uid, 0);
        if (lastStudy == 0) return 0;
        long offset = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis());
        long today = (System.currentTimeMillis() + offset) / (1000L * 60 * 60 * 24);
        long lastDay = (lastStudy + offset) / (1000L * 60 * 60 * 24);
        long diff = today - lastDay;
        if (diff > 1) {
            prefs.edit().putInt("streak_" + uid, 0).apply();
            return 0;
        }
        return prefs.getInt("streak_" + uid, 0);
    }

    private void updateStreak() {
        String uid = uid();
        long lastStudy = prefs.getLong("last_study_date_" + uid, 0);
        long today = localDayNumber();
        long offset = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis());
        long lastDay = lastStudy == 0 ? -1 : (lastStudy + offset) / (1000L * 60 * 60 * 24);
        long diff = today - lastDay;
        int streak;
        if (diff == 0) streak = prefs.getInt("streak_" + uid, 1);
        else if (diff == 1) streak = prefs.getInt("streak_" + uid, 0) + 1;
        else streak = 1;

        Set<Long> days = getStudyDaySet();
        days.add(today);
        long cutoff = today - 30;
        Set<Long> trimmed = new HashSet<>();
        for (Long d : days) if (d >= cutoff) trimmed.add(d);
        StringBuilder sb = new StringBuilder();
        for (Long d : trimmed) { if (sb.length() > 0) sb.append(","); sb.append(d); }

        prefs.edit()
            .putInt("streak_" + uid, streak)
            .putLong("last_study_date_" + uid, System.currentTimeMillis())
            .putString("study_days_" + uid, sb.toString())
            .apply();
    }

    // ─── Helpers ─────────────────────────────────────────
    private List<FlashcardDto> toFlashcardDtos(List<FlashcardResponse> list) {
        if (list == null) return Collections.emptyList();
        List<FlashcardDto> result = new ArrayList<>();
        for (FlashcardResponse r : list)
            result.add(new FlashcardDto(r.id, r.set_id, r.english, r.vietnamese, r.phonetic,
                r.example != null ? r.example : "",
                r.example_vi != null ? r.example_vi : "",
                r.emoji != null ? r.emoji : "📝"));
        return result;
    }

    private String parseErrorBody(String body) {
        if (body == null || body.trim().isEmpty()) return "Lỗi không xác định";
        try {
            JSONObject json = new JSONObject(body);
            String raw = json.optString("msg", "");
            if (raw.isEmpty()) raw = json.optString("message", "");
            if (raw.isEmpty()) raw = json.optString("error_description", "");
            if (raw.isEmpty()) raw = json.optString("error", "");
            return parseError(raw.isEmpty() ? null : raw);
        } catch (Exception e) { return "Lỗi không xác định"; }
    }

    private String parseError(String msg) {
        if (msg == null) return "Lỗi không xác định";
        if (msg.contains("already registered")) return "Email đã được đăng ký";
        if (msg.contains("Invalid login")) return "Sai email hoặc mật khẩu";
        if (msg.contains("Email not confirmed")) return "Vui lòng xác nhận email trong hộp thư";
        return msg;
    }

    private String join(List<String> list, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
