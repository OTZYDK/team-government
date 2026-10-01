// Dallas College Bookstore Shopping Cart

let cart = [];

// Add a product to the cart
function addToCart(productName, productPrice) {

    cart.push({
        name: productName,
        price: productPrice
    });

    alert(productName + " has been added to your cart!");

    updateCart();
}

// Update the shopping cart display
function updateCart() {

    const cartSection = document.getElementById("cart");

    let total = 0;

    let cartHTML = `
        <h2>Shopping Cart</h2>
    `;

    if (cart.length === 0) {

        cartHTML += `
            <p>Your cart is currently empty.</p>
        `;

    } else {

        cartHTML += `<ul>`;

        cart.forEach(function(item) {

            cartHTML += `
                <li>${item.name} - $${item.price.toFixed(2)}</li>
            `;

            total += item.price;
        });

        cartHTML += `</ul>`;

        cartHTML += `
            <p><strong>Total: $${total.toFixed(2)}</strong></p>
            <button onclick="clearCart()">Clear Cart</button>
        `;
    }

    cartSection.innerHTML = cartHTML;
}

// Clear the shopping cart
function clearCart() {

    cart = [];

    updateCart();
}