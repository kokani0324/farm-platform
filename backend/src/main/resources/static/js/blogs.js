/*
  blogs.html — 部落格列表
  - GET /api/blogs?size=&blogTypeId=&keyword=
*/

const esc = NongHeader.escapeHtml;

let activeTypeId = null;
let currentKeyword = "";

(async function init() {
  const q = new URLSearchParams(location.search);
  if (q.has("blogTypeId")) activeTypeId = Number(q.get("blogTypeId")) || null;
  if (q.has("keyword"))    { currentKeyword = q.get("keyword"); document.getElementById("keyword").value = currentKeyword; }
  await loadBlogs();
  document.getElementById("searchForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    currentKeyword = document.getElementById("keyword").value.trim();
    syncQuery();
    await loadBlogs();
  });
  document.addEventListener("click", async (e) => {
    const pill = e.target.closest("[data-type-id]");
    if (!pill) return;
    const v = pill.dataset.typeId;
    activeTypeId = v === "" ? null : Number(v);
    syncQuery();
    await loadBlogs();
  });
})();

async function loadBlogs() {
  const grid = document.getElementById("blogGrid");
  grid.innerHTML = `<p style="grid-column:1/-1;color:var(--muted);text-align:center;">載入中…</p>`;
  const params = new URLSearchParams({ size: "24" });
  if (activeTypeId != null) params.set("blogTypeId", String(activeTypeId));
  if (currentKeyword) params.set("keyword", currentKeyword);
  try {
    const page = await NongAuth.request("/api/blogs?" + params.toString());
    const list = page.content || [];
    renderPills(list);
    if (!list.length) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">沒有符合的文章。</div>`;
      return;
    }
    grid.innerHTML = list.map(renderCard).join("");
  } catch (e) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">文章載入失敗：${esc(e.message)}</div>`;
  }
}

function renderPills(list) {
  const c = document.getElementById("typePills");
  const map = new Map();
  for (const b of list) {
    if (b.blogTypeId != null && !map.has(b.blogTypeId)) map.set(b.blogTypeId, b.blogTypeName || "未分類");
  }
  const pills = [{ id: null, name: "全部" }, ...Array.from(map, ([id, name]) => ({ id, name }))];
  c.innerHTML = pills.map((p) => `
    <button type="button" class="category-pill ${same(p.id, activeTypeId) ? "is-active" : ""}" data-type-id="${p.id == null ? "" : p.id}">${esc(p.name)}</button>
  `).join("");
}

function renderCard(b) {
  return `
    <article>
      <span>${esc(b.blogTypeName || "")}</span>
      <h3><a href="blog-detail.html?id=${b.id}">${esc(b.title)}</a></h3>
      <p>${esc(truncate(b.content || "", 100))}</p>
      <div class="card-meta" style="margin-top:10px">
        <span>${esc(b.authorName || "")}</span>
        <span>👁 ${b.viewCount || 0} ｜ ♡ ${b.likeCount || 0} ｜ 💬 ${b.commentCount || 0}</span>
      </div>
    </article>
  `;
}

function truncate(s, n) { s = String(s).replace(/\s+/g, " "); return s.length > n ? s.slice(0, n) + "…" : s; }
function same(a, b) { return (a == null && b == null) || a === b; }
function syncQuery() {
  const params = new URLSearchParams();
  if (activeTypeId != null) params.set("blogTypeId", String(activeTypeId));
  if (currentKeyword) params.set("keyword", currentKeyword);
  const next = params.toString();
  history.replaceState(null, "", next ? `?${next}` : location.pathname);
}
