/*
  farm-trip-detail.html — 體驗活動詳情 + 場次選擇 + 預約
  - GET  /api/farm-trips/:id
  - POST /api/farm-trips/sessions/:sessionId/orders
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("tripView");

const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS  = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };

let trip = null;
let activeSession = null;

(async function init() {
  const id = new URLSearchParams(location.search).get("id");
  if (!id) { view.innerHTML = empty("缺少活動 id"); return; }
  try {
    trip = await NongAuth.request(`/api/farm-trips/${id}`);
    render(trip);
    bindEvents();
  } catch (e) { view.innerHTML = empty("活動載入失敗：" + e.message); }
})();

function render(t) {
  const img = (t.imageUrl && t.imageUrl.trim())
    || "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1400&q=80";
  const unit = PRICING_LABELS[t.pricingMode] || "每人";
  const isPerWeight = t.pricingMode === "PER_WEIGHT";

  view.innerHTML = `
    <section class="detail-layout">
      <div class="detail-cover" style="background-image:url('${esc(img)}')" role="img"></div>
      <div class="detail-info">
        <span class="eyebrow">farm experience</span>
        <h1>${esc(t.title)}</h1>
        <div class="meta-row">
          <span>小農：${esc(t.farmerName || "")}</span>
          <span>類別：${esc(TRIP_TYPE_LABELS[t.tripType] || "")}</span>
          <span>地點：${esc(t.location || "")}</span>
        </div>
        <div class="price-big">${currency(t.price)} <small style="font-size:14px;color:var(--muted)">/ ${unit}</small></div>
        <div class="meta-row"><span>⭐ ${(t.averageRating ?? 0).toFixed(1)} (${t.ratingCount ?? 0} 則評論)</span></div>
        ${isPerWeight ? `<div class="info-banner">本活動依「實際採收公斤數」計價，下單僅佔位、活動當天現場過磅結算。</div>` : ""}
        <p style="line-height:1.9;color:#3a463b;white-space:pre-wrap">${esc(t.intro || "")}</p>
      </div>
    </section>

    <section class="detail-body">
      <div class="section-heading compact"><span class="eyebrow">sessions</span><h2>可預約場次</h2></div>
      <div id="sessionList"></div>
    </section>
  `;

  renderSessions(t.sessions || []);
}

const WD_LABELS = ["週日","週一","週二","週三","週四","週五","週六"];
const MO_LABELS = ["一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"];

function renderSessions(sessions) {
  const wrap = document.getElementById("sessionList");
  if (!sessions.length) { wrap.innerHTML = `<div class="empty-state">尚無開放場次</div>`; return; }

  const now = Date.now();
  const isPerWeight = trip.pricingMode === "PER_WEIGHT";

  // 過濾掉 CANCELLED + 按月份分組
  const visible = sessions.filter(s => s.status !== "CANCELLED");
  if (!visible.length) { wrap.innerHTML = `<div class="empty-state">所有場次都已取消或結束</div>`; return; }

  const groups = new Map();
  visible.forEach(s => {
    const d = new Date(s.tripStart);
    const key = `${d.getFullYear()}-${d.getMonth()}`;
    if (!groups.has(key)) groups.set(key, { y: d.getFullYear(), m: d.getMonth(), items: [] });
    groups.get(key).items.push(s);
  });

  let html = "";
  for (const { y, m, items } of groups.values()) {
    html += `
      <div class="month-block">
        <div class="month-head">
          <span class="m">${y} 年 ${MO_LABELS[m]}</span>
          <span class="c">${items.length} 個場次</span>
        </div>
        <div class="session-grid">
          ${items.map(s => renderSessionCard(s, now, isPerWeight)).join("")}
        </div>
      </div>
    `;
  }
  wrap.innerHTML = html;
}

function renderSessionCard(s, now, isPerWeight) {
  const start = new Date(s.tripStart);
  const end   = new Date(s.tripEnd);
  const bookStart = new Date(s.bookStart).getTime();
  const bookEnd = new Date(s.bookEnd).getTime();

  const tooEarly = bookStart > now;
  const tooLate = bookEnd < now;
  const noSlot = !isPerWeight && (s.remaining ?? 0) <= 0;
  const completed = s.status === "COMPLETED";

  const bookable = s.status === "ACTIVE" && !tooEarly && !tooLate && !noSlot;

  let badgeHtml = "";
  if (completed) badgeHtml = `<span class="badge-tag badge-cancelled">已結束</span>`;
  else if (tooLate) badgeHtml = `<span class="badge-tag badge-cancelled">報名結束</span>`;
  else if (noSlot) badgeHtml = `<span class="badge-tag badge-low">已額滿</span>`;
  else if (!isPerWeight && (s.remaining ?? 99) <= 3) badgeHtml = `<span class="badge-tag badge-low">剩 ${s.remaining} 名</span>`;
  else if (bookEnd - now < 3 * 86400000 && !tooLate) badgeHtml = `<span class="badge-tag badge-soon">即將截止</span>`;

  let btnLabel = "預約此場";
  if (completed) btnLabel = "已結束";
  else if (s.status === "CANCELLED") btnLabel = "已取消";
  else if (tooEarly) btnLabel = `${fmtDate(s.bookStart)} 起開放`;
  else if (tooLate) btnLabel = "報名已截止";
  else if (noSlot) btnLabel = "已額滿";

  const capLine = isPerWeight
    ? `無人數上限`
    : `已預約 <strong>${s.attendance ?? 0}</strong> / ${(s.attendance ?? 0) + (s.remaining ?? 0)} 人`;

  return `
    <div class="session-card ${bookable ? "" : "is-disabled"}">
      ${badgeHtml}
      <div class="session-day">
        <span class="dom">${start.getDate()}</span>
        <span class="mo">${MO_LABELS[start.getMonth()]}</span>
        <span class="wd">${WD_LABELS[start.getDay()]}</span>
      </div>
      <div class="session-time">🕘 ${fmtTime(s.tripStart)} – ${fmtTime(s.tripEnd)}</div>
      <div class="session-meta">
        ${capLine}<br>
        報名截止：<strong>${fmtDate(s.bookEnd)} ${fmtTime(s.bookEnd)}</strong>
      </div>
      <div class="session-price">${currency(s.sessionPrice)} <small>/ ${PRICING_LABELS[trip.pricingMode]}</small></div>
      <button class="book-btn" data-book-session="${s.id}" data-price="${s.sessionPrice}" ${bookable ? "" : "disabled"}>${btnLabel}</button>
    </div>
  `;
}

function bindEvents() {
  document.addEventListener("click", (e) => {
    const btn = e.target.closest("[data-book-session]");
    if (btn) {
      const sid = Number(btn.dataset.bookSession);
      openModal(sid, Number(btn.dataset.price));
      return;
    }
    if (e.target.matches("[data-close-modal]") || e.target.classList.contains("modal-backdrop")) closeModal();
  });
  document.getElementById("bookForm").addEventListener("submit", submitBooking);
}

function openModal(sessionId, sessionPrice) {
  const user = NongAuth.getConsumerUser();
  if (!user) { toast("請先登入"); setTimeout(() => location.href = "login.html", 700); return; }
  if (user.type !== "MEMBER") { toast("僅會員可預約", true); return; }

  activeSession = trip.sessions.find(s => s.id === sessionId);
  const isPerWeight = trip.pricingMode === "PER_WEIGHT";

  const summary = document.getElementById("sessionSummary");
  if (summary) {
    summary.innerHTML = `
      <strong>${esc(trip.title)}</strong><br>
      ${fmtDate(activeSession.tripStart)} ${fmtTime(activeSession.tripStart)}–${fmtTime(activeSession.tripEnd)}<br>
      <span style="color:var(--muted)">場次價 ${currency(sessionPrice)} / ${PRICING_LABELS[trip.pricingMode]}</span>
      ${isPerWeight ? `<div class="info-banner" style="margin-top:8px">下單僅佔位，現場過磅結算總金額。</div>` : ""}
    `;
  }

  document.getElementById("contactName").value = user.name || "";
  document.getElementById("contactPhone").value = "";
  document.getElementById("numPeople").value = 1;
  document.getElementById("note").value = "";
  document.getElementById("bookModal").classList.add("is-open");
}
function closeModal() { document.getElementById("bookModal").classList.remove("is-open"); activeSession = null; }

async function submitBooking(e) {
  e.preventDefault();
  if (!activeSession) return;
  const payload = {
    numPeople: Number(document.getElementById("numPeople").value) || 1,
    contactName: document.getElementById("contactName").value.trim(),
    contactPhone: document.getElementById("contactPhone").value.trim(),
    note: document.getElementById("note").value.trim() || null
  };
  try {
    await NongAuth.request(`/api/farm-trips/sessions/${activeSession.id}/orders`, {
      method: "POST", body: JSON.stringify(payload)
    });
    closeModal();
    toast("預約成功");
    setTimeout(() => location.href = "my-farm-trip-bookings.html", 700);
  } catch (err) { toast(err.message || "預約失敗", true); }
}

function fmtDate(iso) { if (!iso) return "—"; const d = new Date(iso); if (isNaN(d.getTime())) return iso; const p = (n) => String(n).padStart(2,"0"); return `${d.getFullYear()}/${p(d.getMonth()+1)}/${p(d.getDate())}`; }
function fmtTime(iso) { if (!iso) return ""; const d = new Date(iso); if (isNaN(d.getTime())) return ""; const p = (n) => String(n).padStart(2,"0"); return `${p(d.getHours())}:${p(d.getMinutes())}`; }
function empty(msg) { return `<div class="empty-state">${esc(msg)}</div>`; }
function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
