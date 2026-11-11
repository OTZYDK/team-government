(function () {
    const STORAGE_KEY = "dallasCollegeBookstoreCart";
    const API_URL = "http://localhost:8080/api/items";
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

    function getTax(item, lineSubtotal) {
        const rawTaxExempt = item.taxExempt ?? item.tax_exempt;
        const taxExempt = rawTaxExempt === true || String(rawTaxExempt).toLowerCase() === "true";
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
                    rows.appendChild(row);
                    return;
                }

                const unitPrice = Number(item.price) || 0;
                const lineSubtotal = unitPrice * line.quantity;
                const lineTax = getTax(item, lineSubtotal);
                subtotal += lineSubtotal;
                taxTotal += lineTax;

                addCell(row, item.description || "Store item");
                addCell(row, String(line.id));
                addCell(row, String(line.quantity));
                addCell(row, formatMoney(unitPrice));
                addCell(row, formatMoney(lineTax));
                addCell(row, formatMoney(lineSubtotal + lineTax));
                rows.appendChild(row);
            });

            document.getElementById("receipt-subtotal").textContent = formatMoney(subtotal);
            document.getElementById("receipt-tax").textContent = formatMoney(taxTotal);
            document.getElementById("receipt-total").textContent = formatMoney(subtotal + taxTotal);
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
