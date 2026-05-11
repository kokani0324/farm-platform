/*
  checkout.html — 結帳
  - GET /api/cart 取摘要
  - POST /api/orders/checkout 送出
  - 依 OrderService 行為，會依小農自動拆單為多張 Order
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;
const view = document.getElementById("checkoutView");

(async function init() {
  const user = NongAuth.getConsumerUser();
  if (!user) { location.href = "login.html?reason=auth"; return; }
  if ((user.activeRole || user.role) !== "CONSUMER") {
    view.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">目前身份不是消費者，無法結帳。</div>`; return;
  }
  let cart;
  try { cart = await NongAuth.request("/api/cart"); }
  catch (e) { view.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">購物車載入失敗：${esc(e.message)}</div>`; return; }
  if (!cart.items || !cart.items.length) {
    view.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">購物車是空的。<br><br><a class="primary-button" href="products.html">去逛商品</a></div>`;
    return;
  }
  render(cart, user);
})();

function render(cart, user) {
  view.innerHTML = `
    <section>
      <div class="section-heading compact"><span class="eyebrow">order summary</span><h2>訂單摘要</h2></div>
      <table class="cart-table">
        <thead><tr><th>商品</th><th>數量</th><th>小計</th></tr></thead>
        <tbody>
          ${cart.items.map((it) => `
            <tr>
              <td>
                <div style="font-weight:600">${esc(it.name)}</div>
                <div style="color:var(--muted);font-size:12px">${esc(it.farmerName || "")} ｜ ${currency(it.price)} × ${it.quantity}</div>
              </td>
              <td>${it.quantity}</td>
              <td>${currency(it.subtotal)}</td>
            </tr>
          `).join("")}
        </tbody>
      </table>
      <div class="cart-summary"><span>共 ${cart.totalQuantity} 件</span><strong>合計：${currency(cart.totalAmount)}</strong></div>
    </section>

    <section>
      <div class="section-heading compact"><span class="eyebrow">recipient</span><h2>收件與付款</h2></div>
      <form id="checkoutForm" class="auth-card" style="box-shadow:none;width:100%;padding:22px">
        <div class="form-grid">
          <label class="field"><span>收件人姓名</span><input type="text" id="recipientName" maxlength="50" required value="${esc(user.name || "")}"></label>
          <label class="field"><span>收件人電話</span><input type="text" id="recipientPhone" maxlength="20" required></label>
        </div>
        <label class="field"><span>收件地址</span><input type="text" id="shippingAddress" maxlength="200" required></label>
        <label class="field"><span>備註（選填）</span><textarea id="note" maxlength="500" style="min-height:60px"></textarea></label>
        <label class="field"><span>付款方式</span>
          <select id="paymentMethod" required>
            <option value="CREDIT_CARD_SIM">模擬信用卡（按下即視為已付款）</option>
            <option value="BANK_TRANSFER_SIM">模擬 ATM 轉帳</option>
            <option value="CASH_ON_DELIVERY">貨到付款</option>
          </select>
        </label>
        <button class="primary-button" type="submit" style="width:100%;margin-top:8px">確認下單</button>
      </form>
    </section>
  `;
  document.getElementById("checkoutForm").addEventListener("submit", submit);
}

async function submit(e) {
  e.preventDefault();
  const payload = {
    recipientName: document.getElementById("recipientName").value.trim(),
    recipientPhone: document.getElementById("recipientPhone").value.trim(),
    shippingAddress: document.getElementById("shippingAddress").value.trim(),
    note: document.getElementById("note").value.trim() || null,
    paymentMethod: document.getElementById("paymentMethod").value
  };
  const btn = e.target.querySelector("button[type=submit]");
  btn.disabled = true; btn.textContent = "送出中…";
  try {
    const orders = await NongAuth.request("/api/orders/checkout", {
      method: "POST", body: JSON.stringify(payload)
    });
    toast(`已建立 ${orders.length} 張訂單`);
    await NongHeader.refreshCartBadge();
    setTimeout(() => location.href = "orders.html", 700);
  } catch (err) {
    toast(err.message || "結帳失敗", true);
    btn.disabled = false; btn.textContent = "確認下單";
  }
}

function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
