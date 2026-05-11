/*
  blog-detail.html — 文章詳情 + 按讚 + 留言
  - GET  /api/blogs/:id
  - GET  /api/blogs/:id/comments
  - POST /api/blogs/:id/like
  - POST /api/blogs/:id/comments
*/

const esc = NongHeader.escapeHtml;
const view = document.getElementById("blogView");

let blogId = null;
let blog = null;

(async function init() {
  blogId = new URLSearchParams(location.search).get("id");
  if (!blogId) { view.innerHTML = empty("缺少文章 id"); return; }
  await reload();
})();

async function reload() {
  try {
    const [b, comments] = await Promise.all([
      NongAuth.request(`/api/blogs/${blogId}`),
      NongAuth.request(`/api/blogs/${blogId}/comments?size=50`)
    ]);
    blog = b;
    render(b, comments.content || []);
  } catch (e) {
    view.innerHTML = empty("文章載入失敗：" + e.message);
  }
}

function render(b, comments) {
  const cover = b.coverImageUrl && b.coverImageUrl.trim() ? `<div class="detail-cover" style="background-image:url('${esc(b.coverImageUrl)}');min-height:300px;margin-bottom:22px"></div>` : "";
  view.innerHTML = `
    <article class="detail-body">
      <span class="eyebrow">${esc(b.blogTypeName || "")}</span>
      <h1 style="margin:6px 0 14px;font-size:clamp(28px,4vw,44px);line-height:1.2">${esc(b.title)}</h1>
      <div class="meta-row" style="color:var(--muted);font-size:13px;margin-bottom:18px">
        <span>作者：${esc(b.authorName || "")}</span>
        <span>👁 ${b.viewCount || 0}</span>
        <span>♡ ${b.likeCount || 0}</span>
        <span>💬 ${b.commentCount || 0}</span>
        <span>${fmt(b.createdAt)}</span>
      </div>
      ${cover}
      <div style="white-space:pre-wrap;line-height:2;color:#3a463b;font-size:16px">${esc(b.content || "")}</div>

      <div class="action-row" style="margin-top:24px">
        <button class="secondary-button" id="likeBtn">♡ 按讚 (${b.likeCount || 0})</button>
        <button class="user-btn ghost" id="reportBtn" style="color:var(--clay)">檢舉</button>
      </div>
    </article>

    <section class="detail-body" style="margin-top:0">
      <div class="section-heading compact"><span class="eyebrow">comments</span><h2>留言（${comments.length}）</h2></div>
      <form id="commentForm" class="comment-form" style="margin-bottom:18px">
        <textarea id="commentBody" maxlength="500" placeholder="留下你的想法… (登入後才能留言)"></textarea>
        <div style="text-align:right;margin-top:8px"><button class="primary-button" type="submit">送出留言</button></div>
      </form>
      <div class="comment-list">
        ${comments.length === 0
          ? `<div class="empty-state" style="margin:0">還沒有留言，第一個來分享吧。</div>`
          : comments.map((c) => `
            <div class="comment-item">
              <div class="meta">${esc(c.authorName || "匿名")} ・ ${fmt(c.createdAt)}</div>
              <div style="white-space:pre-wrap">${esc(c.content || "")}</div>
            </div>
          `).join("")}
      </div>
    </section>
  `;
  bindEvents();
}

function bindEvents() {
  document.getElementById("likeBtn").addEventListener("click", async () => {
    try {
      const updated = await NongAuth.request(`/api/blogs/${blogId}/like`, { method: "POST" });
      blog = updated;
      document.getElementById("likeBtn").textContent = `♡ 按讚 (${updated.likeCount || 0})`;
      toast("已按讚");
    } catch (e) { toast(e.message || "按讚失敗", true); }
  });

  document.getElementById("reportBtn").addEventListener("click", async () => {
    const user = NongAuth.getConsumerUser();
    if (!user) { toast("請先登入再檢舉"); return; }
    const reason = prompt("檢舉原因？(會送到管理員)");
    if (!reason) return;
    try {
      await NongAuth.request(`/api/blogs/${blogId}/report`, {
        method: "POST", body: JSON.stringify({ reason })
      });
      toast("已送出檢舉，謝謝你");
    } catch (e) { toast(e.message || "檢舉失敗", true); }
  });

  document.getElementById("commentForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const user = NongAuth.getConsumerUser();
    if (!user) { toast("請先登入再留言"); setTimeout(() => location.href = "login.html", 700); return; }
    const content = document.getElementById("commentBody").value.trim();
    if (!content) { toast("請輸入留言內容", true); return; }
    try {
      await NongAuth.request(`/api/blogs/${blogId}/comments`, {
        method: "POST", body: JSON.stringify({ content })
      });
      toast("已留言");
      document.getElementById("commentBody").value = "";
      await reload();
    } catch (err) { toast(err.message || "留言失敗", true); }
  });
}

function fmt(iso) {
  if (!iso) return "—"; const d = new Date(iso); if (isNaN(d.getTime())) return iso;
  const p = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}/${p(d.getMonth()+1)}/${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}
function empty(msg) { return `<div class="empty-state">${esc(msg)}</div>`; }
function toast(msg, isError) {
  const t = document.getElementById("toast"); if (!t) return;
  t.textContent = msg; t.classList.toggle("is-error", !!isError); t.classList.add("is-visible");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("is-visible"), 1800);
}
