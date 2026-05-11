/*
  product-detail.html — 商品詳情
  - GET /api/public/products/:id
  - POST /api/cart/items
  - POST /api/group-buys/requests （若 groupBuyEnabled）
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("detailView");

let product = null;

(async function init() {
  const id = new URLSearchParams(location.search).get("id");
  if (!id) { view.innerHTML = empty("缺少商品 id"); return; }
  try {
    product = await NongAuth.request(`/api/public/products/${id}`);
    render(product);
    bindEvents();
  } catch (e) {
    view.innerHTML = empty("商品載入失敗：" + e.message);
  }
})();

function render(p) {
  const img = (p.imageUrl && p.imageUrl.trim())
    || "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=1400&q=80";
  view.innerHTML = `
    <section class="detail-layout">
      <div class="detail-cover" style="background-image:url('${esc(img)}')" role="img" aria-label="${esc(p.name)} 商品圖片"></div>
      <div class="detail-info">
        <span class="eyebrow">product</span>
        <h1>${esc(p.name)}</h1>
        <div class="meta-row">
          <span>小農：${esc(p.farmerName || "")}</span>
          <span>分類：${esc(p.categoryName || "")}</span>
          ${p.origin ? `<span>產地：${esc(p.origin)}</span>` : ""}
          ${p.shippingMethod ? `<span>出貨：${esc(p.shippingMethod)}</span>` : ""}
        </div>
        <div class="price-big">${currency(p.price)} <small style="font-size:14px;color:var(--muted)">/ ${esc(p.unit || "件")}</small></div>
        <div class="desc">${esc(p.description || "")}</div>
        <div class="action-row">
          <label class="field" style="margin:0">
            <span>數量</span>
            <input type="number" id="qty" class="qty-input" value="1" min="1" max="${p.stock || 99}">
          </label>
          <button class="primary-button" id="addBtn">加入購物車</button>
          ${p.groupBuyEnabled ? `<button class="secondary-button" id="hostBtn">我要發起團購</button>` : ""}
          <span style="color:var(--muted);font-size:14px">剩餘庫存 ${p.stock ?? "—"}</span>
        </div>
      </div>
    </section>

    <section class="detail-body">
      <div class="section-heading compact">
        <span class="eyebrow">about the farmer</span>
        <h2>關於小農</h2>
      </div>
      <p style="color:var(--muted);line-height:1.9">由 ${esc(p.farmerName || "小農")} 上架，平台保留產銷履歷與檢驗欄位。如有問題可透過後台聯絡客服。</p>
    </section>
  `;
}

function bindEvents() {
  document.getElementById("addBtn").addEventListener("click", addToCart);
  const hostBtn = document.getElementById("hostBtn");
  if (hostBtn) hostBtn.addEventListener("click", openGroupBuyModal);

  // modal close
  document.addEventListener("click", (e) => {
    if (e.target.matches("[data-close-modal]") || e.target.classList.contains("modal-backdrop")) {
      closeModal();
    }
  });
  document.getElementById("groupBuyForm").addEventListener("submit", submitGroupBuy);
}

async function addToCart() {
  const user = NongAuth.getConsumerUser();
  if (!user) { toast("請先登入"); setTimeout(() => location.href = "login.html", 700); return; }
  if ((user.activeRole || user.role) !== "CONSUMER") { toast("非消費者身份無法購買", true); return; }
  const qty = Math.max(1, Number(document.getElementById("qty").value) || 1);
  const btn = document.getElementById("addBtn");
  btn.disabled = true; btn.textContent = "加入中…";
  try {
    await NongAuth.request("/api/cart/items", {
      method: "POST",
      body: JSON.stringify({ productId: product.id, quantity: qty })
    });
    toast("已加入購物車");
    await NongHeader.refreshCartBadge();
  } catch (e) { toast(e.message || "加入失敗", true); }
  finally { btn.disabled = false; btn.textContent = "加入購物車"; }
}

function openGroupBuyModal() {
  const user = NongAuth.getConsumerUser();
  if (!user) { toast("請先登入"); setTimeout(() => location.href = "login.html", 700); return; }
  // 預設值：開團 = 現在 + 5min；截止 = 開團 + 3 天
  const now = new Date(Date.now() + 5 * 60 * 1000);
  const dl  = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
  document.getElementById("gbOpen").value = toLocalInput(now);
  document.getElementById("gbDeadline").value = toLocalInput(dl);
  document.getElementById("gbPrice").value = Number(product.price);
  document.getElementById("gbTarget").value = 5;
  document.getElementById("gbMessage").value = "";
  document.getElementById("groupBuyModal").classList.add("is-open");
}

function closeModal() { document.getElementById("groupBuyModal").classList.remove("is-open"); }

async function submitGroupBuy(e) {
  e.preventDefault();
  const payload = {
    productId: product.id,
    targetQuantity: Number(document.getElementById("gbTarget").value),
    groupPrice: Number(document.getElementById("gbPrice").value),
    openDate: document.getElementById("gbOpen").value,
    deadlineDate: document.getElementById("gbDeadline").value,
    message: document.getElementById("gbMessage").value || null
  };
  try {
    await NongAuth.request("/api/group-buys/requests", {
      method: "POST", body: JSON.stringify(payload)
    });
    closeModal();
    toast("已送出申請，等小農審核");
  } catch (err) { toast(err.message || "送出失敗", true); }
}

function toLocalInput(d) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function empty(msg) { return `<div class="empty-state">${esc(msg)}</div>`; }
function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
