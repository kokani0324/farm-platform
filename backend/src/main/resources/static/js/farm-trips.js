/*
  farm-trips.html — 體驗活動列表
  - GET /api/farm-trips?size=&tripType=
  - 分類用 tripType (FARM_EXPERIENCE / FIELD_VISIT)，spec 拿掉了單獨的分類表
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;

const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS  = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };

let activeTripType = null;

(async function init() { await loadTrips(); })();

async function loadTrips() {
  const grid = document.getElementById("tripGrid");
  grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">載入中…</p>`;
  const params = new URLSearchParams({ size: "24" });
  if (activeTripType) params.set("tripType", activeTripType);
  try {
    const page = await NongAuth.request("/api/farm-trips?" + params.toString());
    const list = page.content || [];
    renderPills();
    if (!list.length) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">目前沒有開放預約的體驗活動。</div>`;
      return;
    }
    grid.innerHTML = list.map(renderCard).join("");
  } catch (e) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">體驗活動載入失敗：${esc(e.message)}</div>`;
  }
}

function renderPills() {
  const c = document.getElementById("categoryPills");
  if (!c) return;
  const pills = [
    { type: null, name: "全部" },
    { type: "FARM_EXPERIENCE", name: "農場體驗" },
    { type: "FIELD_VISIT", name: "產地參訪" },
  ];
  c.innerHTML = pills.map(p => `
    <button type="button" class="category-pill ${same(p.type, activeTripType) ? "is-active" : ""}"
            data-trip-type="${p.type == null ? "" : p.type}">${esc(p.name)}</button>
  `).join("");
  c.onclick = async (e) => {
    const pill = e.target.closest("[data-trip-type]"); if (!pill) return;
    const v = pill.dataset.tripType;
    activeTripType = v === "" ? null : v;
    await loadTrips();
  };
}

function renderCard(t) {
  const img = (t.imageUrl && t.imageUrl.trim())
    || "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";
  const unit = PRICING_LABELS[t.pricingMode] || "每人";
  return `
    <article class="product-card">
      <a href="farm-trip-detail.html?id=${t.id}" class="product-image" style="background-image:url('${esc(img)}')" role="img" aria-label="${esc(t.title)} 活動圖片"></a>
      <div class="product-body">
        <div class="product-meta">
          <span>${esc(t.farmerName || "小農")}</span>
          <span>${esc(TRIP_TYPE_LABELS[t.tripType] || "")}</span>
        </div>
        <h3><a href="farm-trip-detail.html?id=${t.id}">${esc(t.title)}</a></h3>
        <p>${esc(t.intro || "")}</p>
        <div class="product-meta">
          <span>${esc(t.location || "")}</span>
          <strong class="price">${currency(t.price)} / ${unit}</strong>
        </div>
        <div class="product-meta" style="margin-top:8px">
          <span>⭐ ${(t.averageRating ?? 0).toFixed(1)} (${t.ratingCount ?? 0})</span>
          <a class="secondary-button" href="farm-trip-detail.html?id=${t.id}">看場次與預約</a>
        </div>
      </div>
    </article>
  `;
}

function same(a, b) { return (a == null && b == null) || a === b; }
