# 你儂我農（Nong Nong）

CFA104 班專題 — 第二組「我家門前有塊地」

> 連結台灣小農與你的餐桌，吃得新鮮，吃得安心。

## 技術棧

- **後端**：Java 17 + Spring Boot 3.4 + Spring Security + JPA + JWT + MySQL（Maven）
- **前端**：純 HTML5 + CSS3 + JavaScript（無框架、無建置工具）
- **資料庫**：MySQL 8.0（utf8mb4）
- **靜態服務**：前端檔案放在 `backend/src/main/resources/static/`，由 Spring Boot 同源服務，啟動後端即可

## 專案結構

```
farm-platform/
├── backend/                                      ← Spring Boot 後端 + 靜態前端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/farm/platform/
│       │   ├── FarmPlatformApplication.java
│       │   ├── config/        ← Security / CORS 設定
│       │   ├── controller/    ← REST API（auth / 商品 / 購物車 / 訂單 / 團購 / 體驗 / 部落格 / 後台 / 管理員）
│       │   ├── dto/           ← 請求 / 回應物件
│       │   ├── entity/        ← JPA 實體
│       │   ├── repository/    ← 資料存取層
│       │   ├── security/      ← JWT 工具 + Filter
│       │   └── service/       ← 業務邏輯
│       └── resources/
│           ├── application.yml
│           └── static/        ← 純 HTML/CSS/JS 前端
│               ├── *.html     ← 28 個頁面（消費者 + 小農工作台 + 管理員後台）
│               ├── css/styles.css
│               └── js/        ← auth.js / header.js / farmer-shell.js / admin-shell.js + 各頁腳本
├── scripts/                                      ← 資料庫 migration
└── frontend_vue_backup/                          ← 舊 Vue 版完整備份（已停用）
```

## 開發階段

| Phase | 內容 | 狀態 |
|---|---|---|
| 1 | 會員系統 / JWT 登入註冊 + 多身份切換 | ✅ |
| 2 | 商品管理（小農 CRUD）+ 公開商品瀏覽 + 分類 | ✅ |
| 3 | 購物車 + 結帳 + 訂單管理 + 付款 / 出貨 / 收貨 | ✅ |
| 4 | 團購（發起 / 加入 / 審核 / 成團結算）+ 體驗活動 CRUD + 預約 | ✅ |
| 5 | GB_ORDER 重構（host 一張整單 + N 筆 participation）+ 託管金流欄位 | ✅ |
| 6 | 後台管理（會員啟用 / 商品下架）+ 部落格 + 檢舉處理 | ✅ |
| — | 純靜態前端（取代 Vue）+ Spring Boot 同源服務 | ✅ |

## 啟動步驟

### 1. 建立資料庫

打開 MySQL Workbench 或 CLI，執行：

```sql
CREATE DATABASE farm_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 設定資料庫密碼

編輯 `backend/src/main/resources/application.yml`，把 `password` 改成你 MySQL root 的密碼，或設環境變數 `DB_PASSWORD`。

### 3. 啟動後端（前端會一起啟）

用 IntelliJ 打開 `backend/` 跑 `FarmPlatformApplication.main()`；或在命令列：

```powershell
cd backend
mvn spring-boot:run
```

### 4. 打開瀏覽器

```
http://localhost:8080
```

直接看到消費者首頁。所有頁面都在這個 port 下：

- `http://localhost:8080/` — 首頁
- `http://localhost:8080/products.html` — 逛商品
- `http://localhost:8080/login.html` — 登入
- `http://localhost:8080/farmer.html` — 小農工作台（FARMER 登入後）
- `http://localhost:8080/admin.html` — 管理員後台（ADMIN 登入後）

## 預設測試帳號（DataSeeder 自動建立）

| 角色 | Email | 密碼 |
|---|---|---|
| 管理員 | `admin@nong.com` | `admin1234` |
| 範例小農 | `demo@farmer.com` | `123456` |

> 新註冊的小農預設停用，需由管理員在 `/admin-users.html` 啟用後才能登入。

## 頁面一覽（28 頁）

**消費者**：`index` `products` `product-detail` `group-buys` `group-buy-detail` `farm-trips` `farm-trip-detail` `blogs` `blog-detail` `cart` `checkout` `orders` `order-detail` `profile` `login` `register` `blog-form` `my-blogs` `my-group-buy-requests` `my-group-buys` `my-group-buy-orders` `my-farm-trip-bookings`

**小農工作台**：`farmer`(dashboard) `farmer-products` `farmer-product-form` `farmer-orders` `farmer-group-buy-requests` `farmer-group-buys` `farmer-group-buy-orders` `farmer-farm-trips` `farmer-farm-trip-form` `farmer-farm-trip-bookings`

**管理員後台**：`admin`(dashboard) `admin-users` `admin-products` `admin-blog-reports`

## 會話策略：離開後台即登出

為了避免角色混淆，前端對不同角色用不同的儲存：

| 角色 | 儲存位置 | 行為 |
|---|---|---|
| CONSUMER | `localStorage` | 跨 tab 保留、關瀏覽器仍記得 |
| FARMER / ADMIN | `sessionStorage` | **關 tab 即清除**（離開後台 = 自動登出）|

小農工作台 / 管理員後台是完全獨立的頁面，沒有「回首頁」連結，避免使用者誤踩；要回首頁就只能登出後再點。

## API 速查

| 公開 | 方法 | 路徑 |
|---|---|---|
| 登入 | POST | `/api/auth/login` |
| 註冊 | POST | `/api/auth/register` |
| 商品列表 | GET | `/api/public/products?categoryId=&keyword=&size=` |
| 商品分類 | GET | `/api/public/categories` |
| 商品詳情 | GET | `/api/public/products/{id}` |
| 團購列表 | GET | `/api/group-buys` |
| 團購詳情 | GET | `/api/group-buys/{id}` |
| 體驗活動 | GET | `/api/farm-trips` |
| 部落格 | GET | `/api/blogs` |

| 需登入 | 方法 | 路徑 |
|---|---|---|
| 角色切換 | POST | `/api/account/switch-role` |
| 購物車 | GET / POST / PUT / DELETE | `/api/cart[/items[/{productId}]]` |
| 結帳 | POST | `/api/orders/checkout` |
| 我的訂單 | GET | `/api/orders` |
| 加入團購 | POST | `/api/group-buys/{id}/join` |
| 預約體驗 | POST | `/api/farm-trips/{id}/bookings` |
| 按讚 / 留言 | POST | `/api/blogs/{id}/like`、`/comments` |

| 小農（ROLE_FARMER） | 方法 | 路徑 |
|---|---|---|
| 我的商品 CRUD | GET / POST / PUT / DELETE | `/api/farmer/products[/{id}]` |
| 接到的訂單 | GET | `/api/orders/farmer` |
| 出貨 | POST | `/api/orders/{id}/ship` |
| 團購審核 | POST | `/api/farmer/group-buy-requests/{id}/review` |
| 體驗活動 CRUD | GET / POST / PUT | `/api/farmer/farm-trips[/{id}]` |
| 預約管理 | GET | `/api/farmer/farm-trip-bookings` |

| 管理員（ROLE_ADMIN） | 方法 | 路徑 |
|---|---|---|
| 平台統計 | GET | `/api/admin/stats` |
| 會員管理 | GET / POST | `/api/admin/users[/{id}/enable\|disable]` |
| 商品管理 | GET / POST | `/api/admin/products[/{id}/take-down\|restore]` |
| 檢舉處理 | GET / POST | `/api/admin/blog-reports`、`/api/admin/blog-comment-reports` |

## 注意事項

- `application.yml` JDBC URL 必須用 `characterEncoding=UTF-8`，MySQL Connector 9.x 不認 `utf8mb4`
- `spring.jpa.open-in-view=false`，所以 Service 層要 lazy load 關聯時記得加 `@Transactional`
- 修改 `src/main/resources/static/` 下的檔案後，需 `mvn resources:resources` 或重啟 Spring Boot 才會生效
- 用 PowerShell `Invoke-RestMethod` 測 API 時，中文要包 UTF-8 bytes，否則會傳 `????`
- 舊 Vue 版完整備份在 `frontend_vue_backup/`，要還原就改回 `frontend/` 並 `npm install && npm run dev`
