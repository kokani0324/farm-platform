let farms = [];
const markers = new Map();

const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";

const farmIcon = L.divIcon({
    className: "farm-marker-shell",
    html: `
        <div style="
            width: 42px;
            height: 42px;
            display: grid;
            place-items: center;
            border: 3px solid #4f9368;
            border-radius: 50%;
            background: #fff;
            color: #4f9368;
            box-shadow: 0 4px 12px rgba(0, 0, 0, .28);
        ">
            <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
                <g fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M7 15h18l-2.3 9.5a3 3 0 0 1-2.9 2.3h-7.6a3 3 0 0 1-2.9-2.3L7 15Z"/>
                    <path d="M10 15c.8-3.9 3-6 6-6s5.2 2.1 6 6"/>
                    <path d="M16 9V4"/>
                    <path d="M16 8c-3.1-.2-5.3-1.7-6.5-4.3"/>
                    <path d="M16 8c3.1-.2 5.3-1.7 6.5-4.3"/>
                    <path d="M11 20h10"/>
                </g>
            </svg>
        </div>
    `,
    iconSize: [42, 42],
    iconAnchor: [21, 21],
    popupAnchor: [0, -24]
});

const map = L.map("map", {
    center: [23.7, 121],
    zoom: 8.7,
    minZoom: 7,
    maxBounds: [
        [21.7, 119.0],
        [25.5, 122.8]
    ]
});

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
}).addTo(map);

init();

async function init() {
    farms = await loadFarms();
    renderFarmList();
    renderMarkers();
    // fitFarmBounds();
}

async function loadFarms() {
    const farmerPage = await NongAuth.request("/api/public/farmers?size=100");
    const productPage = await NongAuth.request("/api/public/products?size=200");

    const farmers = Array.isArray(farmerPage.content) ? farmerPage.content : farmerPage;
    const products = Array.isArray(productPage.content) ? productPage.content : productPage;

    return farmers
        .filter((farmer) => hasCoordinate(farmer.locLat) && hasCoordinate(farmer.locLong))
        .map((farmer) => ({
            id: farmer.id,
            name: farmer.farmName || "未命名農場",
            subtitle: farmer.farmAddress || "台灣在地小農",
            description: farmer.farmDesc || "這位小農尚未填寫介紹。",
            lat: Number(farmer.locLat),
            lng: Number(farmer.locLong),
            image: resolveFarmImage(farmer, products),
            link: `producer-detail.html?id=${farmer.id}`
        }));
}

function hasCoordinate(value) {
    return value !== null
        && value !== undefined
        && String(value).trim() !== ""
        && Number.isFinite(Number(value));
}

function resolveFarmImage(farmer, products) {
    const product = products.find((item) =>
        Number(item.farmerId) === Number(farmer.id) && item.imageUrl
    );

    return product ? product.imageUrl : DEFAULT_IMAGE;
}
function fitFarmBounds() {
    if (!farms.length) return;

    const bounds = L.latLngBounds(
        farms.map((farm) => [farm.lat, farm.lng])
    );

    map.fitBounds(bounds, {
        padding: [80, 80],
        maxZoom: 9
    });
}

function renderFarmList() {
    const list = document.getElementById("farmList");

    list.innerHTML = farms.map((farm) => `
        <article class="farm-card" data-farm-id="${farm.id}">
            <img src="${farm.image}" alt="${farm.name}">
            <div>
                <h2>${farm.name}</h2>
                <p>${farm.subtitle}</p>
            </div>
        </article>
    `).join("");

    list.addEventListener("click", (event) => {
        const card = event.target.closest(".farm-card");
        if (!card) return;

        const farm = farms.find((item) => item.id === Number(card.dataset.farmId));
        if (!farm) return;

        selectFarm(farm);
    });
}

function renderMarkers() {
    farms.forEach((farm) => {
        const marker = L.marker([farm.lat, farm.lng], { icon: farmIcon })
            .addTo(map)
            .bindPopup(farm.name);

        marker.on("click", () => {
            selectFarm(farm);
        });

        markers.set(farm.id, marker);
    });
}

function selectFarm(farm) {
    document.querySelectorAll(".farm-card").forEach((card) => {
        card.classList.toggle("is-active", Number(card.dataset.farmId) === farm.id);
    });

    map.flyTo([farm.lat, farm.lng], 11);

    const marker = markers.get(farm.id);
    if (marker) marker.openPopup();

    renderFarmDetail(farm);
}

function renderFarmDetail(farm) {
    const detail = document.getElementById("farmDetail");

    detail.classList.remove("is-hidden");
    detail.innerHTML = `
        <button class="detail-close" type="button" aria-label="關閉">×</button>
        <img src="${farm.image}" alt="${farm.name}">
        <div class="farm-detail-content">
            <h2>${farm.name}</h2>
            <p>${farm.description}</p>
            <a class="detail-button" href="${farm.link}">前往農場頁</a>
        </div>
    `;

    detail.querySelector(".detail-close").addEventListener("click", () => {
        detail.classList.add("is-hidden");
    });
}
