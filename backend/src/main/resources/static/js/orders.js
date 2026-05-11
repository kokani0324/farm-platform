/*
  orders.html — 我的訂單列表
  - GET /api/orders
  - 顯示訂單編號、小農、狀態、金額、建立時間
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("ordersView");

const STATUS_LABEL = {
  PENDING_PAYMENT: { text: "待付款", cls: "warning" },
  PAID:            { text: "已付款・等待出貨", cls: "" },
  SHIPPED:         { text: "已出貨", cls: "" },
  COMPLETED:       { text: "已完成", cls: "success" },
  CANCELLED:       { text: "已取消", cls: "muted" }
};

(async function init() {
  const user = NongAuth.getConsumerUser();
  if (!user) { location.href = "login.html?reason=auth"; return; }
  try {
    const page = await NongAuth.request("/api/orders?size=30");
    const list = page.content || [];
    if (!list.length) {
      view.innerHTML = `<div class="empty-state">還沒有任何訂單。<br><br><a class="primary-button" href="products.html">去逛商品</a></div>`;
      return;
    }
    view.innerHTML = `
      <table class="cart-table">
        <thead><tr><th>訂單編號</th><th>小農</th><th>狀態</th><th>金額</th><th>建立時間</th><th></th></tr></thead>
        <tbody>${list.map(renderRow).join("")}</tbody>
      </table>
    `;
  } catch (e) {
    view.innerHTML = `<div class="empty-state">訂單載入失敗：${esc(e.message)}</div>`;
  }
})();

function renderRow(o) {
  const s = STATUS_LABEL[o.status] || { text: o.status, cls: "muted" };
  return `
    <tr>
      <td><a href="order-detail.html?id=${o.id}" style="color:var(--leaf);font-weight:600">${esc(o.orderNo)}</a></td>
      <td>${esc(o.farmerName || "")}</td>
      <td><span class="status-tag ${s.cls}">${esc(s.text)}</span></td>
      <td>${currency(o.totalAmount)}</td>
      <td>${fmt(o.createdAt)}</td>
      <td><a class="user-btn ghost" href="order-detail.html?id=${o.id}">查看</a></td>
    </tr>
  `;
}

function fmt(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
