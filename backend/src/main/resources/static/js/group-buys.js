/*
  group-buys.html — 團購列表頁
  - GET /api/group-buys?page=&size=
  - 顯示商品圖、進度條、剩餘時間、團購價 vs 原價
*/

const esc = NongHeader.escapeHtml;
const currency = (v) => `NT$ ${Number(v).toLocaleString("zh-TW")}`;

(async function init() {
  const grid = document.getElementById("groupGrid");
  grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">載入中…</p>`;
  try {
    const page = await NongAuth.request("/api/group-buys?size=24");
    const list = page.content || [];
    if (!list.length) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">目前沒有進行中的團購。小農或團主可以從商品頁發起。</div>`;
      return;
    }
    grid.innerHTML = list.map(renderCard).join("");
  } catch (e) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">團購載入失敗：${esc(e.message)}</div>`;
  }
})();

function renderCard(g) {
  const img = g.productImageUrl && g.productImageUrl.trim()
    ? g.productImageUrl
    : "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";
  const deadline = g.deadlineDate ? fmt(g.deadlineDate) : "—";
  return `
    <article class="product-card">
      <a href="group-buy-detail.html?id=${g.id}" class="product-image" style="background-image:url('${esc(img)}')" role="img" aria-label="${esc(g.productName)} 團購圖片"></a>
      <div class="product-body">
        <div class="product-meta"><span>團主 ${esc(g.hostName || "")}</span><span>截止 ${esc(deadline)}</span></div>
        <h3><a href="group-buy-detail.html?id=${g.id}">${esc(g.productName)}</a></h3>
        <p>原價 ${currency(g.productPrice)}　團購價 <strong class="price">${currency(g.groupPrice)}</strong>　省 ${currency(g.saving)}</p>
        <div class="progress-bar" aria-label="團購進度"><span style="width:${g.percent || 0}%"></span></div>
        <div class="product-meta" style="margin-top:10px">
          <span>${g.currentQuantity || 0} / ${g.targetQuantity || 0} 份（剩 ${g.remainingQuantity || 0}）</span>
          <a class="secondary-button" href="group-buy-detail.html?id=${g.id}">${g.joined ? "我已參加" : "查看 / 加入"}</a>
        </div>
      </div>
    </article>
  `;
}

function fmt(iso) {
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${m}/${day}`;
}
