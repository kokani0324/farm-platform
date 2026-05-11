/*
  index.html — 首頁專屬腳本
  - 幻燈片
  - 本週精選商品（取 /api/public/products 前 6 筆）
  - 邊吃邊讀（取 /api/blogs 前 3 篇）
  共用 header 邏輯在 header.js
*/

const FALLBACK_IMAGES = [
  "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1587049352851-8d4e89133924?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1607083206968-13611e3d76db?auto=format&fit=crop&w=900&q=80"
];

let activeSlide = 0;
let slideTimer;

const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const escapeHtml = NongHeader.escapeHtml;

async function initHome() {
  initSlider();
  await Promise.all([renderFeaturedProducts(), renderRecentBlogs()]);
}

async function renderFeaturedProducts() {
  const grid = document.getElementById("productGrid");
  if (!grid) return;
  try {
    const page = await NongAuth.request("/api/public/products?size=6");
    const list = Array.isArray(page) ? page : (page.content || []);
    if (!list.length) { grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">目前沒有上架商品。</p>`; return; }
    grid.innerHTML = list.map((p, i) => {
      const img = (p.imageUrl && p.imageUrl.trim()) || FALLBACK_IMAGES[i % FALLBACK_IMAGES.length];
      return `
        <article class="product-card">
          <a href="product-detail.html?id=${p.id}" class="product-image" style="background-image:url('${escapeHtml(img)}')" role="img" aria-label="${escapeHtml(p.name)} 商品圖片"></a>
          <div class="product-body">
            <div class="product-meta"><span>${escapeHtml(p.farmerName || "小農")}</span><span>${escapeHtml(p.categoryName || "")}</span></div>
            <h3><a href="product-detail.html?id=${p.id}">${escapeHtml(p.name)}</a></h3>
            <p>${escapeHtml(p.description || "")}</p>
            <div class="product-meta">
              <strong class="price">${currency(p.price)}</strong>
              <a class="secondary-button" href="product-detail.html?id=${p.id}">查看商品</a>
            </div>
          </div>
        </article>
      `;
    }).join("");
  } catch (e) {
    grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">商品載入失敗：${escapeHtml(e.message)}</p>`;
  }
}

async function renderRecentBlogs() {
  const grid = document.getElementById("blogPreview");
  if (!grid) return;
  try {
    const page = await NongAuth.request("/api/blogs?size=3");
    const list = Array.isArray(page) ? page : (page.content || []);
    if (!list.length) { grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">尚無文章。</p>`; return; }
    grid.innerHTML = list.map((b) => `
      <article>
        <span>${escapeHtml(b.blogTypeName || "")}</span>
        <h3><a href="blog-detail.html?id=${b.id}">${escapeHtml(b.title)}</a></h3>
        <p>${escapeHtml(truncate(b.content || "", 80))}</p>
      </article>
    `).join("");
  } catch (e) {
    grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">文章載入失敗。</p>`;
  }
}

function truncate(s, n) { s = String(s).replace(/\s+/g, " "); return s.length > n ? s.slice(0, n) + "…" : s; }

function initSlider() {
  const dots = document.querySelectorAll("[data-slide-to]");
  if (!dots.length) return;
  dots.forEach((dot) => dot.addEventListener("click", () => { showSlide(Number(dot.dataset.slideTo)); restartSlider(); }));
  restartSlider();
}
function restartSlider() {
  window.clearInterval(slideTimer);
  slideTimer = window.setInterval(() => showSlide((activeSlide + 1) % document.querySelectorAll("[data-slide]").length), 4800);
}
function showSlide(index) {
  activeSlide = index;
  document.querySelectorAll("[data-slide]").forEach((s, i) => s.classList.toggle("is-active", i === index));
  document.querySelectorAll("[data-slide-to]").forEach((d, i) => d.classList.toggle("is-active", i === index));
}

if (document.body.dataset.activeNav === "home") initHome();
