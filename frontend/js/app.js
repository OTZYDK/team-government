
 // Dallas College Bookstore App

const API_URL = "http://localhost:8080/api/items";

let allItems = [];
let activeCategory = "";
let activeTag = "";
let searchText = "";

// Set up the page
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();

    // These elements only exist on manager.html
    const form = document.getElementById("item-form");
    const updateButton = document.getElementById("update-item");
    const deleteButton = document.getElementById("delete-item");

    if (form) {
        form.addEventListener("submit", addItem);
    }

    if (updateButton) {
        updateButton.addEventListener("click", updateItem);
    }

    if (deleteButton) {
        deleteButton.addEventListener("click", deleteItem);
    }

    // Search form on the homepage
    const searchForm = document.querySelector(".search-form");

    if (searchForm) {
        searchForm.addEventListener("submit", event => {
            event.preventDefault();

            const searchInput = searchForm.querySelector(
                'input[type="search"]'
            );

            searchText = searchInput.value.trim().toLowerCase();
            displayItems(allItems);
        });
    }

    // Search as the user types
    const searchInput = document.querySelector(
        '.search-form input[type="search"]'
    );

    if (searchInput) {
        searchInput.addEventListener("input", () => {
            searchText = searchInput.value.trim().toLowerCase();
            displayItems(allItems);
        });
    }

    // Category and tag links
    document.querySelectorAll(".category-links a").forEach(link => {
        link.addEventListener("click", event => {
            event.preventDefault();

            const target = link.getAttribute("href")
                .replace("#", "")
                .toLowerCase();

            const categories = [
                "books", "device", "tool", "snack",
                "drink", "stationery", "apparel", "error"
            ];

            if (categories.includes(target)) {
                activeCategory =
                    target === "books" ? "book" : target;
                activeTag = "";
            } else {
                activeTag = target.replace(/-/g, " ");
                activeCategory = "";
            }

            displayItems(allItems);
        });
    });
});

// Get all items from the Java backend
async function fetchItems() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load items.");
        }

        allItems = await response.json();
        displayItems(allItems);
    } catch (error) {
        console.error("Error fetching items:", error);

        const container = document.getElementById("store-container");

        if (container) {
            container.innerHTML =
                "<p>Unable to load items. Make sure the Java server is running.</p>";
        }
    }
}

// Display products and apply search/category filters
function displayItems(items) {
    const container = document.getElementById("store-container");

    if (!container) return;

    container.innerHTML = "";

    const filteredItems = items.filter(item => {
        const description = String(item.description || "").toLowerCase();
        const category = String(item.category || "").toLowerCase();

        const tags = Array.isArray(item.tags)
            ? item.tags.map(tag => String(tag).toLowerCase())
            : [];

        const matchesSearch =
            !searchText ||
            description.includes(searchText) ||
            category.includes(searchText) ||
            tags.some(tag => tag.includes(searchText));

        const matchesCategory =
            !activeCategory || category === activeCategory;

        const matchesTag =
            !activeTag ||
            tags.some(tag => tag.replace(/-/g, " ").includes(activeTag));

        return matchesSearch && matchesCategory && matchesTag;
    });

    if (filteredItems.length === 0) {
        container.innerHTML = "<p>No matching products found.</p>";
        return;
    }

    filteredItems.forEach(item => {
        const card = document.createElement("div");
        card.className = "item-card";

        const category = document.createElement("span");
        category.className = "item-category";
        category.textContent = item.category || "";

        const description = document.createElement("h3");
        description.textContent = item.description || "";

        const price = document.createElement("p");
        price.className = "price";
        price.textContent = "$" + Number(item.price).toFixed(2);

        const tagsContainer = document.createElement("div");
        tagsContainer.className = "tags";

        const tags = Array.isArray(item.tags) ? item.tags : [];

        tags.forEach(tag => {
            const tagElement = document.createElement("span");
            tagElement.className = "tag";
            tagElement.textContent = tag;
            tagsContainer.appendChild(tagElement);
        });

        const id = document.createElement("p");
        id.textContent = "Item ID: " + item.id;

        card.append(category, description, price, tagsContainer, id);
        container.appendChild(card);
    });
}

// Read item information from the manager form
function getFormItem() {
    const id = Number(document.getElementById("item-id").value);
    const price = Number(document.getElementById("item-price").value);
    const description =
        document.getElementById("item-description").value.trim();
    const category =
        document.getElementById("item-category").value;
    const tags = document.getElementById("item-tags").value
        .split(",")
        .map(tag => tag.trim())
        .filter(tag => tag !== "");

    if (!id || price < 0 || !description || !category) {
        throw new Error("Please enter a valid ID, price, description, and category.");
    }

    return { id, price, description, category, tags };
}

// Handle backend errors
async function checkResponse(response) {
    const text = await response.text();
    let data = {};

    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = {};
    }

    if (!response.ok) {
        throw new Error(
            data.error || "Request failed with status " + response.status
        );
    }

    return data;
}

// Add a new item
async function addItem(event) {
    event.preventDefault();

    try {
        const item = getFormItem();

        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(item)
        });

        const newItem = await checkResponse(response);

        alert("Item added successfully: " + newItem.description);
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error adding item:", error);
        alert("Unable to add item: " + error.message);
    }
}

// Update an existing item
async function updateItem() {
    try {
        const item = getFormItem();

        const response = await fetch(`${API_URL}/${item.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(item)
        });

        const updatedItem = await checkResponse(response);

        alert("Item updated successfully: " + updatedItem.description);
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error updating item:", error);
        alert("Unable to update item: " + error.message);
    }
}

// Delete an existing item
async function deleteItem() {
    const id = Number(document.getElementById("item-id").value);

    if (!id) {
        alert("Please enter an Item ID.");
        return;
    }

    if (!confirm("Are you sure you want to delete item " + id + "?")) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        const deletedItem = await checkResponse(response);

        alert("Item deleted successfully: " + deletedItem.description);
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error deleting item:", error);
        alert("Unable to delete item: " + error.message);
    }
}

