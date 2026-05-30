package com.example.flashcardapp.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.flashcardapp.data.dao.*;
import com.example.flashcardapp.data.entity.*;
import java.util.Arrays;
import java.util.concurrent.Executors;

@Database(
    entities = {User.class, CardSet.class, Flashcard.class, Folder.class,
                CardProgress.class, SetProgress.class, StudySession.class,
                com.example.flashcardapp.data.entity.SavedWord.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract CardSetDao cardSetDao();
    public abstract FlashcardDao flashcardDao();
    public abstract FolderDao folderDao();
    public abstract CardProgressDao cardProgressDao();
    public abstract SetProgressDao setProgressDao();
    public abstract StudySessionDao studySessionDao();
    public abstract com.example.flashcardapp.data.dao.SavedWordDao savedWordDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "flashcard_db"
                    ).fallbackToDestructiveMigration().addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> seedDatabase(INSTANCE));
                        }
                    }).build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDatabase(AppDatabase db) {
        db.userDao().insert(new User("demo-user", "Demo User", "demo@test.com", "123456",
                String.valueOf(System.currentTimeMillis())));

        db.cardSetDao().insertAll(Arrays.asList(
            new CardSet("animals", "Animals", "Động Vật", "🐾", "#10b981,#0d9488", "#10b981"),
            new CardSet("colors", "Colors", "Màu Sắc", "🎨", "#a855f7,#ec4899", "#a855f7"),
            new CardSet("food", "Food", "Thức Ăn", "🍽️", "#f97316,#eab308", "#f97316"),
            new CardSet("body", "Body Parts", "Bộ Phận Cơ Thể", "💪", "#3b82f6,#6366f1", "#3b82f6"),
            new CardSet("travel", "Travel", "Du Lịch", "✈️", "#06b6d4,#3b82f6", "#06b6d4")
        ));

        db.flashcardDao().insertAll(Arrays.asList(
            new Flashcard("a1","animals","Dog","Chó","/dɒɡ/","The dog is playing in the garden.","Con chó đang chơi trong vườn.","🐕"),
            new Flashcard("a2","animals","Cat","Mèo","/kæt/","My cat sleeps all day long.","Con mèo của tôi ngủ cả ngày.","🐈"),
            new Flashcard("a3","animals","Elephant","Voi","/ˈɛlɪfənt/","Elephants are the largest land animals.","Voi là động vật trên cạn lớn nhất.","🐘"),
            new Flashcard("a4","animals","Lion","Sư tử","/ˈlaɪən/","The lion is the king of the jungle.","Sư tử là vua của rừng già.","🦁"),
            new Flashcard("a5","animals","Tiger","Hổ","/ˈtaɪɡər/","Tigers are powerful hunters.","Hổ là những thợ săn mạnh mẽ.","🐯"),
            new Flashcard("a6","animals","Rabbit","Thỏ","/ˈræbɪt/","The rabbit hops around the meadow.","Con thỏ nhảy xung quanh đồng cỏ.","🐰"),
            new Flashcard("a7","animals","Bird","Chim","/bɜːrd/","A beautiful bird is singing in the tree.","Một con chim đẹp đang hót trên cây.","🐦"),
            new Flashcard("a8","animals","Fish","Cá","/fɪʃ/","Fish live in water.","Cá sống dưới nước.","🐟"),
            new Flashcard("a9","animals","Horse","Ngựa","/hɔːrs/","The horse runs very fast.","Con ngựa chạy rất nhanh.","🐴"),
            new Flashcard("a10","animals","Monkey","Khỉ","/ˈmʌŋki/","Monkeys love to eat bananas.","Khỉ thích ăn chuối.","🐒"),
            new Flashcard("c1","colors","Red","Đỏ","/rɛd/","She is wearing a red dress.","Cô ấy mặc chiếc váy màu đỏ.","🔴"),
            new Flashcard("c2","colors","Blue","Xanh dương","/bluː/","The sky is blue and clear today.","Bầu trời hôm nay trong xanh.","🔵"),
            new Flashcard("c3","colors","Green","Xanh lá","/ɡriːn/","Leaves are green in spring.","Lá cây xanh vào mùa xuân.","🟢"),
            new Flashcard("c4","colors","Yellow","Vàng","/ˈjɛloʊ/","Sunflowers are bright yellow.","Hoa hướng dương có màu vàng tươi.","🟡"),
            new Flashcard("c5","colors","Purple","Tím","/ˈpɜːrpəl/","Lavender flowers are purple.","Hoa oải hương có màu tím.","🟣"),
            new Flashcard("c6","colors","Orange","Cam","/ˈɒrɪndʒ/","The orange sunset is beautiful.","Hoàng hôn màu cam thật đẹp.","🟠"),
            new Flashcard("c7","colors","Pink","Hồng","/pɪŋk/","Cherry blossoms are light pink.","Hoa anh đào có màu hồng nhạt.","🌸"),
            new Flashcard("c8","colors","White","Trắng","/waɪt/","Snow is pure white.","Tuyết có màu trắng tinh khiết.","⬜"),
            new Flashcard("c9","colors","Black","Đen","/blæk/","The night sky is black.","Bầu trời đêm có màu đen.","⬛"),
            new Flashcard("c10","colors","Brown","Nâu","/braʊn/","The tree trunk is brown.","Thân cây có màu nâu.","🟤"),
            new Flashcard("f1","food","Apple","Táo","/ˈæpəl/","An apple a day keeps the doctor away.","Mỗi ngày ăn một quả táo giúp tránh xa bác sĩ.","🍎"),
            new Flashcard("f2","food","Banana","Chuối","/bəˈnɑːnə/","Monkeys love to eat bananas.","Khỉ thích ăn chuối.","🍌"),
            new Flashcard("f3","food","Rice","Cơm / Gạo","/raɪs/","Rice is a staple food in Vietnam.","Cơm là thức ăn chính ở Việt Nam.","🍚"),
            new Flashcard("f4","food","Bread","Bánh mì","/brɛd/","I eat bread for breakfast every morning.","Tôi ăn bánh mì vào bữa sáng mỗi ngày.","🍞"),
            new Flashcard("f5","food","Egg","Trứng","/ɛɡ/","Scrambled eggs are my favorite.","Trứng bác là món yêu thích của tôi.","🥚"),
            new Flashcard("f6","food","Milk","Sữa","/mɪlk/","Children should drink milk every day.","Trẻ em nên uống sữa mỗi ngày.","🥛"),
            new Flashcard("f7","food","Cake","Bánh kem","/keɪk/","We ate a delicious birthday cake.","Chúng tôi ăn một chiếc bánh sinh nhật ngon.","🎂"),
            new Flashcard("f8","food","Noodle","Mì / Bún","/ˈnuːdəl/","Noodle soup is popular in Vietnam.","Phở rất phổ biến ở Việt Nam.","🍜"),
            new Flashcard("f9","food","Orange","Cam","/ˈɒrɪndʒ/","I drink orange juice every morning.","Tôi uống nước cam mỗi buổi sáng.","🍊"),
            new Flashcard("f10","food","Chicken","Gà","/ˈtʃɪkɪn/","Grilled chicken is very healthy.","Gà nướng rất tốt cho sức khỏe.","🍗"),
            new Flashcard("b1","body","Head","Đầu","/hɛd/","She nodded her head in agreement.","Cô ấy gật đầu đồng ý.","🗣️"),
            new Flashcard("b2","body","Eye","Mắt","/aɪ/","He has beautiful blue eyes.","Anh ấy có đôi mắt xanh đẹp.","👁️"),
            new Flashcard("b3","body","Ear","Tai","/ɪər/","She has good ears for music.","Cô ấy có tai nghe nhạc tốt.","👂"),
            new Flashcard("b4","body","Nose","Mũi","/noʊz/","Dogs have a very sensitive nose.","Chó có chiếc mũi rất nhạy.","👃"),
            new Flashcard("b5","body","Mouth","Miệng","/maʊθ/","Open your mouth and say 'Ah'.","Mở miệng ra và nói 'A'.","👄"),
            new Flashcard("b6","body","Hand","Tay","/hænd/","She waved her hand to say goodbye.","Cô ấy vẫy tay chào tạm biệt.","✋"),
            new Flashcard("b7","body","Foot","Chân","/fʊt/","My foot hurts after walking so far.","Chân tôi đau sau khi đi bộ xa vậy.","🦶"),
            new Flashcard("b8","body","Heart","Tim","/hɑːrt/","The heart pumps blood throughout the body.","Tim bơm máu đến khắp cơ thể.","❤️"),
            new Flashcard("b9","body","Arm","Cánh tay","/ɑːrm/","He broke his arm playing football.","Anh ấy bị gãy tay khi đá bóng.","💪"),
            new Flashcard("b10","body","Leg","Cẳng chân","/lɛɡ/","She has long and beautiful legs.","Cô ấy có đôi chân dài và đẹp.","🦵"),
            new Flashcard("t1","travel","Airport","Sân bay","/ˈɛərpɔːrt/","We arrived at the airport two hours early.","Chúng tôi đến sân bay trước hai tiếng.","✈️"),
            new Flashcard("t2","travel","Hotel","Khách sạn","/hoʊˈtɛl/","We stayed at a five-star hotel.","Chúng tôi ở khách sạn năm sao.","🏨"),
            new Flashcard("t3","travel","Passport","Hộ chiếu","/ˈpæspɔːrt/","Don't forget to bring your passport.","Đừng quên mang theo hộ chiếu.","🛂"),
            new Flashcard("t4","travel","Ticket","Vé","/ˈtɪkɪt/","I bought two tickets for the concert.","Tôi mua hai vé cho buổi hòa nhạc.","🎫"),
            new Flashcard("t5","travel","Luggage","Hành lý","/ˈlʌɡɪdʒ/","My luggage was too heavy.","Hành lý của tôi quá nặng.","🧳"),
            new Flashcard("t6","travel","Map","Bản đồ","/mæp/","We used a map to find the hotel.","Chúng tôi dùng bản đồ để tìm khách sạn.","🗺️"),
            new Flashcard("t7","travel","Beach","Bãi biển","/biːtʃ/","The beach was crowded in summer.","Bãi biển đông người vào mùa hè.","🏖️"),
            new Flashcard("t8","travel","Mountain","Núi","/ˈmaʊntɪn/","We hiked up the mountain.","Chúng tôi leo núi.","⛰️"),
            new Flashcard("t9","travel","Tour","Chuyến tham quan","/tʊər/","We booked a city tour for tomorrow.","Chúng tôi đặt tour tham quan thành phố cho ngày mai.","🚌"),
            new Flashcard("t10","travel","Souvenir","Quà lưu niệm","/ˌsuːvəˈnɪər/","I bought souvenirs for my family.","Tôi mua quà lưu niệm cho gia đình.","🛍️")
        ));
    }
}
