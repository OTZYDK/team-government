(function () {
    const STORAGE_KEY = "dallasCollegeBookstoreCart";
    const API_URL = "/api/items";
    // Matches the current backend's default rate until tax values are returned by the API.
    const DEMO_TAX_RATE_PERCENT = 2.9;
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

    function isTaxExempt(item) {
            let settings = { categories: {}, items: {} };
            try {
                const saved = JSON.parse(localStorage.getItem("dallasCollegeBookstoreTaxExemptions") || "{}");
                settings = {
                    categories: saved.categories && typeof saved.categories === "object" ? saved.categories : {},
                    items: saved.items && typeof saved.items === "object" ? saved.items : {}
                };
            } catch (error) {
                // Fall back to tax details returned by the item API.
            }
    
            const itemId = String(item.id);
            const category = String(item.category || "").trim().toUpperCase();
            if (Object.prototype.hasOwnProperty.call(settings.items, itemId)) {
                return Boolean(settings.items[itemId]);
            }
            if (Object.prototype.hasOwnProperty.call(settings.categories, category)) {
                return Boolean(settings.categories[category]);
            }
    
            const apiValue = item.taxExempt ?? item.tax_exempt;
            return apiValue === true || String(apiValue).toLowerCase() === "true";
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
        let savingsTotal = 0;
        let taxTotal = 0;
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
            let discountedLineSubtotal = 0;
            if (item) {
                const pricing = window.BookstorePricing.getBreakdown(item, line.quantity);
                subtotal += pricing.subtotal;
                savingsTotal += pricing.savings;
                discountedLineSubtotal = pricing.discountedSubtotal;

                if (pricing.hasBundleOffer) {
                    const bundleNote = document.createElement("p");
                    bundleNote.className = "multi-buy-note";
                    bundleNote.textContent = pricing.bundleApplied
                        ? `${pricing.bundleQuantity} for ${formatMoney(pricing.bundlePrice)} applied` +
                          (pricing.bundleLeftoverCount > 0
                              ? `; ${pricing.bundleLeftoverCount} at regular price`
                              : "")
                        : `Add ${pricing.bundleQuantity} for ${formatMoney(pricing.bundlePrice)}`;
                    details.appendChild(bundleNote);
                }

                if (pricing.savings > 0) {
                    const originalPrice = document.createElement("span");
                    originalPrice.className = "original-price";
                    originalPrice.textContent = formatMoney(pricing.unitPrice) + " each";

                    const salePrice = document.createElement("strong");
                    salePrice.className = "sale-price";
                    salePrice.textContent = formatMoney(pricing.discountedUnitPrice) + " each";
                    price.append(originalPrice, salePrice);
                } else {
                    price.textContent = formatMoney(pricing.unitPrice) + " each";
                }

                const unitPrice = pricing.unitPrice;
                
                const taxExempt = isTaxExempt(item);
                const rawTaxRate = item.taxRatePercent ?? item.taxRate ?? item.tax_rate;
                const parsedTaxRate = Number(rawTaxRate);
                const hasItemTaxRate = rawTaxRate !== undefined
                    && rawTaxRate !== null
                    && rawTaxRate !== ""
                    && Number.isFinite(parsedTaxRate)
                    && parsedTaxRate >= 0;

                if (!taxExempt) {
                    const taxRatePercent = hasItemTaxRate ? parsedTaxRate : DEMO_TAX_RATE_PERCENT;
                    const lineTax = unitPrice * line.quantity * taxRatePercent / 100;
                    taxTotal += Math.round((lineTax + Number.EPSILON) * 100) / 100;
                }
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
                ? formatMoney(discountedLineSubtotal)
                : "";

            row.append(details, price, controls, lineTotal, remove);
            container.appendChild(row);
        });

        const subtotalElement = document.getElementById("cart-subtotal");
        const savingsElement = document.getElementById("cart-savings");
        const totalElement = document.getElementById("cart-total");
        const taxElement = document.getElementById("cart-tax");
        if (subtotalElement) subtotalElement.textContent = formatMoney(subtotal);
        if (savingsElement) savingsElement.textContent = formatMoney(savingsTotal);
        if (taxElement) taxElement.textContent = formatMoney(taxTotal);
        if (totalElement) totalElement.textContent = formatMoney(Math.max(0, subtotal - savingsTotal) + taxTotal);
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



