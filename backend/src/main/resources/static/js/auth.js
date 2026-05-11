/*
  你儂我農 — 登入 / 註冊 / 會話 共用函式

  「離開後台就要登出」的實作策略：
  - CONSUMER token：放 localStorage（沿用舊 Vue 版 key），跨 tab、關瀏覽器仍記得
  - FARMER / ADMIN token：只放 sessionStorage，關 tab 自動清除
  - 進消費者頁時主動清掉 sessionStorage 中的後台 key
  - 後台頁完全沒有「回首頁」連結，只剩登出鈕，登出後導去 login.html
*/
const NongAuth = (function () {
  const API_BASE = ""; // 同源，相對路徑即可
  const CONSUMER_TOKEN_KEY = "nong_token";
  const CONSUMER_USER_KEY  = "nong_user";
  const BACKOFFICE_TOKEN_KEY = "nong_backoffice_token";
  const BACKOFFICE_USER_KEY  = "nong_backoffice_user";

  async function request(path, options = {}) {
    const headers = Object.assign(
      { "Content-Type": "application/json" },
      options.headers || {}
    );
    const token = getActiveToken();
    if (token && !headers.Authorization) headers.Authorization = `Bearer ${token}`;
    const res = await fetch(API_BASE + path, Object.assign({}, options, { headers }));
    if (!res.ok) {
      let msg = `${res.status} ${res.statusText}`;
      try {
        const ct = res.headers.get("content-type") || "";
        if (ct.includes("application/json")) {
          const body = await res.json();
          msg = body.message || body.error || JSON.stringify(body);
        } else {
          const text = await res.text();
          if (text) msg = text;
        }
      } catch (_e) { /* swallow */ }
      const err = new Error(msg);
      err.status = res.status;
      throw err;
    }
    if (res.status === 204) return null;
    return res.json();
  }

  async function login(email, password) {
    return request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
  }

  async function register(payload) {
    return request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  }

  /** 切換 active role；需該帳號實際擁有目標角色，回傳新的 AuthResponse */
  async function switchRole(targetRole) {
    const next = await request("/api/account/switch-role", {
      method: "POST",
      body: JSON.stringify({ role: targetRole })
    });
    saveSession(next);
    return next;
  }

  function saveSession(authResp) {
    const role = authResp.activeRole || authResp.role;
    if (role === "FARMER" || role === "ADMIN") {
      sessionStorage.setItem(BACKOFFICE_TOKEN_KEY, authResp.token);
      sessionStorage.setItem(BACKOFFICE_USER_KEY, JSON.stringify(authResp));
      localStorage.removeItem(CONSUMER_TOKEN_KEY);
      localStorage.removeItem(CONSUMER_USER_KEY);
    } else {
      localStorage.setItem(CONSUMER_TOKEN_KEY, authResp.token);
      localStorage.setItem(CONSUMER_USER_KEY, JSON.stringify(authResp));
      sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
      sessionStorage.removeItem(BACKOFFICE_USER_KEY);
    }
  }

  function getConsumerUser() {
    const raw = localStorage.getItem(CONSUMER_USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }
  function getConsumerToken() { return localStorage.getItem(CONSUMER_TOKEN_KEY); }

  function getBackofficeUser() {
    const raw = sessionStorage.getItem(BACKOFFICE_USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }
  function getBackofficeToken() { return sessionStorage.getItem(BACKOFFICE_TOKEN_KEY); }

  /** 取得目前有效的會話（後台優先，消費者 fallback）；只有當前頁類型對得上才回傳 */
  function getActiveUser() {
    const page = document.body && document.body.dataset.page;
    if (page === "farmer" || page === "admin") return getBackofficeUser();
    return getConsumerUser();
  }
  function getActiveToken() {
    const page = document.body && document.body.dataset.page;
    if (page === "farmer" || page === "admin") return getBackofficeToken();
    return getConsumerToken();
  }

  function logoutAll() {
    localStorage.removeItem(CONSUMER_TOKEN_KEY);
    localStorage.removeItem(CONSUMER_USER_KEY);
    sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
    sessionStorage.removeItem(BACKOFFICE_USER_KEY);
  }
  function logoutBackoffice() {
    sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
    sessionStorage.removeItem(BACKOFFICE_USER_KEY);
  }

  /** 進入後台頁時呼叫；缺 token 或角色不符就踢回 login.html */
  function requireBackofficeRole(expectedRole) {
    const user = getBackofficeUser();
    const token = getBackofficeToken();
    if (!user || !token) {
      window.location.replace("login.html?reason=auth");
      return null;
    }
    const role = user.activeRole || user.role;
    if (role !== expectedRole) {
      window.location.replace("login.html?reason=role");
      return null;
    }
    return user;
  }

  function redirectAfterLogin(authResp) {
    const role = authResp.activeRole || authResp.role;
    if (role === "ADMIN")  { window.location.href = "admin.html";  return; }
    if (role === "FARMER") { window.location.href = "farmer.html"; return; }
    window.location.href = "index.html";
  }

  return {
    request,
    login, register, switchRole,
    saveSession,
    getConsumerUser, getConsumerToken,
    getBackofficeUser, getBackofficeToken,
    getActiveUser, getActiveToken,
    logoutAll, logoutBackoffice,
    requireBackofficeRole,
    redirectAfterLogin
  };
})();
