/*
  小農工作台共用殼：所有 farmer-*.html 都載入這支
  - 角色守衛：未登入 / 非 FARMER 即踢回 login.html
  - 注入 header + sidebar；依 <body data-active-side="xxx"> 高亮選中項
  - 注入登出鈕（清掉 sessionStorage 後導去 login.html）

  小農後台是完全獨立的頁面，沒有消費者 nav；
  離開頁面（關 tab / 跳到 index.html）會因為 sessionStorage 自動失效。
*/
const FarmerShell = (function () {
  const SIDEBAR = [
    { key: "dashboard", label: "今日營運",     href: "farmer.html" },
    { key: "products",  label: "商品管理",     href: "farmer-products.html" },
    { key: "orders",    label: "訂單管理",     href: "farmer-orders.html" },
    { key: "gb-requests", label: "團購審核",   href: "farmer-group-buy-requests.html" },
    { key: "group-buys",  label: "我的團購活動", href: "farmer-group-buys.html" },
    { key: "gb-orders",   label: "團購整單",   href: "farmer-group-buy-orders.html" },
    { key: "farm-trips",  label: "體驗活動",   href: "farmer-farm-trips.html" },
    { key: "trip-bookings", label: "體驗預約管理", href: "farmer-farm-trip-bookings.html" }
  ];

  function init() {
    const user = NongAuth.requireBackofficeRole("FARMER");
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
        <a class="brand" href="farmer.html">
          <span class="brand-seal">農</span>
          <span><strong>小農工作台</strong><small>farmer console</small></span>
        </a>
        <nav class="utility-nav">
          <span class="session-pill">小農 ${escapeHtml(user.name)} ｜ ${escapeHtml(user.email)}</span>
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
        <p class="eyebrow">小農工作台</p>
        <h1>我的後台</h1>
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
