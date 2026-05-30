package com.example.flashcardapp;

import android.util.Log;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SupabaseManager {

    // ─── Request bodies ───────────────────────────────────────────
    public static class SignUpBody {
        public final String email, password;
        public final Map<String, String> data;
        public SignUpBody(String email, String password, Map<String, String> data) {
            this.email = email; this.password = password; this.data = data;
        }
    }

    public static class SignInBody {
        public final String email, password;
        public SignInBody(String email, String password) { this.email = email; this.password = password; }
    }

    public static class FolderBody {
        public final String user_id, name, emoji, color;
        public FolderBody(String user_id, String name, String emoji, String color) {
            this.user_id = user_id; this.name = name; this.emoji = emoji; this.color = color;
        }
    }

    public static class FolderUpdateBody {
        public final List<String> word_ids;
        public FolderUpdateBody(List<String> word_ids) { this.word_ids = word_ids; }
    }

    public static class ProgressBody {
        public final String user_id, card_id, set_id;
        public final boolean known;
        public final int repetitions, interval_days;
        public final double ease_factor;
        public ProgressBody(String user_id, String card_id, String set_id, boolean known,
                            int repetitions, double ease_factor, int interval_days) {
            this.user_id = user_id; this.card_id = card_id; this.set_id = set_id;
            this.known = known; this.repetitions = repetitions;
            this.ease_factor = ease_factor; this.interval_days = interval_days;
        }
    }

    public static class SessionBody {
        public final String user_id, set_id;
        public final int cards_studied, known_count;
        public SessionBody(String user_id, String set_id, int cards_studied, int known_count) {
            this.user_id = user_id; this.set_id = set_id;
            this.cards_studied = cards_studied; this.known_count = known_count;
        }
    }

    public static class ProfileUpdateBody {
        public final String name;
        public ProfileUpdateBody(String name) { this.name = name; }
    }

    // ─── Response models ─────────────────────────────────────────
    public static class ProfileResponse { public String id, name, email; }

    public static class AuthResponse {
        public String access_token;
        public UserResponse user;
        public String error_description, msg, message;
    }

    public static class UserResponse { public String id, email; }

    public static class CardSetResponse {
        public String id, title, title_vi, emoji, gradient, accent_color;
    }

    public static class FlashcardResponse {
        public String id, set_id, english, vietnamese, phonetic, example, example_vi, emoji;
    }

    public static class FolderResponse {
        public String id, user_id, name, emoji, color;
        public List<String> word_ids;
    }

    public static class ProgressResponse {
        public String id, user_id, card_id, set_id;
        public boolean known;
        public int repetitions, interval_days;
        public double ease_factor;
    }

    public static class IdResponse { public String id; }

    // ─── Retrofit API ────────────────────────────────────────────
    public interface SupabaseApi {
        @POST("auth/v1/signup")
        Call<AuthResponse> signUp(@Body SignUpBody body);

        @POST("auth/v1/token?grant_type=password")
        Call<AuthResponse> signIn(@Body SignInBody body);

        @GET("rest/v1/card_sets")
        Call<List<CardSetResponse>> getCardSets(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("select") String select);

        @GET("rest/v1/flashcards")
        Call<List<FlashcardResponse>> getFlashcards(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("set_id") String setId,
            @Query("select") String select);

        @GET("rest/v1/flashcards")
        Call<List<FlashcardResponse>> searchFlashcards(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("or") String or,
            @Query("select") String select);

        @GET("rest/v1/flashcards")
        Call<List<FlashcardResponse>> getAllFlashcards(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("select") String select);

        @GET("rest/v1/flashcards")
        Call<List<FlashcardResponse>> getFlashcardsByIds(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("id") String ids,
            @Query("select") String select);

        @GET("rest/v1/profiles")
        Call<List<ProfileResponse>> getProfile(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("id") String id,
            @Query("select") String select);

        @GET("rest/v1/folders")
        Call<List<FolderResponse>> getFolders(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId,
            @Query("select") String select);

        @POST("rest/v1/folders")
        Call<List<FolderResponse>> createFolder(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Header("Prefer") String prefer,
            @Body FolderBody body);

        @GET("rest/v1/card_progress")
        Call<List<ProgressResponse>> getProgress(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId,
            @Query("select") String select);

        @POST("rest/v1/card_progress")
        Call<Void> upsertProgress(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Header("Prefer") String prefer,
            @Body ProgressBody body);

        @DELETE("rest/v1/folders")
        Call<Void> deleteFolder(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("id") String id);

        @PATCH("rest/v1/folders")
        Call<Void> updateFolderWords(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("id") String id,
            @Body FolderUpdateBody body);

        @POST("rest/v1/study_sessions")
        Call<Void> insertSession(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Body SessionBody body);

        @GET("rest/v1/study_sessions")
        Call<List<IdResponse>> getSessions(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId,
            @Query("select") String select);

        @PATCH("rest/v1/profiles")
        Call<Void> updateProfile(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Header("Prefer") String prefer,
            @Query("id") String id,
            @Body ProfileUpdateBody body);

        @DELETE("auth/v1/user")
        Call<Void> deleteUser(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey);

        @POST("rest/v1/rpc/delete_user")
        Call<Void> deleteUserRpc(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Body java.util.Map<String, Object> body);

        @DELETE("rest/v1/card_progress")
        Call<Void> deleteProgressByUser(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId);

        @DELETE("rest/v1/study_sessions")
        Call<Void> deleteSessionsByUser(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId);

        @DELETE("rest/v1/folders")
        Call<Void> deleteFoldersByUser(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("user_id") String userId);

        @DELETE("rest/v1/profiles")
        Call<Void> deleteProfileById(
            @Header("Authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("id") String id);
    }

    // ─── Singleton ───────────────────────────────────────────────
    private static SupabaseApi instance;

    public static synchronized SupabaseApi getApi() {
        if (instance == null) {
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor(msg -> Log.d("HTTP", msg));
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);

            X509TrustManager trustAll = new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            };

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request req = chain.request().newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_KEY)
                        .build();
                    return chain.proceed(req);
                })
                .addInterceptor(logger);

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{trustAll}, new SecureRandom());
                builder.sslSocketFactory(sc.getSocketFactory(), trustAll)
                       .hostnameVerifier((host, session) -> true);
            } catch (Exception e) {
                Log.w("SSL", "Could not set up trust-all: " + e.getMessage());
            }

            OkHttpClient client = builder.build();
            instance = new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(
                    new GsonBuilder().setLenient().create()))
                .build()
                .create(SupabaseApi.class);
        }
        return instance;
    }
}
