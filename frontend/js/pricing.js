(function () {
    function roundMoney(value) {
        return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
    }

    function numberOrZero(value) {
        const parsed = Number(value);
        return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
    }

    function getBreakdown(item, quantity = 1) {
        const unitPrice = Math.max(0, Number(item.price) || 0);
        const rate = numberOrZero(
            item.discountRatePercent ?? item.discountRate ?? item.DiscountRate
        );
        const dollarOff = numberOrZero(
            item.discountDollarAmount ?? item.discountDollar ?? item.DiscountDollar
        );
        const lineSubtotal = roundMoney(unitPrice * quantity);

        const rawBundleQuantity = item.multiBuyQuantity
            ?? item.multiBuyQty
            ?? item.bundleQuantity
            ?? item.buyQuantity
            ?? item.MultiBuyQuantity;
        const rawBundlePrice = item.multiBuyPrice
            ?? item.bundlePrice
            ?? item.bundleTotal
            ?? item.MultiBuyPrice;
        const bundleQuantity = Number(rawBundleQuantity);
        const bundlePrice = Number(rawBundlePrice);
        const hasBundleOffer = Number.isInteger(bundleQuantity)
            && bundleQuantity > 1
            && rawBundlePrice !== undefined
            && rawBundlePrice !== null
            && rawBundlePrice !== ""
            && Number.isFinite(bundlePrice)
            && bundlePrice >= 0;

        let bundleSubtotal = lineSubtotal;
        let bundleApplied = false;
        let bundleCount = 0;
        let bundleLeftoverCount = 0;
        if (hasBundleOffer && quantity >= bundleQuantity) {
            bundleCount = Math.floor(quantity / bundleQuantity);
            bundleLeftoverCount = quantity % bundleQuantity;
            const offeredTotal = bundleCount * bundlePrice + bundleLeftoverCount * unitPrice;
            bundleSubtotal = Math.min(lineSubtotal, roundMoney(offeredTotal));
            bundleApplied = true;
        }

        const percentSavings = bundleSubtotal * rate / 100;
        const dollarSavings = dollarOff * quantity;
        const standardSavings = Math.min(bundleSubtotal, roundMoney(percentSavings + dollarSavings));
        const discountedSubtotal = Math.max(0, roundMoney(bundleSubtotal - standardSavings));
        const lineSavings = Math.min(lineSubtotal, roundMoney(lineSubtotal - discountedSubtotal));
        const discountedUnitPrice = quantity > 0 ? discountedSubtotal / quantity : unitPrice;

        return {
            unitPrice,
            discountedUnitPrice: roundMoney(discountedUnitPrice),
            subtotal: lineSubtotal,
            savings: lineSavings,
            discountedSubtotal,
            hasBundleOffer,
            bundleApplied,
            bundleQuantity: hasBundleOffer ? bundleQuantity : 0,
            bundlePrice: hasBundleOffer ? roundMoney(bundlePrice) : 0,
            bundleCount,
            bundleLeftoverCount
        };
    }

    function formatMoney(value) {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "USD"
        }).format(value);
    }

    window.BookstorePricing = { getBreakdown, formatMoney, roundMoney };
})();
