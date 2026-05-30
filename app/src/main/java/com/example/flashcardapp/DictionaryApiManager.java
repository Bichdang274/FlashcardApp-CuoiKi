package com.example.flashcardapp;

import android.util.Log;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DictionaryApiManager {

    public static class DictEntry {
        public String word, phonetic;
        public List<Phonetic> phonetics;
        public List<Meaning> meanings;

        public static class Phonetic {
            public String text;
        }

        public static class Meaning {
            public String partOfSpeech;
            public List<Definition> definitions;

            public static class Definition {
                public String definition, example;
            }
        }

        public String getPhonetic() {
            if (phonetic != null && !phonetic.isEmpty()) return phonetic;
            if (phonetics != null) {
                for (Phonetic p : phonetics) {
                    if (p.text != null && !p.text.isEmpty()) return p.text;
                }
            }
            return "";
        }
    }

    public interface DictApi {
        @GET("api/v2/entries/en/{word}")
        Call<List<DictEntry>> lookup(@Path("word") String word);
    }

    private static DictApi instance;

    public static synchronized DictApi getApi() {
        if (instance == null) {
            X509TrustManager trustAll = new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            };
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS);
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{trustAll}, new SecureRandom());
                builder.sslSocketFactory(sc.getSocketFactory(), trustAll)
                       .hostnameVerifier((host, session) -> true);
            } catch (Exception e) {
                Log.w("DictSSL", e.getMessage());
            }
            instance = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/")
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create(
                    new GsonBuilder().setLenient().create()))
                .build()
                .create(DictApi.class);
        }
        return instance;
    }
}
