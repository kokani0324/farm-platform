/*
  products.html — 商品列表頁
  - 分類 pill：GET /api/public/categories
  - 商品：GET /api/public/products?categoryId=&keyword=
  - 加入購物車：POST /api/cart/items (需登入消費者)
*/

const FALLBACK_IMG = [
  "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1587049352851-8d4e89133924?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1607083206968-13611e3d76db?auto=format&fit=crop&w=900&q=80"
];

const $ = (s) => document.querySelector(s);
const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;

let categories = [{ id: null, name: "全部" }];
let activeCategoryId = null;
let currentKeyword = "";

(async function init() {
  loadFiltersFromQuery();
  await loadCategories();
  renderPills();
  await loadProducts();
  bindEvents();
})();

function loadFiltersFromQuery() {
  const q = new URLSearchParams(location.search);
  if (q.has("categoryId")) activeCategoryId = Number(q.get("categoryId")) || null;
  if (q.has("keyword")) {
    currentKeyword = q.get("keyword");
    $("#keyword").value = currentKeyword;
  }
}

async function loadCategories() {
  try {
    const cats = await NongAuth.request("/api/public/categories");
    categories = [{ id: null, name: "全部" }, ...cats];
  } catch (e) { console.warn("/api/public/categories 失敗：", e.message); }
}

function renderPills() {
  const c = $("#categoryPills");
  c.innerHTML = categories.map((cat) => `
    <button type="button" class="category-pill ${same(cat.id, activeCategoryId) ? "is-active" : ""}" data-category-id="${cat.id == null ? "" : cat.id}">
      ${esc(cat.name)}
    </button>
  `).join("");
}

async function loadProducts() {
  const grid = $("#productGrid");
  grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">載入中…</p>`;
  const params = new URLSearchParams({ size: "24" });
  if (activeCategoryId != null) params.set("categoryId", String(activeCategoryId));
  if (currentKeyword) params.set("keyword", currentKeyword);
  try {
    const page = await NongAuth.request("/api/public/products?" + params.toString());
    const list = page.content || [];
    if (!list.length) { grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">沒有符合的商品。試試其他分類或關鍵字。</div>`; return; }
    grid.innerHTML = list.map((p, i) => {
      const img = (p.imageUrl && p.imageUrl.trim()) || FALLBACK_IMG[i % FALLBACK_IMG.length];
      return `
        <article class="product-card">
          <a href="product-detail.html?id=${p.id}" class="product-image" style="background-image:url('${esc(img)}')" role="img" aria-label="${esc(p.name)} 商品圖片"></a>
          <div class="product-body">
            <div class="product-meta"><span>${esc(p.farmerName || "小農")}</span><span>${esc(p.categoryName || "")}</span></div>
            <h3><a href="product-detail.html?id=${p.id}">${esc(p.name)}</a></h3>
            <p>${esc(p.description || "")}</p>
            <div class="product-meta">
              <strong class="price">${currency(p.price)}</strong>
              <button class="secondary-button" type="button" data-add-to-cart="${p.id}">加入購物車</button>
            </div>
          </div>
        </article>
      `;
    }).join("");
  } catch (e) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">商品載入失敗：${esc(e.message)}</div>`;
  }
}

function bindEvents() {
  document.addEventListener("click", async (e) => {
    const pill = e.target.closest("[data-category-id]");
    if (pill) {
      const v = pill.dataset.categoryId;
      activeCategoryId = v === "" ? null : Number(v);
      syncQuery();
      renderPills();
      await loadProducts();
      return;
    }
    const addBtn = e.target.closest("[data-add-to-cart]");
    if (addBtn) {
      await addToCart(Number(addBtn.dataset.addToCart), addBtn);
      return;
    }
  });

  $("#searchForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    currentKeyword = $("#keyword").value.trim();
    syncQuery();
    await loadProducts();
  });
}

function syncQuery() {
  const params = new URLSearchParams();
  if (activeCategoryId != null) params.set("categoryId", String(activeCategoryId));
  if (currentKeyword) params.set("keyword", currentKeyword);
  const next = params.toString();
  history.replaceState(null, "", next ? `?${next}` : location.pathname);
}

async function addToCart(productId, btn) {
  const user = NongAuth.getConsumerUser();
  if (!user) {
    toast("請先登入再加入購物車", true);
    setTimeout(() => location.href = "login.html", 800);
    return;
  }
  if ((user.activeRole || user.role) !== "CONSUMER") {
    toast("小農 / 管理員身份無法購買", true);
    return;
  }
  btn.disabled = true;
  const original = btn.textContent;
  btn.textContent = "加入中…";
  try {
    await NongAuth.request("/api/cart/items", {
      method: "POST",
      body: JSON.stringify({ productId, quantity: 1 })
    });
    toast("已加入購物車");
    await NongHeader.refreshCartBadge();
  } catch (e) {
    toast(e.message || "加入失敗", true);
  } finally {
    btn.disabled = false;
    btn.textContent = original;
  }
}

function toast(msg, isError) {
  const t = $("#toast");
  if (!t) return;
  t.textContent = msg;
  t.classList.toggle("is-error", !!isError);
  t.classList.add("is-visible");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}

function same(a, b) { return (a == null && b == null) || a === b; }
