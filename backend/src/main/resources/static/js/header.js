/*
  共用 header：每個消費者頁面載入即執行，根據登入狀態渲染右上角，並把當前頁的 nav 加上 active 樣式。
  使用方式：頁面 <body data-page="consumer" data-active-nav="products"> + 在 <head> 之後載入 auth.js、本檔。
*/
(function clearBackofficeSessionOnConsumer() {
  // 進入任何消費者頁面時，主動清掉殘留的後台 session（離開後台 = 登出原則的最後一道）
  try {
    sessionStorage.removeItem("nong_backoffice_token");
    sessionStorage.removeItem("nong_backoffice_user");
  } catch (_e) { /* ignore */ }
})();

const NongHeader = (function () {
  function init() {
    injectHeaderHostIfNeeded();
    highlightActiveNav();
    renderUserZone();
    bindMobileNav();
    refreshCartBadge();
  }

  /**
   * 新頁面若用 <div id="headerHost"></div> 取代整個 <header> markup,
   * 這裡會自動注入；舊頁有 inline header 也沒關係,host 不存在就不做事。
   */
  function injectHeaderHostIfNeeded() {
    const host = document.getElementById("headerHost");
    if (!host) return;
    host.outerHTML = `
      <header class="site-header">
        <a class="brand" href="index.html" aria-label="你儂我農首頁">
          <span class="brand-seal">儂</span>
          <span><strong>你儂我農</strong><small>from soil to table</small></span>
        </a>
        <button class="mobile-menu" id="mobileMenu" type="button" aria-label="開啟選單">☰</button>
        <nav class="main-nav" id="mainNav" aria-label="消費者主要功能">
          <div class="nav-item"><a href="index.html"      data-nav-key="home">首頁</a></div>
          <div class="nav-item"><a href="products.html"   data-nav-key="products">逛商品</a></div>
          <div class="nav-item"><a href="group-buys.html" data-nav-key="group-buys">團購</a></div>
          <div class="nav-item"><a href="farm-trips.html" data-nav-key="farm-trips">體驗活動</a></div>
          <div class="nav-item"><a href="blogs.html"      data-nav-key="blogs">部落格</a></div>
          <div class="nav-item"><a href="news.html"       data-nav-key="news">最新消息</a></div>
          <div class="user-zone" id="userZone"></div>
        </nav>
      </header>
    `;
  }

  function highlightActiveNav() {
    const active = document.body.dataset.activeNav;
    if (!active) return;
    document.querySelectorAll("[data-nav-key]").forEach((el) => {
      el.classList.toggle("is-active", el.dataset.navKey === active);
    });
  }

  function renderUserZone() {
    const zone = document.getElementById("userZone");
    if (!zone) return;
    const user = NongAuth.getConsumerUser();
    if (!user) {
      zone.innerHTML = `
        <a class="user-btn ghost" href="login.html">登入</a>
        <a class="user-btn primary" href="register.html">註冊</a>
      `;
      return;
    }
    const type = user.type;
    const typeLabel = type === "FARMER" ? "小農" : type === "ADMIN" ? "管理員" : "會員";
    const initial = (user.name || user.email || "U").slice(0, 1);

    zone.innerHTML = `
      <a class="cart-icon" href="cart.html" aria-label="購物車">
        <span aria-hidden="true">🛒</span>
        <span class="cart-badge" id="cartBadge">0</span>
      </a>
      <span class="user-greeting">${typeLabel}・${escapeHtml(user.name || user.email)}</span>
      <div class="user-dropdown" id="userDropdown">
        <button class="user-avatar" type="button" aria-haspopup="true">${escapeHtml(initial)}</button>
        <div class="user-menu" role="menu">
          <div class="menu-head">
            <strong>${escapeHtml(user.name || "")}</strong>
            ${escapeHtml(user.email || "")}
          </div>
          ${buildUserMenuItems(user)}
        </div>
      </div>
    `;
    bindUserMenu();
  }

  function buildUserMenuItems(user) {
    const type = user.type;
    const isMember = type === "MEMBER";
    const isFarmer = type === "FARMER";
    const isAdmin  = type === "ADMIN";
    const items = [`<a href="profile.html">會員中心</a>`];

    if (isMember) {
      items.push(`<a href="orders.html">我的訂單</a>`);
      items.push(`<a href="wishlist.html">我的收藏 ♥</a>`);
      items.push(`<a href="my-group-buy-requests.html">我發起的團購</a>`);
      items.push(`<a href="my-group-buys.html">我參加的團購</a>`);
      items.push(`<a href="my-group-buy-orders.html">我的團購整單</a>`);
      items.push(`<a href="my-farm-trip-bookings.html">我的體驗預約</a>`);
      items.push(`<a href="my-blogs.html">我的文章</a>`);
    }
    if (isFarmer) {
      items.push(`<a href="farmer.html">前往小農工作台</a>`);
      items.push(`<a href="my-blogs.html">我的文章</a>`);
    }
    if (isAdmin) {
      items.push(`<div class="divider"></div>`);
      items.push(`<a href="admin.html">前往管理員後台</a>`);
    }

    // Phase A 拆分後一個帳號只屬一種身份,不再提供切換按鈕

    items.push(`<div class="divider"></div>`);
    items.push(`<button class="danger" data-action="logout">登出</button>`);
    return items.join("");
  }

  function bindUserMenu() {
    const dropdown = document.getElementById("userDropdown");
    if (!dropdown) return;
    dropdown.addEventListener("click", async (e) => {
      const target = e.target.closest("[data-action]");
      if (!target) return;
      const action = target.dataset.action;
      if (action === "logout") {
        NongAuth.logoutAll();
        window.location.href = "index.html";
      }
      // Phase A 拆分後拿掉 switchRole;若要在會員/小農之間切換,各自獨立帳號重新登入
    });

    const avatar = dropdown.querySelector(".user-avatar");
    avatar.addEventListener("click", (e) => {
      e.stopPropagation();
      dropdown.classList.toggle("is-open");
    });
    document.addEventListener("click", () => dropdown.classList.remove("is-open"));
  }

  function bindMobileNav() {
    const button = document.getElementById("mobileMenu");
    const nav = document.getElementById("mainNav");
    if (!button || !nav) return;
    button.addEventListener("click", () => nav.classList.toggle("is-open"));
  }

  async function refreshCartBadge() {
    const badge = document.getElementById("cartBadge");
    if (!badge) return;
    const user = NongAuth.getConsumerUser();
    if (!user || user.type !== "MEMBER") {
      badge.classList.remove("is-visible");
      return;
    }
    try {
      const cart = await NongAuth.request("/api/cart");
      const qty = cart?.totalQuantity ?? 0;
      badge.textContent = qty > 99 ? "99+" : String(qty);
      badge.classList.toggle("is-visible", qty > 0);
    } catch (_e) {
      badge.classList.remove("is-visible");
    }
  }

  function escapeHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, (c) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[c]);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  return { refreshCartBadge, renderUserZone, escapeHtml };
})();
