// Dallas College Bookstore App

// frontend/js/app.js
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();
});

// Fetch bookstore items from the backend API
function fetchItems() {
    fetch('http://localhost:8080/api/items')
        .then(response => response.json())
        .then(items => {
            displayItems(items);
        })
        .catch(error => {
            console.error("Error fetching items:", error);
            document.getElementById("store-container").innerHTML =
                "<p>Unable to load bookstore items. Please try again.</p>"
        });
}

// Display bookstore items on the page
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