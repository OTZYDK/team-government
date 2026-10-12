// Dallas College Bookstore App

const API_URL = "http://localhost:8080/api/items";

// Load catalog when the page opens
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();

    document
        .getElementById("item-form")
        .addEventListener("submit", addItem);

    document
        .getElementById("update-item")
        .addEventListener("click", updateItem);

    document
        .getElementById("delete-item")
        .addEventListener("click", deleteItem);
});

// Get all items from the Java backend
function fetchItems() {
    fetch(API_URL)
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to load items.");
            }

            return response.json();
        })
        .then(items => {
            displayItems(items);
        })
        .catch(error => {
            console.error("Error fetching items:", error);
<<<<<<< HEAD

            document.getElementById("store-container").innerHTML =
                "<p>Unable to load bookstore items. Please try again.</p>";
        });
}

// Display catalog items
function displayItems(items) {
    const container =
        document.getElementById("store-container");

    container.innerHTML = "";

    items.forEach(item => {

        const tagsHtml = item.tags
            .map(tag => `<span class="tag">${tag}</span>`)
            .join(" ");

        const card = `
            <div class="item-card">

                <span class="item-category">
                    ${item.category}
                </span>

                <h3>${item.description}</h3>

                <p class="price">
                    $${item.price.toFixed(2)}
                </p>

                <div class="tags">
                    ${tagsHtml}
                </div>

                <p>Item ID: ${item.id}</p>

            </div>
        `;

=======
            document.getElementById("store-container").innerHTML =
                "<p>Could not load items. Is the Java server running?</p>";
        });
}

// Turn the list of items into HTML cards inside #store-container
function displayItems(items) {
    const container = document.getElementById("store-container");
    container.innerHTML = "";

    items.forEach(item => {
        const tagsHtml = item.tags
            .map(tag => `<span class="tag">${tag}</span>`)
            .join("");

        const card = `
            <div class="item-card">
                <span class="item-category">${item.category}</span>
                <h3>${item.description}</h3>
                <p class="price">$${item.price.toFixed(2)}</p>
                <div class="tags">${tagsHtml}</div>
            </div>
        `;
>>>>>>> cbbd191fa0cb34f7133f7ac54cf4d6c49ef5c86b
        container.innerHTML += card;
    });
}

<<<<<<< HEAD
// Read item information from the manager form
function getFormItem() {

    const id =
        Number(document.getElementById("item-id").value);

    const price =
        Number(document.getElementById("item-price").value);

    const description =
        document.getElementById("item-description").value.trim();

    const category =
        document.getElementById("item-category").value;

    const tags =
        document
            .getElementById("item-tags")
            .value
            .split(",")
            .map(tag => tag.trim())
            .filter(tag => tag !== "");

    return {
        id,
        price,
        description,
        category,
        tags
    };
}

// Add a new item
function addItem(event) {

    event.preventDefault();

    const item = getFormItem();

    fetch(API_URL, {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(item)
    })
        .then(response => {

            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.error);
                });
            }

            return response.json();
        })
        .then(newItem => {

            alert(
                "Item added successfully: " +
                newItem.description
            );

            document
                .getElementById("item-form")
                .reset();

            fetchItems();
        })
        .catch(error => {

            console.error("Error adding item:", error);

            alert(
                "Unable to add item: " +
                error.message
            );
        });
}

// Update an existing item
function updateItem() {

    const item = getFormItem();

    fetch(`${API_URL}/${item.id}`, {
        method: "PUT",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(item)
    })
        .then(response => {

            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.error);
                });
            }

            return response.json();
        })
        .then(updatedItem => {

            alert(
                "Item updated successfully: " +
                updatedItem.description
            );

            document
                .getElementById("item-form")
                .reset();

            fetchItems();
        })
        .catch(error => {

            console.error("Error updating item:", error);

            alert(
                "Unable to update item: " +
                error.message
            );
        });
}

// Delete an existing item
function deleteItem() {

    const id =
        Number(document.getElementById("item-id").value);

    if (!id) {
        alert("Please enter an Item ID.");
        return;
    }

    const confirmed =
        confirm(
            "Are you sure you want to delete item " +
            id +
            "?"
        );

    if (!confirmed) {
        return;
    }

    fetch(`${API_URL}/${id}`, {
        method: "DELETE"
    })
        .then(response => {

            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.error);
                });
            }

            return response.json();
        })
        .then(deletedItem => {

            alert(
                "Item deleted successfully: " +
                deletedItem.description
            );

            document
                .getElementById("item-form")
                .reset();

            fetchItems();
        })
        .catch(error => {

            console.error("Error deleting item:", error);

            alert(
                "Unable to delete item: " +
                error.message
            );
        });
}
=======
// ---------- ADD ITEM FORM ----------

// Attach a submit handler to the Manager Catalog form
function attachFormListener() {
    const form = document.getElementById("addItemForm");
    if (!form) {
        console.error("Form #addItemForm not found in the page");
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();   // stop the browser from reloading the page

        const formElement = event.target;
        const formData = new FormData(formElement);
        const params = new URLSearchParams(formData);

        try {
            const response = await fetch("http://localhost:8080/api/items", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params.toString()
            });

            if (!response.ok) throw new Error("Server responded with " + response.status);

            document.getElementById("form-message").textContent = "Item added!";
            formElement.reset();

            // Refresh the product grid so the new item appears immediately
            fetchItems();

        } catch (error) {
            console.error("Failed to add item:", error);
            document.getElementById("form-message").textContent = "Failed to add item.";
        }
    });
}
>>>>>>> cbbd191fa0cb34f7133f7ac54cf4d6c49ef5c86b
