/*
  order-detail.html — 訂單詳情（消費者視角）
  - GET /api/orders/:id
  - 依狀態提供：付款 / 取消 / 確認收貨 按鈕
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("orderView");

const STATUS_LABEL = {
  PENDING_PAYMENT: { text: "待付款", cls: "warning" },
  PAID:            { text: "已付款・等待出貨", cls: "" },
  SHIPPED:         { text: "已出貨", cls: "" },
  COMPLETED:       { text: "已完成", cls: "success" },
  CANCELLED:       { text: "已取消", cls: "muted" }
};
const PAY_LABEL = {
  CREDIT_CARD_SIM: "模擬信用卡",
  BANK_TRANSFER_SIM: "模擬 ATM 轉帳",
  CASH_ON_DELIVERY: "貨到付款"
};

let order = null;

(async function init() {
  const id = new URLSearchParams(location.search).get("id");
  if (!id) { view.innerHTML = empty("缺少訂單 id"); return; }
  await reload(id);
})();

async function reload(id) {
  try {
    order = await NongAuth.request(`/api/orders/${id}`);
    render(order);
  } catch (e) {
    view.innerHTML = empty("訂單載入失敗：" + e.message);
  }
}

function render(o) {
  const s = STATUS_LABEL[o.status] || { text: o.status, cls: "muted" };
  const actions = buildActions(o);
  view.innerHTML = `
    <section class="page-header" style="text-align:left;padding:24px 0 16px">
      <span class="eyebrow">order #${esc(o.orderNo)}</span>
      <h1 style="margin:6px 0">訂單詳情 <span class="status-tag ${s.cls}" style="vertical-align:middle">${esc(s.text)}</span></h1>
      <p style="margin:0">${fmt(o.createdAt)} 建立 ｜ 小農 ${esc(o.farmerName || "")} ｜ 付款 ${esc(PAY_LABEL[o.paymentMethod] || o.paymentMethod)}</p>
    </section>

    <table class="cart-table">
      <thead><tr><th>商品</th><th>單價</th><th>數量</th><th>小計</th></tr></thead>
      <tbody>
        ${(o.items || []).map((it) => `
          <tr>
            <td>
              <div style="font-weight:600">${esc(it.productName)}</div>
              <div style="color:var(--muted);font-size:12px">${esc(it.unit || "")}</div>
            </td>
            <td>${currency(it.unitPrice)}</td>
            <td>${it.quantity}</td>
            <td><strong>${currency(it.subtotal)}</strong></td>
          </tr>
        `).join("")}
      </tbody>
    </table>

    <div class="cart-summary">
      <strong>合計：${currency(o.totalAmount)}</strong>
    </div>

    <div class="detail-layout" style="margin-top:24px">
      <section>
        <div class="section-heading compact"><span class="eyebrow">recipient</span><h2>收件資訊</h2></div>
        <p style="line-height:1.9;color:#3a463b">
          ${esc(o.recipientName || "")}<br>
          ${esc(o.recipientPhone || "")}<br>
          ${esc(o.shippingAddress || "")}
          ${o.note ? `<br>備註：${esc(o.note)}` : ""}
        </p>
      </section>
      <section>
        <div class="section-heading compact"><span class="eyebrow">actions</span><h2>可執行的動作</h2></div>
        <div class="action-row">${actions}</div>
        ${o.paidAt ? `<p style="margin-top:14px;color:var(--muted);font-size:13px">付款時間：${fmt(o.paidAt)}</p>` : ""}
      </section>
    </div>
  `;
  bindActions();
}

function buildActions(o) {
  switch (o.status) {
    case "PENDING_PAYMENT":
      return `
        <button class="primary-button"   data-action="pay">立即付款（模擬）</button>
        <button class="user-btn ghost"   data-action="cancel" style="color:var(--clay)">取消訂單</button>
      `;
    case "PAID":
    case "SHIPPED":
      return `<button class="primary-button" data-action="confirm">確認收貨</button>`;
    case "COMPLETED":
      return `<span style="color:var(--muted)">訂單已完成</span>`;
    case "CANCELLED":
      return `<span style="color:var(--muted)">訂單已取消</span>`;
    default:
      return "";
  }
}

function bindActions() {
  document.querySelectorAll("[data-action]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const action = btn.dataset.action;
      const map = { pay: "pay", cancel: "cancel", confirm: "confirm" };
      const path = map[action];
      if (!path) return;
      btn.disabled = true;
      try {
        await NongAuth.request(`/api/orders/${order.id}/${path}`, { method: "POST" });
        toast(action === "pay" ? "付款完成" : action === "cancel" ? "已取消" : "已確認收貨");
        await reload(order.id);
      } catch (e) {
        toast(e.message || "失敗", true);
        btn.disabled = false;
      }
    });
  });
}

function fmt(iso) {
  if (!iso) return "—";
  const d = new Date(iso); if (isNaN(d.getTime())) return iso;
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
function empty(msg) { return `<div class="empty-state">${esc(msg)}</div>`; }
function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
