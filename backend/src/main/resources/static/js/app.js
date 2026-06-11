/*
  index.html — 首頁專屬腳本
  - 幻燈片
  - 主題分類與精選商品
  - 體驗活動、最新消息、部落格
  共用 header 邏輯在 header.js
*/

const HOME_PRODUCT_IMAGES = [
  "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1587049352851-8d4e89133924?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1607083206968-13611e3d76db?auto=format&fit=crop&w=900&q=80"
];

const HOME_TRIP_IMAGES = [
  "https://storage.googleapis.com/cka101-15/farmtest/hero-farm-main.jpg",
  "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1523741543316-beb7fc7023d8?auto=format&fit=crop&w=900&q=80"
];

const HOME_NEWS_IMAGE = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";

const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };

let activeSlide = 0;
let slideTimer;
let homeCategories = [{ id: null, name: "全部商品" }];
let activeHomeCategoryId = null;

const escapeHtml = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v || 0).toLocaleString("zh-TW")}`;

async function initHome() {
  initSlider();
  await Promise.all([
    renderHomeCategories(),
    renderFeaturedProducts(),
    renderFeaturedTrips(),
    renderLatestNews(),
    renderRecentBlogs()
  ]);
}

async function renderHomeCategories() {
  const host = document.getElementById("homeCategoryPills");
  if (!host) return;
  const defaults = [{ id: null, name: "全部商品" }];
  try {
    const cats = await NongAuth.request("/api/public/categories");
    homeCategories = Array.isArray(cats) && cats.length
      ? [{ id: null, name: "全部商品" }, ...cats.slice(0, 5)]
      : defaults;
  } catch (_e) {
    homeCategories = defaults;
  }
  renderCategoryPills();
  bindCategoryPills();
}

function renderCategoryPills() {
  const host = document.getElementById("homeCategoryPills");
  if (!host) return;
  host.innerHTML = homeCategories.map((cat) => `
    <button type="button" class="category-pill ${sameCategory(cat.id, activeHomeCategoryId) ? "is-active" : ""}"
            data-home-category-id="${cat.id == null ? "" : cat.id}">
      ${escapeHtml(cat.name)}
    </button>
  `).join("");
}

function bindCategoryPills() {
  const host = document.getElementById("homeCategoryPills");
  if (!host) return;
  host.addEventListener("click", async (e) => {
    const pill = e.target.closest("[data-home-category-id]");
    if (!pill) return;
    const value = pill.dataset.homeCategoryId;
    activeHomeCategoryId = value === "" ? null : Number(value);
    renderCategoryPills();
    await renderFeaturedProducts();
  });
}

async function renderFeaturedProducts() {
  const grid = document.getElementById("productGrid");
  if (!grid) return;
  grid.innerHTML = loadingCard("精選商品載入中");
  try {
    const params = new URLSearchParams({ size: "3" });
    if (activeHomeCategoryId != null) params.set("categoryId", String(activeHomeCategoryId));
    const page = await NongAuth.request("/api/public/products?" + params.toString());
    const list = normalizePage(page);
    if (!list.length) {
      grid.innerHTML = emptyCard("目前沒有上架商品。");
      return;
    }
    grid.innerHTML = list.map((p, i) => {
      const img = cleanImage(p.imageUrl) || HOME_PRODUCT_IMAGES[i % HOME_PRODUCT_IMAGES.length];
      return `
        <article class="showcase-card">
          <a href="product-detail.html?id=${p.id}" class="showcase-image" style="background-image:url('${escapeHtml(img)}')" aria-label="${escapeHtml(p.name)} 商品圖片"></a>
          <div class="showcase-body">
            <div class="showcase-meta"><span>${escapeHtml(p.farmerName || "小農")}</span><span>${escapeHtml(p.categoryName || "當季商品")}</span></div>
            <h3><a href="product-detail.html?id=${p.id}">${escapeHtml(p.name)}</a></h3>
            <p>${escapeHtml(truncate(p.description || "產地直送，依季節與採收狀況供應。", 70))}</p>
            <div class="showcase-actions">
              <strong class="price">${currency(p.price)}</strong>
              <a class="icon-link" href="product-detail.html?id=${p.id}" aria-label="查看商品">查看</a>
            </div>
          </div>
        </article>
      `;
    }).join("");
  } catch (e) {
    grid.innerHTML = emptyCard(`商品載入失敗：${e.message}`);
  }
}

async function renderFeaturedTrips() {
  const grid = document.getElementById("tripPreview");
  if (!grid) return;
  grid.innerHTML = loadingCard("體驗活動載入中");
  try {
    const page = await NongAuth.request("/api/farm-trips?size=3");
    const list = normalizePage(page);
    if (!list.length) {
      grid.innerHTML = emptyCard("目前沒有開放預約的體驗活動。");
      return;
    }
    grid.innerHTML = list.map((t, i) => {
      const img = cleanImage(t.imageUrl) || HOME_TRIP_IMAGES[i % HOME_TRIP_IMAGES.length];
      const unit = PRICING_LABELS[t.pricingMode] || "每人";
      return `
        <article class="showcase-card trip-card">
          <a href="farm-trip-detail.html?id=${t.id}" class="showcase-image" style="background-image:url('${escapeHtml(img)}')" aria-label="${escapeHtml(t.title)} 活動圖片"></a>
          <div class="showcase-body">
            <div class="showcase-meta"><span>${escapeHtml(t.farmerName || "小農")}</span><span>${escapeHtml(TRIP_TYPE_LABELS[t.tripType] || "食農體驗")}</span></div>
            <h3><a href="farm-trip-detail.html?id=${t.id}">${escapeHtml(t.title)}</a></h3>
            <p>${escapeHtml(truncate(t.intro || "走進產地，認識作物、風土與農人的日常。", 72))}</p>
            <div class="showcase-actions">
              <strong class="price">${currency(t.price)} / ${unit}</strong>
              <a class="icon-link" href="farm-trip-detail.html?id=${t.id}">預約</a>
            </div>
          </div>
        </article>
      `;
    }).join("");
  } catch (e) {
    grid.innerHTML = emptyCard(`活動載入失敗：${e.message}`);
  }
}

async function renderLatestNews() {
  const grid = document.getElementById("newsPreview");
  if (!grid) return;
  grid.innerHTML = loadingCard("最新消息載入中");
  try {
    const page = await NongAuth.request("/api/news?size=3");
    const list = normalizePage(page);
    if (!list.length) {
      grid.innerHTML = emptyCard("目前沒有最新消息。");
      return;
    }
    grid.innerHTML = list.map((n) => {
      const img = cleanImage(n.coverImageUrl) || HOME_NEWS_IMAGE;
      return `
        <article class="news-card">
          <a class="news-thumb" href="news-detail.html?id=${n.id}" style="background-image:url('${escapeHtml(img)}')" aria-label="${escapeHtml(n.title)} 封面"></a>
          <div class="news-body">
            <time>${formatDate(n.publishedAt || n.createdAt)}</time>
            <h3><a href="news-detail.html?id=${n.id}">${escapeHtml(n.title)}</a></h3>
            <p>${escapeHtml(truncate(n.summary || n.content || "平台公告與重要更新。", 90))}</p>
            <a class="text-link" href="news-detail.html?id=${n.id}">閱讀全文 ›</a>
          </div>
        </article>
      `;
    }).join("");
  } catch (e) {
    grid.innerHTML = emptyCard(`消息載入失敗：${e.message}`);
  }
}

async function renderRecentBlogs() {
  const grid = document.getElementById("blogPreview");
  if (!grid) return;
  grid.innerHTML = loadingCard("文章載入中");
  try {
    const page = await NongAuth.request("/api/blogs?size=3");
    const list = normalizePage(page);
    if (!list.length) {
      grid.innerHTML = emptyCard("尚無文章。");
      return;
    }
    grid.innerHTML = list.map((b) => `
      <article class="journal-card">
        <span>${escapeHtml(b.blogTypeName || "產地日記")}</span>
        <h3><a href="blog-detail.html?id=${b.id}">${escapeHtml(b.title)}</a></h3>
        <p>${escapeHtml(truncate(b.content || "", 92))}</p>
        <a class="text-link" href="blog-detail.html?id=${b.id}">繼續閱讀 ›</a>
      </article>
    `).join("");
  } catch (_e) {
    grid.innerHTML = emptyCard("文章載入失敗。");
  }
}

function initSlider() {
  const dots = document.querySelectorAll("[data-slide-to]");
  if (!dots.length) return;
  dots.forEach((dot) => {
    dot.addEventListener("click", () => {
      showSlide(Number(dot.dataset.slideTo));
      restartSlider();
    });
  });
  restartSlider();
}

function restartSlider() {
  window.clearInterval(slideTimer);
  slideTimer = window.setInterval(() => {
    const slides = document.querySelectorAll("[data-slide]");
    if (!slides.length) return;
    showSlide((activeSlide + 1) % slides.length);
  }, 5200);
}

function showSlide(index) {
  activeSlide = index;
  document.querySelectorAll("[data-slide]").forEach((slide, i) => slide.classList.toggle("is-active", i === index));
  document.querySelectorAll("[data-slide-to]").forEach((dot, i) => dot.classList.toggle("is-active", i === index));
}

function normalizePage(page) {
  if (Array.isArray(page)) return page;
  return page?.content || [];
}

function cleanImage(url) {
  const value = String(url || "").trim();
  return value || null;
}

function truncate(text, length) {
  const clean = String(text || "").replace(/\s+/g, " ").trim();
  return clean.length > length ? clean.slice(0, length) + "..." : clean;
}

function formatDate(iso) {
  if (!iso) return "近期公告";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "近期公告";
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
}

function sameCategory(a, b) {
  return (a == null && b == null) || Number(a) === Number(b);
}

function loadingCard(text) {
  return `<div class="inline-state">${escapeHtml(text)}...</div>`;
}

function emptyCard(text) {
  return `<div class="inline-state">${escapeHtml(text)}</div>`;
}

if (document.body.dataset.activeNav === "home") initHome();
