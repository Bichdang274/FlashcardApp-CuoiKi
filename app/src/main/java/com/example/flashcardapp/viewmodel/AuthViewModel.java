package com.example.flashcardapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flashcardapp.data.model.ProfileDto;
import com.example.flashcardapp.data.repository.SupabaseRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final SupabaseRepository repo;
    private final MutableLiveData<AuthState> _authState = new MutableLiveData<>(new AuthState.Idle());
    public final LiveData<AuthState> authState = _authState;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new SupabaseRepository(app);
    }

    public boolean isLoggedIn() { return repo.isLoggedIn(); }
    public boolean isOnboardingDone() { return repo.isOnboardingDone(); }
    public void setOnboardingDone() { repo.setOnboardingDone(); }

    public void login(String email, String password) {
        _authState.setValue(new AuthState.Loading());
        executor.execute(() -> {
            try {
                ProfileDto profile = repo.signIn(email, password);
                _authState.postValue(new AuthState.Success(profile));
            } catch (Exception e) {
                String msg = e.getMessage();
                _authState.postValue(new AuthState.Error(msg != null ? msg : "Lỗi không xác định"));
            }
        });
    }

    public void register(String name, String email, String password) {
        _authState.setValue(new AuthState.Loading());
        executor.execute(() -> {
            try {
                repo.signUp(name, email, password);
                _authState.postValue(new AuthState.Registered());
            } catch (Exception e) {
                String msg = e.getMessage();
                _authState.postValue(new AuthState.Error(msg != null ? msg : "Lỗi không xác định"));
            }
        });
    }

    public void logout() {
        executor.execute(() -> {
            repo.signOut();
            _authState.postValue(new AuthState.LoggedOut());
        });
    }

    public void deleteAccount() {
        _authState.setValue(new AuthState.Loading());
        executor.execute(() -> {
            try {
                repo.deleteAccount();
                _authState.postValue(new AuthState.LoggedOut());
            } catch (Exception e) {
                String msg = e.getMessage();
                _authState.postValue(new AuthState.Error(msg != null ? msg : "Không thể xóa tài khoản"));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // ─── AuthState ───────────────────────────────────────
    public static abstract class AuthState {
        public static final class Idle extends AuthState {}
        public static final class Loading extends AuthState {}
        public static final class Registered extends AuthState {}
        public static final class LoggedOut extends AuthState {}
        public static final class Success extends AuthState {
            public final ProfileDto user;
            public Success(ProfileDto user) { this.user = user; }
        }
        public static final class Error extends AuthState {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }
}
