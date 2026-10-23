// Dallas College Bookstore App


const API_URL = "http://localhost:8080/api/items";

let allItems = [];
let selectedCategory = "";
let selectedTag = "";
let searchText = "";

// Start the app when the page loads
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();

    // Manager dashboard controls only exist on manager.html
    const itemForm = document.getElementById("item-form");
    const updateButton = document.getElementById("update-item");
    const deleteButton = document.getElementById("delete-item");

    if (itemForm) {
        itemForm.addEventListener("submit", addItem);
    }

    if (updateButton) {
        updateButton.addEventListener("click", updateItem);
    }

    if (deleteButton) {
        deleteButton.addEventListener("click", deleteItem);
    }

    // Customer homepage search
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

        const searchInput = searchForm.querySelector(
            'input[type="search"]'
        );

        if (searchInput) {
            searchInput.addEventListener("input", () => {
                searchText = searchInput.value.trim().toLowerCase();
                displayItems(allItems);
            });
        }
    }

    // Category and tag links
    document.querySelectorAll(".category-links a").forEach(link => {
        link.addEventListener("click", event => {
            event.preventDefault();

            const value = link.getAttribute("href").replace("#", "");

            if (value.includes("-supplies")) {
                selectedTag = value.replaceAll("-", " ");
                selectedCategory = "";
            } else {
                selectedCategory = value.toUpperCase();
                selectedTag = "";
            }

            displayItems(allItems);
        });
    });
});

// Load items from the Java backend
async function fetchItems() {
    const container = document.getElementById("store-container");

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Could not load items.");
        }

        allItems = await response.json();
        displayItems(allItems);
    } catch (error) {
        console.error("Error loading items:", error);

        if (container) {
            container.textContent =
                "Could not load items. Make sure the Java server is running.";
        }
    }
}

// Show the items that match the current search and filters
function displayItems(items) {
    const container = document.getElementById("store-container");

    // Some pages may not have a product list
    if (!container) {
        return;
    }

    container.innerHTML = "";

    const filteredItems = items.filter(item => {
        const description = (item.description || "").toLowerCase();
        const category = (item.category || "").toLowerCase();
        const tags = Array.isArray(item.tags)
            ? item.tags.map(tag => String(tag).toLowerCase())
            : [];

        const matchesSearch =
            description.includes(searchText) ||
            category.includes(searchText) ||
            tags.some(tag => tag.includes(searchText));

        const matchesCategory =
            !selectedCategory ||
            category === selectedCategory.toLowerCase();

        const matchesTag =
            !selectedTag ||
            tags.some(tag => tag.includes(selectedTag));

        return matchesSearch && matchesCategory && matchesTag;
    });

    if (filteredItems.length === 0) {
        container.textContent = "No matching items found.";
        return;
    }

    filteredItems.forEach(item => {
        const card = document.createElement("div");
        card.className = "item-card";

        const category = document.createElement("span");
        category.className = "item-category";
        category.textContent = item.category || "";

        const title = document.createElement("h3");
        title.textContent = item.description || "";

        const price = document.createElement("p");
        price.className = "price";
        price.textContent = `$${Number(item.price).toFixed(2)}`;

        const tagsContainer = document.createElement("div");
        tagsContainer.className = "tags";

        if (Array.isArray(item.tags)) {
            item.tags.forEach(tag => {
                const tagElement = document.createElement("span");
                tagElement.className = "tag";
                tagElement.textContent = tag;
                tagsContainer.appendChild(tagElement);
            });
        }

        card.appendChild(category);
        card.appendChild(title);
        card.appendChild(price);
        card.appendChild(tagsContainer);

        // Show the item ID on the manager dashboard
        if (document.getElementById("item-form")) {
            const id = document.createElement("p");
            id.textContent = `Item ID: ${item.id}`;
            card.appendChild(id);
        }

        container.appendChild(card);
    });
}

// Read values from the manager form
function getFormItem() {
    const id = Number(document.getElementById("item-id").value);
    const price = Number(document.getElementById("item-price").value);
    const description =
        document.getElementById("item-description").value.trim();
    const category = document.getElementById("item-category").value;
    const tags = document.getElementById("item-tags").value
        .split(",")
        .map(tag => tag.trim())
        .filter(tag => tag !== "");

    if (
        !Number.isInteger(id) ||
        id <= 0 ||
        !Number.isFinite(price) ||
        price < 0 ||
        !description ||
        !category
    ) {
        throw new Error("Enter a valid ID, price, description, and category.");
    }

    return { id, price, description, category, tags };
}

// Handle API responses
async function checkResponse(response) {
    if (!response.ok) {
        let message = "The request failed.";

        try {
            const error = await response.json();
            message = error.error || message;
        } catch {
            // Use the default message if the response is not JSON
        }

        throw new Error(message);
    }

    return response.json();
}

// Add an item
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

        alert(`Item added successfully: ${newItem.description}`);
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error adding item:", error);
        alert(`Unable to add item: ${error.message}`);
    }
}

// Update an item using its ID
async function updateItem() {
    try {
        const item = getFormItem();

        const response = await fetch(`${API_URL}/${item.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(item)
        });

        const updatedItem = await checkResponse(response);

        alert(`Item updated successfully: ${updatedItem.description}`);
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error updating item:", error);
        alert(`Unable to update item: ${error.message}`);
    }
}

// Delete an item using its ID
async function deleteItem() {
    const id = Number(document.getElementById("item-id").value);

    if (!Number.isInteger(id) || id <= 0) {
        alert("Enter the ID of the item you want to delete.");
        return;
    }

    if (!confirm(`Are you sure you want to delete item ${id}?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        await checkResponse(response);

        alert("Item deleted successfully.");
        document.getElementById("item-form").reset();
        await fetchItems();
    } catch (error) {
        console.error("Error deleting item:", error);
        alert(`Unable to delete item: ${error.message}`);
    }
}
