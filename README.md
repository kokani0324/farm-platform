# 你儂我農（Nong Nong）

CFA104 班專題 — 第二組「我家門前有塊地」

> 連結台灣小農與你的餐桌，吃得新鮮，吃得安心。

## 技術棧

- **後端**：Java 17 + Spring Boot 3.4 + Spring Security + JPA + MySQL（Maven）
- **前端**：Vue 3 + Vite + Tailwind CSS + Naive UI + Pinia + Vue Router

## 專案結構

```
farm-platform/
├── backend/              ← Spring Boot 後端
│   ├── pom.xml
│   ├── sql/
│   │   └── 01_init.sql   ← 建立資料庫
│   └── src/main/java/com/farm/platform/
│       ├── FarmPlatformApplication.java
│       ├── config/       ← Security/CORS
│       ├── controller/   ← REST API
│       ├── dto/          ← 請求/回應物件
│       ├── entity/       ← JPA 實體（資料表）
│       ├── repository/   ← 資料存取層
│       ├── security/     ← JWT 工具與 Filter
│       └── service/      ← 業務邏輯
└── frontend/             ← Vue 前端
    ├── package.json
    ├── tailwind.config.js
    ├── vite.config.js
    └── src/
        ├── api/         ← Axios + JWT 攔截器
        ├── stores/      ← Pinia auth store
        ├── router/      ← Vue Router + 守衛
        ├── layouts/     ← DefaultLayout / BlankLayout
        ├── components/  ← AppHeader / AppFooter
        └── views/       ← HomeView / LoginView / RegisterView
```

## 開發階段

| Phase | 內容 | 狀態 |
|---|---|---|
| 1 | 會員系統 / JWT 登入註冊 | 進行中 |
| 2 | 商品管理 + 商品瀏覽 | 待做 |
| 3 | 購物車 + 訂單 + 結帳 | 待做 |
| 4 | 團購 + 體驗活動 | 待做 |
| 5 | 後台管理 + 報表 + 部落格 | 待做 |

## 啟動步驟

### 1. 建立資料庫
打開 MySQL Workbench（或 cmd），執行：
```sql
CREATE DATABASE farm_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 設定資料庫密碼
編輯 `backend/src/main/resources/application.yml`，將 `password` 改成你的 MySQL root 密碼。

### 3. 啟動後端
用 IntelliJ 開啟 `backend/` 資料夾，等 Maven 載入依賴後，執行 `FarmPlatformApplication.main()`。

伺服器會在 **http://localhost:8080** 啟動。

## API 測試

### 註冊（消費者）
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456",
  "name": "測試用戶",
  "phone": "0912345678",
  "role": "CONSUMER"
}
```

### 註冊（小農，預設停用需審核）
```json
{
  "email": "farmer@example.com",
  "password": "123456",
  "name": "王小農",
  "phone": "0987654321",
  "role": "FARMER"
}
```

### 登入
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456"
}
```

回應會包含 `token`，未來 API 呼叫請加上 header：
```
Authorization: Bearer <你的 token>
```

---

## 啟動前端

```powershell
cd frontend
npm install      # 第一次需要
npm run dev
```

前端會在 **http://localhost:5173** 啟動，開啟瀏覽器即可看到首頁、登入、註冊。

⚠️ 前端啟動前，請先啟動後端（http://localhost:8080），否則 API 呼叫會失敗。
