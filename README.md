# FlashCard English - Android Studio Project

Ứng dụng học flashcard tiếng Anh, convert từ thiết kế Figma (React/TypeScript) sang Android Native (Kotlin).

## 🛠 Yêu cầu

- **Android Studio** Hedgehog (2023.1.1) trở lên
- **JDK 17**
- **Android SDK**: minSdk 26 (Android 8.0), targetSdk 34
- **Gradle**: 8.2.0

## 🚀 Cách mở project

1. Mở **Android Studio**
2. Chọn **File → Open**
3. Chọn thư mục `FlashcardApp`
4. Để Gradle sync xong (lần đầu cần tải dependencies ~2 phút)
5. Chọn thiết bị / emulator rồi nhấn **Run ▶**

> **Demo account**: `demo@test.com` / `123456`

## 📱 Các màn hình

| Màn hình | Mô tả |
|---|---|
| Splash | Kiểm tra auth, route đến đúng màn hình |
| Onboarding | 3 trang giới thiệu app |
| Login | Đăng nhập với email/mật khẩu |
| Register | Tạo tài khoản mới |
| ForgotPassword | Nhập email, nhận OTP |
| VerifyOTP | Nhập 6 số OTP |
| ResetPassword | Đặt lại mật khẩu |
| Home | Trang chủ: thống kê, folders, bộ thẻ |
| Study | Học thẻ với flip animation + thuật toán SM-2 |
| Quiz | Kiểm tra 4 đáp án, tô màu đúng/sai |
| Dictionary | Tìm kiếm từ vựng toàn bộ database |
| Account | Thông kê cá nhân, đăng xuất |
| SetDetail | Chi tiết bộ thẻ, danh sách tất cả từ |

## 🏗 Kiến trúc

```
app/
├── data/
│   ├── entity/         # Room entities: User, CardSet, Flashcard, Folder, Progress
│   ├── dao/            # Data Access Objects
│   ├── AppDatabase.kt  # Room DB + pre-seeded data (50 flashcards)
│   └── repository/     # FlashcardRepository (single source of truth)
├── viewmodel/          # AuthVM, HomeVM, StudyVM, QuizVM, DictionaryVM, AccountVM
├── adapter/            # RecyclerView adapters
├── ui/                 # Fragments cho mỗi màn hình
└── MainActivity.kt     # Host NavHostFragment
```

## 📊 Dữ liệu

5 bộ thẻ, mỗi bộ 10 từ:
- 🐾 **Animals** - Động Vật
- 🎨 **Colors** - Màu Sắc  
- 🍽️ **Food** - Thức Ăn
- 💪 **Body Parts** - Bộ Phận Cơ Thể
- ✈️ **Travel** - Du Lịch

## 🎨 Thiết kế

- **Nền**: Gradient tối `#0F172A → #1E0B3A`
- **Màu chủ**: Purple `#A855F7` → Pink `#EC4899`
- **Typography**: System default
- **Animation**: Flip card, fade-in-up, slide

## 🔌 Dependencies chính

- **Room 2.6.1** - Local database
- **Navigation Component 2.7.6** - Fragment navigation
- **Material Components 1.11.0** - UI components
- **Lottie 6.3.0** - Animations
- **DotsIndicator 5.0** - Onboarding dots
- **Gson 2.10.1** - JSON serialization
- **Coroutines 1.7.3** - Async operations
