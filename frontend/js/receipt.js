(function () {
    const STORAGE_KEY = "dallasCollegeBookstoreCart";
    const API_URL = "/api/items";
    const DEMO_TAX_RATE_PERCENT = 2.9;

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

    function formatMoney(value) {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "USD"
        }).format(value);
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

        const rawTaxRate = item.taxRatePercent ?? item.taxRate ?? item.tax_rate;
        const parsedTaxRate = Number(rawTaxRate);
        const hasItemTaxRate = rawTaxRate !== undefined
            && rawTaxRate !== null
            && rawTaxRate !== ""
            && Number.isFinite(parsedTaxRate)
            && parsedTaxRate >= 0;
        const taxRatePercent = hasItemTaxRate ? parsedTaxRate : DEMO_TAX_RATE_PERCENT;
        return Math.round((lineSubtotal * taxRatePercent / 100 + Number.EPSILON) * 100) / 100;
    }

    function addCell(row, value, className) {
        const cell = document.createElement("td");
        cell.textContent = value;
        if (className) cell.className = className;
        row.appendChild(cell);
    }

    async function renderReceipt() {
        const status = document.getElementById("receipt-status");
        const content = document.getElementById("receipt-content");
        const rows = document.getElementById("receipt-rows");

        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error("Catalog request failed");
            const items = await response.json();
            const itemById = new Map(items.map(item => [Number(item.id), item]));
            const cart = readCart();

            if (cart.length === 0) {
                status.textContent = "Your cart is empty. Add products to see an itemized receipt.";
                return;
            }

            let subtotal = 0;
            let savingsTotal = 0;
            let taxTotal = 0;
            rows.replaceChildren();

            cart.forEach(line => {
                const row = document.createElement("tr");
                const item = itemById.get(line.id);

                if (!item) {
                    addCell(row, "Item no longer in catalog");
                    addCell(row, String(line.id));
                    addCell(row, String(line.quantity));
                    addCell(row, "Unavailable");
                    addCell(row, "—");
                    addCell(row, "—");
                    addCell(row, "—");
                    rows.appendChild(row);
                    return;
                }

                const pricing = window.BookstorePricing.getBreakdown(item, line.quantity);
                const lineTax = getTax(item, pricing.subtotal);
                subtotal += pricing.subtotal;
                savingsTotal += pricing.savings;
                taxTotal += lineTax;

                addCell(row, item.description || "Store item");
                addCell(row, String(line.id));
                addCell(row, String(line.quantity));
                addCell(row, formatMoney(pricing.unitPrice));
                addCell(row, pricing.savings > 0 ? "−" + formatMoney(pricing.savings) : "—", "receipt-savings-cell");
                addCell(row, formatMoney(lineTax));
                addCell(row, formatMoney(pricing.discountedSubtotal + lineTax));
                rows.appendChild(row);
            });

            document.getElementById("receipt-subtotal").textContent = formatMoney(subtotal);
            document.getElementById("receipt-savings").textContent = formatMoney(savingsTotal);
            document.getElementById("receipt-tax").textContent = formatMoney(taxTotal);
            document.getElementById("receipt-total").textContent = formatMoney(Math.max(0, subtotal - savingsTotal) + taxTotal);
            content.hidden = false;
            status.textContent = "";
        } catch (error) {
            status.textContent = "Unable to load receipt details. Make sure the Java catalog server is running, then refresh this page.";
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("print-receipt").addEventListener("click", () => window.print());
        renderReceipt();
    });
})();
