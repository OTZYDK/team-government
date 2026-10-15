document.getElementById("login-form").addEventListener("submit", function(event) {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const errorMessage = document.getElementById("login-error");

    if (username === "team government" && password === "manager123") {
        window.location.href = "manager.html";
    } else {
        errorMessage.textContent = "Incorrect username or password. Please try again.";
        errorMessage.style.color = "#B93832";
    }
});