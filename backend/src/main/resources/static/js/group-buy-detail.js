/*
  group-buy-detail.html — 團購詳情 + 加入 / 退出
  - GET  /api/group-buys/:id
  - POST /api/group-buys/:id/join (帶收件 + 付款資訊)
  - POST /api/group-buys/:id/withdraw
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("gbView");

let gb = null;

(async function init() {
  const id = new URLSearchParams(location.search).get("id");
  if (!id) { view.innerHTML = empty("缺少團購 id"); return; }
  await reload(id);
})();

async function reload(id) {
  try {
    gb = await NongAuth.request(`/api/group-buys/${id}`);
    render(gb);
    bindEvents();
  } catch (e) { view.innerHTML = empty("團購載入失敗：" + e.message); }
}

function render(g) {
  const img = (g.productImageUrl && g.productImageUrl.trim())
    || "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=1400&q=80";
  const canJoin = g.status === "OPEN" || g.status === "ONGOING" || g.status === "ACTIVE";
  view.innerHTML = `
    <section class="detail-layout">
      <div class="detail-cover" style="background-image:url('${esc(img)}')" role="img"></div>
      <div class="detail-info">
        <span class="eyebrow">group buy</span>
        <h1>${esc(g.productName)}</h1>
        <div class="meta-row">
          <span>團主：${esc(g.hostName || "")}</span>
          <span>小農：${esc(g.farmerName || "")}</span>
          <span>狀態：${esc(g.status)}</span>
        </div>
        <div class="meta-row">
          <span>開團：${fmt(g.openDate)}</span>
          <span>截止：${fmt(g.deadlineDate)}</span>
        </div>
        <div class="price-big">${currency(g.groupPrice)} <small style="font-size:14px;color:var(--muted)">/ ${esc(g.productUnit || "")}　原價 ${currency(g.productPrice)}，省 ${currency(g.saving)}</small></div>
        <div class="progress-bar" aria-label="團購進度"><span style="width:${g.percent || 0}%"></span></div>
        <p style="margin-top:10px;color:var(--muted)">${g.currentQuantity || 0} / ${g.targetQuantity || 0} 份（剩 ${g.remainingQuantity || 0}）</p>

        <div class="action-row" style="margin-top:14px">
          ${g.joined
            ? `<span class="status-tag success">我已加入 ${g.myQuantity || 0} 份</span><button class="user-btn ghost" id="withdrawBtn" style="color:var(--clay)">退出團購</button>`
            : `<button class="primary-button" id="joinBtn" ${canJoin ? "" : "disabled"}>${canJoin ? "我要加入" : "目前不可加入"}</button>`
          }
        </div>
      </div>
    </section>
  `;
}

function bindEvents() {
  const joinBtn = document.getElementById("joinBtn");
  const withdrawBtn = document.getElementById("withdrawBtn");
  if (joinBtn) joinBtn.addEventListener("click", openJoinModal);
  if (withdrawBtn) withdrawBtn.addEventListener("click", withdraw);

  document.addEventListener("click", (e) => {
    if (e.target.matches("[data-close-modal]") || e.target.classList.contains("modal-backdrop")) closeModal();
  });
  document.getElementById("joinForm").addEventListener("submit", submitJoin);
}

function openJoinModal() {
  const user = NongAuth.getConsumerUser();
  if (!user) { toast("請先登入"); setTimeout(() => location.href = "login.html", 700); return; }
  document.getElementById("recipientName").value = user.name || "";
  document.getElementById("recipientPhone").value = "";
  document.getElementById("quantity").value = 1;
  document.getElementById("note").value = "";
  document.getElementById("joinModal").classList.add("is-open");
}
function closeModal() { document.getElementById("joinModal").classList.remove("is-open"); }

async function submitJoin(e) {
  e.preventDefault();
  const payload = {
    quantity: Number(document.getElementById("quantity").value) || 1,
    paymentMethod: document.getElementById("paymentMethod").value,
    recipientName: document.getElementById("recipientName").value.trim(),
    recipientPhone: document.getElementById("recipientPhone").value.trim(),
    shippingZipcode: document.getElementById("shippingZipcode").value.trim(),
    shippingCity: document.getElementById("shippingCity").value.trim(),
    shippingDist: document.getElementById("shippingDist").value.trim(),
    shippingDetail: document.getElementById("shippingDetail").value.trim(),
    note: document.getElementById("note").value.trim() || null
  };
  try {
    await NongAuth.request(`/api/group-buys/${gb.id}/join`, {
      method: "POST", body: JSON.stringify(payload)
    });
    closeModal();
    toast("已加入團購");
    await reload(gb.id);
  } catch (err) { toast(err.message || "加入失敗", true); }
}

async function withdraw() {
  if (!confirm("確定要退出這個團購嗎？")) return;
  try {
    await NongAuth.request(`/api/group-buys/${gb.id}/withdraw`, { method: "POST" });
    toast("已退出");
    await reload(gb.id);
  } catch (e) { toast(e.message || "退出失敗", true); }
}

function fmt(iso) {
  if (!iso) return "—"; const d = new Date(iso); if (isNaN(d.getTime())) return iso;
  const p = (n) => String(n).padStart(2, "0");
  return `${p(d.getMonth()+1)}/${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}
function empty(msg) { return `<div class="empty-state">${esc(msg)}</div>`; }
function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
