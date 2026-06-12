const ProducerPage = (function () {
  const esc = NongHeader.escapeHtml;
  const currency = (v) => `NT$ ${Number(v || 0).toLocaleString("zh-TW")}`;
  const PRODUCER_FALLBACKS = [
    "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1523741543316-beb7fc7023d8?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1492496913980-501348b61469?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=1200&q=80"
  ];
  const PRODUCT_FALLBACK = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";
  const FARM_VISUALS = [
    { names: ["\u793a\u7bc4\u8fb2\u5834"], url: "https://storage.googleapis.com/cka101-15/form/exampleform1.png" },
    { names: ["\u68a8\u5c71\u96f2\u9802\u679c\u5712"], url: "https://storage.googleapis.com/cka101-15/form/fruitform.png" },
    { names: ["\u6c60\u4e0a\u79be\u7530"], url: "https://storage.googleapis.com/cka101-15/form/miform1.png" },
    { names: ["\u6771\u5c71\u653e\u7267\u86cb\u5834"], url: "https://storage.googleapis.com/cka101-15/form/eggform.png" },
    { names: ["\u6749\u6797\u6eaa\u6709\u6a5f\u8336\u574a"], url: "https://storage.googleapis.com/cka101-15/form/teaform1.png" }
  ];
  const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
  const PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每斤" };
  const scopes = [
    { key: "all", label: "全部農場" },
    { key: "products", label: "有商品" },
    { key: "groupBuys", label: "有團購" },
    { key: "trips", label: "有體驗" },
    { key: "blogs", label: "有日記" }
  ];

  let state = {
    farmers: [],
    products: [],
    groupBuys: [],
    trips: [],
    blogs: [],
    producers: [],
    activeScope: "all",
    keyword: ""
  };

  document.addEventListener("DOMContentLoaded", init);

  async function init() {
    if (document.getElementById("producerGrid")) {
      await initListPage();
      return;
    }
    if (document.getElementById("producerDetailView")) {
      await initDetailPage();
    }
  }

  async function initListPage() {
    readListQuery();
    renderScopePills();
    bindListEvents();
    await loadData();
    renderProducerList();
  }

  async function initDetailPage() {
    const id = Number(new URLSearchParams(location.search).get("id"));
    const view = document.getElementById("producerDetailView");
    if (!id) {
      view.innerHTML = emptyBlock("找不到農場 id");
      return;
    }
    const producer = await loadProducerDetail(id);
    if (!producer) {
      view.innerHTML = emptyBlock("目前沒有這個農場的公開內容");
      return;
    }
    renderProducerDetail(view, producer);
  }

  function readListQuery() {
    const q = new URLSearchParams(location.search);
    const scope = q.get("scope");
    state.activeScope = scopes.some((s) => s.key === scope) ? scope : "all";
    state.keyword = q.get("keyword") || "";
    const input = document.getElementById("producerKeyword");
    if (input) input.value = state.keyword;
  }

  function bindListEvents() {
    document.addEventListener("click", (e) => {
      const pill = e.target.closest("[data-producer-scope]");
      if (!pill) return;
      state.activeScope = pill.dataset.producerScope || "all";
      syncListQuery();
      renderScopePills();
      renderProducerList();
    });
    document.getElementById("producerSearchForm")?.addEventListener("submit", (e) => {
      e.preventDefault();
      state.keyword = document.getElementById("producerKeyword").value.trim();
      syncListQuery();
      loadData().then(renderProducerList);
    });
  }

  async function loadData() {
    const params = new URLSearchParams({ size: "100", sort: "createdAt,desc" });
    if (state.keyword) params.set("keyword", state.keyword);
    const [farmersPage, products, groupBuys, trips, blogs] = await Promise.all([
      fetchPage("/api/public/farmers?" + params.toString()),
      fetchPage("/api/public/products?size=100&sort=createdAt,desc"),
      fetchPage("/api/group-buys?size=100&sort=createdAt,desc"),
      fetchPage("/api/farm-trips?size=100&sort=createdAt,desc"),
      fetchPage("/api/blogs?size=100&sort=createdAt,desc")
    ]);
    state.farmers = farmersPage;
    state.products = products;
    state.groupBuys = groupBuys;
    state.trips = trips;
    state.blogs = blogs.filter((b) => b.authorType === "FARMER");
    state.producers = buildProducers();
  }

  async function loadProducerDetail(id) {
    const [farmer, products, groupBuys, trips, blogs] = await Promise.all([
      fetchOne(`/api/public/farmers/${id}`),
      fetchPage(`/api/public/farmers/${id}/products?size=24&sort=createdAt,desc`),
      fetchPage(`/api/public/farmers/${id}/group-buys?size=12&sort=deadlineDate,asc`),
      fetchPage(`/api/public/farmers/${id}/farm-trips?size=12&sort=createdAt,desc`),
      fetchPage(`/api/public/farmers/${id}/blogs?size=12&sort=createdAt,desc`)
    ]);
    if (!farmer) return null;
    return enrichProducer({
      id: farmer.id,
      name: farmer.farmName || "未命名農場",
      farmName: farmer.farmName,
      farmAddress: farmer.farmAddress,
      farmDesc: farmer.farmDesc,
      phone: farmer.phone,
      locLat: farmer.locLat,
      locLong: farmer.locLong,
      products,
      groupBuys,
      trips,
      blogs
    });
  }

  async function fetchPage(path) {
    try {
      const page = await NongAuth.request(path);
      if (Array.isArray(page)) return page;
      return Array.isArray(page?.content) ? page.content : [];
    } catch (e) {
      console.warn(path, e.message);
      return [];
    }
  }

  async function fetchOne(path) {
    try {
      return await NongAuth.request(path);
    } catch (e) {
      console.warn(path, e.message);
      return null;
    }
  }

  function buildProducers() {
    const map = new Map();
    state.farmers.forEach((farmer) => {
      if (farmer.id == null) return;
      map.set(farmer.id, {
        id: farmer.id,
        name: farmer.farmName || "未命名農場",
        farmName: farmer.farmName,
        farmAddress: farmer.farmAddress,
        farmDesc: farmer.farmDesc,
        phone: farmer.phone,
        locLat: farmer.locLat,
        locLong: farmer.locLong,
        products: [],
        groupBuys: [],
        trips: [],
        blogs: []
      });
    });
    state.products.forEach((item) => addItem(map, item.farmerId, item.farmerName, "products", item));
    state.groupBuys.forEach((item) => addItem(map, item.farmerId, item.farmerName, "groupBuys", item));
    state.trips.forEach((item) => addItem(map, item.farmerId, item.farmerName, "trips", item));
    state.blogs.forEach((item) => addItem(map, item.authorId, item.authorName, "blogs", item));

    return Array.from(map.values())
      .map(enrichProducer)
      .sort((a, b) => b.score - a.score || a.name.localeCompare(b.name, "zh-Hant"));
  }

  function addItem(map, id, name, bucket, item) {
    if (id == null) return;
    if (!map.has(id)) {
      return;
    }
    const producer = map.get(id);
    if (!producer.name && name) producer.name = name;
    producer[bucket].push(item);
  }

  function enrichProducer(producer) {
    const productImages = producer.products.map((p) => p.imageUrl);
    const groupImages = producer.groupBuys.map((g) => g.productImageUrl);
    const tripImages = producer.trips.map((t) => t.imageUrl);
    const blogImages = producer.blogs.map((b) => b.coverImageUrl);
    const images = [...productImages, ...tripImages, ...groupImages, ...blogImages].map(cleanImage).filter(Boolean);
    const origins = unique([
      producer.farmAddress,
      ...producer.products.map((p) => p.origin),
      ...producer.trips.map((t) => t.location)
    ]);
    const tags = unique([
      producer.farmAddress,
      ...producer.products.map((p) => p.categoryName),
      ...producer.trips.map((t) => TRIP_TYPE_LABELS[t.tripType]),
      ...producer.blogs.map((b) => b.blogTypeName)
    ]).slice(0, 5);
    const introSource =
      producer.farmDesc ||
      producer.products.find((p) => p.description)?.description ||
      producer.trips.find((t) => t.intro)?.intro ||
      producer.blogs.find((b) => b.content)?.content ||
      "";
    const score = producer.products.length * 4 + producer.groupBuys.length * 3 + producer.trips.length * 3 + producer.blogs.length;
    const farmVisual = resolveFarmVisual(producer);
    return {
      ...producer,
      logoUrl: farmVisual,
      heroImage: farmVisual || images[0] || PRODUCER_FALLBACKS[producer.id % PRODUCER_FALLBACKS.length],
      gallery: unique([farmVisual, ...images]).slice(0, 4),
      origins,
      tags,
      intro: truncate(introSource, 150),
      score
    };
  }

  function renderScopePills() {
    const host = document.getElementById("producerScopePills");
    if (!host) return;
    host.innerHTML = scopes.map((scope) => `
      <button type="button"
              class="category-pill ${state.activeScope === scope.key ? "is-active" : ""}"
              data-producer-scope="${scope.key}">
        ${esc(scope.label)}
      </button>
    `).join("");
  }

  function renderProducerList() {
    const grid = document.getElementById("producerGrid");
    if (!grid) return;
    const list = filterProducers();
    if (!list.length) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;margin:0;">目前沒有符合條件的農場</div>`;
      return;
    }
    grid.innerHTML = list.map(renderProducerCard).join("");
  }

  function filterProducers() {
    const keyword = state.keyword.trim().toLowerCase();
    return state.producers.filter((producer) => {
      const scopeOk = state.activeScope === "all" || producer[state.activeScope].length > 0;
      if (!scopeOk) return false;
      if (!keyword) return true;
      const haystack = [
        producer.name,
        producer.intro,
        ...producer.origins,
        ...producer.tags,
        ...producer.products.map((p) => p.name),
        ...producer.groupBuys.map((g) => g.productName),
        ...producer.trips.map((t) => t.title),
        ...producer.blogs.map((b) => b.title)
      ].join(" ").toLowerCase();
      return haystack.includes(keyword);
    });
  }

  function renderProducerCard(producer) {
    return `
      <article class="producer-card">
        <a class="producer-card-image" href="producer-detail.html?id=${producer.id}"
           style="background-image:url('${esc(producer.heroImage)}')" aria-label="${esc(producer.name)}">
          ${producer.logoUrl ? `<span class="producer-logo-mark"><img src="${esc(producer.logoUrl)}" alt="${esc(producer.name)} logo"></span>` : ""}
        </a>
        <div class="producer-card-body">
          <div class="producer-card-top">
            <span class="eyebrow">${esc(producer.origins[0] || "local producer")}</span>
            <span>${producer.products.length + producer.groupBuys.length + producer.trips.length + producer.blogs.length} 則公開內容</span>
          </div>
          <h2><a href="producer-detail.html?id=${producer.id}">${esc(producer.name)}</a></h2>
          <p>${esc(producer.intro || "把當季收成、產地活動與日常紀錄整理在這裡。")}</p>
          <div class="producer-tags">${producer.tags.map((tag) => `<span>${esc(tag)}</span>`).join("")}</div>
          <div class="producer-card-stats">
            <span><strong>${producer.products.length}</strong> 商品</span>
            <span><strong>${producer.groupBuys.length}</strong> 團購</span>
            <span><strong>${producer.trips.length}</strong> 體驗</span>
            <span><strong>${producer.blogs.length}</strong> 日記</span>
          </div>
        </div>
      </article>
    `;
  }

  function renderProducerDetail(view, producer) {
    const heroGallery = (producer.gallery.length ? producer.gallery : [producer.heroImage]).slice(0, 4);
    view.innerHTML = `
      <section class="producer-hero">
        <div class="producer-hero-media" style="background-image:url('${esc(producer.heroImage)}')" role="img" aria-label="${esc(producer.name)}"></div>
        <div class="producer-hero-copy">
          <div class="producer-title-lockup">
            ${producer.logoUrl ? `<img class="producer-detail-logo" src="${esc(producer.logoUrl)}" alt="${esc(producer.name)} logo">` : ""}
            <div>
              <span class="eyebrow">${esc(producer.origins[0] || "producer")}</span>
              <h1>${esc(producer.name)}</h1>
            </div>
          </div>
          <p>${esc(producer.intro || "這裡整理了農場目前公開的商品、團購、體驗活動與產地日記。")}</p>
          <div class="producer-hero-actions">
            <a class="primary-button" href="#producerProducts">看商品</a>
            <a class="secondary-button" href="producers.html">回農場清單</a>
          </div>
          <div class="producer-quick-stats">
            <span><strong>${producer.products.length}</strong> 商品</span>
            <span><strong>${producer.groupBuys.length}</strong> 團購</span>
            <span><strong>${producer.trips.length}</strong> 體驗</span>
            <span><strong>${producer.blogs.length}</strong> 日記</span>
          </div>
          ${renderContactFacts(producer)}
        </div>
      </section>

      <nav class="producer-anchor-nav" aria-label="農場內容">
        <a href="#producerAbout">介紹</a>
        <a href="#producerProducts">商品</a>
        <a href="#producerGroups">團購</a>
        <a href="#producerTrips">體驗活動</a>
        <a href="#producerBlogs">產地日記</a>
      </nav>

      <section class="producer-section producer-about" id="producerAbout">
        <div class="section-heading compact">
          <span class="eyebrow">about</span>
          <h2>農場介紹</h2>
        </div>
        <div class="producer-about-grid">
          <div>
            <p>${esc(producer.intro || "跟著這個農場的公開內容，看見從產地、採收、出貨到餐桌的完整節奏。")}</p>
            <div class="producer-tags">${producer.tags.map((tag) => `<span>${esc(tag)}</span>`).join("")}</div>
          </div>
          <div class="producer-gallery">
            ${heroGallery.map((img) => `<span style="background-image:url('${esc(img)}')"></span>`).join("")}
          </div>
        </div>
      </section>

      ${renderProductsSection(producer.products)}
      ${renderGroupsSection(producer.groupBuys)}
      ${renderTripsSection(producer.trips)}
      ${renderBlogsSection(producer.blogs)}
    `;
  }

  function renderProductsSection(products) {
    return `
      <section class="producer-section" id="producerProducts">
        ${sectionHead("products", "農場商品")}
        ${products.length ? `<div class="producer-content-grid">${products.slice(0, 8).map(renderProductCard).join("")}</div>` : sectionEmpty("目前沒有公開商品")}
      </section>
    `;
  }

  function renderGroupsSection(groupBuys) {
    return `
      <section class="producer-section" id="producerGroups">
        ${sectionHead("group buys", "團購")}
        ${groupBuys.length ? `<div class="producer-content-grid">${groupBuys.slice(0, 6).map(renderGroupCard).join("")}</div>` : sectionEmpty("目前沒有公開團購")}
      </section>
    `;
  }

  function renderTripsSection(trips) {
    return `
      <section class="producer-section" id="producerTrips">
        ${sectionHead("farm trips", "體驗活動")}
        ${trips.length ? `<div class="producer-content-grid">${trips.slice(0, 6).map(renderTripCard).join("")}</div>` : sectionEmpty("目前沒有公開體驗活動")}
      </section>
    `;
  }

  function renderBlogsSection(blogs) {
    return `
      <section class="producer-section" id="producerBlogs">
        ${sectionHead("journal", "產地日記")}
        ${blogs.length ? `<div class="producer-journal-list">${blogs.slice(0, 8).map(renderBlogItem).join("")}</div>` : sectionEmpty("目前沒有公開產地日記")}
      </section>
    `;
  }

  function renderContactFacts(producer) {
    const facts = [
      producer.farmAddress ? { label: "地址", value: producer.farmAddress } : null,
      producer.phone ? { label: "電話", value: producer.phone } : null
    ].filter(Boolean);
    if (!facts.length) return "";
    return `
      <div class="producer-contact-facts">
        ${facts.map((fact) => `<span><b>${esc(fact.label)}</b>${esc(fact.value)}</span>`).join("")}
      </div>
    `;
  }

  function renderProductCard(p) {
    const img = cleanImage(p.imageUrl) || PRODUCT_FALLBACK;
    return `
      <article class="product-card">
        <a href="product-detail.html?id=${p.id}" class="product-image" style="background-image:url('${esc(img)}')" aria-label="${esc(p.name)}"></a>
        <div class="product-body">
          <div class="product-meta"><span>${esc(p.categoryName || "商品")}</span><span>${esc(p.origin || "")}</span></div>
          <h3><a href="product-detail.html?id=${p.id}">${esc(p.name)}</a></h3>
          <p>${esc(truncate(p.description || "", 86))}</p>
          <div class="product-meta">
            <strong class="price">${currency(p.price)}</strong>
            <a class="secondary-button" href="product-detail.html?id=${p.id}">查看</a>
          </div>
        </div>
      </article>
    `;
  }

  function renderGroupCard(g) {
    const img = cleanImage(g.productImageUrl) || PRODUCT_FALLBACK;
    return `
      <article class="product-card">
        <a href="group-buy-detail.html?id=${g.id}" class="product-image" style="background-image:url('${esc(img)}')" aria-label="${esc(g.productName)}"></a>
        <div class="product-body">
          <div class="product-meta"><span>目標 ${g.targetQuantity || 0}</span><span>${formatDate(g.deadlineDate)}</span></div>
          <h3><a href="group-buy-detail.html?id=${g.id}">${esc(g.productName)}</a></h3>
          <p>團購價 <strong class="price">${currency(g.groupPrice)}</strong>，目前 ${g.currentQuantity || 0} / ${g.targetQuantity || 0}。</p>
          <div class="progress-bar" aria-label="團購進度"><span style="width:${g.percent || 0}%"></span></div>
        </div>
      </article>
    `;
  }

  function renderTripCard(t) {
    const img = cleanImage(t.imageUrl) || PRODUCER_FALLBACKS[t.id % PRODUCER_FALLBACKS.length];
    const unit = PRICING_LABELS[t.pricingMode] || "每人";
    return `
      <article class="product-card">
        <a href="farm-trip-detail.html?id=${t.id}" class="product-image" style="background-image:url('${esc(img)}')" aria-label="${esc(t.title)}"></a>
        <div class="product-body">
          <div class="product-meta"><span>${esc(TRIP_TYPE_LABELS[t.tripType] || "體驗活動")}</span><span>${esc(t.location || "")}</span></div>
          <h3><a href="farm-trip-detail.html?id=${t.id}">${esc(t.title)}</a></h3>
          <p>${esc(truncate(t.intro || "", 92))}</p>
          <div class="product-meta">
            <strong class="price">${currency(t.price)} / ${unit}</strong>
            <a class="secondary-button" href="farm-trip-detail.html?id=${t.id}">預約</a>
          </div>
        </div>
      </article>
    `;
  }

  function renderBlogItem(b) {
    return `
      <article class="producer-journal-item">
        <span>${esc(b.blogTypeName || "日記")} · ${formatDate(b.createdAt)}</span>
        <h3><a href="blog-detail.html?id=${b.id}">${esc(b.title)}</a></h3>
        <p>${esc(truncate(b.content || "", 130))}</p>
      </article>
    `;
  }

  function sectionHead(eyebrow, title) {
    return `
      <div class="section-heading compact">
        <span class="eyebrow">${esc(eyebrow)}</span>
        <h2>${esc(title)}</h2>
      </div>
    `;
  }

  function sectionEmpty(message) {
    return `<div class="producer-section-empty">${esc(message)}</div>`;
  }

  function emptyBlock(message) {
    return `<div class="empty-state">${esc(message)}</div>`;
  }

  function syncListQuery() {
    const params = new URLSearchParams();
    if (state.activeScope !== "all") params.set("scope", state.activeScope);
    if (state.keyword) params.set("keyword", state.keyword);
    const next = params.toString();
    history.replaceState(null, "", next ? `?${next}` : location.pathname);
  }

  function cleanImage(value) {
    const v = String(value || "").trim();
    return v ? v : "";
  }

  function resolveFarmVisual(producer) {
    const name = String(producer.farmName || producer.name || "");
    const matched = FARM_VISUALS.find((entry) => entry.names.some((candidate) => name.includes(candidate)));
    return matched ? matched.url : "";
  }

  function unique(values) {
    return [...new Set(values.map((v) => String(v || "").trim()).filter(Boolean))];
  }

  function truncate(value, max) {
    const text = String(value || "").replace(/\s+/g, " ").trim();
    return text.length > max ? text.slice(0, max) + "..." : text;
  }

  function formatDate(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return `${d.getMonth() + 1}/${d.getDate()}`;
  }
})();
