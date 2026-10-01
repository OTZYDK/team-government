// Dallas College Bookstore App 

 

let cart = []; 

let total = 0; 

 

// Add a product to the shopping cart 

function addToCart(name, price) { 

    cart.push({ 

        name: name, 

        price: price 

    }); 

 

    total += price; 

 

    updateCart(); 

 

    alert(name + " has been added to your cart!"); 

} 

 

// Update the cart display 

function updateCart() { 

    const cartList = document.getElementById("cart-list"); 

    const cartTotal = document.getElementById("cart-total"); 

 

    if (!cartList || !cartTotal) { 

        return; 

    } 

 

    cartList.innerHTML = ""; 

 

    cart.forEach(function(item, index) { 

        const li = document.createElement("li"); 

 

        li.innerHTML = ` 

            ${item.name} - $${item.price.toFixed(2)} 

            <button onclick="removeFromCart(${index})">Remove</button> 

        `; 

 

        cartList.appendChild(li); 

    }); 

 

    cartTotal.textContent = "Total: $" + total.toFixed(2); 

} 

 

// Remove an item from the cart 

function removeFromCart(index) { 

    total -= cart[index].price; 

    cart.splice(index, 1); 

 

    updateCart(); 

} 

 

// Clear the entire cart 

function clearCart() { 

    cart = []; 

    total = 0; 

 

    updateCart(); 

} 
