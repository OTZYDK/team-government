// Dallas College Bookstore App
// frontend/js/app.js

// Wait for the DOM to be fully ready, then run our startup code
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();           // load the product grid
    attachFormListener();   // wire up the "add item" form
});

// ---------- PRODUCT GRID ----------

// Fetch all items from the Java backend and render them
function fetchItems() {
    fetch('http://localhost:8080/api/items')
        .then(response => response.json())
        .then(items => {
            displayItems(items);
        })
        .catch(error => {
            console.error("Error fetching items:", error);
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
        container.innerHTML += card;
    });
}

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