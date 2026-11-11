
 // Dallas College Bookstore App

const API_URL = "/api/items";
const TAX_EXEMPTIONS_STORAGE_KEY = "dallasCollegeBookstoreTaxExemptions";

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

    setupTaxExemptionControls();

    const inventoryFilter = document.getElementById("inventory-category-filter");
    if (inventoryFilter) {
        inventoryFilter.addEventListener("change", () => renderInventoryLevels(allItems));
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
        renderInventoryLevels(allItems);
    } catch (error) {
        console.error("Error fetching items:", error);

        const container = document.getElementById("store-container");

        if (container) {
            container.innerHTML =
                "<p>Unable to load items. Make sure the Java server is running.</p>";
        }

        const inventoryCount = document.getElementById("inventory-item-count");
        const inventoryBody = document.getElementById("inventory-table-body");
        if (inventoryCount) inventoryCount.textContent = "Inventory could not be loaded.";
        if (inventoryBody) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = 4;
            cell.textContent = "Unable to load items. Make sure the Java server is running.";
            row.appendChild(cell);
            inventoryBody.replaceChildren(row);
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
        const pricing = window.BookstorePricing.getBreakdown(item);
        let savingsBadge = null;
        let bundleBadge = null;
        if (pricing.savings > 0) {
            const originalPrice = document.createElement("span");
            originalPrice.className = "original-price";
            originalPrice.textContent = window.BookstorePricing.formatMoney(pricing.unitPrice);

            const salePrice = document.createElement("strong");
            salePrice.className = "sale-price";
            salePrice.textContent = window.BookstorePricing.formatMoney(pricing.discountedUnitPrice);
            price.append(originalPrice, salePrice);

            savingsBadge = document.createElement("span");
            savingsBadge.className = "discount-savings-badge";
            savingsBadge.textContent = "Save " + window.BookstorePricing.formatMoney(pricing.savings);
        } else {
            price.textContent = window.BookstorePricing.formatMoney(pricing.unitPrice);
        }

        if (pricing.hasBundleOffer) {
            bundleBadge = document.createElement("span");
            bundleBadge.className = "multi-buy-badge";
            bundleBadge.textContent = `${pricing.bundleQuantity} for ${window.BookstorePricing.formatMoney(pricing.bundlePrice)}`;
        }

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
        card.append(category, description, price);
        if (savingsBadge) card.appendChild(savingsBadge);
        if (bundleBadge) card.appendChild(bundleBadge);
        card.append(tagsContainer, id);

        if (document.getElementById("item-form")) {
            card.appendChild(createItemTaxToggle(item));
        }

        // The manager catalog is for editing; customer cards can be added to the cart.
        if (!document.getElementById("item-form")) {
            const addButton = document.createElement("button");
            addButton.type = "button";
            addButton.className = "add-to-cart-button";
            addButton.textContent = "Add to Cart";
            addButton.addEventListener("click", () => {
                const added = window.BookstoreCart && window.BookstoreCart.addItem(item.id);
                if (added) {
                    addButton.textContent = "Added to Cart";
                    addButton.disabled = true;
                    window.setTimeout(() => {
                        addButton.textContent = "Add to Cart";
                        addButton.disabled = false;
                    }, 900);
                }
            });
            card.appendChild(addButton);
        }

        container.appendChild(card);
    });
}

// Show every catalog item and filter the inventory table by category.
function renderInventoryLevels(items) {
    const tableBody = document.getElementById("inventory-table-body");
    if (!tableBody) return;

    const categoryFilter = document.getElementById("inventory-category-filter");
    const selectedCategory = categoryFilter ? categoryFilter.value.toUpperCase() : "";
    const filteredItems = items.filter(item =>
        !selectedCategory || String(item.category || "").trim().toUpperCase() === selectedCategory
    );
    const missingStockCount = filteredItems.filter(item => {
        const stock = item.supply ?? item.Supply;
        return stock === undefined || stock === null || stock === "" || !Number.isFinite(Number(stock)) || Number(stock) < 0;
    }).length;

    const count = document.getElementById("inventory-item-count");
    if (count) {
        count.textContent = missingStockCount > 0
            ? `Showing ${filteredItems.length} of ${items.length} products. Stock counts are missing for ${missingStockCount}.`
            : `Showing ${filteredItems.length} of ${items.length} products.`;
    }

    tableBody.replaceChildren();
    if (filteredItems.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 4;
        cell.textContent = "No products in this category.";
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }

    filteredItems.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.description || "Unnamed product",
            String(item.id ?? "—"),
            String(item.category || "—")
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        const stockCell = document.createElement("td");
        const rawStock = item.supply ?? item.Supply;
        const stock = Number(rawStock);
        if (rawStock !== undefined && rawStock !== null && rawStock !== "" && Number.isFinite(stock) && stock >= 0) {
            const stockLabel = document.createElement("span");
            stockLabel.className = "inventory-stock";
            if (stock === 0) {
                stockLabel.classList.add("is-out-of-stock");
                stockLabel.textContent = "Out of stock";
            } else {
                if (stock <= 5) stockLabel.classList.add("is-low-stock");
                stockLabel.textContent = `${stock.toLocaleString("en-US")} in stock`;
            }
            stockCell.appendChild(stockLabel);
        } else {
            stockCell.textContent = "—";
            stockCell.className = "inventory-stock-missing";
        }
        row.appendChild(stockCell);
        tableBody.appendChild(row);
    });
}

function readTaxExemptionSettings() {
    try {
        const saved = JSON.parse(localStorage.getItem(TAX_EXEMPTIONS_STORAGE_KEY) || "{}");
        return {
            categories: saved.categories && typeof saved.categories === "object" ? saved.categories : {},
            items: saved.items && typeof saved.items === "object" ? saved.items : {}
        };
    } catch (error) {
        return { categories: {}, items: {} };
    }
}

function saveTaxExemptionSettings(settings) {
    try {
        localStorage.setItem(TAX_EXEMPTIONS_STORAGE_KEY, JSON.stringify(settings));
        return true;
    } catch (error) {
        return false;
    }
}

function hasTaxExemptionOverride(map, key) {
    return Object.prototype.hasOwnProperty.call(map, key);
}

function isItemTaxExempt(item) {
    const settings = readTaxExemptionSettings();
    const itemId = String(item.id);
    const category = String(item.category || "").trim().toUpperCase();

    if (hasTaxExemptionOverride(settings.items, itemId)) {
        return Boolean(settings.items[itemId]);
    }

    if (hasTaxExemptionOverride(settings.categories, category)) {
        return Boolean(settings.categories[category]);
    }

    const apiValue = item.taxExempt ?? item.tax_exempt;
    return apiValue === true || String(apiValue).toLowerCase() === "true";
}

function showTaxExemptionFeedback(message, isError = false) {
    const feedback = document.getElementById("tax-exemption-feedback");
    if (!feedback) return;
    feedback.textContent = message;
    feedback.classList.toggle("is-error", isError);
}

function createItemTaxToggle(item) {
    const label = document.createElement("label");
    label.className = "tax-toggle item-tax-toggle";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = isItemTaxExempt(item);
    checkbox.setAttribute("aria-label", "Mark " + item.description + " as tax-exempt");

    const track = document.createElement("span");
    track.className = "tax-toggle-track";
    track.setAttribute("aria-hidden", "true");

    const text = document.createElement("span");
    text.className = "tax-toggle-label";
    text.textContent = "Tax-exempt";

    checkbox.addEventListener("change", () => {
        const settings = readTaxExemptionSettings();
        settings.items[String(item.id)] = checkbox.checked;
        if (!saveTaxExemptionSettings(settings)) {
            showTaxExemptionFeedback("Could not save this setting in the browser.", true);
            return;
        }

        showTaxExemptionFeedback(
            item.description + (checkbox.checked ? " is now tax-exempt." : " is no longer tax-exempt.")
        );
        displayItems(allItems);
    });

    label.append(checkbox, track, text);
    return label;
}

function setupTaxExemptionControls() {
    const categorySelect = document.getElementById("tax-category");
    const categoryCheckbox = document.getElementById("category-tax-exempt");
    if (!categorySelect || !categoryCheckbox) return;

    const syncCategoryCheckbox = () => {
        const settings = readTaxExemptionSettings();
        categoryCheckbox.checked = Boolean(settings.categories[categorySelect.value]);
    };

    categorySelect.addEventListener("change", syncCategoryCheckbox);
    categoryCheckbox.addEventListener("change", () => {
        const settings = readTaxExemptionSettings();
        const category = categorySelect.value;
        settings.categories[category] = categoryCheckbox.checked;

        if (!saveTaxExemptionSettings(settings)) {
            showTaxExemptionFeedback("Could not save this setting in the browser.", true);
            return;
        }

        showTaxExemptionFeedback(
            category.charAt(0) + category.slice(1).toLowerCase()
            + (categoryCheckbox.checked ? " items are now tax-exempt by default." : " items are no longer tax-exempt by default.")
        );
        displayItems(allItems);
    });

    syncCategoryCheckbox();
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
