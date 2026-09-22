const AUTH = "http://localhost:8081";
const USER = "http://localhost:8082";

/* ---------- DOM ---------- */

const loginPage = document.getElementById("loginPage");
const appPage = document.getElementById("appPage");

const email = document.getElementById("email");
const password = document.getElementById("password");
const error = document.getElementById("error");

const userName = document.getElementById("userName");
const userEmail = document.getElementById("userEmail");
const roleText = document.getElementById("roleText");

const pageTitle = document.getElementById("pageTitle");
const view = document.getElementById("view");

let authUser = null;

/* ---------- Авторизация ---------- */

document.getElementById("loginBtn")?.addEventListener("click", login);
document.getElementById("logoutBtn")?.addEventListener("click", logout);

document.getElementById("profileBtn")?.addEventListener("click", loadProfile);
document.getElementById("equipmentBtn")?.addEventListener("click", loadEquipment);
document.getElementById("requestsBtn")?.addEventListener("click", loadRequests);

checkAuth();

async function login() {

    error.innerText = "";

    const response = await fetch(`${AUTH}/api/auth/login`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email.value,
            password: password.value
        })
    });

    const data = await response.json();

    if (!response.ok) {
        error.innerText = data.message;
        return;
    }

    authUser = data;
    showApp();
    loadProfile();
}

async function checkAuth() {

    try {

        const response = await fetch(`${AUTH}/api/auth/me`, {
            credentials: "include"
        });

        if (!response.ok) return;

        authUser = await response.json();

        showApp();
        loadProfile();

    } catch {
    }

}

async function logout() {

    await fetch(`${AUTH}/api/auth/logout`, {
        method: "POST",
        credentials: "include"
    });

    location.reload();
}

/* ---------- UI ---------- */

function showApp() {

    loginPage.style.display = "none";
    appPage.style.display = "block";

    userEmail.innerText = authUser.email;
    roleText.innerText = authUser.role;
}

/* ---------- Профиль ---------- */

async function loadProfile() {

    pageTitle.innerText = "Мой профиль";

    const response = await fetch(`${USER}/api/profile/me`, {
        credentials: "include"
    });

    if (!response.ok) {
        view.innerHTML = `
            <div class="panel">
                <h2>Ошибка загрузки профиля</h2>
            </div>
        `;
        return;
    }

    const profile = await response.json();

    userName.innerText =
        `${profile.lastName} ${profile.firstName}`;

    switch (authUser.role) {

        case "CLIENT":
            renderClient(profile);
            break;

        case "ENGINEER":
            renderEngineer(profile);
            break;

        case "DISPATCHER":
        case "ADMIN":
            renderDispatcher(profile);
            break;

        default:
            view.innerHTML = `
                <div class="panel">
                    Неизвестная роль
                </div>
            `;
    }

}

/* ---------- CLIENT ---------- */

function renderClient(p) {

    view.innerHTML = `
        <div class="panel">

            <h2>Личные данные</h2>

            <div class="grid">

                <div class="field">
                    <label>Фамилия</label>
                    <input value="${p.lastName ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Имя</label>
                    <input value="${p.firstName ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Отчество</label>
                    <input value="${p.middleName ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Телефон</label>
                    <input value="${p.phone ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Город</label>
                    <input value="${p.city ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Улица</label>
                    <input value="${p.street ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Дом</label>
                    <input value="${p.house ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Квартира</label>
                    <input value="${p.apartment ?? ""}" readonly>
                </div>

            </div>

        </div>
    `;
}

/* ---------- ENGINEER ---------- */

function renderEngineer(p) {

    view.innerHTML = `
        <div class="panel">

            <h2>Профиль мастера</h2>

            <div class="grid">

                <div class="field">
                    <label>Фамилия</label>
                    <input value="${p.lastName}" readonly>
                </div>

                <div class="field">
                    <label>Имя</label>
                    <input value="${p.firstName}" readonly>
                </div>

                <div class="field">
                    <label>Отчество</label>
                    <input value="${p.middleName ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Телефон</label>
                    <input value="${p.phone}" readonly>
                </div>

                <div class="field">
                    <label>Табельный номер</label>
                    <input value="${p.employeeNumber}" readonly>
                </div>

                <div class="field">
                    <label>Специализация</label>
                    <input value="${p.specialization}" readonly>
                </div>

                <div class="field">
                    <label>Статус</label>
                    <input value="${p.status}" readonly>
                </div>

                <div class="field">
                    <label>Зона обслуживания</label>
                    <input value="${p.zoneId ?? "Не назначена"}" readonly>
                </div>

            </div>

        </div>
    `;
}

/* ---------- DISPATCHER / ADMIN ---------- */

function renderDispatcher(p) {

    const title =
        authUser.role === "ADMIN"
            ? "Профиль администратора"
            : "Профиль диспетчера";

    view.innerHTML = `
        <div class="panel">

            <h2>${title}</h2>

            <div class="grid">

                <div class="field">
                    <label>Фамилия</label>
                    <input value="${p.lastName}" readonly>
                </div>

                <div class="field">
                    <label>Имя</label>
                    <input value="${p.firstName}" readonly>
                </div>

                <div class="field">
                    <label>Отчество</label>
                    <input value="${p.middleName ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Телефон</label>
                    <input value="${p.phone}" readonly>
                </div>

                <div class="field">
                    <label>Табельный номер</label>
                    <input value="${p.employeeNumber}" readonly>
                </div>

                <div class="field">
                    <label>Подразделение</label>
                    <input value="${p.department}" readonly>
                </div>

            </div>

        </div>
    `;
}

/* ---------- Заглушки ---------- */

function loadEquipment() {

    pageTitle.innerText = "Моё оборудование";

    view.innerHTML = `
        <div class="panel">
            <h2>Моё оборудование</h2>
            <p>Раздел будет реализован следующим этапом.</p>
        </div>
    `;
}

function loadRequests() {

    pageTitle.innerText = "Мои заявки";

    view.innerHTML = `
        <div class="panel">
            <h2>Мои заявки</h2>
            <p>Раздел будет реализован следующим этапом.</p>
        </div>
    `;
}