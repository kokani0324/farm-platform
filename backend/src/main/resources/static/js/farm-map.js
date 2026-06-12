let farms = [];
const markers = new Map();

const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";

const farmIcon = L.divIcon({
    className: "farm-marker",
    html: "農",
    iconSize: [34, 34],
    iconAnchor: [17, 17]
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
        .filter((farmer) => Number.isFinite(Number(farmer.locLat)) && Number.isFinite(Number(farmer.locLong)))
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
        const marker = L.marker([farm.lat, farm.lng])
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
