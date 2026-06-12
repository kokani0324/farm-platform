# Docker + VM 部署筆記

這份部署方式適合把整個專案放到一台 Ubuntu VM 上，並用 Docker Compose 一次啟動：

- Spring Boot 後端與靜態前端
- MySQL 8
- Redis

## 建議 VM 規格

展示、作業、低流量測試：

| 項目 | 建議 |
| --- | --- |
| OS | Ubuntu 22.04 LTS 或 Ubuntu 24.04 LTS |
| CPU | 2 vCPU |
| 記憶體 | 4 GB RAM |
| 硬碟 | 30 GB SSD |
| 對外開放 Port | 22, 8080 |

更省錢但比較緊繃：

| 項目 | 最低可跑 |
| --- | --- |
| CPU | 1 vCPU |
| 記憶體 | 2 GB RAM |
| 硬碟 | 20 GB SSD |

如果同一台 VM 要長期放資料、上傳圖片、或多人同時測試，建議從 2 vCPU / 4 GB RAM / 30 GB SSD 起跳。

## VM 上安裝 Docker

登入 VM 後執行：

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

確認 Docker 可用：

```bash
sudo docker run hello-world
docker compose version
```

如果想不用每次打 `sudo`：

```bash
sudo usermod -aG docker $USER
```

執行後登出 VM 再登入一次。

## 放上專案

用 GitHub clone：

```bash
git clone <你的 repo URL>
cd farm-platform
```

或是把專案壓縮上傳到 VM，再進入專案根目錄。

## 設定正式密碼

複製範例環境檔：

```bash
cp .env.example .env
```

編輯 `.env`：

```bash
nano .env
```

至少建議改：

```env
DB_PASSWORD=你的MySQL密碼
JWT_SECRET=你的正式JWT_SECRET
```

`.env` 已經被 `.gitignore` 忽略，不應該提交進 Git。

## 啟動

在專案根目錄執行：

```bash
docker compose up -d --build
```

看狀態：

```bash
docker compose ps
```

看後端 log：

```bash
docker compose logs -f backend
```

打開網站：

```text
http://VM_IP:8080/
```

## 更新程式

如果是用 Git：

```bash
git pull
docker compose up -d --build
```

## 停止服務

只停止服務，保留 MySQL 資料：

```bash
docker compose down
```

停止並刪除 MySQL 資料，重新跑乾淨資料庫：

```bash
docker compose down -v
docker compose up -d --build
```

平常不要用 `down -v`，除非你確定要清空資料庫。
