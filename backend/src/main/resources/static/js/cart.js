/*
  cart.html — 購物車
  - GET    /api/cart           取購物車
  - PUT    /api/cart/items/:id 改數量
  - DELETE /api/cart/items/:id 移除
  - DELETE /api/cart           清空
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;

const cartView = document.getElementById("cartView");

(async function init() {
  const user = NongAuth.getConsumerUser();
  if (!user) {
    cartView.innerHTML = `
      <div class="empty-state">尚未登入。<br><br><a class="primary-button" href="login.html?reason=auth">前往登入</a></div>
    `;
    return;
  }
  if ((user.activeRole || user.role) !== "CONSUMER") {
    cartView.innerHTML = `<div class="empty-state">目前身份不是消費者，無法購物。</div>`;
    return;
  }
  await refresh();
})();

async function refresh() {
  cartView.innerHTML = `<div class="empty-state">載入中…</div>`;
  let cart;
  try {
    cart = await NongAuth.request("/api/cart");
  } catch (e) {
    cartView.innerHTML = `<div class="empty-state">購物車載入失敗：${esc(e.message)}</div>`;
    return;
  }
  render(cart);
  await NongHeader.refreshCartBadge();
}

function render(cart) {
  const items = cart.items || [];
  if (!items.length) {
    cartView.innerHTML = `
      <div class="empty-state">購物車是空的。<br><br><a class="primary-button" href="products.html">去逛商品</a></div>
    `;
    return;
  }
  const rows = items.map((it) => {
    const img = (it.imageUrl && it.imageUrl.trim())
      || "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=200";
    const unavailable = it.available === false;
    return `
      <tr data-row-id="${it.productId}">
        <td><div class="cart-thumb" style="background-image:url('${esc(img)}')"></div></td>
        <td>
          <div style="font-weight:600">${esc(it.name)}</div>
          <div style="color:var(--muted);font-size:12px">${esc(it.farmerName || "")} ｜ ${esc(it.unit || "")}</div>
          ${unavailable ? `<div style="color:var(--clay);font-size:12px;margin-top:4px">已下架或庫存不足</div>` : ""}
        </td>
        <td>${currency(it.price)}</td>
        <td>
          <input type="number" min="1" max="${it.stock || 99}" value="${it.quantity}" class="qty-input" data-qty-for="${it.productId}">
        </td>
        <td><strong>${currency(it.subtotal)}</strong></td>
        <td><button class="user-btn ghost" data-remove="${it.productId}" style="color:var(--clay)">移除</button></td>
      </tr>
    `;
  }).join("");

  cartView.innerHTML = `
    <table class="cart-table">
      <thead><tr><th></th><th>商品</th><th>單價</th><th>數量</th><th>小計</th><th></th></tr></thead>
      <tbody>${rows}</tbody>
    </table>

    <div class="cart-summary">
      <span>共 ${cart.totalQuantity} 件</span>
      <strong>合計：${currency(cart.totalAmount)}</strong>
      <button class="user-btn ghost" id="clearBtn" style="color:var(--clay)">清空</button>
      <a class="primary-button" href="checkout.html">前往結帳</a>
    </div>
  `;
  bindEvents();
}

function bindEvents() {
  document.querySelectorAll("[data-remove]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const productId = btn.dataset.remove;
      btn.disabled = true;
      try {
        await NongAuth.request(`/api/cart/items/${productId}`, { method: "DELETE" });
        toast("已移除");
        await refresh();
      } catch (e) { toast(e.message || "移除失敗", true); btn.disabled = false; }
    });
  });

  document.querySelectorAll("[data-qty-for]").forEach((input) => {
    let timer;
    input.addEventListener("change", async () => {
      const productId = input.dataset.qtyFor;
      const qty = Math.max(1, Number(input.value) || 1);
      input.value = qty;
      clearTimeout(timer);
      timer = setTimeout(async () => {
        try {
          await NongAuth.request(`/api/cart/items/${productId}`, {
            method: "PUT",
            body: JSON.stringify({ quantity: qty })
          });
          await refresh();
        } catch (e) { toast(e.message || "更新失敗", true); }
      }, 200);
    });
  });

  const clearBtn = document.getElementById("clearBtn");
  if (clearBtn) {
    clearBtn.addEventListener("click", async () => {
      if (!confirm("確定要清空購物車嗎？")) return;
      try {
        await NongAuth.request("/api/cart", { method: "DELETE" });
        toast("已清空");
        await refresh();
      } catch (e) { toast(e.message || "清空失敗", true); }
    });
  }
}

function toast(msg, isError) {
  const t = document.getElementById("toast");
  if (!t) return;
  t.textContent = msg;
  t.classList.toggle("is-error", !!isError);
  t.classList.add("is-visible");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
