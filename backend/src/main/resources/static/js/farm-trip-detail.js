/*
  farm-trip-detail.html — 體驗活動詳情 + 預約
  - GET  /api/farm-trips/:id
  - POST /api/farm-trips/:id/bookings
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("tripView");

let trip = null;

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
  const canBook = t.status === "ACTIVE" && (t.remaining ?? 0) > 0;
  view.innerHTML = `
    <section class="detail-layout">
      <div class="detail-cover" style="background-image:url('${esc(img)}')" role="img"></div>
      <div class="detail-info">
        <span class="eyebrow">farm experience</span>
        <h1>${esc(t.title)}</h1>
        <div class="meta-row">
          <span>小農：${esc(t.farmerName || "")}</span>
          <span>類別：${esc(t.categoryName || "")}</span>
          <span>地點：${esc(t.location || "")}</span>
        </div>
        <div class="meta-row">
          <span>活動日：${fmtDate(t.tripStart)} ${fmtTime(t.tripStart)}–${fmtTime(t.tripEnd)}</span>
          <span>報名期間：${fmtDate(t.bookStart)} ~ ${fmtDate(t.bookEnd)}</span>
        </div>
        <div class="price-big">${currency(t.price)} <small style="font-size:14px;color:var(--muted)">/ 人</small></div>
        <p style="line-height:1.9;color:#3a463b;white-space:pre-wrap">${esc(t.intro || "")}</p>
        <div class="action-row" style="margin-top:14px">
          <span class="status-tag ${canBook ? "success" : "muted"}">${canBook ? "可預約" : "未開放"}</span>
          <span>剩餘名額 ${t.remaining ?? "—"} / 總額 ${t.capacity ?? "—"}</span>
          <button class="primary-button" id="bookBtn" ${canBook ? "" : "disabled"}>立即預約</button>
        </div>
      </div>
    </section>
  `;
}

function bindEvents() {
  const bookBtn = document.getElementById("bookBtn");
  if (bookBtn) bookBtn.addEventListener("click", openModal);

  document.addEventListener("click", (e) => {
    if (e.target.matches("[data-close-modal]") || e.target.classList.contains("modal-backdrop")) closeModal();
  });
  document.getElementById("bookForm").addEventListener("submit", submitBooking);
}

function openModal() {
  const user = NongAuth.getConsumerUser();
  if (!user) { toast("請先登入"); setTimeout(() => location.href = "login.html", 700); return; }
  document.getElementById("contactName").value = user.name || "";
  document.getElementById("contactPhone").value = "";
  document.getElementById("numPeople").value = 1;
  document.getElementById("note").value = "";
  document.getElementById("bookModal").classList.add("is-open");
}
function closeModal() { document.getElementById("bookModal").classList.remove("is-open"); }

async function submitBooking(e) {
  e.preventDefault();
  const payload = {
    numPeople: Number(document.getElementById("numPeople").value) || 1,
    contactName: document.getElementById("contactName").value.trim(),
    contactPhone: document.getElementById("contactPhone").value.trim(),
    note: document.getElementById("note").value.trim() || null
  };
  try {
    await NongAuth.request(`/api/farm-trips/${trip.id}/bookings`, {
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
