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

    // Получаем текущего пользователя
    const meResponse = await fetch(`${AUTH}/api/auth/me`, {
        credentials: "include"
    });

    authUser = await meResponse.json();

    // Сначала загружаем профиль, потом показываем приложение
    await showApp();
}

async function checkAuth() {

    try {

        const response = await fetch(`${AUTH}/api/auth/me`, {
            credentials: "include"
        });

        if (!response.ok) return;

        authUser = await response.json();

        await showApp();

    } catch {
        // Пользователь не авторизован
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

async function showApp() {

    loginPage.style.display = "none";
    appPage.style.display = "block";

    userEmail.innerText = authUser.email;
    roleText.innerText = authUser.role;

    await loadProfile();
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

    switch (profile.role) {

        case "CLIENT":
            renderClient(profile);
            break;

        case "ENGINEER":
            renderEngineer(profile);
            break;

        case "DISPATCHER":
            renderDispatcher(profile);
            break;

        case "ADMIN":
            renderAdmin(profile);
            break;

        default:
            view.innerHTML = `
                <div class="panel">
                    <h2>Неизвестная роль</h2>
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
                    <label>Табельный номер</label>
                    <input value="${p.employeeNumber ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Специализация</label>
                    <input value="${p.specialization ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Статус</label>
                    <input value="${p.status ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Зона обслуживания</label>
                    <input value="${p.zoneId ?? "Не назначена"}" readonly>
                </div>

            </div>

        </div>
    `;
}

/* ---------- DISPATCHER ---------- */

function renderDispatcher(p) {

    view.innerHTML = `
        <div class="panel">

            <h2>Профиль диспетчера</h2>

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
                    <label>Табельный номер</label>
                    <input value="${p.employeeNumber ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Подразделение</label>
                    <input value="${p.department ?? ""}" readonly>
                </div>

            </div>

        </div>
    `;
}

/* ---------- ADMIN ---------- */

function renderAdmin(p) {

    view.innerHTML = `
        <div class="panel">

            <h2>Профиль администратора</h2>

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
                    <label>Табельный номер</label>
                    <input value="${p.employeeNumber ?? ""}" readonly>
                </div>

                <div class="field">
                    <label>Должность</label>
                    <input value="${p.position ?? ""}" readonly>
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