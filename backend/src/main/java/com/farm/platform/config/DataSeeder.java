package com.farm.platform.config;

import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.Admin;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.entity.Member;
import com.farm.platform.account.entity.MembershipLevel;
import com.farm.platform.admin.entity.AdminClass;
import com.farm.platform.admin.entity.AdminClassRole;
import com.farm.platform.admin.entity.AdminRoleDef;
import com.farm.platform.blog.entity.Blog;
import com.farm.platform.blog.entity.BlogStatus;
import com.farm.platform.blog.entity.BlogType;
import com.farm.platform.farmtrip.entity.FarmTrip;
import com.farm.platform.farmtrip.entity.FarmTripAudit;
import com.farm.platform.farmtrip.entity.FarmTripAuditStatus;
import com.farm.platform.farmtrip.entity.FarmTripSession;
import com.farm.platform.farmtrip.entity.FarmTripSessionStatus;
import com.farm.platform.farmtrip.entity.FarmTripStatus;
import com.farm.platform.farmtrip.entity.TripType;
import com.farm.platform.news.entity.News;
import com.farm.platform.news.entity.NewsStatus;
import com.farm.platform.shop.entity.Category;
import com.farm.platform.shop.entity.PricingMode;
import com.farm.platform.shop.entity.Product;
import com.farm.platform.shop.entity.ProductStatus;
import com.farm.platform.account.repository.AdminRepository;
import com.farm.platform.account.repository.FarmerRepository;
import com.farm.platform.account.repository.MemberRepository;
import com.farm.platform.admin.repository.AdminClassRepository;
import com.farm.platform.admin.repository.AdminClassRoleRepository;
import com.farm.platform.admin.repository.AdminRoleDefRepository;
import com.farm.platform.blog.repository.BlogRepository;
import com.farm.platform.blog.repository.BlogTypeRepository;
import com.farm.platform.farmtrip.repository.FarmTripAuditRepository;
import com.farm.platform.farmtrip.repository.FarmTripRepository;
import com.farm.platform.farmtrip.repository.FarmTripSessionRepository;
import com.farm.platform.news.repository.NewsRepository;
import com.farm.platform.shop.repository.CategoryRepository;
import com.farm.platform.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 種子採「不存在時建立」策略，重複執行不會炸。
 * 主要產出：5 個小農 × (3 商品 + 1 體驗活動 + 1 部落格文章)。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminClassRepository adminClassRepository;
    private final AdminRoleDefRepository adminRoleDefRepository;
    private final AdminClassRoleRepository adminClassRoleRepository;
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final FarmerRepository farmerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BlogTypeRepository blogTypeRepository;
    private final BlogRepository blogRepository;
    private final FarmTripRepository farmTripRepository;
    private final FarmTripSessionRepository farmTripSessionRepository;
    private final FarmTripAuditRepository farmTripAuditRepository;
    private final NewsRepository newsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminRolesAndClasses();
        Admin admin = seedAdminAccount();
        seedDemoMember();

        seedBlogTypes();
        migrateLegacyBlogTypes();

        seedCategories();

        List<Farmer> farmers = seedDemoFarmers();
        for (Farmer f : farmers) {
            seedProductsForFarmer(f);
            seedFarmTripForFarmer(f, admin);
            seedFarmerBlog(f);
        }
        backfillProductImages();

        seedNews(admin);
    }

    /** 為任何 imageUrl 為空的商品補預設 unsplash 圖（防止前台卡片少圖）。 */
    private void backfillProductImages() {
        java.util.Map<String, String> defaults = java.util.Map.of(
                "有機小白菜",   "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800",
                "梨山富士蘋果", "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800",
                "花蓮糙米",     "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800",
                "紅蘿蔔",       "https://images.unsplash.com/photo-1582515073490-39981397c445?w=800",
                "土雞蛋",       "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=800"
        );
        String genericFallback = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=800";
        int count = 0;
        for (Product p : productRepository.findAll()) {
            if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) continue;
            String url = defaults.getOrDefault(p.getName(), genericFallback);
            p.setImageUrl(url);
            productRepository.save(p);
            count++;
        }
        if (count > 0) log.info("[Seeder] 已為 {} 個商品補上預設圖片", count);
    }

    /* ============================ Admin Role / Class ============================ */

    private void seedAdminRolesAndClasses() {
        List<String> roleNames = List.of("超級管理員", "管理員", "審核員", "爭議處理員");
        for (String n : roleNames) {
            adminRoleDefRepository.findByName(n).orElseGet(() ->
                    adminRoleDefRepository.save(AdminRoleDef.builder().name(n).description(n + "權限").build()));
        }
        AdminClass superClass = adminClassRepository.findByName("超級管理員").orElseGet(() ->
                adminClassRepository.save(AdminClass.builder().name("超級管理員").description("擁有全部功能權限").build()));
        adminRoleDefRepository.findAll().forEach(role -> {
            AdminClassRole.Key key = new AdminClassRole.Key(superClass.getId(), role.getId());
            if (!adminClassRoleRepository.existsById(key)) {
                adminClassRoleRepository.save(AdminClassRole.builder()
                        .classId(superClass.getId()).roleId(role.getId()).build());
            }
        });
    }

    private Admin seedAdminAccount() {
        return adminRepository.findByEmail("admin@nong.com").orElseGet(() -> {
            AdminClass superClass = adminClassRepository.findByName("超級管理員")
                    .orElseThrow(() -> new IllegalStateException("超級管理員 class 未建立"));
            Admin a = adminRepository.save(Admin.builder()
                    .email("admin@nong.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .name("系統管理員")
                    .classId(superClass.getId())
                    .status(AccountStatus.NORMAL)
                    .build());
            log.info("[Seeder] 建立超級管理員 admin@nong.com / admin1234");
            return a;
        });
    }

    private void seedDemoMember() {
        if (memberRepository.existsByEmail("demo@member.com")) return;
        memberRepository.save(Member.builder()
                .email("demo@member.com")
                .password(passwordEncoder.encode("123456"))
                .name("範例會員").nickname("demo")
                .phone("0900000001").address("台北市信義區範例路 1 號")
                .level(MembershipLevel.GENERAL).status(AccountStatus.NORMAL)
                .isFarmer(false).build());
        log.info("[Seeder] 建立範例會員 demo@member.com / 123456");
    }

    /* ============================ Blog Types：4 類，產地日記限定小農 ============================ */

    private void seedBlogTypes() {
        record Bt(String name, String desc, String icon, int sort, boolean farmerOnly) {}
        List<Bt> types = List.of(
                new Bt("產地日記",     "小農自家栽培紀錄與分享（限小農發表）", "🌾", 1, true),
                new Bt("蔬果知識分享", "蔬果挑選、保存、營養小百科",         "🥗", 2, false),
                new Bt("農作體驗回顧", "參加體驗活動或產地參訪的心得",        "📸", 3, false),
                new Bt("食譜分享",     "在家就能做的料理",                  "🍳", 4, false)
        );
        for (Bt t : types) {
            blogTypeRepository.findByName(t.name()).ifPresentOrElse(
                    existing -> {
                        boolean changed = false;
                        if (existing.getFarmerOnly() == null || existing.getFarmerOnly() != t.farmerOnly()) {
                            existing.setFarmerOnly(t.farmerOnly()); changed = true;
                        }
                        if (existing.getSortOrder() == null || existing.getSortOrder() != t.sort()) {
                            existing.setSortOrder(t.sort()); changed = true;
                        }
                        if (changed) blogTypeRepository.save(existing);
                    },
                    () -> blogTypeRepository.save(BlogType.builder()
                            .name(t.name()).description(t.desc()).icon(t.icon())
                            .sortOrder(t.sort()).farmerOnly(t.farmerOnly()).build())
            );
        }
    }

    /**
     * 把舊類別 (「食材料理」「營養知識」「活動公告」「社群交流」「產地故事」「最新消息」) 的文章
     * 遷移到新類別，然後刪除舊類別。
     *
     * 使用 bulk update (JPQL @Modifying) 而非 dirty checking，避免 commit 時 UPDATE/DELETE 順序問題。
     */
    private void migrateLegacyBlogTypes() {
        BlogType fallback = blogTypeRepository.findByName("蔬果知識分享").orElseThrow();
        BlogType farmerDiary = blogTypeRepository.findByName("產地日記").orElseThrow();
        // 「產地故事」歷史上只有小農用 → 遷到「產地日記」；其餘遷到「蔬果知識分享」
        String[] obsolete = {"食材料理", "營養知識", "活動公告", "社群交流", "產地故事", "最新消息"};
        for (String n : obsolete) {
            BlogType old = blogTypeRepository.findByName(n).orElse(null);
            if (old == null) continue;
            BlogType target = "產地故事".equals(n) ? farmerDiary : fallback;
            int migrated = blogRepository.reassignBlogType(old, target);
            blogTypeRepository.delete(old);
            log.info("[Seeder] 已刪除舊類別「{}」並遷移 {} 篇文章 → {}", n, migrated, target.getName());
        }
    }

    /* ============================ 商品分類 ============================ */

    private void seedCategories() {
        record Cat(String code, String name, String icon, int sort) {}
        List<Cat> cats = List.of(
                new Cat("vegetable", "葉菜類", "🥬", 1),
                new Cat("root",      "根莖類", "🥕", 2),
                new Cat("fruit",     "水果類", "🍎", 3),
                new Cat("grain",     "穀物類", "🌾", 4),
                new Cat("egg",       "蛋類",   "🥚", 5),
                new Cat("processed", "加工品", "🍯", 6),
                new Cat("tea",       "茶飲",   "🍵", 7),
                new Cat("other",     "其他",   "🛒", 8)
        );
        for (Cat c : cats) {
            if (!categoryRepository.existsByCode(c.code())) {
                categoryRepository.save(Category.builder()
                        .code(c.code()).name(c.name()).icon(c.icon())
                        .sortOrder(c.sort()).build());
            }
        }
    }

    /* ============================ 5 位小農 ============================ */

    private List<Farmer> seedDemoFarmers() {
        record FarmerSeed(String email, String farmName, String address, String phone, String desc,
                          String lat, String lng) {}
        List<FarmerSeed> seeds = List.of(
                new FarmerSeed("demo@farmer.com",    "示範農場",       "宜蘭縣三星鄉示範路 1 號",   "0900000002", "宜蘭三星葉菜根莖多元種植，平台示範用。",
                        "24.6667", "121.6500"),
                new FarmerSeed("orchard@farmer.com", "梨山雲頂果園",   "台中市和平區梨山里中興路 88 號", "0900000003", "海拔 2000 公尺低溫栽培，蘋果水梨高山桃當令。",
                        "24.2542", "121.2550"),
                new FarmerSeed("paddy@farmer.com",   "池上禾田",       "台東縣池上鄉萬安村稻香路 12 號", "0900000004", "縱谷無毒栽培稻米，糙米白米糯米皆有。",
                        "23.1240", "121.2190"),
                new FarmerSeed("tea@farmer.com",     "杉林溪有機茶坊", "南投縣鹿谷鄉內湖村凍頂巷 5 號",  "0900000005", "凍頂烏龍世家三代，全程有機栽培。",
                        "23.6745", "120.7795"),
                new FarmerSeed("coop@farmer.com",    "東山放牧蛋場",   "台南市東山區嶺南里果毅後 26 號", "0900000006", "完全放牧土雞，蛋黃色澤金黃濃郁。",
                        "23.3206", "120.4036")
        );

        List<Farmer> result = new ArrayList<>();
        for (FarmerSeed s : seeds) {
            Farmer f = farmerRepository.findByEmail(s.email()).orElseGet(() -> {
                Farmer saved = farmerRepository.save(Farmer.builder()
                        .email(s.email())
                        .password(passwordEncoder.encode("123456"))
                        .farmName(s.farmName())
                        .farmAddress(s.address())
                        .phone(s.phone())
                        .farmDesc(s.desc())
                        .locLat(new BigDecimal(s.lat()))
                        .locLong(new BigDecimal(s.lng()))
                        .certPassed(true)
                        .status(AccountStatus.NORMAL)
                        .build());
                log.info("[Seeder] 建立範例小農 {} / 123456 ({})", s.email(), s.farmName());
                return saved;
            });
            if (isMissingDemoCoordinate(f)) {
                f.setLocLat(new BigDecimal(s.lat()));
                f.setLocLong(new BigDecimal(s.lng()));
                f = farmerRepository.save(f);
                log.info("[Seeder] 已補上範例小農 {} 的地圖座標", s.email());
            }
            result.add(f);
        }
        return result;
    }

    private boolean isMissingDemoCoordinate(Farmer farmer) {
        return farmer.getLocLat() == null
                || farmer.getLocLong() == null
                || BigDecimal.ZERO.compareTo(farmer.getLocLat()) == 0
                || BigDecimal.ZERO.compareTo(farmer.getLocLong()) == 0;
    }

    /* ============================ 每位小農 3 個商品 ============================ */

    private record ProductSeed(String catCode, String name, String desc, String price, String unit,
                               int stock, String origin, String shipping, boolean groupBuyEnabled, String img) {}

    private void seedProductsForFarmer(Farmer f) {
        List<ProductSeed> seeds = switch (f.getEmail()) {
            case "demo@farmer.com" -> List.of(
                    new ProductSeed("vegetable", "有機小白菜", "無農藥栽培，當日採收", "60", "斤", 50, "宜蘭三星", "黑貓", true,
                            "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800"),
                    new ProductSeed("root",      "胡蘿蔔",     "產地直送，自然甜度",  "45", "斤", 60, "宜蘭三星", "黑貓", false,
                            "https://images.unsplash.com/photo-1582515073490-39981397c445?w=800"),
                    new ProductSeed("vegetable", "青蔥",       "三星上將梨蔥，蔥白厚", "120", "把", 40, "宜蘭三星", "黑貓", true,
                            "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=800")
            );
            case "orchard@farmer.com" -> List.of(
                    new ProductSeed("fruit", "梨山富士蘋果", "高山低溫栽培、甜脆多汁", "180", "公斤", 80, "台中梨山", "黑貓", true,
                            "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800"),
                    new ProductSeed("fruit", "雪梨水梨",     "果肉清甜、清香爆汁",    "150", "公斤", 70, "台中梨山", "黑貓", true,
                            "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=800"),
                    new ProductSeed("fruit", "高山水蜜桃",   "夏季限定、香氣濃郁",    "220", "公斤", 40, "台中梨山", "黑貓", false,
                            "https://images.unsplash.com/photo-1595124983639-89bce62c50b8?w=800")
            );
            case "paddy@farmer.com" -> List.of(
                    new ProductSeed("grain", "池上糙米",   "無毒栽培糙米，營養滿點", "200", "公斤", 60, "台東池上", "全家店到店", true,
                            "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"),
                    new ProductSeed("grain", "池上白米",   "縱谷產區優質白米",      "180", "公斤", 80, "台東池上", "全家店到店", true,
                            "https://images.unsplash.com/photo-1568347877321-f8935c7dc5a8?w=800"),
                    new ProductSeed("grain", "池上糯米",   "釀酒做粿都合適",        "210", "公斤", 30, "台東池上", "全家店到店", false,
                            "https://images.unsplash.com/photo-1626078434326-7c87706cab69?w=800")
            );
            case "tea@farmer.com" -> List.of(
                    new ProductSeed("tea", "凍頂烏龍茶", "中焙火、回甘明顯",     "650", "150g", 50, "南投鹿谷", "黑貓", false,
                            "https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=800"),
                    new ProductSeed("tea", "杉林溪高山茶", "海拔 1600 公尺、花香清雅", "900", "150g", 30, "南投鹿谷", "黑貓", true,
                            "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=800"),
                    new ProductSeed("tea", "蜜香紅茶",   "小綠葉蟬咬過、自然蜜香", "780", "150g", 25, "南投鹿谷", "黑貓", false,
                            "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=800")
            );
            case "coop@farmer.com" -> List.of(
                    new ProductSeed("egg", "放牧土雞蛋", "完全放牧、蛋黃濃郁",    "12", "顆", 300, "台南東山", "自取", false,
                            "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=800"),
                    new ProductSeed("egg", "溫泉鄉鴨蛋", "東山溫泉養殖",         "18", "顆", 200, "台南東山", "黑貓", false,
                            "https://images.unsplash.com/photo-1607103058027-4c5a09b3a9b9?w=800"),
                    new ProductSeed("processed", "古早味鹹蛋", "傳統工法、油亮金黃", "25", "顆", 150, "台南東山", "黑貓", true,
                            "https://images.unsplash.com/photo-1614807546936-d40c75e54e0a?w=800")
            );
            default -> List.of();
        };
        for (ProductSeed s : seeds) {
            if (productRepository.existsByNameAndFarmer(s.name(), f)) continue;
            Category cat = categoryRepository.findByCode(s.catCode()).orElseThrow(() ->
                    new IllegalStateException("找不到分類 code=" + s.catCode()));
            productRepository.save(Product.builder()
                    .name(s.name()).description(s.desc())
                    .price(new BigDecimal(s.price())).unit(s.unit()).stock(s.stock())
                    .origin(s.origin()).shippingMethod(s.shipping())
                    .groupBuyEnabled(s.groupBuyEnabled())
                    .imageUrl(s.img())
                    .status(ProductStatus.ACTIVE)
                    .farmer(f).category(cat).build());
        }
    }

    /* ============================ 每位小農 1 個體驗活動 + 場次 + 審核通過 ============================ */

    private record TripSeed(TripType type, PricingMode mode, String title, String intro, String image,
                            String price, Integer capacityPerSession, int[] daysFromNow, int durationHours) {}

    private void seedFarmTripForFarmer(Farmer f, Admin admin) {
        TripSeed seed = switch (f.getEmail()) {
            case "demo@farmer.com" -> new TripSeed(TripType.FARM_EXPERIENCE, PricingMode.PER_PERSON,
                    "三星蔥採摘體驗", "親手採摘三星蔥、現包蔥油餅",
                    "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=800",
                    "300", 20, new int[]{7, 14, 21}, 3);
            case "orchard@farmer.com" -> new TripSeed(TripType.FARM_EXPERIENCE, PricingMode.PER_WEIGHT,
                    "高山蘋果現採論斤秤", "親手採摘蘋果，現場過磅論斤計費",
                    "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800",
                    "180", null, new int[]{10, 17}, 8);
            case "paddy@farmer.com" -> new TripSeed(TripType.FIELD_VISIT, PricingMode.PER_PERSON,
                    "池上稻浪導覽一日遊", "走訪池上萬安田、稻米加工廠",
                    "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800",
                    "500", 15, new int[]{12}, 6);
            case "tea@farmer.com" -> new TripSeed(TripType.FARM_EXPERIENCE, PricingMode.PER_PERSON,
                    "凍頂烏龍製茶體驗", "從採茶、揉茶到品茗一日完成",
                    "https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=800",
                    "650", 12, new int[]{15, 22}, 5);
            case "coop@farmer.com" -> new TripSeed(TripType.FIELD_VISIT, PricingMode.PER_PERSON,
                    "東山放牧蛋場參訪", "看雞如何放牧、體驗撿蛋",
                    "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=800",
                    "350", 18, new int[]{14}, 2);
            default -> null;
        };
        if (seed == null) return;
        if (farmTripRepository.existsByTitleAndFarmer(seed.title(), f)) return;

        FarmTrip t = farmTripRepository.save(FarmTrip.builder()
                .farmer(f).tripType(seed.type()).pricingMode(seed.mode())
                .title(seed.title()).intro(seed.intro()).imageUrl(seed.image())
                .location(f.getFarmAddress())
                .price(new BigDecimal(seed.price()))
                .capacityPerSession(seed.capacityPerSession())
                .status(FarmTripStatus.ACTIVE)
                .ratingCount(0).ratingTotalStars(0)
                .build());

        farmTripAuditRepository.save(FarmTripAudit.builder()
                .farmTrip(t).admin(admin)
                .status(FarmTripAuditStatus.APPROVED).reason("種子資料自動通過").build());

        LocalDateTime now = LocalDateTime.now();
        for (int daysOut : seed.daysFromNow()) {
            LocalDateTime start = now.plusDays(daysOut).withHour(9).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(seed.durationHours());
            LocalDateTime bookEnd = start.minusDays(3).withHour(23).withMinute(59);
            if (bookEnd.isBefore(now)) bookEnd = start.minusHours(1);

            farmTripSessionRepository.save(FarmTripSession.builder()
                    .farmTrip(t)
                    .sessionPrice(new BigDecimal(seed.price()))
                    .tripStart(start).tripEnd(end)
                    .bookStart(now.minusDays(1)).bookEnd(bookEnd)
                    .attendance(0)
                    .status(FarmTripSessionStatus.ACTIVE).build());
        }
        log.info("[Seeder] {} 的體驗活動「{}」+ {} 場已建立", f.getFarmName(), seed.title(), seed.daysFromNow().length);
    }

    /* ============================ 每位小農 1 篇部落格文章 ============================ */

    private void seedFarmerBlog(Farmer f) {
        record DiarySeed(String typeName, String title, String content, String cover) {}
        DiarySeed seed = switch (f.getEmail()) {
            case "demo@farmer.com" -> new DiarySeed(
                    "產地日記",
                    "三星地區的小白菜栽培紀錄",
                    "從整地、播種到採收，記錄宜蘭三星的小白菜栽培過程。\n我們堅持不噴農藥，雖然辛苦，但能讓消費者吃得安心。",
                    "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800");
            case "orchard@farmer.com" -> new DiarySeed(
                    "蔬果知識分享",
                    "高山蘋果挑選與保存方式",
                    "梨山蘋果最怕悶熱，收到後建議放入冷藏蔬果室。\n挑選時可以看果皮亮度、香氣與重量，沉手的通常水分更足。",
                    "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800");
            case "paddy@farmer.com" -> new DiarySeed(
                    "農作體驗回顧",
                    "池上稻浪導覽體驗回顧",
                    "這次帶大家走進萬安田區，從水圳、秧苗到稻穗成熟都實地看一次。\n孩子們第一次摸到稻穀，也理解一碗飯背後的時間。",
                    "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800");
            case "tea@farmer.com" -> new DiarySeed(
                    "食譜分享",
                    "冷泡烏龍茶與茶香飯做法",
                    "高山烏龍很適合冷泡，茶葉與冷水約 1:80，放冰箱 6 小時即可。\n泡完的茶葉也能拌入白飯與海鹽，做成清爽茶香飯。",
                    "https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=800");
            case "coop@farmer.com" -> new DiarySeed(
                    "產地日記",
                    "東山放牧蛋場的雞舍日常",
                    "我們的雞每天都在山林裡放牧、自由覓食。\n蛋黃顏色金黃濃郁，因為餵的是無毒蔬菜與穀物。",
                    "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=800");
            default -> null;
        };
        if (seed == null) return;

        BlogType type = blogTypeRepository.findByName(seed.typeName()).orElseThrow();
        Blog blog = blogRepository.findFirstByAuthorFarmerOrderByCreatedAtDesc(f)
                .orElseGet(() -> Blog.builder().authorFarmer(f).build());
        blog.setBlogType(type);
        blog.setTitle(seed.title());
        blog.setContent(seed.content());
        blog.setCoverImageUrl(seed.cover());
        blog.setStatus(BlogStatus.PUBLISHED);
        blogRepository.save(blog);
    }

    /* ============================ 最新消息 ============================ */

    private void seedNews(Admin admin) {
        if (newsRepository.count() > 0) return;
        LocalDateTime now = LocalDateTime.now();
        newsRepository.save(News.builder()
                .title("平台正式上線囉！")
                .summary("你儂我農農產直送平台 1.0 上線，串接小農與消費者。")
                .content("我們致力於把產地直送到您的餐桌。\n首批合作小農 5 位，涵蓋葉菜、水果、糧穀、茶飲、蛋禽。\n團購、體驗活動、產地日記全功能開放中。")
                .coverImageUrl("https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=1200")
                .status(NewsStatus.PUBLISHED).admin(admin).publishedAt(now.minusDays(3))
                .build());
        newsRepository.save(News.builder()
                .title("體驗活動新增「按採收重量計價」模式")
                .summary("活動可依採摘水果重量計算金額，自由度更高、消費者體驗更貼近真實採收。")
                .content("新增「按採收重量計價」模式：\n- 預約只佔位、活動當天現場過磅結算\n- 場次無人數上限\n- 報名截止建議活動前 3 天\n\n首批採用此模式的活動：梨山雲頂果園的高山蘋果現採論斤秤。")
                .coverImageUrl("https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=1200")
                .status(NewsStatus.PUBLISHED).admin(admin).publishedAt(now.minusDays(1))
                .build());
        log.info("[Seeder] 已建立 2 筆最新消息");
    }
}
