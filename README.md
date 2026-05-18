# 你儂我農（Nong Nong）

CFA104 班專題 — 第二組「我家門前有塊地」

> 連結台灣小農與你的餐桌，吃得新鮮，吃得安心。

## 技術棧

- **後端**：Java 17+ / Spring Boot 3.4 / Spring Security / Spring Data JPA / JWT / MySQL 8.0 / **Redis**
- **前端**：純 HTML5 + CSS3 + JavaScript（無框架、無建置工具）
- **建置**：Maven 3.9+
- **靜態服務**：前端檔案放在 `backend/src/main/resources/static/`，由 Spring Boot 同源服務

## 必備依賴（開發機要先裝）

| 工具 | 版本 | 用途 |
|---|---|---|
| JDK | 17+（25 也可） | 編譯與執行 |
| Maven | 3.9+ | 建置工具 |
| MySQL | 8.0（utf8mb4） | 主資料庫 |
| Redis | 6+（預設 `localhost:6379`） | **購物車存儲**，沒裝後台會 connect refused |

## 專案結構

```
farm-platform/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/farm/platform/
│       │   ├── FarmPlatformApplication.java
│       │   ├── config/        ← SecurityConfig / DataSeeder
│       │   ├── controller/    ← REST API
│       │   ├── dto/           ← 請求 / 回應物件
│       │   ├── entity/        ← JPA 實體
│       │   ├── repository/    ← 資料存取層
│       │   ├── security/      ← JWT 工具 + Filter
│       │   └── service/       ← 業務邏輯
│       └── resources/
│           ├── application.yml
│           └── static/        ← 47 個 HTML 頁面 + css / js
├── scripts/                  ← 資料庫 migration 草稿
└── frontend_vue_backup/      ← 舊 Vue 版備份（已停用）
```

## 開發進度

| Phase | 內容 | 狀態 |
|---|---|---|
| 1 | 會員系統 / JWT 登入註冊 | ✅ |
| 2 | 商品管理 + 公開瀏覽 + 分類 | ✅ |
| 3 | 購物車 + 結帳 + 訂單 / 付款 / 出貨 / 收貨 | ✅ |
| 4 | 團購（發起 / 加入 / 審核 / 成團結算）+ 體驗活動 CRUD + 預約 | ✅ |
| 5 | GB_ORDER 重構（host 一張整單）+ 託管金流欄位 | ✅ |
| 6 | 後台管理（會員啟停 / 商品下架）+ 部落格 + 檢舉處理 | ✅ |
| A | 拆分 USER → Member / Farmer / Admin 三套帳號表 | ✅ |
| B | 所有 controller 復活、entity FK 切到 Member/Farmer | ✅ |
| C | **體驗活動 5 表重構**：trip / session / audit / order / comment | ✅ |
| – | 商品收藏 wishlist | ✅ |
| – | **最新消息 NEWS** 獨立模組 | ✅ |
| – | 部落格類別整理（產地日記限定小農）+ 文章關聯商品 | ✅ |
| – | 場次按重量計價（PER_WEIGHT，現場過磅結算） | ✅ |
| – | 小農批次新增場次（日期區間 + 每日固定時段） | ✅ |
| – | 管理員體驗活動審核流程 | ✅ |

## 啟動步驟（第一次拿到 repo 或在新電腦上）

### 1. 建立資料庫

MySQL Workbench 或 CLI：

```sql
CREATE DATABASE farm_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 確認 Redis 已啟動

```powershell
# Windows：用 chocolatey 或 WSL 都可
redis-server
# 應該看到 6379 port 在 listen
```

### 3. 設定資料庫密碼

`backend/src/main/resources/application.yml` 預設 `${DB_PASSWORD:123456}` — fallback 為 123456。其他密碼用環境變數覆寫：

```powershell
$env:DB_PASSWORD = "你的MySQL密碼"
```

### 4. 啟動後端

IntelliJ：直接跑 `FarmPlatformApplication.main()`；或命令列：

```powershell
cd backend
mvn spring-boot:run
```

啟動時：
- Hibernate `ddl-auto=update` 會自動建立所有表
- `DataSeeder` 會灌入種子資料（5 個小農 + 商品 + 體驗活動 + 場次 + 部落格 + 最新消息）

### 5. 打開瀏覽器

```
http://localhost:8080/
```

---

## ⚠️ 升級指引（已 clone 過舊版的人）

`schema` 變動非常大，**最簡單也最安全的方式是 DROP DATABASE 重建**：

```sql
DROP DATABASE farm_platform;
CREATE DATABASE farm_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

接著重啟 Spring Boot，Hibernate 自動建新 schema，DataSeeder 自動補種子。

### 為什麼不能用 `mvn spring-boot:run` 直接跑舊 DB

- `ddl-auto=update` 只 ADD 不 DROP，舊欄位（如 `farm_trips.trip_start NOT NULL`）會留著卡住新資料插入
- 新增的 NOT NULL 欄位（如 `pricing_mode`）對舊資料會炸（無 DEFAULT）
- 舊 `farm_trip_bookings` 表的 FK 還指向已刪除的 entity

### 主要 schema 變動清單

- **刪表**：`users` / `user_roles` / `farm_trip_categories` / `farm_trip_bookings`
- **新增表**：`farm_trip_sessions` / `farm_trip_audits` / `farm_trip_orders` / `farm_trip_comments` / `news` / `blog_products` / `product_wishlists`
- **`farm_trips` 全換**：加 `pricing_mode` / `capacity_per_session` / `rating_count` / `rating_total_stars`；移除時間 / 容量 / category 等欄位
- **`blog_types`**：加 `farmer_only` BIT
- **舊 `User` entity 已刪**，登入登出邏輯改走 Member / Farmer / Admin 三表

---

## 預設測試帳號（DataSeeder 自動建立）

| 角色 | Email | 密碼 | 農場 / 名稱 |
|---|---|---|---|
| 管理員 | `admin@nong.com` | `admin1234` | 系統管理員 |
| 範例會員 | `demo@member.com` | `123456` | 範例會員 |
| 小農 1 | `demo@farmer.com` | `123456` | 示範農場（宜蘭三星，葉菜 / 根莖） |
| 小農 2 | `orchard@farmer.com` | `123456` | 梨山雲頂果園（台中梨山，蘋果 / 梨 / 桃） |
| 小農 3 | `paddy@farmer.com` | `123456` | 池上禾田（台東池上，糙米 / 白米 / 糯米） |
| 小農 4 | `tea@farmer.com` | `123456` | 杉林溪有機茶坊（南投鹿谷，烏龍 / 紅茶） |
| 小農 5 | `coop@farmer.com` | `123456` | 東山放牧蛋場（台南東山，雞蛋 / 鴨蛋 / 鹹蛋） |

每位小農種子預設 3 個商品 + 1 個體驗活動（含場次 + 通過審核）+ 1 篇產地日記。

管理員入口在隱藏網址：`http://localhost:8080/console-admin-x9k2p.html`

## 頁面一覽（47 頁）

**消費者前台（25）**
`index` `products` `product-detail` `group-buys` `group-buy-detail` `farm-trips` `farm-trip-detail` `blogs` `blog-detail` `news` `news-detail` `cart` `checkout` `orders` `order-detail` `profile` `login` `register` `farmer-register` `blog-form` `my-blogs` `my-group-buy-requests` `my-group-buys` `my-group-buy-orders` `my-farm-trip-bookings` `wishlist`

**小農工作台（13）**
`farmer`(dashboard) `farmer-products` `farmer-product-form` `farmer-orders` `farmer-group-buy-requests` `farmer-group-buys` `farmer-group-buy-orders` `farmer-farm-trips` `farmer-farm-trip-form` `farmer-farm-trip-sessions` `farmer-farm-trip-bookings` `farmer-blogs` `farmer-blog-form`

**管理員後台（8）**
`console-admin-x9k2p`(隱藏登入) `admin`(dashboard) `admin-users` `admin-products` `admin-farmer-reviews` `admin-farm-trip-audits` `admin-news` `admin-blog-reports`

## 會話策略

Phase A 拆分後一個帳號只屬一種身份（MEMBER / FARMER / ADMIN）。前端對不同身份用不同儲存：

| 身份 | 儲存位置 | 行為 |
|---|---|---|
| MEMBER | `localStorage` | 跨 tab 保留、關瀏覽器仍記得 |
| FARMER / ADMIN | `sessionStorage` | **關 tab 即清除**（離開後台 = 自動登出）|

小農工作台 / 管理員後台是完全獨立的頁面，沒有「回首頁」連結，避免使用者誤踩。

## API 速查

### 公開

| 方法 | 路徑 |
|---|---|
| POST | `/api/auth/login`（body 帶 `userType: MEMBER \| FARMER`）|
| POST | `/api/auth/member/register` |
| POST | `/api/auth/farmer/register` |
| POST | `/api/auth/admin/login`（隱藏）|
| GET | `/api/public/categories` |
| GET | `/api/public/products?categoryId=&keyword=&size=` |
| GET | `/api/public/products/{id}` |
| GET | `/api/group-buys` / `/api/group-buys/{id}` |
| GET | `/api/farm-trips?tripType=` / `/api/farm-trips/{id}` |
| GET | `/api/blogs?typeId=&keyword=` / `/api/blogs/{id}` / `/api/blogs/types` |
| GET | `/api/news` / `/api/news/{id}` |

### 會員（ROLE_MEMBER）

| 方法 | 路徑 |
|---|---|
| GET / PUT / DELETE | `/api/cart[/items[/{productId}]]` |
| POST | `/api/orders/checkout` |
| GET | `/api/orders` |
| POST | `/api/orders/{id}/pay` / `/cancel` / `/confirm` |
| POST | `/api/group-buys/{id}/join` / `/api/group-buys/requests`（發起）|
| POST | `/api/farm-trips/sessions/{sessionId}/orders`（預約場次）|
| POST | `/api/farm-trips/orders/{orderId}/cancel` |
| GET | `/api/farm-trips/orders/mine` |
| POST | `/api/farm-trips/{tripId}/comments`（必須有 COMPLETED 訂單才能評） |
| GET / POST / DELETE | `/api/wishlist[/products/{id}]` |
| POST | `/api/blogs`（content + productIds，可介紹商品） |
| GET / PUT | `/api/account/me` / `/profile` / `/password` |

### 小農（ROLE_FARMER）

| 方法 | 路徑 |
|---|---|
| GET / POST / PUT / DELETE | `/api/farmer/products[/{id}]` |
| GET | `/api/orders/farmer` |
| POST | `/api/orders/{id}/ship` |
| POST | `/api/farmer/group-buy-requests/{id}/review` |
| GET / POST / PUT | `/api/farmer/farm-trips[/{id}]` |
| POST | `/api/farmer/farm-trips/{id}/sessions/batch`（批次建場次）|
| POST | `/api/farmer/farm-trip-sessions/{id}/cancel` |
| GET | `/api/farmer/farm-trip-orders` |
| POST | `/api/farmer/farm-trip-orders/{id}/complete`（PER_WEIGHT 補登重量結算） |

### 管理員（ROLE_ADMIN）

| 方法 | 路徑 |
|---|---|
| GET | `/api/admin/stats` |
| GET / POST | `/api/admin/members[/{id}/enable\|disable]` |
| GET / POST | `/api/admin/farmers[/{id}/cert-pass\|cert-reject\|enable\|disable]` |
| GET | `/api/admin/farmers/pending` |
| GET / POST | `/api/admin/products[/{id}/take-down\|restore]` |
| GET / POST | `/api/admin/farm-trips/pending` / `/api/admin/farm-trips/{id}/audit` |
| GET / POST / PUT / DELETE | `/api/admin/news[/{id}]` |
| GET / POST | `/api/admin/blog-reports[/{id}/handle]` |

## 注意事項

- `application.yml` JDBC URL 用 `characterEncoding=UTF-8`，MySQL Connector 9.x 不認 `utf8mb4`
- `spring.jpa.open-in-view=false`，Service 層 lazy load 關聯時記得加 `@Transactional`
- 修改 `src/main/resources/static/` 下的檔案後，瀏覽器 Ctrl+F5 強制重整即可（不用重啟 Spring Boot）
- 修改 Java 程式碼後，DevTools 通常會自動 reload；但 `DataSeeder` (`CommandLineRunner`) 一定要完整重啟才會跑
- 用 PowerShell `Invoke-RestMethod` 測 API 時，中文要包 UTF-8 bytes，否則會傳 `????`
- 舊 Vue 版完整備份在 `frontend_vue_backup/`，要還原就改回 `frontend/` 並 `npm install && npm run dev`
