(function () {
    const STORAGE_KEY = "dallasCollegeBookstoreCart";
    const API_URL = "http://localhost:8080/api/items";
    let temporaryCart = [];
    let catalogItems = [];

    function readCart() {
        try {
            const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
            if (!Array.isArray(saved)) return [];

            return saved
                .filter(line => Number.isInteger(Number(line.id)) && Number(line.id) > 0)
                .map(line => ({
                    id: Number(line.id),
                    quantity: Math.max(1, Math.min(99, Number.parseInt(line.quantity, 10) || 1))
                }));
        } catch (error) {
            return temporaryCart;
        }
    }

    function updateCartCount(cart) {
        const count = cart.reduce((total, line) => total + line.quantity, 0);
        document.querySelectorAll("[data-cart-count]").forEach(element => {
            element.textContent = String(count);
        });
    }

    function saveCart(cart) {
        temporaryCart = cart;
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
        } catch (error) {
            // Keep the cart available for this page if browser storage is disabled.
        }
        updateCartCount(cart);
    }

    function addItem(itemId) {
        const id = Number(itemId);
        if (!Number.isInteger(id) || id <= 0) return false;

        const cart = readCart();
        const existing = cart.find(line => line.id === id);
        if (existing) {
            existing.quantity = Math.min(99, existing.quantity + 1);
        } else {
            cart.push({ id, quantity: 1 });
        }
        saveCart(cart);
        return true;
    }

    function changeQuantity(itemId, amount) {
        const id = Number(itemId);
        const cart = readCart();
        const line = cart.find(entry => entry.id === id);
        if (!line) return;

        line.quantity = Math.max(1, Math.min(99, line.quantity + amount));
        saveCart(cart);
    }

    function removeItem(itemId) {
        const id = Number(itemId);
        saveCart(readCart().filter(line => line.id !== id));
    }

    function formatMoney(value) {
        return "$" + value.toFixed(2);
    }

    function showFeedback(message, isError) {
        const feedback = document.getElementById("cart-feedback");
        if (!feedback) return;
        feedback.textContent = message;
        feedback.classList.toggle("is-error", Boolean(isError));
    }

    function createQuantityButton(label, itemId, change, description) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "quantity-button";
        button.textContent = label;
        button.setAttribute("aria-label", description);
        button.addEventListener("click", () => {
            changeQuantity(itemId, change);
            renderCart(catalogItems);
        });
        return button;
    }

    function renderCart(items) {
        const container = document.getElementById("cart-items");
        if (!container) return;

        const cart = readCart();
        const itemById = new Map(items.map(item => [Number(item.id), item]));
        let subtotal = 0;
        container.replaceChildren();

        if (cart.length === 0) {
            const empty = document.createElement("p");
            empty.className = "cart-empty";
            empty.textContent = "Your cart is empty. Add an item from the catalog or enter an item code above.";
            container.appendChild(empty);
        }

        cart.forEach(line => {
            const item = itemById.get(line.id);
            const row = document.createElement("article");
            row.className = "cart-line";

            const details = document.createElement("div");
            details.className = "cart-line-details";
            const name = document.createElement("h3");
            name.textContent = item ? item.description : "Item no longer in catalog";
            const code = document.createElement("p");
            code.textContent = "Item code: " + line.id;
            details.append(name, code);

            const price = document.createElement("p");
            price.className = "cart-line-price";
            if (item) {
                const unitPrice = Number(item.price) || 0;
                subtotal += unitPrice * line.quantity;
                price.textContent = formatMoney(unitPrice) + " each";
            } else {
                price.textContent = "Unavailable";
            }

            const controls = document.createElement("div");
            controls.className = "quantity-controls";
            controls.appendChild(createQuantityButton("−", line.id, -1, "Decrease quantity of item " + line.id));
            const quantity = document.createElement("span");
            quantity.className = "quantity-value";
            quantity.textContent = String(line.quantity);
            quantity.setAttribute("aria-label", "Quantity " + line.quantity);
            controls.append(quantity);
            controls.appendChild(createQuantityButton("+", line.id, 1, "Increase quantity of item " + line.id));

            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "remove-item-button";
            remove.textContent = "Remove";
            remove.addEventListener("click", () => {
                removeItem(line.id);
                renderCart(catalogItems);
                showFeedback("Item removed from your cart.", false);
            });

            const lineTotal = document.createElement("strong");
            lineTotal.className = "cart-line-total";
            lineTotal.textContent = item
                ? formatMoney((Number(item.price) || 0) * line.quantity)
                : "";

            row.append(details, price, controls, lineTotal, remove);
            container.appendChild(row);
        });

        const subtotalElement = document.getElementById("cart-subtotal");
        const totalElement = document.getElementById("cart-total");
        if (subtotalElement) subtotalElement.textContent = formatMoney(subtotal);
        if (totalElement) totalElement.textContent = formatMoney(subtotal);
        updateCartCount(cart);
    }

    async function loadCartPage() {
        const container = document.getElementById("cart-items");
        if (!container) return;

        const form = document.getElementById("cart-item-code-form");
        if (form) {
            form.addEventListener("submit", event => {
                event.preventDefault();
                const input = document.getElementById("cart-item-code");
                const code = input.value.trim();

                if (!/^\d+$/.test(code)) {
                    showFeedback("Enter a valid numeric item code.", true);
                    input.focus();
                    return;
                }

                const item = catalogItems.find(product => Number(product.id) === Number(code));
                if (!item) {
                    showFeedback("No catalog item has code " + code + ". Check the code and try again.", true);
                    input.focus();
                    return;
                }

                addItem(item.id);
                renderCart(catalogItems);
                showFeedback(item.description + " added to your cart.", false);
                input.value = "";
                input.focus();
            });
        }

        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error("Catalog request failed");
            catalogItems = await response.json();
            renderCart(catalogItems);
        } catch (error) {
            container.textContent = "Unable to load item details. Make sure the Java catalog server is running, then refresh this page.";
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        updateCartCount(readCart());
        loadCartPage();
    });

    window.BookstoreCart = { addItem };
})();



