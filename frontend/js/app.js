"use strict";

const API_URL = "http://localhost:8080/api/items";
const LOGIN_URL = "http://localhost:8080/api/login";
const LOGOUT_URL = "http://localhost:8080/api/logout";

let sessionToken = sessionStorage.getItem("sessionToken");
let currentRole = sessionStorage.getItem("currentRole");
let currentUsername = sessionStorage.getItem("currentUsername");

document.addEventListener("DOMContentLoaded", initializeApp);

function initializeApp() {
    document.getElementById("login-form")
        .addEventListener("submit", login);

    document.getElementById("logout-button")
        .addEventListener("click", logout);

    document.getElementById("item-form")
        .addEventListener("submit", addItem);

    document.getElementById("update-item")
        .addEventListener("click", updateItem);

    document.getElementById("delete-item")
        .addEventListener("click", deleteItem);

    if (sessionToken && currentRole && currentUsername) {
        showStore();
        fetchItems();
    }
}

async function login(event) {
    event.preventDefault();

    const message = document.getElementById("login-message");
    const button = document.getElementById("login-button");

    message.textContent = "";
    button.disabled = true;

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    try {
        const response = await fetch(LOGIN_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Login failed.");
        }

        if (!data.token || !data.role) {
            throw new Error("The server returned an invalid login response.");
        }

        sessionToken = data.token;
        currentRole = data.role;
        currentUsername = data.username || username;

        sessionStorage.setItem("sessionToken", sessionToken);
        sessionStorage.setItem("currentRole", currentRole);
        sessionStorage.setItem("currentUsername", currentUsername);

        document.getElementById("password").value = "";

        showStore();
        await fetchItems();
    } catch (error) {
        message.textContent =
            `${error.message} Check that the Java server is running.`;
    } finally {
        button.disabled = false;
    }
}

function showStore() {
    document.getElementById("login-section").style.display = "none";
    document.getElementById("app-content").style.display = "block";

    document.getElementById("user-info").textContent =
        `Logged in as ${currentUsername} (${currentRole})`;

    document.getElementById("manager").style.display =
        currentRole === "manager" ? "block" : "none";

    document.getElementById("store-container").textContent =
        "Loading items...";
}

async function logout() {
    try {
        if (sessionToken) {
            await fetch(LOGOUT_URL, {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${sessionToken}`
                }
            });
        }
    } catch (error) {
        console.error("Logout request failed:", error);
    } finally {
        clearSession();
    }
}

function clearSession() {
    sessionToken = null;
    currentRole = null;
    currentUsername = null;

    sessionStorage.removeItem("sessionToken");
    sessionStorage.removeItem("currentRole");
    sessionStorage.removeItem("currentUsername");

    document.getElementById("login-form").reset();
    document.getElementById("login-message").textContent = "";
    document.getElementById("manager-message").textContent = "";

    document.getElementById("login-section").style.display = "block";
    document.getElementById("app-content").style.display = "none";
    document.getElementById("manager").style.display = "none";

    document.getElementById("store-container").textContent =
        "Log in to load the catalog.";
}

async function apiRequest(url, options = {}) {
    if (!sessionToken) {
        throw new Error("Please log in first.");
    }

    const headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${sessionToken}`
    };

    const response = await fetch(url, {
        ...options,
        headers
    });

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (response.status === 401) {
        clearSession();
        throw new Error("Your session expired. Please log in again.");
    }

    if (!response.ok) {
        throw new Error(
            data && typeof data === "object" && data.error
                ? data.error
                : `Request failed (${response.status}).`
        );
    }

    return data;
}

async function fetchItems() {
    const container = document.getElementById("store-container");

    try {
        const items = await apiRequest(API_URL);

        if (!Array.isArray(items)) {
            throw new Error("Unexpected catalog response.");
        }

        displayItems(items);
    } catch (error) {
        console.error("Error fetching items:", error);

        if (sessionToken) {
            container.textContent = `Unable to load items: ${error.message}`;
        }
    }
}

function getField(item, ...names) {
    for (const name of names) {
        if (item[name] !== undefined && item[name] !== null) {
            return item[name];
        }
    }

    const keys = Object.keys(item);

    for (const name of names) {
        const key = keys.find(
            candidate => candidate.toLowerCase() === name.toLowerCase()
        );

        if (key !== undefined && item[key] !== null) {
            return item[key];
        }
    }

    return "";
}

function displayItems(items) {
    const container = document.getElementById("store-container");
    container.replaceChildren();

    if (items.length === 0) {
        container.textContent = "No items are currently available.";
        return;
    }

    for (const item of items) {
        const card = document.createElement("article");
        card.className = "product-card";

        const title = document.createElement("h3");
        title.textContent =
            getField(item, "description", "name", "title") || "Unnamed item";

        const price = document.createElement("p");
        const rawPrice = getField(item, "price");
        const numericPrice = Number(rawPrice);

        price.textContent =
            rawPrice !== "" && Number.isFinite(numericPrice)
                ? `$${numericPrice.toFixed(2)}`
                : "Price unavailable";

        card.append(title, price);

        const category = getField(item, "category");
        if (category) {
            const element = document.createElement("p");
            element.textContent = `Category: ${category}`;
            card.appendChild(element);
        }

        const tags = getField(item, "tags");
        if (tags) {
            const element = document.createElement("p");
            element.textContent = `Tags: ${Array.isArray(tags) ? tags.join(", ") : tags}`;
            card.appendChild(element);
        }

        if (currentRole === "manager") {
            const selectButton = document.createElement("button");
            selectButton.type = "button";
            selectButton.textContent = "Select for Editing";

            selectButton.addEventListener("click", () => {
                document.getElementById("item-id").value =
                    getField(item, "id", "itemId", "item_id");
                document.getElementById("item-price").value =
                    getField(item, "price");
                document.getElementById("item-description").value =
                    getField(item, "description", "name", "title");
                document.getElementById("item-category").value =
                    getField(item, "category");
                document.getElementById("item-tags").value =
                    Array.isArray(tags) ? tags.join(", ") : tags;
                document.getElementById("manager-message").textContent =
                    "Item selected for editing.";
            });

            card.appendChild(selectButton);
        }

        container.appendChild(card);
    }
}

function requireManager() {
    if (currentRole !== "manager") {
        throw new Error("Manager permissions are required.");
    }
}

function getFormItem() {
    return {
        price: Number(document.getElementById("item-price").value),
        description: document.getElementById("item-description").value.trim(),
        category: document.getElementById("item-category").value,
        tags: document.getElementById("item-tags").value
            .split(",")
            .map(tag => tag.trim())
            .filter(Boolean)
    };
}

function validateItem(item) {
    if (
        !item.description ||
        !item.category ||
        !Number.isFinite(item.price) ||
        item.price < 0
    ) {
        throw new Error("Enter a valid price, description, and category.");
    }
}

async function addItem(event) {
    event.preventDefault();

    const message = document.getElementById("manager-message");

    try {
        requireManager();

        const item = getFormItem();
        validateItem(item);

        await apiRequest(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(item)
        });

        message.textContent = "Item added successfully.";
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function updateItem() {
    const message = document.getElementById("manager-message");

    try {
        requireManager();

        const id = document.getElementById("item-id").value.trim();
        if (!id) {
            throw new Error("Select an item or enter its ID first.");
        }

        const item = getFormItem();
        validateItem(item);

        await apiRequest(`${API_URL}/${encodeURIComponent(id)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(item)
        });

        message.textContent = "Item updated successfully.";
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function deleteItem() {
    const message = document.getElementById("manager-message");

    try {
        requireManager();

        const id = document.getElementById("item-id").value.trim();
        if (!id) {
            throw new Error("Select an item or enter its ID first.");
        }

        if (!confirm(`Delete item ${id}?`)) {
            return;
        }

        await apiRequest(`${API_URL}/${encodeURIComponent(id)}`, {
            method: "DELETE"
        });

        message.textContent = "Item deleted successfully.";
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        message.textContent = error.message;
    }
}
