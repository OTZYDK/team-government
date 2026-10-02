// frontend/js/app.js
document.addEventListener("DOMContentLoaded", () => {
    fetchItems();
});

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
