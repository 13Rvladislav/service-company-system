const API = "http://localhost:8082/api";

let profile = {};

// ==========================
// ИНИЦИАЛИЗАЦИЯ
// ==========================
document.addEventListener("DOMContentLoaded", async () => {

    await loadProfile();

    document.getElementById("editBtn").addEventListener("click", enableEdit);
    document.getElementById("saveBtn").addEventListener("click", saveProfile);

    document.getElementById("city").addEventListener("change", onCityChange);
    document.getElementById("street").addEventListener("change", onStreetChange);

    document.getElementById("uploadAvatar").addEventListener("click", () => {
        document.getElementById("avatarInput").click();
    });

    document.getElementById("avatarInput").addEventListener("change", uploadAvatar);

});

// ==========================
// ЗАГРУЗКА ПРОФИЛЯ
// ==========================
async function loadProfile() {

    const response = await fetch(`${API}/profile/me`, {
        credentials: "include"
    });

    if (!response.ok) {
        alert("Не удалось загрузить профиль");
        return;
    }

    profile = await response.json();

    document.getElementById("fullName").textContent =
        `${profile.lastName} ${profile.firstName}`;

    document.getElementById("role").textContent = profile.role;

    document.getElementById("firstName").value = profile.firstName;
    document.getElementById("lastName").value = profile.lastName;
    document.getElementById("middleName").value = profile.middleName ?? "";
    document.getElementById("phone").value = profile.phone;
    document.getElementById("apartment").value = profile.apartment ?? "";

    if (profile.hasAvatar) {
        document.getElementById("avatar").src =
            `${API}/profile/me/avatar?${Date.now()}`;
    }

    await loadCities();
}

// ==========================
// ГОРОДА
// ==========================
async function loadCities() {

    const response = await fetch(`${API}/address/cities`, {
        credentials: "include"
    });

    const cities = await response.json();

    const select = document.getElementById("city");
    select.innerHTML = "";

    let selectedCityId = null;

    cities.forEach(city => {

        const option = document.createElement("option");
        option.value = city.id;
        option.textContent = city.name;

        if (city.name === profile.city) {
            option.selected = true;
            selectedCityId = city.id;
        }

        select.appendChild(option);

    });

    if (!selectedCityId && cities.length > 0) {
        selectedCityId = cities[0].id;
    }

    await loadStreets(selectedCityId);

}

// ==========================
// УЛИЦЫ
// ==========================
async function loadStreets(cityId) {

    const response = await fetch(
        `${API}/address/cities/${cityId}/streets`,
        { credentials: "include" }
    );

    const streets = await response.json();

    const select = document.getElementById("street");
    select.innerHTML = "";

    let selectedStreetId = null;

    streets.forEach(street => {

        const option = document.createElement("option");
        option.value = street.id;
        option.textContent = street.name;

        if (street.name === profile.street) {
            option.selected = true;
            selectedStreetId = street.id;
        }

        select.appendChild(option);

    });

    if (!selectedStreetId && streets.length > 0) {
        selectedStreetId = streets[0].id;
    }

    await loadHouses(selectedStreetId);

}

// ==========================
// ДОМА
// ==========================
async function loadHouses(streetId) {

    const response = await fetch(
        `${API}/address/streets/${streetId}/houses`,
        { credentials: "include" }
    );

    const houses = await response.json();

    const select = document.getElementById("house");
    select.innerHTML = "";

    houses.forEach(house => {

        const option = document.createElement("option");
        option.value = house.id;
        option.textContent = house.number;

        if (house.id === profile.houseId) {
            option.selected = true;
        }

        select.appendChild(option);

    });

}

// ==========================
// СМЕНА ГОРОДА
// ==========================
async function onCityChange() {

    const cityId = document.getElementById("city").value;
    await loadStreets(cityId);

}

// ==========================
// СМЕНА УЛИЦЫ
// ==========================
async function onStreetChange() {

    const streetId = document.getElementById("street").value;
    await loadHouses(streetId);

}

// ==========================
// РЕДАКТИРОВАНИЕ
// ==========================
function enableEdit() {

    document.querySelectorAll(
        "#firstName,#lastName,#middleName,#phone,#apartment,#city,#street,#house"
    ).forEach(el => el.disabled = false);

    document.getElementById("editBtn").style.display = "none";
    document.getElementById("saveBtn").style.display = "inline-block";

}

// ==========================
// СОХРАНЕНИЕ
// ==========================
async function saveProfile() {

    const body = {

        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        middleName: document.getElementById("middleName").value,
        phone: document.getElementById("phone").value,
        houseId: document.getElementById("house").value,
        apartment: document.getElementById("apartment").value

    };

    const response = await fetch(`${API}/profile/me`, {

        method: "PUT",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)

    });

    if (!response.ok) {
        alert("Ошибка сохранения профиля");
        return;
    }

    document.querySelectorAll(
        "#firstName,#lastName,#middleName,#phone,#apartment,#city,#street,#house"
    ).forEach(el => el.disabled = true);

    document.getElementById("editBtn").style.display = "inline-block";
    document.getElementById("saveBtn").style.display = "none";

    await loadProfile();

    alert("Профиль успешно сохранён");

}

// ==========================
// ЗАГРУЗКА АВАТАРА
// ==========================
async function uploadAvatar(event) {

    const file = event.target.files[0];

    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API}/profile/me/avatar`, {

        method: "POST",
        credentials: "include",
        body: formData

    });

    if (!response.ok) {
        alert("Ошибка загрузки фото");
        return;
    }

    document.getElementById("avatar").src =
        `${API}/profile/me/avatar?${Date.now()}`;

    alert("Фото успешно обновлено");

}