# FlashCard English — Ứng dụng học Flashcard tiếng Anh (Android)

Ứng dụng Android học từ vựng tiếng Anh bằng flashcard: học thẻ, quiz, game ghép thẻ, tra từ điển và quản lý tài khoản. Dữ liệu được đồng bộ qua **Supabase** (backend REST).

> Viết bằng **Java** (Android Native), không phải Kotlin.

## 🛠 Yêu cầu môi trường

- **Android Studio** Hedgehog (2023.1.1) trở lên
- **JDK 17**
- **Android SDK**: minSdk 26 (Android 8.0) · targetSdk / compileSdk 34
- **Gradle** 8.9 (đã kèm wrapper) · **Android Gradle Plugin** 8.5.2

## 🚀 Cách chạy project

1. **Clone** repo và mở thư mục bằng **Android Studio** (`File → Open`).
2. Tạo file `local.properties` ở thư mục gốc (file này **không** được commit lên git) trỏ tới Android SDK của máy bạn:
   ```properties
   sdk.dir=C\:\\Users\\<TEN_MAY>\\AppData\\Local\\Android\\Sdk
   ```
   > Trên macOS/Linux: `sdk.dir=/Users/<ten>/Library/Android/sdk`
3. Chờ Gradle sync xong (lần đầu cần tải dependencies).
4. Chọn thiết bị / emulator (Android 8.0+) rồi nhấn **Run ▶**.

Build từ dòng lệnh:
```bash
./gradlew :app:assembleDebug
```

> 🔑 **Tài khoản demo**: `demo@test.com` / `123456`
> ℹ️ Cấu hình Supabase (`SUPABASE_URL`, `SUPABASE_KEY`) được khai báo sẵn trong `app/build.gradle` qua `buildConfigField`.

## 📱 Các màn hình

| Màn hình | Mô tả |
|---|---|
| Splash | Kiểm tra trạng thái đăng nhập, điều hướng vào đúng màn hình |
| Onboarding | Các trang giới thiệu app (ViewPager2 + DotsIndicator) |
| Login / Register | Đăng nhập, tạo tài khoản |
| ForgotPassword → VerifyOTP → ResetPassword | Quên mật khẩu, nhập OTP, đặt lại mật khẩu |
| Home | Trang chủ: thống kê, folders, danh sách bộ thẻ |
| Study | Học thẻ với flip animation, ghi nhận tiến độ |
| Quiz | Kiểm tra trắc nghiệm 4 đáp án |
| Matching Game | Game ghép thẻ (nối từ với nghĩa) |
| Dictionary | Tra từ điển tiếng Anh + phát âm (Text-to-Speech), lưu từ |
| Account / EditProfile | Thông tin & thống kê cá nhân, chỉnh sửa hồ sơ, đăng xuất |
| SetDetail | Chi tiết bộ thẻ, danh sách tất cả từ trong bộ |

## 🏗 Kiến trúc

Mô hình **MVVM** (Fragment + ViewModel + Repository), điều hướng bằng **Navigation Component**.

```
app/src/main/java/com/example/flashcardapp/
├── MainActivity.java          # Host NavHostFragment + bottom navigation
├── SupabaseManager.java       # Khởi tạo / quản lý phiên Supabase
├── DictionaryApiManager.java  # Gọi API từ điển ngoài (api.dictionaryapi.dev)
├── BootReceiver.java          # Đặt lại lịch nhắc học sau khi khởi động máy
├── NotificationReceiver.java  # Hiển thị thông báo nhắc học
├── data/
│   ├── entity/                # Room entities: User, CardSet, Flashcard, Folder,
│   │                          #   SavedWord, CardProgress, SetProgress, StudySession
│   ├── dao/                   # Room DAO tương ứng
│   ├── model/                 # DTO map với Supabase (CardSetDto, FlashcardDto, ...)
│   ├── repository/
│   │   ├── SupabaseRepository.java   # Nguồn dữ liệu chính (REST qua Supabase)
│   │   └── FlashcardRepository.java
│   └── AppDatabase.java       # Cấu hình Room (cache cục bộ, seed tài khoản demo)
├── viewmodel/                 # AuthVM, HomeVM, StudyVM, QuizVM, MatchingGameVM,
│                              #   DictionaryVM, AccountVM
├── adapter/                   # RecyclerView adapters
└── ui/                        # Fragment cho từng màn hình
    ├── onboarding/  auth/  home/  study/  quiz/
    ├── game/  dictionary/  account/  set/
    └── SplashFragment.java
```

Điều hướng: `res/navigation/nav_graph.xml` (luồng ngoài: splash/onboarding/auth) và `res/navigation/nav_inner.xml` (luồng trong app sau khi đăng nhập).

## 🔌 Dữ liệu & dịch vụ

- **Supabase** — xác thực và lưu trữ dữ liệu (bộ thẻ, từ vựng, tiến độ học) qua REST API.
- **Room 2.6.1** — entity/DAO cho lưu trữ cục bộ.
- **api.dictionaryapi.dev** — tra nghĩa, phiên âm cho màn Dictionary; phát âm bằng Android **TextToSpeech**.
- **Thông báo nhắc học** — `AlarmManager` + `NotificationReceiver`, tự đặt lại sau khi khởi động máy (`BootReceiver`).

Quyền sử dụng (AndroidManifest): `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## 📦 Dependencies chính

| Thư viện | Phiên bản | Dùng cho |
|---|---|---|
| AndroidX AppCompat / Core | 1.6.1 / 1.12.0 | Nền tảng |
| Material Components | 1.11.0 | Giao diện |
| Navigation (fragment/ui) | 2.7.7 | Điều hướng Fragment |
| Lifecycle (ViewModel/LiveData) | 2.7.0 | MVVM |
| Room (runtime + compiler) | 2.6.1 | Lưu trữ cục bộ |
| RecyclerView / ViewPager2 | 1.3.2 / 1.0.0 | Danh sách, onboarding |
| OkHttp + Logging Interceptor | 4.12.0 | Gọi REST (Supabase) |
| Retrofit + Gson Converter | 2.9.0 | API client |
| Gson | 2.10.1 | JSON |
| Lottie | 6.3.0 | Animation |
| DotsIndicator | 5.0 | Chấm trang onboarding |

## 👥 Phân chia công việc

| Thành viên | Phần phụ trách |
|---|---|
| **Bích** | Khởi tạo project, cấu hình chung, Onboarding, Đăng nhập / Đăng ký, lớp dữ liệu Supabase |
| **Trang** | Home, Study, Quiz, Matching Game, Set detail |
| **An** | Dictionary, Account |

## 🌿 Nhánh Git

- `main` — bản tích hợp đầy đủ (build & chạy được).
- `Trang-dev`, `An-dev` — nhánh tính năng của từng người (dựa trên nền chung của Bích).
