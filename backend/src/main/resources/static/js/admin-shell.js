/*
  管理員後台共用殼：所有 admin-*.html / admin.html 都載入這支
  - 角色守衛：未登入 / 非 ADMIN 即踢回 login.html
  - 注入 header + sidebar；依 <body data-active-side="xxx"> 高亮選中項
  - 登出鈕清掉 sessionStorage 後導去 login.html
*/
const AdminShell = (function () {
  const SIDEBAR = [
    { key: "dashboard", label: "管理總覽",   href: "admin.html" },
    { key: "users",     label: "會員管理",   href: "admin-users.html" },
    { key: "products",  label: "商品管理",   href: "admin-products.html" },
    { key: "blog-reports", label: "檢舉處理", href: "admin-blog-reports.html" }
  ];

  function init() {
    const user = NongAuth.requireBackofficeRole("ADMIN");
    if (!user) return null;
    injectHeader(user);
    injectSidebar();
    bindLogout();
    return user;
  }

  function injectHeader(user) {
    const host = document.getElementById("headerHost");
    if (!host) return;
    host.outerHTML = `
      <header class="site-header dashboard-header">
        <a class="brand" href="admin.html">
          <span class="brand-seal">管</span>
          <span><strong>管理員後台</strong><small>admin console</small></span>
        </a>
        <nav class="utility-nav">
          <span class="session-pill">管理員 ${escapeHtml(user.name)} ｜ ${escapeHtml(user.email)}</span>
          <button class="logout-button" id="logoutBtn" type="button">登出</button>
        </nav>
      </header>
    `;
  }

  function injectSidebar() {
    const host = document.getElementById("sidebarHost");
    if (!host) return;
    const active = document.body.dataset.activeSide || "";
    host.outerHTML = `
      <aside class="dashboard-sidebar">
        <p class="eyebrow">平台營運</p>
        <h1>管理模組</h1>
        ${SIDEBAR.map((s) => `
          <a class="side-link ${s.key === active ? "is-active" : ""}" href="${s.href}">${escapeHtml(s.label)}</a>
        `).join("")}
      </aside>
    `;
  }

  function bindLogout() {
    const btn = document.getElementById("logoutBtn");
    if (!btn) return;
    btn.addEventListener("click", () => {
      NongAuth.logoutBackoffice();
      window.location.replace("login.html?reason=logout");
    });
  }

  function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, (c) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[c]);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  return { escapeHtml };
})();
