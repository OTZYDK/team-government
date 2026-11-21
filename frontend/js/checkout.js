(function () {
    const STORAGE_KEY = "dallasCollegeBookstoreCart";
    const API_URL = "/api/items";
    const DEMO_TAX_RATE_PERCENT = 2.9;
    let orderTotal = 0;
    let cartIsEmpty = true;

    function money(value) {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "USD"
        }).format(value);
    }

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
            return [];
        }
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

    function getTax(item, lineSubtotal) {
        const taxExempt = isTaxExempt(item);
        if (taxExempt) return 0;

        const rawRate = item.taxRatePercent ?? item.taxRate ?? item.tax_rate;
        const rate = Number(rawRate);
        const hasRate = rawRate !== undefined && rawRate !== null && rawRate !== ""
            && Number.isFinite(rate) && rate >= 0;
        const ratePercent = hasRate ? rate : DEMO_TAX_RATE_PERCENT;
        return Math.round((lineSubtotal * ratePercent / 100 + Number.EPSILON) * 100) / 100;
    }

    function addSummaryLine(container, item, quantity) {
        const row = document.createElement("div");
        row.className = "checkout-item-row";

        const text = document.createElement("div");
        const name = document.createElement("strong");
        name.textContent = item ? item.description : "Unavailable item";
        const quantityText = document.createElement("span");
        quantityText.textContent = "Qty " + quantity;
        text.append(name, quantityText);

        if (item) {
            const pricing = window.BookstorePricing.getBreakdown(item, quantity);
            if (pricing.hasBundleOffer) {
                const bundleNote = document.createElement("span");
                bundleNote.className = "multi-buy-note";
                bundleNote.textContent = pricing.bundleApplied
                    ? `${pricing.bundleQuantity} for ${money(pricing.bundlePrice)} applied` +
                      (pricing.bundleLeftoverCount > 0
                          ? `; ${pricing.bundleLeftoverCount} at regular price`
                          : "")
                    : `Add ${pricing.bundleQuantity} for ${money(pricing.bundlePrice)}`;
                text.appendChild(bundleNote);
            }
        }

        const linePrice = document.createElement("div");
        linePrice.className = "checkout-line-prices";
        if (item) {
            const pricing = window.BookstorePricing.getBreakdown(item, quantity);
            if (pricing.savings > 0) {
                const original = document.createElement("span");
                original.className = "original-price";
                original.textContent = money(pricing.subtotal);
                const discounted = document.createElement("strong");
                discounted.className = "sale-price";
                discounted.textContent = money(pricing.discountedSubtotal);
                linePrice.append(original, discounted);
            } else {
                const regular = document.createElement("strong");
                regular.textContent = money(pricing.subtotal);
                linePrice.appendChild(regular);
            }
        } else {
            linePrice.textContent = "—";
        }
        row.append(text, linePrice);
        container.appendChild(row);
    }

    async function loadSummary() {
        const container = document.getElementById("checkout-items");
        const button = document.getElementById("place-order");
        const cart = readCart();
        const count = cart.reduce((total, line) => total + line.quantity, 0);
        document.querySelectorAll("[data-cart-count]").forEach(node => {
            node.textContent = String(count);
        });

        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error("Could not load the catalog");
            const items = await response.json();
            const itemById = new Map(items.map(item => [Number(item.id), item]));
            let subtotal = 0;
            let savingsTotal = 0;
            let taxTotal = 0;
            container.replaceChildren();

            if (cart.length === 0) {
                const empty = document.createElement("p");
                empty.className = "checkout-empty";
                empty.textContent = "Your cart is empty. Add something from the bookstore to continue.";
                container.appendChild(empty);
                button.disabled = true;
            } else {
                cartIsEmpty = false;
                cart.forEach(line => {
                    const item = itemById.get(line.id);
                    addSummaryLine(container, item, line.quantity);
                    if (!item) return;

                    const pricing = window.BookstorePricing.getBreakdown(item, line.quantity);
                    subtotal += pricing.subtotal;
                    savingsTotal += pricing.savings;
                    taxTotal += getTax(item, pricing.subtotal);
                });
            }

            orderTotal = Math.max(0, subtotal - savingsTotal) + taxTotal;
            document.getElementById("checkout-subtotal").textContent = money(subtotal);
            document.getElementById("checkout-savings").textContent = money(savingsTotal);
            document.getElementById("checkout-tax").textContent = money(taxTotal);
            document.getElementById("checkout-total").textContent = money(orderTotal);
            updateCashChangePreview();
        } catch (error) {
            container.textContent = "Unable to load your order. Return to the cart and refresh the store.";
            button.disabled = true;
        }
    }

    function setPaymentMethod(method) {
        const cashFields = document.getElementById("cash-fields");
        const cardFields = document.getElementById("card-fields");
        const cashSelected = method === "cash";

        cashFields.hidden = !cashSelected;
        cardFields.hidden = cashSelected;
        cashFields.querySelectorAll("input").forEach(input => {
            input.disabled = !cashSelected;
            input.required = cashSelected;
        });
        cardFields.querySelectorAll("input").forEach(input => {
            input.disabled = cashSelected;
            input.required = !cashSelected;
        });
    }

    function showFeedback(message, isError) {
        const feedback = document.getElementById("checkout-feedback");
        feedback.textContent = message;
        feedback.classList.toggle("is-error", Boolean(isError));
    }

    function updateCashChangePreview() {
        const input = document.getElementById("cash-received");
        const preview = document.getElementById("cash-change-preview");
        if (!input || !preview) return;

        preview.classList.remove("is-error", "is-success");
        if (!input.value.trim()) {
            preview.textContent = "Enter cash received to calculate change.";
            return;
        }

        const received = Number(input.value);
        if (!Number.isFinite(received) || received < 0) {
            preview.textContent = "Enter a valid cash amount.";
            preview.classList.add("is-error");
            return;
        }
        if (cartIsEmpty) {
            preview.textContent = "Add items to your cart to calculate change.";
            return;
        }

        const differenceInCents = Math.round((received - orderTotal + Number.EPSILON) * 100);
        if (differenceInCents < 0) {
            preview.textContent = "Amount due: " + money(Math.abs(differenceInCents) / 100) + " more.";
            preview.classList.add("is-error");
        } else if (differenceInCents === 0) {
            preview.textContent = "Exact amount received. No change due.";
            preview.classList.add("is-success");
        } else {
            preview.textContent = "Estimated change: " + money(differenceInCents / 100);
            preview.classList.add("is-success");
        }
    }

    function submitCheckout(event) {
        event.preventDefault();
        if (cartIsEmpty) {
            showFeedback("Add an item to your cart before checking out.", true);
            return;
        }

        const method = document.querySelector('input[name="paymentMethod"]:checked').value;
        if (method === "cash") {
            const received = Number(document.getElementById("cash-received").value);
            if (!Number.isFinite(received) || received < orderTotal) {
                showFeedback("Enter cash equal to or greater than the order total of " + money(orderTotal) + ".", true);
                return;
            }
        } else {
            const digits = document.getElementById("card-number").value.replace(/[ -]/g, "");
            if (!/^4\d{15}$/.test(digits)) {
                showFeedback("Enter a 16-digit Visa number starting with 4.", true);
                return;
            }
        }

        const studentId = document.getElementById("student-id").value.trim();
        showFeedback(
            studentId
                ? "Payment has not been processed. Student eligibility and savings cannot be checked until checkout is connected; your cart is unchanged."
                : "Payment has not been processed. Checkout is not connected yet, and your cart is unchanged.",
            true
        );
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll('input[name="paymentMethod"]').forEach(input => {
            input.addEventListener("change", () => setPaymentMethod(input.value));
        });
        document.getElementById("cash-received").addEventListener("input", updateCashChangePreview);
        document.getElementById("apply-student-discount").addEventListener("click", () => {
            const studentId = document.getElementById("student-id").value.trim();
            const feedback = document.getElementById("student-discount-feedback");

            if (!studentId) {
                feedback.textContent = "Scan or enter your student ID first.";
                return;
            }

            feedback.textContent = "Student discount eligibility cannot be checked until the checkout service is connected.";
        });
        document.getElementById("checkout-form").addEventListener("submit", submitCheckout);
        setPaymentMethod("cash");
        loadSummary();
    });
})();
