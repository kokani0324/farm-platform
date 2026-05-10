package com.farm.platform.config;

import com.farm.platform.entity.*;
import com.farm.platform.repository.BlogRepository;
import com.farm.platform.repository.BlogTypeRepository;
import com.farm.platform.repository.CategoryRepository;
import com.farm.platform.repository.FarmTripCategoryRepository;
import com.farm.platform.repository.FarmTripRepository;
import com.farm.platform.repository.ProductRepository;
import com.farm.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 啟動時匯入預設資料：
 *  - 8 個商品分類
 *  - 1 個範例小農（已啟用）
 *  - 1 個管理員
 *  - 5 個範例商品
 * 已存在的資料不會重複匯入。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FarmTripCategoryRepository farmTripCategoryRepository;
    private final FarmTripRepository farmTripRepository;
    private final BlogTypeRepository blogTypeRepository;
    private final BlogRepository blogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedCategories();
        backfillUserRoles();
        seedAdminAndDemoFarmer();
        seedDemoProducts();
        seedFarmTripCategories();
        seedDemoFarmTrips();
        seedBlogTypes();
        seedDemoBlogs();
    }

    /**
     * 遷移既有資料:roles 集合空的使用者,依 role 欄位回填。
     * - ADMIN 帳號 → roles = {ADMIN}
     * - FARMER 帳號 → roles = {CONSUMER, FARMER}
     * - CONSUMER 帳號 → roles = {CONSUMER}
     * 已有 roles 的使用者不動。
     */
    private void backfillUserRoles() {
        int patched = 0;
        for (User u : userRepository.findAll()) {
            if (u.getRoles() != null && !u.getRoles().isEmpty()) continue;
            Set<Role> rs = new HashSet<>();
            if (u.getRole() == Role.ADMIN) {
                rs.add(Role.ADMIN);
            } else {
                rs.add(Role.CONSUMER);
                if (u.getRole() == Role.FARMER) rs.add(Role.FARMER);
            }
            u.setRoles(rs);
            userRepository.save(u);
            patched++;
        }
        if (patched > 0) {
            log.info("[Seed] 回填 {} 個既有使用者的 roles 集合", patched);
        }
    }

    private void seedCategories() {
        record Cat(String code, String name, String icon, int order) {}
        List<Cat> defaults = List.of(
                new Cat("leafy",     "葉菜類",     "🥬", 1),
                new Cat("root",      "根莖類",     "🥕", 2),
                new Cat("fruit",     "當季水果",   "🍎", 3),
                new Cat("grain",     "米麥雜糧",   "🌾", 4),
                new Cat("mushroom",  "菇蕈菌類",   "🍄", 5),
                new Cat("tea",       "茶葉飲品",   "🍵", 6),
                new Cat("processed", "加工食品",   "🥫", 7),
                new Cat("box",       "嚴選蔬果箱", "📦", 8)
        );

        for (Cat c : defaults) {
            if (!categoryRepository.existsByCode(c.code())) {
                categoryRepository.save(Category.builder()
                        .code(c.code()).name(c.name()).icon(c.icon()).sortOrder(c.order())
                        .build());
                log.info("[Seed] 新增分類 {}", c.name());
            }
        }
    }

    private void seedAdminAndDemoFarmer() {
        if (!userRepository.existsByEmail("admin@nong.com")) {
            userRepository.save(User.builder()
                    .email("admin@nong.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .name("系統管理員")
                    .role(Role.ADMIN)
                    .roles(new HashSet<>(Set.of(Role.ADMIN)))
                    .enabled(true)
                    .build());
            log.info("[Seed] 新增管理員 admin@nong.com / admin1234");
        }

        if (!userRepository.existsByEmail("demo@farmer.com")) {
            userRepository.save(User.builder()
                    .email("demo@farmer.com")
                    .password(passwordEncoder.encode("123456"))
                    .name("阿美姐田園")
                    .phone("0912000111")
                    .role(Role.FARMER)
                    .roles(new HashSet<>(Set.of(Role.CONSUMER, Role.FARMER)))
                    .enabled(true)  // 範例小農直接啟用，方便測試
                    .build());
            log.info("[Seed] 新增範例小農 demo@farmer.com / 123456");
        }
    }

    private void seedDemoProducts() {
        if (productRepository.count() > 0) return;

        User farmer = userRepository.findByEmail("demo@farmer.com").orElse(null);
        if (farmer == null) return;

        record Demo(String catCode, String name, String desc, String price, String unit, int stock, String origin, String imageUrl, boolean groupBuy) {}
        List<Demo> demos = List.of(
                new Demo("leafy", "有機溫室小白菜",
                        "新鮮現採、無農藥殘留，葉片青翠厚實，清甜爽脆。",
                        "60.00", "包(300g)", 50, "雲林麥寮",
                        "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600",
                        true),
                new Demo("fruit", "梨山高山蜜蘋果",
                        "海拔 2000 公尺種植，糖度 14 度以上，脆甜多汁。",
                        "320.00", "禮盒(2kg)", 30, "台中梨山",
                        "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=600",
                        true),
                new Demo("root", "台農 57 號黃地瓜",
                        "鬆軟綿密、自然甘甜，烤地瓜的最佳選擇。",
                        "150.00", "盒(3斤)", 80, "雲林水林",
                        "https://images.unsplash.com/photo-1591796111824-cf73fa1f2dab?w=600",
                        false),
                new Demo("grain", "東部有機糙米",
                        "花蓮富里友善耕作、無毒栽培，營養完整。",
                        "240.00", "包(2kg)", 100, "花蓮富里",
                        "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600",
                        true),
                new Demo("box", "當週嚴選蔬果箱",
                        "農夫每週精選 6-8 樣當季蔬果，產地直送。",
                        "599.00", "箱", 25, "全台多產地",
                        "https://images.unsplash.com/photo-1542838132-92c53300491e?w=600",
                        false)
        );

        for (Demo d : demos) {
            Category cat = categoryRepository.findByCode(d.catCode()).orElse(null);
            if (cat == null) continue;
            productRepository.save(Product.builder()
                    .name(d.name())
                    .description(d.desc())
                    .price(new BigDecimal(d.price()))
                    .unit(d.unit())
                    .stock(d.stock())
                    .imageUrl(d.imageUrl())
                    .origin(d.origin())
                    .shippingMethod("黑貓宅配")
                    .groupBuyEnabled(d.groupBuy())
                    .status(ProductStatus.ACTIVE)
                    .farmer(farmer)
                    .category(cat)
                    .build());
        }
        log.info("[Seed] 新增 {} 筆範例商品", demos.size());
    }

    private void seedFarmTripCategories() {
        record Cat(String code, String name, String icon, int order) {}
        List<Cat> defaults = List.of(
                new Cat("harvest",  "採收體驗",   "🌾", 1),
                new Cat("diy",      "DIY 工作坊", "🛠️", 2),
                new Cat("tour",     "農場導覽",   "🏡", 3),
                new Cat("ecology",  "生態探索",   "🦋", 4),
                new Cat("camp",     "農場營隊",   "⛺", 5)
        );
        for (Cat c : defaults) {
            if (!farmTripCategoryRepository.existsByCode(c.code())) {
                farmTripCategoryRepository.save(FarmTripCategory.builder()
                        .code(c.code()).name(c.name()).icon(c.icon()).sortOrder(c.order())
                        .build());
            }
        }
    }

    private void seedDemoFarmTrips() {
        if (farmTripRepository.count() > 0) return;
        User farmer = userRepository.findByEmail("demo@farmer.com").orElse(null);
        if (farmer == null) return;

        record Demo(String catCode, TripType type, String title, String intro, String img,
                    String location, String price, int capacity, int daysToTrip) {}
        List<Demo> demos = List.of(
                new Demo("harvest", TripType.FARM_EXPERIENCE,
                        "麥寮小白菜採收 + 認識有機農法",
                        "走進溫室親手採收當天現摘的小白菜,跟著阿美姐學認識友善土地的栽種方式。含午餐、紀念品,適合親子。",
                        "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800",
                        "雲林縣麥寮鄉", "680.00", 20, 14),
                new Demo("tour", TripType.FIELD_VISIT,
                        "梨山高山果園產地參訪",
                        "海拔 2000 公尺的蘋果園實地走訪,看採摘現場、品評熟成蘋果,認識高山栽培的辛苦。",
                        "https://images.unsplash.com/photo-1589217157232-464b505b197f?w=800",
                        "台中市和平區梨山", "1200.00", 15, 21),
                new Demo("diy", TripType.FARM_EXPERIENCE,
                        "地瓜窯烤 + 手作地瓜餅 DIY",
                        "從田裡挖地瓜開始,接著用磚窯烤地瓜,最後做地瓜餅帶回家。整天行程含三餐。",
                        "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800",
                        "雲林縣水林鄉", "880.00", 16, 10)
        );
        LocalDateTime now = LocalDateTime.now();
        for (Demo d : demos) {
            FarmTripCategory cat = farmTripCategoryRepository.findByCode(d.catCode()).orElse(null);
            if (cat == null) continue;
            LocalDateTime tripStart = now.plusDays(d.daysToTrip()).withHour(9).withMinute(0).withSecond(0).withNano(0);
            farmTripRepository.save(FarmTrip.builder()
                    .farmer(farmer)
                    .category(cat)
                    .tripType(d.type())
                    .title(d.title())
                    .intro(d.intro())
                    .imageUrl(d.img())
                    .location(d.location())
                    .price(new BigDecimal(d.price()))
                    .capacity(d.capacity())
                    .currentBookings(0)
                    .tripStart(tripStart)
                    .tripEnd(tripStart.plusHours(8))
                    .bookStart(now)
                    .bookEnd(tripStart.minusDays(2))
                    .status(FarmTripStatus.ACTIVE)
                    .build());
        }
        log.info("[Seed] 新增 {} 筆範例體驗活動", demos.size());
    }

    private void seedBlogTypes() {
        record T(String name, String desc, String icon, int order) {}
        List<T> defaults = List.of(
                new T("產地故事",   "走訪小農、採訪幕後", "📖", 1),
                new T("料理食譜",   "在地食材的料理分享", "🍳", 2),
                new T("耕種知識",   "農法、節氣、選擇",   "🌱", 3),
                new T("體驗心得",   "農場體驗活動分享",   "✏️", 4),
                new T("活動公告",   "平台與小農活動消息", "📢", 5)
        );
        for (T t : defaults) {
            if (!blogTypeRepository.existsByName(t.name())) {
                blogTypeRepository.save(BlogType.builder()
                        .name(t.name()).description(t.desc()).icon(t.icon()).sortOrder(t.order())
                        .build());
            }
        }
    }

    private void seedDemoBlogs() {
        if (blogRepository.count() > 0) return;
        User farmer = userRepository.findByEmail("demo@farmer.com").orElse(null);
        User admin = userRepository.findByEmail("admin@nong.com").orElse(null);
        if (farmer == null) return;

        record Demo(String typeName, User author, String title, String content, String img) {}
        List<Demo> demos = List.of(
                new Demo("產地故事", farmer,
                        "雲林麥寮的清晨,我和我的有機溫室",
                        "每天清晨 5 點,我就會起床走進溫室。露水還沒散,小白菜的葉片上還閃著光。\n\n做有機這條路不容易。十年前剛接手這片田時,我的鄰居都笑我傻 — 「不噴藥怎麼種得活?」但我堅持下來,因為我相信土地會回報你的真心。\n\n這篇想跟大家分享我們溫室的日常,還有為什麼我堅持友善耕作。",
                        "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200"),
                new Demo("料理食譜", farmer,
                        "三步驟簡單做:小白菜炒蒜片",
                        "材料:小白菜一把、大蒜 4 瓣、鹽巴少許、橄欖油\n\n步驟:\n1. 小白菜洗淨切段,大蒜切片。\n2. 鍋中放橄欖油加熱,放入蒜片爆香。\n3. 下小白菜大火快炒,加鹽起鍋。\n\n簡單的食材,簡單的料理,反而最能吃出蔬菜本身的鮮甜。",
                        "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=1200"),
                new Demo("耕種知識", farmer,
                        "什麼是友善耕作?跟有機農法有什麼不同",
                        "「友善耕作」不等於「有機認證」。友善耕作強調的是與環境共存、減少化學投入,但不一定通過第三方驗證。\n\n有機認證有嚴格規範:三年轉作期、獨立檢測、定期稽核。對小農來說成本高。\n\n友善耕作則更彈性,著重在實際做法 — 例如不使用合成農藥、不施化肥、保留田邊原生植物作為棲地。",
                        "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=1200"),
                new Demo("活動公告", admin == null ? farmer : admin,
                        "你儂我農 平台正式上線!",
                        "經過半年的開發,平台終於正式上線了。\n\n我們的願景很簡單:讓你直接認識每一位小農、每一塊土地。沒有層層中盤、沒有刻意行銷,只有真實的食物與真實的故事。\n\n上線首月,所有體驗活動 9 折優惠,團購商品免運。歡迎大家來體驗!",
                        "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200")
        );

        for (Demo d : demos) {
            BlogType type = blogTypeRepository.findByName(d.typeName()).orElse(null);
            if (type == null) continue;
            blogRepository.save(Blog.builder()
                    .author(d.author())
                    .blogType(type)
                    .title(d.title())
                    .content(d.content())
                    .coverImageUrl(d.img())
                    .likeCount(0).commentCount(0).viewCount(0)
                    .status(BlogStatus.PUBLISHED)
                    .build());
        }
        log.info("[Seed] 新增 {} 筆範例部落格文章", demos.size());
    }
}
