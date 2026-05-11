/*
  farm-trips.html — 體驗活動列表
  - GET /api/farm-trips?size=&categoryId=
  - 從回傳結果反推出分類（後端沒提供 categories endpoint 也可運作）
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;

let activeCategoryId = null;
let allTrips = [];

(async function init() {
  await loadTrips();
})();

async function loadTrips() {
  const grid = document.getElementById("tripGrid");
  grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">載入中…</p>`;
  const params = new URLSearchParams({ size: "24" });
  if (activeCategoryId != null) params.set("categoryId", String(activeCategoryId));
  try {
    const page = await NongAuth.request("/api/farm-trips?" + params.toString());
    const list = page.content || [];
    allTrips = list;
    if (!list.length) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">目前沒有開放預約的體驗活動。</div>`;
      renderPills([]);
      return;
    }
    renderPills(list);
    grid.innerHTML = list.map(renderCard).join("");
    bindPills();
  } catch (e) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">體驗活動載入失敗：${esc(e.message)}</div>`;
  }
}

function renderPills(list) {
  const c = document.getElementById("categoryPills");
  if (!c) return;
  // 從目前 list 反推分類；保留「全部」
  const map = new Map();
  for (const t of list) {
    if (t.categoryId != null && !map.has(t.categoryId)) map.set(t.categoryId, t.categoryName || "未分類");
  }
  const pills = [{ id: null, name: "全部" }, ...Array.from(map, ([id, name]) => ({ id, name }))];
  c.innerHTML = pills.map((p) => `
    <button type="button" class="category-pill ${same(p.id, activeCategoryId) ? "is-active" : ""}" data-category-id="${p.id == null ? "" : p.id}">${esc(p.name)}</button>
  `).join("");
}

function bindPills() {
  document.addEventListener("click", async (e) => {
    const pill = e.target.closest("[data-category-id]");
    if (!pill) return;
    const v = pill.dataset.categoryId;
    activeCategoryId = v === "" ? null : Number(v);
    await loadTrips();
  }, { once: true });
}

function renderCard(t) {
  const img = (t.imageUrl && t.imageUrl.trim())
    || "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";
  return `
    <article class="product-card">
      <a href="farm-trip-detail.html?id=${t.id}" class="product-image" style="background-image:url('${esc(img)}')" role="img" aria-label="${esc(t.title)} 活動圖片"></a>
      <div class="product-body">
        <div class="product-meta"><span>${esc(t.farmerName || "小農")}</span><span>${esc(t.categoryName || "")}</span></div>
        <h3><a href="farm-trip-detail.html?id=${t.id}">${esc(t.title)}</a></h3>
        <p>${esc(t.intro || "")}</p>
        <div class="product-meta">
          <span>${fmt(t.tripStart)} ｜ ${esc(t.location || "")}</span>
          <strong class="price">${currency(t.price)}</strong>
        </div>
        <div class="product-meta" style="margin-top:8px">
          <span>名額 ${t.remaining ?? "—"} / ${t.capacity ?? "—"}</span>
          <a class="secondary-button" href="farm-trip-detail.html?id=${t.id}">查看與預約</a>
        </div>
      </div>
    </article>
  `;
}

function fmt(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}/${m}/${day}`;
}

function same(a, b) { return (a == null && b == null) || a === b; }
